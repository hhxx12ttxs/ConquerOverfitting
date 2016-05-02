/**
 * Copyright (c) 2008 Raise Partner
 * 22, av. Doyen Louis Weil,
 * 38000 Grenoble, France
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: sebastien@chassande.fr
 */
package com.raisepartner.chartfusion.web.server.xmlchartbuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import com.raisepartner.chartfusion.web.client.servletapi.xmlchart.ChartParamName;

/**
 * Common implementation of XMLChartBuilder
 * @author S.Chassande-Barrioz
 */
public abstract class XMLChartBuilder {


	/**
	 * @return an integer value representing the type of chart.
	 */
	public abstract int getChartType();

	/**
	 * computes the XML data required by the FusionChart animation.
	 * @param requestParameterProvider is the holder of the request parameters
	 * @param sessionParamterProvider is the holder of the session parameters.
	 * @return a String containing the XML data
	 * @throws TechnicalException
	 */
	public abstract String getXMLData(ParameterProvider requestParameterProvider,
			ParameterProvider sessionParamterProvider);

	protected String buildRequestIdentifier(String[] paramNames, ParameterProvider pp) {
		StringBuffer sb = new StringBuffer();
		String sep = "";
		for (int i = 0; i < paramNames.length; i++) {
			sb.append(sep);
			sep = ",";
			sb.append(paramNames[i]);
			sb.append("=");
			sb.append((String) pp.getParameter(paramNames[i]));
		}
		return sb.toString();
	}

	protected Date getBeginDate(ParameterProvider requestParameterProvider) {
		long l = getlongParam(requestParameterProvider, ChartParamName.BEGIN_DATE, -1);
		return l == -1 ? null : new Date(l);
	}

	protected byte getByteParam(ParameterProvider requestParameterProvider, String paramName) {
		String str = (String) requestParameterProvider.getParameter(paramName);
		return Integer.valueOf(str).byteValue();
	}


	protected Date getDate(ParameterProvider requestParameterProvider) {
		long l = getlongParam(requestParameterProvider, ChartParamName.DATE, -1);
		return l == -1 ? null : new Date(l);
	}

	protected Date getEndDate(ParameterProvider requestParameterProvider) {
		long l = getlongParam(requestParameterProvider, ChartParamName.END_DATE, -1);
		return l == -1 ? null : new Date(l);
	}

	protected Integer getIntParam(ParameterProvider requestParameterProvider, String paramName) {
		String str = (String) requestParameterProvider.getParameter(paramName);
		try {
			return Integer.valueOf(str).intValue();
		} catch (NumberFormatException e) {
			return null;
		}
	}

	protected Locale getLocale(String l) {
		Locale[] ls = Locale.getAvailableLocales();
		for (int i = 0; i < ls.length; i++) {
			if (ls[i].getLanguage().equals(l)) {
				return ls[i];
			}
		}
		return Locale.getDefault();
	}

	protected long getlongParam(ParameterProvider requestParameterProvider, String paramName,
			long defaultValue) {
		Long l = getLongParam(requestParameterProvider, paramName);
		return l == null ? defaultValue : l.longValue();
	}

