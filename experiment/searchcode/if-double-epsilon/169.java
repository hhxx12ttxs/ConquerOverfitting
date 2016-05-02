import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// http://sto-forum.perfectworld.com/showthread.php?t=91474&highlight=CombatLog
// http://sto-forum.perfectworld.com/showthread.php?t=318291&highlight=CombatLog

public class STOResistViewer {
	public static final String LOG_LINE_FIELD_SEPARATOR = ",";
	public static final double EPSILON = 0.0000001;
	public static final String NO_DATA_STRING = "__.__%";
	public static final long UPDATE_INTERVAL = 500;
	public static final long RESIST_DATA_HOLD_INTERVAL = 1000;
	public static final long RESIST_LAST_DATA_HOLD_INTERVAL = 1 * 60 * 1000;
	public static final long SMART_SELECT_DATA_INTERVAL = 1000;
	public static final long TEAM_DETERMINATION_DATA_INTERVAL = 5 * 60 * 1000;
	// currently preventing ALL NPCs from being shown on the drop-down name list
	public static final Pattern INTID_BLACKLIST_REGEX = Pattern.compile("^C\\[.*$"); 
	public static final String CONFIG_FILE_LOCATION = "sto_resistance_viewer.config";
	public static final String VERSION = "v1.3";
	
