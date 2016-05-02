package weka;

import weka.core.Instance;

public class SMARTStringToWordsFilter extends AbstractStringToWordsFilter {
	
	public enum WeightingScheme {
		BNC, NNC, ONC, ANC, BTC, OKC, NDTC, NDPC, ADPN, ADPSN, ODKN, BDTSN, ADTSN, BTN, NTC, BNN, NNN;
	}

	private WeightingScheme m_Scheme;

	public void setTransform(WeightingScheme scheme) {
		m_Scheme = scheme;
	}

	public WeightingScheme getTransform() {
		return m_Scheme;
	}

	@Override
	protected void doTransform(Instance instance, int firstCopy) {
		try {
			int max = 0, dl = 0;

			for (int index = firstCopy; index < instance.numAttributes(); ++index) {
				int tf = (int) instance.value(index);
				if(instance.isMissing(index) || tf == 0) continue;
				max = Math.max((int) instance.value(index), max);
				dl += tf;
			}
			
			for (int index = firstCopy; index < instance.numAttributes(); ++index) {
				int tf = (int) instance.value(index);
				if(instance.isMissing(index) || tf == 0) continue;
				double a = 0.5 + 0.5 * tf / max;
				//double L = l(tf) / l(m_AvgDocLength);
				double o = o(K1, B, tf, dl);
				int df = m_DocsCounts[index];
				double idf = Math.log(m_NumInstances / (double) df);
				//double p = Math.log(new Double(m_NumInstances - df) / df);
				double k = Math.log(new Double(m_NumInstances - df + 0.5)
						/ (m_NumInstances + 0.5));
				
				int clazz = (int)instance.classValue();
				int df1 = m_PerClassDF.get(index)[clazz];
				int df2 = df - df1;
				
				int N1 = m_NumInstancesPerClass[clazz];
				int N2 = m_NumInstances - N1;

				double dt = Math.log(new Double(N1 * df2) / (N2 * df1));
				double dts = Math.log(new Double(N1 * df2 + 0.5)
						/ (N2 * df1 + 0.5));
				double dp = Math.log(new Double(N1 - df1) * df2
						/ ((N2 - df2) * df1));
				double dps = Math.log(new Double(N1 - df1) * df2 + 0.5)
						/ ((N2 - df2) * df1 + 0.5);
				double dk = Math.log(((0.5 + N1 - df1) * df2 + 0.5)
						/ ((0.5 + N2 - df2) * df1 + 0.5));
				double value = tf;

				switch (m_Scheme) {
				case BNC:
					value = 1.0;
					break;
				case BTC:
					value = idf;
					break;
				case ADPN:
					value = a * dp;
					break;
				case ADPSN:
					value = a * dps;
					break;
				case ADTSN:
					value = a * dts;
					break;
				case ANC:
					value = a;
					break;
				case BDTSN:
					value = dts;
					break;
				case NDPC:
					value = dp * df;
					break;
				case NDTC:
					value = dt * df;
					break;
				case NNC:
					value = (double) tf;
					break;
				case ODKN:
					value = o * dk;
					break;
				case OKC:
					value = o * k;
					break;
				case ONC:
					value = o;
					break;
				case BTN:
					value = idf;
					break;
				case NTC:
					value = idf * tf;
					break;
				case BNN:
					value = 1.0;
					break;
				}
				instance.setValue(index, Math.abs(value));
			}
			
			if(m_Scheme.name().endsWith("C")) {
				double sigma = 0;
				for (int index = firstCopy; index < instance.numAttributes(); ++index) {
					double w = instance.value(index);
					if(instance.isMissing(index) || w == 0) continue;
					sigma += w * w;
				}
				final double ic = Math.sqrt(sigma);
				// Cosine normalize
				for (int index = firstCopy; index < instance.numAttributes(); ++index) {
					double value = instance.value(index);
					if(instance.isMissing(index) || value == 0) continue;
					instance.setValue(index, value / ic);
				}
			}
		} catch (ArithmeticException e) {
			e.printStackTrace();
		}
	}

	final double K1 = 1.2;
	final double B = 0.95;

	public static double l(double tf) {
		return Math.log(tf) + 1;
	}

	public double o(double k1, double b, int tf, int dl) {
		return (k1 + 1) * tf / (k1 * (1 - b + b * dl / m_AvgDocLength) + tf);
	}
}
