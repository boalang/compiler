@ECHO OFF
 
"%JAVA_HOME%"\bin\java -cp .;"%~dp0dist\boa-compiler.jar";"%~dp0lib\*";"%~dp0dist\lib\evaluator\*";"%~dp0dist\lib\datagen\*";"%~dp0dist\compile" boa.BoaMain %*
pause
