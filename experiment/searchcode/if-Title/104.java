package edu.iit.swyne.server;

import edu.iit.swyne.ee.*;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.text.*;
import org.glowacki.*;

import org.apache.commons.digester.Digester;
import org.dom4j.*;
import org.dom4j.io.XMLWriter;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.VCARD;

public class DatabaseFunctionality
{
	private static final String createDocLocTable = "CREATE TABLE IF NOT EXISTS doc_loc(" +
	"doc_id INT UNSIGNED NOT NULL, latitude DOUBLE, longitude DOUBLE);";
	private static final String createDocDateTable = "CREATE TABLE IF NOT EXISTS doc_date(" +
	"doc_id INT UNSIGNED NOT NULL, date DATE);";
	private static final String createDocumentsTable = "CREATE TABLE IF NOT EXISTS documents(doc_id INT UNSIGNED"
		+ " NOT NULL AUTO_INCREMENT PRIMARY KEY, collection_id INT UNSIGNED NOT NULL, date_added TIMESTAMP, source TEXT, title TEXT, "
		+ "txt LONGTEXT);"; 
   private static final String createCollectionsTable = "CREATE TABLE IF NOT EXISTS collections(collection INT UNSIGNED"
   + " NOT NULL AUTO_INCREMENT PRIMARY KEY, collection_name VARCHAR(64));";

   private static final String fetchCollectionsTable = "SELECT * FROM collections;";

	private static final String insertDate = "INSERT INTO doc_date VALUES(?,?);";
	private static final String insertLoc  = "INSERT INTO doc_loc VALUES(?,?,?);";
	private static final String insertDoc  = "INSERT INTO documents (date_added, collection_id, source, title, txt) VALUES(?,?, ?,?,?);";
   private static final String insertCollection = "INSERT INTO collections (collection_name) VALUES(?);";

	private static Connection conn;
	private static EntityExtractor ee;
   private static HashMap collections;
   private static String sesame_server;
   private static String sesame_repo;

