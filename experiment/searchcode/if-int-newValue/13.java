package com.objectwave.utility;

/**
 * This type was created in VisualAge.
 */
public class DebugOutput extends java.io.OutputStream
{
	public static final int OFF = 0;
	public static final int LOW = 1;
	public static final int MEDIUM = 5;
	public static final int HIGH = 10;

	private int level = OFF;
	private String prefix = "Debug: ";
	private java.text.DateFormat dateFormat = null;
	private boolean includeThreadHash;
	private java.io.OutputStream outputStream = System.out;
	private java.io.Writer outputWriter;
	private static DebugOutput defaultOutput = new DebugOutput();

/**
 * DebugOutput constructor comment.
 */
public DebugOutput() {
	super();
}
/**
 * This method was created in VisualAge.
 * @param level int
 */
public DebugOutput(int level)
{
	this.level = level;
}
/**
 * This method was created in VisualAge.
 * @param level int
 * @param prefix java.lang.String
 */
public DebugOutput(int level, String prefix)
{
	this.level = level;
	this.prefix = prefix;
}
/**
 * This method was created in VisualAge.
 * @param level int
 * @param prefix java.lang.String
 * @param dateFormat java.text.DateFormat
 */
public DebugOutput(int level, String prefix, java.text.DateFormat dateFormat)
{
	this.level = level;
	this.prefix = prefix;
	this.dateFormat = dateFormat;
}
/**
 * This method was created in VisualAge.
 */
public void close() throws java.io.IOException
{
	flush();
}
/**
 * This method was created in VisualAge.
 */
public void flush() throws java.io.IOException
{
	getOutputStream().flush();
}
/**
 * This method was created in VisualAge.
 * @return java.text.DateFormat
 */
public java.text.DateFormat getDateFormat() {
	return dateFormat;
}
	public static DebugOutput getDefault() { return defaultOutput; }
/**
 * This method was created in VisualAge.
 * @return boolean
 */
public boolean getIncludeThreadHash() {
	return includeThreadHash;
}
/**
 * This method was created in VisualAge.
 * @return int
 */
public int getLevel() {
	return level;
}
/**
 * This method was created in VisualAge.
 * @return java.io.OutputStream
 */
public java.io.OutputStream getOutputStream()
{
	if (getOutputWriter() == null)
		setOutputWriter(new java.io.OutputStreamWriter(outputStream));
	return outputStream;
}
/**
 * This method was created in VisualAge.
 * @return java.io.Writer
 */
protected java.io.Writer getOutputWriter()
{
	if (outputWriter == null && outputStream != null)
		outputWriter = new java.io.OutputStreamWriter(outputStream);
	return outputWriter;
}
/**
 * This method was created in VisualAge.
 * @return java.lang.String
 */
public String getPrefix() {
	return prefix;
}
/**
 * Print
 * @param level int
 * @param string java.lang.String
 */
public void println(int level, String string)
{
	if (level > this.level)
		return;

	String prefix = "";
	if (getIncludeThreadHash())
		prefix += "(" + Thread.currentThread().hashCode() + ")";
	if (getDateFormat() != null)
		prefix += getDateFormat().format(new java.util.Date());
	prefix += getPrefix();

	try { getOutputWriter().write(prefix + string + "\n"); getOutputWriter().flush(); }
	catch (java.io.IOException e) { }
}
/**
 * This method was created in VisualAge.
 * @param newValue java.text.DateFormat
 */
public void setDateFormat(java.text.DateFormat newValue) {
	this.dateFormat = newValue;
}
/**
 * This method was created in VisualAge.
 * @param newValue boolean
 */
public void setIncludeThreadHash(boolean newValue) {
	this.includeThreadHash = newValue;
}
/**
 * This method was created in VisualAge.
 * @param newValue int
 */
public void setLevel(int newValue) {
	this.level = newValue;
}
/**
 * This method was created in VisualAge.
 * @param newValue java.io.OutputStream
 */
public void setOutputStream(java.io.OutputStream newValue)
{
	this.outputStream = newValue;
	setOutputWriter(new java.io.OutputStreamWriter(outputStream));
}
/**
 * This method was created in VisualAge.
 * @param newValue java.io.Writer
 */
protected void setOutputWriter(java.io.Writer newValue) {
	this.outputWriter = newValue;
}
/**
 * This method was created in VisualAge.
 * @param newValue java.lang.String
 */
public void setPrefix(String newValue) {
	this.prefix = newValue;
}
/**
 * This method was created in VisualAge.
 * @param b byte[]
 * @param off int
 * @param len int
 */
public void write(byte b[], int off, int len)  throws java.io.IOException
{
	getOutputStream().write(b, off, len);
}
/**
 * This method was created in VisualAge.
 * @param b int
 */
public void write(int b) throws java.io.IOException
{
	getOutputStream().write(b);
}
}
