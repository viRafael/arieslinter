package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

@StatelessCheck
public class DefaultTestCheck extends AbstractTestSmellCheck {
    private Set<String> forbiddenClassNames = new HashSet<>(
            Arrays.asList("ExampleUnitTest", "ExampleInstrumentedTest"));

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.CLASS_DEF };
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
        DetailAST ident = ast.findFirstToken(TokenTypes.IDENT);

        if (ident != null) {
            String className = ident.getText();

            // Verifica se o nome começa com os prefixos proibidos
            for (String forbidden : forbiddenClassNames) {
                if (className.startsWith(forbidden)) {
                    log(ast.getLineNo(), "Default Test detected: rename the class ''{0}'' or delete it", className);
                    break;
                }
            }
        }
    }

    // Permite configurar os nomes proibidos via XML (opcional)
    public void setForbiddenClassNames(String[] names) {
        this.forbiddenClassNames = new HashSet<>(Arrays.asList(names));
    }
}