package com.fcas;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    public static class SystemSnapshot implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        public List<Department> departments = new ArrayList<>();
        public List<Course> courses = new ArrayList<>();
        public List<Teacher> teachers = new ArrayList<>();
        public List<Section> sections = new ArrayList<>();
    }

    public static void saveToFile(File file, SystemSnapshot snapshot) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(snapshot);
        }
    }

    public static SystemSnapshot loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (SystemSnapshot) ois.readObject();
        }
    }

    /**
     * Exports schedule table to CSV format.
     */
    public static void exportScheduleToCSV(File file, List<Section> sections) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println("Section ID,Course Code,Course Name,Credits,Room / Hall,Time Slot,Assigned Faculty,Faculty Email");
            for (Section s : sections) {
                String cCode = s.getCourse() != null ? s.getCourse().getCourseCode() : "";
                String cName = s.getCourse() != null ? s.getCourse().getCourseName() : "";
                int credits = s.getCourse() != null ? s.getCourse().getCredits() : 0;
                String teacherName = s.getTeacher() != null ? s.getTeacher().getName() : "Unassigned";
                String teacherEmail = s.getTeacher() != null ? s.getTeacher().getEmail() : "N/A";

                writer.printf("\"%s\",\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        escapeCsv(s.getSectionId()),
                        escapeCsv(cCode),
                        escapeCsv(cName),
                        credits,
                        escapeCsv(s.getRoomNo()),
                        escapeCsv(s.getTimeSlot()),
                        escapeCsv(teacherName),
                        escapeCsv(teacherEmail));
            }
        }
    }

    /**
     * Exports faculty workload report to CSV format.
     */
    public static void exportFacultyWorkloadReportToCSV(File file, List<Teacher> teachers) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println("Faculty ID,Name,Email,Department,Role,Current Hours,Max Hours,Status");
            for (Teacher t : teachers) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\"%n",
                        escapeCsv(t.getId()),
                        escapeCsv(t.getName()),
                        escapeCsv(t.getEmail()),
                        escapeCsv(t.getDepartment()),
                        escapeCsv(t.getRole()),
                        t.getCurrentHours(),
                        t.getMaxHours(),
                        escapeCsv(t.getTeachingStatus()));
            }
        }
    }

    private static String escapeCsv(String str) {
        if (str == null) return "";
        return str.replace("\"", "\"\"");
    }
}
