Wellcome,

This is a repository for the project Arieslinter.

First, you need to install the Arieslinter in your local maven repository:
```
git clone https://github.com/tassiovirginio/arieslinter.git

cd arieslinter

mvn clean install
```
---

add file checkstyle.xml in your project root directory.

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN" "http://checkstyle.sourceforge.net/dtds/configuration_1_3.dtd">
<module name="Checker">
    <module name="TreeWalker">
        <module name="br.ufba.arieslinter.checks.VerboseTestCheck" />
    </module>
</module>
```

---

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC "-//Puppy Crawl//DTD Check Configuration 1.3//EN" "http://checkstyle.sourceforge.net/dtds/configuration_1_3.dtd">

<module name="Checker">
    <module name="TreeWalker">
        <module name="br.ufba.arieslinter.checks.VerboseTestCheck" />
        <module name="br.ufba.arieslinter.checks.UnknownFixtureCheck" />
        <module name="br.ufba.arieslinter.checks.SleepyTestCheck" />
        <!-- <module name="br.ufba.arieslinter.checks.DuplicateAssertCheck" /> -->
    </module>
</module>
```


---

## For Visual Studio Code

Add the VSCode plugin "Checkstyle for Java"

add in your settings.xml - VSCODE

```
"java.checkstyle.configuration": "{path_of_the_file}/checkstyle.xml",
"java.checkstyle.autocheck": true,
"java.checkstyle.modules": ["/home/{user}/.m2/repository/br/ufba/arieslinter/1.0/arieslinter-1.0.jar"],
```

replace the "{path_of_the_file}" with the path of the file checkstyle.xml in your project root directory. 
and replace the "{user}" with your user name in your system.

---
## For IntelliJ



---
## For Eclipse


