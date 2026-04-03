# Employee Payroll System

A console-style Java GUI application for managing employee payroll, demonstrating core Object-Oriented Programming (OOP) concepts.

---

## OOP Concepts Demonstrated

| Concept | Where Used |
|---|---|
| **Classes & Objects** | `Employee`, `Admin`, `PayrollService`, all subclasses |
| **Constructors** | Default + parameterized constructors in all model classes |
| **Encapsulation** | Private fields with getters/setters throughout |
| **Inheritance** | `FullTimeEmployee`, `PartTimeEmployee`, `ContractEmployee` extend `Employee` |
| **Polymorphism** | `calculateSalary()` called on any `Employee` type via abstract method |
| **Method Overriding** | `calculateSalary()`, `getEmployeeType()`, `toString()` in each subclass |
| **Method Overloading** | `searchEmployees(String)`, `searchEmployees(double)`, `searchEmployees(String, String)` in `PayrollService` |
| **Exception Handling** | Custom `PayrollException`, try-catch in all service and UI methods |
| **Packages** | `com.payroll`, `com.payroll.model`, `com.payroll.service`, `com.payroll.exception` |

---

## Project Structure

```
EmployeePayrollSystem/
├── src/
│   └── com/
│       └── payroll/
│           ├── Main.java                        ← GUI entry point
│           ├── model/
│           │   ├── Employee.java                ← Abstract base class
│           │   ├── FullTimeEmployee.java         ← Inherits Employee
│           │   ├── PartTimeEmployee.java         ← Inherits Employee
│           │   ├── ContractEmployee.java         ← Inherits Employee
│           │   └── Admin.java                   ← Admin credentials & auth
│           ├── service/
│           │   └── PayrollService.java           ← Business logic & CRUD
│           └── exception/
│               └── PayrollException.java         ← Custom exception
├── compile.bat     ← Windows compile script
├── compile.sh      ← Linux/macOS compile script
├── run.bat         ← Windows run script
├── run.sh          ← Linux/macOS run script
└── README.md
```

---

## How to Run

### Prerequisites
- Java JDK 17 or higher installed
- `javac` and `java` available in your PATH

### Windows
```bat
compile.bat
run.bat
```

### Linux / macOS
```bash
chmod +x compile.sh run.sh
./compile.sh
./run.sh
```

### Manual (any OS)
```bash
# From the EmployeePayrollSystem/ directory:

# Compile
javac -d out src/com/payroll/exception/PayrollException.java
javac -d out -cp out src/com/payroll/model/Employee.java
javac -d out -cp out src/com/payroll/model/FullTimeEmployee.java src/com/payroll/model/PartTimeEmployee.java src/com/payroll/model/ContractEmployee.java src/com/payroll/model/Admin.java
javac -d out -cp out src/com/payroll/service/PayrollService.java
javac -d out -cp out src/com/payroll/Main.java

# Run
java -cp out com.payroll.Main
```

---

## Login Credentials

| Field    | Value      |
|----------|------------|
| Username | `admin`    |
| Password | `admin123` |

---

## Features

- **Add / Edit / Delete** employees (Full-Time, Part-Time, Contract)
- **Search** by name or minimum salary (uses overloaded methods)
- **Payslip generation** per employee
- **Payroll reports** with total and average salary
- **Department reports** and summaries
- **Activity log** with timestamps
- **Export report** to a `.txt` file
- **Login screen** with authentication

---

## Employee Types & Salary Calculation

| Type | Formula |
|---|---|
| Full-Time | `Base Salary + Bonus + Medical Allowance (5000) - Deductions` |
| Part-Time | `Hourly Rate × Hours Worked` |
| Contract  | `(Contract Amount ÷ Duration in months) + Completion Bonus` |
