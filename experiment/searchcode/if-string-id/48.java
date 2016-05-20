/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blog.wordpress.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Breath
 */
@Entity
@Table(name = "article")
@TableGenerator(name = "ART_SEQ",
table = "SEQUENCE_TABLE",
pkColumnName = "SEQUENCE_NAME",
valueColumnName = "SEQUENCE_COUNT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Article.findAll", query = "SELECT a FROM Article a"),
    @NamedQuery(name = "Article.findById", query = "SELECT a FROM Article a WHERE a.id = :id"),
    @NamedQuery(name = "Article.findByStatus", query = "SELECT a FROM Article a WHERE a.status = :status"),
    @NamedQuery(name = "Article.findByDate", query = "SELECT a FROM Article a WHERE a.date = :date"),
    @NamedQuery(name = "Article.findByTitle", query = "SELECT a FROM Article a WHERE a.title = :title"),
    @NamedQuery(name = "Article.findByTag", query = "SELECT a FROM Article a WHERE a.tag = :tag")})
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ART_SEQ")
    @Basic(optional = false)
    @NotNull
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 9)
    @Column(name = "status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "title")
    private String title;
    @Lob
    @Column(name = "image")
    private byte[] image;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "tag")
    private String tag;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 2147483647)
    @Column(name = "article_body")
    private String articleBody;
    @JoinTable(name = "article_has_tag", joinColumns = {
        @JoinColumn(name = "article_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "tag_id", referencedColumnName = "id")})
    @ManyToMany
    private List<Tag> tagList;
    @JoinTable(name = "article_has_category", joinColumns = {
        @JoinColumn(name = "article_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "category_id", referencedColumnName = "id")})
    @ManyToMany
    private List<Category> categoryList;
    @JoinColumn(name = "registered_user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private RegisteredUser registeredUserId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articleId")
    private List<Comment> commentList;

    public Article() {
    }

    public Article(Integer id) {
        this.id = id;
    }

    public Article(Integer id, String status, Date date, String title, String tag, String articleBody) {
        this.id = id;
        this.status = status;
        this.date = date;
        this.title = title;
        this.tag = tag;
        this.articleBody = articleBody;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getArticleBody() {
        return articleBody;
    }

    public void setArticleBody(String articleBody) {
        this.articleBody = articleBody;
    }

    @XmlTransient
    public List<Tag> getTagList() {
        return tagList;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

    @XmlTransient
    public List<Category> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public RegisteredUser getRegisteredUserId() {
        return registeredUserId;
    }

    public void setRegisteredUserId(RegisteredUser registeredUserId) {
        this.registeredUserId = registeredUserId;
    }

    @XmlTransient
    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
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
        if (!(object instanceof Article)) {
            return false;
        }
        Article other = (Article) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.blog.wordpress.entities.Article[ id=" + id + " ]";
    }
}

