package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;

@StatelessCheck
public class VerboseTestCheck extends AbstractTestSmellCheck {
    private int max;

    public VerboseTestCheck() {
        this.max = 30;
    }

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
        DetailAST slist = ast.findFirstToken(TokenTypes.SLIST);
        boolean hasTestAnnotation = hasAnyAnnotation(ast, TestAnnotations.ALL_TEST_ANNOTATIONS);

        if (slist != null && hasTestAnnotation == true) {
            // Encontra a chave de fechamento '}' (último filho do SLIST)
            int startLine = slist.getLineNo(); // Linha inicial (abertura '{')
            int endLine = slist.getLastChild().getLineNo(); // Linha final (fechamento '}')

            // Calcula o número de linhas no método
            int methodLength = endLine - startLine + 1;
            if (methodLength > max) {
                log(startLine, "Verbose Test detected: method with " + methodLength + " lines.");

            }
        }
    }
}