package TAIC.test ; 
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import TAIC.Classifier.Classifier;
import TAIC.Classifier.SVM;
import TAIC.text.Dict;
import TAIC.text.TextBayes;

public class DataGen1 extends DataGen{
	Vector < String > [] imageCorpus ;
	int trainSize , testSize , classes , totalKeys; 
	int ITERATIONS = 10 ;
	int imageWord [][] = null ; 
	int imagePerWord = 0 ;
	double p_w_c[][] = null;
	Dict dict = new Dict ( "dict.txt") ;
	
	public static void main ( String argu [] ) {
		if ( argu.length < 5 ) {
			System.out.println ( "argument: webImage , trainImageSize, keyNo , testSize , iteration" ) ;
			return ; 
		}
		Integer.valueOf ( argu [ 3 ] ) ;
		DataGen temp = new DataGen (  ) ;
		temp.test(Integer.valueOf ( argu [ 0 ] ) , Integer.valueOf ( argu [ 1 ] ) , 
					Integer.valueOf ( argu [ 2 ] ) , Integer.valueOf ( argu [ 3 ]),  Integer.valueOf ( argu [ 4 ]) , "");
	}
		
	public DataGen1 () {
		readIn ();
	}
	
	void readIn () {   //   if mode = 0 then it is the 2 class mode ; else it is the 4 class mode,
		int i , j , totalPic , thisClass = 0  ;
		try {
			Scanner scanner ;
			
			scanner = new Scanner ( new File ( "svmCorpus" ) ) ; 
			totalPic = scanner.nextInt () ;
			classes = scanner.nextInt () ;
			imageCorpus = new Vector[ classes * 2  ];
			for ( i = 0 ; i < classes * 2 ; i ++ ) imageCorpus [ i ] = new Vector < String >() ;
			
			for ( i = 0 ; i < totalPic ; i ++ ) {
				if ( ! scanner.hasNextInt() ) break ; 
				thisClass = scanner.nextInt () ;
				imageCorpus [ thisClass ].add ( (thisClass%classes==0?"0":"1") + " " + scanner.nextLine().trim() );
			}
			
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
			classifier.lambda = e ; 
			classifier.train ( "trainset" ) ;
			if ( a != 0  ) {
				TextBayes tb = new TextBayes ( ) ;
				tb.train( "auxilary" ) ;
				classifier.addInAuxilary( tb, dict , e ) ;
			}
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

		
		ByteArrayOutputStream  trainOut = null , testOut = null ; 
		try {
			PrintStream ftrain , ftest, faux ;
			
			faux = new PrintStream ( new File ( "auxilary")) ; 
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
			
			for ( i = 0 ; i < classes ; i ++ ) {
				total = imageCorpus[ i ].size() ; 
				b = new boolean [ total ] ;
				for ( j =0 ;j < webImageSize ;j ++ ) {
					temp = random.nextInt( total ) ;
					if ( ! b [ temp ]){
						faux.println ( imageCorpus[ i ].get( temp ) ) ;
						b [ temp ] = true; 
					}
					else j -- ; 
				}
			}
				
			for ( j = classes ; j < classes * 2 ; j ++ ) {
				total = imageCorpus [ j ].size() ; 
				b = new boolean [ total ] ;
				for ( i = 0 ; i < trainImageSize + testImageSize ; i ++ ) {
					temp = random.nextInt ( total ) ;
					if ( ! b[ temp ] ) {
						if ( i < trainImageSize ) ftrain.println ( imageCorpus[ j ].get ( temp ) ) ;
						else ftest.println ( imageCorpus[ j ].get ( temp ) ) ;
						b [ temp ] = true ; 
					}
					else i -- ; 
				}
			}
			
			ftrain.close();
			ftest.close() ;
			faux.close() ; 
			
			if ( ! (classifier instanceof SVM )) picProb.close() ;
			
			if ( classifier.isPipe() ) {
				classifier.trainPipe = trainOut.toByteArray() ;
				classifier.testPipe = testOut.toByteArray() ; 
			}
		}
		catch ( Exception e ) {e.printStackTrace () ; }
	}
}
