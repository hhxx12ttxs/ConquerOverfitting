import java.util.*;

public class Airplanes {
	int [][] seats = new int[6][27];
	public void printSeats() {	
		for ( int c = 0; c <= 5; c++ ) {
			for ( int row = 0; row <=26; row++) {
				System.out.print(seats[c][row] + " ");
				if ( c == 2 ){
					System.out.print ( "      " );
			
				} else {
					System.out.println ( " " ); 
				}
			}
		}
	}
	
	public void add(int ppl) {    // 0 1 2   3 4 5
		int i=0;
		int j=0;
		if (ppl==3){
			if ((seats[i][0]==0)){
				seats[i][0]=1;
				seats[i][1]=1;
				seats[i][2]=1;
			} else {
				if ((seats[i][3]==0)){
					seats[i][3]=1;
					seats[i][4]=1;
					seats[i][5]=1;
				} else {
					i++;
				}
			} 
			
			/*if ((seats[i][0]==0)){
				seats[i][0]=1;
				seats[i][1]=1;
			} else {
				if ((seats[i][3]==0)){
					seats[i][3]=1;
					seats[i][4]=1;
				}
			}*/
			
			
			
		}
		
	}
	
	
	// 1 sec
	public static void main (String args[]) {
		int[][] arr = null;
		Airplanes plane = new Airplanes(); 
		plane.printSeats();
		for ( int i = 0; i <= 5; i++ ) {
			for ( int i2 = 0; i2 <=26; i2++) {
			//	if(arr[i][i2]==0){
					
				//}
			}
		}
        //
		/*Will come to use : bool placeGroup(int places[27][6], int groupSize);
void printSeats(int places[27][6]); */
    plane.add(3);
    plane.printSeats();
   
  /*      //Opredelqne na mesta za horata
            //1.Printing seats
        int [][] seats = new int[6][27];
               for ( int row = 0; row < 26; row++) {
                    for ( int c = 0; c <= 5; c++ ) {
                        System.out.print(seats[c][row] + " ");
                        if ( c == 2 ) 
                            System.out.print ( "      " );
                    }
                    System.out.println ( " " );
                     
               }
    */    
        // generate 100 numbers
    /*
        int sum = 0;
        for ( int i = 0; sum < 162; i ++ ) {
            int rvalue = new Random().nextInt(3) + 1;
                if( ( sum + rvalue ) <= 162){
                sum += rvalue; 
                }
               System.out.println(rvalue);
         
        }  
        System.out.println(sum);
        */
    
  
   }  
}


