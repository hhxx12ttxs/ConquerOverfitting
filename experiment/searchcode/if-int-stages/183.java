/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package triploa;

import javax.microedition.io.*;
import java.io.*;
import org.kxml2.io.*;
import org.kxml2.kdom.*;
import org.xmlpull.v1.*;
import java.util.Vector;
import javax.microedition.lcdui.*;
import de.enough.polish.util.Locale;
import de.enough.polish.util.TextUtil;

/**
 *
 * @author User
 */
public class WebMethods implements Runnable {

    HttpConnection con = null;
    int userID = 0;
    String url = null;
    String base = "http://triploa.cli.di.unipi.it/service/rest/";
    Reader in = null;
    MainMIDlet midlet = null;
    protected final int LOGIN = 0;
    protected final int SEARCH = 1;
    protected final int MY_PROFILE = 2;
    protected final int GET_PROFILE = 3;
    protected final int INSERT = 4;
    protected final int BOOK = 5;
    protected final int BOOKED = 6;
    protected final int OFFERED = 7;
    protected final int DETAILED_INFO = 8;
    protected final int SEARCH_MAP_POINT = 10;
    protected final int SEARCH_FROM_POINT = 11;
    protected final int SEARCH_TO_POINT = 12;
    protected final int REVERSE_GEOCODING = 13;
    protected final int UNREAD_MSG = 14;
    protected final int ALL_MSG = 15;
    protected final int READED_MSG = 16;
    protected final int SEND_MSG = 17;
    protected final int SENT_MSG = 18;
    protected final int DELETE_MSG = 19;
    protected final int VEHICLE_LIST = 20;
    static int vecchio = -1;
    int searchtype;
    static String from,  to,  data,  seats,  info,  key;
    static int start = 0,  end = 0;
    protected final int ADD_FAVORITE = 23;
    protected final int CANCEL = 9;
    protected final int REMOVE_FAVORITE = 24;
    protected final int FAVORITE = 22;
    protected final int DELETE = 21;
    protected final int NOTIFICATION_LIST = 25;
    protected final int BANNED_LIST = 26;
    protected final int BUDDY_LIST = 27;
    protected final int ADD_FRIEND = 28;
    protected final int REMOVE_FRIEND = 29;
    protected final int BAN_FRIEND = 30;
    protected final int UNBAN_FRIEND = 31;
    protected final int USER_REQUEST_LIST=32;
    protected final int FRIENDSHIP_NOTIFICATION=33;
    protected final int DENY_FRIENDSHIP=34;
    protected final int ACCEPT_FRIENDSHIP=35;
    private String deviation;
    private int id;
    private int vehicleID;
    private String vehicleClass;
    private boolean idFreq;
    private boolean draft,license;
    private String estimatedTime,estimatedBudget,expiration;
    private boolean smokers;
    private String tripId;
    
    

    public WebMethods(MainMIDlet m) {
        midlet = m;
    }
    int method = -1;

    /**
     * call when a result call of WS has passed the error checking
     * @param p Parser
     */
    protected void oKcases(KXmlParser p) {
        switch (method) {

            case LOGIN:
                doLogin(p);
                break;
            case MY_PROFILE:
                doMyProfile(p);
                break;
            case GET_PROFILE:
                break;
            case SEARCH:
                doSearch(p);
                break;
            case BOOKED:
                doBooked(p);
                break;
            case BOOK:
                doBook(p);
                break;
            case OFFERED:
                doOffered(p);
                break;
            case DETAILED_INFO:
                doDetailedInfo(p);
                break;
            case INSERT:
                doInsert(p);
                break;
            case SEARCH_MAP_POINT:
                doMapPoint(p);
                break;
            case REVERSE_GEOCODING:
                doReverseGeocoding(p);
                break;
            case UNREAD_MSG:
                doUnreadMessage(p);
                break;
            case DELETE_MSG:
                doDeleteMessage(p);
                break;
            case SEND_MSG:
                doSendMessage(p);
                break;
            case SENT_MSG:
                doSentMessage(p);
                break;
            case ALL_MSG:
                doAllMessage(p);
                break;
            case VEHICLE_LIST:
                doVehicleList(p);
                break;
            case CANCEL:
                doCancelBook(p);
                break;
            case DELETE:
                doFavorite(p);
                break;
            case FAVORITE:
                doFavorite(p);
                break;
            case ADD_FAVORITE:
                doAddFavorite(p);
                break;
            case REMOVE_FAVORITE:
                doRemoveFavorite(p);
                break;
            case NOTIFICATION_LIST:
                doNotification(p);
                break;
            case BUDDY_LIST:
                doBuddy(p);
                break;
            case BANNED_LIST:
                doBanned(p);
                break;
            case ADD_FRIEND:
                doAddFriend(p);
                break;
            case REMOVE_FRIEND:
                doRemoveFriend(p);
                break;
            case BAN_FRIEND:
                doBan(p);
                break;
            case UNBAN_FRIEND:
                doUnBan(p);
                break;
                case USER_REQUEST_LIST:
                doUserRequestList(p);
                break;
                case FRIENDSHIP_NOTIFICATION:
                doFriendShipNotification(p);
                break;
                case DENY_FRIENDSHIP:
                doDenyFriendShip(p);
                break;
                case ACCEPT_FRIENDSHIP:
                doAcceptFriendShip(p);
                break;
        }
    }

