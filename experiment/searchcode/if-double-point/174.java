/*
<<<<<<< HEAD
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Instance.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core;

import java.io.Serializable;
import java.util.Enumeration;

/**
 * Class for handling an instance. All values (numeric, date, nominal, string
 * or relational) are internally stored as floating-point numbers. If an
 * attribute is nominal (or a string or relational), the stored value is the
 * index of the corresponding nominal (or string or relational) value in the
 * attribute's definition. We have chosen this approach in favor of a more
 * elegant object-oriented approach because it is much faster. <p>
 *
 * Typical usage (code from the main() method of this class): <p>
 *
 * <code>
 * ... <br>
 *      
 * // Create empty instance with three attribute values <br>
 * Instance inst = new Instance(3); <br><br>
 *     
 * // Set instance's values for the attributes "length", "weight", and "position"<br>
 * inst.setValue(length, 5.3); <br>
 * inst.setValue(weight, 300); <br>
 * inst.setValue(position, "first"); <br><br>
 *   
 * // Set instance's dataset to be the dataset "race" <br>
 * inst.setDataset(race); <br><br>
 *   
 * // Print the instance <br>
 * System.out.println("The instance: " + inst); <br>
 *
 * ... <br>
 * </code><p>
 *
 * All methods that change an instance are safe, ie. a change of an
 * instance does not affect any other instances. All methods that
 * change an instance's attribute values clone the attribute value
 * vector before it is changed. If your application heavily modifies
 * instance values, it may be faster to create a new instance from scratch.
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 1.28 $ 
 */
