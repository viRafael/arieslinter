package br.ufba.arieslinter.checks.abstracts;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.Set;

public abstract class AbstractTestSmellCheck extends AbstractCheck {

    protected boolean hasAnnotation(DetailAST ast, String annotationName) {
        DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null)
            return false;

        for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getType() == TokenTypes.ANNOTATION) {
                DetailAST annotationIdent = child.findFirstToken(TokenTypes.IDENT);
                if (annotationIdent != null &&
                        annotationIdent.getText().equals(annotationName)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean hasAnyAnnotation(DetailAST ast, Set<String> annotations) {
        return annotations.stream().anyMatch(ann -> hasAnnotation(ast, ann));
    }

    protected DetailAST findParentMethod(DetailAST ast) {
        DetailAST parent = ast.getParent();

        while (parent != null) {
            if (parent.getType() == TokenTypes.METHOD_DEF) {
                return parent;
            }
            parent = parent.getParent();
        }

        return null;
    }

    protected String getMethodName(DetailAST methodCall) {
        DetailAST identNode = methodCall.findFirstToken(TokenTypes.IDENT);

        if (identNode == null) {
            DetailAST dotNode = methodCall.findFirstToken(TokenTypes.DOT);
            if (dotNode != null) {
                identNode = dotNode.getLastChild();
            }
        }

        return identNode != null ? identNode.getText() : null;
    }
}
