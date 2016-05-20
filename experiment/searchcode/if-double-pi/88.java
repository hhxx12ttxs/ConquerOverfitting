/*******************************************************************************
 * Copyright (c) 2001, 2010 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/epl-v10.html
 *
 * Contributors:
 *     Mathew A. Nelson
 *     - Initial API and implementation
 *     Flemming N. Larsen
 *     - Code cleanup
 *     - Bugfix: updateMovement() checked for distanceRemaining > 1 instead of
 *       distanceRemaining > 0 if slowingDown and moveDirection == -1
 *     - Bugfix: Substituted wait(10000) with wait() in execute() method, so
 *       that robots do not hang when game is paused
 *     - Bugfix: Teleportation when turning the robot to 0 degrees while forcing
 *       the robot towards the bottom
 *     - Added setPaintEnabled() and isPaintEnabled()
 *     - Added setSGPaintEnabled() and isSGPaintEnabled()
 *     - Replaced the colorIndex with bodyColor, gunColor, and radarColor
 *     - Replaced the setColors() with setBodyColor(), setGunColor(), and
 *       setRadarColor()
 *     - Added bulletColor, scanColor, setBulletColor(), and setScanColor() and
 *       removed getColorIndex()
 *     - Optimizations
 *     - Ported to Java 5
 *     - Bugfix: HitRobotEvent.isMyFault() returned false despite the fact that
 *       the robot was moving toward the robot it collides with. This was the
 *       case when distanceRemaining == 0
 *     - Removed isDead field as the robot state is used as replacement
 *     - Added isAlive() method
 *     - Added constructor for creating a new robot with a name only
 *     - Added the set() that copies a RobotRecord into this robot in order to
 *       support the replay feature
 *     - Fixed synchronization issues with several member fields
 *     - Added features to support the new JuniorRobot class
 *     - Added cleanupStaticFields() for clearing static fields on robots
 *     - Added getMaxTurnRate()
 *     - Added turnAndMove() in order to support the turnAheadLeft(),
 *       turnAheadRight(), turnBackLeft(), and turnBackRight() for the
 *       JuniorRobot, which moves the robot in a perfect curve that follows a
 *       circle
 *     - Changed the behaviour of checkRobotCollision() so that HitRobotEvents
 *       are only created and sent to robot when damage do occur. Previously, a
 *       robot could receive HitRobotEvents even when no damage was done
 *     - Renamed scanReset() to rescan()
 *     - Added getStatusEvents()
 *     - Added getGraphicsProxy(), getPaintEvents()
 *     Luis Crespo
 *     - Added states
 *     Titus Chen
 *     - Bugfix: Hit wall and teleporting problems with checkWallCollision()
 *     Robert D. Maupin
 *     - Replaced old collection types like Vector and Hashtable with
 *       synchronized List and HashMap
 *     Nathaniel Troutman
 *     - Added cleanup() method for cleaning up references to internal classes
 *       to prevent circular references causing memory leaks
 *     Pavel Savara
 *     - Re-work of robot interfaces
 *     - hosting related logic moved to robot proxy
 *     - interlocked synchronization
 *     - (almost) minimized surface between RobotPeer and RobotProxy to serializable messages.
 *******************************************************************************/
package net.sf.robocode.battle.peer;


import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static net.sf.robocode.io.Logger.logMessage;
import static robocode.util.Utils.normalAbsoluteAngle;
import static robocode.util.Utils.normalNearAbsoluteAngle;
import static robocode.util.Utils.normalRelativeAngle;

import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.sf.robocode.battle.Battle;
import net.sf.robocode.host.IHostManager;
import net.sf.robocode.host.RobotStatics;
import net.sf.robocode.host.events.EventManager;
import net.sf.robocode.host.events.EventQueue;
import net.sf.robocode.host.proxies.IHostingRobotProxy;
import net.sf.robocode.io.Logger;
import net.sf.robocode.peer.BadBehavior;
import net.sf.robocode.peer.BulletStatus;
import net.sf.robocode.peer.DebugProperty;
import net.sf.robocode.peer.ExecCommands;
import net.sf.robocode.peer.ExecResults;
import net.sf.robocode.peer.IRobotPeer;
import net.sf.robocode.peer.BulletStatus;
import net.sf.robocode.peer.TeamMessage;
import net.sf.robocode.repository.IRobotRepositoryItem;
import net.sf.robocode.security.HiddenAccess;
import net.sf.robocode.serialization.RbSerializer;
import robocode.DeathEvent;
import robocode.Event;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotStatus;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.WinEvent;
import robocode.control.RandomFactory;
import robocode.control.RobotSpecification;
import robocode.control.snapshot.BulletState;
import robocode.control.snapshot.RobotState;
import robocode.exception.AbortedException;
import robocode.exception.DeathException;
import robocode.exception.WinException;


/**
 * RobotPeer is an object that deals with game mechanics and rules, and makes
 * sure that robots abides the rules.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 * @author Luis Crespo (contributor)
 * @author Titus Chen (contributor)
 * @author Robert D. Maupin (contributor)
 * @author Nathaniel Troutman (contributor)
 * @author Pavel Savara (contributor)
 * @author Patrick Cupka (contributor)
 * @author Julian Kent (contributor)
 * @author "Positive" (contributor)
 */
public final class RobotPeer extends RobotPeerWeaponSystem implements IRobotPeerBattle, IRobotPeer {

