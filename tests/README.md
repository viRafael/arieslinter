# Testes do Arieslinter

Esta pasta tem dois tipos de teste. Nenhum deles usa JUnit nem Maven: são scripts Python que chamam o Java e o Checkstyle pela linha de comando.

## 1. Teste de detecção (corpus)

Responde à pergunta: **"o Arieslinter detecta os mesmos test smells em qualquer ambiente?"**

A detecção é feita pelo motor do Checkstyle, e os plugins de IDE apenas chamam esse motor. Por isso o resultado só pode variar com a versão do Checkstyle e a do Java. Este teste mede isso.

| Arquivo | Função |
| :--- | :--- |
| `corpus/SmellsTest.java` | Classe JUnit 5 com vários smells conhecidos (sleep, teste vazio, assertion roulette, mystery guest, etc.). |
| `corpus/Junit4Test.java` | Classe JUnit 4 com `@Ignore`, fixture geral e teste verboso. |
| `corpus/ModernSyntaxTest.java` | Usa sintaxe Java 14 a 17 (record, `switch` expression, text block, pattern matching) para expor diferenças de parser entre versões do Checkstyle. |
| `corpus/CleanTest.java` | Teste bem escrito. Não deve gerar nenhum aviso. |
| `checkstyle.xml` | Configuração com as 17 checks do Arieslinter. É uma cópia da que o instalador gera. |
| `expected.txt` | Lista esperada de achados (`Arquivo.java:linha NomeDaCheck`). |
| `check_corpus.py` | Roda o Checkstyle com o JAR do Arieslinter sobre o corpus e compara com `expected.txt`. |

Uso:

```bash
python3 tests/check_corpus.py --java java \
  --checkstyle-jar checkstyle-10.23.1-all.jar \
  --arieslinter-jar arieslinter-1.0.jar
```

Para regenerar o esperado depois de mudar uma check de propósito, adicione `--update` e revise o diff antes de commitar.

O `expected.txt` é um registro do comportamento atual, revisado à mão, e não uma especificação independente. Ele protege contra regressões, mas não prova que cada check está conceitualmente correta.

Resultado já obtido: os mesmos 33 achados com Checkstyle 8.45.1, 9.3, 10.0, 10.12.5, 10.23.1, 11.1.0, 12.3.0, 13.0.0 e 14.3.0, em Java 11, 17 e 25.

## 2. Testes unitários do instalador

`test_installer.py` testa as funções do `install.py` sem rede e sem tocar nas suas IDEs:

- leitura da versão do Java;
- leitura de `settings.json` com comentários e vírgulas finais (JSONC);
- mesclagem das settings, versão fixa do Checkstyle e backup;
- recusa de sobrescrever um `settings.json` ilegível;
- tratamento de um `checkstyle.xml` já existente (manter, sobrescrever com backup);
- seleção de IDEs;
- garantia de que forks do Open VSX não recebem a extensão exclusiva da Microsoft.

```bash
python3 -m unittest discover -s tests -p "test_*.py"
```

## O que estes testes NÃO cobrem

- A extensão carregando o JAR dentro de cada editor.
- Windows, macOS e IntelliJ.
- Se cada check é conceitualmente correta (falsos positivos e negativos além do corpus).
- Os testes não rodam em CI: o repositório ainda não tem pipeline.