public class Instance
  implements Copyable, Serializable, RevisionHandler {
  
  /** for serialization */
  static final long serialVersionUID = 1482635194499365122L;
  
  /** Constant representing a missing value. */
  protected static final double MISSING_VALUE = Double.NaN;

  /** 
   * The dataset the instance has access to.  Null if the instance
   * doesn't have access to any dataset.  Only if an instance has
   * access to a dataset, it knows about the actual attribute types.  
   */
  protected /*@spec_public@*/ Instances m_Dataset;

  /** The instance's attribute values. */
  protected /*@spec_public non_null@*/ double[] m_AttValues;

  /** The instance's weight. */
  protected double m_Weight;

  /**
   * Constructor that copies the attribute values and the weight from
   * the given instance. Reference to the dataset is set to null.
   * (ie. the instance doesn't have access to information about the
   * attribute types)
   *
   * @param instance the instance from which the attribute
   * values and the weight are to be copied 
   */
  //@ ensures m_Dataset == null;
  public Instance(/*@non_null@*/ Instance instance) {
    
    m_AttValues = instance.m_AttValues;
    m_Weight = instance.m_Weight;
    m_Dataset = null;
  }

  /**
   * Constructor that inititalizes instance variable with given
   * values. Reference to the dataset is set to null. (ie. the instance
   * doesn't have access to information about the attribute types)
   *
   * @param weight the instance's weight
   * @param attValues a vector of attribute values 
   */
  //@ ensures m_Dataset == null;
  public Instance(double weight,  /*@non_null@*/ double[]attValues){
    
    m_AttValues = attValues;
    m_Weight = weight;
    m_Dataset = null;
  }

  /**
   * Constructor of an instance that sets weight to one, all values to
   * be missing, and the reference to the dataset to null. (ie. the instance
   * doesn't have access to information about the attribute types)
   *
   * @param numAttributes the size of the instance 
   */
  //@ requires numAttributes > 0;    // Or maybe == 0 is okay too?
  //@ ensures m_Dataset == null;
  public Instance(int numAttributes) {
    
    m_AttValues = new double[numAttributes];
    for (int i = 0; i < m_AttValues.length; i++) {
      m_AttValues[i] = MISSING_VALUE;
    }
    m_Weight = 1;
    m_Dataset = null;
  }

  /**
   * Returns the attribute with the given index.
   *
   * @param index the attribute's index
   * @return the attribute at the given position
   * @throws UnassignedDatasetException if instance doesn't have access to a
   * dataset
   */ 
  //@ requires m_Dataset != null;
  public /*@pure@*/ Attribute attribute(int index) {
   
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.attribute(index);
  }

  /**
   * Returns the attribute with the given index. Does the same
   * thing as attribute().
   *
   * @param indexOfIndex the index of the attribute's index 
   * @return the attribute at the given position
   * @throws UnassignedDatasetException if instance doesn't have access to a
   * dataset
   */ 
  //@ requires m_Dataset != null;
  public /*@pure@*/ Attribute attributeSparse(int indexOfIndex) {
   
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.attribute(indexOfIndex);
  }

  /**
   * Returns class attribute.
   *
   * @return the class attribute
   * @throws UnassignedDatasetException if the class is not set or the
   * instance doesn't have access to a dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ Attribute classAttribute() {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.classAttribute();
  }

  /**
   * Returns the class attribute's index.
   *
   * @return the class index as an integer 
   * @throws UnassignedDatasetException if instance doesn't have access to a dataset 
   */
  //@ requires m_Dataset != null;
  //@ ensures  \result == m_Dataset.classIndex();
  public /*@pure@*/ int classIndex() {
    
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.classIndex();
  }

  /**
   * Tests if an instance's class is missing.
   *
   * @return true if the instance's class is missing
   * @throws UnassignedClassException if the class is not set or the instance doesn't
   * have access to a dataset
   */
  //@ requires classIndex() >= 0;
  public /*@pure@*/ boolean classIsMissing() {

    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    return isMissing(classIndex());
  }

  /**
   * Returns an instance's class value in internal format. (ie. as a
   * floating-point number)
   *
   * @return the corresponding value as a double (If the 
   * corresponding attribute is nominal (or a string) then it returns the 
   * value's index as a double).
   * @throws UnassignedClassException if the class is not set or the instance doesn't
   * have access to a dataset 
   */
  //@ requires classIndex() >= 0;
  public /*@pure@*/ double classValue() {
    
    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    return value(classIndex());
  }

  /**
   * Produces a shallow copy of this instance. The copy has
   * access to the same dataset. (if you want to make a copy
   * that doesn't have access to the dataset, use 
   * <code>new Instance(instance)</code>
   *
   * @return the shallow copy
   */
  //@ also ensures \result != null;
  //@ also ensures \result instanceof Instance;
  //@ also ensures ((Instance)\result).m_Dataset == m_Dataset;
  public /*@pure@*/ Object copy() {

    Instance result = new Instance(this);
    result.m_Dataset = m_Dataset;
    return result;
  }

  /**
   * Returns the dataset this instance has access to. (ie. obtains
   * information about attribute types from) Null if the instance
   * doesn't have access to a dataset.
   *
   * @return the dataset the instance has accesss to
   */
  //@ ensures \result == m_Dataset;
  public /*@pure@*/ Instances dataset() {

    return m_Dataset;
  }

  /**
   * Deletes an attribute at the given position (0 to 
   * numAttributes() - 1). Only succeeds if the instance does not
   * have access to any dataset because otherwise inconsistencies
   * could be introduced.
   *
   * @param position the attribute's position
   * @throws RuntimeException if the instance has access to a
   * dataset 
   */
  //@ requires m_Dataset != null;
  public void deleteAttributeAt(int position) {

    if (m_Dataset != null) {
      throw new RuntimeException("Instance has access to a dataset!");
    }
    forceDeleteAttributeAt(position);
  }

  /**
   * Returns an enumeration of all the attributes.
   *
   * @return enumeration of all the attributes
   * @throws UnassignedDatasetException if the instance doesn't
   * have access to a dataset 
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ Enumeration enumerateAttributes() {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.enumerateAttributes();
  }

  /**
   * Tests if the headers of two instances are equivalent.
   *
   * @param inst another instance
   * @return true if the header of the given instance is 
   * equivalent to this instance's header
   * @throws UnassignedDatasetException if instance doesn't have access to any
   * dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ boolean equalHeaders(Instance inst) {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.equalHeaders(inst.m_Dataset);
  }

  /**
   * Tests whether an instance has a missing value. Skips the class attribute if set.
   * @return true if instance has a missing value.
   * @throws UnassignedDatasetException if instance doesn't have access to any
   * dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ boolean hasMissingValue() {
    
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    for (int i = 0; i < numAttributes(); i++) {
      if (i != classIndex()) {
	if (isMissing(i)) {
	  return true;
	}
      }
    }
    return false;
  }

  /**
   * Returns the index of the attribute stored at the given position.
   * Just returns the given value.
   *
   * @param position the position 
   * @return the index of the attribute stored at the given position
   */
  public /*@pure@*/ int index(int position) {

    return position;
  }

  /**
   * Inserts an attribute at the given position (0 to 
   * numAttributes()). Only succeeds if the instance does not
   * have access to any dataset because otherwise inconsistencies
   * could be introduced.
   *
   * @param position the attribute's position
   * @throws RuntimeException if the instance has accesss to a
   * dataset
   * @throws IllegalArgumentException if the position is out of range
   */
  //@ requires m_Dataset == null;
  //@ requires 0 <= position && position <= numAttributes();
  public void insertAttributeAt(int position) {

    if (m_Dataset != null) {
      throw new RuntimeException("Instance has accesss to a dataset!");
    }
    if ((position < 0) ||
	(position > numAttributes())) {
      throw new IllegalArgumentException("Can't insert attribute: index out "+
                                         "of range");
    }
    forceInsertAttributeAt(position);
  }

  /**
   * Tests if a specific value is "missing".
   *
   * @param attIndex the attribute's index
   * @return true if the value is "missing"
   */
  public /*@pure@*/ boolean isMissing(int attIndex) {

    if (Double.isNaN(m_AttValues[attIndex])) {
      return true;
    }
    return false;
  }

  /**
   * Tests if a specific value is "missing". Does
   * the same thing as isMissing() if applied to an Instance.
   *
   * @param indexOfIndex the index of the attribute's index 
   * @return true if the value is "missing"
   */
  public /*@pure@*/ boolean isMissingSparse(int indexOfIndex) {

    if (Double.isNaN(m_AttValues[indexOfIndex])) {
      return true;
    }
    return false;
  }

  /**
   * Tests if a specific value is "missing".
   * The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   * @return true if the value is "missing"
   */
  public /*@pure@*/ boolean isMissing(Attribute att) {

    return isMissing(att.index());
  }

  /**
   * Tests if the given value codes "missing".
   *
   * @param val the value to be tested
   * @return true if val codes "missing"
   */
  public static /*@pure@*/ boolean isMissingValue(double val) {

    return Double.isNaN(val);
  }

  /**
   * Merges this instance with the given instance and returns
   * the result. Dataset is set to null.
   *
   * @param inst the instance to be merged with this one
   * @return the merged instances
   */
  public Instance mergeInstance(Instance inst) {

    int m = 0;
    double [] newVals = new double[numAttributes() + inst.numAttributes()];
    for (int j = 0; j < numAttributes(); j++, m++) {
      newVals[m] = value(j);
    }
    for (int j = 0; j < inst.numAttributes(); j++, m++) {
      newVals[m] = inst.value(j);
    }
    return new Instance(1.0, newVals);
  }

  /**
   * Returns the double that codes "missing".
   *
   * @return the double that codes "missing"
   */
  public /*@pure@*/ static double missingValue() {

    return MISSING_VALUE;
  }

  /**
   * Returns the number of attributes.
   *
   * @return the number of attributes as an integer
   */
  //@ ensures \result == m_AttValues.length;
  public /*@pure@*/ int numAttributes() {

    return m_AttValues.length;
  }

  /**
   * Returns the number of class labels.
   *
   * @return the number of class labels as an integer if the 
   * class attribute is nominal, 1 otherwise.
   * @throws UnassignedDatasetException if instance doesn't have access to any
   * dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ int numClasses() {
    
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.numClasses();
  }

  /**
   * Returns the number of values present. Always the same as numAttributes().
   *
   * @return the number of values
   */
  //@ ensures \result == m_AttValues.length;
  public /*@pure@*/ int numValues() {

    return m_AttValues.length;
  }

  /** 
   * Replaces all missing values in the instance with the
   * values contained in the given array. A deep copy of
   * the vector of attribute values is performed before the
   * values are replaced.
   *
   * @param array containing the means and modes
   * @throws IllegalArgumentException if numbers of attributes are unequal
   */
  public void replaceMissingValues(double[] array) {
	 
    if ((array == null) || 
	(array.length != m_AttValues.length)) {
      throw new IllegalArgumentException("Unequal number of attributes!");
    }
    freshAttributeVector();
    for (int i = 0; i < m_AttValues.length; i++) {
      if (isMissing(i)) {
	m_AttValues[i] = array[i];
      }
    }
  }

  /**
   * Sets the class value of an instance to be "missing". A deep copy of
   * the vector of attribute values is performed before the
   * value is set to be missing.
   *
   * @throws UnassignedClassException if the class is not set
   * @throws UnassignedDatasetException if the instance doesn't
   * have access to a dataset
   */
  //@ requires classIndex() >= 0;
  public void setClassMissing() {

    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setMissing(classIndex());
  }

  /**
   * Sets the class value of an instance to the given value (internal
   * floating-point format).  A deep copy of the vector of attribute
   * values is performed before the value is set.
   *
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   * @throws UnassignedClassException if the class is not set
   * @throws UnaddignedDatasetException if the instance doesn't
   * have access to a dataset 
   */
  //@ requires classIndex() >= 0;
  public void setClassValue(double value) {

    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setValue(classIndex(), value);
  }

  /**
   * Sets the class value of an instance to the given value. A deep
   * copy of the vector of attribute values is performed before the
   * value is set.
   *
   * @param value the new class value (If the class
   * is a string attribute and the value can't be found,
   * the value is added to the attribute).
   * @throws UnassignedClassException if the class is not set
   * @throws UnassignedDatasetException if the dataset is not set
   * @throws IllegalArgumentException if the attribute is not
   * nominal or a string, or the value couldn't be found for a nominal
   * attribute 
   */
  //@ requires classIndex() >= 0;
  public final void setClassValue(String value) {

    if (classIndex() < 0) {
      throw new UnassignedClassException("Class is not set!");
    }
    setValue(classIndex(), value);
  }

  /**
   * Sets the reference to the dataset. Does not check if the instance
   * is compatible with the dataset. Note: the dataset does not know
   * about this instance. If the structure of the dataset's header
   * gets changed, this instance will not be adjusted automatically.
   *
   * @param instances the reference to the dataset 
   */
  public final void setDataset(Instances instances) {
    
    m_Dataset = instances;
  }

  /**
   * Sets a specific value to be "missing". Performs a deep copy
   * of the vector of attribute values before the value is set to
   * be missing.
   *
   * @param attIndex the attribute's index
   */
  public final void setMissing(int attIndex) {

    setValue(attIndex, MISSING_VALUE);
  }

  /**
   * Sets a specific value to be "missing". Performs a deep copy
   * of the vector of attribute values before the value is set to
   * be missing. The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   */
  public final void setMissing(Attribute att) {

    setMissing(att.index());
  }

  /**
   * Sets a specific value in the instance to the given value 
   * (internal floating-point format). Performs a deep copy
   * of the vector of attribute values before the value is set.
   *
   * @param attIndex the attribute's index 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   */
  public void setValue(int attIndex, double value) {
    
    freshAttributeVector();
    m_AttValues[attIndex] = value;
  }

  /**
   * Sets a specific value in the instance to the given value 
   * (internal floating-point format). Performs a deep copy
   * of the vector of attribute values before the value is set.
   * Does exactly the same thing as setValue().
   *
   * @param indexOfIndex the index of the attribute's index 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   */
  public void setValueSparse(int indexOfIndex, double value) {
    
    freshAttributeVector();
    m_AttValues[indexOfIndex] = value;
  }

  /**
   * Sets a value of a nominal or string attribute to the given
   * value. Performs a deep copy of the vector of attribute values
   * before the value is set.
   *
   * @param attIndex the attribute's index
   * @param value the new attribute value (If the attribute
   * is a string attribute and the value can't be found,
   * the value is added to the attribute).
   * @throws UnassignedDatasetException if the dataset is not set
   * @throws IllegalArgumentException if the selected
   * attribute is not nominal or a string, or the supplied value couldn't 
   * be found for a nominal attribute 
   */
  //@ requires m_Dataset != null;
  public final void setValue(int attIndex, String value) {
    
    int valIndex;

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    if (!attribute(attIndex).isNominal() &&
	!attribute(attIndex).isString()) {
      throw new IllegalArgumentException("Attribute neither nominal nor string!");
    }
    valIndex = attribute(attIndex).indexOfValue(value);
    if (valIndex == -1) {
      if (attribute(attIndex).isNominal()) {
	throw new IllegalArgumentException("Value not defined for given nominal attribute!");
      } else {
	attribute(attIndex).forceAddValue(value);
	valIndex = attribute(attIndex).indexOfValue(value);
      }
    }
    setValue(attIndex, (double)valIndex); 
  }

  /**
   * Sets a specific value in the instance to the given value
   * (internal floating-point format). Performs a deep copy of the
   * vector of attribute values before the value is set, so if you are
   * planning on calling setValue many times it may be faster to
   * create a new instance using toDoubleArray.  The given attribute
   * has to belong to a dataset.
   *
   * @param att the attribute 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   */
  public final void setValue(Attribute att, double value) {

    setValue(att.index(), value);
  }

  /**
   * Sets a value of an nominal or string attribute to the given
   * value. Performs a deep copy of the vector of attribute values
   * before the value is set, so if you are planning on calling setValue many
   * times it may be faster to create a new instance using toDoubleArray.
   * The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   * @param value the new attribute value (If the attribute
   * is a string attribute and the value can't be found,
   * the value is added to the attribute).
   * @throws IllegalArgumentException if the the attribute is not
   * nominal or a string, or the value couldn't be found for a nominal
   * attribute 
   */
  public final void setValue(Attribute att, String value) {

    if (!att.isNominal() &&
	!att.isString()) {
      throw new IllegalArgumentException("Attribute neither nominal nor string!");
    }
    int valIndex = att.indexOfValue(value);
    if (valIndex == -1) {
      if (att.isNominal()) {
	throw new IllegalArgumentException("Value not defined for given nominal attribute!");
      } else {
	att.forceAddValue(value);
	valIndex = att.indexOfValue(value);
      }
    }
    setValue(att.index(), (double)valIndex);
  }

  /**
   * Modifies the instances value for an attribute (floating point
   * representation). Unlike in <code>setValue</code> no deep copy is
   * produced, i.e. the actual value is modified.
   *
   * @param attIndex the attribute's index 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).
   * @author Arne Muller (arne.muller@gmail.com)
   */
  public void modifyValue(int attIndex, double value) {
    
    m_AttValues[attIndex] = value;
  }
  
  /**
   * Sets the weight of an instance.
   *
   * @param weight the weight
   */
  public final void setWeight(double weight) {

    m_Weight = weight;
  }

  /** 
   * Returns the relational value of a relational attribute.
   *
   * @param attIndex the attribute's index
   * @return the corresponding relation as an Instances object
   * @throws IllegalArgumentException if the attribute is not a
   * relation-valued attribute
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  //@ requires m_Dataset != null;
  public final /*@pure@*/ Instances relationalValue(int attIndex) {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    } 
    return relationalValue(m_Dataset.attribute(attIndex));
  }


  /** 
   * Returns the relational value of a relational attribute.
   *
   * @param att the attribute
   * @return the corresponding relation as an Instances object
   * @throws IllegalArgumentException if the attribute is not a
   * relation-valued attribute
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  public final /*@pure@*/ Instances relationalValue(Attribute att) {

    int attIndex = att.index();
    if (att.isRelationValued()) {
      return att.relation((int) value(attIndex));
    } else {
      throw new IllegalArgumentException("Attribute isn't relation-valued!");
    }
  }

  /** 
   * Returns the value of a nominal, string, date, or relational attribute
   * for the instance as a string.
   *
   * @param attIndex the attribute's index
   * @return the value as a string
   * @throws IllegalArgumentException if the attribute is not a nominal,
   * string, date, or relation-valued attribute.
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  //@ requires m_Dataset != null;
  public final /*@pure@*/ String stringValue(int attIndex) {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    } 
    return stringValue(m_Dataset.attribute(attIndex));
  }


  /** 
   * Returns the value of a nominal, string, date, or relational attribute
   * for the instance as a string.
   *
   * @param att the attribute
   * @return the value as a string
   * @throws IllegalArgumentException if the attribute is not a nominal,
   * string, date, or relation-valued attribute.
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  public final /*@pure@*/ String stringValue(Attribute att) {

    int attIndex = att.index();
    switch (att.type()) {
    case Attribute.NOMINAL:
    case Attribute.STRING:
      return att.value((int) value(attIndex));
    case Attribute.DATE:
      return att.formatDate(value(attIndex));
    case Attribute.RELATIONAL:
      return att.relation((int) value(attIndex)).stringWithoutHeader();
    default:
      throw new IllegalArgumentException("Attribute isn't nominal, string or date!");
    }
  }

  /**
   * Returns the values of each attribute as an array of doubles.
   *
   * @return an array containing all the instance attribute values
   */
  public double[] toDoubleArray() {

    double[] newValues = new double[m_AttValues.length];
    System.arraycopy(m_AttValues, 0, newValues, 0, 
		     m_AttValues.length);
    return newValues;
  }

  /**
   * Returns the description of one instance. If the instance
   * doesn't have access to a dataset, it returns the internal
   * floating-point values. Quotes string
   * values that contain whitespace characters.
   *
   * @return the instance's description as a string
   */
  public String toString() {

    StringBuffer text = new StringBuffer();
    
    for (int i = 0; i < m_AttValues.length; i++) {
      if (i > 0) text.append(",");
      text.append(toString(i));
    }

    if (m_Weight != 1.0) {
      text.append(",{" + Utils.doubleToString(m_Weight, 6) + "}");
    }

    return text.toString();
  }

  /**
   * Returns the description of one value of the instance as a 
   * string. If the instance doesn't have access to a dataset, it 
   * returns the internal floating-point value. Quotes string
   * values that contain whitespace characters, or if they
   * are a question mark.
   *
   * @param attIndex the attribute's index
   * @return the value's description as a string
   */
  public final /*@pure@*/ String toString(int attIndex) {

   StringBuffer text = new StringBuffer();
   
   if (isMissing(attIndex)) {
     text.append("?");
   } else {
     if (m_Dataset == null) {
       text.append(Utils.doubleToString(m_AttValues[attIndex],6));
     } else {
       switch (m_Dataset.attribute(attIndex).type()) {
       case Attribute.NOMINAL:
       case Attribute.STRING:
       case Attribute.DATE:
       case Attribute.RELATIONAL:
         text.append(Utils.quote(stringValue(attIndex)));
         break;
       case Attribute.NUMERIC:
	 text.append(Utils.doubleToString(value(attIndex),6));
         break;
       default:
         throw new IllegalStateException("Unknown attribute type");
       }
     }
   }
   return text.toString();
  }

  /**
   * Returns the description of one value of the instance as a 
   * string. If the instance doesn't have access to a dataset it 
   * returns the internal floating-point value. Quotes string
   * values that contain whitespace characters, or if they
   * are a question mark.
   * The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   * @return the value's description as a string
   */
  public final String toString(Attribute att) {
   
   return toString(att.index());
  }

  /**
   * Returns an instance's attribute value in internal format.
   *
   * @param attIndex the attribute's index
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a 
   * double).
   */
  public /*@pure@*/ double value(int attIndex) {

    return m_AttValues[attIndex];
  }

  /**
   * Returns an instance's attribute value in internal format.
   * Does exactly the same thing as value() if applied to an Instance.
   *
   * @param indexOfIndex the index of the attribute's index
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a 
   * double).
   */
  public /*@pure@*/ double valueSparse(int indexOfIndex) {

    return m_AttValues[indexOfIndex];
  }  

  /**
   * Returns an instance's attribute value in internal format.
   * The given attribute has to belong to a dataset.
   *
   * @param att the attribute
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a
   * double).
   */
  public /*@pure@*/ double value(Attribute att) {

    return value(att.index());
  }

  /**
   * Returns the instance's weight.
   *
   * @return the instance's weight as a double
   */
  public final /*@pure@*/ double weight() {

    return m_Weight;
  }

  /**
   * Deletes an attribute at the given position (0 to 
   * numAttributes() - 1).
   *
   * @param position the attribute's position
   */
  void forceDeleteAttributeAt(int position) {

    double[] newValues = new double[m_AttValues.length - 1];

    System.arraycopy(m_AttValues, 0, newValues, 0, position);
    if (position < m_AttValues.length - 1) {
      System.arraycopy(m_AttValues, position + 1, 
		       newValues, position, 
		       m_AttValues.length - (position + 1));
    }
    m_AttValues = newValues;
  }

  /**
   * Inserts an attribute at the given position
   * (0 to numAttributes()) and sets its value to be missing. 
   *
   * @param position the attribute's position
   */
  void forceInsertAttributeAt(int position)  {

    double[] newValues = new double[m_AttValues.length + 1];

    System.arraycopy(m_AttValues, 0, newValues, 0, position);
    newValues[position] = MISSING_VALUE;
    System.arraycopy(m_AttValues, position, newValues, 
		     position + 1, m_AttValues.length - position);
    m_AttValues = newValues;
  }

  /**
   * Private constructor for subclasses. Does nothing.
   */
  protected Instance() {
  }

  /**
   * Clones the attribute vector of the instance and
   * overwrites it with the clone.
   */
  private void freshAttributeVector() {

    m_AttValues = toDoubleArray();
  }

  /**
   * Main method for testing this class.
   * 
   * @param options the commandline options - ignored
   */
  //@ requires options != null;
  public static void main(String[] options) {

    try {

      // Create numeric attributes "length" and "weight"
      Attribute length = new Attribute("length");
      Attribute weight = new Attribute("weight");
      
      // Create vector to hold nominal values "first", "second", "third" 
      FastVector my_nominal_values = new FastVector(3); 
      my_nominal_values.addElement("first"); 
      my_nominal_values.addElement("second"); 
      my_nominal_values.addElement("third"); 
      
      // Create nominal attribute "position" 
      Attribute position = new Attribute("position", my_nominal_values);
      
      // Create vector of the above attributes 
      FastVector attributes = new FastVector(3);
      attributes.addElement(length);
      attributes.addElement(weight);
      attributes.addElement(position);
      
      // Create the empty dataset "race" with above attributes
      Instances race = new Instances("race", attributes, 0);
      
      // Make position the class attribute
      race.setClassIndex(position.index());
      
      // Create empty instance with three attribute values
      Instance inst = new Instance(3);
      
      // Set instance's values for the attributes "length", "weight", and "position"
      inst.setValue(length, 5.3);
      inst.setValue(weight, 300);
      inst.setValue(position, "first");
      
      // Set instance's dataset to be the dataset "race"
      inst.setDataset(race);
      
      // Print the instance
      System.out.println("The instance: " + inst);
      
      // Print the first attribute
      System.out.println("First attribute: " + inst.attribute(0));
      
      // Print the class attribute
      System.out.println("Class attribute: " + inst.classAttribute());
      
      // Print the class index
      System.out.println("Class index: " + inst.classIndex());
      
      // Say if class is missing
      System.out.println("Class is missing: " + inst.classIsMissing());
      
      // Print the instance's class value in internal format
      System.out.println("Class value (internal format): " + inst.classValue());
      
      // Print a shallow copy of this instance
      Instance copy = (Instance) inst.copy();
      System.out.println("Shallow copy: " + copy);
      
      // Set dataset for shallow copy
      copy.setDataset(inst.dataset());
      System.out.println("Shallow copy with dataset set: " + copy);
      
      // Unset dataset for copy, delete first attribute, and insert it again
      copy.setDataset(null);
      copy.deleteAttributeAt(0);
      copy.insertAttributeAt(0);
      copy.setDataset(inst.dataset());
      System.out.println("Copy with first attribute deleted and inserted: " + copy); 
      
      // Enumerate attributes (leaving out the class attribute)
      System.out.println("Enumerating attributes (leaving out class):");
      Enumeration enu = inst.enumerateAttributes();
      while (enu.hasMoreElements()) {
	Attribute att = (Attribute) enu.nextElement();
	System.out.println(att);
      }
      
      // Headers are equivalent?
      System.out.println("Header of original and copy equivalent: " +
			 inst.equalHeaders(copy));

      // Test for missing values
      System.out.println("Length of copy missing: " + copy.isMissing(length));
      System.out.println("Weight of copy missing: " + copy.isMissing(weight.index()));
      System.out.println("Length of copy missing: " + 
			 Instance.isMissingValue(copy.value(length)));
      System.out.println("Missing value coded as: " + Instance.missingValue());

      // Prints number of attributes and classes
      System.out.println("Number of attributes: " + copy.numAttributes());
      System.out.println("Number of classes: " + copy.numClasses());

      // Replace missing values
      double[] meansAndModes = {2, 3, 0};
      copy.replaceMissingValues(meansAndModes);
      System.out.println("Copy with missing value replaced: " + copy);

      // Setting and getting values and weights
      copy.setClassMissing();
      System.out.println("Copy with missing class: " + copy);
      copy.setClassValue(0);
      System.out.println("Copy with class value set to first value: " + copy);
      copy.setClassValue("third");
      System.out.println("Copy with class value set to \"third\": " + copy);
      copy.setMissing(1);
      System.out.println("Copy with second attribute set to be missing: " + copy);
      copy.setMissing(length);
      System.out.println("Copy with length set to be missing: " + copy);
      copy.setValue(0, 0);
      System.out.println("Copy with first attribute set to 0: " + copy);
      copy.setValue(weight, 1);
      System.out.println("Copy with weight attribute set to 1: " + copy);
      copy.setValue(position, "second");
      System.out.println("Copy with position set to \"second\": " + copy);
      copy.setValue(2, "first");
      System.out.println("Copy with last attribute set to \"first\": " + copy);
      System.out.println("Current weight of instance copy: " + copy.weight());
      copy.setWeight(2);
      System.out.println("Current weight of instance copy (set to 2): " + copy.weight());
      System.out.println("Last value of copy: " + copy.toString(2));
      System.out.println("Value of position for copy: " + copy.toString(position));
      System.out.println("Last value of copy (internal format): " + copy.value(2));
      System.out.println("Value of position for copy (internal format): " + 
			 copy.value(position));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.28 $");
  }
}
=======
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.osgeo.proj4j.proj;