	private static final int MAX_SKIPPED_TURNS = 30;
	private static final int MAX_SKIPPED_TURNS_WITH_IO = 240;

	private final RobotSpecification robotSpecification;

	private IHostingRobotProxy robotProxy;
	private AtomicReference<RobotStatus> status = new AtomicReference<RobotStatus>();
	private AtomicReference<List<TeamMessage>> teamMessages = new AtomicReference<List<TeamMessage>>(
			new ArrayList<TeamMessage>());
	private int skippedTurns;

	private boolean isIORobot;
	private boolean isPaintEnabled;
	private boolean sgPaintEnabled;

	private final Arc2D scanArc;
	private final RbSerializer rbSerializer;
	protected boolean scan;
	protected final AtomicBoolean isSleeping = new AtomicBoolean(false);
	private final AtomicBoolean halt = new AtomicBoolean(false);

	public boolean isSleeping() {
		return isSleeping.get();
	}

	public boolean getHalt() {
		return halt.get();
	}

	public void setHalt(boolean value) {
		halt.set(value);
	}


	public RobotPeer(Battle battle, IHostManager hostManager, RobotSpecification robotSpecification, int duplicate, TeamPeer team, int index, int contestantIndex) {
		super(battle);
		if (team != null) {
			team.add(this);
		}

		rbSerializer = new RbSerializer();
		scanArc = new Arc2D.Double();
		teamPeer = team;
		state = RobotState.ACTIVE;
		battleRules = battle.getBattleRules();

		this.robotSpecification = robotSpecification;

		boolean isLeader = teamPeer != null && teamPeer.size() == 1;
		String teamName = team == null ? null : team.getName();
		List<String> teamMembers = team == null ? null : team.getMemberNames(); 

		statics = new RobotStatics(robotSpecification, duplicate, isLeader, battleRules, teamName, teamMembers, index,
				contestantIndex);
		statistics = new RobotStatistics(this, battle.getRobotsCount());

		robotProxy = (IHostingRobotProxy) hostManager.createRobotProxy(robotSpecification, statics, this);
	}

	public void print(Throwable ex) {
		println(ex.toString());
		StackTraceElement[] trace = ex.getStackTrace();

		for (StackTraceElement aTrace : trace) {
			println("\tat " + aTrace);
		}

		Throwable ourCause = ex.getCause();

		if (ourCause != null) {
			print(ourCause);
		}
	}

	public void printProxy(String s) {
		synchronized (proxyText) {
			proxyText.append(s);
		}
	}

	public String readOutText() {
		synchronized (proxyText) {
			final String robotText = battleText.toString() + proxyText.toString();

			battleText.setLength(0);
			proxyText.setLength(0);
			return robotText;
		}
	}

	public RobotStatistics getRobotStatistics() {
		return statistics;
	}

	public ContestantStatistics getStatistics() {
		return statistics;
	}

	public RobotSpecification getRobotSpecification() {
		return robotSpecification;
	}

	// -------------------
	// statics 
	// -------------------

	public boolean isDroid() {
		return statics.isDroid();
	}

	public boolean isJuniorRobot() {
		return statics.isJuniorRobot();
	}

	public boolean isInteractiveRobot() {
		return statics.isInteractiveRobot();
	}

	public boolean isPaintRobot() {
		return statics.isPaintRobot();
	}

	public boolean isAdvancedRobot() {
		return statics.isAdvancedRobot();
	}

	public boolean isTeamRobot() {
		return statics.isTeamRobot();
	}

	public String getShortName() {
		return statics.getShortName();
	}

	public String getVeryShortName() {
		return statics.getVeryShortName();
	}

	public int getIndex() {
		return statics.getIndex();
	}

	public int getContestIndex() {
		return statics.getContestIndex();
	}

	// -------------------
	// status 
	// -------------------

	public void setPaintEnabled(boolean enabled) {
		isPaintEnabled = enabled;
	}

	public boolean isPaintEnabled() {
		return isPaintEnabled;
	}

	public void setSGPaintEnabled(boolean enabled) {
		sgPaintEnabled = enabled;
	}

	public boolean isSGPaintEnabled() {
		return sgPaintEnabled;
	}

	public Arc2D getScanArc() {
		return scanArc;
	}

	// -------------------
	// robot space
	// -------------------

	public double getRadarHeading() {
		return radarHeading;
	}

	public int getBodyColor() {
		return commands.get().getBodyColor();
	}

	public int getRadarColor() {
		return commands.get().getRadarColor();
	}

	public int getScanColor() {
		return commands.get().getScanColor();
	}

	// ------------
	// team
	// ------------

	public String getTeamName() {
		return statics.getTeamName();
	}

	public boolean isTeamLeader() {
		return statics.isTeamLeader();
	}

	ByteBuffer bidirectionalBuffer;
	protected RobotStatistics statistics;
	protected final TeamPeer teamPeer;
	protected boolean inCollision;
	protected AtomicReference<EventQueue> events = new AtomicReference<EventQueue>(new EventQueue());
	protected final AtomicBoolean isRunning = new AtomicBoolean(false);
	protected double lastGunHeading;
	protected double lastRadarHeading;
	public void setupBuffer(ByteBuffer bidirectionalBuffer) {
		this.bidirectionalBuffer = bidirectionalBuffer;
	}

	public void setupThread() {
		Thread.currentThread().setName(getName());
	}

