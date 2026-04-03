package com.payroll;

import com.payroll.exception.PayrollException;
import com.payroll.model.*;
import com.payroll.service.PayrollService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Main entry point and GUI class for the Employee Payroll System.
 * Builds and manages the Swing-based admin interface.
 *
 * Demonstrates: GUI design, Event-driven programming, OOP integration
 *
 * Default login: username = admin, password = admin123
 */
public class Main extends JFrame {

    private PayrollService payrollService;
    private Admin admin;
    private boolean isLoggedIn = false;

    // ==================== GUI COMPONENTS ====================
    private JTabbedPane tabbedPane;
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JLabel statusLabel;
    private JTextArea reportArea;
    private JTextArea deptReportArea;
    private JComboBox<String> deptCombo;

    /**
     * Constructor — sets up the payroll service, admin, and launches the login dialog.
     * If login is successful, the main GUI is initialized.
     */
    public Main() {
        payrollService = new PayrollService();
        admin = new Admin("admin", "admin123", "admin@payroll.com");

        setTitle("Employee Payroll System - Admin Panel");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Show login before the main window becomes visible
        showLoginDialog();

        if (isLoggedIn) {
            initializeGUI();
            setVisible(true);
        } else {
            System.exit(0);
        }
    }

    // ==================== LOGIN ====================

    /**
     * Displays a modal login dialog. Sets isLoggedIn = true on successful authentication.
     * No credentials are shown in error messages for security.
     */
    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Admin Login", true);
        loginDialog.setSize(400, 280);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Employee Payroll System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(userField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(loginButton, gbc);
        gbc.gridx = 1;
        panel.add(cancelButton, gbc);

        loginDialog.add(panel, BorderLayout.CENTER);

        // Login action — validates credentials without exposing them in the UI
        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (username.equals(admin.getUsername()) && admin.authenticate(password)) {
                admin.updateLastLogin();
                isLoggedIn = true;
                loginDialog.dispose();
            } else {
                // Security: do NOT show the correct password in the error message
                JOptionPane.showMessageDialog(loginDialog,
                        "Invalid username or password. Please try again.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                passField.setText(""); // Clear password field for retry
            }
        });

        cancelButton.addActionListener(e -> loginDialog.dispose());

