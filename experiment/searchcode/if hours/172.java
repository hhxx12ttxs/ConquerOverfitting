package students;

/**
 * Created by compik on 7/18/15.
 */
public class LazyStudent extends Student {
    private int hoursIDream;

    public LazyStudent(int age, String name, int hoursIDream) {
        super(age, name);
        this.hoursIDream = hoursIDream;
    }

    public int getHoursIDream() {
        return hoursIDream;
    }

    public void setHoursIDream(int hoursIDream) {
        this.hoursIDream = hoursIDream;
    }

    @Override
    public String toString() {
        return "LazyStudent{" + "age=" + getAge() +
                ", name=" + getName() + ", hoursIDream=" + hoursIDream + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)
                && this.hoursIDream == ((LazyStudent) obj).getHoursIDream()) {
            return true;
        }
        return false;
    }
}

