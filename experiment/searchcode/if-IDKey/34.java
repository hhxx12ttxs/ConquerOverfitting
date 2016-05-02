package persistence.comment;

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
import model.Comment;
import org.apache.log4j.Logger;

/**
 * Implementacion de CommentDAO para persistir la informacion en ficheros
 * 
 * @param commentMap mapa de comentarios para operar sobre el
 * @param fileName fichero donde se persistira la informacion
 * @param commentPersistenceManager CommentDAO de ficheros
 * @param logger para generar las trazas
 */
public class CommentDAOFileImplementation implements CommentDAO {
    private Map<UUID, Comment> commentMap = new HashMap<UUID, Comment>();
    private String fileName = "";
    private static CommentDAOFileImplementation commentPersistenceManager = null;
    private static final Logger logger = Logger.getLogger(CommentDAOFileImplementation.class.getName());

    private CommentDAOFileImplementation() {
    }

    public static CommentDAO getCommentDAOFileImplementation() {
        if (commentPersistenceManager == null) {
            commentPersistenceManager = new CommentDAOFileImplementation();
        }
        return commentPersistenceManager;
    }

    @Override
    public synchronized boolean createComment(Comment comment) {
        if (commentMap.containsKey(comment.getId())) {
            return false;
        } else {
            commentMap.put(comment.getId(), comment);
            return true;
        }
    }

    @Override
    public synchronized Comment readComment(String id) {
        UUID idKey = UUID.fromString(id);
        return commentMap.get(idKey);
    }

    @Override
    public synchronized ArrayList<Comment> listComment(String recordId) {
        ArrayList<Comment> list = new ArrayList();
        for(UUID id : commentMap.keySet()) {
            Comment comment = commentMap.get(id);
            if(comment.getRecordIdAsString().equals(recordId)) {
                list.add(comment);
            }
        }
        return list;
    }

    @Override
    public synchronized boolean updateComment(String id, Comment comment) {
        UUID idKey = UUID.fromString(id);
        if (!commentMap.containsKey(idKey)) {
            return false;
        } else {
            commentMap.put(comment.getId(), comment);
            return true;
        }
    }

    @Override
    public synchronized boolean deleteComment(String id) {
        UUID idKey = UUID.fromString(id);
        if (!commentMap.containsKey(idKey)) {
            return false;
        } else {
            commentMap.remove(idKey);
            return true;
        }
    }

    @Override
    public boolean setUp(String url, String driver, String user, String password) {
        int urlIndex = url.lastIndexOf("/");
        if(urlIndex!=-1 && !url.endsWith("/"))
            this.fileName = this.fileName.concat(url.substring(url.lastIndexOf("/")+1));
        this.fileName = this.fileName.concat("COMENTS");
        try {
            File file = new File(fileName);
            logger.trace("Empleando el fichero con ruta: "+file.getCanonicalPath());
            if(!file.exists()) {
                return true;
            }
            InputStream inputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            int numberOfComments = (Integer) objectInputStream.readObject();
            for(int i=0; i<numberOfComments; i++) {
                Comment comment = (Comment) objectInputStream.readObject();
                commentMap.put(comment.getId(), comment);
            }
            objectInputStream.close();
        } catch(Exception ex) {
            logger.error("No se pudieron cargar los comentarios del archivo",ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean disconnect() {
        try {
            OutputStream outputStream = new FileOutputStream(new File(fileName));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(commentMap.size());
            for(Comment comment : commentMap.values()) {
                objectOutputStream.writeObject(comment);
            }
            objectOutputStream.close();
        } catch (IOException ex) {
            logger.error("No se pudieron guardar los comentarios en el archivo",ex);
            return false;
        }
        return true;
    }

}

