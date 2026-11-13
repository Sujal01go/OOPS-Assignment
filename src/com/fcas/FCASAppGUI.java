package com.fcas;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern High-End Enterprise UI for Faculty-Course Assignment System (FCAS).
 * Styled with FlatLaf Light, custom cards, clean typography, and zero-setup SQLite database.
 */
public class FCASAppGUI extends JFrame {

    // Domain models
    private AssignmentSystem assignmentSystem;
    private List<Department> departments;
    private List<Course> courses;

    // UI Tables & Models
    private DefaultTableModel teacherTableModel;
    private DefaultTableModel sectionTableModel;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel deptTableModel;

    private TableRowSorter<DefaultTableModel> teacherSorter;
    private TableRowSorter<DefaultTableModel> sectionSorter;

    // Controls
    private JComboBox<TeacherItem> teacherCombo;
    private JComboBox<SectionItem> sectionCombo;
    private JTextField teacherSearchField;
    private JTextField sectionSearchField;
    private JTextArea logArea;

    // Executive KPI Summary Labels
    private JLabel totalFacultyLabel;
    private JLabel totalSectionsLabel;
    private JLabel assignedSectionsLabel;
    private JLabel capacityUtilLabel;
    private JLabel dbStatusLabel;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Colors
    private static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    private static final Color DARK_HEADER = new Color(15, 23, 42);
    private static final Color BG_MAIN = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);

    // Item Wrappers for Combo Boxes
    public record TeacherItem(Teacher teacher) {
        @Override
        public String toString() {
            return teacher.getName() + " (" + teacher.getId() + ") - Load: " +
                    teacher.getCurrentHours() + "/" + teacher.getMaxHours() + "h [" + teacher.getDepartment() + "]";
        }
    }

    public record SectionItem(Section section) {
        @Override
        public String toString() {
            String cCode = section.getCourse() != null ? section.getCourse().getCourseCode() : "N/A";
            String teacherName = section.getTeacher() != null ? " [✓ " + section.getTeacher().getName() + "]" : " [Unassigned]";
            return section.getSectionId() + " (" + cCode + ") @ " + section.getTimeSlot() + teacherName;
        }
    }

    public FCASAppGUI() {
        this.assignmentSystem = new AssignmentSystem();
        this.departments = new ArrayList<>();
        this.courses = new ArrayList<>();

        initUI();
        loadSystemData();
    }

    /**
     * Professional flat button generator with crisp visible text, smooth rounded edges, and hover feedback.
     */
    private JButton createActionButton(String text, Color bgColor, Color fgColor, int width, int height) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(
                            Math.min(255, bgColor.getRed() + 15),
                            Math.min(255, bgColor.getGreen() + 15),
                            Math.min(255, bgColor.getBlue() + 15)
                    ));
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fgColor);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        if (width > 0 && height > 0) {
            btn.setPreferredSize(new Dimension(width, height));
        }
        return btn;
    }

    private void loadSystemData() {
        DatabaseManager.seedIndianFacultyData(assignmentSystem, departments, courses);
        refreshAllViews();
        logMessage("Connected to zero-setup local SQLite database (fcas_database.db).");
        logMessage("Loaded Indian University faculty profiles & academic subjects.");
    }

    private void initUI() {
        setTitle("Faculty-Course Assignment System (FCAS) - Academic Management Suite");
        setSize(1300, 880);
        setMinimumSize(new Dimension(1100, 740));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Menu Bar
        setJMenuBar(createMenuBar());

        // Master Container
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBorder(new EmptyBorder(12, 14, 12, 14));
        mainContainer.setBackground(BG_MAIN);

        // Top Banner & KPI Summary
        JPanel northSection = new JPanel(new BorderLayout(8, 8));
        northSection.setBackground(BG_MAIN);
        northSection.add(createHeaderBanner(), BorderLayout.NORTH);
        northSection.add(createKpiPanel(), BorderLayout.SOUTH);
        mainContainer.add(northSection, BorderLayout.NORTH);

        // Center: Tabbed Workspace
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        tabbedPane.addTab("Allocation Workspace", createAssignmentWorkspaceTab());
        tabbedPane.addTab("Faculty Directory", createFacultyDirectoryTab());
        tabbedPane.addTab("Courses & Subjects", createCourseSectionTab());
        tabbedPane.addTab("Departments", createDepartmentTab());
        tabbedPane.addTab("Weekly Timetable Matrix", createTimetableMatrixTab());
        tabbedPane.addTab("Workload Analytics", createAnalyticsTab());
        mainContainer.add(tabbedPane, BorderLayout.CENTER);

        // Bottom: Status & Audit Console
        mainContainer.add(createLogConsolePanel(), BorderLayout.SOUTH);

        setContentPane(mainContainer);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Database Menu
        JMenu dbMenu = new JMenu("Database");
        JMenuItem reloadDbItem = new JMenuItem("Reload from Database");
        JMenuItem resetDbItem = new JMenuItem("Reset & Re-seed Database (Default Indian Faculty)");

        reloadDbItem.addActionListener(e -> {
            try {
                DatabaseManager.loadFromDatabase(assignmentSystem, departments, courses);
                refreshAllViews();
                logMessage("DATABASE: Reloaded records from SQLite database.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load database: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        resetDbItem.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to reset the database?\nThis will recreate all tables with default Indian faculty records.",
                    "Reset Database", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                try {
                    DatabaseManager.resetDatabase();
                    DatabaseManager.loadFromDatabase(assignmentSystem, departments, courses);
                    refreshAllViews();
                    logMessage("DATABASE: Successfully reset and re-seeded with Indian faculty records.");
                    JOptionPane.showMessageDialog(this, "Database re-seeded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Reset error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dbMenu.add(reloadDbItem);
        dbMenu.addSeparator();
        dbMenu.add(resetDbItem);

        // Smart Actions
        JMenu smartMenu = new JMenu("Smart Actions");
        JMenuItem autoAssignItem = new JMenuItem("⚡ Run Smart Auto-Assignment Optimizer");
        JMenuItem clearAllAssignments = new JMenuItem("⚠️ Clear All Section Assignments");

        autoAssignItem.addActionListener(e -> handleAutoAssignOptimizer());
        clearAllAssignments.addActionListener(e -> handleClearAllAssignments());

        smartMenu.add(autoAssignItem);
        smartMenu.addSeparator();
        smartMenu.add(clearAllAssignments);

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveSnapshot = new JMenuItem("Save Backup Snapshot (.fcas)...");
        JMenuItem loadSnapshot = new JMenuItem("Restore Backup Snapshot (.fcas)...");
        JMenuItem exitItem = new JMenuItem("Exit");

        saveSnapshot.addActionListener(e -> handleSaveSnapshot());
        loadSnapshot.addActionListener(e -> handleLoadSnapshot());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(saveSnapshot);
        fileMenu.add(loadSnapshot);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Export Reports
        JMenu exportMenu = new JMenu("Export");
        JMenuItem expSchedule = new JMenuItem("Export Timetable Schedule (CSV)...");
        JMenuItem expWorkload = new JMenuItem("Export Faculty Workload Report (CSV)...");

        expSchedule.addActionListener(e -> handleExportScheduleCSV());
        expWorkload.addActionListener(e -> handleExportWorkloadCSV());

        exportMenu.add(expSchedule);
        exportMenu.add(expWorkload);

        // Help
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("System Architecture & Blueprint...");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(dbMenu);
        menuBar.add(smartMenu);
        menuBar.add(exportMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JPanel createHeaderBanner() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_HEADER);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Faculty-Course Assignment System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        dbStatusLabel = new JLabel("● Database: SQLite Local Embedded (fcas_database.db) • Indian University Academic Workload Suite");
        dbStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dbStatusLabel.setForeground(new Color(52, 211, 153));

        header.add(title, BorderLayout.NORTH);
        header.add(dbStatusLabel, BorderLayout.SOUTH);
        return header;
    }

    private JPanel createKpiPanel() {
        JPanel kpiContainer = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiContainer.setBackground(BG_MAIN);

        totalFacultyLabel = new JLabel("0");
        totalSectionsLabel = new JLabel("0");
        assignedSectionsLabel = new JLabel("0");
        capacityUtilLabel = new JLabel("0.0%");

        kpiContainer.add(createCard("TOTAL REGISTERED FACULTY", totalFacultyLabel, new Color(37, 99, 235)));
        kpiContainer.add(createCard("TOTAL CLASS SECTIONS", totalSectionsLabel, new Color(13, 148, 136)));
        kpiContainer.add(createCard("ALLOCATION STATUS", assignedSectionsLabel, new Color(217, 119, 6)));
        kpiContainer.add(createCard("FACULTY CAPACITY UTILIZATION", capacityUtilLabel, new Color(16, 185, 129)));

        return kpiContainer;
    }

    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(4, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // --- Tab 1: Allocation Workspace ---
    private JPanel createAssignmentWorkspaceTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Allocation Toolbar Card
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        controlPanel.setBackground(CARD_BG);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        teacherCombo = new JComboBox<>();
        teacherCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        teacherCombo.setPreferredSize(new Dimension(300, 34));

        sectionCombo = new JComboBox<>();
        sectionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sectionCombo.setPreferredSize(new Dimension(330, 34));

        // Polished modern action buttons
        JButton assignBtn = createActionButton("Assign Section", new Color(37, 99, 235), Color.WHITE, 135, 34);
        JButton unassignBtn = createActionButton("Unassign", new Color(239, 68, 68), Color.WHITE, 100, 34);
        JButton autoBtn = createActionButton("⚡ Auto-Assign", new Color(16, 185, 129), Color.WHITE, 130, 34);

        JLabel facLbl = new JLabel("Faculty:");
        facLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel secLbl = new JLabel("Section:");
        secLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

        controlPanel.add(facLbl);
        controlPanel.add(teacherCombo);
        controlPanel.add(secLbl);
        controlPanel.add(sectionCombo);
        controlPanel.add(assignBtn);
        controlPanel.add(unassignBtn);
        controlPanel.add(autoBtn);

        assignBtn.addActionListener(e -> handleAssignAction());
        unassignBtn.addActionListener(e -> handleUnassignAction());
        autoBtn.addActionListener(e -> handleAutoAssignOptimizer());

        // Split View
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createTeacherTableSubPanel(),
                createSectionTableSubPanel());
        splitPane.setResizeWeight(0.44);
        splitPane.setDividerSize(6);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTeacherTableSubPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        searchBar.setBackground(CARD_BG);
        JLabel searchLbl = new JLabel("🔍 Filter Faculty:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchBar.add(searchLbl);
        teacherSearchField = new JTextField(15);
        teacherSearchField.setPreferredSize(new Dimension(160, 28));
        searchBar.add(teacherSearchField);

        String[] cols = {"ID", "Name", "Role", "Dept", "Load (Hrs)", "Teaching Status"};
        teacherTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(teacherTableModel);
        styleTable(table);
        teacherSorter = new TableRowSorter<>(teacherTableModel);
        table.setRowSorter(teacherSorter);

        teacherSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String text = teacherSearchField.getText().trim();
            teacherSorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
        }));

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSectionTableSubPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        searchBar.setBackground(CARD_BG);
        JLabel searchLbl = new JLabel("🔍 Filter Sections:");
        searchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchBar.add(searchLbl);
        sectionSearchField = new JTextField(15);
        sectionSearchField.setPreferredSize(new Dimension(160, 28));
        searchBar.add(sectionSearchField);

        String[] cols = {"Section", "Course", "Credits", "Room / Hall", "Time Slot", "Assigned Faculty"};
        sectionTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(sectionTableModel);
        styleTable(table);
        sectionSorter = new TableRowSorter<>(sectionTableModel);
        table.setRowSorter(sectionSorter);

        sectionSearchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            String text = sectionSearchField.getText().trim();
            sectionSorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
        }));

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 2: Faculty Directory & Management ---
    private JPanel createFacultyDirectoryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(CARD_BG);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JButton addFtBtn = createActionButton("+ Add Full-Time Faculty", new Color(37, 99, 235), Color.WHITE, 190, 34);
        JButton addPtBtn = createActionButton("+ Add Part-Time Faculty", new Color(13, 148, 136), Color.WHITE, 190, 34);
        JButton editBtn = createActionButton("Edit Selected", new Color(217, 119, 6), Color.WHITE, 125, 34);
        JButton removeBtn = createActionButton("Delete Selected", new Color(239, 68, 68), Color.WHITE, 135, 34);

        toolbar.add(addFtBtn);
        toolbar.add(addPtBtn);
        toolbar.add(editBtn);
        toolbar.add(removeBtn);

        panel.add(toolbar, BorderLayout.NORTH);

        JTable table = new JTable(teacherTableModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addFtBtn.addActionListener(e -> showAddFacultyDialog(true));
        addPtBtn.addActionListener(e -> showAddFacultyDialog(false));
        editBtn.addActionListener(e -> handleEditFaculty(table));
        removeBtn.addActionListener(e -> handleDeleteFaculty(table));

        return panel;
    }

    private void handleEditFaculty(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member to edit.", "Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) teacherTableModel.getValueAt(modelRow, 0);
        Teacher t = assignmentSystem.getTeacherList().stream()
                .filter(x -> x.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);

        if (t != null) {
            JTextField nameField = new JTextField(t.getName());
            JTextField emailField = new JTextField(t.getEmail());
            JTextField deptField = new JTextField(t.getDepartment());
            JTextField maxHrsField = new JTextField(String.valueOf(t.getMaxHours()));

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
            form.add(new JLabel("Full Name:")); form.add(nameField);
            form.add(new JLabel("Email:")); form.add(emailField);
            form.add(new JLabel("Department:")); form.add(deptField);
            form.add(new JLabel("Max Workload (Hours):")); form.add(maxHrsField);

            int res = JOptionPane.showConfirmDialog(this, form, "Edit Faculty - " + t.getId(), JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                try {
                    t.setName(nameField.getText().trim());
                    t.setEmail(emailField.getText().trim());
                    t.setDepartment(deptField.getText().trim().toUpperCase());
                    t.setMaxHours(Integer.parseInt(maxHrsField.getText().trim()));

                    DatabaseManager.addTeacherToDB(t);
                    refreshAllViews();
                    logMessage("DATABASE & SYSTEM: Updated faculty profile for " + t.getName());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleDeleteFaculty(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty member from the table.", "Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String id = (String) teacherTableModel.getValueAt(modelRow, 0);
        Teacher target = assignmentSystem.getTeacherList().stream()
                .filter(t -> t.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);

        if (target != null) {
            int conf = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete " + target.getName() + "?\nAll assigned sections will be unassigned automatically.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                assignmentSystem.removeTeacher(target);
                for (Department d : departments) {
                    d.removeTeacher(target);
                }
                DatabaseManager.deleteTeacherFromDB(target.getId());
                refreshAllViews();
                logMessage("DATABASE & SYSTEM: Removed faculty member " + target.getName() + " (" + target.getId() + ")");
            }
        }
    }

    // --- Tab 3: Courses & Sections Management ---
    private JPanel createCourseSectionTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(CARD_BG);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JButton addCourseBtn = createActionButton("+ Add Subject / Course", new Color(37, 99, 235), Color.WHITE, 185, 34);
        JButton addSectionBtn = createActionButton("+ Add Class Section", new Color(13, 148, 136), Color.WHITE, 165, 34);
        JButton deleteCourseBtn = createActionButton("Delete Selected Course", new Color(239, 68, 68), Color.WHITE, 185, 34);

        toolbar.add(addCourseBtn);
        toolbar.add(addSectionBtn);
        toolbar.add(deleteCourseBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Course Code", "Subject / Course Title", "Credits / Hours", "Total Sections", "Department"};
        courseTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(courseTableModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addCourseBtn.addActionListener(e -> showAddCourseDialog());
        addSectionBtn.addActionListener(e -> showAddSectionDialog());
        deleteCourseBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a course to delete.", "Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String code = (String) courseTableModel.getValueAt(modelRow, 0);
            Course target = courses.stream().filter(c -> c.getCourseCode().equalsIgnoreCase(code)).findFirst().orElse(null);
            if (target != null) {
                int conf = JOptionPane.showConfirmDialog(this, "Delete course " + target.getCourseCode() + " and all its sections?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (conf == JOptionPane.YES_OPTION) {
                    for (Section s : new ArrayList<>(target.getSections())) {
                        assignmentSystem.removeSection(s);
                    }
                    courses.remove(target);
                    for (Department d : departments) d.removeCourse(target);
                    DatabaseManager.deleteCourseFromDB(target.getCourseCode());
                    refreshAllViews();
                    logMessage("DATABASE & SYSTEM: Deleted course " + target.getCourseCode());
                }
            }
        });

        return panel;
    }

    // --- Tab 4: Departments ---
    private JPanel createDepartmentTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.setBackground(CARD_BG);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JButton addDeptBtn = createActionButton("+ Add Academic Department", new Color(37, 99, 235), Color.WHITE, 210, 34);
        toolbar.add(addDeptBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Dept ID", "Department Name", "Total Faculty Aggregated", "Total Courses"};
        deptTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(deptTableModel);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addDeptBtn.addActionListener(e -> showAddDepartmentDialog());
        return panel;
    }

    // --- Tab 5: Timetable Matrix ---
    private JPanel createTimetableMatrixTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_MAIN);
        JLabel infoLbl = new JLabel("📅 Institutional Weekly Timetable Schedule by Slot");
        infoLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoLbl.setForeground(new Color(30, 41, 59));
        topBar.add(infoLbl, BorderLayout.WEST);

        JButton refreshMatrixBtn = createActionButton("↻ Refresh Timetable Matrix", new Color(37, 99, 235), Color.WHITE, 200, 32);
        topBar.add(refreshMatrixBtn, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Day & Time Slot", "Section ID", "Course Code", "Classroom / Hall", "Faculty Assigned"};
        DefaultTableModel matrixModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(matrixModel);
        styleTable(table);

        refreshMatrixBtn.addActionListener(e -> {
            matrixModel.setRowCount(0);
            for (Section s : assignmentSystem.getSectionList()) {
                String cCode = s.getCourse() != null ? s.getCourse().getCourseCode() : "N/A";
                String fac = s.getTeacher() != null ? s.getTeacher().getName() : "(Unassigned)";
                matrixModel.addRow(new Object[]{
                        s.getTimeSlot(),
                        s.getSectionId(),
                        cCode,
                        s.getRoomNo(),
                        fac
                });
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 6: Department Analytics ---
    private JPanel createAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        statsArea.setBackground(CARD_BG);
        statsArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JButton generateBtn = createActionButton("Generate Academic Workload Utilization Report", new Color(15, 23, 42), Color.WHITE, 360, 36);

        generateBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("=================================================================================\n");
            sb.append("        INSTITUTIONAL ACADEMIC WORKLOAD & TEACHING CAPACITY REPORT              \n");
            sb.append("=================================================================================\n\n");

            int ftCount = 0;
            int ptCount = 0;
            int totalCapacity = 0;
            int totalAllocatedHours = 0;

            for (Teacher t : assignmentSystem.getTeacherList()) {
                if (t instanceof FullTimeTeacher) ftCount++;
                else ptCount++;

                totalCapacity += t.getMaxHours();
                totalAllocatedHours += t.getCurrentHours();

                sb.append(String.format(" • [%s] %-25s | Dept: %-6s | Role: %-15s | Workload: %2d/%2d hrs | %s\n",
                        t.getId(), t.getName(), t.getDepartment(),
                        (t instanceof FullTimeTeacher ? "Full-Time" : "Part-Time"),
                        t.getCurrentHours(), t.getMaxHours(), t.getTeachingStatus()));
            }

            sb.append("\n---------------------------------------------------------------------------------\n");
            sb.append(String.format(" Total Registered Faculty: %d (Full-Time: %d, Part-Time: %d)\n",
                    assignmentSystem.getTeacherList().size(), ftCount, ptCount));
            sb.append(String.format(" Total Allocated Teaching Hours: %d hours\n", totalAllocatedHours));
            sb.append(String.format(" Total Approved Workload Capacity: %d hours\n", totalCapacity));
            sb.append(String.format(" Overall Institution Workload Utilization: %.1f%%\n",
                    (totalCapacity > 0 ? (totalAllocatedHours * 100.0 / totalCapacity) : 0)));
            sb.append("=================================================================================\n");
            statsArea.setText(sb.toString());
        });

        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLogConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                " Live Audit & Conflict Console ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(71, 85, 105)
        ));

        logArea = new JTextArea(4, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(DARK_HEADER);
        logArea.setForeground(new Color(226, 232, 240));

        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(30, 41, 59));
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(new Color(30, 41, 59));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    // --- Business Actions ---

    private void handleAssignAction() {
        TeacherItem tItem = (TeacherItem) teacherCombo.getSelectedItem();
        SectionItem sItem = (SectionItem) sectionCombo.getSelectedItem();

        if (tItem == null || sItem == null) {
            JOptionPane.showMessageDialog(this, "Select both a Teacher and a Section to perform assignment.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Teacher teacher = tItem.teacher();
        Section section = sItem.section();

        try {
            boolean success = assignmentSystem.assignCourseToTeacher(teacher, section);
            if (success) {
                DatabaseManager.syncAssignmentToDatabase(section);
                String msg = String.format("Assigned %s to %s (%s) @ %s",
                        teacher.getName(),
                        section.getCourse() != null ? section.getCourse().getCourseCode() : "Course",
                        section.getSectionId(),
                        section.getTimeSlot());
                logMessage("SUCCESS: " + msg);
                JOptionPane.showMessageDialog(this, msg, "Assignment Confirmed", JOptionPane.INFORMATION_MESSAGE);
                refreshAllViews();
            }
        } catch (AssignmentException ex) {
            logMessage("REJECTED: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Assignment Conflict / Overload", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUnassignAction() {
        SectionItem sItem = (SectionItem) sectionCombo.getSelectedItem();
        if (sItem == null || sItem.section() == null) {
            JOptionPane.showMessageDialog(this, "Select a Section to unassign.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Section section = sItem.section();
        if (section.getTeacher() == null) {
            JOptionPane.showMessageDialog(this, "Section " + section.getSectionId() + " has no assigned teacher.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String teacherName = section.getTeacher().getName();
        boolean ok = assignmentSystem.unassignSection(section);
        if (ok) {
            DatabaseManager.syncAssignmentToDatabase(section);
            String msg = "Unassigned " + teacherName + " from section " + section.getSectionId();
            logMessage("ACTION: " + msg);
            JOptionPane.showMessageDialog(this, msg, "Unassigned", JOptionPane.INFORMATION_MESSAGE);
            refreshAllViews();
        }
    }

    private void handleAutoAssignOptimizer() {
        int assignedCount = 0;
        List<Section> unassignedSections = assignmentSystem.getSectionList().stream()
                .filter(s -> s.getTeacher() == null)
                .toList();

        if (unassignedSections.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All sections are already assigned!", "Auto-Assign", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Section sec : unassignedSections) {
            for (Teacher t : assignmentSystem.getTeacherList()) {
                int requiredHrs = sec.getCourse() != null ? sec.getCourse().getWorkload() : 0;
                if (t.canTeach(requiredHrs) && !assignmentSystem.checkConflict(t, sec)) {
                    try {
                        assignmentSystem.assignCourseToTeacher(t, sec);
                        DatabaseManager.syncAssignmentToDatabase(sec);
                        assignedCount++;
                        logMessage("AUTO-ASSIGN: Allocated " + t.getName() + " -> " + sec.getSectionId() + " (" + sec.getTimeSlot() + ")");
                        break;
                    } catch (AssignmentException ignored) {
                    }
                }
            }
        }

        refreshAllViews();
        String summary = "Auto-Assignment complete. " + assignedCount + " sections successfully assigned without conflicts!";
        logMessage("OPTIMIZER: " + summary);
        JOptionPane.showMessageDialog(this, summary, "Optimizer Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleClearAllAssignments() {
        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all section assignments?", "Clear All", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            for (Section s : assignmentSystem.getSectionList()) {
                assignmentSystem.unassignSection(s);
                DatabaseManager.syncAssignmentToDatabase(s);
            }
            refreshAllViews();
            logMessage("ACTION: All section assignments cleared.");
        }
    }

    // --- Dynamic Creation Dialogs ---

    private void showAddFacultyDialog(boolean isFullTime) {
        String roleStr = isFullTime ? "Full-Time Faculty" : "Part-Time Faculty (Adjunct)";
        int nextIdNum = assignmentSystem.getTeacherList().size() + 1;
        JTextField idField = new JTextField((isFullTime ? "FT0" : "PT0") + nextIdNum);
        JTextField nameField = new JTextField(isFullTime ? "Dr. " : "Prof. ");
        JTextField emailField = new JTextField("@univ.ac.in");
        
        JComboBox<Department> deptBox = new JComboBox<>();
        for (Department d : departments) deptBox.addItem(d);

        JTextField maxHrsField = new JTextField(isFullTime ? "8" : "6");

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Faculty ID:"));
        form.add(idField);
        form.add(new JLabel("Full Name (Indian Format):"));
        form.add(nameField);
        form.add(new JLabel("Institutional Email:"));
        form.add(emailField);
        form.add(new JLabel("Department:"));
        form.add(deptBox);
        form.add(new JLabel("Max Workload (Hours):"));
        form.add(maxHrsField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New " + roleStr, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                Department selDept = (Department) deptBox.getSelectedItem();
                String dept = selDept != null ? selDept.getDeptId() : "CSE";
                int maxHrs = Integer.parseInt(maxHrsField.getText().trim());

                if (id.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "ID and Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Teacher t = isFullTime
                        ? new FullTimeTeacher(id, name, email, dept, maxHrs)
                        : new PartTimeTeacher(id, name, email, dept, maxHrs);

                assignmentSystem.registerTeacher(t);
                if (selDept != null) {
                    selDept.addTeacher(t);
                }

                DatabaseManager.addTeacherToDB(t);
                refreshAllViews();
                logMessage("DATABASE & SYSTEM: Added faculty member " + name + " (" + t.getRole() + ")");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddCourseDialog() {
        JTextField codeField = new JTextField("CS");
        JTextField nameField = new JTextField();
        JTextField creditsField = new JTextField("3");

        JComboBox<Department> deptBox = new JComboBox<>();
        for (Department d : departments) deptBox.addItem(d);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Course Code:"));
        form.add(codeField);
        form.add(new JLabel("Subject / Course Title:"));
        form.add(nameField);
        form.add(new JLabel("Credits (Workload Hours):"));
        form.add(creditsField);
        form.add(new JLabel("Department:"));
        form.add(deptBox);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Subject / Course", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String code = codeField.getText().trim().toUpperCase();
                String name = nameField.getText().trim();
                int credits = Integer.parseInt(creditsField.getText().trim());

                if (code.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Course code and title cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Course c = new StandardCourse(code, name, credits);
                courses.add(c);

                Department selDept = (Department) deptBox.getSelectedItem();
                String deptId = selDept != null ? selDept.getDeptId() : null;
                if (selDept != null) {
                    selDept.addCourse(c);
                }

                DatabaseManager.addCourseToDB(c, deptId);
                refreshAllViews();
                logMessage("DATABASE & SYSTEM: Registered Subject " + c.getCourseInfo());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid inputs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddSectionDialog() {
        if (courses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No courses available. Please create a course first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<Course> courseBox = new JComboBox<>();
        for (Course c : courses) courseBox.addItem(c);

        int nextSecNum = assignmentSystem.getSectionList().size() + 1;
        JTextField secIdField = new JTextField("SEC-" + (char) ('A' + (nextSecNum % 26)));
        JTextField roomField = new JTextField("Aryabhata Hall 101");
        
        JComboBox<String> slotBox = new JComboBox<>(new String[]{
                "Mon 09:00 - 11:00",
                "Mon 11:30 - 13:30",
                "Mon 14:00 - 16:00",
                "Tue 10:00 - 12:00",
                "Tue 14:00 - 16:00",
                "Wed 10:00 - 12:00",
                "Wed 13:00 - 15:00",
                "Thu 10:00 - 12:00",
                "Thu 14:00 - 16:00",
                "Fri 09:00 - 11:00",
                "Fri 11:30 - 13:30"
        });

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Parent Subject:"));
        form.add(courseBox);
        form.add(new JLabel("Section ID:"));
        form.add(secIdField);
        form.add(new JLabel("Classroom / Hall:"));
        form.add(roomField);
        form.add(new JLabel("Time Slot:"));
        form.add(slotBox);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Section (Composition)", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Course parent = (Course) courseBox.getSelectedItem();
            String secId = secIdField.getText().trim().toUpperCase();
            String room = roomField.getText().trim();
            String slot = (String) slotBox.getSelectedItem();

            if (parent != null && !secId.isEmpty()) {
                Section sec = parent.createSection(secId, room, slot);
                assignmentSystem.registerSection(sec);
                DatabaseManager.addSectionToDB(sec);
                refreshAllViews();
                logMessage("DATABASE & SYSTEM: Created section " + sec.getSchedule());
            }
        }
    }

    private void showAddDepartmentDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.add(new JLabel("Department ID:"));
        form.add(idField);
        form.add(new JLabel("Department Name:"));
        form.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add Department (Aggregation)", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim().toUpperCase();
            String name = nameField.getText().trim();
            if (!id.isEmpty() && !name.isEmpty()) {
                Department dept = new Department(id, name);
                departments.add(dept);
                DatabaseManager.addDepartmentToDB(dept);
                refreshAllViews();
                logMessage("DATABASE & SYSTEM: Created department " + dept.getDepartmentSummary());
            }
        }
    }

    // --- Persistence & Export Handlers ---

    private void handleSaveSnapshot() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("fcas_system_snapshot.fcas"));
        chooser.setFileFilter(new FileNameExtensionFilter("FCAS Snapshot (*.fcas)", "fcas"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DataManager.SystemSnapshot snapshot = new DataManager.SystemSnapshot();
                snapshot.departments = new ArrayList<>(this.departments);
                snapshot.courses = new ArrayList<>(this.courses);
                snapshot.teachers = new ArrayList<>(this.assignmentSystem.getTeacherList());
                snapshot.sections = new ArrayList<>(this.assignmentSystem.getSectionList());

                DataManager.saveToFile(chooser.getSelectedFile(), snapshot);
                logMessage("BACKUP: System state saved to " + chooser.getSelectedFile().getName());
                JOptionPane.showMessageDialog(this, "System snapshot successfully saved!", "Save Snapshot", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLoadSnapshot() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("FCAS Snapshot (*.fcas)", "fcas"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DataManager.SystemSnapshot snapshot = DataManager.loadFromFile(chooser.getSelectedFile());
                this.departments = snapshot.departments;
                this.courses = snapshot.courses;

                this.assignmentSystem.clearAll();
                for (Teacher t : snapshot.teachers) this.assignmentSystem.registerTeacher(t);
                for (Section s : snapshot.sections) this.assignmentSystem.registerSection(s);

                refreshAllViews();
                logMessage("BACKUP: Restored snapshot from " + chooser.getSelectedFile().getName());
                JOptionPane.showMessageDialog(this, "Snapshot loaded successfully!", "Load Snapshot", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading snapshot: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportScheduleCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("university_schedule_timetable.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Comma Separated (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DataManager.exportScheduleToCSV(chooser.getSelectedFile(), assignmentSystem.getSectionList());
                logMessage("EXPORT: Timetable exported to " + chooser.getSelectedFile().getName());
                JOptionPane.showMessageDialog(this, "Timetable exported successfully to CSV!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportWorkloadCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("faculty_workload_report.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Comma Separated (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                DataManager.exportFacultyWorkloadReportToCSV(chooser.getSelectedFile(), assignmentSystem.getTeacherList());
                logMessage("EXPORT: Faculty workload report exported to " + chooser.getSelectedFile().getName());
                JOptionPane.showMessageDialog(this, "Workload report exported successfully to CSV!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAboutDialog() {
        String msg = """
                Faculty-Course Assignment System (FCAS)
                Enterprise Academic Architecture (Pure Java & Java Swing)
                
                • Zero-Setup Local Database: Embedded SQLite (fcas_database.db).
                • Pure Academic Workload: Focused strictly on Course Schedules & Load Distribution.
                • Abstraction: Person & Course base classes; WorkloadRule interface contract.
                • Encapsulation: Strict private fields, accessors, and business constraints.
                • Inheritance: FullTimeTeacher & PartTimeTeacher extending Teacher -> Person.
                • Polymorphism: getTeachingStatus() and getRole() overridden dynamically.
                • Composition: Section instances owned by parent Course.
                • Aggregation: Departments managing independent Teacher and Course references.
                • Controller & Association: AssignmentSystem with double-booking prevention.
                • Optimizer: Smart Auto-Assign engine with workload & slot conflict checking.
                """;
        JOptionPane.showMessageDialog(this, msg, "About FCAS Architecture", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logMessage(String message) {
        String time = LocalDateTime.now().format(timeFmt);
        logArea.append("[" + time + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // --- UI View Synchronization ---

    private void refreshAllViews() {
        refreshFacultyTable();
        refreshSectionsTable();
        refreshCoursesTable();
        refreshDepartmentTable();
        refreshComboBoxes();
        updateKpiMetrics();
    }

    private void refreshFacultyTable() {
        teacherTableModel.setRowCount(0);
        for (Teacher t : assignmentSystem.getTeacherList()) {
            teacherTableModel.addRow(new Object[]{
                    t.getId(),
                    t.getName(),
                    t.getRole(),
                    t.getDepartment(),
                    t.getCurrentHours() + " / " + t.getMaxHours() + " hrs",
                    t.getTeachingStatus()
            });
        }
    }

    private void refreshSectionsTable() {
        sectionTableModel.setRowCount(0);
        for (Section s : assignmentSystem.getSectionList()) {
            String cCode = s.getCourse() != null ? s.getCourse().getCourseCode() : "N/A";
            int credits = s.getCourse() != null ? s.getCourse().getCredits() : 0;
            String fac = s.getTeacher() != null ? s.getTeacher().getName() : "(Unassigned)";
            sectionTableModel.addRow(new Object[]{
                    s.getSectionId(),
                    cCode,
                    credits + " hrs",
                    s.getRoomNo(),
                    s.getTimeSlot(),
                    fac
            });
        }
    }

    private void refreshCoursesTable() {
        courseTableModel.setRowCount(0);
        for (Course c : courses) {
            String deptName = "Independent";
            for (Department d : departments) {
                if (d.getCourses().contains(c)) {
                    deptName = d.getDeptName();
                    break;
                }
            }
            courseTableModel.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCredits() + " hrs",
                    c.getSections().size(),
                    deptName
            });
        }
    }

    private void refreshDepartmentTable() {
        deptTableModel.setRowCount(0);
        for (Department d : departments) {
            deptTableModel.addRow(new Object[]{
                    d.getDeptId(),
                    d.getDeptName(),
                    d.getTeachers().size(),
                    d.getCourses().size()
            });
        }
    }

    private void refreshComboBoxes() {
        teacherCombo.removeAllItems();
        for (Teacher t : assignmentSystem.getTeacherList()) {
            teacherCombo.addItem(new TeacherItem(t));
        }

        sectionCombo.removeAllItems();
        for (Section s : assignmentSystem.getSectionList()) {
            sectionCombo.addItem(new SectionItem(s));
        }
    }

    private void updateKpiMetrics() {
        int totalTeachers = assignmentSystem.getTeacherList().size();
        int totalSecs = assignmentSystem.getSectionList().size();
        long assignedCount = assignmentSystem.getSectionList().stream()
                .filter(s -> s.getTeacher() != null).count();

        int totalCapacity = assignmentSystem.getTeacherList().stream().mapToInt(Teacher::getMaxHours).sum();
        int totalAllocated = assignmentSystem.getTeacherList().stream().mapToInt(Teacher::getCurrentHours).sum();
        double pct = totalCapacity > 0 ? (totalAllocated * 100.0 / totalCapacity) : 0.0;

        totalFacultyLabel.setText(String.valueOf(totalTeachers));
        totalSectionsLabel.setText(String.valueOf(totalSecs));
        assignedSectionsLabel.setText(assignedCount + " / " + totalSecs);
        capacityUtilLabel.setText(String.format("%.1f%% (%dh / %dh)", pct, totalAllocated, totalCapacity));
    }

    @FunctionalInterface
    interface SimpleDocumentRunnable {
        void run();
    }

    static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final SimpleDocumentRunnable runnable;

        SimpleDocumentListener(SimpleDocumentRunnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            runnable.run();
        }
    }

    public static void main(String[] args) {
        // Modern FlatLaf theme with graceful fallback
        SwingUtilities.invokeLater(() -> {
            try {
                FlatLightLaf.setup();
                UIManager.put("Button.arc", 8);
                UIManager.put("Component.arc", 8);
                UIManager.put("ProgressBar.arc", 8);
                UIManager.put("TextComponent.arc", 8);
            } catch (Throwable t) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }
            }
            FCASAppGUI gui = new FCASAppGUI();
            gui.setVisible(true);
        });
    }
}
