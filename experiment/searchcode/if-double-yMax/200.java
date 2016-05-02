package au.org.ecoinformatics.s2s.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import au.org.ecoinformatics.s2s.json.StudyLocationPoint;
import au.org.ecoinformatics.s2s.util.CoordinateUtils;


/**
 * Retrieving from safari is a short term measure - hence the crude jdbc.
 * Safari schema is also prone to change.
 * 
 * Might need to create some indexes in the db too.
 *
 *
 create index site_location_point_point_ix on site_location_point ( point );
 create index site_location_name_ix on site_location(site_location_name );
 
 * 
 * 
 * I think I`ll follow the same process as the other service - 
 * read everything in at startup, then run queries off the maps
 * 
 * @author a1042238
 *
 */
@Service
public class S2SStudyLocationPointServiceSafariImpl implements
		S2SStudyLocationPointService {

	@Autowired
	@Qualifier(value="dataSource")
	private DataSource safariPostgresDataSource;
	
    private List<StudyLocationPoint> studyLocationPointList = new ArrayList<StudyLocationPoint>();
	
	private Map<String, StudyLocationPoint> siteNameTostudyLocationPointMap = new HashMap<String, StudyLocationPoint>();
	
	public List<StudyLocationPoint> getStudyLocationPoints(String bboxString) {
		if(bboxString == null){
			return getAllStudyLocationPointList();
		}
		return getPlotPointsFilteredByBBOX(bboxString);
	}
	
	private List<StudyLocationPoint> getAllStudyLocationPointList(){
		if(studyLocationPointList.size() == 0){
			studyLocationPointList = readStudyLocationPointsFromDb();
		}
		return studyLocationPointList;
	}
	
	public static final String studyLocationPointQuery = 
			"select slp.id," +
            "       sl.site_location_name,"+
            "       sl.established_date," +
            "       slp.point,"+
            "       slp.longitude,"+
            "       slp.latitude "+
            "from site_location_point slp "+
            "inner join site_location sl on slp.site_location_id = sl.id "+
            "where slp.point = 'C' or slp.point = 'PP1' "+
            "order by sl.site_location_name, slp.point ";
	
	//Nothing fancy - just fire up a connection
	public List<StudyLocationPoint> readStudyLocationPointsFromDb(){
		List<StudyLocationPoint> studyLocationPointList = new ArrayList<StudyLocationPoint>();
		Connection c = null;
		try {
			c = safariPostgresDataSource.getConnection();
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery( studyLocationPointQuery );
			String lastSiteName = "";
			
			while(rs.next()){
				String siteName = rs.getString(2);
				if(siteName == null || siteName.equals(lastSiteName)){
					continue;
				}
				
				lastSiteName = siteName;
				Timestamp timeStamp = rs.getTimestamp(3);
				String longitude = rs.getString(5);
				String latitude = rs.getString(6);
				studyLocationPointList.add(buildNewStudyLocationPoint(siteName, timeStamp, longitude, latitude ) );
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if( c != null){
				try {
					c.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		return studyLocationPointList;
	}
	
	private StudyLocationPoint buildNewStudyLocationPoint(String siteName, Timestamp date, String longitude, String latitude){
		String establishedDate = null;
		if(date != null){
			Date estDate = new Date();
			estDate.setTime( date.getTime() );
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			establishedDate = sdf.format(estDate);
		}
		Double doubLat = CoordinateUtils.convertLatLonMinuteDecimalSecondCoordToDecimal(latitude);
		Double doubLon = CoordinateUtils.convertLatLonMinuteDecimalSecondCoordToDecimal(longitude);
		String wkt = "POINT (" + doubLon.toString() + ", " + doubLat.toString() + ")";
		return new StudyLocationPoint(siteName, establishedDate, doubLon, doubLat, wkt);
	}
	
	private void initialiseSiteNameTostudyLocationPointMap(){
		siteNameTostudyLocationPointMap.clear();
		List<StudyLocationPoint> pointList = getAllStudyLocationPointList();
		for(StudyLocationPoint sp : pointList){
			siteNameTostudyLocationPointMap.put(sp.getSiteName(), sp);
		}
	}
	
	/**
	 * Copied from the Prototype!! as is most of this class.
	 * 
	 * All rather innefficient - but this is a prototype - 
	 * 
	 * @param bboxString
	 * @return
	 */
	public List<StudyLocationPoint> getPlotPointsFilteredByBBOX(String bboxString){
		List<StudyLocationPoint> filteredPoints = new ArrayList<StudyLocationPoint>();
		
		//split up the BBOX string 
		String [] bboxPieces = bboxString.split(",");
		if(bboxPieces.length != 4){
			return filteredPoints;
		}
		
		double xMin = 0.0;
		double yMin = 0.0;
		double xMax = 0.0;
		double yMax = 0.0;
		try{
			xMin = Double.parseDouble(bboxPieces[0]);
			yMin = Double.parseDouble(bboxPieces[1]);
			xMax = Double.parseDouble(bboxPieces[2]);
			yMax = Double.parseDouble(bboxPieces[3]);
		}catch(NumberFormatException e){
			e.printStackTrace();
			return filteredPoints;
		}
		
		for(StudyLocationPoint point : getAllStudyLocationPointList()){
			if( CoordinateUtils.isPointInBBOX(point, xMin, yMin, xMax, yMax) ){
				filteredPoints.add(point);
			}
		}
		return filteredPoints;
	}
	
	@Override
	public StudyLocationPoint getStudyLocationPoint(String siteName) {
		if(siteNameTostudyLocationPointMap.size() == 0){
			initialiseSiteNameTostudyLocationPointMap();
		}
		return siteNameTostudyLocationPointMap.get(siteName);
	}

	public DataSource getSafariPostgresDataSource() {
		return safariPostgresDataSource;
	}

	public void setSafariPostgresDataSource(DataSource safariPostgresDataSource) {
		this.safariPostgresDataSource = safariPostgresDataSource;
	}

}

