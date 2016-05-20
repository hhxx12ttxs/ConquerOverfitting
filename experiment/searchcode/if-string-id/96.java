/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package chessminion.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author ben
 */
@Entity
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    protected ChessPlayer user;
    @ManyToOne
    protected ChessGame game;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    protected Date messageTime = new Date();
    protected String messageContent;

    protected ChatMessage() {
    }

    public ChatMessage(ChessPlayer user, ChessGame game, String messageContent) {
        this.user = user;
        this.game = game;
        this.messageContent = messageContent;
    }


    

    /**
     * Get the value of messageContent
     *
     * @return the value of messageContent
     */
    public String getMessageContent() {
        return messageContent;
    }

    /**
     * Get the value of messageTime
     *
     * @return the value of messageTime
     */
    public Date getTimestamp() {
        return messageTime;
    }


    /**
     * Get the value of game
     *
     * @return the value of game
     */
    public ChessGame getGame() {
        return game;
    }

    /**
     * Get the value of user
     *
     * @return the value of user
     */
    public ChessPlayer getUser() {
        return user;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ChatMessage)) {
            return false;
        }
        ChatMessage other = (ChatMessage) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "chessminion.entities.ChatMessage[id=" + id + "]";
    }

    public String format() {
        return String.format(
            "[%s] %s: %s",
            new Object[]{
                getTimestamp(),
                getUser().getUsername(),
                getMessageContent()
            }
        );
    }
}

