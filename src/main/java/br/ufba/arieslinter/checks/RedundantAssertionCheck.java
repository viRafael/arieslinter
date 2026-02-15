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

        // Pula a mensagem se o primeiro parâmetro for String literal
        List<DetailAST> params = unwrappedParams;
        if (unwrappedParams.get(0).getType() == TokenTypes.STRING_LITERAL) {
            if (unwrappedParams.size() > 1) {
                params = unwrappedParams.subList(1, unwrappedParams.size());
            } else {
                return; // Só tem a mensagem
            }
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

    /**
     * Verifica se o nó representa um valor real (não um separador).
     */
    private boolean isValueNode(DetailAST node) {
        int type = node.getType();

        // Literais
        if (type == TokenTypes.LITERAL_TRUE ||
                type == TokenTypes.LITERAL_FALSE ||
                type == TokenTypes.LITERAL_NULL ||
                type == TokenTypes.STRING_LITERAL ||
                type == TokenTypes.NUM_INT ||
                type == TokenTypes.NUM_LONG ||
                type == TokenTypes.NUM_FLOAT ||
                type == TokenTypes.NUM_DOUBLE) {
            return true;
        }

        // Identificadores (variáveis)
        if (type == TokenTypes.IDENT) {
            return true;
        }

        // Chamadas de método
        if (type == TokenTypes.METHOD_CALL) {
            return true;
        }

        // Acessos a campos (obj.field)
        if (type == TokenTypes.DOT) {
            return true;
        }

        // New expressions
        if (type == TokenTypes.LITERAL_NEW) {
            return true;
        }

        // Expressões binárias/unárias
        if (type == TokenTypes.PLUS || type == TokenTypes.MINUS ||
                type == TokenTypes.STAR || type == TokenTypes.DIV ||
                type == TokenTypes.MOD ||
                type == TokenTypes.EQUAL || type == TokenTypes.NOT_EQUAL ||
                type == TokenTypes.GT || type == TokenTypes.LT ||
                type == TokenTypes.GE || type == TokenTypes.LE ||
                type == TokenTypes.LOR || type == TokenTypes.LAND ||
                type == TokenTypes.LNOT) {
            return true;
        }

        // Lambda
        if (type == TokenTypes.LAMBDA) {
            return true;
        }

        // Arrays
        if (type == TokenTypes.ARRAY_INIT) {
            return true;
        }

        return false;
    }

    private boolean checkRedundancy(String methodName, List<DetailAST> params) {
        // Assertions de igualdade - compara se os dois parâmetros são idênticos
        if (methodName.equals("assertEquals") ||
                methodName.equals("assertSame") ||
                methodName.equals("assertArrayEquals") ||
                methodName.equals("assertIterableEquals") ||
                methodName.equals("assertLinesMatch")) {
            return isRedundantEqualityAssertion(params);
        }

        // Assertions de desigualdade - redundante se comparar valores iguais
        if (methodName.equals("assertNotEquals") ||
                methodName.equals("assertNotSame")) {
            return isRedundantInequalityAssertion(params);
        }

        // Assertions booleanas
        if (methodName.equals("assertTrue")) {
            return isRedundantTrueAssertion(params);
        }

        if (methodName.equals("assertFalse")) {
            return isRedundantFalseAssertion(params);
        }

        // Assertions de null
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

        DetailAST expected = params.get(0);
        DetailAST actual = params.get(1);

        return areIdentical(expected, actual);
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

    /**
     * Verifica se dois nós AST são estruturalmente idênticos.
     */
    private boolean areIdentical(DetailAST node1, DetailAST node2) {
        if (node1 == null && node2 == null) {
            return true;
        }

        if (node1 == null || node2 == null) {
            return false;
        }

        // Tipos diferentes
        if (node1.getType() != node2.getType()) {
            return false;
        }

        // Compara texto
        String text1 = node1.getText();
        String text2 = node2.getText();

        if (text1 == null && text2 == null) {
        } else if (text1 == null || text2 == null) {
            return false;
        } else if (!text1.equals(text2)) {
            return false;
        }

        // Compara filhos recursivamente
        DetailAST child1 = node1.getFirstChild();
        DetailAST child2 = node2.getFirstChild();

        while (child1 != null && child2 != null) {
            if (!areIdentical(child1, child2)) {
                return false;
            }
            child1 = child1.getNextSibling();
            child2 = child2.getNextSibling();
        }

        // Ambos devem ter terminado ao mesmo tempo
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

    protected String getMethodName(DetailAST methodCall) {
        DetailAST identNode = methodCall.findFirstToken(TokenTypes.IDENT);

        if (identNode == null) {
            DetailAST dotNode = methodCall.findFirstToken(TokenTypes.DOT);
            if (dotNode != null) {
                identNode = dotNode.getLastChild();
            }
        }

        return identNode != null ? identNode.getText() : null;
    }

    private boolean isAssertionMethod(String methodName) {
        return methodName.startsWith("assert") || methodName.equals("fail");
    }
}