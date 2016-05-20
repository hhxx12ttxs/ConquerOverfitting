/*
 * Copyright 2011 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.checklistbank.model;

import org.gbif.ecat.model.Identifiable;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Description implements Comparable, Identifiable<Integer> {

  private Integer id;
  private Integer usageId;
  private String description;
  private String language;
  private Integer typeId;
  private String type;
  private Integer sourceId;
  private String source;
  private String creator;
  private String contributor;
  private String license;

  /**
   * @see Comparable#compareTo(Object)
   */
  public int compareTo(Object object) {
    Description myClass = (Description) object;
    return new CompareToBuilder().append(this.typeId, myClass.typeId).append(this.language, myClass.language)
      .append(this.creator, myClass.creator).append(this.id, myClass.id).toComparison();
  }

  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof Description)) {
      return false;
    }
    Description rhs = (Description) object;
    return new EqualsBuilder().append(this.typeId, rhs.typeId).append(this.language, rhs.language)
      .append(this.creator, rhs.creator).append(this.id, rhs.id).isEquals();
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(151271395, 144449).append(this.usageId).append(this.typeId).append(this.language)
      .append(this.creator).append(this.description)

      .toHashCode();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getUsageId() {
    return usageId;
  }

  public void setUsageId(Integer usageId) {
    this.usageId = usageId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public Integer getTypeId() {
    return typeId;
  }

  public void setTypeId(Integer typeId) {
    this.typeId = typeId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getSourceId() {
    return sourceId;
  }

  public void setSourceId(Integer sourceId) {
    this.sourceId = sourceId;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getContributor() {
    return contributor;
  }

  public void setContributor(String contributor) {
    this.contributor = contributor;
  }

  public String getLicense() {
    return license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}

