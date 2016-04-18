class Duration {
    private int hours, mins;

    Duration() {
        //default constructor
    }

    Duration(int hours, int mins) {
        this.hours = hours;
        this.mins = mins;
    }

    void get() {
        hours = Console.readInt();
        mins = Console.readInt();
    }

    void put() {
        System.out.println(hours + " hours " + mins + " minutes");
    }

    boolean isLonger(Duration d2) {
        return(hours >= d2.hours && mins >= d2.mins);
    }

    Duration add(Duration d2) {
        Duration result = new Duration();
        result.hours = hours + d2.hours;
        result.mins = mins + d2.mins;
        if((result.mins % 60) != 0) {
            result.hours = result.hours+(result.mins / 60);
            result.mins =  result.mins%60;
        }
        return result;
    }
}

class Part7 {
    public static void main(String[] args) {
        Duration d1 = new Duration();
        Duration d2 = new Duration();
        d1.get();
        d2.get();
        d1.add(d2).put();
    }
}
