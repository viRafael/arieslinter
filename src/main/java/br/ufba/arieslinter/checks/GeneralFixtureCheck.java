package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

    // TODO: TESTAR CLASSE GeneralFixtureCheck

@StatelessCheck
public class GeneralFixtureCheck extends AbstractCheck {
    private Map<String, Set<String>> setUpFields = new HashMap<>();
    private Map<String, Set<String>> testMethodUsages = new HashMap<>();
    private String currentClass;

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
        setUpFields.clear();
        testMethodUsages.clear();
        currentClass = null;
    }

    @Override
    public void leaveToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            checkUnusedFields();
            currentClass = null;
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            currentClass = ast.findFirstToken(TokenTypes.IDENT).getText();
            setUpFields.put(currentClass, new HashSet<>());
            testMethodUsages.put(currentClass, new HashSet<>());
        } else if (ast.getType() == TokenTypes.METHOD_DEF && currentClass != null) {
            processMethod(ast);
        }
    }

    private void processMethod(DetailAST methodAst) {
        if (hasAnnotation(methodAst, "Before")) {
            collectSetUpFields(methodAst);
        } else if (hasAnnotation(methodAst, "Test")) {
            collectTestMethodUsages(methodAst);
        }
    }

    private void collectSetUpFields(DetailAST methodAst) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist == null) return;

        DetailAST child = slist.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.EXPR) {
                processAssignment(child.getFirstChild());
            }
            child = child.getNextSibling();
        }
    }

    private void processAssignment(DetailAST expr) {
        if (expr.getType() == TokenTypes.ASSIGN) {
            DetailAST lhs = expr.getFirstChild();
            if (lhs.getType() == TokenTypes.DOT || lhs.getType() == TokenTypes.IDENT) {
                String fieldName = getFieldName(lhs);
                if (fieldName != null) {
                    setUpFields.get(currentClass).add(fieldName);
                }
            }
        }
    }

    private String getFieldName(DetailAST lhs) {
        if (lhs.getType() == TokenTypes.DOT) {
            return lhs.getLastChild().getText(); // this.field
        }
        return lhs.getText(); // field
    }

    private void collectTestMethodUsages(DetailAST methodAst) {
        Set<String> usedFields = new HashSet<>();
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist != null) {
            findFieldUsages(slist, usedFields);
        }
        testMethodUsages.get(currentClass).addAll(usedFields);
    }

    private void findFieldUsages(DetailAST node, Set<String> usedFields) {
        DetailAST child = node.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.IDENT) {
                String ident = child.getText();
                if (setUpFields.get(currentClass).contains(ident)) {
                    usedFields.add(ident);
                }
            }
            findFieldUsages(child, usedFields);
            child = child.getNextSibling();
        }
    }

    private void checkUnusedFields() {
        Set<String> unusedFields = new HashSet<>(setUpFields.get(currentClass));
        unusedFields.removeAll(testMethodUsages.get(currentClass));

        for (String field : unusedFields) {
            log(0, "Campo '" + field + "' inicializado em setUp() não é utilizado em todos os testes");
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