# 💼 Employee Payroll System

A Java Swing desktop application for managing employee payroll, built as an OOP assignment project.

## 🔐 Login
| Username | Password |
|----------|----------|
| `admin`  | `admin123` |

## 🚀 How to Run

**Windows**
```bat
compile.bat
run.bat
```

**Linux / macOS**
```bash
chmod +x compile.sh run.sh
./compile.sh && ./run.sh
```

**IntelliJ IDEA**
1. Open the `EmployeePayrollSystem` folder
2. Right-click `src/` → Mark as Sources Root
3. Run `Main.java`

> Requires Java JDK 17+

## 📦 Package Structure
com.payroll
├── Main.java              ← GUI entry point
├── model/                 ← Employee, FullTimeEmployee, PartTimeEmployee, ContractEmployee, Admin
├── service/               ← PayrollService (CRUD + search)
└── exception/             ← PayrollException
## ✨ Features
- Add / Edit / Delete employees (Full-Time, Part-Time, Contract)
- Search by name or minimum salary
- Payslip generation per employee
- Full payroll & department reports
- Export report to `.txt`
- Activity log with timestamps

## 🧠 OOP Concepts Used
`Inheritance` `Polymorphism` `Encapsulation` `Abstraction`
`Method Overloading` `Method Overriding` `Exception Handling` `Packages`
