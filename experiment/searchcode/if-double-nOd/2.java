package it.unibo.alchemist.boundary.monitors;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.model.implementations.molecules.LsaMolecule;
import it.unibo.alchemist.model.implementations.nodes.LsaNode;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.ILsaMolecule;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IReaction;
import it.unibo.alchemist.model.interfaces.ITime;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Francesca Cioffi
 * @version 20120416
 * 
 * 
 */
//CHECKSTYLE:OFF
@Deprecated
public class LsaReportSurvey implements OutputMonitor<Double, Double, List<? extends ILsaMolecule>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2016342612941968861L;
	private double time;
	private StringBuffer output = new StringBuffer();
	private int nsource;
	private String nl = System.getProperty("line.separator");
	// private String filename = "report.txt";
	private int targets;
	private int timestep = 0;
	private final List<int[]> data; // matrix with informations about the
									// simulation
	// private final List<List<Integer>> data = new ArrayList<>();
	private static ArrayList<Integer> idDisplay = new ArrayList<Integer>();

	public LsaReportSurvey(IEnvironment<Double, Double, List<? extends ILsaMolecule>> env, int gradients, List<int[]> row) {

		this.targets = gradients;
		this.data = row;

		for (INode<List<? extends ILsaMolecule>> n : env.getNodes()) {

			LsaNode nod = (LsaNode) n;

			if (nod.getConcentration(new LsaMolecule("person,Type")).size() != 0) {

				// output.append(" ---- PERSON ----- ");
				// output.append(nl);
				// output.append("Id: " + nod.getId());
				// output.append(nl);
				// output.append("Lsa Space: " + nod.getLsaSpace());
				// output.append(nl);

			} else if (nod.getConcentration(new LsaMolecule("source,ID,Type,Type1")).size() != 0) {

				idDisplay.add(nod.getId());

				System.out.println("Nodo " + nsource + " " + nod.getId()); // NOPMD

				nsource += 1;

				// output.append(" ---- SOURCE ----- ");
				// output.append(nl);
				// output.append("Id: " + nod.getId());
				// output.append(nl);
				// output.append("Lsa Space: " + nod.getLsaSpace());
				// output.append(nl);

			}

		}

		// output.append("Number of visitors: " + nperson);
		// output.append(nl);
		// output.append("Number of points of interest: " + nsource);
		// output.append(nl);

	}

	@Override
	public void stepDone(IEnvironment<Double, Double, List<? extends ILsaMolecule>> env, final IReaction<List<? extends ILsaMolecule>> r, ITime time, long step) {

		this.time = time.toDouble();

		if (this.time > timestep) {
			// output.append("Number of visitors: " + nperson);
			// output.append(nl);

			// output.append(" Number of nodes: " + env.getNodes().size());
			// output.append(nl);

			// array with the informations about the simulation:
			// the first column represent the instant of time,
			// the successive targets columns represent the visitors by target,
			// the successive targets columns represent the display by target
			// int dataByTarget[] = new int[1+targets*2];

			// dataByTarget[0] = timestep;

			// int[] dataByTarget = new int[1+targets*2];
			// int[] dataByTarget = new int[1+targets]; // save the timestep and
			// the values of the displays
			int[] dataByTarget = new int[1 + nsource];
			data.add(dataByTarget);

			dataByTarget[0] = timestep;

			// System.out.println("Timestep: " + timestep + "\n");

			for (INode<List<? extends ILsaMolecule>> n : env.getNodes()) {

				LsaNode nod = (LsaNode) n;

				// if(nod.getConcentration(new
				// LsaMolecule("person,Type")).size()!=0){
				//
				//
				// for(int i=0; i<targets; i++){
				// if (nod.getConcentration(new
				// LsaMolecule("person,target"+i)).size()!=0){
				// dataByTarget[1+i]++;
				// // System.out.println( "1 - Index: " + i+1 + " " +
				// dataByTarget[1+i] + "\n");
				// // dataByTarget.set(1+i, dataByTarget.get(1+i)+1);
				// }
				// }
				//
				// }

				if (nod.getConcentration(new LsaMolecule("source,ID,Type,Type1")).size() != 0) {

					for (int i = 0; i < nsource; i++) {
						for (int j = 0; j < targets; j++) {
							if (nod.getConcentration(new LsaMolecule("source," + idDisplay.get(i) + ",target" + j + ",Type1")).size() != 0) {
								// System.out.println("Info: " +
								// idDisplay.get(i) + ", target" + j);
								dataByTarget[1 + i] = j;
								break;
							}
						}
					}
				}

				// if(nod.getConcentration(new
				// LsaMolecule("source,ID,Type,Type1")).size()!=0){
				//
				// for(int i=0; i<targets; i++){
				// if (nod.getConcentration(new
				// LsaMolecule("source,ID,target"+i+",Type1")).size()!=0){
				// dataByTarget[1+targets+i]++;
				// // System.out.println( "2 - Index: " + 1+targets+i + " " +
				// dataByTarget[1+targets+i] + "\n");
				// // dataByTarget.set(1+targets+i,
				// dataByTarget.get(1+targets+i)+1);
				// }
				// }
				//
				// }

				// if(nod.getConcentration(new
				// LsaMolecule("source,ID,Type,Type1")).size()!=0){
				// for(int i=0; i<targets; i++){
				// if (nod.getConcentration(new
				// LsaMolecule("source,ID,target"+i+",Type1")).size()!=0){
				// dataByTarget[1+i]++;
				// // System.out.println( "2 - Index: " + 1+targets+i + " " +
				// dataByTarget[1+targets+i] + "\n");
				// // ataByTarget.set(1+targets+i,
				// dataByTarget.get(1+targets+i)+1);
				// }
				// }
				//
				// }

			}

			// int prop = nperson/nsource;
			//
			// for(int i=0; i<dataByTarget.length; i++){
			// if (i>targets){
			// dataByTarget[i] = prop*dataByTarget[i];
			// output.append(dataByTarget[i] + "\t");
			// System.out.println(dataByTarget[i] + "\n");
			// }
			// else{
			// output.append(dataByTarget[i] + "\t");
			// System.out.println(dataByTarget[i] + "\n");
			// }
			// }

			for (int i = 0; i < dataByTarget.length; i++) {
				output.append(dataByTarget[i] + "\t");
				// System.out.println(dataByTarget[i] + "\n");
			}

			output.append(nl);

			timestep++;

		}

	}

	public double rounds(double number, int nDecimal) {
		return Math.round(number * Math.pow(10, nDecimal)) / Math.pow(10, nDecimal);
	}

	public String getResult() {
		return output.toString();
	}

}

