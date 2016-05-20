package dulw;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.log10;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

public abstract class SignalsUtils {

	public static final double chartStep = 0.0001;

	private static double mySinc(double t) {
		if (t == 0.0) {
			return 1;
		} else {
			return sin(PI * t) / (PI * t);
		}
	}

	/**
	 * use this function to get XYSeries from linear function i.e. sinus
	 * Important, const noise and gausse noise are treat linear functions
	 * 
	 * @param function
	 * @param t1
	 *            is a start time
	 * @param d
	 *            is a duration
	 * @return
	 * @throws FunctionEvaluationException
	 */
	public static XYSeries packLinearFunctionToXYSeries(
			UnivariateRealFunction function, double t1, double d)
			throws FunctionEvaluationException {
		XYSeries data = new XYSeries("Skok jednostkowy");
		for (double t = t1; t <= (d + t1); t += chartStep) {
			data.add(t, function.value(t));
		}
		return data;
	}

	/**
	 * use this function to get XYSeries from discretic functions i.e. impulse
	 * 
	 * @param function
	 * @param t1
	 *            is a start time
	 * @param d
	 *            is a duration
	 * @param f
	 *            is a frequency
	 * @return
	 * @throws FunctionEvaluationException
	 */
	public static XYSeries packDiscretFunctionToXYSeries(
			UnivariateRealFunction function, double t1, double d, double f)
			throws FunctionEvaluationException {
		XYSeries data = new XYSeries("Skok jednostkowy");
		for (double t = t1; t <= (d + t1); t += 1 / f) {
			data.add(t, function.value(t));
		}
		return data;
	}

	/**
	 * this method simpling signal with given frequency
	 * 
	 * @param function
	 * @param t1
	 * @param d
	 * @param f
	 * @return
	 * @throws FunctionEvaluationException
	 */
	public static List<double[]> simplingSignal(
			UnivariateRealFunction function, double t1, double d,
			double simplingFrequency) throws FunctionEvaluationException {
		List<double[]> simples = new ArrayList<double[]>();
		double table[];
		for (double t = t1; t <= (d + t1); t += 1 / simplingFrequency) {
			table = new double[2];
			table[0] = t;
			table[1] = function.value(t);
			simples.add(table);
		}
		return simples;
	}

	/**
	 * 
	 * @param series
	 * @return
	 */
	public static List<double[]> fromXYSeriesToList(XYSeries series) {
		List<double[]> simples = new ArrayList<double[]>();
		@SuppressWarnings("unchecked")
		List<XYDataItem> items = series.getItems();
		double table[];
		for (XYDataItem item : items) {
			table = new double[2];
			table[0] = item.getXValue();
			table[1] = item.getYValue();
			simples.add(table);
		}
		return simples;
	}

	/**
	 * 
	 * @param simples
	 * @return
	 */
	public static XYSeries fromListToXYSeries(List<double[]> simples) {
		XYSeries series = new XYSeries("");
		for (double[] table : simples) {
			series.add(table[0], table[1]);
		}
		return series;
	}

	/**
	 * 
	 * @param tab
	 *            tablica wartoci kolejnych próbek
	 * @param t1
	 *            czas poczštkowy
	 * @param simplingFrequency
	 *            częstotliwoć próbkowania filtrowanego sygnału
	 * @return
	 */
	public static XYSeries fromTableToXYSeries(double[] tab, double t1,
			int simplingFrequency, boolean isCorellation) {
		XYSeries xs = new XYSeries("");
		double simpleTime = t1;
		if (isCorellation) {
			simpleTime -= 2.0 / simplingFrequency;
		}
		for (double d : tab) {
			xs.add(simpleTime, d);
			simpleTime += 1.0 / simplingFrequency;
		}
		return xs;
	}

	/**
	 * 
	 * @param series
	 *            XYSeries
	 * @param description
	 *            opis histogramu
	 * @param bins
	 *            liczba przedziałów
	 * @return
	 */
	public static HistogramDataset toHistogram(XYSeries series,
			String description, int bins) {
		@SuppressWarnings("unchecked")
		List<XYDataItem> simples = series.getItems();
		double values[] = new double[simples.size()];
		int index = 0;
		for (XYDataItem item : simples) {
			values[index] = item.getYValue();
			++index;
		}
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries(description, values, bins);
		return dataset;
	}

