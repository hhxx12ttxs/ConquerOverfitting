package it.unical.dimes.processmining.test;

import it.unical.dimes.processmining.bk.ExtractorManager;
import it.unical.dimes.processmining.bk.WorkflowConstraintExtractor;
import it.unical.dimes.processmining.core.CNMining;
import it.unical.dimes.processmining.core.Constraint;
import it.unical.dimes.processmining.core.ConstraintParser;
import it.unical.dimes.processmining.core.Edge;
import it.unical.dimes.processmining.core.FakeDependency;
import it.unical.dimes.processmining.core.Forbidden;
import it.unical.dimes.processmining.core.Graph;
import it.unical.dimes.processmining.core.LogUnfolder;
import it.unical.dimes.processmining.core.Node;
import it.unical.dimes.processmining.core.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;

/**
 * @author frank
 *
 * Created on 2013-09-22, 10:35:40 AM
 */

public class ProductRecallConstraintExtractor {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		boolean enable_constraints = false;

		boolean enable_postProcessing = true;

		boolean enable_bindingComputation = true;

		// SOGLIA CAUSAL SCORE POST PROCESSING
		double sigma_up_cs_diff = 0.2;

		// SOGLIA CAUSAL SCORE CONSTRAINTS
		double sigma_low_cs_constr_edges = 0.0;

		// SOGLIA RUMORE
		double sigma_log_noise = 0;

		double ff = 0.9;

		double relative_to_best = 0.75;

		XLog log = Utils.parseLog(
				"C:\\Users\\flupia\\Dropbox\\ProcessMining-Tetris\\4francesco\\logVarsize_5perc\\all_100_0.mxml", new XFactoryNaiveImpl());



		LinkedList<Forbidden> lista_forbidden = new LinkedList<Forbidden>();

		LinkedList<Constraint> vincoli_positivi = new LinkedList<Constraint>();

		LinkedList<Constraint> vincoli_negati = new LinkedList<Constraint>();

		if (enable_constraints) {

			ConstraintParser cp = new ConstraintParser("Constraints.xml");

			cp.run();

			LinkedList<Constraint> constraints = cp.getConstraints();

			for (Constraint constr : constraints) {

				if (constr.isPositiveConstraint()) {
					vincoli_positivi.add(constr);
				} else {
					for (String body : constr.getBodyList())
						lista_forbidden.add(new Forbidden(body, constr.getHead()));

					vincoli_negati.add(constr);
				}

			}
		}
		LinkedList<Forbidden> lista_forbidden_unfolded = new LinkedList<Forbidden>();

		LinkedList<Constraint> vincoli_positivi_unfolded = new LinkedList<Constraint>();

		LinkedList<Constraint> vincoli_negati_unfolded = new LinkedList<Constraint>();

		CNMining cnm = new CNMining();

		// XLog log =
		// Utils.parseLog("/home/frank/prom/log_giu_set_trace_filtrato_3.xes",
		// new XFactoryNaiveImpl());

		// aggiungi attivita fittizie

		cnm.aggiungiAttivitaFittizia(log);

		// UNFOLD DEL LOG
		Object[] array = LogUnfolder.unfold(log);

		Map<String, Integer> map = (Map<String, Integer>) array[0];

		Map<String, LinkedList<String>> attivita_tracce = (Map<String, LinkedList<String>>) array[1];

		Map<String, LinkedList<String>> traccia_attivita = (Map<String, LinkedList<String>>) array[2];

		if (enable_constraints)
			cnm.creaVincoliUnfolded(vincoli_positivi, vincoli_negati, lista_forbidden, vincoli_positivi_unfolded,
					vincoli_negati_unfolded, lista_forbidden_unfolded, map);

		double[][] csm = cnm.calcoloMatriceDeiCausalScore(log, map, traccia_attivita, ff);

		//				double[][] m = cnm.buildNextMatrix(log, map, traccia_attivita);

		double[][] m = cnm.buildBestNextMatrix(log, map, traccia_attivita, csm, lista_forbidden_unfolded);

	
		if (sigma_log_noise > 0) {

			for (int i = 0; i < m.length; i++) {

				for (int j = 0; j < m.length; j++)

					if (m[i][j] <= sigma_log_noise * traccia_attivita.size())
						// rimuovo gli archi poco frequenti (ovvero quelli che
						// occorrono meno di sigma_2 * logSize volte)
						m[i][j] = 0;
			}

		}

		System.out.println();

