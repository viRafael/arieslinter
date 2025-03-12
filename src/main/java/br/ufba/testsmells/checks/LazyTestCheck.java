package br.ufba.testsmells.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

    //TODO: TESTAR CLASSE LazyTestCheck

@StatelessCheck
public class LazyTestCheck extends AbstractCheck {
    private String currentClass;
    private Map<String, Set<String>> classMethodCalls = new HashMap<>();

    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF };
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
    public void beginTree(DetailAST rootAST) {
        classMethodCalls.clear();
        currentClass = null;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            currentClass = ast.findFirstToken(TokenTypes.IDENT).getText();
            classMethodCalls.put(currentClass, new HashSet<>());
        } else if (ast.getType() == TokenTypes.METHOD_DEF && hasAnnotation(ast, "Test")) {
            processTestMethod(ast);
        }
    }

    private void processTestMethod(DetailAST methodAst) {
        Set<String> calledMethods = new HashSet<>();
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist != null) {
            findMethodCalls(slist, calledMethods);
        }
        classMethodCalls.get(currentClass).addAll(calledMethods);
    }

    private void findMethodCalls(DetailAST node, Set<String> calledMethods) {
        DetailAST child = node.getFirstChild();

        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_CALL) {
                String methodName = getFullMethodName(child);
                if (!isExcludedMethod(methodName)) {
                    calledMethods.add(methodName);
                }
            }
            findMethodCalls(child, calledMethods);
            child = child.getNextSibling();
        }
    }

    private String getFullMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            return dot.getFirstChild().getText() + "." + dot.getLastChild().getText();
        }
        return methodCall.findFirstToken(TokenTypes.IDENT).getText();
    }

    private boolean isExcludedMethod(String methodName) {
        // Ignora métodos de assert e framework de teste
        return methodName.startsWith("assert") ||
                methodName.startsWith("fail") ||
                methodName.contains(".getClass()");
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            checkForLazyTestPattern();
            currentClass = null;
        }
    }

    private void checkForLazyTestPattern() {
        Map<String, Integer> methodCount = new HashMap<>();
        for (String method : classMethodCalls.get(currentClass)) {
            methodCount.put(method, methodCount.getOrDefault(method, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : methodCount.entrySet()) {
            if (entry.getValue() > 1) {
                log(0, "Lazy Test detectado: '" + entry.getKey() +
                        "' chamado em múltiplos testes. Considere refatorar para evitar duplicação.");
            }
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