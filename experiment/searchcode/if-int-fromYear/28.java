package net.pdp7.f1.prediction.scraper;

import java.io.IOException;

import net.pdp7.commons.spring.context.annotation.AnnotationConfigApplicationContextUtils;
import net.pdp7.commons.util.MapUtils;
import net.pdp7.f1.prediction.model.SchemaService;
import net.pdp7.f1.prediction.spring.DataSourceConfig;
import net.pdp7.f1.prediction.spring.F1PredictionConfig;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

// FIXME: does not seem to handle correctly http://en.wikipedia.org/wiki/2011_Monaco_Grand_Prix , Maldonado's finish position of "18â "

public class WikipediaScraper {
	
	protected final SimpleJdbcTemplate jdbcTemplate;
	protected final CircuitNameNormalizer circuitNameNormalizer;

	public WikipediaScraper(SimpleJdbcTemplate jdbcTemplate, CircuitNameNormalizer circuitNameNormalizer) {
		this.jdbcTemplate = jdbcTemplate;
		this.circuitNameNormalizer = circuitNameNormalizer;
	}
	
	public void scrape(int season) throws IOException {
		jdbcTemplate.update("merge into seasons(season) values (?)", season);
		
		WebClient webClient = new WebClient();
		HtmlPage page = webClient.getPage("http://en.wikipedia.org/wiki/" + season + "_Formula_One_season");
		scrapeSeasonTeamDrivers(season, page);
		scrapeCalendar(season, page);
		scrapeStandings(season, page);
	}

	protected void scrapeCalendar(int season, HtmlPage page) {
		HtmlTable calendarTable = page.getFirstByXPath("//span[contains(@id,'race_schedule') or contains(@id,'Race_Calendar') or contains(@id,'calendar') or contains(@id,'Calendar')]/../following-sibling::table[1]");
		
		int start = season > 2006 ? 2 : 1; // from 2007 onwards, table header has two rows
		int extraneousRows = season == 2009 ? 1 : 0; // 2009 has an extra sources row at the bottom
		
		for(int i= start; i<calendarTable.getRowCount() - extraneousRows; i++) {
			String circuitName = circuitNameNormalizer.normalize(calendarTable.getRow(i).getCell(3).asText());
			jdbcTemplate.update("merge into circuits(circuit_name) values (?)", circuitName);
			jdbcTemplate.update("insert into calendar(season, round, grand_prix, circuit_name) values(?,?,?,?)", 
					season, 
					i - start + 1, 
					calendarTable.getRow(i).getCell(2).asText(),
					circuitName);
		}
	}

	protected void scrapeSeasonTeamDrivers(int season, HtmlPage page) {
		HtmlTable driversAndConstructorsTable = page.getFirstByXPath("//span[@id='Drivers_and_constructors' or @id='Teams_and_drivers']/../following-sibling::table[1]");
		for(int i=1; i<driversAndConstructorsTable.getRowCount(); i++) {
			String constructor = driversAndConstructorsTable.getCellAt(i, 1).asText();
			HtmlTableCell possibleDriverCell = driversAndConstructorsTable.getCellAt(i, 6);
			
			// rowspans (one row per driver, but constructor spans several rows) means sometimes we need to extract data in a different way...
			String driver = null;
			if(possibleDriverCell != null) {
				driver = possibleDriverCell.asText();
			}
			else {
				Object matching = driversAndConstructorsTable.getRow(i).getFirstByXPath("descendant::a[2]");
				
				// Sakon Yamamoto car change in 2010 means odd rowspanning!
				if(matching != null) {
					driver = ((HtmlAnchor) matching).asText();
				}
				else {
					continue;
				}
			}
			
			driver = driver.replaceAll("\\[.*\\]", "").trim();
			constructor = constructor.replaceAll("\\[.*\\]", "").trim();

			jdbcTemplate.update("merge into teams(team_name) values (?)", constructor);
			jdbcTemplate.update("merge into drivers(driver_name) values (?)", driver);
			
			jdbcTemplate.update("insert into season_team_drivers(season, team_name, driver_name) values (?,?,?)", season, constructor, driver);
		}
	}
	
	protected void scrapeStandings(int season, HtmlPage page) {
		
		String tableExtractXPath = season > 2010 
				? "//span[@id='Drivers_standings']/../following-sibling::table[2]//table"
				: "//span[@id='Drivers']/../following-sibling::table[1]//table";
		
		HtmlTable standingsTable = page.getFirstByXPath(tableExtractXPath);
		
		for(int i=1; i<standingsTable.getRowCount() - 1; i++) {
			String driver = standingsTable.getCellAt(i, 1).asText().trim();
			
			for(int j=2; j<standingsTable.getRow(i).getCells().size()-1; j++) {
				HtmlTableCell resultCell = standingsTable.getCellAt(i, j);
				
				boolean pole = resultCell.getByXPath(".//b").size() > 0;
				boolean fastestLap = resultCell.getByXPath(".//i").size() > 0;
				
				String text = resultCell.asText();
				
				if(text.trim().length() == 0) {
					continue;
				}
				
				Integer finishPosition = text.matches("^\\d+") ? Integer.parseInt(text.replaceAll("\\D", "")) : null;
				String otherResult = text.matches("^\\d+") ? null : text;
				
				jdbcTemplate.update("insert into grand_prix_driver_results(season, round, driver_name, pole, fastest_lap, finish_position, other_result) values (?,?,?,?,?,?,?)", 
						season,
						j-1,
						driver,
						pole,
						fastestLap,
						finishPosition,
						otherResult);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		String databaseFileName = args[0];
		int fromYear = Integer.parseInt(args[1]);
		int toYear = Integer.parseInt(args[2]);
		
		AnnotationConfigApplicationContext applicationContext = AnnotationConfigApplicationContextUtils.createConfiguredAnnotationConfigApplicationContext(
				MapUtils.createPropertiesFromMap(MapUtils.build("jdbc.url", "jdbc:h2:" + databaseFileName).map), 
				F1PredictionConfig.class, DataSourceConfig.JdbcUrlDataSourceConfig.class);
		
		applicationContext.getBean("schemaService", SchemaService.class).createSchema();
		
		WikipediaScraper wikipediaScraper = applicationContext.getBean("wikipediaScraper", WikipediaScraper.class);
		
		for(int i = fromYear; i<= toYear; i++) {
			wikipediaScraper.scrape(i);
		}
		
		System.out.println("Finished ok!");
	}
}

