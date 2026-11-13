package com.fcas;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Section implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String sectionId;
    private String roomNo;
    private String timeSlot;
    private Course course;   // Owner course (Composition)
    private Teacher teacher; // Assigned teacher (Association)

    // Package-private / protected constructor so sections are created via Course
    protected Section(String sectionId, String roomNo, String timeSlot, Course course) {
        this.sectionId = sectionId != null ? sectionId.trim().toUpperCase() : "";
        this.roomNo = roomNo != null ? roomNo.trim() : "";
        this.timeSlot = timeSlot != null ? timeSlot.trim() : "";
        this.course = course;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId != null ? sectionId.trim().toUpperCase() : "";
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo != null ? roomNo.trim() : "";
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot != null ? timeSlot.trim() : "";
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void assignTeacher(Teacher t) {
        this.teacher = t;
    }

    public String getSchedule() {
        String teacherName = (teacher != null) ? teacher.getName() : "Unassigned";
        String courseCode = (course != null) ? course.getCourseCode() : "No Course";
        return String.format("Section: %s | Course: %s | Room: %s | TimeSlot: %s | Faculty: %s",
                sectionId, courseCode, roomNo, timeSlot, teacherName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return Objects.equals(sectionId, section.sectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectionId);
    }

    @Override
    public String toString() {
        return getSchedule();
    }
}
