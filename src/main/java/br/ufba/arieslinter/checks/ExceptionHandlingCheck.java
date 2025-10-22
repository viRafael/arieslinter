package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class ExceptionHandlingCheck extends AbstractCheck {
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

        if (hasThrow || hasCatch) {
            log(ast.getLineNo(), "Exception Handling detected: Use JUnit exception handling instead of manual throw/catch");
        }
    }

    private boolean containsThrowStatement(DetailAST node) {
        return scanForToken(node, TokenTypes.LITERAL_THROW);
    }

    /**
     * Verifica se há catch clause, EXCLUINDO try-with-resources.
     * Try-with-resources é apenas para gerenciamento de recursos, não para exception handling.
     */
    private boolean containsCatchClause(DetailAST node) {
        DetailAST current = node.getFirstChild();

        while (current != null) {
            // Procura por blocos LITERAL_TRY
            if (current.getType() == TokenTypes.LITERAL_TRY) {
                // Verifica se NÃO é try-with-resources
                if (!isTryWithResources(current)) {
                    // Agora procura por LITERAL_CATCH dentro deste try
                    if (hasCatchBlock(current)) {
                        return true;
                    }
                }
            }

            // Recursão para filhos
            if (current.hasChildren()) {
                if (containsCatchClause(current)) {
                    return true;
                }
            }

            current = current.getNextSibling();
        }

        return false;
    }

    /**
     * Verifica se um bloco TRY é try-with-resources.
     * Try-with-resources tem um RESOURCE_SPECIFICATION como filho.
     */
    private boolean isTryWithResources(DetailAST tryNode) {
        return tryNode.findFirstToken(TokenTypes.RESOURCE_SPECIFICATION) != null;
    }

    /**
     * Verifica se um bloco TRY tem pelo menos um CATCH.
     */
    private boolean hasCatchBlock(DetailAST tryNode) {
        DetailAST child = tryNode.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.LITERAL_CATCH) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
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

    // Método Auxiliar
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