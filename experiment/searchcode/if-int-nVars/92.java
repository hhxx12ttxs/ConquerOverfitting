/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2008 Daniel Le Berre
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *******************************************************************************/
package org.sat4j.sat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.AbstractLauncher;
import org.sat4j.core.ASolverFactory;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.reader.PBInstanceReader;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.SearchListener;
import org.sat4j.tools.DotSearchTracing;

/**
 * This class is used to launch the SAT solvers from the command line. It is
 * compliant with the SAT competition (www.satcompetition.org) I/O format. The
 * launcher is to be used as follows:
 * 
 * <pre>
 *                [solvername] filename [key=value]*
 * </pre>
 * 
 * If no solver name is given, then the default solver of the solver factory is
 * used (@see org.sat4j.core.ASolverFactory#defaultSolver()).
 * 
 * @author leberre
 */
public class Lanceur extends AbstractLauncher {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Lance le prouveur sur un fichier Dimacs.
	 * 
	 * @param args
	 *            doit contenir le nom d'un fichier Dimacs, eventuellement
	 *            compress?.
	 */
	public static void main(final String[] args) {
		AbstractLauncher lanceur = new Lanceur();
		lanceur.run(args);
		System.exit(lanceur.getExitCode().value());
	}

	protected ASolverFactory<ISolver> factory;

	private String filename;

	private int k = -1;

	@SuppressWarnings("nls")
	private Options createCLIOptions() {
		Options options = new Options();

		options.addOption("l", "library", true,
				"specifies the name of the library used (minisat by default)");
		options.addOption("s", "solver", true,
				"specifies the name of a prebuilt solver from the library");
		options.addOption("S", "Solver", true,
				"setup a solver using a solver config string");
		options.addOption("t", "timeout", true,
				"specifies the timeout (in seconds)");
		options.addOption("T", "timeoutms", true,
				"specifies the timeout (in milliseconds)");
		options.addOption("C", "conflictbased", false,
				"conflict based timeout (for deterministic behavior)");
		options.addOption("d", "dot", true,
				"create a sat4j.dot file in current directory representing the search");
		options.addOption("f", "filename", true,
				"specifies the file to use (in conjunction with -d for instance)");
		options.addOption("m", "mute", false, "Set launcher in silent mode");
		options.addOption("k", "kleast", true,
				"limit the search to models having at least k variables set to false");
		options.addOption("r", "trace", true,
				"Search Listener to use for tracing the behavior of the solver");
		Option op = options.getOption("l");
		op.setArgName("libname");
		op = options.getOption("s");
		op.setArgName("solvername");
		op = options.getOption("S");
		op.setArgName("solverStringDefinition");
		op = options.getOption("t");
		op.setArgName("number");
		op = options.getOption("T");
		op.setArgName("number");
		op = options.getOption("C");
		op.setArgName("number");
		op = options.getOption("k");
		op.setArgName("number");
		op = options.getOption("d");
		op.setArgName("filename");
		op = options.getOption("f");
		op.setArgName("filename");
		op = options.getOption("r");
		op.setArgName("searchlistener");
		return options;
	}

