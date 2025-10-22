package br.ufba.arieslinter.checks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class SensitiveEqualityCheck extends AbstractCheck {

    // Anotações de teste conhecidas
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
        if (hasTestAnnotation(ast)) {
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

    // Método Auxiliar - Corrigido para detectar múltiplas anotações de teste
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