package il.ac.technion.cs.ssdl.parsing;

import static il.ac.technion.cs.ssdl.utils.DBC.require;
import il.ac.technion.cs.ssdl.utils.DBC;
import il.ac.technion.cs.ssdl.utils.IO;
import il.ac.technion.cs.ssdl.utils.Position;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Desciption of a location in a source code. Contains the the beginning
 * position, the end position and a description of the source file.
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
	 * True if this instance represents a synthetic location (that is a location
	 * that was forced by the parser to represent a location in another source
	 * file)
	 */
	public final boolean synthetic;
	/**
	 * The actual piece of source code that this object specifes
	 */
	private final String fragment;

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
	 * @return the string "Line NNN" where NNN is the line where this location
	 *         begins.
	 * @see #toString()
	 */
	public String where() {
		return "Line " + begin.line;
	}

	/**
	 * Create a location from a {@link TokenInfo}
	 * 
	 * @param inputResource
	 *        InputResource to which this locations belongs
	 * @param t
	 *        a token with the positioning information to initialize the newly
	 *        created {@link Location}
	 */
	public Location(InputResource inputResource, TokenInfo t) {
		this(inputResource, t.beginLine, t.beginColumn, t.endLine, t.endColumn, false);
	}

	public Location(InputResource ir, int beginLine, int beginColumn, int endLine, int endColumn) {
		this(ir, beginLine, beginColumn, endLine, endColumn, false);
	}

	/**
	 * Create a new {@link Location} object
	 * 
	 * @param inputResource
	 *        InputResource to which this locations belongs
	 * @param beginLine
	 *        the line where this {@link Location} begins
	 * @param beginColumn
	 *        the column where this {@link Location} begins
	 * @param endLine
	 *        the line where this {@link Location} ends
	 * @param endColumn
	 *        the columns where this {@link Location} begins
	 * @param synthetic
	 *        Does this location represent a position in another source file?
	 */
	public Location(InputResource inputResource, int beginLine, int beginColumn, int endLine, int endColumn, boolean synthetic) {
		require(inputResource != null);
		sourceFileName = inputResource.name();
		sourceFileId = inputResource.id();
		require(endLine > beginLine || endColumn >= beginColumn);
		begin = new Position(beginLine, beginColumn);
		end = new Position(endLine, endColumn);
		this.synthetic = synthetic;
		fragment = computeFragment(inputResource);
	}

	/**
	 * Create a {@link Location} from its begin and end {@link Position}s
	 * 
	 * @param inputResource
	 *        InputResource to which this locations belongs
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
		synthetic = false;
		fragment = computeFragment(inputResource);
	}

	private Location(int sourceFileId, String sourceFileName, Position b, Position e, boolean synthetic, String fragment) {
		this(sourceFileId, sourceFileName, b.line, b.column, e.line, e.column, synthetic, fragment);
	}

	private Location(int sourceFileId, String sourceFileName, int bl, int bc, int el, int ec, boolean synthetic, String fragment) {
		this.sourceFileId = sourceFileId;
		this.sourceFileName = sourceFileName;
		begin = new Position(bl, bc);
		end = new Position(el, ec);
		this.synthetic = synthetic;
		this.fragment = fragment;
	}

	private String computeFragment(InputResource ir) {
		try {
			List<String> lines = ir.lines();
			DBC.require(end.line < lines.size(), "end=" + end + " lines.size=" + lines.size() + " lines=\n" + IO.concatLines(lines));
		
			StringBuilder sb = new StringBuilder(100);
			for (int i = begin.line; i <= end.line; ++i)
				sb.append(lines.get(i));
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		require(other == null || sourceFileId == other.sourceFileId);
		if (other == null)
			return this;
		return new Location(sourceFileId, sourceFileName, startsBefore(other) ? begin : other.begin, endsBefore(other) ? other.end : end, synthetic
		        || other.synthetic, startsBefore(other) ? fragment : other.fragment);
	}

	public int compareTo(Location other) {
		int diff = sourceFileId - other.sourceFileId;
		if (diff != 0)
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
		return sourceFileId == that.sourceFileId && begin.equals(that.begin) && end.equals(that.end);
	}

	// public Location shiftLines(int delta) {
	// return new Location(sourceFileId, sourceFileName, begin.line + delta,
	// begin.column, end.line + delta, end.column, this.synthetic);
	// }
	public String getSourceCode() {
		return fragment;
	}
}