	/**
	 * 
	 * @param series
	 *            List<double[2]>
	 * @param description
	 *            opis histogramu
	 * @param bins
	 *            liczba przedziałów
	 * @return
	 */
	public static HistogramDataset toHistogram(List<double[]> series,
			String description, int bins) {
		double values[] = new double[series.size()];
		int index = 0;
		for (double[] item : series) {
			values[index] = item[1];
			++index;
		}
		HistogramDataset dataset = new HistogramDataset();
		dataset.addSeries(description, values, bins);
		return dataset;
	}

	public static XYSeries ZohInterpolation(SignalWrapper wrapper) {
		XYSeries data = new XYSeries("");
		List<double[]> rowData = wrapper.getData();
		double lastValue = rowData.get(0)[1];
		int index = 1;
		// generuje dużo punktów żeby funkcja przypominała cišgłš
		for (double t = wrapper.getT1(); t <= (wrapper.getD() + wrapper.getT1()); t += chartStep) {

			if (index < rowData.size() - 1 && t >= rowData.get(index)[0]) {
				++index;
				lastValue = rowData.get(index)[1];
				data.add(t, lastValue);
			} else {
				data.add(t, lastValue);
			}
		}
		return data;
	}

	public static XYSeries FohInterpolation(SignalWrapper wrapper) {
		XYSeries data = new XYSeries("");
		List<double[]> rowData = wrapper.getData();
		// dane do interpolacji liniowej
		double y0 = rowData.get(0)[1];
		double y1 = rowData.get(1)[1];
		double h = 1 / wrapper.getSimplingFrequency();// podstawa dużego
														// trójkšta
		double x0 = rowData.get(0)[0];
		double x1 = rowData.get(1)[0];
		int index = 1;
		double value;
		// generuje dużo punktów żeby funkcja przypominała cišgłš
		for (double t = wrapper.getT1(); t <= (wrapper.getD() + wrapper.getT1()); t += chartStep) {

			if (index < rowData.size() - 1 && t >= rowData.get(index)[0]) {
				++index;
				y0 = y1;
				y1 = rowData.get(index)[1];
				x0 = x1;
				x1 = rowData.get(index)[0];
			}
			// interpolacja liniowa
			value = y0 + ((t - x0) * (y1 - y0)) / h;
			data.add(t, value);
		}
		return data;
	}

	/**
	 * method use sinc function for interpolation
	 * 
	 * @param wrapper
	 * @param precision
	 * @return
	 */
	public static XYSeries sincInterpolation(SignalWrapper wrapper,
			int precision) {
		XYSeries data = new XYSeries("");
		List<double[]> rowData = wrapper.getData();
		// generuje dużo punktów żeby funkcja przypominała cišgłš
		for (double t = wrapper.getT1(); t <= (wrapper.getD() + wrapper.getT1()); t += chartStep) {

			// obliczam numer próbki najbliżej czasu t
			double tempN = (t - wrapper.getT1())
					* wrapper.getSimplingFrequency();
			int closerN = (int) tempN;

			double sum = 0;// wynik interpolacji dla danego t
			// próbki brane pod uwagę przy obliczaniu wartoci dla danego t
			for (int n = closerN - (precision / 2); n <= closerN
					+ (precision / 2); ++n) {
				// czy indeks w zakresie
				if (n >= 0 && n < rowData.size()) {
					sum += (rowData.get(n)[1])
							* mySinc((t - wrapper.getT1())
									* wrapper.getSimplingFrequency() - n);
				}
			}
			data.add(t, sum);
		}
		return data;
	}

	@Deprecated
	public static List<double[]> quantCut(List<double[]> data, double A,
			int noQuants) {
		List<double[]> kwantData = new ArrayList<double[]>();
		double step = A * 2 / (noQuants);// nie wiem czy nie trzeba odjšc jeden
											// od liczby poziomów
		double[] tab;
		for (double[] d : data) {
			tab = new double[2];
			tab[0] = d[0];
			// okreslanie kwantu
			double lastStep = -A;
			for (double i = -A; i <= A; i += step) {
				if (d[1] >= i) {
					lastStep = i;
				} else {
					break;
				}
			}
			tab[1] = lastStep;
			kwantData.add(tab);
		}
		return kwantData;
	}

