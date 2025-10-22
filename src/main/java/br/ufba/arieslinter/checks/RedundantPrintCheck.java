package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.*;

@StatelessCheck
public class RedundantPrintCheck extends AbstractCheck {
    private Set<String> forbiddenMethodNames = new HashSet<>(Arrays.asList("print", "println", "printf", "write"));
    private Set<String> forbiddenQualifiers = new HashSet<>(Arrays.asList("System.out", "System.err"));

    // Anotações de teste conhecidas
    private Set<String> testAnnotations = new HashSet<>(Arrays.asList(
            "Test",
            "ParameterizedTest",
            "RepeatedTest",
            "TestFactory",
            "TestTemplate"));

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
        if (!hasTestAnnotation(ast))
            return;

        DetailAST methodBody = ast.findFirstToken(TokenTypes.SLIST);
        if (methodBody != null) {
            checkForPrintStatements(methodBody);
        }
    }

    private void checkForPrintStatements(DetailAST node) {
        DetailAST current = node.getFirstChild();

        while (current != null) {
            if (current.getType() == TokenTypes.METHOD_CALL) {
                String methodName = getMethodName(current);
                String qualifier = getQualifier(current);

                boolean isForbidden = (methodName != null && forbiddenMethodNames.contains(methodName)) ||
                        (qualifier != null && forbiddenQualifiers.contains(qualifier));

                if (isForbidden) {
                    log(current.getLineNo(), "Redundant Print detected: method ''{0}'', remove it",
                            methodName != null ? methodName : qualifier);
                }
            }
            if (current.hasChildren()) {
                checkForPrintStatements(current);
            }
            current = current.getNextSibling();
        }
    }

    private String getMethodName(DetailAST methodCallAst) {
        DetailAST dotOrIdent = methodCallAst.getFirstChild();
        if (dotOrIdent == null)
            return null;

        if (dotOrIdent.getType() == TokenTypes.DOT) {
            DetailAST methodNameNode = dotOrIdent.getLastChild();
            return methodNameNode != null ? methodNameNode.getText() : null;
        } else if (dotOrIdent.getType() == TokenTypes.IDENT) {
            return dotOrIdent.getText();
        }

        return null;
    }

    private String getQualifier(DetailAST methodCallAst) {
        DetailAST dotOrIdent = methodCallAst.getFirstChild();
        if (dotOrIdent == null || dotOrIdent.getType() != TokenTypes.DOT)
            return null;

        List<String> parts = new ArrayList<>();
        DetailAST current = dotOrIdent;
        while (current != null && current.getType() == TokenTypes.DOT) {
            DetailAST lastChild = current.getLastChild();
            if (lastChild != null) {
                parts.add(lastChild.getText());
            }
            current = current.getFirstChild();
        }

        if (current != null) {
            parts.add(current.getText());
        }

        Collections.reverse(parts);
        return parts.size() > 1 ? String.join(".", parts.subList(0, parts.size() - 1)) : // Remove o método
                null;
    }

    // Método Auxiliar - Corrigido para detectar múltiplas anotações de teste
    private boolean hasTestAnnotation(DetailAST methodAst) {
        DetailAST modifiers = methodAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers != null) {
            for (DetailAST child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getType() == TokenTypes.ANNOTATION) {
                    DetailAST annotationIdent = child.findFirstToken(TokenTypes.IDENT);
                    if (annotationIdent != null && testAnnotations.contains(annotationIdent.getText())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}