		// COSTRUISCO IL GRAFO
		System.out.println("COSTRUISCO GRAFO UNFOLDED ORIGINALE SOLO LOG... ");

		Graph graph = new Graph();

		// LinkedList<Node> lista_nodi = new LinkedList<Node>();
		//
		// for (int s = 0; s < m.length; s++)
		// lista_nodi.add(new Node("" + s));

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			String key = entry.getKey();
			Integer value = entry.getValue();
			Node node = new Node(key, value);

			if (!graph.getMap().containsKey(node))
				graph.getMap().put(node, new LinkedHashSet<Node>());
		}

		for (int p = 0; p < m.length; p++)
			for (int r = 0; r < m[0].length; r++)
				if (m[p][r] > 0) {
					Node np = graph.getNode(cnm.getKeyByValue(map, p), p);

					Node nr = graph.getNode(cnm.getKeyByValue(map, r), r);

					graph.addEdge(np, nr, false);

					np.incr_Outer_degree();
					nr.incr_Inner_degree();
				}

		System.out.println();
		System.out.println();

		cnm.stampaGrafo(graph);

		System.out.println();

		// System.out.println("STAMPA RAPPORTO CAUSAL SCORE");
		// System.out.println();
		// a2bv2.stampaCSDependency(graph, csm);
		// System.out.println();
		//
		// System.out.println("STAMPA ARCHI INDIRETTI");
		// System.out.println();
		// a2bv2.stampaUndirectEdges(graph, csm, map);
		// System.out.println();

		System.out.println("GRAFO FOLDED ORIGINALE SOLO LOG");

		Map<String, Integer> folded_map = new TreeMap<String, Integer>();
		Map<String, LinkedList<String>> folded_attivita_tracce = new TreeMap<String, LinkedList<String>>();
		Map<String, LinkedList<String>> folded_traccia_attivita = new TreeMap<String, LinkedList<String>>();

		Graph folded_G_Ori = cnm.getGrafoAggregato(graph, log, true, folded_map, folded_attivita_tracce,
				folded_traccia_attivita);

		System.out.println();

		cnm.stampaGrafo(folded_G_Ori);

		System.out.println();
		System.out.println();

		String stats = "initial statistics:\n[number of nodes] = " + folded_G_Ori.getMap().keySet().size() + "\n"
				+ "[number of edges] = " + folded_G_Ori.getLista_archi().size() + "\n";
		// CHECK CONSISTENZA VINCOLI

		boolean vincoli_consistenti = cnm.verifica_consistenza_vincoli(vincoli_positivi, vincoli_negati);

		if (!vincoli_consistenti) {
			System.out.println("FALLIMENTO VINCOLI INCONSISTENTI ");
			System.exit(0);
		}

		/***
		 * COSTRUISCO PG0
		 * 
		 * 
		 */

		if (enable_constraints) {
			System.out.println("STAMPA PG0 FOLDED");

			cnm.buildPG0(graph, m, vincoli_positivi_unfolded, vincoli_positivi, vincoli_negati, lista_forbidden,
					lista_forbidden_unfolded, map, attivita_tracce, traccia_attivita, csm, sigma_low_cs_constr_edges,
					folded_G_Ori, folded_map);

			Graph folded_PG0 = cnm.getGrafoAggregato(graph, log, false, folded_map, folded_attivita_tracce,
					folded_traccia_attivita);

			System.out.println();

			cnm.stampaGrafo(folded_PG0);

			System.out.println();
			System.out.println();

			if (!cnm.verificaVincoliPositivi(folded_PG0, null, null, vincoli_positivi, folded_map)) {
				System.out.println("FALLIMENTO PG0 NON SODDISFA I VINCOLI POSITIVI!");
				System.exit(0);
			}
		}
		// LISTA ATTIVITA PARALLELE RILEVATE

		LinkedList<FakeDependency> attivita_parallele = cnm.getAttivitaParallele(m, graph, map, vincoli_positivi,
				folded_map, folded_G_Ori);

		int counter = 1;

		System.out.println("Stampa attivita parallele iniziali...");

		for (FakeDependency ap : attivita_parallele)
			System.out.println("[" + counter++ + "] " + cnm.getKeyByValue(map, ap.getAttivita_x()) + " ; "
					+ cnm.getKeyByValue(map, ap.getAttivita_y()));

		System.out.println();
		System.out.println("START ALGORITMO 2... ");
		System.out.println();

		// algoritmo1(m, graph, map, attivita_tracce, traccia_attivita, csm);

		cnm.algoritmo2(m, graph, map, attivita_tracce, traccia_attivita, csm, sigma_up_cs_diff, folded_map,
				lista_forbidden, vincoli_positivi, vincoli_negati);
		System.out.println();

		System.out.println("GRAFO DOPO AVER APPLICATO ALGORITMO 2");
		System.out.println();

		cnm.stampaGrafo(graph);

		System.out.println("ATTIVITA PARALLELE RESIDUE DOPO ALGORITMO 2...");

		System.out.println();

		Graph folded_g = cnm.getGrafoAggregato(graph, log, false, folded_map, folded_attivita_tracce,
				folded_traccia_attivita);

		// setto le marche di nuovo a false
		for (Node n : graph.listaNodi())
			n.setMark(false);

		LinkedList<FakeDependency> attivita_parallele_residue = cnm.getAttivitaParallele(m, graph, map,
				vincoli_positivi, folded_map, folded_g);

		counter = 1;

		for (FakeDependency ap : attivita_parallele_residue)
			System.out.println("[" + counter++ + "] " + cnm.getKeyByValue(map, ap.getAttivita_x()) + " ; "
					+ cnm.getKeyByValue(map, ap.getAttivita_y()));

		System.out.println();
		System.out.println();

		for (Edge e : folded_g.getLista_archi()) {
			Iterator<Constraint> it = vincoli_positivi.iterator();
			while (it.hasNext()) {
				Constraint c = it.next();
				if (c.getBodyList().contains(e.getX().getNomeAttivita())
						&& c.getHead().equals(e.getY().getNomeAttivita())) {

					e.setFlag(true);
					System.out.println(e + " OK!!!!!!");
					break;
				} else
					System.out.println("NOT OK!!!!!!!");
			}
		}

		System.out.println("GRAFO FOLDED ");

		System.out.println();

		cnm.stampaGrafo(folded_g);

		//		saveGraphAsImage(folded_g, "grafo_folded_ante_pp");

		// eliminiamo gli archi poco frequenti in base al causal score
		double[][] csmOri = cnm.calcoloMatriceDeiCausalScore(log, folded_map, folded_traccia_attivita, ff);

		System.out.println();

		stats += "\nafter main loop:\n" + "[number of edges] = " + folded_g.getLista_archi().size() + "\n";



		//		cnm.toCausalNet(nodiFolded, folded_g, folded_map, "folded" + s.getLogName().substring(0, 1).toUpperCase()
		//				+ s.getLogName().substring(1, s.getLogName().length() - 2), true);

		System.out.println("POST-PROCESSING RIMOZIONE DIPENDENZE INDIRETTE... ");
		System.out.println();

		cnm.postProcessing_dip_indirette(folded_g, folded_map, folded_attivita_tracce, folded_traccia_attivita, csmOri,
				sigma_log_noise, vincoli_positivi);

		cnm.stampaGrafo(folded_g);

		System.out.println();

		System.out.println("POST-PROCESSING RIMOZIONE LINK SOPRAVVISSUTI PARALLELI... ");

		System.out.println();

		cnm.postProcessing_paralleli(folded_g, csmOri, folded_map, folded_attivita_tracce, folded_traccia_attivita,
				sigma_up_cs_diff, sigma_log_noise, vincoli_positivi);

		System.out.println();

		//		System.out.println("RIMOZIONE DIPENDENZE STRANE");
		//		cnm.removeStrangeDependencies(folded_g, folded_map, vincoli_positivi);

		//		Node start = new Node(attivita_iniziale + "+complete", folded_map.get(attivita_iniziale + "+complete"));
		//		Node end = new Node(attivita_finale + "+complete", folded_map.get(attivita_finale + "+complete"));

		Node start = new Node(CNMining.attivita_iniziale, folded_map.get(CNMining.attivita_iniziale));
		Node end = new Node(CNMining.attivita_finale, folded_map.get(CNMining.attivita_finale));

		LinkedList<Node> startActivities = new LinkedList<Node>();

		LinkedList<Node> endActivities = new LinkedList<Node>();

		folded_g = cnm.rimuoviAttivitaFittizie(folded_g, folded_map, folded_traccia_attivita, folded_attivita_tracce,
				start, end, log, startActivities, endActivities);

		cnm.stampaGrafo(folded_g);
		//		cnm.toCausalNet(nodiFolded, folded_g, folded_map, "foldedPP" + s.getLogName().substring(0, 1).toUpperCase()
		//				+ s.getLogName().substring(1, s.getLogName().length() - 2), true);

		// euristica di rimozione bindings poco frequenti

		// soglia

		//		double tau = 0.1;
		//
		//		cnm.removeUnfrequentBindings(folded_g, foldedTraces, nodiFolded, tau);

		if(enable_bindingComputation)
		cnm.computeBindings(folded_g, folded_traccia_attivita, folded_map);

		System.out.println("PROCEDURA REMOVABLE-EDGES ");

		csmOri = cnm.calcoloMatriceDeiCausalScore(log, folded_map, folded_traccia_attivita, ff);

		while (true) {

			// Ricalcolo il set degli archi eliminabili
			LinkedList<Edge> removableEdges = cnm.removableEdges(folded_g, csmOri, vincoli_positivi, folded_map,
					relative_to_best);

			if (removableEdges.size() == 0)
				break;

			Edge bestRemovable = null;

			double worst_causal_score = Double.MAX_VALUE;

			Iterator<Edge> it = removableEdges.iterator();

			while (it.hasNext()) {

				Edge e = it.next();

				double e_cs = csmOri[e.getX().getID_attivita()][e.getY().getID_attivita()];

				if (e_cs < worst_causal_score) {
					worst_causal_score = e_cs;
					bestRemovable = e;
				}
			}

			folded_g.removeEdge(bestRemovable.getX(), bestRemovable.getY());

			System.out.println("RIMOSSO ARCO " + bestRemovable.getX().getNomeAttivita() + " -> "
					+ bestRemovable.getY().getNomeAttivita());
			
			if(enable_bindingComputation) {
			
			//RIMOZIONE DAI BINDING 

			HashMap<TreeSet<Integer>, Integer> obX = bestRemovable.getX().getOutput();

			HashMap<TreeSet<Integer>, Integer> ibY = bestRemovable.getY().getInput();

			for (TreeSet<Integer> ts : obX.keySet())
				ts.remove(bestRemovable.getY().getID_attivita());

			for (TreeSet<Integer> ts : ibY.keySet())
				ts.remove(bestRemovable.getX().getID_attivita());

			//RIMOZIONE DAI BINDING ESTESI

			HashMap<TreeSet<Integer>, Integer> extendedObX = bestRemovable.getX().getExtendedOutput();

			HashMap<TreeSet<Integer>, Integer> extendedIbY = bestRemovable.getY().getExtendedInput();

			for (TreeSet<Integer> ts : extendedObX.keySet())
				ts.remove(bestRemovable.getY().getID_attivita());

			for (TreeSet<Integer> ts : extendedIbY.keySet())
				ts.remove(bestRemovable.getX().getID_attivita());
			
			}
			
			removableEdges.remove(bestRemovable);
			
		}

		cnm.stampaGrafo(folded_g);
		
		boolean[][] realMatrixConstraint = cnm.generaAdjacentsMatrix(folded_g);
		
		//Estrazione constraints dal modello vero (calcolato su all_100_0) di productRecall
							Map<Integer, String> reverse_folded_map = new TreeMap<Integer, String>();
	
							for (Map.Entry<String, Integer> entry : folded_map.entrySet()) {
	
								reverse_folded_map.put(entry.getValue(), entry.getKey());
							}
							

							
							WorkflowConstraintExtractor extractor = new WorkflowConstraintExtractor(
									realMatrixConstraint, folded_map, reverse_folded_map);
							
							for (int shuffleNr =0; shuffleNr<5; shuffleNr++) {
								
								for (int j1 = 10; j1 < 60; j1 += 10) {
									
	//								for (int k1 = 0; k1 < 60; k1 += 10) {
									
//										if(j1 == 0)
//											continue;
										
										ExtractorManager em = new ExtractorManager();
										
										em.extractConstraints(extractor, 0, 0, 0, j1, shuffleNr);
										
										
									}	//								}
								
								extractor.shuffle();
								
								}
							
							
	
							}
							
/*							for (int j1 = 0; j1 < 60; j1 += 10) {
	
//								for (int k1 = 0; k1 < 60; k1 += 10) {
	
									for (int i1 = 0; i1 < 5; i1++) {
	
//										if(j1 == 0 && k1 == 0)
//											continue;
										if(j1 == 0)
											continue;
										
										WorkflowConstraintExtractor extractor = new WorkflowConstraintExtractor(
												realMatrixConstraint, folded_map, reverse_folded_map);
	
	
										ExtractorManager em = new ExtractorManager();
	
										
										em.extractConstraints(extractor, 0, 0, 0, j1, i1);
										
	
									}
//								}
							}*/
		
	}



