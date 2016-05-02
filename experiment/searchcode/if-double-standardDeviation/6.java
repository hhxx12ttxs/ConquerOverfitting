package lrg.insider.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Pattern;

import lrg.common.abstractions.entities.ResultEntity;
import lrg.common.abstractions.plugins.filters.FilteringRule;
import lrg.common.abstractions.plugins.filters.composed.NotComposedFilteringRule;
import lrg.common.metamodel.MetaModel;
import lrg.insider.metamodel.MemoriaCPPModelBuilder;
import lrg.insider.metamodel.MemoriaJavaModelBuilder;
import lrg.insider.metamodel.MemoriaModelBuilder;
import lrg.insider.plugins.filters.memoria.classes.IsInner;
import lrg.memoria.core.System;
import lrg.memoria.utils.Logger;

public class IPlasmaStat {
	// specified in CLI and later used in most methods

	private static String metricFilePath;

	public static String projectName(String source_path) {
		int index = source_path.lastIndexOf(java.io.File.separator);

		if (index < 0)
			return source_path;

		return source_path.substring(index + 1);
	}

	public static System parseCodeAndBuildModel(String language,
			String source_path) {
		String log_file = projectName(source_path) + "_log.txt";
		String cache_file = projectName(source_path) + "_cache.dat";
		java.io.File err = new java.io.File(log_file);
		try {
			err.createNewFile();
			Logger logger = new Logger(new FileOutputStream(err));
			java.lang.System.setOut(logger);
			java.lang.System.setErr(logger);
			java.lang.System.err.println("Building model for source folder "
					+ source_path);

			MemoriaModelBuilder modelBuilder = null;

			if (language.compareTo("cpp") == 0)
				modelBuilder = new MemoriaCPPModelBuilder(source_path,
						cache_file, null);
			else if (language.compareTo("java") == 0)
				modelBuilder = new MemoriaJavaModelBuilder(source_path,
						cache_file, "", null);
			else
				return null;
			MetaModel.createFrom(modelBuilder, source_path);

			logger.close();

			return modelBuilder.getCurrentSystem();

		} catch (Exception pex) {
			return null;
		}
	}

	public static void main(String args[]) {
		lrg.memoria.core.System mySystem;
		ArrayList<String> metricsIdentifiers;

		if (args.length != 4) {
			java.lang.System.err.println("Wrong number of parameters: "
					+ args.length + " instead of 4");
			java.lang.System.err
					.println("Usage: iplasmastat [cpp|java] source_path config_file output_file_path");
			java.lang.System.exit(1);
		}

		metricsIdentifiers = parseConfigFile(args[2]);
		mySystem = parseCodeAndBuildModel(args[0], args[1]);

		if (mySystem == null) {
			java.lang.System.err.println("Model could not be built!");
			java.lang.System.exit(2);
		}

		metricFilePath = args[3];

		try {
			computeAndWriteProperties(mySystem, metricsIdentifiers);
		} catch (InvalidConfigFileArgumentNumberException e) {
			java.lang.System.err.println(e);
			java.lang.System.exit(1);
		}

		java.lang.System.out.println(getStatisticalThreshold("low", "LOC"));

		java.lang.System.out.println(getStatisticalThreshold("high",
				"CYCLO / LOC"));

		java.lang.System.out.println(getStatisticalThreshold("low", "AVG_HIT"));
	}

