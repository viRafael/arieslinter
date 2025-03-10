package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

    // TODO: TESTAR CLASSE RedundantPrintCheck
// Caso para teste
//        @Test
//        public void testValid() {
//            logger.info("Log válido"); // Não reporta
//        }

@StatelessCheck
public class RedundantPrintCheck extends AbstractCheck {
    private static final Set<String> PRINT_METHODS = new HashSet<>(
            Arrays.asList("print", "println", "printf", "write")
    );

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
            checkForPrintStatements(ast);
        }
    }

    private void checkForPrintStatements(DetailAST methodAst) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist != null) {
            scanForPrintCalls(slist);
        }
    }

    private void scanForPrintCalls(DetailAST node) {
        DetailAST child = node.getFirstChild();

        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_CALL) {
                processMethodCall(child);
            }
            scanForPrintCalls(child); // Busca recursiva

            child = child.getNextSibling();
        }
    }

    private void processMethodCall(DetailAST methodCall) {
        String methodName = getMethodName(methodCall);
        String target = getMethodTarget(methodCall);

        if (isSystemOutOrErr(target) && PRINT_METHODS.contains(methodName)) {
            log(methodCall.getLineNo(),
                    "Redundant Print detectado: " + target + "." + methodName);
        }
    }

    private String getMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null && dot.getLastChild().getType() == TokenTypes.IDENT) {
            return dot.getLastChild().getText();
        }
        return "";
    }

    private String getMethodTarget(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            // System.out.println → "System.out"
            return dot.getFirstChild().getText();
        }
        return "";
    }

    private boolean isSystemOutOrErr(String target) {
        return target.equals("System.out") || target.equals("System.err");
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