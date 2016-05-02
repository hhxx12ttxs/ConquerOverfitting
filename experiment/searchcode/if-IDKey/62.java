package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import persistence.comment.CommentDAO;
import persistence.comment.CommentPersistFactory;
import persistence.record.RecordDAO;
import persistence.record.RecordPersistFactory;
import persistence.stock.StockDAO;
import persistence.stock.StockPersistFactory;

/**
 * Catalogo de discos que se venden en la tienda.
 * Proporciona operaciones CRUD tanto de productos como de su stock disponible.
 * Tamibien proporciona la gestion de los comentarios de los productos, estos
 * inicialmente no se mapean de la base de datos hasta que no son requeridos,
 * y una vez leidos ya se almacenan en el mapa.
 * Destacar que las operaciones referentes con el stock se realizaran
 * directamente sobre la persistencia empleada, para evitar incoherencias en
 * caso de error y la aplicacion no pudiese cerrarse correctamente.
 * Ademas genera una lista de productos destacados y otra de novedades
 * 
 * @param catalog catalogo de discos de la tienda
 * @param comments mapa con los comentarios de los discos
 * @param featuredRecords array con los discos destacados(los que se van vendiendo)
 * @param newRecords array con las novedades
 * @param recordDAO gestor de la persistrencia de los discos
 * @param stockDAO gestor de la persistencia del stock
 * @param commentDAO gestor de la persistencia de los comentarios
 * @param lockOfConnection para modificaciones estructurales
 */
public class Catalog implements Serializable {
    private HashMap<UUID,Record> catalog = null;
    private HashMap<UUID,ArrayList<Comment>> comments = null;
    private Record[] featuredRecords = null;
    private Record[] newRecords = null;
    private RecordDAO recordDAO = null;
    private StockDAO stockDAO = null;
    private CommentDAO commentDAO = null;
    private final Object lockOfConnection = new Object();
    
    public Catalog() {
        catalog = new HashMap<UUID,Record>();
        comments = new HashMap<UUID,ArrayList<Comment>>();
        featuredRecords = new Record[10];
        newRecords = new Record[10];
    }
    
    /**
     * Devuelve el Mapa del catalogo
     * @return catalog
     */
    public HashMap<UUID,Record> getCatalog() {
        return catalog;
    }
    
    /**
     * Devuelve el Mapa de comentarios sobre los discos
     * @return catalog
     */
    public HashMap<UUID,ArrayList<Comment>> getComments() {
        return comments;
    }
    
    /**
     * Devuelve la lista de los discos destacados.
     * Estos son de los ultimos productos vendidos
     * @return featuredRecords
     */
    public Record[] getFeaturedRecords() {
        return featuredRecords;
    }
    
    /**
     * Devuelve la lista de los ultimos discos anhadidos
     * @return newRecords
     */
    public Record[] getNewRecords() {
        return newRecords;
    }
    
    /**
     * Inicializa el catalogo cargando todos los productos en el
     * @param persistenceMechanism mecanismo de persistencia empleado
     * @return true si hay exito, false en caso contrario
     */
    public boolean setUp(String persistenceMechanism) {
        recordDAO = RecordPersistFactory.getRecordDAO(persistenceMechanism);
        stockDAO = StockPersistFactory.getStockDAO(persistenceMechanism);
        commentDAO = CommentPersistFactory.getCommentDAO(persistenceMechanism);
        if(recordDAO == null || stockDAO == null || commentDAO == null) {
            return false;
        } else {
            this.catalog = (HashMap) recordDAO.getRecordMap();
            this.loadNewRecords();
            this.loadFeaturedRecords();           
        }
        return true;
    }
    
    /**
     * Carga los ultimos articulos agregados a la persistencia
     */
    private void loadNewRecords() {
        Record[] records = new Record[catalog.size()];
        int n = 0;
        for(UUID id : catalog.keySet()) {
            records[n] = catalog.get(id);
            n++;
        }
        int lastPos = records.length-1;
        for(int i=0;i<newRecords.length && lastPos>=0;lastPos--) {
            newRecords[i] = records[lastPos];
            i++;
        }
    }
    
    /**
     * En la carga inicial, para evitar cargar todos los comentarios
     * simplemente decimos que los articulos destacados son los nuevos
     */
    private void loadFeaturedRecords() {
        featuredRecords = newRecords;
    }
    
