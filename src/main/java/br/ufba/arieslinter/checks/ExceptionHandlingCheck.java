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

        if (hasExceptionHandling(methodBody)) {
            log(ast.getLineNo(), "Exception Handling: test method contains a try block, a catch clause, or a throw statement; "
                    + "use JUnit''s built-in exception handling instead.");
        }
    }

    /**
     * Verifica se o método possui tratamento de exceção (try, catch ou throw).
     */
    private boolean hasExceptionHandling(DetailAST node) {
        DetailAST current = node.getFirstChild();

        while (current != null) {
            if (current.getType() == TokenTypes.LITERAL_CATCH) {
                return true;
            }

            if (current.getType() == TokenTypes.LITERAL_THROW) {
                return true;
            }

            if (current.getType() == TokenTypes.LITERAL_TRY) {
                return true;
            }

            // Recursão para nós filhos
            if (current.hasChildren()) {
                if (hasExceptionHandling(current)) {
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