@ECHO OFF

IF "%1" == "" (
    IF "%2" == "" (
        ECHO "Usage: %0 file.boa output-dir\ [options]"
		"%~dp0\boa.bat" -e
        EXIT /B 1
    )
)

SET input=%1
SET output=%2

SHIFT
SHIFT

IF NOT EXIST "%input%" (
    ECHO "input '%input%' is not a file"
	ECHO "Usage: %0 file.boa output-dir\ [options]"
	"%~dp0\boa.bat" -e
    EXIT /B 2
)

"%~dp0\boa.bat" -e -d dataset/ -i %input% -o %output% %*
