package utils.parsing;

import static utils.DBC.require;

import java.io.Serializable;
import java.util.List;

import utils.DBC;
import utils.Position;

/**
 * Desciption of a location in a source code. Contains the the beginning position,
 * the end position and a description of the source file.
 *
 * @author imaman
 */
public class Location implements Serializable, Comparable<Location> {
   
   private final String sourceFileName;
   private final int sourceFileId;
   
   private static final long serialVersionUID = -2173713610989691041L;
   /**
    * Where does this location begin
    */
   public final Position begin;
   /**
    * Where does this location end
    */
   public final Position end;
   /**
    * True if this instance represents a synthetic location (that is a
    * location that was forced by the parser to represent a location in
    * another source file)
    */
   public final boolean synthetic;

   /**
    * Where was this location defined
    * 
    * @return The name of the source file object
    */
   public String enclosingFile() {
      return sourceFileName;
   }

   @Override public String toString() {
      return begin.toString() + (begin.equals(end) ? "" : ".." + end);
   }

   /**
    * Provide a short textual description of this location
    * 
    * @return the string "Line NNN" where NNN is the line where this
    *         location begins.
    * @see #toString()
    */
   public String where() {
      return "Line " + begin.line;
   }

   /**
    * Create a location from a {@link TokenInfo}
    * 
    * @param inputResource InputResource to which this locations belongs 
    * @param t
    *        a token with the positioning information to initialize the
    *        newly created {@link Location}
    */
   public Location(InputResource inputResource, TokenInfo t) {
      this(inputResource, t.beginLine, t.beginColumn, t.endLine, t.endColumn, 
         false);
   }

   public Location(InputResource env, int beginLine, int beginColumn, int endLine, int endColumn) {
      this(env, beginLine, beginColumn, endLine, endColumn, false);
   }

   /**
    * Create a new {@link Location} object
    * 
    * @param inputResource InputResource to which this locations belongs 
    * @param beginLine
    *        the line where this {@link Location} begins
    * @param beginColumn
    *        the column where this {@link Location} begins
    * @param endLine
    *        the line where this {@link Location} ends
    * @param endColumn
    *        the columns where this {@link Location} begins
    * @param synthetic
    *        Does this location represent a position in another source
    *        file?
    */
   public Location(InputResource inputResource, int beginLine, int beginColumn, 
      int endLine, int endColumn, boolean synthetic) {
      require(inputResource != null);
      
      sourceFileName = inputResource.name();
      sourceFileId = inputResource.id();
      
      require(endLine > beginLine || endColumn >= beginColumn);
      begin = new Position(beginLine, beginColumn);
      end = new Position(endLine, endColumn);
      this.synthetic = synthetic;
   }

   /**
    * Create a {@link Location} from its begin and end {@link Position}s
    * 
    * @param inputResource InputResource to which this locations belongs 
    * @param begin
    *        where does this {@link Location} begin
    * @param end
    *        where does this {@link Location} end
    */
   public Location(InputResource inputResource, Position begin, Position end) {
      require(!end.before(begin));
      require(inputResource != null);
      sourceFileName = inputResource.name();
      sourceFileId = inputResource.id();
      this.begin = begin;
      this.end = end;
      this.synthetic = false;
   }

   private Location(int sourceFileId, String sourceFileName, Position b, Position e, boolean synthetic)
   {
      this(sourceFileId, sourceFileName, b.line, b.column, e.line, e.column, synthetic);
   }

   private Location(int sourceFileId, String sourceFileName, int bl, int bc, int el, int ec, boolean synthetic)
   {
      this.sourceFileId = sourceFileId;
      this.sourceFileName = sourceFileName;
      this.begin = new Position(bl, bc);
      this.end = new Position(el, ec);
      this.synthetic = synthetic;
   }
   
   public boolean startsBefore(Location other) {
      return begin.before(other.begin);
   }

   public boolean endsBefore(Location other) {
      return end.before(other.end);
   }

   public boolean contained(Location other) {
      if (other == null)
         return false;
      return !startsBefore(other) && !other.endsBefore(this);
   }

   public Location merge(Location other) {
      DBC.require(other == null || this.sourceFileId == other.sourceFileId);
      if (other == null)
         return this;
      return new Location(sourceFileId, sourceFileName, 
         startsBefore(other) ? begin : other.begin, 
         endsBefore(other) ? other.end : end, 
         this.synthetic || other.synthetic);
   }

   public int compareTo(Location other) {
      int diff = this.sourceFileId - other.sourceFileId;
      if(diff != 0)
         return diff;
      return begin.compareTo(other.begin);
   }

   @Override public int hashCode() {
      return begin.hashCode() ^ end.hashCode();
   }

   @Override public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || !getClass().equals(o.getClass()))
         return false;
      Location that = (Location) o;
      return (sourceFileId == that.sourceFileId) && begin.equals(that.begin) && end.equals(that.end);
   }

   public String getSourceCode(List<String> sourceCodeLines)
   {
      StringBuilder sb = new StringBuilder(100);
      
      for(int i = this.begin.line; i <= this.end.line; ++i)
         sb.append(sourceCodeLines.get(i));
      
      return sb.toString();
   }

   public Location shiftLines(int delta)
   {
      return new Location(sourceFileId, sourceFileName, begin.line + delta, 
         begin.column, end.line + delta, end.column, this.synthetic);
   }
}

