@ECHO OFF
 
"%JAVA_HOME%"\bin\java -cp .;"%~dp0dist\boa-compiler.jar";"%~dp0lib\*";"%~dp0lib\evaluator\*";"%~dp0lib\datagen\*";"%~dp0compile" boa.BoaMain %*
pause
