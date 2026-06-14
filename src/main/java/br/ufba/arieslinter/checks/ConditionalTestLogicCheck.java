package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

@StatelessCheck
public class ConditionalTestLogicCheck extends AbstractTestSmellCheck {
  private static final int[] CONDITIONAL_TOKENS = {
    TokenTypes.LITERAL_IF,
    TokenTypes.LITERAL_FOR,
    TokenTypes.LITERAL_WHILE,
    TokenTypes.LITERAL_DO,
    TokenTypes.LITERAL_SWITCH,
    TokenTypes.QUESTION, // Para operador ternário (?)
  };

  @Override
  public int[] getAcceptableTokens() {
    return new int[] {TokenTypes.METHOD_DEF};
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
    if (hasAnyAnnotation(ast, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
      DetailAST slist = ast.findFirstToken(TokenTypes.SLIST);
      if (slist != null) {
        checkAndLogConditionals(slist);
      }
    }
  }

  private void checkAndLogConditionals(DetailAST node) {
    DetailAST child = node.getFirstChild();
    while (child != null) {
      if (isConditionalToken(child.getType())) {
        String statementName = getStatementName(child.getType());
        log(child.getLineNo(), "Conditional Test Logic: remove the " + statementName + " and split the test cases into separate test methods.");
      }
      checkAndLogConditionals(child);
      child = child.getNextSibling();
    }
  }

  private String getStatementName(int tokenType) {
    switch (tokenType) {
      case TokenTypes.LITERAL_IF:
        return "''if'' statement";
      case TokenTypes.LITERAL_FOR:
        return "''for'' loop";
      case TokenTypes.LITERAL_WHILE:
        return "''while'' loop";
      case TokenTypes.LITERAL_DO:
        return "''do-while'' loop";
      case TokenTypes.LITERAL_SWITCH:
        return "''switch'' statement";
      case TokenTypes.QUESTION:
        return "ternary operator";
      default:
        return "conditional structure";
    }
  }

  private boolean isConditionalToken(int tokenType) {
    for (int conditionalToken : CONDITIONAL_TOKENS) {
      if (tokenType == conditionalToken) {
        return true;
      }
    }
    return false;
  }
}