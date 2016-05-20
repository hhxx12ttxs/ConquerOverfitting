package TAIC.IPLM ; 
import java.util.* ; 
import java.io.* ;
import TAIC.Classifier.* ; 

public class DataGen {
	Vector < String > [] imageCorpus ;
	int trainSize , testSize , classes , totalKeys; 
	int ITERATIONS = 10 ;
	double result [] = null ;
	int imageWord [][] = null ; 
	int imagePerWord = 0 ;
	double p_w_c[][] = null;
	//double nambda ; 
	
	public static void main ( String argu [] ) {
		if ( argu.length < 5 ) {
			System.out.println ( "argument: webImage , trainImageSize, keyNo , testSize , iteration" ) ;
			return ; 
		}
		Integer.valueOf ( argu [ 3 ] ) ;
		DataGen temp = new DataGen (  ) ;
		temp.test(Integer.valueOf ( argu [ 0 ] ) , Integer.valueOf ( argu [ 1 ] ) , 
					Integer.valueOf ( argu [ 2 ] ) , Integer.valueOf ( argu [ 3 ]),  Integer.valueOf ( argu [ 4 ]) , "");
		//temp.five_Fold () ; 
	}
		
	
	void readIn () {   //   if mode = 0 then it is the 2 class mode ; else it is the 4 class mode,
		int i , j , totalPic , thisClass = 0  ;
		try {
			Scanner scanner = new Scanner ( new File ( "word2image.txt") ) ; 
			classes = scanner.nextInt() ; 
			totalKeys = scanner.nextInt() ;
			p_w_c = new double [ classes ][ totalKeys ];
			for ( i = 0 ;  i < classes ; i ++ ) 
				for ( j = 0 ; j < totalKeys ; j ++ ) {
					int classNo = scanner.nextInt() ;
					int wordNo = scanner.nextInt() ; 
					p_w_c [classNo][wordNo] = scanner.nextDouble() ; 			
				}
			//nambda = scanner.nextDouble() ; 
			scanner.close() ;
			
			scanner = new Scanner ( new File ( "svmCorpus" ) ) ; 
			totalPic = scanner.nextInt () ;
			classes = scanner.nextInt () ;
			imageWord = new int [ classes ][ totalPic ] ;
			imageCorpus = new Vector [ classes * 2  ];
			for ( i = 0 ; i < classes * 2 ; i ++ ) imageCorpus [ i ] = new Vector < String >() ;
			
			int [] len = new int [ classes ] ; 
			for ( i = 0 ; i < totalPic ; i ++ ) {
				thisClass = scanner.nextInt () ;
				if ( thisClass < classes ) imageWord [ thisClass ][ len [ thisClass ] ++ ] = scanner.nextInt() ; 
				else scanner.nextInt() ; 
				imageCorpus [ thisClass ].add ( (thisClass%classes==0?"-1":"+1") + " " + scanner.nextLine().trim() );
			}
			
			scanner.close() ;
			
			
			
		
//			for ( i = 0 ; i < classes * 2 ;i ++ ) {
//				for ( j = 0 ; j < imageCorpus [ i ].size(); j ++ ) System.out.println ( imageCorpus[ i].get(j));
//				System.out.println ( "==========================" ) ;
//			}
		
		}
		catch ( Exception e ){ e.printStackTrace () ; }
	}
	
	
	public TestResult test ( int a , int b , int c , int d, int it , String path ) {  // webimages , trainImage , keywords, testsize, iteration
		return test ( a, b, c, d , it , 1.0 , path) ; 
	}

