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
          "Assertion Roulette: Test method has {0} assertions without explanatory messages. "
              + "Add a message as the first parameter to identify which assertion failed.",
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
   * A mensagem é o PRIMEIRO parâmetro, se for String literal ou lambda.
   */
  private boolean hasMessage(DetailAST methodCall) {
    DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
    if (elist == null) {
      return false;
    }

    DetailAST firstParam = elist.getFirstChild();
    if (firstParam == null) {
      return false;
    }

    // Desembrulha EXPR se necessário
    if (firstParam.getType() == TokenTypes.EXPR) {
      firstParam = firstParam.getFirstChild();
    }

    if (firstParam == null) {
      return false;
    }

    // Primeiro parâmetro é String literal → tem mensagem
    if (firstParam.getType() == TokenTypes.STRING_LITERAL) {
      // Verifica se a String não está vazia
      String text = firstParam.getText();
      return text != null && text.length() > 2;
    }

    // Primeiro parâmetro é lambda → tem mensagem (Supplier<String>)
    if (firstParam.getType() == TokenTypes.LAMBDA) {
      return true;
    }

    return false;
  }
}