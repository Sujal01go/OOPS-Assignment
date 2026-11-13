package com.fcas;

import java.io.Serial;

public class PartTimeTeacher extends Teacher {
    @Serial
    private static final long serialVersionUID = 1L;

    public PartTimeTeacher(String id, String name, String email, String department, int maxHours) {
        super(id, name, email, department, maxHours);
    }

    // Overloaded constructor for backwards compatibility
    public PartTimeTeacher(String id, String name, String email, String department, int maxHours, double rateIgnored) {
        super(id, name, email, department, maxHours);
    }

    @Override
    public String getTeachingStatus() {
        int remaining = getMaxHours() - getCurrentHours();
        if (remaining <= 0) {
            return "Visiting/Adjunct (Capacity Reached)";
        }
        return "Visiting/Adjunct (" + remaining + "h Available)";
    }

    @Override
    public String getRole() {
        return "Part-Time Faculty (Adjunct)";
    }
}
