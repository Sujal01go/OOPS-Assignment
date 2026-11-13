package com.fcas;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("     FACULTY-COURSE ASSIGNMENT SYSTEM (OBJECT-ORIENTED DESIGN)    ");
        System.out.println("==================================================================");

        // 1. Departments (Aggregation)
        Department csDept = new Department("CSE-DEPT", "Computer Science & Engineering");

        // 2. Teachers (Indian Faculty - Inheritance & Polymorphism)
        Teacher profSharma = new FullTimeTeacher("FT01", "Dr. Rajesh Sharma", "rajesh.sharma@univ.ac.in", "CSE", 8, 125000.00);
        Teacher profIyer = new PartTimeTeacher("PT01", "Prof. Priya Iyer", "priya.iyer@univ.ac.in", "CSE", 6, 1800.00);

        csDept.addTeacher(profSharma);
        csDept.addTeacher(profIyer);

        // 3. Courses & Sections (Composition)
        Course csCourse = new StandardCourse("CS201", "Data Structures & Algorithms", 4);
        Course dbCourse = new StandardCourse("CS301", "Database Systems", 3);

        csDept.addCourse(csCourse);
        csDept.addCourse(dbCourse);

        Section secA = csCourse.createSection("SEC-A", "Aryabhata Hall 101", "Mon 09:00 - 11:00");
        Section secB = csCourse.createSection("SEC-B", "Ramanujan Hall 202", "Mon 09:00 - 11:00"); // Same timeslot for conflict test!
        Section secC = dbCourse.createSection("SEC-C", "Turing Lab 301", "Tue 10:00 - 12:00");

        // 4. Controller: AssignmentSystem
        AssignmentSystem controller = new AssignmentSystem();
        controller.registerTeacher(profSharma);
        controller.registerTeacher(profIyer);
        controller.registerSection(secA);
        controller.registerSection(secB);
        controller.registerSection(secC);

        System.out.println("\n--- 1. DEMONSTRATING CORE ASSIGNMENTS ---");
        tryAssign(controller, profSharma, secA);

        // Test 2: Conflict detection (Prof. Sharma already has Sec A at Mon 09:00 - 11:00)
        System.out.println("\n--- 2. DEMONSTRATING CONFLICT DETECTION ---");
        tryAssign(controller, profSharma, secB);

        // Test 3: Assign Sec B to Part-time teacher Priya Iyer instead
        tryAssign(controller, profIyer, secB);

        // Test 4: Assign Sec C to Priya (Tests workload limit: 4 + 3 = 7 hrs > 6 hrs max)
        System.out.println("\n--- 3. DEMONSTRATING WORKLOAD CAPACITY ENFORCEMENT ---");
        tryAssign(controller, profIyer, secC);

        // Assign Sec C to Dr. Sharma instead (Sharma max: 8 hrs, current: 4 hrs, needs: 3 hrs -> 7 <= 8)
        tryAssign(controller, profSharma, secC);

        // 5. Demonstrating Polymorphism (calculateSalary & getRole)
        System.out.println("\n--- 4. DEMONSTRATING POLYMORPHISM & ENCAPSULATION ---");
        System.out.println(profSharma.getRole() + ": " + profSharma.getDetails());
        System.out.println(profIyer.getRole() + ": " + profIyer.getDetails());

        // 6. Section Schedules
        System.out.println("\n--- 5. FINAL TIMETABLE / SECTION SCHEDULE ---");
        for (Section s : controller.getSectionList()) {
            System.out.println(" • " + s.getSchedule());
        }

        System.out.println("\n==================================================================");
        System.out.println("               SYSTEM DEMONSTRATION COMPLETE                      ");
        System.out.println("==================================================================");
    }

    private static void tryAssign(AssignmentSystem controller, Teacher t, Section s) {
        try {
            boolean success = controller.assignCourseToTeacher(t, s);
            if (success) {
                System.out.println("[Success] Assigned " + t.getName() + " to " +
                        (s.getCourse() != null ? s.getCourse().getCourseCode() : "Course") +
                        " (" + s.getSectionId() + ") at " + s.getTimeSlot());
            }
        } catch (AssignmentException ex) {
            System.out.println("[Rejected] " + ex.getMessage());
        }
    }
}