    /**
     * Inserta un disco en el catalogo y en la lista de novedades
     * @param record disco a insertar
     * @param stock existencias del disco
     * @return true si hay exito, false en caso contrario
     */
    public boolean addRecord(Record record, int stock) {
        if(recordDAO.createRecord(record)) {
            UUID recordId = record.getId();
            synchronized (lockOfConnection) {
                stockDAO.createStock(new Stock(recordId,stock));
                catalog.put(recordId, record);
            }
            addRecordToArray(newRecords,catalog.get(recordId));
            return true;
        }
        return false;
    }
    
    /**
     * Inserta un disco en el catalogo. Por defecto 0 de stock
     * @param record disco a insertar
     * @return true si hay exito, false en caso contrario
     */
    public boolean addRecord(Record record) {
        return addRecord(record,0);
    }
    
    /**
     * Devuelve un determinado disco
     * @param id identificador del disco
     * @return el disco elegido. null si no lo encuentra
     */
    public Record getRecord(UUID id) {
        Record record = null;
        record = catalog.get(id);
        return record;
    }
    
    /**
     * Devuelve un determinado disco
     * @param id identificador del disco
     * @return el disco elegido. null si no lo encuentra
     */
    public Record getRecord(String id) {
        return getRecord(UUID.fromString(id));
    }
    
    /**
     * Devuelve el stock de un articulo
     * @param id identificador del producto
     * @return stock del producto. -1 si no existe el producto
     */
    public int getStock(String id) {
        Stock stock = stockDAO.readStock(id);
        if(stock != null) {
            return stock.getStock();
        }
        return -1;            
    }
    
    /**
     * Devuelve el stock de un articulo
     * @param id identificador del producto
     * @return stock del producto. -1 si no existe el producto
     */
    public int getStock(UUID id) {
        return getStock(id.toString());         
    }
    
