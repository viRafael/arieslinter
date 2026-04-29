package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

/**
 * Detects Unknown Test smell (assertionless test).
 * Detection: A test method that does not contain a single assertion statement 
 * and does not have the 'expected' parameter in the @Test annotation.
 */
@StatelessCheck
public class UnknownTestCheck extends AbstractTestSmellCheck {
    
    private final Set<String> assertionMethods = new HashSet<>(Arrays.asList(
            "assert", "assertTrue", "assertFalse", "assertEquals", "assertNotEquals",
            "assertNull", "assertNotNull", "assertSame", "assertNotSame",
            "assertArrayEquals", "assertThrows", "assertDoesNotThrow", "assertTimeout",
            "assertTimeoutPreemptively", "assertAll", "assertIterableEquals",
            "assertLinesMatch", "fail", "assertThat"));

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

        if (hasExpectedException(ast)) {
            return;
        }

        if (!hasAssertions(ast)) {
            log(ast.getLineNo(), "Unknown Test: Test method ''{0}'' contains no assertion statements.",
                    getMethodName(ast));
        }
    }

    /**
     * Verifica se o método contém alguma assertion (incluindo a keyword 'assert' do Java).
     */
    private boolean hasAssertions(DetailAST methodAst) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist == null) {
            return false;
        }

        return scanForAssertions(slist);
    }

    private boolean scanForAssertions(DetailAST node) {
        if (node == null) return false;

        // Keyword assert do Java
        if (node.getType() == TokenTypes.LITERAL_ASSERT) {
            return true;
        }

        // Chamadas de método (assertEquals, etc)
        if (node.getType() == TokenTypes.METHOD_CALL) {
            String methodName = getMethodNameFromCall(node);
            if (methodName != null && assertionMethods.contains(methodName)) {
                return true;
            }
        }

        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (scanForAssertions(child)) {
                return true;
            }
            child = child.getNextSibling();
        }

        return false;
    }

    /**
     * Verifica se a anotação @Test possui o parâmetro 'expected'.
     */
    private boolean hasExpectedException(DetailAST methodAst) {
        DetailAST modifiers = methodAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) return false;

        for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == TokenTypes.ANNOTATION) {
                if (isAnnotationWithExpectedParam(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAnnotationWithExpectedParam(DetailAST annotationNode) {
        DetailAST ident = annotationNode.findFirstToken(TokenTypes.IDENT);
        if (ident == null || !ident.getText().equals("Test")) {
            return false;
        }

        // Procura por 'expected' dentro da anotação
        // ANNOTATION -> AT, IDENT, LPAREN, ELIST (ou ANNOTATION_MEMBER_VALUE_PAIR), RPAREN
        return hasMember(annotationNode, "expected");
    }

    private boolean hasMember(DetailAST node, String memberName) {
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                DetailAST memberIdent = child.findFirstToken(TokenTypes.IDENT);
                if (memberIdent != null && memberName.equals(memberIdent.getText())) {
                    return true;
                }
            }
            if (hasMember(child, memberName)) return true;
            child = child.getNextSibling();
        }
        return false;
    }

    private String getMethodNameFromCall(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            return dot.getLastChild().getText();
        }
        DetailAST ident = methodCall.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : null;
    }

    protected String getMethodName(DetailAST methodAst) {
        DetailAST ident = methodAst.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : "unknown";
    }
}
