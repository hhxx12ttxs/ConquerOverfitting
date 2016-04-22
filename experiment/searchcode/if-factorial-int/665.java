public class PermutationSequence {
    public String getPermutation(int n, int k) {
        int[] factorial = new int[10];
        int product = 1; factorial[0] = 1;
        for (int i = 1; i <= 9; i++) {
            product *= i;
            factorial[i] = product;
        }
        List<Integer> list = new ArrayList();
        for (int i = 1; i <= n; i++)
            list.add(i);
        StringBuilder sb = new StringBuilder();
        for (int i = n - 1; i >= 0; i--) {
            /*
            for (int t = 1; t <= i + 1; t++) {
                if (t * factorial[i] >= k) {
                    sb.append(list.remove(t - 1));
                    k -= (t - 1) * factorial[i];
                    break;
                }
            }
            */
            int bit = k / factorial[i] - (k % factorial[i] == 0 ? 1 : 0);
            sb.append(list.remove(bit));
            k -= bit * factorial[i];
        }
        return sb.toString();
    }
}