	static
	{
		try{
			Properties p = SwyneServer.props;
			Class.forName(p.getProperty("swyne.server.driver", "com.mysql.jdbc.Driver")).newInstance();
			String jdbcUrl =  p.getProperty("swyne.server.jdbcurl");
         String lexer_path = p.getProperty("swyne.ee.lexer_path");
         sesame_server = p.getProperty("swyne.server.sesame");
         sesame_repo = p.getProperty("swyne.server.repo");
         //System.out.println(jdbcUrl);
			conn = DriverManager.getConnection(jdbcUrl);
			initTables();
         initCollections();
         ee = new EntityExtractor(lexer_path);
		}catch(java.sql.SQLException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void initTables() throws Exception
	{
		Statement s = conn.createStatement();
		s.executeUpdate(createDocumentsTable);
		s.executeUpdate(createDocDateTable);
		s.executeUpdate(createDocLocTable);
      s.executeUpdate(createCollectionsTable);
		s.close();
	}
   private static void initCollections() throws Exception
   {
      Statement s;
      ResultSet rs;

      collections = new HashMap();
      s = conn.createStatement();
      rs = s.executeQuery(fetchCollectionsTable);
      while(rs.next())
      {
         String collectionName;
         int collectionID;
         collectionID = rs.getInt(1);
         collectionName = rs.getString(2);
         collections.put(collectionName, new Integer(collectionID));
      }
      rs.close();
      s.close();
      
   }
	public static String getDocument(long id) throws Exception
	{
		String fetchDocument = "SELECT txt FROM documents WHERE doc_id=" + id + ";";
		String ret;
		ResultSet rs;
		Statement s = conn.createStatement();
		rs = s.executeQuery(fetchDocument);
		if(rs.next())
		{
			String txt;
			txt = rs.getString("txt");
			return txt;
		}else
			throw new Exception("Document not found");
	}


	public static boolean addDocument(HashMap vals) throws Exception
	{
		String city;
		String state;
		String title;
		String published;
		String source;
		String article;
		String location;
      String collection;
      int collection_id;

		OutputStream rdfformat = null;
		InputStream is = null;

		Document xml;
		Iterator i;
		int doc_id = 0;
     
      
		city = (String)vals.get("city");
		state = (String)vals.get("state");
		title = (String)vals.get("title");
		published = (String)vals.get("published");
		source = (String)vals.get("source");
		article = (String)vals.get("article");
      collection = (String)vals.get("collection");

      if(city != null)
         city =city.trim();
      if(state != null)
         state = state.trim();
      if(published != null)
         published = published.trim();
      if(source != null)
         source = source.trim();
      else
         throw new Exception("Error: source not specified");
      if(article != null)
         article = article.trim();
      else
         throw new Exception("Error: article not specified");
      if(title != null)
         title = title.trim();
      else
         throw new Exception("Error: title not specified");
      if(collection == null || collection.trim().equals(""))
         collection = "Default";
		
      location = "";
		if(city != null)
			location = city + ",";
		if(state != null)
			location += state;
		xml = ee.buildEntities(article, source, title, location);
		
      collection_id = getCollectionId(collection, true);
      PreparedStatement ndoc = conn.prepareStatement(insertDoc, Statement.RETURN_GENERATED_KEYS);
		ndoc.setDate(1, new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
		ndoc.setInt(2, collection_id);
      ndoc.setString(3, source);
		ndoc.setString(4, title);
		ndoc.setString(5, article);
		ndoc.executeUpdate();
		ResultSet rs = ndoc.getGeneratedKeys();
		if(rs.next())
			doc_id = rs.getInt(1);
		else
			throw new Exception("Document not inserted");

		rs.close();
		ndoc.close();
      xml.getRootElement().addElement("Date").addText(published);
		i = xml.getRootElement().elementIterator("Date");
		PreparedStatement idate = conn.prepareStatement(insertDate);
		while(i.hasNext())
		{
			String txt = ((Element)i.next()).getText();
         //System.out.println("Date: " + txt);
			//Jaeyeon, convert txt(a date), into the format yyyy-mm-dd. Store in converted
         //System.out.println("TEXT: " + txt);
			//Date rawdate = new Date(txt);
			SimpleDateFormat fdm = new SimpleDateFormat("yyyy-mm-dd");
		//FROM ***************************************************** 
			//String formattedDate = fdm.parse().toString();
			//String[] parsedDate = formattedDate.split("-");
			//java.sql.Date date = new java.sql.Date(Integer.valueOf(parsedDate[0]), Integer.valueOf(parsedDate[1]), Integer.valueOf(parsedDate[2]));
			//java.sql.Date date = new java.sql.Date(fdm.parse(txt).getTime());
         
         java.sql.Date date = new java.sql.Date(CalendarParser.parse(txt).getTime().getTime());
		
		// TO *****************************************************
			idate.setInt(1, doc_id);
			idate.setDate(2, date);
			idate.executeUpdate();
		}
		idate.close(); 
		i = xml.getRootElement().elementIterator("Geocoded");
		PreparedStatement iloc = conn.prepareStatement(insertLoc);
		while(i.hasNext())
		{
			Element el = (Element)i.next();
			double latitude = Double.parseDouble(el.element("Latitude").getText());
			double longitude = Double.parseDouble(el.element("Longitude").getText());
         //System.out.println("Latitude: " + latitude + " Longitude " + longitude);
			iloc.setInt(1, doc_id);
			iloc.setDouble(2, latitude);
			iloc.setDouble(3, longitude);
         iloc.executeUpdate();
		}
		//Jaeyeon add your RDF code here...xml is the xml data of the entities
		// convert XML to RDF
		//System.out.println("*******************************");
		StringWriter sw = new StringWriter();
		new XMLWriter(sw).write(xml);
		//System.out.println(sw.toString());
		XMLtoRDF(sw.toString(), doc_id);
		
		//byte[] cbyte = null;
		//rdfformat.write(cbyte);
		//is.read(cbyte);
		
		// load RDF to Sesame
		RDFtoSesame();

		return true;
	}
	public static void RDFtoSesame(){

		//set server name and repository ID
		String sesameServer = sesame_server; 
		String repositoryID = sesame_repo;

		//initialize repository
		Repository myRepository = new HTTPRepository(sesameServer, repositoryID);

		File rdffile = new File("./xml.tmp");
		try {
			myRepository.initialize();
			RepositoryConnection con = myRepository.getConnection();

			if( rdffile.toString().length() != 0)
				con.add(rdffile, null, RDFFormat.RDFXML);
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void XMLtoRDF(String xml, int doc_id){
		//OutputStream os = null;
		//FileOuputStream fos = new FileOutputStream("asdf.temp");
		try {
			//System.out.println(xml.getBytes());
			//System.out.println("******XMLtoRDF*********\n"+xml);
			
			StringReader rdr = new StringReader(xml);
			//System.out.println(rdr.toString());
			//rdr.read(xml.toCharArray());
			//InputStream rdr = null;
			//rdr.read(xml.getBytes());
			

			/** xml parsig rules **/
			Digester digester = new Digester();
			digester.setValidating( false );
			digester.setUseContextClassLoader(true);

			digester.addObjectCreate( "Entities", Article.class );
			digester.addSetProperties( "Entities" );
			digester.addBeanPropertySetter( "Entities/PERSON", "person" );
			digester.addBeanPropertySetter( "Entities/LOCATION", "location" );
			digester.addBeanPropertySetter( "Entities/ORGANIZATION", "org" );
			digester.addBeanPropertySetter( "Entities/Date", "date" );
			Article article = (Article) digester.parse(rdr);

			FileOutputStream tempfile = new FileOutputStream("./xml.tmp");

			// create an empty model
			Model model = ModelFactory.createDefaultModel();

			
			Iterator it;
			// create the resource and add the properties cascading style
			Resource RDFTest = model.createResource();
			String source = article.getUrl();
			RDFTest.addProperty(VCARD.SOURCE, source);	//uri
			RDFTest.addProperty(VCARD.KEY, String.valueOf(doc_id));		//doc_id
			String title = article.getTitle();
			RDFTest.addProperty(VCARD.TITLE, title);
			it = article.getPersonList();
			while(it.hasNext())
				RDFTest.addProperty(VCARD.NAME, it.next().toString());	//person
			it = article.getLocationList();
			while(it.hasNext())
				RDFTest.addProperty(VCARD.Locality, it.next().toString());	//location
			it = article.getOrgList();
			while(it.hasNext())
				RDFTest.addProperty(VCARD.ORG, it.next().toString());	//organization
		
			model.write(tempfile);
			

		} catch( Exception exc ) {
			exc.printStackTrace();
		}
	}


   private static int getCollectionId(String collectionName, boolean addIfNotExist) throws Exception
   {
      String collection;
      Integer i;
      
      if(collectionName == null || (collection = collectionName.trim()).equals(""))
      {
         collection = "Default";
      }
      
      i = (Integer)collections.get(collection);
      if(i == null)
      {
         if(addIfNotExist)
         {
            i = new Integer(addCollection(collectionName));
         }else
            return -1;
      }
      return i.intValue();
   }

   private static int addCollection(String collectionName) throws Exception
   {
      Statement s;
      ResultSet rs;
      PreparedStatement ncol;
      int collectionNum;
		
      ncol = conn.prepareStatement(insertCollection, Statement.RETURN_GENERATED_KEYS);
      ncol.setString(1, collectionName.trim());
      ncol.executeUpdate();
      rs = ncol.getGeneratedKeys();
      rs.next();
      collectionNum =  rs.getInt(1);
      collections.put(collectionName.trim(), collectionNum);
      return collectionNum;
   }
}

