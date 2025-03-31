package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class ExceptionHandlingCheck extends AbstractCheck {
    private boolean allowThrow = false;  // Permite configurar via XML se deseja ignorar throw
    private boolean allowCatch = false;  // Permite configurar via XML se deseja ignorar catch

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
        if (!hasAnnotation(ast, "Test")) return;

        DetailAST methodBody = ast.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) return;

        boolean hasThrow = containsThrowStatement(methodBody);
        boolean hasCatch = containsCatchClause(methodBody);

        if ((!allowThrow && hasThrow) || (!allowCatch && hasCatch)) {
            log(ast.getLineNo(), "Exception Handling detected: Use JUnit exception handling instead of manual throw/catch");
        }
    }

    private boolean containsThrowStatement(DetailAST node) {
        return scanForToken(node, TokenTypes.LITERAL_THROW);
    }

    private boolean containsCatchClause(DetailAST node) {
        return scanForToken(node, TokenTypes.LITERAL_CATCH);
    }
            
    private boolean scanForToken(DetailAST node, int tokenType) {
        DetailAST current = node.getFirstChild();

        while (current != null) {
            if (current.getType() == tokenType) {
                return true;
            }
            if (current.hasChildren()) {
                if (scanForToken(current, tokenType)) {
                    return true;
                }
            }

            current = current.getNextSibling();
        }

        return false;
    }

    // Setters para configuração via XML
    public void setAllowThrow(boolean allow) {
        this.allowThrow = allow;
    }

    public void setAllowCatch(boolean allow) {
        this.allowCatch = allow;
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