import java.util.*;

class Sortieren {
	// new int[]{ 4, 7, 2, 8, 3, 6, 5, 1 };
	// new int[]{ 1, 7, 2, 8, 3, 6, 5, 4 };
	// new int[]{ 1, 2, 7, 8, 3, 6, 5, 4 };
	// new int[]{ 1, 2, 3, 8, 7, 6, 5, 4 };
	// new int[]{ 1, 2, 3, 4, 7, 6, 5, 8 };

	/**
	 * Die Methode durchsucht das Feld a ab Position x
	 * und findet die kleinste Zahl, und speichert deren
	 * Stelle in einer Zwischenvariable Minimum ab.
	 * Dann vertauscht sie die Elemente an x und Minimum.
	 */
	public static void kleinsteTauschen(int[] a, int x) {
		int minPos = x;
		
		for (int i = x + 1; i < a.length; i = i + 1) {
			if (a[i] < a[minPos]) {
				minPos = i;
			}
		}
		
		// Minimum des Felds a ist an Position minPos
		int tmp = a[minPos];
		a[minPos] = a[x];
		a[x] = tmp;
	}
	
	public static void sortieren() {
		int[] a = { 1, 2, 7, 8, 3, 6, 5, 4 };
		System.out.println( Arrays.toString(a) );
		kleinsteTauschen(a, 2);
		System.out.println( Arrays.toString(a) );
		// ==> { 1, 2, *3*, 8, *7*, 6, 5, 4 };
	}
	
	public static void main(String[] args) {
		Sortieren.sortieren();
	}
}
