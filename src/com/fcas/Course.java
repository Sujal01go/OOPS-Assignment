package com.fcas;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class Course implements WorkloadRule, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    protected String courseCode;
    protected String courseName;
    protected int credits;
    protected List<Section> sections;

    public Course(String courseCode, String courseName, int credits) {
        this.courseCode = courseCode != null ? courseCode.trim().toUpperCase() : "";
        this.courseName = courseName != null ? courseName.trim() : "";
        this.credits = credits;
        this.sections = new ArrayList<>();
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode != null ? courseCode.trim().toUpperCase() : "";
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName != null ? courseName.trim() : "";
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    /**
     * Composition: Sections are created and owned exclusively by Course.
     */
    public Section createSection(String sectionId, String roomNo, String timeSlot) {
        Section section = new Section(sectionId, roomNo, timeSlot, this);
        this.sections.add(section);
        return section;
    }

    public boolean removeSection(Section s) {
        if (s != null && sections.contains(s)) {
            if (s.getTeacher() != null) {
                s.getTeacher().unassignCourse(this);
                s.assignTeacher(null);
            }
            return sections.remove(s);
        }
        return false;
    }

    @Override
    public int getWorkload() {
        return credits;
    }

    @Override
    public boolean checkSchedule() {
        for (int i = 0; i < sections.size(); i++) {
            for (int j = i + 1; j < sections.size(); j++) {
                if (sections.get(i).getTimeSlot().equalsIgnoreCase(sections.get(j).getTimeSlot())) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getCourseInfo() {
        return "[" + courseCode + "] " + courseName + " (" + credits + " Credits, " + sections.size() + " sections)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(courseCode, course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }

    @Override
    public String toString() {
        return getCourseInfo();
    }
}