import org.osgeo.proj4j.*;
import org.osgeo.proj4j.datum.Ellipsoid;
import org.osgeo.proj4j.units.AngleFormat;
import org.osgeo.proj4j.units.Unit;
import org.osgeo.proj4j.units.Units;
import org.osgeo.proj4j.util.ProjectionMath;

/**
 * A map projection is a mathematical algorithm
 * for representing a spheroidal surface 
 * on a plane.
 * A single projection
 * defines a (usually infinite) family of
 * {@link CoordinateReferenceSystem}s,
 * distinguished by different values for the
 * projection parameters.
 */
public abstract class Projection implements Cloneable {

	/**
	 * The minimum latitude of the bounds of this projection
	 */
	protected double minLatitude = -Math.PI/2;

	/**
	 * The minimum longitude of the bounds of this projection. This is relative to the projection centre.
	 */
	protected double minLongitude = -Math.PI;

	/**
	 * The maximum latitude of the bounds of this projection
	 */
	protected double maxLatitude = Math.PI/2;

	/**
	 * The maximum longitude of the bounds of this projection. This is relative to the projection centre.
	 */
	protected double maxLongitude = Math.PI;

	/**
	 * The latitude of the centre of projection
	 */
	protected double projectionLatitude = 0.0;

	/**
	 * The longitude of the centre of projection, in radians
	 */
	protected double projectionLongitude = 0.0;