	/**
	 * @param args
	 */
	public static JFormattedTextField processingIndicator;
	public static void main(String[] args) {
		// First, get the location of the combat log.
		File combatLogFile = getCombatLogFile();
		if ( combatLogFile == null ) {
			return;
		}
		System.out.println( combatLogFile );
		RandomAccessFile inputLog;
		try {
			inputLog = new RandomAccessFile( combatLogFile, "r" );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return;
		}
		
		boolean replayingOldLog = askIfReplayingOldLog();
		
		// TODO: Improve graphics.
		long lastUpdate = 0;
		
		// prep loop
		AllShieldHullDamageGroups movingTotals = new AllShieldHullDamageGroups( -1 );
		AllShieldHullDamageGroups lastTotals = new AllShieldHullDamageGroups( -1 );
		PriorityQueue<UniqueNameAndTime> lastTotalTimes = new PriorityQueue<UniqueNameAndTime>();
		AllDamageRecord movingDamageRecord = new AllDamageRecord( -1 );
		AllDamageRecord longTermDamageRecord = new AllDamageRecord( -1 );
		SortedSet<String> knownNames = new TreeSet<String>();
		MutableTime displayedTime = new MutableTime();
		final ResistDisplayWindow rdw = new ResistDisplayWindow( "Renim@renimalt", movingTotals, 
				lastTotals, movingDamageRecord, longTermDamageRecord, knownNames, displayedTime );
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                rdw.setVisible( true );
            }
        });
		Queue<AllShieldHullDamageGroups> heldResults = new LinkedList<AllShieldHullDamageGroups>();
		Queue<AllDamageRecord> heldDamageRecords = new LinkedList<AllDamageRecord>();
		Queue<AllDamageRecord> heldLongTermDamageRecords = new LinkedList<AllDamageRecord>();
		Set<LogEntry> currEntries = new HashSet<LogEntry>();
		
		if ( !replayingOldLog ) {
			// first read, skip past old junk
			try {
				inputLog.seek( inputLog.length() );
			} catch (IOException e) {
				try { inputLog.close(); } catch (IOException e1) {}
				return;
			}
			readWholeLine(inputLog);  // Discard the first "line" that we get; it could a fragment of a whole line.
		}
		
		LogEntry currEntry = new LogEntry( readWholeLine(inputLog) );
		currEntries.add( currEntry );
		AllShieldHullDamageGroups currResults = new AllShieldHullDamageGroups( currEntry.time );
		AllDamageRecord currDamageRecord = new AllDamageRecord( currEntry.time );
		currDamageRecord.add( currEntry );
		addToKnownNames( knownNames, currEntry );
		
		while ( true ) {
			currEntry = new LogEntry( readWholeLine(inputLog) );
			while ( currEntry.time == currResults.time ) {
				// same "action block".
				addToKnownNames( knownNames, currEntry );
				
				// Add to damage record
				currDamageRecord.add( currEntry );
				
				// check if this entry represents damage - >0 base mag
				// TODO: Improve this!
				if ( currEntry.mag > 0 ) {
					// try to pair this entry up with a previous one in this block.
					LogEntry pair = null;
					for ( LogEntry le : currEntries ) {
						if ( le.isPair( currEntry ) ) {
							pair = le;
							break;
						}
					}
					if ( pair != null ) {
						// we've found a pair
						// we can update our current results and discard the pair
						// First check if this is really a shield/hull pair.
						// TODO: Improve this check.
						if ( currEntry.type.equals("Shield") && !pair.type.equals("Shield") ||
								!currEntry.type.equals("Shield") && pair.type.equals("Shield") ) {
							// Sort into shield vs. hull damage
							boolean currIsShieldEntry = currEntry.type.equals("Shield");
							LogEntry shieldEntry = currIsShieldEntry ? currEntry : pair;
							LogEntry hullEntry = currIsShieldEntry ? pair : currEntry;
							// Extract the target and damage type.
							UniqueName target = new UniqueName( hullEntry.dispTarget, hullEntry.intTarget );
							String damageType = hullEntry.type;
							// Extract the shield/hull mag/base mag
							// shieldEntry.mag and hullEntry.mag report damage taken
							// hullEntry.baseMag is overall base damage of attack
							// shieldEntry.baseMag is the amount of damage that the shield absorbed * (1 - hull resist),
							// or _the damage that the hull would have taken if the shield weren't there_.
							double hullMag = hullEntry.mag;
							double shieldMag = shieldEntry.mag;
							double hullBaseMag = hullEntry.baseMag * hullEntry.mag / (shieldEntry.baseMag + hullEntry.mag);
							double shieldBaseMag = hullEntry.baseMag - hullBaseMag;
							// Add to current results.
							addToAllShieldHullDamageGroups( currResults, target, damageType, 
									shieldMag, shieldBaseMag, 
									hullMag, hullBaseMag );
						}
						currEntries.remove(pair);
					} else {
						// no pair found, put in currentEntries.
						currEntries.add( currEntry );
					}
				} else {
					// not dealing damage, we're not interested in it.
				}
				currEntry = new LogEntry( readWholeLine(inputLog) );
			}
			// We're done with an action block.
			// Update the moving damage record.
			// Add the current damage record to the moving damage record.
			synchronized ( movingDamageRecord ) {
				movingDamageRecord.add( currDamageRecord );
				heldDamageRecords.add( currDamageRecord );
				// Clear old damage records from the moving damage record.
				while ( heldDamageRecords.peek().time < currResults.time - SMART_SELECT_DATA_INTERVAL ) {
					movingDamageRecord.subtract( heldDamageRecords.poll() );
				}
			}
			
			synchronized ( longTermDamageRecord ) {
				longTermDamageRecord.add( currDamageRecord );
				heldLongTermDamageRecords.add( currDamageRecord );
				// Clear old damage records from the moving damage record.
				while ( heldLongTermDamageRecords.peek().time < currResults.time - TEAM_DETERMINATION_DATA_INTERVAL ) {
					longTermDamageRecord.subtract( heldLongTermDamageRecords.poll() );
				}
			}
			
			// Add all non-paired damage entries to the current entries.
			for ( LogEntry le : currEntries ) {
				UniqueName target = new UniqueName( le.dispTarget, le.intTarget );
				String damageType = le.type;
				if ( damageType.equals("Shield") ) {
					// Shield damage.
					addToAllShieldHullDamageGroups( currResults, target, damageType, le.mag, le.baseMag, 0, 0 );
				} else {
					// Hull damage.
					addToAllShieldHullDamageGroups( currResults, target, damageType, 0, 0, le.mag, le.baseMag );
				}
			}
			currEntries.clear();
			
			// Update resist entries.
			// currResults now contains the sum of all the shield/hull damage done over this action block.
			// Add it to the moving total.
			synchronized ( movingTotals ) {				
				for ( UniqueName n : currResults.entities.keySet() ) {
					ShieldHullDamageGroup shdg = currResults.entities.get( n );
					for ( String damageType : shdg.damageByType.keySet() ) {
						DamageResistGroup drg = shdg.damageByType.get( damageType );
						addToAllShieldHullDamageGroups( movingTotals, n, damageType, 
								drg.shieldMag, drg.shieldBaseMag, 
								drg.hullMag, drg.hullBaseMag );
					}
				}

				// add currResults to the queue of held results
				heldResults.add( currResults );
				// clean old results from the moving total.
				synchronized ( lastTotals ) {
					while ( heldResults.peek().time < currResults.time - RESIST_DATA_HOLD_INTERVAL ) {
						AllShieldHullDamageGroups oldResults = heldResults.poll();
						for ( UniqueName n : oldResults.entities.keySet() ) {
							ShieldHullDamageGroup shdg = oldResults.entities.get( n );
							for ( String damageType : shdg.damageByType.keySet() ) {
								DamageResistGroup drg = shdg.damageByType.get( damageType );
								DamageResistGroup oldDrg = 
										addToAllShieldHullDamageGroups( movingTotals, n, damageType, 
												-drg.shieldMag, -drg.shieldBaseMag, 
												-drg.hullMag, -drg.hullBaseMag );
								if ( Math.abs(oldDrg.shieldBaseMag - drg.shieldBaseMag) < EPSILON ||
										Math.abs(oldDrg.hullBaseMag - drg.hullBaseMag) < EPSILON ) {
									ShieldHullDamageGroup lastShdg = lastTotals.entities.get( n );
									if ( lastShdg == null ) {
										lastShdg = new ShieldHullDamageGroup();
										lastTotals.entities.put( n, lastShdg );
									}
									DamageResistGroup lastDrg = lastShdg.damageByType.get( damageType );
									if ( lastDrg == null ) {
										lastDrg = new DamageResistGroup();
										lastShdg.damageByType.put( damageType, lastDrg );
									}
									if ( Math.abs( oldDrg.shieldBaseMag - drg.shieldBaseMag ) < EPSILON 
											&& Math.abs( oldDrg.shieldBaseMag ) > EPSILON ) {
										lastDrg.shieldMag = oldDrg.shieldMag;
										lastDrg.shieldBaseMag = oldDrg.shieldBaseMag;
									}
									if ( Math.abs( oldDrg.hullBaseMag - drg.hullBaseMag ) < EPSILON 
											&& Math.abs( oldDrg.hullBaseMag ) > EPSILON ) {
										lastDrg.hullMag = oldDrg.hullMag;
										lastDrg.hullBaseMag = oldDrg.hullBaseMag;
									}
									UniqueNameAndTime nameAndTime = new UniqueNameAndTime( n, currResults.time );
									lastTotalTimes.remove( nameAndTime );
									lastTotalTimes.add( nameAndTime );
								}
							}
						}
					}
					
					// clear old things from the last total data.
					while ( !lastTotalTimes.isEmpty() && 
							lastTotalTimes.peek().time < currResults.time - RESIST_LAST_DATA_HOLD_INTERVAL ) {
						lastTotals.entities.remove( lastTotalTimes.poll().name );
					}
				}
			}
			// !!! IMPORTANT !!! movingTotal now holds current results.
			long lastTimeInTotal = currResults.time;
			
			// Prep for the next iteration of the outer loop.
			currResults = new AllShieldHullDamageGroups( currEntry.time );
			currDamageRecord = new AllDamageRecord( currEntry.time );
			currDamageRecord.add( currEntry );
			addToKnownNames( knownNames, currEntry );
			currEntries.add( currEntry );
			
			
			long prevTime = currResults.time;
			
			// TODO: improve graphical representation.
			if ( prevTime - lastUpdate >= UPDATE_INTERVAL ) { // update each time 0.5s has gone by in the log
				synchronized ( displayedTime ) {
					displayedTime.time = lastTimeInTotal;
				}
				SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
		                rdw.updateDisplay();
		            }
		        });
				if ( replayingOldLog && lastUpdate != 0 ) {
					try {
						Thread.sleep( prevTime - lastUpdate );
					} catch (InterruptedException e) {
						
					}
				}
				lastUpdate = prevTime;
			}
		}
	}
	
	public static class UniqueNameAndTime implements Comparable<UniqueNameAndTime> {
		public final UniqueName name;
		public final long time;
		
		public UniqueNameAndTime( UniqueName name, long time ) {
			this.name = name;
			this.time = time;
		}
		
		public boolean equals( Object other ) {
			if ( other instanceof UniqueNameAndTime ) {
				return name.equals( ((UniqueNameAndTime) other).name );
			} else {
				return false;
			}
		}

		@Override
		public int compareTo( UniqueNameAndTime other ) {
			return (int) Math.max( Math.min(this.time - other.time, Integer.MIN_VALUE), Integer.MAX_VALUE );
		}
		
		@Override
		public String toString() {
			return "(" + name + ", " + time + ")";
		}
	}
	
	// ask if replaying an old log
	public static boolean askIfReplayingOldLog() {
		// display a simple dialog box
		return JOptionPane.showConfirmDialog( null, 
				"Are you replaying an old combat log?\n" + 
				"(Select Yes if you selected an old combat log and want the resists for that match to be played back to you.\n" + 
				"Select No if you are connecting to the live combat log and want resists to be displayed in real time.)",
				"", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION;
	}
	
	// extract name@handle for players
	// otherwise display name for non-pet non-player-owned NPCs
	public static void addToKnownNames( SortedSet<String> knownNames, LogEntry entry ) {
		synchronized ( knownNames ) {
			if ( !entry.dispOwner.equals("") && !INTID_BLACKLIST_REGEX.matcher( entry.intOwner ).matches() ) {
				if ( entry.intOwner.startsWith("P") ) {
					knownNames.add( extractNameAtHandle( entry.intOwner ) );
				} else {
					knownNames.add( entry.dispOwner );
				}
			}
			if ( !entry.dispSource.equals("") && !INTID_BLACKLIST_REGEX.matcher( entry.intSource ).matches() ) {
				if ( entry.intSource.startsWith("P") ) {
					knownNames.add( extractNameAtHandle( entry.intSource ) );
				} else {
					knownNames.add( entry.dispSource );
				}
			}
			if ( !entry.dispTarget.equals("") && !INTID_BLACKLIST_REGEX.matcher( entry.intTarget ).matches() ) {
				if ( entry.intTarget.startsWith("P") ) {
					knownNames.add( extractNameAtHandle( entry.intTarget ) );
				} else {
					knownNames.add( entry.dispTarget );
				}
			}
		}
	}
	
	public static String extractNameAtHandle( String internalName ) {
		if ( internalName.startsWith("P") ) {
			try {
				return internalName.split(" ", 2)[1].split("]")[0];
			} catch ( Exception e ) {
				return null;
			}
		} else {
			return null;
		}
		
	}
	
	public static class AllDamageRecord {
		public final long time;
		public final Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> damage;
		public final Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> heal;
		
		public AllDamageRecord( long time ) {
			this.time = time;
			this.damage = new HashMap<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>>();
			this.heal = new HashMap<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>>();
		}
		
		public synchronized void add( AllDamageRecord other ) {
			synchronized (other) {
				combine( this.damage, other.damage, true );
				combine( this.heal, other.heal, true );
			}
			this.clean();
		}
		
		public synchronized void subtract( AllDamageRecord other ) {
			synchronized (other) {
				combine( this.damage, other.damage, false );
				combine( this.heal, other.heal, false );
			}
			this.clean();
		}
		
		public synchronized void add( LogEntry entry ) {
			if ( entry.type.equals("Shield") ) {
				this.add( new UniqueName(entry.dispOwner, entry.intOwner),
						new UniqueName(entry.dispTarget, entry.intTarget),
						entry.type, entry.mag, 0, 0, 0 );
				if ( !entry.intOwner.equals(entry.intSource) ) {
					this.add( new UniqueName(entry.dispSource, entry.intSource),
							new UniqueName(entry.dispTarget, entry.intTarget),
							entry.type, 0, entry.mag, 0, 0 );
				}
			} else {
				this.add( new UniqueName(entry.dispOwner, entry.intOwner),
						new UniqueName(entry.dispTarget, entry.intTarget),
						entry.type, 0, 0, entry.mag, 0 );
				if ( !entry.intOwner.equals(entry.intSource) ) {
					this.add( new UniqueName(entry.dispSource, entry.intSource),
							new UniqueName(entry.dispTarget, entry.intTarget),
							entry.type, 0, 0, 0, entry.mag );
				}
			}
			this.clean();
		}
		
		// positive is damage.  negative is heal.
		public synchronized void add( UniqueName source, UniqueName target, String damageType, 
				double shieldMag, double petShieldMag, double hullMag, double petHullMag ) {
			Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> shieldMap = shieldMag > 0 ? damage : heal;
			Map<UniqueName, Map<String, ShieldHullDamage>> shieldSourceMap = shieldMap.get( source );
			if ( shieldSourceMap == null ) {
				shieldSourceMap = new HashMap<UniqueName, Map<String, ShieldHullDamage>>();
				shieldMap.put( source, shieldSourceMap );
			}
			Map<String, ShieldHullDamage> shieldSourceTargetMap = shieldSourceMap.get( target );
			if ( shieldSourceTargetMap == null ) {
				shieldSourceTargetMap = new HashMap<String, ShieldHullDamage>();
				shieldSourceMap.put( target, shieldSourceTargetMap );
			}
			ShieldHullDamage shieldSHD = shieldSourceTargetMap.get( damageType );
			if ( shieldSHD == null ) {
				shieldSHD = new ShieldHullDamage();
				shieldSourceTargetMap.put( damageType, shieldSHD );
			}
			shieldSHD.shield += shieldMag;
			Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> petShieldMap = petShieldMag > 0 ? damage : heal;
			Map<UniqueName, Map<String, ShieldHullDamage>> petShieldSourceMap = petShieldMap.get( source );
			if ( petShieldSourceMap == null ) {
				petShieldSourceMap = new HashMap<UniqueName, Map<String, ShieldHullDamage>>();
				petShieldMap.put( source, petShieldSourceMap );
			}
			Map<String, ShieldHullDamage> petShieldSourceTargetMap = petShieldSourceMap.get( target );
			if ( petShieldSourceTargetMap == null ) {
				petShieldSourceTargetMap = new HashMap<String, ShieldHullDamage>();
				petShieldSourceMap.put( target, petShieldSourceTargetMap );
			}
			ShieldHullDamage petShieldSHD = petShieldSourceTargetMap.get( damageType );
			if ( petShieldSHD == null ) {
				petShieldSHD = new ShieldHullDamage();
				petShieldSourceTargetMap.put( damageType, petShieldSHD );
			}
			petShieldSHD.shield += petShieldMag;
			Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> hullMap = hullMag > 0 ? damage : heal;
			Map<UniqueName, Map<String, ShieldHullDamage>> hullSourceMap = hullMap.get( source );
			if ( hullSourceMap == null ) {
				hullSourceMap = new HashMap<UniqueName, Map<String, ShieldHullDamage>>();
				hullMap.put( source, hullSourceMap );
			}
			Map<String, ShieldHullDamage> hullSourceTargetMap = hullSourceMap.get( target );
			if ( hullSourceTargetMap == null ) {
				hullSourceTargetMap = new HashMap<String, ShieldHullDamage>();
				hullSourceMap.put( target, hullSourceTargetMap );
			}
			ShieldHullDamage hullSHD = hullSourceTargetMap.get( damageType );
			if ( hullSHD == null ) {
				hullSHD = new ShieldHullDamage();
				hullSourceTargetMap.put( damageType, hullSHD );
			}
			hullSHD.hull += hullMag;
			Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> petHullMap = petHullMag > 0 ? damage : heal;
			Map<UniqueName, Map<String, ShieldHullDamage>> petHullSourceMap = hullMap.get( source );
			if ( petHullSourceMap == null ) {
				petHullSourceMap = new HashMap<UniqueName, Map<String, ShieldHullDamage>>();
				petHullMap.put( source, petHullSourceMap );
			}
			Map<String, ShieldHullDamage> petHullSourceTargetMap = petHullSourceMap.get( target );
			if ( petHullSourceTargetMap == null ) {
				petHullSourceTargetMap = new HashMap<String, ShieldHullDamage>();
				petHullSourceMap.put( target, petHullSourceTargetMap );
			}
			ShieldHullDamage petHullSHD = petHullSourceTargetMap.get( damageType );
			if ( petHullSHD == null ) {
				petHullSHD = new ShieldHullDamage();
				petHullSourceTargetMap.put( damageType, petHullSHD );
			}
			petHullSHD.hull += petHullMag;
			this.clean();
		}
		
		// first = first +/- second
		private static void combine( Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> first,
				Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> second, boolean add ) {
			for ( UniqueName source : second.keySet() ) {
				Map<UniqueName, Map<String, ShieldHullDamage>> secondSourceMap = second.get( source );
				Map<UniqueName, Map<String, ShieldHullDamage>> firstSourceMap = first.get( source );
				if ( firstSourceMap == null ) {
					firstSourceMap = new HashMap<UniqueName, Map<String, ShieldHullDamage>>();
					first.put( source, firstSourceMap );
				}
				for ( UniqueName target : secondSourceMap.keySet() ) {
					Map<String, ShieldHullDamage> secondSourceTargetMap = secondSourceMap.get( target );
					Map<String, ShieldHullDamage> firstSourceTargetMap = firstSourceMap.get( target );
					if ( firstSourceTargetMap == null ) {
						firstSourceTargetMap = new HashMap<String, ShieldHullDamage>();
						firstSourceMap.put( target, firstSourceTargetMap );
					}
					for ( String damageType : secondSourceTargetMap.keySet() ) {
						ShieldHullDamage secondSHD = secondSourceTargetMap.get( damageType );
						ShieldHullDamage firstSHD = firstSourceTargetMap.get( damageType );
						if ( firstSHD == null ) {
							firstSHD = new ShieldHullDamage();
							firstSourceTargetMap.put( damageType, firstSHD );
						}
						if ( add ) {
							firstSHD.shield += secondSHD.shield;
							firstSHD.petShield += secondSHD.petShield;
							firstSHD.hull += secondSHD.hull;
							firstSHD.petHull += secondSHD.petHull;
						} else {
							firstSHD.shield -= secondSHD.shield;
							firstSHD.petShield -= secondSHD.petShield;
							firstSHD.hull -= secondSHD.hull;
							firstSHD.petHull -= secondSHD.petHull;
						}
					}
				}
			}
		}
		
		private void clean() {
			clean( damage );
			clean( heal );
		}
		
		private static void clean( Map<UniqueName, Map<UniqueName, Map<String, ShieldHullDamage>>> map ) {
			Iterator<UniqueName> sources = map.keySet().iterator();
			while ( sources.hasNext() ) {
				UniqueName source = sources.next();
				Map<UniqueName, Map<String, ShieldHullDamage>> sourceMap = map.get( source );
				Iterator<UniqueName> targets = sourceMap.keySet().iterator();
				while ( targets.hasNext() ) {
					UniqueName target = targets.next();
					Map<String, ShieldHullDamage> sourceTargetMap = sourceMap.get( target );
					Iterator<String> damageTypes = sourceTargetMap.keySet().iterator();
					while ( damageTypes.hasNext() ) {
						String damageType = damageTypes.next();
						ShieldHullDamage shd = sourceTargetMap.get( damageType );
						if ( Math.abs( shd.shield ) < EPSILON && Math.abs( shd.petShield ) < EPSILON &&
								Math.abs( shd.hull ) < EPSILON && Math.abs( shd.petHull ) < EPSILON ) {
							damageTypes.remove();
						}
					}
					if ( sourceTargetMap.isEmpty() ) {
						targets.remove();
					}
				}
				if ( sourceMap.isEmpty() ) {
					sources.remove();
				}
			}
		}
	}
	
	public static class ShieldHullDamage {
		double shield;
		double petShield;
		double hull;
		double petHull;
	}
	
	public static class MutableTime {
		long time;
	}
	
	public static class Team {
		public final Set<UniqueName> members;
		public final Map<Team, Boolean> relations; // true = ally, false = enemy
		
		public Team() {
			members = new HashSet<UniqueName>();
			relations = new HashMap<Team, Boolean>();
			this.relations.put( this, true );
		}
		
		private static class DamageHeal {
			double damage;
			double heal; // REMEMBER heals are NEGATIVE values
		}
		
		private static DamageHeal extractDamageHealToTeam( AllDamageRecord allDR, Set<UniqueName> members, UniqueName name ) {
			DamageHeal result = new DamageHeal();
			if ( allDR.damage.containsKey( name ) ) {
				Map<UniqueName, Map<String, ShieldHullDamage>> nameDamage = allDR.damage.get( name );
				Set<UniqueName> targets = new HashSet<UniqueName>( nameDamage.keySet() );
				targets.retainAll( members );
				for ( UniqueName target : targets ) {
					Map<String, ShieldHullDamage> nameTargetDamageMap = nameDamage.get( target );
					for ( String damageType : nameTargetDamageMap.keySet() ) {
						ShieldHullDamage shd = nameTargetDamageMap.get( damageType );
						result.damage += shd.shield + shd.hull; // not counting pet damage so that we don't double-count
					}
				}
			}
			if ( allDR.heal.containsKey( name ) ) {
				Map<UniqueName, Map<String, ShieldHullDamage>> nameHeal = allDR.heal.get( name );
				Set<UniqueName> targets = new HashSet<UniqueName>( nameHeal.keySet() );
				targets.retainAll( members );
				for ( UniqueName target : targets ) {
					Map<String, ShieldHullDamage> nameTargetHealMap = nameHeal.get( target );
					for ( String healType : nameTargetHealMap.keySet() ) {
						ShieldHullDamage shd = nameTargetHealMap.get( healType );
						result.heal += shd.shield + shd.hull;
					}
				}
			}
			return result;
		}
		
		public boolean isLikelyMember( AllDamageRecord allDR, UniqueName name ) {
			double damageBalance = 0;
			DamageHeal dh = extractDamageHealToTeam( allDR, this.members, name );
			damageBalance += dh.damage + dh.heal;
			Set<UniqueName> temp = new HashSet<UniqueName>();
			temp.add( name );
			for ( UniqueName thisTeamMember : this.members ) {
				DamageHeal ndh = extractDamageHealToTeam( allDR, temp, thisTeamMember );
				damageBalance += ndh.damage;
				damageBalance += ndh.heal;
			}
			return damageBalance < 0;
		}
		
		public boolean isLikelyAlly( AllDamageRecord allDR, Team otherTeam ) {
			double damageBalance = 0; // positive means we exchanged damage more than heal
			for ( UniqueName name : otherTeam.members ) {
				DamageHeal ndh = extractDamageHealToTeam( allDR, this.members, name );
				damageBalance += ndh.damage;
				damageBalance += ndh.heal;
			}
			for ( UniqueName name : this.members ) {
				DamageHeal ndh = extractDamageHealToTeam( allDR, otherTeam.members, name );
				damageBalance += ndh.damage;
				damageBalance += ndh.heal;
			}
			return damageBalance <= 0;
		}
		
		public String toString() {
			return "Team" + members; 
		}
	}
	
	public static class AboutWindow extends JFrame {
		
		public static final AboutWindow WINDOW = new AboutWindow(); 
		
		private AboutWindow() {
			super();
			
			this.setTitle("About STO Resist Viewer " + VERSION);
			this.setAlwaysOnTop(true);
			
			JEditorPane aboutField = new JEditorPane();
			aboutField.setContentType( "text/html" );
			aboutField.setBackground( UIManager.getColor( "JLabel.background" ) );
			aboutField.setEditable( false );
			aboutField.setText("<html><b>STO Resist Viewer</b><br> "+
			"<b>Version:</b> " + VERSION + "<br><br>" +
			"<b>Created by:</b> Renimalt<br><br>" + 
			"<b>Special thanks to:</b><br>" +
			"@dontdrunkimshoot<br>" +
			"@EmoeJoe<br>" +
			"@sifix01<br>" +
			"@thegrimcorsair<br>" + 
			"@VegetableO<br>" +
			"@usshannibal<br>" +
			"And all of my beta testers!");
			
			this.add( aboutField );
			this.pack();
		}
	}
	
	public static class ResistDisplayWindow extends JFrame {
		private static final long serialVersionUID = 1L;
		public static final String CLEAR_KNOWN_NAMES_TEXT = "<Clear name list>";
		public static final int DISPLAY_FIELD_ALIGNMENT = JTextField.RIGHT;
		
		public static enum TeamAssistType {
			SELECT_ALLY_TAKING_FIRE("ATF", "Displays the ally taking the most damage." ),
			SELECT_ALLY_LOWEST_RESIST("ALR", "Displays the ally with lowest known average resists." ),
			SELECT_ENEMY_TAKING_FIRE("ETF", "Displays the enemy taking the most damage." ),
			SELECT_ENEMY_LOWEST_RESIST("ELR", "Displays the enemy with the lowest known average resists.");
			
			private String displayString;
			private String toolTipString;
			
			private TeamAssistType( String displayString, String toolTipString ) {
				this.displayString = displayString;
				this.toolTipString = toolTipString;
			}
			
			public String getDisplayString() {
				return displayString;
			}
			
			public String getToolTipString() {
				return toolTipString;
			}
			
			@Override
			public String toString() {
				return displayString;
			}
		}
		
		public static enum MonitoredDamageType {
			PHASER("Phaser", "Pha", Color.ORANGE),
			DISRUPTOR("Disruptor", "Dis", Color.decode("#5ffa32")),
			PLASMA("Plasma", "Pla", Color.decode("#6EFBF3")),
			TETRYON("Tetryon", "Tet", Color.decode("#2898f0")),
			POLARON("Polaron", "Pol", Color.decode("#7863ec")),
			ANTIPROTON("AntiProton", "AP", Color.RED),
			KINETIC("Kinetic", "Kin", Color.DARK_GRAY);
			
			public final String logName;
			public final String abbreviation;
			public final Color displayColor;
			
			private MonitoredDamageType( String logName, String abbreviation, Color displayColor ) {
				this.logName = logName;
				this.abbreviation = abbreviation;
				this.displayColor = displayColor;
			}
			
			@Override
			public String toString() {
				return logName;
			}
		}
		
		public class ResistDisplayPair {
			public final JFormattedTextField srd;
			public final JFormattedTextField hrd;
			
			public ResistDisplayPair( JFormattedTextField srd, JFormattedTextField hrd ) {
				this.srd = srd;
				this.hrd = hrd;
			}
		}
		
		private class PermanentNameSelectionWindow extends JFrame {
			private final ResistDisplayWindow rdw;
			
			private final JButton addNameButton;
			private final JButton removeNameButton;
			private final DefaultListModel nameListModel;
			private final JList nameList;
			private final JTextField addNameField;
			private final AddNameButtonListener anbl;
			private final RemoveNameButtonListener rnbl;
			
			public PermanentNameSelectionWindow( ResistDisplayWindow rdw ) {
				super();
				
				this.rdw = rdw;
				
				this.setLayout( new BorderLayout() );
				this.setTitle("Set permanent names...");
				this.setAlwaysOnTop( true );
				
				nameListModel = new DefaultListModel();
				anbl = new AddNameButtonListener();
				rnbl = new RemoveNameButtonListener();
				
				nameList = new JList( nameListModel );
				nameList.addListSelectionListener( rnbl );
				JScrollPane nameListScrollPane = new JScrollPane( nameList );
				
				addNameButton = new JButton("Add");
				addNameButton.addActionListener( anbl );
				addNameButton.setEnabled( false );
				
				addNameField = new JTextField();
				addNameField.getDocument().addDocumentListener( anbl );
				
				removeNameButton = new JButton("Remove");
				removeNameButton.addActionListener( rnbl );
				removeNameButton.setEnabled( false );
				
				JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
				buttonPane.add(removeNameButton);
				buttonPane.add(Box.createHorizontalStrut(5));
				buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
				buttonPane.add(Box.createHorizontalStrut(5));
				buttonPane.add(addNameField);
				buttonPane.add(addNameButton);
				
				JLabel infoLabel = new JLabel("<html>Permanent names stay in the name list <br>" +
						"even after you clear it.</html>");
				
				this.add( infoLabel, BorderLayout.PAGE_START );
				this.add( nameListScrollPane, BorderLayout.CENTER );
				this.add( buttonPane, BorderLayout.PAGE_END );
				this.pack();
			}
			
			private class RemoveNameButtonListener implements ActionListener, ListSelectionListener {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Object[] selections = nameList.getSelectedValues();
					
					synchronized( permanentNames ) {
						for ( Object name : selections ) {
							nameListModel.removeElement( name );
							permanentNames.remove( name );
						}
						// Don't need to update the display window; removal of a permanent name
						// still leaves it in the name list until the name list is cleared.
					}
					
					if ( nameListModel.getSize() == 0 ) {
						removeNameButton.setEnabled( false );
					}
				}

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					if ( arg0.getValueIsAdjusting() == false ) {
						if ( nameList.getSelectedValues().length > 0 ) {
							removeNameButton.setEnabled( true );
						} else {
							removeNameButton.setEnabled( false );
						}
					}
				}
				
			}
			
			private class AddNameButtonListener implements ActionListener, DocumentListener {

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					handlePossiblyEmptyField();
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					addNameButton.setEnabled( true );
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					handlePossiblyEmptyField();
				}
				
				public void handlePossiblyEmptyField() {
					if ( addNameField.getDocument().getLength() <= 0 ) {
						addNameButton.setEnabled( false );
					} else {
						addNameButton.setEnabled( true );
					}
				}

				@Override
				public void actionPerformed(ActionEvent arg0) {
					String name = addNameField.getText();
					addNameField.setText("");
					if ( !name.equals("") && !permanentNames.contains( name ) ) {
						synchronized( permanentNames ) {
							permanentNames.add( name );
							int i = 0;
							while ( i < nameListModel.getSize() ) {
								if ( name.compareTo((String) nameListModel.getElementAt( i )) < 0) {
									nameListModel.insertElementAt( name, i );
									break;
								}
								i++;
							}
							if ( i == nameListModel.getSize() ) {
								nameListModel.addElement( name );
							}
							rdw.updateDisplay();
						}
					}
				}
				
			}
		}
		
		private String target;
		private String selfName;
		private boolean smartSelect;
		private boolean teamAssist;
		private TeamAssistType teamAssistType;
		private boolean dontShowNPCs;
		
		public final AllShieldHullDamageGroups allSHDG;
		public final AllShieldHullDamageGroups lastSHDG;
		public final AllDamageRecord allDR;
		public final AllDamageRecord longTermDR;
		public final SortedSet<String> knownNames;
		public final MutableTime currTime;
		public final SortedSet<String> permanentNames;
		public final PermanentNameSelectionWindow permanentNameSelectionWindow;
		
		private final JFormattedTextField timeField;
		private final JComboBox targetField;
		private final DefaultComboBoxModel targetFieldModel;
 		private final JComboBox selfNameField;
		private final DefaultComboBoxModel selfNameFieldModel;
		
		private final Map<MonitoredDamageType, ResistDisplayPair> resistDisplays;
		
		public ResistDisplayWindow( String targetName, AllShieldHullDamageGroups allSHDG,
				AllShieldHullDamageGroups lastSHDG, AllDamageRecord allDR, AllDamageRecord longTermDR,
				SortedSet<String> names, MutableTime currTime ) {
			super();
			target = targetName;
			selfName = "";
			smartSelect = false;
			teamAssist = false;
			teamAssistType = TeamAssistType.SELECT_ENEMY_TAKING_FIRE;
			dontShowNPCs = false;
			this.allSHDG = allSHDG;
			this.lastSHDG = lastSHDG;
			this.allDR = allDR;
			this.longTermDR = longTermDR;
			this.knownNames = names;
			this.currTime = currTime;
			this.permanentNames = new TreeSet<String>();
			this.permanentNameSelectionWindow = new PermanentNameSelectionWindow( this );
			
			synchronized (knownNames) {
				knownNames.add( target );
				knownNames.addAll( permanentNames );
			}
			
			// hack to get better colors for top box when SS is on
			UIManager.put("ComboBox.disabledForeground", Color.GRAY);    
			
			this.setTitle( VERSION );
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setAlwaysOnTop(true);
			this.setLayout(new GridBagLayout());
			GridBagConstraints gbConstraints = new GridBagConstraints();
			gbConstraints.fill = GridBagConstraints.BOTH;
			gbConstraints.anchor = GridBagConstraints.CENTER;
			Container compContainer = this.getContentPane();
			
			// BEGIN Time/Options Bar Layout
			JPanel topPanel = new JPanel( new GridBagLayout() );
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 0;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 6;
			compContainer.add(topPanel, gbConstraints);
			
			timeField = new JFormattedTextField( new SimpleDateFormat( "MMM dd yyyy, HH:mm:ss" ) );
			timeField.setValue( currTime.time );
			timeField.setEditable( false );
			timeField.setHorizontalAlignment( JTextField.CENTER );
			timeField.setToolTipText( "The time in the combat log that the viewer is currently showing." );
			gbConstraints.gridx = 1;
			gbConstraints.gridy = 0;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 5;
			gbConstraints.weightx = 4.5/6;
			topPanel.add(timeField, gbConstraints);
			
			// The options menu.
			// TODO: Resolve problems with menu bar not showing sometimes.  Tweak values?
			JMenuBar optionsMenuBar = new JMenuBar();
			optionsMenuBar.setMargin( new Insets(0, 0, 0, 0) );
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 0;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 1;
			gbConstraints.weightx = 1.5/6;
			topPanel.add(optionsMenuBar, gbConstraints);
			gbConstraints.weightx = 0;
			
			JMenu optionsMenu = new JMenu( "Menu" );
			optionsMenuBar.add( optionsMenu );
			optionsMenu.setToolTipText( "Show options" );
			
			JCheckBoxMenuItem dontShowNPCsMenuItem = new JCheckBoxMenuItem("Don't show NPCs");
			optionsMenu.add(dontShowNPCsMenuItem);
			dontShowNPCsMenuItem.setToolTipText( "NPCs will not be shown in SS and TA modes." );
			dontShowNPCsMenuItem.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					dontShowNPCs = arg0.getStateChange() == ItemEvent.SELECTED;
				}
				
			});
			
			optionsMenu.add( new JSeparator( SwingConstants.HORIZONTAL ) );
			
			JMenuItem setPermanentNamesMenuItem = new JMenuItem("Set permanent names");
			optionsMenu.add( setPermanentNamesMenuItem );
			setPermanentNamesMenuItem.setToolTipText( "Permanent names will not be cleared from the name list.");
			setPermanentNamesMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// Pull up the permanent-names window.
					synchronized (permanentNameSelectionWindow) {
						permanentNameSelectionWindow.setVisible( true );
					}
				}
				
			});
			
			optionsMenu.add( new JSeparator( SwingConstants.HORIZONTAL ) );
			
			JMenuItem aboutMenuItem = new JMenuItem("About STO Resist Viewer");
			optionsMenu.add( aboutMenuItem );
			aboutMenuItem.setToolTipText( "Display information about the STO Resist Viewer." );
			aboutMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// Pull up the about window.
					synchronized( AboutWindow.WINDOW ) {
						AboutWindow.WINDOW.setVisible( true );
					}
				}
				
			});
			// END Time/Options Bar Layout
			
			final ResistDisplayWindow thisWindow = this;
			ActionListener targetAndSelfNameFieldActionListener = new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JComboBox source = (JComboBox) e.getSource();
					synchronized ( knownNames ) {
						if ( source.isEnabled() ) {
							String chosenName = (String) source.getSelectedItem();
							chosenName = chosenName == null ? "" : chosenName;
							if ( chosenName.equals( CLEAR_KNOWN_NAMES_TEXT ) ) {
								// Clear the known name list.
								target = "";
								selfName = "";
								knownNames.clear();
								knownNames.addAll( permanentNames );
								targetFieldModel.removeAllElements();
								targetFieldModel.addElement( CLEAR_KNOWN_NAMES_TEXT );
								targetField.setSelectedItem( null );
								selfNameFieldModel.removeAllElements();
								selfNameFieldModel.addElement( CLEAR_KNOWN_NAMES_TEXT );
								selfNameField.setSelectedItem( null );
								for ( String s : knownNames ) {
									if ( !s.equals("") ) {
										targetFieldModel.addElement( s );
										selfNameFieldModel.addElement( s );
									}
								}
							} else if ( !chosenName.equals("") && !knownNames.contains( chosenName ) ) {
								// The user entered a name that isn't in the list.
								// Add it to the list, and set it as either target or self.
								knownNames.add( chosenName );
								int i = 1;
								while ( i < targetFieldModel.getSize() ) {
									if ( chosenName.compareTo((String) targetFieldModel.getElementAt( i )) < 0) {
										targetFieldModel.insertElementAt( chosenName, i );
										selfNameFieldModel.insertElementAt( chosenName, i );
										break;
									}
									i++;
								}
								if ( i == targetFieldModel.getSize() ) {
									targetFieldModel.addElement( chosenName );
									selfNameFieldModel.addElement( chosenName );
								}
								if ( source == targetField ) {
									target = chosenName;
								} else {
									selfName = chosenName;
								}
							} else {
								// The user entered a name that is in the list.
								// Set it as either target or self.
								if ( source == targetField ) {
									target = chosenName;
								} else {
									selfName = chosenName;
								}
							}
							thisWindow.updateDisplay();
						}
					}
				}

			};
			
			// java 6 doesn't allow parameterization of this :|
			targetFieldModel = new DefaultComboBoxModel();
			targetFieldModel.addElement( CLEAR_KNOWN_NAMES_TEXT );
			synchronized (knownNames) {
				for ( String s : knownNames ) {
					if ( !s.equals("") ) {
						targetFieldModel.addElement( s );
					}
				}
			}
			targetField = new JComboBox( targetFieldModel );
			targetField.setToolTipText( "The currently-displayed resist viewer target." );
			targetField.setSelectedItem( target );
			targetField.setEditable( true );
			targetField.setEnabled( true );
			((JTextField) targetField.getEditor().getEditorComponent()).setDisabledTextColor( Color.GRAY );
			targetField.addActionListener( targetAndSelfNameFieldActionListener );
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 1;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 6;
			compContainer.add(targetField, gbConstraints);
			
			// BEGIN Team Assist stuff
			final JPanel teamAssistOptionsPanel = new JPanel( new GridLayout( 2, 2 ) );
			final ButtonGroup teamAssistOptionsButtonGroup = new ButtonGroup();
			final Map<TeamAssistType, JRadioButton> teamAssistOptionsButtons = 
					new HashMap<TeamAssistType, JRadioButton>();
			for ( final TeamAssistType tat : TeamAssistType.values() ) {
				JRadioButton button = new JRadioButton( tat.getDisplayString() );
				button.setToolTipText ( tat.getToolTipString() );
				button.setEnabled( false );
				button.addActionListener( new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						if ( ( (JRadioButton) arg0.getSource() ).isSelected() ) {
							teamAssistType = tat;
						}
						thisWindow.updateDisplay();
					}
					
				});
				teamAssistOptionsButtons.put( tat, button );
				teamAssistOptionsPanel.add( button );
				teamAssistOptionsButtonGroup.add( button );
			}
			// start with the initial button selecting being the enemy taking the most fire
			teamAssistOptionsButtons.get( TeamAssistType.SELECT_ENEMY_TAKING_FIRE ).setSelected( true );
			gbConstraints.gridx = 2;
			gbConstraints.gridy = 3;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 4;
			gbConstraints.anchor = GridBagConstraints.CENTER;
			compContainer.add(teamAssistOptionsPanel, gbConstraints);
			
			JLabel teamAssistLabel = new JLabel("TA?");
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 3;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 1;
			gbConstraints.anchor = GridBagConstraints.EAST;
			compContainer.add(teamAssistLabel, gbConstraints);
			final JCheckBox teamAssistCheckBox = new JCheckBox();
			teamAssistCheckBox.setToolTipText( "Check this box to enable Team Assist mode. " +
					"In Team Assist, the viewer automatically figures out teams from the perspective of the SS name " +
					"and focuses on the target you specify using the options to the right.  " +
					"Requires that SS be enabled." );
			teamAssistCheckBox.setEnabled( false );
			gbConstraints.gridx = 1;
			gbConstraints.gridy = 3;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 1;
			gbConstraints.anchor = GridBagConstraints.WEST;
			compContainer.add(teamAssistCheckBox, gbConstraints);
			teamAssistCheckBox.setSelected( false );
			teamAssistCheckBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					teamAssist = arg0.getStateChange() == ItemEvent.SELECTED;
					for ( JRadioButton b : teamAssistOptionsButtons.values() ) {
						b.setEnabled( teamAssist ); // if the state changes, we know that SS == true
					}
					thisWindow.updateDisplay();
				}
				
			});
			// END Team Assist stuff.
			
			// BEGIN Smart Select stuff.
			JLabel smartSelectLabel = new JLabel("SS?");
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 2;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 1;
			gbConstraints.anchor = GridBagConstraints.EAST;
			compContainer.add(smartSelectLabel, gbConstraints);
			JCheckBox smartSelectCheckBox = new JCheckBox();
			smartSelectCheckBox.setToolTipText( "Check this box to enable Stupid Select. " +
					"By itself, Stupid Select causes the viewer to autofocus on the person " +
					"that is being dealt the most damage by the name in the box to the right.  " +
					"Team Assist will change the focused target." );
			gbConstraints.gridx = 1;
			gbConstraints.gridy = 2;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 1;
			gbConstraints.anchor = GridBagConstraints.WEST;
			compContainer.add(smartSelectCheckBox, gbConstraints);
			smartSelectCheckBox.setSelected( false );
			smartSelectCheckBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					smartSelect = arg0.getStateChange() == ItemEvent.SELECTED;
					selfNameField.setEnabled( smartSelect );
					targetField.setEnabled( !smartSelect );
					if ( smartSelect ) {
						((JTextField) targetField.getEditor().getEditorComponent()).setBackground( UIManager.getColor("ComboBox.disabledBackground") );
						((JTextField) selfNameField.getEditor().getEditorComponent()).setBackground( Color.WHITE );
					} else {
						((JTextField) targetField.getEditor().getEditorComponent()).setBackground( Color.WHITE );
						((JTextField) selfNameField.getEditor().getEditorComponent()).setBackground( UIManager.getColor("ComboBox.disabledBackground") );
					}
					teamAssistCheckBox.setEnabled( smartSelect );
					for ( JRadioButton b : teamAssistOptionsButtons.values() ) {
						b.setEnabled( smartSelect && teamAssist );
					}
					thisWindow.updateDisplay();
				}
				
			});
			selfNameFieldModel = new DefaultComboBoxModel();
			selfNameFieldModel.addElement( CLEAR_KNOWN_NAMES_TEXT );
			synchronized (knownNames) {
				for ( String s : knownNames ) {
					if ( !s.equals("") ) {
						selfNameFieldModel.addElement( s );
					}
				}
			}
			selfNameField = new JComboBox( selfNameFieldModel );
			selfNameField.setToolTipText( "The current Stupid Select focus." );
			selfNameField.setSelectedItem( null );
			selfNameField.setEditable( true );
			selfNameField.setEnabled( false );
			((JTextField) selfNameField.getEditor().getEditorComponent()).setDisabledTextColor( Color.GRAY );
			((JTextField) selfNameField.getEditor().getEditorComponent()).setBackground( UIManager.getColor("ComboBox.disabledBackground") );
			selfNameField.addActionListener( targetAndSelfNameFieldActionListener );
			gbConstraints.gridx = 2;
			gbConstraints.gridy = 2;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 4;
			gbConstraints.anchor = GridBagConstraints.EAST;
			compContainer.add(selfNameField, gbConstraints);
			// END Smart Select stuff
			
			JFormattedTextField tempLabel = new JFormattedTextField("Resist");
			tempLabel.setHorizontalAlignment(JTextField.CENTER);
			processingIndicator = tempLabel;
			tempLabel.setEditable(false);
			gbConstraints.gridx = 0;
			gbConstraints.gridy = 4;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 2;
			gbConstraints.weightx = 0;
			compContainer.add(tempLabel, gbConstraints);
			tempLabel = new JFormattedTextField("Shield");
			tempLabel.setHorizontalAlignment(JTextField.CENTER);
			tempLabel.setEditable(false);
			gbConstraints.gridx = 2;
			gbConstraints.gridy = 4;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 2;
			gbConstraints.weightx = 0.5;
			compContainer.add(tempLabel, gbConstraints);
			tempLabel = new JFormattedTextField("Hull");
			tempLabel.setHorizontalAlignment(JTextField.CENTER);
			tempLabel.setEditable(false);
			gbConstraints.gridx = 4;
			gbConstraints.gridy = 4;
			gbConstraints.gridheight = 1;
			gbConstraints.gridwidth = 2;
			gbConstraints.weightx = 0.5;
			compContainer.add(tempLabel, gbConstraints);
			gbConstraints.weightx = 0;
			
			this.resistDisplays = new HashMap<MonitoredDamageType, ResistDisplayPair>();
			for ( MonitoredDamageType dt : MonitoredDamageType.values() ) {
				gbConstraints.gridy += 1;
				JFormattedTextField colorBox = new JFormattedTextField("");
				colorBox.setEditable(false);
				colorBox.setBackground(dt.displayColor);
				gbConstraints.gridx = 0;
				gbConstraints.gridheight = 1;
				gbConstraints.gridwidth = 1;
				compContainer.add(colorBox, gbConstraints);
				JFormattedTextField abbrDisplay = new JFormattedTextField(dt.abbreviation);
				abbrDisplay.setColumns(2);
				abbrDisplay.setEditable(false);
				gbConstraints.gridx = 1;
				gbConstraints.gridheight = 1;
				gbConstraints.gridwidth = 1;
				compContainer.add(abbrDisplay, gbConstraints);
				JFormattedTextField srd = createResistDisplayField();
				gbConstraints.gridx = 2;
				gbConstraints.gridheight = 1;
				gbConstraints.gridwidth = 2;
				compContainer.add(srd, gbConstraints);
				JFormattedTextField hrd = createResistDisplayField();
				gbConstraints.gridx = 4;
				gbConstraints.gridheight = 1;
				gbConstraints.gridwidth = 2;
				compContainer.add(hrd, gbConstraints);
				resistDisplays.put( dt, new ResistDisplayPair( srd, hrd ) );
			}
			this.pack();
			this.updateDisplay();
		}
		
		private JFormattedTextField createResistDisplayField() {
			JFormattedTextField result = new JFormattedTextField(NO_DATA_STRING);
			result.setForeground( Color.GRAY );
			result.setHorizontalAlignment(DISPLAY_FIELD_ALIGNMENT);
			result.setFont( new Font(result.getFont().getName(),Font.BOLD,
					result.getFont().getSize()) );
			result.setEditable(false);
			return result;
		}
		
		public void updateDisplay() {
			// Update the time.
			synchronized( allSHDG ) {
				synchronized ( lastSHDG ) {
					synchronized ( allDR ) {
						synchronized ( longTermDR ) {
							synchronized ( knownNames ) {
								synchronized ( currTime ) {
									// Update the current time display
									timeField.setValue( currTime.time );

									// Update the name models.
									knownNames.addAll( permanentNames );
									if ( knownNames.size() < targetFieldModel.getSize() - 1) {
										// Somebody externally deleted names.  Figure out which names were deleted
										// and mirror.
										for ( int i = 1; i < targetFieldModel.getSize(); i++ ) {
											String s = (String) targetFieldModel.getElementAt( i );
											if ( !knownNames.contains( s ) ) {
												if ( !s.equals("") && 
														( !(targetField.getSelectedItem().equals( s ) && targetField.isEnabled() ) || 
																!(selfNameField.getSelectedItem().equals( s ) && selfNameField.isEnabled() ) ) ) {
													// This isn't currently in an active field.
													// We can delete it safely.
													if ( targetField.getSelectedItem().equals( s ) ) {
														target = "";
														targetField.setSelectedItem( null );
													}
													if ( selfNameField.getSelectedItem().equals( s ) ) {
														selfName = "";
														selfNameField.setSelectedItem( null );
													}
													targetFieldModel.removeElementAt( i );
													selfNameFieldModel.removeElementAt( i );
												} else {
													// This is currently in an active field.
													// We shouldn't delete it.
													knownNames.add( s );
												}
												i--;
											}
										}
									}
									int i = 1;
									Iterator<String> iter = knownNames.iterator();
									String curr = iter.hasNext() ? iter.next() : null;
									while ( curr != null && i < targetFieldModel.getSize() ) {
										while ( curr != null && 
												curr.compareTo( (String) targetFieldModel.getElementAt( i ) ) < 0 ) {
											targetFieldModel.insertElementAt( curr, i );
											selfNameFieldModel.insertElementAt( curr, i );
											if ( iter.hasNext() ) {
												curr = iter.next();
											} else {
												curr = null;
											}
											i++;
										}
										if ( curr.equals( targetFieldModel.getElementAt( i ) ) ) {
											if ( iter.hasNext() ) {
												curr = iter.next();
											} else {
												curr = null;
											}
										}
										i++;
									}
									if ( curr != null ) {
										targetFieldModel.addElement( curr );
										selfNameFieldModel.addElement( curr );
										while ( iter.hasNext() ) {
											curr = iter.next();
											targetFieldModel.addElement( curr );
											selfNameFieldModel.addElement( curr );
										}
									}

									// Decide who to display information on
									UniqueName toDisplay = null;
									Set<UniqueName> validNames = new HashSet<UniqueName>();
									validNames.addAll( allSHDG.entities.keySet() );
									validNames.addAll( lastSHDG.entities.keySet() );
									if ( smartSelect ) {
										// Smart select is on.
										if ( !selfName.equals( "" ) ) {
											// First identify the UniqueName corresponding to selfName
											Set<UniqueName> namesToConsider = new HashSet<UniqueName>();
											namesToConsider.addAll( longTermDR.damage.keySet() );
											for ( UniqueName n : longTermDR.damage.keySet() ) {
												namesToConsider.addAll( longTermDR.damage.get( n ).keySet() );
											}
											namesToConsider.addAll( longTermDR.heal.keySet() );
											for ( UniqueName n : longTermDR.heal.keySet() ) {
												namesToConsider.addAll( longTermDR.heal.get( n ).keySet() );
											}
											namesToConsider.remove( new UniqueName( "", "" ) );
											UniqueName self = null;
											for ( UniqueName n : namesToConsider ) {
												if ( n.displayName.equalsIgnoreCase( selfName ) || 
														( selfName.contains("@") && 
																n.internalName.toUpperCase().matches( "^P\\[\\d*@\\d* " + selfName.toUpperCase() + "\\]$" ) ) ) {
													self = n;
													break;
												}
											}
											if ( self != null ) {
												// We found an entity matching that name.
												if ( teamAssist ) {
													// Team assist is on.
													// First, figure out teams.
													Set<Team> teams = new HashSet<Team>();
													if ( !namesToConsider.isEmpty() && namesToConsider.contains( self ) ) {
														// Assign teams
														Team selfTeam = null;
														Iterator<UniqueName> namesIter = namesToConsider.iterator();
														{
															UniqueName firstName = namesIter.next();
															Team temp = new Team();
															temp.members.add( firstName );
															if ( firstName.equals(self) ) {
																selfTeam = temp;
															}
															teams.add( temp );
														}
														while ( namesIter.hasNext() ) {
															UniqueName n = namesIter.next();
															Team toAddTo = null;
															for ( Team t : teams ) {
																if ( t.isLikelyMember( longTermDR, n ) ) {
																	toAddTo = t;
																	break;
																}
															}
															if ( toAddTo == null ) {
																toAddTo = new Team();
																teams.add( toAddTo );
															}
															toAddTo.members.add( n );
															if ( n.equals(self) ) {
																selfTeam = toAddTo;
															}
														}
														// Establish team relationships
														for ( Team t1 : teams ) {
															for ( Team t2 : teams ) {
																if ( !t1.relations.containsKey( t2 ) ) {
																	boolean relation = t1.isLikelyAlly( longTermDR, t2 );
																	t1.relations.put( t2, relation );
																	t2.relations.put( t1, relation );
																}
															}
														}
														// We now have team relationship data.
														// Get ally and enemy names.
														Set<UniqueName> allyNames = new HashSet<UniqueName>( selfTeam.members );
														Set<UniqueName> enemyNames = new HashSet<UniqueName>( );
														for ( Team t : selfTeam.relations.keySet() ) {
															if ( selfTeam.relations.get( t ) ) {
																// Allies
																allyNames.addAll( t.members );
															} else {
																// Enemies
																enemyNames.addAll( t.members );
															}
														}
														// Use this to choose target based on teamAssistType.
														switch ( teamAssistType ) {
														case SELECT_ALLY_TAKING_FIRE:
														case SELECT_ENEMY_TAKING_FIRE:
														{
															Set<UniqueName> targetNamesToConsider = 
																	teamAssistType == TeamAssistType.SELECT_ALLY_TAKING_FIRE ?
																			allyNames : enemyNames;
															targetNamesToConsider.retainAll( allSHDG.entities.keySet() );
															
															UniqueName bestPlayerName = null;
															double bestPlayerDamage = 0;
															UniqueName bestNPCName = null;
															double bestNPCDamage = 0;
															for ( UniqueName target : targetNamesToConsider ) {
																double damage = 0;
																for ( UniqueName source : allDR.damage.keySet() ) {
																	Map<String, ShieldHullDamage> damageMap = 
																			allDR.damage.get( source ).get( target );
																	if ( damageMap != null ) {
																		for ( String dt : damageMap.keySet() ) {
																			ShieldHullDamage shd = damageMap.get( dt );
																			damage += shd.shield + shd.hull; // Not counting pet damage here;
																			// pets will be covered by themselves.
																		}
																	}
																}

																if ( target.isPlayer() ) {
																	if ( bestPlayerName == null || damage > bestPlayerDamage ) {
																		bestPlayerName = target;
																		bestPlayerDamage = damage;
																	}
																} else {
																	if ( bestNPCName == null || damage > bestNPCDamage ) {
																		bestNPCName = target;
																		bestNPCDamage = damage;
																	}
																}
															}
															
															UniqueName bestName = null;
															if ( dontShowNPCs ) {
																bestName = bestPlayerName;
															} else {
																bestName = bestPlayerDamage > bestNPCDamage ? bestPlayerName : bestNPCName;
															}
															
															if ( bestName != null ) {
																toDisplay = bestName;
																target = toDisplay.toString();
																targetField.setSelectedItem( target );
															} else {
																// No ally/enemy took damage!
																target = "";
																targetField.setSelectedItem( null );
															}
															break;
														}
														case SELECT_ALLY_LOWEST_RESIST:
														case SELECT_ENEMY_LOWEST_RESIST:
														{
															Set<UniqueName> targetNamesToConsider = 
																	teamAssistType == TeamAssistType.SELECT_ALLY_LOWEST_RESIST ?
																			allyNames : enemyNames;
															targetNamesToConsider.retainAll( allSHDG.entities.keySet() );
															// The "best" SHDG represents lowest average resists.
															UniqueName bestNPCName = null;
															double bestNPCAvgResist = 0;
															UniqueName bestPlayerName = null;
															double bestPlayerAvgResist = 0;
															for ( UniqueName target : targetNamesToConsider ) {
																ShieldHullDamageGroup shdg = allSHDG.entities.get( target );
																double sumResist = 0;
																int counted = 0;
																for ( MonitoredDamageType dt : MonitoredDamageType.values() ) {
																	DamageResistGroup drg = shdg.damageByType.get( dt.logName );
																	if ( drg != null ) {
																		sumResist += drg.shieldMag / drg.shieldBaseMag + drg.hullMag / drg.hullBaseMag;
																		counted++;
																	}
																}
																double avgResist = sumResist / counted;
																if ( target.isPlayer() ) {
																	if ( bestPlayerName == null || avgResist < bestPlayerAvgResist ) {
																		bestPlayerName = target;
																		bestPlayerAvgResist = avgResist;
																	}
																} else {
																	if ( bestNPCName == null || avgResist < bestNPCAvgResist ) {
																		bestNPCName = target;
																		bestNPCAvgResist = avgResist;
																	}
																}
															}
															
															UniqueName bestName = null;
															if ( dontShowNPCs ) {
																bestName = bestPlayerName;
															} else {
																bestName = bestPlayerAvgResist < bestNPCAvgResist ? bestPlayerName : bestNPCName; 
															}
															
															if ( bestName != null ) {
																toDisplay = bestName;
																target = toDisplay.toString();
																targetField.setSelectedItem( target );
															} else {
																// No ally/enemy resist information is available.
																target = "";
																targetField.setSelectedItem( null );
															}
															break;
														}
														default:
															throw new IllegalStateException( teamAssistType + " did not have a coded target-selection method." );
														}
													} else {
														// We have no data.
														target = "";
														targetField.setSelectedItem( null );
													}
												} else {
													// Find the person that selfName has been dealing the most damage to
													// and choose them as the display target.
													Map<UniqueName, Map<String, ShieldHullDamage>> selfMap = allDR.damage.get( self );
													if ( selfMap != null ) {
														Set<UniqueName> names = new HashSet<UniqueName>( selfMap.keySet() );
														names.retainAll( allSHDG.entities.keySet() );
														UniqueName bestPlayerName = null;
														double mostPlayerDamage = 0;
														UniqueName bestNPCName = null;
														double mostNPCDamage = 0;
														for ( UniqueName target : names ) {
															Map<String, ShieldHullDamage> selfTMap = selfMap.get( target );
															double currDamage = 0;
															for ( String dt : selfTMap.keySet() ) {
																ShieldHullDamage shd = selfTMap.get( dt );
																// we include pet damage here
																currDamage += shd.shield + shd.petShield + shd.hull + shd.petHull;
															}
															if ( target.isPlayer() ) {
																if ( bestPlayerName == null || currDamage > mostPlayerDamage ) {
																	bestPlayerName = target;
																	mostPlayerDamage = currDamage;
																}
															} else {
																if ( bestNPCName == null || currDamage > mostNPCDamage ) {
																	bestNPCName = target;
																	mostNPCDamage = currDamage;
																}
															}
														}
														
														UniqueName smartSelectTarget = null;
														if ( dontShowNPCs ) {
															smartSelectTarget = bestPlayerName;
														} else {
															smartSelectTarget = mostPlayerDamage > mostNPCDamage ? bestPlayerName : bestNPCName;
														}
														
														if ( smartSelectTarget != null ) {
															// We've got a target.
															toDisplay = smartSelectTarget;
															if ( toDisplay.isPlayer() ) {
																// Display the name@handle if is player
																target = extractNameAtHandle( toDisplay.internalName );
															} else {
																target = toDisplay.displayName;
															}
															targetField.setSelectedItem( target );
														} else {
															// The entity hasn't damaged anybody yet.  Our target is nothing.
															target = "";
															targetField.setSelectedItem( null );
														}
													} else {
														// The entity hasn't damaged anybody yet.  Our target is nothing.
														target = "";
														targetField.setSelectedItem( null );
													}
												}
											} else {
												// No entity matching that name.
												target = "";
												targetField.setSelectedItem( null );
											}
										} else {
											target = "";
											targetField.setSelectedItem( null );
										}
									} else {
										if ( !target.equals( "" ) ) {
											for ( UniqueName n : validNames ) {
												if ( n.displayName.equalsIgnoreCase( target ) || 
														( target.contains("@") && 
																n.internalName.toUpperCase().matches( "^P\\[\\d*@\\d* " + target.toUpperCase() + "\\]$" ) ) ) {
													toDisplay = n;
													break;
												}
											}
										} else {
											target = "";
											targetField.setSelectedItem( null );
										}
									}

									// Display the information
									if ( toDisplay != null ) {
										for ( MonitoredDamageType dt : MonitoredDamageType.values() ) {
											ResistDisplayPair rdp = resistDisplays.get( dt );
											DamageResistGroup drg = null;
											{
												ShieldHullDamageGroup shdg = allSHDG.entities.get( toDisplay );
												if ( shdg != null ) {
													drg = shdg.damageByType.get( dt.logName );
												}
											}
											DamageResistGroup lastDrg = null;
											{
												ShieldHullDamageGroup lSHDG = lastSHDG.entities.get( toDisplay );
												if ( lSHDG != null ) {
													lastDrg = lSHDG.damageByType.get( dt.logName );
												}
											}
											if ( drg != null && Math.abs( drg.shieldBaseMag ) > EPSILON ) {
												Formatter tempFormatter = new Formatter();
												rdp.srd.setForeground( Color.BLACK );
												rdp.srd.setText( tempFormatter.format( "%4.2f%%", 100 - drg.shieldMag/drg.shieldBaseMag*100 ).toString() );
												tempFormatter.close();
											} else if ( lastDrg != null && Math.abs( lastDrg.shieldBaseMag ) > EPSILON ) {
												Formatter tempFormatter = new Formatter();
												rdp.srd.setForeground( Color.GRAY );
												rdp.srd.setText( tempFormatter.format( "%4.2f%%", 100 - lastDrg.shieldMag/lastDrg.shieldBaseMag*100 ).toString() );
												tempFormatter.close();
											} else {
												rdp.srd.setForeground( Color.GRAY );
												rdp.srd.setText( NO_DATA_STRING );
											}
											if ( drg != null && Math.abs( drg.hullBaseMag ) > EPSILON ) {
												Formatter tempFormatter = new Formatter();
												rdp.hrd.setForeground( Color.BLACK );
												rdp.hrd.setText( tempFormatter.format( "%4.2f%%", 100 - drg.hullMag/drg.hullBaseMag*100 ).toString() );
												tempFormatter.close();
											} else if ( lastDrg != null && Math.abs( lastDrg.hullBaseMag ) > EPSILON ) {
												Formatter tempFormatter = new Formatter();
												rdp.hrd.setForeground( Color.GRAY );
												rdp.hrd.setText( tempFormatter.format( "%4.2f%%", 100 - lastDrg.hullMag/lastDrg.hullBaseMag*100 ).toString() );
												tempFormatter.close();
											} else {
												rdp.hrd.setForeground( Color.GRAY );
												rdp.hrd.setText( NO_DATA_STRING );
											}
										}
									} else {
										for ( MonitoredDamageType dt : MonitoredDamageType.values() ) {
											ResistDisplayPair rdp = resistDisplays.get( dt );
											rdp.srd.setForeground( Color.GRAY );
											rdp.srd.setText( NO_DATA_STRING );
											rdp.hrd.setForeground( Color.GRAY );
											rdp.hrd.setText( NO_DATA_STRING );
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static DamageResistGroup addToAllShieldHullDamageGroups( AllShieldHullDamageGroups damageGroups, 
			UniqueName target, String damageType,
			double shieldMag, double shieldBaseMag, 
			double hullMag, double hullBaseMag ) {
		synchronized (damageGroups) {
			ShieldHullDamageGroup shdg = damageGroups.entities.get( target );
			DamageResistGroup result = new DamageResistGroup();
			if ( shdg == null ) {
				shdg = new ShieldHullDamageGroup();
				damageGroups.entities.put( target, shdg );
			}
			DamageResistGroup drg = shdg.damageByType.get( damageType );
			if ( drg == null ) {
				drg = new DamageResistGroup();
				shdg.damageByType.put( damageType, drg );
			}
			result.shieldMag = drg.shieldMag;
			result.shieldBaseMag = drg.shieldBaseMag;
			result.hullMag = drg.hullMag;
			result.hullBaseMag = drg.hullBaseMag;
			drg.shieldMag += shieldMag;
			drg.shieldBaseMag += shieldBaseMag;
			drg.hullMag += hullMag;
			drg.hullBaseMag += hullBaseMag;
			if ( Math.abs( drg.shieldMag ) < EPSILON && Math.abs( drg.shieldBaseMag ) < EPSILON && 
					Math.abs( drg.hullMag ) < EPSILON && Math.abs( drg.hullBaseMag ) < EPSILON ) {
				shdg.damageByType.remove( damageType );
			}
			if ( shdg.damageByType.size() == 0 ) {
				damageGroups.entities.remove( target );
			}
			return result;
		}
	}
	
	public static class UniqueName {
		public final String displayName;
		public final String internalName;
		
		public UniqueName( String displayName, String internalName ) {
			this.displayName = displayName;
			this.internalName = internalName;
		}
		
		public boolean isPlayer() {
			return this.internalName.length() > 0 && this.internalName.charAt(0) == 'P';
		}
		
		@Override
		public int hashCode() {
			return displayName.hashCode() + internalName.hashCode() * 2;
		}
		
		@Override
		public boolean equals( Object other ) {
			if ( other instanceof UniqueName ) {
				UniqueName o = (UniqueName) other;
				return displayName.equals( o.displayName ) && internalName.equals( o.internalName );
			} else {
				return false;
			}
		}
		
		@Override
		public String toString() {
			if ( this.isPlayer() ) {
				return extractNameAtHandle( internalName );
			} else {
				return displayName;
			}
		}
	}
	
	public static class AllShieldHullDamageGroups {
		public final long time;
		public final Map<UniqueName, ShieldHullDamageGroup> entities;
		
		public AllShieldHullDamageGroups( long time ) {
			this.time = time;
			this.entities = new HashMap<UniqueName,ShieldHullDamageGroup>();
		}
	}
	
	public static class ShieldHullDamageGroup {
		public final Map<String, DamageResistGroup> damageByType = new HashMap<String, DamageResistGroup>();
	}
	
	public static class DamageResistGroup {
		double shieldMag;
		double shieldBaseMag;
		double hullMag;
		double hullBaseMag;
	}
	
	// Reads a whole line from the growing log file.  Blocks until it has read a _whole_ line.
	static String inputBuffer = "";
	static Queue<String> storedStrings = new LinkedList<String>();
	static final int CHARS_TO_READ_AT_ONCE = 500;
	static byte[] byteBuffer = new byte[CHARS_TO_READ_AT_ONCE];
	public static String readWholeLine(RandomAccessFile input) {
		try {
			if ( !storedStrings.isEmpty() ) {
				return storedStrings.poll();
			}
			while ( !inputBuffer.contains("\n") ) {
				int bytesRead = input.read( byteBuffer );
				while ( bytesRead == -1 ) {
					processingIndicator.setForeground( Color.RED );
					try {
						Thread.sleep( 50 );
					} catch (InterruptedException e) {
						// Do nothing.
					}
					bytesRead = input.read( byteBuffer );
				}
				inputBuffer = inputBuffer + new String( byteBuffer, 0, bytesRead );
			}
			processingIndicator.setForeground( Color.GREEN );
			String[] strings = inputBuffer.split("\n", -1);
			for ( int i = 1; i < strings.length - 1; i++ ) {
				storedStrings.add( strings[i] );
			}
			inputBuffer = strings[strings.length - 1];
			return strings[0];
		} catch (IOException e) {
			throw new IllegalStateException();
		}
	}
	
	public static File getCombatLogFile() {
		File f = new File( CONFIG_FILE_LOCATION );
		String startDir = null;
		JFileChooser fc = new JFileChooser();
		if ( f.exists() && f.canRead() ) {
			try {
				startDir = (new Scanner( f )).nextLine();
			} catch (FileNotFoundException e) {
				// do nothing
			}
		}
		if ( startDir != null ) {
			fc.setCurrentDirectory( new File( startDir ) );
		} else {
			fc.setCurrentDirectory( new File( System.getProperty("user.dir") ) );
		}
		fc.setDialogTitle( "Open active STO combat log..." );
		int returnStatus = fc.showOpenDialog(null);
		File result;
		if ( returnStatus == JFileChooser.APPROVE_OPTION ) {
			// User has selected a "combat log".
			result = fc.getSelectedFile();
			String currDir = fc.getCurrentDirectory().getAbsolutePath();
			try {
				PrintStream os = new PrintStream( f );
				os.println( currDir );
				os.close();
			} catch (FileNotFoundException e) {
				// Do nothing; just ask again next time we start up.
			}
		} else {
			// User has canceled
			result = null;
		}
		return result;
	}
	
	// A class representing a single log entry.
	public static class LogEntry {
		public final long time; // measured in milliseconds
		public final String dispOwner; // 0th
		public final String intOwner; // 1st
		public final String dispSource; // 2nd
		public final String intSource; // 3rd
		public final String dispTarget; // 4th
		public final String intTarget; // 5th
		public final String dispEvent; // 6th
		public final String intEvent; // 7th
		public final String type; // 8th
		public final Collection<String> flags; // 9th
		// positive if damage, negative if heals, regardless of what the log actually reports
		public final double mag; // 10th
		public final double baseMag; // 11th
		
		public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yy:MM:dd:HH:mm:ss.SSS");
		
		@Override
		public String toString() {
			return new Date(time) + " | " + dispOwner + " | " + intOwner + " | " + dispSource + " | " + 
					intSource + " | " + dispTarget + " | " + intTarget + " | " + 
					dispEvent + " | " + intEvent + " | " + type + " | " + flags + " | " + mag + " | " + baseMag;
		}
		
		public LogEntry( String logLine ) {
			String[] initialSplit = logLine.split("::", 2);
			// Interpret the date.
			try {
				// Do an ugly bit of time format trickery to get the date format to parse correctly.
				time = dateFormat.parse(initialSplit[0] + "00").getTime();
			} catch (ParseException e) {
				throw new IllegalArgumentException("Time failed to parse.");
			}
			
			// Sort the rest of the line into the appropriate fields.
			String[] fullSplit = initialSplit[1].split(",", -1);
			if ( fullSplit.length != 12 )  {
				throw new IllegalArgumentException("Line did not have 12 expected fields.");
			}
			
			// Start with the flags.
			String[] flagStrings = fullSplit[9].split("\\|", -1);
			Collection<String> tempFlags = new ArrayList<String>(flagStrings.length);
			for (int i = 0; i < flagStrings.length; i++) {
				tempFlags.add(flagStrings[i]);
			}
			flags = Collections.unmodifiableCollection(tempFlags);
			
			// Now the damage type.
			type = fullSplit[8];
			
			// Now the owner.
			dispOwner = fullSplit[0];
			intOwner = fullSplit[1];
			
			// Now the source.  If the source is the same as the owner, the displayed source in the log
			// is "" and the internal source in the log is "*".
			if ( fullSplit[2].equals("") && fullSplit[3].equals("*") ) {
				dispSource = dispOwner;
				intSource = intOwner;
			} else {
				dispSource = fullSplit[2];
				intSource = fullSplit[3];
			}
			
			// Now the target.  If the target is the same as the source, the displayed target in the
			// log is "" and the internal target is "*".
			if (  fullSplit[4].equals("") &&fullSplit[5].equals("*") ) {
				dispTarget = dispSource;
				intTarget = intSource;
			} else {
				dispTarget = fullSplit[4];
				intTarget = fullSplit[5];
			}
			
			// Now the event.
			dispEvent = fullSplit[6];
			intEvent = fullSplit[7];
			
			// Finally we can figure out the magnitude/base magnitude.
			double tempMag = Double.parseDouble(fullSplit[10]);
			double tempBaseMag = Double.parseDouble(fullSplit[11]);
			// STO is irksome in that it reports both damage AND heals to shields as NEGATIVE.
			// Heals are identified by negative magnitude and 0 base magnitude;
			// damage is identified by negative magnitude and non-zero base magnitude.
			// Hopefully this works even if the target has 0 SDR; see note on how hull is handled below.
			if ( type.equals("Shield") && Math.abs( tempBaseMag ) > EPSILON) {
				tempMag = -tempMag;
				tempBaseMag = -tempBaseMag;
			}
			// In addition, occasionally damage that doesn't hit any sort of shields at all
			// (e.g. borg transformers which simply lack shields); the mag will be positive
			// and the base mag will be 0.  I don't know if this can happen with shields.
			if ( tempMag > 0 && Math.abs( tempBaseMag ) < EPSILON ) {
				tempBaseMag = tempMag;
			}
			mag = tempMag;
			baseMag = tempBaseMag;
			//System.out.println(this);
		}
		
		// Determines whether this log entry is a "pair" of the other one.
		// Used in linking shield/bleedthrough entries.
		// Pairs are identified by:
		//  - same time
		//  - same owner
		//  - same source
		//  - same target
		//  - same event
		//  - one is shield and other is non-shield type.
		// Note that this _still_ isn't enough to definitively link two events, but when they're this
		// close it doesn't matter if the pairing is swapped.
		public boolean isPair( LogEntry other ) {
			return other.time == this.time && 
					other.dispOwner.equals(this.dispOwner) && other.intOwner.equals(this.intOwner) &&
					other.dispSource.equals(this.dispSource) && other.intSource.equals(this.intSource) &&
					other.dispTarget.equals(this.dispTarget) && other.intTarget.equals(this.intTarget) &&
					other.dispEvent.equals(this.dispEvent) && other.intEvent.equals(this.intEvent) &&
					( ( other.type.equals("Shield") && !this.type.equals("Shield") ) 
							|| ( !other.type.equals("Shield") && this.type.equals("Shield") ) );
		}
	}
}

