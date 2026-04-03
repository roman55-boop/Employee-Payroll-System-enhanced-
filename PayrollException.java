@echo off
echo Compiling Employee Payroll System...
if not exist out mkdir out

javac -d out src/com/payroll/exception/PayrollException.java
javac -d out -cp out src/com/payroll/model/Employee.java
javac -d out -cp out src/com/payroll/model/FullTimeEmployee.java src/com/payroll/model/PartTimeEmployee.java src/com/payroll/model/ContractEmployee.java src/com/payroll/model/Admin.java
javac -d out -cp out src/com/payroll/service/PayrollService.java
javac -d out -cp out src/com/payroll/Main.java

if %ERRORLEVEL% == 0 (
    echo Compilation successful!
) else (
    echo Compilation failed. Check errors above.
)
pause
