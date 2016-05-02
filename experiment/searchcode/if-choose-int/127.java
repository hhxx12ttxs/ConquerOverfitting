package data;


import domain.Book;
import domain.Organization;
import domain.Person;
import domain.util.URIGenerator;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import rdfmodel.RDFModel;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Stefan
 */
public class GoogleBooksWS extends WebService{

    private static final int RESULTS_PER_PAGE=40;
    private int trenutniBrojStrane=1;
    private int ukupanBrojStrana;
    private String baseUrl="https://www.googleapis.com/books/v1/volumes?q=+subject:computers&maxResults=40&key=%20AIzaSyCfhdVI8zPi7BAL_UdlAb406nYnN6-hSks&startIndex=";
    private boolean prvi=true;
    private boolean kraj=false;
    
    @Override
    public void parse(String tekst) throws Exception {
        
                //List<Book> lista=new ArrayList<>();
                
                JSONParser parser = new JSONParser();
		Object obj = parser.parse(tekst);
		JSONObject jsonObject = (JSONObject) obj;
                if(prvi){
                    int brojRezultata=Integer.parseInt(jsonObject.get("totalItems")+""); 
                    ukupanBrojStrana=brojRezultata/RESULTS_PER_PAGE+1;
                    System.out.println("Broj strana" +ukupanBrojStrana);
                    prvi=false;
                }
                
                System.out.println("Trenutni broj strane "+trenutniBrojStrane);
                JSONArray niz=(JSONArray) jsonObject.get("items");
                if(niz==null){
                    kraj=true;
                }else{
                    Iterator iterator = niz.iterator();
                    while (iterator.hasNext()) {
                        Book b=new Book();
                        b.setUri(URIGenerator.generateUri(b));
                        JSONObject book=(JSONObject) iterator.next();
                        b.setTitle((String) ((JSONObject)book.get("volumeInfo")).get("title"));
                        JSONArray autori=(JSONArray) ((JSONObject)book.get("volumeInfo")).get("authors");
                        if(autori!=null){
                            Iterator it=autori.iterator();
                            while(it.hasNext()){
                                Person p=new Person();
                                p.setUri(URIGenerator.generateUri(p));
                                p.setName((String) it.next());
                                b.getAuthors().add(p);
                            }
                        }else{
                            System.out.println("Autori su null");
                        }
                        Organization o=new Organization();
                        o.setUri(URIGenerator.generateUri(o));
                        o.setName((String) ((JSONObject)book.get("volumeInfo")).get("publisher"));
                        b.setPublisher(o);
                        String date=(String) ((JSONObject)book.get("volumeInfo")).get("publishedDate");
                        System.out.println(date);
                        b.setDatePublished(date);

                        String description=(String)((JSONObject)book.get("volumeInfo")).get("description");
                        b.setDescription(description);

                        Object brStr=((JSONObject)book.get("volumeInfo")).get("pageCount");
                        if(brStr!=null){
                            long broj=Long.parseLong(brStr.toString());
                            b.setNumberOfPages((int)broj);
                        }

                        JSONArray identifiers=(JSONArray)((JSONObject)book.get("volumeInfo")).get("industryIdentifiers");
                        if(identifiers!=null){
                            for(int i=0;i<identifiers.size();i++){
                                JSONObject identifier=(JSONObject) identifiers.get(i);
                                if("ISBN_13".equals((String)identifier.get("type"))){
                                    b.setIsbn((String)identifier.get("identifier"));
                                }
                            }
                        }
                        //b.setIsbn((String) ((JSONObject) ((JSONArray)((JSONObject)book.get("volumeInfo")).get("industryIdentifiers")).get(1)).get("identifier"));

                        lista.add(b);

                        }

                        if(trenutniBrojStrane<ukupanBrojStrana){
                            trenutniBrojStrane++;
                        }else{
                            kraj=true;
                        }
                }
    }

    @Override
    public String getUrl() {
        return baseUrl+izracunajStartIndex();
        //return "https://www.googleapis.com/books/v1/volumes?q=+subject:computers&maxResults=40&key=%20AIzaSyCfhdVI8zPi7BAL_UdlAb406nYnN6-hSks&startIndex=41";
        //return "https://www.googleapis.com/books/v1/volumes?q=+subject:computers&key=%20AIzaSyCfhdVI8zPi7BAL_UdlAb406nYnN6-hSks";
    }
    
    public int izracunajStartIndex(){
        return (trenutniBrojStrane-1)*40+1;
    }

    @Override
    public void setRequestType(HttpURLConnection conn) throws Exception{
        try {
            conn.setRequestMethod("GET");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void setDataType(HttpURLConnection conn) {
        conn.setRequestProperty("Accept", "application/json");
    }

    @Override
    public boolean daLiJeKraj() {
        return kraj;
    }
    
}

