package newExamples.archive;

import gaia.cu1.tools.exception.GaiaException;
import gaia.cu1.tools.numeric.algebra.GVector3d;
import gaia.cu1.params.GaiaParam;
import gaiasimu.GaiaSimuEnums;
import gaiasimu.GaiaSimuTime;
import gaiasimu.SimuException;
import gaiasimu.gaia.Gaia;
import gaiasimu.GaiaSimuEnums.FoV;
import gaiasimu.gaia.spacecraft.Transit;
import gaiasimu.universe.source.Astrometry;
import gaiasimu.universe.source.stellar.ComponentAstrometry;
import gaiasimu.universe.source.stellar.ComponentPhotometry;
import gaiasimu.universe.source.stellar.ExoPlanet;
import gaiasimu.universe.source.stellar.ExoPlanetPhysicalParameters;
import gaiasimu.universe.source.stellar.OrbitalParams;
import gaiasimu.universe.source.stellar.Star;
import gaiasimu.universe.source.stellar.StarPhysicalParameters;
import gaiasimu.universe.source.stellar.StarSystem;
import gaiasimu.universe.source.stellar.StarSystemSimuException;
import gaiasimu.universe.source.stellar.StellarAstrometry;
import gaiasimu.universe.source.stellar.StellarPhysicalParameters;
import gaiasimu.universe.source.stellar.StellarSource;

import java.io.IOException;
import java.util.ArrayList;

import newExamples.gaia.cu4.du438.current_cycle.DmsSimuDU437;


/**
 * Simple example to demonstrate how to use GaiaSimu features
 * @author Fabien Chereau
 */

public class GSInvScanLaw {

	/**
     * Run the example.
     * @param args the command line arguments
	 * @throws IOException
	 * @throws GaiaException 
	 * @throws StarSystemSimuException 
     */
	
