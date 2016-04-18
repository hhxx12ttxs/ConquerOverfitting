package com.codeforces.beforeexam;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BeforeExam {
	
	public int minHours;
	public int maxHours;
	
	public BeforeExam(int minHours, int maxHours) {
		this.minHours = minHours;
		this.maxHours = maxHours;
	}
	
	public int getHours() {
		return maxHours - minHours;
	}

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		String[] daysHours = in.nextLine().split(" ");
		int days = Integer.parseInt(daysHours[0]);
		int hours = Integer.parseInt(daysHours[1]);

		List<BeforeExam> schedule = new ArrayList<BeforeExam>(days);
		int totalMin = 0, totalMax = 0;

		for (int i = 0; i < days; i++) {
			String[] minMaxHours = in.nextLine().split(" ");
			BeforeExam d = new BeforeExam(Integer.parseInt(minMaxHours[0]), Integer.parseInt(minMaxHours[1]));
			
			schedule.add(d);
			
			totalMin += d.minHours;
			totalMax += d.maxHours;
		}
		in.close();

		if (hours >= totalMin && hours <= totalMax) {
			System.out.println("YES");
			
			for(BeforeExam d : schedule) {
				
				int margin = hours - totalMin;
				if (margin >= d.getHours()) {
					hours -= d.maxHours;
					System.out.print(d.maxHours + " ");
				}
				else if (margin < d.getHours()) {
					hours -= margin + d.minHours;
					System.out.print((margin + d.minHours) + " ");
				}
				
				totalMin -= d.minHours;
				
				assert (hours >= totalMin);
			}
			
			assert (hours == 0);
		}
		else {
			System.out.println("NO");
		}
	}
}
