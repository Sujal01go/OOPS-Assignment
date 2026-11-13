package com.fcas;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Teacher extends Person implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String department;
    private int maxHours;
    private int currentHours;
    private List<Course> assignedCourses;

    public Teacher(String id, String name, String email, String department, int maxHours) {
        super(id, name, email);
        this.department = department != null ? department.trim() : "";
        this.maxHours = maxHours;
        this.currentHours = 0;
        this.assignedCourses = new ArrayList<>();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department != null ? department.trim() : "";
    }

    public int getMaxHours() {
        return maxHours;
    }

    public void setMaxHours(int maxHours) {
        this.maxHours = maxHours;
    }

    public int getCurrentHours() {
        return currentHours;
    }

    public void setCurrentHours(int currentHours) {
        this.currentHours = Math.max(0, currentHours);
    }

    public List<Course> getAssignedCourses() {
        return Collections.unmodifiableList(assignedCourses);
    }

    public boolean canTeach(int hrs) {
        return (this.currentHours + hrs) <= this.maxHours;
    }

    public boolean assignCourse(Course c) {
        if (c == null) return false;
        int requiredHours = c.getWorkload();
        if (canTeach(requiredHours)) {
            if (!assignedCourses.contains(c)) {
                assignedCourses.add(c);
            }
            this.currentHours += requiredHours;
            return true;
        }
        return false;
    }

    public boolean unassignCourse(Course c) {
        if (c == null) return false;
        int requiredHours = c.getWorkload();
        this.currentHours = Math.max(0, this.currentHours - requiredHours);
        assignedCourses.remove(c);
        return true;
    }

    public void resetWorkload() {
        this.currentHours = 0;
        this.assignedCourses.clear();
    }

    /**
     * Polymorphic method from blueprint: defines faculty teaching status and workload load factor.
     */
    public abstract String getTeachingStatus();

    // Default double calculateSalary() maintained for strict blueprint compatibility
    public double calculateSalary() {
        return 0.0;
    }

    @Override
    public String getDetails() {
        return super.getDetails() + " | Dept: " + department +
                ", Load: " + currentHours + "/" + maxHours + " hrs" +
                ", Status: " + getTeachingStatus();
    }
}
