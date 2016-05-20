/*
 Â Copyright 2011 S. Merrony
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

package uk.co.stephenmerrony.mxml2abc;

import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;

import noNamespace.Accidental;
import noNamespace.Articulations;
import noNamespace.Attributes;
import noNamespace.Backup;
import noNamespace.BarStyle;
import noNamespace.Barline;
import noNamespace.Direction;
import noNamespace.Dynamics;
import noNamespace.FormattedText;
import noNamespace.Forward;
import noNamespace.Harmony;
import noNamespace.Kind;
import noNamespace.Notations;
import noNamespace.Note;
import noNamespace.Ornaments;
import noNamespace.Pitch;
import noNamespace.RightLeftMiddle;
import noNamespace.RootAlter;
import noNamespace.Slur;
import noNamespace.StartStop;
import noNamespace.StartStopContinue;
import noNamespace.Stem;
import noNamespace.Time;
import noNamespace.ScorePartwiseDocument.ScorePartwise.Part.Measure;

public class Bar {

	private boolean inTuplet = false;
	private int tupletCounter = 0;
	private boolean inGraceNotes = false;
	
	private boolean warnedBackup = false;
	
	private Headers headers;
	
	public Bar( final Headers hdrs ) {
		headers = hdrs;
	}
	
	public boolean measureToABC( final Measure measure, 
								 final String partID, 
								 final String voiceID, 
								 final boolean prevClosingBarline, 
								 int divisions ) {
		
		final XmlObject[] barObjects = measure.selectPath( "./*" );

		// work through all elements of the bar in the order they appear in the 
		// source file (which IS significant)
		boolean inChord = false, inCresc = false, inDim = false;
		boolean leftBarlineOnly = false;
		boolean hasClosingBarline = false;
		boolean inferredBarline = false;
		boolean noNotesYet = true;
		
//		// TODO: experimental new system/page - screws up inline lyrics
//		
//		if (measure.sizeOfPrintArray() > 0 && 
//			measure.getPrintArray( 0 ).isSetNewSystem() && 
//			measure.getPrintArray( 0 ).getNewSystem().toString().contentEquals( "yes" ) ) {
//			System.out.println();
//		}
		
		
		
		// for each object in the bar...
		for (int boIx = 0; boIx < barObjects.length; boIx++) {
			
			final XmlObject thisObject = barObjects[boIx];
			
//		for (XmlObject thisObject : barObjects) {
		
			// skip processing if not for the current (or no) voice
			if (voiceID != null &&
				thisObject instanceof Note) {
				final Note tmpNote = (Note) thisObject;
				if (tmpNote.isSetVoice() && 
					!tmpNote.getVoice().contentEquals( voiceID )){
					continue;
				}
			}

			// warn if we encounter "backup" instructions - cannot handle for now
			if (thisObject instanceof Backup  && !warnedBackup) {
				System.err.println( "Warning: This version of mxml2abc cannot reliably handle MusicXML with <backup> instructions.  Results will be unpredictable!" );
				warnedBackup = true;
			}
			
			if (thisObject instanceof Forward) {
				final Forward fwd = (Forward) thisObject;
				// skip if not for this voice
				if (fwd.isSetVoice() &&
					(voiceID != null  && !fwd.getVoice().contentEquals( voiceID ))
				   ) {
					continue;
				} else {
					// if (fwd.getDuration().intValue() == divisions) { // whole bar forward
						if (inChord) {
							System.out.print( ']' );
							inChord = false;
						}
						System.out.printf( " x%d ", fwd.getDuration().intValue() / divisions );
					// }
				}
			}
			
			// barlines
			if (thisObject instanceof Barline) {
				if (inChord) {
					System.out.print( ']' );
					inChord = false;
				}
				final Barline barline = (Barline) thisObject;
				if (barline != null) {
					leftBarlineOnly = barlineToABC( barline );
					if (!leftBarlineOnly) { hasClosingBarline = true; }
				}
				inferredBarline = true; // bit of a fudge so that next object round we don't infer a barline
			} else {
				// infer ordinary barline if not one at start of bar ********
				if (noNotesYet && !prevClosingBarline && !inferredBarline) {
					if (inChord) {
						System.out.print( ']' );
						inChord = false;
					}
					System.out.print( " |" );
					inferredBarline = true;
				}
			}

	
			// change of metre or key
			if (thisObject instanceof Attributes) {
				final Attributes atts = (Attributes) thisObject;
				
				if (inChord) {
					System.out.print( ']' );
					inChord = false;
				}
				
				if (atts.sizeOfTimeArray() > 0) {
					final Time ta = atts.getTimeArray(0);		
					// don't output if 1sr bar and matches M:
					if ( ! (measure.getNumber().contentEquals( "1" ) && 
							headers.numBeats != null &&
							headers.numBeats.contentEquals( ta.getBeatsArray( 0 ) ) &&
							headers.beatType.contentEquals( ta.getBeatTypeArray( 0  ))
							)
						){
						System.out.printf( " [M: %s/%s] ", 
								ta.getBeatsArray( 0 ),
								ta.getBeatTypeArray( 0 )
						);
					}
				}
				
				if (atts.sizeOfKeyArray() > 0 ) {
					final int fifths = atts.getKeyArray( 0 ).getFifths().intValue();
					// don't output if 1st bar and main key
					if (!(measure.getNumber().contentEquals( "1" ) && fifths == headers.initialKeyFifths)) {
						System.out.printf( " [K: %s] ", Headers.keyFromFifths( fifths )  );
					}
				}
			}
			
			
			// notes
			if (thisObject instanceof Note) {
				noNotesYet = false;
				final Note note = (Note) thisObject;
				if (note != null && 
					((voiceID == null) || (note.isSetVoice() && note.getVoice().contentEquals( voiceID )))
				   ) {				
					
					// finish off grace notes if required
					if (inGraceNotes && !note.isSetGrace()) {
						System.out.print( '}' );
						inGraceNotes = false;
					}
					
					// finish off chord if required
					if (inChord && !note.isSetChord()) {
						System.out.print( ']' );
						inChord = false;
					}
					
					// start of grace notes?
					if (!inGraceNotes && note.isSetGrace()){
						System.out.print( '{' );
						inGraceNotes = true;
					}
					
					// check if there's another note following with chord set
					if ((boIx < barObjects.length - 1) &&
						(barObjects[boIx + 1] instanceof Note)) {
						final Note nextNote = (Note) barObjects[boIx + 1];
						// start of chord?
						if (!note.isSetChord() && nextNote.isSetChord()) {
							if (nextNote.sizeOfBeamArray() == 0) {
								System.out.print( ' ' );
							} else {
								if (nextNote.getBeamArray( 0 ).enumValue().toString().equals( "begin" )) {
									System.out.print( ' ' );
								}
							}
							System.out.print( '[' );
							inChord = true;
						} 
					} 
					
					
					
					noteToABC( note, inChord, divisions );
					
					
				}
			} else {
				if (inChord) {
					System.out.print( ']' );
					inChord = false;
				}
			}
			
			// "harmony" (guitar chords)
			if (thisObject instanceof Harmony) {
				final Harmony harmony = (Harmony) thisObject;
				if (harmony != null) {
					harmonyToABC( harmony );
				}
			}
			
			//I think some of this was MusicXML v.1 only...
			// standalone dynamics
			if (thisObject instanceof Direction) {
				final Direction direction = (Direction) thisObject;
				
				if (direction.getDirectionTypeArray( 0 ).sizeOfDynamicsArray() > 0) {
					standaloneDynamicsToABC( direction.getDirectionTypeArray( 0 ).getDynamicsArray(0) );
				}
				
				if (direction.getDirectionTypeArray( 0 ).sizeOfWordsArray() > 0) {
					directionWordsToABC( direction.getDirectionTypeArray( 0 ).getWordsArray( 0 ));
				}
				
				if (direction.getDirectionTypeArray( 0 ).isSetWedge()) {
					if (direction.getDirectionTypeArray( 0 ).getWedge().getType().toString() == "crescendo") {
						System.out.print( "!<(!" );
						inCresc = true;
					}
					if (direction.getDirectionTypeArray( 0 ).getWedge().getType().toString() == "diminuendo") {
						System.out.print( "!>(!" );
						inDim = true;
					}
					if (direction.getDirectionTypeArray( 0 ).getWedge().getType().toString() == "stop") {
						if (inCresc) {
							System.out.print( "!<)!" );
							inCresc = false;
						}
						if (inDim) {
							System.out.print( "!>)!" );
							inDim = false;
						}
					}
				}
				
			}
			
			
			
		}
		
//		// close a chord at end of bar
		if (inChord) {
			System.out.print( ']' );
			inChord = false;
		}
		
		// if there were no notes or rests then insert a spacer (invisible rest)
		if (noNotesYet) {
			System.out.print( " Z " );
		}
		
		return hasClosingBarline;
	}

	private void directionWordsToABC( final FormattedText wordsArray ) {
		System.out.printf( "\"^%s\"", wordsArray.getStringValue() );
		
	}

	private void standaloneDynamicsToABC( final Dynamics dynamicsArray ) {
		if (dynamicsArray.sizeOfMfArray() > 0) System.out.print( "!mf!" );
		if (dynamicsArray.sizeOfMpArray() > 0) System.out.print( "!mp!" );
		if (dynamicsArray.sizeOfFArray() > 0) System.out.print( "!f!" );
		if (dynamicsArray.sizeOfFfArray() > 0) System.out.print( "!ff!" );
		if (dynamicsArray.sizeOfFffArray() > 0) System.out.print( "!fff!" );
		if (dynamicsArray.sizeOfPArray() > 0) System.out.print( "!p!" );
		if (dynamicsArray.sizeOfPpArray() > 0) System.out.print( "!pp!" );
		if (dynamicsArray.sizeOfPppArray() > 0) System.out.print( "!ppp!" );
	}

	private void harmonyToABC( final Harmony harmony ) {
		
		System.out.printf( "\"%s", harmony.getRootArray(0).getRootStep().enumValue() );
		if (harmony.getRootArray(0).isSetRootAlter()) {
			final RootAlter ra = harmony.getRootArray( 0 ).getRootAlter();
			final BigDecimal bdra = ra.getBigDecimalValue();
			if (bdra.intValue() == -1) {
				System.out.print( 'b' );
			}
			if (bdra.intValue() == 1) {
				System.out.print( '#' );
			}
		}
		
		if (harmony.getKindArray( 0 ).enumValue() != null) {
			switch (harmony.getKindArray( 0 ).enumValue().intValue()) {
			case Kind.INT_MINOR: 
				System.out.print( 'm' );
				break;
			case Kind.INT_MINOR_SEVENTH: 
				System.out.print( "m7" );
				break;
			case Kind.INT_DIMINISHED:
				System.out.print( "dim" );
				break;
			case Kind.INT_DIMINISHED_SEVENTH:
				System.out.print( "dim7" );
				break;
			case Kind.INT_AUGMENTED:
				System.out.print( "aug" );
				break;
			case Kind.INT_DOMINANT:
				System.out.print( '7' );
				break;
			case Kind.INT_MAJOR_SIXTH:
				System.out.print( "6" );
				break;
			case Kind.INT_MAJOR_SEVENTH:
				System.out.print( "maj7" );
				break;
			case Kind.INT_MAJOR_NINTH:
				System.out.print( '9' );
				break;
			}
		}
		System.out.print( "\"" );
	}

	private void noteToABC( final Note note, final boolean inChord, final int divisions ) {
		
		// tuplets (can apply to rests or notes...)
		if (note.isSetTimeModification()) {

			if (inTuplet) {
				tupletCounter--;
			} else {
				System.out.printf( "(%s", note.getTimeModification().getActualNotes() );
				inTuplet = true;
				tupletCounter = note.getTimeModification().getActualNotes().intValue() - 1;
				// TODO: Fudge!
				if (note.getTimeModification().isSetNormalType()) {
					System.out.print( ":2:2" );
					tupletCounter--;
				}
			}
			if (tupletCounter == 0) {
				inTuplet = false;
			}
			
		} else {
			inTuplet = false;
			tupletCounter = 0;
		}
	
	
		if (note.isSetRest()) {
			if (note.isSetType() && 
				!note.getType().enumValue().toString().equals( "whole" )) {
				System.out.printf( "z%s", noteLengthToABC( note ) );
			} else {
				if (note.isSetDuration()) {
					float restLength = note.getDuration().floatValue() / divisions;
					if (restLength == 1.5) {
						System.out.print( "z3/2" );
					} else {
						if (restLength >=1) {
							System.out.print( "z" +  note.getDuration().intValue() / divisions );
						} else {
							if (restLength == 0.5) {
								System.out.print( "z/" );
							} else {
								if (restLength == 0.25) {
									System.out.print( "z//" );
								}
							}
						}
					}
				} else {
					System.out.print( "Z" );
				}
			}
		}
		
		if (note.isSetGrace()) {
			
		}
		
		// tenuto, accent, staccato
		if (note.sizeOfNotationsArray() > 0) {
			Notations not = note.getNotationsArray( 0 );
			for ( Articulations art : not.getArticulationsArray() ) {
				if (art.sizeOfTenutoArray() > 0) {
					System.out.print( "!tenuto!" );
				}
				if (art.sizeOfAccentArray() > 0) {
					System.out.print(  "!>!" );
				}
				if (art.sizeOfStaccatoArray() > 0) {
					System.out.print( '.' );
				}
			}
		}
				
		if (note.isSetPitch()) {
			
			// beaming - if not beamed then put a space out
			if (!inChord) {
				if (note.sizeOfBeamArray() == 0) {
					System.out.print( ' ' );
				} else {
					if (note.getBeamArray( 0 ).enumValue().toString().equals( "begin" )) {
						System.out.print( ' ' );
					}
				}
			}
			
			// handle slur starts
			if ((note.sizeOfNotationsArray() > 0) && (note.getNotationsArray( 0 ).sizeOfSlurArray() > 0)) {
				startSlursToABC( note.getNotationsArray( 0 ).getSlurArray() );
			}
			
	
			// decorations (ornaments etc.)
			if (note.sizeOfNotationsArray() > 0) {
				decorationsToABC( note.getNotationsArray() );
			}
			
			
			// accidentals
			if (note.isSetAccidental()) {
				System.out.printf( "%s", accidentalToABC( note.getAccidental() ) );
			}
			
			// note itself
			System.out.printf( "%s", notePitchToABC( note.getPitch()) );
			
			// stemless?
			if (note.isSetStem() && note.getStem().enumValue() == Stem.NONE) {
				System.out.print( '0' );
			}
			
			// note duration
			System.out.printf( "%s", noteLengthToABC( note ) );
			
			// handle slur ends
			if ((note.sizeOfNotationsArray() > 0) && (note.getNotationsArray( 0 ).sizeOfSlurArray() > 0)) {
				endSlursToABC( note.getNotationsArray( 0 ).getSlurArray() );
			}
			
			// tie
			if (note.getTieArray().length > 0) {
				if (note.getTieArray( 0 ).getType() == StartStop.START) {
					System.out.print( '-' );
				}
			}
		}
	}

	private void decorationsToABC( final Notations[] notations ) {
		
		for ( Notations nota : notations ) {
			
			if (nota.sizeOfDynamicsArray() > 0) {
				dynamicsToABC( nota.getDynamicsArray() );
			}
			
			if (nota.sizeOfOrnamentsArray() > 0) {
				ornamentsToABC( nota.getOrnamentsArray() );
			}
			
			if (nota.sizeOfFermataArray() > 0) {
				System.out.print( 'H' );
			}
			
		}
		
	}

	private void ornamentsToABC( final Ornaments[] ornamentsArray ) {
		
		for ( Ornaments orna : ornamentsArray ) {
			if (orna.sizeOfTurnArray() > 0) {
				System.out.print( '~' );
			}
			if (orna.sizeOfTrillMarkArray() > 0) {
				System.out.print( 'T' );
			}
			if (orna.sizeOfMordentArray() > 0) {
				System.out.print( 'M' );
			}
			if (orna.sizeOfInvertedMordentArray() > 0) {
				System.out.print( 'P' );
			}
			if (orna.sizeOfInvertedTurnArray() > 0) {
				System.out.print( "!invertedturn!" );
			}
		}
		
	}

	private void dynamicsToABC( final Dynamics[] dynamicsArray ) {
		
		for ( Dynamics dyn : dynamicsArray ) {
			System.out.printf( "!%s!", dyn.toString() );
		}
		
	}

	private void startSlursToABC( final Slur[] slurs ) {

		for ( Slur slu : slurs ) {
			if (slu.getType() == StartStopContinue.START) {
				System.out.print( '(' );
			}
		}
	}

	private void endSlursToABC( final Slur[] slurs ) {

		for ( Slur slu : slurs ) {
			if (slu.getType() == StartStopContinue.STOP) {
				System.out.print( ')' );
			}
		}
	}
	
	private String notePitchToABC( final Pitch pitch ) {
		
		String abcNote = pitch.getStep().toString();
		
		if (pitch.getOctave() > 4) { abcNote = abcNote.toLowerCase(); }
		
		switch (pitch.getOctave()) {
		case 6: abcNote += "'"; break;
		case 7: abcNote += "''"; break;
		case 8: abcNote += "'''"; break;
		case 9: abcNote += "''''"; break;
		case 3: abcNote += ","; break;
		case 2: abcNote += ",,"; break;
		case 1: abcNote += ",,,"; break;
		case 0: abcNote += ",,,,"; break;
		}
		
		return abcNote;
	}

	private String accidentalToABC( final Accidental accidental ) {
		
		if (accidental.enumValue() == Accidental.DOUBLE_SHARP) return "^^";
		if (accidental.enumValue() == Accidental.FLAT) return "_";
		if (accidental.enumValue() == Accidental.FLAT_FLAT) return "__";
		if (accidental.enumValue() == Accidental.NATURAL) return "=";
		if (accidental.enumValue() == Accidental.SHARP) return "^";
		
		return "";
	}

	/**
	 * Assumes L: 1/4
	 * 
	 * @param note
	 * @return
	 */
	private String noteLengthToABC( final Note note ) {
		
		String len = "";
		if (!note.isSetType()) { return len; }
		
		final String durationName = note.getType().getStringValue();
		int numerator = 1, denominator = 1;
		
		if (durationName.contentEquals( "eighth" )) denominator = 2;
		if (durationName.contentEquals( "16th" )) denominator = 4;
		if (durationName.contentEquals( "32nd" )) denominator = 8;
		if (durationName.contentEquals( "half" )) numerator = 2;
		if (durationName.contentEquals( "whole" )) numerator = 4;
		
		if (note.sizeOfDotArray() > 0) {

			if (note.sizeOfDotArray() == 1) {
				if (denominator == 1  && numerator > 1) {
					numerator *= 1.5;
				} else {
					numerator = 3;
					denominator *= 2;
				}
			}
		
		}
				
		if (numerator != 1) len = Integer.toString( numerator );
		
		switch (denominator) {
		case 1:
			break;
		case 2:
			len += "/";
			break;
		default:
			len += "/" + denominator;
		}
		
		return len;		
	}

	private boolean barlineToABC( final Barline barlineArray ) {

		boolean leftOnly = false;

		if (barlineArray.isSetLocation()) {
			//			if (barlineArray.getLocation() == RightLeftMiddle.RIGHT) {
			if (barlineArray.isSetBarStyle()) {

				if (barlineArray.isSetRepeat()) {
					if (barlineArray.getRepeat().getDirection().toString() == "backward") {
						System.out.print( " :" );
					}
				}

				if (barlineArray.isSetBarStyle()) {
					if (barlineArray.getBarStyle().enumValue() == BarStyle.LIGHT_HEAVY ) {
						System.out.print( "|]" );
					}
					if (barlineArray.getBarStyle().enumValue() == BarStyle.HEAVY_LIGHT) {
						System.out.print( "[|" );
					}
					if (barlineArray.getBarStyle().enumValue() == BarStyle.LIGHT_LIGHT) {
						System.out.print( "||" );
					}
					if (barlineArray.getBarStyle().enumValue() == BarStyle.DOTTED ) {
						System.out.print( ".|" );
					}
					if (barlineArray.getBarStyle().enumValue() == BarStyle.REGULAR) {
						System.out.print( "|" );
					}
				} 
			} 
			//			}
			// nth-time bar?
			if (barlineArray.getLocation() == RightLeftMiddle.LEFT) {
				if (barlineArray.isSetRepeat()) {
					if (barlineArray.getRepeat().getDirection().toString() == "forward") {
						System.out.print( ":" );
					}
				}
				if (barlineArray.isSetEnding()) {
					System.out.printf( "[%s ", barlineArray.getEnding().getNumber() );
				}
				leftOnly = true;
			}

			// special case for end of nth-time bar
			if (barlineArray.getLocation() == RightLeftMiddle.RIGHT &&
				barlineArray.isSetEnding() &&
				barlineArray.getEnding().getType().toString().contentEquals( "discontinue" )) {
				System.out.print( " |" );
			}
		}

		return leftOnly;	
	}

}

