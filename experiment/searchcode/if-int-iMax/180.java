package alkynedbmki;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AKdatabase {
    
    public AKdatabase() throws Exception {
        
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite://home/domagoj/Programming/XBMC/alkyneDB/database.sqlite");
        
        List allfiles = getFiles("//home/domagoj/Music",null);
        String[] thefiles = converter(allfiles);
        
        Statement stat = conn.createStatement();
        stat.executeUpdate("create table if not exists \"music_songs\" (path TEXT); ");
        
        PreparedStatement prep = conn.prepareStatement("insert into \"music_songs\" (path) values (?);");
        
        for (String f : thefiles){
            // stat.executeUpdate("insert into \"music_songs\" (path) values (\""+f+"\");" );
            prep.setString(1, f);
            prep.addBatch();
        }
        
        conn.setAutoCommit(false);
        prep.executeBatch();
        conn.setAutoCommit(true);
        
        conn.close();
        
        //filldata();
        
    }
    
    private void filldata(){
        String path = "//home/domagoj/Music";
        String[] extensions = new String[2];
        extensions[0] = "mp3";
        extensions[1] = "wav";
        
        List songs = getFiles(path,extensions);
        
        for (int i = 0; i < songs.size(); i++){
            System.out.println(songs.get(i));
        }
        
    }
    private List getFiles(String path, String[] extensions){
        List returner = new ArrayList();
        
        File[] dir = new File(path).listFiles();
        if (dir != null){
            for(File d : dir){
                if (d.isFile()){
                    returner.add(d.getAbsolutePath());
                }
                else if (d.isDirectory()){
                    List returner2 = getFiles(d.getAbsolutePath(), extensions);
                    returner.addAll(returner2);
                }
            }
        }
        
        return returner;
    }
    private String[] converter(List files){
        int imax = files.size();
        String[] returner = new String[imax];
        
        for (int i = 0; i < imax; i++){
            returner[i] = (String) files.get(i);
        }
        
        return returner;
    }

}