	/**
	 * Configure the solver according to the command line parameters.
	 * 
	 * @param args
	 *            the command line
	 * @return a solver properly configured.
	 */
	@SuppressWarnings({ "nls", "unchecked" })
	@Override
	protected ISolver configureSolver(String[] args) {
		Options options = createCLIOptions();
		if (args.length == 0) {
			HelpFormatter helpf = new HelpFormatter();
			helpf.printHelp("java -jar sat4j.jar", options, true);
			return null;
		}
		try {
			CommandLine cmd = new PosixParser().parse(options, args);

			String framework = cmd.getOptionValue("l"); //$NON-NLS-1$
			if (framework == null) { //$NON-NLS-1$
				framework = "minisat";
			}

			try {
				Class<?> clazz = Class
						.forName("org.sat4j." + framework + ".SolverFactory"); //$NON-NLS-1$ //$NON-NLS-2$
				Class<?>[] params = {};
				Method m = clazz.getMethod("instance", params); //$NON-NLS-1$
				factory = (ASolverFactory) m.invoke(null, (Object[]) null);
			} catch (Exception e) { // DLB Findbugs warning ok
				log("Wrong framework: " + framework
						+ ". Using minisat instead.");
				factory = org.sat4j.minisat.SolverFactory.instance();
			}

			ISolver asolver;
			if (cmd.hasOption("s")) {
				log("Available solvers: "
						+ Arrays.asList(factory.solverNames()));
				String solvername = cmd.getOptionValue("s");
				if (solvername == null) {
					asolver = factory.defaultSolver();
				} else {
					asolver = factory.createSolverByName(solvername);
				}
			} else {
				asolver = factory.defaultSolver();
			}

			if (cmd.hasOption("S")) {
				String configuredSolver = cmd.getOptionValue("S");
				if (configuredSolver == null) {
					stringUsage();
					return null;
				}
				asolver = configureFromString(configuredSolver, asolver);
			}

			String timeout = cmd.getOptionValue("t");
			if (timeout == null) {
				timeout = cmd.getOptionValue("T");
				if (timeout != null) {
					asolver.setTimeoutMs(Long.parseLong(timeout));
				}
			} else {
				if (cmd.hasOption("C")) {
					asolver.setTimeoutOnConflicts(Integer.parseInt(timeout));
				} else {
					asolver.setTimeout(Integer.parseInt(timeout));
				}
			}
			filename = cmd.getOptionValue("f");

			if (cmd.hasOption("d")) {
				String dotfilename = null;
				if (filename != null) {
					dotfilename = cmd.getOptionValue("d");
				}
				if (dotfilename == null) {
					dotfilename = "sat4j.dot";
				}
				asolver.setSearchListener(new DotSearchTracing(dotfilename,
						null));
			}

			if (cmd.hasOption("m")) {
				setSilent(true);
			}

			if (cmd.hasOption("k")) {
				Integer myk = Integer.valueOf(cmd.getOptionValue("k"));
				if (myk != null) {
					k = myk.intValue();
				}
			}
			if (cmd.hasOption("r")) {
				String listener = cmd.getOptionValue("r");
				try {

					SearchListener slistener = (SearchListener) Class
							.forName(listener).getConstructor(String.class)
							.newInstance("sat4j.trace");
					asolver.setSearchListener(slistener);
				} catch (InstantiationException e) {
					log("wrong parameter for search listener: "
							+ e.getLocalizedMessage());
				} catch (IllegalAccessException e) {
					log("wrong parameter for search listener: "
							+ e.getLocalizedMessage());
				} catch (ClassNotFoundException e) {
					log("wrong parameter for search listener: " + listener);
				} catch (IllegalArgumentException e) {
					log("wrong parameter for search listener: "
							+ e.getLocalizedMessage());
				} catch (SecurityException e) {
					log("wrong parameter for search listener: "
							+ e.getLocalizedMessage());
				} catch (InvocationTargetException e) {
					log("wrong parameter for search listener: "
							+ e.getLocalizedMessage());
				} catch (NoSuchMethodException e) {
					log("wrong parameter for search listener: "
							+ e.getLocalizedMessage());
				}
			}
			int others = 0;
			String[] rargs = cmd.getArgs();
			if (filename == null && rargs.length > 0) {
				filename = rargs[others++];
			}

			// use remaining data to configure the solver
			while (others < rargs.length) {
				String[] param = rargs[others].split("="); //$NON-NLS-1$
				assert param.length == 2;
				log("setting " + param[0] + " to " + param[1]); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					BeanUtils.setProperty(asolver, param[0], param[1]);
				} catch (Exception e) {
					log("Cannot set parameter : " //$NON-NLS-1$
							+ args[others]);
				}
				others++;
			}

			getLogWriter().println(asolver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
			return asolver;
		} catch (ParseException e1) {
			HelpFormatter helpf = new HelpFormatter();
			helpf.printHelp("java -jar sat4j.jar", options, true);
			usage();
		}
		return null;
	}

	@Override
	protected Reader createReader(ISolver theSolver, String problemname) {
		if (theSolver instanceof IPBSolver) {
			return new PBInstanceReader((IPBSolver) theSolver);
		}
		return new InstanceReader(theSolver);
	}

