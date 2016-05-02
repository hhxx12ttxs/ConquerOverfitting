
package Dao;

import Javabeans.diagnostico;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import utilitarios.ConexionBd;
import utilitarios.Helpers;
import utilitarios.Data;


public class diagnosticoDAO extends ConexionBd{
    
    private diagnostico diag;
    private Data dt;
    private String _table= "diagnostico";
    private PreparedStatement  pt = null;
    private ResultSet rs = null;
    private DefaultTableModel datos; 
    private String _error = "diagnosticoDAO_";
    private Helpers hp;
    private Statement s = null;
    
   public void getTableAll(JTable tblDatos , JLabel lblcant, String idpac){
        try{
            datos = new DefaultTableModel();
            hp = new Helpers();
            dt = new Data();
            Object[] fila; 
            String Table = this._table;
            getConexion();
            s = conexion.createStatement();
            String qs = "select iddiag,tipo,descripcion,costo from "+Table;
            if (!"".equals(idpac)) {
                qs = qs + " where idpac = '"+idpac+"'";
            }
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
            lblcant.setText(String .valueOf(num));
            rs.close();
            closeConexion(); 
        }
        catch(Exception e){
            System.out.println(_error + "getTableAll: "+e);
        }
    }

    public int save(int idpac, int tipo, String descripcion, double costo) {
        int i=0;
        try{
            Date date = new Date(0000-00-00);
            hp = new Helpers();
            getConexion();
            String Table = this._table;
            String created = hp.getDateNow();
            diag = new diagnostico(0, idpac, tipo, descripcion, costo, created);
            String query= "insert into "+Table+"(idpac,tipo,descripcion,costo,created) values(?,?,?,?,?)";
            pt  = conexion.prepareStatement(query);
 
            pt.setInt(1,diag.getIdpac());
            pt.setInt(2,diag.getTipo());
            pt.setString(3,diag.getDescripcion());
            pt.setDouble(4,diag.getCosto());
            pt.setDate(5,date.valueOf(diag.getCreated()));
            i= pt.executeUpdate();
            pt.close();
            closeConexion();
        }
        catch(Exception e){
            System.out.println(_error + "save: "+e);
        }
        
        return i;
    }
    public int delete(int iddiag, int idpac) {
        int i=0;
        try{
            hp = new Helpers();
            getConexion();
            String Table = this._table;
            diag = new diagnostico();
            diag.setIdpac(idpac);
            diag.setIddiag(iddiag);
            String query= "delete from "+Table+" where iddiag=? and idpac=?";
            System.out.println("diag: "+diag.getIddiag()+" pac: "+diag.getIdpac());
            pt  = conexion.prepareStatement(query);
 
            pt.setInt(1,diag.getIddiag());
            pt.setInt(2,diag.getIdpac());
            i= pt.executeUpdate();
            pt.close();
            closeConexion();
        }
        catch(Exception e){
            System.out.println(_error + "delete: "+e);
        }
        
        return i;
    }
    
}

