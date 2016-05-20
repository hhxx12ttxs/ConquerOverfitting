package org.gbif.checklistbank.model;

import org.gbif.ecat.model.Identifiable;
import org.gbif.metadata.DateUtils;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class Reference implements Identifiable<Integer> {

  private Integer id;
  private String link;
  private String doi;
  private String title;
  private String creator;
  private String date;
  private Date dateParsed;
  private String source;
  private String description;
  private String subject;
  private String language;
  private String rights;

  public Reference() {

  }

  public Reference(String title, String creator, String date, String source, String description, String subject,
    String language, String rights) {
    super();
    this.title = title;
    this.creator = creator;
    this.date = date;
    this.dateParsed = DateUtils.parse(date);
    this.source = source;
    this.description = description;
    this.subject = subject;
    this.language = language;
    this.rights = rights;
  }

  /**
   * @return
   */
  public String buildCitation() {
    StringBuilder sb = new StringBuilder();
    if (!StringUtils.isBlank(creator)) {
      sb.append(creator);
      sb.append(": ");
    }
    if (!StringUtils.isBlank(title) && !StringUtils.isBlank(source)) {
      sb.append(title);
      sb.append(". ");
      sb.append(source);
    } else if (!StringUtils.isBlank(title)) {
      sb.append(title);
    } else if (!StringUtils.isBlank(source)) {
      sb.append(source);
    }
    if (!StringUtils.isBlank(date)) {
      sb.append(". ");
      sb.append(date);
    }
    return StringUtils.trimToNull(sb.toString());
  }

  public String getCreator() {
    return creator;
  }

  public String getDate() {
    return date;
  }

  public Date getDateParsed() {
    return dateParsed;
  }

  public String getDescription() {
    return description;
  }

  public String getDoi() {
    return doi;
  }

  public Integer getId() {
    return id;
  }

  public String getLanguage() {
    return language;
  }

  public String getLink() {
    return link;
  }

  public String getRights() {
    return rights;
  }

  public String getSource() {
    return source;
  }

  public String getSubject() {
    return subject;
  }

  public String getTitle() {
    return title;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void setDateParsed(Date dateParsed) {
    this.dateParsed = dateParsed;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public void setRights(String rights) {
    this.rights = rights;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}

