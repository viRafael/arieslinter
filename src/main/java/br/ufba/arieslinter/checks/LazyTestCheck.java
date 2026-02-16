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
public class LazyTestCheck extends AbstractTestSmellCheck {
    private final Map<String, Set<Integer>> productionMethodCalls = new HashMap<>();
    private final Set<String> reportedMethods = new HashSet<>();

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
        productionMethodCalls.clear();
        reportedMethods.clear();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (isTestClass(ast)) {
            productionMethodCalls.clear();
            reportedMethods.clear();

            collectMethodCallsFromTests(ast);
            reportLazyTests();
        }
    }

    /**
     * Verifica se a classe contém métodos de teste.
     */
    private boolean isTestClass(DetailAST classDefAst) {
        DetailAST objBlock = classDefAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return false;
        }

        // Verifica se tem pelo menos um método @Test
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
     * Coleta chamadas de métodos de todos os testes da classe.
     */
    private void collectMethodCallsFromTests(DetailAST classDefAst) {
        DetailAST objBlock = classDefAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return;
        }

        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF) {
                if (hasAnyAnnotation(child, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
                    // É um método de teste, coleta suas chamadas
                    collectMethodCalls(child);
                }
            }
            child = child.getNextSibling();
        }
    }

    /**
     * Coleta todas as chamadas de métodos de produção em um teste.
     */
    private void collectMethodCalls(DetailAST testMethodAst) {
        DetailAST methodBody = testMethodAst.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) {
            return;
        }

        int testLine = testMethodAst.getLineNo();
        scanForMethodCalls(methodBody, testLine);
    }

    /**
     * Escaneia recursivamente por chamadas de método.
     */
    private void scanForMethodCalls(DetailAST node, int testLine) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.METHOD_CALL) {
            processMethodCall(node, testLine);
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForMethodCalls(child, testLine);
            child = child.getNextSibling();
        }
    }

    /**
     * Processa uma chamada de método.
     */
    private void processMethodCall(DetailAST methodCall, int testLine) {
        String methodSignature = getMethodSignature(methodCall);

        if (methodSignature == null || isTestFrameworkMethod(methodSignature)) {
            return;
        }

        // Adiciona esta chamada ao mapa
        productionMethodCalls
                .computeIfAbsent(methodSignature, k -> new HashSet<>())
                .add(testLine);
    }

    /**
     * Cria uma assinatura única para o método chamado.
     * Formato: "objeto.metodo" ou "metodo" se for chamada direta
     */
    private String getMethodSignature(DetailAST methodCall) {
        DetailAST firstChild = methodCall.getFirstChild();
        if (firstChild == null) {
            return null;
        }

        // Chamada qualificada: obj.method()
        if (firstChild.getType() == TokenTypes.DOT) {
            DetailAST target = firstChild.getFirstChild();
            DetailAST methodName = firstChild.getLastChild();

            if (target == null || methodName == null) {
                return null;
            }

            String targetName = getNodeText(target);
            String method = methodName.getText();

            return targetName + "." + method;
        }

        // Chamada direta: method()
        if (firstChild.getType() == TokenTypes.IDENT) {
            return firstChild.getText();
        }

        return null;
    }

    /**
     * Extrai o texto de um nó, lidando com DOT (this.obj.method).
     */
    private String getNodeText(DetailAST node) {
        if (node == null) {
            return "";
        }

        if (node.getType() == TokenTypes.IDENT) {
            return node.getText();
        }

        if (node.getType() == TokenTypes.DOT) {
            // Para this.obj, pega só "obj"
            DetailAST lastChild = node.getLastChild();
            return lastChild != null ? lastChild.getText() : "";
        }

        return node.getText();
    }

    /**
     * Verifica se é método de framework de teste (assert, verify, etc).
     */
    private boolean isTestFrameworkMethod(String methodSignature) {
        if (methodSignature == null) {
            return true;
        }

        // Extrai apenas o nome do método (remove prefixo "obj.")
        String methodName = methodSignature;
        int dotIndex = methodSignature.lastIndexOf('.');
        if (dotIndex >= 0) {
            methodName = methodSignature.substring(dotIndex + 1);
        }

        // Ignora métodos de frameworks de teste e mocking
        return methodName.startsWith("assert") ||
                methodName.startsWith("verify") ||
                methodName.startsWith("when") ||
                methodName.startsWith("given") ||
                methodName.startsWith("then") ||
                methodName.equals("fail") ||
                methodName.startsWith("mock") ||
                methodName.startsWith("spy") ||
                methodName.equals("times") ||
                methodName.equals("never") ||
                methodName.equals("any") ||
                methodName.equals("eq") ||
                methodName.equals("capture");
    }

    /**
     * Reporta métodos de produção chamados por múltiplos testes.
     */
    private void reportLazyTests() {
        for (Map.Entry<String, Set<Integer>> entry : productionMethodCalls.entrySet()) {
            String methodSignature = entry.getKey();
            Set<Integer> callLines = entry.getValue();

            // Lazy Test: método chamado por 2 ou mais testes
            if (callLines.size() >= 2 && !reportedMethods.contains(methodSignature)) {
                // Reporta na primeira linha onde aparece
                int firstLine = callLines.stream().min(Integer::compare).orElse(0);

                log(firstLine,
                        "Lazy Test: Production method ''{0}'' is called by {1} different test methods. "
                                + "Consider consolidating these tests or refactoring the production method.",
                        methodSignature,
                        callLines.size());

                reportedMethods.add(methodSignature);
            }
        }
    }
}