	/**
	 * Standard parallel 1 (for projections which use it)
	 */
	protected double projectionLatitude1 = 0.0;

	/**
	 * Standard parallel 2 (for projections which use it)
	 */
	protected double projectionLatitude2 = 0.0;

  /**
   * The projection alpha value
   */
  protected double alpha = Double.NaN;

  /**
   * The projection lonc value
   */
  protected double lonc = Double.NaN;

  /**
   * The projection scale factor
   */
  protected double scaleFactor = 1.0;

	/**
	 * The false Easting of this projection
	 */
	protected double falseEasting = 0;

	/**
	 * The false Northing of this projection
	 */
	protected double falseNorthing = 0;

  /**
   * Indicates whether a Southern Hemisphere UTM zone
   */
  protected boolean isSouth = false;
	/**
	 * The latitude of true scale. Only used by specific projections.
	 */
	protected double trueScaleLatitude = 0.0;

	/**
	 * The equator radius
	 */
	protected double a = 0;

	/**
	 * The eccentricity
	 */
	protected double e = 0;

	/**
	 * The eccentricity squared
	 */
	protected double es = 0;

	/**
	 * 1-(eccentricity squared)
	 */
	protected double one_es = 0;

	/**
	 * 1/(1-(eccentricity squared))
	 */
	protected double rone_es = 0;

