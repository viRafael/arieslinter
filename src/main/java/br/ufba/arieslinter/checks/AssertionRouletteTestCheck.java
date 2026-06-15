package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

@StatelessCheck
public class AssertionRouletteTestCheck extends AbstractTestSmellCheck {

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
    if (!hasAnyAnnotation(ast, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
      return;
    }

    List<DetailAST> assertionsWithoutMessage = collectAssertionsWithoutMessage(ast);

    // Assertion Roulette: 2 ou mais assertions sem mensagem
    if (assertionsWithoutMessage.size() >= 2) {
      log(ast.getLineNo(),
          "Assertion Roulette: test method has {0} assertions without explanatory messages, "
              + "add a message as the first parameter to identify which assertion failed.",
          assertionsWithoutMessage.size());
    }
  }

  /**
   * Busca recursiva para encontrar assertions em qualquer profundidade.
   */
  private List<DetailAST> collectAssertionsWithoutMessage(DetailAST methodAst) {
    List<DetailAST> assertionsWithoutMessage = new ArrayList<>();

    DetailAST methodBody = methodAst.findFirstToken(TokenTypes.SLIST);
    if (methodBody != null) {
      scanForAssertions(methodBody, assertionsWithoutMessage);
    }

    return assertionsWithoutMessage;
  }

  /**
   * Escaneia recursivamente por assertions sem mensagem.
   */
  private void scanForAssertions(DetailAST node, List<DetailAST> assertionsWithoutMessage) {
    if (node == null) {
      return;
    }

    if (node.getType() == TokenTypes.METHOD_CALL && isAssertionMethod(node)) {
      if (!hasMessage(node)) {
        assertionsWithoutMessage.add(node);
      }
    }

    // Continua busca recursiva
    DetailAST child = node.getFirstChild();
    while (child != null) {
      scanForAssertions(child, assertionsWithoutMessage);
      child = child.getNextSibling();
    }
  }

  /**
   * Verifica se é um método de assertion.
   */
  private boolean isAssertionMethod(DetailAST methodCall) {
    String methodName = getMethodName(methodCall);
    return methodName != null &&
        (methodName.startsWith("assert") || methodName.equals("fail"));
  }

  /**
   * Verifica se a assertion tem mensagem.
   * Considera as assinaturas do JUnit 4 (mensagem no início) e JUnit 5 (mensagem no fim).
   */
  private boolean hasMessage(DetailAST methodCall) {
    DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
    if (elist == null) {
      return false;
    }

    List<DetailAST> args = getArguments(elist);
    int argCount = args.size();
    String methodName = getMethodName(methodCall);

    if (methodName == null) {
      return false;
    }

    // fail() pode ter 0 ou 1 argumento (a mensagem)
    if ("fail".equals(methodName)) {
      return argCount >= 1 && isStringOrLambda(args.get(0));
    }

    // Assertions com 1 ou 2 parâmetros (assertTrue, assertFalse, assertNull, assertNotNull)
    if (methodName.equals("assertTrue") || methodName.equals("assertFalse") ||
        methodName.equals("assertNull") || methodName.equals("assertNotNull")) {
      if (argCount == 2) {
        // JUnit 4: (message, condition) ou JUnit 5: (condition, message)
        return isStringOrLambda(args.get(0)) || isStringOrLambda(args.get(1));
      }
      return false;
    }

    // Assertions com 2 ou 3 parâmetros (assertEquals, assertNotEquals, assertSame, assertNotSame, assertArrayEquals)
    if (methodName.equals("assertEquals") || methodName.equals("assertNotEquals") ||
        methodName.equals("assertSame") || methodName.equals("assertNotSame") ||
        methodName.equals("assertArrayEquals")) {
      if (argCount == 3) {
        // JUnit 4: (message, expected, actual) ou JUnit 5: (expected, actual, message)
        return isStringOrLambda(args.get(0)) || isStringOrLambda(args.get(2));
      }
      if (argCount == 4) {
        // Caso especial JUnit 4: assertEquals(String message, double expected, double actual, double delta)
        return isStringOrLambda(args.get(0));
      }
      return false;
    }

    // Fallback para outros métodos assertX(...)
    if (argCount >= 2) {
      return isStringOrLambda(args.get(0)) || isStringOrLambda(args.get(argCount - 1));
    }

    return false;
  }

  /**
   * Extrai a lista de expressões (argumentos) de um nó ELIST.
   */
  private List<DetailAST> getArguments(DetailAST elist) {
    List<DetailAST> args = new ArrayList<>();
    DetailAST child = elist.getFirstChild();
    while (child != null) {
      if (child.getType() == TokenTypes.EXPR) {
        args.add(child);
      }
      child = child.getNextSibling();
    }
    return args;
  }

  /**
   * Verifica se um nó de parâmetro é uma String literal ou um Lambda (Supplier de mensagem).
   */
  private boolean isStringOrLambda(DetailAST param) {
    if (param == null) {
      return false;
    }

    DetailAST node = param;
    if (node.getType() == TokenTypes.EXPR) {
      node = node.getFirstChild();
    }

    if (node == null) {
      return false;
    }

    if (node.getType() == TokenTypes.STRING_LITERAL) {
      String text = node.getText();
      return text != null && text.length() > 2; // Ignora ""
    }

    return node.getType() == TokenTypes.LAMBDA;
  }
}