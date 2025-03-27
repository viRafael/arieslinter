add file checkstyle.xml 

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN" "http://checkstyle.sourceforge.net/dtds/configuration_1_3.dtd">
<module name="Checker">
    <module name="TreeWalker">
        <module name="br.ufba.testsmells.checks.VerboseTestCheck" />
    </module>
</module>
```


<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN" "http://checkstyle.sourceforge.net/dtds/configuration_1_3.dtd">

<module name="Checker">
    <module name="TreeWalker">
        <module name="br.ufba.testsmells.checks.VerboseTestCheck" />
        <module name="br.ufba.testsmells.checks.UnknownFixtureCheck" />
        <module name="br.ufba.testsmells.checks.SleepyTestCheck" />
        <!-- <module name="br.ufba.testsmells.checks.DuplicateAssertCheck" /> -->
    </module>
</module>

---

vscode plugin -> "Checkstyle for Java"

configuração settings.xml - VSCODE

"java.checkstyle.configuration": "/home/tassio/Desenvolvimento/repo.git/jnose-core/checkstyle.xml",
"java.checkstyle.autocheck": true,
"java.checkstyle.modules": ["/home/tassio/.m2/repository/br/ufba/ufba-testsmells-checkstyle-checks/1.0/ufba-testsmells-checkstyle-checks-1.0.jar"],

