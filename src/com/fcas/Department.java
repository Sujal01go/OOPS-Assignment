package com.fcas;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Department implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String deptId;
    private String deptName;
    private List<Teacher> teachers;
    private List<Course> courses;

    public Department(String deptId, String deptName) {
        this.deptId = deptId != null ? deptId.trim().toUpperCase() : "";
        this.deptName = deptName != null ? deptName.trim() : "";
        this.teachers = new ArrayList<>();
        this.courses = new ArrayList<>();
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId != null ? deptId.trim().toUpperCase() : "";
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName != null ? deptName.trim() : "";
    }

    public void addTeacher(Teacher t) {
        if (t != null && !teachers.contains(t)) {
            teachers.add(t);
        }
    }

    public boolean removeTeacher(Teacher t) {
        return teachers.remove(t);
    }

    public void addCourse(Course c) {
        if (c != null && !courses.contains(c)) {
            courses.add(c);
        }
    }

    public boolean removeCourse(Course c) {
        return courses.remove(c);
    }

    public List<Teacher> getTeachers() {
        return Collections.unmodifiableList(teachers);
    }

    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    public String getDepartmentSummary() {
        return String.format("Department: %s (%s) - Faculty: %d, Courses: %d",
                deptName, deptId, teachers.size(), courses.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(deptId, that.deptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deptId);
    }

    @Override
    public String toString() {
        return deptName + " (" + deptId + ")";
    }
}
