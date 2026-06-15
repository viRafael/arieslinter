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

/**
 * Detecta Eager Test: teste que chama múltiplos métodos de produção diferentes.
 */
@StatelessCheck
public class EagerTestCheck extends AbstractTestSmellCheck {
    // Número máximo de métodos de produção diferentes permitidos
    private int maxProductionMethods = 1;

    // Conjunto de tipos comuns do Java, Coleções e Ferramentas de Teste que NÃO são classes de produção
    private static final Set<String> IGNORED_TYPES = new HashSet<>(Arrays.asList(
        // Tipos primitivos e invólucros padrão
        "String", "CharSequence", "StringBuilder", "StringBuffer", "Object", "Class", "System", "Math",
        "Integer", "Double", "Float", "Boolean", "Long", "Short", "Byte", "Character",
        "int", "double", "float", "boolean", "long", "short", "byte", "char", "void",
        
        // Classes de IO e NIO
        "File", "Path", "Paths", "Files", "InputStream", "OutputStream", "Reader", "Writer", 
        "StringReader", "StringWriter", "BufferedReader", "BufferedWriter", 
        "BufferedInputStream", "BufferedOutputStream", "FileInputStream", "FileOutputStream", 
        "FileReader", "FileWriter", "PrintStream", "PrintWriter", "ByteArrayInputStream", "ByteArrayOutputStream",
        "Charset", "StandardCharsets",
        
        // Coleções, Estruturas de Dados e Utilitários
        "Collection", "List", "ArrayList", "LinkedList", "Vector", "Stack",
        "Set", "HashSet", "LinkedHashSet", "TreeSet", "NavigableSet", "SortedSet",
        "Map", "HashMap", "LinkedHashMap", "TreeMap", "Hashtable", "SortedMap", "NavigableMap",
        "Queue", "Deque", "ArrayDeque", "PriorityQueue",
        "Iterator", "ListIterator", "Enumeration", "Properties", 
        "Arrays", "Collections", "UUID", "Random", "Date", "Calendar", "TimeUnit", 
        "Thread", "Runnable", "Callable", "Future", "Stream", "Arguments", "Collectors", "Collector",
        "BitSet", "SplittableRandom", "ThreadLocalRandom", "Optional",
        
        // Classes de rede, e-mail, formato e localização comuns
        "URL", "URI", "Locale", "DateFormat", "SimpleDateFormat", "InternetAddress", "MimeMessage",
        "JarFile", "JarEntry", "JarOutputStream", "JarInputStream", "ZipFile", "ZipEntry",
        
        // Interfaces Funcionais comuns
        "IntPredicate", "Predicate", "Consumer", "Function", "Supplier", "BiConsumer", "BiFunction",
        
        // Exceções e Erros
        "Exception", "Throwable", "Error", "RuntimeException", "IOException", 
        "NullPointerException", "IllegalArgumentException", "IllegalStateException", 
        "IndexOutOfBoundsException", "ArrayIndexOutOfBoundsException", "AssertionError"
    ));

    private static final Set<String> COMMON_UTILITY_METHODS = new HashSet<>(Arrays.asList(
        "isEmpty", "size", "length", "get", "add", "put", "remove", "contains",
        "iterator", "stream", "hasNext", "next", "addElement", "containsKey", "containsValue",
        "setLength", "append", "clear", "format", "available", "close"
    ));

    private static final Set<String> IGNORED_VARIABLE_NAMES = new HashSet<>(Arrays.asList(
        "set", "list", "map", "coll", "collection", "iterator", "iter", "builder", "sb", "helper",
        "properties", "props", "config", "settings"
    ));

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

        // Mapeia variáveis locais, parâmetros e campos da classe para seus respectivos tipos
        Map<String, String> varTypes = collectVariableTypes(ast);
        String testClassName = getTestClassName(ast);

        // Coleta métodos de produção únicos chamados neste teste
        Set<String> productionMethods = collectProductionMethods(ast, varTypes, testClassName);

