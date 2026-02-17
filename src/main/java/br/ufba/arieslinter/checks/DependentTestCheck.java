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
public class DependentTestCheck extends AbstractTestSmellCheck {
    private String currentClass;
    
    // Métodos de teste por classe: classe -> conjunto de nomes de métodos @Test
    private final Map<String, Set<String>> classTestMethods = new HashMap<>();
    
    // Dependências detectadas: método teste -> conjunto de testes que ele chama
    private final Map<String, Map<String, Integer>> testDependencies = new HashMap<>();

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {TokenTypes.CLASS_DEF};
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
        classTestMethods.clear();
        testDependencies.clear();
        currentClass = null;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (!isTestClass(ast)) {
            return;
        }

        currentClass = getClassName(ast);
        classTestMethods.put(currentClass, new HashSet<>());

        analyzeTestClass(ast);
        reportDependencies();
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

        // Primeira passada: coleta nomes de todos os métodos @Test
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF) {
                if (hasAnyAnnotation(child, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
                    String methodName = getMethodName(child);
                    classTestMethods.get(currentClass).add(methodName);
                }
            }
            child = child.getNextSibling();
        }

        // Segunda passada: analisa dependências
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
     * Analisa um método de teste em busca de chamadas a outros testes.
     */
    private void analyzeTestMethod(DetailAST testMethodAst) {
        String testMethodName = getMethodName(testMethodAst);
        Map<String, Integer> dependencies = new HashMap<>();

        DetailAST methodBody = testMethodAst.findFirstToken(TokenTypes.SLIST);
        if (methodBody != null) {
            scanForTestCalls(methodBody, dependencies);
        }

        if (!dependencies.isEmpty()) {
            testDependencies.put(testMethodName, dependencies);
        }
    }

    /**
     * Escaneia recursivamente por chamadas a outros métodos de teste.
     */
    private void scanForTestCalls(DetailAST node, Map<String, Integer> dependencies) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.METHOD_CALL) {
            String calledMethod = extractMethodName(node);
            
            // Verifica se é chamada a outro método de teste da mesma classe
            if (calledMethod != null && 
                classTestMethods.get(currentClass).contains(calledMethod)) {
                dependencies.put(calledMethod, node.getLineNo());
            }
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForTestCalls(child, dependencies);
            child = child.getNextSibling();
        }
    }

    /**
     * Extrai o nome do método de uma chamada METHOD_CALL.
     */
    private String extractMethodName(DetailAST methodCall) {
        DetailAST firstChild = methodCall.getFirstChild();
        if (firstChild == null) {
            return null;
        }

        // Chamada qualificada: obj.method() ou this.method()
        if (firstChild.getType() == TokenTypes.DOT) {
            DetailAST methodNameNode = firstChild.getLastChild();
            return methodNameNode != null ? methodNameNode.getText() : null;
        }

        // Chamada direta: method()
        if (firstChild.getType() == TokenTypes.IDENT) {
            return firstChild.getText();
        }

        return null;
    }

    /**
     * Reporta todas as dependências detectadas.
     */
    private void reportDependencies() {
        for (Map.Entry<String, Map<String, Integer>> entry : testDependencies.entrySet()) {
            String testMethod = entry.getKey();
            Map<String, Integer> dependencies = entry.getValue();

            for (Map.Entry<String, Integer> dep : dependencies.entrySet()) {
                String dependentTest = dep.getKey();
                int line = dep.getValue();

                log(line,
                    "Dependent Test: Test method ''{0}'' calls another test method ''{1}''. "
                        + "Tests should be independent and not call each other.",
                    testMethod, dependentTest);
            }
        }
    }
}