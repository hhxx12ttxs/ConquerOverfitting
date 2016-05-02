// ORM class for table 'test'
// WARNING: This class is AUTO-GENERATED. Modify at your own risk.
//
// Debug information:
// Generated date: Sat Apr 26 16:29:18 PDT 2014
// For connector: org.apache.sqoop.manager.PostgresqlManager
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.lib.db.DBWritable;
import com.cloudera.sqoop.lib.JdbcWritableBridge;
import com.cloudera.sqoop.lib.DelimiterSet;
import com.cloudera.sqoop.lib.FieldFormatter;
import com.cloudera.sqoop.lib.RecordParser;
import com.cloudera.sqoop.lib.BooleanParser;
import com.cloudera.sqoop.lib.BlobRef;
import com.cloudera.sqoop.lib.ClobRef;
import com.cloudera.sqoop.lib.LargeObjectLoader;
import com.cloudera.sqoop.lib.SqoopRecord;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class test extends SqoopRecord  implements DBWritable, Writable {
  private final int PROTOCOL_VERSION = 3;
  public int getClassFormatVersion() { return PROTOCOL_VERSION; }
  protected ResultSet __cur_result_set;
  private String vid;
  public String get_vid() {
    return vid;
  }
  public void set_vid(String vid) {
    this.vid = vid;
  }
  public test with_vid(String vid) {
    this.vid = vid;
    return this;
  }
  private Double ratio;
  public Double get_ratio() {
    return ratio;
  }
  public void set_ratio(Double ratio) {
    this.ratio = ratio;
  }
  public test with_ratio(Double ratio) {
    this.ratio = ratio;
    return this;
  }
  private String category;
  public String get_category() {
    return category;
  }
  public void set_category(String category) {
    this.category = category;
  }
  public test with_category(String category) {
    this.category = category;
    return this;
  }
  private String dancer;
  public String get_dancer() {
    return dancer;
  }
  public void set_dancer(String dancer) {
    this.dancer = dancer;
  }
  public test with_dancer(String dancer) {
    this.dancer = dancer;
    return this;
  }
  private String description;
  public String get_description() {
    return description;
  }
  public void set_description(String description) {
    this.description = description;
  }
  public test with_description(String description) {
    this.description = description;
    return this;
  }
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof test)) {
      return false;
    }
    test that = (test) o;
    boolean equal = true;
    equal = equal && (this.vid == null ? that.vid == null : this.vid.equals(that.vid));
    equal = equal && (this.ratio == null ? that.ratio == null : this.ratio.equals(that.ratio));
    equal = equal && (this.category == null ? that.category == null : this.category.equals(that.category));
    equal = equal && (this.dancer == null ? that.dancer == null : this.dancer.equals(that.dancer));
    equal = equal && (this.description == null ? that.description == null : this.description.equals(that.description));
    return equal;
  }
  public void readFields(ResultSet __dbResults) throws SQLException {
    this.__cur_result_set = __dbResults;
    this.vid = JdbcWritableBridge.readString(1, __dbResults);
    this.ratio = JdbcWritableBridge.readDouble(2, __dbResults);
    this.category = JdbcWritableBridge.readString(3, __dbResults);
    this.dancer = JdbcWritableBridge.readString(4, __dbResults);
    this.description = JdbcWritableBridge.readString(5, __dbResults);
  }
  public void loadLargeObjects(LargeObjectLoader __loader)
      throws SQLException, IOException, InterruptedException {
  }
  public void write(PreparedStatement __dbStmt) throws SQLException {
    write(__dbStmt, 0);
  }

  public int write(PreparedStatement __dbStmt, int __off) throws SQLException {
    JdbcWritableBridge.writeString(vid, 1 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeDouble(ratio, 2 + __off, 8, __dbStmt);
    JdbcWritableBridge.writeString(category, 3 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeString(dancer, 4 + __off, 12, __dbStmt);
    JdbcWritableBridge.writeString(description, 5 + __off, 12, __dbStmt);
    return 5;
  }
  public void readFields(DataInput __dataIn) throws IOException {
    if (__dataIn.readBoolean()) { 
        this.vid = null;
    } else {
    this.vid = Text.readString(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.ratio = null;
    } else {
    this.ratio = Double.valueOf(__dataIn.readDouble());
    }
    if (__dataIn.readBoolean()) { 
        this.category = null;
    } else {
    this.category = Text.readString(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.dancer = null;
    } else {
    this.dancer = Text.readString(__dataIn);
    }
    if (__dataIn.readBoolean()) { 
        this.description = null;
    } else {
    this.description = Text.readString(__dataIn);
    }
  }
  public void write(DataOutput __dataOut) throws IOException {
    if (null == this.vid) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, vid);
    }
    if (null == this.ratio) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    __dataOut.writeDouble(this.ratio);
    }
    if (null == this.category) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, category);
    }
    if (null == this.dancer) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, dancer);
    }
    if (null == this.description) { 
        __dataOut.writeBoolean(true);
    } else {
        __dataOut.writeBoolean(false);
    Text.writeString(__dataOut, description);
    }
  }
  private final DelimiterSet __outputDelimiters = new DelimiterSet((char) 44, (char) 10, (char) 0, (char) 0, false);
  public String toString() {
    return toString(__outputDelimiters, true);
  }
  public String toString(DelimiterSet delimiters) {
    return toString(delimiters, true);
  }
  public String toString(boolean useRecordDelim) {
    return toString(__outputDelimiters, useRecordDelim);
  }
  public String toString(DelimiterSet delimiters, boolean useRecordDelim) {
    StringBuilder __sb = new StringBuilder();
    char fieldDelim = delimiters.getFieldsTerminatedBy();
    __sb.append(FieldFormatter.escapeAndEnclose(vid==null?"null":vid, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(ratio==null?"null":"" + ratio, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(category==null?"null":category, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(dancer==null?"null":dancer, delimiters));
    __sb.append(fieldDelim);
    __sb.append(FieldFormatter.escapeAndEnclose(description==null?"null":description, delimiters));
    if (useRecordDelim) {
      __sb.append(delimiters.getLinesTerminatedBy());
    }
    return __sb.toString();
  }
  private final DelimiterSet __inputDelimiters = new DelimiterSet((char) 9, (char) 10, (char) 0, (char) 13, false);
  private RecordParser __parser;
  public void parse(Text __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(CharSequence __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(byte [] __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(char [] __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(ByteBuffer __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  public void parse(CharBuffer __record) throws RecordParser.ParseError {
    if (null == this.__parser) {
      this.__parser = new RecordParser(__inputDelimiters);
    }
    List<String> __fields = this.__parser.parseRecord(__record);
    __loadFromFields(__fields);
  }

  private void __loadFromFields(List<String> fields) {
    Iterator<String> __it = fields.listIterator();
    String __cur_str;
    __cur_str = __it.next();
    if (__cur_str.equals("null")) { this.vid = null; } else {
      this.vid = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null") || __cur_str.length() == 0) { this.ratio = null; } else {
      this.ratio = Double.valueOf(__cur_str);
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null")) { this.category = null; } else {
      this.category = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null")) { this.dancer = null; } else {
      this.dancer = __cur_str;
    }

    __cur_str = __it.next();
    if (__cur_str.equals("null")) { this.description = null; } else {
      this.description = __cur_str;
    }

  }

  public Object clone() throws CloneNotSupportedException {
    test o = (test) super.clone();
    return o;
  }

  public Map<String, Object> getFieldMap() {
    Map<String, Object> __sqoop$field_map = new TreeMap<String, Object>();
    __sqoop$field_map.put("vid", this.vid);
    __sqoop$field_map.put("ratio", this.ratio);
    __sqoop$field_map.put("category", this.category);
    __sqoop$field_map.put("dancer", this.dancer);
    __sqoop$field_map.put("description", this.description);
    return __sqoop$field_map;
  }

  public void setField(String __fieldName, Object __fieldVal) {
    if ("vid".equals(__fieldName)) {
      this.vid = (String) __fieldVal;
    }
    else    if ("ratio".equals(__fieldName)) {
      this.ratio = (Double) __fieldVal;
    }
    else    if ("category".equals(__fieldName)) {
      this.category = (String) __fieldVal;
    }
    else    if ("dancer".equals(__fieldName)) {
      this.dancer = (String) __fieldVal;
    }
    else    if ("description".equals(__fieldName)) {
      this.description = (String) __fieldVal;
    }
    else {
      throw new RuntimeException("No such field: " + __fieldName);
    }
  }
}

