/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blog.wordpress.entities;

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
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Breath
 */
@Entity
@Table(name = "comment")
@TableGenerator(name = "COMM_SEQ",
table = "SEQUENCE_TABLE",
pkColumnName = "SEQUENCE_NAME",
valueColumnName = "SEQUENCE_COUNT")

@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Comment.findAll", query = "SELECT c FROM Comment c"),
    @NamedQuery(name = "Comment.findById", query = "SELECT c FROM Comment c WHERE c.id = :id"),
    @NamedQuery(name = "Comment.findByVisitorName", query = "SELECT c FROM Comment c WHERE c.visitorName = :visitorName"),
    @NamedQuery(name = "Comment.findByVisitorEmail", query = "SELECT c FROM Comment c WHERE c.visitorEmail = :visitorEmail"),
    @NamedQuery(name = "Comment.findByVisitorWebsite", query = "SELECT c FROM Comment c WHERE c.visitorWebsite = :visitorWebsite")})
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "COMM_SEQ")
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 2147483647)
    @Column(name = "comment_body")
    private String commentBody;
    @Size(max = 45)
    @Column(name = "visitor_name")
    private String visitorName;
    @Size(max = 100)
    @Column(name = "visitor_email")
    private String visitorEmail;
    @Size(max = 150)
    @Column(name = "visitor_website")
    private String visitorWebsite;
    @JoinColumn(name = "article_id", referencedColumnName = "id")
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
        return "com.blog.wordpress.entities.Comment[ id=" + id + " ]";
    }
    
}

