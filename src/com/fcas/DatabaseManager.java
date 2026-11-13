package com.fcas;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Embedded Zero-Setup SQLite Database Manager.
 * Focuses purely on Academic Load & Assignments (No payment or monetary fields).
 */
public class DatabaseManager {

    private static final String DB_FILE = "fcas_database.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    public static boolean isDatabaseDriverAvailable() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Departments table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS departments (
                    dept_id TEXT PRIMARY KEY,
                    dept_name TEXT NOT NULL
                );
            """);

            // 2. Teachers table (Purely academic, no salary)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS teachers (
                    teacher_id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    department TEXT NOT NULL,
                    max_hours INTEGER NOT NULL,
                    role_type TEXT NOT NULL
                );
            """);

            // 3. Courses table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    course_code TEXT PRIMARY KEY,
                    course_name TEXT NOT NULL,
                    credits INTEGER NOT NULL,
                    dept_id TEXT,
                    FOREIGN KEY(dept_id) REFERENCES departments(dept_id)
                );
            """);

            // 4. Sections table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sections (
                    section_id TEXT PRIMARY KEY,
                    course_code TEXT NOT NULL,
                    room_no TEXT NOT NULL,
                    time_slot TEXT NOT NULL,
                    assigned_teacher_id TEXT,
                    FOREIGN KEY(course_code) REFERENCES courses(course_code),
                    FOREIGN KEY(assigned_teacher_id) REFERENCES teachers(teacher_id)
                );
            """);
        }
    }

    public static boolean isDatabaseEmpty() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM teachers")) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException ignored) {
        }
        return true;
    }

    public static void seedIndianFacultyData(AssignmentSystem system, List<Department> departments, List<Course> courses) {
        try {
            initializeDatabase();
            if (isDatabaseEmpty()) {
                seedDefaultDataToDatabase();
            }
            loadFromDatabase(system, departments, courses);
        } catch (SQLException e) {
            System.err.println("[Database] SQLite error: " + e.getMessage() + ". Falling back to in-memory seed.");
            seedInMemoryFallback(system, departments, courses);
        }
    }

    private static void seedDefaultDataToDatabase() throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // Departments
            try (PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO departments VALUES (?, ?)")) {
                ps.setString(1, "CSE-DEPT");
                ps.setString(2, "Computer Science & Engineering");
                ps.executeUpdate();

                ps.setString(1, "ECE-DEPT");
                ps.setString(2, "Electronics & Communication Engineering");
                ps.executeUpdate();

                ps.setString(1, "IT-DEPT");
                ps.setString(2, "Information Technology & AI");
                ps.executeUpdate();

                ps.setString(1, "MATH-DEPT");
                ps.setString(2, "Department of Mathematics & Computing");
                ps.executeUpdate();
            }

            // 10 Renowned Indian Faculty Members (Pure academic info)
            try (PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO teachers VALUES (?, ?, ?, ?, ?, ?)")) {
                Object[][] facultyData = {
                    {"FT01", "Dr. Rajesh Sharma", "rajesh.sharma@univ.ac.in", "CSE", 8, "FULL_TIME"},
                    {"PT01", "Prof. Priya Iyer", "priya.iyer@univ.ac.in", "CSE", 6, "PART_TIME"},
                    {"FT02", "Dr. Vikram Malhotra", "vikram.malhotra@univ.ac.in", "ECE", 10, "FULL_TIME"},
                    {"PT02", "Prof. Ananya Deshmukh", "ananya.deshmukh@univ.ac.in", "MATH", 6, "PART_TIME"},
                    {"FT03", "Dr. Suresh Nambiar", "suresh.nambiar@univ.ac.in", "CSE", 8, "FULL_TIME"},
                    {"FT04", "Dr. Kavita Radhakrishnan", "kavita.r@univ.ac.in", "IT", 8, "FULL_TIME"},
                    {"PT03", "Prof. Arjun Sengupta", "arjun.s@univ.ac.in", "ECE", 6, "PART_TIME"},
                    {"FT05", "Dr. Neha Mukherjee", "neha.m@univ.ac.in", "IT", 8, "FULL_TIME"},
                    {"PT04", "Prof. Manish Verma", "manish.verma@univ.ac.in", "MATH", 4, "PART_TIME"},
                    {"FT06", "Dr. Arvind Swaminathan", "arvind.s@univ.ac.in", "CSE", 10, "FULL_TIME"}
                };

                for (Object[] row : facultyData) {
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.setString(4, (String) row[3]);
                    ps.setInt(5, (Integer) row[4]);
                    ps.setString(6, (String) row[5]);
                    ps.executeUpdate();
                }
            }

            // 8 University Courses
            try (PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO courses VALUES (?, ?, ?, ?)")) {
                Object[][] courseData = {
                    {"CS201", "Data Structures & Algorithms", 4, "CSE-DEPT"},
                    {"CS301", "Database Management Systems", 3, "CSE-DEPT"},
                    {"CS405", "Operating Systems & Concurrency", 3, "CSE-DEPT"},
                    {"IT302", "Machine Learning & Deep Neural Nets", 4, "IT-DEPT"},
                    {"IT401", "Cloud Computing & DevOps", 3, "IT-DEPT"},
                    {"EC101", "Digital Signal Processing", 4, "ECE-DEPT"},
                    {"EC205", "VLSI System Design", 3, "ECE-DEPT"},
                    {"MA202", "Discrete Mathematical Structures", 3, "MATH-DEPT"}
                };

                for (Object[] row : courseData) {
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setInt(3, (Integer) row[2]);
                    ps.setString(4, (String) row[3]);
                    ps.executeUpdate();
                }
            }

            // Sections
            try (PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO sections VALUES (?, ?, ?, ?, ?)")) {
                Object[][] sectionData = {
                    {"SEC-A", "CS201", "Aryabhata Hall 101", "Mon 09:00 - 11:00", "FT01"},
                    {"SEC-B", "CS201", "Ramanujan Hall 202", "Mon 09:00 - 11:00", null},
                    {"SEC-C", "CS301", "Turing Lab 301", "Tue 10:00 - 12:00", "FT03"},
                    {"SEC-D", "CS405", "Kalam Auditorium", "Wed 13:00 - 15:00", null},
                    {"SEC-E", "IT302", "Ada Lovelace AI Lab", "Thu 10:00 - 12:00", "FT04"},
                    {"SEC-F", "IT401", "Vikram Sarabhai Hall", "Thu 14:00 - 16:00", null},
                    {"SEC-G", "EC101", "Homi Bhabha Lab 104", "Mon 14:00 - 16:00", "FT02"},
                    {"SEC-H", "EC205", "JC Bose Embedded Lab", "Wed 10:00 - 12:00", null},
                    {"SEC-I", "MA202", "Visvesvaraya Hall 401", "Fri 09:00 - 11:00", null}
                };

                for (Object[] row : sectionData) {
                    ps.setString(1, (String) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setString(3, (String) row[2]);
                    ps.setString(4, (String) row[3]);
                    ps.setString(5, (String) row[4]);
                    ps.executeUpdate();
                }
            }

            conn.commit();
        }
    }

    public static void loadFromDatabase(AssignmentSystem system, List<Department> departments, List<Course> courses) throws SQLException {
        system.clearAll();
        departments.clear();
        courses.clear();

        try (Connection conn = getConnection()) {
            // Load Departments
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM departments")) {
                while (rs.next()) {
                    departments.add(new Department(rs.getString("dept_id"), rs.getString("dept_name")));
                }
            }

            // Load Teachers
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM teachers")) {
                while (rs.next()) {
                    String id = rs.getString("teacher_id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    String dept = rs.getString("department");
                    int maxHrs = rs.getInt("max_hours");
                    String roleType = rs.getString("role_type");

                    Teacher t = "PART_TIME".equalsIgnoreCase(roleType)
                            ? new PartTimeTeacher(id, name, email, dept, maxHrs)
                            : new FullTimeTeacher(id, name, email, dept, maxHrs);

                    system.registerTeacher(t);
                    for (Department d : departments) {
                        if (d.getDeptId().equalsIgnoreCase(dept) || d.getDeptName().toLowerCase().contains(dept.toLowerCase())) {
                            d.addTeacher(t);
                        }
                    }
                }
            }

            // Load Courses
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM courses")) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    int credits = rs.getInt("credits");
                    String deptId = rs.getString("dept_id");

                    Course c = new StandardCourse(code, name, credits);
                    courses.add(c);

                    for (Department d : departments) {
                        if (d.getDeptId().equalsIgnoreCase(deptId)) {
                            d.addCourse(c);
                        }
                    }
                }
            }

            // Load Sections
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM sections")) {
                while (rs.next()) {
                    String secId = rs.getString("section_id");
                    String courseCode = rs.getString("course_code");
                    String roomNo = rs.getString("room_no");
                    String timeSlot = rs.getString("time_slot");
                    String assignedTeacherId = rs.getString("assigned_teacher_id");

                    Course parent = courses.stream()
                            .filter(c -> c.getCourseCode().equalsIgnoreCase(courseCode))
                            .findFirst().orElse(null);

                    if (parent != null) {
                        Section sec = parent.createSection(secId, roomNo, timeSlot);
                        system.registerSection(sec);

                        if (assignedTeacherId != null && !assignedTeacherId.trim().isEmpty()) {
                            Teacher assignedTeacher = system.getTeacherList().stream()
                                    .filter(t -> t.getId().equalsIgnoreCase(assignedTeacherId))
                                    .findFirst().orElse(null);
                            if (assignedTeacher != null) {
                                try {
                                    system.assignCourseToTeacher(assignedTeacher, sec);
                                } catch (AssignmentException ignored) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void syncAssignmentToDatabase(Section section) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE sections SET assigned_teacher_id = ? WHERE section_id = ?")) {
            if (section.getTeacher() != null) {
                ps.setString(1, section.getTeacher().getId());
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setString(2, section.getSectionId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Sync error: " + e.getMessage());
        }
    }

    public static void addTeacherToDB(Teacher t) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO teachers VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, t.getId());
            ps.setString(2, t.getName());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getDepartment());
            ps.setInt(5, t.getMaxHours());
            ps.setString(6, t instanceof PartTimeTeacher ? "PART_TIME" : "FULL_TIME");
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Insert teacher error: " + e.getMessage());
        }
    }

    public static void deleteTeacherFromDB(String teacherId) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE sections SET assigned_teacher_id = NULL WHERE assigned_teacher_id = ?")) {
                ps.setString(1, teacherId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM teachers WHERE teacher_id = ?")) {
                ps.setString(1, teacherId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[Database] Delete teacher error: " + e.getMessage());
        }
    }

    public static void addCourseToDB(Course c, String deptId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO courses VALUES (?, ?, ?, ?)")) {
            ps.setString(1, c.getCourseCode());
            ps.setString(2, c.getCourseName());
            ps.setInt(3, c.getCredits());
            ps.setString(4, deptId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Insert course error: " + e.getMessage());
        }
    }

    public static void deleteCourseFromDB(String courseCode) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sections WHERE course_code = ?")) {
                ps.setString(1, courseCode);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE course_code = ?")) {
                ps.setString(1, courseCode);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[Database] Delete course error: " + e.getMessage());
        }
    }

    public static void addSectionToDB(Section s) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO sections VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, s.getSectionId());
            ps.setString(2, s.getCourse() != null ? s.getCourse().getCourseCode() : "");
            ps.setString(3, s.getRoomNo());
            ps.setString(4, s.getTimeSlot());
            ps.setString(5, s.getTeacher() != null ? s.getTeacher().getId() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Insert section error: " + e.getMessage());
        }
    }

    public static void deleteSectionFromDB(String sectionId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM sections WHERE section_id = ?")) {
            ps.setString(1, sectionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Delete section error: " + e.getMessage());
        }
    }

    public static void addDepartmentToDB(Department d) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO departments VALUES (?, ?)")) {
            ps.setString(1, d.getDeptId());
            ps.setString(2, d.getDeptName());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Insert department error: " + e.getMessage());
        }
    }

    public static void resetDatabase() throws SQLException {
        File f = new File(DB_FILE);
        if (f.exists()) {
            f.delete();
        }
        initializeDatabase();
        seedDefaultDataToDatabase();
    }

    private static void seedInMemoryFallback(AssignmentSystem system, List<Department> departments, List<Course> courses) {
        Department cs = new Department("CSE-DEPT", "Computer Science & Engineering");
        departments.add(cs);

        Teacher t1 = new FullTimeTeacher("FT01", "Dr. Rajesh Sharma", "rajesh.sharma@univ.ac.in", "CSE", 8);
        Teacher t2 = new PartTimeTeacher("PT01", "Prof. Priya Iyer", "priya.iyer@univ.ac.in", "CSE", 6);
        cs.addTeacher(t1);
        cs.addTeacher(t2);
        system.registerTeacher(t1);
        system.registerTeacher(t2);

        Course c1 = new StandardCourse("CS201", "Data Structures & Algorithms", 4);
        courses.add(c1);
        cs.addCourse(c1);

        Section s1 = c1.createSection("SEC-A", "Aryabhata Hall 101", "Mon 09:00 - 11:00");
        system.registerSection(s1);
        try {
            system.assignCourseToTeacher(t1, s1);
        } catch (AssignmentException ignored) {
        }
    }
}
