package org.eclipse.core.filesystem.internal;

import org.eclipse.core.filesystem.IFileInfo;

/**
 * This class should be used by file system providers in their implementation
 * of API methods that return {@link IFileInfo} objects.
 * 
 * @since org.eclipse.core.filesystem 1.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FileInfo implements IFileInfo {
	/**
	 * Internal attribute indicating if the file is a directory
	 */
	private static final int ATTRIBUTE_DIRECTORY = 1 << 0;

	/**
	 * Internal attribute indicating if the file exists.
	 */
	private static final int ATTRIBUTE_EXISTS = 1 << 16;

	/**
	 * Bit field of file attributes. Initialized to specify a writable resource.
	 */
	private int attributes = IFileInfo.ATTRIBUTE_OWNER_WRITE | IFileInfo.ATTRIBUTE_OWNER_READ;

	/**
	 * The last modified time.
	 */
	private long lastModified = IFileInfo.NONE;

	/**
	 * The file length.
	 */
	private long length = IFileInfo.NONE;

	/**
	 * The file name.
	 */
	private String name = ""; //$NON-NLS-1$

	/**
	 * The target file name if this is a symbolic link
	 */
	private String linkTarget = null;

	/**
	 * Creates a new file information object with default values.
	 */
	public FileInfo() {
		super();
	}

	/**
	 * Creates a new file information object. All values except the file name
	 * will have default values.
	 * 
	 * @param name The name of this file
	 */
	public FileInfo(String name) {
		super();
		this.name = name;
	}

	/**
	 * Convenience method to clear a masked region of the attributes bit field.
	 * 
	 * @param mask The mask to be cleared
	 */
	private void clear(int mask) {
		attributes &= ~mask;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			//we know this object is cloneable
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return name.compareTo(((FileInfo) o).name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#exists()
	 */
	public boolean exists() {
		return getAttribute(ATTRIBUTE_EXISTS);
	}

	public boolean getAttribute(int attribute) {
		if (attribute == IFileInfo.ATTRIBUTE_READ_ONLY && isAttributeSuported(IFileInfo.ATTRIBUTE_OWNER_WRITE))
			return (!isSet(IFileInfo.ATTRIBUTE_OWNER_WRITE)) || isSet(IFileInfo.ATTRIBUTE_IMMUTABLE);
		else if (attribute == IFileInfo.ATTRIBUTE_EXECUTABLE && isAttributeSuported(IFileInfo.ATTRIBUTE_OWNER_EXECUTE))
			return isSet(IFileInfo.ATTRIBUTE_OWNER_EXECUTE);
		else
			return isSet(attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#getStringAttribute(int)
	 */
	public String getStringAttribute(int attribute) {
		if (attribute == IFileInfo.ATTRIBUTE_LINK_TARGET)
			return this.linkTarget;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#lastModified()
	 */
	public long getLastModified() {
		return lastModified;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#length()
	 */
	public long getLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#isDirectory()
	 */
	public boolean isDirectory() {
		return isSet(ATTRIBUTE_DIRECTORY);
	}

	private boolean isSet(long mask) {
		return (attributes & mask) != 0;
	}

	private void set(int mask) {
		attributes |= mask;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#setAttribute(int, boolean)
	 */
	public void setAttribute(int attribute, boolean value) {
		if (attribute == IFileInfo.ATTRIBUTE_READ_ONLY && isAttributeSuported(IFileInfo.ATTRIBUTE_OWNER_WRITE)) {
			if (value) {
				clear(IFileInfo.ATTRIBUTE_OWNER_WRITE | IFileInfo.ATTRIBUTE_OTHER_WRITE | IFileInfo.ATTRIBUTE_GROUP_WRITE);
				set(IFileInfo.ATTRIBUTE_IMMUTABLE);
			} else {
				set(IFileInfo.ATTRIBUTE_OWNER_WRITE | IFileInfo.ATTRIBUTE_OWNER_READ);
				clear(IFileInfo.ATTRIBUTE_IMMUTABLE);
			}
		} else if (attribute == IFileInfo.ATTRIBUTE_EXECUTABLE && isAttributeSuported(IFileInfo.ATTRIBUTE_OWNER_EXECUTE)) {
			if (value)
				set(IFileInfo.ATTRIBUTE_OWNER_EXECUTE);
			else
				clear(IFileInfo.ATTRIBUTE_OWNER_EXECUTE | IFileInfo.ATTRIBUTE_GROUP_EXECUTE | IFileInfo.ATTRIBUTE_OTHER_EXECUTE);
		} else {
			if (value)
				set(attribute);
			else
				clear(attribute);
		}
	}

	private static boolean isAttributeSuported(int value) {
		// TODO check this
//		return (LocalFileNativesManager.getSupportedAttributes() & value) != 0;
		return false;
	}

	/**
	 * Sets whether this is a file or directory.
	 * 
	 * @param value <code>true</code> if this is a directory, and <code>false</code>
	 * if this is a file.
	 */
	public void setDirectory(boolean value) {
		if (value)
			set(ATTRIBUTE_DIRECTORY);
		else
			clear(ATTRIBUTE_DIRECTORY);
	}

	/**
	 * Sets whether this file or directory exists.
	 * 
	 * @param value <code>true</code> if this file exists, and <code>false</code>
	 * otherwise.
	 */
	public void setExists(boolean value) {
		if (value)
			set(ATTRIBUTE_EXISTS);
		else
			clear(ATTRIBUTE_EXISTS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileInfo#setLastModified(long)
	 */
	public void setLastModified(long value) {
		lastModified = value;
	}

	/**
	 * Sets the length of this file. A value of {@link IFileInfo#NONE}
	 * indicates the file does not exist, is a directory, or the length could not be computed.
	 * 
	 * @param value the length of this file, or {@link IFileInfo#NONE}
	 */
	public void setLength(long value) {
		this.length = value;
	}

	/**
	 * Sets the name of this file.
	 * 
	 * @param name The file name
	 */
	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException();
		this.name = name;
	}

	/**
	 * Sets or clears a String attribute, e.g. symbolic link target.
	 * 
	 * @param attribute The kind of attribute to set. Currently only
	 * {@link IFileInfo#ATTRIBUTE_LINK_TARGET} is supported.
	 * @param value The String attribute, or <code>null</code> to clear
	 * the attribute
	 * @since org.eclipse.core.filesystem 1.1
	 */
	public void setStringAttribute(int attribute, String value) {
		if (attribute == IFileInfo.ATTRIBUTE_LINK_TARGET)
			this.linkTarget = value;
	}

	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return name;
	}
}
