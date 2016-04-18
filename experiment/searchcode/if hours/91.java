package Ch_8_Exercises;

/**
 * Created by jonwelker on 9/2/14.
 */
public class Ch_8_04_employeeHours {

    public static void main(String[] args) {
        int[] employee = {0, 1, 2, 3, 4, 5, 6, 7};
        int[][] empHours = {
                {2, 4, 3, 4, 5, 8, 8},
                {7, 3, 4, 3, 3, 4, 4},
                {3, 3, 4, 3, 3, 2, 2},
                {9, 3, 4, 7, 3, 4, 1},
                {3, 5, 4, 3, 6, 3, 8},
                {3, 4, 4, 6, 3, 4, 4},
                {3, 7, 4, 8, 3, 8, 4},
                {6, 3, 5, 9, 2, 7, 9}};

        int[] temp = new int[empHours[0].length];


        for (int i = 0; i < empHours.length; i++) {
            int max = sumHours(empHours, i);
            int index = i;
            for (int j = i; j < empHours.length; j++) {
                if (max < sumHours(empHours, j)) {
                    max = sumHours(empHours, j);
                    index = j;

                }
            }
            temp = empHours[i];
            empHours[i] = empHours[index];
            empHours[index] = temp;
            int empNum = employee[i];
            employee[i] = employee[index];
            employee[index] = empNum;
        }

        for (int i = 0; i < empHours.length; i++) {
            System.out.println("Employee " + employee[i] + " hours are: " + sumHours(empHours, i));
        }

    }



    public static int sumHours(int[][] empHours, int emp) {
        int sum = 0;
        for (int i = 0; i < empHours[emp].length; i++) {
            sum += empHours[emp][i];
        }
        return sum;
    }

}

