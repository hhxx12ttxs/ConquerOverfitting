/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.city.set.eia.cityblog.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Breath
 */
@Entity
@Table(name = "article", catalog = "citypress", schema = "", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"title"})})
@TableGenerator(name = "art_user_generator",
table = "sequence_generator_table",
pkColumnName = "SEQUENCE_NAME",
valueColumnName = "SEQUENCE_COUNT",
pkColumnValue="art_user_sequence")

@NamedQueries({
    @NamedQuery(name = "Article.findAll", query = "SELECT a FROM Article a")})
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,generator = "art_user_generator")
    @Basic(optional = false)
    @NotNull
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 9)
    @Column(name = "status", nullable = false, length = 9)
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "title", nullable = false, length = 45)
    private String title;
    @Lob
    @Basic(fetch=FetchType.LAZY)
    @Column(name = "image")
    private byte[] image;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "tag", nullable = false, length = 45)
    private String tag;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 2147483647)
    @Column(name = "article_body", nullable = false, length = 2147483647)
    private String articleBody;
    @JoinTable(name = "article_has_tag", joinColumns = {
        @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)}, inverseJoinColumns = {
        @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)})
    @ManyToMany
    private Collection<Tag> tagCollection;
    @JoinTable(name = "article_has_category", joinColumns = {
        @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)}, inverseJoinColumns = {
        @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)})
    @ManyToMany
    private Collection<Category> categoryCollection;
    @JoinColumn(name = "registered_user_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private RegisteredUser registeredUserId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articleId")
    private Collection<Comment> commentCollection;

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
    public Collection<Tag> getTagCollection() {
        return tagCollection;
    }

    public void setTagCollection(Collection<Tag> tagCollection) {
        this.tagCollection = tagCollection;
    }

    @XmlTransient
    public Collection<Category> getCategoryCollection() {
        return categoryCollection;
    }

    public void setCategoryCollection(Collection<Category> categoryCollection) {
        this.categoryCollection = categoryCollection;
    }

    public RegisteredUser getRegisteredUserId() {
        return registeredUserId;
    }

    public void setRegisteredUserId(RegisteredUser registeredUserId) {
        this.registeredUserId = registeredUserId;
    }

    @XmlTransient
    public Collection<Comment> getCommentCollection() {
        return commentCollection;
    }

    public void setCommentCollection(Collection<Comment> commentCollection) {
        this.commentCollection = commentCollection;
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
        return "edu.city.set.eia.cityblog.entities.Article[ id=" + id + " ]";
    }
    
}

