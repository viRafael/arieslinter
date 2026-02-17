package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@StatelessCheck
public class GeneralFixtureCheck extends AbstractTestSmellCheck {
    // Campos inicializados no setUp por classe
    private final Map<String, Set<String>> setupFields = new HashMap<>();

    // Campos usados por cada teste: classe -> (método teste -> campos usados)
    private final Map<String, Map<String, Set<String>>> testFieldUsages = new HashMap<>();

    // Armazena a linha do método setUp por classe
    private final Map<String, Integer> setupLines = new HashMap<>();

    private String currentClass;

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.CLASS_DEF };
    }

    @Override
    public int[] getRequiredTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        setupFields.clear();
        testFieldUsages.clear();
        setupLines.clear();
        currentClass = null;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (!isTestClass(ast)) {
            return;
        }

        currentClass = getClassName(ast);
        setupFields.put(currentClass, new HashSet<>());
        testFieldUsages.put(currentClass, new HashMap<>());
        setupLines.put(currentClass, 0);

        analyzeTestClass(ast);
        reportGeneralFixtures();
    }

    /**
     * Verifica se é uma classe de teste.
     */
    private boolean isTestClass(DetailAST classDefAst) {
        String className = getClassName(classDefAst);
        if (className != null &&
                (className.endsWith("Test") || className.endsWith("Tests"))) {
            return true;
        }
        return hasTestMethods(classDefAst);
    }

    /**
     * Obtém o nome da classe.
     */
    private String getClassName(DetailAST classDefAst) {
        DetailAST identNode = classDefAst.findFirstToken(TokenTypes.IDENT);
        return identNode != null ? identNode.getText() : null;
    }

    /**
        // Reporta problemas
     * Verifica se tem métodos @Test.
     */
    private boolean hasTestMethods(DetailAST classDefAst) {
        DetailAST objBlock = classDefAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return false;
        }

        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF) {
                if (hasAnyAnnotation(child, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }

    /**
     * Analisa toda a classe de teste.
     */
    private void analyzeTestClass(DetailAST classDefAst) {
        DetailAST objBlock = classDefAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return;
        }

        // Primeira passada: coleta campos do setUp
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF) {
                if (hasSetUpAnnotation(child)) {
                    collectSetUpFields(child);
                }
            }
            child = child.getNextSibling();
        }

        // Segunda passada: analisa uso de campos em testes
        child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF) {
                if (hasAnyAnnotation(child, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
                    analyzeTestMethod(child);
                }
            }
            child = child.getNextSibling();
        }
    }

    /**
     * Verifica se o método tem anotação @Before ou @BeforeEach.
     */
    private boolean hasSetUpAnnotation(DetailAST methodAst) {
        DetailAST modifiers = methodAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return false;
        }

        DetailAST child = modifiers.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.ANNOTATION) {
                DetailAST annotationIdent = child.findFirstToken(TokenTypes.IDENT);
                if (annotationIdent != null) {
                    String annotation = annotationIdent.getText();
                    if (annotation.equals("Before") ||
                            annotation.equals("BeforeEach") ||
                            annotation.equals("BeforeAll")) {
                        return true;
                    }
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }

    /**
     * Coleta campos que são inicializados no método setUp.
     */
    private void collectSetUpFields(DetailAST setupMethodAst) {
        // NOVO: Guarda a linha do método setUp
        setupLines.put(currentClass, setupMethodAst.getLineNo());

        DetailAST methodBody = setupMethodAst.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) {
            return;
        }

        scanForFieldAssignments(methodBody, setupFields.get(currentClass));
    }

    /**
     * Escaneia por assignments a campos (this.field = ... ou field = ...).
     */
    private void scanForFieldAssignments(DetailAST node, Set<String> fields) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.ASSIGN) {
            String fieldName = getAssignedFieldName(node);
            if (fieldName != null) {
                fields.add(fieldName);
            }
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForFieldAssignments(child, fields);
            child = child.getNextSibling();
        }
    }

    /**
     * Extrai o nome do campo sendo atribuído.
     * Ex: this.calculator = ... → "calculator"
     * field = ... → "field"
     */
    private String getAssignedFieldName(DetailAST assignNode) {
        DetailAST lhs = assignNode.getFirstChild();
        if (lhs == null) {
            return null;
        }

        // this.field
        if (lhs.getType() == TokenTypes.DOT) {
            DetailAST firstPart = lhs.getFirstChild();
            if (firstPart != null && firstPart.getType() == TokenTypes.LITERAL_THIS) {
                DetailAST fieldNode = lhs.getLastChild();
                return fieldNode != null ? fieldNode.getText() : null;
            }
        }

        // field (direto)
        if (lhs.getType() == TokenTypes.IDENT) {
            return lhs.getText();
        }

        return null;
    }

    /**
     * Analisa quais campos são usados em um método de teste.
     */
    private void analyzeTestMethod(DetailAST testMethodAst) {
        String testMethodName = getMethodName(testMethodAst);
        Set<String> usedFields = new HashSet<>();

        DetailAST methodBody = testMethodAst.findFirstToken(TokenTypes.SLIST);
        if (methodBody != null) {
            scanForFieldUsages(methodBody, usedFields);
        }

        testFieldUsages.get(currentClass).put(testMethodName, usedFields);
    }

    /**
     * Escaneia por usos de campos (leitura).
     */
    private void scanForFieldUsages(DetailAST node, Set<String> usedFields) {
        if (node == null) {
            return;
        }

        // Procura por IDENTs que podem ser campos
        if (node.getType() == TokenTypes.IDENT) {
            String name = node.getText();
            // Verifica se é um campo do setUp
            if (setupFields.get(currentClass).contains(name)) {
                usedFields.add(name);
            }
        }

        // this.field
        if (node.getType() == TokenTypes.DOT) {
            DetailAST firstPart = node.getFirstChild();
            if (firstPart != null && firstPart.getType() == TokenTypes.LITERAL_THIS) {
                DetailAST fieldNode = node.getLastChild();
                if (fieldNode != null) {
                    String fieldName = fieldNode.getText();
                    if (setupFields.get(currentClass).contains(fieldName)) {
                        usedFields.add(fieldName);
                    }
                }
            }
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForFieldUsages(child, usedFields);
            child = child.getNextSibling();
        }
    }

    /**
     * Reporta campos do setUp que não são usados por todos os testes.
     */
    private void reportGeneralFixtures() {
        Set<String> fieldsInSetup = setupFields.get(currentClass);
        Map<String, Set<String>> testUsages = testFieldUsages.get(currentClass);

        if (fieldsInSetup.isEmpty() || testUsages.isEmpty()) {
            return;
        }

        int setupLine = setupLines.getOrDefault(currentClass, 1);

        // Para cada campo do setUp
        for (String field : fieldsInSetup) {
            int testsUsingField = 0;
            int totalTests = testUsages.size();

            // Conta quantos testes usam este campo
            for (Set<String> fieldsUsedInTest : testUsages.values()) {
                if (fieldsUsedInTest.contains(field)) {
                    testsUsingField++;
                }
            }

            // Se nem todos os testes usam, é General Fixture
            if (testsUsingField < totalTests && testsUsingField > 0) {
                log(setupLine,
                        "General Fixture: Field ''{0}'' initialized in setUp() but only used by {1} of {2} test methods. "
                                + "Consider moving initialization to individual tests that need it.",
                        field, testsUsingField, totalTests);
            } else if (testsUsingField == 0) {
                log(setupLine,
                        "General Fixture: Field ''{0}'' initialized in setUp() but not used by any test method. "
                                + "Remove unused initialization.",
                        field);
            }
        }
    }
}