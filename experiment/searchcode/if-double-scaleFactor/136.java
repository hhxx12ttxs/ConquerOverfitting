import java.util.Scanner;
public class Repeated<P> implements Pict {
	private final P[][] elems;
	private int height; 	// height > 0
	private int width;		// width > 0
	private int freeHeight = 0; // 0 <= freeHeight < height
	private int freeWidth = 0; // 0 <= freeWidth < width
	private double scaleFactor; // 0.1 <= scaleFactor <= 10.0
	
	@SuppressWarnings("unchecked")
	public Repeated(int height, int width) {
		this.height = height;
		this.width = width;
		this.elems = (P[][]) new Object[height][width];	
		this.scaleFactor = 1.0; //muss mit 1.0 initiallisiert werden, damit es Scaled nicht beeinflusst 
	}
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}
	
	//wird benoetigt fuer subclass Scaled um auf das Array zuzugreifen
	public P getElem(int x, int y){
		return elems[x][y];
	}
	
	
	// es muss ein freies Feld vorhanden sein, sonst wird nicht hinzugefuegt
	public boolean add(P elem) {
		if(freeWidth <= width-1) {
			elems[freeHeight][freeWidth] = elem;
			freeWidth++;
			return true;
		} else if(freeHeight <= height-1) {
			freeWidth = 0;
			freeHeight++;
			elems[freeHeight][freeWidth] = elem;
			freeWidth++;
			return true;
		}
		return false;
	}
	
	@Override
	/* alle Felder muessen vorher befuellt sein
 		P muss toString() implementieren
 		liefert ein 2-Dimensionales Bild
 	*/
	public String toString() {
		int maxHeight = 0;  //maximale Hoehe eines Objekts P
		int maxWidth = 0;	//maximale Breite eines Objekts P
		String result = "";
		
		//bestimmen der maxHeight und maxWidth:
		for(int i=0; i<this.height; i++) {
			for(int j=0; j<this.width; j++) {
				int currHeight = 0;
				int currWidth = 0;
				Scanner sc = new Scanner(elems[i][j].toString());
				while(sc.hasNextLine()) {
					currHeight++;
					currWidth = sc.nextLine().length();
				}
				if(currHeight > maxHeight) {
					maxHeight = currHeight;
				}
				if(currWidth > maxWidth) {
					maxWidth = currWidth;
				}
			}
		}
		
		int[] beginIndex = new int[width]; //enthaelt die Indizes der Elemente P in den Zeilen, ab denen nach dem naechsten Zeilenumbruch gesucht werden soll
		int scaledWidth = (int)Math.ceil(this.width * maxWidth * scaleFactor);
		int counter = 0;	//zaehlt die Zeilenumbrueche
		for(int i=0;i < this.height;i++) {
			
			for(int x=0;x < this.width;x++) {
				beginIndex[x] = 0;
			}
			
			for(int h=0;h < maxHeight; h++) {
				
				
				for(int j=0;j < this.width;j++) {
					String temporary = "";
					if(beginIndex[j] < elems[i][j].toString().length()) {
						try {
						temporary = elems[i][j].toString().substring(beginIndex[j], elems[i][j].toString().indexOf("\n",beginIndex[j]));
						} catch(StringIndexOutOfBoundsException s) {
							temporary = elems[i][j].toString();
						}
					}
					
					for(int x = temporary.length(); x < maxWidth; x++) {
						temporary += " ";
					}
					
					if(beginIndex[j] < elems[i][j].toString().length()) {
						beginIndex[j] = elems[i][j].toString().indexOf("\n",beginIndex[j]) + 1;
					}
					
					result += temporary;
					if(j == this.width-1) {
						
						if(scaleFactor < 1) {
							result = result.substring(0,scaledWidth+counter*scaledWidth+counter);
						}
						
						if(scaleFactor > 1) {
							String zeile = result.substring((scaledWidth*counter+counter),result.length());
							
							for(int d=0; result.length()-counter*scaledWidth-counter < scaledWidth;d++) {
								result += zeile.charAt(d);
								if(d == zeile.length()-1) {
									d=-1;
								}
							}
						}
						result += "\n";
						counter++;
					}
				}
			}
		}
		scaledWidth = result.indexOf("\n")+1;
		
		// Hoehe skalieren (scaleFactor != 1)
		if(scaleFactor < 1) {
			result = result.substring(0,(int)Math.ceil(maxHeight*height*scaleFactor)*scaledWidth);
		}
		if(scaleFactor > 1) {
			for(int i=0;result.length() < Math.ceil(maxHeight*height*scaleFactor)*scaledWidth;i++) {
					result += result.substring(i*scaledWidth,(i+1)*scaledWidth);
			}
		}
		
		return result;
	}
	
	// 0.1 <= factor <= 10.0
	public void scale(double factor) {
		this.scaleFactor = factor;
	}
	
}

