package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class EmptyTestCheck extends AbstractCheck {

    // TODO: TESTAR CLASSE EmptyTestCheck

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
        DetailAST slist = ast.findFirstToken(TokenTypes.SLIST);

        if (hasAnnotation(ast, "Test")) {
            if (isEmptyMethodBody(slist)) {
                log(ast.getLineNo(), "Empty Test detected: delete it or write the test"); 
            }
        }
    }

    private boolean isEmptyMethodBody(DetailAST slist) {
        DetailAST child = slist.getFirstChild();

        // Percorre todos os nós filhos do SLIST
        while (child != null) {
            // Ignora ponto-e-vírgula vazios (ex: { ; })
            if (child.getType() != TokenTypes.SEMI) {
                return false;
            }
            child = child.getNextSibling();
        }
        return true;
    }

    // Métodu Auxiliar
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