        // Eager Test: chama múltiplos métodos de produção diferentes
        if (productionMethods.size() > maxProductionMethods) {
            log(ast.getLineNo(),
                    "Eager Test: Test method calls {0} different production methods. "
                            + "Tests should focus on a single behavior, splite into separate tests.",
                    productionMethods.size());
        }
    }

    private String getTestClassName(DetailAST methodAst) {
        DetailAST classDef = findEnclosingClass(methodAst);
        if (classDef != null) {
            DetailAST identNode = classDef.findFirstToken(TokenTypes.IDENT);
            if (identNode != null) {
                return identNode.getText();
            }
        }
        return "";
    }

    /**
     * Coleta todos os métodos de produção ÚNICOS chamados no teste.
     * Retorna um Set para contar apenas métodos diferentes.
     */
    private Set<String> collectProductionMethods(DetailAST methodAst, Map<String, String> varTypes, String testClassName) {
        Set<String> productionMethods = new HashSet<>();

        DetailAST methodBody = methodAst.findFirstToken(TokenTypes.SLIST);
        if (methodBody == null) {
            return productionMethods;
        }

        scanForProductionMethods(methodBody, productionMethods, varTypes, testClassName);
        return productionMethods;
    }

    /**
     * Escaneia recursivamente por chamadas de métodos de produção.
     */
    private void scanForProductionMethods(DetailAST node, Set<String> productionMethods, Map<String, String> varTypes, String testClassName) {
        if (node == null) {
            return;
        }

        if (node.getType() == TokenTypes.METHOD_CALL) {
            processMethodCall(node, productionMethods, varTypes, testClassName);
        }

        // Continua busca recursiva
        DetailAST child = node.getFirstChild();
        while (child != null) {
            scanForProductionMethods(child, productionMethods, varTypes, testClassName);
            child = child.getNextSibling();
        }
    }

    /**
     * Processa uma chamada de método e adiciona ao Set se for método de produção legítimo.
     */
    private void processMethodCall(DetailAST methodCall, Set<String> productionMethods, Map<String, String> varTypes, String testClassName) {
        String targetName = "";
        String methodName = "";

        DetailAST firstChild = methodCall.getFirstChild();
        if (firstChild == null) {
            return;
        }

        // Chamada qualificada: obj.method()
        if (firstChild.getType() == TokenTypes.DOT) {
            DetailAST target = firstChild.getFirstChild();
            DetailAST methodNameNode = firstChild.getLastChild();

            if (target != null && methodNameNode != null) {
                if (target.getType() == TokenTypes.METHOD_CALL) {
                    return;
                }
                targetName = getTargetName(target);
                methodName = methodNameNode.getText();
            }
        } else if (firstChild.getType() == TokenTypes.IDENT) {
            // Chamada direta: method()
            methodName = firstChild.getText();
        }

        if (methodName.isEmpty()) {
            return;
        }

        // Ignora métodos de frameworks de teste, getters/setters, Object e utilitários comuns
        if (isTestFrameworkMethod(methodName) || isGetterOrSetter(methodName) || isObjectMethod(methodName) || isCommonUtilityMethod(methodName)) {
            return;
        }

        // Ignora métodos de escuta/listener
        if ((methodName.startsWith("add") || methodName.startsWith("remove")) && methodName.endsWith("Listener")) {
            return;
        }

        // Se houver um objeto de destino (ex: obj.method())
        if (!targetName.isEmpty()) {
            // Ignora chamadas a this/super
            if ("this".equals(targetName) || "super".equals(targetName)) {
                return;
            }

            // Ignora se o nome da variável de destino for um nome genérico comum
            if (IGNORED_VARIABLE_NAMES.contains(targetName.toLowerCase())) {
                return;
            }

            // Verifica se o destino é uma variável local ou campo mapeado
            String type = varTypes.get(targetName);
            if (type != null) {
                // Se for um tipo utilitário ou do Java padrão, ignora
                if (IGNORED_TYPES.contains(type)) {
                    return;
                }
            } else {
                // Se não conhecemos a variável, mas o próprio nome do target é um tipo ignorado (ex: Paths, System)
                if (IGNORED_TYPES.contains(targetName)) {
                    return;
                }

                // Se o target for estático (inicia com letra maiúscula)
                if (Character.isLetter(targetName.charAt(0)) && Character.isUpperCase(targetName.charAt(0))) {
                    // Se não for a própria classe sendo testada, e terminar com Utils/Helper/Factory/Builder/Class, ignora
                    boolean isClassUnderTest = !testClassName.isEmpty() && testClassName.contains(targetName);
                    if (!isClassUnderTest) {
                        if (targetName.endsWith("Utils") || targetName.endsWith("Helper") || 
                            targetName.endsWith("Factory") || targetName.endsWith("Builder") ||
                            targetName.endsWith("Class") || IGNORED_TYPES.contains(targetName)) {
                            return;
                        }
                    }
                }
            }
        } else {
            // Chamadas diretas (ex: helperLocal()) geralmente são métodos auxiliares do próprio teste, não de produção
            return;
        }

        // Se passou em todas as regras, é considerado um método de produção!
        productionMethods.add(methodName);
    }

    private boolean isCommonUtilityMethod(String methodName) {
        return COMMON_UTILITY_METHODS.contains(methodName);
    }

    /**
     * Coleta o mapeamento de variáveis para seus respectivos tipos (campos, parâmetros e locais).
     */
    private Map<String, String> collectVariableTypes(DetailAST methodAst) {
        Map<String, String> varTypes = new HashMap<>();

        // 1. Campos da classe envolvente
        DetailAST classDef = findEnclosingClass(methodAst);
        if (classDef != null) {
            DetailAST objBlock = classDef.findFirstToken(TokenTypes.OBJBLOCK);
            if (objBlock != null) {
                for (DetailAST child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child.getType() == TokenTypes.VARIABLE_DEF) {
                        registerVariable(child, varTypes);
                    }
                }
            }
        }

        // 2. Parâmetros do método
        DetailAST parameters = methodAst.findFirstToken(TokenTypes.PARAMETERS);
        if (parameters != null) {
            for (DetailAST child = parameters.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getType() == TokenTypes.PARAMETER_DEF) {
                    registerVariable(child, varTypes);
                }
            }
        }

        // 3. Variáveis locais no corpo do método
        DetailAST methodBody = methodAst.findFirstToken(TokenTypes.SLIST);
        if (methodBody != null) {
            scanLocalVariables(methodBody, varTypes);
        }

        return varTypes;
    }

    private DetailAST findEnclosingClass(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == TokenTypes.CLASS_DEF) {
                return parent;
            }
            parent = parent.getParent();
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

    /**
     * Extrai o nome do objeto/target da chamada.
     */
    private String getTargetName(DetailAST target) {
        if (target == null) {
            return "";
        }

        // IDENT simples: obj.method()
        if (target.getType() == TokenTypes.IDENT) {
            return target.getText();
        }

        // DOT aninhado: this.obj.method() ou obj.field.method()
        if (target.getType() == TokenTypes.DOT) {
            DetailAST lastPart = target.getLastChild();
            return lastPart != null ? lastPart.getText() : "";
        }

        // Literal THIS ou SUPER
        if (target.getType() == TokenTypes.LITERAL_THIS) {
            return "this";
        }
        if (target.getType() == TokenTypes.LITERAL_SUPER) {
            return "super";
        }

        return target.getText();
    }

    /**
     * Verifica se é método de framework de teste ou mocking.
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

    /**
     * Verifica se é getter/setter.
     */
    private boolean isGetterOrSetter(String methodName) {
        return (methodName.startsWith("get") ||
                methodName.startsWith("set") ||
                methodName.startsWith("is")) &&
                methodName.length() > 3 &&
                Character.isUpperCase(methodName.charAt(methodName.startsWith("is") ? 2 : 3));
    }

    /**
     * Verifica se é método de Object (equals, hashCode, toString, etc).
     */
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
}