        loginDialog.setVisible(true);
    }

    // ==================== GUI INITIALIZATION ====================

    /**
     * Builds the main GUI layout: menu bar, tabbed pane, and status bar.
     */
    private void initializeGUI() {
        setLayout(new BorderLayout());

        // ---- Menu Bar ----
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export Report");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // ---- Tabbed Panels ----
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Employee Management", createEmployeeManagementPanel());
        tabbedPane.addTab("Payroll Reports",     createPayrollReportsPanel());
        tabbedPane.addTab("Department Reports",  createDepartmentReportsPanel());
        tabbedPane.addTab("Activity Log",        createActivityLogPanel());
        add(tabbedPane, BorderLayout.CENTER);

        // ---- Status Bar ----
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.SOUTH);
        updateStatus();

        // ---- Menu Actions ----
        exportItem.addActionListener(e -> exportReport());
        exitItem.addActionListener(e -> System.exit(0));
        aboutItem.addActionListener(e -> showAbout());

        refreshEmployeeTable();
    }

    // ==================== TAB: EMPLOYEE MANAGEMENT ====================

    /**
     * Creates the Employee Management tab with a table and CRUD action buttons.
     */
    private JPanel createEmployeeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton addButton    = createStyledButton("Add Employee",    new Color(0, 153, 76));
        JButton editButton   = createStyledButton("Edit Employee",   new Color(0, 102, 204));
        JButton deleteButton = createStyledButton("Delete Employee", new Color(204, 0, 0));
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(refreshButton);

        // Employee table
        String[] columns = {"ID", "Name", "Type", "Department", "Email", "Salary"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table is read-only — edits go through the dialog
            }
        };
        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        employeeTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Employee List"));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Wire up button actions
        addButton.addActionListener(e -> showAddEmployeeDialog());
        editButton.addActionListener(e -> showEditEmployeeDialog());
        deleteButton.addActionListener(e -> deleteEmployee());
        searchButton.addActionListener(e -> searchEmployee());
        refreshButton.addActionListener(e -> refreshEmployeeTable());

        return panel;
    }

    /**
     * Helper to create a colored action button with white text.
     */
    private JButton createStyledButton(String label, Color background) {
        JButton btn = new JButton(label);
        btn.setBackground(background);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        return btn;
    }

    // ==================== TAB: PAYROLL REPORTS ====================

    /**
     * Creates the Payroll Reports tab with report generation, payslip, and top earners.
     */
    private JPanel createPayrollReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Report Options"));

        JButton generateReportBtn = createStyledButton("Generate Payroll Report", new Color(0, 102, 204));
        JButton generatePayslipBtn = new JButton("Generate Payslip");
        JButton topEarnersBtn      = new JButton("View Top Earners");

        topPanel.add(generateReportBtn);
        topPanel.add(generatePayslipBtn);
        topPanel.add(topEarnersBtn);

        reportArea = new JTextArea();
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        generateReportBtn.addActionListener(e -> reportArea.setText(generateFullReport()));
        generatePayslipBtn.addActionListener(e -> showPayslipDialog());
        topEarnersBtn.addActionListener(e -> showTopEarnersDialog());

        return panel;
    }

    // ==================== TAB: DEPARTMENT REPORTS ====================

    /**
     * Creates the Department Reports tab with per-department and summary reports.
     */
    private JPanel createDepartmentReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Department Selection"));

        deptCombo = new JComboBox<>();
        JButton viewButton        = new JButton("View Report");
        JButton summaryButton     = new JButton("View Summary");
        JButton refreshDeptButton = new JButton("Refresh Departments");

        inputPanel.add(new JLabel("Department:"));
        inputPanel.add(deptCombo);
        inputPanel.add(viewButton);
        inputPanel.add(summaryButton);
        inputPanel.add(refreshDeptButton);

        deptReportArea = new JTextArea();
        deptReportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        deptReportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(deptReportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Department Report"));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshDepartments();

        viewButton.addActionListener(e -> {
            String dept = (String) deptCombo.getSelectedItem();
            if (dept != null) deptReportArea.setText(generateDepartmentReport(dept));
        });
        summaryButton.addActionListener(e -> deptReportArea.setText(generateDepartmentSummary()));
        refreshDeptButton.addActionListener(e -> refreshDepartments());

        return panel;
    }

    // ==================== TAB: ACTIVITY LOG ====================

    /**
     * Creates the Activity Log tab showing all system events with timestamps.
     */
    private JPanel createActivityLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshLogBtn = new JButton("Refresh Log");
        JButton clearLogBtn   = new JButton("Clear Display");
        buttonPanel.add(refreshLogBtn);
        buttonPanel.add(clearLogBtn);

        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Activity Log"));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshLogBtn.addActionListener(e -> refreshActivityLog());
        clearLogBtn.addActionListener(e -> logArea.setText(""));

        refreshActivityLog();
        return panel;
    }

    // ==================== DIALOGS ====================

    /**
     * Shows a dialog to add a new employee.
     * Dynamically changes form fields based on the selected employee type.
     */
    private void showAddEmployeeDialog() {
        JDialog dialog = new JDialog(this, "Add New Employee", true);
        dialog.setSize(500, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Full-Time", "Part-Time", "Contract"});
        JTextField nameField  = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField deptField  = new JTextField(20);

        // Dynamic panel that changes fields based on employee type
        JPanel dynamicPanel = new JPanel(new GridBagLayout());
        dynamicPanel.setBorder(BorderFactory.createTitledBorder("Type-Specific Details"));

        addFormRow(panel, gbc, 0, "Employee Type:", typeCombo);
        addFormRow(panel, gbc, 1, "Name:",          nameField);
        addFormRow(panel, gbc, 2, "Email:",          emailField);
        addFormRow(panel, gbc, 3, "Department:",     deptField);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(dynamicPanel, gbc);

        // Reusable text fields for dynamic section
        JTextField field1 = new JTextField(15);
        JTextField field2 = new JTextField(15);
        JTextField field3 = new JTextField(15);

        // Rebuild dynamic section when employee type changes
        typeCombo.addActionListener(e -> {
            dynamicPanel.removeAll();
            GridBagConstraints dgbc = new GridBagConstraints();
            dgbc.fill = GridBagConstraints.HORIZONTAL;
            dgbc.insets = new Insets(5, 5, 5, 5);
            String type = (String) typeCombo.getSelectedItem();
            if ("Full-Time".equals(type)) {
                addFormRow(dynamicPanel, dgbc, 0, "Base Salary:",  field1);
                addFormRow(dynamicPanel, dgbc, 1, "Bonus:",        field2);
                addFormRow(dynamicPanel, dgbc, 2, "Deductions:",   field3);
                field1.setText("30000"); field2.setText("5000"); field3.setText("2000");
            } else if ("Part-Time".equals(type)) {
                addFormRow(dynamicPanel, dgbc, 0, "Hourly Rate:",   field1);
                addFormRow(dynamicPanel, dgbc, 1, "Hours Worked:",  field2);
                field1.setText("25"); field2.setText("80"); field3.setText("");
            } else {
                addFormRow(dynamicPanel, dgbc, 0, "Contract Amount:", field1);
                addFormRow(dynamicPanel, dgbc, 1, "Duration (months):", field2);
                addFormRow(dynamicPanel, dgbc, 2, "Completion Bonus:", field3);
                field1.setText("60000"); field2.setText("6"); field3.setText("2000");
            }
            dynamicPanel.revalidate();
            dynamicPanel.repaint();
        });

        // Bug fix: "Full-Time" is already selected at index 0 when the combo is created,
        // so setSelectedIndex(0) does NOT fire the ActionListener (no change detected).
        // We force it by temporarily deselecting first, which triggers the listener properly.
        typeCombo.setSelectedIndex(-1); // deselect
        typeCombo.setSelectedIndex(0);  // re-select "Full-Time" → fires ActionListener → fills panel

        JButton saveBtn   = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        JPanel btnPanel   = new JPanel(new FlowLayout());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(panel,   BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            try {
                String type  = (String) typeCombo.getSelectedItem();
                String name  = nameField.getText().trim();
                String email = emailField.getText().trim();
                String dept  = deptField.getText().trim();

                // Validate required base fields
                if (name.isEmpty() || email.isEmpty() || dept.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.");
                    return;
                }

                Employee employee = buildEmployee(type, name, email, dept, field1, field2, field3);
                if (employee == null) return;

                payrollService.addEmployee(employee);
                JOptionPane.showMessageDialog(dialog,
                        "Employee added successfully!\nAssigned ID: " + employee.getEmployeeId());
                refreshEmployeeTable();
                updateStatus();
                refreshDepartments();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric values for salary fields.");
            } catch (PayrollException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    /**
     * Shows a fully functional Edit Employee dialog pre-populated with the selected employee's data.
     * Fixed: replaces the old placeholder message with actual editing capability.
     */
    private void showEditEmployeeDialog() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee from the table to edit.");
            return;
        }

        // Get the selected employee's ID from the table
        int employeeId = (int) tableModel.getValueAt(selectedRow, 0);
        Employee existing;
        try {
            existing = payrollService.getEmployee(employeeId);
        } catch (PayrollException ex) {
            JOptionPane.showMessageDialog(this, "Could not load employee: " + ex.getMessage());
            return;
        }

        JDialog dialog = new JDialog(this, "Edit Employee - ID: " + employeeId, true);
        dialog.setSize(500, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Pre-fill common fields with existing data
        JTextField nameField  = new JTextField(existing.getName(), 20);
        JTextField emailField = new JTextField(existing.getEmail(), 20);
        JTextField deptField  = new JTextField(existing.getDepartment(), 20);

        // Type is fixed and shown as read-only label
        JLabel typeLabel = new JLabel(existing.getEmployeeType());
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD));

        addFormRow(panel, gbc, 0, "Employee Type:", typeLabel);
        addFormRow(panel, gbc, 1, "Name:",          nameField);
        addFormRow(panel, gbc, 2, "Email:",          emailField);
        addFormRow(panel, gbc, 3, "Department:",     deptField);

        // Dynamic section pre-filled based on existing employee type
        JPanel dynamicPanel = new JPanel(new GridBagLayout());
        dynamicPanel.setBorder(BorderFactory.createTitledBorder("Type-Specific Details"));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(dynamicPanel, gbc);

        JTextField field1 = new JTextField(15);
        JTextField field2 = new JTextField(15);
        JTextField field3 = new JTextField(15);

        GridBagConstraints dgbc = new GridBagConstraints();
        dgbc.fill = GridBagConstraints.HORIZONTAL;
        dgbc.insets = new Insets(5, 5, 5, 5);

        // Pre-populate type-specific fields
        if (existing instanceof FullTimeEmployee fte) {
            addFormRow(dynamicPanel, dgbc, 0, "Base Salary:",  field1);
            addFormRow(dynamicPanel, dgbc, 1, "Bonus:",        field2);
            addFormRow(dynamicPanel, dgbc, 2, "Deductions:",   field3);
            field1.setText(String.valueOf(fte.getBaseSalary()));
            field2.setText(String.valueOf(fte.getBonus()));
            field3.setText(String.valueOf(fte.getDeductions()));
        } else if (existing instanceof PartTimeEmployee pte) {
            addFormRow(dynamicPanel, dgbc, 0, "Hourly Rate:",  field1);
            addFormRow(dynamicPanel, dgbc, 1, "Hours Worked:", field2);
            field1.setText(String.valueOf(pte.getHourlyRate()));
            field2.setText(String.valueOf(pte.getHoursWorked()));
        } else if (existing instanceof ContractEmployee ce) {
            addFormRow(dynamicPanel, dgbc, 0, "Contract Amount:",   field1);
            addFormRow(dynamicPanel, dgbc, 1, "Duration (months):", field2);
            addFormRow(dynamicPanel, dgbc, 2, "Completion Bonus:",  field3);
            field1.setText(String.valueOf(ce.getContractAmount()));
            field2.setText(String.valueOf(ce.getContractDuration()));
            field3.setText(String.valueOf(ce.getCompletionBonus()));
        }

        JButton saveBtn   = new JButton("Update");
        JButton cancelBtn = new JButton("Cancel");
        JPanel btnPanel   = new JPanel(new FlowLayout());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(panel,   BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            try {
                String name  = nameField.getText().trim();
                String email = emailField.getText().trim();
                String dept  = deptField.getText().trim();

                if (name.isEmpty() || email.isEmpty() || dept.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.");
                    return;
                }

                // Build updated employee of the same type
                Employee updated = buildEmployee(
                        existing.getEmployeeType(), name, email, dept, field1, field2, field3);
                if (updated == null) return;

                payrollService.updateEmployee(employeeId, updated);
                JOptionPane.showMessageDialog(dialog, "Employee updated successfully!");
                refreshEmployeeTable();
                updateStatus();
                refreshDepartments();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numeric values.");
            } catch (PayrollException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    /**
     * Shared helper: builds an Employee object from form fields based on type.
     * Used by both Add and Edit dialogs.
     */
    private Employee buildEmployee(String type, String name, String email, String dept,
                                   JTextField f1, JTextField f2, JTextField f3) {
        switch (type) {
            case "Full-Time" -> {
                double base  = Double.parseDouble(f1.getText().trim());
                double bonus = Double.parseDouble(f2.getText().trim());
                double ded   = Double.parseDouble(f3.getText().trim());
                return new FullTimeEmployee(name, email, dept, base, bonus, ded);
            }
            case "Part-Time" -> {
                double rate  = Double.parseDouble(f1.getText().trim());
                int hours    = Integer.parseInt(f2.getText().trim());
                return new PartTimeEmployee(name, email, dept, rate, hours);
            }
            case "Contract" -> {
                double amount    = Double.parseDouble(f1.getText().trim());
                int duration     = Integer.parseInt(f2.getText().trim());
                double compBonus = f3.getText().trim().isEmpty() ? 0 : Double.parseDouble(f3.getText().trim());
                return new ContractEmployee(name, email, dept, amount, duration, compBonus);
            }
            default -> {
                JOptionPane.showMessageDialog(this, "Unknown employee type: " + type);
                return null;
            }
        }
    }

    /**
     * Utility method to add a label + component pair in a GridBagLayout panel.
     */
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    /**
     * Deletes the currently selected employee after a confirmation prompt.
     */
    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.");
            return;
        }

        int employeeId    = (int) tableModel.getValueAt(selectedRow, 0);
        String employeeName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete " + employeeName + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                payrollService.deleteEmployee(employeeId);
                JOptionPane.showMessageDialog(this, employeeName + " has been deleted.");
                refreshEmployeeTable();
                updateStatus();
                refreshDepartments();
            } catch (PayrollException ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
            }
        }
    }

    /**
     * Opens a search dialog that supports searching by name or by minimum salary.
     * Demonstrates use of overloaded searchEmployees() methods.
     */
    private void searchEmployee() {
        String[] options = {"Search by Name", "Search by Min Salary"};
        int choice = JOptionPane.showOptionDialog(this,
                "Select search type:", "Search Employee",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            // Search by name — calls searchEmployees(String)
            String name = JOptionPane.showInputDialog(this, "Enter employee name to search:");
            if (name == null || name.trim().isEmpty()) return;
            List<Employee> results = payrollService.searchEmployees(name.trim());
            displaySearchResults(results, "Search by Name: " + name);

        } else if (choice == 1) {
            // Search by minimum salary — calls searchEmployees(double)
            String minStr = JOptionPane.showInputDialog(this, "Enter minimum salary:");
            if (minStr == null || minStr.trim().isEmpty()) return;
            try {
                double min = Double.parseDouble(minStr.trim());
                List<Employee> results = payrollService.searchEmployees(min);
                displaySearchResults(results, "Employees earning >= " + min);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        }
    }

    /**
     * Displays search results in a scrollable dialog.
     */
    private void displaySearchResults(List<Employee> results, String title) {
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employees found for: " + title);
            return;
        }
        StringBuilder sb = new StringBuilder("Found " + results.size() + " result(s):\n\n");
        for (Employee emp : results) {
            sb.append(emp.toString()).append("\n");
        }
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane sp = new JScrollPane(textArea);
        sp.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(this, sp, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a payslip dialog for a given employee ID.
     */
    private void showPayslipDialog() {
        String idStr = JOptionPane.showInputDialog(this, "Enter Employee ID:");
        if (idStr == null) return;
        try {
            int id = Integer.parseInt(idStr.trim());
            Employee emp = payrollService.getEmployee(id);

            StringBuilder sb = new StringBuilder();
            sb.append("=".repeat(60)).append("\n");
            sb.append("                    PAYSLIP\n");
            sb.append("=".repeat(60)).append("\n");
            sb.append("Employee ID:  ").append(emp.getEmployeeId()).append("\n");
            sb.append("Name:         ").append(emp.getName()).append("\n");
            sb.append("Email:        ").append(emp.getEmail()).append("\n");
            sb.append("Department:   ").append(emp.getDepartment()).append("\n");
            sb.append("Type:         ").append(emp.getEmployeeType()).append("\n");
            sb.append("-".repeat(60)).append("\n");

            if (emp instanceof FullTimeEmployee fte) {
                sb.append("Base Salary:         ").append(fte.getBaseSalary()).append("\n");
                sb.append("Bonus:               ").append(fte.getBonus()).append("\n");
                sb.append("Medical Allowance:   ").append(fte.getMedicalAllowance()).append("\n");
                sb.append("Deductions:          ").append(fte.getDeductions()).append("\n");
            } else if (emp instanceof PartTimeEmployee pte) {
                sb.append("Hourly Rate:         ").append(pte.getHourlyRate()).append("\n");
                sb.append("Hours Worked:        ").append(pte.getHoursWorked()).append("\n");
            } else if (emp instanceof ContractEmployee ce) {
                sb.append("Contract Amount:     ").append(ce.getContractAmount()).append("\n");
                sb.append("Contract Duration:   ").append(ce.getContractDuration()).append(" months\n");
                sb.append("Completion Bonus:    ").append(ce.getCompletionBonus()).append("\n");
            }

            sb.append("-".repeat(60)).append("\n");
            sb.append("NET SALARY:   ").append(String.format("%.2f", emp.calculateSalary())).append("\n");
            sb.append("=".repeat(60)).append("\n");

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            JScrollPane sp = new JScrollPane(textArea);
            sp.setPreferredSize(new Dimension(500, 400));
            JOptionPane.showMessageDialog(this, sp, "Payslip - " + emp.getName(),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric ID.");
        } catch (PayrollException ex) {
            JOptionPane.showMessageDialog(this, "Employee not found: " + ex.getMessage());
        }
    }

    /**
     * Shows the top N earners in a dialog.
     */
    private void showTopEarnersDialog() {
        String nStr = JOptionPane.showInputDialog(this, "How many top earners to display?", "3");
        if (nStr == null) return;
        try {
            int n = Integer.parseInt(nStr.trim());
            List<Employee> topEarners = payrollService.getTopEarners(n);

            StringBuilder sb = new StringBuilder();
            sb.append("=".repeat(70)).append("\n");
            sb.append("TOP ").append(n).append(" EARNERS\n");
            sb.append("=".repeat(70)).append("\n");
            sb.append(String.format("%-5s %-25s %-12s %-15s %s\n", "Rank", "Name", "Type", "Department", "Salary"));
            sb.append("-".repeat(70)).append("\n");

            int rank = 1;
            for (Employee emp : topEarners) {
                sb.append(String.format("%-5d %-25s %-12s %-15s %.2f\n",
                        rank++, emp.getName(), emp.getEmployeeType(), emp.getDepartment(), emp.calculateSalary()));
            }
            sb.append("=".repeat(70)).append("\n");

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            JScrollPane sp = new JScrollPane(textArea);
            sp.setPreferredSize(new Dimension(600, 350));
            JOptionPane.showMessageDialog(this, sp, "Top Earners", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
        }
    }

    // ==================== REPORT GENERATORS ====================

    /**
     * Generates a full payroll report for all employees.
     */
    private String generateFullReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(80)).append("\n");
        sb.append("                    EMPLOYEE PAYROLL REPORT\n");
        sb.append(new SimpleDateFormat("Generated: dd-MMM-yyyy HH:mm:ss\n").format(new Date()));
        sb.append("=".repeat(80)).append("\n");
        sb.append(String.format("%-8s %-25s %-12s %-15s %-12s\n", "ID", "Name", "Type", "Department", "Salary"));
        sb.append("-".repeat(80)).append("\n");

        for (Employee emp : payrollService.getAllEmployees()) {
            sb.append(String.format("%-8d %-25s %-12s %-15s $%-10.2f\n",
                    emp.getEmployeeId(), emp.getName(), emp.getEmployeeType(),
                    emp.getDepartment(), emp.calculateSalary()));
        }

        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("Total Employees: %d\n", payrollService.getEmployeeCount()));
        sb.append(String.format("Total Payroll:   %.2f\n", payrollService.calculateTotalPayroll()));
        if (payrollService.getEmployeeCount() > 0) {
            sb.append(String.format("Average Salary:  %.2f\n",
                    payrollService.calculateTotalPayroll() / payrollService.getEmployeeCount()));
        }
        sb.append("=".repeat(80)).append("\n");
        return sb.toString();
    }

    /**
     * Generates a report for a single department.
     */
    private String generateDepartmentReport(String department) {
        List<Employee> employees = payrollService.getEmployeesByDepartment(department);
        if (employees.isEmpty()) return "No employees found in department: " + department;

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(70)).append("\n");
        sb.append("DEPARTMENT REPORT: ").append(department.toUpperCase()).append("\n");
        sb.append("=".repeat(70)).append("\n");
        sb.append(String.format("%-8s %-25s %-12s %-10s\n", "ID", "Name", "Type", "Salary"));
        sb.append("-".repeat(70)).append("\n");

        double total = 0;
        for (Employee emp : employees) {
            sb.append(String.format("%-8d %-25s %-12s %-10.2f\n",
                    emp.getEmployeeId(), emp.getName(), emp.getEmployeeType(), emp.calculateSalary()));
            total += emp.calculateSalary();
        }
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("Employees: %d | Total Payroll: %.2f\n", employees.size(), total));
        sb.append("=".repeat(70)).append("\n");
        return sb.toString();
    }

    /**
     * Generates a summary of payroll broken down by department.
     */
    private String generateDepartmentSummary() {
        Map<String, Double> deptPayroll = payrollService.getDepartmentWisePayroll();
        if (deptPayroll.isEmpty()) return "No department data available.";

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("DEPARTMENT-WISE PAYROLL SUMMARY\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("%-20s %-15s\n", "Department", "Total Payroll"));
        sb.append("-".repeat(60)).append("\n");

        for (Map.Entry<String, Double> entry : deptPayroll.entrySet()) {
            sb.append(String.format("%-20s %-15.2f\n", entry.getKey(), entry.getValue()));
        }

        sb.append("-".repeat(60)).append("\n");
        sb.append(String.format("Grand Total: %.2f\n", payrollService.calculateTotalPayroll()));
        sb.append("=".repeat(60)).append("\n");
        return sb.toString();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Exports the full payroll report to a text file chosen by the user.
     */
    private void exportReport() {
        String filename = JOptionPane.showInputDialog(this, "Enter filename (e.g., payroll.txt):", "payroll_report.txt");
        if (filename == null || filename.trim().isEmpty()) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename.trim()))) {
            writer.write(generateFullReport());
            JOptionPane.showMessageDialog(this, "Report exported to: " + filename);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage());
        }
    }

    /** Reloads the employee table from the payroll service data. */
    private void refreshEmployeeTable() {
        tableModel.setRowCount(0);
        for (Employee emp : payrollService.getAllEmployees()) {
            tableModel.addRow(new Object[]{
                    emp.getEmployeeId(), emp.getName(), emp.getEmployeeType(),
                    emp.getDepartment(), emp.getEmail(), String.format("%.2f", emp.calculateSalary())
            });
        }
    }

    /** Reloads the department dropdown from current employee data. */
    private void refreshDepartments() {
        deptCombo.removeAllItems();
        for (String dept : payrollService.getAllDepartments()) {
            deptCombo.addItem(dept);
        }
        if (deptCombo.getItemCount() == 0) deptCombo.addItem("No departments");
    }

    /** Reloads the activity log text area. */
    private void refreshActivityLog() {
        StringBuilder sb = new StringBuilder();
        for (String log : payrollService.getActivityLog()) sb.append(log).append("\n");
        logArea.setText(sb.toString());
    }

    /** Updates the status bar with current employee count and total payroll. */
    private void updateStatus() {
        statusLabel.setText(MessageFormat.format(
                " Ready | Employees: {0} | Total Payroll: {1}",
                payrollService.getEmployeeCount(),
                String.format("%.2f", payrollService.calculateTotalPayroll())));
    }

    /** Shows the About dialog. */
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Employee Payroll System\nVersion 2.0\n\n" +
                        "Demonstrates core OOP concepts:\n" +
                        " - Inheritance, Polymorphism, Encapsulation\n" +
                        " - Method Overloading & Overriding\n" +
                        " - Exception Handling\n" +
                        " - Packages & Class Structure\n\n" +
                        "Built with Java Swing (GUI)\n" +
                        "© 2024 Employee Payroll System",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== MAIN METHOD ====================

    /**
     * Application entry point.
     * Uses SwingUtilities.invokeLater to ensure GUI is created on the Event Dispatch Thread.
     */
    public static void main(String[] args) {
        // Apply system look and feel for native OS appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch the application safely on the Swing thread
        SwingUtilities.invokeLater(Main::new);
    }
}
