package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

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
    private Set<String> allowedClasses = new HashSet<>(Arrays.asList("ArrayList", "HashMap")); // Exemplos permitidos
    private boolean ignoreTestFrameworkClasses = true; // Ignora classes como Mockito/JUnit

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
        if (!hasAnnotation(ast, "Test")) return;

        DetailAST methodBody = ast.findFirstToken(TokenTypes.SLIST);
        if (methodBody != null) {
            checkForConstructorCalls(methodBody);
        }
    }

    private void checkForConstructorCalls(DetailAST node) {
        DetailAST current = node.getFirstChild();

        while (current != null) {
            if (current.getType() == TokenTypes.LITERAL_NEW) {
                String className = extractClassName(current);
                if (!isAllowed(className)) {
                    log(current.getLineNo(), "Constructor Initialization detected: Chamada ao construtor ''{0}'' no método de teste", className);
                }
            }

            // Verifica filhos recursivamente
            if (current.hasChildren()) {    
                checkForConstructorCalls(current);
            }

            current = current.getNextSibling();
        }
    }

    private String extractClassName(DetailAST newToken) {
        DetailAST type = newToken.getFirstChild();
        if (type.getType() == TokenTypes.IDENT) {
            return type.getText();
        } else if (type.getType() == TokenTypes.DOT) { // Para classes qualificadas (ex: new com.example.MyClass())
            return type.getLastChild().getText();
        }
        return "-1";
    }

    private boolean isAllowed(String className) {
        // Permite classes configuradas ou do framework de teste
        return allowedClasses.contains(className) || 
               (ignoreTestFrameworkClasses && className.matches("^(Mock|Test|Assert|Matchers).*"));
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
