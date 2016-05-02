package persistence.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import model.User;
import org.apache.log4j.Logger;

/**
 * Implementacion de UserDAO para persistir la informacion en ficheros
 * 
 * @param userMap mapa de usuarios para operar sobre el
 * @param fileName fichero donde se persistira la informacion
 * @param userPersistenceManager UserDAO de ficheros
 * @param logger para generar las trazas
 */
public class UserDAOFileImplementation implements UserDAO{
    private Map<UUID, User> userMap = new HashMap<UUID, User>();
    private String fileName = "";
    private static UserDAOFileImplementation userPersistenceManager = null;
    private static final Logger logger = Logger.getLogger(UserDAOFileImplementation.class.getName());

    private UserDAOFileImplementation() {
    }

    public static UserDAO getUserDAOFileImplementation() {
        if (userPersistenceManager == null) {
            userPersistenceManager = new UserDAOFileImplementation();
        }
        return userPersistenceManager;
    }

    @Override
    public synchronized boolean createUser(User user) {
        if (!userMap.containsKey(user.getId())) {
            for(UUID id : userMap.keySet()) {
                if(userMap.get(id).getNick().equals(user.getNick()))
                    return false;
            }
            userMap.put(user.getId(), user);
            return true;
        }
        return false;
    }

    @Override
    public synchronized User readUser(String id) {
        UUID idKey = UUID.fromString(id);
        return userMap.get(idKey);
    }
    
    @Override
    public synchronized User readUserByNick(String nick) {
        if(nick != null) {
            for(UUID id : userMap.keySet()) {
                User user = userMap.get(id);
                if(user.getNick().equals(nick))
                    return user;
            }
        }
        return null;
    }

    @Override
    public synchronized ArrayList<User> listUser(String nick, String firstName,
            String lastName, String address, String email, int day, int month, int year) {
        ArrayList<User> list = new ArrayList();
        for(UUID id : userMap.keySet()) {
            User user = userMap.get(id);
            boolean match = true;
            if(nick!=null && !user.getNick().equalsIgnoreCase(nick)) {
                match = false;
            }
            if(match && firstName!=null && !user.getFirstName().equalsIgnoreCase(firstName)) {
                match = false;
            }
            if(match && lastName!=null && !user.getLastName().equalsIgnoreCase(lastName)) {
                match = false;
            }
            if(match && address!=null && !user.getAddress().equalsIgnoreCase(address)) {
                match = false;
            }
            if(match && email!=null && !user.getEmail().equalsIgnoreCase(email)) {
                match = false;
            }
            Calendar birthDate = Calendar.getInstance();
            birthDate.clear();
            birthDate.set(year, month-1, day);
            if(match && (day!=0 && month!=0 && year!=0) && !user.getBirthDate().equals(birthDate)) {
                match = false;
            }
            if(match) {
                list.add(user);
            }
        }
        return list;
    }

    @Override
    public synchronized boolean updateUser(String id, User user) {
        UUID idKey = UUID.fromString(id);
        if (!userMap.containsKey(idKey)) {
            return false;
        } else {
            userMap.put(user.getId(), user);
            return true;
        }
    }

    @Override
    public synchronized boolean deleteUser(String id) {
        UUID idKey = UUID.fromString(id);
        if (!userMap.containsKey(idKey)) {
            return false;
        } else {
            userMap.remove(idKey);
            return true;
        }
    }

    @Override
    public boolean setUp(String url, String driver, String user, String password) {
        int urlIndex = url.lastIndexOf("/");
        if(urlIndex!=-1 && !url.endsWith("/"))
            this.fileName = this.fileName.concat(url.substring(url.lastIndexOf("/")+1));
        this.fileName = this.fileName.concat("USERS");
        try {
            File file = new File(fileName);
            logger.trace("Empleando el fichero con ruta: "+file.getCanonicalPath());
            if(!file.exists()) {
                return true;
            }
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            int numberOfUsers = (Integer) objectInputStream.readObject();
            for(int i=0; i<numberOfUsers; i++) {
                User client = (User) objectInputStream.readObject();
                userMap.put(client.getId(), client);
            }
            objectInputStream.close();
        } catch(Exception ex) {
            logger.error("No se pudieron cargar los Usuarios del archivo",ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean disconnect() {
        try {
            OutputStream outputStream = new FileOutputStream(new File(fileName));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(userMap.size());
            for(User user : userMap.values()) {
                objectOutputStream.writeObject(user);
            }
            objectOutputStream.close();
        } catch (IOException ex) {
            logger.error("No se pudieron guardar los Usuarios en el archivo",ex);
            return false;
        }
        return true;
    }

}

