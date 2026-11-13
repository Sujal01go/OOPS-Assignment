package com.fcas;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssignmentSystem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<Teacher> teacherList;
    private List<Section> sectionList;

    public AssignmentSystem() {
        this.teacherList = new ArrayList<>();
        this.sectionList = new ArrayList<>();
    }

    public void registerTeacher(Teacher t) {
        if (t != null && !teacherList.contains(t)) {
            teacherList.add(t);
        }
    }

    public boolean removeTeacher(Teacher t) {
        if (t != null) {
            // Unassign from any sections first
            for (Section s : sectionList) {
                if (t.equals(s.getTeacher())) {
                    s.assignTeacher(null);
                }
            }
            return teacherList.remove(t);
        }
        return false;
    }

    public void registerSection(Section s) {
        if (s != null && !sectionList.contains(s)) {
            sectionList.add(s);
        }
    }

    public boolean removeSection(Section s) {
        if (s != null) {
            if (s.getTeacher() != null && s.getCourse() != null) {
                s.getTeacher().unassignCourse(s.getCourse());
                s.assignTeacher(null);
            }
            return sectionList.remove(s);
        }
        return false;
    }

    public List<Teacher> getTeacherList() {
        return Collections.unmodifiableList(teacherList);
    }

    public List<Section> getSectionList() {
        return Collections.unmodifiableList(sectionList);
    }

    /**
     * Checks if the teacher has any time slot clash with other sections.
     * Ignores the target section if it's already assigned to this teacher.
     */
    public boolean checkConflict(Teacher t, Section s) {
        if (t == null || s == null) return false;

        String targetSlot = s.getTimeSlot() != null ? s.getTimeSlot().trim().toLowerCase() : "";
        for (Section existing : sectionList) {
            if (existing != null && existing != s && t.equals(existing.getTeacher())) {
                String existingSlot = existing.getTimeSlot() != null ? existing.getTimeSlot().trim().toLowerCase() : "";
                if (!targetSlot.isEmpty() && targetSlot.equals(existingSlot)) {
                    return true; // Conflict found!
                }
            }
        }
        return false;
    }

    /**
     * Assigns course section to teacher considering capacity and schedule conflicts.
     * Throws AssignmentException on violation.
     */
    public boolean assignCourseToTeacher(Teacher t, Section s) throws AssignmentException {
        if (t == null || s == null) {
            throw new AssignmentException("Teacher or Section cannot be null.");
        }

        // Already assigned to this exact section?
        if (t.equals(s.getTeacher())) {
            return true;
        }

        // 1. Conflict check
        if (checkConflict(t, s)) {
            throw new AssignmentException("Schedule conflict: " + t.getName() +
                    " is already assigned to another class during timeslot '" + s.getTimeSlot() + "'.");
        }

        // 2. Workload capacity check
        int requiredHours = s.getCourse() != null ? s.getCourse().getWorkload() : 0;
        if (!t.canTeach(requiredHours)) {
            throw new AssignmentException("Workload exceeded: " + t.getName() +
                    " cannot take " + requiredHours + " additional hours. Current: " +
                    t.getCurrentHours() + "h / Max: " + t.getMaxHours() + "h.");
        }

        // If section had a previous teacher, unassign them first
        Teacher oldTeacher = s.getTeacher();
        if (oldTeacher != null && s.getCourse() != null) {
            oldTeacher.unassignCourse(s.getCourse());
        }

        // 3. Assign
        boolean courseAssigned = t.assignCourse(s.getCourse());
        if (courseAssigned) {
            s.assignTeacher(t);
            registerTeacher(t);
            registerSection(s);
            return true;
        } else {
            throw new AssignmentException("Failed to allocate workload to " + t.getName());
        }
    }

    /**
     * Unassign a teacher from a section.
     */
    public boolean unassignSection(Section s) {
        if (s != null && s.getTeacher() != null) {
            Teacher t = s.getTeacher();
            if (s.getCourse() != null) {
                t.unassignCourse(s.getCourse());
            }
            s.assignTeacher(null);
            return true;
        }
        return false;
    }

    public void clearAll() {
        for (Teacher t : teacherList) {
            t.resetWorkload();
        }
        for (Section s : sectionList) {
            s.assignTeacher(null);
        }
        teacherList.clear();
        sectionList.clear();
    }
}
