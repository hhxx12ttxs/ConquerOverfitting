public class Razionale {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		setNumeratore(1 + (int) (Math.random() * 10));
		setDenominatore(1 + (int) (Math.random() * 10));

		System.out.println("La frazione inserita č: " + getNumeratore() + "/"
				+ getDenominatore());

		System.out.println("La frazione č apparente? "
				+ apparente(getNumeratore(), getDenominatore()));

		System.out.println("La frazione č propria? "
				+ propria(getNumeratore(), getDenominatore()));

		moltiplica(getNumeratore(), getDenominatore(),
				(1 + (int) (Math.random() * 10)));

		divide(getNumeratore(), getDenominatore(),
				(1 + (int) (Math.random() * 10)));
	}

	static int numeratore, denominatore;

	public static void setNumeratore(int num) {
		numeratore = num;
	}

	public static void setDenominatore(int den) {
		denominatore = den;
	}

	public static int getNumeratore() {
		return numeratore;
	}

	public static int getDenominatore() {
		return denominatore;
	}

	public static boolean apparente(int num, int den) {
		if (num % den == 0)
			return true;
		else
			return false;
	}

	public static boolean propria(int num, int den) {
		if (num < den)
			return true;
		else
			return false;
	}

	public static void moltiplica(int num, int den, int moltiplicatore) {
		int temp = num * moltiplicatore;
		System.out.println("La frazione data, moltiplicata per "
				+ moltiplicatore + " č " + temp + "/" + den);
	}

	public static void divide(int num, int den, int divisore) {
		System.out.println("Devo dividere la frazione data per " + divisore
				+ ", ovvero la devo moltiplicare per 1/" + divisore);
		int newNum = 1 * num;
		int newDen = divisore * den;

		System.out.println("La frazione data, diviso 1/" + divisore + " č: "
				+ newNum + "/" + newDen);
	}

}

