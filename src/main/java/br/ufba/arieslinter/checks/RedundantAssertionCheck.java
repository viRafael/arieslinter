package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.ArrayList;
import java.util.List;

@StatelessCheck
public class RedundantAssertionCheck extends AbstractTestSmellCheck {

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
        if (methodBody != null) {
            scanForAssertions(methodBody);
        }
    }

    private void scanForAssertions(DetailAST node) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.METHOD_CALL) {
            processAssertion(node);
        }

        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForAssertions(child);
            child = child.getNextSibling();
        }
    }

    private void processAssertion(DetailAST methodCall) {
        String methodName = getMethodName(methodCall);

        if (methodName == null || !isAssertionMethod(methodName)) {
            return;
        }

        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist == null) {
            return;
        }

        // Pega os parâmetros e desembrulha os EXPR
        List<DetailAST> allParams = getParameters(elist);
        List<DetailAST> unwrappedParams = unwrapExpressions(allParams);

        if (unwrappedParams.isEmpty()) {
            return;
        }

        List<DetailAST> params = excludeMessage(methodName, unwrappedParams);

        if (params.isEmpty()) {
            return;
        }

        // Verifica redundância baseado no tipo de assertion
        boolean isRedundant = checkRedundancy(methodName, params);

        if (isRedundant) {
            log(methodCall.getLineNo(), "Redundant Assertion: ''{0}'' compares identical values. " +
                    "This assertion will always pass/fail and provides no value.",
                    methodName);
        }
    }

    /**
     * Remove o parâmetro de mensagem se ele estiver presente.
     */
    private List<DetailAST> excludeMessage(String methodName, List<DetailAST> params) {
        int size = params.size();

        // Para assertions de 1 argumento (assertTrue, assertNull, etc)
        // Se houver 2 argumentos e o primeiro for String, o primeiro é a mensagem.
        if (isSingleParamAssertion(methodName)) {
            if (size >= 2 && params.get(0).getType() == TokenTypes.STRING_LITERAL) {
                return params.subList(1, size);
            }
        }

        // Para assertions de 2 argumentos (assertEquals, assertSame, etc)
        // Se houver 3 argumentos e o primeiro for String, o primeiro é a mensagem.
        if (isDoubleParamAssertion(methodName)) {
            if ((size == 3 || size == 4) && params.get(0).getType() == TokenTypes.STRING_LITERAL) {
                return params.subList(1, size);
            }
        }

        return params;
    }

    private boolean isSingleParamAssertion(String methodName) {
        return methodName.equals("assertTrue") || methodName.equals("assertFalse") ||
               methodName.equals("assertNull") || methodName.equals("assertNotNull");
    }

    private boolean isDoubleParamAssertion(String methodName) {
        return methodName.equals("assertEquals") || methodName.equals("assertNotEquals") ||
               methodName.equals("assertSame") || methodName.equals("assertNotSame") ||
               methodName.equals("assertArrayEquals");
    }

    /**
     * Desembrulha nós EXPR e filtra apenas valores reais.
     */
    private List<DetailAST> unwrapExpressions(List<DetailAST> params) {
        List<DetailAST> unwrapped = new ArrayList<>();

        for (DetailAST param : params) {
            DetailAST actualValue = param;

            // Se é EXPR, desembrulha
            if (param.getType() == TokenTypes.EXPR) {
                actualValue = param.getFirstChild();
            }

            if (actualValue == null) {
                continue;
            }

            // Apenas adiciona se for um tipo de valor válido
            if (isValueNode(actualValue)) {
                unwrapped.add(actualValue);
            }
        }

        return unwrapped;
    }

    private boolean checkRedundancy(String methodName, List<DetailAST> params) {
        // Assertions de igualdade
        if (methodName.equals("assertEquals") ||
                methodName.equals("assertSame") ||
                methodName.equals("assertArrayEquals") ||
                methodName.equals("assertIterableEquals") ||
                methodName.equals("assertLinesMatch")) {
            return isRedundantEqualityAssertion(params);
        }

        // Assertions de desigualdade
        if (methodName.equals("assertNotEquals") ||
                methodName.equals("assertNotSame")) {
            return isRedundantInequalityAssertion(params);
        }

        if (methodName.equals("assertTrue")) {
            return isRedundantTrueAssertion(params);
        }

        if (methodName.equals("assertFalse")) {
            return isRedundantFalseAssertion(params);
        }

        if (methodName.equals("assertNull")) {
            return isRedundantNullAssertion(params);
        }

        if (methodName.equals("assertNotNull")) {
            return isRedundantNotNullAssertion(params);
        }

        return false;
    }

    private boolean isRedundantTrueAssertion(List<DetailAST> params) {
        if (params.isEmpty()) {
            return false;
        }
        DetailAST condition = params.get(0);
        return condition.getType() == TokenTypes.LITERAL_TRUE;
    }

    private boolean isRedundantFalseAssertion(List<DetailAST> params) {
        if (params.isEmpty()) {
            return false;
        }
        DetailAST condition = params.get(0);
        return condition.getType() == TokenTypes.LITERAL_FALSE;
    }

    private boolean isRedundantEqualityAssertion(List<DetailAST> params) {
        if (params.size() < 2) {
            return false;
        }
        return areIdentical(params.get(0), params.get(1));
    }

    private boolean isRedundantInequalityAssertion(List<DetailAST> params) {
        if (params.size() < 2) {
            return false;
        }
        return areIdentical(params.get(0), params.get(1));
    }

    private boolean isRedundantNullAssertion(List<DetailAST> params) {
        if (params.isEmpty()) {
            return false;
        }
        return params.get(0).getType() == TokenTypes.LITERAL_NULL;
    }

    private boolean isRedundantNotNullAssertion(List<DetailAST> params) {
        if (params.isEmpty()) {
            return false;
        }
        return params.get(0).getType() == TokenTypes.LITERAL_NEW;
    }

    private boolean areIdentical(DetailAST node1, DetailAST node2) {
        if (node1 == null && node2 == null) return true;
        if (node1 == null || node2 == null) return false;
        if (node1.getType() != node2.getType()) return false;

        String text1 = node1.getText();
        String text2 = node2.getText();
        if (text1 != null && !text1.equals(text2)) return false;

        DetailAST child1 = node1.getFirstChild();
        DetailAST child2 = node2.getFirstChild();
        while (child1 != null && child2 != null) {
            if (!areIdentical(child1, child2)) return false;
            child1 = child1.getNextSibling();
            child2 = child2.getNextSibling();
        }
        return child1 == null && child2 == null;
    }

    private List<DetailAST> getParameters(DetailAST elist) {
        List<DetailAST> params = new ArrayList<>();
        DetailAST param = elist.getFirstChild();
        while (param != null) {
            params.add(param);
            param = param.getNextSibling();
        }
        return params;
    }

    private boolean isAssertionMethod(String methodName) {
        return methodName.startsWith("assert") || methodName.equals("fail");
    }

    private boolean isValueNode(DetailAST node) {
        int type = node.getType();
        return type == TokenTypes.LITERAL_TRUE || type == TokenTypes.LITERAL_FALSE ||
               type == TokenTypes.LITERAL_NULL || type == TokenTypes.STRING_LITERAL ||
               type == TokenTypes.NUM_INT || type == TokenTypes.NUM_LONG ||
               type == TokenTypes.NUM_FLOAT || type == TokenTypes.NUM_DOUBLE ||
               type == TokenTypes.IDENT || type == TokenTypes.METHOD_CALL ||
               type == TokenTypes.DOT || type == TokenTypes.LITERAL_NEW ||
               type == TokenTypes.PLUS || type == TokenTypes.MINUS ||
               type == TokenTypes.STAR || type == TokenTypes.DIV ||
               type == TokenTypes.MOD || type == TokenTypes.EQUAL ||
               type == TokenTypes.NOT_EQUAL || type == TokenTypes.GT ||
               type == TokenTypes.LT || type == TokenTypes.GE ||
               type == TokenTypes.LE || type == TokenTypes.LOR ||
               type == TokenTypes.LAND || type == TokenTypes.LNOT ||
               type == TokenTypes.LAMBDA || type == TokenTypes.ARRAY_INIT;
    }
}
