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
package org.gbif.checklistbank.api.model;

import org.gbif.checklistbank.api.model.vocabulary.LifeStage;
import org.gbif.checklistbank.api.model.vocabulary.Sex;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Objects;


/**
 * VernacularName Model Object represents a vernacular name for a scientific taxon.
 *
 * @see <a href="http://rs.gbif.org/extension/gbif/1.0/vernacularname.xml">Vernacular Name Definition</a>
 */
public class VernacularName extends NameUsageComponent {

  private String vernacularName;
  private String language;
  private LifeStage lifeStage;
  private Sex sex;
  private String country;
  private String area;
  private String source;
  private Boolean preferred;
  private Boolean plural;

  /**
   * A common or vernacular name.
   * <blockquote>
   * <p>
   * <i>Example:</i> Andean Condor", "Condor Andino", "American Eagle", "G?nsegeier".
   * </p>
   * </blockquote>
   *
   * @return the vernacularName
   */
  @NotNull
  public String getVernacularName() {
    return vernacularName;
  }

  /**
   * @param vernacularName the vernacularName to set
   */
  public void setVernacularName(String vernacularName) {
    this.vernacularName = vernacularName;
  }

  /**
   * ISO 639-1 language code used for the vernacular name value.
   * <blockquote>
   * <p>
   * <i>Example:</i> ES - EN
   * </p>
   * </blockquote>
   *
   * @return the language
   */
  @Nullable
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * The age class or life stage of the species for which the vernacular name applies. Best practice is to utilise a
   * controlled list of terms for this value.
   * <blockquote>
   * <p>
   * <i>Example:</i> juvenile" is the life stage of the fish Pomatomus saltatrix for which the name "snapper blue"
   * refers.
   * </p>
   * </blockquote>
   *
   * @return the lifeStage
   *
   * @see <a href="http://rs.gbif.org/vocabulary/gbif/life_stage.xml">Life Stage GBIF Vocabulary</a>
   */
  @Nullable
  public LifeStage getLifeStage() {
    return lifeStage;
  }

  /**
   * @param lifeStage the lifeStage to set
   */
  public void setLifeStage(LifeStage lifeStage) {
    this.lifeStage = lifeStage;
  }

  /**
   * The sex (gender) of the taxon for which the vernacular name applies when the vernacular name is limited to a
   * specific gender of a species. If not limited sex should be empty. For example the vernacular name "Buck" applies
   * to the "Male" gender of the species, Odocoileus virginianus.
   * <blockquote>
   * <p>
   * <i>Example:</i> male.
   * </p>
   * </blockquote>
   *
   * @return the sex
   *
   * @see <a href="http://rs.gbif.org/vocabulary/gbif/sex.xml">Sex GBIF Vocabulary</a>
   */
  @Nullable
  public Sex getSex() {
    return sex;
  }

  /**
   * @param sex the sex to set
   */
  public void setSex(Sex sex) {
    this.sex = sex;
  }

  /**
   * The standard code for the country in which the vernacular name is used. Recommended best practice is to use the
   * ISO 3166-1-alpha-2 country codes available as a vocabulary at http://rs.gbif.org/vocabulary/iso/3166-1_alpha2.xml.
   * For multiple countries separate values with a comma ",".
   * <blockquote>
   * <p>
   * <i>Example:</i> "AR" for Argentina, "SV" for El Salvador. "AR,CR,SV" for Argentina, Costa Rica, and El Salvador
   * combined.
   * </p>
   * </blockquote>
   *
   * @return the country
   */
  @Nullable
  public String getCountry() {
    return country;
  }

  /**
   * @param country the country to set
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * The area for the vernacular name.
   *
   * @return the area
   */
  @Nullable
  public String getArea() {
    return area;
  }

  /**
   * @param area the area to set
   */
  public void setArea(String area) {
    this.area = area;
  }

  /**
   * Bibliographic citation referencing a source where the vernacular name refers to the cited species.
   * <blockquote>
   * <p>
   * <i>Example:</i> Peterson Field Guide to the Eastern Seashore, Houghton Mifflin Co, 1961, p131.
   * </p>
   * </blockquote>
   *
   * @return the source
   */
  @Nullable
  public String getSource() {
    return source;
  }

  /**
   * @param source the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * This term is true if the source citing the use of this vernacular name indicates the usage has some preference or
   * specific standing over other possible vernacular names used for the species.
   * <blockquote>
   * <p>
   * <i>Example:</i> Some organisations have attempted to assign specific and unique vernacular names for particular
   * taxon groups in a systematic attempt to bring order and consistency to the use of these names. For example, the
   * American Ornithological Union assigns the name "Pearl Kite" for the taxon, Gampsonyx swainsonii. The value of
   * isPreferredName for this record would be true.
   * </p>
   * </blockquote>
   *
   * @return the preferred
   *
   * @see <a href="http://rs.gbif.org/vocabulary/basic/boolean.xml">Boolean Vocabulary</a>
   */
  @Nullable
  public Boolean isPreferred() {
    return preferred;
  }

  /**
   * @param preferred the preferred to set
   */
  public void setPreferred(Boolean preferred) {
    this.preferred = preferred;
  }

  /**
   * This value is true if the vernacular name it qualifies refers to a plural form of the name.
   * <blockquote>
   * <p>
   * <i>Example:</i> The term "Schoolies" is the plural form of a name used along the coastal Northeastern U.S. for
   * groups of juvenile fish of the species, Morone saxatilis.
   * </p>
   * </blockquote>
   *
   * @return the plural
   *
   * @see <a href="http://rs.gbif.org/vocabulary/basic/boolean.xml">Boolean Vocabulary</a>
   */
  @Nullable
  public Boolean isPlural() {
    return plural;
  }

  /**
   * @param plural the plural to set
   */
  public void setPlural(Boolean plural) {
    this.plural = plural;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("super", super.toString()).add("vernacularName", vernacularName)
      .add("language", language).add("lifeStage", lifeStage).add("sex", sex).add("country", country).add("area", area)
      .add("source", source).add("preferred", preferred).add("plural", plural).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), vernacularName, language, lifeStage, sex, country, area, source,
      preferred, plural);
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof VernacularName) {
      if (!super.equals(object)) {
        return false;
      }
      VernacularName that = (VernacularName) object;
      return Objects.equal(this.vernacularName, that.vernacularName) && Objects.equal(this.language, that.language)
        && Objects.equal(this.lifeStage, that.lifeStage) && Objects.equal(this.sex, that.sex)
        && Objects.equal(this.country, that.country) && Objects.equal(this.area, that.area)
        && Objects.equal(this.source, that.source) && Objects.equal(this.preferred, that.preferred)
        && Objects.equal(this.plural, that.plural);
    }
    return false;
  }
}

