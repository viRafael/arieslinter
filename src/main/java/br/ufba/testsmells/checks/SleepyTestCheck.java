package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class SleepyTestCheck extends AbstractCheck {

    // TODO: TESTAR CLASSE SleepyTestCheck


    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.METHOD_CALL };
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
        if (isSleepCall(ast)) {
            DetailAST enclosingMethod = findEnclosingMethodDef(ast);
            if (enclosingMethod != null && hasTestAnnotation(enclosingMethod)) {
                log(ast.getLineNo(), "Sleepy Test detected: Avoid using Thread.sleep in tests.");
            }
        }
    }

    private boolean isSleepCall(DetailAST methodCall) {
        DetailAST firstChild = methodCall.getFirstChild();
        if (firstChild == null) {
            return false;
        }

        // Verifica chamadas do tipo Thread.sleep()
        if (firstChild.getType() == TokenTypes.DOT) {
            return "Thread.sleep".equals(firstChild.getText());
        }
        // Verifica chamadas diretas a sleep() (via importação estática)
        else if (firstChild.getType() == TokenTypes.IDENT) {
            return "sleep".equals(firstChild.getText());
        }

        return false;
    }

    private DetailAST findEnclosingMethodDef(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == TokenTypes.METHOD_DEF) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private boolean hasTestAnnotation(DetailAST methodDef) {
        DetailAST modifiers = methodDef.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers != null) {
            for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getType() == TokenTypes.ANNOTATION) {
                    DetailAST annotationIdent = child.findFirstToken(TokenTypes.IDENT);
                    // Considera tanto 'Test' quanto o nome completo (ex: org.junit.Test)
                    if (annotationIdent != null && ("Test".equals(annotationIdent.getText())
                            || "org.junit.Test".equals(annotationIdent.getText()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}