Make binary patch with maven and svn for java
===============================================
This is for my lovely daughter.

1. export the different file and compile it.
2. create a remove shell script.
3. create a auto extract file for linux


bootstrap
=========
1. install python2.7 and add to path
2. install maven and add to path
3. run tools\install-system-scope-lib-to-local-repo.cmd on windows
   or run tools/install-system-scope-lib-to-local-repo.sh on linux/unix-like

I don't like python!?
=====================
OK! If you hate python, just use the mvn command to install the jars.

ex:
``` sh
mvn install:install-file -Dfile=kaptcha-2.1.1.jar -DgroupId=com.google.code.kaptcha -DartifactId=kaptcha -Dversion=2.1.1 -Dpackaging=jar
```

What's the groupId, artifactId, version, classifier infomation?
===============================================================
Check pom.xml yourself.