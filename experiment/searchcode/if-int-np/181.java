package org.bitbucket.artbugorski.postmedclaimfinder.parser;


import java.io.*;
import java.util.*;

import org.junit.*;

import org.bitbucket.artbugorski.util.NaryTree;
import org.bitbucket.artbugorski.postmedclaimfinder.TestUtil;
import org.bitbucket.artbugorski.postmedclaimfinder.AppUtil.*;
import org.bitbucket.artbugorski.postmedclaimfinder.experiment.ExperimentSettings;
import org.bitbucket.artbugorski.postmedclaimfinder.filter.WordFilter;
import org.bitbucket.artbugorski.postmedclaimfinder.model.*;
import org.bitbucket.artbugorski.postmedclaimfinder.matcher.*;
import org.bitbucket.artbugorski.postmedclaimfinder.scorer.DefaultScorers;
import org.bitbucket.artbugorski.postmedclaimfinder.reporter.CorpusWeightedAverageReporter;

import static org.junit.Assert.*;
import static org.bitbucket.artbugorski.postmedclaimfinder.parser.StepByStepParser.*;
import static org.bitbucket.artbugorski.postmedclaimfinder.TestUtil.*;


public class StepByStepParserTest {



	//_ **FIELDS** _//


	private DocumentInformation dummyDocInfo;



	//_ **INFRASTRUCTURE** _//


	@Before
	public void setUp() {
		dummyDocInfo = TestUtil.getNewDummyDocInfo();
	}



	//_ **METHODS** _//


    @Test( expected=NullPointerException.class )
    public void shouldFailOnNullString(){
        StepByStepParser.findMatchingParen( 0, null );
    }


    @Test( expected=IllegalArgumentException.class )
    public void shouldFailOnEmptyString(){
        StepByStepParser.findMatchingParen( 0, "" );
    }


    @Test( expected=IllegalArgumentException.class )
    public void shouldFailWhenNonOpenParenIndicated(){
        StepByStepParser.findMatchingParen( 1, "()" );
    }


    @Test( expected=IndexOutOfBoundsException.class )
    public void shouldFailOnIllegalIndex(){
        StepByStepParser.findMatchingParen( 2, "()" );
    }


	@Test
	public void shouldPassBaseCase(){
		int pos = StepByStepParser.findMatchingParen( 0, "()" );
		assertEquals( 1, pos );
	}


	@Test
	public void shouldPassNestedCase(){
		int pos = StepByStepParser.findMatchingParen( 0, "(()())" );
		assertEquals( 5, pos );
	}


	@Test
	public void shouldPassComplexNestedCase(){
		int pos = StepByStepParser.findMatchingParen( 0, "((()())((()))())" );
		assertEquals( 15, pos );
	}


	@Test( expected=IllegalArgumentException.class )
	public void shouldFailOnUnterminated(){
		StepByStepParser.findMatchingParen( 0, "(" );
	}


	@Test
	public void testHead() {
		final String data = "(TOP (S (SBAR (IN If) (S (NP (NP (DT the) (NN N) (NN KxD) (NNS mutants)) (PP (IN of) (NP (NN Rab24)))) (VP (VBP function) (PP (IN as) (NP (JJ dominant) (NNS suppressors) (, ,)))))) (NP (DT these) (NNS studies)) (VP (MD may) (VP (VB point) (PP (TO to) (NP (NP (DT a) (JJ unique) (NN role)) (PP (IN for) (NP (NN Rab24))) (PP (IN in) (NP (NP (NN degradation)) (PP (IN of) (NP (NP (JJ misfolded) (JJ cellular) (NNS proteins) (CC or) (VBG trafficking)) (PP (IN of) (NP (NNS proteins))))))))) (PP (TO to) (NP (DT the) (JJ nuclear) (NN envelope) (. .)))))))";
		assertEquals( "TOP", head(data) );
	}


	@Test
	public void atomTailTest() {
		final String data = "(a b)";
		assertEquals( "b", tail(data) );
	}


