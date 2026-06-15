package br.ufba.arieslinter.checks;
 
import br.ufba.arieslinter.checks.abstracts.AbstractTestSmellCheck;
import br.ufba.arieslinter.checks.constants.TestAnnotations;
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
 
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
 
@StatelessCheck
public class LazyTestCheck extends AbstractTestSmellCheck {
 
    private static final Set<String> IGNORED_TYPES = new HashSet<>(Arrays.asList(
        // Primitive types and wrappers
        "String", "CharSequence", "StringBuilder", "StringBuffer", "Object", "Class", "System", "Math",
        "Integer", "Double", "Float", "Boolean", "Long", "Short", "Byte", "Character",
        "int", "double", "float", "boolean", "long", "short", "byte", "char", "void",
        
        // IO classes
        "File", "Path", "Paths", "Files", "InputStream", "OutputStream", "Reader", "Writer", 
        "StringReader", "StringWriter", "BufferedReader", "BufferedWriter", 
        "BufferedInputStream", "BufferedOutputStream", "FileInputStream", "FileOutputStream", 
        "FileReader", "FileWriter", "PrintStream", "PrintWriter", "ByteArrayInputStream", "ByteArrayOutputStream",
        "Charset", "StandardCharsets",
        
        // Collections & Utilities
        "Collection", "List", "ArrayList", "LinkedList", "Vector", "Stack",
        "Set", "HashSet", "LinkedHashSet", "TreeSet", 
        "Map", "HashMap", "LinkedHashMap", "TreeMap", "Hashtable",
        "Queue", "Deque", "ArrayDeque", "PriorityQueue",
        "Iterator", "ListIterator", "Enumeration", "Properties", 
        "Arrays", "Collections", "UUID", "Random", "Date", "Calendar", "TimeUnit", 
        "Thread", "Runnable", "Callable", "Future", "Stream", "Arguments", "Collectors", "Collector",
        
        // Exceptions
        "Exception", "Throwable", "Error", "RuntimeException", "IOException", 
        "NullPointerException", "IllegalArgumentException", "IllegalStateException", 
        "IndexOutOfBoundsException", "ArrayIndexOutOfBoundsException", "AssertionError"
    ));
 
    private static final Set<String> COMMON_UTILITY_METHODS = new HashSet<>(Arrays.asList(
        "isEmpty", "size", "length", "get", "add", "put", "remove", "contains",
        "iterator", "stream", "hasNext", "next", "addElement", "containsKey", "containsValue"
    ));
 
    @Override
    public int[] getAcceptableTokens() {
        return new int[] { TokenTypes.CLASS_DEF };
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
        // No-op since we are stateless and carry no instance fields
    }
 
    @Override
    public void visitToken(DetailAST ast) {
        if (isTestClass(ast)) {
            // State is local to the class definition being visited to remain stateless/thread-safe
            Map<String, Set<Integer>> productionMethodCalls = new HashMap<>();
            Set<String> reportedMethods = new HashSet<>();
 
            // Mapeia variáveis de classe (fields) para seus tipos
            Map<String, String> classFields = new HashMap<>();
            DetailAST objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
            if (objBlock != null) {
                for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child.getType() == TokenTypes.VARIABLE_DEF) {
                        registerVariable(child, classFields);
                    }
                }
            }
 
