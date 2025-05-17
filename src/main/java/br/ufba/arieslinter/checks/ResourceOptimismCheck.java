package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

    // TODO: TESTAR CLASSE ResourceOptimismCheck
//    Verificação por Variável:
//      Apenas rastreia usos da mesma variável. Se o métodu receber um File como parâmetro, não será detectado.
//
//    Análise de Fluxo:
//      Não verifica se a verificação está logicamente antes do uso (apenas se existe no métodu).
//
//    Tipos Genéricos:
//      Assume que o tipo é declarado como File, não considerando imports completos (java.io.File).


@StatelessCheck
public class ResourceOptimismCheck extends AbstractCheck {
    // Métodos de verificação de existência válidos
    private static final Set<String> EXISTENCE_CHECKS = Set.of("exists", "isFile", "notExists");

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
        if (hasAnnotation(ast, "Test")) {
            checkForResourceOptimism(ast);
        }
    }

    private void checkForResourceOptimism(DetailAST methodAst) {
        // Map para rastrear variáveis File e se foram verificadas
        Map<String, Boolean> fileVariables = new HashMap<>();

        collectFileVariables(methodAst, fileVariables);
        checkFileUsage(methodAst, fileVariables);
    }

    // Coleta variáveis do tipo File
    private void collectFileVariables(DetailAST methodAst, Map<String, Boolean> fileVariables) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist == null) return;

        DetailAST child = slist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.VARIABLE_DEF) {
                String typeName = getTypeName(child);

                if ("File".equals(typeName)) {
                    String varName = child.findFirstToken(TokenTypes.IDENT).getText();
                    fileVariables.put(varName, false);
                }
            }
            child = child.getNextSibling();
        }
    }

    // Verifica usos de File sem verificação
    private void checkFileUsage(DetailAST methodAst, Map<String, Boolean> fileVariables) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist == null) return;

        DetailAST child = slist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR) {
                DetailAST methodCall = child.getFirstChild();
                if (methodCall != null && methodCall.getType() == TokenTypes.METHOD_CALL) {
                    processMethodCall(methodCall, fileVariables);
                }
            }
            child = child.getNextSibling();
        }
    }

    private void processMethodCall(DetailAST methodCall, Map<String, Boolean> fileVariables) {
        DetailAST dot = methodCall.getFirstChild();
        if (dot != null && dot.getType() == TokenTypes.DOT) {
            DetailAST target = dot.getFirstChild();
            String methodName = dot.getLastChild().getText();
            String varName = target.getText();

            if (fileVariables.containsKey(varName)) {
                if (EXISTENCE_CHECKS.contains(methodName)) {
                    fileVariables.put(varName, true); // Marca como verificado
                } else if (!fileVariables.get(varName)) {
                    log(methodCall.getLineNo(),
                            "Resource Optimism: '" + varName + "." + methodName + "' sem verificação de existência");
                }
            }
        }
    }

    // Obtém o nome do tipo da variável (ex: "File")
    private String getTypeName(DetailAST variableDef) {
        DetailAST type = variableDef.findFirstToken(TokenTypes.TYPE);
        return type.getFirstChild().getText();
    }

    // Métodu auxiliar
    private boolean hasAnnotation(DetailAST methodAst, String annotationName) {
        DetailAST modifiers = methodAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers != null) {
            for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getType() == TokenTypes.ANNOTATION) {
                    DetailAST annotationIdent = child.findFirstToken(TokenTypes.IDENT);
                    if (annotationIdent != null && annotationIdent.getText().equals(annotationName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