	public void executeImplSerial() throws IOException {
		ExecCommands commands = (ExecCommands) rbSerializer.deserialize(bidirectionalBuffer);

		final ExecResults results = executeImpl(commands);

		bidirectionalBuffer.clear();
		rbSerializer.serializeToBuffer(bidirectionalBuffer, RbSerializer.ExecResults_TYPE, results);
	}

	public void waitForBattleEndImplSerial() throws IOException {
		ExecCommands commands = (ExecCommands) rbSerializer.deserialize(bidirectionalBuffer);

		final ExecResults results = waitForBattleEndImpl(commands);

		bidirectionalBuffer.clear();
		rbSerializer.serializeToBuffer(bidirectionalBuffer, RbSerializer.ExecResults_TYPE, results);
	}

	public final ExecResults executeImpl(ExecCommands newCommands) {
		validateCommands(newCommands);

		if (!isExecFinishedAndDisabled) {
			// from robot to battle
			commands.set(new ExecCommands(newCommands, true));
			printProxy(newCommands.getOutputText());
		} else {
			// slow down spammer
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		// If we are stopping, yet the robot took action (in onWin or onDeath), stop now.
		if (battle.isAborted()) {
			isExecFinishedAndDisabled = true;
			throw new AbortedException();
		}
		if (isDead()) {
			isExecFinishedAndDisabled = true;
			throw new DeathException();
		}
		if (getHalt()) {
			isExecFinishedAndDisabled = true;
			if (isWinner) {
				throw new WinException();
			} else {
				throw new AbortedException();
			}
		}

		waitForNextTurn();

		// from battle to robot
		final ExecCommands resCommands = new ExecCommands(this.commands.get(), false);
		final RobotStatus resStatus = status.get();

		final boolean shouldWait = battle.isAborted() || (battle.isLastRound() && isWinner());

		return new ExecResults(resCommands, resStatus, readoutEvents(), readoutTeamMessages(), readoutProjectiles(),
				getHalt(), shouldWait, isPaintEnabled());
	}

	public final ExecResults waitForBattleEndImpl(ExecCommands newCommands) {
		if (!getHalt()) {
			// from robot to battle
			commands.set(new ExecCommands(newCommands, true));
			printProxy(newCommands.getOutputText());

			waitForNextTurn();
		}
		// from battle to robot
		final ExecCommands resCommands = new ExecCommands(this.commands.get(), false);
		final RobotStatus resStatus = status.get();

		final boolean shouldWait = battle.isAborted() || (battle.isLastRound() && !isWinner());

		readoutTeamMessages(); // throw away
		
		return new ExecResults(resCommands, resStatus, readoutEvents(), new ArrayList<TeamMessage>(), readoutProjectiles(),
				getHalt(), shouldWait, false);
	}

	private void validateCommands(ExecCommands newCommands) {
		if (Double.isNaN(newCommands.getMaxTurnRate())) {
			println("You cannot setMaxTurnRate to: " + newCommands.getMaxTurnRate());
		}
		newCommands.setMaxTurnRate(Math.min(abs(newCommands.getMaxTurnRate()), Rules.MAX_TURN_RATE_RADIANS));

		if (Double.isNaN(newCommands.getMaxVelocity())) {
			println("You cannot setMaxVelocity to: " + newCommands.getMaxVelocity());
		}
		newCommands.setMaxVelocity(Math.min(abs(newCommands.getMaxVelocity()), Rules.MAX_VELOCITY));
	}

	private List<Event> readoutEvents() {
		return events.getAndSet(new EventQueue());
	}

	private List<TeamMessage> readoutTeamMessages() {
		return teamMessages.getAndSet(new ArrayList<TeamMessage>());
	}

	private List<BulletStatus> readoutProjectiles() {
		return projectileUpdates.getAndSet(new ArrayList<BulletStatus>());
	}

	private void waitForNextTurn() {
		synchronized (isSleeping) {
			// Notify the battle that we are now asleep.
			// This ends any pending wait() call in battle.runRound().
			// Should not actually take place until we release the lock in wait(), below.
			isSleeping.set(true);
			isSleeping.notifyAll();
			// Notifying battle that we're asleep
			// Sleeping and waiting for battle to wake us up.
			try {
				isSleeping.wait();
			} catch (InterruptedException e) {
				// We are expecting this to happen when a round is ended!

				// Immediately reasserts the exception by interrupting the caller thread itself
				Thread.currentThread().interrupt();
			}
			isSleeping.set(false);
			// Notify battle thread, which is waiting in
			// our wakeup() call, to return.
			// It's quite possible, by the way, that we'll be back in sleep (above)
			// before the battle thread actually wakes up
			isSleeping.notifyAll();
		}
	}

	// -----------
	// called on battle thread
	// -----------

	public void waitWakeup() {
		synchronized (isSleeping) {
			if (isSleeping()) {
				// Wake up the thread
				isSleeping.notifyAll();
				try {
					isSleeping.wait(10000);
				} catch (InterruptedException e) {
					// Immediately reasserts the exception by interrupting the caller thread itself
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	public void waitWakeupNoWait() {
		synchronized (isSleeping) {
			if (isSleeping()) {
				// Wake up the thread
				isSleeping.notifyAll();
			}
		}
	}

	public void waitSleeping(long millisWait, int nanosWait) {
		synchronized (isSleeping) {
			// It's quite possible for simple robots to
			// complete their processing before we get here,
			// so we test if the robot is already asleep.

			if (!isSleeping()) {
				try {
					for (long i = millisWait; i > 0 && !isSleeping() && isRunning(); i--) {
						isSleeping.wait(0, 999999);
					}
					if (!isSleeping() && isRunning()) {
						isSleeping.wait(0, nanosWait);
					}
				} catch (InterruptedException e) {
					// Immediately reasserts the exception by interrupting the caller thread itself
					Thread.currentThread().interrupt();

					logMessage("Wait for " + getName() + " interrupted.");
				}
			}
		}
	}

	public void setSkippedTurns() {
		if (isSleeping() || !isRunning() || battle.isDebugging() || isPaintEnabled()) {
			skippedTurns = 0;
		} else {
			println("SYSTEM: " + getShortName() + " skipped turn " + battle.getTime());
			skippedTurns++;
			events.get().clear(false);
			if (!isDead()) {
				addEvent(new SkippedTurnEvent(battle.getTime()));
			}

			if ((!isIORobot && (skippedTurns > MAX_SKIPPED_TURNS))
					|| (isIORobot && (skippedTurns > MAX_SKIPPED_TURNS_WITH_IO))) {
				println("SYSTEM: " + getShortName() + " has not performed any actions in a reasonable amount of time.");
				println("SYSTEM: No score will be generated.");
				setHalt(true);
				waitWakeupNoWait();
				punishBadBehavior(BadBehavior.SKIPPED_TOO_MANY_TURNS);
				robotProxy.forceStopThread();
			}
		}
	}

	public void initializeRound(List<RobotPeer> robots, double[][] initialRobotPositions) {
		boolean valid = false;

		if (initialRobotPositions != null) {

			if (statics.getIndex() >= 0 && statics.getIndex() < initialRobotPositions.length) {
				double[] pos = initialRobotPositions[statics.getIndex()];

				x = pos[0];
				y = pos[1];
				bodyHeading = pos[2];
				gunHeading = radarHeading = bodyHeading;
				updateBoundingBox();
				valid = validSpot(robots);
			}
		}

		if (!valid) {
			final Random random = RandomFactory.getRandom();

			for (int j = 0; j < 1000; j++) {
				x = RobotPeerEngine.WIDTH + random.nextDouble() * (battleRules.getBattlefieldWidth() - 2 * RobotPeerEngine.WIDTH);
				y = RobotPeerEngine.HEIGHT + random.nextDouble() * (battleRules.getBattlefieldHeight() - 2 * RobotPeerEngine.HEIGHT);
				bodyHeading = 2 * Math.PI * random.nextDouble();
				gunHeading = radarHeading = bodyHeading;
				updateBoundingBox();

				if (validSpot(robots)) {
					break;
				}
			}
		}

		setState(RobotState.ACTIVE);

		isWinner = false;
		velocity = 0;

		if (statics.isTeamLeader() && statics.isDroid()) {
			energy = 220;
		} else if (statics.isTeamLeader()) {
			energy = 200;
		} else if (statics.isDroid()) {
			energy = 120;
		} else {
			energy = 100;
		}
		gunHeat = 3;

		setHalt(false);
		isExecFinishedAndDisabled = false;
		isEnergyDrained = false;

		scan = false;

		inCollision = false;

		scanArc.setAngleStart(0);
		scanArc.setAngleExtent(0);
		scanArc.setFrame(-100, -100, 1, 1);

		skippedTurns = 0;

		status = new AtomicReference<RobotStatus>();

		readoutEvents();
		readoutTeamMessages();
		readoutProjectiles();

		battleText.setLength(0);
		proxyText.setLength(0);

		// Prepare new execution commands, but copy the colors from the last commands.
		// Bugfix [2628217] - Robot Colors don't stick between rounds.
		ExecCommands newExecCommands = new ExecCommands();

		newExecCommands.copyColors(commands.get());
		commands = new AtomicReference<ExecCommands>(newExecCommands);
	}

	public void startRound(long waitMillis, int waitNanos) {
		Logger.logMessage(".", false);

		statistics.initialize();

		ExecCommands newExecCommands = new ExecCommands();

		// Copy the colors from the last commands.
		// Bugfix [2628217] - Robot Colors don't stick between rounds.
		newExecCommands.copyColors(commands.get());

		currentCommands = newExecCommands;
		int others = battle.getActiveRobots() - (isAlive() ? 1 : 0);
		RobotStatus stat = HiddenAccess.createStatus(energy, x, y, bodyHeading, gunHeading, radarHeading, velocity,
				currentCommands.getBodyTurnRemaining(), currentCommands.getRadarTurnRemaining(),
				currentCommands.getGunTurnRemaining(), currentCommands.getDistanceRemaining(), gunHeat, others,
				battle.getRoundNum(), battle.getNumRounds(), battle.getTime());

		status.set(stat);
		robotProxy.startRound(currentCommands, stat);

		synchronized (isSleeping) {
			try {
				// Wait for the robot to go to sleep (take action)
				isSleeping.wait(waitMillis, waitNanos);
			} catch (InterruptedException e) {
				logMessage("Wait for " + getName() + " interrupted.");

				// Immediately reasserts the exception by interrupting the caller thread itself
				Thread.currentThread().interrupt();
			}
		}
		if (!isSleeping() && !battle.isDebugging()) {
			logMessage("\n" + getName() + " still has not started after " + waitMillis + " ms... giving up.");
		}
	}

	public void performLoadCommands() {
		currentCommands = commands.get();

		fire(currentCommands.getProjectiles());

		if (currentCommands.isScan()) {
			scan = true;
		}

		if (currentCommands.isIORobot()) {
			isIORobot = true;
		}

		if (currentCommands.isMoved()) {
			currentCommands.setMoved(false);
		}
	}

	public void performScan(List<RobotPeer> robots) {
		if (isDead()) {
			return;
		}

		turnedRadarWithGun = false;
		// scan
		if (scan) {
			scan(lastRadarHeading, robots);
			turnedRadarWithGun = (lastGunHeading == lastRadarHeading) && (gunHeading == radarHeading);
			scan = false;
		}

		// dispatch messages
		if (statics.isTeamRobot() && teamPeer != null) {
			for (TeamMessage teamMessage : currentCommands.getTeamMessages()) {
				for (RobotPeer member : teamPeer) {
					if (checkDispatchToMember(member, teamMessage.recipient)) {
						member.addTeamMessage(teamMessage);
					}
				}
			}
		}
		currentCommands = null;
		lastHeading = -1;
		lastGunHeading = -1;
		lastRadarHeading = -1;
	}

	private void addTeamMessage(TeamMessage message) {
		final List<TeamMessage> queue = teamMessages.get();

		queue.add(message);
	}

	private boolean checkDispatchToMember(RobotPeerWeaponSystem member, String recipient) {
		if (member.isAlive()) {
			if (recipient == null) {
				if (member != this) {
					return true;
				}
			} else {
				final int nl = recipient.length();
				final String currentName = member.statics.getName();

				if ((currentName.length() >= nl && currentName.substring(0, nl).equals(recipient))) {
					return true;
				}

				final String currentClassName = member.statics.getFullClassName();

				if ((currentClassName.length() >= nl && currentClassName.substring(0, nl).equals(recipient))) {
					return true;
				}

			}
		}
		return false;
	}

	private void scan(double lastRadarHeading, List<RobotPeer> robots) {
		if (statics.isDroid()) {
			return;
		}

		double startAngle = lastRadarHeading;
		double scanRadians = getRadarHeading() - startAngle;

		// Check if we passed through 360
		if (scanRadians < -PI) {
			scanRadians = 2 * PI + scanRadians;
		} else if (scanRadians > PI) {
			scanRadians = scanRadians - 2 * PI;
		}

		// In our coords, we are scanning clockwise, with +y up
		// In java coords, we are scanning counterclockwise, with +y down
		// All we need to do is adjust our angle by -90 for this to work.
		startAngle -= PI / 2;

		startAngle = normalAbsoluteAngle(startAngle);

		scanArc.setArc(x - Rules.RADAR_SCAN_RADIUS, y - Rules.RADAR_SCAN_RADIUS, 2 * Rules.RADAR_SCAN_RADIUS,
				2 * Rules.RADAR_SCAN_RADIUS, 180.0 * startAngle / PI, 180.0 * scanRadians / PI, Arc2D.PIE);

		for (RobotPeer otherRobot : robots) {
			if (!(otherRobot == null || otherRobot == this || otherRobot.isDead())
					&& intersects(scanArc, otherRobot.boundingBox)) {
				double dx = otherRobot.x - x;
				double dy = otherRobot.y - y;
				double angle = atan2(dx, dy);
				double dist = Math.hypot(dx, dy);

				final ScannedRobotEvent event = new ScannedRobotEvent(getNameForEvent(otherRobot), otherRobot.energy,
						normalRelativeAngle(angle - getBodyHeading()), dist, otherRobot.getBodyHeading(),
						otherRobot.getVelocity());

				addEvent(event);
			}
		}
	}

	private boolean intersects(Arc2D arc, Rectangle2D rect) {
		return (rect.intersectsLine(arc.getCenterX(), arc.getCenterY(), arc.getStartPoint().getX(),
				arc.getStartPoint().getY()))
				|| arc.intersects(rect);
	}

	public void setRunning(boolean value) {
		isRunning.set(value);
	}

	public void punishBadBehavior(BadBehavior badBehavior) {
		kill(); // Bug fix [2828479] - Missed onRobotDeath events

		statistics.setInactive();

		final IRobotRepositoryItem repositoryItem = (IRobotRepositoryItem) HiddenAccess.getFileSpecification(
				robotSpecification);

		StringBuffer message = new StringBuffer(getName()).append(' ');

		boolean disableInRepository = false; // Per default, robots are not disabled in the repository

		switch (badBehavior) {
		case CANNOT_START:
			message.append("could not be started or loaded.");
			disableInRepository = true; // Disable in repository when it cannot be started anyways
			break;

		case UNSTOPPABLE:
			message.append("cannot be stopped.");
			break;

		case SKIPPED_TOO_MANY_TURNS:
			message.append("has skipped too many turns.");
			break;

		case SECURITY_VIOLATION:
			message.append("has caused a security violation.");
			disableInRepository = true; // No mercy here!
			break;
		}

		if (disableInRepository) {
			repositoryItem.setValid(false);			
			message.append(" This ").append(repositoryItem.isTeam() ? "team" : "robot").append(
					" has been banned and will not be allowed to participate in battles.");
		}

		logMessage(message.toString());
	}

	public void setWinner(boolean newWinner) {
		isWinner = newWinner;
	}

	public void waitForStop() {
		robotProxy.waitForStopThread();
	}

	/**
	 * Clean things up removing all references to the robot.
	 */
	public void cleanup() {
		battle = null;

		if (robotProxy != null) {
			robotProxy.cleanup();
			robotProxy = null;
		}

		if (statistics != null) {
			statistics.cleanup();
			statistics = null;
		}

		status = null;
		commands = null;
		events = null;
		teamMessages = null;
		projectileUpdates = null;
		battleText.setLength(0);
		proxyText.setLength(0);
		statics = null;
		battleRules = null;
	}

	public Object getGraphicsCalls() {
		return commands.get().getGraphicsCalls();
	}

	public boolean isTryingToPaint() {
		return commands.get().isTryingToPaint();
	}

	public List<DebugProperty> getDebugProperties() {
		return commands.get().getDebugProperties();
	}

	public void publishStatus(long currentTurn) {

		final ExecCommands currentCommands = commands.get();
		int others = battle.getActiveRobots() - (isAlive() ? 1 : 0);
		RobotStatus stat = HiddenAccess.createStatus(energy, x, y, bodyHeading, gunHeading, radarHeading, velocity,
				currentCommands.getBodyTurnRemaining(), currentCommands.getRadarTurnRemaining(),
				currentCommands.getGunTurnRemaining(), currentCommands.getDistanceRemaining(), gunHeat, others,
				battle.getRoundNum(), battle.getNumRounds(), battle.getTime());

		status.set(stat);
	}

	public int compareTo(ContestantPeer cp) {
		double myScore = statistics.getTotalScore();
		double hisScore = cp.getStatistics().getTotalScore();

		if (statistics.isInRound()) {
			myScore += statistics.getCurrentScore();
			hisScore += cp.getStatistics().getCurrentScore();
		}
		if (myScore < hisScore) {
			return -1;
		}
		if (myScore > hisScore) {
			return 1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return statics.getShortName() + "(" + (int) energy + ") X" + (int) x + " Y" + (int) y + " " + state.toString()
				+ (isSleeping() ? " sleeping " : "") + (isRunning() ? " running" : "") + (getHalt() ? " halted" : "");
	}

	public String getName() {
		return statics.getName();
	}

	public String getAnonymousName() {
		return statics.getAnonymousName();
	}

	public TeamPeer getTeamPeer() {
		return teamPeer;
	}

	public boolean isTeamMate(RobotPeerEngine otherRobot) {
		if (getTeamPeer() != null) {
			for (RobotPeerEngine mate : getTeamPeer()) {
				if (otherRobot == mate) {
					return true;
				}
			}	
		}
		return false;
	}

	public String getNameForEvent(RobotPeer otherRobot) {
		if (battleRules.getHideEnemyNames() && !isTeamMate(otherRobot)) {
			return otherRobot.getAnonymousName();
		}
		return otherRobot.getName();
	}

	private void checkRobotCollision(List<RobotPeer> robots) {
		inCollision = false;
	
		for (RobotPeer otherRobot : robots) {
			if (!(otherRobot == null || otherRobot == this || otherRobot.isDead())
					&& boundingBox.intersects(otherRobot.boundingBox)) {
				// Bounce back
				double angle = atan2(otherRobot.x - x, otherRobot.y - y);
	
				double movedx = velocity * sin(bodyHeading);
				double movedy = velocity * cos(bodyHeading);
	
				boolean atFault;
				double bearing = normalRelativeAngle(angle - bodyHeading);
	
				if ((velocity > 0 && bearing > -PI / 2 && bearing < PI / 2)
						|| (velocity < 0 && (bearing < -PI / 2 || bearing > PI / 2))) {
	
					inCollision = true;
					atFault = true;
					velocity = 0;
					currentCommands.setDistanceRemaining(0);
					x -= movedx;
					y -= movedy;
	
					boolean teamFire = (teamPeer != null && teamPeer == otherRobot.teamPeer);
	
					if (!teamFire) {
						statistics.scoreRammingDamage(otherRobot.getName());
					}
	
					this.updateEnergy(-Rules.ROBOT_HIT_DAMAGE);
					otherRobot.updateEnergy(-Rules.ROBOT_HIT_DAMAGE);
	
					if (otherRobot.energy == 0) {
						if (otherRobot.isAlive()) {
							otherRobot.kill();
							if (!teamFire) {
								final double bonus = statistics.scoreRammingKill(otherRobot.getName());
	
								if (bonus > 0) {
									println(
											"SYSTEM: Ram bonus for killing " + this.getNameForEvent(otherRobot) + ": "
											+ (int) (bonus + .5));
								}
							}
						}
					}
					addEvent(
							new HitRobotEvent(getNameForEvent(otherRobot), normalRelativeAngle(angle - bodyHeading),
							otherRobot.energy, atFault));
					otherRobot.addEvent(
							new HitRobotEvent(getNameForEvent(this),
							normalRelativeAngle(PI + angle - otherRobot.getBodyHeading()), energy, false));
				}
			}
		}
		if (inCollision) {
			setState(RobotState.HIT_ROBOT);
		}
	}

	private void checkWallCollision() {
		boolean hitWall = false;
		double fixx = 0, fixy = 0;
		double angle = 0;
	
		if (x > getBattleFieldWidth() - HALF_WIDTH_OFFSET) {
			hitWall = true;
			fixx = getBattleFieldWidth() - HALF_WIDTH_OFFSET - x;
			angle = normalRelativeAngle(PI / 2 - bodyHeading);
		}
	
		if (x < HALF_WIDTH_OFFSET) {
			hitWall = true;
			fixx = HALF_WIDTH_OFFSET - x;
			angle = normalRelativeAngle(3 * PI / 2 - bodyHeading);
		}
	
		if (y > getBattleFieldHeight() - HALF_HEIGHT_OFFSET) {
			hitWall = true;
			fixy = getBattleFieldHeight() - HALF_HEIGHT_OFFSET - y;
			angle = normalRelativeAngle(-bodyHeading);
		}
	
		if (y < HALF_HEIGHT_OFFSET) {
			hitWall = true;
			fixy = HALF_HEIGHT_OFFSET - y;
			angle = normalRelativeAngle(PI - bodyHeading);
		}
	
		if (hitWall) {
			addEvent(new HitWallEvent(angle));
	
			// only fix both x and y values if hitting wall at an angle
			if ((bodyHeading % (Math.PI / 2)) != 0) {
				double tanHeading = tan(bodyHeading);
	
				// if it hits bottom or top wall
				if (fixx == 0) {
					fixx = fixy * tanHeading;
				} // if it hits a side wall
				else if (fixy == 0) {
					fixy = fixx / tanHeading;
				} // if the robot hits 2 walls at the same time (rare, but just in case)
				else if (abs(fixx / tanHeading) > abs(fixy)) {
					fixy = fixx / tanHeading;
				} else if (abs(fixy * tanHeading) > abs(fixx)) {
					fixx = fixy * tanHeading;
				}
			}
			x += fixx;
			y += fixy;
	
			x = (HALF_WIDTH_OFFSET >= x)
					? HALF_WIDTH_OFFSET
					: ((getBattleFieldWidth() - HALF_WIDTH_OFFSET < x) ? getBattleFieldWidth() - HALF_WIDTH_OFFSET : x);
			y = (HALF_HEIGHT_OFFSET >= y)
					? HALF_HEIGHT_OFFSET
					: ((getBattleFieldHeight() - HALF_HEIGHT_OFFSET < y) ? getBattleFieldHeight() - HALF_HEIGHT_OFFSET : y);
	
			// Update energy, but do not reset inactiveTurnCount
			if (statics.isAdvancedRobot()) {
				setEnergy(energy - Rules.getWallHitDamage(velocity), false);
			}
	
			updateBoundingBox();
	
			currentCommands.setDistanceRemaining(0);
			velocity = 0;
		}
		if (hitWall) {
			setState(RobotState.HIT_WALL);
		}
	}

	private double getBattleFieldHeight() {
		return battleRules.getBattlefieldHeight();
	}

	private double getBattleFieldWidth() {
		return battleRules.getBattlefieldWidth();
	}

	public final void performMove(List<RobotPeer> robots, double zapEnergy) {
	
		// Reset robot state to active if it is not dead
		if (isDead()) {
			return;
		}
	
		setState(RobotState.ACTIVE);
	
		updateGunHeat();
	
		lastHeading = bodyHeading;
		lastGunHeading = gunHeading;
		lastRadarHeading = radarHeading;
		final double lastX = x;
		final double lastY = y;
	
		if (!inCollision) {
			updateHeading();
		}
	
		updateGunHeading();
		updateRadarHeading();
		updateMovement();
	
		// At this point, robot has turned then moved.
		// We could be touching a wall or another bot...
	
		// First and foremost, we can never go through a wall:
		checkWallCollision();
	
		// Now check for robot collision
		checkRobotCollision(robots);
	
		// Scan false means robot did not call scan() manually.
		// But if we're moving, scan
		if (!scan) {
			scan = (lastHeading != bodyHeading || lastGunHeading != gunHeading || lastRadarHeading != radarHeading
					|| lastX != x || lastY != y);
		}
	
		if (isDead()) {
			return;
		}
	
		// zap
		if (zapEnergy != 0) {
			zap(zapEnergy);
		}
	}

	public void addEvent(Event event) {
		if (isRunning()) {
			final EventQueue queue = events.get();
	
			if ((queue.size() > EventManager.MAX_QUEUE_SIZE)
					&& !(event instanceof DeathEvent || event instanceof WinEvent || event instanceof SkippedTurnEvent)) {
				println(
						"Not adding to " + statics.getShortName() + "'s queue, exceeded " + EventManager.MAX_QUEUE_SIZE
						+ " events in queue.");
				// clean up old stuff                
				queue.clear(battle.getTime() - EventManager.MAX_EVENT_STACK);
				return;
			}
			queue.add(event);
		}
	}

	protected void updateRadarHeading() {
		if (currentCommands.getRadarTurnRemaining() > 0) {
			if (currentCommands.getRadarTurnRemaining() < Rules.RADAR_TURN_RATE_RADIANS) {
				radarHeading += currentCommands.getRadarTurnRemaining();
				currentCommands.setRadarTurnRemaining(0);
			} else {
				radarHeading += Rules.RADAR_TURN_RATE_RADIANS;
				currentCommands.setRadarTurnRemaining(
						currentCommands.getRadarTurnRemaining() - Rules.RADAR_TURN_RATE_RADIANS);
			}
		} else if (currentCommands.getRadarTurnRemaining() < 0) {
			if (currentCommands.getRadarTurnRemaining() > -Rules.RADAR_TURN_RATE_RADIANS) {
				radarHeading += currentCommands.getRadarTurnRemaining();
				currentCommands.setRadarTurnRemaining(0);
			} else {
				radarHeading -= Rules.RADAR_TURN_RATE_RADIANS;
				currentCommands.setRadarTurnRemaining(
						currentCommands.getRadarTurnRemaining() + Rules.RADAR_TURN_RATE_RADIANS);
			}
		}
	
		radarHeading = normalAbsoluteAngle(radarHeading);
	}

	public void kill() {
		battle.resetInactiveTurnCount(10.0);
		if (isAlive()) {
			addEvent(new DeathEvent());
			if (statics.isTeamLeader()) {
				for (RobotPeer teammate : teamPeer) {
					if (!(teammate.isDead() || teammate == this)) {
						teammate.updateEnergy(-30);
	
						BulletPeer sBullet = new BulletPeer(this, battleRules, -1);
	
						sBullet.setState(BulletState.HIT_VICTIM);
						sBullet.setX(teammate.x);
						sBullet.setY(teammate.y);
						sBullet.setVictim(teammate);
						sBullet.setPower(4);
						battle.addBullet(sBullet);
					}
				}
			}
			battle.registerDeathRobot(this);
	
			// 'fake' bullet for explosion on self
			final ExplosionPeer fake = new ExplosionPeer(this, battleRules);
	
			battle.addBullet(fake);
		}
		updateEnergy(-energy);
	
		setState(RobotState.DEAD);
	}

	protected void zap(double zapAmount) {
		if (energy == 0) {
			kill();
			return;
		}
		energy -= abs(zapAmount);
		if (energy < .1) {
			energy = 0;
			currentCommands.setDistanceRemaining(0);
			currentCommands.setBodyTurnRemaining(0);
		}
	}

	protected void updateHeading() {
		boolean normalizeHeading = true;
	
		double turnRate = min(currentCommands.getMaxTurnRate(),
				(.4 + .6 * (1 - (abs(velocity) / Rules.MAX_VELOCITY))) * Rules.MAX_TURN_RATE_RADIANS);
	
		if (currentCommands.getBodyTurnRemaining() > 0) {
			if (currentCommands.getBodyTurnRemaining() < turnRate) {
				bodyHeading += currentCommands.getBodyTurnRemaining();
				gunHeading += currentCommands.getBodyTurnRemaining();
				radarHeading += currentCommands.getBodyTurnRemaining();
				if (currentCommands.isAdjustGunForBodyTurn()) {
					currentCommands.setGunTurnRemaining(
							currentCommands.getGunTurnRemaining() - currentCommands.getBodyTurnRemaining());
				}
				if (currentCommands.isAdjustRadarForBodyTurn()) {
					currentCommands.setRadarTurnRemaining(
							currentCommands.getRadarTurnRemaining() - currentCommands.getBodyTurnRemaining());
				}
				currentCommands.setBodyTurnRemaining(0);
			} else {
				bodyHeading += turnRate;
				gunHeading += turnRate;
				radarHeading += turnRate;
				currentCommands.setBodyTurnRemaining(currentCommands.getBodyTurnRemaining() - turnRate);
				if (currentCommands.isAdjustGunForBodyTurn()) {
					currentCommands.setGunTurnRemaining(currentCommands.getGunTurnRemaining() - turnRate);
				}
				if (currentCommands.isAdjustRadarForBodyTurn()) {
					currentCommands.setRadarTurnRemaining(currentCommands.getRadarTurnRemaining() - turnRate);
				}
			}
		} else if (currentCommands.getBodyTurnRemaining() < 0) {
			if (currentCommands.getBodyTurnRemaining() > -turnRate) {
				bodyHeading += currentCommands.getBodyTurnRemaining();
				gunHeading += currentCommands.getBodyTurnRemaining();
				radarHeading += currentCommands.getBodyTurnRemaining();
				if (currentCommands.isAdjustGunForBodyTurn()) {
					currentCommands.setGunTurnRemaining(
							currentCommands.getGunTurnRemaining() - currentCommands.getBodyTurnRemaining());
				}
				if (currentCommands.isAdjustRadarForBodyTurn()) {
					currentCommands.setRadarTurnRemaining(
							currentCommands.getRadarTurnRemaining() - currentCommands.getBodyTurnRemaining());
				}
				currentCommands.setBodyTurnRemaining(0);
			} else {
				bodyHeading -= turnRate;
				gunHeading -= turnRate;
				radarHeading -= turnRate;
				currentCommands.setBodyTurnRemaining(currentCommands.getBodyTurnRemaining() + turnRate);
				if (currentCommands.isAdjustGunForBodyTurn()) {
					currentCommands.setGunTurnRemaining(currentCommands.getGunTurnRemaining() + turnRate);
				}
				if (currentCommands.isAdjustRadarForBodyTurn()) {
					currentCommands.setRadarTurnRemaining(currentCommands.getRadarTurnRemaining() + turnRate);
				}
			}
		} else {
			normalizeHeading = false;
		}
	
		if (normalizeHeading) {
			if (currentCommands.getBodyTurnRemaining() == 0) {
				bodyHeading = normalNearAbsoluteAngle(bodyHeading);
			} else {
				bodyHeading = normalAbsoluteAngle(bodyHeading);
			}
		}
		if (Double.isNaN(bodyHeading)) {
			Logger.realErr.println("HOW IS HEADING NAN HERE");
		}
	}

	public boolean isRunning() {
		return isRunning.get();
	}
}

