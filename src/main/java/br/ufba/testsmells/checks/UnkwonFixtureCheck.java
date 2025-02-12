package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class UnkwonFixtureCheck extends AbstractCheck {

    private int assertCount = 0;

    @Override
    public int[] getRequiredTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        // Tokens que queremos monitorar: METHOD_DEF e EXPR
        return new int[] { TokenTypes.METHOD_DEF, TokenTypes.LITERAL_ASSERT, TokenTypes.METHOD_CALL};
    }

    @Override
    public void visitToken(DetailAST ast) {

        boolean hasTestAnnotation = hasAnnotation(ast, "Test");
        
        if (ast.getType() == TokenTypes.METHOD_DEF && hasTestAnnotation) {
            // Reinicia o contador ao entrar em um novo método
            assertCount = 0;
        } else if (ast.getType() == TokenTypes.LITERAL_ASSERT) {
            assertCount++;
        } else if (ast.getType() == TokenTypes.METHOD_CALL) {
            if("assert".equals(ast.getFirstChild().getText())) {
                assertCount++;
            }
            if("assertTrue".equals(ast.getFirstChild().getText())) {
                assertCount++;
            }
            if("assertEquals".equals(ast.getFirstChild().getText())) {
                assertCount++;
            }
            if("assertFalse".equals(ast.getFirstChild().getText())) {
                assertCount++;
            }
            if("assertNotEquals".equals(ast.getFirstChild().getText())) {
                assertCount++;
            }
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        boolean hasTestAnnotation = hasAnnotation(ast, "Test");

        if (ast.getType() == TokenTypes.METHOD_DEF && hasTestAnnotation) {
            // Quando sair de um método, verifica se há chamadas 'assert'
            if (assertCount == 0) {
                log(ast.getLineNo()+1, "Número de chamadas 'assert' é ZERO ");
            }
        }
    }

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