            collectMethodCallsFromTests(ast, classFields, productionMethodCalls);
            reportLazyTests(productionMethodCalls, reportedMethods);
        }
    }
 
    /**
     * Verifica se a classe contém métodos de teste.
     */
    private boolean isTestClass(DetailAST classDefAst) {
        DetailAST objBlock = classDefAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return false;
        }
 
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF) {
                if (hasAnyAnnotation(child, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
 
        return false;
    }
 
    /**
     * Coleta chamadas de métodos de todos os testes da classe.
     */
    private void collectMethodCallsFromTests(DetailAST classDefAst, Map<String, String> classFields, Map<String, Set<Integer>> productionMethodCalls) {
        DetailAST objBlock = classDefAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return;
        }
 
        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF) {
                if (hasAnyAnnotation(child, TestAnnotations.ALL_TEST_ANNOTATIONS)) {
                    // É um método de teste, coleta suas chamadas
                    Map<String, String> varTypes = new HashMap<>(classFields);
 
                    // Parâmetros do método
                    DetailAST parameters = child.findFirstToken(TokenTypes.PARAMETERS);
                    if (parameters != null) {
                        for (DetailAST param = parameters.getFirstChild(); param != null; param = param.getNextSibling()) {
                            if (param.getType() == TokenTypes.PARAMETER_DEF) {
                                registerVariable(param, varTypes);
                            }
                        }
                    }
 
                    // Variáveis locais
                    DetailAST methodBody = child.findFirstToken(TokenTypes.SLIST);
                    if (methodBody != null) {
                        scanLocalVariables(methodBody, varTypes);
                    }
 
                    int testLine = child.getLineNo();
                    scanForMethodCalls(methodBody, testLine, varTypes, productionMethodCalls);
                }
            }
            child = child.getNextSibling();
        }
    }
 
    /**
     * Escaneia recursivamente por chamadas de método.
     */
    private void scanForMethodCalls(DetailAST node, int testLine, Map<String, String> varTypes, Map<String, Set<Integer>> productionMethodCalls) {
        if (node == null) {
            return;
        }
 
        if (node.getType() == TokenTypes.METHOD_CALL) {
            processMethodCall(node, testLine, varTypes, productionMethodCalls);
        }
 
        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForMethodCalls(child, testLine, varTypes, productionMethodCalls);
            child = child.getNextSibling();
        }
    }
 
    /**
     * Processa uma chamada de método.
     */
    private void processMethodCall(DetailAST methodCall, int testLine, Map<String, String> varTypes, Map<String, Set<Integer>> productionMethodCalls) {
        String methodSignature = getMethodSignature(methodCall, varTypes);
 
        if (methodSignature == null) {
            return;
        }
 
        // Divide assinatura em targetType e methodName
        String targetType = "";
        String methodName = methodSignature;
        int dotIndex = methodSignature.lastIndexOf('.');
        if (dotIndex >= 0) {
            targetType = methodSignature.substring(0, dotIndex);
            methodName = methodSignature.substring(dotIndex + 1);
        }
 
        if (isTestFrameworkMethod(methodName) || isObjectMethod(methodName) || isCommonUtilityMethod(methodName)) {
            return;
        }
 
        if (!targetType.isEmpty() && IGNORED_TYPES.contains(targetType)) {
            return;
        }
 
        // Adiciona esta chamada ao mapa
        productionMethodCalls
                .computeIfAbsent(methodSignature, k -> new HashSet<>())
                .add(testLine);
    }
 
    /**
     * Cria uma assinatura única para o método chamado.
     * Formato: "Classe.metodo" ou "objeto.metodo" se for chamada direta
     */
    private String getMethodSignature(DetailAST methodCall, Map<String, String> varTypes) {
        DetailAST firstChild = methodCall.getFirstChild();
        if (firstChild == null) {
            return null;
        }
 
        // Chamada qualificada: obj.method()
        if (firstChild.getType() == TokenTypes.DOT) {
            DetailAST target = firstChild.getFirstChild();
            DetailAST methodNameNode = firstChild.getLastChild();
 
            if (target == null || methodNameNode == null) {
                return null;
            }
 
            // Ignora chamadas encadeadas em chamadas de método
            if (target.getType() == TokenTypes.METHOD_CALL) {
                return null;
            }
 
            String methodName = methodNameNode.getText();
 
            // Se o target for "new ClassName()"
            if (target.getType() == TokenTypes.LITERAL_NEW) {
                DetailAST typeNode = target.getFirstChild();
                String className = getTypeName(typeNode);
                return className != null ? className + "." + methodName : null;
            }
 
            String targetName = getTargetName(target);
            if ("this".equals(targetName) || "super".equals(targetName)) {
                return null; // Chamadas a métodos da própria classe de teste
            }
 
            // Tenta mapear o nome do target para seu tipo
            String type = varTypes.get(targetName);
            if (type != null) {
                return type + "." + methodName;
            }
 
            return targetName + "." + methodName;
        }
 
        // Chamada direta: method()
        if (firstChild.getType() == TokenTypes.IDENT) {
            return null; // Chamada direta local
        }
 
        return null;
    }
 
    private void registerVariable(DetailAST varDef, Map<String, String> varTypes) {
        DetailAST typeNode = varDef.findFirstToken(TokenTypes.TYPE);
        DetailAST identNode = varDef.findFirstToken(TokenTypes.IDENT);
        if (typeNode != null && identNode != null) {
            String varName = identNode.getText();
            String typeName = getTypeName(typeNode);
            if (typeName != null) {
                varTypes.put(varName, typeName);
            }
        }
    }
 
    private String getTypeName(DetailAST typeNode) {
        DetailAST identNode = typeNode.findFirstToken(TokenTypes.IDENT);
        if (identNode != null) {
            return identNode.getText();
        }
        DetailAST dotNode = typeNode.findFirstToken(TokenTypes.DOT);
        if (dotNode != null) {
            DetailAST lastChild = dotNode.getLastChild();
            if (lastChild != null && lastChild.getType() == TokenTypes.IDENT) {
                return lastChild.getText();
            }
        }
        DetailAST firstChild = typeNode.getFirstChild();
        return firstChild != null ? firstChild.getText() : null;
    }
 
    private void scanLocalVariables(DetailAST node, Map<String, String> varTypes) {
        if (node == null) {
            return;
        }
        if (node.getType() == TokenTypes.VARIABLE_DEF) {
            registerVariable(node, varTypes);
        }
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanLocalVariables(child, varTypes);
            child = child.getNextSibling();
        }
    }
 
    private String getTargetName(DetailAST target) {
        if (target == null) {
            return "";
        }
        if (target.getType() == TokenTypes.IDENT) {
            return target.getText();
        }
        if (target.getType() == TokenTypes.DOT) {
            DetailAST lastPart = target.getLastChild();
            return lastPart != null ? lastPart.getText() : "";
        }
        if (target.getType() == TokenTypes.LITERAL_THIS) {
            return "this";
        }
        if (target.getType() == TokenTypes.LITERAL_SUPER) {
            return "super";
        }
        return target.getText();
    }
 
    /**
     * Verifica se é método de framework de teste (assert, verify, etc).
     */
    private boolean isTestFrameworkMethod(String methodName) {
        return methodName.startsWith("assert") ||
                methodName.startsWith("verify") ||
                methodName.startsWith("when") ||
                methodName.startsWith("given") ||
                methodName.startsWith("then") ||
                methodName.equals("fail") ||
                methodName.startsWith("mock") ||
                methodName.startsWith("spy") ||
                methodName.equals("times") ||
                methodName.equals("never") ||
                methodName.equals("any") ||
                methodName.equals("eq") ||
                methodName.equals("capture") ||
                methodName.startsWith("doThrow") ||
                methodName.startsWith("doReturn") ||
                methodName.startsWith("doAnswer") ||
                methodName.startsWith("doNothing") ||
                methodName.equals("reset") ||
                methodName.equals("clearInvocations");
    }
 
    private boolean isGetterOrSetter(String methodName) {
        return (methodName.startsWith("get") ||
                methodName.startsWith("set") ||
                methodName.startsWith("is")) &&
                methodName.length() > 3 &&
                Character.isUpperCase(methodName.charAt(methodName.startsWith("is") ? 2 : 3));
    }
 
    private boolean isObjectMethod(String methodName) {
        return methodName.equals("equals") ||
                methodName.equals("hashCode") ||
                methodName.equals("toString") ||
                methodName.equals("getClass") ||
                methodName.equals("clone") ||
                methodName.equals("finalize") ||
                methodName.equals("wait") ||
                methodName.equals("notify") ||
                methodName.equals("notifyAll");
    }
 
    private boolean isCommonUtilityMethod(String methodName) {
        return COMMON_UTILITY_METHODS.contains(methodName);
    }
 
    /**
     * Reporta métodos de produção chamados por múltiplos testes.
     */
    private void reportLazyTests(Map<String, Set<Integer>> productionMethodCalls, Set<String> reportedMethods) {
        for (Map.Entry<String, Set<Integer>> entry : productionMethodCalls.entrySet()) {
            String methodSignature = entry.getKey();
            Set<Integer> callLines = entry.getValue();
 
            // Lazy Test: método chamado por 2 ou mais testes
            if (callLines.size() >= 2 && !reportedMethods.contains(methodSignature)) {
                // Reporta na primeira linha onde aparece
                int firstLine = callLines.stream().min(Integer::compare).orElse(0);
 
                log(firstLine,
                        "Lazy Test: production method ''{0}'' is called by {1} different test methods, "
                                + "consider consolidating these tests or refactoring the production method.",
                        methodSignature,
                        callLines.size());
 
                reportedMethods.add(methodSignature);
            }
        }
    }
}