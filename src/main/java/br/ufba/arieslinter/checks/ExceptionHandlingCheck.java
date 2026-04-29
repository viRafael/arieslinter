package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;

@StatelessCheck
public class ExceptionHandlingCheck extends AbstractTestSmellCheck {
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
        if (!hasTestAnnotation(ast))
            return;

        DetailAST methodBody = ast.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null)
            return;

        boolean hasCatch = containsToken(methodBody, TokenTypes.LITERAL_CATCH);
        boolean hasThrow = containsToken(methodBody, TokenTypes.LITERAL_THROW);

        if (hasCatch || hasThrow) {
            log(ast.getLineNo(), "Exception Handling: Test method contains a catch clause or a throw statement. "
                    + "Use JUnit''s built-in exception handling instead.");
        }
    }

    /**
     * Verifica se existe um token específico dentro do nó fornecido
     * (recursivamente).
     */
    private boolean containsToken(DetailAST node, int tokenType) {
        DetailAST current = node.getFirstChild();

        while (current != null) {
            if (current.getType() == tokenType) {
                return true;
            }

            // Recursão para filhos
            if (current.hasChildren()) {
                if (containsToken(current, tokenType)) {
                    return true;
                }
            }

            current = current.getNextSibling();
        }

        return false;
    }

    // Método Auxiliar
    private boolean hasTestAnnotation(DetailAST methodAst) {
        DetailAST modifiers = methodAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers != null) {
            for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getType() == TokenTypes.ANNOTATION) {
                    DetailAST annotationIdent = child.findFirstToken(TokenTypes.IDENT);
                    if (annotationIdent != null && TestAnnotations.ALL_TEST_ANNOTATIONS.contains(annotationIdent.getText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}