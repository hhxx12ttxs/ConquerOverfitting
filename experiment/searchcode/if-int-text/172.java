/*
 * Buffer.java - jEdit buffer
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1998, 2003 Slava Pestov
 * Portions copyright (C) 1999, 2000 mike dillon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit;

//{{{ Imports
import gnu.regexp.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.search.RESearchMatcher;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.util.*;
//}}}

/**
 * A <code>Buffer</code> represents the contents of an open text
 * file as it is maintained in the computer's memory (as opposed to
 * how it may be stored on a disk).<p>
 *
 * In a BeanShell script, you can obtain the current buffer instance from the
 * <code>buffer</code> variable.<p>
 *
 * This class does not have a public constructor.
 * Buffers can be opened and closed using methods in the <code>jEdit</code>
 * class.<p>
 *
 * This class is partially thread-safe, however you must pay attention to two
 * very important guidelines:
 * <ul>
 * <li>Changes to a buffer can only be made from the AWT thread.
 * <li>When accessing the buffer from another thread, you must
 * grab a read lock if you plan on performing more than one call, to ensure that
 * the buffer contents are not changed by the AWT thread for the duration of the
 * lock. Only methods whose descriptions specify thread safety can be invoked
 * from other threads.
 * </ul>
 *
 * @author Slava Pestov
 * @version $Id: Buffer.java 4819 2003-07-03 21:06:33Z spestov $
 */
public class Buffer
{
	//{{{ Some constants
	/**
	 * Line separator property.
	 */
	public static final String LINESEP = "lineSeparator";

	/**
	 * Backed up property.
	 * @since jEdit 3.2pre2
	 */
	public static final String BACKED_UP = "Buffer__backedUp";

	/**
	 * Caret info properties.
	 * @since jEdit 3.2pre1
	 */
	public static final String CARET = "Buffer__caret";
	public static final String SELECTION = "Buffer__selection";

	/**
	 * This should be a physical line number, so that the scroll
	 * position is preserved correctly across reloads (which will
	 * affect virtual line numbers, due to fold being reset)
	 */
	public static final String SCROLL_VERT = "Buffer__scrollVert";
	public static final String SCROLL_HORIZ = "Buffer__scrollHoriz";

	/**
	 * Character encoding used when loading and saving.
	 * @since jEdit 3.2pre4
	 */
	public static final String ENCODING = "encoding";

	/**
	 * This property is set to 'true' if the file has a trailing newline.
	 * @since jEdit 4.0pre1
	 */
	public static final String TRAILING_EOL = "trailingEOL";

	/**
	 * This property is set to 'true' if the file should be GZipped.
	 * @since jEdit 4.0pre4
	 */
	public static final String GZIPPED = "gzipped";
	//}}}

	//{{{ Input/output methods

	//{{{ showInsertFileDialog() method
	/**
	 * Displays the 'insert file' dialog box and inserts the selected file
	 * into the buffer.
	 * @param view The view
	 * @since jEdit 2.7pre2
	 */
	public void showInsertFileDialog(View view)
	{
		String[] files = GUIUtilities.showVFSFileDialog(view,null,
			VFSBrowser.OPEN_DIALOG,false);

		if(files != null)
			insertFile(view,files[0]);
	} //}}}