	@Test
	public void shouldHandleEmptyTail() {
		final String data = "(NN)";//looks stupid but has been found in the collins output
		assertEquals( "", tail(data) );
	}


	@Test
	public void shouldTailessHead() {
		final String data = "(NN)";//looks stupid but has been found in the collins output
		assertEquals( "NN", head(data) );
	}


	@Test
	public void compoundTailTest() {
		final String data = "(a (b c))";
		assertEquals( "(b c)", tail(data) );
	}


	@Test
	public void atomicParseTest(){
		final String line = "(NNS mutants)";
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new ByteArrayInputStream( line.getBytes() ),   createDefaultSettings()   );
		final NaryTree<Word> nt = parser.parseLine( line, WordFilter.NULL_FILTER );
		assertEquals( PartOfSpeech.NNS, nt.getLabel() );
		assertEquals( "mutants", nt.getValue().getWordValue() );
	}


	@Test
	public void compoundParseTest(){
		final String line = "(TOP (NNS mutants))";
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new ByteArrayInputStream( line.getBytes() ),   createDefaultSettings()   );
		final NaryTree<Word> nt = parser.parseLine( line, WordFilter.NULL_FILTER );

		assertEquals( GrammarPart.TOP, nt.getLabel() );
		assertNotNull( nt.getChild(PartOfSpeech.NNS) );
		assertEquals( PartOfSpeech.NNS, nt.getChild(PartOfSpeech.NNS).getLabel() );
		assertEquals( "mutants", nt.getChild(PartOfSpeech.NNS).getValue().getWordValue() );
	}


	@Test
	public void compoundTestAtomic(){
		final String data = "(TOP (NNS mutants))";
		assertFalse( atomic(data) );
	}


	@Test
	public void atomicTestAtomic(){
		final String data = "(NNS mutants))";
		assertTrue( atomic(data) );
	}


	@Test
	public void untaggedCompoundSExpHeadTest() {
		final String data = "((X (SYM /)) (NP (NN Rab1B) (NNS chimeras)))";

		assertEquals( "Head of untagged expression should be empty.", "", head(data) );
	}


	@Test
	public void untaggedCompoundSExpTailTest() {
		final String data = "((X (SYM /)) (NP (NN Rab1B) (NNS chimeras)))";

		assertEquals( "(X (SYM /)) (NP (NN Rab1B) (NNS chimeras))", tail(data) );
	}


	@Test
	public void shouldHandleUntaggedCompoundSExp() {
		final String data = "((X (SYM /)) (NP (NN Rab1B) (NNS chimeras)))";
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new ByteArrayInputStream( data.getBytes() ),   createDefaultSettings()   );
		final NaryTree<Word> nt = parser.parseLine( data, WordFilter.NULL_FILTER );

		assertEquals( GrammarPart._UNTAGGED, nt.getLabel() );

		final NaryTree<Word> X = nt.getChild( GrammarPart.X );
		assertEquals( PartOfSpeech.SYM, X.getChild(PartOfSpeech.SYM).getValue().getPart() );
		assertEquals( "/", X.getChild(PartOfSpeech.SYM).getValue().getWordValue() );

		final NaryTree<Word> NP = nt.getChild( GrammarPart.NP );
		assertNotNull( NP.getChild(PartOfSpeech.NN) );
		assertNotNull( NP.getChild(PartOfSpeech.NNS));
	}


	@Test
	public void lineParsingAcceptanceTest() throws IOException {
		final BufferedReader bufIn = new BufferedReader( new FileReader(BMC_SAMPLE_DATA) );
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new FileInputStream(BMC_SAMPLE_DATA),   createDefaultSettings()   );

		String line;

		while( (line=bufIn.readLine()) != null ){
			//System.out.println( "Working on: " + line );
			parser.parseLine( line, WordFilter.NULL_FILTER  );
		}
	}


