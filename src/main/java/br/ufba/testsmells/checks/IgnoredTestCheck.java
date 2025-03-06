package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

    // TODO: TESTAR CLASSE IgnoredTestCheck

@StatelessCheck
public class IgnoredTestCheck extends AbstractCheck {
    @Override
    public int[] getAcceptableTokens() {
        // Monitora classes e métodos
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF };
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
        if (hasAnnotation(ast, "Ignore")) {
            String elementType = ast.getType() == TokenTypes.CLASS_DEF ? "Classe" : "Método";
            log(ast.getLineNo(), elementType + " de teste ignorado detectado (@Ignore)");
        }
    }

    // Métodu auxiliar
    private boolean hasAnnotation(DetailAST ast, String annotationName) {
        DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
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