    /**
     * Actualiza el stock de un producto
     * @param id del producto
     * @param stock nuevo stock del producto
     * @return true si hay exito, false en caso contrario
     */
    public boolean setStock(String id, int stock) {
        if(getRecord(id) != null) {
            synchronized (lockOfConnection) {
                boolean stockSetted = stockDAO.updateStock(id, stock);
                if(stockSetted) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Actualiza el stock de un producto
     * @param id del producto
     * @param stock nuevo stock del producto
     * @return true si hay exito, false en caso contrario
     */
    public boolean setStock(UUID id, int stock) {
        return setStock(id.toString(),stock);
    }
    
    /**
     * Lista los discos que cumplen las caracteristicas solicitadas del catalogo
     * Dejando los campos en null, estos no se tendran en cuenta en la busqueda
     * @param name nombre del disco
     * @param artist artista del disco
     * @param recordLabel distribuidora del disco
     * @param type tipo musical del disco
     * @return ArrayList de Records. Vacio si no se encontraron coincidencias
     */
    public ArrayList<Record> searchRecord(String name, String artist,
            String recordLabel, String type) {
        ArrayList<Record> list = new ArrayList();
        for(UUID id : catalog.keySet()) {
            Record record = catalog.get(id);
            boolean match = true;
            if(name!=null && !record.getName().equalsIgnoreCase(name)) {
                match = false;
            }
            if(match && artist!=null && !record.getArtist().equalsIgnoreCase(artist)) {
                match = false;
            }
            if(match && recordLabel!=null && !record.getRecordLabel().equalsIgnoreCase(recordLabel)) {
                match = false;
            }
            if(match && type!=null && !record.getType().equalsIgnoreCase(type)) {
                match = false;
            }
            if(match) {
                list.add(record);
            }
        }
        return list;
    }
    
    /**
     * Discos y stock que cumplen las caracteristicas solicitadas del catalogo
     * Dejando los campos en null, estos no se tendran en cuenta en la busqueda
     * @param name nombre del disco
     * @param artist artista del disco
     * @param recordLabel distribuidora del disco
     * @param type tipo musical del disco
     * @return HashMap vacio si no se encontraron coincidencias
     */
    public HashMap<Record,Integer> searchRecordAndStock(String name, String artist,
            String recordLabel, String type) {
        HashMap<Record,Integer> map = new HashMap<Record,Integer>();
        ArrayList<Record> list = searchRecord(name,artist,recordLabel,type);
        for(Record record : list) {
            int stock = getStock(record.getId());
            map.put(record, stock);
        }
        return map;
    }
      
    /**
     * Actualiza un disco del catalogo y de las listas de destacados y nuevos
     * @param id del disco a actualizar
     * @param record nueva informacion del disco
     * @return true si hay exito, false en caso contrario
     */
    public boolean updateRecord(String id, Record record) {
        synchronized (lockOfConnection) {
            if(recordDAO.updateRecord(id, record)) {
                catalog.put(UUID.fromString(id), record);
                updateRecordFromArray(featuredRecords,catalog.get(UUID.fromString(id)));
                updateRecordFromArray(newRecords,catalog.get(UUID.fromString(id)));
                return true;
            }
        }
        return false;
    }
    
    /**
     * Actualiza un disco del catalogo
     * @param id del disco a actualizar
     * @param record nueva informacion del disco
     * @return true si hay exito, false en caso contrario
     */
    public boolean updateRecord(UUID id, Record record) {
        return updateRecord(id.toString(),record);
    }
    
    /**
     * Borra un disco del catalogo, así como su stock y de las listas
     * de destacados, novedades, y sus comentarios (si los tenia)
     * @param id del disco a borrar
     * @return true si hay exito, false en caso contrario
     */
    public boolean removeRecord(String id) {
        synchronized (lockOfConnection) {
            if(recordDAO.deleteRecord(id) && stockDAO.deleteStock(id)) {
                catalog.remove(UUID.fromString(id));
                removeRecordFromArray(featuredRecords,catalog.get(UUID.fromString(id)));
                removeRecordFromArray(newRecords,catalog.get(UUID.fromString(id)));
                for(Comment comment : getRecordComments(id)) {
                    removeComment(comment.getIdAsString());
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Borra un disco del catalogo, así como su stock
     * @param id del disco a borrar
     * @return true si hay exito, false en caso contrario
     */
    public boolean removeRecord(UUID id) {
        return removeRecord(id.toString());
    }  
    
    /**
     * Comprueba si hay existencias del producto
     * @param id producto
     * @return true si hay stock, false en caso contrario
     */
    public boolean isInStock(String id) {
        if(getStock(id) > 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Comprueba si hay existencias del producto
     * @param id producto
     * @return true si hay stock, false en caso contrario
     */
    public boolean isInStock(UUID id) {
        return isInStock(id.toString());
    }
    
    /**
     * Incrementa el stock del producto en la cantidad dada
     * @param id del producto a incrementar
     * @param increment numero de productos a incrementar
     * @return true si se ha realizado con exito, false en caso contrario
     */
    public boolean incrementStock(String id, int increment) {
        synchronized (lockOfConnection) {
            if(setStock(id,getStock(id)+increment)) {
                return true;
            }
        return false;
        }
    }
    
    /**
     * Incrementa el stock del producto en una unidad
     * @param id del producto a incrementar
     * @param increment numero de productos a incrementar
     * @return true si se ha realizado con exito, false en caso contrario
     */
    public boolean incrementStock(UUID id, int increment) {
        return incrementStock(id.toString(),increment);
    }
    
    /**
     * Decrementa el stock del producto en una unidad
     * @param id del producto a decrementar
     * @param decrement numero de productos a disminuir
     * @return true si se ha realizado con exito, false en caso contrario
     */
    public boolean decrementStock(String id, int decrement) {
        synchronized (lockOfConnection) {
            int stock = getStock(id);
            if(stock-decrement >= 0) {
                if(setStock(id,stock-decrement)) {
                    addRecordToArray(featuredRecords,catalog.get(UUID.fromString(id)));
                    return true;
                }
            }
        return false;
        }
    }
    
    /**
     * Decrementa el stock del producto en una unidad
     * @param id del producto a decrementar
     * @param decrement numero de productos a disminuir
     * @return true si se ha realizado con exito, false en caso contrario
     */
    public boolean decrementStock(UUID id, int decrement) {
        return decrementStock(id.toString(),decrement);
    }
    
    /**
     * Devuelve los comentarios asociados a un disco. Si no se encuentran
     * en el mapa de comentarios, se buscan en la base de datos y con estos
     * se actualiza el mapa de comentarios
     * @param id del disco
     * @return lista de comentarios del disco. Null si no hay ninguno
     */
    public ArrayList<Comment> getRecordComments(String id) {
        UUID idKey = UUID.fromString(id);
        if(catalog.containsKey(idKey)) {
            if(comments.containsKey(idKey)) {
                return comments.get(idKey);
            } else {
                ArrayList<Comment> list = commentDAO.listComment(id);
                comments.put(idKey, list);
                return list;
            }
        }
        return null;
    }
    
    /**
     * Obten un comentario directamente, dado su id
     * @param commentId
     * @return null si no se encuentra
     */
    public Comment getComment(String commentId) {
        return commentDAO.readComment(commentId);
    }
    
    /**
     * Agregar un comentario, se agregara tanto al mapa como a la persistencia
     * @param comment a agregar
     * @return true si hay exito, false en caso contrario
     */
    public boolean addComment(Comment comment) {
        UUID recordId = comment.getRecordId();
        if(catalog.containsKey(recordId) && commentDAO.createComment(comment)) {
            comments.put(recordId,commentDAO.listComment(recordId.toString()));
            return true;      
        }
        return false;
    }
    
    /**
     * Elimina un comentario determinado
     * @param id del comentario a eliminar
     * @return true si hay exito, false en caso contrario
     */
    public boolean removeComment(String id) {
        Comment comment = commentDAO.readComment(id);
        if(commentDAO.deleteComment(id)) {
            ArrayList<Comment> list = comments.get(comment.getRecordId());
            if(list != null)
                list.remove(comment);
            return true;
        }
        return false;
    }
    
    /**
     * Calcula el precio total de el mapa dados id del disco y cantidad del mismo
     * @param recordAndQuantity mapa con el disco y la cantidad
     * @return bigDecimal del total
     */
    public BigDecimal getTotalPrice(HashMap<UUID,Integer> recordAndQuantity) {
        BigDecimal updatedTotal = new BigDecimal("0");
        if(!recordAndQuantity.isEmpty()) {
            for(UUID recordId : recordAndQuantity.keySet()) {
                Record record = catalog.get(recordId);
                BigDecimal recordTotalPrice = record.getPrice();
                BigDecimal quantity = new BigDecimal(recordAndQuantity.get(recordId));
                recordTotalPrice = recordTotalPrice.multiply(quantity); 
                updatedTotal = updatedTotal.add(recordTotalPrice);
            }
        }
        return updatedTotal;
    }
    
    /**
     * Calcula el precio total de el mapa dados id del disco y cantidad del mismo
     * @param recordAndQuantity mapa con el disco y la cantidad
     * @return String formateado de la forma: 34,99 €
     */
    public String getTotalPriceAsFormatedString(HashMap<UUID,Integer> recordAndQuantity) {
        NumberFormat n = NumberFormat.getCurrencyInstance(Locale.FRANCE); 
        double doublePrice = getTotalPrice(recordAndQuantity).doubleValue();
        return n.format(doublePrice);
    }
           
    /**
     * Anhade el disco al  array indicado si este no existia previamente.
     * su informacion por la proporcionada.
     * @param list array donde insertar el disco
     * @param record disco a insertar 
     */
    private void addRecordToArray(Record[] list,Record record) {
        boolean found = false;
        for(int i=0;i<list.length && !found;i++) {
            if(list[i] != null && list[i].equals(record))
                found = true;
        }
        if(!found) {
            Record buff = null;
            Record old = list[0];
            for(int i=0;i+1<list.length;i++) {
                buff = list[i+1];
                list[i+1] = old;
                old = buff;             
            }
            list[0] = record; 
        }       
    }
    
    /**
     * Elimina el disco si este se encontraba previamente en el array
     * @param list array de donde borrar el disco
     * @param record disco a eliminar
     */
    private void removeRecordFromArray(Record[] list,Record record) {
        int found = -1;
        for(int i=0;i<list.length;i++) {
            if(list[i].equals(record)) {
                list[i] = null;
                found = i;
            }
        }
        if(found != -1) {
            for(int i=found+1;i<list.length;i++) {
                list[i-1] = list[i];
            }
        }
    }
    
    /**
     * Actualiza la informacion del disco si se encuentra en la lista
     * su informacion por la proporcionada.
     * @param list array donde actualizar el disco
     * @param record disco a actualizar
     */
    private void updateRecordFromArray(Record[] list,Record record) {
        boolean found = false;
        for(int i=0;i<list.length && !found;i++) {
            if(list[i] != null && list[i].equals(record)) {
                list[i] = record;
                found = true;
            }
        }      
    }

}
