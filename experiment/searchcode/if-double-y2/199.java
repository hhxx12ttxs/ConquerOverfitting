package OfflineDemo;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import OfflineDemo.DataStructures.AsteroidBelt;
import OfflineDemo.DataStructures.Moon;
import OfflineDemo.DataStructures.Planet;
import OfflineDemo.DataStructures.Point;
import OfflineDemo.DataStructures.SolarSystem;
import OfflineDemo.DataStructures.Station;
import OfflineDemo.DataStructures.SystemObject;

import com.google.appengine.api.datastore.Key;

public class GalaxyGenerator {


	public static SolarSystem[] generateGalaxy(JspWriter out)
			throws IOException {

		SolarSystem[] gal = new SolarSystem[StaticSettings.getNumOfSystems()];
		out.write(
				"Generating new empty galaxy with slots for " + gal.length
						+ " solarsystems.\n");
		int numOfFeatures = 0;

		// Creates the first solar system at center of galaxy
		gal[0] = createFirstSystem();
		numOfFeatures += gal[0].contents.length;

		for (int systemNum = 1; systemNum < gal.length; systemNum++) {
			// Gets a random existing system from the second half of generated
			// systems
			// Helps for branching outward from center
			// Used to be: int rndSys =
			// (systemNum==1?0:StaticSettings.RNG.nextInt(systemNum-1));
			int rndSys = (systemNum == 1 ? 0 : StaticSettings.RNG
					.nextInt((systemNum) / 2) + (systemNum - 1) / 2);// +((systemNum-1)/2));//
																		// +
																		// (systemNum-1)/2);
			// Uses this system to generate another galaxy within jumping
			// distance
			gal[systemNum] = generateSystem(gal[rndSys].loc);
			numOfFeatures += gal[systemNum].contents.length;
		}

		out.write(
				"Filled those systems with " + numOfFeatures + " features.\n");
		int numOfConnections = 0;

		int singleLinkSystems = 0;

		for (SolarSystem s : gal) {

			Deque<Key> d = new ArrayDeque<Key>();
			SolarSystem closest = gal[0];
			for (int sys2 = 0; sys2 < gal.length; sys2++) {
				if (s.loc.distanceTo(gal[sys2].loc) <= StaticSettings
						.getJumpDistance()
						&& s.loc.distanceTo(gal[sys2].loc) != 0) {
					d.add(gal[sys2].getKey());
				}
				if (s.loc.distanceTo(gal[sys2].loc) < s.loc
						.distanceTo(closest.loc) && gal[sys2] != s)
					closest = gal[sys2];
			}

			if (d.size() < 1) {
				// If the deque is empty at this point, still connect the
				// single closest system.
				d.add(closest.getKey());
				// FIXME: and insert us into their list.
			}

			s.nearbySystems = d.toArray(new Key[0]);
			numOfConnections += s.nearbySystems.length;

			if (s.nearbySystems.length == 1) {
				s.markStarterSystem();
				singleLinkSystems++;
			}
		}
		com.cse2010.group1.PMF.getPM().close();

		out.write("Made " + numOfConnections + " connections.\n");
		out.write(
				"There are " + singleLinkSystems
						+ " systems with only one connection.\n");
		return gal;
	}

