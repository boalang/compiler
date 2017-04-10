@ECHO OFF
 
IF "%1" == "" (
    ECHO "Usage: %0 file.boa [options]"
	"%~dp0\boa.bat" -c
    EXIT /B 1
)

IF NOT EXIST "%1" (
    ECHO "input '%1' is not a file"
    ECHO "Usage: %0 file.boa [options]"
	"%~dp0\boa.bat" -c
    EXIT /B 2
)

"%~dp0\boa.bat" -c -i %*
