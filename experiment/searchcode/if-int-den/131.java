import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;


public class ForensicTool {
	static Scanner scan = new Scanner(System.in);	
	static Filemanager file = new Filemanager(); // skapar filhanteraren 

	public static void main(String[] args){



		// Programet körs och menyn loppas om
		while (true){
			for (int i = 0; i <20;i++) System.out.println();   // rensar clear
			printMenu();
			val();


		}


	}
	public static void printMenu(){

		System.out.println("----- Forensic TOool v1.0---------");
		System.out.println("1. Lista alla filer i en ");
		System.out.println("2. Sok efter filtyp");
		System.out.println("3. Sok efter andrade filer");
		System.out.println("4. Sok i filer"); 
		System.out.println("5. Sok efter filstorlekar");
		System.out.println("6. Kryptera fil");
		System.out.println("7. Dekryptera fil");
		System.out.println("0. Avsluta");

	}
	public static void val(){
		System.out.print("Vad valjer du: ");

		int val = scan.nextInt();

		switch (val){

		// case 1 kallar på listFiles som returnerar en array av alla filer i en mapp och undermappar
		//och skriver ut den.
		case 1:
			File a = file.getFile(); // filhanteraren används för att välja fil
			//listFiles(a);

			for (File f :listFiles(a)){
				System.out.println(f);
			}
			scan.nextLine(); // "paus" så användaren behöver trycka enter för att den ska gå vidare
			scan.nextLine();
			break;

			//case 2 ber användaren om att skriva in en fityp och kallar på findTypes för att hitta alla filer av
			//den typen i en mapp. mycket av koden ligger här och inte i metoden för att metoden ska kunna användas av andra funktioner
		case 2:
			System.out.println ("Enter the type of file you want to search for");
			final String filetype = scan.next();
			File[] filer = findTypes(file.getFile(), filetype);
			System.out.println ("List of all " + filetype );
			int count = 0;
			for (File file : filer) {
				System.out.println(file.toString());
				count++;
			}
			System.out.println ("total number of " + filetype + " files: " + count);

			scan.nextLine();
			scan.nextLine();
			break;
			// case 3 listar alla filer ändrade efter ett visst datum i en mapp
		case 3:
			listChangedFiles(listFiles(file.getFile()));
			break;
			// case 4 söker i filer efter en String. Använder findTypes för att bara söka igenom en viss filtyp
			// i detta fallet .txt. går att välja andra typer också typ .log etc.
		case 4:
			sokiFil(findTypes(file.getFile(), ".txt"));
			break;

			// case 5 listar alla filer med en viss storlek i ett intervall.
			// Filesize.class kan ockå köras helt separat.
		case 5:

			Filesize.printMenu();
			int choice = scan.nextInt();
			while (choice != 0)
			{
				Filesize.dispatch(choice, file.getFile());
				Filesize.printMenu();
				choice = scan.nextInt();
			}
			break;
			//case 6 krypterar innehållet i en textfil och sparar det i en ny fil
		case 6:
			Kryptering.main(file.getFile());
			break;
			//case 7 dekrypterar innehållet i en krypterad textfil om användaren har rätt nyckel 
		case 7:
			Dekryptering.main(file.getFile());
			break;
			//case 0 avslutar programmet	
		case 0:
			System.out.println("Tack");
			System.exit(0);
			break;

		default:
			System.out.println("Fel val");
			break;

		}


	}
	//Sparar alla filer i en mapp och dess undermappar i en array. returnerar arrayen.
	public static File[] listFiles(File path){

		File [] file = path.listFiles(); // börjar med att lista alla filer och mappar i den mapp man valt

		//skapar separat lista för filer och mappar
		ArrayList<File> filer = new ArrayList<File>();
		ArrayList<File> mappar = new ArrayList<File>(); 

		// lagrar alla filer och mappar som hittades i arrayen
		for (File f : file){
			if(f != null){ //för att skippa oläsliga filer eller andra fel
				if (f.isDirectory()){
					mappar.add(f);
				}
				else if (f.isFile()){
					filer.add(f);
				}
			}
		}

		// går igenom alla undermappar tills det inte finns några mer mappar att gå igenom
		while(!mappar.isEmpty()){
			File temp = mappar.get(0);

			if(temp != null){ 	//för att skippa oläsliga filer eller andra fel
				File [] map = temp.listFiles();
				if(map != null){ 	//för att skippa oläsliga filer eller andra fel
					for (File f : map ){
						if(f != null){ 	//för att skippa oläsliga filer eller andra fel
							if (f.isDirectory()){
								mappar.add(f);

							}
							else if (f.isFile()){
								filer.add(f);
							}

						}
					}
				}
			}

			mappar.remove(0); // tar bort den mappen som den har listat från arraylisten
		}

		File[] files = filer.toArray(new File[filer.size()]);    // returnarar arraylisten med filer som File array    
		return files;

	}
	// returnerar en array med filer av en viss filtyp från en mapp. 
	//tar som input mappen att söka igenom och filtypen att leta efter
	public static File[] findTypes(File files, final String filetype){

		//skapar ett textfilter objekt för att kunna filtrera ut vald filtyp ur listan med filer
		FilenameFilter textFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) { 
				if (name.endsWith(filetype)) {
					return true;
				} 
				else {
					return false;
				}
			}
		};


		return files.listFiles(textFilter); // listar alla filer med sitt textfilter och returnerar


	}

	// letar efter filer som är ändrade efter ett visst datum och skriver ut dem
	public static void listChangedFiles (File[] files)
	{

		SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd"); // skapar en datumformaterar med samma format som vi matar in
		System.out.println("Skriv in ett datum YYYYMMDD. Det kommer printa ut filer som blivit ändrade efter detta datumet.");
		scan.nextLine();
		String date=scan.nextLine();

		// kollar så man matat in datumet i rätt format och att det är ett giltligt datum
		while(true)
		{
			boolean L = date.length() == 8;
			boolean Y, M, D, number;
			Y=false;
			M=false;
			D=false;
			number=true;
			if(L)
			{
				for(int i=0; i<8; i++)
				{
					if(!Character.isDigit(date.charAt(i)))
						number=false;
				}
			}
			if (L && number)
			{
				Y = Integer.parseInt(date.substring(0, 4)) <= 2013;
				M = Integer.parseInt(date.substring(4, 6)) <=12;		
				D = Integer.parseInt(date.substring(6, 8)) <=31;	
			}
			if(Y && M && D && L && number)
			{
				break;
			}
			else{
				System.out.println("Ogiltligt datum! skriv om skriv rätt! YYYYMMDD");
				date=scan.nextLine();
			}
		}
		// gör om strängen till en inte för att kunna jämföra
		int dateint = Integer.parseInt(date);

		//kollar om filer är ändrade senare än man matat in och skriver ut dem
		//skulle också kunna lagras i en array och returneras
		for (File i: files)
		{
			int file_date=Integer.parseInt(ft.format(i.lastModified())); // använder datumformateraren för att göra om värdet man får i ms
			if(file_date>dateint)
				System.out.println(i+ "\t" +file_date);
		}
		scan.nextLine();
		scan.nextLine();
	}
	// Söker efter text i textfiler och skriver ut var texten hittats
	public static void sokiFil(File[] file){

		System.out.println("Skriv in ditt sökord");
		String sokord=scan.next();
		try{
			//kollar igenom alla filer i arrayen
			for (int index = 0; index<file.length; index++){
				//skapar ett scanner objekt för att kunna läsa i filen
				Scanner filescan = new Scanner (file[index]);

				int rad = 1; // raden som den börjar på
				String rader =""; // String med de rader där texten hittats
				boolean finns = false; // blir true om texten hittas i filen
				while (filescan.hasNextLine()) { // scannartills filens slut

					String line = filescan.nextLine(); // scannar raden

					if (line.indexOf(sokord)!=-1){ // om ordet eller texten hittas
						rader += rad +": "+line+"\n"; //lagra radnummer och raden i Strängen rader för utprintning senare
						finns = true; // texten hittades
					}    
					rad++; // plussar på radnummer
				}
				if (finns){ //om ordet hittades skrivs sökvägen till filen ut och de rader som matchade
					System.out.println(file[index]);
					System.out.println(rader);
				}
				filescan.close(); // stänger scanner strömmen.

			}
		}

		catch(Exception e){System.out.println(e);}

		scan.nextLine();
		scan.nextLine();

	}


}

