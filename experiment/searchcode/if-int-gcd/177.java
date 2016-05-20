/*
 * Created on 18-aug-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.suijten.bordermaker;

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * @author OrbitZ
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public abstract class MoveRowsTableModel extends AbstractTableModel {
	public void moveRow(List v, int start, int end, int to) {
		int shift = to - start;
		int first, last;
		if (shift < 0) {
			first = to;
			last = end;
		} else {
			first = start;
			last = to + end - start;
		}
		rotate(v, first, last + 1, shift);

		fireTableRowsUpdated(first, last);
	}

	private static void rotate(List v, int a, int b, int shift) {
		int size = b - a;
		int r = size - shift;
		int g = gcd(size, r);
		for (int i = 0; i < g; i++) {
			int to = i;
			Object tmp = v.get(a + to);
			for (int from = (to + r) % size; from != i; from = (to + r) % size) {
				v.set(a + to, v.get(a + from));
				to = from;
			}
			v.set(a + to, tmp);
		}
	}

	private static int gcd(int i, int j) {
		return (j == 0) ? i : gcd(j, i % j);
	}
}
