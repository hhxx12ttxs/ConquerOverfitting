package persistence.record;

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
import model.Record;
import org.apache.log4j.Logger;

/**
 * Implementacion de RecordDAO para persistir la informacion en ficheros
 * 
 * @param recordMap mapa de discos para operar sobre el
 * @param fileName fichero donde se persistira la informacion
 * @param recordPersistenceManager RecordDAO de ficheros
 * @param logger para generar las trazas
 */
public class RecordDAOFileImplementation implements RecordDAO {
    private Map<UUID, Record> recordMap = new HashMap<UUID, Record>();
    private String fileName = "";
    private static RecordDAOFileImplementation recordPersistenceManager = null;
    private static final Logger logger = Logger.getLogger(RecordDAOFileImplementation.class.getName());

    private RecordDAOFileImplementation() {
    }

    public static RecordDAO getRecordDAOFileImplementation() {
        if (recordPersistenceManager == null) {
            recordPersistenceManager = new RecordDAOFileImplementation();
        }
        return recordPersistenceManager;
    }

    @Override
    public synchronized boolean createRecord(Record record) {
        if (recordMap.containsKey(record.getId())) {
            return false;
        } else {
            recordMap.put(record.getId(), record);
            return true;
        }
    }

    @Override
    public synchronized Record readRecord(String id) {
        UUID idKey = UUID.fromString(id);
        return recordMap.get(idKey);
    }

    @Override
    public synchronized ArrayList<Record> listRecord(String name, String artist,
            String recordLabel, String type) {
        ArrayList<Record> list = new ArrayList();
        for(UUID id : recordMap.keySet()) {
            Record record = recordMap.get(id);
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
            if(match && type!=null && !record.getRecordLabel().equalsIgnoreCase(type)) {
                match = false;
            }
            if(match) {
                list.add(record);
            }
        }
        return list;
    }

    @Override
    public synchronized boolean updateRecord(String id, Record record) {
        UUID idKey = UUID.fromString(id);
        if (!recordMap.containsKey(idKey)) {
            return false;
        } else {
            recordMap.put(record.getId(), record);
            return true;
        }
    }

    @Override
    public synchronized boolean deleteRecord(String id) {
        UUID idKey = UUID.fromString(id);
        if (!recordMap.containsKey(idKey)) {
            return false;
        } else {
            recordMap.remove(idKey);
            return true;
        }
    }
    
    @Override
    public Map<UUID,Record> getRecordMap() {
        return recordMap;
    }

    @Override
    public boolean setUp(String url, String driver, String user, String password) {
        int urlIndex = url.lastIndexOf("/");
        if(urlIndex!=-1 && !url.endsWith("/"))
            this.fileName = this.fileName.concat(url.substring(url.lastIndexOf("/")+1));
        this.fileName = this.fileName.concat("RECORDS");
        try {
            File file = new File(fileName);
            logger.trace("Empleando el fichero con ruta: "+file.getCanonicalPath());
            if(!file.exists()) {
                return true;
            }
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            int numberOfRecords = (Integer) objectInputStream.readObject();
            for(int i=0; i<numberOfRecords; i++) {
                Record record = (Record) objectInputStream.readObject();
                recordMap.put(record.getId(), record);
            }
            objectInputStream.close();
        } catch(Exception ex) {
            logger.error("No se pudieron cargar los Records del archivo",ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean disconnect() {
        try {
            OutputStream outputStream = new FileOutputStream(new File(fileName));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(recordMap.size());
            for(Record record : recordMap.values()) {
                objectOutputStream.writeObject(record);
            }
            objectOutputStream.close();
        } catch (IOException ex) {
            logger.error("No se pudieron guardar los Records en el archivo",ex);
            return false;
        }
        return true;
    }
}
