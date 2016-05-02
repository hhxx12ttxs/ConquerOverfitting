package ab2.aufgabe1.a;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * ALP III - Julian
 * <p/>
 * User: Baka
 * Date: 06.11.12
 * Time: 12:11
 * To change this template use File | Settings | File Templates.
 */
public class main {
    public static void main(String[] args){

        Random random = new Random();
        int length = 8;
        int[] test = new int[length];
        for (int i = 0; i < length; i++){
            test[i] = random.nextInt();
        }

        int[] result = slowSort(test);

        for(int i = 0; i < result.length; i++){
            int cur = result[i];
            System.out.println(i + ":" + cur);
        }

        System.out.println("PERMS: " + BENCHMARK_permCount);

        //==============================================================
        // S T A T I S T I C S zur randomisierten Permutation (Aufgabe b)
        //==============================================================

        // jeweils 6 Durchläufe:   ~ Durchschnitt:
        // 2 Elemente  --> [2][1][1][1][1][2]   ~ 1.333_
        // 3 Elemente  --> [18][5][5][3][29][4] ~ 10.667_
        // 4 Elemente  --> [8][9][18][40][18][47] ~ 23.333_
        // 5 Elemente  --> [411][157][30][49][399][96] ~ 190.333_
        // 6 Elemente  --> [3047][396][164][811][172][244] ~ 805.666_

    }

    private static int BENCHMARK_permCount = 0;

    /**
     * Mit diesem Flag kann Aufgabe b an- und abgeschaltet werden
     */
    private static boolean IS_AUFGABE_B = false;

    /**
     * performs a slow sort on the target array and returns the sorted array
     * @param array unsorted array
     * @return sorted array
     */
    private static int[] slowSort(int[] array){
        if (isSorted(array)){
            // array has 0 or 1 elements or is already sorted
            return array;
        }

        List<int[]> perms = permutations(array);
        if (IS_AUFGABE_B){
            // "Beschreiben Sie, wie Sie eine zufallige Permutation erzeugen, so dass jede gleichwahrscheinlich ist."
            // Es wird zufällig aus allen möglichen Permutationen gewählt. Somit hängt die "Wahrscheinlichkeit" vom
            // jeweiligen Algorithmus ab, der die Zufallszahlen generiert, da alle Permutationen in der Liste gleich oft
            // vorkommen und somit für jedes Element die Wahrscheinlichkeit 1/n besteht.
            Random random = new Random();
            while (true){
                BENCHMARK_permCount++;
                int idx = random.nextInt(perms.size());
                int[] perm = perms.get(idx);
                if (isSorted(perm)){
                    return perm;
                }
            }
        } else{
            for(int[] perm : perms){
                BENCHMARK_permCount++;
                if (isSorted(perm)){
                    return perm;
                }
            }
        }

        throw new NoSuchFieldError("Wir tun so als sei das die <KeinePermutationGefunden>Exception");
    }

    private static void swap (int[] array, int a, int b){
        int temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }

    /**
     * generates all permutations of the given array
     * @param array
     * @return
     */
    public static List<int[]> permutations (int[] array){
        int endIndex = array.length-1;
        List<int[]> accumulator = new ArrayList<int[]>();
        permutations(array, endIndex, accumulator);
        return accumulator;
    }

    /**
     * generates all permutations recursivly
     * The last element is fixed and all permutations of the previous elements are generated (recursivly)
     * Than swap the last element and procede till all elements have been the last element.
     * @param array
     * @param endIndex
     * @param accumulator
     */
    private static void permutations (int[] array, int endIndex, List<int[]> accumulator){
        if(endIndex==0){
            accumulator.add((int[])array.clone());
        } else{
            permutations(array, endIndex -1, accumulator);
            for(int i = 0; i <= endIndex-1; i++){
                swap(array,i,endIndex);
                permutations(array, endIndex-1, accumulator);
                // reset to original state
                swap(array,i,endIndex);
            }
        }
    }

    /**
     * checks, if the array is sorted
     * @param array
     * @return TRUE when sorted, else false
     */
    private static boolean isSorted(int[] array){
        for (int i = 1; i < array.length; i++){
            if (array[i-1] > array[i]){
                return false;
            }
        }
        return true;
    }
}

