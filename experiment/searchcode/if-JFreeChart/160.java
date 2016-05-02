package com.springone.myrestaurants.web;

import static org.springframework.data.document.mongodb.query.Criteria.*;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;
import com.springone.myrestaurants.dao.RestaurantDao;
import com.springone.myrestaurants.domain.Restaurant;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.document.analytics.ControllerCounter;
import org.springframework.data.document.mongodb.MongoTemplate;
import org.springframework.data.document.mongodb.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/charts")
@Controller
public class ChartController {

	@Autowired
	private RestaurantDao restaurantDao;


	@RequestMapping("/favorites.png")
	public void renderChart(String variation, OutputStream stream)
			throws Exception {
		boolean rotate = "rotate".equals(variation); // add ?variation=rotate to
		// the URL to rotate the
		// chart
		JFreeChart chart = generateChart(rotate);
		ChartUtilities.writeChartAsPNG(stream, chart, 750, 400);
	}

	@RequestMapping("/controllers.png")
	public void renderControllers(String controllerName, OutputStream stream)
			throws Exception {
		JFreeChart chart = generateControllerChart(controllerName);
		ChartUtilities.writeChartAsPNG(stream, chart, 750, 400);
	}

	private JFreeChart generateControllerChart(String controllerName) {
		DefaultCategoryDataset dataset = getControllerData(controllerName);

		String xAxisLabel;
		String title;
		if (controllerName != null) {
			xAxisLabel = controllerName;
			title = controllerName + " Actions";
		} else {
			xAxisLabel = "Controllers";
			title = "Controller Invocations";
		}
		return ChartFactory.createBarChart(title,
				xAxisLabel,
				"Number of times invoked", // y-axis label
				dataset, PlotOrientation.VERTICAL, true, // legend displayed
				true, // tooltips displayed
				false); // no URLs*/
	}

	private JFreeChart generateChart(boolean rotate) {
		DefaultCategoryDataset dataset = getFavoritesData();

		return ChartFactory.createBarChart("Favorited Restaurants", // title
				"Restaurants", // x-axis label
				"Number of times recommended", // y-axis label
				dataset, rotate ? PlotOrientation.HORIZONTAL
				: PlotOrientation.VERTICAL, true, // legend displayed
				true, // tooltips displayed
				false); // no URLs*/
	}

	private DefaultCategoryDataset getControllerData(String controllerName) {
		MongoTemplate mongoTemplate;
		DefaultCategoryDataset ds = null;
		try {
			Mongo m = new Mongo();
			mongoTemplate = new MongoTemplate(m, "mvc");

			List<ControllerCounter> counters;
			ds = new DefaultCategoryDataset();

			if (controllerName != null) {
				counters = mongoTemplate.find(new Query(where("name").is(controllerName)), ControllerCounter.class, "counters");
				for (ControllerCounter controllerCounter : counters) {
					Map<String, Double> methodInvocations = controllerCounter.getMethods();
					Set<Entry<String, Double>> es = methodInvocations.entrySet();
					for (Entry<String, Double> entry : es) {
						ds.addValue(entry.getValue(), "invoked", entry.getKey());
					}
				}
			} else {
				counters = mongoTemplate.findAll(ControllerCounter.class, "counters");
				for (ControllerCounter controllerCounter : counters) {
					ds.addValue(controllerCounter.getCount(), "invoked (aggregate)", controllerCounter.getName());
				}
			}
			/*
			if (result instanceof BasicDBList) {
				BasicDBList dbList = (BasicDBList) result;
				for (Iterator iterator = dbList.iterator(); iterator.hasNext();) {
					DBObject dbo = (DBObject) iterator.next();
					System.out.println(dbo);
					Restaurant r = restaurantDao.findRestaurant(Long.parseLong(dbo.get("parameters.p1").toString()));
					ds.addValue(Double.parseDouble(dbo.get("count").toString()), "recommended", r.getName());
				}
			}*/
			return ds;


		} catch (Exception e) {
			e.printStackTrace();
		}
		return ds;
	}


	private DefaultCategoryDataset getFavoritesData() {
		MongoTemplate mongoTemplate;
		DefaultCategoryDataset ds = null;
		try {
			Mongo m = new Mongo();
			mongoTemplate = new MongoTemplate(m, "mvc");

			DBObject result = getTopRecommendedRestaurants(mongoTemplate);
			/* Example data.
			 * [ { "parameters.p1" : "1" , "count" : 5.0} , 
			 *   { "parameters.p1" : "2" , "count" : 6.0} , 
			 *   { "parameters.p1" : "3" , "count" : 3.0} , 
			 *   { "parameters.p1" : "4" , "count" : 8.0}]
			 */
			ds = new DefaultCategoryDataset();
			if (result instanceof BasicDBList) {
				BasicDBList dbList = (BasicDBList) result;
				for (Iterator iterator = dbList.iterator(); iterator.hasNext(); ) {
					DBObject dbo = (DBObject) iterator.next();
					System.out.println(dbo);
					Restaurant r = restaurantDao.findRestaurant(Long.parseLong(dbo.get("parameters.p1").toString()));
					ds.addValue(Double.parseDouble(dbo.get("count").toString()), "recommended", r.getName());
				}
			}
			return ds;


		} catch (Exception e) {
			e.printStackTrace();
		}
		return ds;


	}


	public DBObject getTopRecommendedRestaurants(MongoTemplate mongoTemplate) {
		//This circumvents exception translation
		DBCollection collection = mongoTemplate.getCollection("mvc");

		Date startDate = createDate(1, 5, 2010);
		Date endDate = createDate(1, 12, 2010);

		DBObject cond = QueryBuilder.start("date").greaterThanEquals(startDate).lessThan(endDate).and("action").is("addFavoriteRestaurant").get();
		DBObject key = new BasicDBObject("parameters.p1", true);

		DBObject intitial = new BasicDBObject("count", 0);
		DBObject result = collection.group(key, cond, intitial, "function(doc, out){ out.count++; }");


		//List<ParameterRanking> parameterRanking = mongoTemplate.queryForListGroupBy(groupQuery, ParameterRanking.class);

		return result;
	}

	private Date createDate(int day, int month, int year) {
		Calendar d = Calendar.getInstance();
		d.clear();
		d.set(Calendar.YEAR, year);
		d.set(Calendar.MONTH, month);
		d.set(Calendar.DATE, day);
		return d.getTime();
	}

}