	/**
	 * The ellipsoid used by this projection
	 */
	protected Ellipsoid ellipsoid;

	/**
	 * True if this projection is using a sphere (es == 0)
	 */
	protected boolean spherical;

	/**
	 * True if this projection is geocentric
	 */
	protected boolean geocentric;

	/**
	 * The name of this projection
	 */
	protected String name = null;

	/**
	 * Conversion factor from metres to whatever units the projection uses.
	 */
	protected double fromMetres = 1;

	/**
	 * The total scale factor = Earth radius * units
	 */
	protected double totalScale = 0;

	/**
	 * falseEasting, adjusted to the appropriate units using fromMetres
	 */
	private double totalFalseEasting = 0;

  /**
   * falseNorthing, adjusted to the appropriate units using fromMetres
   */
  private double totalFalseNorthing = 0;

  /**
   * units of this projection.  Default is metres, but may be degrees
   */
  protected Unit unit = null;

	// Some useful constants
	protected final static double EPS10 = 1e-10;
	protected final static double RTD = 180.0/Math.PI;
	protected final static double DTR = Math.PI/180.0;
	
	protected Projection() {
		setEllipsoid( Ellipsoid.SPHERE );
	}
	
	public Object clone() {
		try {
			Projection e = (Projection)super.clone();
			return e;
		}
		catch ( CloneNotSupportedException e ) {
			throw new InternalError();
		}
	}
	
