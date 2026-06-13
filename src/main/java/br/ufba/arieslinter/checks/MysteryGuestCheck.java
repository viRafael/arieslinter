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
public class MysteryGuestCheck extends AbstractTestSmellCheck {
    private static final Set<String> EXTERNAL_RESOURCE_CLASSES = new HashSet<>();

    static {
        // File System
        EXTERNAL_RESOURCE_CLASSES.add("File");
        EXTERNAL_RESOURCE_CLASSES.add("FileInputStream");
        EXTERNAL_RESOURCE_CLASSES.add("FileOutputStream");
        EXTERNAL_RESOURCE_CLASSES.add("FileReader");
        EXTERNAL_RESOURCE_CLASSES.add("FileWriter");
        EXTERNAL_RESOURCE_CLASSES.add("RandomAccessFile");
        EXTERNAL_RESOURCE_CLASSES.add("JarFile");
        EXTERNAL_RESOURCE_CLASSES.add("JarOutputStream");
        EXTERNAL_RESOURCE_CLASSES.add("ZipFile");
        EXTERNAL_RESOURCE_CLASSES.add("ZipOutputStream");
        EXTERNAL_RESOURCE_CLASSES.add("Files");

        // Database
        EXTERNAL_RESOURCE_CLASSES.add("Connection");
        EXTERNAL_RESOURCE_CLASSES.add("DriverManager");
        EXTERNAL_RESOURCE_CLASSES.add("Statement");
        EXTERNAL_RESOURCE_CLASSES.add("PreparedStatement");
        EXTERNAL_RESOURCE_CLASSES.add("CallableStatement");
        EXTERNAL_RESOURCE_CLASSES.add("ResultSet");

        // Network
        EXTERNAL_RESOURCE_CLASSES.add("Socket");
        EXTERNAL_RESOURCE_CLASSES.add("ServerSocket");
        EXTERNAL_RESOURCE_CLASSES.add("URL");
        EXTERNAL_RESOURCE_CLASSES.add("URLConnection");
        EXTERNAL_RESOURCE_CLASSES.add("HttpURLConnection");
        EXTERNAL_RESOURCE_CLASSES.add("HttpClient");
        EXTERNAL_RESOURCE_CLASSES.add("HttpRequest");

        // Email
        EXTERNAL_RESOURCE_CLASSES.add("Session");
        EXTERNAL_RESOURCE_CLASSES.add("Transport");
        EXTERNAL_RESOURCE_CLASSES.add("Message");
        EXTERNAL_RESOURCE_CLASSES.add("MimeMessage");
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.METHOD_DEF };
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
    public void visitToken(DetailAST ast) {
        if (!hasAnyAnnotation(ast, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
            return;
        }

        DetailAST methodBody = ast.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) {
            return;
        }

        // 1. Obter campos não mockados da classe
        Map<String, DetailAST> unmockedFields = getUnmockedFields(ast);

        // 2. Escanear o corpo do método por recursos externos
        Set<String> reportedResources = new HashSet<>();
        scanMethodForMysteryGuest(methodBody, unmockedFields, reportedResources);
    }

