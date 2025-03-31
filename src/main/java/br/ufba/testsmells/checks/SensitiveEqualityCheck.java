package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

    // TODO: TESTAR CLASSE SensitiveEqualityCheck

@StatelessCheck
public class SensitiveEqualityCheck extends AbstractCheck {

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
        if (hasAnnotation(ast, "Test")) {
            checkForToStringCalls(ast); 
        }
    }

    private void checkForToStringCalls(DetailAST methodAst) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist != null) {
            scanForMethodCalls(slist);
        }
    }

    private void scanForMethodCalls(DetailAST node) {
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_CALL) {
                processMethodCall(child);
            }
            scanForMethodCalls(child); // Busca recursiva

            child = child.getNextSibling();
        }
    }

    private void processMethodCall(DetailAST methodCall) {
        String methodName = getMethodName(methodCall);
        if ("toString".equals(methodName)) {
            log(methodCall.getLineNo(),
                    "Sensitive Equality detected: do not use toString()");
        }
    }

    private String getMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            return dot.getLastChild().getText(); // Ex: obj.toString() → "toString"
        }

        DetailAST ident = methodCall.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : "";
    }

    // Métodu auxiliar
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