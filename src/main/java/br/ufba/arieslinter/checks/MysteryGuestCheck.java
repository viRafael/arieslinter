package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
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
        EXTERNAL_RESOURCE_CLASSES.add("Path");
        EXTERNAL_RESOURCE_CLASSES.add("Paths");
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

    // Classes de mock/test que são permitidas
    private static final Set<String> ALLOWED_TEST_CLASSES = new HashSet<>();

    static {
        // Mockito
        ALLOWED_TEST_CLASSES.add("Mock");
        ALLOWED_TEST_CLASSES.add("Mockito");
        ALLOWED_TEST_CLASSES.add("MockedStatic");
        ALLOWED_TEST_CLASSES.add("MockedConstruction");

        // Test utilities
        ALLOWED_TEST_CLASSES.add("TemporaryFolder");
        ALLOWED_TEST_CLASSES.add("TempDir");

        // In-memory databases
        ALLOWED_TEST_CLASSES.add("H2");
        ALLOWED_TEST_CLASSES.add("HSQLDB");
        ALLOWED_TEST_CLASSES.add("Derby");
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

        scanForExternalResources(methodBody);
    }

    /**
     * Escaneia recursivamente por instanciações de recursos externos.
     */
    private void scanForExternalResources(DetailAST node) {
        if (node == null) {
            return;
        }

        // Verifica se é um 'new' (instanciação)
        if (node.getType() == TokenTypes.LITERAL_NEW) {
            processNewExpression(node);
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForExternalResources(child);
            child = child.getNextSibling();
        }
    }

    /**
     * Processa uma expressão 'new' verificando se é recurso externo.
     */
    private void processNewExpression(DetailAST newNode) {
        String className = getClassName(newNode);

        if (className == null || className.isEmpty()) {
            return;
        }

        // Verifica se é uma classe de recurso externo
        if (isExternalResource(className)) {
            // Verifica se não está dentro de um mock (ex: mock(File.class))
            if (!isInsideMockCall(newNode)) {
                log(newNode.getLineNo(),
                        "Mystery Guest: Test instantiates external resource ''{0}''. "
                                + "Use mocks or in-memory alternatives instead of real external resources.",
                        className);
            }
        }
    }

    /**
     * Extrai o nome da classe de uma expressão 'new'.
     */
    private String getClassName(DetailAST newNode) {
        // O tipo vem logo após o 'new'
        DetailAST typeNode = newNode.getFirstChild();
        if (typeNode == null) {
            return null;
        }

        // Pode ser IDENT (new File) ou DOT (new java.io.File)
        if (typeNode.getType() == TokenTypes.IDENT) {
            return typeNode.getText();
        } else if (typeNode.getType() == TokenTypes.DOT) {
            // Pega apenas o nome da classe (File de java.io.File)
            DetailAST lastChild = typeNode.getLastChild();
            return lastChild != null ? lastChild.getText() : null;
        }

        return null;
    }

    /**
     * Verifica se a classe é um recurso externo proibido.
     */
    private boolean isExternalResource(String className) {
        return EXTERNAL_RESOURCE_CLASSES.contains(className);
    }

    /**
     * Verifica se a instanciação está dentro de uma chamada mock().
     * Ex: mock(File.class) é permitido
     */
    private boolean isInsideMockCall(DetailAST node) {
        DetailAST parent = node.getParent();

        while (parent != null) {
            if (parent.getType() == TokenTypes.METHOD_CALL) {
                String methodName = getMethodName(parent);

                // Métodos de mocking conhecidos
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
}