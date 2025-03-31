package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.ArrayList;
import java.util.List;

    // TODO: TESTAR CLASSE RedundantAssertionCheck
//    Comparação textual
//      Parâmetros com a mesma representação textual serão considerados iguais, mesmo que sejam variáveis diferentes.
//      Exemplo: assertEquals(a, a) → reportado, mesmo que a seja uma variável.
//
//    Expressões complexas:
//      Não avalia expressões (ex: assertEquals(a + b, a + b)), apenas compara o texto do parâmetro.
//      Saída: Assert redundante: 'assertEquals' (a + b).
//
//    Métodos sem parâmetros:
//      fail() ou assertAll() não serão verificados, pois não têm parâmetros.

@StatelessCheck
public class RedundantAssertionCheck extends AbstractCheck {

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
            checkForRedundantAsserts(ast);
        }
    }

    private void checkForRedundantAsserts(DetailAST methodAst) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);

        if (slist != null) {
            DetailAST child = slist.getFirstChild();
            while (child != null) {
                if (child.getType() == TokenTypes.EXPR) {
                    DetailAST methodCall = child.getFirstChild();
                    if (methodCall != null && methodCall.getType() == TokenTypes.METHOD_CALL) {
                        processMethodCall(methodCall);
                    }
                }
                
                child = child.getNextSibling();
            }
        }
    }

    private void processMethodCall(DetailAST methodCall) {
        String methodName = methodCall.findFirstToken(TokenTypes.IDENT).getText();
        if (!methodName.startsWith("assert")) return;

        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        List<DetailAST> params = getChildren(elist);

        // Verifica se o primeiro parâmetro é uma mensagem (STRING_LITERAL)
        boolean hasMessage = !params.isEmpty() && params.get(0).getType() == TokenTypes.STRING_LITERAL;
        List<DetailAST> relevantParams = hasMessage ? params.subList(1, params.size()) : params;

        // Verifica parâmetros redundantes
        checkForIdenticalParams(methodCall, methodName, relevantParams);
    }

    private void checkForIdenticalParams(DetailAST methodCall, String methodName, List<DetailAST> params) {
        for (int i = 0; i < params.size(); i++) {
            for (int j = i + 1; j < params.size(); j++) {
                if (params.get(i).getText().equals(params.get(j).getText())) {
                    //String paramValue = params.get(i).getText();
                    log(methodCall.getLineNo(),
                            // "Assert redundante em '" + methodName + "': parâmetros idênticos (" + paramValue + ")");
                            "Redundant Assertion deteced: refactoring ou remove it");
                    return; // Reporta apenas uma vez por assert
                }
            }
        }
    }

    private List<DetailAST> getChildren(DetailAST node) {
        List<DetailAST> children = new ArrayList<>();
        DetailAST child = node.getFirstChild();
        while (child != null) {
            children.add(child);
            child = child.getNextSibling();
        }
        return children;
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