	@Override
	public void displayLicense() {
		super.displayLicense();
		log("This software uses some libraries from the Jakarta Commons project. See jakarta.apache.org for details."); //$NON-NLS-1$
	}

	@Override
	public void usage() {
		showAvailableSolvers(factory);
	}

	@Override
	protected String getInstanceName(String[] args) {
		return filename;
	}

	@SuppressWarnings("unchecked")
	private final ISolver configureFromString(String solverconfig,
			ISolver theSolver) {
		// AFAIK, there is no easy way to solve parameterized problems
		// when building the solver at runtime.
		StringTokenizer stk = new StringTokenizer(solverconfig, ",");
		Properties pf = new Properties();
		String token;
		String[] couple;
		while (stk.hasMoreElements()) {
			token = stk.nextToken();
			couple = token.split("=");
			pf.setProperty(couple[0], couple[1]);
		}
		Solver aSolver = (Solver) theSolver;
		DataStructureFactory dsf = setupObject("DSF", pf);
		if (dsf != null) {
			aSolver.setDataStructureFactory(dsf);
		}
		LearningStrategy learning = setupObject("LEARNING", pf);
		if (learning != null) {
			aSolver.setLearner(learning);
			learning.setSolver(aSolver);
		}
		IOrder order = setupObject("ORDER", pf);
		if (order != null) {
			aSolver.setOrder(order);
		}
		IPhaseSelectionStrategy pss = setupObject("PHASE", pf);
		if (pss != null) {
			aSolver.getOrder().setPhaseSelectionStrategy(pss);
		}
		RestartStrategy restarter = setupObject("RESTARTS", pf);
		if (restarter != null) {
			aSolver.setRestartStrategy(restarter);
		}
		String simp = pf.getProperty("SIMP");
		if (simp != null) {
			aSolver.setSimplifier(simp);
		}
		SearchParams params = setupObject("PARAMS", pf);
		if (params != null) {
			aSolver.setSearchParams(params);
		}
		String memory = pf.getProperty("MEMORY");
		if ("GLUCOSE".equalsIgnoreCase(memory)) {
			log("configuring MEMORY");
			aSolver.setLearnedConstraintsDeletionStrategy(aSolver.glucose);
		}
		return theSolver;
	}

	private void stringUsage() {
		log("Available building blocks: DSF, LEARNING, ORDER, PHASE, RESTARTS, SIMP, PARAMS");
		log("Example: -S RESTARTS=org.sat4j.minisat.restarts.LubyRestarts/factor:512,LEARNING=org.sat4j.minisat.learning.MiniSATLearning");
	}

	@SuppressWarnings("unchecked")
	private final <T> T setupObject(String component, Properties pf) {
		try {
			String configline = pf.getProperty(component);
			if (configline == null) {
				return null;
			}
			log("configuring " + component);
			String[] config = configline.split("/");
			T comp = (T) Class.forName(config[0]).newInstance();
			for (int i = 1; i < config.length; i++) {
				String[] param = config[i].split(":"); //$NON-NLS-1$
				assert param.length == 2;
				try {
					// Check first that the property really exists
					BeanUtils.getProperty(comp, param[0]);
					BeanUtils.setProperty(comp, param[0], param[1]);
				} catch (Exception e) {
					log("Problem with component " + config[0] + " " + e);
				}
			}
			return comp;
		} catch (InstantiationException e) {
			log("Problem with component " + component + " " + e);
		} catch (IllegalAccessException e) {
			log("Problem with component " + component + " " + e);
		} catch (ClassNotFoundException e) {
			log("Problem with component " + component + " " + e);
		}
		return null;
	}

	@Override
	protected IProblem readProblem(String problemname)
			throws FileNotFoundException, ParseFormatException, IOException,
			ContradictionException {
		ISolver theSolver = (ISolver) super.readProblem(problemname);
		if (k > 0) {
			IVecInt literals = new VecInt();
			for (int i = 1; i <= theSolver.nVars(); i++) {
				literals.push(-i);
			}
			theSolver.addAtLeast(literals, k);
			log("Limiting solutions to those having at least " + k
					+ " variables assigned to false");
		}
		return theSolver;
	}
}

