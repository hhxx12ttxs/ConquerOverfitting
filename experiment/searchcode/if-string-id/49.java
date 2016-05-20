/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.city.set.eia.cityblog.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Breath
 */
@Entity
@Table(name = "comment", catalog = "citypress", schema = "")
@TableGenerator(name = "comm_user_generator",
table = "sequence_generator_table",
pkColumnName = "SEQUENCE_NAME",
valueColumnName = "SEQUENCE_COUNT",
pkColumnValue="comm_user_sequence")


@NamedQueries({
    @NamedQuery(name = "Comment.findAll", query = "SELECT c FROM Comment c")})
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,generator = "comm_user_generator")
    @Basic(optional = false)
    @NotNull
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 2147483647)
    @Column(name = "comment_body", nullable = false, length = 2147483647)
    private String commentBody;
    @Size(max = 45)
    @Column(name = "visitor_name", length = 45)
    private String visitorName;
    @Size(max = 100)
    @Column(name = "visitor_email", length = 100)
    private String visitorEmail;
    @Size(max = 150)
    @Column(name = "visitor_website", length = 150)
    private String visitorWebsite;
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Article articleId;
    @JoinColumn(name = "registered_user_id", referencedColumnName = "id")
    @ManyToOne
    private RegisteredUser registeredUserId;

    public Comment() {
    }

    public Comment(Integer id) {
        this.id = id;
    }

    public Comment(Integer id, String commentBody) {
        this.id = id;
        this.commentBody = commentBody;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCommentBody() {
        return commentBody;
    }

    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorEmail() {
        return visitorEmail;
    }

    public void setVisitorEmail(String visitorEmail) {
        this.visitorEmail = visitorEmail;
    }

    public String getVisitorWebsite() {
        return visitorWebsite;
    }

    public void setVisitorWebsite(String visitorWebsite) {
        this.visitorWebsite = visitorWebsite;
    }

    public Article getArticleId() {
        return articleId;
    }

    public void setArticleId(Article articleId) {
        this.articleId = articleId;
    }

    public RegisteredUser getRegisteredUserId() {
        return registeredUserId;
    }

    public void setRegisteredUserId(RegisteredUser registeredUserId) {
        this.registeredUserId = registeredUserId;
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
        if (!(object instanceof Comment)) {
            return false;
        }
        Comment other = (Comment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "edu.city.set.eia.cityblog.entities.Comment[ id=" + id + " ]";
    }
    
}

