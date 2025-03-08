package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

    // TODO: TESTAR CLASSE DefaultTestCheck

@StatelessCheck
public class DefaultTestCheck extends AbstractCheck {
    private Set<String> forbiddenClassNames = new HashSet<>(Arrays.asList("ExampleUnitTest"
            , "ExampleInstrumentedTest"));

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
        // Obtém o nome da classe
        DetailAST ident = ast.findFirstToken(TokenTypes.IDENT);
        if (ident != null) {
            String className = ident.getText();

            // Verifica se o nome está na lista de proibidos
            if (forbiddenClassNames.contains(className)) {
                log(ast.getLineNo(), "Default Test detectado.", className);
            }
        }
    }

    // Permite configurar os nomes proibidos via XML (opcional)
    public void setForbiddenClassNames(String[] names) {
        this.forbiddenClassNames = new HashSet<>(Arrays.asList(names));
    }
}