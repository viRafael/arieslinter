package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;


@StatelessCheck
public class VerboseTestCheck extends AbstractCheck {
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
        boolean hasTestAnnotation = hasAnnotation(ast, "Test");

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

    public void setMax(int max) {
        this.max = max;
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

}