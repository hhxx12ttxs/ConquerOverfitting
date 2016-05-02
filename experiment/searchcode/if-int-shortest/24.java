import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class main {

	// [id][x - y]
	static double[][] points = new double[280][3];

	public static void main(String[] args) throws IOException {
		parser(args);
		findClosest();
		// buildMap();
		// double d =Double.valueOf("7.03600e+02");
		// System.out.println(d);
	}

	// 4 8.75100e+02 1.13610e+03
	public static void parser(String[] arg) throws IOException {
		FileReader fr = new FileReader(new File("/Users/krantz/git/ClosestPoint/ClosestPoint/lab4/usa13509.tsp"));
		BufferedReader br = new BufferedReader(fr);

		// NODE_COORD_SECTION
		// 1 245552.778 817827.778
		// 2 247133.333 810905.556
		// Alt.
		// 2 5.51200e+02 9.96400e+02
		// 3 6.27400e+02 9.96400e+02

		String trimRead;
		String read;
		while (br.ready()) {
			read = br.readLine();
			trimRead = read;
			trimRead = read.replaceAll("\\s+", " ");
			if (trimRead.charAt(0) == ' ') {
				trimRead = trimRead.substring(1);
			}

			String[] nbrs = trimRead.split(" ");

			if (Character.isDigit((nbrs[0].charAt(0)))) {
				int nodeNbr = Integer.parseInt(nbrs[0]);
				String xNbr = nbrs[1].trim();
				double x = Double.valueOf(xNbr);
				String yNbr = nbrs[2].trim();
				double y = Double.valueOf(yNbr);

				points[nodeNbr][0] = nodeNbr;
				points[nodeNbr][1] = x;
				points[nodeNbr][2] = y;
//				System.out.println("NodeNbr: " + nodeNbr);
//				System.out.println(x);
//				System.out.println(y);
			} else if (read.contains("DIMENSION")) {

				String[] dim = read.split(":");
				String l = dim[1].trim();
				int length = Integer.parseInt(l);
				points = new double[length + 1][3];
				;
			}

		}

		br.close();
	}

	private static void findClosest() {
		double shortest = Double.MAX_VALUE;

		for (int i = 0; i < points.length; i++) {
			double id = points[i][0];
			double x = points[i][1];
			double y = points[i][2];
			for (int j = 0; j < points.length; j++) {
				if (i != j) {
					double otherId = points[j][0];
					double otherX = points[j][1];
					double otherY = points[j][2];
//					System.out.println("ID: " + id + " X: " + x + " Y: " + y);
//					System.out.println("ID: " + otherId + "X: " + otherX + " Y: " + otherY);
					double l = Math.hypot((otherX - x), (otherY - y));
//					System.out.println("Hypot: " + l);
					if (l < shortest) {
//						if(l == 0.0){
//							System.out.println("HEHE");
//						}
						shortest = l;
					}
				}
			}
//			System.out.println();
		}
		System.out.println("Shortest is: " + shortest);

	}

	private static void buildMap() {

		// id 0-1 closest
		points[0][0] = 0;
		points[1][0] = 1;
		points[2][0] = 2;
		points[3][0] = 3;

		points[0][1] = 0;
		points[1][1] = 5;
		points[2][1] = 6;
		points[3][1] = 7;

		points[0][2] = 0;
		points[1][2] = 4;
		points[2][2] = 8;
		points[3][2] = 9;
	}

}

