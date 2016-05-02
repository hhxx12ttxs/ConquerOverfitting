package it.unibo.alchemist.model.implementations.environments;

import java.util.ArrayList;
import java.util.List;

import it.unibo.alchemist.model.implementations.environments.Continous2DEnvWSocialLayer;
import it.unibo.alchemist.model.implementations.molecules.Molecule;
import it.unibo.alchemist.model.implementations.molecules.MoleculeFactory;
import it.unibo.alchemist.model.implementations.nodes.SocialNode;
import it.unibo.alchemist.model.implementations.positions.Continuous2DEuclidean;
import it.unibo.alchemist.model.interfaces.INode;

/**
 * @author Luca Mella
 * 
 */
public class Plain2DSpaceWCavemanSociety extends Continous2DEnvWSocialLayer<Double> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7649052870594684166L;
	/**
	 * 
	 */
	private final int nPOI;
	/**
	 * 
	 */
	private final List<INode<Double>> POIs;

	/**
	 * @param minDist
	 *            Minimum distance for physical neighbor autolink (2D euclidean)
	 * @param maxDist
	 *            Maximum distance for physical neighbor autolink (2D euclidean)
	 * @param nPoi
	 *            Number of POI in the environment and consequentially number of
	 *            different interest and also number of communities
	 */
	public Plain2DSpaceWCavemanSociety(final double minDist, final double maxDist, final int nPoi) {
		super(minDist, maxDist);
		this.nPOI = (nPoi > 0 ? nPoi : 1);
		this.POIs = new ArrayList<>(this.nPOI);

		createAndAddPOIs();

		this.setSocialAutolinker(new CavemanWInterest(this, this.socialNeighCache, this.POIs));

	}

	/**
	 * Place Points of Interests
	 */
	private void createAndAddPOIs() {
		int ncol, i, x, y;
		ncol = (int) Math.floor(Math.sqrt(nPOI));
		for (i = 0, x = 0, y = 0; i < nPOI; i++, x++) {
			if (x >= ncol) {
				x = 0;
				y++;
			}
			// Create A POI node whit an interest
			INode<Double> n = new SocialNode();
			n.setConcentration(Molecule.getMoleculeByName(MoleculeFactory.POIMOLPREFIX + i), 1.0);
			// Place it sufficiently far from other POIs (we don't want
			// auto-linking between POIs)
			this.addNonSocialNode(n, new Continuous2DEuclidean(x * getMaxDistance() * 15, y
					* getMaxDistance() * 15));
			this.POIs.add(n);
		}
	}

	/**
	 * @return
	 */
	public List<? extends INode<Double>> getPOIs() {
		return this.POIs;
	}

}

