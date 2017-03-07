@ECHO OFF
 
"%JAVA_HOME%"\bin\java -cp .;"%~dp0dist\boa-compiler.jar";"%~dp0lib\*";"%~dp0lib\evaluator\*";"%~dp0compile" boa.BoaMain -e -d dataset/ -i %1 -o %2 %*
