@echo off
set "JAVA_HOME=%~dp0.tools\jdk17\jdk-17.0.12+7"
set "PATH=%JAVA_HOME%\bin;%~dp0.tools\maven\apache-maven-3.9.9\bin;%PATH%"
"%~dp0.tools\maven\apache-maven-3.9.9\bin\mvn.cmd" %*
