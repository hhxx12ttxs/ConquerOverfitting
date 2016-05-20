package com.framedobjects.dashwell.db.meta;

/**
 * @author Jens Richnow
 * 
 */
public class MetaField {

  public static final String TYPE_CURRENCY = "Currency";
  public static final String TYPE_DATE_TIME = "DateTime";
  public static final String TYPE_DECIMAL = "Decimal";
  public static final String TYPE_INTEGER = "Integer";
  public static final String TYPE_STRING = "String";
  public static final String TYPE_UNKNOWN = "Unknown";
  private int fieldID = 0;
  private int tableID = 0;
  private int connectionID = 0;
  private String qualifiedName = null;
  private String tableName = null;
  private String name = null;
  private int fieldType = 0;
  private String type = null;
  private String nativeType = "";
  private String size = null; // column_size.
  private int decimalDigits = 0;
  private String readOnly = null;
  private String uniqueKey = null;
  private String description = null;
  
  public MetaField(){
  	
  }
  
  public MetaField(int fieldID, int tableID, String name, int fieldType,
  		String nativeType, String description, int columnSize, int decimalDigits){
  	this.fieldID = fieldID;
  	this.tableID = tableID;
  	this.name = name;
  	this.fieldType = fieldType;
  	this.nativeType = nativeType;
  	this.description = description;
  	this.size = String.valueOf(columnSize);
  	this.decimalDigits = decimalDigits;
  }

  /**
   * @return Returns the description.
   */
  public String getDescription() {
    return description == null ? "" : description;
  }

  /**
   * @param description
   *          The description to set.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the primaryKey.
   */
  public String getUniqueKey() {
    return uniqueKey == null ? "" : uniqueKey;
  }

  /**
   * @param primaryKey
   *          The primaryKey to set.
   */
  public void setUniqueKey(String primaryKey) {
    this.uniqueKey = primaryKey;
  }

  /**
   * @return Returns the readOnly.
   */
  public String getReadOnly() {
    return readOnly == null ? "" : readOnly;
  }

  /**
   * @param readOnly
   *          The readOnly to set.
   */
  public void setReadOnly(String readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * @return Returns the size.
   */
  public String getSize() {
    return size == null ? "" : size;
  }

  /**
   * @param size
   *          The size to set.
   */
  public void setSize(String size) {
    this.size = size;
  }

  /**
   * @return Returns the type.
   */
  public String getType() {
    return type == null ? "" : type;
  }

  /**
   * @param type
   *          The type to set.
   */
  public void setType(String type) {
    this.type = type;
  }

  public int getDecimalDigits() {
    return decimalDigits;
  }

  public void setDecimalDigits(int decimalDigits) {
    this.decimalDigits = decimalDigits;
  }

  /**
   * @return Returns the nativeSize.
   */
  public String getNativeType() {
    return nativeType == null ? "" : nativeType;
  }

  /**
   * @param nativeSize
   *          The nativeSize to set.
   */
  public void setNativeType(String nativeSize) {
    this.nativeType = nativeSize;
  }

  /**
   * @return Returns the connectionID.
   */
  public int getConnectionID() {
    return connectionID;
  }

  /**
   * @param connectionID
   *          The connectionID to set.
   */
  public void setConnectionID(int connectionID) {
    this.connectionID = connectionID;
  }

  /**
   * @return Returns the fieldID.
   */
  public int getFieldID() {
    return fieldID;
  }

  /**
   * @param fieldID
   *          The fieldID to set.
   */
  public void setFieldID(int fieldID) {
    this.fieldID = fieldID;
  }

  /**
   * @return Returns the tableID.
   */
  public int getTableID() {
    return tableID;
  }

  /**
   * @param tableID
   *          The tableID to set.
   */
  public void setTableID(int tableID) {
    this.tableID = tableID;
  }

  /**
   * @return Returns the fieldType.
   */
  public int getFieldType() {
    return fieldType;
  }

  /**
   * @param fieldType
   *          The fieldType to set.
   */
  public void setFieldType(int fieldType) {
    this.fieldType = fieldType;
  }

  /**
   * @return Returns the qualifiedName.
   */
  public String getQualifiedName() {
    if (qualifiedName == null){
      qualifiedName = tableName + "." + name;
    }
    return qualifiedName;
  }

  /**
   * @param qualifiedName The qualifiedName to set.
   */
  public void setQualifiedName(String qualifiedName) {
    this.qualifiedName = qualifiedName;
  }

  /**
   * @return Returns the tableName.
   */
  public String getTableName() {
    return tableName == null ? "" : tableName;
  }

  /**
   * @param tableName The tableName to set.
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
}