	public static double getStatisticalThreshold(String thresholdType,
			String metricName) {

		// thresholdType= Low/High/Very High

		File inFile;

		/* Variables needed for computing the thresholds */
		int dataSetSize = 0;
		double mean = 0, standardDeviation = 0;
		/* END Variables needed for computing the thresholds */

		// make inFile if path ends in separator or not
		if (metricFilePath.substring(metricFilePath.length() - 1).equals(
				java.io.File.separator))
			inFile = new File(metricFilePath + "metricsdBase.txt");
		else
			inFile = new File(metricFilePath + java.io.File.separator
					+ "metricsdBase.txt");

		if (Pattern.matches("([a-zA-Z]|_)*", metricName)) { // just metric
			try {
				LineNumberReader ln = new LineNumberReader(new FileReader(
						inFile));
				// dataSetSize = number of lines in the file - 1(the table head)

				while (ln.readLine() != null) {
					dataSetSize++;
				}
				dataSetSize--; // minus the table head
				ln.close();

				// read info and compute mean and standard deviation
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new DataInputStream(new FileInputStream(inFile))));
				String line;
				String[] buff;
				String st = "\\s";
				int i = 0, index = 0;

				// read table head
				line = br.readLine();
				buff = line.split(st);
				for (i = 0; i < buff.length; i++)
					if (buff[i].equalsIgnoreCase(metricName))
						index = i; // determine the index of the desired metric

				// calculate mean
				while ((line = br.readLine()) != null) {
					buff = line.split(st);
					mean += Double.parseDouble(buff[index]);
				}
				mean /= dataSetSize;

				// calculate standard deviation
				br.close();
				// re-reading the file
				br = new BufferedReader(new InputStreamReader(
						new DataInputStream(new FileInputStream(inFile))));
				br.readLine(); // table head is discarded first!
				while ((line = br.readLine()) != null) {
					buff = line.split(st);
					standardDeviation += Math.pow((Double
							.parseDouble(buff[index]) - mean), 2);
				}
				standardDeviation = Math.sqrt(((double) 1 / dataSetSize)
						* standardDeviation);

				// return threshold
				if (thresholdType.equalsIgnoreCase("low")) {
					if (mean - standardDeviation > 0)
						return (mean - standardDeviation);
					else
						return 0;
				}

				if (thresholdType.equalsIgnoreCase("high"))
					return (mean + standardDeviation);

				if (thresholdType.equalsIgnoreCase("very high"))
					return (mean + standardDeviation) * 1.5;

			} catch (IOException e) {
				java.lang.System.out
						.println("Error when reading from metricsdBase.txt: "
								+ e.getMessage());
			}
		}

		if (Pattern.matches("([a-zA-Z]|_)* (\\+|\\-|\\*|\\/) ([a-zA-Z]|_)*",
				metricName)) {
			String[] aux = metricName.split("\\s");
			if (aux[1].equals("+"))
				return (getStatisticalThreshold(thresholdType, aux[0]) + getStatisticalThreshold(
						thresholdType, aux[2]));
			if (aux[1].equals("-"))
				return (getStatisticalThreshold(thresholdType, aux[0]) - getStatisticalThreshold(
						thresholdType, aux[2]));
			if (aux[1].equals("*"))
				return (getStatisticalThreshold(thresholdType, aux[0]) * getStatisticalThreshold(
						thresholdType, aux[2]));
			if (aux[1].equals("/")) {
				if (getStatisticalThreshold(thresholdType, aux[2]) != 0)
					return (getStatisticalThreshold(thresholdType, aux[0]) / getStatisticalThreshold(
							thresholdType, aux[2]));
				else 
					return -2; // error code: division by 0 is not possible!
			}
		}
		return -1; // means something is not OK as thresholds are positive!
	}

	private static void computeAndWriteProperties(System mySystem,
			ArrayList<String> metricsIdentifiers)
			throws InvalidConfigFileArgumentNumberException {

		File outFile;

		/* decimal formating variables */
		DecimalFormatSymbols sy = new DecimalFormatSymbols(Locale.getDefault());
		// sets . as decimal separator no matter what settings the SO has
		sy.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("0.##", sy);
		/* END decimal formating variables */

		/* variables for the standard metrics */
		double loc = 0, cyclo = 0, fanout = 0, fanoutcalls = 0;
		FilteringRule notInnerClass = new NotComposedFilteringRule(
				new IsInner());

		String st = "    ";
		/* END variables for the standard metrics */

		if (metricFilePath.substring(metricFilePath.length() - 1).equals(
				java.io.File.separator))
			outFile = new File(metricFilePath + "metricsdBase.txt");
		else
			outFile = new File(metricFilePath + java.io.File.separator
					+ "metricsdBase.txt");

		try {
			/* ------------ test and write table head ----------- */
			if (!outFile.exists())
				outFile.createNewFile();
			FileWriter fstout = new FileWriter(outFile, true);
			BufferedWriter bw = new BufferedWriter(fstout);

			if (outFile.length() == 0) {// iff file is empty write head of table
				bw.write("CYCLO" + st + "LOC" + st + "NOM" + st + "NOC" + st
						+ "NOP" + st + "AVG_HIT" + st + "AVG_NDD" + st
						+ "FANOUT" + st + "FANOUTCALLS" + st);
				ListIterator iter = metricsIdentifiers.listIterator();
				while (iter.hasNext()) {
					String[] buff = ((String) iter.next()).split("\\s");
					if (buff.length == 1)
						bw.write(buff[0] + st);
					else if (buff.length == 3)
						bw.write(buff[1] + st);
					else
						throw new InvalidConfigFileArgumentNumberException();
				}
				bw.newLine();
			}
			/* ------------------- end table head ------------------------ */

			/* ------------------- write metrics ------------------------ */
			// static metrics
			cyclo = ((Double) mySystem.getGroup("method group").getProperty(
					"CYCLO").aggregate("sum").getValue()).doubleValue();
			cyclo += ((Double) mySystem.getGroup("global function group")
					.getProperty("CYCLO").aggregate("sum").getValue())
					.doubleValue();
			bw.write(df.format(cyclo) + st);

			loc = ((Double) mySystem.getGroup("method group")
					.getProperty("LOC").aggregate("sum").getValue())
					.doubleValue();
			loc += ((Double) mySystem.getGroup("global function group")
					.getProperty("LOC").aggregate("sum").getValue())
					.doubleValue();
			bw.write(df.format(loc) + st);

			bw.write(mySystem.getGroup("method group").union(
					mySystem.getGroup("global function group")).size()
					+ st);

			bw.write(mySystem.getGroup("class group")
					.applyFilter("model class").applyFilter(notInnerClass)
					.size()
					+ st);

			bw.write(mySystem.getGroup("package group").applyFilter(
					"model package").size()
					+ st);

			bw.write(df.format(((Double) mySystem.getProperty("AVG_HIT")
					.getValue()).doubleValue())
					+ st);

			bw.write(df.format(((Double) mySystem.getProperty("AVG_NDD")
					.getValue()).doubleValue())
					+ st);

			fanout = ((Double) mySystem.getGroup("method group").getProperty(
					"FANOUT").aggregate("sum").getValue()).doubleValue();
			fanout += ((Double) mySystem.getGroup("global function group")
					.getProperty("FANOUT").aggregate("sum").getValue())
					.doubleValue();
			bw.write(df.format(fanout) + st);

			fanoutcalls = ((Double) mySystem.getGroup("method group")
					.getProperty("FANOUTCLASS").aggregate("sum").getValue())
					.doubleValue();
			fanoutcalls += ((Double) mySystem
					.getGroup("global function group").getProperty(
							"FANOUTCLASS").aggregate("sum").getValue())
					.doubleValue();
			bw.write(df.format(fanoutcalls) + st);
			bw.flush();
			// end static metrics

			// dynamic metrics
			ListIterator iter = metricsIdentifiers.listIterator();
			while (iter.hasNext()) {
				String[] buff = ((String) iter.next()).split("\\s");
				if (buff.length == 1) {
					/* system metric */
					bw.write(df.format(((ResultEntity) mySystem
							.getProperty(buff[0])).getValue())
							+ st);
				} else if (buff.length == 3) {
					/* metric for an entity */
					bw.write(df
							.format(mySystem.getGroup(buff[2] + " group")
									.getProperty(buff[1]).aggregate(buff[0])
									.getValue())
							+ st);
				}
			}
			// end dynamic metrics
			/* ------------------- end write metrics ------------------------ */
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			java.lang.System.err
					.println("ERROR: when writing/reading in metricdBase File"
							+ e.getMessage());
		}

	}

	private static ArrayList<String> parseConfigFile(String configFileName) {
		ArrayList<String> properties = new ArrayList<String>();
		try {
			FileInputStream fstream = new FileInputStream(configFileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)
				properties.add(strLine);
			in.close();
		} catch (Exception e) {// Catch exception if any
			java.lang.System.err.println("Error: " + e.getMessage());
			java.lang.System.exit(1);
		}
		return properties;
	}
}

