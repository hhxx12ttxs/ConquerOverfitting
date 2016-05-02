package ATTS;

/**
 * ATTSutility
 * @author Edison Chindrawaly
 */

import java.io.PrintWriter;
import java.sql.*;
import java.util.Vector;

public class ATTSutility
{
 private boolean startFlag = false;
  
 public ATTSutility()
 {
  startFlag = true;
 }
 
 public void close()
 {
  startFlag = false;
 }

/**
 * verifyAuthorization's method is verify employee authorization access to the database.
 * the employee's access level and rank determine his/her access to certain info in
 * the database. It checks for passkey and password to match the id of course.
 * @param  DBFill dbFill - contains the connection to db
 *         Closure cls - contains error message for the any type of error
 *         PrintWriter out - contains ServletResponse that writes to Servlet
 *         int id - contains employee id
 *         String pk - contains employee pass key
 *         STring pwd - contains employee password
 *         int lvl - contains employee access level that is allowed to access the db
 *         String rank - contains employee rank that is allowed to access the db
 * @return boolean true if employee has the right access to the db else false
 */
 public boolean verifyAuthorization(DBFill db,Closure cls,PrintWriter out,
                                    int id,String pk,String pwd,int lvl,String rank) 
 throws SQLException
 {
  if(!startFlag)
    throw new SQLException("<BR>ATTSutility has not been started</BR>");
  if((db==null)||(cls==null)||(out==null)||(id<=0)||(pk==null)||(lvl<=0))
  {
   cls.error(1,null);
   return false;
  }
  StringBuffer x=new StringBuffer("WHERE employeeID=\"");
  if(pwd != null)
  {
   x.append(id+"\" AND employeePassKey=MD5(\""+pk+"\") AND employeePassword=MD5(\"");
   x.append(pwd+"\");"); 
  }
  else
   x.append(id+"\" AND employeePassKey=MD5(\""+pk+"\");");

  DBEncapsulation r = db.QuerySingleSpecific("employee",x.toString(),out);
  x.delete(0,x.length());
  if(r == null)
  {
   x.append("Authorization untuk employeeID["+id+"] tidak ditemukan.");
   cls.error(0,x.toString());
   x.delete(0,x.length());
   return false;
  }
  int pos1 = r.getColumnNamePosition("employeeValid");
  if(pos1<0)
  {
   cls.error(0,"employee validity not found in the encapsulation");
   return false;
  }
  if(r.getColumnStringValues(pos1).compareToIgnoreCase("N")==0)
  {
   cls.error(0,"employee id ini tidak valid di database.");
   return false;
  }
  pos1 = r.getColumnNamePosition("employeeAccessLvl");
  if((pos1==r.NOT_FOUND)||(pos1==r.OUT_RANGE)||(pos1==r.OUT_RANGE))
  {
   cls.error(0,"employee access level not found in the encapsulation");
   r.destroyAll();
   return false;
  }
  if(r.getColumnIntegerValue(pos1)>lvl) // if employee access level bigger than 
  {                                     // the determine access level of the info 
   cls.error(7,null);                   // then the employee is not allow.
   r.destroyAll();
   return false; 
  }
  if(rank != null)
  {
   pos1 = r.getColumnNamePosition("employeePosition");
   if(pos1<0)
   {
    cls.error(0,"employee position not found in the encapsulation");
    r.destroyAll();
    return false;
   }
   if(rank.compareToIgnoreCase(r.getColumnStringValues(pos1))!=0)
   {
    cls.error(0,"Your employee position is not allow to do this operation");
    return false;
   }
  }
  r.destroyAll();
  return true;
 }

/**
 * queryAirline's method is to query all the airline in ATTSairline db
 * @param  DBFill dbFill - contains the connection to db
 *         PrintWriter out - contains ServletResponse that writes to Servlet
 * @return Vector of airline if avail else return null
 */
 public Vector queryAirline(DBFill dbFill,PrintWriter out) throws SQLException
 {
  if(!startFlag)
    throw new SQLException("ATTSutility has not been started!");
  return dbFill.QuerySpecific("airline",null,out); 
 }

/**
 * queryCity's method is to query all the city in the ATTSairline db.
 * @param  DBFill dbFill - contains the connection to db
 *         PrintWriter out - contains ServletResponse that writes to Servlet
 * @return Vector of city if available else return null
 */
 public Vector queryCity(DBFill dbFill,PrintWriter out) throws SQLException
 {
  if(!startFlag)
    throw new SQLException("ATTSutility has not been started!");
  return dbFill.QuerySpecific("airlineCity",null,out);
 }

/**
 * queryCurrency's method is to query currency from ATTSairline db. 
 * @param  DBFill dbFill - contains the connection to db
 *         PrintWriter out - contains ServletResponse that writes to Servlet
 * @return Vector of currency if available else return null
 */
 public Vector queryCurrency(DBFill dbFill,PrintWriter out) throws SQLException
 {
  if(!startFlag)
    throw new SQLException("ATTSutility has not been started!");
  return dbFill.QuerySpecific("airlineCurrency",null,out);
 }

/**
 * queryExchangeRate's method is to query the exchange rate in the ATTSairline db
 * @param  DBFill dbFill - contains the connection to db
 *         PrintWriter out - contains ServletResponse that writes to Servlet
 * @return Vector of exchange rates from db or null if none avail
 */
 public Vector queryExchangeRate(DBFill dbFill,PrintWriter out) throws SQLException
 {
  if(!startFlag)
    throw new SQLException("ATTSutility has not been started!");
  return dbFill.QuerySpecific("airlineExchangeRate",null,out); 
 }

/**
 * queryAllEmp's method is to query employees with the specific conditions met.
 * The query result will be a group of employees (if condition is met) or else one or null
 * @param  DBFill dbFill - contains the connection to db
 *         String cond - contains the conditions of query for employees
 *         PrintWriter out - contains ServletResponse that writes to Servlet
 * @return Vector of employees or null if no employee found.
 */
 public Vector queryAllEmp(DBFill dbFill,String cond,PrintWriter out) throws SQLException
 {
  if(!startFlag)
    throw new SQLException("ATTSutility has not been started");
  return dbFill.QuerySpecific("employee",cond,out);
 }

/**
 * queryEmployee's method is to query a specific employee out from ATTSemployee's db
 * @param  DBFill dbFill - contains connection to the db.
 *         String cond - contains condition to retrieve the specific employee (may not
 *         be null value)
 *         PrintWriter out - contains ServletResponse that writes to Servlet
 * @return DBEncapsulation with the specific employee else null if not found
 */
 public DBEncapsulation queryEmployee(DBFill dbFill,String cond,PrintWriter out)
	throws SQLException
 {
  if(!startFlag)
    throw new SQLException("ATTSutility has not been started");
  if(cond == null)
  {
   out.println("<BR>queryEmployee may not have the null value as the condition</BR>");
   return null;
  }
  return dbFill.QuerySingleSpecific("employee",cond,out);
 }

/**
 * find's method to find the DBEncapsulation that contains the pair value of 
 * String name/value inside the Vector. If it does not find it, it returns null
 * @param  Vector v - contains DBEncapsulation that need to be search
 *         String name - the name part of name/value pair
 *         String val - the value part of name/value pair
 * @return DBEncapsulation if found, else return null
 */
 public DBEncapsulation find(Vector v,String name,String val) throws SQLException
 {
  if((v == null)||(name == null)||(val == null))
    return null;
  DBEncapsulation r = null;
  int pos1 = 0;
  int size = v.size();
  for(int i=0;i<size;i++)
  {
   r = (DBEncapsulation)((DBEncapsulation)v.get(i)).clone();
   pos1 = r.getColumnNamePosition(name);
   if((pos1==r.NOT_FOUND)||(pos1==r.OUT_RANGE)||(pos1==r.SAME_SIZE))
     break; 
   if(r.getColumnStringValues(pos1).compareToIgnoreCase(val)==0)
     return r;
   r.destroyAll();
  }
  return null;
 }

/**
 * createBegin's method is to create the beginning of form and table including
 * the parameters value like hidden value and so on.
 * @param  String servletName - contains servlet name the form is intended to.
 *         String hiddenName - contains the name of the hidden value.
 *         String val - contains the value of the hidden value.
 *         boolean hidden - contains true if user wants to use hidden value.
 *         PrintWriter out - contains ServletResponse to write out to Servlet
 */
 public void createBegin(String servletName,String hiddenName,String val,
		         boolean hidden, PrintWriter out)
 {
  out.println("<FORM METHOD=POST ACTION=\""+servletName+"\">");
  if(hidden)
    out.println("<INPUT TYPE=HIDDEN NAME=\""+hiddenName+"\" VALUE=\""+val+"\">");
  out.println("<TABLE BORDER=0 WIDTH=75%>");
 } 

/**
 * createEnd's method is to close the table format and the form input.
 * @param  PrintWriter out - contain ServletResponse to write out to Servlet
 */
 public void createEnd(PrintWriter out)
 {
  out.println("</TABLE>");
  out.println("<INPUT TYPE=SUBMIT VALUE=SUBMIT>&nbsp;<INPUT TYPE=RESET VALUE=CLEAR>");
  out.println("</FORM>");
 }

/**
 * checkTime's method is to check the String for valid Time format - HH:MM:SS
 * and make sure that the hour/minute/second does not exceed the allowed limit
 * @param  String time - contain time format string to be check
 * 	   PrintWriter out - contain ServletRespond to write out to servlet
 * @return boolean true if time format is valid else false
 */
 public boolean checkTime(String time, PrintWriter out)
 {
  if(time == null)
    return false;
  if(time.length()!=8)
  {
   if(out!=null)
     out.println("<BR><B>Time Format: HH:MM:SS  contoh 21:01:22</B></BR>");
   else
     System.out.println("Time Format: HH:MM:SS contoh 21:01:22");
   return false;
  }
  int p1 = time.indexOf(':');
  int p2 = time.indexOf(':',p1+1);
  if((p1<0)||(p2<0))
  {
   if(out!=null)
     out.println("<BR><B>Time Format: HH:MM:SS contoh 21:01:22</B></BR>");
   else
     System.out.println("Time Format: HH:MM:SS contoh 21:01:22");
   return false;
  }
  String hour = time.substring(0,p1);
  String min  = time.substring(p1+1,p2);
  String sec  = time.substring(p2+1,time.length());
  if((!checkDigit(hour,true,out))||(!checkDigit(min,true,out))||(!checkDigit(sec,true,out)))
  {
   if(out!=null)
     out.println("Time Format: yang anda masukan bukan digit");
   else
     System.out.println("Time Format: yang anda masukan bukan digit");
   return false;
  }
  int hr = Integer.parseInt(hour);
  int mn = Integer.parseInt(min);
  int sc = Integer.parseInt(sec);
  boolean error = false;
  if((hr<0)||(hr>24))
  {
   if(out!=null)
     out.println("Time Format: invalid hour "+hr);
   else
     System.out.println("Time Format: invalid hour "+hr);
   error = true; 
  }  
  if((mn<0)||(mn>59))
  {
   if(out!=null)
     out.println("Time Format: invalid minute "+mn);
   else
     System.out.println("Time Format: invalid minute "+mn);
   error = true;
  } 
  if((sc<0)||(sc>59))
  {
   if(out!=null)
     out.println("Time format: invalid second "+sc);
   else
     System.out.println("Time format: invalid second "+sc);
   error = true;
  }
  if(error)
    return false; 
  return true;
 }

/**
 * checkDigit's method is to check whether the string contains numerical digits.
 * It is flexible in checking due to the need to allow certain special character to 
 * be allowed in the checking. 
 * @param  String dt - contain digit to be check.
 *         boolean pureDigit - contain user request to check for pure digit/not
 *         PrintWriter out - contain ServletResponse to write out to servlet.
 * @return true if it is digits
 */
 public boolean checkDigit(String dt,boolean pureDigit,PrintWriter out)
 {
  if(dt == null)
    return false;
  int size = dt.length();
  for(int i=0;i<size;i++)
    if((!Character.isDigit(dt.charAt(i)))&&(!pureDigit))
      if((dt.charAt(i)!='.')&&(dt.charAt(i)!=',')&&(dt.charAt(i)!='-')&&(dt.charAt(i)!='/'))
      {
       if(out==null)
         System.out.println(dt+" di posisi["+i+"] bukan digit: "+dt.charAt(i));
       else
         out.println("<BR><B>"+dt+" di posisi["+i+"] bukan digit: "+dt.charAt(i));
       return false; 
      }
    else 
     return false;
  return true;  
 }

/**
 * checkInteger's method is to check the string for Integer value that does not
 * exceeded the min and the max values that is given by the user
 * @param String dt - contains string of integer to be checked
 *        int min - contains the minimum value 
 *        int max - contains the maximum value
 * @return boolean true if it meets the specification else return false
 */
 public boolean checkInteger(String dt,int min,int max)
 {
  if((dt==null)||(min<0)||(max<0))
   return false;
  if(!checkDigit(dt,true,null))
   return false;
  int len = dt.length();
  int val = 0;
  for(int i=0;i<len;i++)
  {
   val = Integer.parseInt(String.valueOf(dt.charAt(i)));
   if((val<min)||(val>max)) 
    return false;
  }
  return true; 
 }

/**
 * converge's method is to converge two vectors to become one vector if
 * both vectors present their own conditions meet.
 * @param Vector v1, v2 - contain DBEncapsulation that will be converge
 *        String c1, c2 - contain String conditions that have to be met
 * @return Vector v1 - contains the result of convergence
 */
 public Vector converge(Vector v1, Vector v2, String c1, String c2) throws SQLException
 {
  if((v1 == null)||(v2 == null)||(c1 == null)||(c2 == null))
   return null;
  if((c1.length()<=1)||(c2.length()<=1))
   return null;
  DBEncapsulation d1 = null;
  DBEncapsulation d2 = null;
  int size1 = v1.size();
  int size2 = v2.size();
  int p1 = 0;
  StringBuffer str1 = new StringBuffer();
  StringBuffer str2 = new StringBuffer();
  for(int i=0;i<size1;i++)
  {
   d1 = (DBEncapsulation)((DBEncapsulation)v1.get(i)).clone();
   str1.append(d1.getColumnStringValues(d1.getColumnNamePosition(c1)));
   do
   {
    d2 = (DBEncapsulation)((DBEncapsulation)v2.get(p1)).clone();
    if(str2.length()==0)
      str2.append(d2.getColumnStringValues(d2.getColumnNamePosition(c2)));
    if(str1.toString().compareToIgnoreCase(str2.toString())==0)
    {
     d1.mergeEncap(d2);
     p1++;
     d2.destroyAll();
    }
    else
    {
     d2.destroyAll();
     str2.delete(0,str2.length());
     break; 
    }
    str2.delete(0,str2.length());
   } while(p1<size2);
   str1.delete(0,str1.length());
   d1.destroyAll();
  }
  return v1;
 } // end of converge's method 

/**
 * prepareAirRoute's method is to prepare vector that contains all the info
 * about the airline route. The info consisted as the following order:
 * airline abbr,airline route, airline class, passanger type, and price.
 * It requires the calling method to pass in the db connection to the airline.
 * @param DBFill dbFill - contains connection to airline db
 * @return Vector v - if info existed, else return null
 */
 public Vector prepareAirRoute(DBFill db,Closure cls) throws SQLException
 {
  if(db == null)
  {
   cls.error(1,"dbFill"); 
   return null;
  }
  Vector v = null;
  Vector air = queryAirline(db,null); 
  if(air == null)
  {
   cls.error(5,"airline");
   return null;
  }
  Vector city = queryCity(db,null);
  if(city == null)
  {
   cls.error(0,"Tidak ada nama-nama kota di database");
   return null;
  }
  Vector r = new Vector();
  StringBuffer str = new StringBuffer("WHERE routeValid=\"Y\" ORDER BY routeAirlineID;");
  v = db.QuerySpecific("airlineRoute",str.toString(),null); 
  if(v == null)
  {
   cls.error(0,"Tidak ada rute penerbangan yang valid di database");
   return null;
  }
  int p1  = 0;
  int val = 0;
  int size= 0;
  DBEncapsulation t1 = null; // temp storage
  DBEncapsulation t2 = null; // temp storage 
  DBEncapsulation t3 = null; // temp storage
  Vector airClass = null;
  while(v.size()!=0)
  {
   t1 = (DBEncapsulation)v.remove(0);
   val = t1.getColumnIntegerValue(t1.getColumnNamePosition("routeKey")); 
   if(val<1)
    throw new SQLException("Invalid value of routeKey");
   p1 = t1.getColumnNamePosition("routeOrig");
   t2 = find(city,"cityKey",t1.getColumnStringValues(p1));
   if(t2==null)
    throw new SQLException("Query of the name of Origin City resulted in null");
   t1.replaceValue(p1,t2.getColumnStringValue(t2.getColumnNamePosition("cityAbbr")));
   p1 = t1.getColumnNamePosition("routeDest");
   t2.clearAll();
   t2 = find(city,"cityKey",t1.getColumnStringValues(p1));
   if(t2==null)
    throw new SQLException("Query of the name of Destination City resulted in null");
   t1.replaceValue(p1,t2.getColumnStringValue(t2.getColumnNamePosition("cityAbbr")));
   t2.destroyAll();
   p1 = t1.getColumnNamePosition("routeViaSN");
   if(t1.getColumnStringValue(p1).length()<=1)
    t1.removeNameVal(p1); 
   p1 = t1.getColumnNamePosition("routeDTKey");
   if(t1.getColumnIntegerValue(p1)>0)
   {
    str.append("WHERE routeDTKey=\""+t1.getColumnIntegerValue(p1)+"\";"); 
    t2 = db.QuerySingleSpecific("airlineRouteDT",str.toString(),null);
    if(t2 == null)
     throw new SQLException("Query to route Day and Time resulted in null");
    t1.mergeEncap((DBEncapsulation)t2.clone());
    t2.destroyAll();
    str.delete(0,str.length());
   }
   p1 = t1.getColumnNamePosition("routeMNote");
   if(t1.getColumnIntegerValue(p1)>0)
   {
    t2 = completeAirNotes(db,t1.getColumnIntegerValue(p1),"MEDIUM",cls); 
    if(t2 == null)
     throw new SQLException("Query to routeMNote resulted in null");
    t1.replaceValue(p1,t2.getColumnStringValue(t2.getColumnNamePosition("mediumNote")));
    t2.destroyAll();
   }
   str.append("WHERE classRouteKey=\""+val+"\";");
   airClass = db.QuerySpecific("airlineRouteClass",str.toString(),null);
   if(airClass == null)
    throw new SQLException("Query of airlineRouteClass resulted in null"); 
   size = airClass.size();
   str.delete(0,str.length());
   for(int i=0;i<size;i++)
   {
    t2 = (DBEncapsulation)((DBEncapsulation)airClass.get(i)).clone();
    p1 = t2.getColumnNamePosition("classMNote");
    t3 = completeAirNotes(db,t2.getColumnIntegerValue(p1),"MEDIUM",cls);
    if(t3 == null)
     throw new SQLException("Query of route medium note resulted in null");
    t2.replaceValue(p1,t3.getColumnStringValue(t3.getColumnNamePosition("mediumNote"))); 
    p1 = t2.getColumnNamePosition("classKey");
    t3.destroyAll();
    str.append("WHERE extraInfoRouteClassKey=\""+t2.getColumnIntegerValue(p1)+"\";");   
    t3 = db.QuerySingleSpecific("airlineExtraInfo",str.toString(),null);
    if(t3 == null)
     throw new SQLException("Query of airlineExtraInfo resulted in null");
    t2.mergeEncap((DBEncapsulation)t3.clone());
    t3.destroyAll();
    str.delete(0,str.length());
    str.append("WHERE classRulesRouteKey=\""+t2.getColumnIntegerValue(p1)+"\";");
    t3 = db.QuerySingleSpecific("airlineClassRules",str.toString(),null);
    if(t3 == null)
     throw new SQLException("Query airlineClassRules resulted in null");
    t2.mergeEncap((DBEncapsulation)t3.clone());
    t1.mergeEncap((DBEncapsulation)t2.clone());
    t3.destroyAll();
    t2.destroyAll();
    str.delete(0,str.length());
   } 
   t2 = find(air,"airlineID",t1.getColumnStringValues(
      t1.getColumnNamePosition("routeAirlineID")));
   t1.setColumnNameVal("airlineAbbr",t2.getColumnStringValue(
      t2.getColumnNamePosition("airlineAbbr")));
   r.addElement(t1.clone());
   converge(v,airClass,"routeKey","classRouteKey");
   t2.clearAll(); 
   t1.clearAll();
  }
  if(!air.isEmpty())
   air.removeAllElements();
  if(!airClass.isEmpty())
   airClass.removeAllElements();
  if(!v.isEmpty())
   v.removeAllElements();
  return r;
 } // end of prepareAirRoute's method


/**
 * completeAirNotes' method is to fetch the airline notes in the database
 * and return it in form of DBEncapsulation. It can fetch all 3 sizes of airline notes
 * given the predetermined string. If it does not find the notes, it returns null.
 * @param 
 * @return DBEncapsulation if found else return null
 */
 public DBEncapsulation completeAirNotes(DBFill db,int key,String mode,Closure cls)
  throws SQLException
 {
  if((db==null)||(mode==null))
  {
   cls.error(1,null);
   return null;
  }
  if(key<=0)
  {
   cls.error(0,"Key for database should not be zero or less");
   return null;
  }
  StringBuffer str = new StringBuffer();
  DBEncapsulation r = null;
  if(mode.compareToIgnoreCase("SMALL")==0)
  {
   str.append("WHERE airlineSmallNoteKey=\""+key+"\";");
   r = db.QuerySingleSpecific("airlineSmallNote",str.toString(),null);
   if(r == null)
    cls.error(0,"No airlineSmallNote found");
  }
  else if(mode.compareToIgnoreCase("MEDIUM")==0)
  {
   str.append("WHERE airlineSmallNoteKey=\""+key+"\";");
   r = db.QuerySingleSpecific("airlineMediumNote",str.toString(),null);
   if(r == null)
    cls.error(0,"No airlineMediumNote found");
  }
  else if(mode.compareToIgnoreCase("BIG")==0)
  {
   str.append("WHERE airlineSmallNoteKey=\""+key+"\";");
   r = db.QuerySingleSpecific("airlineBigNote",str.toString(),null);
   if(r == null)
    cls.error(0,"No airlineBigNote found");
  }
  else
   cls.error(0,"no available mode for these request");
  str.delete(0,str.length());
  return r;
 }


/**
 * calAirPAXPaid's method is to calculate the full price of the air fare.
 * @param double bf  - contains base fare
 *        double ppn - contains ppn value in double
 *        double iwjr- contains iwjr value in double 
 * @return double of full price of airticket
 */
 public double calAirPAXPaid(double bf,double ppn,double iwjr)
  throws SQLException
 {
  if((bf<=0.00)||(ppn>=1.00)||(iwjr<=0.00))
   throw new SQLException("calAirFullFare needs values for sell/ppn/iwjr");
  return (bf+(bf*ppn)+iwjr);
 }

/**
 * calAirPublishFare's method is to calculate airline publish fare given
 * the value of PAXPaid info, value of DT, and value of IWJR
 * @param double PAXPaid
 *        double DT
 *        double IWJR
 * @return double Airline Publish Fare
 */
 public double calAirPublishFare(double PAXPaid,double DT,double IWJR)
  throws SQLException
 {
  if((PAXPaid<=0.00)||(DT>=1.00)||(IWJR<=0.00))
   throw new SQLException("value of PAXPaid/DT/IWJR equal or less than zero");
  return ((PAXPaid-IWJR)/(1 - DT));
 }

/**
 * calAirBaseFare's method is to calculate airline base fare given
 * the value of publish fare and value of DT (Discount Tour)
 * @param double pf - contains publish fare value
 *        double dt - contains discount tour value (less than 1.00)
 * @return double base fare
 */
 public double calAirBaseFare(double pf,double dt) throws SQLException
 {
  if((pf<=0.00)||(dt>=1.00))
   throw new SQLException("wrong input value format of publish fare/DT");
  return (pf-(pf*dt));
 }

} // end of ATTSutility

