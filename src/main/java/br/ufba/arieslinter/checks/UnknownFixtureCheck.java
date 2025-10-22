package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

@StatelessCheck
public class UnknownFixtureCheck extends AbstractCheck {
    private int assertCount = 0;

    private Set<String> testAnnotations = new HashSet<>(Arrays.asList(
            "Test",
            "ParameterizedTest",
            "RepeatedTest",
            "TestFactory",
            "TestTemplate"));

    private Set<String> assertionMethods = new HashSet<>(Arrays.asList(
            "assert",
            "assertTrue",
            "assertFalse",
            "assertEquals",
            "assertNotEquals",
            "assertNull",
            "assertNotNull",
            "assertSame",
            "assertNotSame",
            "assertArrayEquals",
            "assertThrows",
            "assertDoesNotThrow",
            "assertTimeout",
            "assertTimeoutPreemptively",
            "assertAll",
            "assertIterableEquals",
            "assertLinesMatch",
            "fail"));

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
        return new int[] { TokenTypes.METHOD_DEF, TokenTypes.LITERAL_ASSERT, TokenTypes.METHOD_CALL };
    }

    @Override
    public void visitToken(DetailAST ast) {

        boolean hasTestAnnotation = hasTestAnnotation(ast);

        if (ast.getType() == TokenTypes.METHOD_DEF && hasTestAnnotation) {
            // Reinicia o contador ao entrar em um novo método
            assertCount = 0;

        } else if (ast.getType() == TokenTypes.LITERAL_ASSERT) {
            assertCount++;
        } else if (ast.getType() == TokenTypes.METHOD_CALL) {
            String methodName = getMethodName(ast);
            if (methodName != null && assertionMethods.contains(methodName)) {
                assertCount++;
            }
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        boolean hasTestAnnotation = hasTestAnnotation(ast);

        if (ast.getType() == TokenTypes.METHOD_DEF && hasTestAnnotation) {
            // Quando sair de um método, verifica se há chamadas 'assert'
            if (assertCount == 0) {
                log(ast.getLineNo() + 1, "Unknown Test detected: without assertions");
            }
        }
    }

    private String getMethodName(DetailAST methodCallAst) {
        DetailAST firstChild = methodCallAst.getFirstChild();
        if (firstChild == null)
            return null;

        // Pode ser um IDENT simples ou um DOT (para chamadas qualificadas)
        if (firstChild.getType() == TokenTypes.IDENT) {
            return firstChild.getText();
        } else if (firstChild.getType() == TokenTypes.DOT) {
            DetailAST methodNameNode = firstChild.getLastChild();
            return methodNameNode != null ? methodNameNode.getText() : null;
        }

        return null;
    }

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