package airptool.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;

import airptool.core.coefficients.BaroniUrbaniCoefficientStrategy;
import airptool.core.coefficients.DotProductCoefficientStrategy;
import airptool.core.coefficients.HamannCoefficientStrategy;
import airptool.core.coefficients.ICoefficientStrategy;
import airptool.core.coefficients.JaccardCoefficientStrategy;
import airptool.core.coefficients.KulczynskiCoefficientStrategy;
import airptool.core.coefficients.OchiaiCoefficientStrategy;
import airptool.core.coefficients.PSCCoefficientStrategy;
import airptool.core.coefficients.PhiBinaryDistance;
import airptool.core.coefficients.RelativeMatchingCoefficientStrategy;
import airptool.core.coefficients.RogersTanimotoCoefficientStrategy;
import airptool.core.coefficients.RussellRaoCoefficientStrategy;
import airptool.core.coefficients.SMCCoefficientStrategy;
import airptool.core.coefficients.SokalBinaryDistanceCoefficientStrategy;
import airptool.core.coefficients.SokalSneath2CoefficientStrategy;
import airptool.core.coefficients.SokalSneath4CoefficientStrategy;
import airptool.core.coefficients.SokalSneathCoefficientStrategy;
import airptool.core.coefficients.SorensonCoefficientStrategy;
import airptool.core.coefficients.YuleCoefficientStrategy;
import airptool.util.FormatUtil;

public class SuitableModule {
	private static final ICoefficientStrategy[] coefficientStrategies = { new JaccardCoefficientStrategy(), new SMCCoefficientStrategy(),
			new YuleCoefficientStrategy(), new HamannCoefficientStrategy(), new SorensonCoefficientStrategy(),
			new RogersTanimotoCoefficientStrategy(), new SokalSneathCoefficientStrategy(), new RussellRaoCoefficientStrategy(),
			new BaroniUrbaniCoefficientStrategy(), new SokalBinaryDistanceCoefficientStrategy(), new OchiaiCoefficientStrategy(),
			new PhiBinaryDistance(), new PSCCoefficientStrategy(), new DotProductCoefficientStrategy(),
			new KulczynskiCoefficientStrategy(), new SokalSneath2CoefficientStrategy(), new SokalSneath4CoefficientStrategy(),
			new RelativeMatchingCoefficientStrategy() };

	public static StringBuilder calculateAll(final DataStructure ds, final String classUnderAnalysis, final String expectedModule,
			final Collection<? extends Object> dependenciesClassUnderAnalysis, final Map<String, Collection<? extends Object>> packagesDependencies,
			final Collection<? extends Object> universeOfDependencies) {
		StringBuilder resume = new StringBuilder();
		resume.append(classUnderAnalysis + "\t");

		Map<Class<? extends ICoefficientStrategy>, Set<Object[]>> suitableModulesByCoefficient = calculate(ds, classUnderAnalysis,
				coefficientStrategies, dependenciesClassUnderAnalysis, packagesDependencies, universeOfDependencies);

		for (ICoefficientStrategy cs : coefficientStrategies) {
			Set<Object[]> suitableModules = suitableModulesByCoefficient.get(cs.getClass());

			if (suitableModules != null) {
				int i = 0;
				boolean flag = true;
				for (Object[] o : suitableModules) {
					i++;
					if (o[0].equals(expectedModule) && !cs.getClass().equals(SokalBinaryDistanceCoefficientStrategy.class)) {
						resume.append(i + "\t" + FormatUtil.formatDouble((Double) o[1]) + "\t");
						flag = false;
						break;
					} else if (o[0].equals(expectedModule) && cs.getClass().equals(SokalBinaryDistanceCoefficientStrategy.class)) {
						resume.append(suitableModules.size() - i + 1 + "\t" + FormatUtil.formatDouble((Double) o[1]) + "\t");
						flag = false;
						break;
					}
				}
				if (flag) {
					resume.append(++i + "\t" + "0" + "\t");
				}
			}
		}

		resume.append("\t");

		return resume;
	}

	private static Map<Class<? extends ICoefficientStrategy>, Set<Object[]>> calculate(final DataStructure ds,
			final String classUnderAnalysis, final ICoefficientStrategy[] coefficientStrategies,
			final Collection<? extends Object> dependenciesClassUnderAnalysis, final Map<String, Collection<? extends Object>> packagesDependencies,
			final Collection<? extends Object> universeOfDependencies) {

		Map<Class<? extends ICoefficientStrategy>, Set<Object[]>> result = new LinkedHashMap<Class<? extends ICoefficientStrategy>, Set<Object[]>>();
		for (ICoefficientStrategy cs : coefficientStrategies) {
			result.put(cs.getClass(), new TreeSet<Object[]>(new Comparator<Object[]>() {
				@SuppressWarnings("unchecked")
				@Override
				public int compare(Object[] o1, Object[] o2) {
					return ((Comparable<Double>) o2[1]).compareTo((Double) o1[1]);
				}
			}));
		}

		/*
		 * Also, if dependenciesClassA was empty, we do not calculate the
		 * suitable module.
		 */
		if (dependenciesClassUnderAnalysis.isEmpty()) {
			return null;
		}

		for (String respectiveModuleName : packagesDependencies.keySet()) {
			/*
			 * If dependencyType is null, the function above will consider all
			 * dependencies
			 */
			final Collection<? extends Object> dependenciesPackageUnderAnalysis = packagesDependencies.get(respectiveModuleName);

			int a = CollectionUtils.intersection(dependenciesClassUnderAnalysis, dependenciesPackageUnderAnalysis).size(); // numberAB
			int b = CollectionUtils.subtract(dependenciesClassUnderAnalysis, dependenciesPackageUnderAnalysis).size(); // numberAsubB
			int c = CollectionUtils.subtract(dependenciesPackageUnderAnalysis, dependenciesClassUnderAnalysis).size(); // numberBsubA
			int d = universeOfDependencies.size() - a - b - c; // numberNotAB

			for (ICoefficientStrategy cs : coefficientStrategies) {
				double similarity = cs.calculate(a, b, c, d);

				/* In order to avoid NaN values */
				if (!Double.isNaN(similarity) && !Double.isInfinite(similarity)) {
					result.get(cs.getClass()).add(new Object[] { respectiveModuleName, similarity });
				}

			}

		}
		return result;
	}

}
