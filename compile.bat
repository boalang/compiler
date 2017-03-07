@ECHO OFF
 
"%JAVA_HOME%"\bin\java -cp .;"%~dp0dist\boa-compiler.jar";"%~dp0lib\*" boa.BoaMain -c -i %*
