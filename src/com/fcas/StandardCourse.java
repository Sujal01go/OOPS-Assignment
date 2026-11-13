package com.fcas;

import java.io.Serial;

public class StandardCourse extends Course {
    @Serial
    private static final long serialVersionUID = 1L;

    public StandardCourse(String courseCode, String courseName, int credits) {
        super(courseCode, courseName, credits);
    }
}
