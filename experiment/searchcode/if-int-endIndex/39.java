System.out.println(returnIndex(initialMass, 26));
}

public static int returnIndex(int[] m, int elementToSearch) {
int startIndex = 0;
int endIndex = m.length - 1;
int middleIndex = (endIndex - startIndex) / 2;
while (elementToSearch != m[middleIndex] &amp;&amp; startIndex < endIndex) {