	/**
	 * Projects a geographic point (in degrees), producing a projected result 
	 * (in the units of the target coordinate system).
	 * 
   * @param src the input geographic coordinate (in degrees)
   * @param dst the projected coordinate (in coordinate system units)
   * @return the target coordinate 
	 */
	public ProjCoordinate project( ProjCoordinate src, ProjCoordinate dst ) {
		double x = src.x*DTR;
		if ( projectionLongitude != 0 )
			x = ProjectionMath.normalizeLongitude( x-projectionLongitude );
		return projectRadians(x, src.y*DTR, dst);
	}

	/**
	 * Projects a geographic point (in radians), producing a projected result 
	 * (in the units of the target coordinate system).
	 * 
   * @param src the input geographic coordinate (in radians)
   * @param dst the projected coordinate (in coordinate system units)
   * @return the target coordinate 
	 * 
	 */
	public ProjCoordinate projectRadians( ProjCoordinate src, ProjCoordinate dst ) {
		double x = src.x;
		if ( projectionLongitude != 0 )
			x = ProjectionMath.normalizeLongitude( x-projectionLongitude );
		return projectRadians(x, src.y, dst);
	}
	
	/**
	 * Transform a geographic point (in radians), 
	 * producing a projected result (in the units of the target coordinate system).
	 * 
	 * @param x the geographic x ordinate (in radians)
	 * @param y the geographic y ordinate (in radians)
   * @param dst the projected coordinate (in coordinate system units)
	 * @return the target coordinate
	 */
	private ProjCoordinate projectRadians(double x, double y, ProjCoordinate dst ) {
		project(x, y, dst);
    if (unit == Units.DEGREES) {
      // convert radians to DD
      dst.x *= RTD;
      dst.y *= RTD;
    }
    else {
      // assume result is in metres
      dst.x = totalScale * dst.x + totalFalseEasting;
      dst.y = totalScale * dst.y + totalFalseNorthing;
    }
		return dst;
	}

	/**
	 * Computes the projection of a given point 
	 * (i.e. from geographics to projection space). 
	 * This should be overridden for all projections.
	 * 
	 * @param x the geographic x ordinate (in radians)
	 * @param y the geographic y ordinatee (in radians)
   * @param dst the projected coordinate (in coordinate system units)
	 * @return the target coordinate
	 */
	protected ProjCoordinate project(double x, double y, ProjCoordinate dst) {
		dst.x = x;
		dst.y = y;
		return dst;
	}

