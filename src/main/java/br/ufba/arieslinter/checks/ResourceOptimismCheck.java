package br.ufba.arieslinter.checks;

import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Detecta Resource Optimism: uso de recursos externos (File) sem verificar
 * existência.
 * 
 * Limitações conhecidas:
 * - Não faz análise de fluxo de controle (não garante ordem de verificação vs
 * uso)
 * - Rastreia apenas variáveis declaradas no método (não parâmetros)
 * - Não detecta recursos passados como parâmetros de métodos
 */
@StatelessCheck
public class ResourceOptimismCheck extends AbstractTestSmellCheck {

    // Tipos de recursos que requerem verificação
    private static final Set<String> FILE_TYPES = Set.of(
            "File",
            "FileInputStream",
            "FileOutputStream",
            "FileReader",
            "FileWriter");

    // Métodos que verificam existência do recurso
    private static final Set<String> EXISTENCE_CHECKS = Set.of(
            "exists",
            "isFile",
            "isDirectory",
            "notExists",
            "canRead",
            "canWrite");

    // Métodos que usam o recurso (requerem verificação prévia)
    private static final Set<String> USAGE_METHODS = Set.of(
            "read",
            "write",
            "delete",
            "createNewFile",
            "mkdir",
            "mkdirs",
            "list",
            "listFiles",
            "length",
            "lastModified");

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

        checkForResourceOptimism(ast);
    }

    /**
     * Verifica se o método usa recursos sem verificar existência.
     */
    private void checkForResourceOptimism(DetailAST methodAst) {
        // Mapa: nome da variável -> foi verificada?
        Map<String, Boolean> fileVariables = new HashMap<>();

        // Conjunto de variáveis que já foram reportadas (evita duplicatas)
        Set<String> reportedVars = new HashSet<>();

        collectFileVariables(methodAst, fileVariables);
        checkFileUsageRecursive(methodAst, fileVariables, reportedVars);
    }

    /**
     * Coleta todas as variáveis do tipo File declaradas no método.
     */
    private void collectFileVariables(DetailAST methodAst, Map<String, Boolean> fileVariables) {
        DetailAST methodBody = methodAst.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) {
            return;
        }

        scanForVariableDeclarations(methodBody, fileVariables);
    }

    /**
     * Busca recursiva por declarações de variáveis File.
     */
    private void scanForVariableDeclarations(DetailAST node, Map<String, Boolean> fileVariables) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.VARIABLE_DEF) {
            String typeName = getTypeName(node);

            if (FILE_TYPES.contains(typeName)) {
                DetailAST identNode = node.findFirstToken(TokenTypes.IDENT);
                if (identNode != null) {
                    String varName = identNode.getText();
                    fileVariables.put(varName, false); // Inicialmente não verificada
                }
            }
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForVariableDeclarations(child, fileVariables);
            child = child.getNextSibling();
        }
    }

    /**
     * Verifica recursivamente o uso de variáveis File.
     */
    private void checkFileUsageRecursive(DetailAST node,
            Map<String, Boolean> fileVariables,
            Set<String> reportedVars) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.METHOD_CALL) {
            processMethodCall(node, fileVariables, reportedVars);
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            checkFileUsageRecursive(child, fileVariables, reportedVars);
            child = child.getNextSibling();
        }
    }

    /**
     * Processa uma chamada de método verificando se é uso de File.
     */
    private void processMethodCall(DetailAST methodCall,
            Map<String, Boolean> fileVariables,
            Set<String> reportedVars) {
        DetailAST firstChild = methodCall.getFirstChild();

        // Verifica se é chamada do tipo: variavel.metodo()
        if (firstChild == null || firstChild.getType() != TokenTypes.DOT) {
            return;
        }

        DetailAST target = firstChild.getFirstChild();
        DetailAST methodNameNode = firstChild.getLastChild();

        if (target == null || methodNameNode == null) {
            return;
        }

        String varName = target.getText();
        String methodName = methodNameNode.getText();

        // Verifica se é uma variável File que estamos rastreando
        if (!fileVariables.containsKey(varName)) {
            return;
        }

        // Se é método de verificação, marca como verificada
        if (EXISTENCE_CHECKS.contains(methodName)) {
            fileVariables.put(varName, true);
            return;
        }

        // Se é método de uso e não foi verificada, reporta
        if (USAGE_METHODS.contains(methodName)) {
            Boolean wasChecked = fileVariables.get(varName);

            if (wasChecked == null || !wasChecked) {
                // Só reporta uma vez por variável
                if (!reportedVars.contains(varName)) {
                    log(methodCall.getLineNo(),
                            "Resource Optimism: File variable ''{0}'' is used without checking existence. "
                                    + "Call exists(), isFile(), or similar before using ''{1}''.",
                            varName, methodName);
                    reportedVars.add(varName);
                }
            }
        }
    }

    /**
     * Extrai o nome do tipo de uma declaração de variável.
     */
    private String getTypeName(DetailAST variableDef) {
        DetailAST typeNode = variableDef.findFirstToken(TokenTypes.TYPE);
        if (typeNode == null) {
            return null;
        }

        DetailAST firstChild = typeNode.getFirstChild();
        if (firstChild == null) {
            return null;
        }

        int childType = firstChild.getType();

        // Tipos primitivos
        if (childType == TokenTypes.LITERAL_LONG) {
            return "long";
        } else if (childType == TokenTypes.LITERAL_INT) {
            return "int";
        } else if (childType == TokenTypes.LITERAL_BOOLEAN) {
            return "boolean";
        } else if (childType == TokenTypes.LITERAL_BYTE) {
            return "byte";
        } else if (childType == TokenTypes.LITERAL_SHORT) {
            return "short";
        } else if (childType == TokenTypes.LITERAL_FLOAT) {
            return "float";
        } else if (childType == TokenTypes.LITERAL_DOUBLE) {
            return "double";
        } else if (childType == TokenTypes.LITERAL_CHAR) {
            return "char";
        }

        // Tipos de referência
        else if (childType == TokenTypes.IDENT) {
            return firstChild.getText();
        }

        // Tipos qualificados (java.io.File)
        else if (childType == TokenTypes.DOT) {
            DetailAST lastChild = firstChild.getLastChild();
            if (lastChild != null) {
                return lastChild.getText();
            }
        }

        // Tipo desconhecido - não quebra, apenas retorna null
        return null;
    }
}