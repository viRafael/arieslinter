package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

/**
 * Detecta Eager Test: teste que chama múltiplos métodos de produção diferentes.
 */
@StatelessCheck
public class EagerTestCheck extends AbstractTestSmellCheck {
    // Número máximo de métodos de produção diferentes permitidos
    private int maxProductionMethods = 1;

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

        // Coleta métodos de produção únicos chamados neste teste
        Set<String> productionMethods = collectProductionMethods(ast);

        // Eager Test: chama múltiplos métodos de produção diferentes
        if (productionMethods.size() > maxProductionMethods) {
            log(ast.getLineNo(),
                    "Eager Test: Test method calls {0} different production methods. "
                            + "Tests should focus on a single behavior. Consider splitting into separate tests.",
                    productionMethods.size());
        }
    }

    /**
     * Coleta todos os métodos de produção ÚNICOS chamados no teste.
     * Retorna um Set para contar apenas métodos diferentes.
     */
    private Set<String> collectProductionMethods(DetailAST methodAst) {
        Set<String> productionMethods = new HashSet<>();

        DetailAST methodBody = methodAst.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) {
            return productionMethods;
        }

        scanForProductionMethods(methodBody, productionMethods);
        return productionMethods;
    }

    /**
     * Escaneia recursivamente por chamadas de métodos de produção.
     */
    private void scanForProductionMethods(DetailAST node, Set<String> productionMethods) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.METHOD_CALL) {
            processMethodCall(node, productionMethods);
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForProductionMethods(child, productionMethods);
            child = child.getNextSibling();
        }
    }

    /**
     * Processa uma chamada de método e adiciona ao Set se for método de produção.
     */
    private void processMethodCall(DetailAST methodCall, Set<String> productionMethods) {
        String methodSignature = getMethodSignature(methodCall);

        if (methodSignature != null && isProductionMethod(methodSignature)) {
            productionMethods.add(methodSignature);
        }
    }

    /**
     * Cria uma assinatura para o método chamado.
     * Formato: "objeto.metodo" ou apenas "metodo"
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

            String targetName = getTargetName(target);
            String method = methodName.getText();

            // Ignora chamadas a this/super
            if ("this".equals(targetName) || "super".equals(targetName)) {
                return method;
            }

            return targetName + "." + method;
        }

        // Chamada direta: method()
        if (firstChild.getType() == TokenTypes.IDENT) {
            return firstChild.getText();
        }

        return null;
    }

    /**
     * Extrai o nome do objeto/target da chamada.
     */
    private String getTargetName(DetailAST target) {
        if (target == null) {
            return "";
        }

        // IDENT simples: obj.method()
        if (target.getType() == TokenTypes.IDENT) {
            return target.getText();
        }

        // DOT aninhado: this.obj.method() ou obj.field.method()
        if (target.getType() == TokenTypes.DOT) {
            DetailAST lastPart = target.getLastChild();
            return lastPart != null ? lastPart.getText() : "";
        }

        // Literal THIS ou SUPER
        if (target.getType() == TokenTypes.LITERAL_THIS) {
            return "this";
        }
        if (target.getType() == TokenTypes.LITERAL_SUPER) {
            return "super";
        }

        return target.getText();
    }

    /**
     * Verifica se é método de produção (não é framework de teste/mock).
     */
    private boolean isProductionMethod(String methodSignature) {
        if (methodSignature == null) {
            return false;
        }

        // Extrai apenas o nome do método (sem prefixo)
        String methodName = methodSignature;
        int dotIndex = methodSignature.lastIndexOf('.');
        if (dotIndex >= 0) {
            methodName = methodSignature.substring(dotIndex + 1);
        }

        // Ignora métodos de frameworks de teste
        if (isTestFrameworkMethod(methodName)) {
            return false;
        }

        // Ignora getters/setters simples (opcional)
        if (isGetterOrSetter(methodName)) {
            return false;
        }

        // Ignora métodos de Object
        if (isObjectMethod(methodName)) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se é método de framework de teste ou mocking.
     */
    private boolean isTestFrameworkMethod(String methodName) {
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
                methodName.equals("capture") ||
                methodName.startsWith("doThrow") ||
                methodName.startsWith("doReturn") ||
                methodName.startsWith("doAnswer") ||
                methodName.startsWith("doNothing") ||
                methodName.equals("reset") ||
                methodName.equals("clearInvocations");
    }

    /**
     * Verifica se é getter/setter (opcional - pode querer contar ou não).
     */
    private boolean isGetterOrSetter(String methodName) {
        // Considera getters/setters como métodos triviais que não contam
        return (methodName.startsWith("get") ||
                methodName.startsWith("set") ||
                methodName.startsWith("is")) &&
                methodName.length() > 3 &&
                Character.isUpperCase(methodName.charAt(methodName.startsWith("is") ? 2 : 3));
    }

    /**
     * Verifica se é método de Object (equals, hashCode, toString, etc).
     */
    private boolean isObjectMethod(String methodName) {
        return methodName.equals("equals") ||
                methodName.equals("hashCode") ||
                methodName.equals("toString") ||
                methodName.equals("getClass") ||
                methodName.equals("clone") ||
                methodName.equals("finalize") ||
                methodName.equals("wait") ||
                methodName.equals("notify") ||
                methodName.equals("notifyAll");
    }
}