	/**
	 * Inverse-projects a point (in the units defined by the coordinate system), 
	 * producing a geographic result (in degrees)
	 * 
   * @param src the input projected coordinate (in coordinate system units)
   * @param dst the inverse-projected geographic coordinate (in degrees)
   * @return the target coordinate
	 */
	public ProjCoordinate inverseProject(ProjCoordinate src, ProjCoordinate dst) {
    inverseProjectRadians(src, dst);
		dst.x *= RTD;
		dst.y *= RTD;
		return dst;
	}

	/**
	 * Inverse-transforms a point (in the units defined by the coordinate system), 
	 * producing a geographic result (in radians)
	 * 
	 * @param src the input projected coordinate (in coordinate system units)
   * @param dst the inverse-projected geographic coordinate (in radians)
   * @return the target coordinate
	 * 
	 */
	public ProjCoordinate inverseProjectRadians(ProjCoordinate src, ProjCoordinate dst) {
    double x;
    double y;
    if (unit == Units.DEGREES) {
      // convert DD to radians
      x = src.x * DTR;
      y = src.y * DTR;
    }
    else {
      x = (src.x - totalFalseEasting) / totalScale;
      y = (src.y - totalFalseNorthing) / totalScale;
    }
		projectInverse(x, y, dst);
		if (dst.x < -Math.PI)
			dst.x = -Math.PI;
		else if (dst.x > Math.PI)
			dst.x = Math.PI;
		if (projectionLongitude != 0)
			dst.x = ProjectionMath.normalizeLongitude(dst.x+projectionLongitude);
		return dst;
	}

	/**
	 * Computes the inverse projection of a given point 
	 * (i.e. from projection space to geographics). 
	 * This should be overridden for all projections.
	 * 
	 * @param x the projected x ordinate (in coordinate system units)
	 * @param y the projected y ordinate (in coordinate system units)
	 * @param dst the inverse-projected geographic coordinate  (in radians)
	 * @return the target coordinate
	 */
	protected ProjCoordinate projectInverse(double x, double y, ProjCoordinate dst) {
		dst.x = x;
		dst.y = y;
		return dst;
	}

	
	/**
	 * Tests whether this projection is conformal.
	 * A conformal projection preserves local angles.
	 * 
	 * @return true if this projection is conformal
	 */
	public boolean isConformal() {
		return false;
	}
	
	/**
	 * Tests whether this projection is equal-area
   * An equal-area projection preserves relative sizes
   * of projected areas.
   * 
   * @return true if this projection is equal-area
	 */
	public boolean isEqualArea() {
		return false;
	}
	
	/**
	 * Tests whether this projection has an inverse.
	 * If this method returns <tt>true</tt>
	 * then the {@link #inverseProject(ProjCoordinate, ProjCoordinate)}
	 * and {@link #inverseProjectRadians(ProjCoordinate, ProjCoordinate)}
	 * methods will return meaningful results.
	 * 
	 * @return true if this projection has an inverse
	 */
	public boolean hasInverse() {
		return false;
	}

	/**
	 * Tests whether under this projection lines of 
	 * latitude and longitude form a rectangular grid
	 */
	public boolean isRectilinear() {
		return false;
	}

	/**
	 * Returns true if latitude lines are parallel for this projection
	 */
	public boolean parallelsAreParallel() {
		return isRectilinear();
	}

	/**
	 * Returns true if the given lat/long point is visible in this projection
	 */
	public boolean inside(double x, double y) {
		x = normalizeLongitude( (float)(x*DTR-projectionLongitude) );
		return minLongitude <= x && x <= maxLongitude && minLatitude <= y && y <= maxLatitude;
	}

	/**
	 * Set the name of this projection.
	 */
	public void setName( String name ) {
		this.name = name;
	}
	
	public String getName() {
		if ( name != null )
			return name;
		return toString();
	}

	/**
	 * Get a string which describes this projection in PROJ.4 format.
	 */
	public String getPROJ4Description() {
		AngleFormat format = new AngleFormat( AngleFormat.ddmmssPattern, false );
		StringBuffer sb = new StringBuffer();
		sb.append(
			"+proj="+getName()+
			" +a="+a
		);
		if ( es != 0 )
			sb.append( " +es="+es );
		sb.append( " +lon_0=" );
		format.format( projectionLongitude, sb, null );
		sb.append( " +lat_0=" );
		format.format( projectionLatitude, sb, null );
		if ( falseEasting != 1 )
			sb.append( " +x_0="+falseEasting );
		if ( falseNorthing != 1 )
			sb.append( " +y_0="+falseNorthing );
		if ( scaleFactor != 1 )
			sb.append( " +k="+scaleFactor );
		if ( fromMetres != 1 )
			sb.append( " +fr_meters="+fromMetres );
		return sb.toString();
	}

	public String toString() {
		return "None";
	}

	/**
	 * Set the minimum latitude. This is only used for Shape clipping and doesn't affect projection.
	 */
	public void setMinLatitude( double minLatitude ) {
		this.minLatitude = minLatitude;
	}
	
	public double getMinLatitude() {
		return minLatitude;
	}

	/**
	 * Set the maximum latitude. This is only used for Shape clipping and doesn't affect projection.
	 */
	public void setMaxLatitude( double maxLatitude ) {
		this.maxLatitude = maxLatitude;
	}
	
	public double getMaxLatitude() {
		return maxLatitude;
	}

	public double getMaxLatitudeDegrees() {
		return maxLatitude*RTD;
	}

	public double getMinLatitudeDegrees() {
		return minLatitude*RTD;
	}

	public void setMinLongitude( double minLongitude ) {
		this.minLongitude = minLongitude;
	}
	
	public double getMinLongitude() {
		return minLongitude;
	}

	public void setMinLongitudeDegrees( double minLongitude ) {
		this.minLongitude = DTR*minLongitude;
	}
	
	public double getMinLongitudeDegrees() {
		return minLongitude*RTD;
	}

	public void setMaxLongitude( double maxLongitude ) {
		this.maxLongitude = maxLongitude;
	}
	
	public double getMaxLongitude() {
		return maxLongitude;
	}

	public void setMaxLongitudeDegrees( double maxLongitude ) {
		this.maxLongitude = DTR*maxLongitude;
	}
	
	public double getMaxLongitudeDegrees() {
		return maxLongitude*RTD;
	}

	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude( double projectionLatitude ) {
		this.projectionLatitude = projectionLatitude;
	}
	
