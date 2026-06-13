package br.ufba.arieslinter.checks;
 
import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
/**
 * Detecta Resource Optimism: uso de recursos externos (File) sem verificar existência.
 * Detection: A test method utilizes an instance of a File class without calling the
 * exists(), isFile() or notExists() methods of the object.
 */
@StatelessCheck
public class ResourceOptimismCheck extends AbstractTestSmellCheck {
 
    private static final Set<String> FILE_TYPES = Set.of(
            "File",
            "java.io.File");
 
    private static final Set<String> EXISTENCE_CHECKS = Set.of(
            "exists",
            "isFile",
            "notExists");
 
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
 
        Map<String, DetailAST> fileVariables = new HashMap<>();
        List<DetailAST> anonymousInstantiations = new ArrayList<>();
        Set<String> checkedVariables = new HashSet<>();
        Set<DetailAST> checkedInstantiations = new HashSet<>();
 
        DetailAST methodBody = ast.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) {
            return;
        }
 
        // 1. Coleta variáveis/instanciações de File e verifica quais chamaram os métodos de verificação
        scanMethodBody(methodBody, fileVariables, anonymousInstantiations, checkedVariables, checkedInstantiations);
 
        // 2. Reporta variáveis de File que não tiveram a existência checada
        for (Map.Entry<String, DetailAST> entry : fileVariables.entrySet()) {
            String varName = entry.getKey();
            if (!checkedVariables.contains(varName)) {
                log(entry.getValue().getLineNo(),
                        "Resource Optimism: File variable ''{0}'' is utilized without checking existence. "
                                + "Call exists(), isFile(), or notExists() before using it.",
                        varName);
            }
        }
 
        // 3. Reporta instanciações anônimas de File que não tiveram a existência checada
        for (DetailAST newAst : anonymousInstantiations) {
            if (!checkedInstantiations.contains(newAst)) {
                log(newAst.getLineNo(),
                        "Resource Optimism: Anonymous File instance is utilized without checking existence. "
                                + "Call exists(), isFile(), or notExists() before using it.");
            }
        }
    }
 
    private void scanMethodBody(DetailAST node,
                                Map<String, DetailAST> fileVariables,
                                List<DetailAST> anonymousInstantiations,
                                Set<String> checkedVariables,
                                Set<DetailAST> checkedInstantiations) {
        if (node == null) {
            return;
        }
 
        // Declaração de variáveis locais
        if (node.getType() == TokenTypes.VARIABLE_DEF) {
            String typeName = getTypeName(node);
            if (typeName != null && FILE_TYPES.contains(typeName)) {
                DetailAST identNode = node.findFirstToken(TokenTypes.IDENT);
                if (identNode != null) {
                    fileVariables.put(identNode.getText(), node);
                }
            }
        }
 
        // Parâmetros (ex: foreach loop)
        if (node.getType() == TokenTypes.PARAMETER_DEF) {
            String typeName = getTypeName(node);
            if (typeName != null && FILE_TYPES.contains(typeName)) {
                DetailAST identNode = node.findFirstToken(TokenTypes.IDENT);
                if (identNode != null) {
                    fileVariables.put(identNode.getText(), node);
                }
            }
        }
 
        // Instanciações diretas
        if (node.getType() == TokenTypes.LITERAL_NEW) {
            String typeName = getNewTypeName(node);
            if (typeName != null && FILE_TYPES.contains(typeName)) {
                if (!isAssignedToFileVariable(node, fileVariables)) {
                    anonymousInstantiations.add(node);
                }
            }
        }
 
        // Chamadas de método
        if (node.getType() == TokenTypes.METHOD_CALL) {
            processMethodCall(node, checkedVariables, checkedInstantiations);
        }
 
        // Recursão nos filhos
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanMethodBody(child, fileVariables, anonymousInstantiations, checkedVariables, checkedInstantiations);
            child = child.getNextSibling();
        }
    }
 
    private void processMethodCall(DetailAST methodCall,
                                   Set<String> checkedVariables,
                                   Set<DetailAST> checkedInstantiations) {
        DetailAST firstChild = methodCall.getFirstChild();
        if (firstChild == null || firstChild.getType() != TokenTypes.DOT) {
            return;
        }
 
        DetailAST target = firstChild.getFirstChild();
        DetailAST methodNameNode = firstChild.getLastChild();
 
        if (target == null || methodNameNode == null) {
            return;
        }
 
        String methodName = methodNameNode.getText();
 
        if (EXISTENCE_CHECKS.contains(methodName)) {
            if (target.getType() == TokenTypes.IDENT) {
                checkedVariables.add(target.getText());
            } else if (target.getType() == TokenTypes.DOT) {
                DetailAST lastChild = target.getLastChild();
                if (lastChild != null && lastChild.getType() == TokenTypes.IDENT) {
                    checkedVariables.add(lastChild.getText());
                }
            } else if (target.getType() == TokenTypes.LITERAL_NEW) {
                checkedInstantiations.add(target);
            }
        }
    }
 
    private boolean isAssignedToFileVariable(DetailAST literalNew, Map<String, DetailAST> fileVariables) {
        DetailAST parent = literalNew.getParent();
        while (parent != null && parent.getType() != TokenTypes.SLIST && parent.getType() != TokenTypes.METHOD_DEF) {
            if (parent.getType() == TokenTypes.VARIABLE_DEF) {
                String typeName = getTypeName(parent);
                if (typeName != null && FILE_TYPES.contains(typeName)) {
                    return true;
                }
            }
            if (parent.getType() == TokenTypes.ASSIGN) {
                DetailAST lhs = parent.getFirstChild();
                if (lhs != null && lhs.getType() == TokenTypes.IDENT) {
                    if (fileVariables.containsKey(lhs.getText())) {
                        return true;
                    }
                }
            }
            parent = parent.getParent();
        }
        return false;
    }
 
    private String getNewTypeName(DetailAST literalNew) {
        DetailAST firstChild = literalNew.getFirstChild();
        if (firstChild == null) {
            return null;
        }
        if (firstChild.getType() == TokenTypes.IDENT) {
            return firstChild.getText();
        }
        if (firstChild.getType() == TokenTypes.DOT) {
            DetailAST lastChild = firstChild.getLastChild();
            if (lastChild != null) {
                return lastChild.getText();
            }
        }
        return null;
    }
 
    private String getTypeName(DetailAST variableDef) {
        DetailAST typeNode = variableDef.findFirstToken(TokenTypes.TYPE);
        if (typeNode == null) {
            return null;
        }
 
        // Ignora arrays (ex: File[])
        if (typeNode.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null) {
            return null;
        }
 
        DetailAST firstChild = typeNode.getFirstChild();
        if (firstChild == null) {
            return null;
        }
 
        int childType = firstChild.getType();
 
        if (childType == TokenTypes.IDENT) {
            return firstChild.getText();
        } else if (childType == TokenTypes.DOT) {
            DetailAST lastChild = firstChild.getLastChild();
            if (lastChild != null) {
                return lastChild.getText();
            }
        }
 
        return null;
    }
}