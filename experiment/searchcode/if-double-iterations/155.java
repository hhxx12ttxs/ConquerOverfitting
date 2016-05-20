package TAIC.test ; 
import java.util.* ; 
import java.io.* ;
import TAIC.Classifier.* ; 

public class DataGen { 
	public static int MaxClass = TestMode.para.getParaInt( "MaxClass" ) ;
	Vector < String > [] imageCorpus ;
	int trainSize , testSize , classes , totalKeys; 
	int ITERATIONS = 10 ;
	int imageWord [][] = null ; 
	int imagePerWord = 0 ;
	double p_w_c[][] = null;
	//double nambda ;
	//String list[][] ; 
	
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
		
	public DataGen () {
		readIn ();
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


			
//			list = new String [ classes ][] ; 
//			for ( i = 0 ; i < classes  ; i ++ ) list [ i ] = new String [ 500 ]; 
//			Scanner ic = new Scanner ( new File ( "ImageCorpus.txt" )) ; 
			
			scanner = new Scanner ( new File ( "svmCorpus" ) ) ; 
			totalPic = scanner.nextInt () ;
			classes = scanner.nextInt () ;
			imageWord = new int [ classes ][ totalPic ] ;
			imageCorpus = new Vector [ classes * 2  ];
			for ( i = 0 ; i < classes * 2 ; i ++ ) imageCorpus [ i ] = new Vector < String >() ;
			
			int [] len = new int [ classes ] ; 
			for ( i = 0 ; i < totalPic ; i ++ ) {
				if ( ! scanner.hasNextInt() ) break ; 
				thisClass = scanner.nextInt () ;
				if ( thisClass < classes ) imageWord [ thisClass ][ len [ thisClass ] ++ ] = scanner.nextInt() ; 
				else scanner.nextInt() ; 
				//imageCorpus [ thisClass ].add ( (thisClass%classes==0?"-1":"+1") + " " + scanner.nextLine().trim() );   // this line is for SVM
				imageCorpus [ thisClass ].add ( (thisClass % classes) + " " + scanner.nextLine().trim() );   // this line is for Multiclass Classification
				
//				ic.nextInt() ; ic.nextInt() ; 
//				if ( thisClass >= classes ) list [ thisClass % classes ][ imageCorpus[ thisClass ].size() - 1 ] = ic.nextLine().trim() ;
//				else ic.nextLine() ; 
			}
//			ic.close() ;
			scanner.close() ;
				
			
//			for ( i = 0 ; i < classes * 2 ;i ++ ) {
//				for ( j = 0 ; j < imageCorpus [ i ].size(); j ++ ) System.out.println ( imageCorpus[ i].get(j));
//				System.out.println ( "==========================" ) ;
//			}
		
		}
		catch ( Exception e ){ e.printStackTrace () ; }
	}
	
	
	public TestResult test ( int a , int b , int c , int d, int it , String path ) { 
		// webimages , trainImage , keywords, testsize, iteration
		return test ( a, b, c, d , it , 1.0 , path) ; 
	}

	public TestResult test ( int a , int b , int c , int d, int it , double e , String path ) {  
		// webimages (K) , BaseImage , keywords, testsize,  iteration, nambda
		int i ; 
		double sum = 0 , temp ;

		PrintStream fout =null; 
		try {
			if ( !path.equals( "" ) )
				fout = new PrintStream ( new File ( path + a + "_" + b + "_" + c + "_" + it ) ); 
		}catch ( Exception ex ) {
			ex.printStackTrace (); 
		}
		//fout = System.out ; 

		
		Classifier classifier = Classifier.cFact.getClassifer( Classifier.cConfig.get("classifier" )) ; 
		
		ITERATIONS =  it ;
		TestResult result = new TestResult () ;  
		result.iters = it ;
		result.details = new double [ ITERATIONS ] ;
		for ( i = 0 ; i < ITERATIONS ; i ++ ) { 
			randomTest ( a , b, c , d ,e, classifier ) ;
			classifier.train ( "trainset" ) ;
			temp = classifier.test ( "testset" ) ; 
			sum +=  temp ; 
			result.details [ i ] = temp ; 
			if ( fout != null ) fout.println ( "Iteration " + i + ": " + temp ) ;
		}
		double average = sum / ITERATIONS ;
		if ( fout != null ) fout.println ( "Average :" + average + " " + deviation ( result.details , average )  ) ;	
		result.average = average ;
//		result.deviation = deviation ( result.details , average ) ; 
		return  result ;
	} 
	
	double deviation ( double [] result , double average ){
		double total = 0 ;
		int i ; 
		for ( i = 0 ; i < ITERATIONS ; i ++ ) 
			total += ( result [ i ] - average ) * ( result [ i ] - average ) ;
		return Math.sqrt ( total / ( ITERATIONS - 1 )) ;
	}
	
	void randomTest ( int webImageSize , int trainImageSize , int keys , 
			int testImageSize , double lambda , Classifier classifier ) {
		int i , j , k , total , temp  ; 
		boolean b [] ;
		Random random = new Random ( (new Date()).getTime () ) ;
		int curImage[][] = new int [ classes ][ keys ] ;
		double classTotal [] = new double [ classes ] ;  
		for ( i = 0 ; i < classes ; i ++ )
			for ( j = 0 ; j < keys ; j ++ ) classTotal [ i ] += p_w_c[ i ][ j ] ;
		
		classifier.lambda = lambda ;
		ByteArrayOutputStream  trainOut = null , testOut = null ; 
		try {
			PrintStream ftrain , ftest;
			if ( classifier.isPipe() ) {
				trainOut = new ByteArrayOutputStream () ; 
				ftrain = new PrintStream ( trainOut ) ;  
			} else ftrain  = new PrintStream ( new File ( "trainset" ) );
			
			if ( classifier.isPipe () ) {
				testOut = new ByteArrayOutputStream () ; 
				ftest = new PrintStream ( testOut ) ;  
			} else ftest  = new PrintStream ( new File ( "testset" ) );
			
			PrintStream picProb = null ;
			if ( ! (classifier instanceof SVM )) 
				picProb = new PrintStream ( new File ( "picProb.txt" ) ) ;
			else {
				lambda *= lambda ; 
				lambda *= lambda ; 
			}
			
//			System.out.println ( webImageSize + " " + trainImageSize + " " + testImageSize + " " + lambda ) ; 
			for ( i = 0 ; i < classes ; i ++ ) {
				for ( j = 0 ; j < imageCorpus[i].size() ; j ++ ) {
					int word =  imageWord [i][j] ;
					//System.out.println ( i + " " + j + " " + imageCorpus[i].size() + " " +   word) ;
					if ( word >= keys ) continue ;
					if ( word != 0 ) if ( curImage[i][word] >= webImageSize ) continue; else ; 
					else  if ( curImage[i][word] >= webImageSize  ) continue;
					curImage[i][word] ++ ;
					if ( ! (classifier instanceof SVM )) {
						ftrain.println ( imageCorpus[ i ].get ( j ) ) ;
						picProb.println ( p_w_c[i][word] / classTotal [ i ]) ;
					}
					else {
						String str = imageCorpus[ i ].get ( j ) ;
						if ( MaxClass == 2 ) if ( str.startsWith("0") ) str = str.replaceFirst("0", "-1"); else; 
						else str = str.replaceFirst( String.valueOf( str.charAt(0)) , String.valueOf(str.charAt(0)- '0'+ 1)) ;
						ftrain.println ( str) ;	
					}
				}
			}		
				
//			PrintStream fout = new PrintStream ( new File ( "ori_image.txt" )) ;
			
//			random.setSeed( 123425 ) ; 
			for ( j = classes ; j < classes * 2 ; j ++ ) {
				total = imageCorpus [ j ].size() ; 
				b = new boolean [ total ] ;
				for ( i = 0 ; i < trainImageSize + testImageSize ; i ++ ) {
					temp = random.nextInt ( total ) ;
					if ( ! b[ temp ] ) {
						if ( i < trainImageSize ){
							if ( ! (classifier instanceof SVM )) {
								ftrain.println ( imageCorpus[ j ].get ( temp ) ) ;
								picProb.println ( lambda )  ;
							}
							else {
								String str = imageCorpus[ j ].get ( temp ) ;
								if ( MaxClass == 2 ) if ( str.startsWith("0") ) str = str.replaceFirst("0", "-1"); else; 
								else str = str.replaceFirst( String.valueOf( str.charAt(0)) , String.valueOf(str.charAt(0)- '0'+ 1)) ;
								ftrain.println ( str) ;	
							}
						}
						else {
							if ( ! (classifier instanceof SVM )) ftest.println ( imageCorpus[ j ].get ( temp ) ) ;
							else {
								String str = imageCorpus[ j ].get ( temp ) ;
								if ( MaxClass == 2 ) if ( str.startsWith("0") ) str = str.replaceFirst("0", "-1"); else; 
								else str = str.replaceFirst( String.valueOf( str.charAt(0)) , String.valueOf(str.charAt(0)- '0'+ 1)) ;
								ftest.println ( str) ;	
							} 
						}
						b [ temp ] = true ; 
					}
					else i -- ; 
				}
			}
			
//			fout.close() ; 
			
//			int totalPic = 0 ;
//			for ( i = 0 ; i  < classes ; i ++ )  
//				for (j = 0 ; j < keys ;j ++ ) totalPic += curImage [ i ][ j ] ;
//			picProb.println ( classes + " " + totalPic + " " +  keys ) ; 
	
//			for ( i = 0 ; i  < classes ; i ++ ) { 
//				for (j = 0; j < keys ; j ++ ) picProb.print ( curImage[i][j] + " " ) ;
//				picProb.println() ; 
//			}
			
			ftrain.close();
			ftest.close() ;
			
			if ( ! (classifier instanceof SVM )) picProb.close() ;
			
			if ( classifier.isPipe() ) {
				classifier.trainPipe = trainOut.toByteArray() ;
				classifier.testPipe = testOut.toByteArray() ; 
			}
		}
		catch ( Exception e ) {e.printStackTrace () ; }
	}
	
}