	public double getProjectionLatitude() {
		return projectionLatitude;
	}
	
	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitudeDegrees( double projectionLatitude ) {
		this.projectionLatitude = DTR*projectionLatitude;
	}
	
	public double getProjectionLatitudeDegrees() {
		return projectionLatitude*RTD;
	}
	
	/**
	 * Set the projection longitude in radians.
	 */
	public void setProjectionLongitude( double projectionLongitude ) {
		this.projectionLongitude = normalizeLongitudeRadians( projectionLongitude );
	}
	
	public double getProjectionLongitude() {
		return projectionLongitude;
	}
	
	/**
	 * Set the projection longitude in degrees.
	 */
	public void setProjectionLongitudeDegrees( double projectionLongitude ) {
		this.projectionLongitude = DTR*projectionLongitude;
	}
	
	public double getProjectionLongitudeDegrees() {
		return projectionLongitude*RTD;
	}
	
	/**
	 * Set the latitude of true scale in radians. This is only used by certain projections.
	 */
	public void setTrueScaleLatitude( double trueScaleLatitude ) {
		this.trueScaleLatitude = trueScaleLatitude;
	}
	
	public double getTrueScaleLatitude() {
		return trueScaleLatitude;
	}
	
	/**
	 * Set the latitude of true scale in degrees. This is only used by certain projections.
	 */
	public void setTrueScaleLatitudeDegrees( double trueScaleLatitude ) {
		this.trueScaleLatitude = DTR*trueScaleLatitude;
	}
	
	public double getTrueScaleLatitudeDegrees() {
		return trueScaleLatitude*RTD;
	}
	
	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude1( double projectionLatitude1 ) {
		this.projectionLatitude1 = projectionLatitude1;
	}
	
	public double getProjectionLatitude1() {
		return projectionLatitude1;
	}
	
	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitude1Degrees( double projectionLatitude1 ) {
		this.projectionLatitude1 = DTR*projectionLatitude1;
	}
	
	public double getProjectionLatitude1Degrees() {
		return projectionLatitude1*RTD;
	}
	
	/**
	 * Set the projection latitude in radians.
	 */
	public void setProjectionLatitude2( double projectionLatitude2 ) {
		this.projectionLatitude2 = projectionLatitude2;
	}
	
	public double getProjectionLatitude2() {
		return projectionLatitude2;
	}
	
	/**
	 * Set the projection latitude in degrees.
	 */
	public void setProjectionLatitude2Degrees( double projectionLatitude2 ) {
		this.projectionLatitude2 = DTR*projectionLatitude2;
	}
	
	public double getProjectionLatitude2Degrees() {
		return projectionLatitude2*RTD;
	}
	
  /**
   * Sets the alpha value.
   */
  public void setAlphaDegrees( double alpha ) {
    this.alpha = DTR * alpha;
  }
  
  /**
   * Gets the alpha value, in radians.
   * 
   * @return the alpha value
   */
  public double getAlpha()
  { 
    return alpha;
  }
  
  /**
   * Sets the lonc value.
   */
  public void setLonCDegrees( double lonc ) {
    this.lonc = DTR * lonc;
  }
  
  /**
   * Gets the lonc value, in radians.
   * 
   * @return the lonc value
   */
  public double getLonC()
  { 
    return lonc;
  }
  
  /**
   * Set the false Northing in projected units.
   */
  public void setFalseNorthing( double falseNorthing ) {
    this.falseNorthing = falseNorthing;
  }
  
	public double getFalseNorthing() {
		return falseNorthing;
	}
	
	/**
	 * Set the false Easting in projected units.
	 */
	public void setFalseEasting( double falseEasting ) {
		this.falseEasting = falseEasting;
	}
	
	public double getFalseEasting() {
		return falseEasting;
	}
	
  public void setSouthernHemisphere(boolean isSouth)
  {
    this.isSouth = isSouth;
  }
  
  public boolean getSouthernHemisphere() { return isSouth; }
  
	/**
	 * Set the projection scale factor. This is set to 1 by default.
	 * This value is called "k0" in PROJ.4.
	 */
	public void setScaleFactor( double scaleFactor ) {
		this.scaleFactor = scaleFactor;
	}

	/**
	 * Gets the projection scale factor.
   * This value is called "k0" in PROJ.4.
	 * 
	 * @return
	 */
	public double getScaleFactor() {
		return scaleFactor;
	}

	public double getEquatorRadius() {
		return a;
	}

	/**
	 * Set the conversion factor from metres to projected units. This is set to 1 by default.
	 */
	public void setFromMetres( double fromMetres ) {
		this.fromMetres = fromMetres;
	}
	
	public double getFromMetres() {
		return fromMetres;
	}
	
	public void setEllipsoid( Ellipsoid ellipsoid ) {
		this.ellipsoid = ellipsoid;
		a = ellipsoid.equatorRadius;
		e = ellipsoid.eccentricity;
		es = ellipsoid.eccentricity2;
	}
	
	public Ellipsoid getEllipsoid() {
		return ellipsoid;
	}

	/**
	 * Returns the ESPG code for this projection, or 0 if unknown.
	 */
	public int getEPSGCode() {
		return 0;
	}
	
  public void setUnits(Unit unit)
  {
    this.unit = unit;
  }
  
	/**
	 * Initialize the projection. This should be called after setting parameters and before using the projection.
	 * This is for performance reasons as initialization may be expensive.
	 */
	public void initialize() {
		spherical = (e == 0.0);
		one_es = 1-es;
		rone_es = 1.0/one_es;
		totalScale = a * fromMetres;
		totalFalseEasting = falseEasting * fromMetres;
		totalFalseNorthing = falseNorthing * fromMetres;		
	}

	public static float normalizeLongitude(float angle) {
		if ( Double.isInfinite(angle) || Double.isNaN(angle) )
			throw new InvalidValueException("Infinite or NaN longitude");
		while (angle > 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;
		return angle;
	}

	public static double normalizeLongitudeRadians( double angle ) {
		if ( Double.isInfinite(angle) || Double.isNaN(angle) )
			throw new InvalidValueException("Infinite or NaN longitude");
		while (angle > Math.PI)
			angle -= ProjectionMath.TWOPI;
		while (angle < -Math.PI)
			angle += ProjectionMath.TWOPI;
		return angle;
	}

}

>>>>>>> 76aa07461566a5976980e6696204781271955163

