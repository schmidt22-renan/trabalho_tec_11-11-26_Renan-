package model;

import java.util.Objects;

public class Participant {
    private final String id;
    private final String name;

    public Participant(String id, String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome do participante não pode ser vazio.");
        }
        this.id = Objects.requireNonNull(id, "ID não pode ser nulo.");
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}