	public static List<double[]> quantCutTreeSet(List<double[]> data, double A,
			int noQuants) {
		List<double[]> kwantData = new ArrayList<double[]>();
		double step = A * 2 / (noQuants);// nie wiem czy nie trzeba odjšc jeden
											// od liczby poziomów
		double[] tab;
		TreeSet levels = new TreeSet();
		for (double i = -A; i <= A; i += step) {
			levels.add(i);
		}
		for (double[] d : data) {
			tab = new double[2];
			tab[0] = d[0];
			// okreslanie kwantu
			tab[1] = (Double) levels.floor(d[1]);
			kwantData.add(tab);
		}
		return kwantData;
	}

	@Deprecated
	public static List<double[]> quantRound(List<double[]> data, double A,
			int noQuants) {
		List<double[]> kwantData = new ArrayList<double[]>();
		double step = A * 2 / (noQuants);
		double[] tab;
		TreeSet levels = new TreeSet();
		for (double i = -A; i <= A; i += step) {
			levels.add(i);
		}
		for (double[] d : data) {
			tab = new double[2];
			tab[0] = d[0];
			// okreslanie kwantu
			double flr = (Double) levels.floor(d[1]);
			tab[1] = d[1] >= (flr + step / 2) ? flr + step : flr;
			kwantData.add(tab);
		}
		return kwantData;
	}

	public static List<double[]> quantRoundTreeSet(List<double[]> data,
			double A, int noQuants) {
		List<double[]> kwantData = new ArrayList<double[]>();
		double step = A * 2 / (noQuants);// nie wiem czy nie trzeba odjšc jeden
											// od liczby poziomów
		double[] tab;
		for (double[] d : data) {
			tab = new double[2];
			tab[0] = d[0];
			// okreslanie kwantu
			double lastStep = -A;
			for (double i = -A; i <= A; i += step) {
				if (d[1] >= i) {
					lastStep = i;
				} else {
					if (d[1] >= lastStep + (step / 2)) {
						lastStep = i;
					}
					break;
				}
			}
			tab[1] = lastStep;
			kwantData.add(tab);
		}
		return kwantData;
	}

	public static double MSE(List<double[]> orginal, List<double[]> recovered) {
		if (orginal.size() != recovered.size()) {
			throw new IllegalArgumentException(
					"sygnały muszš mieć tyle samo próbek w MSE");
		}
		double mse = 0;
		for (int i = 0; i < orginal.size(); ++i) {
			mse += pow(recovered.get(i)[1] - orginal.get(i)[1], 2.0);
		}
		return mse / orginal.size();
	}

	public static double SNR(List<double[]> orginal, List<double[]> recovered) {
		if (orginal.size() != recovered.size()) {
			throw new IllegalArgumentException(
					"sygnały muszš mieć tyle samo próbek w SNR");
		}
		double mse = 0;
		double snr = 0;
		for (int i = 0; i < orginal.size(); ++i) {
			mse += pow(recovered.get(i)[1] - orginal.get(i)[1], 2.0);
			snr += pow(orginal.get(i)[1], 2.0);
		}
		return 10 * log10(snr / mse);
	}

	public static double SNR(List<double[]> orginal, List<double[]> recovered,
			double mse) {
		if (orginal.size() != recovered.size()) {
			throw new IllegalArgumentException(
					"sygnały muszš mieć tyle samo próbek w SNR");
		}
		double snr = 0;
		for (int i = 0; i < orginal.size(); ++i) {
			snr += pow(orginal.get(i)[1], 2.0);
		}
		return 10 * log10(snr / mse);
	}

	public static double PSNR(List<double[]> orginal, List<double[]> recovered,
			double A) {
		if (orginal.size() != recovered.size()) {
			throw new IllegalArgumentException(
					"sygnały muszš mieć tyle samo próbek w PSNR");
		}
		return 10 * log10(A / MSE(orginal, recovered));
	}

	public static double MD(List<double[]> orginal, List<double[]> recovered) {
		if (orginal.size() != recovered.size()) {
			throw new IllegalArgumentException(
					"sygnały muszš mieć tyle samo próbek w PSNR");
		}
		double max = 0;
		for (int i = 0; i < orginal.size(); ++i) {
			double dif = abs(orginal.get(i)[1] - recovered.get(i)[1]);
			if (dif > max) {
				max = dif;
			}
		}
		return max;
	}

