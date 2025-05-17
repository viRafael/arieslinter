package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class ConditionalTestLogicCheck extends AbstractCheck {
    // Lista de tokens para estruturas condicionais/iterativas
    private static final int[] CONDITIONAL_TOKENS = {
            TokenTypes.LITERAL_IF,
            TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_WHILE,
            TokenTypes.LITERAL_DO,
            TokenTypes.LITERAL_SWITCH,
            TokenTypes.QUESTION        // Para operador ternário (?)
    };

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
        if (hasAnnotation(ast, "Test") && containsConditionalLogic(ast)) {
            log(ast.getLineNo(), "Conditional Test Logic detected: test contains complex conditional logic");
        }
    }

    private boolean containsConditionalLogic(DetailAST methodAst) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist != null) {
            return checkChildrenForConditionals(slist);
        }
        return false;
    }

    private boolean checkChildrenForConditionals(DetailAST node) {
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (isConditionalToken(child.getType())) {
                return true;
            }
            if (checkChildrenForConditionals(child)) {
                return true;
            }
            child = child.getNextSibling();
        }
        return false;
    }

    private boolean isConditionalToken(int tokenType) {
        for (int conditionalToken : CONDITIONAL_TOKENS) {
            if (tokenType == conditionalToken) {
                return true;
            }
        }
        return false;
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