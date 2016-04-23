package ralfherzog.pwman3.database.sqlite.column;

import android.database.sqlite.SQLiteException;

public class SQLiteColumn {
	private String name;
	private int primitiveType = SQLiteColumnType.TYPE_NULL;
	
	private Boolean isPrimaryKey;
	private Boolean isNull;
	private Boolean isAutoIncrement;
	private Boolean isUnsigned;
	private Boolean isUnique;
	
	private String comment;
	private String extraOptions;
	private String defaultValue;
	
	public SQLiteColumn( String columnName, String columnType ) throws SQLiteException {
		name = columnName;
		setType( columnType );
	}

	public String getTypeAsString() {
		switch( primitiveType ) {
			case SQLiteColumnType.TYPE_BLOB:
				return "BLOB";
			case SQLiteColumnType.TYPE_INTEGER:
				return "INTEGER";
			case SQLiteColumnType.TYPE_REAL:
				return "REAL";
			case SQLiteColumnType.TYPE_TEXT:
				return "TEXT";
			case SQLiteColumnType.TYPE_NULL:
				return "NULL";
		}
		return "NULL";
	}

	private void setType( String columnType ) throws SQLiteException {
		if ( columnType.equals("NULL") ) {
			primitiveType = SQLiteColumnType.TYPE_NULL;
		} else if ( columnType.equals("INTEGER") ) {
			primitiveType = SQLiteColumnType.TYPE_INTEGER;
		} else if ( columnType.equals("REAL") ) {
			primitiveType = SQLiteColumnType.TYPE_REAL;
		} else if ( columnType.equals("TEXT") ) {
			primitiveType = SQLiteColumnType.TYPE_TEXT;
		} else if ( columnType.equals("BLOB") ) {
			primitiveType = SQLiteColumnType.TYPE_BLOB;
		} else {
			throw new SQLiteException( "Not a supported type: " + columnType );
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPrimitiveType() {
		return primitiveType;
	}

	public void setPrimitiveType(int primitiveType) {
		this.primitiveType = primitiveType;
	}

	public Boolean getIsPrimaryKey() {
		return isPrimaryKey;
	}

	public void setIsPrimaryKey(Boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public Boolean getIsNull() {
		return isNull;
	}

	public void setIsNull(Boolean isNull) {
		this.isNull = isNull;
	}

	public Boolean getIsAutoIncrement() {
		return isAutoIncrement;
	}

	public void setIsAutoIncrement(Boolean isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}

	public Boolean getIsUnsigned() {
		return isUnsigned;
	}

	public void setIsUnsigned(Boolean isUnsigned) {
		this.isUnsigned = isUnsigned;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getExtraOptions() {
		return extraOptions;
	}

	public void setExtraOptions(String extraOptions) {
		this.extraOptions = extraOptions;
	}

	public void setUnique(Boolean isUnique) {
		this.isUnique = isUnique;
	}
	
	public Boolean getIsUnique() {
		return isUnique;
	}

	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getDefault() {
		return defaultValue;
	}
}

