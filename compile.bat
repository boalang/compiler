@ECHO OFF
 
"%JAVA_HOME%"\bin\java -cp .;"%~dp0dist\boa-compiler.jar";"%~dp0lib\hadoop-core-1.0.4.jar";"%~dp0lib\commons-lang-2.4.jar";"%~dp0lib\commons-math-2.1.jar" boa.BoaMain -c -i %*
