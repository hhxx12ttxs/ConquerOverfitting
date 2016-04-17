package sample.refactoring;

// Before
class Car1 {
    private double speedPerHours;
    private double hours;

    public Car1(double speedPerHours, double hours) {
        this.speedPerHours = speedPerHours;
        this.hours = hours;
    }

    public double totalDistance() throws Exception {
        double temp = hours * 30;
        if (temp >= 100.0) {
            throw new Exception("over time");
        }

        temp = speedPerHours * 1.2;
        if (temp >= 100) {
            throw new Exception("overspeed");
        }

        return speedPerHours * hours;
    }
}

// After
class Car2 {
    private double speedPerHours;
    private double hours;

    public Car2(double speedPerHours, double hours) {
        this.speedPerHours = speedPerHours;
        this.hours = hours;
    }

    public double totalDistance() throws Exception {
        double monthlyDrivingTime = hours * 30;
        if (monthlyDrivingTime >= 100.0) {
            throw new Exception("over time");
        }

        double drivingSpeed = speedPerHours * 1.2;
        if (drivingSpeed >= 100) {
            throw new Exception("overspeed");
        }

        return speedPerHours * hours;
    }
}

class SplitTemporaryVariable {
    public static void main(String[] args) {
        try {
            Car1 c1 = new Car1(30, 3);
            Car2 c2 = new Car2(30, 3);
            assert c1.totalDistance() == c2.totalDistance();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

