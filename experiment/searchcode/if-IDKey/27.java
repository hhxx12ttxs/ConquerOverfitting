package persistence.stock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import model.Stock;
import org.apache.log4j.Logger;

/**
 * Implementacion de StockDAO para persistir la informacion en ficheros
 * 
 * @param saleMap mapa de stocks para operar sobre el
 * @param fileName fichero donde se persistira la informacion
 * @param stockPersistenceManager StockDAO de ficheros
 * @param logger para generar las trazas
 */
public class StockDAOFileImplementation implements StockDAO{
    private Map<UUID, Stock> stockMap = new HashMap<UUID, Stock>();
    private String fileName = "";
    private static StockDAOFileImplementation stockPersistenceManager = null;
    private static final Logger logger = Logger.getLogger(StockDAOFileImplementation.class.getName());

    private StockDAOFileImplementation() {
    }

    public static StockDAO getStockDAOFileImplementation() {
        if (stockPersistenceManager == null) {
            stockPersistenceManager = new StockDAOFileImplementation();
        }
        return stockPersistenceManager;
    }

    @Override
    public synchronized boolean createStock(Stock stock) {
        if (stockMap.containsKey(stock.getRecordId())) {
            return false;
        } else {
            stockMap.put(stock.getRecordId(), stock);
            return true;
        }
    }

    @Override
    public synchronized Stock readStock(String recordId) {
        UUID idKey = UUID.fromString(recordId);
        return stockMap.get(idKey);
    }

    @Override
    public synchronized boolean updateStock(String recordId, int stock) {
        UUID idKey = UUID.fromString(recordId);
        if (!stockMap.containsKey(idKey)) {
            return false;
        } else {
            
            stockMap.put(idKey, new Stock(idKey,stock));
            return true;
        }
    }

    @Override
    public synchronized boolean deleteStock(String recordId) {
        UUID idKey = UUID.fromString(recordId);
        if (!stockMap.containsKey(idKey)) {
            return false;
        } else {
            stockMap.remove(idKey);
            return true;
        }
    }

    @Override
    public boolean setUp(String url, String driver, String user, String password) {
        int urlIndex = url.lastIndexOf("/");
        if(urlIndex!=-1 && !url.endsWith("/"))
            this.fileName = this.fileName.concat(url.substring(url.lastIndexOf("/")+1));
        this.fileName = this.fileName.concat("STOCKS");
        try {
            File file = new File(fileName);
            logger.trace("Empleando el fichero con ruta: "+file.getCanonicalPath());
            if(!file.exists()) {
                return true;
            }
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            int numberOfStocks = (Integer) objectInputStream.readObject();
            for(int i=0; i<numberOfStocks; i++) {
                Stock stock = (Stock) objectInputStream.readObject();
                stockMap.put(stock.getRecordId(), stock);
            }
            objectInputStream.close();
        } catch(Exception ex) {
            logger.error("No se pudieron cargar los stocks del archivo",ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean disconnect() {
        try {
            OutputStream outputStream = new FileOutputStream(new File(fileName));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(stockMap.size());
            for(Stock stock : stockMap.values()) {
                objectOutputStream.writeObject(stock);
            }
            objectOutputStream.close();
        } catch (IOException ex) {
            logger.error("No se pudieron guardar los stocks en el archivo",ex);
            return false;
        }
        return true;
    }

}