	public TestResult test ( int a , int b , int c , int d, int it , double e , String path ) {  
		// webimages , trainImage , keywords, testsize,  iteration, nambda
		int i ; 
		double sum = 0 , temp ;
		
		readIn ();
		PrintStream fout =null; 
		try {
			fout = new PrintStream ( new File ( path + a + "_" + b + "_" + c + "_" + it ) ); 
		}catch ( Exception ex ) {
			ex.printStackTrace (); 
		}
		//fout = System.out ; 
		
		//fout.println ( a + " " + b ) ; 
		
//		Classifier classifier = new Bayes () ;
		Classifier classifier = new RiskEstimate () ;
		
		ITERATIONS =  it ;		
		result = new double [ ITERATIONS ] ; 
		for ( i = 0 ; i < ITERATIONS ; i ++ ) { 
			randomTest ( a , b, c , d ,e ) ;
			classifier.train ( "trainset" ) ;
			temp = classifier.test ( "testset" ) ; 
			sum +=  temp ; 
			result [ i ] = temp ; 
			fout.println ( "Iteration " + i + ": " + temp ) ;
		}
		double average = sum / ITERATIONS ;
		fout.println ( "Average :" + average + " " + deviation ( average )  ) ;	
		return new TestResult ( average , deviation ( average ) ) ;
	} 
	
	double deviation ( double average ){
		double total = 0 ;
		int i ; 
		for ( i = 0 ; i < ITERATIONS ; i ++ ) 
			total += ( result [ i ] - average ) * ( result [ i ] - average ) ;
		return Math.sqrt ( total / ( ITERATIONS - 1 )) ;
	}
	
	void randomTest ( int webImageSize , int trainImageSize , int keys , int testImageSize , double nambda ) {
		int i , j , k , total , temp  ; 
		boolean b [] ;
		Random random = new Random ( (new Date()).getTime () ) ;
		int curImage[][] = new int [ classes ][ keys ] ;
		double classTotal [] = new double [ classes ] ;  
		for ( i = 0 ; i < classes ; i ++ )
			for ( j = 0 ; j < keys ; j ++ ) classTotal [ i ] += p_w_c[ i ][ j ] ;
		
		try {
			PrintStream ftrain = new PrintStream ( new File ( "trainset" ) );
			PrintStream ftest = new PrintStream ( new File ( "testset" ) ) ;
			PrintStream picProb = new PrintStream ( new File ( "picProb.txt" ) ) ;
			
			for ( i = 0 ; i < classes ; i ++ ) {
				for ( j = 0 ; j < imageCorpus[i].size() ; j ++ ) {
					int word =  imageWord [i][j] ;
					//System.out.println ( i + " " + j + " " + imageCorpus[i].size() + " " +   word) ;
					if ( word >= keys ) continue ;
					if ( word != 0 ) if ( curImage[i][word] >= webImageSize / keys) continue; else ; 
					else  if ( curImage[i][word] >= webImageSize / keys + webImageSize % keys ) continue;
					curImage[i][word] ++ ;
					ftrain.println ( imageCorpus[ i ].get ( j ) ) ;
					picProb.println ( p_w_c[i][word] / classTotal [ i ]) ; 
				}
			}		
				
			for ( j = classes ; j < classes * 2 ; j ++ ) {
				total = imageCorpus [ j ].size() ; 
				b = new boolean [ total ] ;
				for ( i = 0 ; i < trainImageSize + testImageSize ; i ++ ) {
					temp = random.nextInt ( total ) ;
					if ( ! b[ temp ] ) {
						if ( i < trainImageSize ){
							ftrain.println ( imageCorpus[ j ].get ( temp ) ) ;
							picProb.println ( nambda )  ;
						}
						else ftest.println ( imageCorpus[ j ].get ( temp ) ) ;
						b [ temp ] = true ; 
					}
					else i -- ; 
				}
			}
			
//			int totalPic = 0 ;
//			for ( i = 0 ; i  < classes ; i ++ )  
//				for (j = 0 ; j < keys ;j ++ ) totalPic += curImage [ i ][ j ] ;
//			picProb.println ( classes + " " + totalPic + " " +  keys ) ; 
//	
//			for ( i = 0 ; i  < classes ; i ++ ) { 
//				for (j = 0; j < keys ; j ++ ) picProb.print ( curImage[i][j] + " " ) ;
//				picProb.println() ; 
//			}
			
			ftrain.close();
			ftest.close() ;
			picProb.println () ; 
		}
		catch ( Exception e ) {e.printStackTrace () ; }
	}
	
}
