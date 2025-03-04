package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@StatelessCheck
public class AssertionRouletteTestCheck extends AbstractCheck {
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

    private static class AssertInfo {
        String message;
        int line;

        AssertInfo(String message, int line) {
            this.message = message;
            this.line = line;
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (hasAnnotation(ast, "Test")) {
            List<AssertInfo> asserts = collectAsserts(ast);
            checkAssertionRoulette(asserts);
        }
    }

    private List<AssertInfo> collectAsserts(DetailAST methodAst) {
        List<AssertInfo> asserts = new ArrayList<>();
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);

        if (slist != null) {
            DetailAST child = slist.getFirstChild();
            while (child != null) {
                if (child.getType() == TokenTypes.EXPR) {
                    DetailAST methodCall = child.getFirstChild();
                    if (methodCall != null && methodCall.getType() == TokenTypes.METHOD_CALL) {
                        processMethodCall(methodCall, asserts);
                    }
                }
                child = child.getNextSibling();
            }
        }
        return asserts;
    }

    private void processMethodCall(DetailAST methodCall, List<AssertInfo> asserts) {
        String methodName = methodCall.findFirstToken(TokenTypes.IDENT).getText();
        if (methodName.startsWith("assert")) {
            String message = extractMessage(methodCall);
            asserts.add(new AssertInfo(message, methodCall.getLineNo()));
        }
    }

    private String extractMessage(DetailAST methodCall) {
        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        if (elist != null) {
            DetailAST firstParam = elist.getFirstChild();
            if (firstParam != null && firstParam.getType() == TokenTypes.STRING_LITERAL) {
                return firstParam.getText();
            }
        }
        return null;
    }

    private void checkAssertionRoulette(List<AssertInfo> asserts) {
        if (asserts.size() < 2) return;

        // Verifica asserts sem mensagem
        long noMessageCount = asserts.stream().filter(a -> a.message == null).count();
        if (noMessageCount >= 2) {
            log(asserts.get(0).line, "Múltiplos asserts sem mensagem descritiva");
        }

        // Verifica mensagens duplicadas
        Map<String, List<Integer>> messageMap = new HashMap<>();
        for (AssertInfo assertInfo : asserts) {
            if (assertInfo.message != null) {
                messageMap.computeIfAbsent(assertInfo.message, k -> new ArrayList<>())
                        .add(assertInfo.line);
            }
        }

        for (Map.Entry<String, List<Integer>> entry : messageMap.entrySet()) {
            if (entry.getValue().size() >= 2) {
                entry.getValue().forEach(line ->
                        log(line, "Mensagem duplicada em assert: " + entry.getKey()));
            }
        }
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