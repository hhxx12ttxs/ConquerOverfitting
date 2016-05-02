import java.io.*;
import java.util.*;

public class Filemanager {

	private static Scanner scan = new Scanner(System.in);
	private int pos=0;	//position i filhanteraren för skrollning .
	private File path;	// sökvägen som filhanteraren listar / är inne i
	private boolean val;	// om fil är vald för att returnera
	private File vald;	//fil att returnera



	public Filemanager(){
		getRoot();	// när objektet skapas får man välja enhet att jobba med
	}

	//visar alla rootenheter och låter användaren välja en att arbeta mot
	public void getRoot(){

		val = false; 

		for (int i = 0; i <20;i++) System.out.println();   // rensar clear
		printRubrik();
		printFiles(File.listRoots());


		do{
			System.out.print("Valj en enhet att arbeta med: ");
			String val = scan.nextLine();

			//kollar vilken enhet man väljer och sätter sokvägen till den
			if (val.matches("[0-9]+")){
				int nr =  Integer.parseInt(val);
				path = File.listRoots()[nr];
				this.val=true;
			}
			else{
				System.out.println("Felaktigt val");
			}
		}while(!this.val); // sålänge man inte valt ngt så frågar den igen


	}
	// huvudfunktion som kan köras för att välja en fil. 
	// Man får välja en fil eller mapp som metoden returnerar
	public File getFile(){

		val = false;

		while (true){ //

			for (int i = 0; i <20;i++) System.out.println();   // rensar clear
			printRubrik(); // printar rubrik
			printFiles(path); // printar alla filer i sokvägen man jobbar i
			printVal(); //printar lite options
			choose(); // låter användaren skriva in ngt

			if (val)
				return vald; // om man valt en mapp returnera den. loop avslutas


		}


	}
	// Printar rubriken, separat ifall man vill bygga vidare med tex olika rubriker
	public void printRubrik(){
		System.out.println("Valj en fil eller map");
		System.out.println("-----------------------------------------");

	}
	// Skoter utprintningen av alla filer. tar som inputh en path att skriva ut.
	public void printFiles(File sok){
		System.out.println("Namn                 	             |"); 


		//Printar alla filer från den positionen man är på beroende på om man "scrollat" eller inte och 10 filer ner.
		//Kollar också så att pos + 10 inte är större än längden på listan. 
		for (int i = pos; i <((pos+10)  < list(sok).length ? (pos+10) : list(sok).length) ; i++){
			System.out.println(i+": "+list(sok)[i]);// printar filens sokväg och nummer i listan
		}
		System.out.println("-----------------------------------------");

	}
	//sköter utprintningen av filer. tar som input en file array istället och används i getRoot.
	public void printFiles(File[] sok){
		System.out.println("Namn                 ");



		for (int i = pos; i <((pos+10)  < sok.length ? (pos+10) : sok.length) ; i++){
			System.out.println(i+": "+sok[i]);
		}
		System.out.println("-----------------------------------------");

	}
	// Skriver ut valen. det man kan skriva för att navigera i filhanteraren
	public void printVal(){

		//cd (nr) för att gå in i den mappen. cd .. för att gå upp en mapp
		//val (nr) för att välja en mapp eller fil. val . för att välja den mappen man är i
		// u för att gå upp i listan och d för att gå ner		
		System.out.println("  cd (nr)(..)  |  val (nr)(.) |  u  |  d  ");
		System.out.println("-----------------------------------------");

	}
	// låter användaren skriva in ngt och kollar med regex vad användaren valt.
	public void choose(){

		System.out.print("Skriv ngt: ");
		String val = "";
		val = scan.nextLine();

		// flyttar postitionen 5 steg upp om man inte är vid toppen redan
		if (val.matches("u")){
			if (pos-5>0)	
				pos -= 5;
			else
				pos = 0;
		}
		// flyttar positionen 5 steg ner om man inte är vid slutet
		else if (val.matches("d")){
			if (pos+5 < list(path).length){	
				pos += 5;
			}			

		}
		// om man skriver cd (nr) så flyttas man in i den mappen som är vald.
		else if (val.matches("cd\\s[0-9]+")){
			int nr =  Integer.parseInt(val.substring(3)); 
			path = list(path)[nr];
			pos = 0;
		}
		//om man skriver cd .. så går man upp en mapp
		else if (val.matches("cd\\s\\.\\.")){

			path = path.getParentFile();
			pos = 0;
		}
		// om man skriver val . så väljer man den mappen man är i
		else if (val.matches("val\\s\\.")){
			vald = path;
			this.val = true;
		}
		// om man skriver val (nr) så väljer man en mapp eller fil
		else if (val.matches("val\\s[0-9]+")){
			int nr =  Integer.parseInt(val.substring(4));
			vald = list(path)[nr];
			this.val = true;
		}
		//om man skriver cd (sokväg) så går man in den mappen om man skrivit ett giltligt namn
		else if (val.matches("cd\\s.+")){
			if (new File(path.toString() + val.substring(3)).isDirectory() || new File(path.toString() + val.substring(3)).isFile() ){
				path = new File(path.toString() + val.substring(3)); 
				pos = 0;
			}

			else{
				System.out.println(path.toString() + val.substring(3) +" ar inte en mapp eller fil");
				choose();
			}
		}
		// om man skriver val (sökväg) så väljer den den mappen om det är en mapp eller fil
		else if (val.matches("val\\s.+")){
			if (new File(path.toString() + val.substring(4)).isDirectory() || new File(path.toString() + val.substring(4)).isFile() ){
				vald = new File(path.toString() +val.substring(4));
				this.val = true;
			}
			else{
				System.out.println(path.toString() + val.substring(4) +" ar inte en mapp eller fil");
				choose();
			}
		}
		// om man skrvit fel eller inte valt ngt så får man testa igen
		else{
			System.out.println("inget giltligt val");
			choose();
		}

	}
	//listar alla filer i en sokväg till en array
	public File[] list(File sok){

		File[] filer = sok.listFiles();
		return filer;
	}
	//Funktion för att spara alla filer i en map och undermappar i en array. 
	//Kallar på sig själv för att gå igenom alla undermappar och returnerar en Array
	public File [] walk( String path ) {

		ArrayList<File> filer = new ArrayList<File>(); //skapar en arraylist
		File root = new File( path );
		File[] list = root.listFiles(); //listar filer

		if (list != null){ //för att skippa oläsliga filer eller andra fel
			for ( File f : list ) {
				if (f != null){ //för att skippa oläsliga filer eller andra fel
					if ( f.isDirectory() ) {
						walk( f.getAbsolutePath() ); // om det är en mapp, kör sig själv.
						// System.out.println( "Dir:" + f.getAbsoluteFile() );
					}
					else {
						//System.out.println( "File:" + f.getAbsoluteFile() );
						filer.add(f); //om fil lägger den till i listan.
					}
				}
			}
		}

		return filer.toArray(new File[filer.size()]); // returnarar arraylisten som File array
	}
}