	//{{{ reload() method
	/**
	 * Reloads the buffer from disk, asking for confirmation if the buffer
	 * has unsaved changes.
	 * @param view The view
	 * @since jEdit 2.7pre2
	 */
	public void reload(View view)
	{
		if(getFlag(DIRTY))
		{
			String[] args = { name };
			int result = GUIUtilities.confirm(view,"changedreload",
				args,JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if(result != JOptionPane.YES_OPTION)
				return;
		}

		view.getEditPane().saveCaretInfo();
		load(view,true);
	} //}}}

	//{{{ load() method
	/**
	 * Loads the buffer from disk, even if it is loaded already.
	 * @param view The view
	 * @param reload If true, user will not be asked to recover autosave
	 * file, if any
	 *
	 * @since 2.5pre1
	 */
	public boolean load(final View view, final boolean reload)
	{
		if(isPerformingIO())
		{
			GUIUtilities.error(view,"buffer-multiple-io",null);
			return false;
		}

		setBooleanProperty(BufferIORequest.ERROR_OCCURRED,false);

		setFlag(LOADING,true);

		// view text areas temporarily blank out while a buffer is
		// being loaded, to indicate to the user that there is no
		// data available yet.
		if(!getFlag(TEMPORARY))
			EditBus.send(new BufferUpdate(this,view,BufferUpdate.LOAD_STARTED));

		final boolean loadAutosave;

		if(reload || !getFlag(NEW_FILE))
		{
			if(file != null)
				modTime = file.lastModified();

			// Only on initial load
			if(!reload && autosaveFile != null && autosaveFile.exists())
				loadAutosave = recoverAutosave(view);
			else
			{
				if(autosaveFile != null)
					autosaveFile.delete();
				loadAutosave = false;
			}

			if(!loadAutosave)
			{
				VFS vfs = VFSManager.getVFSForPath(path);

				if(!checkFileForLoad(view,vfs,path))
				{
					setFlag(LOADING,false);
					return false;
				}

				if(isNewFile())
					/* ie, checkFileForLoad() set this */;
				else
				{
					if(!vfs.load(view,this,path))
					{
						setFlag(LOADING,false);
						return false;
					}
				}
			}
		}
		else
			loadAutosave = false;

		//{{{ Do some stuff once loading is finished
		Runnable runnable = new Runnable()
		{
			public void run()
			{
				String newPath = getStringProperty(
					BufferIORequest.NEW_PATH);
				Segment seg = (Segment)getProperty(
					BufferIORequest.LOAD_DATA);
				IntegerArray endOffsets = (IntegerArray)
					getProperty(BufferIORequest.END_OFFSETS);

				if(seg == null)
					seg = new Segment(new char[1024],0,0);
				if(endOffsets == null)
				{
					endOffsets = new IntegerArray();
					endOffsets.add(1);
				}

				try
				{
					writeLock();

					// For `reload' command
					firePreContentRemoved(0,0,getLineCount()
						- 1,getLength());

					contentMgr.remove(0,getLength());
					lineMgr.contentRemoved(0,0,getLineCount()
						- 1,getLength());
					positionMgr.contentRemoved(0,getLength());
					fireContentRemoved(0,0,getLineCount()
						- 1,getLength());

					// theoretically a segment could
					// have seg.offset != 0 but
					// SegmentBuffer never does that
					contentMgr._setContent(seg.array,seg.count);

					lineMgr._contentInserted(endOffsets);
					positionMgr.contentInserted(0,seg.count);

					fireContentInserted(0,0,
						endOffsets.getSize() - 1,
						seg.count - 1);
				}
				finally
				{
					writeUnlock();
				}

				unsetProperty(BufferIORequest.LOAD_DATA);
				unsetProperty(BufferIORequest.END_OFFSETS);
				unsetProperty(BufferIORequest.NEW_PATH);

				undoMgr.clear();
				undoMgr.setLimit(jEdit.getIntegerProperty(
					"buffer.undoCount",100));

				if(!getFlag(TEMPORARY))
					finishLoading();

				setFlag(LOADING,false);

				// if reloading a file, clear dirty flag
				if(reload)
					setDirty(false);

				if(!loadAutosave && newPath != null)
					setPath(newPath);

				// if loadAutosave is false, we loaded an
				// autosave file, so we set 'dirty' to true

				// note that we don't use setDirty(),
				// because a) that would send an unnecessary
				// message, b) it would also set the
				// AUTOSAVE_DIRTY flag, which will make
				// the autosave thread write out a
				// redundant autosave file
				if(loadAutosave)
					setFlag(DIRTY,true);

				// send some EditBus messages
				if(!getFlag(TEMPORARY))
				{
					EditBus.send(new BufferUpdate(Buffer.this,
						view,BufferUpdate.LOADED));
					//EditBus.send(new BufferUpdate(Buffer.this,
					//	view,BufferUpdate.MARKERS_CHANGED));
				}
			}
		}; //}}}

		if(getFlag(TEMPORARY))
			runnable.run();
		else
			VFSManager.runInAWTThread(runnable);

		return true;
	} //}}}

	//{{{ insertFile() method
	/**
	 * Loads a file from disk, and inserts it into this buffer.
	 * @param view The view
	 *
	 * @since 4.0pre1
	 */
	public boolean insertFile(final View view, String path)
	{
		if(isPerformingIO())
		{
			GUIUtilities.error(view,"buffer-multiple-io",null);
			return false;
		}

		setBooleanProperty(BufferIORequest.ERROR_OCCURRED,false);

		path = MiscUtilities.constructPath(this.path,path);

		Buffer buffer = jEdit.getBuffer(path);
		if(buffer != null)
		{
			view.getTextArea().setSelectedText(
				buffer.getText(0,buffer.getLength()));
			return true;
		}

		VFS vfs = VFSManager.getVFSForPath(path);

		setFlag(IO,true);

		// this returns false if initial sanity
		// checks (if the file is a directory, etc)
		// fail
		if(!vfs.insert(view,this,path))
		{
			setFlag(IO,false);
			return false;
		}

		// Do some stuff once loading is finished
		VFSManager.runInAWTThread(new Runnable()
		{
			public void run()
			{
				setFlag(IO,false);

				SegmentBuffer sbuf = (SegmentBuffer)getProperty(
					BufferIORequest.LOAD_DATA);
				if(sbuf != null)
				{
					unsetProperty(BufferIORequest.LOAD_DATA);

					view.getTextArea().setSelectedText(sbuf.toString());
				}
			}
		});

		return true;
	} //}}}

	//{{{ autosave() method
	/**
	 * Autosaves this buffer.
	 */
	public void autosave()
	{
		if(autosaveFile == null || !getFlag(AUTOSAVE_DIRTY)
			|| !getFlag(DIRTY)
			|| getFlag(LOADING)
			|| getFlag(IO))
			return;

		setFlag(AUTOSAVE_DIRTY,false);

		VFSManager.runInWorkThread(new BufferIORequest(
			BufferIORequest.AUTOSAVE,null,this,null,
			VFSManager.getFileVFS(),autosaveFile.getPath()));
	} //}}}

	//{{{ saveAs() method
	/**
	 * Prompts the user for a file to save this buffer to.
	 * @param view The view
	 * @param rename True if the buffer's path should be changed, false
	 * if only a copy should be saved to the specified filename
	 * @since jEdit 2.6pre5
	 */
	public boolean saveAs(View view, boolean rename)
	{
		String[] files = GUIUtilities.showVFSFileDialog(view,path,
			VFSBrowser.SAVE_DIALOG,false);

		// files[] should have length 1, since the dialog type is
		// SAVE_DIALOG
		if(files == null)
			return false;

		return save(view,files[0],rename);
	} //}}}

	//{{{ save() method
	/**
	 * Saves this buffer to the specified path name, or the current path
	 * name if it's null.
	 * @param view The view
	 * @param path The path name to save the buffer to, or null to use
	 * the existing path
	 */
	public boolean save(View view, String path)
	{
		return save(view,path,true);
	} //}}}

	//{{{ save() method
	/**
	 * Saves this buffer to the specified path name, or the current path
	 * name if it's null.
	 * @param view The view
	 * @param path The path name to save the buffer to, or null to use
	 * the existing path
	 * @param rename True if the buffer's path should be changed, false
	 * if only a copy should be saved to the specified filename
	 * @since jEdit 2.6pre5
	 */
	public boolean save(final View view, String path, final boolean rename)
	{
		if(isPerformingIO())
		{
			GUIUtilities.error(view,"buffer-multiple-io",null);
			return false;
		}

		setBooleanProperty(BufferIORequest.ERROR_OCCURRED,false);

		if(path == null && getFlag(NEW_FILE))
			return saveAs(view,rename);

		if(path == null && file != null)
		{
			long newModTime = file.lastModified();

			if(newModTime != modTime
				&& jEdit.getBooleanProperty("view.checkModStatus"))
			{
				Object[] args = { this.path };
				int result = GUIUtilities.confirm(view,
					"filechanged-save",args,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
				if(result != JOptionPane.YES_OPTION)
					return false;
			}
		}

		setFlag(IO,true);
		EditBus.send(new BufferUpdate(this,view,BufferUpdate.SAVING));

		final String oldPath = this.path;
		final String newPath = (path == null ? this.path : path);

		VFS vfs = VFSManager.getVFSForPath(newPath);

		if(!checkFileForSave(view,vfs,newPath))
		{
			setFlag(IO,false);
			return false;
		}

		if(!vfs.save(view,this,newPath))
		{
			setFlag(IO,false);
			return false;
		}

		// Once save is complete, do a few other things
		VFSManager.runInAWTThread(new Runnable()
		{
			public void run()
			{
				setFlag(IO,false);
				finishSaving(view,oldPath,newPath,rename,
					getBooleanProperty(BufferIORequest
					.ERROR_OCCURRED));
			}
		});

		return true;
	} //}}}

	//{{{ checkFileStatus() method
	public static final int FILE_NOT_CHANGED = 0;
	public static final int FILE_CHANGED = 1;
	public static final int FILE_DELETED = 2;
	/**
	 * Check if the buffer has changed on disk.
	 * @return One of <code>NOT_CHANGED</code>, <code>CHANGED</code>, or
	 * <code>DELETED</code>.
	 *
	 * @since jEdit 4.2pre1
	 */
	public int checkFileStatus(View view)
	{
		// - don't do these checks while a save is in progress,
		// because for a moment newModTime will be greater than
		// oldModTime, due to the multithreading
		// - only supported on local file system
		if(!getFlag(IO) && !getFlag(LOADING) && file != null
			&& !getFlag(NEW_FILE))
		{
			boolean newReadOnly = (file.exists() && !file.canWrite());
			if(newReadOnly != getFlag(READ_ONLY))
			{
				setFlag(READ_ONLY,newReadOnly);
				EditBus.send(new BufferUpdate(this,null,
					BufferUpdate.DIRTY_CHANGED));
			}

			long oldModTime = modTime;
			long newModTime = file.lastModified();

			if(newModTime != oldModTime)
			{
				modTime = newModTime;

				if(!file.exists())
				{
					setFlag(NEW_FILE,true);
					setDirty(true);
					return FILE_DELETED;
				}
				else
				{
					return FILE_CHANGED;
				}
			}
		}

		return FILE_NOT_CHANGED;
	} //}}}

	//}}}

	//{{{ Getters/setter methods for various buffer meta-data

	//{{{ getLastModified() method
	/**
	 * Returns the last time jEdit modified the file on disk.
	 * This method is thread-safe.
	 */
	public long getLastModified()
	{
		return modTime;
	} //}}}

	//{{{ setLastModified() method
	/**
	 * Sets the last time jEdit modified the file on disk.
	 * @param modTime The new modification time
	 */
	public void setLastModified(long modTime)
	{
		this.modTime = modTime;
	} //}}}

	//{{{ getVFS() method
	/**
	 * Returns the virtual filesystem responsible for loading and
	 * saving this buffer. This method is thread-safe.
	 */
	public VFS getVFS()
	{
		return VFSManager.getVFSForPath(path);
	} //}}}

	//{{{ getAutosaveFile() method
	/**
	 * Returns the autosave file for this buffer. This may be null if
	 * the file is non-local.
	 */
	public File getAutosaveFile()
	{
		return autosaveFile;
	} //}}}

	//{{{ getName() method
	/**
	 * Returns the name of this buffer. This method is thread-safe.
	 */
	public String getName()
	{
		return name;
	} //}}}

	//{{{ getPath() method
	/**
	 * Returns the path name of this buffer. This method is thread-safe.
	 */
	public String getPath()
	{
		return path;
	} //}}}

	//{{{ getSymlinkPath() method
	/**
	 * If this file is a symbolic link, returns the link destination.
	 * Otherwise returns the file's path. This method is thread-safe.
	 * @since jEdit 4.2pre1
	 */
	public String getSymlinkPath()
	{
		return symlinkPath;
	} //}}}

	//{{{ getDirectory() method
	/**
	 * Returns the directory containing this buffer.
	 * @since jEdit 4.1pre11
	 */
	public String getDirectory()
	{
		return directory;
	} //}}}

	//{{{ isClosed() method
	/**
	 * Returns true if this buffer has been closed with
	 * {@link org.gjt.sp.jedit.jEdit#closeBuffer(View,Buffer)}.
	 * This method is thread-safe.
	 */
	public boolean isClosed()
	{
		return getFlag(CLOSED);
	} //}}}

	//{{{ isLoaded() method
	/**
	 * Returns true if the buffer is loaded. This method is thread-safe.
	 */
	public boolean isLoaded()
	{
		return !getFlag(LOADING);
	} //}}}

	//{{{ isPerformingIO() method
	/**
	 * Returns true if the buffer is currently performing I/O.
	 * This method is thread-safe.
	 * @since jEdit 2.7pre1
	 */
	public boolean isPerformingIO()
	{
		return getFlag(LOADING) || getFlag(IO);
	} //}}}

	//{{{ isNewFile() method
	/**
	 * Returns whether this buffer lacks a corresponding version on disk.
	 * This method is thread-safe.
	 */
	public boolean isNewFile()
	{
		return getFlag(NEW_FILE);
	} //}}}

	//{{{ setNewFile() method
	/**
	 * Sets the new file flag.
	 * @param newFile The new file flag
	 */
	public void setNewFile(boolean newFile)
	{
		setFlag(NEW_FILE,newFile);
		if(!newFile)
			setFlag(UNTITLED,false);
	} //}}}

	//{{{ isUntitled() method
	/**
	 * Returns true if this file is 'untitled'. This method is thread-safe.
	 */
	public boolean isUntitled()
	{
		return getFlag(UNTITLED);
	} //}}}

	//{{{ isDirty() method
	/**
	 * Returns whether there have been unsaved changes to this buffer.
	 * This method is thread-safe.
	 */
	public boolean isDirty()
	{
		return getFlag(DIRTY);
	} //}}}

	//{{{ isReadOnly() method
	/**
	 * Returns true if this file is read only, false otherwise.
	 * This method is thread-safe.
	 */
	public boolean isReadOnly()
	{
		return getFlag(READ_ONLY) || getFlag(READ_ONLY_OVERRIDE);
	} //}}}

	//{{{ isEditable() method
	/**
	 * Returns true if this file is editable, false otherwise. A file may
	 * become uneditable if it is read only, or if I/O is in progress.
	 * This method is thread-safe.
	 * @since jEdit 2.7pre1
	 */
	public boolean isEditable()
	{
		return !(getFlag(READ_ONLY) || getFlag(READ_ONLY_OVERRIDE)
			|| getFlag(IO) || getFlag(LOADING));
	} //}}}

	//{{{ isReadOnly() method
	/**
	 * Sets the read only flag.
	 * @param readOnly The read only flag
	 */
	public void setReadOnly(boolean readOnly)
	{
		setFlag(READ_ONLY_OVERRIDE,readOnly);
	} //}}}

	//{{{ setDirty() method
	/**
	 * Sets the 'dirty' (changed since last save) flag of this buffer.
	 */
	public void setDirty(boolean d)
	{
		boolean old_d = getFlag(DIRTY);

		if(d)
		{
			if(isEditable())
			{
				setFlag(DIRTY,true);
				setFlag(AUTOSAVE_DIRTY,true);
			}
		}
		else
		{
			setFlag(DIRTY,false);
			setFlag(AUTOSAVE_DIRTY,false);

			if(autosaveFile != null)
				autosaveFile.delete();

			// fixes dirty flag not being reset on
			// save/insert/undo/redo/undo
			if(!getFlag(UNDO_IN_PROGRESS))
			{
				// this ensures that undo can clear the dirty flag properly
				// when all edits up to a save are undone
				undoMgr.bufferSaved();
			}
		}

		if(d != old_d)
		{
			EditBus.send(new BufferUpdate(this,null,
				BufferUpdate.DIRTY_CHANGED));
		}
	} //}}}

	//{{{ isTemporary() method
	/**
	 * Returns if this is a temporary buffer. This method is thread-safe.
	 * @see jEdit#openTemporary(View,String,String,boolean)
	 * @see jEdit#commitTemporary(Buffer)
	 * @since jEdit 2.2pre7
	 */
	public boolean isTemporary()
	{
		return getFlag(TEMPORARY);
	} //}}}

	//{{{ getIcon() method
	/**
	 * Returns this buffer's icon.
	 * @since jEdit 2.6pre6
	 */
	public Icon getIcon()
	{
		if(getFlag(DIRTY))
			return GUIUtilities.DIRTY_BUFFER_ICON;
		else if(getFlag(READ_ONLY) || getFlag(READ_ONLY_OVERRIDE))
			return GUIUtilities.READ_ONLY_BUFFER_ICON;
		else if(getFlag(NEW_FILE))
			return GUIUtilities.NEW_BUFFER_ICON;
		else
			return GUIUtilities.NORMAL_BUFFER_ICON;
	} //}}}

	//}}}

	//{{{ Thread safety

	//{{{ readLock() method
	/**
	 * The buffer is guaranteed not to change between calls to
	 * {@link #readLock()} and {@link #readUnlock()}.
	 */
	public void readLock()
	{
		lock.readLock();
	} //}}}

	//{{{ readUnlock() method
	/**
	 * The buffer is guaranteed not to change between calls to
	 * {@link #readLock()} and {@link #readUnlock()}.
	 */
	public void readUnlock()
	{
		lock.readUnlock();
	} //}}}

	//{{{ writeLock() method
	/**
	 * Attempting to obtain read lock will block between calls to
	 * {@link #writeLock()} and {@link #writeUnlock()}.
	 */
	public void writeLock()
	{
		lock.writeLock();
	} //}}}

	//{{{ writeUnlock() method
	/**
	 * Attempting to obtain read lock will block between calls to
	 * {@link #writeLock()} and {@link #writeUnlock()}.
	 */
	public void writeUnlock()
	{
		lock.writeUnlock();
	} //}}}

	//}}}

	//{{{ Line offset methods

	//{{{ getLength() method
	/**
	 * Returns the number of characters in the buffer. This method is thread-safe.
	 */
	public int getLength()
	{
		// no need to lock since this just returns a value and that's it
		return contentMgr.getLength();
	} //}}}

	//{{{ getLineCount() method
	/**
	 * Returns the number of physical lines in the buffer.
	 * This method is thread-safe.
	 * @since jEdit 3.1pre1
	 */
	public int getLineCount()
	{
		// no need to lock since this just returns a value and that's it
		return lineMgr.getLineCount();
	} //}}}

	//{{{ getLineOfOffset() method
	/**
	 * Returns the line containing the specified offset.
	 * This method is thread-safe.
	 * @param offset The offset
	 * @since jEdit 4.0pre1
	 */
	public int getLineOfOffset(int offset)
	{
		try
		{
			readLock();

			if(offset < 0 || offset > getLength())
				throw new ArrayIndexOutOfBoundsException(offset);

			return lineMgr.getLineOfOffset(offset);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineStartOffset() method
	/**
	 * Returns the start offset of the specified line.
	 * This method is thread-safe.
	 * @param line The line
	 * @return The start offset of the specified line
	 * @since jEdit 4.0pre1
	 */
	public int getLineStartOffset(int line)
	{
		try
		{
			readLock();

			if(line < 0 || line >= lineMgr.getLineCount())
				throw new ArrayIndexOutOfBoundsException(line);
			else if(line == 0)
				return 0;

			return lineMgr.getLineEndOffset(line - 1);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineEndOffset() method
	/**
	 * Returns the end offset of the specified line.
	 * This method is thread-safe.
	 * @param line The line
	 * @return The end offset of the specified line
	 * invalid.
	 * @since jEdit 4.0pre1
	 */
	public int getLineEndOffset(int line)
	{
		try
		{
			readLock();

			if(line < 0 || line >= lineMgr.getLineCount())
				throw new ArrayIndexOutOfBoundsException(line);

			return lineMgr.getLineEndOffset(line);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineLength() method
	/**
	 * Returns the length of the specified line.
	 * This method is thread-safe.
	 * @param line The line
	 * @since jEdit 4.0pre1
	 */
	public int getLineLength(int line)
	{
		try
		{
			readLock();

			return getLineEndOffset(line)
				- getLineStartOffset(line) - 1;
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//}}}

	//{{{ Text getters and setters

	//{{{ getLineText() method
	/**
	 * Returns the text on the specified line.
	 * This method is thread-safe.
	 * @param line The line
	 * @return The text, or null if the line is invalid
	 * @since jEdit 4.0pre1
	 */
	public String getLineText(int line)
	{
		if(line < 0 || line >= lineMgr.getLineCount())
			throw new ArrayIndexOutOfBoundsException(line);

		try
		{
			readLock();

			int start = (line == 0 ? 0
				: lineMgr.getLineEndOffset(line - 1));
			int end = lineMgr.getLineEndOffset(line);

			return getText(start,end - start - 1);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getLineText() method
	/**
	 * Returns the specified line in a <code>Segment</code>.<p>
	 *
	 * Using a <classname>Segment</classname> is generally more
	 * efficient than using a <classname>String</classname> because it
	 * results in less memory allocation and array copying.<p>
	 *
	 * This method is thread-safe.
	 *
	 * @param line The line
	 * @since jEdit 4.0pre1
	 */
	public void getLineText(int line, Segment segment)
	{
		if(line < 0 || line >= lineMgr.getLineCount())
			throw new ArrayIndexOutOfBoundsException(line);

		try
		{
			readLock();

			int start = (line == 0 ? 0
				: lineMgr.getLineEndOffset(line - 1));
			int end = lineMgr.getLineEndOffset(line);

			getText(start,end - start - 1,segment);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getText() method
	/**
	 * Returns the specified text range. This method is thread-safe.
	 * @param start The start offset
	 * @param length The number of characters to get
	 */
	public String getText(int start, int length)
	{
		try
		{
			readLock();

			if(start < 0 || length < 0
				|| start + length > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(start + ":" + length);

			return contentMgr.getText(start,length);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getText() method
	/**
	 * Returns the specified text range in a <code>Segment</code>.<p>
	 *
	 * Using a <classname>Segment</classname> is generally more
	 * efficient than using a <classname>String</classname> because it
	 * results in less memory allocation and array copying.<p>
	 *
	 * This method is thread-safe.
	 *
	 * @param start The start offset
	 * @param length The number of characters to get
	 * @param seg The segment to copy the text to
	 */
	public void getText(int start, int length, Segment seg)
	{
		try
		{
			readLock();

			if(start < 0 || length < 0
				|| start + length > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(start + ":" + length);

			contentMgr.getText(start,length,seg);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ insert() method
	/**
	 * Inserts a string into the buffer.
	 * @param offset The offset
	 * @param str The string
	 * @since jEdit 4.0pre1
	 */
	public void insert(int offset, String str)
	{
		if(str == null)
			return;

		int len = str.length();

		if(len == 0)
			return;

		if(isReadOnly())
			throw new RuntimeException("buffer read-only");

		try
		{
			writeLock();

			if(offset < 0 || offset > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(offset);

			contentMgr.insert(offset,str);

			integerArray.clear();

			for(int i = 0; i < len; i++)
			{
				if(str.charAt(i) == '\n')
					integerArray.add(i + 1);
			}

			if(!getFlag(UNDO_IN_PROGRESS))
			{
				undoMgr.contentInserted(offset,len,str,
					!getFlag(DIRTY));
			}

			contentInserted(offset,len,integerArray);
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ insert() method
	/**
	 * Inserts a string into the buffer.
	 * @param offset The offset
	 * @param seg The segment
	 * @since jEdit 4.0pre1
	 */
	public void insert(int offset, Segment seg)
	{
		if(seg.count == 0)
			return;

		if(isReadOnly())
			throw new RuntimeException("buffer read-only");

		try
		{
			writeLock();

			if(offset < 0 || offset > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(offset);

			contentMgr.insert(offset,seg);

			integerArray.clear();

			for(int i = 0; i < seg.count; i++)
			{
				if(seg.array[seg.offset + i] == '\n')
					integerArray.add(i + 1);
			}

			if(!getFlag(UNDO_IN_PROGRESS))
			{
				undoMgr.contentInserted(offset,seg.count,
					seg.toString(),!getFlag(DIRTY));
			}

			contentInserted(offset,seg.count,integerArray);
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ remove() method
	/**
	 * Removes the specified rang efrom the buffer.
	 * @param offset The start offset
	 * @param length The number of characters to remove
	 */
	public void remove(int offset, int length)
	{
		if(length == 0)
			return;

		if(isReadOnly())
			throw new RuntimeException("buffer read-only");

		try
		{
			writeLock();

			if(offset < 0 || length < 0
				|| offset + length > contentMgr.getLength())
				throw new ArrayIndexOutOfBoundsException(offset + ":" + length);

			int startLine = lineMgr.getLineOfOffset(offset);
			int endLine = lineMgr.getLineOfOffset(offset + length);

			int numLines = endLine - startLine;

			if(!getFlag(UNDO_IN_PROGRESS) && !getFlag(LOADING))
			{
				undoMgr.contentRemoved(offset,length,
					getText(offset,length),
					!getFlag(DIRTY));
			}

			firePreContentRemoved(startLine,offset,numLines,length);

			contentMgr.remove(offset,length);
			lineMgr.contentRemoved(startLine,offset,numLines,length);
			positionMgr.contentRemoved(offset,length);

			fireContentRemoved(startLine,offset,numLines,length);

			setDirty(true);
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//}}}

	//{{{ Undo

	//{{{ undo() method
	/**
	 * Undoes the most recent edit.
	 *
	 * @since jEdit 4.0pre1
	 */
	public void undo(JEditTextArea textArea)
	{
		if(undoMgr == null)
			return;

		if(!isEditable())
		{
			textArea.getToolkit().beep();
			return;
		}

		try
		{
			writeLock();

			setFlag(UNDO_IN_PROGRESS,true);
			int caret = undoMgr.undo();

			if(caret == -1)
				textArea.getToolkit().beep();
			else
				textArea.setCaretPosition(caret);

			fireTransactionComplete();
		}
		finally
		{
			setFlag(UNDO_IN_PROGRESS,false);

			writeUnlock();
		}
	} //}}}

	//{{{ redo() method
	/**
	 * Redoes the most recently undone edit.
	 *
	 * @since jEdit 2.7pre2
	 */
	public void redo(JEditTextArea textArea)
	{
		if(undoMgr == null)
			return;

		if(!isEditable())
		{
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		try
		{
			writeLock();

			setFlag(UNDO_IN_PROGRESS,true);
			int caret = undoMgr.redo();
			if(caret == -1)
				textArea.getToolkit().beep();
			else
				textArea.setCaretPosition(caret);

			fireTransactionComplete();
		}
		finally
		{
			setFlag(UNDO_IN_PROGRESS,false);

			writeUnlock();
		}
	} //}}}

	//{{{ isTransactionInProgress() method
	/**
	 * Returns if an undo or compound edit is currently in progress. If this
	 * method returns true, then eventually a
	 * {@link org.gjt.sp.jedit.buffer.BufferChangeListener#transactionComplete(Buffer)}
	 * buffer event will get fired.
	 * @since jEdit 4.0pre6
	 */
	public boolean isTransactionInProgress()
	{
		return getFlag(UNDO_IN_PROGRESS) || insideCompoundEdit();
	} //}}}

	//{{{ beginCompoundEdit() method
	/**
	 * Starts a compound edit. All edits from now on until
	 * {@link #endCompoundEdit()} are called will be merged
	 * into one. This can be used to make a complex operation
	 * undoable in one step. Nested calls to
	 * {@link #beginCompoundEdit()} behave as expected,
	 * requiring the same number of {@link #endCompoundEdit()}
	 * calls to end the edit.
	 * @see #endCompoundEdit()
	 */
	public void beginCompoundEdit()
	{
		// Why?
		//if(getFlag(TEMPORARY))
		//	return;

		try
		{
			writeLock();

			undoMgr.beginCompoundEdit();
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//{{{ endCompoundEdit() method
	/**
	 * Ends a compound edit. All edits performed since
	 * {@link #beginCompoundEdit()} was called can now
	 * be undone in one step by calling {@link #undo(JEditTextArea)}.
	 * @see #beginCompoundEdit()
	 */
	public void endCompoundEdit()
	{
		// Why?
		//if(getFlag(TEMPORARY))
		//	return;

		try
		{
			writeLock();

			undoMgr.endCompoundEdit();

			if(!insideCompoundEdit())
				fireTransactionComplete();
		}
		finally
		{
			writeUnlock();
		}
	}//}}}

	//{{{ insideCompoundEdit() method
	/**
	 * Returns if a compound edit is currently active.
	 * @since jEdit 3.1pre1
	 */
	public boolean insideCompoundEdit()
	{
		return undoMgr.insideCompoundEdit();
	} //}}}

	//}}}

	//{{{ Buffer events
	public static final int NORMAL_PRIORITY = 0;
	public static final int HIGH_PRIORITY = 1;
	static class Listener
	{
		BufferChangeListener listener;
		int priority;

		Listener(BufferChangeListener listener, int priority)
		{
			this.listener = listener;
			this.priority = priority;
		}
	}

	//{{{ addBufferChangeListener() method
	/**
	 * Adds a buffer change listener.
	 * @param listener The listener
	 * @param priority Listeners with HIGH_PRIORITY get the event before
	 * listeners with NORMAL_PRIORITY
	 * @since jEdit 4.2pre2
	 */
	public void addBufferChangeListener(BufferChangeListener listener,
		int priority)
	{
		Listener l = new Listener(listener,priority);
		for(int i = 0; i < bufferListeners.size(); i++)
		{
			Listener _l = (Listener)bufferListeners.get(i);
			if(_l.priority < priority)
			{
				bufferListeners.insertElementAt(l,i);
				return;
			}
		}
		bufferListeners.addElement(l);
	} //}}}

	//{{{ addBufferChangeListener() method
	/**
	 * Adds a buffer change listener.
	 * @param listener The listener
	 * @since jEdit 4.0pre1
	 */
	public void addBufferChangeListener(BufferChangeListener listener)
	{
		addBufferChangeListener(listener,NORMAL_PRIORITY);
	} //}}}

	//{{{ removeBufferChangeListener() method
	/**
	 * Removes a buffer change listener.
	 * @param listener The listener
	 * @since jEdit 4.0pre1
	 */
	public void removeBufferChangeListener(BufferChangeListener listener)
	{
		for(int i = 0; i < bufferListeners.size(); i++)
		{
			if(((Listener)bufferListeners.get(i)).listener == listener)
			{
				bufferListeners.removeElementAt(i);
				return;
			}
		}
	} //}}}

	//{{{ getBufferChangeListeners() method
	/**
	 * Returns an array of registered buffer change listeners.
	 * @param listener The listener
	 * @since jEdit 4.1pre3
	 */
	public BufferChangeListener[] getBufferChangeListeners()
	{
		BufferChangeListener[] returnValue
			= new BufferChangeListener[
			bufferListeners.size()];
		for(int i = 0; i < returnValue.length; i++)
		{
			returnValue[i] = ((Listener)bufferListeners.get(i))
				.listener;
		}
		return returnValue;
	} //}}}

	//}}}

	//{{{ Property methods

	//{{{ propertiesChanged() method
	/**
	 * Reloads settings from the properties. This should be called
	 * after the <code>syntax</code> or <code>folding</code>
	 * buffer-local properties are changed.
	 */
	public void propertiesChanged()
	{
		String folding = getStringProperty("folding");
		FoldHandler handler = FoldHandler.getFoldHandler(folding);

		if(handler != null)
		{
			setFoldHandler(handler);
		}
		else
		{
			if (folding != null)
				Log.log(Log.WARNING, this, path + ": invalid 'folding' property: " + folding); 
			setFoldHandler(new DummyFoldHandler());
		}

		EditBus.send(new BufferUpdate(this,null,BufferUpdate.PROPERTIES_CHANGED));

		String newWrap = getStringProperty("wrap");
		if(wrap != null && !newWrap.equals(wrap))
		{
			lineMgr.invalidateScreenLineCounts();
			if(isLoaded())
				fireWrapModeChanged();
		}
		this.wrap = newWrap;
	} //}}}

	//{{{ getTabSize() method
	/**
	 * Returns the tab size used in this buffer. This is equivalent
	 * to calling <code>getProperty("tabSize")</code>.
	 * This method is thread-safe.
	 */
	public int getTabSize()
	{
		return getIntegerProperty("tabSize",8);
	} //}}}

	//{{{ getIndentSize() method
	/**
	 * Returns the indent size used in this buffer. This is equivalent
	 * to calling <code>getProperty("indentSize")</code>.
	 * This method is thread-safe.
	 * @since jEdit 2.7pre1
	 */
	public int getIndentSize()
	{
		return getIntegerProperty("indentSize",8);
	} //}}}

	//{{{ getProperty() method
	/**
	 * Returns the value of a buffer-local property.<p>
	 *
	 * Using this method is generally discouraged, because it returns an
	 * <code>Object</code> which must be cast to another type
	 * in order to be useful, and this can cause problems if the object
	 * is of a different type than what the caller expects.<p>
	 *
	 * The following methods should be used instead:
	 * <ul>
	 * <li>{@link #getStringProperty(String)}</li>
	 * <li>{@link #getBooleanProperty(String)}</li>
	 * <li>{@link #getIntegerProperty(String,int)}</li>
	 * <li>{@link #getRegexpProperty(String,int,gnu.regexp.RESyntax)}</li>
	 * </ul>
	 *
	 * This method is thread-safe.
	 *
	 * @param name The property name. For backwards compatibility, this
	 * is an <code>Object</code>, not a <code>String</code>.
	 */
	public Object getProperty(Object name)
	{
		synchronized(propertyLock)
		{
			// First try the buffer-local properties
			PropValue o = (PropValue)properties.get(name);
			if(o != null)
				return o.value;

			// For backwards compatibility
			if(!(name instanceof String))
				return null;

			// Now try mode.<mode>.<property>
			if(mode != null)
			{
				Object retVal = mode.getProperty((String)name);
				if(retVal == null)
					return null;

				properties.put(name,new PropValue(retVal,true));
				return retVal;
			}
			else
			{
				// Now try buffer.<property>
				String value = jEdit.getProperty("buffer." + name);
				if(value == null)
					return null;

				// Try returning it as an integer first
				Object retVal;
				try
				{
					retVal = new Integer(value);
				}
				catch(NumberFormatException nf)
				{
					retVal = value;
				}
				properties.put(name,new PropValue(retVal,true));
				return retVal;
			}
		}
	} //}}}

	//{{{ setProperty() method
	/**
	 * Sets the value of a buffer-local property.
	 * @param name The property name
	 * @param value The property value
	 * @since jEdit 4.0pre1
	 */
	public void setProperty(String name, Object value)
	{
		if(value == null)
			properties.remove(name);
		else
		{
			PropValue test = (PropValue)properties.get(name);
			if(test == null)
				properties.put(name,new PropValue(value,false));
			else if(test.value.equals(value))
			{
				// do nothing
			}
			else
			{
				test.value = value;
				test.defaultValue = false;
			}
		}
	} //}}}

	//{{{ unsetProperty() method
	/**
	 * Clears the value of a buffer-local property.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public void unsetProperty(String name)
	{
		properties.remove(name);
	} //}}}

	//{{{ getStringProperty() method
	/**
	 * Returns the value of a string property. This method is thread-safe.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public String getStringProperty(String name)
	{
		Object obj = getProperty(name);
		if(obj != null)
			return obj.toString();
		else
			return null;
	} //}}}

	//{{{ setStringProperty() method
	/**
	 * Sets a string property.
	 * @param name The property name
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public void setStringProperty(String name, String value)
	{
		setProperty(name,value);
	} //}}}

	//{{{ getBooleanProperty() method
	/**
	 * Returns the value of a boolean property. This method is thread-safe.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public boolean getBooleanProperty(String name)
	{
		Object obj = getProperty(name);
		if(obj instanceof Boolean)
			return ((Boolean)obj).booleanValue();
		else if("true".equals(obj) || "on".equals(obj) || "yes".equals(obj))
			return true;
		else
			return false;
	} //}}}

	//{{{ setBooleanProperty() method
	/**
	 * Sets a boolean property.
	 * @param name The property name
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public void setBooleanProperty(String name, boolean value)
	{
		setProperty(name,value ? Boolean.TRUE : Boolean.FALSE);
	} //}}}

	//{{{ getIntegerProperty() method
	/**
	 * Returns the value of an integer property. This method is thread-safe.
	 * @param name The property name
	 * @since jEdit 4.0pre1
	 */
	public int getIntegerProperty(String name, int defaultValue)
	{
		boolean defaultValueFlag;
		Object obj;
		PropValue value = (PropValue)properties.get(name);
		if(value != null)
		{
			obj = value.value;
			defaultValueFlag = value.defaultValue;
		}
		else
		{
			obj = getProperty(name);
			// will be cached from now on...
			defaultValueFlag = true;
		}

		if(obj == null)
			return defaultValue;
		else if(obj instanceof Number)
			return ((Number)obj).intValue();
		else
		{
			try
			{
				int returnValue = Integer.parseInt(
					obj.toString().trim());
				properties.put(name,new PropValue(
					new Integer(returnValue),
					defaultValueFlag));
				return returnValue;
			}
			catch(Exception e)
			{
				return defaultValue;
			}
		}
	} //}}}

	//{{{ setIntegerProperty() method
	/**
	 * Sets an integer property.
	 * @param name The property name
	 * @param value The value
	 * @since jEdit 4.0pre1
	 */
	public void setIntegerProperty(String name, int value)
	{
		setProperty(name,new Integer(value));
	} //}}}

	//{{{ getRegexpProperty() method
	/**
	 * Returns the value of a property as a regular expression.
	 * This method is thread-safe.
	 * @param name The property name
	 * @param cflags Regular expression compilation flags
	 * @param syntax Regular expression syntax
	 * @since jEdit 4.1pre9
	 */
	public RE getRegexpProperty(String name, int cflags,
		RESyntax syntax) throws REException
	{
		synchronized(propertyLock)
		{
			boolean defaultValueFlag;
			Object obj;
			PropValue value = (PropValue)properties.get(name);
			if(value != null)
			{
				obj = value.value;
				defaultValueFlag = value.defaultValue;
			}
			else
			{
				obj = getProperty(name);
				// will be cached from now on...
				defaultValueFlag = true;
			}

			if(obj == null)
				return null;
			else if(obj instanceof RE)
				return (RE)obj;
			else
			{
				RE re = new RE(obj.toString(),cflags,syntax);
				properties.put(name,new PropValue(re,
					defaultValueFlag));
				return re;
			}
		}
	} //}}}

	//{{{ getRuleSetAtOffset() method
	/**
	 * Returns the syntax highlighting ruleset at the specified offset.
	 * @since jEdit 4.1pre1
	 */
	public ParserRuleSet getRuleSetAtOffset(int offset)
	{
		int line = getLineOfOffset(offset);
		offset -= getLineStartOffset(line);
		if(offset != 0)
			offset--;

		DefaultTokenHandler tokens = new DefaultTokenHandler();
		markTokens(line,tokens);
		Token token = TextUtilities.getTokenAtOffset(tokens.getTokens(),offset);
		return token.rules;
	} //}}}

	//{{{ getKeywordMapAtOffset() method
	/**
	 * Returns the syntax highlighting keyword map in effect at the
	 * specified offset. Used by the <b>Complete Word</b> command to
	 * complete keywords.
	 * @param offset The offset
	 * @since jEdit 4.0pre3
	 */
	public KeywordMap getKeywordMapAtOffset(int offset)
	{
		return getRuleSetAtOffset(offset).getKeywords();
	} //}}}

	//{{{ getContextSensitiveProperty() method
	/**
	 * Some settings, like comment start and end strings, can
	 * vary between different parts of a buffer (HTML text and inline
	 * JavaScript, for example).
	 * @param offset The offset
	 * @param name The property name
	 * @since jEdit 4.0pre3
	 */
	public String getContextSensitiveProperty(int offset, String name)
	{
		ParserRuleSet rules = getRuleSetAtOffset(offset);

		Object value = null;

		Hashtable rulesetProps = rules.getProperties();
		if(rulesetProps != null)
			value = rulesetProps.get(name);

		if(value == null)
		{
			value = jEdit.getMode(rules.getModeName())
				.getProperty(name);

			if(value == null)
				value = mode.getProperty(name);
		}

		if(value == null)
			return null;
		else
			return String.valueOf(value);
	} //}}}

	//{{{ Used to store property values
	static class PropValue
	{
		PropValue(Object value, boolean defaultValue)
		{
			if(value == null)
				throw new NullPointerException();
			this.value = value;
			this.defaultValue = defaultValue;
		}

		Object value;

		/**
		 * If this is true, then this value is cached from the mode
		 * or global defaults, so when the defaults change this property
		 * value must be reset.
		 */
		boolean defaultValue;

		/**
		 * For debugging purposes.
		 */
		public String toString()
		{
			return value.toString();
		}
	} //}}}

	//{{{ toggleWordWrap() method
	/**
	 * Toggles word wrap between the three available modes. This is used
	 * by the status bar.
	 * @param view We show a message in the view's status bar
	 * @since jEdit 4.1pre3
	 */
	public void toggleWordWrap(View view)
	{
		String wrap = getStringProperty("wrap");
		if(wrap.equals("none"))
			wrap = "soft";
		else if(wrap.equals("soft"))
			wrap = "hard";
		else if(wrap.equals("hard"))
			wrap = "none";
		view.getStatus().setMessageAndClear(jEdit.getProperty(
			"view.status.wrap-changed",new String[] {
			wrap }));
		setProperty("wrap",wrap);
		propertiesChanged();
	} //}}}

	//{{{ toggleLineSeparator() method
	/**
	 * Toggles the line separator between the three available settings.
	 * This is used by the status bar.
	 * @param view We show a message in the view's status bar
	 * @since jEdit 4.1pre3
	 */
	public void toggleLineSeparator(View view)
	{
		String status = null;
		String lineSep = getStringProperty("lineSeparator");
		if("\n".equals(lineSep))
		{
			status = "windows";
			lineSep = "\r\n";
		}
		else if("\r\n".equals(lineSep))
		{
			status = "mac";
			lineSep = "\r";
		}
		else if("\r".equals(lineSep))
		{
			status = "unix";
			lineSep = "\n";
		}
		view.getStatus().setMessageAndClear(jEdit.getProperty(
			"view.status.linesep-changed",new String[] {
			jEdit.getProperty("lineSep." + status) }));
		setProperty("lineSeparator",lineSep);
		setDirty(true);
		propertiesChanged();
	} //}}}

	//}}}

	//{{{ Edit modes, syntax highlighting

	//{{{ getMode() method
	/**
	 * Returns this buffer's edit mode. This method is thread-safe.
	 */
	public Mode getMode()
	{
		return mode;
	} //}}}

	//{{{ setMode() method
	/**
	 * Sets this buffer's edit mode. Note that calling this before a buffer
	 * is loaded will have no effect; in that case, set the "mode" property
	 * to the name of the mode. A bit inelegant, I know...
	 * @param mode The mode name
	 * @since jEdit 4.2pre1
	 */
	public void setMode(String mode)
	{
		setMode(jEdit.getMode(mode));
	} //}}}

	//{{{ setMode() method
	/**
	 * Sets this buffer's edit mode. Note that calling this before a buffer
	 * is loaded will have no effect; in that case, set the "mode" property
	 * to the name of the mode. A bit inelegant, I know...
	 * @param mode The mode
	 */
	public void setMode(Mode mode)
	{
		/* This protects against stupid people (like me)
		 * doing stuff like buffer.setMode(jEdit.getMode(...)); */
		if(mode == null)
			throw new NullPointerException("Mode must be non-null");

		this.mode = mode;

		textMode = "text".equals(mode.getName());

		setTokenMarker(mode.getTokenMarker());

		resetCachedProperties();
		propertiesChanged();
	} //}}}

	//{{{ setMode() method
	/**
	 * Sets this buffer's edit mode by calling the accept() method
	 * of each registered edit mode.
	 */
	public void setMode()
	{
		String userMode = getStringProperty("mode");
		if(userMode != null)
		{
			Mode m = jEdit.getMode(userMode);
			if(m != null)
			{
				setMode(m);
				return;
			}
		}

		String nogzName = name.substring(0,name.length() -
			(name.endsWith(".gz") ? 3 : 0));
		Mode[] modes = jEdit.getModes();

		String firstLine = getLineText(0);

		for(int i = 0; i < modes.length; i++)
		{
			if(modes[i].accept(nogzName,firstLine))
			{
				setMode(modes[i]);
				return;
			}
		}

		Mode defaultMode = jEdit.getMode(jEdit.getProperty("buffer.defaultMode"));
		if(defaultMode == null)
			defaultMode = jEdit.getMode("text");
		setMode(defaultMode);
	} //}}}

	//{{{ markTokens() method
	/**
	 * Returns the syntax tokens for the specified line.
	 * @param lineIndex The line number
	 * @param tokenHandler The token handler that will receive the syntax
	 * tokens
	 * @since jEdit 4.1pre1
	 */
	public void markTokens(int lineIndex, TokenHandler tokenHandler)
	{
		Segment seg;
		if(SwingUtilities.isEventDispatchThread())
			seg = this.seg;
		else
			seg = new Segment();

		if(lineIndex < 0 || lineIndex >= lineMgr.getLineCount())
			throw new ArrayIndexOutOfBoundsException(lineIndex);

		int firstInvalidLineContext = lineMgr.getFirstInvalidLineContext();
		int start;
		if(textMode || firstInvalidLineContext == -1)
		{
			start = lineIndex;
		}
		else
		{
			start = Math.min(firstInvalidLineContext,
				lineIndex);
		}

		if(Debug.TOKEN_MARKER_DEBUG)
			Log.log(Log.DEBUG,this,"tokenize from " + start + " to " + lineIndex);
		for(int i = start; i <= lineIndex; i++)
		{
			getLineText(i,seg);

			TokenMarker.LineContext context = lineMgr.getLineContext(i);
			ParserRule oldRule;
			ParserRuleSet oldRules;
			char[] oldSpanEndSubst;
			if(context == null)
			{
				//System.err.println(i + ": null context");
				oldRule = null;
				oldRules = null;
				oldSpanEndSubst = null;
			}
			else
			{
				oldRule = context.inRule;
				oldRules = context.rules;
				oldSpanEndSubst = (context.parent != null
					? context.parent.spanEndSubst
					: null);
			}

			TokenMarker.LineContext prevContext = (
				(i == 0 || textMode) ? null
				: lineMgr.getLineContext(i - 1)
			);

			context = tokenMarker.markTokens(prevContext,
				(i == lineIndex ? tokenHandler
				: DummyTokenHandler.INSTANCE),seg);
			lineMgr.setLineContext(i,context);

			// Could incorrectly be set to 'false' with
			// recursive delegates, where the chaining might
			// have changed but not the rule set in question (?)
			if(oldRule != context.inRule)
			{
				nextLineRequested = true;
			}
			else if(oldRules != context.rules)
			{
				nextLineRequested = true;
			}
			else if(!MiscUtilities.objectsEqual(oldSpanEndSubst,
				context.spanEndSubst))
			{
				nextLineRequested = true;
			}
		}

		int lineCount = lineMgr.getLineCount();
		if(lineCount - 1 == lineIndex)
			lineMgr.setFirstInvalidLineContext(-1);
		else if(nextLineRequested)
			lineMgr.setFirstInvalidLineContext(lineIndex + 1);
		else if(firstInvalidLineContext == -1)
			/* do nothing */;
		else
		{
			lineMgr.setFirstInvalidLineContext(Math.max(
				firstInvalidLineContext,lineIndex + 1));
		}
	} //}}}

	//{{{ isNextLineRequested() method
	/**
	 * Returns true if the next line should be repainted. This
	 * will return true after a line has been tokenized that starts
	 * a multiline token that continues onto the next line.
	 */
	public boolean isNextLineRequested()
	{
		boolean retVal = nextLineRequested;
		nextLineRequested = false;
		return retVal;
	} //}}}

	//}}}

	//{{{ Indentation

	//{{{ removeTrailingWhiteSpace() method
	/**
	 * Removes trailing whitespace from all lines in the specified list.
	 * @param lines The line numbers
	 * @since jEdit 3.2pre1
	 */
	public void removeTrailingWhiteSpace(int[] lines)
	{
		try
		{
			beginCompoundEdit();

			for(int i = 0; i < lines.length; i++)
			{
				int pos, lineStart, lineEnd, tail;

				getLineText(lines[i],seg);

				// blank line
				if (seg.count == 0) continue;

				lineStart = seg.offset;
				lineEnd = seg.offset + seg.count - 1;

				for (pos = lineEnd; pos >= lineStart; pos--)
				{
					if (!Character.isWhitespace(seg.array[pos]))
						break;
				}

				tail = lineEnd - pos;

				// no whitespace
				if (tail == 0) continue;

				remove(getLineEndOffset(lines[i]) - 1 - tail,tail);
			}
		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ shiftIndentLeft() method
	/**
	 * Shifts the indent of each line in the specified list to the left.
	 * @param lines The line numbers
	 * @since jEdit 3.2pre1
	 */
	public void shiftIndentLeft(int[] lines)
	{
		int tabSize = getTabSize();
		int indentSize = getIndentSize();
		boolean noTabs = getBooleanProperty("noTabs");

		try
		{
			beginCompoundEdit();

			for(int i = 0; i < lines.length; i++)
			{
				int lineStart = getLineStartOffset(lines[i]);
				String line = getLineText(lines[i]);
				int whiteSpace = MiscUtilities
					.getLeadingWhiteSpace(line);
				if(whiteSpace == 0)
					continue;
				int whiteSpaceWidth = Math.max(0,MiscUtilities
					.getLeadingWhiteSpaceWidth(line,tabSize)
					- indentSize);
	
				insert(lineStart + whiteSpace,MiscUtilities
					.createWhiteSpace(whiteSpaceWidth,
					(noTabs ? 0 : tabSize)));
				remove(lineStart,whiteSpace);
			}

		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ shiftIndentRight() method
	/**
	 * Shifts the indent of each line in the specified list to the right.
	 * @param lines The line numbers
	 * @since jEdit 3.2pre1
	 */
	public void shiftIndentRight(int[] lines)
	{
		try
		{
			beginCompoundEdit();

			int tabSize = getTabSize();
			int indentSize = getIndentSize();
			boolean noTabs = getBooleanProperty("noTabs");
			for(int i = 0; i < lines.length; i++)
			{
				int lineStart = getLineStartOffset(lines[i]);
				String line = getLineText(lines[i]);
				int whiteSpace = MiscUtilities
					.getLeadingWhiteSpace(line);

				// silly usability hack
				//if(lines.length != 1 && whiteSpace == 0)
				//	continue;

				int whiteSpaceWidth = MiscUtilities
					.getLeadingWhiteSpaceWidth(
					line,tabSize) + indentSize;
				insert(lineStart + whiteSpace,MiscUtilities
					.createWhiteSpace(whiteSpaceWidth,
					(noTabs ? 0 : tabSize)));
				remove(lineStart,whiteSpace);
			}
		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ indentLines() method
	/**
	 * Indents all specified lines.
	 * @param start The first line to indent
	 * @param end The last line to indent
	 * @since jEdit 3.1pre3
	 */
	public void indentLines(int start, int end)
	{
		try
		{
			beginCompoundEdit();
			for(int i = start; i <= end; i++)
				indentLine(i,true);
		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ indentLines() method
	/**
	 * Indents all specified lines.
	 * @param lines The line numbers
	 * @since jEdit 3.2pre1
	 */
	public void indentLines(int[] lines)
	{
		try
		{
			beginCompoundEdit();
			for(int i = 0; i < lines.length; i++)
				indentLine(lines[i],true);
		}
		finally
		{
			endCompoundEdit();
		}
	} //}}}

	//{{{ indentLine() method
	/**
	 * @deprecated Use {@link #indentLine(int,boolean)} instead.
	 */
	public boolean indentLine(int lineIndex, boolean canIncreaseIndent,
		boolean canDecreaseIndent)
	{
		return indentLine(lineIndex,canDecreaseIndent);
	} //}}}

	//{{{ indentLine() method
	/**
	 * Indents the specified line.
	 * @param line The line number to indent
	 * @param canDecreaseIndent If true, the indent can be decreased as a
	 * result of this. Set this to false for Tab key.
	 * @return true If indentation took place, false otherwise.
	 * @since jEdit 4.2pre2
	 */
	public boolean indentLine(int lineIndex, boolean canDecreaseIndent)
	{
		int[] whitespaceChars = new int[1];
		int currentIndent = getCurrentIdentForLine(lineIndex,
			whitespaceChars);
		int idealIndent = getIdealIndentForLine(lineIndex);

		if(idealIndent == -1 || idealIndent == currentIndent
			|| (!canDecreaseIndent && idealIndent < currentIndent))
			return false;

		// Do it
		try
		{
			beginCompoundEdit();

			int start = getLineStartOffset(lineIndex);

			remove(start,whitespaceChars[0]);
			insert(start,MiscUtilities.createWhiteSpace(
				idealIndent,(getBooleanProperty("noTabs")
				? 0 : getTabSize())));
		}
		finally
		{
			endCompoundEdit();
		}

		return true;
	} //}}}

	//{{{ getCurrentIdentForLine() method
	/**
	 * Returns the line's current leading indent.
	 * @param lineIndex The line number
	 * @param whitespaceChars If this is non-null, the number of whitespace
	 * characters is stored at the 0 index
	 * @since jEdit 4.2pre2
	 */
	public int getCurrentIdentForLine(int lineIndex, int[] whitespaceChars)
	{
		getLineText(lineIndex,seg);

		int tabSize = getTabSize();

		int currentIndent = 0;
loop:		for(int i = 0; i < seg.count; i++)
		{
			char c = seg.array[seg.offset + i];
			switch(c)
			{
			case ' ':
				currentIndent++;
				whitespaceChars[0]++;
				break;
			case '\t':
				currentIndent += (tabSize - (currentIndent
					% tabSize));
				whitespaceChars[0]++;
				break;
			default:
				break loop;
			}
		}

		return currentIndent;
	} //}}}

	//{{{ getIdealIndentForLine() method
	/**
	 * Returns the ideal leading indent for the specified line.
	 * This will apply the various auto-indent rules.
	 * @param lineIndex The line number
	 */
	public int getIdealIndentForLine(int lineIndex)
	{
		final String EXPLICIT_START = "{{{";
		final String EXPLICIT_END = "}}}";

		if(lineIndex == 0)
			return -1;

		//{{{ Get properties
		String openBrackets = getStringProperty("indentOpenBrackets");
		if(openBrackets == null)
			openBrackets = "";

		String closeBrackets = getStringProperty("indentCloseBrackets");
		if(closeBrackets == null)
			closeBrackets = "";

		RE indentNextLineRE;
		try
		{
			indentNextLineRE = getRegexpProperty("indentNextLine",
				RE.REG_ICASE,RESearchMatcher.RE_SYNTAX_JEDIT);
		}
		catch(REException re)
		{
			indentNextLineRE = null;
			Log.log(Log.ERROR,this,"Invalid indentNextLine regexp");
			Log.log(Log.ERROR,this,re);
		}

		RE indentNextLinesRE;
		try
		{
			indentNextLinesRE = getRegexpProperty("indentNextLines",
				RE.REG_ICASE,RESearchMatcher.RE_SYNTAX_JEDIT);
		}
		catch(REException re)
		{
			indentNextLinesRE = null;
			Log.log(Log.ERROR,this,"Invalid indentNextLines regexp");
			Log.log(Log.ERROR,this,re);
		}

		boolean doubleBracketIndent = getBooleanProperty("doubleBracketIndent");
		boolean lineUpClosingBracket = getBooleanProperty("lineUpClosingBracket");

		int tabSize = getTabSize();
		int indentSize = getIndentSize();
		//}}}

		//{{{ Get indent attributes of previous line
		int prevLineIndex = getPriorNonEmptyLine(lineIndex);
		if(prevLineIndex == -1)
			return -1;

		String prevLine = getLineText(prevLineIndex);

		/*
		 * On the previous line,
		 * if(bob) { --> +1
		 * if(bob) { } --> 0
		 * } else if(bob) { --> +1
		 */
		boolean prevLineStart = true; // False after initial indent
		int indent = 0; // Indent width (tab expanded)
		int prevLineBrackets = 0; // Additional bracket indent
		int prevLineCloseBracketIndex = -1; // For finding whether we're in
		                                    // this kind of construct:
		                                    // if (cond1)
		                                    //   while (cond2)
		                                    //     if (cond3){
		                                    //
		                                    //     }
		                                    // So we know to indent the next line under the 1st if.
		int prevLineUnclosedParenIndex = -1; // Index of the last unclosed parenthesis
		Stack openParens = new Stack();

		for(int i = 0; i < prevLine.length(); i++)
		{
			char c = prevLine.charAt(i);
			switch(c)
			{
			case ' ':
				if(prevLineStart)
					indent++;
				break;
			case '\t':
				if(prevLineStart)
				{
					indent += (tabSize
						- (indent
						% tabSize));
				}
				break;
			case '(':
				openParens.push(new Integer(i));
				break;
			case ')':
				if(openParens.size() > 0)
				{
					openParens.pop();
				}
				break;
			default:
				prevLineStart = false;

				if(closeBrackets.indexOf(c) != -1)
				{
					if(prevLine.regionMatches(false,
						i,EXPLICIT_END,0,3))
						i += 2;
					else
					{
						prevLineBrackets--;
						if(prevLineBrackets < 0)
						{
							if(lineUpClosingBracket)
								prevLineBrackets = 0;
							prevLineCloseBracketIndex = i;
						}
					}
				}
				else if(openBrackets.indexOf(c) != -1)
				{
					if(prevLine.regionMatches(false,
						i,EXPLICIT_START,0,3))
						i += 2;
					else
						prevLineBrackets++;
				}

				break;
			}
		}

		if(openParens.size() > 0)
		{
			prevLineUnclosedParenIndex = ((Integer) openParens.pop()).intValue();
		} //}}}

		//{{{ Get indent attributes for current line
		String line = getLineText(lineIndex);

		/*
		 * On the current line,
		 * } --> -1
		 * } else if(bob) { --> -1
		 * if(bob) { } --> 0
		 */
		int lineBrackets = 0; // Additional bracket indent
		int closeBracketIndex = -1; // For lining up closing
			// and opening brackets
		for(int i = 0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			if(closeBrackets.indexOf(c) != -1)
			{
				if(line.regionMatches(false,
					i,EXPLICIT_END,0,3))
					i += 2;
				else
				{
					closeBracketIndex = i;
					lineBrackets--;
				}
			}
			else if(openBrackets.indexOf(c) != -1)
			{
				if(line.regionMatches(false,
					i,EXPLICIT_START,0,3))
					i += 2;
				else if(lineBrackets >= 0)
					lineBrackets++;
			}
		}

		if(openParens.size() > 0)
		{
			prevLineUnclosedParenIndex = ((Integer) openParens.pop()).intValue();
		} //}}}

		//{{{ Deep indenting
		if(getBooleanProperty("deepIndent"))
		{
			if(prevLineUnclosedParenIndex != -1)
			{
				indent = prevLineUnclosedParenIndex;
				for(int i = 0; i < prevLine.length(); i++)
				{
					if(prevLine.charAt(i) == '\t')
					{
						indent += tabSize-1;
					}
					else
					{
						break;
					}
				}

				indent++;
				return indent;
			}
		}
		//}}}

		//{{{ Handle brackets
		if(prevLineBrackets > 0)
			indent += (indentSize * prevLineBrackets);

		if(lineUpClosingBracket)
		{
			if(lineBrackets < 0)
			{
				int offset = TextUtilities.findMatchingBracket(
					this,lineIndex,closeBracketIndex);
				if(offset != -1)
				{
					String closeLine = getLineText(getLineOfOffset(offset));
					indent = MiscUtilities.getLeadingWhiteSpaceWidth(
						closeLine,tabSize);
				}
				else
					return -1;
			}
		}
		else
		{
			if(prevLineBrackets < 0)
			{
				int offset = TextUtilities.findMatchingBracket(
					this,prevLineIndex,prevLineCloseBracketIndex);
				if(offset != -1)
				{
					String closeLine = getLineText(getLineOfOffset(offset));
					indent = MiscUtilities.getLeadingWhiteSpaceWidth(
						closeLine,tabSize);
				}
				else
					return -1;
			}
		}//}}}

		//{{{ Handle regexps
		if(lineBrackets >= 0)
		{
			// If the previous line matches indentNextLine or indentNextLines,
			// add a level of indent
			if((lineBrackets == 0 || doubleBracketIndent)
				&& indentNextLinesRE != null
				&& indentNextLinesRE.isMatch(prevLine))
			{
				indent += indentSize;
			}
			else if(indentNextLineRE != null)
			{
				if((lineBrackets == 0 || doubleBracketIndent)
					&& indentNextLineRE.isMatch(prevLine))
					indent += indentSize;

				// we don't want
				// if(foo)
				// {
				// <--- decreased indent
				else if(prevLineBrackets == 0)
				{
					// While prior lines match indentNextLine, remove a level of indent
					// this correctly handles constructs like:
					// if(foo)
					//     if(bar)
					//         if(baz)
					// <--- put indent here
					int prevPrevLineIndex;
					/* if(prevLineCloseBracketIndex != -1)
					{
						int offset = TextUtilities.findMatchingBracket(
							this,prevLineIndex,prevLineCloseBracketIndex);
						if(offset == -1)
							return -1;
						prevPrevLineIndex = getLineOfOffset(offset);
					}
					else */
						prevPrevLineIndex = getPriorNonEmptyLine(prevLineIndex);

					while(prevPrevLineIndex != -1)
					{
						if(indentNextLineRE.isMatch(getLineText(prevPrevLineIndex)))
							indent -= indentSize;
						else
							break;

						prevPrevLineIndex = getPriorNonEmptyLine(prevPrevLineIndex);
					}
				}
			}
		} //}}}

		return indent;
	} //}}}

	//{{{ getVirtualWidth() method
	/**
	 * Returns the virtual column number (taking tabs into account) of the
	 * specified position.
	 *
	 * @param line The line number
	 * @param column The column number
	 * @since jEdit 4.1pre1
	 */
	public int getVirtualWidth(int line, int column)
	{
		try
		{
			readLock();

			int start = getLineStartOffset(line);
			getText(start,column,seg);

			return MiscUtilities.getVirtualWidth(seg,getTabSize());
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ getOffsetOfVirtualColumn() method
	/**
	 * Returns the offset of a virtual column number (taking tabs
	 * into account) relative to the start of the line in question.
	 *
	 * @param line The line number
	 * @param column The virtual column number
	 * @param totalVirtualWidth If this array is non-null, the total
	 * virtual width will be stored in its first location if this method
	 * returns -1.
	 *
	 * @return -1 if the column is out of bounds
	 *
	 * @since jEdit 4.1pre1
	 */
	public int getOffsetOfVirtualColumn(int line, int column,
		int[] totalVirtualWidth)
	{
		try
		{
			readLock();

			getLineText(line,seg);

			return MiscUtilities.getOffsetOfVirtualColumn(seg,
				getTabSize(),column,totalVirtualWidth);
		}
		finally
		{
			readUnlock();
		}
	} //}}}

	//{{{ insertAtColumn()
	/**
	 * Like the {@link #insert(int,String)} method, but inserts the string at
	 * the specified virtual column. Inserts spaces as appropriate if
	 * the line is shorter than the column.
	 * @param line The line number
	 * @param col The virtual column number
	 * @param str The string
	 */
	public void insertAtColumn(int line, int col, String str)
	{
		try
		{
			writeLock();

			int[] total = new int[1];
			int offset = getOffsetOfVirtualColumn(line,col,total);
			if(offset == -1)
			{
				offset = getLineEndOffset(line) - 1;
				str = MiscUtilities.createWhiteSpace(col - total[0],0) + str;
			}
			else
				offset += getLineStartOffset(line);

			insert(offset,str);
		}
		finally
		{
			writeUnlock();
		}
	} //}}}

	//}}}

	//{{{ Deprecated methods

	//{{{ putProperty() method
	/**
	 * @deprecated Call <code>setProperty()</code> instead.
	 */
	public void putProperty(Object name, Object value)
	{
		// for backwards compatibility
		if(!(name instanceof String))
			return;

		setProperty((String)name,value);
	} //}}}

	//{{{ putBooleanProperty() method
	/**
	 * @deprecated Call <code>setBooleanProperty()</code> instead
	 */
	public void putBooleanProperty(String name, boolean value)
	{
		setBooleanProperty(name,value);
	} //}}}

	//{{{ markTokens() method
	/**
	 * @deprecated Use org.gjt.sp.jedit.syntax.DefaultTokenHandler instead
	 */
	public static class TokenList extends DefaultTokenHandler
	{
		public Token getFirstToken()
		{
			return getTokens();
		}
	}

	/**
	 * @deprecated Use the other form of <code>markTokens()</code> instead
	 */
	public TokenList markTokens(int lineIndex)
	{
		TokenList list = new TokenList();
		markTokens(lineIndex,list);
		return list;
	} //}}}

	//{{{ getRootElements() method
	/**
	 * @deprecated
	 */
	public Element[] g
