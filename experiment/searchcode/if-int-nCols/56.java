
package Dao;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import utilitarios.ConexionBd;
import Javabeans.paciente; 
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import utilitarios.Helpers;
import utilitarios.Data;

public class pacientesDAO extends ConexionBd {
   
    private paciente pac;
    private String _table= "paciente";
    private PreparedStatement  pt = null;
    private ResultSet rs = null;
    private DefaultTableModel datos; 
    private String _error = "pacientesDAO_";
    private Helpers hp;
    private Data dt;
    private Statement s = null;
    private Connection conn = null;
    private JasperPrint jasperPrint;
    private JasperViewer jviewer;
    private JasperReport masterReport= null;
    private Map parametro;
    
   public void getTableAll(JTable tblDatos , JLabel lblcant, String nombre){
        try{
            datos = new DefaultTableModel();
            hp = new Helpers();
            Object[] fila; 
            String Table = this._table;
            getConexion();
            s = conexion.createStatement();
            String qs = "select * from "+Table;
            if (!"".equals(nombre)) {
                qs = qs + " where nombre like '%"+nombre+"%'";
            }
            rs = s.executeQuery(qs);
            ResultSetMetaData meta = rs.getMetaData();
            int nCols = meta.getColumnCount();
            fila = new Object[nCols];
            String[] colum = new String[nCols];
            for (int i=0; i<nCols; ++i) {
                datos.addColumn(meta.getColumnName(i+1));
                colum[i]=meta.getColumnName(i+1);
                System.out.println(colum[i]);

            }
            while(rs.next()){
                for(int i=0; i<nCols; ++i){   
                    fila[i] = rs.getObject(i+1);
                }
                datos.addRow(fila);
            }
            
            tblDatos.setModel(datos);
            int num = tblDatos.getRowCount();
            lblcant.setText(String .valueOf(num));
            rs.close();
            closeConexion(); 
        }
        catch(Exception e){
            System.out.println(_error + "getTableAll: "+e);
        }
    }
   public paciente getValues(int idpac){
       pac =  new paciente();
        try{
            //Preparando
            getConexion();
            String Table = this._table;
            String campos[] = new String[6];
            String query= "select * from "+Table+" where idpac = "+idpac+"";
            s = conexion.createStatement();
            rs = s.executeQuery(query);
            ResultSetMetaData meta = rs.getMetaData();
            int nCols = meta.getColumnCount();
            campos = new String[nCols+1];

            rs.next();
            for(int i=1;i<=nCols;i++){
                 campos[i]=rs.getString(i);
            }
            rs.close();

            pac.setNombre(campos[2]);
            pac.setApellido(campos[3]);
            pac.setTelefono(campos[4]);
            pac.setDireccion(campos[5]);
            if("t".equals(campos[6])){
                campos[6] = "true";
            }
            pac.setEstado(Boolean.parseBoolean(campos[6]));
            
        }
        catch(Exception e){
            System.out.println(_error + "getValues: "+e);
        }
        closeConexion();
        return pac;
    }
    public int save(String nombre, String apellido, String telefono, String direccion, boolean estado) {
        int i=0;
        try{
            
            int cant = 0;
            Date date = new Date(0000-00-00);
            hp = new Helpers();
            getConexion();
            System.out.println("1");
            String Table = this._table;
            String created = hp.getDateNow();
            pac = new paciente(0, nombre, apellido, telefono, direccion, estado, created);
            String query= "insert into "+Table+"(nombre,apellido,telefono,direccion,estado,created) values(?,?,?,?,?,?)";
            pt  = conexion.prepareStatement(query);

            pt.setString(1,pac.getNombre());
            pt.setString(2,pac.getApellido());
            pt.setString(3,pac.getTelefono());
            pt.setString(4,pac.getDireccion());
            pt.setBoolean(5,pac.isEstado());
            pt.setDate(6,date.valueOf(pac.getCreated()));
            i= pt.executeUpdate();
            pt.close();
            closeConexion();
        }
        catch(Exception e){
            System.out.println(_error + "save: "+e);
        }
        
        return i;
    }
    public int update(int idpac, String nombre, String apellido, String telefono, String direccion, boolean estado) {
        int i=0;
        try{
            Date date = new Date(0000-00-00);
            hp = new Helpers();
            getConexion();
            String Table = this._table;
            String created = hp.getDateNow();
            pac = new paciente(idpac, nombre, apellido, telefono, direccion, estado, created);
            String query= "update "+Table+" set nombre=?,apellido=?,telefono=?,direccion=?,estado=? where idpac=?";
            pt  = conexion.prepareStatement(query);
 
            pt.setString(1,pac.getNombre());
            pt.setString(2,pac.getApellido());
            pt.setString(3,pac.getTelefono());
            pt.setString(4,pac.getDireccion());
            pt.setBoolean(5,pac.isEstado());
            pt.setInt(6,pac.getIdpac());
            i= pt.executeUpdate();
            pt.close();
            closeConexion();
        }
        catch(Exception e){
            System.out.println(_error + "update: "+e);
        }
        
        return i;
    }
    public int delete(int idpac) {
        int i=0;
        try{
            hp = new Helpers();
            getConexion();
            String Table = this._table;
            pac = new paciente();
            pac.setIdpac(idpac);
            String query= "delete from "+Table+" where idpac=?";
            pt  = conexion.prepareStatement(query);
 
            pt.setInt(1,pac.getIdpac());
            i= pt.executeUpdate();
            pt.close();
            closeConexion();
        }
        catch(Exception e){
            System.out.println(_error + "delete: "+e);
        }
        
        return i;
    }
   public void getTableAllPagos(JTable tblDatos , JLabel lblcant){
        try{
            datos = new DefaultTableModel();
            hp = new Helpers();
            dt =  new Data();
            Object[] fila; 
            String Table = this._table;
            getConexion();
            s = conexion.createStatement();
            String qs = "select d.created, p.nombre,p.apellido, d.tipo, d.costo "
                    + "from diagnostico d, paciente p "
                    + "where p.idpac = d.idpac";
            rs = s.executeQuery(qs);
            ResultSetMetaData meta = rs.getMetaData();
            int nCols = meta.getColumnCount();
            fila = new Object[nCols];
            String[] colum = new String[nCols];
            int tipo = -1;
            for (int i=0; i<nCols; ++i) {
                datos.addColumn(meta.getColumnName(i+1));
                colum[i]=meta.getColumnName(i+1);
                if("tipo".equals(colum[i])){
                    tipo = i;
                }
            }
            while(rs.next()){
                for(int i=0; i<nCols; ++i){
                    if(i==tipo){
                        fila[i] = dt.G_TYPE[rs.getInt(i+1)];
                    } else {
                        fila[i] = rs.getObject(i+1);
                    }
                }
                datos.addRow(fila);
            }
            
            tblDatos.setModel(datos);
            int num = tblDatos.getRowCount();
            
            int sum = 0;
            s = conexion.createStatement();
            qs = "select sum(costo) from diagnostico ";
            rs = s.executeQuery(qs);
            while(rs.next()){ 
               sum = rs.getInt(1);
            }
            System.out.println("Suma: "+sum);
            
            lblcant.setText(String .valueOf(sum));
            rs.close();
            closeConexion(); 
        }
        catch(Exception e){
            System.out.println(_error + "getTableAll: "+e);
        }
    }
   public void getReport(int idpac){
       try{
            getConexion();

            int sum = 0;
            s = conexion.createStatement();
            String qs = "select sum(costo) from diagnostico where idpac= "+idpac;
            rs = s.executeQuery(qs);
            while(rs.next()){ 
                sum = rs.getInt(1);
             }

            conn = getConetion();
            File archivo = new  File("reportes/report.jasper");
            //crear reporte
            System.out.println("Cargando desde: " + archivo);
            if(archivo == null){
                System.out.println("No se encuentra el archivo.");
            }
            JasperReport masterReport= null;
            try {
                masterReport= (JasperReport) JRLoader.loadObject(archivo);
            } catch (JRException e) {
                System.out.println("Error cargando el reporte maestro: " + e.getMessage());
            }
            Map parametro= new HashMap();
            parametro.put("idpac",idpac);
            parametro.put("total",sum);

            JasperPrint jasperPrint= JasperFillManager.fillReport(masterReport,parametro,conn);
            JasperViewer jviewer= new JasperViewer(jasperPrint,false);
            jviewer.setTitle("Asistencia Personal");
            jviewer.setVisible(true);
            closeConexion();
        } catch (Exception j) {
            System.out.println("Mensaje de Error:"+j);
        }
   }
}

