package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class MagicNumberCheck extends AbstractCheck {

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {
                TokenTypes.NUM_INT,
                TokenTypes.NUM_LONG,
                TokenTypes.NUM_DOUBLE,
                TokenTypes.NUM_FLOAT
        };
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
        if (isInStaticFinalField(ast) || isInAnnotation(ast)) {
            return;
        }

        if (isInTestMethod(ast) && isInMethodArgumentOrAssert(ast)) {
            log(ast.getLineNo(), "Magic number detected: use a variable with a self-expanatory name");
        }
    }

    private DetailAST findParentMethod(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == TokenTypes.METHOD_DEF) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private boolean isInStaticFinalField(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == TokenTypes.VARIABLE_DEF) {
                DetailAST modifiers = parent.findFirstToken(TokenTypes.MODIFIERS);
                if (modifiers != null) {    
                    // Usar LITERAL_STATIC e LITERAL_FINAL
                    boolean isStatic = modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
                    boolean isFinal = modifiers.findFirstToken(TokenTypes.FINAL) != null;
                    return isStatic && isFinal;
                }
            }
            parent = parent.getParent();
        }
        return false;
    }

    private boolean isInAnnotation(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == TokenTypes.ANNOTATION) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private boolean isInTestMethod(DetailAST ast) {
        DetailAST methodDef = findParentMethod(ast);
        return methodDef != null && hasAnnotation(methodDef, "Test");
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

    // Novo métodu para verificar contexto de uso
    private boolean isInMethodArgumentOrAssert(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            // Caso 1: Argumento de métodu (ex: assertEquals(250, ...))
            if (parent.getType() == TokenTypes.ELIST) {
                DetailAST grandParent = parent.getParent();
                if (grandParent.getType() == TokenTypes.METHOD_CALL) {
                    return true;
                }
            }

            // Caso 2: Assert (ex: assertTrue(x > 5))
            if (parent.getType() == TokenTypes.EXPR) {
                DetailAST exprParent = parent.getParent();
                if (exprParent != null && exprParent.getType() == TokenTypes.LITERAL_ASSERT) {
                    return true;
                }
            }
            parent = parent.getParent();
        }
        return false;
    }
}   