	protected Long getLongParam(ParameterProvider requestParameterProvider, String paramName) {
		String str = (String) requestParameterProvider.getParameter(paramName);
		try {
			return Long.valueOf(str);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public double getMarge(double min, double max) {
		return Math.abs(max - min) * 0.1;
	}

	public int getPrecision(double d) {
		if (d < 0.0001) {
			return 4;
		} else if (d < 0.001) {
			return 3;
		} else if (d < 0.01) {
			return 2;
		} else if (d < 0.1) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * @see #getScale(double, double, int, double) Auto compute the orgin
	 */
	public double[] getScale(double dataMin, double dataMax, int nbinterval) {
		if (dataMax < dataMin) {
			return getScale(dataMax, dataMin, nbinterval);
		} else {
			double nbInterval = nbinterval;
			double d = Math.abs(dataMax - dataMin) / nbInterval;
			int i;
			for (i = SCALE_UNITS.length - 1; i >= 0 && SCALE_UNITS[i] > d; i--)
				;
			final double unit;
			if (i == (SCALE_UNITS.length - 1)) {
				unit = SCALE_UNITS[i];
			} else {
				unit = SCALE_UNITS[i + 1];
			}
			double middle = roundSup((dataMax + dataMin) / 2, unit);
			return getScale(dataMin, dataMax, nbinterval, middle);
		}
	}

	/**
	 * computes min, max and increment for a scale
	 * @param dataMin is the min value
	 * @param dataMax is the max max
	 * @param nbinterval is the number of expected interval
	 * @param origin
	 * @return
	 */
	public double[] getScale(double dataMin, double dataMax, int maxNbInterval, double origin) {
		if (dataMax < dataMin) {
			return getScale(dataMax, dataMin, maxNbInterval, origin);
		} else if (Double.compare(dataMax, dataMin) == 0) {
			if (Double.compare(dataMax, 0) == 0) {
				return getScale(-2.0, 2.0, maxNbInterval, origin);
			} else {
				return getScale(dataMin * 0.9, dataMax * 1.1, maxNbInterval, origin);
			}
		} else if (maxNbInterval < 1 || origin > dataMax || origin < dataMin) {
			return null;
		}
		double nbInterval = maxNbInterval;
		double d = Math.abs(dataMax - dataMin) / nbInterval;
		int i;
		for (i = SCALE_UNITS.length - 1; i >= 0 && SCALE_UNITS[i] > d; i--)
			;
		final double unit;
		if (i == (SCALE_UNITS.length - 1)) {
			unit = SCALE_UNITS[i];
		} else {
			unit = SCALE_UNITS[i + 1];
		}
		d = (origin - dataMin) / unit;
		double d2 = Math.floor(d);
		if (d2 < d) {
			d2 += 1.0;
		}
		double min = roundInf(origin - (unit * d2), unit);
		double max = Math.min(roundSup(dataMax, unit),
				roundSup(min + unit * (nbInterval + 1), unit));
		return new double[] { min, max, unit };
	}

	public int[] getVisibleLabel(int nbLabel, int nbVisibleLabel) {
		int[] enabledLabels = new int[nbLabel];
		Arrays.fill(enabledLabels, 0);
		for (int k = 0; k < nbVisibleLabel; k++) {
			int idx = (k * (nbLabel - 1)) / (nbVisibleLabel - 1);
			enabledLabels[idx] = 1;
		}
		return enabledLabels;
	}

	/**
	 * 
	 * @param dates
	 * @return
	 */
	protected static String[] dateToLabel(Date[] dates, int freq) {
		String[] res = new String[dates.length];
		final DateFormat df;
		switch (freq) {
		default :
		case 0 :
			df = new SimpleDateFormat("dd-MM-yy HH:mm");
			break;
		case 1 :
			df = new SimpleDateFormat("dd-MM-yy");
		}
		for (int i = 0; i < dates.length; i++) {
			res[i] = df.format(dates[i]);
		}
		return res;
	}
	
	protected static String[] toLabel(Date[] dates, int frequency) {
		String[] res = new String[dates.length];
		Calendar c = Calendar.getInstance();
		int i = 0;
		for (int j = 0; j < dates.length; j++) {
			Date d = dates[i];
			c.setTime(d);
			StringBuffer sb = new StringBuffer();
			switch (frequency) {
			case Calendar.DAY_OF_MONTH:
				sb.append(toString(c.get(Calendar.DAY_OF_MONTH), 2)).append("/");
			case Calendar.MONTH:
				sb.append(toString(c.get(Calendar.MONTH) + 1, 2)).append("/");
			case Calendar.YEAR:
				sb.append(toString(c.get(Calendar.YEAR) % 100, 2));
			}
			res[i] = sb.toString();
			i++;
		}
		return res;
	}

	public static String toString(int number, int minNbDigit) {
		if (number < 0) {
			return "-" + toString(-number, minNbDigit);
		}
		String s = Integer.toString(number);
		int nb0 = minNbDigit - s.length();
		if (nb0 > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < nb0; i++) {
				sb.append('0');
			}
			sb.append(s);
			return sb.toString();
		} else {
			return s;
		}
	}

	public final static double[] SCALE_UNITS = new double[] { 1e-18, 2e-18, 5e-18, 1e-17, 2e-17,
			5e-17, 1e-16, 2e-16, 5e-16, 1e-15, 2e-15, 5e-15, 1e-14, 2e-14, 5e-14, 1e-13, 2e-13,
			5e-13, 1e-12, 2e-12, 5e-12, 1e-11, 2e-11, 5e-11, 1e-10, 2e-10, 5e-10, 1e-9, 2e-9, 5e-9,
			1e-8, 2e-8, 5e-8, 1e-7, 2e-7, 5e-7, 1e-6, 2e-6, 5e-6, 1e-5, 2e-5, 5e-5, 1e-4, 2e-4,
			5e-4, 1e-3, 2e-3, 5e-3, 1e-2, 2e-2, 5e-2, 1e-1, 2e-1, 5e-1, 1.0, 2.0, 5.0, 1e1, 2e1,
			5e1, 1e2, 2e2, 5e2, 1e3, 2e3, 5e3, 1e4, 2e4, 5e4, 1e5, 2e5, 5e5, 1e6, 2e6, 5e6, 1e7,
			2e7, 5e7, 1e8, 2e8, 5e8, 1e9, 2e9, 5e9, 1e10, 2e10, 5e10, 1e11, 2e11, 5e11, 1e12, 2e12,
			5e12, 1e13, 2e13, 5e13, 1e14, 2e14, 5e14, 1e15, 2e15, 5e15, 1e16, 2e16, 5e16, 1e17,
			2e17, 5e17, 1e18, 2e18, 5e18, };

	public final static double roundInf(double val, double unit) {
		double d = val / unit;
		double d2 = Math.floor(d);
		return d2 * unit;
	}

	public final static double roundSup(double val, double unit) {
		double d = val / unit;
		double d2 = Math.floor(d);
		if (d2 < d) {
			d2 += 1.0;
		}
		return d2 * unit;
	}

	public static <T> List<T> toList(T[] t) {
		ArrayList<T> l = new ArrayList<T>(t.length);
		for (int i = 0; i < t.length; i++) {
			l.add(t[i]);
		}
		return l;
	}

	public static String wrap(String str, int size) {
		StringBuffer sb = new StringBuffer();
		int currentLength = 0;
		final String delims = " -.:'/\\";
		StringTokenizer st = new StringTokenizer(str, delims, true);
		while (st.hasMoreTokens()) {
			final String token = st.nextToken();
			final int tokenLength = token.length();
			if (tokenLength > size) {
				// Token is greater than the wrap size
				if (currentLength > 0) {
					sb.append("\n");
				}
				sb.append(token).append("\n");
				currentLength = 0;
			} else {
				if ((currentLength + tokenLength) <= size) {
					sb.append(token);
					currentLength += tokenLength;
				} else {
					sb.append("\n").append(token);
					currentLength = tokenLength;
				}
			}
		}
		return sb.toString();
	}
}
