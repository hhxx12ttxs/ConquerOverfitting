/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Database;

import Logic.Filters.CernetPacketFilter;
import Logic.Filters.IPacketFilter;
import Network.IPacket;
import Network.Packet;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.sqlite.JDBC;
/**
 *
 * @author Kuziki
 */
public class Database implements IDatabaseProxy{
    private Database()
    {
        try {
            conn = getConn();
        } catch (Exception ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static Database instance = null;
    public synchronized  static IDatabaseProxy instance()
    {
        if(instance==null){
            instance = new Database();
        }
        return instance;
    }
    synchronized  public void savePacket(List<IPacket> p)
    {
        try {
            conn.setAutoCommit(false);
            Statement stat = conn.createStatement();
            for(int i=0 ; i<p.size() ; ++i) // Change to Foreach
//            for(IPacket pack : p)
            {
                IPacket pack = p.get(i);
                Date rdate=pack.getPacketRecvTime();
                int sip  = pack.getSourceAddress();
                int dip  = pack.getDestAddress();
                int sport = pack.getSourcePort();
                int dport = pack.getDestPort();
                int size = pack.getPacketLength();
                int flag = pack.getPacketFlag();
                boolean  UpOrDown = pack.isUpload();
                String insertSql = String.format("insert into Detail(PRecvTime,PS_IP,PD_IP,PS_Port,PD_Port,PSize,PIsUpload,PFlag) values(%d,%d,%d,%d,%d,%d,%d,%d)"
                        , rdate.getTime(),sip,dip,sport,dport,size,UpOrDown?1:0,flag);
                stat.addBatch(insertSql);
            }
            stat.executeBatch();
            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
                JDialog j = new JDialog();
                j.setAlwaysOnTop(true);
                j.setTitle("Database Error");
                JLabel ver = new JLabel("Database has been destoryed,please delete it");
                j.add(ver);
                j.setSize(320, 240);
                j.setVisible(true);
            } catch (SQLException ex1) {
            }
        }
    }
    public List<IPacket> getPacket(Date from,Date to) throws SQLException
    {
        conn.setAutoCommit(true);
        Statement stat = conn.createStatement();
        Long fd = from.getTime();
        Long td = to.getTime();
        String sqlQuery = "select * from detail where PRecvTime >= ";
        sqlQuery += Long.toString(fd);
        sqlQuery += " and PRecvTime <= ";
        sqlQuery += Long.toString(td);
        ResultSet rs = stat.executeQuery(sqlQuery);
        List< IPacket > p = new ArrayList< IPacket >();
        while(rs.next())
        {
            Packet pack = new Packet();
            Date d = new Date();
            d.setTime(rs.getLong(2));
            pack.RecvTime = d;
            pack.SIP = rs.getInt("PS_IP");
            pack.DIP = rs.getInt("PD_IP");
            pack.SPort = rs.getInt(5);
            pack.DPort = rs.getInt(6);
            pack.PackLen = rs.getInt(7);
            pack.IsUpdate = rs.getBoolean(8);
            pack.PacketFlag = rs.getInt("PFlag");
            p.add(pack);
        }
        rs.close();
        return p;
    }
    public List<IPacket> getPacket(Date cdate) throws SQLException
    {
        int year = cdate.getYear();
        int month = cdate.getMonth();
        int day = cdate.getDate();
        int tday = day+1;
        Date from  = new Date(year,month,day);
        Date to = new Date(year,month,tday);
        return getPacket(from, to);
    }
    private static String url="org.sqlite.JDBC";
    private static String dri="jdbc:sqlite:flow.sqlite";
    private static String table0 = "CREATE TABLE Detail ( PPacketID integer PRIMARY KEY AUTOINCREMENT,PRecvTime integer(8) NOT NULL,PS_IP integer NOT NULL,PD_IP integer NOT NULL,PS_Port integer NOT NULL,PD_Port integer NOT NULL,PSize integer NOT NULL,PIsUpload boolean NOT NULL,PFlag integer NOT NULL)";
    private static String table1 = "CREATE TABLE Simple (PDate date PRIMARY KEY,POuterLegnth integer NOT NULL,PInnerLength integer NOT NULL)";
    public static Connection getConn() throws Exception
    {
        File f = new File("flow.sqlite");
        boolean fexist = f.exists();
        Class.forName(url);
        Connection conn = DriverManager.getConnection(dri);
        if(!fexist)
        {
            Statement stat = conn.createStatement();
            stat.execute(table0);
            stat.execute(table1);
        }
        return conn;
    }
    public void Closedb() throws SQLException
    {
        conn.close();
    }
    private static Connection conn;

    public List<Flow> getFlow(Date from, Date to) throws SQLException
    {
        List<Flow> sDate = new ArrayList<Flow>();
        conn.setAutoCommit(true);
        Statement stat = conn.createStatement();
        int fromYear = from.getYear()+1900;
        int fromMonth = from.getMonth()+1;
        int fromDay = from.getDate();
        String fyear,fmonth,fday;
        fyear = Integer.toString(fromYear);
        fmonth = Integer.toString(fromMonth);
        fday = Integer.toString(fromDay);
        if(fromMonth < 10) fmonth = "0" + fmonth;
        if(fromDay < 10)fday = "0" + fday;
        String fromD = "'"+fyear + "-"+fmonth +"-"+fday+"'";
        int toYear = to.getYear()+1900;
        int toMonth = to.getMonth()+1;
        int toDay = to.getDate();
        String tyear,tmonth,tday;
        tyear = Integer.toString(toYear);
        tmonth = Integer.toString(toMonth);
        tday = Integer.toString(toDay);
        if(toMonth < 10) tmonth = "0" + tmonth;
        if(toDay < 10)tday = "0" + tday;
        String toD = "'"+tyear + "-"+tmonth +"-"+tday+"'";
        String sqlQuery = "select * from Simple where PDate >= " +fromD + " and PDate <= " + toD;
        ResultSet rs = stat.executeQuery(sqlQuery);
        while(rs.next())
        {
            Flow sp = new Flow();
            String ts = rs.getString(1);
            String[] ymd = ts.split("-");
            int yy = Integer.parseInt(ymd[0])-1900;
            int mm = Integer.parseInt(ymd[1])-1;
            int dd = Integer.parseInt(ymd[2]);
            sp.sDate = new Date(yy,mm,dd);
            sp.outerSize = rs.getLong(2) / 1024;
            sp.innerSize = rs.getLong(3) / 1024;
            sDate.add(sp);
        }
        return sDate;
    }

    public Flow getFlow(Date cdate) throws SQLException {
        List<Flow> gf = getFlow(cdate, cdate);
        if(gf.isEmpty())
        {
            Flow ans = new Flow();
            return ans;
        }
        else return gf.get(0);
    }
    public void compress(Date cdate)throws SQLException
    {
        conn.setAutoCommit(true);
        Statement stat = conn.createStatement();
        Long deadtime = cdate.getTime();
        String sqlQuery = "select * from Detail where PRecvTime < ";
        sqlQuery += Long.toString(deadtime);
        ResultSet rs = stat.executeQuery(sqlQuery);
        List< Packet > p = new ArrayList< Packet >();
        String sqlDel = "delete from Detail where PRecvTime < ";
        sqlDel += Long.toString(deadtime);
        while(rs.next())
        {
            Packet pack = new Packet();
            Date d = new Date();
            d.setTime(rs.getLong(2));
            pack.RecvTime = d;
            pack.SIP = rs.getInt(3);
            pack.DIP = rs.getInt(4);
            pack.SPort = rs.getInt(5);
            pack.DPort = rs.getInt(6);
            pack.PackLen = rs.getInt(7);
            pack.IsUpdate = rs.getBoolean(8);
            pack.PacketFlag = rs.getInt(9);
            p.add(pack);
        }
        rs.close();
        stat.execute(sqlDel);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String temp = "";
        String sqlInsert = "";
        String sqlUpdate = "";
        int total_in = 0;
        int total_out = 0;
        for(Packet ip : p)
        {
            temp = sdf.format(ip.RecvTime);
            if(inStub(ip))
            {
                System.out.println(ip.getDestAddressString());
                System.out.println(ip.getSourceAddressString());
                sqlQuery = "select * from Simple where PDate = '";
                sqlQuery =  sqlQuery + temp + "'";
                ResultSet rtemp = stat.executeQuery(sqlQuery);
                if(rtemp.next())
                {
                    int origin = rtemp.getInt(3);
                    origin += ip.PackLen;
                    sqlUpdate = "update Simple set PInnerLength = ";
                    sqlUpdate  = sqlUpdate + Integer.toString(origin) + " where PDate = '";
                    sqlUpdate = sqlUpdate + temp + "'";
                    stat.execute(sqlUpdate);
                }
                else
                {
                    sqlInsert = "insert into Simple values('";
                    sqlInsert = sqlInsert + temp +"',0," + Integer.toString(ip.PackLen)+")";
                    stat.execute(sqlInsert);
                }
            }
            else
            {
                sqlQuery = "select * from Simple where PDate = '";
                sqlQuery =  sqlQuery + temp + "'";
                ResultSet rtemp = stat.executeQuery(sqlQuery);
                if(rtemp.next())
                {
                    int origin = rtemp.getInt(2);
                    origin += ip.PackLen;
                    sqlUpdate = "update Simple set POuterLegnth = ";
                    sqlUpdate  = sqlUpdate + Integer.toString(origin) + " where PDate = '";
                    sqlUpdate = sqlUpdate + temp + "'";
                    stat.execute(sqlUpdate);
                }
                else
                {
                    sqlInsert = "insert into Simple values('";
                    sqlInsert = sqlInsert + temp + Integer.toString(ip.PackLen)+"',0)";
                    stat.execute(sqlInsert);
                }
            }
        }
    }
    private boolean inStub(Packet p)
    {
        IPacketFilter pf = CernetPacketFilter.Instance();
        return pf.check(p);
    }
}

