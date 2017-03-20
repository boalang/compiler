@ECHO OFF

IF "%1" == "" && "%2" == "" (
    ECHO "Usage: %0 path\to\input.boa output-dir [options]"
    EXIT 1
)

input="%1"
output="%2"

SHIFT
SHIFT

IF NOT EXIST "%input%" (
    ECHO "input '%input%' is not a file"
    EXIT 2
)

REM need to convert this to batch script
REM if [ -d $2 ]; then
REM     read -n 1 -p "output directory '$2' exists - delete? [Y/n] " yn
REM     echo ""
REM
REM     yn=`echo $yn | tr '[:upper:]' '[:lower:]'`
REM
REM     if [[ $yn =~ ^(y| ) ]] | [ -z $yn ]; then
REM         rm -Rf $2
REM     else
REM         echo "Please remove or provide a different output directory."
REM         exit -3
REM     fi
REM fi

"%~dp0\boa.bat" -e -d dataset/ -i %input% -o %output% %*
