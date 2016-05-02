
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Profesor p = new Profesor("Cristina", "POO");
		Student s = new Student("Octavian", 10);
		
		System.out.println(p.toString());
		System.out.println(s.toString());
		
		Student sc = s;
		Student s2 = new Student("Dincu", 9);
		
		System.out.println("Octavian == Octavian: " +  s.equals(sc));
		System.out.println("Octavian == Dincu: " + s.equals(s2));
		
		System.out.println("\nUpCasting\n");
		Persoana[] vp = new Persoana[4];
		vp[0] = new Profesor("Ghiu", "Chin");
		vp[1] = new Student("Octavian", 5);
		vp[2] = new Student("Dincu", 10);
		vp[3] = new Profesor("Unu","PI");
		
		for(int i = 0; i < vp.length; i++) {
			System.out.println(vp[i].toString());
		}
		
		System.out.println("\nDownCasting\n");
		for(int i = 0; i < vp.length; i++) {
			if( vp[i] instanceof Profesor)
				((Profesor)vp[i]).preda();
			if( vp[i] instanceof Student)
				((Student)vp[i]).invata();
		}
		
		System.out.println("\nDownCasting Aiurea\n");
		for(int i = 0; i < vp.length; i++) {
			((Profesor)vp[i]).preda();
		}
	}

}

