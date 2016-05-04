/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wiqa.server.plugins;

import Classes.DebugClass;
import Classes.PluginsTypes;
import Interfaces.IPluginsBase;
import Interfaces.IBDPlugin;
import com.sun.rowset.CachedRowSetImpl;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

/**
 *
 * @author Maniak
 */
public class DB_PostgreSQL implements IPluginsBase, IBDPlugin {

    BufferedOutputStream bos;
    BufferedInputStream bis;

    private class NormalizeData {

        private Object _data;

        public NormalizeData(Object data) {
            _data = data;
        }

        @Override
        public String toString() {
            if (_data == null) {
                return "";
            }
            return _data.toString();
        }

        public String getString() {
            if (_data == null) {
                return "";
            }
            return _data.toString();
        }

        public int getInt() {
            if (_data == null) {
                return 0;
            }
            return Integer.parseInt(_data.toString());
        }

        public int getInteger() {
            return getInt();
        }

        public boolean getBoolean() {
            if (_data == null) {
                return false;
            }
            return Boolean.parseBoolean(_data.toString());
        }
    }

//    private Vector<NormalizeData> Normalize(ResultSet data) {
//        try {
//            ResultSetMetaData rsMetaData = data.getMetaData();
//            int numberOfColumns = rsMetaData.getColumnCount();
//            Vector<NormalizeData> tmp = new Vector<NormalizeData>();
//            tmp.add(new NormalizeData(""));
//            for (int i = 1; i <= numberOfColumns; i++) {
//                tmp.add(new NormalizeData(data.getObject(i)));
//            }
//            return tmp;
//        } catch (Exception ex) {
//            exClass.Except(ex);
//        }
//        return null;
//    }
//
//    private Vector<NormalizeData> Normalize(CachedRowSet data) {
//        try {
//            ResultSetMetaData rsMetaData = data.getMetaData();
//            int numberOfColumns = rsMetaData.getColumnCount();
//            Vector<NormalizeData> tmp = new Vector<NormalizeData>();
//            tmp.add(new NormalizeData(""));
//            for (int i = 1; i <= numberOfColumns; i++) {
//                tmp.add(new NormalizeData(data.getObject(i)));
//            }
//            return tmp;
//        } catch (Exception ex) {
//            exClass.Except(ex);
//        }
//        return null;
//    }
    private CachedRowSet NormalizeRowSet(CachedRowSet data) {
        try {
            ResultSetMetaData rsMetaData = data.getMetaData();
            int numberOfColumns = rsMetaData.getColumnCount();
            data.beforeFirst();
            while (data.next()) {
                boolean b = false;
                for (int i = 1; i <= numberOfColumns; i++) {
                    Object tp = data.getObject(i);
                    if (tp == null) {
                        switch (rsMetaData.getColumnType(i)) {
                            case -7: //bool
                                tp = false;
                                data.updateBoolean(rsMetaData.getColumnName(i), (Boolean) tp);
                                break;
                            case 4:  // int4
                                tp = 0;
                                data.updateInt(rsMetaData.getColumnName(i), (Integer) tp);
                                break;
                            case 91: //date
                                //?????? ?? ??????
                                break;
                            default:
                                tp = "";
                                data.updateString(rsMetaData.getColumnName(i), tp.toString());
                                break;
                        }
                        b = true;
                    }
                }
                if (b) {
                    data.updateRow();
                }
            }
            data.beforeFirst();
            return data;
        } catch (Exception ex) {
            exClass.Except(ex);
        }
        return null;
    }

    private ResultSet NormalizeRowSet(ResultSet data) {
        try {
            ResultSetMetaData rsMetaData = data.getMetaData();
            int numberOfColumns = rsMetaData.getColumnCount();

            data.beforeFirst();
            while (data.next()) {
                boolean b = false;
                for (int i = 1; i <= numberOfColumns; i++) {
                    Object tp = data.getObject(i);
                    if (tp == null) {
                        switch (rsMetaData.getColumnType(i)) {
                            case -7: //bool
                                tp = false;
                                data.updateBoolean(rsMetaData.getColumnName(i), (Boolean) tp);
                                break;
                            case 4:  // int4
                                tp = 0;
                                data.updateInt(rsMetaData.getColumnName(i), (Integer) tp);
                                break;
                            case 91: //date
                                //?????? ?? ??????
                                break;
                            default:
                                tp = "";
                                data.updateString(rsMetaData.getColumnName(i), tp.toString());
                                break;
                        }
                        b = true;
                    }
                }
                if (b) {
                    data.updateRow();
                }
            }
            data.beforeFirst();
            return data;
        } catch (Exception ex) {
            exClass.Except(ex);
        }
        return null;
    }

    public String getName() {
        return "WIQA-PostgreSQL v1.0";
    }
    private static final Class[] parameters = new Class[]{
        URL.class
    };

    public void init() {
    }
    private String _ServerAddress;
    private String _Port;
    private String _DataBase;
    private String _User;
    private String _Password;
    private Connection connection;
    private DebugClass exClass = new DebugClass(this);

    public CachedRowSet GetExampleTable() {
        try {
            CachedRowSet oCachedRowSet = new CachedRowSetImpl();
            oCachedRowSet.setCommand("select * from test");
            oCachedRowSet.execute(connection);
            return NormalizeRowSet(oCachedRowSet);
//                rs.close();

        } catch (Exception ex) {
            exClass.Except(ex);
            reinit();
            return null;
        }
    }

    public PluginsTypes GetType() {
        return PluginsTypes.Database;
    }

    public void destroy() {
        try {
            connection.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void init(String ServerAddress, String Port, String Base, String User, String Password) {

        this._ServerAddress = ServerAddress;
        this._Port = Port;
        this._DataBase = Base;
        this._User = User;
        this._Password = Password;
        reinit();
    }

    private void reinit() {
        try {
            Class.forName("org.postgresql.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:postgresql://" + _ServerAddress + "/" + _DataBase, _User, _Password);

        } catch (Exception ex) {
            exClass.Except(ex);
        }
    }

    public CachedRowSet DoQuery(String Query) {
        try {
            CachedRowSet oCachedRowSet = new CachedRowSetImpl();
            oCachedRowSet.setCommand(Query);
            oCachedRowSet.execute(connection);
            return NormalizeRowSet(oCachedRowSet);
//                rs.close();

        } catch (Exception ex) {
            exClass.Except(ex);
            reinit();
            return null;
        }

    }

    public CachedRowSet RetValue(String qstr) {
        try {
            CachedRowSet oCachedRowSet = new CachedRowSetImpl();
            exClass.PrintMessage(qstr);
            oCachedRowSet.setCommand(qstr);
            oCachedRowSet.execute(connection);
            exClass.PrintMessage(oCachedRowSet);
            return NormalizeRowSet(oCachedRowSet);
//                rs.close();

        } catch (Exception ex) {
            exClass.Except(ex);
            reinit();
            return null;
        }
    }

    public void DoCommand(String Command) {
        try {
            Statement s = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            s.executeUpdate(Command);
            s.close();
        } catch (Exception ex) {
            exClass.Except(ex);
            reinit();
        }
    }

    public Vector<String> RegisterClient(String Login, String Password) {
        Vector<String> Temp = new Vector<String>();
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery("select ITnum,SFName,SSName,SSurName,Imgr,IRights,IPost,SNick,CStatus from USERS where SNick='" + Login + "' and SPasswd='" + Password + "' and CStatus>0"));

            if (rs.next()) {

                Temp.add(rs.getString(1));
                Temp.add(rs.getString(2));
                Temp.add(rs.getString(7));
                Temp.add(rs.getString(9));
                Temp.add(rs.getString(5));
                Temp.add(rs.getString(6));
                Temp.add(rs.getString(8));
            }
            return Temp;
//                rs.close();
        } catch (Exception ex) {
            exClass.Except(ex);

            reinit();
            return Temp;
        }
    }

