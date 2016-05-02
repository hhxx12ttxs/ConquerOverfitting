package wisematches.server.web.controllers.playground;

import org.junit.Test;
import wisematches.playground.tracking.RatingCurve;
import wisematches.server.web.utils.RatingChart;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class PlayerProfileControllerTest {
	public PlayerProfileControllerTest() {
	}

	final String[] NAMES = new String[]{"Jun", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

	@SuppressWarnings("unchecked")
	private RatingCurve getRatingCurve(int resolution, Date start, Date end) {
		int prev = 1200;
		int count = ((int) ((end.getTime() - start.getTime()) / 1000 / 60 / 60 / 24)) / resolution;
		List list = new ArrayList();
		for (int i = 0; i < count; i++) {
			short rating = (short) (prev + -10 + Math.random() * 20);
			list.add(new Object[]{i, (short) (rating - 10), rating, (short) (rating + 20), rating});
			prev = rating;
		}
		return new RatingCurve(resolution, start, end, list);
	}

	@Test
	public void asd() {
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.MONTH, 1);
		final int middle = c.get(Calendar.MONTH) + 1;
		final Date end = c.getTime();

		c.add(Calendar.DAY_OF_YEAR, -365);
		final Date start = c.getTime();

		RatingChart chart = new RatingChart(getRatingCurve(10, start, end), middle);

		StringBuilder b = new StringBuilder();
		b.append("http://chart.apis.google.com/chart");
		b.append("?chf=bg,s,67676700");
		b.append("&chxl=0:|");

		final int[] monthIndexes = chart.getMonthIndexes();
		for (int i = 0; i < monthIndexes.length; i++) {
			if (i % 2 != 0) {
				b.append(NAMES[monthIndexes[i] - 1]);
			}
			b.append("|");
		}
		b.append("1:|");
		for (int i = chart.getMinRating(); i < chart.getMaxRating(); i += 100) {
			b.append(i);
			b.append("|");
		}
		b.append(chart.getMaxRating());
		b.append("&chxs=0,676767,11.5,0,lt,676767|1,676767,15.5,0,l,676767");
		b.append("&chxt=x,y");
		b.append("&chs=300x150");
		b.append("&cht=lxy");
		b.append("&chco=008000,FFCC33,AA0033");
		b.append("&chd=e:");
//		b.append(chart.getEncodedPoints()).append(",");
//		b.append(chart.getEncodedRatingsMin()).append(",");
//		b.append(chart.getEncodedPoints()).append(",");
//		b.append(chart.getEncodedRatingsAvg()).append(",");
//		b.append(chart.getEncodedPoints()).append(",");
//		b.append(chart.getEncodedRatingsMax()).append(",");
		b.append("&chg=8.33,25");
		b.append("&chls=1,4,4|1|1,4,4");

		System.out.println(b.length());
		System.out.println(b);
	}
}