	private static SolarSystem generateSystem(Point lstLoc) {
		SolarSystem s = new SolarSystem();

		Double jd = StaticSettings.getJumpDistance() - .001;

		// At center
		if (lstLoc.x > .4 && lstLoc.x < .60 && lstLoc.y < .60 && lstLoc.y > .40) {
			// Get x & y values from cos/sin(-20 - 110)
			double angle = StaticSettings.RNG.nextDouble() * 2 * Math.PI;
			double x2 = lstLoc.x + jd * Math.cos(angle);
			double y2 = lstLoc.y + jd * Math.sin(angle);

			s.loc = new Point(x2, y2);
		} else
		// Quadrant 1
		if (lstLoc.x >= .5 && lstLoc.y >= .5) {
			// Get x & y values from cos/sin(-20 - 110)
			double angle = StaticSettings.RNG.nextDouble() * Math.PI / 1.5;
			double x2 = lstLoc.x + jd * Math.cos(angle);
			double y2 = lstLoc.y + jd * Math.sin(angle);

			s.loc = new Point(x2, y2);
		} else
		// Quadrant 2
		if (lstLoc.x <= .5 && lstLoc.y >= .5) {
			// Get x & y values from cos/sin(90-180)
			double angle = (StaticSettings.RNG.nextDouble() * Math.PI / 1.5)
					+ Math.PI / 2;
			double x2 = lstLoc.x + jd * Math.cos(angle);
			double y2 = lstLoc.y + jd * Math.sin(angle);

			s.loc = new Point(x2, y2);
		} else
		// Quadrant 3
		if (lstLoc.x <= .5 && lstLoc.y <= .5) {
			// Get x & y values from cos/sin(180-270)
			double angle = (StaticSettings.RNG.nextDouble() * Math.PI / 1.5)
					+ Math.PI;
			double x2 = lstLoc.x + jd * Math.cos(angle);
			double y2 = lstLoc.y + jd * Math.sin(angle);

			s.loc = new Point(x2, y2);
		} else
		// Quadrant 4
		if (lstLoc.x >= .5 && lstLoc.y <= .5) {
			// Get x & y values from cos/sin(270-360)
			double angle = (StaticSettings.RNG.nextDouble() * Math.PI / 1.5)
					+ Math.PI * (3 / 2);
			double x2 = lstLoc.x + jd * Math.cos(angle);
			double y2 = lstLoc.y + jd * Math.sin(angle);

			s.loc = new Point(x2, y2);
		}

		// Fill this system with a randomly generated bunch of
		// systemObjects

		int objectCount = StaticSettings.RNG.nextInt(StaticSettings
				.getMaxFeatures());
		Deque<Key> features = new ArrayDeque<Key>();

		for (int i = 0; i < objectCount; i++) {
			features.add(getFeature().getKey());
		}
		s.contents = features.toArray(new Key[0]);

		return s;
	}

	private static SolarSystem createFirstSystem() {
		SolarSystem s = new SolarSystem();
		s.loc = new Point(.5, .5);

		int objectCount = StaticSettings.RNG.nextInt(StaticSettings
				.getMaxFeatures());
		Deque<Key> features = new ArrayDeque<Key>();

		for (int i = 0; i < objectCount; i++) {
			SystemObject j = getFeature();
			com.cse2010.group1.PMF.getPM()
					.makePersistent(j); // PERSIST!
			features.add(j.getKey());
		}
		s.contents = features.toArray(new Key[0]);
		return s;
	}

	private static SystemObject getFeature() {
		int i = StaticSettings.RNG.nextInt(3);
		
		switch (i) {
		case 0:
			SystemObject a = new AsteroidBelt();
			
					com.cse2010.group1.PMF.getPM().makePersistent(a); // PERSIST!

			return a;
		case 1:
			Station s = new Station();
			
			com.cse2010.group1.PMF.getPM().makePersistent(s); // PERSIST!
			return s;
		default:
		case 2:
			Planet p = new Planet();
			p.contents = getPlanetContents();
			com.cse2010.group1.PMF.getPM().makePersistent(p); // PERSIST!
			return p;

		}

	}

	private static Key[] getPlanetContents() {
		int i = StaticSettings.RNG.nextInt(3);
		switch (i) {

		case 1:
			int j = StaticSettings.RNG.nextInt(4) + 1;
			Key[] moons = new Key[j];
			
			for (int k = 0; k < j; k++) {
				Moon s = new Moon();
				
				com.cse2010.group1.PMF.getPM().makePersistent(s); // PERSIST!
				
				moons[k] = s.getKey();
			}
			return moons;
		case 2:
			Station s = new Station();
			Key[] station = { s.getKey()};
			com.cse2010.group1.PMF.getPM().makePersistent(s); // PERSIST!
			return station;

		default:
		case 0:
			return null;

		}

	}
}

