package dcapi.util;

import java.sql.Timestamp;
import java.util.*;
import com.google.gson.*;

public class Reservation{

    public static final int VALID_RESERVATION = 0;
    public static final int INVALID_USR = 1;
    public static final int INVALID_EMAIL = 2;
    public static final int INVALID_SLOT = 3;
    public static final int INVALID_START = 4;
    public static final int CONFLICT_USR = 5;

    public static final int PWD_MIN = 8;
    public static final int PWD_MAX = 12;
    
    private Timestamp start_time = null, end_time = null;
    private long reserv_id = -1;
    private String user_name = null, user_pwd = null;
    private String user_email = null;
    //For generating random user_name user_pwd and user_email
    private static final String charSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-@.";
    private static final String pwdSet = charSet;
    public static final String userNamePattern = "([a-zA-Z]|[0-9]){6,16}";
    private static final int user_name_len = 10;
    private static final int user_pwd_len = 10;
    private static final int user_email_len = 10;

    public static final long oneDay = 24 * 60 * 60 *1000L;
    public static final long minSlot = 1 * 60 * 60 * 1000L;
    public static final long maxSlot = 24 * 60 * 60 * 1000L;
    public static final long startLimit = 24 * 60 * 60 * 1000L;

    public static final Timestamp minTime = new Timestamp(110, 0, 1, 0, 0, 0, 0);
    public static final Timestamp maxTime = new Timestamp(210, 0, 1, 0, 0, 0, 0);