    private void doAddFavorite(KXmlParser p) {
        try {
            midlet.ShowMessage(Locale.get("Added"));

        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }
    private void doUserRequestList(KXmlParser p) {
        try {
            midlet.userRequestList(UserList.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }
    private void doFriendShipNotification(KXmlParser p) {
        try {
            midlet.friendShipNotification(UserList.create(p));

        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }
    private void doDenyFriendShip(KXmlParser p) {
        try {
            midlet.ShowMessage(Locale.get("FriendDenied"));

        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }
    private void doAcceptFriendShip(KXmlParser p) {
        try {
            midlet.ShowMessage(Locale.get("FriendAccepted"));

        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doAllMessage(KXmlParser p) {
        try {
            midlet.allMsg(MessageList.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doCancelBook(KXmlParser p) {
        try {
            midlet.cancelledBook();
            midlet.ShowMessage(Locale.get("Deleted"));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doDeleteMessage(KXmlParser p) {
        try {
            midlet.deletedMsg();
            midlet.ShowMessage(Locale.get("Deleted"));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }


    private void doFavorite(KXmlParser p) {
        try {
            midlet.insertFavoriteResult(SearchResult.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }
GeoCoding g=null;
    private void doMapPoint(KXmlParser p) {
        try {
            
            g = GeoCoding.create(p);
            if (g.getResults().size() == 1) {
                setLocazione(0);
            } else if(g.getResults().size() > 1)
            {
                midlet.getGeoList().deleteAll();
                midlet.getGeoList().append(Locale.get("Unknown"), null);
                for(int i=0;i<g.getResults().size();i++)
                {
                     Locazione l = (Locazione) g.getResults().elementAt(i);
                     midlet.getGeoList().append(l.getAddress(), null);
                }
                midlet.switchDisplayable(null, midlet.getGeoList());
            }
            else{
                midlet.ShowError("Error getting coordinates");
            } //errr
        } catch (Exception e) {
            System.out.println("Errore " + e.getMessage());
            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }
    /**
     * Call when user choise the alternative Location of the searched Address
     * @param index
     * @throws triploa.LOAException
     */
    public void setLocazione(int index) throws LOAException
    {
        
        Locazione l = (Locazione) g.getResults().elementAt(index);
        if (vecchio > 0) {
                    System.out.println("vecchio>-1");
                    if (this.searchtype == this.SEARCH_FROM_POINT) {
                        System.out.println("scrittura coordinate from");
                        from = l.getLat() + "," + l.getLng();
                    } else {
                        System.out.println("scrittura coordinate to");
                        to = l.getLat() + "," + l.getLng();
                        
                    }
                    if (vecchio == SEARCH) {
                        vecchio = -1;
                        System.out.println("Si richiama search");
                        midlet.getFromTextField().setString(from);
                        midlet.getToTextField().setString(to);
                        search(key, from, to, this.data, this.start, end, this.seats);
                        return;
                    }
                    if (vecchio == INSERT) {
                        vecchio = -1;
                        System.out.println("Si richiama insert");
                        midlet.getInsertFromTextField().setString(from);
                        midlet.getInsertToTextField().setString(to);
                        insert(key, from, to, this.data, seats, info,deviation,id,vehicleID,vehicleClass,idFreq,estimatedTime,estimatedBudget,draft,expiration,license,smokers);
                        return;
                    }
                    if (vecchio == BOOK) {
                        vecchio = -1;
                        midlet.coord1=from;
                        midlet.coord2=to;
                        book(key,tripId, from, to, seats,deviation, info);
                        return;
                    }
                }
                midlet.showMap(l.getLat(), l.getLng());
    }
    private void doLogin(KXmlParser p) {
        String api_key = null;
        int token = -1;
        boolean exit = false, apikey = false;
        UserProfile up = null;
        try {
            while (!exit) {
                token = p.next();
                switch (token) {
                    case KXmlParser.END_DOCUMENT:
                        exit = true;  //exit form while
                        break;
                    case KXmlParser.START_TAG:

                        System.out.println(p.getName());
                        if (p.getName().equals("AuthToken")) {
                            apikey = true;
                        } else if (p.getName().equals("Payload")) {
                            up = UserProfile.create(p);
                            apikey = false;
                        } else {
                            apikey = false;
                        }
                        break;
                    case KXmlParser.END_TAG:
                        apikey = false;
                        break;
                    case KXmlParser.TEXT:
                        if (apikey) {
                            api_key = p.getText();
                        }
                        break;
                }
            }
        } catch (Exception e) {
            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
        try {
        midlet.finalizeLogin(api_key, up);
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doNotification(KXmlParser p) {
        try {
        midlet.showNotification(NotificationList.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doBuddy(KXmlParser p) {
        try {
        //midlet.insertBookedResult(SearchResult.create(p));
            midlet.buddyList(UserList.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doRemoveFavorite(KXmlParser p) {
        try {
        //midlet.insertBookedResult(SearchResult.create(p));
            midlet.travelRemoved();
            midlet.ShowMessage(Locale.get("TravelRemoved"));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doBanned(KXmlParser p) {
        try {
            midlet.bannedList(UserList.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doAddFriend(KXmlParser p) {
        try {
        midlet.ShowMessage(Locale.get("User")+" "+Locale.get("Added"));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doRemoveFriend(KXmlParser p) {
        try {
        midlet.ShowMessage(Locale.get("User")+" "+Locale.get("Removed"));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doBan(KXmlParser p) {
        try {
        midlet.ShowMessage(Locale.get("User")+" "+Locale.get("Banned"));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doUnBan(KXmlParser p) {
        try {
        midlet.ShowMessage(Locale.get("User")+" "+Locale.get("UnBanned"));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doSearch(KXmlParser p) {
        try {
            midlet.insertSearchResult(SearchResult.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doBooked(KXmlParser p) {
        try {
            midlet.insertBookedResult(SearchResult.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doDetailedInfo(KXmlParser p) {
        try {
            midlet.updateTravel(p);

        } catch (Exception e) {
        }
    }

    private void doInsert(KXmlParser p) {
        try {
            midlet.tripInserted();

        } catch (Exception e) {
        }
    }

    private void doBook(KXmlParser p) {
        try {
            midlet.ShowMessage(Locale.get("Booked"));
            
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doOffered(KXmlParser p) {
        try {
            midlet.insertOfferedResult(SearchResult.create(p));
        } catch (Exception e) {
            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doReverseGeocoding(KXmlParser p) {
        try {

        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    /**
     * Call for WS request
     */
    public void run() {
        try {
            //connection to web services
            System.out.println("Connessione a " + url);
            con = (HttpConnection) Connector.open(url, Connector.READ);
            InputStream is = con.openInputStream();
            //String xml="<?xml version=\"1.0\" encoding=\"utf-8\" ?><rsp stat=\"ok\"><result searchId=\"123\"><travel><id>1024</id><from>milano</from><to>roma</to><date>12/04/2008</date></travel><travel><id>1025</id><from>pisa</from><to>firenze</to><date>13/04/2008</date></travel></result></rsp>";

            //in = new InputStreamReader(new ByteArrayInputStream(xml.getBytes()));
            in = new InputStreamReader(is);
            System.out.println("Dati ricevuti");
        } catch (IOException e) {
            System.out.println("Errore 1 " + e.getMessage());

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
        try {
            KXmlParser p = new KXmlParser();
            p.setInput(in);
            if (errorChecking(p)) {
                oKcases(p);
            } else {
                midlet.ShowError("Error Code :" + code + " Message: " + msg);
            }
        } catch (Exception e) {
            System.out.println("Errore 2 " + e.getMessage());

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        //throw new LOAException("400",e.getMessage());
        }
    }
    LOAException exce = null;
    String msg = null, code = null;

    private void doSendMessage(KXmlParser p) {
        try {
            //midlet.insertBookedResult(SearchResult.create(p));
            midlet.ShowMessage(Locale.get("Sent"));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }

    }

    private void doSentMessage(KXmlParser p) {
        try {
            //midlet.insertBookedResult(SearchResult.create(p));
            midlet.sentMsg(MessageList.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doUnreadMessage(KXmlParser p) {
        try {
            //midlet.insertBookedResult(SearchResult.create(p));
            midlet.unreadMsg(MessageList.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }

    private void doVehicleList(KXmlParser p) {
        try {
        midlet.setVehicleList(VehicleList.create(p));
        } catch (Exception e) {

            midlet.ShowError(Locale.get("Error") + " " + e.getMessage());
        }
    }
    /**
     * Do error checking fort the result of WS call
     * */
    private boolean errorChecking(KXmlParser p) throws LOAException { //da cambiare
        int token = -1;
        boolean exit = false, fail = false, m = false, c = false;
        String lasttag = "";
        try {
            while (!exit) {
                token = p.next();
                switch (token) {
                    case KXmlParser.END_DOCUMENT:
                        exit = true;  //exit form while
                        break;
                    case KXmlParser.START_TAG:
                        System.out.println(p.getName());
                        lasttag = p.getName();
                        break;
                    case KXmlParser.END_TAG:
                        if (p.getName().equals("Status")) {
                            exit = true;
                        }
                        lasttag = "";
                        break;
                    case KXmlParser.TEXT:
                        if (lasttag.equals("ErrorCode")) {
                            c = true;
                            code = p.getText();
                        }
                        if (lasttag.equals("Message")) {
                            m = true;
                            msg = p.getText();
                        }
                        if (lasttag.equals("Status")) {
                            if (p.getText().equals("fail")) {
                                fail = true;
                            }
                            exit = true;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new LOAException("400", e.getMessage());
        }
        if (fail) {
            exce = new LOAException(code, msg);
        }
        return !fail;
    }

    /**
     * Call WS for do Login
     * @param user
     * @param password
     * @throws triploa.LOAException
     */
    public void login(String user, String password) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=login&username=");
        sb.append(TextUtil.replace(user, " ", "%20"));
        sb.append("&password=");
        sb.append(TextUtil.replace(password, " ", "%20"));
        url = sb.toString();
        method = LOGIN;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * chack if the string is null or empty
     * @param s String
     * @return true if not null and not empty,false otherwise
     */
    public boolean sNN(String s) //string not null
    {

        return s != null && !s.equals("");
    }
    StringBuffer app;

    /**
     * Call WS for getting address coordinate
     * @param api_key
     * @param state
     * @param city
     * @param address
     * @throws triploa.LOAException
     */
    public void getMapPoint(String api_key, String state, String city, String address) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestGeoRef.ashx?method=geoCoding&api_key=");
        sb.append(api_key);
        if (sNN(state)) {
            sb.append("&state=");
            sb.append(state);
        }
        if (sNN(city)) {
            sb.append("&city=");
            sb.append(city);
        }
        if (sNN(address)) {
            sb.append("&road=");
            sb.append(address);
        }
        url = sb.toString();
        method = SEARCH_MAP_POINT;
        Thread t = new Thread(this);
        t.start();

    }

    /**
     * Call WS for getting address coordinate
     * @param api_key
     * @param location
     * @throws triploa.LOAException
     */
    public void getMapPoint(String api_key, String location) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestGeoRef.ashx?method=geoCoding&api_key=");
        sb.append(api_key);
        if (sNN(location)) {
            sb.append("&location=");
            System.out.println("Ricerca coordinate " + location);
            sb.append(TextUtil.replace(location, " ", "%20"));
        }
        url = sb.toString();
        method = SEARCH_MAP_POINT;
        Thread t = new Thread(this);
        t.start();

    }

    /**
     * Call WS for searching Travels
     * @param api_key
     * @param from
     * @param to
     * @param fromDate
     * @param start
     * @param max
     * @param toDate
     * @throws triploa.LOAException
     */
    public void search(String api_key, String from, String to, String fromDate, int start, int max, String toDate) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=TripSearch&auth_token=");
        sb.append(api_key);
        if (sNN(from)) {
            sb.append("&from=");
            if (from.indexOf(",") > 0) {
                sb.append(from);
            } else {
                System.out.println("Ricerca coordinate from");
                searchtype = this.SEARCH_FROM_POINT;
                vecchio = SEARCH;
                this.key = api_key;
                this.from = TextUtil.replace(from, " ", "%20");
                this.to = TextUtil.replace(to, " ", "%20");
                this.data = fromDate;
                this.seats = toDate;
                this.start = start;
                this.end = max;
                this.getMapPoint(api_key, from);
                return;
            }
        }
        if (this.sNN(to)) {
            sb.append("&to=");
            if (to.indexOf(",") > 0) {
                sb.append(to);
            } else {
                System.out.println("Ricerca coordinate to");
                searchtype = this.SEARCH_TO_POINT;
                vecchio = SEARCH;
                this.key = api_key;
                this.from = TextUtil.replace(from, " ", "%20");
                this.to = TextUtil.replace(to, " ", "%20");
                this.data = fromDate;
                this.seats = toDate;
                this.start = start;
                this.end = max;
                this.getMapPoint(api_key, to);
                return;
            }
        }
        if (this.sNN(fromDate)) {
            sb.append("&from_date=");
            sb.append(fromDate);
        }
        if (this.sNN(toDate)) {
            sb.append("&to_date=");
            sb.append(toDate);
        }
        sb.append("&start=");
        sb.append(start);
        sb.append("&max=");
        sb.append(max);
        url = sb.toString();
        vecchio = -1;
        System.out.println("Ricerca viaggi");
        method = SEARCH;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * @deprecated
     * Call WS for inser Travel
     * @param api_key
     * @param departure
     * @param arrival
     * @param departureDate
     * @param freeSeats
     * @param generalNotes
     * @throws triploa.LOAException
     */
    public void insert(String api_key, String departure, String arrival, String departureDate, String freeSeats, String generalNotes) throws LOAException {

        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=CreateTrip&auth_token=");
        sb.append(api_key);
        if (sNN(departure)) {
            sb.append("&departure_coords=");
            if (departure.indexOf(",") > 0) {
                sb.append(departure);
            } else {
                searchtype = this.SEARCH_FROM_POINT;
                vecchio = INSERT;
                this.key = api_key;
                from = TextUtil.replace(departure, " ", "%20");
                to = TextUtil.replace(arrival, " ", "%20");
                this.data = departureDate;
                this.seats = freeSeats;
                this.info = TextUtil.replace(generalNotes, " ", "%20");
                this.getMapPoint(api_key, departure);
                return;
            }

        }
        if (this.sNN(arrival)) {
            sb.append("&arrival_coords=");
            if (arrival.indexOf(",") > 0) {
                sb.append(arrival);
            } else {
                searchtype = this.SEARCH_TO_POINT;
                vecchio = INSERT;
                this.key = api_key;
                from = TextUtil.replace(departure, " ", "%20");
                to = TextUtil.replace(arrival, " ", "%20");
                this.data = departureDate;
                this.seats = freeSeats;
                this.info = TextUtil.replace(generalNotes, " ", "%20");
                this.getMapPoint(api_key, arrival);
                return;
            }
        }
        if (this.sNN(departureDate)) {
            sb.append("&departure_date=");
            sb.append(departureDate);
        }
        if (this.sNN(generalNotes)) {
            sb.append("&general_notes=");
            sb.append(TextUtil.replace(generalNotes, " ", "%20"));
        }
        sb.append("&free_seats=");
        sb.append(freeSeats);
        url = sb.toString();
        vecchio = -1;
        method = INSERT;
        Thread t = new Thread(this);
        t.start();
    }
    /**
     * Call WS for insert new Travel
     * @param api_key
     * @param departure
     * @param arrival
     * @param departureDate
     * @param freeSeats
     * @param generalNotes
     * @param deviation
     * @param id
     * @param vehicleID
     * @param vehicleClass
     * @param isFrequently
     * @param estimatedTime
     * @param estimatedBudget
     * @param draft
     * @param expirationDate
     * @param license
     * @param smokers
     * @throws triploa.LOAException
     */
    public void insert(String api_key, String departure, String arrival, String departureDate, String freeSeats, String generalNotes,String deviation,int id,int vehicleID,String vehicleClass,boolean isFrequently,String estimatedTime,String estimatedBudget,boolean draft,String expirationDate,boolean license,boolean smokers) throws LOAException {

        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=SaveTrip&auth_token=");
        sb.append(api_key);
        if (sNN(departure)) {
            sb.append("&trip[Stages][0][Origin][Coords]=");
            if (departure.indexOf(",") > 0) {
                sb.append(departure);
            } else {
                searchtype = this.SEARCH_FROM_POINT;
                vecchio = INSERT;
                this.key = api_key;
                from = TextUtil.replace(departure, " ", "%20");
                to = TextUtil.replace(arrival, " ", "%20");
                this.data = departureDate;
                this.seats = freeSeats;
                this.info = TextUtil.replace(generalNotes, " ", "%20");
                this.deviation=deviation;
                this.id=id;
                this.vehicleID=vehicleID;
                this.vehicleClass=vehicleClass;
                this.idFreq=isFrequently;
                this.draft=draft;
                this.estimatedTime=estimatedTime;
                this.estimatedBudget=estimatedBudget;
                this.expiration=expirationDate;
                this.smokers=smokers;
                this.license=license;
                this.getMapPoint(api_key, departure);
                return;
            }

        }
        if (this.sNN(arrival)) {
            sb.append("&trip[Stages][0][Destination][Coords]=");
            if (arrival.indexOf(",") > 0) {
                sb.append(arrival);
            } else {
                searchtype = this.SEARCH_TO_POINT;
                vecchio = INSERT;
                this.key = api_key;
                from = TextUtil.replace(departure, " ", "%20");
                to = TextUtil.replace(arrival, " ", "%20");
                this.data = departureDate;
                this.seats = freeSeats;
                this.info = TextUtil.replace(generalNotes, " ", "%20");
                this.deviation=deviation;
                this.id=id;
                this.vehicleID=vehicleID;
                this.vehicleClass=vehicleClass;
                this.idFreq=isFrequently;
                this.draft=draft;
                this.estimatedTime=estimatedTime;
                this.estimatedBudget=estimatedBudget;
                this.expiration=expirationDate;
                this.smokers=smokers;
                this.license=license;
                this.getMapPoint(api_key, arrival);
                return;
            }
        }
        if (this.sNN(departureDate)) {
            sb.append("&trip[DepartureDate]=");
            sb.append(departureDate);
        }
        /*if (this.sNN(expirationDate)) {
            sb.append("&trip[ReservationExpirationDate]=");
            sb.append(expirationDate);
        }*/
        if (this.sNN(generalNotes)) {
            sb.append("&trip[Notes]=");
            sb.append(TextUtil.replace(generalNotes, " ", "%20"));
        }
        sb.append("&trip[FreeSeats]=");
        sb.append(freeSeats);
        sb.append("&trip[Draft]=");
        sb.append(draft);
        //sb.append("&trip[RequireLicensed]=");
        //sb.append(license);
        //sb.append("&trip[AcceptSmokers]=");
        //sb.append(smokers);
        sb.append("&trip[ID]=");
        sb.append(id);
        sb.append("&trip[IsFrequent]=");
        sb.append(isFrequently);
        sb.append("&trip[DeviationRadius]=");
        sb.append(deviation);
        sb.append("&trip[Stages][0][EstimatedBudget]=");
        sb.append(estimatedBudget);
        sb.append("&trip[Stages][0][EstimatedTime]=");
        sb.append(estimatedTime);
        sb.append("&trip[Stages][0][Vehicle][Id]=");
        sb.append(vehicleID);
        sb.append("&trip[Stages][0][Vehicle][VehicleClass]=");
        sb.append(vehicleClass);
        url = sb.toString();
        vecchio = -1;
        method = INSERT;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting booked Travel
     * @param api_key
     * @param start
     * @param max
     * @throws triploa.LOAException
     */
    public void booked(String api_key, int start, int max) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=BookedTrips&auth_token=");
        sb.append(api_key);
        sb.append("&start=");
        sb.append(start);
        sb.append("&max=");
        sb.append(max);
        url = sb.toString();
        method = BOOKED;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting offered Travel
     * @param api_key
     * @param start
     * @param max
     * @throws triploa.LOAException
     */
    public void offered(String api_key, int start, int max) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=OfferedTrips&auth_token=");
        sb.append(api_key);
        sb.append("&start=");
        sb.append(start);
        sb.append("&max=");
        sb.append(max);
        url = sb.toString();
        method = OFFERED;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting favorite Trip
     * @param api_key
     * @param start
     * @param max
     * @throws triploa.LOAException
     */
    public void favoriteTrip(String api_key, int start, int max) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=FavoritesTrips&auth_token=");
        sb.append(api_key);
        sb.append("&start=");
        sb.append(start);
        sb.append("&max=");
        sb.append(max);
        url = sb.toString();
        method = FAVORITE;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for do reverse Geocoding
     * @param api_key
     * @param lat
     * @param lon
     * @throws triploa.LOAException
     */
    public void reverseGeocoding(String api_key, float lat, float lon) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestGeoRef.aspx?method=reverseGeoCoding&auth_token=");
        sb.append(api_key);
        sb.append("&latitudine=");
        sb.append(lat);
        sb.append("&longitudine=");
        sb.append(lon);
        url = sb.toString();
        method = REVERSE_GEOCODING;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for book travel
     * @deprecated
     * @param api_key
     * @param tripId
     * @throws triploa.LOAException
     */
    public void book(String api_key, String tripId) throws LOAException {
        /*Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=BookTrip&auth_token=");
        sb.append(api_key);
        sb.append("&trip_id=");
        sb.append(tripId);
        sb.append("&from=");
        Stage s=(Stage)midlet.t.stages.elementAt(0);
        sb.append();
        
        url = sb.toString();
        method = BOOK;
        Thread t = new Thread(this);
        t.start();*/
        
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=BookTrip&auth_token=");
        sb.append(api_key);
        sb.append("&trip_id=");
        sb.append(tripId);
        url = sb.toString();
        method = BOOK;
        Thread t = new Thread(this);
        t.start();
    }
    /**
     * Call WS for booking a travel
     * @param api_key
     * @param tripId
     * @param from
     * @param to
     * @param seats
     * @param tollerance
     * @param notes
     * @throws triploa.LOAException
     */
    public void book(String api_key, String tripId,String from,String to,String seats,String tollerance,String notes) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=RequestReservation&auth_token=");
        sb.append(api_key);
        sb.append("&trip_id=");
        sb.append(tripId);
        if(sNN(from)){
            sb.append("&from=");
            boolean ok=true;
            int appi=from.indexOf(',');
            if(appi<0) ok=false;
            else{
            String sub=from.substring(0, appi-1);
            try
            {
                Float.parseFloat(sub);
            }catch(NumberFormatException e)
            {
                ok=false;
            }
            }
            if(ok)
                sb.append(from);
            else { 
                searchtype = this.SEARCH_FROM_POINT;
                vecchio = BOOK;
                this.key = api_key;
                this.from = TextUtil.replace(from, " ", "%20");
                this.to = TextUtil.replace(to, " ", "%20");
                this.seats = seats;
                this.info = TextUtil.replace(notes, " ", "%20");
                this.deviation=tollerance;
                this.tripId=tripId;
                this.getMapPoint(api_key, from);
                return;
            }
        }
        if(sNN(to)){
            sb.append("&to=");
            boolean ok=true;
            int appi=to.indexOf(',');
            if(appi<=0) ok=false;
            else{
                String sub=to.substring(0, appi-1);
                try
                {
                    Float.parseFloat(sub);
                }catch(NumberFormatException e)
                {
                    ok=false;
                }
            }
            if(ok)
                sb.append(to);
            else { 
                searchtype = this.SEARCH_TO_POINT;
                vecchio = BOOK;
                this.key = api_key;
                this.from = TextUtil.replace(from, " ", "%20");
                this.to = TextUtil.replace(to, " ", "%20");
                this.seats = seats;
                this.info = TextUtil.replace(notes, " ", "%20");
                this.deviation=tollerance;
                this.tripId=tripId;
                this.getMapPoint(api_key, to);
                return;
            }
        }
        sb.append("&seats_no=");
        sb.append(seats);
        sb.append("&tolerance=");
        sb.append(tollerance);
        sb.append("&notes=");
        sb.append(TextUtil.replace(notes, " ", "%20"));
        url = sb.toString();
        method = BOOK;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for cancel a booked Travel
     * @param api_key
     * @param tripId
     * @throws triploa.LOAException
     */
    public void cancelBookedTrip(String api_key, String tripId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=CancelTrip&auth_token=");
        sb.append(api_key);
        sb.append("&trip_id=");
        sb.append(tripId);
        url = sb.toString();
        method = CANCEL;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for deleting Offered Travel
     * @param api_key
     * @param tripId
     * @throws triploa.LOAException
     */
    public void deleteOfferedTrip(String api_key, String tripId) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=DeleteTrip&auth_token=");
        sb.append(api_key);
        sb.append("&trip_id=");
        sb.append(tripId);
        url = sb.toString();
        method = DELETE;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for adding a Travel to Favorite
     * @param api_key
     * @param tripId
     * @throws triploa.LOAException
     */
    public void addToFavorite(String api_key, String tripId) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=AddTripToFavorites&auth_token=");
        sb.append(api_key);
        sb.append("&trip_id=");
        sb.append(tripId);
        url = sb.toString();
        method = ADD_FAVORITE;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for removing a Travel form Favorite
     * @param api_key
     * @param tripId
     * @throws triploa.LOAException
     */
    public void removeFromFavorite(String api_key, String tripId) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=RemoveTripFromFavorites&auth_token=");
        sb.append(api_key);
        sb.append("&trip_id=");
        sb.append(tripId);
        url = sb.toString();
        method = REMOVE_FAVORITE;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting detalied info for a Travel
     * @param api_key
     * @param tripId
     * @throws triploa.LOAException
     */
    public void getDetailedInfo(String api_key, int tripId) throws LOAException {
        Vector list = new Vector();
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestTrip.ashx?method=GetTripDetails&auth_token=");
        sb.append(api_key);
        sb.append("&trip_id=");
        sb.append(tripId);
        url = sb.toString();
        method = DETAILED_INFO;
        Thread t = new Thread(this);
        t.start();
    }

    private void doMyProfile(KXmlParser p) {
        try {
            midlet.updateProfile(UserProfile.create(p));
        } catch (Exception e) {
            midlet.ShowError(Locale.get("Error") + " " + Locale.get("in") + " " + Locale.get("ViewProfile"));
        }
    }

    /**
     * Call WS for getting profiles
     * @param api_key
     * @param username
     * @throws triploa.LOAException
     */
    public void getProfile(String api_key, String username) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=getProfile&auth_token=");
        sb.append(api_key);
        sb.append("&user_id=");
        sb.append(username);
        url = sb.toString();
        method = MY_PROFILE;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting unreaded Message
     * @param api_key
     * @param username
     * @throws triploa.LOAException
     */
    public void getUnreadMessages(String api_key, String username) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestMessage.ashx?method=GetUnreadMessages&auth_token=");
        sb.append(api_key);
        sb.append("&userid=");
        sb.append(username);
        url = sb.toString();
        method = UNREAD_MSG;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting Vehicle List for the user
     * @param api_key
     * @param userId
     * @throws triploa.LOAException
     */
    public void getVehicleList(String api_key, String userId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=GetUserVehicleList&auth_token=");
        sb.append(api_key);
        sb.append("&user_id=");
        sb.append(userId);
        url = sb.toString();
        method = VEHICLE_LIST;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for add a Friend
     * @param api_key
     * @param userId
     * @throws triploa.LOAException
     */
    public void addFriend(String api_key, String userId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=AddFriend&auth_token=");
        sb.append(api_key);
        sb.append("&friend_id=");
        sb.append(userId);
        url = sb.toString();
        method = ADD_FRIEND;
        Thread t = new Thread(this);
        t.start();
    }
    /**
     * Call WS for Accept a Friend
     * @param api_key
     * @param userId
     * @throws triploa.LOAException
     */
    public void acceptFriendshipRequest(String api_key, String userId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=AcceptFriendshipRequest&auth_token=");
        sb.append(api_key);
        sb.append("&friend_id=");
        sb.append(userId);
        url = sb.toString();
        method = this.ACCEPT_FRIENDSHIP;
        Thread t = new Thread(this);
        t.start();
    }
    /**
     * Call WS for Denied a Friend Request
     * @param api_key
     * @param userId
     * @throws triploa.LOAException
     */
    public void denyFriendshipRequest(String api_key, String userId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=DenyFriendshipRequest&auth_token=");
        sb.append(api_key);
        sb.append("&friend_id=");
        sb.append(userId);
        url = sb.toString();
        method = this.DENY_FRIENDSHIP;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for unBan a Friend
     * @param api_key
     * @param userId
     * @throws triploa.LOAException
     */
    public void unbanFriend(String api_key, String userId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=UnbanFriend&auth_token=");
        sb.append(api_key);
        sb.append("&friend_id=");
        sb.append(userId);
        url = sb.toString();
        method = UNBAN_FRIEND;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for ban a Friend 
     * @param api_key
     * @param userId
     * @throws triploa.LOAException
     */
    public void banFriend(String api_key, String userId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=BanFriend&auth_token=");
        sb.append(api_key);
        sb.append("&friend_id=");
        sb.append(userId);
        url = sb.toString();
        method = BAN_FRIEND;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for remove a Friend
     * @param api_key
     * @param userId
     * @throws triploa.LOAException
     */
    public void removeFriend(String api_key, String userId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=RemoveFriend&auth_token=");
        sb.append(api_key);
        sb.append("&friend_id=");
        sb.append(userId);
        url = sb.toString();
        method = REMOVE_FRIEND;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting Notifications
     * @param api_key
     * @throws triploa.LOAException
     */
    public void getNotificationList(String api_key) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=GetUserNotificationsList&auth_token=");
        sb.append(api_key);
        url = sb.toString();
        method = NOTIFICATION_LIST;
        Thread t = new Thread(this);
        t.start();
    }
    /**
     * Call WS for getting the current request of Friendship
     * @param api_key
     * @throws triploa.LOAException
     */
    public void getUserFriendshipRequests(String api_key) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=GetUserFriendshipRequests&auth_token=");
        sb.append(api_key);
        url = sb.toString();
        method = USER_REQUEST_LIST;
        Thread t = new Thread(this);
        t.start();
    }
    /**
     * Call WS for getting the current queuq of Friendship request
     * @param api_key
     * @throws triploa.LOAException
     */
    public void getUserFriendshipNotifications(String api_key) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=GetUserFriendshipNotifications&auth_token=");
        sb.append(api_key);
        url = sb.toString();
        method = this.FRIENDSHIP_NOTIFICATION;
        Thread t = new Thread(this);
        t.start();
    }
    /**
     * Call WS for getting Friend List
     * @param api_key
     * @throws triploa.LOAException
     */
    public void getBuddyList(String api_key) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=GetBuddyList&auth_token=");
        sb.append(api_key);
        url = sb.toString();
        method = BUDDY_LIST;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting banned Friend List
     * @param api_key
     * @throws triploa.LOAException
     */
    public void getBannedList(String api_key) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestUser.ashx?method=GetUserNotificationList&auth_token=");
        sb.append(api_key);
        url = sb.toString();
        method = BANNED_LIST;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for setting to readed a message
     * @param api_key
     * @param msgId
     * @throws triploa.LOAException
     */
    public void setReadMessage(String api_key, int msgId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestMessage.ashx?method=MarkMessageAsRead&auth_token=");
        sb.append(api_key);
        sb.append("&message_id=");
        sb.append(msgId);
        url = sb.toString();
        method = READED_MSG;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for deleting a message
     * @param api_key
     * @param msgId
     * @throws triploa.LOAException
     */
    public void deleteMessage(String api_key, int msgId) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestMessage.ashx?method=DeleteMessage&auth_token=");
        sb.append(api_key);
        sb.append("&message_id=");
        sb.append(msgId);
        url = sb.toString();
        method = DELETE_MSG;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting Messages
     * @param api_key
     * @param username
     * @param start
     * @param max
     * @throws triploa.LOAException
     */
    public void getMessages(String api_key, String username, int start, int max) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestMessage.ashx?method=GetReceivedMessages&auth_token=");
        sb.append(api_key);
        sb.append("&userid=");
        sb.append(username);
        sb.append("&start=");
        sb.append(start);
        sb.append("&max=");
        sb.append(max);
        url = sb.toString();
        method = ALL_MSG;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for getting Sent Messages
     * @param api_key
     * @param username
     * @param start
     * @param max
     * @throws triploa.LOAException
     */
    public void getSentMessages(String api_key, String username, int start, int max) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestMessage.ashx?method=GetSentMessages&auth_token=");
        sb.append(api_key);
        sb.append("&userid=");
        sb.append(username);
        sb.append("&start=");
        sb.append(start);
        sb.append("&max=");
        sb.append(max);
        url = sb.toString();
        method = SENT_MSG;
        Thread t = new Thread(this);
        t.start();
    }

    /**
     * Call WS for send a Message
     * @param api_key
     * @param username
     * @param title
     * @param text
     * @param priority
     * @throws triploa.LOAException
     */
    public void sendMessage(String api_key, String username, String title, String text, boolean priority) throws LOAException {
        StringBuffer sb = new StringBuffer(base);
        sb.append("RestMessage.ashx?method=SendMessage&auth_token=");
        sb.append(api_key);
        sb.append("&receivers=");
        sb.append(username);
        sb.append("&title=");
        sb.append(TextUtil.replace(title, " ", "%20"));
        sb.append("&text=");
        sb.append(TextUtil.replace(text, " ", "%20"));
        sb.append("&priority=");
        sb.append(priority);
        url = sb.toString();
        method = SEND_MSG;
        Thread t = new Thread(this);
        t.start();
    }
}

