package jvb;


public class Slice {
    int start, end, step;

    public Slice() {
        start = end = 0;
        step = 1;
    }

    public Slice(int end) {
        this.end = end;
        start = 0;
        step = 1;
    }

    public Slice(int start, int end) {
        this.start = start;
        this.end = end;
        step = 1;
    }

    public Slice(int start, int end, int step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Slice slice = (Slice) o;

        if (start != slice.start) return false;
        if (end != slice.end) return false;
        return step == slice.step;

    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + step;
        return result;
    }

    @Override
    public String toString() {
        return "Slice{" +
                "start=" + start +
                ", end=" + end +
                ", step=" + step +
                '}';
    }
}