	public static void main(String[] args) throws SimuException, GaiaException, IOException, StarSystemSimuException {
		
		// Use DMSSimuDU437 to generate planetary system
		DmsSimuDU437 dmsSimuDU437 = new DmsSimuDU437();
		
		// Exoplanet orbital parameters
		double eccentricity   = 0.27;
		double inclination    = 0.838;   // (rad)
		double nodeAngle      = 0.4;   // position angle of the line of nodes (rad)
		double omega2         = 0.1;   // argument of periastron (rad)
		double period         = 30.26;   // (days)
		//double semiMajorAxis  = 0.13;   // semi-maj. axis (A.U.) of the secondary (a2)
		double timePeriastron = 1.0;   // (days)
		double massPlanet     = 1.0;    // (Jupiter masses)
		double bondAlbedo     = 0.5;	// Albedo; shouldn't matter for astrometry
		
		// Star data
		//long   idRoot   = 1223345453L;
		double magMv    = 10.17;        // Absolute magnitude. 51 Peg: +4.7. GOG does nothing for G < 12.0
		double vMinusI  = 1.52;        // Intrinsec Mean (V-I) colour. 51 Peg: +0.8
		double absV     = 0.0;         // Interstellar absortion in the V band
		double ra       = 343.31972458; // RA  ICRS (deg). 51 Peg: 344.3665854
		double dec      = -14.263700556; // DEC ICRS (deg). 51 Peg: 20.76883222
		//double parallax = 65.10;     // (mas)
		//double muRa     = 208.07;    // Proper motion RA  (mas/yr)
		//double muDec    = 60.96;     // Proper motion DEC (mas/yr)
		double parallax = 213.3;       // (mas). 51 Peg: 64.07 mas
		double distance = 1000 / parallax;// (pc)
		double muRa     = 0.0;           // Proper motion RA  (mas/yr)
		double muDec    = 0.0;           // Proper motion DEC (mas/yr)
		double vRad     = -1.7;       // Radial velocity (km/s)
		double feH      = 0.05;         // [Fe/H]
		double alphaE   = 0.0;         // Alpha elements enhancement
		double mass     = 0.334;         // (Msun?)
		double magBol   = 12.1;         // (Compute from bolometric corrections in Allen)
		int    pop      = 6;           // (Besancon model. 6: 5-7 Gyr)
		String spType   = "M4 V";       // String defining the spectral type
		//double teff     = 5500;       // (G2)
		//double logg     = 4.4;         // log g (cgs)
		//double radius   = 2.0;         // (Rsun?)
		
		// Generate solar system with star and planet

		ExoplanetSystem system = new ExoplanetSystem("GJ 876");
		Star star = system.createStar(spType, magMv, distance, vMinusI, absV, ra, dec, parallax, muRa, muDec, vRad, mass, magBol, pop, feH, alphaE);
		OrbitalParams orbParam = dmsSimuDU437.generateOrbitalParams(
				star, massPlanet / Math.sin(inclination), period, timePeriastron, eccentricity, omega2, nodeAngle, inclination);
		// StellarSource source = dmsSimuDU437.generateSystem( star, orbitalParams );
		
		// Copied for adjustment from DmsSimuDU437.java
		
		int nbCompanions = 1; // only one planet for the moment
		StellarSource[] components = new StellarSource[nbCompanions + 1];
		StellarAstrometry astrom = (StellarAstrometry) star.getAstrometry();
		double massM1 = ((StellarPhysicalParameters) star.getPhysParam()).getMass();
		double massM2 = Math.pow(massM1/orbParam.semiMajorAxis, 3./2.)*
		orbParam.period/GaiaParam.Nature.JULIANYEAR_DAY - massM1;
		massM2 *= GaiaParam.Nature.SUN_MASS/ GaiaParam.Nature.JUPITER_MASS;

		// init primary star

		long primaryID = star.getRootId();
		ComponentAstrometry primaryAstrometry = new ComponentAstrometry();

		ComponentPhotometry primaryPhotometry = new ComponentPhotometry(primaryAstrometry, magMv,
				distance, vMinusI, absV, star.getPhotometry().getExtinctionCurve());
		StarPhysicalParameters origPhys = star.getStarPhysicalParams();

		// TODO: some physical parameters of the primary may not be coherent anymore... 
		StarPhysicalParameters primaryPhys = new StarPhysicalParameters(
				origPhys.getTeff(), origPhys.getFeH(), origPhys.getlogg(), origPhys.getAlphaElements(),
				origPhys.getMass(), origPhys.getSpectralType(), 10.0, origPhys.getMeanRadius(),
				origPhys.getStellarPop(), origPhys.getvsini());

		star = new Star(primaryID, primaryPhotometry, primaryAstrometry, primaryPhys);
		components[0] = star;

		//
		// init Planet
		//
		int compnb = 1;

		double tEqu2 = origPhys.getTeff() * Math.sqrt(origPhys.getMeanRadius() / (2 * orbParam.semiMajorAxis));
		tEqu2 *= Math.pow((1 - bondAlbedo), 1./4);
		
		// Calculating radius -- not necessary for astrometry, could be replaced by simpler model
		final double a[] = {1.2158123, -0.15080468, 0.69730554, -0.017303049, 0.00026388263, -1.2822719e-6,
			3.3135757e-9, -4.8366013e-12, 4.0017118e-15, -1.7477892e-18, 3.1347937e-22};
		double plRadius = a[0] + a[1] * Math.pow(mass, a[2]);
		for (int i = 3; i < a.length; i++) {
			plRadius += a[i] * Math.pow(mass, a[i - 2]);
		}
		
		plRadius *= GaiaParam.Nature.JUPITER_POLARRADIUS/GaiaParam.Nature.SUN_EQUATORIALRADIUS;
		
		// TODO put a correct value for absolute visual and bolometric magnitude, vsini, V-I
		double magMV2 = 32.58, MBol2 = magMV2-4; //Mag MV from Baraffe models.
		double vSini2 = 0, vmi2 = 9.9999;

		// Initialize ComponentAstrometry pointing to the primary and then correct with setCompanion()
		ComponentAstrometry compAstrom = new ComponentAstrometry(compnb, orbParam);
		ExoPlanetPhysicalParameters compPhys = new ExoPlanetPhysicalParameters(
				tEqu2, plRadius, vSini2, massM2
				/ GaiaParam.Nature.SUN_MASS* GaiaParam.Nature.JUPITER_MASS, MBol2, primaryPhys) ;
		
		ComponentPhotometry compPhot = new ComponentPhotometry(compAstrom, magMV2,
				distance, vmi2, absV, star.getPhotometry().getExtinctionCurve());

		StellarSource comp = new ExoPlanet(primaryID, compPhot, compAstrom, compPhys);
		compPhot.setTargetAstroSource(comp);
		compAstrom.setTargetAstroSource(comp);
		compPhys.setTargetAstroSource(comp);

		components[compnb] = comp;

		// parameters for the stellar system itself
		StarSystem starSystem = new StarSystem(primaryID, components, astrom);
		
		// End of copied part
		
		if (starSystem == null)
			System.err.println("Source not generated!");
		
		// Create position vector
		
		GVector3d vICRS = new GVector3d(Math.toRadians(ra), Math.toRadians(dec));
		vICRS.scale(distance*GaiaParam.Nature.PARSEC_METER);
		
		// Compute the list of all the transits of Sirius in the FOV1
		// For the inverse scanning law, we need to give the half aperture of the field
		
		double halfAperture = GaiaParam.Satellite.FOV_AC*GaiaParam.Nature.DEGREE_RADIAN/2;
		
		// Create and initialize a Gaia instance with default values

		Gaia gaia = new Gaia();
		
		// This method returns the list of all the transits
		
		ArrayList<Transit> fov1Transits = gaia.attitude.inverseScan(vICRS, halfAperture, gaia.getTelescope(FoV.FOV1));
		ArrayList<Transit> fov2Transits = gaia.attitude.inverseScan(vICRS, halfAperture, gaia.getTelescope(FoV.FOV2));
		System.out.println("GJ 876 will transit "+fov1Transits.size()+" times in FOV1 during the mission.");
		System.out.println("GJ 876 will transit "+fov2Transits.size()+" times in FOV2 during the mission.");
		
		// Merge arrays
		
		ArrayList<Transit> allTransits = new ArrayList<Transit>();
		allTransits.addAll(fov1Transits);
		allTransits.addAll(fov2Transits);
		
		// Print astrometry information
		
		System.out.println("\n-- astrometric data -- ");
		
		for ( Transit tr : allTransits ) {
			
			final boolean relativity = false; // whether to include relativity
			final double lambda = 600.0; // wavelength in nm

			GaiaSimuTime time = tr.getGSTime();
			Astrometry ast = star.getAstrometry();
			double trAlpha = ast.getCoMRSAstrometricParam(time, relativity).getAlpha();
			double trDelta = ast.getCoMRSAstrometricParam(time, relativity).getDelta();
			
			GVector3d comrs = ast.getCoMRSPosition(time, relativity);
			System.out.println(comrs);
			System.out.println(time);
			System.out.println(tr.getFov());
			GVector3d fprs = gaia.fromCoMRStoFPRS(comrs, time, tr.getFov(), GaiaSimuEnums.FpField.AF, lambda);
			GVector3d fieldAngles = gaia.fromFPRStoFieldAngles(fprs, tr.getFov());
			
			System.out.printf("%.15e,\t%.15e,\t%.15e,\t%.15e,\t%.15e\n", time.getJD(),
					fieldAngles.getX(), fieldAngles.getY(), trAlpha, trDelta);
			
		}
		
	}

}

