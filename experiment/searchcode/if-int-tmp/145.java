package org.wito;

import java.util.Arrays;

// http://www.ics.uci.edu/~eppstein/161/960130.html
public class KSelectMOM {

	public static int selectForSmall(int arr[], int k) {
		int[] tmp = Arrays.copyOf(arr, arr.length);
		Arrays.sort(tmp);
		return tmp[k];
	}

	public static int select(int[] arr, int k) {
		if (k >= arr.length) {
			throw new IllegalArgumentException("k make no sense to be >= arr lenght");
		}
		if (arr.length <= 10) {
			return selectForSmall(arr, k);
		}
		int msize = arr.length / 5;
		if (arr.length % 5 != 0) {
			msize++;
		}
		int[] marr = new int[msize];
		int marrIndex = 0;

		int[] tmp = new int[5];
		int tmpIndex = 0;

		for (int i = 0; i < arr.length + 1; i++) {

			tmp[tmpIndex] = arr[i];
			tmpIndex++;

			if (tmpIndex == tmp.length) {
				marr[marrIndex++] = selectForSmall(tmp, 3);
				tmpIndex = 0;

				if (i == arr.length - 1) {
					break;
				}

			} else if (i == arr.length - 1) {
				marr[marrIndex++] = selectForSmall(Arrays.copyOf(tmp, marrIndex), marrIndex / 2);
				tmpIndex = 0;
				break;
			}

		}

		if (marrIndex != marr.length) {
			throw new IllegalStateException();
		}

		int mom = select(marr, marr.length / 2);
		int r = Partition.partition(arr, 0, arr.length - 1, mom);

		if (k > r) {
			return select(Arrays.copyOfRange(arr, r, arr.length), k - r);
		} else {
			return select(Arrays.copyOfRange(arr, 0, r), k);
		}

	}

	int median(int[] arr) {
		return select(arr, arr.length / 2);
	}

	public static void main(String[] args) {

		KSelectMOM m = new KSelectMOM();
		m.median(new int[] { 1, 2, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 99, 88, 77, 66, 55, 44, 33, 22, 11, 112, 3, 4, 4, 56, 1, 2, 4, 5, 6, 7, 8, 9,
				1, 2, 3, 4, 5, 6, 7, 8, 9, 99, 88, 77, 66, 55, 44, 33, 22, 11, 112, 3, 4, 4, 56, 1, 2, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 99, 88, 77,
				66, 55, 44, 33, 22, 11, 112, 3, 4, 4, 56, 1, 2, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 99, 88, 77, 66, 55, 44, 33, 22, 11, 112, 3, 4, 4,
				56, 1, 2, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 99, 88, 77, 66, 55, 44, 33, 22, 11, 112, 3, 4, 4, 56 });

	}
}

