package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class SleepyTestCheck extends AbstractCheck {

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

            if (enclosingMethod != null && hasAnnotation(enclosingMethod, "Test")) {
                log(ast.getLineNo(), "Sleepy Test detected: using 'sleep' in tests.");
            }
        }
    }

    private boolean isSleepCall(DetailAST methodCall) {
        DetailAST firstChild = methodCall.getFirstChild();
        if (firstChild == null) {
            return false;
        }

        // Verificação mais robusta para Thread.sleep()
        if (firstChild.getType() == TokenTypes.DOT) {
            DetailAST owner = firstChild.getFirstChild();
            DetailAST method = firstChild.getLastChild();

            return owner.getText().equals("Thread") && method.getText().equals("sleep");
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

    // Método para extrair nomes qualificados de anotações
    private String getFullAnnotationName(DetailAST annotation) {
        DetailAST nameNode = annotation.findFirstToken(TokenTypes.IDENT);

        if (nameNode == null) {
            nameNode = annotation.findFirstToken(TokenTypes.DOT);
            if (nameNode != null) {
                return getDotExpressionName(nameNode);
            }
        }

        return nameNode != null ? nameNode.getText() : "";
    }

    // Método para lidar com anotações qualificadas (ex: @org.junit.Test)
    private String getDotExpressionName(DetailAST dotNode) {
        StringBuilder name = new StringBuilder();
        DetailAST current = dotNode;

        while (current != null) {
            if (current.getType() == TokenTypes.IDENT) {
                name.insert(0, current.getText());
                current = current.getPreviousSibling();
                if (current != null && current.getType() == TokenTypes.DOT) {
                    name.insert(0, ".");
                }
            } else {
                current = current.getFirstChild();
            }
        }

        return name.toString();
    }

    // Métodu Auxiliar
    private boolean hasAnnotation(DetailAST methodAst, String annotationName) {
        DetailAST modifiers = methodAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers != null) {
            for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getType() == TokenTypes.ANNOTATION) {
                    String fullAnnotationName = getFullAnnotationName(child);
                    if (fullAnnotationName.endsWith("." + annotationName) || fullAnnotationName.equals(annotationName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}