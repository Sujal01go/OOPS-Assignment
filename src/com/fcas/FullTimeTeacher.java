package com.fcas;

import java.io.Serial;

public class FullTimeTeacher extends Teacher {
    @Serial
    private static final long serialVersionUID = 1L;

    public FullTimeTeacher(String id, String name, String email, String department, int maxHours) {
        super(id, name, email, department, maxHours);
    }

    // Overloaded constructor for backwards compatibility
    public FullTimeTeacher(String id, String name, String email, String department, int maxHours, double salaryIgnored) {
        super(id, name, email, department, maxHours);
    }

    @Override
    public String getTeachingStatus() {
        if (getCurrentHours() >= getMaxHours()) {
            return "Full Load (100%)";
        }
        int pct = getMaxHours() > 0 ? (getCurrentHours() * 100 / getMaxHours()) : 0;
        return "Permanent Tenured (" + pct + "% Utilized)";
    }

    @Override
    public String getRole() {
        return "Full-Time Faculty";
    }
}
