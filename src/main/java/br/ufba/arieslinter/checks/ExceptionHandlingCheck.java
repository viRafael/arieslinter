package br.ufba.arieslinter.checks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class ExceptionHandlingCheck extends AbstractCheck {
    private Set<String> testAnnotations = new HashSet<>(Arrays.asList(
            "Test",
            "ParameterizedTest",
            "RepeatedTest",
            "TestFactory",
            "TestTemplate"));

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

        boolean hasTry = containsToken(methodBody, TokenTypes.LITERAL_TRY);
        boolean hasCatch = containsToken(methodBody, TokenTypes.LITERAL_CATCH);
        boolean hasFinally = containsToken(methodBody, TokenTypes.LITERAL_FINALLY);

        if (hasTry || hasCatch || hasFinally) {
            log(ast.getLineNo(), "Exception Handling detected: test method contains try/catch/finally blocks");
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
                    if (annotationIdent != null && testAnnotations.contains(annotationIdent.getText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}