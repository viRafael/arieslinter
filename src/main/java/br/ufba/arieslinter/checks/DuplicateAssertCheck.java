package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.Map;

@StatelessCheck
public class DuplicateAssertCheck extends AbstractCheck {

    // TODO: TESTAR CLASSE DuplicateAssertCheck
    // Exemplos para teste
//    assertEquals(x, y);  // Signature: assertEquals(x:y)
//    assertEquals(y, x);  // Signature: assertEquals(y:x) → Não é duplicata
//
//    assertEquals(2, x + y);  // Signature: assertEquals(2:x+y)
//    assertEquals(2, y + x);  // Signature: assertEquals(2:y+x) → Diferente!
//
//    assertEquals("Erro", 2, x);  // assertEquals(Erro:2:x)
//    assertEquals(2, x);           // assertEquals(2:x) → Diferente!
//
//    assertEquals(5, result);  // Signature: assertEquals(5:result)
//    assertTrue(5 == result);   // Signature: assertTrue(5==result) → Não é duplicata

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
            checkDuplicateAsserts(ast);
        }
    }

    private void checkDuplicateAsserts(DetailAST methodAst) {
        Map<String, Integer> assertSignatures = new HashMap<>();
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);

        if (slist != null) {
            DetailAST child = slist.getFirstChild();
            while (child != null) {
                if (child.getType() == TokenTypes.EXPR) {
                    DetailAST methodCall = child.getFirstChild();
                    if (isAssertCall(methodCall)) {
                        processAssertCall(methodCall, assertSignatures);
                    }
                }
                checkDuplicateAsserts(methodAst); // Busca Recursiva

                child = child.getNextSibling();
            }
        }
    }

    private boolean isAssertCall(DetailAST methodCall) {
        return methodCall != null
                && methodCall.getType() == TokenTypes.METHOD_CALL
                && methodCall.findFirstToken(TokenTypes.IDENT).getText().startsWith("assert");
    }

    private void processAssertCall(DetailAST methodCall, Map<String, Integer> assertSignatures) {
        String signature = buildAssertSignature(methodCall);
        assertSignatures.merge(signature, 1, Integer::sum);

        if (assertSignatures.get(signature) == 2) {
            log(methodCall.getLineNo(), "Duplicate Assert deteced: " + signature + ", remove one");
        }
    }

    private String buildAssertSignature(DetailAST methodCall) {
        StringBuilder signature = new StringBuilder();
        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);

        if (elist != null) {
            DetailAST param = elist.getFirstChild();
            while (param != null) {
                signature.append(param.getText()).append(":");
                param = param.getNextSibling();
            }
        }

        return methodCall.findFirstToken(TokenTypes.IDENT).getText()
                + "(" + signature.toString().replaceAll(":$", "") + ")";
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