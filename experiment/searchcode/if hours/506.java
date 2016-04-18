package student;

/**
 * Created by eugene on 11.04.15.
 */
public class Subject {
    private String title;
    private int hours;

    public Subject(String title, int hours) {
        this.title = title;
        this.hours = hours;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subject subject = (Subject) o;

        if (hours != subject.hours) return false;
        if (title != null ? !title.equals(subject.title) : subject.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + hours;
        return result;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "title='" + title + '\'' +
                ", hours=" + hours +
                '}';
    }
}

