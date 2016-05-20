package TAIC.test;

import java.io.File;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import TAIC.Classifier.Classifier;

public class FiveFold1 {
	Vector < String > [] imageCorpus ;
	int trainSize , testSize , classes , totalKeys; 
	int ITERATIONS = 40 ;
	double result [] = null ;
	int imageWord [][] = null ; 
	int imagePerWord = 0 ;
	double p_w_c[][] = null;
	//double nambda ; 
	
	public static void main ( String argu [] ) {
		int IT = 10 ;
		PrintStream fout = System.out; 
		try {
			if ( argu.length != 0) fout = new PrintStream ( new File ( argu[ 0 ] )); 
		}
		catch ( Exception e ){
			e.printStackTrace() ;
		}
		
		double total = 0 ; 
		for ( int i = 0 ; i < IT ; i ++ ) {
			TestResult result = ( new FiveFold1 ()).test() ;
			total += result.average ; 
			//fout.println ( result.average + "\t" + result.deviation ) ;
		}
		fout.println ( total / IT ) ; 
	}

	public FiveFold1 () {
		readIn ();
	}
	
	void readIn () {   //   if mode = 0 then it is the 2 class mode ; else it is the 4 class mode,
		int i , j , totalPic , thisClass = 0  ;
		try {
			Scanner scanner = new Scanner ( new File ( "svmCorpus" ) ) ; 
			totalPic = scanner.nextInt () ;
			classes = scanner.nextInt () ;
			imageWord = new int [ classes ][ totalPic ] ;
			imageCorpus = new Vector [ classes * 2  ];
			for ( i = 0 ; i < classes * 2 ; i ++ ) imageCorpus [ i ] = new Vector < String >() ;
			
			int [] len = new int [ classes ] ; 
			for ( i = 0 ; i < totalPic ; i ++ ) {
				thisClass = scanner.nextInt () ;
				imageCorpus [ thisClass ].add ( (thisClass%classes==0?"0":"1") + " " + scanner.nextLine().trim() );
			}
			
			scanner.close() ;
		}
		catch ( Exception e ){ e.printStackTrace () ; }
	}
	
	public TestResult test () {  
		int i ; 
		double sum = 0 , temp ;
		Classifier classifier = Classifier.cFact.getClassifer( Classifier.cConfig.get("classifier" )) ; 

		randomGenSet () ;	
		ITERATIONS = 5 ;
		result = new double[ ITERATIONS ] ;
		for ( i = 0 ; i  < 5 ; i ++ ){
			randomTest ( i ) ;
			classifier.train ( "trainset" ) ;
			temp = classifier.test ( "testset" ) ; 
			sum +=  temp ; 
			result [ i ] = temp ; 
		}
		double average = sum / ITERATIONS ;
		return new TestResult ( average , deviation ( average ) ) ;
	} 
	
	double deviation ( double average ){
		double total = 0 ;
		int i ; 
		for ( i = 0 ; i < ITERATIONS ; i ++ ) 
			total += ( result [ i ] - average ) * ( result [ i ] - average ) ;
		return Math.sqrt ( total / ( ITERATIONS - 1 )) ;
	}
	
	void randomTest ( int whichFive ) {
		int i , j , k , total , temp  ; 
		double classTotal [] = new double [ classes ] ;  
		
		try {
			PrintStream ftrain = new PrintStream ( new File ( "trainset" ) );
			PrintStream ftest = new PrintStream ( new File ( "testset" ) ) ;
			PrintStream picProb = new PrintStream ( new File ( "picProb.txt" ) ) ;
							
			for ( i = classes ; i < classes * 2 ; i ++ ) {
				total = imageCorpus [ i ].size() ;
				int size = total / 5 ; 
				int beginPos = size * whichFive ;
				int endPos = Math.min( beginPos + size,  total ) ;
				if ( endPos - size < beginPos ) beginPos = endPos - size ;
				for ( j = 0 ; j < total ; j ++ ) 
					if ( !( beginPos <= j && j < endPos) ){
						ftrain.println ( imageCorpus[ i ].get( j )) ;
						picProb.println ( 1.0  ) ;
					}
				for ( j = beginPos ; j < endPos ; j ++ ) ftest.println( imageCorpus[ i ].get( j ));
			}
			
			ftrain.close();
			ftest.close() ;
			picProb.println () ; 
		}
		catch ( Exception e ) {e.printStackTrace () ; }
	}
	
	
	void randomGenSet () {
		for ( int i = classes ; i < classes * 2 ; i ++ ) {
			int total = imageCorpus[ i ].size() ;
			int list [ ] = new int [ total ] ; 
			boolean b [] = new boolean [ total ] ;
			for ( int j = 0 ; j < total ; j ++ ) {
				int temp = random.nextInt( total ) ; 
				if ( ! b [ temp ] )  list [ j ] = temp ; 
				else j -- ;
				b [ temp ] = true ; 
			}
			Vector <String> result = new Vector <String > () ; 
			for ( int j = 0 ; j < total ; j ++ ) result.add( imageCorpus[ i ].get(list[ j ]));
			imageCorpus [ i ] = result ;
		}
	}
	
	static Random random = new Random () ; 
}

