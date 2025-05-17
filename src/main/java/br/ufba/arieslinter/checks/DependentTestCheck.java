package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

    //TODO: TESTAR CLASSE DependentTestCheck
// Funciona de um forma limitada devido as nuanceas da natureza do Dependente Test
// Ele verifica dentro de uma classe de teste se um teste chama outro teste

@StatelessCheck
public class DependentTestCheck extends AbstractCheck {
    private String currentClass;
    private final Map<String, Set<String>> classTestMethods = new HashMap<>();
    private final Map<String, Set<String>> testDependencies = new HashMap<>();

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
        classTestMethods.clear();
        testDependencies.clear();
        currentClass = null;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            currentClass = ast.findFirstToken(TokenTypes.IDENT).getText();
            classTestMethods.put(currentClass, new HashSet<>());
        } else if (ast.getType() == TokenTypes.METHOD_DEF) {
            processMethod(ast);
        }
    }

    private void processMethod(DetailAST methodAst) {
        if (hasAnnotation(methodAst, "Test")) {
            String methodName = methodAst.findFirstToken(TokenTypes.IDENT).getText();
            classTestMethods.get(currentClass).add(methodName);
            analyzeMethodBody(methodAst, methodName);
        }
    }

    private void analyzeMethodBody(DetailAST methodAst, String currentTestMethod) {
        Set<String> dependencies = new HashSet<>();
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist != null) {
            findMethodCalls(slist, dependencies);
        }
        testDependencies.put(currentTestMethod, dependencies);
    }

    private void findMethodCalls(DetailAST node, Set<String> dependencies) {
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_CALL) {
                String calledMethod = getMethodName(child);
                if (classTestMethods.get(currentClass).contains(calledMethod)) {
                    dependencies.add(calledMethod);
                }
            }
            findMethodCalls(child, dependencies);
            child = child.getNextSibling();
        }
    }

    private String getMethodName(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            return dot.getLastChild().getText();
        }
        return methodCall.findFirstToken(TokenTypes.IDENT).getText();
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            reportDependencies();
            currentClass = null;
        }
    }

    private void reportDependencies() {
        for (Map.Entry<String, Set<String>> entry : testDependencies.entrySet()) {
            for (String dependency : entry.getValue()) {
                log(0, "Dependent Test detectado: '" + entry.getKey() + "' depende de '" + dependency + "'");
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