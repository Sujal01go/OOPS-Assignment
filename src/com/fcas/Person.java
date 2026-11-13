package com.fcas;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class Person implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String name;
    protected String email;

    public Person(String id, String name, String email) {
        this.id = id != null ? id.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.email = email != null ? email.trim() : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDetails() {
        return "ID: " + id + ", Name: " + name + ", Email: " + email;
    }

    public abstract String getRole();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
