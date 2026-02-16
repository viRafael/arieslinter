package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class EmptyTestCheck extends AbstractTestSmellCheck {

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

        if (isEmptyTest(ast)) {
            log(ast.getLineNo(),
                    "Empty Test: Test method ''{0}'' contains no executable statements. "
                            + "Either implement the test or remove it to avoid false test coverage.",
                    getMethodName(ast));
        }
    }

    /**
     * Verifica se o método de teste está vazio (sem statements executáveis).
     */
    private boolean isEmptyTest(DetailAST methodAst) {
        DetailAST methodBody = methodAst.findFirstToken(TokenTypes.SLIST);

        if (methodBody == null) {
            // Método abstrato ou sem corpo (raro em testes)
            return true;
        }

        return !hasExecutableStatements(methodBody);
    }

    /**
     * Verifica se há pelo menos um statement executável no corpo.
     */
    private boolean hasExecutableStatements(DetailAST slist) {
        DetailAST child = slist.getFirstChild();

        while (child != null) {
            if (isExecutableStatement(child)) {
                return true;
            }
            child = child.getNextSibling();
        }

        return false;
    }

    /**
     * Verifica se o nó é um statement executável (não vazio, não comentário).
     */
    private boolean isExecutableStatement(DetailAST node) {
        if (node == null) {
            return false;
        }

        int type = node.getType();

        // Ignora delimitadores vazios
        if (type == TokenTypes.RCURLY ||
                type == TokenTypes.LCURLY ||
                type == TokenTypes.SEMI ||
                type == TokenTypes.EMPTY_STAT) {
            return false;
        }

        // Statements executáveis válidos
        switch (type) {
            case TokenTypes.VARIABLE_DEF:
                return hasInitialization(node);

            case TokenTypes.EXPR:
                return hasContent(node);

            // Estruturas de controle
            case TokenTypes.LITERAL_IF:
            case TokenTypes.LITERAL_FOR:
            case TokenTypes.LITERAL_WHILE:
            case TokenTypes.LITERAL_DO:
            case TokenTypes.LITERAL_SWITCH:
            case TokenTypes.LITERAL_TRY:
            case TokenTypes.LITERAL_THROW:
            case TokenTypes.LITERAL_RETURN:
            case TokenTypes.LITERAL_BREAK:
            case TokenTypes.LITERAL_CONTINUE:
            case TokenTypes.LITERAL_ASSERT:
                return true;

            // Blocos aninhados
            case TokenTypes.SLIST:
                return hasExecutableStatements(node);

            default:
                return false;
        }
    }

    /**
     * Verifica se uma declaração de variável tem inicialização.
     * Ex: int x = 5;
     */
    private boolean hasInitialization(DetailAST variableDef) {
        DetailAST assign = variableDef.findFirstToken(TokenTypes.ASSIGN);
        return assign != null;
    }

    /**
     * Verifica se uma expressão tem conteúdo real.
     */
    private boolean hasContent(DetailAST expr) {
        if (expr == null) {
            return false;
        }

        DetailAST child = expr.getFirstChild();

        // Se não tem filho, está vazia
        if (child == null) {
            return false;
        }

        // Se o filho é apenas um ponto-e-vírgula, está vazia
        if (child.getType() == TokenTypes.SEMI) {
            return false;
        }

        return true;
    }

    /**
     * Extrai o nome do método.
     */
    protected String getMethodName(DetailAST methodAst) {
        DetailAST identNode = methodAst.findFirstToken(TokenTypes.IDENT);
        return identNode != null ? identNode.getText() : "unknown";
    }
}