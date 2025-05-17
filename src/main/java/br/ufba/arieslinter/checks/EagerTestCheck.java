package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

    // TODO: TESTAR CLASSE EagerTestCheck

@StatelessCheck
public class EagerTestCheck extends AbstractCheck {
    private Set<String> excludedMethodPatterns = new HashSet<>(Arrays.asList("assert.*", "verify.*"));
    private int maxAllowedMethods = 1; // Máximo de métodos de produção permitidos

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

        Set<String> productionMethods = new HashSet<>();
        DetailAST methodBody = ast.findFirstToken(TokenTypes.SLIST);

        if (methodBody != null) {
            collectProductionMethodCalls(methodBody, productionMethods);
        }

        if (productionMethods.size() > maxAllowedMethods) {
            log(ast.getLineNo(), "Eager Test detectado: " + productionMethods.size() + " métodos chamados." ,
                    productionMethods.size(), maxAllowedMethods);
        }
    }

    private void collectProductionMethodCalls(DetailAST node, Set<String> productionMethods) {
        DetailAST currentNode = node.getFirstChild();

        while (currentNode != null) {
            if (currentNode.getType() == TokenTypes.METHOD_CALL) {
                String methodName = extractMethodName(currentNode);
                if (methodName != null && !isExcludedMethod(methodName)) {
                    productionMethods.add(methodName);
                }
            }
            // Verifica filhos recursivamente (para capturar chamadas aninhadas)
            if (currentNode.hasChildren()) {
                collectProductionMethodCalls(currentNode, productionMethods);
            }

            currentNode = currentNode.getNextSibling();
        }
    }

    private String extractMethodName(DetailAST methodCallAst) {
        DetailAST firstChild = methodCallAst.getFirstChild();

        if (firstChild.getType() == TokenTypes.DOT) { // Exemplo: obj.metodo()
            return firstChild.getLastChild().getText();
        } else if (firstChild.getType() == TokenTypes.IDENT) { // Exemplo: metodo()
            return firstChild.getText();
        }

        return null;
    }

    private boolean isExcludedMethod(String methodName) {
        return excludedMethodPatterns.stream()
                .anyMatch(methodName::matches);
    }

    public void setMaxAllowedMethods(int max) {
        this.maxAllowedMethods = max;
    }

    // Métodu Auxiliar
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