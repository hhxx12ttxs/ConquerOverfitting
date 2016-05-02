package edu.ufl.qure;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.biomatters.geneious.publicapi.components.GLabel;
import com.biomatters.geneious.publicapi.components.GTextArea;
import com.biomatters.geneious.publicapi.components.OptionsPanel;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;

/**
 * Copyright (C) 2012, Mattia C.F. Prosperi <m.prosperi@epi.ufl.edu>, 
 * Marco Salemi <salemi@pathology.ufl.edu>, and Tyler Strickland <tyler@tylers.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */
public class QuRe {
    private static GTextArea txaProg = new GTextArea();
    private static JFrame frmProg = new JFrame();
    public static boolean cancel = false;

    private static void updateTextArea(final String text) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		if (text.equals("\b")) {
		    String txt = txaProg.getText();
		    txaProg.setText(txt.substring(0, txt.length() - 2));
		}
		txaProg.append(text);
	    }
	});
    }

    private static void redirectSystemStreams() {
	OutputStream out = new OutputStream() {
	    @Override
	    public void write(int b) throws IOException {
		updateTextArea(String.valueOf((char) b));
	    }

	    @Override
	    public void write(byte[] b, int off, int len) throws IOException {
		updateTextArea(new String(b, off, len));
	    }

	    @Override
	    public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	    }
	};

	System.setOut(new PrintStream(out, true));
	System.setErr(new PrintStream(out, true));
    }

    public static void closeQuRe() throws DocumentOperationException {
	frmProg.setVisible(false);

    }

    public static void launchQuRe(List<NucleotideSequenceDocument> seqList,
	    String filename, String directory, String referenceFile, double homopolErr,
	    double nonHomopolErr, int iterations) throws Exception {
	try {
	    QuRe.cancel=false;
	    Date d = new Date();
	    long starttime = d.getTime();
	    frmProg = new JFrame("QuRe");
	    OptionsPanel oPanel = new OptionsPanel();
	    GLabel lblProg = new GLabel("Overall Progress:");
	    JProgressBar progBar = new JProgressBar();
	    txaProg = new GTextArea(14, 80);
	    txaProg.setMargin(new Insets(5, 5, 5, 5));
	    txaProg.setEditable(false);
	    JButton btnClose = new JButton("Close");
	    btnClose.setEnabled(false);
	    btnClose.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    frmProg.setVisible(false);
		    QuRe.cancel = true;
		}
	    });
	    JButton btnCancel = new JButton("Cancel");
	    btnCancel.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		    QuRe.cancel = true;
		}
	    });
	    oPanel.addComponentWithLabel(lblProg, progBar, true);
	    oPanel.addSpanningComponent(new JScrollPane(txaProg));
	    oPanel.addTwoComponents(btnClose,btnCancel,false,false);
	    frmProg.setContentPane(oPanel);
	    frmProg.pack();
	    frmProg.setVisible(true);
	    frmProg.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    class WindowEventHandler extends WindowAdapter {
		public void windowClosing(WindowEvent evt) {

		    QuRe.cancel = true;
		}
	    }
	    frmProg.addWindowListener(new WindowEventHandler());
	    redirectSystemStreams();
	    // double homopolErr = 0.0044d;
	    // double nonHomopolErr = 0.0007d;
	    // INT Input Data

	    int num_proc = Runtime.getRuntime().availableProcessors();
	    num_proc = num_proc - 1;
	    num_proc = Math.max(1, num_proc);
	    // INT Output Data
	    System.out
		    .println("parallel processing enabled: no. of cores available = "
			    + num_proc + "\n");
	    progBar.setValue(5);
	    ReadSet rs = new ReadSet();
	    int kmer = 9;
	    // txaProg.append("Reading input files...\n");
	    rs.readFastaFromList(seqList);
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    rs.readReferenceGenome(referenceFile, kmer);
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    progBar.setValue(10);
	    float gop = 23f;
	    float gep = 0.3f;
	    rs.setRandomScoresParallel(2000, gop, gep, num_proc);
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    progBar.setValue(20);
	    rs.alignParallel(num_proc, gop, gep, kmer);
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    progBar.setValue(30);
	    rs.setAllPvalues("BH");
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    progBar.setValue(35);
	    rs.removeBadReads(0.01d);
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    progBar.setValue(40);
	    rs.estimateBaseSet();
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    progBar.setValue(50);
	    rs.correctReadsParallel(0.01d, nonHomopolErr, homopolErr, num_proc);
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    progBar.setValue(60);
	    rs.updatePopulationStats();
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    progBar.setValue(70);
	    FileWriter fw = new FileWriter(directory + "\\" + filename + "_alignedReads.txt");
	    // fw.write("name\tSNP_string\tmappingPosition\tstart\tstop\torientation\tsimilarity\tinsertions\tpvalue\tadjustedPvalue\r\n");
	    fw.write("name\tvariations\tstart\tstop\tadj_pvalue\r\n");
	    for (int i = 0; i < rs.population.size(); i++) {
		if (QuRe.cancel)
		    throw new DocumentOperationException("Cancelled!");
		fw.write(rs.population.get(i).name + "\t");
		fw.write(rs.population.get(i).SNP_string + "\t");
		// fw.write(rs.population.get(i).mappingPosition+"\t");
		fw.write(rs.population.get(i).start + "\t");
		fw.write(rs.population.get(i).stop + "\t");
		// fw.write(rs.population.get(i).orientation+"\t");
		// fw.write(rs.population.get(i).similarity+"\t");
		// fw.write(rs.population.get(i).insertions+"\t");
		// fw.write(rs.population.get(i).pvalue+"\t");
		fw.write(rs.population.get(i).adjustedPvalue + "");
		fw.write("\r\n");
	    }
	    fw.close();

	    LinkedList<Base> base_list = new LinkedList<Base>();
	    Object[] key = rs.baseSet.keySet().toArray();
	    Arrays.sort(key);
	    for (int i = 0; i < key.length; i++) {
		Base b = rs.baseSet.get(key[i]);
		base_list.add(b);
	    }

	    fw = new FileWriter(directory + "\\" + filename + "_snpTable.txt");
	    // fw.write("position\treference\tconsensus\tA\tC\tG\tT\tdel\tcoverage\tprobcoverage\tentropy\r\n");
	    fw.write("position\treference\tconsensus\tA\tC\tG\tT\tdel\tcoverage\tentropy\r\n");
	    for (int i = 0; i < base_list.size(); i++) {
		if (QuRe.cancel)
		    throw new DocumentOperationException("Cancelled!");
		Base b = base_list.get(i);
		fw.write(b.position + "\t");
		fw.write(b.reference + "\t");
		fw.write(b.consensus + "\t");
		fw.write(b.A + "\t");
		fw.write(b.C + "\t");
		fw.write(b.G + "\t");
		fw.write(b.T + "\t");
		fw.write(b.del + "\t");
		fw.write(b.coverage + "\t");
		// boolean indel = false;
		// if (b.reference=='-' || b.consensus=='-' ||
		// Math.floor(b.position)!=b.position)
		// indel=true;
		// fw.write(Functions.isHomopolymeric((int)Math.floor(b.position-1),rs.consensusGenomeNoIndels,indel)+"\t");
		// fw.write(b.probCoverage+"\t");
		fw.write(b.entropy + "\r\n");

	    }
	    fw.close();

	    d = new Date();
	    long stoptime1 = d.getTime();
	    long timePassed1 = stoptime1 - starttime;

	    // INT Output Data
	    System.out.println("alignment and mapping time = " + timePassed1
		    + " ms");
	    progBar.setValue(80);
	    System.out.println("starting Quasispecies Reconstruction (QuRe)");
	    rs.estimateAmpliconsParallel(iterations, num_proc);
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    LocalVariantSetEnsemble lvse = new LocalVariantSetEnsemble(
		    rs.population, rs.ampliconSet, rs.referenceGenome,
		    homopolErr, nonHomopolErr, num_proc);
	    lvse.printToFile(directory + "\\" + filename + "_overlappingIntervalsSet.txt");
	    ArrayList<GlobalVariant> gvFinal = lvse
		    .quasispeciesReconstructor(rs.referenceGenome);
	    while (gvFinal.size() == 0 && lvse.starts.length > 1) {
		if (QuRe.cancel)
		    throw new DocumentOperationException("Cancelled!");
		double[] newStarts = new double[rs.ampliconSet.starts.length - 1];
		double[] newStops = new double[rs.ampliconSet.stops.length - 1];
		if (Math.random() < 0.5d) {
		    for (int i = 0; i < rs.ampliconSet.starts.length - 1; i++) {
			newStarts[i] = rs.ampliconSet.starts[i];
			newStops[i] = rs.ampliconSet.stops[i];
		    }
		    rs.ampliconSet.starts = newStarts;
		    rs.ampliconSet.stops = newStops;
		} else {
		    for (int i = 0; i < rs.ampliconSet.starts.length - 1; i++) {
			newStarts[i] = rs.ampliconSet.starts[i + 1];
			newStops[i] = rs.ampliconSet.stops[i + 1];
		    }
		    rs.ampliconSet.starts = newStarts;
		    rs.ampliconSet.stops = newStops;
		}
		lvse = new LocalVariantSetEnsemble(rs.population,
			rs.ampliconSet, rs.referenceGenome, homopolErr,
			nonHomopolErr, num_proc);
		lvse.printToFile(directory + "\\" + filename + "_overlappingIntervalsSet.txt");
		gvFinal = lvse.quasispeciesReconstructor(rs.referenceGenome);
	    }
	    progBar.setValue(90);
	    gvFinal = Functions.mergeGlobalVariantSet(gvFinal);
	    // INT Output Data
	    System.out.println("\t\tinitial number of variants = "
		    + gvFinal.size());

	    /*
	     * fw = new
	     * FileWriter(filename+"_reconstructedVariantsNocluster.txt"); for
	     * (int i=0; i<gvFinal.size(); i++) {
	     * fw.write(">QuReNC"+i+"_"+gvFinal.get(i).frequency+"\r\n");
	     * fw.write(gvFinal.get(i).sequence+"\r\n"); } fw.close();
	     */
	    // INT Output Data
	    System.out
		    .print("\tfinal clustering (random search + BIC selection) ");
	    if (gvFinal.size() > 1)
		gvFinal = Functions.correct(gvFinal, rs.ampliconSet.starts[0],
			rs.ampliconSet.stops[rs.ampliconSet.stops.length - 1],
			rs.referenceGenome, homopolErr, nonHomopolErr,
			iterations, num_proc);
	    gvFinal = Functions.mergeGlobalVariantSet(gvFinal);
	    Functions.setFrequencies(gvFinal);
	    System.out.println("\t\tfinal number of variants = "
		    + gvFinal.size());
	    if (QuRe.cancel)
		throw new DocumentOperationException("Cancelled!");
	    fw = new FileWriter(directory + "\\" + filename + "_reconstructedVariants.txt");
	    for (int i = 0; i < gvFinal.size(); i++) {
		if (QuRe.cancel)
		    throw new DocumentOperationException("Cancelled!");
		fw.write(">QuRe" + i + "_" + gvFinal.get(i).frequency + "\r\n");
		fw.write(gvFinal.get(i).sequence + "\r\n");
	    }
	    fw.close();

	    d = new Date();
	    long stoptime2 = d.getTime();
	    long timePassed2 = stoptime2 - stoptime1;
	    long timePassed3 = stoptime2 - starttime;
	    progBar.setValue(100);
	    // INT Output Data
	    System.out
		    .println("amplicon estimation and quasispecies reconstruction time = "
			    + timePassed2 + " ms");
	    System.out.println("total time employed = " + timePassed3 + " ms");
	    btnClose.setEnabled(true);
	    btnCancel.setEnabled(false);
	} catch (DocumentOperationException ex) {
	    frmProg.setVisible(false);
	}
    }

    public static void main(String[] args) throws Exception {
	/*
	 * 
	 * "java [-classpath .] [-Xmx{1,2,3,...}G] QuRe read_file reference_genome_file [homopolymericErrorRate nonHomopolymericErrorRate iterations]"
	 * 
	 * The read file and the reference genome file must be in FASTA format.
	 * 
	 * If the last three parameters are not inserted, default values are
	 * used (0.0044, 0.0007, 10000).
	 * 
	 * args: 0: Read File 1: Reference Genome file 2: homopolymericErrorRate
	 * def: 0.0044 3: nonHomopolymericErrorRate def: 0.0007 4: iterations
	 * def: 10000
	 * 
	 * OUTPUT FILES:
	 * 
	 * Please refer to the paper cited above for a detailed description of
	 * methods.
	 * 
	 * "filename_alignedReads.txt" This file reports reads that align
	 * significantly to the reference genome, with the corresponding
	 * start/stop positions of the pairwise alignment and the p-value of the
	 * alignment. Base changes from the reference are reported in the format
	 * r_p_b, i.e. reference base (r), reference position (p), and replaced
	 * base (b). Insertions and deletions are encoded with "-". Insertions
	 * and multiple insertions take a fractional position. For instance a C
	 * insertion at position 150 is encoded as -_150.5_C, and any other
	 * subsequent insertion is added a half of the previous step (i.e.
	 * 150.75, 150.875, ...). Base changes are corrected according to the
	 * Poisson-based error model. "filename_snpTable.txt" This file prints
	 * the positions with respect to the reference genome numbering that
	 * were significantly covered by the read set, with the corresponding
	 * consensus base and the specific base composition for each position.
	 * Read coverage and base composition entropy are also reported. The
	 * file can be used to look at the genome variation base by base
	 * singularly. "filename_overlappingIntervalsSet.txt" This file reports
	 * the optimal set of overlapping intervals, with start, overlap and
	 * stop positions with respect to the reference genome. For each
	 * overlapping interval, all the distinct reads found are reported along
	 * with their relative frequency (%). Reads are defined by their list of
	 * base changes from the reference genome, divided into changes in the
	 * overlapping and non-overlapping parts. Each overlapping interval can
	 * be regarded as a local quasispecies reconstruction, i.e. the distinct
	 * variants found in a particular sub-region of the mapped reference
	 * genome. "filename_reconstructedVariants.txt" This is the final fasta
	 * file where reconstructed variants (i.e. global quasispecies
	 * reconstruction across the whole, mapped reference genome) are
	 * reported along with their estimated prevalence (%).
	 */

	// INT Output Data
	System.out
		.println("----------------------------------------------------------------------------");
	System.out
		.println("----------------------------------------------------------------------------");

	Date d = new Date();
	long starttime = d.getTime();
	// INT Input Data
	String filename = args[0];
	if (args[1].lastIndexOf('.') != -1)
	    filename = args[0].substring(0, args[0].lastIndexOf('.'));

	double homopolErr = 0.0044d;
	double nonHomopolErr = 0.0007d;

	int iterations = 10000;
	// INT Input Data
	if (args.length > 2) {
	    homopolErr = Double.parseDouble(args[2]);
	    nonHomopolErr = Double.parseDouble(args[3]);
	    iterations = Integer.parseInt(args[4]);
	}

	int num_proc = Runtime.getRuntime().availableProcessors();
	num_proc = num_proc - 1;
	num_proc = Math.max(1, num_proc);
	// INT Output Data
	System.out
		.println("parallel processing enabled: no. of cores available = "
			+ num_proc);

	ReadSet rs = new ReadSet();
	int kmer = 9;
	rs.readFasta(args[0]);
	rs.readReferenceGenome(args[1], kmer);

	float gop = 23f;
	float gep = 0.3f;
	rs.setRandomScoresParallel(2000, gop, gep, num_proc);
	rs.alignParallel(num_proc, gop, gep, kmer);
	rs.setAllPvalues("BH");
	rs.removeBadReads(0.01d);
	rs.estimateBaseSet();
	rs.correctReadsParallel(0.01d, nonHomopolErr, homopolErr, num_proc);
	rs.updatePopulationStats();

	FileWriter fw = new FileWriter(filename + "_alignedReads.txt");
	// fw.write("name\tSNP_string\tmappingPosition\tstart\tstop\torientation\tsimilarity\tinsertions\tpvalue\tadjustedPvalue\r\n");
	fw.write("name\tvariations\tstart\tstop\tadj_pvalue\r\n");
	for (int i = 0; i < rs.population.size(); i++) {
	    fw.write(rs.population.get(i).name + "\t");
	    fw.write(rs.population.get(i).SNP_string + "\t");
	    // fw.write(rs.population.get(i).mappingPosition+"\t");
	    fw.write(rs.population.get(i).start + "\t");
	    fw.write(rs.population.get(i).stop + "\t");
	    // fw.write(rs.population.get(i).orientation+"\t");
	    // fw.write(rs.population.get(i).similarity+"\t");
	    // fw.write(rs.population.get(i).insertions+"\t");
	    // fw.write(rs.population.get(i).pvalue+"\t");
	    fw.write(rs.population.get(i).adjustedPvalue + "");
	    fw.write("\r\n");
	}
	fw.close();

	LinkedList<Base> base_list = new LinkedList<Base>();
	Object[] key = rs.baseSet.keySet().toArray();
	Arrays.sort(key);
	for (int i = 0; i < key.length; i++) {
	    Base b = rs.baseSet.get(key[i]);
	    base_list.add(b);
	}

	fw = new FileWriter(filename + "_snpTable.txt");
	// fw.write("position\treference\tconsensus\tA\tC\tG\tT\tdel\tcoverage\tprobcoverage\tentropy\r\n");
	fw.write("position\treference\tconsensus\tA\tC\tG\tT\tdel\tcoverage\tentropy\r\n");
	for (int i = 0; i < base_list.size(); i++) {
	    Base b = base_list.get(i);
	    fw.write(b.position + "\t");
	    fw.write(b.reference + "\t");
	    fw.write(b.consensus + "\t");
	    fw.write(b.A + "\t");
	    fw.write(b.C + "\t");
	    fw.write(b.G + "\t");
	    fw.write(b.T + "\t");
	    fw.write(b.del + "\t");
	    fw.write(b.coverage + "\t");
	    // boolean indel = false;
	    // if (b.reference=='-' || b.consensus=='-' ||
	    // Math.floor(b.position)!=b.position)
	    // indel=true;
	    // fw.write(Functions.isHomopolymeric((int)Math.floor(b.position-1),rs.consensusGenomeNoIndels,indel)+"\t");
	    // fw.write(b.probCoverage+"\t");
	    fw.write(b.entropy + "\r\n");

	}
	fw.close();

	d = new Date();
	long stoptime1 = d.getTime();
	long timePassed1 = stoptime1 - starttime;

	// INT Output Data
	System.out.println("alignment and mapping time = " + timePassed1
		+ " ms");

	System.out.println("starting Quasispecies Reconstruction (QuRe)");
	rs.estimateAmpliconsParallel(iterations, num_proc);

	LocalVariantSetEnsemble lvse = new LocalVariantSetEnsemble(
		rs.population, rs.ampliconSet, rs.referenceGenome, homopolErr,
		nonHomopolErr, num_proc);
	lvse.printToFile(filename + "_overlappingIntervalsSet.txt");
	ArrayList<GlobalVariant> gvFinal = lvse
		.quasispeciesReconstructor(rs.referenceGenome);
	while (gvFinal.size() == 0 && lvse.starts.length > 1) {
	    double[] newStarts = new double[rs.ampliconSet.starts.length - 1];
	    double[] newStops = new double[rs.ampliconSet.stops.length - 1];
	    if (Math.random() < 0.5d) {
		for (int i = 0; i < rs.ampliconSet.starts.length - 1; i++) {
		    newStarts[i] = rs.ampliconSet.starts[i];
		    newStops[i] = rs.ampliconSet.stops[i];
		}
		rs.ampliconSet.starts = newStarts;
		rs.ampliconSet.stops = newStops;
	    } else {
		for (int i = 0; i < rs.ampliconSet.starts.length - 1; i++) {
		    newStarts[i] = rs.ampliconSet.starts[i + 1];
		    newStops[i] = rs.ampliconSet.stops[i + 1];
		}
		rs.ampliconSet.starts = newStarts;
		rs.ampliconSet.stops = newStops;
	    }
	    lvse = new LocalVariantSetEnsemble(rs.population, rs.ampliconSet,
		    rs.referenceGenome, homopolErr, nonHomopolErr, num_proc);
	    lvse.printToFile(filename + "_overlappingIntervalsSet.txt");
	    gvFinal = lvse.quasispeciesReconstructor(rs.referenceGenome);
	}
	gvFinal = Functions.mergeGlobalVariantSet(gvFinal);
	// INT Output Data
	System.out
		.println("\t\tinitial number of variants = " + gvFinal.size());

	/*
	 * fw = new FileWriter(filename+"_reconstructedVariantsNocluster.txt");
	 * for (int i=0; i<gvFinal.size(); i++) {
	 * fw.write(">QuReNC"+i+"_"+gvFinal.get(i).frequency+"\r\n");
	 * fw.write(gvFinal.get(i).sequence+"\r\n"); } fw.close();
	 */
	// INT Output Data
	System.out.print("\tfinal clustering (random search + BIC selection) ");
	if (gvFinal.size() > 1)
	    gvFinal = Functions.correct(gvFinal, rs.ampliconSet.starts[0],
		    rs.ampliconSet.stops[rs.ampliconSet.stops.length - 1],
		    rs.referenceGenome, homopolErr, nonHomopolErr, iterations,
		    num_proc);
	gvFinal = Functions.mergeGlobalVariantSet(gvFinal);
	Functions.setFrequencies(gvFinal);
	System.out.println("\t\tfinal number of variants = " + gvFinal.size());

	fw = new FileWriter(filename + "_reconstructedVariants.txt");
	for (int i = 0; i < gvFinal.size(); i++) {
	    fw.write(">QuRe" + i + "_" + gvFinal.get(i).frequency + "\r\n");
	    fw.write(gvFinal.get(i).sequence + "\r\n");
	}
	fw.close();

	d = new Date();
	long stoptime2 = d.getTime();
	long timePassed2 = stoptime2 - stoptime1;
	long timePassed3 = stoptime2 - starttime;

	// INT Output Data
	System.out
		.println("amplicon estimation and quasispecies reconstruction time = "
			+ timePassed2 + " ms");
	System.out.println("total time employed = " + timePassed3 + " ms");

	System.out
		.println("----------------------------------------------------------------------------");
	System.out
		.println("----------------------------------------------------------------------------");
    }

}

