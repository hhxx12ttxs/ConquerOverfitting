package persistence.sale;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import model.Sale;
import org.apache.log4j.Logger;

/**
 * Implementacion de SaleDAO para persistir la informacion en ficheros
 * 
 * @param saleMap mapa de ventas para operar sobre el
 * @param fileName fichero donde se persistira la informacion
 * @param salePersistenceManager SaleDAO de ficheros
 * @param logger para generar las trazas
 */
public class SaleDAOFileImplementation implements SaleDAO{
    private Map<UUID, Sale> saleMap = new HashMap<UUID, Sale>();
    private String fileName = "";
    private static SaleDAOFileImplementation salePersistenceManager = null;
    private static final Logger logger = Logger.getLogger(SaleDAOFileImplementation.class.getName());

    private SaleDAOFileImplementation() {
    }

    public static SaleDAO getSaleDAOFileImplementation() {
        if (salePersistenceManager == null) {
            salePersistenceManager = new SaleDAOFileImplementation();
        }
        return salePersistenceManager;
    }

    @Override
    public synchronized boolean createSale(Sale sale) {
        if (saleMap.containsKey(sale.getId())) {
            return false;
        } else {
            saleMap.put(sale.getId(), sale);
            return true;
        }
    }

    @Override
    public synchronized Sale readSale(String id) {
        UUID idKey = UUID.fromString(id);
        return saleMap.get(idKey);
    }

    @Override
    public synchronized ArrayList<Sale> listSale(String customerId, String customerName,
            String address, String paymentForm, String cartFootprint, String recordId) {
        ArrayList<Sale> list = new ArrayList();
        for(UUID id : saleMap.keySet()) {
            Sale sale = saleMap.get(id);
            boolean match = true;
            if(customerId!=null && !sale.getCustomerIdAsString().equals(customerId)) {
                match = false;
            }
            if(customerName!=null && !sale.getCustomerName().equalsIgnoreCase(customerName)) {
                match = false;
            }
            if(address!=null && !sale.getAddress().equalsIgnoreCase(address)) {
                match = false;
            }
            if(paymentForm!=null && !sale.getPaymentForm().equalsIgnoreCase(paymentForm)) {
                match = false;
            }
            if(cartFootprint!=null && !sale.getItems().getFootprint().equals(cartFootprint)) {
                match = false;
            }
            if(recordId!=null && !sale.getItems().getFootprint().contains(recordId)) {
                match = false;
            }
            if(match) {
                list.add(sale);
            }
        }
        return list;
    }

    @Override
    public synchronized boolean updateSale(String id, Sale sale) {
        UUID idKey = UUID.fromString(id);
        if (!saleMap.containsKey(idKey)) {
            return false;
        } else {
            saleMap.put(sale.getId(), sale);
            return true;
        }
    }

    @Override
    public synchronized boolean deleteSale(String id) {
        UUID idKey = UUID.fromString(id);
        if (!saleMap.containsKey(idKey)) {
            return false;
        } else {
            saleMap.remove(idKey);
            return true;
        }
    }

    @Override
    public boolean setUp(String url, String driver, String user, String password) {
        int urlIndex = url.lastIndexOf("/");
        if(urlIndex!=-1 && !url.endsWith("/"))
            this.fileName = this.fileName.concat(url.substring(url.lastIndexOf("/")+1));
        this.fileName = this.fileName.concat("SALES");
        try {
            File file = new File(fileName);
            logger.trace("Empleando el fichero con ruta: "+file.getCanonicalPath());
            if(!file.exists()) {
                return true;
            }
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            int numberOfSales = (Integer) objectInputStream.readObject();
            for(int i=0; i<numberOfSales; i++) {
                Sale sale = (Sale) objectInputStream.readObject();
                saleMap.put(sale.getId(), sale);
            }
            objectInputStream.close();
        } catch(Exception ex) {
            logger.error("No se pudieron cargar las Ventas del archivo",ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean disconnect() {
        try {
            OutputStream outputStream = new FileOutputStream(new File(fileName));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(saleMap.size());
            for(Sale sale : saleMap.values()) {
                objectOutputStream.writeObject(sale);
            }
            objectOutputStream.close();
        } catch (IOException ex) {
            logger.error("No se pudieron guardar las Ventas en el archivo",ex);
            return false;
        }
        return true;
    }

}

