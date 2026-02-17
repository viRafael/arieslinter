package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class ConstructorInitializationCheck extends AbstractTestSmellCheck {

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {TokenTypes.CLASS_DEF};
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
        if (!isTestClass(ast)) {
            return;
        }

        checkForConstructors(ast);
    }

    /**
     * Verifica se é uma classe de teste pelo nome ou por ter métodos @Test.
     */
    private boolean isTestClass(DetailAST classDefAst) {
        String className = getClassName(classDefAst);
        if (className != null &&
            (className.endsWith("Test") || className.endsWith("Tests"))) {
            return true;
        }

        return hasTestMethods(classDefAst);
    }

    /**
     * Procura construtores declarados na classe de teste.
     */
    private void checkForConstructors(DetailAST classDefAst) {
        String className = getClassName(classDefAst);

        DetailAST objBlock = classDefAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return;
        }

        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.CTOR_DEF) {
                log(child.getLineNo(),
                    "Constructor Initialization: Test class ''{0}'' defines a constructor. "
                        + "Use @Before/@BeforeEach for field initialization instead.",
                    className);
            }
            child = child.getNextSibling();
        }
    }

    /**
     * Obtém o nome da classe.
     */
    private String getClassName(DetailAST classDefAst) {
        DetailAST identNode = classDefAst.findFirstToken(TokenTypes.IDENT);
        return identNode != null ? identNode.getText() : null;
    }

    /**
     * Verifica se a classe tem pelo menos um método @Test.
     */
    private boolean hasTestMethods(DetailAST classDefAst) {
        DetailAST objBlock = classDefAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return false;
        }

        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF &&
                hasAnyAnnotation(child, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
                return true;
            }
            child = child.getNextSibling();
        }

        return false;
    }
}