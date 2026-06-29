@echo off
setlocal

set "JAVA_HOME=%JAVA_HOME%"
if defined JAVA_HOME goto findJava
set "JAVA=java"
goto run

:findJava
set "JAVA=%JAVA_HOME%\bin\java.exe"

:run
set "MAVEN_OPTS=%MAVEN_OPTS% -Xmx1024m"
set "DIR=%~dp0"
cd "%DIR%"

"%JAVA%" ^
    -classpath ".mvn\wrapper\maven-wrapper.jar" ^
    -Dmaven.multiModuleProjectDirectory="%DIR%" ^
    org.apache.maven.wrapper.MavenWrapperMain ^
    %*
