package br.ufba.arieslinter.checks;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

    // TODO: TESTAR CLASSE MysteryGuestCheck
//Caso 1: Uso de Arquivo
//@Test
//public void testReadFile() {
//    FileInputStream fis = new FileInputStream("data.txt"); // Reporta
//}

//@Test
//public void testQuery() {
//    Connection conn = new CustomConnection(); // Não reporta (classe não listada)
//    Statement stmt = new Statement();         // Reporta
//}

//@Test
//public void testWithMock() {
//    File mockFile = mock(File.class); // Não reporta
//}

@StatelessCheck
public class MysteryGuestCheck extends AbstractCheck {
    private static final Set<String> EXTERNAL_RESOURCE_CLASSES = new HashSet<>(
            Arrays.asList(
                    "File", "FileInputStream", "FileOutputStream",   // File system
                    "Connection", "Statement", "ResultSet",          // Database
                    "URL", "HttpURLConnection"                       // Network
            )
    );

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
        if (hasAnnotation(ast, "Test")) {
            checkForExternalResources(ast);
        }
    }

    private void checkForExternalResources(DetailAST methodAst) {
        DetailAST slist = methodAst.findFirstToken(TokenTypes.SLIST);
        if (slist != null) {
            scanForResourceCreations(slist);
        }
    }

    private void scanForResourceCreations(DetailAST node) {
        DetailAST child = node.getFirstChild();

        while (child != null) {
            if (child.getType() == TokenTypes.LITERAL_NEW) {
                processNewExpression(child);
            }
            scanForResourceCreations(child); // Busca recursiva
            child = child.getNextSibling();
        }
    }

    private void processNewExpression(DetailAST newAst) {
        String className = getClassName(newAst);

        if (EXTERNAL_RESOURCE_CLASSES.contains(className)) {
            log(newAst.getLineNo(),
                    "Mystery Guest detectado: " + className + ". Prefira mocks ou dados em memória.");
        }
    }

    private String getClassName(DetailAST newAst) {
        DetailAST type = newAst.getFirstChild();
        return type != null ? type.getText() : "";
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