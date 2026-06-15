package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;

@StatelessCheck
public class IgnoredTestCheck extends AbstractTestSmellCheck {
    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF };
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
        if (hasAnnotation(ast, TestAnnotations.IGNORE)) {
            String elementType = ast.getType() == TokenTypes.CLASS_DEF ? "class" : "method";
            log(ast.getLineNo(), "Ignored test detected: " + elementType + " with @Ignore, remove it");
        }
    }
}