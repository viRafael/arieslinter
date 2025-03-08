package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

    // TODO: TESTAR CLASSE ConstructorInitializationCheck

//// Caso 1: Classe de Teste com Construtor Explícito → DEVE REPORTAR (Linha 3)
//    public class CalculatorTest {
//        public CalculatorTest() {
//            // Smell: inicialização no construtor
//        }
//
//        @Test
//        public void testSum() { /*...*/ }
//    }
//
//// Caso 2: Classe Não-Teste com Construtor → NÃO REPORTAR
//class InvalidClass {
//    public InvalidClass() {
//        System.out.println("Não é um teste!");
//    }
//}
//
//// Caso 3: Classe Válida com setUp() → NÃO REPORTAR
//class ValidTest {
//    @Before
//    public void setUp() { /*...*/ }
//
//    @Test
//    public void testValid() { /*...*/ }
//}
//
//// Caso 5: Classe Interna → NÃO REPORTAR
//class MainTest {
//    @Test
//    public void testMain() { /*...*/ }
//
//    class InnerTestClass {
//        public InnerTestClass() { /*...*/ }
//    }
//}
//
//// Caso 6: Construtor Privado → DEVE REPORTAR (Linha 40)
//class PrivateConstructorTest {
//    private PrivateConstructorTest() { /*...*/ }
//
//    @Test
//    public void testPrivate() { /*...*/ }
//}
//
//// Caso 8: Múltiplos Construtores → 2 REPORTES (Linhas 55 e 59)
//class MultipleConstructorsTest {
//    public MultipleConstructorsTest() { /*...*/ }
//
//    public MultipleConstructorsTest(int param) { /*...*/ }
//
//    @Test
//    public void testA() { /*...*/ }
//}

@StatelessCheck
public class ConstructorInitializationCheck extends AbstractCheck {

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
        DetailAST classIdent = ast.findFirstToken(TokenTypes.IDENT);
        if (classIdent == null) return;
        String className = classIdent.getText();

        // Verifica se é uma classe de teste (possui métodos @Test)
        if (!isTestClass(ast)) return;

        // Procura por construtores na classe
        checkForConstructors(ast, className);
    }

    private boolean isTestClass(DetailAST classAst) {
        DetailAST objBlock = classAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) return false;

        // Verifica se há pelo menos um métodu com @Test
        DetailAST method = objBlock.getFirstChild();
        while (method != null) {
            if (method.getType() == TokenTypes.METHOD_DEF && hasAnnotation(method, "Test")) {
                return true;
            }
            method = method.getNextSibling();
        }
        return false;
    }

    private void checkForConstructors(DetailAST classAst, String className) {
        DetailAST objBlock = classAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) return;

        DetailAST method = objBlock.getFirstChild();
        while (method != null) {
            if (method.getType() == TokenTypes.METHOD_DEF) {
                processMethod(method, className);
            }
            method = method.getNextSibling();
        }
    }

    private void processMethod(DetailAST methodDef, String className) {
        // Obtém nome do métodu
        DetailAST methodIdent = methodDef.findFirstToken(TokenTypes.IDENT);
        String methodName = methodIdent.getText();

        // Verifica se é um construtor (nome igual ao da classe)
        if (methodName.equals(className)) {
            log(methodDef.getLineNo(),
                    "Constructor Initialization detectado. Utilize o setUp()");
        }
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
