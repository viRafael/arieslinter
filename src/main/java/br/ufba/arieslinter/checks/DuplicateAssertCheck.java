package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StatelessCheck
public class DuplicateAssertCheck extends AbstractTestSmellCheck {

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

        // Mapa: assinatura da assertion -> lista de linhas onde aparece
        Map<String, List<Integer>> assertSignatures = new HashMap<>();

        DetailAST methodBody = ast.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) {
            return;
        }

        collectAssertions(methodBody, assertSignatures);
        reportDuplicates(assertSignatures);
    }

    /**
     * Coleta todas as assertions e suas assinaturas.
     */
    private void collectAssertions(DetailAST node, Map<String, List<Integer>> assertSignatures) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.METHOD_CALL) {
            if (isAssertionMethod(node)) {
                String signature = buildAssertSignature(node);
                int line = node.getLineNo();

                assertSignatures
                        .computeIfAbsent(signature, k -> new ArrayList<>())
                        .add(line);
            }
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            collectAssertions(child, assertSignatures);
            child = child.getNextSibling();
        }
    }

    /**
     * Verifica se é um método de assertion.
     */
    private boolean isAssertionMethod(DetailAST methodCall) {
        String methodName = getMethodName(methodCall);
        return methodName != null &&
                (methodName.startsWith("assert") || methodName.equals("fail"));
    }

    /**
     * Constrói uma assinatura única para a assertion.
     * Formato: "methodName(param1_signature, param2_signature, ...)"
     */
    private String buildAssertSignature(DetailAST methodCall) {
        StringBuilder signature = new StringBuilder();

        String methodName = getMethodName(methodCall);
        signature.append(methodName).append("(");

        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist != null) {
            List<DetailAST> params = getParameters(elist);

            // Desembrulha EXPR se necessário
            List<DetailAST> unwrappedParams = unwrapExpressions(params);

            for (int i = 0; i < unwrappedParams.size(); i++) {
                if (i > 0) {
                    signature.append(", ");
                }
                signature.append(getParameterSignature(unwrappedParams.get(i)));
            }
        }

        signature.append(")");
        return signature.toString();
    }

    /**
     * Cria uma assinatura para um parâmetro individual.
     * Usa representação estrutural da AST para comparar igualdade.
     */
    private String getParameterSignature(DetailAST param) {
        if (param == null) {
            return "null";
        }

        StringBuilder sig = new StringBuilder();
        buildNodeSignature(param, sig);
        return sig.toString();
    }

    /**
     * Constrói recursivamente a assinatura de um nó AST.
     */
    private void buildNodeSignature(DetailAST node, StringBuilder sig) {
        if (node == null) {
            return;
        }

        // Adiciona tipo e texto do nó
        sig.append(node.getType()).append(":");

        String text = node.getText();
        if (text != null && !text.isEmpty()) {
            sig.append(text);
        }

        // Processa filhos
        DetailAST child = node.getFirstChild();
        if (child != null) {
            sig.append("[");
            while (child != null) {
                buildNodeSignature(child, sig);
                child = child.getNextSibling();
                if (child != null) {
                    sig.append(",");
                }
            }
            sig.append("]");
        }
    }

    /**
     * Reporta assertions que aparecem múltiplas vezes.
     */
    private void reportDuplicates(Map<String, List<Integer>> assertSignatures) {
        for (Map.Entry<String, List<Integer>> entry : assertSignatures.entrySet()) {
            List<Integer> lines = entry.getValue();

            // Duplicata: mesma assertion aparece 2+ vezes
            if (lines.size() >= 2) {
                // Reporta na primeira ocorrência
                int firstLine = lines.get(0);

                log(firstLine,
                        "Duplicate Assert: Assertion appears {0} times in this test method. "
                                + "Remove duplicates or splitting into separate test methods.",
                        lines.size());
            }
        }
    }

    /**
     * Desembrulha nós EXPR para pegar valores reais.
     */
    private List<DetailAST> unwrapExpressions(List<DetailAST> params) {
        List<DetailAST> unwrapped = new ArrayList<>();

        for (DetailAST param : params) {
            DetailAST actualValue = param;

            if (param.getType() == TokenTypes.EXPR) {
                actualValue = param.getFirstChild();
            }

            if (actualValue != null && hasContent(actualValue)) {
                unwrapped.add(actualValue);
            }
        }

        return unwrapped;
    }

    /**
     * Verifica se o nó tem conteúdo real.
     */
    private boolean hasContent(DetailAST node) {
        if (node == null) {
            return false;
        }

        String text = node.getText();
        if (text != null && !text.isEmpty()) {
            return true;
        }

        return node.getFirstChild() != null;
    }

    /**
     * Coleta todos os parâmetros de uma lista de expressões.
     */
    private List<DetailAST> getParameters(DetailAST elist) {
        List<DetailAST> params = new ArrayList<>();
        DetailAST param = elist.getFirstChild();

        while (param != null) {
            params.add(param);
            param = param.getNextSibling();
        }

        return params;
    }
}