    private void scanMethodForMysteryGuest(DetailAST node,
                                           Map<String, DetailAST> unmockedFields,
                                           Set<String> reportedResources) {
        if (node == null) {
            return;
        }

        // Caso 1: Instanciação direta (new File(...))
        if (node.getType() == TokenTypes.LITERAL_NEW) {
            String className = getClassName(node);
            if (className != null && isExternalResource(className)) {
                if (!isInsideMockCall(node) && !reportedResources.contains(className)) {
                    log(node.getLineNo(),
                            "Mystery Guest: Test instantiates external resource ''{0}''. "
                                    + "Use mocks or in-memory alternatives instead.",
                            className);
                    reportedResources.add(className);
                }
            }
        }

        // Caso 2: Declaração de variável local
        if (node.getType() == TokenTypes.VARIABLE_DEF) {
            String typeName = getTypeName(node);
            if (typeName != null && isExternalResource(typeName)) {
                if (!isMocked(node) && !reportedResources.contains(typeName)) {
                    log(node.getLineNo(),
                            "Mystery Guest: Test declares external resource variable of type ''{0}''. "
                                    + "Use mocks or in-memory alternatives instead.",
                            typeName);
                    reportedResources.add(typeName);
                }
            }
        }

        // Caso 3: Declaração de parâmetro (ex: em foreach)
        if (node.getType() == TokenTypes.PARAMETER_DEF) {
            String typeName = getTypeName(node);
            if (typeName != null && isExternalResource(typeName)) {
                if (!reportedResources.contains(typeName)) {
                    log(node.getLineNo(),
                            "Mystery Guest: Test uses external resource parameter of type ''{0}''. "
                                    + "Use mocks or in-memory alternatives instead.",
                            typeName);
                    reportedResources.add(typeName);
                }
            }
        }

        // Caso 4: Chamada de método estático em uma classe proibida (ex: DriverManager.getConnection, Files.write)
        if (node.getType() == TokenTypes.METHOD_CALL) {
            DetailAST firstChild = node.getFirstChild();
            if (firstChild != null && firstChild.getType() == TokenTypes.DOT) {
                DetailAST target = firstChild.getFirstChild();
                if (target != null && target.getType() == TokenTypes.IDENT) {
                    String targetName = target.getText();
                    if (isExternalResource(targetName) && !reportedResources.contains(targetName)) {
                        log(node.getLineNo(),
                                "Mystery Guest: Test calls method on external resource class ''{0}''. "
                                        + "Use mocks or in-memory alternatives instead.",
                                targetName);
                        reportedResources.add(targetName);
                    }
                }
            }
        }

        // Caso 5: Referência a campo não mockado do tipo recurso externo
        if (node.getType() == TokenTypes.IDENT) {
            String name = node.getText();
            if (unmockedFields.containsKey(name)) {
                DetailAST fieldDef = unmockedFields.get(name);
                String typeName = getTypeName(fieldDef);
                if (typeName != null && !reportedResources.contains(name)) {
                    log(node.getLineNo(),
                            "Mystery Guest: Test utilizes class field ''{0}'' of external resource type ''{1}''. "
                                    + "Use mocks or in-memory alternatives instead.",
                            name, typeName);
                    reportedResources.add(name);
                }
            }
        }

        // Recursão para filhos
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanMethodForMysteryGuest(child, unmockedFields, reportedResources);
            child = child.getNextSibling();
        }
    }

    private Map<String, DetailAST> getUnmockedFields(DetailAST methodAst) {
        Map<String, DetailAST> unmockedFields = new HashMap<>();
        DetailAST parent = methodAst.getParent();
        while (parent != null && parent.getType() != TokenTypes.CLASS_DEF) {
            parent = parent.getParent();
        }
        if (parent != null) {
            DetailAST objBlock = parent.findFirstToken(TokenTypes.OBJBLOCK);
            if (objBlock != null) {
                DetailAST child = objBlock.getFirstChild();
                while (child != null) {
                    if (child.getType() == TokenTypes.VARIABLE_DEF) {
                        String typeName = getTypeName(child);
                        if (typeName != null && isExternalResource(typeName)) {
                            if (!isFieldMocked(child)) {
                                DetailAST ident = child.findFirstToken(TokenTypes.IDENT);
                                if (ident != null) {
                                    unmockedFields.put(ident.getText(), child);
                                }
                            }
                        }
                    }
                    child = child.getNextSibling();
                }
            }
        }
        return unmockedFields;
    }

    private boolean isFieldMocked(DetailAST varDef) {
        DetailAST modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers != null) {
            DetailAST child = modifiers.getFirstChild();
            while (child != null) {
                if (child.getType() == TokenTypes.ANNOTATION) {
                    DetailAST annotationIdent = child.findFirstToken(TokenTypes.IDENT);
                    if (annotationIdent != null) {
                        String annotName = annotationIdent.getText();
                        if ("Mock".equals(annotName) || "Spy".equals(annotName) || "MockBean".equals(annotName)) {
                            return true;
                        }
                    }
                }
                child = child.getNextSibling();
            }
        }
        return isMocked(varDef);
    }

    private boolean isMocked(DetailAST varDef) {
        DetailAST assign = varDef.findFirstToken(TokenTypes.ASSIGN);
        if (assign != null) {
            DetailAST expr = assign.findFirstToken(TokenTypes.EXPR);
            if (expr != null) {
                return hasMockCall(expr);
            }
        }
        return false;
    }

    private boolean hasMockCall(DetailAST node) {
        if (node.getType() == TokenTypes.METHOD_CALL) {
            String methodName = getMethodName(node);
            if ("mock".equals(methodName) || "spy".equals(methodName)) {
                return true;
            }
        }
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (hasMockCall(child)) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    private String getClassName(DetailAST newNode) {
        DetailAST typeNode = newNode.getFirstChild();
        if (typeNode == null) {
            return null;
        }
        if (typeNode.getType() == TokenTypes.IDENT) {
            return typeNode.getText();
        } else if (typeNode.getType() == TokenTypes.DOT) {
            DetailAST lastChild = typeNode.getLastChild();
            return lastChild != null ? lastChild.getText() : null;
        }
        return null;
    }

    private boolean isExternalResource(String className) {
        return EXTERNAL_RESOURCE_CLASSES.contains(className);
    }

    private boolean isInsideMockCall(DetailAST node) {
        DetailAST parent = node.getParent();
        while (parent != null) {
            if (parent.getType() == TokenTypes.METHOD_CALL) {
                String methodName = getMethodName(parent);
                if (methodName != null &&
                        (methodName.equals("mock") ||
                                methodName.equals("spy") ||
                                methodName.equals("when") ||
                                methodName.equals("verify"))) {
                    return true;
                }
            }
            parent = parent.getParent();
        }
        return false;
    }

    private String getTypeName(DetailAST variableDef) {
        DetailAST typeNode = variableDef.findFirstToken(TokenTypes.TYPE);
        if (typeNode == null) {
            return null;
        }
        DetailAST firstChild = typeNode.getFirstChild();
        if (firstChild == null) {
            return null;
        }
        if (firstChild.getType() == TokenTypes.IDENT) {
            return firstChild.getText();
        } else if (firstChild.getType() == TokenTypes.DOT) {
            DetailAST lastChild = firstChild.getLastChild();
            if (lastChild != null) {
                return lastChild.getText();
            }
        }
        return null;
    }
}