    public Reservation(){
        this(new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
    }

    public Reservation(Reservation reserv){
        this(reserv.getStartTime(), reserv.getEndTime(), reserv.getReservID(), 
                reserv.getUserName(), reserv.getUserPWD(), reserv.getUserEmail());
    }

    public Reservation(Timestamp start_time, Timestamp end_time){
        this(start_time, end_time, 0, null, null, null);
    }

    public Reservation(Timestamp start_time, Timestamp end_time, long reserv_id,
            String user_name, String user_pwd, String user_email){
        //create a new object to prevent altering Reservation private data
        this.start_time = new Timestamp(start_time.getTime());
        this.end_time = new Timestamp(end_time.getTime());
        this.reserv_id = reserv_id;
        this.user_name = user_name;
        this.user_pwd = user_pwd;
        this.user_email = user_email;
    }

    public static int checkReservation(Reservation reserv){
        long curTime  = System.currentTimeMillis();
        return checkReservation(reserv, curTime);
    }

    public static int checkReservation(Reservation reserv, long curTime){
        Timestamp startTime = reserv.getStartTime();
        Timestamp endTime = reserv.getEndTime();

        long startLong = startTime.getTime();
        long endLong = endTime.getTime();
        long timeDiff = endLong - startLong;

        if(timeDiff > Reservation.maxSlot || timeDiff < Reservation.minSlot){
            return Reservation.INVALID_SLOT;
        }
        else if (startLong < curTime || startLong > (curTime + Reservation.startLimit)){
            return Reservation.INVALID_START;
        }

        String userName = reserv.getUserName();
        if(!userName.matches(userNamePattern)){
            return Reservation.INVALID_USR;
        }

        return Reservation.VALID_RESERVATION;
    }

    public Timestamp getStartTime(){
        return this.start_time;
    }

    public void setStartTime(Timestamp start_time){
        this.start_time = start_time;
    }

    public Timestamp getEndTime(){
        return this.end_time;
    }

    public void setEndTime(Timestamp end_time){
        this.end_time = end_time;
    }

    public long getReservID(){
        return this.reserv_id;
    }

    public void setReservID(long reserv_id){
        this.reserv_id = reserv_id;
    }

    public String getUserName(){
        return this.user_name;
    }

    public void setUserName(String user_name){
        this.user_name = user_name; 
    }

    public String getUserPWD(){
        return this.user_pwd;
    }

    public void setUserPWD(String user_pwd){
        this.user_pwd = user_pwd;
    }

    public String getUserEmail(){
        return this.user_email;
    }

    public void setUserEmail(String user_email){
        this.user_email = user_email;
    }

    public String serialize(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Reservation deSerialize(String data){
        try{
            Gson gson = new Gson();
            return gson.fromJson(data, Reservation.class);
        }
        catch(Exception ex){
            return null;
        }
    }

    public int compareTo(Reservation reserv){
        if (null == reserv)
            return -2;

        if (this.start_time.compareTo(reserv.getEndTime()) > 0){
            return 1;
        }
        else if(this.end_time.compareTo(reserv.getStartTime()) < 0){
            return -1;
        }
        else{
            return 0;
        }
    }

    public static String getRandString(int length){
        return getRandString(Reservation.charSet, length);
    }

    public static String getRandString(String charSet, int length){
        Random rand = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++){
            text[i] = charSet.charAt(rand.nextInt(charSet.length()));
        }
        return new String(text);
    }    

    public static String getRandPWD(){
        Random rand = new Random();
        int randLen = Reservation.PWD_MIN + rand.nextInt(Reservation.PWD_MAX - Reservation.PWD_MIN);
        return getRandString(Reservation.pwdSet, randLen);
    }


    /**
     * Get a random reservation interval, used for debug.
     */
    public static Reservation getRandReserv(){
        return Reservation.getRandReserv(365 * 10 * Reservation.oneDay, 10 * Reservation.oneDay);        
    }

    public static Reservation getRandReserv(long maxStart, long maxDuration){
        Random rand = new Random();
        long startShift = Math.abs(rand.nextLong()) % maxStart;
        long duration = Math.abs(rand.nextLong()) % maxDuration;
        Timestamp startTime = new Timestamp(minTime.getTime() + startShift);
        Timestamp endTime = new Timestamp(minTime.getTime() + startShift + duration);
        String userName = getRandString(user_name_len);
        String userPWD =  getRandString(user_pwd_len);
        String userEmail = getRandString(user_email_len);
        Reservation reserv = new Reservation(startTime, endTime, 0, userName, userPWD, userEmail);
        //        System.out.println(reserv.serialize());
        return reserv;
    }

    public static void main(String argv[]){
        Reservation reserv = new Reservation(Reservation.minTime, Reservation.maxTime);
        System.out.println(reserv.serialize());  
        Reservation reserv1 = Reservation.deSerialize(
                "{\"start_time\":\"Jan 1, 2010 12:00:00 AM\",\"end_time\":\"Jan 1, 2110 12:00:00 AM\",\"reserv_id\":-1}");
        System.out.println(reserv1.serialize());
        System.out.println(Reservation.minTime);
        System.out.println(Reservation.maxTime);
        System.out.println(Reservation.maxTime.compareTo(Reservation.minTime));
        System.out.println(Reservation.minTime.compareTo(Reservation.maxTime));
        Reservation reserv2 = new Reservation(Reservation.minTime, new Timestamp(Reservation.minTime.getTime() + Reservation.oneDay));
        Reservation reserv3 = new Reservation(new Timestamp(Reservation.minTime.getTime() + 2 * Reservation.oneDay), 
                new Timestamp(Reservation.minTime.getTime() + 3 * Reservation.oneDay));
        System.out.println(reserv2.compareTo(reserv3));
        System.out.println(reserv3.compareTo(reserv2));
        System.out.println(reserv.compareTo(reserv2));
        Reservation reserv4 = new Reservation(reserv2);
        System.out.println(reserv2.serialize());
        System.out.println(reserv4.serialize());
        reserv2.setStartTime(new Timestamp(Reservation.minTime.getTime() + 10 * Reservation.oneDay)); 
        System.out.println(reserv2.serialize());
        System.out.println(reserv4.serialize());
        System.out.println(Reservation.getRandString(10));
        System.out.println(Reservation.getRandString(15));
        System.out.println(Reservation.getRandString(20));
        System.out.println(Reservation.getRandString(25));
        System.out.println(Reservation.getRandString(30));
        System.out.println(Reservation.getRandReserv().serialize());
        System.out.println(Reservation.getRandReserv().serialize());
        System.out.println(Reservation.getRandReserv().serialize());
        System.out.println(Reservation.getRandReserv().serialize());

        String name = "abd07A98";
        System.out.println(name.matches(userNamePattern));
        String name1 = name + "Z_";
        System.out.println(name1.matches(userNamePattern));
        String name2 = name + "098909df7asBDCD";
        System.out.println(name2.matches(userNamePattern));
        String name3 = "abc10";
        System.out.println(name3.matches(userNamePattern));



    }

}




