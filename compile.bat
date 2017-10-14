@ECHO OFF
 
IF "%1" == "" (
    ECHO "Usage: %0 file.boa [options]"
	"%~dp0\boa.bat" -c
    EXIT /B 1
)

"%~dp0\boa.bat" -c -i %*