    public CachedRowSet GetProjects(Vector<String> Key) {
        //ADOQ : TADOQuery;
        String str, rig = null;
        int i, j, k;
        CachedRowSet G, UT = null, GT = null;
        Vector<String> GrList, PrjList;
        boolean allow1, allow2;

        allow2 = false;
        GrList = new Vector<String>();
        PrjList = new Vector<String>();
        G = GetGroupsByUser(Key, Integer.parseInt(Key.elementAt(0)), 0);
        try {
            if (!G.wasNull()) {
                while (G.next()) {
                    GrList.add(G.getInt(2) + "");
                }
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        UT = GetTasksByUser(Key, Integer.parseInt(Key.elementAt(0)), 1);
        try {
            if (!UT.wasNull()) {
                while (UT.next()) {
                    allow1 = true;
                    for (i = 0; i < PrjList.size() - 1; i++) {
                        if (PrjList.elementAt(i).equals(UT.getInt(7) + "")) {
                            allow1 = false;
                        }
                    }
                    if (allow1) {
                        PrjList.add(UT.getInt(7) + "");
                    }
                }
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        for (k = 0; k < GrList.size() - 1; k++) {
            GT = GetTasksByGroup(Key, Integer.parseInt(GrList.elementAt(k)), 1);
            try {
                if (!GT.wasNull()) {
                    while (UT.next()) {
                        allow1 = true;
                        for (i = 0; i < PrjList.size() - 1; i++) {
                            if (PrjList.elementAt(i) == (GT.getInt(7) + "")) {
                                allow1 = false;
                            }
                        }
                        if (allow1) {
                            PrjList.add(GT.getInt(7) + "");
                        }
                    }
                }
            } catch (SQLException ex) {
                exClass.Except(ex);
            }
        }
        rig = Key.elementAt(5);
        if (rig.charAt(1) == '1') {
            allow2 = true;
        }
        if (!Key.get(3).equals("2")) {
            str = "select a.Iid, a.SName, a.IAuthor, b.SSurName, b.SNick, a.DDate, a.DEndDate, a.CStatus from Projects a, USERS b where a.IAuthor=b.ITnum and a.CStatus=1";
            if (!allow2) {
                str = str + " and a.Iid=0";
            }
            if (allow2) {
                str = str + " and (a.Iid=0";
                for (i = 0; i < PrjList.size() - 1; i++) {
                    str = str + " or a.Iid=" + PrjList.elementAt(i);
                }
                str = str + ")";
            }
        } else {
            str = "select a.Iid, a.SName, a.IAuthor, b.SSurName, b.SNick, a.DDate, a.DEndDate, a.CStatus from Projects a, USERS b where b.ITnum=" + Key.elementAt(0);
        }
        return RetValue(Key, str);
    }

    public CachedRowSet GetQA(Vector<String> Key, int Owner, int ProjectID, String QAType,
            String QStr, String DT1, String DT2) {
        int i, j;
        String str;
        CachedRowSet Temp;
        if (QStr.equals("")) {
            str = "select a.MText,a.Iid,a.IAuthor,a.Itype,a.DcreateDT,a.SDataInfo,a.SDataPath,a.SSymName,a.IVersion,a.IStatus,a.IReason,a.IWV,a.IOwner,a.Ipid,a.Prec,a.Draft,(select count(i.Iid) from QAReg i where i.IOwner=a.Iid";
            str = str + " and i.IVersion in (select max(k.IVersion) from QAReg k where k.Ipid=i.Ipid and k.IOwner=i.IOwner and k.SSymName=i.SSymName and k.Itype=i.Itype)) from QAReg a where";
            str = str + " a.IVersion in (select max(b.IVersion) from QAReg b where a.Ipid=a.Ipid and b.IOwner=a.IOwner and b.SSymName=a.SSymName and b.Itype=a.Itype)";
            if (Owner > -1) {
                str = str + " and a.IOwner=" + Owner;
            }
            if (ProjectID != 0) {
                str = str + " and a.Ipid=" + ProjectID;
            }
        } else {
            str = "select a.MText,a.Iid,a.IAuthor,a.Itype,a.DcreateDT,a.SDataInfo,a.SDataPath,a.SSymName,a.IVersion,a.IStatus,a.IReason,a.IWV,a.IOwner,a.Ipid,a.Prec,a.Draft,(select count(i.Iid) from QAReg i where i.IOwner=a.Iid";
            str = str + " and i.IVersion in (select max(k.IVersion) from QAReg k where k.Ipid=i.Ipid and k.IOwner=i.IOwner and k.SSymName=i.SSymName and k.Itype=i.Itype)) from QAReg a where (" + QStr + ")";
            str = str + " and a.IVersion in (select max(b.IVersion) from QAReg b where b.Ipid=a.Ipid and b.IOwner=a.IOwner and b.SSymName=a.SSymName and b.Itype=a.Itype)";
            if (Owner > -1) {
                str = str + " and a.IOwner=" + Owner;
            }
            if (ProjectID != 0) {
                str = str + " and a.Ipid=" + ProjectID;
            }
        }
        if (!QAType.equals("0")) {
            str = str + " and a.Itype=" + QAType;
        }
        str = str + " order by a.SSymName";
        Temp = RetValue(Key, str);

        try {
            if (!Temp.isBeforeFirst()) {
                Temp.beforeFirst();
            }
            CachedRowSet Temp2 = null;
            Temp2 = Temp.createCopy();
            while (Temp.next()) {
                Temp2.next();
                for (i = 1; i <= 17; i++) {
                    if (Temp2.getString(i) == null) {
                        Temp2.updateString(i, "");
                    }
                }
                Temp2.updateRow();
                for (i = 1; i <= 7; i++) {
                    Temp.setString(i, Temp2.getString(i).trim());
                }
                Temp.updateInt(8, Temp2.getInt(17));
                Temp.updateString(9, Temp2.getString(8).trim());
                Temp.updateInt(10, Temp2.getInt(9));
                Temp.updateInt(11, Temp2.getInt(10));
                Temp.updateInt(12, Temp2.getInt(11));
                Temp.updateInt(13, Temp2.getInt(12));
                Temp.updateInt(14, Temp2.getInt(13));
                Temp.updateInt(15, Temp2.getInt(14));
                Temp.updateInt(16, Temp2.getInt(15));
                Temp.updateString(17, Temp2.getString(16));
                Temp.updateRow();
            }
            Temp.beforeFirst();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }

        return Temp;
    }

    public Vector<String> AddQA(Vector<String> Key, String QAText, String QAType, String PrjId,
            String OwnerId, int Dead, String EditID, String SDataInfo,
            String SDataPath, String SSymName) {
        String str;
        String s;
        boolean DSFlag;
        boolean allowadd;
        boolean allowedit;
        boolean allow;
        int own;
        int i;
        String oldpath;
        String oldqatype;
        String oldprj;
        String oldowner;
        String oldversion;
        String oldstatus;
        String oldsymname;
        String oldreason;
        String oldprec;
        String olddraft;
        String newversion;
        String oldqatext;
        CachedRowSet W;
        CachedRowSet V;
        Vector<String> Temp = new Vector<String>();
        try {
            allowedit = false;
            allow = false;
            oldqatype = "";
            oldprj = "";
            oldowner = "";
            oldversion = "";
            oldstatus = "";
            oldsymname = "";
            oldreason = "";
            newversion = "";
            oldpath = "";
            oldprec = "";
            olddraft = "";
            oldqatext = "";
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            Statement stmt2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            Statement stmt3 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            s = MakeName(SDataPath, 3);
            allowadd = GetPermission(Key, Integer.parseInt(OwnerId), 2);
            if (Dead == -1) {
                allowedit = GetPermission(Key, Integer.parseInt(EditID), 3);
                str = "select Itype,Ipid,IOwner,IVersion,IStatus,SSymName,IReason,SDataPath,Prec,Draft,MText from QAReg where Iid=" + EditID;
                ResultSet rs2 = stmt2.executeQuery(str);
                if (rs2.next()) {
                    oldqatype = rs2.getString(1);
                    oldprj = rs2.getString(2);
                    oldowner = rs2.getString(3);
                    oldversion = rs2.getString(4);
                    oldstatus = rs2.getString(5);
                    oldsymname = rs2.getString(6);
                    oldreason = rs2.getString(7);
                    oldpath = rs2.getString(8);
                    oldprec = rs2.getString(9);
                    olddraft = rs2.getString(10);
                    oldqatext = rs2.getString(11);
                }
                rs2.close();
                stmt2.close();
                if (SDataPath.trim().equals("no_change")) {
                    s = oldpath;
                } else if ((!SDataPath.trim().equals("")) && (!SDataPath.trim().equals(":"))) {
                    s = SDataPath.trim();
                }
                str = "select max(IVersion) from QAReg where IWV=" + EditID;
                stmt2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs2 = stmt2.executeQuery(str);
                if (rs2.next()) {
                    if (Integer.parseInt(rs2.getString(1)) > Integer.parseInt(oldversion)) {
                        oldversion = rs2.getString(1);
                    }
                }
                rs2.close();
                stmt2.close();
            }
            DSFlag = false;
            if (!oldversion.equals("")) {
                own = Integer.parseInt(oldowner);
            } else {
                own = Integer.parseInt(OwnerId);
            }
            if (own != 0) {
                str = "select IStatus from QAReg where Iid=" + own;
                stmt2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs2 = stmt2.executeQuery(str);
                rs2.next();
                if ((rs2.getInt(1) == 2) || (rs2.getInt(1) == 3)) {
                    DSFlag = true;
                }
                rs2.close();
                stmt2.close();
            }
            if ((!oldversion.equals("")) && (!oldstatus.equals("1"))) {
                DSFlag = true;
            }
            if (!oldversion.equals("")) {
                if (allowedit) {
                    allow = true;
                }
            } else {
                if (allowadd) {
                    allow = true;
                }
            }
            if ((!DSFlag) && (allow)) {
                //ServVar := CreateOleObject('dblib.Serv');
                if (!oldversion.equals("")) {
                    newversion = (Integer.parseInt(oldversion) + 1) + "";
                    InsQA(oldprj, Key.elementAt(0), oldqatype, oldowner, QAText, SDataInfo, s, oldsymname, newversion, oldstatus, oldreason, oldprec, EditID, olddraft);
                } else {
                    InsQA(PrjId, Key.elementAt(0), QAType, OwnerId, QAText, SDataInfo, s, SSymName, "1", "1", "1", "0", "0", " ");
                }
                Temp.add(s);
                str = "select Iid from QAReg where Ipid=" + PrjId + " and IAuthor=" + (Key.elementAt(0)) + " order by oid desc limit 1";
                ResultSet rs3 = stmt3.executeQuery(str);
                rs3.next();
                Temp.add(rs3.getString(1));
                rs3.close();
                stmt3.close();
                if (QAType.equals("8")) {
                    AUT(Integer.parseInt(Key.elementAt(0)), Integer.parseInt(Temp.elementAt(1)), "", 1);
                }
                if (!oldversion.equals("")) {
                    if (oldqatype.equals("8")) {
                        W = GetUsersByTask(Key, Integer.parseInt(EditID), 1);
                        if (!W.wasNull()) {
                            while (W.next()) {
                                EUT(W.getInt(1), 0, Integer.parseInt(Temp.elementAt(1)), "", "", 1, -1);
                            }
                        }
                        V = GetGroupsByTask(Key, Integer.parseInt(EditID), 1);
                        if (!V.wasNull()) {
                            while (V.next()) {
                                EGT(V.getInt(1), 0, Integer.parseInt(Temp.elementAt(1)), "", "", 1, -1);
                            }
                        }
                        str = "update Demonstrations SET PART=" + Temp.elementAt(1) + " where PART=" + EditID;
                        stmt3 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt3.executeUpdate(str);
                        stmt3.close();
                        str = "update Tests SET PART=" + Temp.elementAt(1) + " where PART=" + EditID;
                        stmt3 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt3.executeUpdate(str);
                        stmt3.close();
                        str = "update SoftTasks SET PART=" + Temp.elementAt(1) + " where PART=" + EditID;
                        stmt3 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt3.executeUpdate(str);
                        stmt3.close();
                        str = "update UserTests SET PART=" + Temp.elementAt(1) + " where PART=" + EditID;
                        stmt3 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt3.executeUpdate(str);
                        stmt3.close();
                    }
                    str = "update QAReg SET IOwner=" + Temp.elementAt(1) + " where IOwner=" + EditID;
                    stmt3 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    stmt3.executeUpdate(str);
                    stmt3.close();
                }
            }
            if ((DSFlag) || (!allow)) {
                Temp.clear();
                Temp.add("False");
                Temp.add("0");
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public Vector<String> LoginUGP(String UserName, String Password, String IP) {
        Vector<String> Temp = new Vector<String>();
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery("select * from USERS where CStatus=2 and SNick='" + UserName + "' and SPasswd='" + Password);

            if (rs.next()) {

                Temp.add(rs.getString(1));
                Temp.add(rs.getString(3));
                Temp.add("0");
                Temp.add("0");
                Temp.add("?????????????");
                Temp.add("0");
            }
            return Temp;

        } catch (Exception ex) {
            exClass.Except(ex);

            reinit();
            return Temp;
        }
    }

    public int AddEditUser(Vector<String> Key, int ID, String SNick, String SFName,
            String SSName, String SSurName, String SPasswd, int IPost, int IMgr, int IRights,
            int CStatus, String BornDT, String Address, String Fax, String Pager, String ICQ, String WWW,
            String Phone1, String Phone2, String Email1, String Email2) {
        return AEUser(ID, SNick, SFName, SSName, SSurName, SPasswd, IPost, IMgr, IRights, CStatus, BornDT, Address, Fax, Pager, ICQ, WWW, Phone1, Phone2, Email1, Email2);
    }

    public int AddEditProject(Vector<String> Key, int ID,
            String SName, int IAuthor, int CStatus,
            String EndDT) {
        int PrjId, ParentID, Temp = 0;
        Vector<String> C, GOST, RUP, Diag;
        Temp = AEProject(ID, SName, IAuthor, CStatus, EndDT);
        if (ID == 0) {
            PrjId = Temp;
            if (PrjId > 0) {
                C = AddQA(Key, "????????? ??????", "8", PrjId + "", "0", 0, " ", "", "", "0");
                ParentID = Integer.parseInt(C.elementAt(1));
                GOST = AddQA(Key, "???????????? ?????????? ????", "8", PrjId + "", ParentID + "", 0, " ", "", "", "0.1");
                RUP = AddQA(Key, "???????????? ?????????? RUP", "8", PrjId + "", ParentID + "", 0, " ", "", "", "0.2");
                Diag = AddQA(Key, "?????????? ????????", "8", PrjId + "", ParentID + "", 0, " ", "", "", "0.3");
            }
        }
        return Temp;
    }

    public int AddEditGroup(Vector<String> Key, int ID, String SName,
            String CStatus, int Rights, Boolean Temporary,
            String RegDate, String CloseDate) {
        return AEGroup(ID, SName, CStatus, Rights, Temporary, RegDate, CloseDate);
    }

    public int AddGU(Vector<String> Key, int Igid, int Iuid, String DT,
            int IRights) {
        return AGU(Igid, Iuid, DT, IRights);
    }

    public CachedRowSet GetList(Vector<String> Key, String Table,
            Boolean ShowExcluded) {
        String str;
        if (ShowExcluded == false) {
            str = "select * from " + Table + " where CStatus=1";
        } else {
            str = "select * from " + Table + " where CStatus=1 or CStatus=0";
        }
        return RetValue(Key, str);
    }

    public Vector<String> GetDataByID(int ID, String Table) {
        String str;
        if (Table.toLowerCase().equals("users")) {
            str = "select * from " + Table + " where ITnum='" + ID + "'";
        } else {
            str = "select * from " + Table + " where Iid='" + ID + "'";
        }
        return MakeResult(str, false, Table);
    }

    public CachedRowSet GetGroupsByUser(Vector<String> Key, int uid, int Scope) {
        String str;
        str = "select ID,Igid,Dappdt,Denddt,IStatus,IRights from GU where Iuid='" + uid + "'";
        if (Scope == 1) {
            str = str + " and IStatus='1'";
        }
        if (Scope == 2) {
            str = str + " and IStatus='0'";
        }
        return RetValue(Key, str);
    }

    public Vector<String> MakeResult(String q, Boolean MultiDim,
            String Table) {
        Vector<String> Temp = new Vector<String>();
        int i;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery(q);
            if (MultiDim) {
                if (!rs.wasNull()) {
                    /*if (Table=="users"){
                    //Result := VarArrayCreate([0,7,0,ADOQ.RecordCount-1], varVariant)
                    }else {
                    //Result := VarArrayCreate([0,2,0,ADOQ.RecordCount-1], varVariant);
                    }*/
                    while (rs.next()) {
                        Temp.add(rs.getString(1));

                        if (Table.equals("PGU")) {
                            if (rs.getString(2).equals("")) {
                                Temp.add("");
                            } else {
                                Temp.add(rs.getString(2));
                            }
                            Temp.add(rs.getString(3));
                        }
                        if (Table.equals("USERS")) {
                            if (rs.getString(3).equals("")) {
                                Temp.add("");
                            } else {
                                Temp.add(rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5));
                            }
                            Temp.add(rs.getString(10));
                            Temp.add(rs.getString(2));
                            Temp.add(rs.getString(8));
                            Temp.add(rs.getString(11));
                            Temp.add(rs.getString(12));
                            Temp.add(rs.getString(9));
                        }
                        if (Table.equals("Projects")) {
                            if (rs.getString(2).equals("")) {
                                Temp.add("");
                            } else {
                                Temp.add(rs.getString(2));
                            }
                            Temp.add(rs.getString(5));
                        }
                        if (Table.equals("Groups")) {
                            if (rs.getString(2).equals("")) {
                                Temp.add("");
                            } else {
                                Temp.add(rs.getString(2));
                            }
                            Temp.add(rs.getString(4));
                        }
                    }
                }
            } else {
                if (!rs.wasNull()) {
                    rs.next();
                    if (Table.toUpperCase().equals("USERS")) {
                        Temp.add(rs.getString(1));
                        Temp.add(rs.getString(2));
                        Temp.add(rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5));
                        Temp.add(rs.getString(8));
                        Temp.add(rs.getString(9));
                        Temp.add(rs.getString(10));
                        Temp.add(rs.getString(11));
                        Temp.add(rs.getString(12));
                    } else {
                        for (i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            if (rs.getString(i).equals("")) {
                                Temp.add("");
                            } else {
                                Temp.add(rs.getString(i));
                            }
                        }
                    }
                }
            }
            exClass.PrintMessage(q);
            exClass.PrintMessage(Temp);
            return Temp;
        } catch (Exception ex) {
            exClass.Except(ex);

            reinit();
            return Temp;
        }
    }

    public void ChangeAdmin(String UNAME, String PASS) {
        CA(UNAME, PASS);
    }

    public String MakeName(String Path, int Tab) {
        String tbl, tid;
        String Temp;
        tbl = "";
        tid = "Iid";
        switch (Tab) {
            case 0: {
                tbl = "USERS";
                tid = "ITnum";
                break;
            }
            case 1: {
                tbl = "Projects";
                break;
            }
            case 2: {
                tbl = "Groups";
                break;
            }
            case 3: {
                tbl = "QAReg";
                break;
            }
        }
        Temp = GenerateID(tbl, tid) + "";
        switch (Tab) {
            case 0: {
                Temp = Temp + "U";
                break;
            }
            case 1: {
                Temp = Temp + "P";
                break;
            }
            case 2: {
                Temp = Temp + "G";
                break;
            }
            case 3: {
                Temp = Temp + "Q";
                break;
            }
            case 4: {
                Temp = Temp + "S";
                break;
            }
        }
        char str[] = null;
        Temp = Temp + "_" + Path.substring(Path.lastIndexOf("\\") + 1);
        if (Path.equals("")) {
            Temp = " ";
        }
        if (Path.equals(" ")) {
            Temp = " ";
        }
        return Temp;
    }

    public void StartUpload(String FileName) {
        File file = (new File("config.xml")).getAbsoluteFile().getParentFile();
        String path = file.getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += "INFO" + File.separator;
        file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        path += FileName;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(path));
        } catch (IOException ex) {
            exClass.Except(ex);
        }
    }

    public void FinishLoad() {
        try {
            if (bos != null) {
                bos.close();
                bos = null;
            }
        } catch (IOException ex) {
            exClass.Except(ex);
        }
    }

    public void Upload(Vector<String> Data) {
        try {
            for (int i = 0; i < Data.size(); i++) {
                bos.write((Data.elementAt(i) + "\n").getBytes(), 0, (Data.elementAt(i) + "\n").length());
            }
        } catch (IOException ex) {
            exClass.Except(ex);
        }
    }

    public void StartDownload(String FileName) {
        File file = (new File("config.xml")).getAbsoluteFile().getParentFile();
        String path = file.getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        path += "INFO" + File.separator + FileName;
        if (!file.exists()) {
            return;
        }
        try {
            bis = new BufferedInputStream(new FileInputStream(path));
        } catch (IOException ex) {
            exClass.Except(ex);
        }
    }

    public Vector<String> Download() {
        if (bis == null) {
            return new Vector<String>();
        }
        Vector<String> Temp = new Vector<String>();
        int ch = 0;
        String buf = "";
        try {
            while ((ch = bis.read()) > -1) {
                buf += (char) ch;
            }
            bis.close();
            String[] tt = buf.split("\n");
            for (int i = 0; i < tt.length; i++) {
                Temp.add(tt[i]);
            }
        } catch (Exception ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public String MakeSymName(int OwnerId, String QAType, int ProjectID) {
        String str;
        int inccou;
        int i;
        String Temp = null;
        try {
            inccou = 1;
            Temp = "";
            str = "select SSymName from QAReg where Iid=" + OwnerId + "";
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                Temp = Temp + rs.getString(1);
            }
            rs.close();
            stmt.close();
            str = "select SSymName from QAReg where IOwner=" + OwnerId + "";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                while (rs.next()) {
                    str = rs.getString(1);
                    if (str.indexOf(".") > -1) {
                        char s[] = new char[2];
                        str.getChars(str.length() - 2, str.length(), s, 0);
                        if (s.toString().equals(".0")) {
                            inccou = 0;
                        }
                    } else {
                        if (str.charAt(0) == '0') {
                            inccou = 0;
                        }
                    }
                }
            }
            rs.close();
            stmt.close();
            str = "select Iid from QAReg where IWV=0 and IOwner='" + OwnerId + "' and Itype='" + QAType + "' and Ipid='" + ProjectID + "'";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = NormalizeRowSet(stmt.executeQuery(str));
            if (Temp.length() > 0) {
                Temp = Temp + ".";
            }
            int rsnum = 0;
            while (rs.next()) {
                rsnum++;
            }
            Temp = Temp + (rsnum + inccou) + "";
            rs.close();
            stmt.close();

        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public Vector<String> GetQAType(Boolean How, String What) {
        int i;
        String s;
        Vector<String> Temp = new Vector<String>();
        try {
            if (How) {
                s = "select * from QATypes where ID=" + What;
            } else {
                s = "select * from QATypes where Name='" + What + "'";
            }
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery(s);
            if (rs.next()) {
                for (i = 0; i < 3; i++) {
                    Temp.add(rs.getString(i + 1));
                }
            }
            rs.close();
            stmt.close();

        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public int ChangeQAStatus(Vector<String> Key, int ID, int NewStatus, int Reason) {
        boolean allowflag;
        int Temp = 0;
        allowflag = GetPermission(Key, ID, 4);
        if (allowflag) {
            Temp = CQAS(ID, NewStatus, Reason);
        }
        return Temp;
    }

    public CachedRowSet GetData(String Table) {
        String str;
        str = "select * from " + Table;
        return RetValue(str);
    }

    public CachedRowSet GetAllVersionsByID(Vector<String> Key, int ID) {
        String str = null;
        int i;
        int j;
        int iwv;
        int orig;
        int ident;
        boolean flag;
        Vector<String> IDList = new Vector<String>();
        try {
            str = "select IWV from QAReg where Iid=" + ID + "";
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            rs.next();
            iwv = rs.getInt(1);
            rs.close();
            stmt.close();
            if (iwv != 0) {
                flag = false;
                ident = ID;
                do {
                    str = "select IWV from QAReg where Iid='" + ident + "'";
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    rs = NormalizeRowSet(stmt.executeQuery(str));
                    rs.next();
                    orig = rs.getInt(1);
                    rs.close();
                    stmt.close();
                    if (orig == 0) {
                        flag = true;
                        IDList.add(ident + "");
                    } else {
                        ident = orig;
                        IDList.add(ident + "");
                    }
                } while (flag);
                str = "select a.MText,a.Iid,a.IAuthor,a.Itype,a.DcreateDT,a.SDataInfo,a.SDataPath,a.SSymName,a.IVersion,a.IStatus,a.IReason,a.IWV,a.IOwner,a.Ipid,a.Prec,a.Draft,(select count(i.Iid) from QAReg i where i.IOwner=a.Iid) from QAReg a where ";
                str = str + "a.Iid='" + IDList.elementAt(0) + "' or a.Iid='" + ID + "'";
                for (i = 1; i < IDList.size(); i++) {
                    str = str + " or a.Iid='" + IDList.elementAt(i) + "'";
                }
            } else {
                str = "select a.MText,a.Iid,a.IAuthor,a.Itype,a.DcreateDT,a.SDataInfo,a.SDataPath,a.SSymName,a.IVersion,a.IStatus,a.IReason,a.IWV,a.IOwner,a.Ipid,a.Prec,a.Draft,(select count(i.Iid) from QAReg i where i.IOwner=a.Iid) from QAReg a where ";
                str = str + " a.Iid='" + ID + "'";
            }
            str = str + " order by a.IVersion";

        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return RetValue(Key, str);
    }

    public Boolean ChangeType(Vector<String> Key, int ID, int ZID, int OldType, int NewType) {
        String str;
        int tip;
        boolean allowflag;
        String zsym;
        int p, status;
        Statement stmt;
        ResultSet rs;
        boolean Temp = false;

        try {
            allowflag = GetPermission(Key, ID, 2);
            if (NewType == 1) {
                str = "select count(Iid) from QAReg where IOwner='" + ZID + "' and Itype=1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                if (rs.getInt(1) > 0) {
                    allowflag = false;
                }
                rs.close();
                stmt.close();
            }
            if (allowflag) {
                str = "select Itype,IStatus from QAReg where Iid='" + ID + "'";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                tip = rs.getInt(1);
                status = rs.getInt(2);
                rs.close();
                stmt.close();
                if ((tip == OldType) && (status != 2) && (status != 3)) {
                    str = "select SSymName from QAReg where Iid='" + ZID + "'";
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    rs = NormalizeRowSet(stmt.executeQuery(str));
                    rs.next();
                    zsym = rs.getString(1);
                    rs.close();
                    stmt.close();
                    str = "select count(Iid) from QAReg where IOwner='" + ZID + "' and Itype='" + NewType + "'";
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    rs = NormalizeRowSet(stmt.executeQuery(str));
                    rs.next();
                    p = rs.getInt(1);
                    rs.close();
                    stmt.close();
                    if (p >= 0) {
                        zsym = zsym + "." + (p + 1) + "";
                    }
                    ChangeType(Integer.parseInt(Key.elementAt(0)), ID, NewType, ZID, zsym, Key.elementAt(1));
                    Temp = true;
                } else {
                    Temp = false;
                }
            } else {
                Temp = false;
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public Boolean GetPermission(Vector<String> Key, int ID, int What) {
        String rig;
        int tid, i, j, tid2;
        boolean allowflag, findflag;
        CachedRowSet G, UT, GT, QA;
        Vector<String> GrList, TaskList;
        boolean Temp;
        try {
            allowflag = false;
            // ?????????? ????????????? ???????????? ??????
            findflag = false;
            tid2 = -1;
            tid = 0;
            QA = null;
            QA = GetQA(Key, -1, 0, "0", "a.Iid=" + ID, "", "");
            if (QA.next()) {
                tid = QA.getInt(14);
                if (QA.getInt(4) == 8) {
                    tid = 0;
                }
            } else {
                tid2 = 0;
            }

            if (tid2 != 0) {
                if (tid > 0) {
                    do {
                        QA = GetQA(Key, -1, 0, "0", "a.Iid='" + tid + "'", "", "");
                        QA.next();
                        if (QA.getInt(4) == 8) {
                            findflag = true;
                        } else {
                            tid = QA.getInt(14);
                            if (tid == 0) {
                                findflag = true;
                            }
                        }
                    } while (!findflag);
                }
                // ?????????? ?????? ????? ????????????
                GrList = new Vector<String>();
                TaskList = new Vector<String>();

                G = GetGroupsByUser(Key, Integer.parseInt(Key.elementAt(0)), 1);
                while (G.next()) {
                    GrList.add(G.getString(2));
                }
                UT = GetTasksByUser(Key, Integer.parseInt(Key.elementAt(0)), 1);
                while (UT.next()) {
                    TaskList.add(UT.getString(2));
                }
                for (i = 0; i < GrList.size(); i++) {
                    GT = GetTasksByGroup(Key, Integer.parseInt(GrList.elementAt(i)), 1);
                    Object[] t = GT.toCollection(2).toArray();
                    for (int jk = 0; jk < t.length; jk++) {
                        TaskList.add(t[jk].toString());
                    }

                }

                // ????????? ???????????? ????
                for (i = 0; i < TaskList.size(); i++) {
                    if ((Integer.parseInt(TaskList.elementAt(i)) == tid) || (Integer.parseInt(TaskList.elementAt(i)) == ID)) {
                        allowflag = true;
                    }
                }
                rig = Key.elementAt(5);
                if (rig.charAt(What - 1) == '0') {
                    allowflag = false;
                }
                Temp = allowflag;

            } else {
                Temp = true;
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
            Temp = false;
        }
        return Temp;
    }

    public void UpdateDraft(Vector<String> Key, String Draft, int QAID) {
        String str;
        boolean DSFlag;

        DSFlag = false;
        str = "select IStatus from QAReg where Iid='" + QAID + "'";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            rs.next();
            if (rs.getInt(1) != 1) {
                DSFlag = true;
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        if (!DSFlag) {
            UDraft(Draft, QAID);
        }
    }

    public void UpdateGU(Vector<String> Key, int EditID, int Igid, int Iuid, String DT, String EDT, int IStatus, int IRights) {
        UGU(EditID, Igid, Iuid, DT, EDT, IStatus, IRights);
    }

    public int AddGT(Vector<String> Key, int Igid, int Itid, String DT, int IRights) {
        return AGT(Igid, Itid, DT, IRights);
    }

    public int AddUT(Vector<String> Key, int Iuid, int Itid, String DT, int IRights) {
        return AUT(Iuid, Itid, DT, IRights);
    }

    public void UpdateGT(Vector<String> Key, int EditID, int Igid, int Itid, String DT, String EDT, int IStatus, int IRights) {
        EGT(EditID, Igid, Itid, DT, EDT, IStatus, IRights);
    }

    public void UpdateUT(Vector<String> Key, int EditID, int Iuid, int Itid, String DT, String EDT, int IStatus, int IRights) {
        EUT(EditID, Iuid, Itid, DT, EDT, IStatus, IRights);
    }

    public CachedRowSet GetUsersByGroup(Vector<String> Key, int Igid, int Scope) {
        String str;

        str = "select ID,Iuid,Dappdt,Denddt,IStatus,IRights from GU where Igid='" + Igid + "'";
        if (Scope == 1) {
            str = str + " and IStatus=1";
        }
        if (Scope == 2) {
            str = str + " and IStatus=0";
        }
        return RetValue(Key, str);
    }

    public CachedRowSet GetTasksByUser(Vector<String> Key, int Iuid, int Scope) {
        String str;

        str = "select a.ID,a.Itid,a.Dappdt,a.Denddt,a.IStatus,a.IRights,b.Ipid from UT a, QAReg b where a.Itid=b.Iid and a.Iuid='" + Iuid + "'";
        if (Scope == 1) {
            str = str + " and a.IStatus='1'";
        }
        if (Scope == 2) {
            str = str + " and a.IStatus='0'";
        }
        return RetValue(Key, str);
    }

    public CachedRowSet GetUsersByTask(Vector<String> Key, int Itid, int Scope) {
        String str;

        str = "select ID,Iuid,Dappdt,Denddt,IStatus,IRights from UT where Itid='" + Itid + "'";
        if (Scope == 1) {
            str = str + " and IStatus=1";
        }
        if (Scope == 2) {
            str = str + " and IStatus=0";
        }
        return RetValue(Key, str);
    }

    public CachedRowSet GetTasksByGroup(Vector<String> Key, int Igid, int Scope) {
        String str;

        str = "select a.ID,a.Itid,a.Dappdt,a.Denddt,a.IStatus,a.IRights,b.Ipid from GT a, QAReg b where a.Itid=b.Iid and a.Igid='" + Igid + "'";
        if (Scope == 1) {
            str = str + " and a.IStatus=1";
        }
        if (Scope == 2) {
            str = str + " and a.IStatus=0";
        }
        return RetValue(Key, str);
    }

    public CachedRowSet GetGroupsByTask(Vector<String> Key, int Itid, int Scope) {
        String str;

        str = "select ID,Igid,Dappdt,Denddt,IStatus,IRights from GT where Itid='" + Itid + "'";
        if (Scope == 1) {
            str = str + " and IStatus=1";
        }
        if (Scope == 2) {
            str = str + " and IStatus=0";
        }
        return RetValue(Key, str);
    }

    public CachedRowSet RetValue(Vector<String> Key, String qstr) {
        try {
            CachedRowSet oCachedRowSet = new CachedRowSetImpl();
            exClass.PrintMessage(qstr);
            oCachedRowSet.setCommand(qstr);
            oCachedRowSet.execute(connection);
            exClass.PrintMessage(oCachedRowSet);
            return NormalizeRowSet(oCachedRowSet);
//                rs.close();

        } catch (Exception ex) {
            exClass.Except(ex);
            reinit();
            return null;
        }
    }

    public Vector<String> GetRecordByID(Vector<String> Key, int ID, String Table) {
        String str;
        Vector<String> Temp = new Vector<String>();

        str = "select * from " + Table + " where ";
        if (Table.toLowerCase().equals("users")) {
            str = str + "ITnum";
        } else if ((Table.toLowerCase().equals("qareg")) || (Table.toLowerCase().equals("projects")) || (Table.toLowerCase().equals("groups"))) {
            str = str + "Iid";
        } else {
            str = str + "ID";
        }
        str = str + "=" + ID + "";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    Temp.add(rs.getString(i));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void SetOrgIniParam(String Name, String DName, String Address) {
        //Ini := TIniFile.Create(ExtractFilePath(Application.ExeName)+'server.ini');
        //Ini.WriteString('Organization','Name',Name);
        //Ini.WriteString('Organization','DomainName',DName);
        //Ini.WriteString('Organization','Address',Address);
        //Ini.Free;
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector<String> GetOrgIniParam() {
        File file = new File("server.ini");
        Vector<String> res = new Vector<String>();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"windows-1251"));
            String line = reader.readLine();
            while (line != null)
            {
                res.add(line);
                line = reader.readLine();
            }
            reader.close();
        }
        catch (IOException x)
        {
            exClass.Except(x);
        }
        finally
        {
            return res;
        }
    }

    public int AddEditFuncLinks(Vector<String> Key, int ID, int FirstGroup, int FirstUser, int SecondGroup, int SecondUser) {
        return AEFuncLinks(ID, FirstGroup, FirstUser, SecondGroup, SecondUser);
    }

    public CachedRowSet GetFuncLinks(Vector<String> Key, int ID, int FirstGroup, int FirstUser, int SecondGroup, int SecondUser) {
        String str;
        int i, j;

        str = "select * from FuncLinks";
        if ((ID > 0) || (FirstGroup > 0) || (FirstUser > 0) || (SecondGroup > 0) || (SecondUser > 0)) {
            str = str + " where ";
        }
        if (ID > 0) {
            str = str + "ID=" + ID;
        }
        if (FirstGroup > 0) {
            if (ID > 0) {
                str = str + " and ";
            }
            str = str + "FirstGroup=" + FirstGroup;
        }
        if (FirstUser > 0) {
            if ((ID > 0) || (FirstGroup > 0)) {
                str = str + " and ";
            }
            str = str + "FirstUser=" + FirstUser;
        }
        if (SecondGroup > 0) {
            if ((ID > 0) || (FirstGroup > 0) || (FirstUser > 0)) {
                str = str + " and ";
            }
            str = str + "SecondGroup=" + SecondGroup;
        }
        if (SecondUser > 0) {
            if ((ID > 0) || (FirstGroup > 0) || (FirstUser > 0) || (SecondGroup > 0)) {
                str = str + " and ";
            }
            str = str + "SecondUser=" + SecondUser;
        }
        return RetValue(Key, str);
    }

    public Vector<String> GetTreeElementById(Vector<String> Key, int id) {
        String str;
        Vector<String> Temp = new Vector<String>();
        int i;

        str = "select id,parent,user_id,group_id,node from TreeNode where id='" + id + "'";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                for (i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    Temp.add(rs.getString(i));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetTreeElementList(Vector<String> Key) {
        String str;

        str = "select id,parent,user_id,group_id,node from TreeNode";
        return RetValue(Key, str);
    }

    public void AddTreeElement(Vector<String> Key, int parent, int user_id, int group_id, int node) {
        AddTreeElement(parent, user_id, group_id, node);
    }

    public void EditTreeElement(Vector<String> Key, int id, int parent, int user_id, int group_id, int node) {
        EditTreeElement(id, parent, user_id, group_id, node);
    }

    public void DeleteTreeElement(Vector<String> Key, int id) {
        DeleteTreeElement(id);
    }

    public void DelFuncLink(Vector<String> Key, int ID) {
        DFL(ID);
    }

    public int AddEditTests(Vector<String> Key, int ID, int DISC, int PART, String NAME, int STATUS) {
        return AETests(ID, DISC, PART, NAME, STATUS);
    }

    public CachedRowSet GetTests(Vector<String> Key, int ID, int DISC, int PART, int STATUS) {
        return GetTests(ID, DISC, PART, STATUS);
    }

    public int AddEditQuestions(Vector<String> Key, int ID, int TID, String TXT, String PICPATH, int QTYPE, float COMPL, Boolean COUNTPART, int STATUS) {
        return AEQuestions(ID, TID, TXT, PICPATH, QTYPE, COMPL, COUNTPART, STATUS);
    }

    public CachedRowSet GetQuestions(Vector<String> Key, int ID, int TID, int QTYPE, int STATUS) {
        return GetQuestions(ID, TID, QTYPE, STATUS);
    }

    public int AddEditAnsVariants(Vector<String> Key, int ID, int QID, String TXT, String PICPATH, float TRUTH, int ORDE, int STATUS) {
        return AEAnsVariants(ID, QID, TXT, PICPATH, TRUTH, ORDE, STATUS);
    }

    public CachedRowSet GetAnsVariants(Vector<String> Key, int ID, int QID, int STATUS) {
        return GetAnsVariants(ID, QID, STATUS);
    }

    public int AddEditAnsVars(Vector<String> Key, int ID, int QID, String TXT, String PICPATH, int STATUS) {
        return AEAnsVars(ID, QID, TXT, PICPATH, STATUS);
    }

    public CachedRowSet GetAnsVars(Vector<String> Key, int ID, int QID, int STATUS) {
        return GetAnsVars(ID, QID, STATUS);
    }

    public int AddEditUserTests(Vector<String> Key, int ID, int uid, int TestID, String ExamDT, int Status, int Mark, int TType, int QCount, int TIMELIMIT, int DISC, int PART, int GRID) {
        return AEUserTests(ID, uid, TestID, ExamDT, Status, Mark, TType, QCount, TIMELIMIT, DISC, PART, GRID);
    }

    public CachedRowSet GetUserTests(Vector<String> Key, int ID, int UID, int TestID, int Status, String DT1, String DT2, int Mark, int TType) {
        return GetUserTests(ID, UID, TestID, Status, DT1, DT2, Mark, TType);
    }

    public int AddEditUTResults(Vector<String> Key, int ID, int APPID, float RES, int TM) {
        return AEUTResults(ID, APPID, RES, TM);
    }

    public CachedRowSet GetUTResults(Vector<String> Key, int ID, int APPID) {
        return GetUTResults(ID, APPID);
    }

    public int AddEditUTDetails(Vector<String> Key, int ID, int UTID, int QID, float RES, int TM) {
        return AEUTDetails(ID, UTID, QID, RES, TM);
    }

    public CachedRowSet GetUTDetails(Vector<String> Key, int ID, int UTID) {
        return GetUTDetails(ID, UTID);
    }

    public int AddEditDemos(Vector<String> Key, int ID, String NAME, int DISC, int PART, String DESCR, int STATUS) {
        return AEDemos(ID, NAME, DISC, PART, DESCR, STATUS);
    }

    public CachedRowSet GetDemos(Vector<String> Key, int ID, int DISC, int PART, int STATUS) {
        return GetDemos(ID, DISC, PART, STATUS);
    }

    public int AddEditSlides(Vector<String> Key, int ID, int DID, String NAME, int ORDER, Boolean ISBASIC, String REFERENCE, String DESCR, String DESCRFNAME, int DESCRFSIZE, String DESCRFCOLOR, String DESCRFSTYLE, String DESCRFORE, String SLIDEOUTERBACK, String SLIDEINNERBACK, String SLNAMEFNAME, int SLNAMEFSIZE, String SLNAMEFCOLOR, String SLNAMEFSTYLE, String SOUNDFILE, int STATUS) {
        return AESlides(ID, DID, NAME, ORDER, ISBASIC, REFERENCE, DESCR, DESCRFNAME, DESCRFSIZE, DESCRFCOLOR, DESCRFSTYLE, DESCRFORE, SLIDEOUTERBACK, SLIDEINNERBACK, SLNAMEFNAME, SLNAMEFSIZE, SLNAMEFCOLOR, SLNAMEFSTYLE, SOUNDFILE, STATUS);
    }

    public CachedRowSet GetSlides(Vector<String> Key, int ID, int DID, int ISBASIC, int Status) {
        return GetSlides(ID, DID, ISBASIC, Status);
    }

    public int AddEditSlideBlocks(Vector<String> Key, int ID, int SID, int TIP, int LTX, int LTY, int RBX, int RBY, String PICPATH, int PICSTYLE, String PAR, int PARALIGN, int FIRSTMARGIN, String FONTNAME, int FONTSIZE, String FONTCOLOR, String FONTSTYLE, int LISTSYMBOL, String PREFIX, int MARGIN, String REFERENCE, int STATUS) {
        return AESlideBlocks(ID, SID, TIP, LTX, LTY, RBX, RBY, PICPATH, PICSTYLE, PAR, PARALIGN, FIRSTMARGIN, FONTNAME, FONTSIZE, FONTCOLOR, FONTSTYLE, LISTSYMBOL, PREFIX, MARGIN, REFERENCE, STATUS);
    }

    public CachedRowSet GetSlideBlocks(Vector<String> Key, int ID, int SID, int TIP, int Status) {
        return GetSlideBlocks(ID, SID, TIP, Status);
    }

    public int AddEditListElements(Vector<String> Key, int ID, int BID, String ELEM, int ORDE, int Status) {
        return AEListElements(ID, BID, ELEM, ORDE, Status);
    }

    public CachedRowSet GetListElements(Vector<String> Key, int ID, int BID, int STATUS) {
        return GetListElements(ID, BID, STATUS);
    }

    public int AddEditHints(Vector<String> Key, int ID, int BID, String HINT, int LTX, int LTY, int RBX, int RBY, String BORDERCOLOR, String TEXTCOLOR, String FORECOLOR, String FONTNAME, int FONTSIZE, String FONTSTYLE, String REFERENCE, int STATUS) {
        return AEHints(ID, BID, HINT, LTX, LTY, RBX, RBY, BORDERCOLOR, TEXTCOLOR, FORECOLOR, FONTNAME, FONTSIZE, FONTSTYLE, REFERENCE, STATUS);
    }

    public CachedRowSet GetHints(Vector<String> Key, int ID, int BID, int STATUS) {
        return GetHints(ID, BID, STATUS);
    }

    public int AddEditTransitions(Vector<String> Key, int ID, int BID, int SID, int LTX, int LTY, int RBX, int RBY, String BORDERCOLOR, int STATUS) {
        return AETransitions(ID, BID, SID, LTX, LTY, RBX, RBY, BORDERCOLOR, STATUS);
    }

    public CachedRowSet GetTransitions(Vector<String> Key, int ID, int BID, int STATUS) {
        return GetTransitions(ID, BID, STATUS);
    }

    public int AddEditViewSettings(Vector<String> Key, int ID, int FID, int TS, int MODE, String FLFONT, int FLFONTSIZE, String FLFONTCOLOR, String FLFONTSTYLE, String SCROUTERBACK, String SCRINNERBACK, String STAGEFONT, int STAGEFONTSIZE, String STAGEFONTCOLOR, String STAGEFONTSTYLE, String ACTFONT, int ACTFONTSIZE, String ACTFONTCOLOR, String ACTFONTSTYLE, String TEXTFONT, int TEXTFONTSIZE, String TEXTFONTCOLOR, String TEXTFONTSTYLE, int STATUS) {
        return AEViewSettings(ID, FID, TS, MODE, FLFONT, FLFONTSIZE, FLFONTCOLOR, FLFONTSTYLE, SCROUTERBACK, SCRINNERBACK, STAGEFONT, STAGEFONTSIZE, STAGEFONTCOLOR, STAGEFONTSTYLE, ACTFONT, ACTFONTSIZE, ACTFONTCOLOR, ACTFONTSTYLE, TEXTFONT, TEXTFONTSIZE, TEXTFONTCOLOR, TEXTFONTSTYLE, STATUS);
    }

    public CachedRowSet GetViewSettings(Vector<String> Key, int ID, int FID, int TS, int MODE, int STATUS) {
        return GetViewSettings(ID, FID, TS, MODE, STATUS);
    }

    public void AddManning(Vector<String> Key, String NAME, int NumMembers, int Status) {
        AddManning(NAME, NumMembers, Status);
    }

    public void DeleteManning(Vector<String> Key, int ID) {
        DeleteManning(ID);
    }

    public void EditManning(Vector<String> Key, int ID, String NAM, int NumMembers, int Status) {
        EditManning(ID, NAM, NumMembers, Status);
    }

    public Vector<String> GetManningById(Vector<String> Key, int ID) {
        String str;
        int i;
        Vector<String> Temp = new Vector<String>();

        str = "select ID,Name,CStatus,NumMembers from ManningTable where ID='" + ID + "'";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                for (i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    Temp.add(rs.getString(i));
                }
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void RestoreManning(Vector<String> Key, int ID) {
        RestoreManning(ID);
    }

    public String GetWebArt() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CachedRowSet GetTasks(Vector<String> Key, int Owner, int ProjectID, String QStr) {
        String str;
        CachedRowSet Temp, Temp2;
        int i;

        if (QStr == "") {
            str = "select a.MText,a.Iid,a.IAuthor,a.Itype,a.DcreateDT,a.SDataInfo,a.SDataPath,a.SSymName,a.IVersion,a.IStatus,a.IReason,a.IWV,a.IOwner,a.Ipid,a.Prec,a.Draft,(select count(i.Iid) from QAReg i where i.IType=8 and i.IOwner=a.Iid";
            str = str + " and i.IVersion in (select max(k.IVersion) from QAReg k where k.IOwner=i.IOwner and k.SSymName=i.SSymName and k.Itype=i.Itype)) from QAReg a where";
            str = str + " a.IVersion in (select max(b.IVersion) from QAReg b where b.IOwner=a.IOwner and b.SSymName=a.SSymName and b.Itype=a.Itype)";
            if (Owner > -1) {
                str = str + " and a.IOwner='" + Owner + "'";
            }
            if (ProjectID != 0) {
                str = str + " and a.Ipid='" + ProjectID + "'";
            }
        } else {
            str = "select a.MText,a.Iid,a.IAuthor,a.Itype,a.DcreateDT,a.SDataInfo,a.SDataPath,a.SSymName,a.IVersion,a.IStatus,a.IReason,a.IWV,a.IOwner,a.Ipid,a.Prec,a.Draft,(select count(i.Iid) from QAReg i where i.IType=8 and i.IOwner=a.Iid";
            str = str + " and i.IVersion in (select max(k.IVersion) from QAReg k where k.IOwner=i.IOwner and k.SSymName=i.SSymName and k.Itype=i.Itype)) from QAReg a where (' + QStr + ')";
            str = str + " and a.IVersion in (select max(b.IVersion) from QAReg b where b.IOwner=a.IOwner and b.SSymName=a.SSymName and b.Itype=a.Itype)";
            if (Owner > -1) {
                str = str + " and a.IOwner='" + Owner + "'";
            }
            if (ProjectID != 0) {
                str = str + " and a.Ipid='" + ProjectID + "'";
            }
        }
        str = str + " and a.Itype=8 order by a.SSymName";
        Temp = RetValue(Key, str);

        try {
            if (!Temp.isBeforeFirst()) {
                Temp.beforeFirst();
            }
            Temp2 = null;
            Temp2 = Temp.createCopy();
            while (Temp.next()) {
                Temp2.next();
                for (i = 1; i <= 17; i++) {
                    if (Temp2.getString(i) == null) {
                        Temp2.updateString(i, "");
                    }
                }
                Temp2.updateRow();
                for (i = 1; i <= 7; i++) {
                    Temp.setString(i, Temp2.getString(i).trim());
                }
                Temp.updateInt(8, Temp2.getInt(17));
                Temp.updateString(9, Temp2.getString(8).trim());
                Temp.updateInt(10, Temp2.getInt(9));
                Temp.updateInt(11, Temp2.getInt(10));
                Temp.updateInt(12, Temp2.getInt(11));
                Temp.updateInt(13, Temp2.getInt(12));
                Temp.updateInt(14, Temp2.getInt(13));
                Temp.updateInt(15, Temp2.getInt(14));
                Temp.updateInt(16, Temp2.getInt(15));
                Temp.updateString(17, Temp2.getString(16));
                Temp.updateRow();
            }
            Temp.beforeFirst();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }

        return Temp;
    }

    public CachedRowSet GetQAZ(Vector<String> Key, int Owner, int ProjectID) {
        String str;
        CachedRowSet Temp, Temp2;
        int i;

        str = "select a.MText,a.Iid,a.IAuthor,a.Itype,a.DcreateDT,a.SDataInfo,a.SDataPath,a.SSymName,a.IVersion,a.IStatus,a.IReason,a.IWV,a.IOwner,a.Ipid,a.Prec,a.Draft,(select count(i.Iid) from QAReg i where i.Itype=8 and (i.IStatus<2 or i.IStatus>4) and i.IOwner=a.Iid";
        str = str + " and i.IVersion in (select max(k.IVersion) from QAReg k where k.IOwner=i.IOwner and k.SSymName=i.SSymName and k.Itype=i.Itype)) from QAReg a where a.Itype=8 and (a.IStatus<2 or a.IStatus>4) and ";
        str = str + " a.IVersion in (select max(b.IVersion) from QAReg b where b.IOwner=a.IOwner and b.SSymName=a.SSymName and b.Itype=a.Itype)";
        if (Owner > -1) {
            str = str + " and a.IOwner='" + Owner + "'";
        }
        if (ProjectID != 0) {
            str = str + " and a.Ipid='" + ProjectID + "'";
        }
        str = str + " order by a.SSymName";
        Temp = RetValue(Key, str);

        try {
            if (!Temp.isBeforeFirst()) {
                Temp.beforeFirst();
            }
            Temp2 = null;
            Temp2 = Temp.createCopy();
            while (Temp.next()) {
                Temp2.next();
                for (i = 1; i <= 17; i++) {
                    if (Temp2.getString(i) == null) {
                        Temp2.updateString(i, "");
                    }
                }
                Temp2.updateRow();
                for (i = 1; i <= 7; i++) {
                    Temp.setString(i, Temp2.getString(i).trim());
                }
                Temp.updateInt(8, Temp2.getInt(17));
                Temp.updateString(9, Temp2.getString(8).trim());
                Temp.updateInt(10, Temp2.getInt(9));
                Temp.updateInt(11, Temp2.getInt(10));
                Temp.updateInt(12, Temp2.getInt(11));
                Temp.updateInt(13, Temp2.getInt(12));
                Temp.updateInt(14, Temp2.getInt(13));
                Temp.updateInt(15, Temp2.getInt(14));
                Temp.updateInt(16, Temp2.getInt(15));
                Temp.updateString(17, Temp2.getString(16));
                Temp.updateRow();
            }
            Temp.beforeFirst();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }

        return Temp;
    }

    public void ExcludeGU(Vector<String> Key, int GID, int UID) {
        ExcludeGU(GID, UID);
    }

    public Vector<String> ChangeQAConst(Vector<String> Key, int ID, String TableName, String FieldNew) {
        return ChangeQAConst(ID, Integer.parseInt(Key.elementAt(0)), TableName, FieldNew);
    }

    public Vector<String> ChangeQAType(Vector<String> Key, int ID, String NameNew, String ShortNew, Boolean TPNew) {
        return ChangeQAType(ID, Integer.parseInt(Key.elementAt(0)), NameNew, ShortNew, TPNew);
    }

    public Vector<String> RemoveQAConst(Vector<String> Key, int ID, String TableName) {
        return RemoveQAConst(ID, Integer.parseInt(Key.elementAt(0)), TableName);
    }

    public void RemoveQATree(Vector<String> Key, int ID) {
        RemoveQATree(ID);
    }

    public Vector<String> RemoveQA(Vector<String> Key, int ID) {
        return RemoveQA(ID, Integer.parseInt(Key.elementAt(0)));
    }

    public void RemoveProject(Vector<String> Key, int ID) {
        RemoveProject(ID);
    }

    public int AddEditSoftTasks(Vector<String> Key, int ID, String TXT, String DESCR, String REFERENCE, int DISC, int PART, int STATUS) {
        return AESoftTasks(ID, TXT, DESCR, REFERENCE, DISC, PART, STATUS);
    }

    public CachedRowSet GetSoftTasks(Vector<String> Key, int ID, int DISC, int PART, int STATUS) {
        return GetSoftTasks(ID, DISC, PART, STATUS);
    }

    public int AddEditSoftActions(Vector<String> Key, int ID, int STID, int ORDE, String PICPATH, String PICPATHE, String DESCR, int LTX, int LTY, int RBX, int RBY, int ACTTYPE, int TIMING, String REFERENCE, int STATUS) {
        return AESoftActions(ID, STID, ORDE, PICPATH, PICPATHE, DESCR, LTX, LTY, RBX, RBY, ACTTYPE, TIMING, REFERENCE, STATUS);
    }

    public CachedRowSet GetSoftActions(Vector<String> Key, int ID, int STID, int STATUS) {
        return GetSoftActions(ID, STID, STATUS);
    }

    public int AddEditSoftAddData(Vector<String> Key, int ID, int SAID, String VAL, Boolean TRUTH) {
        return AESoftAddData(ID, SAID, VAL, TRUTH);
    }

    public void DelSoftAddData(Vector<String> Key, int ID) {
        DelSoftAddData(ID);
    }

    public CachedRowSet GetSoftAddData(Vector<String> Key, int ID, int SAID) {
        return GetSoftAddData(ID, SAID);
    }

    public int AddEditSoftUserTasks(Vector<String> Key, int ID, int UID, int STID, String ExamDT, int STATUS, int MARK, int ERRORLIMIT, int TIMELIMIT, int ISACTTIME, int GRID) {
        return AESoftUserTasks(ID, UID, STID, ExamDT, STATUS, MARK, ERRORLIMIT, TIMELIMIT, ISACTTIME, GRID);
    }

    public CachedRowSet GetSoftUserTasks(Vector<String> Key, int ID, int UID, int STID, String DT1, String DT2, int STATUS, int MARK) {
        return GetSoftUserTasks(ID, UID, STID, DT1, DT2, STATUS, MARK);
    }

    public int AddEditSoftUserResults(Vector<String> Key, int ID, int APPID, int RES, int TM) {
        return AESoftUserResults(ID, APPID, RES, TM);
    }

    public CachedRowSet GetSoftUserResults(Vector<String> Key, int APPID) {
        return GetSoftUserResults(APPID);
    }

    public void AddEditSoftUserActions(Vector<String> Key, int ID, int USRID, int SAID, int TM, int RES) {
        AESoftUserActions(ID, USRID, SAID, TM, RES);
    }

    public CachedRowSet GetSoftUserActions(Vector<String> Key, int USRID) {
        return GetSoftUserActions(USRID);
    }

    public void UpdateSoftActionsOrders(Vector<String> Key, int STID, int ORDE) {
        UpdateSoftActionsOrders(STID, ORDE);
    }

    public int AddEditQATypes(Vector<String> Key, int ID, String TP, String Name, String SHName) {
        return AEQATSR(ID, 0, TP, Name, SHName);
    }

    public int AddEditQAStatuses(Vector<String> Key, int ID, String Name) {
        return AEQATSR(ID, 1, "", Name, "");
    }

    public int AddEditReasons(Vector<String> Key, int ID, String Name) {
        return AEQATSR(ID, 2, "", Name, "");
    }

    public int ImportQA(Vector<String> Key, int Ipid, int IAuthor, int Itype, int IOwner, String MText, String SDataInfo, String SDataPath, String SSymName, int IStatus, int IReason) {
        return ImportQA(Ipid, IAuthor, Itype, IOwner, MText, SDataInfo, SDataPath, SSymName, IStatus, IReason);
    }

    public int ImportProject(Vector<String> Key, Boolean NeedRename, String SName, int IAuthor) {
        return ImportProject(NeedRename, SName, IAuthor);
    }

    public void UpdateQAFileName(Vector<String> Key, int ID, String nm) {
        UpdateQAFileName(ID, nm);
    }

    public void RemoveUser(Vector<String> Key, int ID) {
        RemoveUser(ID);
    }

    public int GenerateID(String Table, String IDName) {
        String str;
        int Temp = 0;

        str = "select max(" + IDName + ") from " + Table;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                Temp = rs.getInt(1) + 1;
            } else {
                Temp = 1;
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void onEvent(Interfaces.EventTypes n, String s1, String s2) {
        //
    }

    public int AEProject(int ID, String SName, int IAuthor,
            int CStatus, String EndDT) {
        String str;
        int Temp = 0;
        if (ID > 0) {
            str = "update Projects SET CStatus=" + CStatus;
            if (!SName.equals("")) {
                str = str + ", SName='" + SName.replaceAll("'", "''") + "'";
            }
            if (IAuthor != 0) {
                str = str + ", IAuthor=" + IAuthor;
            }
            if (!EndDT.equals("-")) {
                str = str + ", EndDT='" + EndDT + "'";
            }
            str = str + " where Iid=" + ID;
        } else {
            str = "insert into Projects(SName,IAuthor,CStatus) VALUES('";
            str = str + SName.replaceAll("'", "''") + "'," + IAuthor + "," + CStatus + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            if (ID > 0) {
                Temp = ID;
            } else {
                str = "select Iid from Projects order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public int AEUser(int ID, String SNick, String SFName, String SSName,
            String SSurName, String SPasswd, int IPost, int IMgr, int IRights,
            int CStatus, String BornDT, String Address, String Fax,
            String Pager, String ICQ, String WWW, String Phone1, String Phone2,
            String Email1, String Email2) {
        String str;
        String DT;
        int Temp = 0;

        if (BornDT.trim().equals("")) {
            DT = "01.01.1970";
        } else {
            DT = BornDT;
        }
        if (ID > 0) {
            str = "update USERS SET IPost=" + IPost;
            if (!SNick.equals("")) {
                str = str + ", SNick='" + SNick + "'";
            }
            if (!SFName.equals("")) {
                str = str + ", SFName='" + SFName + "'";
            }
            if (!SSName.equals("")) {
                str = str + ", SSName='" + SSName + "'";
            }
            if (!SSurName.equals("")) {
                str = str + ", SSurName='" + SSurName + "'";
            }
            if (!SPasswd.equals("-")) {
                str = str + ", SPasswd='" + SPasswd + "'";
            }
            if (IMgr > -1) {
                str = str + ", IMgr=" + IMgr;
            }
            if (IRights > -1) {
                str = str + ", IRights=" + IRights;
            }
            if (CStatus > -1) {
                str = str + ", CStatus=" + CStatus;
            }
            if (!BornDT.equals("-")) {
                str = str + ", BornDT='" + DT + "'";
            }
            if (!Address.equals("-")) {
                str = str + ", Address='" + Address + "'";
            }
            if (!Fax.equals("-")) {
                str = str + ", Fax='" + Fax + "'";
            }
            if (!Pager.equals("-")) {
                str = str + ", Pager='" + Pager + "'";
            }
            if (!ICQ.equals("-")) {
                str = str + ", ICQ='" + ICQ + "'";
            }
            if (!WWW.equals("-")) {
                str = str + ", WWW='" + WWW + "'";
            }
            if (!Phone1.equals("-")) {
                str = str + ", Phone1='" + Phone1 + "'";
            }
            if (!Phone2.equals("-")) {
                str = str + ", Phone2='" + Phone2 + "'";
            }
            if (!Email1.equals("-")) {
                str = str + ", Email1='" + Email1 + "'";
            }
            if (!Email2.equals("-")) {
                str = str + ", Email2='" + Email2 + "'";
            }
            str = str + " where ITnum=" + ID;
        } else {
            str = "insert into USERS(SNick,SFName,SSName,SSurName,SPasswd,IPost,IMgr,IRights,CStatus,BornDT,Address,Fax,Pager,ICQ,WWW,Phone1,Phone2,Email1,Email2) VALUES('" + SNick + "','" + SFName + "','" + SSName + "','" + SSurName + "','" + SPasswd + "'," + IPost + "," + IMgr + "," + IRights + "," + CStatus + ",'" + DT + "','" + Address.replaceAll("'", "''") + "','" + Fax + "','" + Pager + "','" + ICQ + "','" + WWW + "','" + Phone1 + "','" + Phone2 + "','" + Email1 + "','" + Email2 + "') Returning itnum";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            if (ID <= 0) {
                ResultSet set = NormalizeRowSet(stmt.executeQuery(str));
                set.next();
                Temp = set.getInt("itnum");
                /*str = "select ITnum from USERS order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();*/
            } else {
                stmt.executeUpdate(str);
                Temp = ID;
            }
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public int AEGroup(int ID, String SName, String CStatus,
            int Rights, boolean Temporary, String RegDate, String CloseDate) {
        String str, tmp;
        int Temp = 0;

        if (Temporary) {
            tmp = "True";
        } else {
            tmp = "False";
        }
        if (ID > 0) {
            str = "update Groups SET Temporary=" + tmp;
            if (!SName.equals("")) {
                str = str + ", SName='" + SName.replaceAll("'", "''") + "'";
            }
            if (Integer.parseInt(CStatus) > -1) {
                str = str + ", CStatus=" + CStatus;
            }
            if (Rights > -1) {
                str = str + ", Rights=" + Rights;
            }
            if (!RegDate.equals("")) {
                str = str + ", RegDate='" + RegDate + "'";
            }
            if (!CloseDate.equals("-")) {
                str = str + ", CloseDate='" + CloseDate + "'";
            }
            str = str + " where Iid=" + ID;
        } else {
            str = "insert into Groups(SName,CStatus,Rights,Temporary) VALUES('" + SName.replaceAll("'", "''") + "',1," + Rights + "," + tmp + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            if (ID > 0) {
                Temp = ID;
            } else {
                str = "select Iid from Groups order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public int AGU(int Igid, int Iuid, String DT, int IRights) {
        String str;
        int Temp = 0;

        if (DT.trim().equals("")) {
            str = "insert into GU(Igid,Iuid,IStatus,IRights) VALUES(" + Igid + "," + Iuid + ",1," + IRights + ")";
        } else {
            str = "insert into GU(Igid,Iuid,Dappdt,IStatus,IRights) VALUES(" + Igid + "," + Iuid + ",'" + DT + "',1," + IRights + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "select ID from GU order by oid desc limit 1";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            rs.next();
            Temp = rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void CA(String UNAME, String PASS) {
        String str;

        str = "update USERS set SNick='" + UNAME.replaceAll("'", "''") + "', SFName='" + UNAME.replaceAll("'", "''") + "', SPasswd='" + PASS.replaceAll("'", "''") + "' where CStatus=2";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public int CQAS(int ID, int NewStatus, int Reason) {
        String str;
        String oprj, oauth = null, otype = null, opar = null, otxt = null, odinfo = null, odpath = null, osym = null, overs = null, oiwv, oprec = null, odraft = null;
        int lastid;
        int Temp;

        oprj = "";
        Temp = 0;
        str = "select * from QAReg where Iid=" + ID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                oprj = rs.getString(2);
                oauth = rs.getString(3);
                otype = rs.getString(4);
                opar = rs.getString(5);
                otxt = rs.getString(7);
                otxt = otxt.replaceAll("'", "''");
                odinfo = rs.getString(8);
                odinfo = odinfo.replaceAll("'", "''");
                odpath = rs.getString(9);
                odpath = odpath.replaceAll("'", "''");
                osym = rs.getString(10);
                overs = rs.getString(11);
                oiwv = rs.getString(14);
                oprec = rs.getString(15);
                odraft = rs.getString(16);
                odraft = odraft.replaceAll("'", "''");
            }
            rs.close();
            stmt.close();
            if (!oprj.equals("")) {
                overs = (Integer.parseInt(overs) + 1) + "";
                str = "insert into QAReg(Ipid,IAuthor,IType,IOwner,MText,SDataInfo,SDataPath,SSymName,IVersion,IStatus,IReason,IWV,Prec,Draft) VALUES(" + oprj + "," + oauth + "," + otype + "," + opar + ",'" + otxt + "','" + odinfo + "','" + odpath + "','" + osym + "'," + overs + "," + NewStatus + "," + Reason + "," + ID + "," + oprec + ",'" + odraft + "')";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.executeUpdate(str);
                stmt.close();
                str = "select Iid from QAReg where IOwner=" + opar + " and IStatus=" + NewStatus + " order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                lastid = rs.getInt(1);
                rs.close();
                stmt.close();
                Temp = lastid;
                str = "update QAReg SET IOwner=" + lastid + " where IOwner=" + ID;
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.executeUpdate(str);
                stmt.close();
                if (otype.equals("8")) {
                    str = "update UT SET Itid=" + lastid + " where Itid=" + ID;
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    stmt.executeUpdate(str);
                    stmt.close();
                    str = "update GT SET Itid=" + lastid + " where Itid=" + ID;
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    stmt.executeUpdate(str);
                    stmt.close();
                    str = "update UserTests SET TestID=" + lastid + " where TestID=" + ID;
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    stmt.executeUpdate(str);
                    stmt.close();
                }
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void InsQA(String Ipid, String IAuthor, String IType, String IOwner, String MText, String SDataInfo,
            String SDataPath, String SSymName, String IVersion, String IStatus, String IReason, String IPrec, String IWV,
            String Draft) {
        String str;

        str = "insert into QAReg(Ipid,IAuthor,IType,IOwner,MText,SDataInfo,SDataPath,SSymName,IVersion,IStatus,IReason,IWV,Prec,Draft) Values(" + Ipid + "," + IAuthor + "," + IType + "," + IOwner + ",'" + MText.replaceAll("'", "''") + "','" + SDataInfo.replaceAll("'", "''") + "','" + SDataPath.replaceAll("'", "''") + "','" + SSymName + "'," + IVersion + "," + IStatus + "," + IReason + "," + IWV + "," + IPrec + ",'" + Draft.replaceAll("'", "''") + "')";
        try {
            exClass.PrintMessage(str);
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.execute(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void ChangeType(int IAuthor, int ID, int Tp, int ZID, String ZSym,
            String UserName) {
        String str, prj, txt;
        int COPYID;

        COPYID = ID;
        str = "select * from QAReg where Iid=" + COPYID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            rs.next();
            prj = rs.getString(2);
            txt = "\n\r\n\r\n\r??? ";
            if (Tp == 1) {
                txt = txt + "????????";
            }
            if (Tp == 8) {
                txt = txt + "??????";
            }
            txt = txt + " ???? ?????????????? ?? ????????? ";
            if (Tp == 1) {
                txt = txt + "?????? A";
            }
            if (Tp == 8) {
                txt = txt + "??????? Q";
            }
            txt = txt + rs.getString(10) + " ????????????? " + UserName;
            if ((Tp != 1) && (Tp != 8)) {
                txt = "";
            }
            str = "insert into QAReg(Ipid,IAuthor,IType,IOwner,MText,SDataInfo,SDataPath,SSymName,IVersion,IStatus,IReason,IWV,Prec,Draft) VALUES(";
            str = str + rs.getString(2) + "," + rs.getString(3) + "," + Tp + ",";
            if (ZID >= 0) {
                str = str + ZID + "";
            } else {
                str = str + rs.getString(5);
            }
            str = str + ",'" + rs.getString(7).replaceAll("'", "''") + txt + "','" + rs.getString(8).replaceAll("'", "''") + "','" + rs.getString(9).replaceAll("'", "''") + "','";
            str = str + ZSym + "',1,1,1,0," + rs.getString(15) + ",'" + rs.getString(16).replaceAll("'", "''") + "')";
            rs.close();
            stmt.close();
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void UDraft(String TXT, int QAID) {
        String str;

        str = "update QAReg SET Draft='" + TXT.replaceAll("'", "''") + "' where Iid=" + QAID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public int AGT(int Igid, int Itid, String DT, int IRights) {
        String str;
        int Temp = 0;

        if (DT.trim().equals("")) {
            str = "insert into GT(Igid,Itid,IStatus,IRights) VALUES(" + Igid + "," + Itid + ",1," + IRights + ")";
        } else {
            str = "insert into GT(Igid,Itid,Dappdt,IStatus,IRights) VALUES(" + Igid + "," + Itid + ",'" + DT + "',1," + IRights + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "select ID from GT order by oid desc limit 1";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            rs.next();
            Temp = rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void EGT(int EditID, int Igid, int Itid, String DT, String EDT,
            int IStatus, int IRights) {
        String str;

        str = "update GT SET IStatus=" + IStatus;
        if (Igid != 0) {
            str = str + ", Igid=" + Igid;
        }
        if (Itid != 0) {
            str = str + ", Itid=" + Itid;
        }
        if (!DT.equals("")) {
            str = str + ", Dappdt='" + DT + "'";
        }
        if (!EDT.equals("")) {
            str = str + ", Denddt='" + EDT + "'";
        }
        if (IRights >= 0) {
            str = str + ", IRights=" + IRights;
        }
        str = str + " where ID=" + EditID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public int AUT(int Iuid, int Itid, String DT, int IRights) {
        String str;
        int Temp = 0;

        if (DT.trim().equals("")) {
            str = "insert into UT(Iuid,Itid,IStatus,IRights) VALUES(" + Iuid + "," + Itid + ",'1'," + IRights + ")";
        } else {
            str = "insert into UT(Iuid,Itid,Dappdt,IStatus,IRights) VALUES(" + Iuid + "," + Itid + ",'" + DT + "','1'," + IRights + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "select ID from UT order by oid desc limit 1";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            rs.next();
            Temp = rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void EUT(int EditID, int Iuid, int Itid, String DT, String EDT,
            int IStatus, int IRights) {
        String str;

        str = "update UT SET IStatus=" + IStatus;
        if (Iuid != 0) {
            str = str + ", Iuid=" + Iuid;
        }
        if (Itid != 0) {
            str = str + ", Itid=" + Itid;
        }
        if (!DT.equals("")) {
            str = str + ", Dappdt='" + DT + "'";
        }
        if (!EDT.equals("")) {
            str = str + ", Denddt='" + EDT + "";
        }
        if (IRights >= 0) {
            str = str + ", IRights=" + IRights;
        }
        str = str + " where ID=" + EditID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void UGU(int EditID, int Igid, int Iuid, String DT, String EDT,
            int IStatus, int IRights) {
        String str;

        str = "update GU SET IStatus=" + IStatus;
        if (Igid != 0) {
            str = str + ", Igid=" + Igid;
        }
        if (Iuid != 0) {
            str = str + ", Iuid=" + Iuid;
        }
        if (!DT.equals("")) {
            str = str + ", Dappdt='" + DT + "'";
        }
        if (!EDT.equals("")) {
            str = str + ", Denddt='" + EDT + "'";
        }
        if (IRights >= 0) {
            str = str + ", IRights=" + IRights;
        }
        str = str + " where ID=" + EditID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public int AEFuncLinks(int ID, int FG, int FU, int SG, int SU) {
        String str;
        int Temp = 0;

        if (ID > 0) {
            str = "update FuncLinks SET FirstGroup=" + FG + ", FirstUser=" + FU + ", SecondGroup=" + SG + ", SecondUser=" + SU + " where ID=" + ID;
        } else {
            str = "insert into FuncLinks(FirstGroup,FirstUser,SecondGroup,SecondUser) VALUES(" + FG + "," + FU + "," + SG + "," + SU + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = 0;
            if (ID <= 0) {
                str = "select ID from FuncLinks order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void AddTreeElement(int parent, int user_id, int group_id, int node) {
        String str;

        str = "insert into TreeNode(parent,user_id,group_id,node) VALUES(" + parent + "," + user_id + "," + group_id + "," + node + ")";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void EditTreeElement(int id, int parent, int user_id, int group_id,
            int node) {
        String str;

        str = "update TreeNode set parent=" + parent + ", user_id=" + user_id + ", group_id=" + group_id + ", node=" + node + " where id=" + id;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void DeleteTreeElement(int id) {
        String str;

        str = "delete from TreeNode where id=" + id;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void DFL(int ID) {
        String str;

        str = "delete from FuncLinks where ID=" + ID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public int AETests(int ID, int DISC, int PART, String NAME, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((DISC > 0) || (PART > -1) || (STATUS > -1) || (!NAME.equals(""))) {
                str = "update Tests SET ";
                if (DISC > 0) {
                    str = str + "DISC=" + DISC;
                }
                if (PART > -1) {
                    if (DISC > 0) {
                        str = str + ", ";
                    }
                    str = str + "PART=" + PART;
                }
                if (!NAME.equals("")) {
                    if ((DISC > 0) || (PART > -1)) {
                        str = str + ", ";
                    }
                    str = str + "NAME='" + NAME.replaceAll("'", "''") + "'";
                }
                if (STATUS > -1) {
                    if ((DISC > 0) || (PART > -1) || (!NAME.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into Tests(DISC,PART,NAME,STATUS) VALUES(";
            str = str + DISC + "," + PART + ",'" + NAME.replaceAll("'", "''") + "',1)";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from Tests order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetTests(int ID, int DISC, int PART, int STATUS) {
        String str;

        str = "select * from Tests";
        if ((ID > 0) || (DISC > 0) || (PART > -1) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (DISC > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "DISC=" + DISC;
            }
            if (PART > -1) {
                if ((ID > 0) || (DISC > 0)) {
                    str = str + " and ";
                }
                str = str + "PART=" + PART;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (DISC > 0) || (PART > -1)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AEQuestions(int ID, int TID, String TXT, String PICPATH,
            int QTYPE, float COMPL, boolean COUNTPART, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((TID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change")) || (QTYPE > -1) || (COMPL > 0) || (STATUS > -1)) {
                str = "update Questions SET ";
                if (TID > 0) {
                    str = str + "TID=" + TID;
                }
                if (!TXT.equals("")) {
                    if (TID > 0) {
                        str = str + ", ";
                    }
                    str = str + "TXT='" + TXT.replaceAll("'", "''") + "'";
                }
                if (!PICPATH.equals("no_change")) {
                    if ((TID > 0) || (!TXT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "PICPATH='" + PICPATH + "'";
                }
                if (QTYPE > -1) {
                    if ((TID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change"))) {
                        str = str + ", ";
                    }
                    str = str + "QTYPE=" + QTYPE;
                }
                if (COMPL > 0) {
                    if ((TID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change")) || (QTYPE > -1)) {
                        str = str + ", ";
                    }
                    DecimalFormat df = new DecimalFormat("0.00");
                    str = str + "COMPL=" + df.format(COMPL);
                }
                if (STATUS > -1) {
                    if ((TID > 0) || (!TXT.equals("")) || (!PICPATH.equals("")) || (QTYPE > -1) || (COMPL > 0)) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into Questions(TID,TXT,PICPATH,QTYPE,COMPL,COUNTPART,STATUS) VALUES(";
            DecimalFormat df = new DecimalFormat("0.00");
            str = str + TID + ",'" + TXT.replaceAll("'", "''") + "','" + PICPATH + "'," + QTYPE + "," + df.format(COMPL) + ",";
            if (COUNTPART) {
                str = str + "True,1)";
            } else {
                str = str + "False,1)";
            }
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from Questions order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetQuestions(int ID, int TID, int QTYPE, int STATUS) {
        String str;

        str = "select * from Questions";
        if ((ID > 0) || (TID > 0) || (QTYPE > -1) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (TID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "TID=" + TID;
            }
            if (QTYPE > -1) {
                if ((ID > 0) || (TID > 0)) {
                    str = str + " and ";
                }
                str = str + "QTYPE=" + QTYPE;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (TID > 0) || (QTYPE > -1)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AEAnsVariants(int ID, int QID, String TXT, String PICPATH,
            float TRUTH, int ORDE, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((QID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change")) || (TRUTH >= 0) || (ORDE > -1) || (STATUS > -1)) {
                str = "update AnsVariants SET ";
                if (QID > 0) {
                    str = str + "QID=" + QID;
                }
                if (!TXT.equals("")) {
                    if (QID > 0) {
                        str = str + ", ";
                    }
                    str = str + "TXT='" + TXT.replaceAll("'", "''") + "'";
                }
                if (!PICPATH.equals("no_change")) {
                    if ((QID > 0) || (!TXT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "PICPATH='" + PICPATH + "'";
                }
                if (TRUTH >= 0) {
                    if ((QID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change"))) {
                        str = str + ", ";
                    }
                    DecimalFormat df = new DecimalFormat("0.000");
                    str = str + "TRUTH=" + df.format(TRUTH);
                }
                if (ORDE > -1) {
                    if ((QID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change")) || (TRUTH >= 0)) {
                        str = str + ", ";
                    }
                    str = str + "ORDE=" + ORDE;
                }
                if (STATUS > -1) {
                    if ((QID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change")) || (TRUTH >= 0) || (ORDE > -1)) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into AnsVariants(QID,TXT,PICPATH,TRUTH,ORDE,STATUS) VALUES(";
            DecimalFormat df = new DecimalFormat("0.000");
            str = str + QID + ",'" + TXT.replaceAll("'", "''") + "','" + PICPATH + "'," + df.format(TRUTH) + "," + ORDE + ",1)";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from AnsVariants order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetAnsVariants(int ID, int QID, int STATUS) {
        String str;

        str = "select * from AnsVariants";
        if ((ID > 0) || (QID > 0) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (QID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "QID=" + QID;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (QID > 0)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AEAnsVars(int ID, int QID, String TXT, String PICPATH, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((QID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change")) || (STATUS > -1)) {
                str = "update AnsVars SET ";
                if (QID > 0) {
                    str = str + "QID=" + QID;
                }
                if (!TXT.equals("")) {
                    if (QID > 0) {
                        str = str + ", ";
                    }
                    str = str + "TXT='" + TXT.replaceAll("'", "''") + "'";
                }
                if (!PICPATH.equals("no_change")) {
                    if ((QID > 0) || (!TXT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "PICPATH='" + PICPATH + "'";
                }
                if (STATUS > -1) {
                    if ((QID > 0) || (!TXT.equals("")) || (!PICPATH.equals("no_change"))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into AnsVars(QID,TXT,PICPATH,STATUS) VALUES(";
            str = str + QID + ",'" + TXT.replaceAll("'", "''") + "','" + PICPATH + "',1)";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from AnsVars order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetAnsVars(int ID, int QID, int STATUS) {
        String str;

        str = "select * from AnsVars";
        if ((ID > 0) || (QID > 0) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (QID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "QID=" + QID;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (QID > 0)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AEUserTests(int ID, int UID, int TestID, String ExamDT,
            int Status, int Mark, int TType, int QCount, int TIMELIMIT,
            int DISC, int PART, int GRID) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((UID > 0) || (TestID > 0) || (!ExamDT.equals("")) || (Status > -1) || (Mark > -1) || (TType > -1) || (QCount > 0) || (TIMELIMIT > -1) || (DISC > -1) || (PART > -1) || (GRID > 0)) {
                str = "update UserTests SET ";
                if (UID > 0) {
                    str = str + "UID=" + UID;
                }
                if (TestID > 0) {
                    if (UID > 0) {
                        str = str + ", ";
                    }
                    str = str + "TestID=" + TestID;
                }
                if (!ExamDT.equals("")) {
                    if ((UID > 0) || (TestID > 0)) {
                        str = str + ", ";
                    }
                    str = str + "ExamDT='" + ExamDT + "'";
                }
                if (Status > -1) {
                    if ((UID > 0) || (TestID > 0) || (!ExamDT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "Status=" + Status;
                }
                if (Mark > -1) {
                    if ((UID > 0) || (TestID > 0) || (!ExamDT.equals("")) || (Status > -1)) {
                        str = str + ", ";
                    }
                    str = str + "Mark=" + Mark;
                }
                if (TType > -1) {
                    if ((UID > 0) || (TestID > 0) || (!ExamDT.equals("")) || (Status > -1) || (Mark > -1)) {
                        str = str + ", ";
                    }
                    str = str + "TType=" + TType;
                }
                if (QCount > 0) {
                    if ((UID > 0) || (TestID > 0) || (!ExamDT.equals("")) || (Status > -1) || (Mark > -1) || (TType > -1)) {
                        str = str + ", ";
                    }
                    str = str + "QCount=" + QCount;
                }
                if (TIMELIMIT > -1) {
                    if ((UID > 0) || (TestID > 0) || (!ExamDT.equals("")) || (Status > -1) || (Mark > -1) || (TType > -1) || (QCount > 0)) {
                        str = str + ", ";
                    }
                    str = str + "TIMELIMIT=" + TIMELIMIT;
                }
                if (DISC > -1) {
                    if ((UID > 0) || (TestID > 0) || (!ExamDT.equals("")) || (Status > -1) || (Mark > -1) || (TType > -1) || (QCount > 0) || (TIMELIMIT > -1)) {
                        str = str + ", ";
                    }
                    str = str + "DISC=" + DISC;
                }
                if (PART > -1) {
                    if ((UID > 0) || (TestID > 0) || (!ExamDT.equals("")) || (Status > -1) || (Mark > -1) || (TType > -1) || (QCount > 0) || (TIMELIMIT > -1) || (DISC > -1)) {
                        str = str + ", ";
                    }
                    str = str + "PART=" + PART;
                }
                if (GRID > 0) {
                    if ((UID > 0) || (TestID > 0) || (!ExamDT.equals("")) || (Status > -1) || (Mark > -1) || (TType > -1) || (QCount > 0) || (TIMELIMIT > -1) || (DISC > -1) || (PART > -1)) {
                        str = str + ", ";
                    }
                    str = str + "GRID=" + GRID;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into UserTests(UID,TestID,ExamDT,Status,Mark,TType,QCount,TIMELIMIT,DISC,PART,GRID) VALUES(";
            str = str + UID + "," + TestID + ",'" + ExamDT + "',1,0," + TType + "," + QCount + "," + TIMELIMIT + "," + DISC + "," + PART + "," + GRID + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from UserTests order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetUserTests(int ID, int UID, int TestID, int Status,
            String DT1, String DT2, int Mark, int TType) {
        String str;

        str = "select * from UserTests";
        if ((ID > 0) || (UID > 0) || (TestID > -1) || (Status > -1) || (Mark > -1) || (TType > -1) || (!DT1.equals("")) || (!DT2.equals(""))) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (UID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "UID=" + UID;
            }
            if (TestID > -1) {
                if ((ID > 0) || (UID > 0)) {
                    str = str + " and ";
                }
                str = str + "TestID=" + TestID;
            }
            if (Status > -1) {
                if ((ID > 0) || (UID > 0) || (TestID > -1)) {
                    str = str + " and ";
                }
                str = str + "Status=" + Status;
            }
            if (Mark > -1) {
                if ((ID > 0) || (UID > 0) || (TestID > -1) || (Status > -1)) {
                    str = str + " and ";
                }
                str = str + "Mark=" + Mark;
            }
            if (TType > -1) {
                if ((ID > 0) || (UID > 0) || (TestID > -1) || (Status > -1) || (Mark > -1)) {
                    str = str + " and ";
                }
                str = str + "TType=" + TType;
            }
            if (!DT1.equals("")) {
                if ((ID > 0) || (UID > 0) || (TestID > -1) || (Status > -1) || (Mark > -1) || (TType > -1)) {
                    str = str + " and ";
                }
                str = str + "ExamDT>='DT1'";
            }
            if (!DT2.equals("")) {
                if ((ID > 0) || (UID > 0) || (TestID > -1) || (Status > -1) || (Mark > -1) || (TType > -1) || (!DT1.equals(""))) {
                    str = str + " and ";
                }
                str = str + "ExamDT<='DT2'";
            }
            str = str + " order by ID";
        }
        return RetValue(str);
    }

    public int AEUTResults(int ID, int APPID, float RES, int TM) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((APPID > 0) || (RES >= 0) || (TM > -1)) {
                str = "update UTResults SET ";
                if (APPID > 0) {
                    str = str + "APPID=" + APPID;
                }
                if (RES >= 0) {
                    if (APPID > 0) {
                        str = str + ", ";
                    }
                    DecimalFormat df = new DecimalFormat("0.000");
                    str = str + "RES=" + df.format(RES);
                }
                if (TM > -1) {
                    if ((APPID > 0) || (RES >= 0)) {
                        str = str + ", ";
                    }
                    str = str + "TM=" + TM;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into UTResults(APPID,RES,TM) VALUES(";
            DecimalFormat df = new DecimalFormat("0.000");
            str = str + APPID + "," + df.format(RES) + "," + TM + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from UTResults order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetUTResults(int ID, int APPID) {
        String str;

        str = "select * from UTResults";
        if ((ID > 0) || (APPID > 0)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (APPID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "APPID=" + APPID;
            }
            str = str + " order by ID";
        }
        return RetValue(str);
    }

    public int AEUTDetails(int ID, int UTID, int QID, float RES, int TM) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((UTID > 0) || (QID > 0) || (RES >= 0) || (TM > -1)) {
                str = "update UTDetails SET ";
                if (UTID > 0) {
                    str = str + "UTID=" + UTID;
                }
                if (QID > 0) {
                    if (UTID > 0) {
                        str = str + ", ";
                    }
                    str = str + "QID=" + QID;
                }
                if (RES >= 0) {
                    if ((UTID > 0) || (QID > 0)) {
                        str = str + ", ";
                    }
                    DecimalFormat df = new DecimalFormat("0.000");
                    str = str + "RES=" + df.format(RES);
                }
                if (TM > -1) {
                    if ((UTID > 0) || (QID > 0) || (RES >= 0)) {
                        str = str + ", ";
                    }
                    str = str + "TM=" + TM;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into UTDetails(UTID,QID,RES,TM) VALUES(";
            DecimalFormat df = new DecimalFormat("0.000");
            str = str + UTID + "," + QID + "," + df.format(RES) + "," + TM + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from UTDetails order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetUTDetails(int ID, int UTID) {
        String str;

        str = "select a.ID,a.UTID,a.QID,a.RES,a.TM,b.TXT from UTDetails a, Questions b where a.QID=b.ID";
        if (ID > 0) {
            str = str + " and a.ID=" + ID;
        }
        if (UTID > 0) {
            str = str + " and a.UTID=" + UTID;
        }
        return RetValue(str);
    }

    public int AEDemos(int ID, String NAME, int DISC, int PART,
            String DESCR, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((!NAME.equals("")) || (DISC > 0) || (PART > -1) || (!DESCR.equals("-")) || (STATUS > -1)) {
                str = "update Demonstrations SET ";
                if (!NAME.equals("")) {
                    str = str + "NAME='" + NAME.replaceAll("'", "''") + "'";
                }
                if (DISC > 0) {
                    if (!NAME.equals("")) {
                        str = str + ", ";
                    }
                    str = str + "DISC=" + DISC;
                }
                if (PART > -1) {
                    if ((!NAME.equals("")) || (DISC > 0)) {
                        str = str + ", ";
                    }
                    str = str + "PART=" + PART;
                }
                if (!DESCR.equals("-")) {
                    if ((!NAME.equals("")) || (DISC > 0) || (PART > -1)) {
                        str = str + ", ";
                    }
                    str = str + "DESCR='" + DESCR.replaceAll("'", "''") + "'";
                }
                if (STATUS > -1) {
                    if ((!NAME.equals("")) || (DISC > 0) || (PART > -1) || (!DESCR.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into Demonstrations(NAME,DISC,PART,DESCR,STATUS) VALUES('";
            str = str + NAME.replaceAll("'", "''") + "'," + DISC + "," + PART + ",'" + DESCR.replaceAll("'", "''") + "'," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from Demonstrations order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetDemos(int ID, int DISC, int PART, int STATUS) {
        String str;

        str = "select * from Demonstrations";
        if ((ID > 0) || (DISC > 0) || (PART > -1) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (DISC > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "DISC=" + DISC;
            }
            if (PART > -1) {
                if ((ID > 0) || (DISC > 0)) {
                    str = str + " and ";
                }
                str = str + "PART=" + PART;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (DISC > 0) || (PART > -1)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AESlides(int ID, int DID, String NAME, int ORDER, boolean ISBASIC,
            String REFERENCE, String DESCR, String DESCRFNAME, int DESCRFSIZE,
            String DESCRFCOLOR, String DESCRFSTYLE, String DESCRFORE,
            String SLIDEOUTERBACK, String SLIDEINNERBACK, String SLNAMEFNAME,
            int SLNAMEFSIZE, String SLNAMEFCOLOR, String SLNAMEFSTYLE,
            String SOUNDFILE, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            str = "update Slides SET ISBASIC=";
            if (ISBASIC) {
                str = str + "True";
            } else {
                str = str + "False";
            }
            if (DID > 0) {
                str = str + ", DID=" + DID;
            }
            if (!NAME.equals("")) {
                str = str + ", NAME='" + NAME.replaceAll("'", "''") + "'";
            }
            if (ORDER > -1) {
                str = str + ", ORDE=" + ORDER;
            }
            if (!REFERENCE.equals("-")) {
                str = str + ", REFERENCE='" + REFERENCE + "'";
            }
            if (!DESCR.equals("-")) {
                str = str + ", DESCR='" + DESCR.replaceAll("'", "''") + "'";
            }
            if (!DESCRFNAME.equals("")) {
                str = str + ", DESCRFNAME='" + DESCRFNAME + "'";
            }
            if (DESCRFSIZE > 0) {
                str = str + ", DESCRFSIZE=" + DESCRFSIZE;
            }
            if (!DESCRFCOLOR.equals("")) {
                str = str + ", DESCRFCOLOR='" + DESCRFCOLOR + "'";
            }
            if (!DESCRFSTYLE.equals("-")) {
                str = str + ", DESCRFSTYLE='" + DESCRFSTYLE + "'";
            }
            if (!DESCRFORE.equals("")) {
                str = str + ", DESCRFORE='" + DESCRFORE + "'";
            }
            if (!SLIDEOUTERBACK.equals("")) {
                str = str + ", SLIDEOUTERBACK='" + SLIDEOUTERBACK + "'";
            }
            if (!SLIDEINNERBACK.equals("")) {
                str = str + ", SLIDEINNERBACK='" + SLIDEINNERBACK + "'";
            }
            if (!SLNAMEFNAME.equals("")) {
                str = str + ", SLNAMEFNAME='" + SLNAMEFNAME + "'";
            }
            if (SLNAMEFSIZE > 0) {
                str = str + ", SLNAMEFSIZE=" + SLNAMEFSIZE;
            }
            if (!SLNAMEFCOLOR.equals("")) {
                str = str + ", SLNAMEFCOLOR='" + SLNAMEFCOLOR + "'";
            }
            if (!SLNAMEFSTYLE.equals("-")) {
                str = str + ", SLNAMEFSTYLE='" + SLNAMEFSTYLE + "'";
            }
            if (!SOUNDFILE.equals("no_change")) {
                str = str + ", SOUNDFILE='" + SOUNDFILE + "'";
            }
            if (STATUS > -1) {
                str = str + ", STATUS=" + STATUS;
            }
            str = str + " where ID=" + ID;
        } else {
            str = "insert into Slides(DID,NAME,ORDE,ISBASIC,REFERENCE,DESCR,DESCRFNAME,DESCRFSIZE,DESCRFCOLOR,DESCRFSTYLE,DESCRFORE,SLIDEOUTERBACK,SLIDEINNERBACK,SLNAMEFNAME,SLNAMEFSIZE,SLNAMEFCOLOR,SLNAMEFSTYLE,SOUNDFILE,STATUS) VALUES(";
            str = str + DID + ",'" + NAME.replaceAll("'", "''") + "'," + ORDER + ",";
            if (ISBASIC) {
                str = str + "True,'";
            } else {
                str = str + "False,'";
            }
            str = str + REFERENCE + "','" + DESCR.replaceAll("'", "''") + "','" + DESCRFNAME + "'," + DESCRFSIZE + ",'" + DESCRFCOLOR + "','" + DESCRFSTYLE + "','" + DESCRFORE + "','" + SLIDEOUTERBACK + "','" + SLIDEINNERBACK + "','" + SLNAMEFNAME + "'," + SLNAMEFSIZE + ",'" + SLNAMEFCOLOR + "','" + SLNAMEFSTYLE + "','" + SOUNDFILE + "'," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from Slides order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetSlides(int ID, int DID, int ISBASIC, int STATUS) {
        String str;

        str = "select * from Slides";
        if ((ID > 0) || (DID > 0) || (ISBASIC > -1) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (DID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "DID=" + DID;
            }
            if (ISBASIC > -1) {
                if ((ID > 0) || (DID > 0)) {
                    str = str + " and ";
                }
                if (ISBASIC == 1) {
                    str = str + "ISBASIC=True";
                } else {
                    str = str + "ISBASIC=False";
                }
            }
            if (STATUS > -1) {
                if ((ID > 0) || (DID > 0) || (ISBASIC > -1)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        str = str + " order by ORDE,ID";
        return RetValue(str);
    }

    public int AESlideBlocks(int ID, int SID, int TIP, int LTX, int LTY,
            int RBX, int RBY, String PICPATH, int PICSTYLE, String PAR,
            int PARALIGN, int FIRSTMARGIN, String FONTNAME, int FONTSIZE,
            String FONTCOLOR, String FONTSTYLE, int LISTSYMBOL,
            String PREFIX, int MARGIN, String REFERENCE, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTCOLOR.equals("")) || (!FONTSTYLE.equals("-")) || (LISTSYMBOL > -1) || (!PREFIX.equals("-")) || (MARGIN > -1) || (!REFERENCE.equals("-")) || (STATUS > -1)) {
                str = "update SlideBlocks SET ";
                if (SID > 0) {
                    str = str + "SID=" + SID;
                }
                if (TIP > -1) {
                    if (SID > 0) {
                        str = str + ", ";
                    }
                    str = str + "TIP=" + TIP;
                }
                if (LTX > -1) {
                    if ((SID > 0) || (TIP > -1)) {
                        str = str + ", ";
                    }
                    str = str + "LTX=" + LTX;
                }
                if (LTY > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1)) {
                        str = str + ", ";
                    }
                    str = str + "LTY=" + LTY;
                }
                if (RBX > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RBX=" + RBX;
                }
                if (RBY > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RBY=" + RBY;
                }
                if (!PICPATH.equals("no_change")) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1)) {
                        str = str + ", ";
                    }
                    str = str + "PICPATH='" + PICPATH + "'";
                }
                if (PICSTYLE > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change"))) {
                        str = str + ", ";
                    }
                    str = str + "PICSTYLE=" + PICSTYLE;
                }
                if (!PAR.equals("")) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1)) {
                        str = str + ", ";
                    }
                    str = str + "PAR='" + PAR.replaceAll("'", "''") + "'";
                }
                if (PARALIGN > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "PARALIGN=" + PARALIGN;
                }
                if (FIRSTMARGIN > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1)) {
                        str = str + ", ";
                    }
                    str = str + "FIRSTMARGIN=" + FIRSTMARGIN;
                }
                if (!FONTNAME.equals("")) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1)) {
                        str = str + ", ";
                    }
                    str = str + "FONTNAME='" + FONTNAME + "'";
                }
                if (FONTSIZE > 0) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "FONTSIZE=" + FONTSIZE;
                }
                if (!FONTCOLOR.equals("")) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals("")) || (FONTSIZE > 0)) {
                        str = str + ", ";
                    }
                    str = str + "FONTCOLOR='" + FONTCOLOR + "'";
                }
                if (!FONTSTYLE.equals("-")) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTCOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "FONTSTYLE='" + FONTSTYLE + "'";
                }
                if (LISTSYMBOL > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTCOLOR.equals("")) || (!FONTSTYLE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "LISTSYMBOL=" + LISTSYMBOL;
                }
                if (!PREFIX.equals("-")) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTCOLOR.equals("")) || (!FONTSTYLE.equals("-")) || (LISTSYMBOL > -1)) {
                        str = str + ", ";
                    }
                    str = str + "PREFIX='" + PREFIX + "'";
                }
                if (MARGIN > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTCOLOR.equals("")) || (!FONTSTYLE.equals("-")) || (LISTSYMBOL > -1) || (!PREFIX.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "MARGIN=" + MARGIN;
                }
                if (!REFERENCE.equals("-")) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTCOLOR.equals("")) || (!FONTSTYLE.equals("-")) || (LISTSYMBOL > -1) || (!PREFIX.equals("-")) || (MARGIN > -1)) {
                        str = str + ", ";
                    }
                    str = str + "REFERENCE='" + REFERENCE + "'";
                }
                if (STATUS > -1) {
                    if ((SID > 0) || (TIP > -1) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!PICPATH.equals("no_change")) || (PICSTYLE > -1) || (!PAR.equals("")) || (PARALIGN > -1) || (FIRSTMARGIN > -1) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTCOLOR.equals("")) || (!FONTSTYLE.equals("-")) || (LISTSYMBOL > -1) || (!PREFIX.equals("-")) || (MARGIN > -1) || (!REFERENCE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into SlideBlocks(SID,TIP,LTX,LTY,RBX,RBY,PICPATH,PICSTYLE,PAR,PARALIGN,FIRSTMARGIN,FONTNAME,FONTSIZE,FONTCOLOR,FONTSTYLE,LISTSYMBOL,PREFIX,MARGIN,REFERENCE,STATUS) VALUES(";
            str = str + SID + "," + TIP + "," + LTX + "," + LTY + "," + RBX + "," + RBY + ",'" + PICPATH + "'," + PICSTYLE + ",'" + PAR.replaceAll("'", "''") + "'," + PARALIGN + "," + FIRSTMARGIN + ",'" + FONTNAME + "'," + FONTSIZE + ",'" + FONTCOLOR + "','" + FONTSTYLE + "'," + LISTSYMBOL + ",'" + PREFIX + "'," + MARGIN + ",'" + REFERENCE + "'," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from SlideBlocks order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetSlideBlocks(int ID, int SID, int TIP, int STATUS) {
        String str;

        str = "select * from SlideBlocks";
        if ((ID > 0) || (SID > 0) || (TIP > -1) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (SID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "SID=" + SID;
            }
            if (TIP > -1) {
                if ((ID > 0) || (SID > 0)) {
                    str = str + " and ";
                }
                str = str + "TIP=" + TIP;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (SID > 0) || (TIP > -1)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AEListElements(int ID, int BID, String ELEM, int ORDE, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((BID > 0) || (!ELEM.equals("")) || (ORDE > 0) || (STATUS > -1)) {
                str = "update ListElements SET ";
                if (BID > 0) {
                    str = str + "BID=" + BID;
                }
                if (!ELEM.equals("")) {
                    if (BID > 0) {
                        str = str + ", ";
                    }
                    str = str + "ELEM='" + ELEM.replaceAll("'", "''") + "'";
                }
                if (ORDE > 0) {
                    if ((BID > 0) || (!ELEM.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "ORDE=" + ORDE;
                }
                if (STATUS > -1) {
                    if ((BID > 0) || (!ELEM.equals("")) || (ORDE > 0)) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into ListElements(BID,ELEM,ORDE,STATUS) VALUES(";
            str = str + BID + ",'" + ELEM.replaceAll("'", "''") + "'," + ORDE + "," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from ListElements order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetListElements(int ID, int BID, int STATUS) {
        String str;

        str = "select * from ListElements";
        if ((ID > 0) || (BID > 0) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (BID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "BID=" + BID;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (BID > 0)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
            str = str + " order by ORDE,ID";
        }
        return RetValue(str);
    }

    public int AEHints(int ID, int BID, String HINT, int LTX, int LTY,
            int RBX, int RBY, String BORDERCOLOR, String TEXTCOLOR,
            String FORECOLOR, String FONTNAME, int FONTSIZE, String FONTSTYLE,
            String REFERENCE, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals("")) || (!TEXTCOLOR.equals("")) || (!FORECOLOR.equals("")) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTSTYLE.equals("-")) || (!REFERENCE.equals("-")) || (STATUS > -1)) {
                str = "update Hints SET ";
                if (BID > 0) {
                    str = str + "BID=" + BID;
                }
                if (!HINT.equals("")) {
                    if (BID > 0) {
                        str = str + ", ";
                    }
                    str = str + "HINT='" + HINT + "'";
                }
                if (LTX > -1) {
                    if ((BID > 0) || (!HINT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "LTX=" + LTX;
                }
                if (LTY > -1) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1)) {
                        str = str + ", ";
                    }
                    str = str + "LTY=" + LTY;
                }
                if (RBX > -1) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RBX=" + RBX;
                }
                if (RBY > -1) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RBY=" + RBY;
                }
                if (!BORDERCOLOR.equals("")) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1)) {
                        str = str + ", ";
                    }
                    str = str + "BORDERCOLOR='" + BORDERCOLOR + "'";
                }
                if (!TEXTCOLOR.equals("")) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "TEXTCOLOR='" + TEXTCOLOR + "'";
                }
                if (!FORECOLOR.equals("")) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals("")) || (!TEXTCOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "FORECOLOR='" + FORECOLOR + "'";
                }
                if (!FONTNAME.equals("")) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals("")) || (!TEXTCOLOR.equals("")) || (!FORECOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "FONTNAME='" + FONTNAME + "'";
                }
                if (FONTSIZE > 0) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals("")) || (!TEXTCOLOR.equals("")) || (!FORECOLOR.equals("")) || (!FONTNAME.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "FONTSIZE=" + FONTSIZE;
                }
                if (!FONTSTYLE.equals("-")) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals("")) || (!TEXTCOLOR.equals("")) || (!FORECOLOR.equals("")) || (!FONTNAME.equals("")) || (FONTSIZE > 0)) {
                        str = str + ", ";
                    }
                    str = str + "FONTSTYLE='" + FONTSTYLE + "'";
                }
                if (!REFERENCE.equals("-")) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals("")) || (!TEXTCOLOR.equals("")) || (!FORECOLOR.equals("")) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTSTYLE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "REFERENCE='" + REFERENCE + "'";
                }
                if (STATUS > -1) {
                    if ((BID > 0) || (!HINT.equals("")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals("")) || (!TEXTCOLOR.equals("")) || (!FORECOLOR.equals("")) || (!FONTNAME.equals("")) || (FONTSIZE > 0) || (!FONTSTYLE.equals("-")) || (!REFERENCE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into Hints(BID,HINT,LTX,LTY,RBX,RBY,BORDERCOLOR,TEXTCOLOR,FORECOLOR,FONTNAME,FONTSIZE,FONTSTYLE,REFERENCE,STATUS) VALUES(";
            str = str + BID + ",'" + HINT.replaceAll("'", "''") + "'," + LTX + "," + LTY + "," + RBX + "," + RBY + ",'" + BORDERCOLOR + "','" + TEXTCOLOR + "','" + FORECOLOR + "','" + FONTNAME + "'," + FONTSIZE + ",'" + FONTSTYLE + "','" + REFERENCE + "'," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from Hints order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetHints(int ID, int BID, int STATUS) {
        String str;

        str = "select * from Hints";
        if ((ID > 0) || (BID > 0) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (BID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "BID=" + BID;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (BID > 0)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AETransitions(int ID, int BID, int SID, int LTX, int LTY, int RBX,
            int RBY, String BORDERCOLOR, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((BID > 0) || (SID > 0) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals("")) || (STATUS > -1)) {
                str = "update Transitions SET ";
                if (BID > 0) {
                    str = str + "BID=" + BID;
                }
                if (SID > 0) {
                    if (BID > 0) {
                        str = str + ", ";
                    }
                    str = str + "SID=" + SID;
                }
                if (LTX > -1) {
                    if ((BID > 0) || (SID > 0)) {
                        str = str + ", ";
                    }
                    str = str + "LTX=" + LTX;
                }
                if (LTY > -1) {
                    if ((BID > 0) || (SID > 0) || (LTX > -1)) {
                        str = str + ", ";
                    }
                    str = str + "LTY=" + LTY;
                }
                if (RBX > -1) {
                    if ((BID > 0) || (SID > 0) || (LTX > -1) || (LTY > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RBX=" + RBX;
                }
                if (RBY > -1) {
                    if ((BID > 0) || (SID > 0) || (LTX > -1) || (LTY > -1) || (RBX > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RBY=" + RBY;
                }
                if (!BORDERCOLOR.equals("")) {
                    if ((BID > 0) || (SID > 0) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1)) {
                        str = str + ", ";
                    }
                    str = str + "BORDERCOLOR='" + BORDERCOLOR + "'";
                }
                if (STATUS > -1) {
                    if ((BID > 0) || (SID > 0) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (!BORDERCOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into Transitions(BID,SID,LTX,LTY,RBX,RBY,BORDERCOLOR,STATUS) VALUES(";
            str = str + BID + "," + SID + "," + LTX + "," + LTY + "," + RBX + "," + RBY + ",'" + BORDERCOLOR + "'," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from Transitions order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetTransitions(int ID, int BID, int STATUS) {
        String str;

        str = "select * from Transitions";
        if ((ID > 0) || (BID > 0) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (BID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "BID=" + BID;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (BID > 0)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AEViewSettings(int ID, int FID, int TS, int MODE, String FLFONT,
            int FLFONTSIZE, String FLFONTCOLOR, String FLFONTSTYLE, String SCROUTERBACK,
            String SCRINNERBACK, String STAGEFONT, int STAGEFONTSIZE, String STAGEFONTCOLOR,
            String STAGEFONTSTYLE, String ACTFONT, int ACTFONTSIZE, String ACTFONTCOLOR,
            String ACTFONTSTYLE, String TEXTFONT, int TEXTFONTSIZE, String TEXTFONTCOLOR,
            String TEXTFONTSTYLE, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals("")) || (ACTFONTSIZE > 0) || (!ACTFONTCOLOR.equals("")) || (!ACTFONTSTYLE.equals("-")) || (!TEXTFONT.equals("")) || (TEXTFONTSIZE > 0) || (!TEXTFONTCOLOR.equals("")) || (!TEXTFONTSTYLE.equals("-")) || (STATUS > -1)) {
                str = "update ViewSettings SET ";
                if (FID > 0) {
                    str = str + "FID=" + FID;
                }
                if (TS > -1) {
                    if (FID > 0) {
                        str = str + ", ";
                    }
                    str = str + "TS=" + TS;
                }
                if (MODE > -1) {
                    if ((FID > 0) || (TS > -1)) {
                        str = str + ", ";
                    }
                    str = str + "MODE=" + MODE;
                }
                if (!FLFONT.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1)) {
                        str = str + ", ";
                    }
                    str = str + "FLFONT='" + FLFONT + "'";
                }
                if (FLFONTSIZE > 0) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "FLFONTSIZE=" + FLFONTSIZE;
                }
                if (!FLFONTCOLOR.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0)) {
                        str = str + ", ";
                    }
                    str = str + "FLFONTCOLOR='" + FLFONTCOLOR + "'";
                }
                if (!FLFONTSTYLE.equals("-")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "FLFONTSTYLE='" + FLFONTSTYLE + "'";
                }
                if (!SCROUTERBACK.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "SCROUTERBACK='" + SCROUTERBACK + "'";
                }
                if (!SCRINNERBACK.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "SCRINNERBACK='" + SCRINNERBACK + "'";
                }
                if (!STAGEFONT.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "STAGEFONT='" + STAGEFONT + "'";
                }
                if (STAGEFONTSIZE > 0) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "STAGEFONTSIZE=" + STAGEFONTSIZE;
                }
                if (!STAGEFONTCOLOR.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0)) {
                        str = str + ", ";
                    }
                    str = str + "STAGEFONTCOLOR='" + STAGEFONTCOLOR + "'";
                }
                if (!STAGEFONTSTYLE.equals("-")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "STAGEFONTSTYLE='" + STAGEFONTSTYLE + "'";
                }
                if (!ACTFONT.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "ACTFONT='" + ACTFONT + "'";
                }
                if (ACTFONTSIZE > 0) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "ACTFONTSIZE=" + ACTFONTSIZE;
                }
                if (!ACTFONTCOLOR.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals("")) || (ACTFONTSIZE > 0)) {
                        str = str + ", ";
                    }
                    str = str + "ACTFONTCOLOR='" + ACTFONTCOLOR + "'";
                }
                if (!ACTFONTSTYLE.equals("-")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals("")) || (ACTFONTSIZE > 0) || (!ACTFONTCOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "ACTFONTSTYLE='" + ACTFONTSTYLE + "'";
                }
                if (!TEXTFONT.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals("")) || (ACTFONTSIZE > 0) || (!ACTFONTCOLOR.equals("")) || (!ACTFONTSTYLE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "TEXTFONT='" + TEXTFONT + "'";
                }
                if (TEXTFONTSIZE > 0) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals("")) || (ACTFONTSIZE > 0) || (!ACTFONTCOLOR.equals("")) || (!ACTFONTSTYLE.equals("-")) || (!TEXTFONT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "TEXTFONTSIZE=" + TEXTFONTSIZE;
                }
                if (!TEXTFONTCOLOR.equals("")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals("")) || (ACTFONTSIZE > 0) || (!ACTFONTCOLOR.equals("")) || (!ACTFONTSTYLE.equals("-")) || (!TEXTFONT.equals("")) || (TEXTFONTSIZE > 0)) {
                        str = str + ", ";
                    }
                    str = str + "TEXTFONTCOLOR='" + TEXTFONTCOLOR + "'";
                }
                if (!TEXTFONTSTYLE.equals("-")) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals("")) || (ACTFONTSIZE > 0) || (!ACTFONTCOLOR.equals("")) || (!ACTFONTSTYLE.equals("-")) || (!TEXTFONT.equals("")) || (TEXTFONTSIZE > 0) || (!TEXTFONTCOLOR.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "TEXTFONTSTYLE='" + TEXTFONTSTYLE + "'";
                }
                if (STATUS > -1) {
                    if ((FID > 0) || (TS > -1) || (MODE > -1) || (!FLFONT.equals("")) || (FLFONTSIZE > 0) || (!FLFONTCOLOR.equals("")) || (!FLFONTSTYLE.equals("-")) || (!SCROUTERBACK.equals("")) || (!SCRINNERBACK.equals("")) || (!STAGEFONT.equals("")) || (STAGEFONTSIZE > 0) || (!STAGEFONTCOLOR.equals("")) || (!STAGEFONTSTYLE.equals("-")) || (!ACTFONT.equals("")) || (ACTFONTSIZE > 0) || (!ACTFONTCOLOR.equals("")) || (!ACTFONTSTYLE.equals("-")) || (!TEXTFONT.equals("")) || (TEXTFONTSIZE > 0) || (!TEXTFONTCOLOR.equals("")) || (!TEXTFONTSTYLE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into ViewSettings(FID,TS,MODE,FLFONT,FLFONTSIZE,FLFONTCOLOR,FLFONTSTYLE,SCROUTERBACK,SCRINNERBACK,STAGEFONT,STAGEFONTSIZE,";
            str = str + "STAGEFONTCOLOR,STAGEFONTSTYLE,ACTFONT,ACTFONTSIZE,ACTFONTCOLOR,ACTFONTSTYLE,TEXTFONT,TEXTFONTSIZE,TEXTFONTCOLOR,TEXTFONTSTYLE,STATUS) VALUES(";
            str = str + FID + "," + TS + "," + MODE + ",'" + FLFONT + "'," + FLFONTSIZE + ",'" + FLFONTCOLOR + "','" + FLFONTSTYLE + "','" + SCROUTERBACK + "','" + SCRINNERBACK + "','" + STAGEFONT + "'," + STAGEFONTSIZE + ",'" + STAGEFONTCOLOR + "','" + STAGEFONTSTYLE + "','" + ACTFONT + "'," + ACTFONTSIZE + ",'" + ACTFONTCOLOR + "','" + ACTFONTSTYLE + "','" + TEXTFONT + "'," + TEXTFONTSIZE + ",'" + TEXTFONTCOLOR + "','" + TEXTFONTSTYLE + "'," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from ViewSettings order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetViewSettings(int ID, int FID, int TS, int MODE, int STATUS) {
        String str = null;

        str = "select a.ID,a.FID,a.TS,a.MODE,a.FLFONT,a.FLFONTSIZE,a.FLFONTCOLOR,a.FLFONTSTYLE,a.SCROUTERBACK,a.SCRINNERBACK,";
        str = str + "a.STAGEFONT,a.STAGEFONTSIZE,a.STAGEFONTCOLOR,a.STAGEFONTSTYLE,a.ACTFONT,a.ACTFONTSIZE,a.ACTFONTCOLOR,";
        str = str + "a.ACTFONTSTYLE,a.TEXTFONT,a.TEXTFONTSIZE,a.TEXTFONTCOLOR,a.TEXTFONTSTYLE,a.STATUS,";
        if (TS == 0) {
            str = str + "b.TXT from ViewSettings a, TypTasks b where a.FID=b.ID and a.TS=0";
        }
        if (TS == 1) {
            str = str + "b.CYPHER from ViewSettings a, Faults b where a.FID=b.ID and a.TS=1";
        }
        if (TS == 2) {
            str = str + "b.TXT from ViewSettings a, SoftTasks b where a.FID=b.ID and a.TS=2";
        }
        if (ID > 0) {
            str = str + " and a.ID=" + ID;
        }
        if (FID > 0) {
            str = str + " and a.FID=" + FID;
        }
        if (MODE > -1) {
            str = str + " and a.MODE=" + MODE;
        }
        if (STATUS > -1) {
            str = str + " and a.STATUS=" + STATUS;
        }
        return RetValue(str);
    }

    public void AddManning(String Name, int NumMembers, int Status) {
        String str;

        str = "insert into ManningTable(Name,CStatus,NumMembers) VALUES('" + Name + "'," + Status + "," + NumMembers + ")";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void DeleteManning(int ID) {
        String str;

        str = "update ManningTable set CStatus=0 where ID=" + ID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void EditManning(int ID, String NAME, int NumMembers, int Status) {
        String str;

        str = "update ManningTable SET CStatus=" + Status;
        if (!NAME.equals("")) {
            str = str + ", Name='" + NAME + "'";
        }
        if (NumMembers > -1) {
            str = str + ", NumMembers=" + NumMembers;
        }
        str = str + " where ID=" + ID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void RestoreManning(int ID) {
        String str;

        str = "update ManningTable set CStatus=1 where ID=" + ID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void ExcludeGU(int GID, int UID) {
        String str;

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        str = "update GU SET DendDT='" + sdf.format(new Date()) + "', ";
        str = str + "IStatus=0 where IStatus=1 and Igid=" + GID + " and Iuid=" + UID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public Vector<String> ChangeQAConst(int ID, int UID, String TableName,
            String FieldNew) {
        String str;
        int last;

        Vector<String> Temp = new Vector<String>();
        str = "select CStatus from USERS where ITnum=" + UID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                if (rs.getInt(1) == 2) {
                    stmt.close();
                    rs.close();
                    if (ID >= 0) {
                        str = "update " + TableName + " set NAME='" + FieldNew + "' where ID=" + ID;
                    } else {
                        str = "select ID from " + TableName + "order by oid desc limit 1";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        rs = NormalizeRowSet(stmt.executeQuery(str));
                        rs.next();
                        last = rs.getInt(1);
                        stmt.close();
                        rs.close();
                        str = "insert into " + TableName + " values (" + (last + 1) + ", '" + FieldNew + "')";
                    }
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    stmt.executeUpdate(str);
                    stmt.close();
                    Temp.add("");
                } else {
                    Temp.add("no_permission");
                }
            } else {
                Temp.add("no_permission");
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public Vector<String> ChangeQAType(int ID, int UID, String NameNew,
            String ShortNew, boolean TPNew) {
        String str;
        String bval;
        int last;

        Vector<String> Temp = new Vector<String>();
        str = "select CStatus from USERS where ITnum=" + UID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                if (rs.getInt(1) == 2) {
                    stmt.close();
                    rs.close();
                    if (TPNew) {
                        bval = "True";
                    } else {
                        bval = "False";
                    }
                    if (ID >= 0) {
                        str = "update QATypes set Name='" + NameNew + "', ShortName='" + ShortNew + "', TP=" + bval + " where ID=" + ID;
                    } else {
                        str = "select ID from QATypes" + "order by oid desc limit 1";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        rs = NormalizeRowSet(stmt.executeQuery(str));
                        rs.next();
                        last = rs.getInt(1);
                        stmt.close();
                        rs.close();
                        str = "insert into QATypes values (" + (last + 1) + ", " + bval + ", '" + NameNew + "', '" + ShortNew + "')";
                    }
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    stmt.executeUpdate(str);
                    stmt.close();
                    Temp.add("");
                } else {
                    Temp.add("no_permission");
                }
            } else {
                Temp.add("no_permission");
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public Vector<String> RemoveQAConst(int ID, int UID, String TableName) {
        String str;

        Vector<String> Temp = new Vector<String>();
        str = "select CStatus from USERS where ITnum=" + UID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                if (rs.getInt(1) == 2) {
                    stmt.close();
                    rs.close();
                    if (TableName.equals("SoftTasks")) {
                        str = "delete from ViewSettings where TS=2 and FID=" + ID;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from SoftUserActions where USRID in (select ID from SoftUserResults where APPID in (select ID from SoftUserTasks where STID=" + ID + "))";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from SoftUserResults where APPID in (select ID from SoftUserTasks where STID=" + ID + ")";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from SoftUserTasks where STID=" + ID;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from SoftAddData where SAID in (select ID from SoftActions where STID=" + ID + ")";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from SoftActions where STID=" + ID;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                    }
                    if (TableName.equals("Tests")) {
                        str = "delete from UTDetails where UTID in (select ID from UTResults where APPID in (select ID from UserTests where TestID=" + ID + "))";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from UTResults where APPID in (select ID from UserTests where TestID=" + ID + ")";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from UserTests where TestID=" + ID;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from AnsVariants where QID in (select ID from Questions where TID=" + ID + ")";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from AnsVars where QID in (select ID from Questions where TID=" + ID + ")";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from Questions where TID=" + ID;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                    }
                    if (TableName.equals("Demonstrations")) {
                        str = "delete from ListElements where BID in (select ID from SlideBlocks where SID in (select ID from Slides where DID=" + ID + "))";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from Hints where BID in (select ID from SlideBlocks where SID in (select ID from Slides where DID=" + ID + "))";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from Transitions where BID in (select ID from SlideBlocks where SID in (select ID from Slides where DID=" + ID + "))";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from SlideBlocks where SID in (select ID from Slides where DID=" + ID + ")";
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from Slides where DID=" + ID;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                    }
                    str = "delete from " + TableName + " where ID=" + ID;
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    stmt.executeUpdate(str);
                    stmt.close();
                    Temp.add("");
                } else {
                    Temp.add("no_permission");
                }
            } else {
                Temp.add("no_permission");
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void RemoveQATree(int ID) {
        String str;
        int i;
        Vector<String> ids = new Vector<String>();

        str = "select Iid from QAReg where IOwner=" + ID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            while (rs.next()) {
                ids.add(rs.getInt(1) + "");
            }
            rs.close();
            stmt.close();
            for (i = 0; i < ids.size(); i++) {
                RemoveQATree(Integer.parseInt(ids.elementAt(i)));
            }
            str = "delete from QAReg where Iid=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from UT where Itid=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from GT where Itid=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public Vector<String> RemoveQA(int ID, int UID) {
        String str, idStr;
        int LastVer, CId, CVer;
        Vector<String> ids = new Vector<String>();
        int i;

        Vector<String> Temp = new Vector<String>();
        str = "select CStatus from USERS where ITnum=" + UID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            if (rs.next()) {
                if (rs.getInt(1) == 2) {
                    stmt.close();
                    rs.close();
                    LastVer = ID;
                    while (true) {
                        str = "select Iid from QAReg where IWV=" + LastVer;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        rs = NormalizeRowSet(stmt.executeQuery(str));
                        if (rs.next()) {
                            LastVer = rs.getInt(1);
                        } else {
                            break;
                        }
                        rs.close();
                        stmt.close();
                    }
                    str = "select Iid from QAReg where IOwner=" + LastVer;
                    stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    rs = NormalizeRowSet(stmt.executeQuery(str));
                    while (rs.next()) {
                        ids.add(rs.getInt(1) + "");
                    }
                    rs.close();
                    stmt.close();
                    for (i = 0; i < ids.size(); i++) {
                        RemoveQATree(Integer.parseInt(ids.elementAt(i)));
                    }
                    CId = LastVer;
                    while (true) {
                        str = "select Iid, IWV, IVersion from QAReg where Iid=" + CId;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        rs = NormalizeRowSet(stmt.executeQuery(str));
                        rs.next();
                        idStr = CId + "";
                        CId = rs.getInt(2);
                        CVer = rs.getInt(3);
                        rs.close();
                        stmt.close();
                        str = "delete from QAReg where Iid=" + idStr;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from UT where Itid=" + idStr;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        str = "delete from GT where Itid=" + idStr;
                        stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        stmt.executeUpdate(str);
                        stmt.close();
                        if (CVer == 1) {
                            break;
                        }
                    }
                    Temp.add("");
                } else {
                    Temp.add("no_permission");
                }
            } else {
                Temp.add("no_permission");
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }

        return Temp;
    }

    public void RemoveProject(int ID) {
        String str;

        try {
            str = "delete from UT where Itid in (select Iid from QAReg where Ipid = " + ID + ")";
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from GT where Itid in (select Iid from QAReg where Ipid = " + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from ListElements where BID in (select ID from SlideBlocks where SID in (select ID from Slides where DID in (select ID from Demonstrations where DISC=" + ID + ")))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from Hints where BID in (select ID from SlideBlocks where SID in (select ID from Slides where DID in (select ID from Demonstrations where DISC=" + ID + ")))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from Transitions where BID in (select ID from SlideBlocks where SID in (select ID from Slides where DID in (select ID from Demonstrations where DISC=" + ID + ")))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SlideBlocks where SID in (select ID from Slides where DID in (select ID from Demonstrations where DISC=" + ID + "))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from Slides where DID in (select ID from Demonstrations where DISC=" + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from Demonstrations where DISC=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from ViewSettings where TS=2 and FID in (select ID from SoftTasks where DISC=" + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftUserActions where USRID in (select ID from SoftUserResults where APPID in (select ID from SoftUserTasks where STID in (select ID from SoftTasks where DISC=" + ID + ")))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftUserResults where APPID in (select ID from SoftUserTasks where STID in (select ID from SoftTasks where DISC=" + ID + "))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftUserTasks where STID in (select ID from SoftTasks where DISC=" + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftAddData where SAID in (select ID from SoftActions where STID in (select ID from SoftTasks where DISC=" + ID + "))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftActions where STID in (select ID from SoftTasks where DISC=" + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftTasks where DISC=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from UTDetails where UTID in (select ID from UTResults where APPID in (select ID from UserTests where TestID in (select ID from Tests where DISC=" + ID + ")))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from UTResults where APPID in (select ID from UserTests where TestID in (select ID from Tests where DISC=" + ID + "))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from UserTests where TestID in (select ID from Tests where DISC=" + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from AnsVariants where QID in (select ID from Questions where TID in (select ID from Tests where DISC=" + ID + "))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from AnsVars where QID in (select ID from Questions where TID in (select ID from Tests where DISC=" + ID + "))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from Questions where TID in (select ID from Tests where DISC=" + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from Tests where DISC=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from QAReg where Ipid = " + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from Projects where Iid = " + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public int AESoftTasks(int ID, String TXT, String DESCR, String REFERENCE,
            int DISC, int PART, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((!TXT.equals("")) || (!DESCR.equals("-")) || (!REFERENCE.equals("-")) || (DISC > 0) || (PART > -1) || (STATUS > -1)) {
                str = "update SoftTasks SET ";
                if (!TXT.equals("")) {
                    str = str + "TXT='" + TXT.replaceAll("'", "''") + "'";
                }
                if (!DESCR.equals("-")) {
                    if (!TXT.equals("")) {
                        str = str + ", ";
                    }
                    str = str + "DESCR='" + DESCR.replaceAll("'", "''") + "'";
                }
                if (!REFERENCE.equals("-")) {
                    if ((!TXT.equals("")) || (!DESCR.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "REFERENCE='" + REFERENCE + "'";
                }
                if (DISC > 0) {
                    if ((!TXT.equals("")) || (!DESCR.equals("-")) || (!REFERENCE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "DISC=" + DISC;
                }
                if (PART > -1) {
                    if ((!TXT.equals("")) || (!DESCR.equals("-")) || (!REFERENCE.equals("-")) || (DISC > 0)) {
                        str = str + ", ";
                    }
                    str = str + "PART=" + PART;
                }
                if (STATUS > -1) {
                    if ((!TXT.equals("")) || (!DESCR.equals("-")) || (!REFERENCE.equals("-")) || (DISC > 0) || (PART > -1)) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into SoftTasks(TXT,DESCR,REFERENCE,DISC,PART,STATUS) VALUES('";
            str = str + TXT.replaceAll("'", "''") + "','" + DESCR.replaceAll("'", "''") + "','" + REFERENCE + "'," + DISC + "," + PART + "," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from SoftTasks order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetSoftTasks(int ID, int DISC, int PART, int STATUS) {
        String str;

        str = "select * from SoftTasks";
        if ((ID > 0) || (DISC > 0) || (PART > -1) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (DISC > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "DISC=" + DISC;
            }
            if (PART > -1) {
                if ((ID > 0) || (DISC > 0)) {
                    str = str + " and ";
                }
                str = str + "PART=" + PART;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (DISC > 0) || (PART > -1)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        return RetValue(str);
    }

    public int AESoftActions(int ID, int STID, int ORDE, String PICPATH,
            String PICPATHE, String DESCR, int LTX, int LTY, int RBX, int RBY,
            int ACTTYPE, int TIMING, String REFERENCE, int STATUS) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (ACTTYPE > -1) || (TIMING > -1) || (!REFERENCE.equals("-")) || (STATUS > -1)) {
                str = "update SoftActions SET ";
                if (STID > 0) {
                    str = str + "STID=" + STID;
                }
                if (ORDE > -1) {
                    if (STID > 0) {
                        str = str + ", ";
                    }
                    str = str + "ORDE=" + ORDE;
                }
                if (!PICPATH.equals("no_change")) {
                    if ((STID > 0) || (ORDE > -1)) {
                        str = str + ", ";
                    }
                    str = str + "PICPATH='" + PICPATH + "'";
                }
                if (!PICPATHE.equals("no_change")) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change"))) {
                        str = str + ", ";
                    }
                    str = str + "PICPATHE='" + PICPATHE + "'";
                }
                if (!DESCR.equals("-")) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change"))) {
                        str = str + ", ";
                    }
                    str = str + "DESCR='" + DESCR.replaceAll("'", "''") + "'";
                }
                if (LTX > -1) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "LTX=" + LTX;
                }
                if (LTY > -1) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-")) || (LTX > -1)) {
                        str = str + ", ";
                    }
                    str = str + "LTY=" + LTY;
                }
                if (RBX > -1) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-")) || (LTX > -1) || (LTY > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RBX=" + RBX;
                }
                if (RBY > -1) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-")) || (LTX > -1) || (LTY > -1) || (RBX > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RBY=" + RBY;
                }
                if (ACTTYPE > -1) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1)) {
                        str = str + ", ";
                    }
                    str = str + "ACTTYPE=" + ACTTYPE;
                }
                if (TIMING > -1) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (ACTTYPE > -1)) {
                        str = str + ", ";
                    }
                    str = str + "TIMING=" + TIMING;
                }
                if (!REFERENCE.equals("-")) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (ACTTYPE > -1) || (TIMING > -1)) {
                        str = str + ", ";
                    }
                    str = str + "REFERENCE='" + REFERENCE + "'";
                }
                if (STATUS > -1) {
                    if ((STID > 0) || (ORDE > -1) || (!PICPATH.equals("no_change")) || (!PICPATHE.equals("no_change")) || (!DESCR.equals("-")) || (LTX > -1) || (LTY > -1) || (RBX > -1) || (RBY > -1) || (ACTTYPE > -1) || (TIMING > -1) || (!REFERENCE.equals("-"))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into SoftActions(STID,ORDE,PICPATH,PICPATHE,DESCR,LTX,LTY,RBX,RBY,ACTTYPE,TIMING,REFERENCE,STATUS) VALUES(";
            str = str + STID + "," + ORDE + ",'" + PICPATH + "','" + PICPATHE + "','" + DESCR.replaceAll("'", "''") + "'," + LTX + "," + LTY + "," + RBX + "," + RBY + "," + ACTTYPE + "," + TIMING + ",'" + REFERENCE + "'," + STATUS + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from SoftActions order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetSoftActions(int ID, int STID, int STATUS) {
        String str;

        str = "select * from SoftActions";
        if ((ID > 0) || (STID > 0) || (STATUS > -1)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (STID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "STID=" + STID;
            }
            if (STATUS > -1) {
                if ((ID > 0) || (STID > 0)) {
                    str = str + " and ";
                }
                str = str + "STATUS=" + STATUS;
            }
        }
        str = str + " order by ORDE,ID";
        return RetValue(str);
    }

    public int AESoftAddData(int ID, int SAID, String VAL, boolean TRUTH) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            str = "update SoftAddData SET TRUTH=";
            if (TRUTH) {
                str = str + "True";
            } else {
                str = str + "False";
            }
            if (SAID > 0) {
                str = str + ", SAID=" + SAID;
            }
            if (!VAL.equals("")) {
                str = str + ", VAL='" + VAL.replaceAll("'", "''") + "'";
            }
            str = str + " where ID=" + ID;
        } else {
            str = "insert into SoftAddData(SAID,VAL,TRUTH) VALUES(";
            str = str + SAID + ",'" + VAL.replaceAll("'", "''") + "',";
            if (TRUTH) {
                str = str + "True)";
            } else {
                str = str + "False)";
            }
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from SoftAddData order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void DelSoftAddData(int ID) {
        String str;

        str = "delete from SoftAddData where ID=" + ID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public CachedRowSet GetSoftAddData(int ID, int SAID) {
        String str;

        str = "select * from SoftAddData";
        if ((ID > 0) || (SAID > 0)) {
            str = str + " where ";
            if (ID > 0) {
                str = str + "ID=" + ID;
            }
            if (SAID > 0) {
                if (ID > 0) {
                    str = str + " and ";
                }
                str = str + "SAID=" + SAID;
            }
        }
        return RetValue(str);
    }

    public int AESoftUserTasks(int ID, int UID, int STID, String ExamDT,
            int STATUS, int MARK, int ERRORLIMIT, int TIMELIMIT, int ISACTTIME,
            int GRID) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((UID > 0) || (STID > 0) || (!ExamDT.equals("")) || (STATUS > -1) || (MARK > -1) || (ERRORLIMIT > -1) || (TIMELIMIT > -1) || (ISACTTIME > -1) || (GRID > 0)) {
                str = "update SoftUserTasks SET ";
                if (UID > 0) {
                    str = str + "UID=" + UID;
                }
                if (STID > 0) {
                    if (UID > 0) {
                        str = str + ", ";
                    }
                    str = str + "STID=" + STID;
                }
                if (!ExamDT.equals("")) {
                    if ((UID > 0) || (STID > 0)) {
                        str = str + ", ";
                    }
                    str = str + "ExamDT='" + ExamDT + "'";
                }
                if (STATUS > -1) {
                    if ((UID > 0) || (STID > 0) || (!ExamDT.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "STATUS=" + STATUS;
                }
                if (MARK > -1) {
                    if ((UID > 0) || (STID > 0) || (!ExamDT.equals("")) || (STATUS > -1)) {
                        str = str + ", ";
                    }
                    str = str + "MARK=" + MARK;
                }
                if (ERRORLIMIT > -1) {
                    if ((UID > 0) || (STID > 0) || (!ExamDT.equals("")) || (STATUS > -1) || (MARK > -1)) {
                        str = str + ", ";
                    }
                    str = str + "ERRORLIMIT=" + ERRORLIMIT;
                }
                if (TIMELIMIT > -1) {
                    if ((UID > 0) || (STID > 0) || (!ExamDT.equals("")) || (STATUS > -1) || (MARK > -1) || (ERRORLIMIT > -1)) {
                        str = str + ", ";
                    }
                    str = str + "TIMELIMIT=" + TIMELIMIT;
                }
                if (ISACTTIME > -1) {
                    if ((UID > 0) || (STID > 0) || (!ExamDT.equals("")) || (STATUS > -1) || (MARK > -1) || (ERRORLIMIT > -1) || (TIMELIMIT > -1)) {
                        str = str + ", ";
                    }
                    str = str + "ISACTTIME=" + ISACTTIME;
                }
                if (GRID > -1) {
                    if ((UID > 0) || (STID > 0) || (!ExamDT.equals("")) || (STATUS > -1) || (MARK > -1) || (ERRORLIMIT > -1) || (TIMELIMIT > -1) || (ISACTTIME > -1)) {
                        str = str + ", ";
                    }
                    str = str + "GRID=" + GRID;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into SoftUserTasks(UID,STID,ExamDT,STATUS,MARK,ERRORLIMIT,TIMELIMIT,ISACTTIME,GRID) VALUES(";
            str = str + UID + "," + STID + ",'" + ExamDT + "'," + STATUS + "," + MARK + "," + ERRORLIMIT + "," + TIMELIMIT + "," + ISACTTIME + "," + GRID + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from SoftUserTasks order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetSoftUserTasks(int ID, int UID, int STID, String DT1,
            String DT2, int STATUS, int MARK) {
        String str;

        str = "select a.ID,a.UID,a.STID,a.ExamDT,a.STATUS,a.MARK,a.ERRORLIMIT,a.TIMELIMIT,a.ISACTTIME,a.GRID,b.DISC,b.PART,b.TXT,b.DESCR from SoftUserTasks a, SoftTasks b";
        str = str + " where a.STID=b.ID";
        if ((ID > 0) || (UID > 0) || (STID > 0) || (STATUS > -1) || (MARK > -1) || (!DT1.equals("")) || (!DT2.equals(""))) {
            if (ID > 0) {
                str = str + " and a.ID=" + ID;
            }
            if (UID > 0) {
                str = str + " and a.UID=" + UID;
            }
            if (STID > 0) {
                str = str + " and a.STID=" + STID;
            }
            if (STATUS > -1) {
                str = str + " and a.STATUS=" + STATUS;
            }
            if (MARK > -1) {
                str = str + " and a.MARK=" + MARK;
            }
            if (!DT1.equals("")) {
                str = str + " and a.ExamDT>='DT1'";
            }
            if (!DT2.equals("")) {
                str = str + " and a.ExamDT<='DT2'";
            }
            str = str + " order by a.ID";
        }
        return RetValue(str);
    }

    public int AESoftUserResults(int ID, int APPID, int RES, int TM) {
        String str = null;
        int Temp = 0;

        if (ID > 0) {
            if ((APPID > 0) || (RES > -1) || (TM > 0)) {
                str = "update SoftUserResults SET ";
                if (APPID > 0) {
                    str = str + "APPID=" + APPID;
                }
                if (RES > -1) {
                    if (APPID > 0) {
                        str = str + ", ";
                    }
                    str = str + "RES=" + RES;
                }
                if (TM > 0) {
                    if ((APPID > 0) || (RES > -1)) {
                        str = str + ", ";
                    }
                    str = str + "TM=" + TM;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into SoftUserResults(APPID,RES,TM) VALUES(";
            str = str + APPID + "," + RES + "," + TM + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from SoftUserResults order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public CachedRowSet GetSoftUserResults(int APPID) {
        String str;

        str = "select * from SoftUserResults";
        if (APPID > 0) {
            str = str + " where APPID=" + APPID;
        }
        str = str + " order by ID";
        return RetValue(str);
    }

    public void AESoftUserActions(int ID, int USRID, int SAID, int TM, int RES) {
        String str = null;

        if (ID > 0) {
            if ((USRID > 0) || (SAID > 0) || (TM > -1) || (RES > -1)) {
                str = "update SoftUserActions SET ";
                if (USRID > 0) {
                    str = str + "USRID=" + USRID;
                }
                if (SAID > 0) {
                    if (USRID > 0) {
                        str = str + ", ";
                    }
                    str = str + "SAID=" + SAID;
                }
                if (TM > -1) {
                    if ((USRID > 0) || (SAID > 0)) {
                        str = str + ", ";
                    }
                    str = str + "TM=" + TM;
                }
                if (RES > -1) {
                    if ((USRID > 0) || (SAID > 0) || (TM > -1)) {
                        str = str + ", ";
                    }
                    str = str + "RES=" + RES;
                }
                str = str + " where ID=" + ID;
            }
        } else {
            str = "insert into SoftUserActions(USRID,SAID,TM,RES) VALUES(";
            str = str + USRID + "," + SAID + "," + TM + "," + RES + ")";
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public CachedRowSet GetSoftUserActions(int USRID) {
        String str;

        str = "select a.ID,a.USRID,a.SAID,a.TM,a.RES,b.DESCR from SoftUserActions a, SoftActions b where a.SAID=b.ID";
        if (USRID > 0) {
            str = str + " and a.USRID=" + USRID;
        }
        str = str + " order by a.ID";
        return RetValue(str);
    }

    public void UpdateSoftActionsOrders(int STID, int ORDE) {
        String str;

        str = "update SoftActions SET ORDE=ORDE-1 where STID=" + STID + " and ORDE>" + ORDE;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public int AEQATSR(int ID, int What, String TP, String Name, String SHName) {
        String str = null, tbl = null;
        int Temp = 0;

        switch (What) {
            case 0: {
                tbl = "QATypes";
                break;
            }
            case 1: {
                tbl = "QAStatus";
                break;
            }
            case 2: {
                tbl = "Reasons";
                break;
            }
        }
        if (ID > 0) {
            if ((!TP.equals("")) || (!Name.equals("")) || (!SHName.equals(""))) {
                str = "update " + tbl + " SET ";
                if (!TP.equals("")) {
                    str = str + "TP=" + TP;
                }
                if (!Name.equals("")) {
                    if (!TP.equals("")) {
                        str = str + ", ";
                    }
                    str = str + "Name='" + Name + "'";
                }
                if (!SHName.equals("")) {
                    if ((!TP.equals("")) || (!Name.equals(""))) {
                        str = str + ", ";
                    }
                    str = str + "ShortName='" + SHName + "'";
                }
                str = str + " where ID=" + ID;
            }
        } else {
            switch (What) {
                case 0: {
                    str = "insert into QATypes(TP,Name,ShortName) VALUES(";
                    str = str + TP + ",'" + Name + "','" + SHName + "')";
                    break;
                }
                case 1: {
                    str = "insert into QAStatus(Name) VALUES('" + Name + "')";
                    break;
                }
                case 2: {
                    str = "insert into Reasons(Name) VALUES('" + Name + "')";
                    break;
                }
            }
        }
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            Temp = ID;
            if (ID <= 0) {
                str = "select ID from " + tbl + " order by oid desc limit 1";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
                rs.next();
                Temp = rs.getInt(1);
                rs.close();
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public int ImportQA(int Ipid, int IAuthor, int Itype, int IOwner,
            String MText, String SDataInfo, String SDataPath, String SSymName,
            int IStatus, int IReason) {
        String str = null;
        int Temp = 0;

        str = "insert into QAReg(Ipid,IAuthor,IType,IOwner,MText,SDataInfo,SDataPath,SSymName,IVersion,IStatus,IReason,IWV,Prec,Draft) Values(";
        str = str + Ipid + "," + IAuthor + "," + Itype + "," + IOwner + ",'" + MText.replaceAll("'", "''") + "','" + SDataInfo.replaceAll("'", "''") + "','" + SDataPath.replaceAll("'", "''") + "','" + SSymName + "',";
        str = str + "1," + IStatus + "," + IReason + ",0,0,'')";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "select Iid from QAReg where Ipid=" + Ipid + " order by oid desc limit 1";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            rs.next();
            Temp = rs.getInt(1);
            rs.close();
            stmt.close();
            if (Itype == 8) {
                str = "insert into UT(Iuid,Itid,IStatus,IRights) VALUES(" + IAuthor + "," + Temp + ",1,1)";
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.executeUpdate(str);
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public int ImportProject(boolean NeedRename, String SName, int IAuthor) {
        String str = null;
        int Temp = 0;

        str = "insert into Projects(SName,IAuthor) Values('" + SName + "'," + IAuthor + ")";
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "select Iid from Projects order by oid desc limit 1";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = NormalizeRowSet(stmt.executeQuery(str));
            rs.next();
            Temp = rs.getInt(1);
            rs.close();
            stmt.close();
            if (NeedRename) {
                str = "update Projects SET SName='" + SName + "_" + Temp + "' where Iid=" + Temp;
                stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.executeUpdate(str);
                stmt.close();
            }
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
        return Temp;
    }

    public void UpdateQAFileName(int ID, String nm) {
        String str;

        str = "update QAReg SET SDataPath='" + nm + "' where Iid=" + ID;
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }

    public void RemoveUser(int ID) {
        String str;
        try {
            str = "delete from UT where Iuid=" + ID;
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from GU where Iuid=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from TreeNode where user_id=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "update USERS SET Imgr=0 where Imgr=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftUserActions where USRID in (select ID from SoftUserResults where APPID in (select ID from SoftUserTasks where UID=" + ID + "))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftUserResults where APPID in (select ID from SoftUserTasks where UID=" + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from SoftUserTasks where UID=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from UTDetails where UTID in (select ID from UTResults where APPID in (select ID from UserTests where UID=" + ID + "))";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from UTResults where APPID in (select ID from UserTests where UID=" + ID + ")";
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from UserTests where UID=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
            str = "delete from USERS where ITnum=" + ID;
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.executeUpdate(str);
            stmt.close();
        } catch (SQLException ex) {
            exClass.Except(ex);
        }
    }
}