//	@Test
//	public void parsingAcceptanceTest() throws IOException {
//
//		assertNotNull(  new StepByStepParser( dummyDocInfo, new FileInputStream(BMC_SAMPLE_DATA), AppUtil.NONE ).parse()  );
//	}


	@Test( expected=IllegalArgumentException.class )
	public void shoudFailOnUnbalancedParens(){
		final String data = "(TOP (S (NP (NP (NNS Studies)) (PP (IN with) (NP (NN Rab24))) ((X (SYM /)) (NP (NN Rab1B) (NNS chimeras))) (VP (VBD indicated) (SBAR (IN that) (S (S (VP (VBG targeting) (PP (IN of) (NP (DT the) (JJ mutant) (NN protein))) (PP (TO to) (NP (NNS inclusions))))) (VP (VBZ requires) (NP (NP (DT the) (JJ unique) (JJ C-terminal) (NN domain)) (PP (IN of) (NP (NN Rab24) (. .))))))))))";
		findMatchingParen(0, data);
	}


	@Test( expected=IllegalArgumentException.class )
	public void shoudFailOnUnbalancedParens2(){
		final String data = "(S (NP (NP (NNS Studies)) (PP (IN with) (NP (NN Rab24))) ((X (SYM /)) (NP (NN Rab1B) (NNS chimeras))) (VP (VBD indicated) (SBAR (IN that) (S (S (VP (VBG targeting) (PP (IN of) (NP (DT the) (JJ mutant) (NN protein))) (PP (TO to) (NP (NNS inclusions))))) (VP (VBZ requires) (NP (NP (DT the) (JJ unique) (JJ C-terminal) (NN domain)) (PP (IN of) (NP (NN Rab24) (. .)))))))))";
		findMatchingParen(0, data);
	}


	@Test
	public void tailShouldReturnAllChildren(){
		assertEquals( "(JJ Mutant) (NN Rab24) (NN GTPase)", tail("(NP (JJ Mutant) (NN Rab24) (NN GTPase))") );
	}


	@Test( expected=IllegalStateException.class )
	public void shouldFailReturningChildWhenHasChildren(){
		final String line = "(NP (JJ Mutant) (NN Rab24) (NN GTPase))";
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new ByteArrayInputStream( line.getBytes() ),   createDefaultSettings()   );
		final NaryTree<Word> nt = parser.parseLine( line, WordFilter.NULL_FILTER  );
		nt.getChild(PartOfSpeech.NN);

		fail( "Has multiple children so should fail to return just one." );
	}


	@Test( expected=IllegalStateException.class )
	public void shouldFailReturningChildWhenHasNoChildren(){
		final String line = "(NP (JJ Mutant) (NN Rab24) (NN GTPase))";
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new ByteArrayInputStream( line.getBytes() ),   createDefaultSettings()   );
		final NaryTree<Word> nt = parser.parseLine( line, WordFilter.NULL_FILTER );
		nt.getChild(PartOfSpeech.DT);

		fail( "Has no such child so should fail to return just one." );
	}


	@Test
	public void multipleChildrenShouldParseCorrectly(){
		final String line = "(NP (JJ Mutant) (NN Rab24) (NN GTPase))";
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new ByteArrayInputStream( line.getBytes() ),   createDefaultSettings()   );
		final NaryTree<Word> nt = parser.parseLine( line, WordFilter.NULL_FILTER  );

		assertNotNull( nt.getChildren(PartOfSpeech.NN) );

		final List<NaryTree<Word>> l = nt.getChildren(PartOfSpeech.NN);
		assertEquals( "Should have only 2 NN children.", 2, l.size() );

		final Word rab24 = l.get(0).getValue(), gtPase = l.get(1).getValue();

		assertEquals( PartOfSpeech.NN, rab24.getPart() );
		assertEquals( "Rab24", rab24.getWordValue() );

		assertEquals( PartOfSpeech.NN, gtPase.getPart() );
		assertEquals( "GTPase", gtPase.getWordValue() );
	}


	/**
	 * This is taken from a run time crash exception.
	 */
	@Test
	public void shouldNotCauseExceptionInTreeMap() {
		final String line = "(TOP (S (NP (PRP We)) (VP (VBD anticipated) (SBAR (IN that) (S (NP (NP (ADJP (RB even) (RB relatively) (JJ small)) (NNS changes)) (PP (IN in) (NP (DT the) (NN Pax6) (: :)))) (NP (NN Pax6) (NN ratio)) (VP (MD might) (VP (VB be) (ADJP (JJ important)) (SBAR (IN since) (S (NP (NP (JJR stronger) (NNS effects)) (PP (IN on) (NP (NN gene) (NN activity))) (PP (IN via) (NP (NN P6CON) (CC and) (NN 5aCON)))) (VP (VBP are) (VP (VBN observed) (SBAR (IN if) (S (NP (NN Pax6) (CC and) (NN Pax6)) (VP (VBP are) (VP (VBN introduced) (PP (IN into) (NP (JJ cultured) (NN cell) (NNS lines))) (PP (IN at) (NP (NP (QP (NNS ratios) (IN of) (CD 1) (: :))) (NP (QP (CD 1) (CC or) (CD 8) (: :))) (NP (NP (CD 1)) (PP (IN than) (PP (IN at) (NP (NP (NNS ratios)) (PP (IN of) (NP (CD 2) (: :))))))) (NP (CD 1) (, ,) (CD 4) (: :)) (NP (QP (CD 1) (CC or) (CD 16) (: :))) (NP (CD 1) (. .)))))))))))))))))))";
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new ByteArrayInputStream( line.getBytes() ),   createDefaultSettings()   );
		final NaryTree<Word> nt = parser.parseLine( line, WordFilter.NULL_FILTER );
	}


	@Test
	public void shouldWork() {
		final String line = "(TOP (S (NP (PRP We)) (VP (VB propose) (SBAR (IN that) (S (NP (NP (DT the) (JJ reduced) (NN incidence) (CC and) (NN severity)) (PP (IN of) (NP (NP (NN GvHD)) (PP (IN after) (NP (NP (JJ allogeneic) (NN transplantation)) (PP (IN of) (NP (JJ umbilical) (NN CB) (NNS cells)))))))) (VP (VBZ is) (ADJP (JJ due) (PP (TO to) (NP (NP (NP (JJR lesser) (NN activation)) (PP (IN of) (NP (JJ specific) (NN transcription) (NNS factors)))) (CC and) (NP (NP (DT a) (JJ subsequent) (NN reduction)) (PP (IN in) (NP (NP (NN production)) (PP (IN of) (NP (JJ certain) (NNS cytokines) (. .)))))))))))))))";
		final StepByStepParser parser = new StepByStepParser(   dummyDocInfo,   new ByteArrayInputStream( line.getBytes() ),   createDefaultSettings()   );
		final NaryTree<Word> nt = parser.parseLine( line, WordFilter.NULL_FILTER );
	}

		static ExperimentSettings createDefaultSettings(){
		final ExperimentSettings settings = new ExperimentSettings();

		settings.setCorpus( Corpus.focused );
		settings.setUseFigLess( false );
		settings.setScoringMethod( DefaultScorers.BINARY_PRESENCE );
		settings.setFilter( WordFilter.NULL_FILTER );
		settings.setReporter( new CorpusWeightedAverageReporter( settings ) );
		settings.setBiGramCutOff( 2 );
		settings.setFixedExpressionRequirement( 7 );
		settings.setFeatureSet( FeatureSet.BIGRAM );
		settings.setCoreCount( Runtime.getRuntime().availableProcessors() );//*/ 1;
		settings.setFilterFixedExpressions( false );

		settings.setMatchingHeuristics(
				Arrays.<Class<? extends SectionMatcher>>asList(
					SentenceTripletMatcher.class,
					ClauseSectionMatcher.class,
					TripletClauseMatcher.class,
					SectionBlobMatcher.class,
					BottomUpMatcher.class
				)
			);

		return settings;
	}


}