	// convolution (splot)
	// oznaczenia zgodnie ze wzorem splotu
	public static double[] convolution(List<double[]> h, List<double[]> x) {
		int yn = h.size() + x.size() - 1;
		int m = h.size() - 1;
		double[] conv = new double[yn];

		for (int n = 0; n < yn; ++n) {
			double sum = 0;
			for (int k = 0; k <= m; ++k) {
				int nk = n - k;
				// sprawdzanie zakresów
				if (k < h.size() && nk >= 0 && nk < x.size()) {
					sum += h.get(k)[1] * x.get(nk)[1];
				}
			}
			conv[n] = sum;
		}
		return conv;
	}

	// convolution (splot)
	// oznaczenia zgodnie ze wzorem splotu
	public static double[] convolution(double[] h, List<double[]> x) {
		int yn = h.length + x.size() - 1;
		int m = h.length - 1;
		double[] conv = new double[yn];

		for (int n = 0; n < yn; ++n) {
			double sum = 0;
			for (int k = 0; k <= m; ++k) {
				int nk = n - k;
				// sprawdzanie zakresów
				if (k < h.length && nk >= 0 && nk < x.size()) {
					sum += h[k] * x.get(nk)[1];
				}
			}
			conv[n] = sum;
		}
		return conv;
	}

	// korelacja1
	public static double[] corellation(List<double[]> h, List<double[]> x) {
		int yn = h.size() + x.size() - 1;
		int m = h.size() - 1;
		double[] cor = new double[yn];

		int index = 0;
		for (int n = -yn / 2; n < yn / 2 + 1; ++n) {
			double sum = 0;
			for (int k = 0; k <= m; ++k) {
				int nk = k - n;
				// sprawdzanie zakresów
				if (k < h.size() && nk >= 0 && nk < x.size()) {
					sum += h.get(k)[1] * x.get(nk)[1];
				}
			}
			cor[index] = sum;
			++index;
		}
		return cor;
	}

	/**
	 * Buduje filtr dolnoprzepustowy z oknem prostokštnym
	 * 
	 * @param M
	 *            rzšd filtru
	 * @param fo
	 *            częstotliwoć obcięcia
	 * @param fp
	 *            częstotliwoć próbkowania
	 * @return
	 */
	public static double[] filter(int m, int f0, int fp, boolean upper,
			boolean hamming) {
		double[] h = new double[m];// współczynniki h(n)
		double k;
		if(upper){
			double f00 = f0 - fp/4;
			k = fp/f00;
		}else{
			k = fp / f0;
		}
		for (int n = 0; n < m; ++n) {
			if (n == (m - 1) / 2) {
				h[n] = 2 / k;
			} else {
				h[n] = sin((2 * PI * (n - (m - 1) / 2)) / k)
						/ (PI * (n - (m - 1) / 2));
			}
			//górnoprzepustowy
			if (upper) {
				h[n] *= pow(-1.0, n);
			}
			//okno hamminga
			if (hamming) {
				h[n] *= 0.53836 - 0.46164 * cos(2 * PI * n / m);
			}
		}
		return h;
	}

	/**
	 * 
	 * @param corelation wynik korelacji
	 * @return liczba próbek mięczy maksymalnš a rodkowš
	 */
	public static int indexDiff(double[] corelation, double T, double fp) {
		double max = 0;
		int index = (int)(corelation.length / 2);// numer próbki maksymalnej
		for (int i = (int)(corelation.length / 2); i < corelation.length; ++i) {
			if (corelation[i] > max) {
				max = corelation[i];
				index = i;
			}
		}
		return index - (corelation.length / 2);
	}
	
	/**
	 * 
	 * @param indexDiff numer próbki maksymalnej w odniesieniu do rodka corelacji
	 * @param fs częstotliwoć próbkowania
	 * @param V prędkoć rozchodzenia się fali w orodku
	 * @return dystans do obiektu
	 */
	public static double distance(int indexDiff, int fs, double V){
		double time = (double)indexDiff / (double)fs;
		return time * V / 2.0;
	}
}

