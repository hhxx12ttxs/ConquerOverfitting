/*
 * Copyright 1997-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package javax.swing.text;

import java.util.*;
import java.io.*;
import java.awt.font.TextAttribute;
import java.text.Bidi;

import javax.swing.UIManager;
import javax.swing.undo.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.*;
import javax.swing.tree.TreeNode;

import sun.font.BidiUtils;
import sun.swing.SwingUtilities2;

/**
 * An implementation of the document interface to serve as a
 * basis for implementing various kinds of documents.  At this
 * level there is very little policy, so there is a corresponding
 * increase in difficulty of use.
 * <p>
 * This class implements a locking mechanism for the document.  It
 * allows multiple readers or one writer, and writers must wait until
 * all observers of the document have been notified of a previous
 * change before beginning another mutation to the document.  The
 * read lock is acquired and released using the <code>render</code>
 * method.  A write lock is aquired by the methods that mutate the
 * document, and are held for the duration of the method call.
 * Notification is done on the thread that produced the mutation,
 * and the thread has full read access to the document for the
 * duration of the notification, but other readers are kept out
 * until the notification has finished.  The notification is a
 * beans event notification which does not allow any further
 * mutations until all listeners have been notified.
 * <p>
 * Any models subclassed from this class and used in conjunction
 * with a text component that has a look and feel implementation
 * that is derived from BasicTextUI may be safely updated
 * asynchronously, because all access to the View hierarchy
 * is serialized by BasicTextUI if the document is of type
 * <code>AbstractDocument</code>.  The locking assumes that an
 * independent thread will access the View hierarchy only from
 * the DocumentListener methods, and that there will be only
 * one event thread active at a time.
 * <p>
 * If concurrency support is desired, there are the following
 * additional implications.  The code path for any DocumentListener
 * implementation and any UndoListener implementation must be threadsafe,
 * and not access the component lock if trying to be safe from deadlocks.
 * The <code>repaint</code> and <code>revalidate</code> methods
 * on JComponent are safe.
 * <p>
 * AbstractDocument models an implied break at the end of the document.
 * Among other things this allows you to position the caret after the last
 * character. As a result of this, <code>getLength</code> returns one less
 * than the length of the Content. If you create your own Content, be
 * sure and initialize it to have an additional character. Refer to
 * StringContent and GapContent for examples of this. Another implication
 * of this is that Elements that model the implied end character will have
 * an endOffset == (getLength() + 1). For example, in DefaultStyledDocument
 * <code>getParagraphElement(getLength()).getEndOffset() == getLength() + 1
 * </code>.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @author  Timothy Prinzing
 */
public abstract class AbstractDocument implements Document, Serializable {

    /**
     * Constructs a new <code>AbstractDocument</code>, wrapped around some
     * specified content storage mechanism.
     *
     * @param data the content
     */
    protected AbstractDocument(Content data) {
        this(data, StyleContext.getDefaultStyleContext());
    }

    /**
     * Constructs a new <code>AbstractDocument</code>, wrapped around some
     * specified content storage mechanism.
     *
     * @param data the content
     * @param context the attribute context
     */
    protected AbstractDocument(Content data, AttributeContext context) {
        this.data = data;
        this.context = context;
        bidiRoot = new BidiRootElement();

        if (defaultI18NProperty == null) {
            // determine default setting for i18n support
            Object o = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return System.getProperty(I18NProperty);
                    }
                }
            );
            if (o != null) {
                defaultI18NProperty = Boolean.valueOf((String)o);
            } else {
                defaultI18NProperty = Boolean.FALSE;
            }
        }
        putProperty( I18NProperty, defaultI18NProperty);

        //REMIND(bcb) This creates an initial bidi element to account for
        //the \n that exists by default in the content.  Doing it this way
        //seems to expose a little too much knowledge of the content given
        //to us by the sub-class.  Consider having the sub-class' constructor
        //make an initial call to insertUpdate.
        writeLock();
        try {
            Element[] p = new Element[1];
            p[0] = new BidiElement( bidiRoot, 0, 1, 0 );
            bidiRoot.replace(0,0,p);
        } finally {
            writeUnlock();
        }
    }

    /**
     * Supports managing a set of properties. Callers
     * can use the <code>documentProperties</code> dictionary
     * to annotate the document with document-wide properties.
     *
     * @return a non-<code>null</code> <code>Dictionary</code>
     * @see #setDocumentProperties
     */
    public Dictionary<Object,Object> getDocumentProperties() {
        if (documentProperties == null) {
            documentProperties = new Hashtable(2);
        }
        return documentProperties;
    }

    /**
     * Replaces the document properties dictionary for this document.
     *
     * @param x the new dictionary
     * @see #getDocumentProperties
     */
    public void setDocumentProperties(Dictionary<Object,Object> x) {
        documentProperties = x;
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param e the event
     * @see EventListenerList
     */
    protected void fireInsertUpdate(DocumentEvent e) {
        notifyingListeners = true;
        try {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DocumentListener.class) {
                    // Lazily create the event:
                    // if (e == null)
                    // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                    ((DocumentListener)listeners[i+1]).insertUpdate(e);
                }
            }
        } finally {
            notifyingListeners = false;
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param e the event
     * @see EventListenerList
     */
    protected void fireChangedUpdate(DocumentEvent e) {
        notifyingListeners = true;
        try {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DocumentListener.class) {
                    // Lazily create the event:
                    // if (e == null)
                    // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                    ((DocumentListener)listeners[i+1]).changedUpdate(e);
                }
            }
        } finally {
            notifyingListeners = false;
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param e the event
     * @see EventListenerList
     */
    protected void fireRemoveUpdate(DocumentEvent e) {
        notifyingListeners = true;
        try {
            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();
            // Process the listeners last to first, notifying
            // those that are interested in this event
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==DocumentListener.class) {
                    // Lazily create the event:
                    // if (e == null)
                    // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                    ((DocumentListener)listeners[i+1]).removeUpdate(e);
                }
            }
        } finally {
            notifyingListeners = false;
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param e the event
     * @see EventListenerList
     */
    protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==UndoableEditListener.class) {
                // Lazily create the event:
                // if (e == null)
                // e = new ListSelectionEvent(this, firstIndex, lastIndex);
                ((UndoableEditListener)listeners[i+1]).undoableEditHappened(e);
            }
        }
    }

    /**
     * Returns an array of all the objects currently registered
     * as <code><em>Foo</em>Listener</code>s
     * upon this document.
     * <code><em>Foo</em>Listener</code>s are registered using the
     * <code>add<em>Foo</em>Listener</code> method.
     *
     * <p>
     * You can specify the <code>listenerType</code> argument
     * with a class literal, such as
     * <code><em>Foo</em>Listener.class</code>.
     * For example, you can query a
     * document <code>d</code>
     * for its document listeners with the following code:
     *
     * <pre>DocumentListener[] mls = (DocumentListener[])(d.getListeners(DocumentListener.class));</pre>
     *
     * If no such listeners exist, this method returns an empty array.
     *
     * @param listenerType the type of listeners requested; this parameter
     *          should specify an interface that descends from
     *          <code>java.util.EventListener</code>
     * @return an array of all objects registered as
     *          <code><em>Foo</em>Listener</code>s on this component,
     *          or an empty array if no such
     *          listeners have been added
     * @exception ClassCastException if <code>listenerType</code>
     *          doesn't specify a class or interface that implements
     *          <code>java.util.EventListener</code>
     *
     * @see #getDocumentListeners
     * @see #getUndoableEditListeners
     *
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return listenerList.getListeners(listenerType);
    }

    /**
     * Gets the asynchronous loading priority.  If less than zero,
     * the document should not be loaded asynchronously.
     *
     * @return the asynchronous loading priority, or <code>-1</code>
     *   if the document should not be loaded asynchronously
     */
    public int getAsynchronousLoadPriority() {
        Integer loadPriority = (Integer)
            getProperty(AbstractDocument.AsyncLoadPriority);
        if (loadPriority != null) {
            return loadPriority.intValue();
        }
        return -1;
    }

    /**
     * Sets the asynchronous loading priority.
     * @param p the new asynchronous loading priority; a value
     *   less than zero indicates that the document should not be
     *   loaded asynchronously
     */
    public void setAsynchronousLoadPriority(int p) {
        Integer loadPriority = (p >= 0) ? new Integer(p) : null;
        putProperty(AbstractDocument.AsyncLoadPriority, loadPriority);
    }

    /**
     * Sets the <code>DocumentFilter</code>. The <code>DocumentFilter</code>
     * is passed <code>insert</code> and <code>remove</code> to conditionally
     * allow inserting/deleting of the text.  A <code>null</code> value
     * indicates that no filtering will occur.
     *
     * @param filter the <code>DocumentFilter</code> used to constrain text
     * @see #getDocumentFilter
     * @since 1.4
     */
    public void setDocumentFilter(DocumentFilter filter) {
        documentFilter = filter;
    }

    /**
     * Returns the <code>DocumentFilter</code> that is responsible for
     * filtering of insertion/removal. A <code>null</code> return value
     * implies no filtering is to occur.
     *
     * @since 1.4
     * @see #setDocumentFilter
     * @return the DocumentFilter
     */
    public DocumentFilter getDocumentFilter() {
        return documentFilter;
    }

    // --- Document methods -----------------------------------------

    /**
     * This allows the model to be safely rendered in the presence
     * of currency, if the model supports being updated asynchronously.
     * The given runnable will be executed in a way that allows it
     * to safely read the model with no changes while the runnable
     * is being executed.  The runnable itself may <em>not</em>
     * make any mutations.
     * <p>
     * This is implemented to aquire a read lock for the duration
     * of the runnables execution.  There may be multiple runnables
     * executing at the same time, and all writers will be blocked
     * while there are active rendering runnables.  If the runnable
     * throws an exception, its lock will be safely released.
     * There is no protection against a runnable that never exits,
     * which will effectively leave the document locked for it's
     * lifetime.
     * <p>
     * If the given runnable attempts to make any mutations in
     * this implementation, a deadlock will occur.  There is
     * no tracking of individual rendering threads to enable
     * detecting this situation, but a subclass could incur
     * the overhead of tracking them and throwing an error.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param r the renderer to execute
     */
    public void render(Runnable r) {
        readLock();
        try {
            r.run();
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns the length of the data.  This is the number of
     * characters of content that represents the users data.
     *
     * @return the length >= 0
     * @see Document#getLength
     */
    public int getLength() {
        return data.length() - 1;
    }

    /**
     * Adds a document listener for notification of any changes.
     *
     * @param listener the <code>DocumentListener</code> to add
     * @see Document#addDocumentListener
     */
    public void addDocumentListener(DocumentListener listener) {
        listenerList.add(DocumentListener.class, listener);
    }

    /**
     * Removes a document listener.
     *
     * @param listener the <code>DocumentListener</code> to remove
     * @see Document#removeDocumentListener
     */
    public void removeDocumentListener(DocumentListener listener) {
        listenerList.remove(DocumentListener.class, listener);
    }

    /**
     * Returns an array of all the document listeners
     * registered on this document.
     *
     * @return all of this document's <code>DocumentListener</code>s
     *         or an empty array if no document listeners are
     *         currently registered
     *
     * @see #addDocumentListener
     * @see #removeDocumentListener
     * @since 1.4
     */
    public DocumentListener[] getDocumentListeners() {
        return (DocumentListener[])listenerList.getListeners(
                DocumentListener.class);
    }

    /**
     * Adds an undo listener for notification of any changes.
     * Undo/Redo operations performed on the <code>UndoableEdit</code>
     * will cause the appropriate DocumentEvent to be fired to keep
     * the view(s) in sync with the model.
     *
     * @param listener the <code>UndoableEditListener</code> to add
     * @see Document#addUndoableEditListener
     */
    public void addUndoableEditListener(UndoableEditListener listener) {
        listenerList.add(UndoableEditListener.class, listener);
    }

    /**
     * Removes an undo listener.
     *
     * @param listener the <code>UndoableEditListener</code> to remove
     * @see Document#removeDocumentListener
     */
    public void removeUndoableEditListener(UndoableEditListener listener) {
        listenerList.remove(UndoableEditListener.class, listener);
    }

    /**
     * Returns an array of all the undoable edit listeners
     * registered on this document.
     *
     * @return all of this document's <code>UndoableEditListener</code>s
     *         or an empty array if no undoable edit listeners are
     *         currently registered
     *
     * @see #addUndoableEditListener
     * @see #removeUndoableEditListener
     *
     * @since 1.4
     */
    public UndoableEditListener[] getUndoableEditListeners() {
        return (UndoableEditListener[])listenerList.getListeners(
                UndoableEditListener.class);
    }

    /**
     * A convenience method for looking up a property value. It is
     * equivalent to:
     * <pre>
     * getDocumentProperties().get(key);
     * </pre>
     *
     * @param key the non-<code>null</code> property key
     * @return the value of this property or <code>null</code>
     * @see #getDocumentProperties
     */
    public final Object getProperty(Object key) {
        return getDocumentProperties().get(key);
    }


    /**
     * A convenience method for storing up a property value.  It is
     * equivalent to:
     * <pre>
     * getDocumentProperties().put(key, value);
     * </pre>
     * If <code>value</code> is <code>null</code> this method will
     * remove the property.
     *
     * @param key the non-<code>null</code> key
     * @param value the property value
     * @see #getDocumentProperties
     */
    public final void putProperty(Object key, Object value) {
        if (value != null) {
            getDocumentProperties().put(key, value);
        } else {
            getDocumentProperties().remove(key);
        }
        if( key == TextAttribute.RUN_DIRECTION
            && Boolean.TRUE.equals(getProperty(I18NProperty)) )
        {
            //REMIND - this needs to flip on the i18n property if run dir
            //is rtl and the i18n property is not already on.
            writeLock();
            try {
                DefaultDocumentEvent e
                    = new DefaultDocumentEvent(0, getLength(),
                                               DocumentEvent.EventType.INSERT);
                updateBidi( e );
            } finally {
                writeUnlock();
            }
        }
    }

    /**
     * Removes some content from the document.
     * Removing content causes a write lock to be held while the
     * actual changes are taking place.  Observers are notified
     * of the change on the thread that called this method.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param offs the starting offset >= 0
     * @param len the number of characters to remove >= 0
     * @exception BadLocationException  the given remove position is not a valid
     *   position within the document
     * @see Document#remove
     */
    public void remove(int offs, int len) throws BadLocationException {
        DocumentFilter filter = getDocumentFilter();

        writeLock();
        try {
            if (filter != null) {
                filter.remove(getFilterBypass(), offs, len);
            }
            else {
                handleRemove(offs, len);
            }
        } finally {
            writeUnlock();
        }
    }

    /**
     * Performs the actual work of the remove. It is assumed the caller
     * will have obtained a <code>writeLock</code> before invoking this.
     */
    void handleRemove(int offs, int len) throws BadLocationException {
        if (len > 0) {
            if (offs < 0 || (offs + len) > getLength()) {
                throw new BadLocationException("Invalid remove",
                                               getLength() + 1);
            }
            DefaultDocumentEvent chng =
                    new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.REMOVE);

            boolean isComposedTextElement = false;
            // Check whether the position of interest is the composed text
            isComposedTextElement = Utilities.isComposedTextElement(this, offs);

            removeUpdate(chng);
            UndoableEdit u = data.remove(offs, len);
            if (u != null) {
                chng.addEdit(u);
            }
            postRemoveUpdate(chng);
            // Mark the edit as done.
            chng.end();
            fireRemoveUpdate(chng);
            // only fire undo if Content implementation supports it
            // undo for the composed text is not supported for now
            if ((u != null) && !isComposedTextElement) {
                fireUndoableEditUpdate(new UndoableEditEvent(this, chng));
            }
        }
    }

    /**
     * Deletes the region of text from <code>offset</code> to
     * <code>offset + length</code>, and replaces it with <code>text</code>.
     * It is up to the implementation as to how this is implemented, some
     * implementations may treat this as two distinct operations: a remove
     * followed by an insert, others may treat the replace as one atomic
     * operation.
     *
     * @param offset index of child element
     * @param length length of text to delete, may be 0 indicating don't
     *               delete anything
     * @param text text to insert, <code>null</code> indicates no text to insert
     * @param attrs AttributeSet indicating attributes of inserted text,
     *              <code>null</code>
     *              is legal, and typically treated as an empty attributeset,
     *              but exact interpretation is left to the subclass
     * @exception BadLocationException the given position is not a valid
     *            position within the document
     * @since 1.4
     */
    public void replace(int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
        if (length == 0 && (text == null || text.length() == 0)) {
            return;
        }
        DocumentFilter filter = getDocumentFilter();

        writeLock();
        try {
            if (filter != null) {
                filter.replace(getFilterBypass(), offset, length, text,
                               attrs);
            }
            else {
                if (length > 0) {
                    remove(offset, length);
                }
                if (text != null && text.length() > 0) {
                    insertString(offset, text, attrs);
                }
            }
        } finally {
            writeUnlock();
        }
    }

    /**
     * Inserts some content into the document.
     * Inserting content causes a write lock to be held while the
     * actual changes are taking place, followed by notification
     * to the observers on the thread that grabbed the write lock.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param offs the starting offset >= 0
     * @param str the string to insert; does nothing with null/empty strings
     * @param a the attributes for the inserted content
     * @exception BadLocationException  the given insert position is not a valid
     *   position within the document
     * @see Document#insertString
     */
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if ((str == null) || (str.length() == 0)) {
            return;
        }
        DocumentFilter filter = getDocumentFilter();

        writeLock();
        try {
            if (filter != null) {
                filter.insertString(getFilterBypass(), offs, str, a);
            }
            else {
                handleInsertString(offs, str, a);
            }
        } finally {
            writeUnlock();
        }
    }

    /**
     * Performs the actual work of inserting the text; it is assumed the
     * caller has obtained a write lock before invoking this.
     */
    void handleInsertString(int offs, String str, AttributeSet a)
                         throws BadLocationException {
        if ((str == null) || (str.length() == 0)) {
            return;
        }
        UndoableEdit u = data.insertString(offs, str);
        DefaultDocumentEvent e =
            new DefaultDocumentEvent(offs, str.length(), DocumentEvent.EventType.INSERT);
        if (u != null) {
            e.addEdit(u);
        }

        // see if complex glyph layout support is needed
        if( getProperty(I18NProperty).equals( Boolean.FALSE ) ) {
            // if a default direction of right-to-left has been specified,
            // we want complex layout even if the text is all left to right.
            Object d = getProperty(TextAttribute.RUN_DIRECTION);
            if ((d != null) && (d.equals(TextAttribute.RUN_DIRECTION_RTL))) {
                putProperty( I18NProperty, Boolean.TRUE);
            } else {
                char[] chars = str.toCharArray();
                if (SwingUtilities2.isComplexLayout(chars, 0, chars.length)) {
                    putProperty( I18NProperty, Boolean.TRUE);
                }
            }
        }

        insertUpdate(e, a);
        // Mark the edit as done.
        e.end();
        fireInsertUpdate(e);
        // only fire undo if Content implementation supports it
        // undo for the composed text is not supported for now
        if (u != null &&
            (a == null || !a.isDefined(StyleConstants.ComposedTextAttribute))) {
            fireUndoableEditUpdate(new UndoableEditEvent(this, e));
        }
    }

    /**
     * Gets a sequence of text from the document.
     *
     * @param offset the starting offset >= 0
     * @param length the number of characters to retrieve >= 0
     * @return the text
     * @exception BadLocationException  the range given includes a position
     *   that is not a valid position within the document
     * @see Document#getText
     */
    public String getText(int offset, int length) throws BadLocationException {
        if (length < 0) {
            throw new BadLocationException("Length must be positive", length);
        }
        String str = data.getString(offset, length);
        return str;
    }

    /**
     * Fetches the text contained within the given portion
     * of the document.
     * <p>
     * If the partialReturn property on the txt parameter is false, the
     * data returned in the Segment will be the entire length requested and
     * may or may not be a copy depending upon how the data was stored.
     * If the partialReturn property is true, only the amount of text that
     * can be returned without creating a copy is returned.  Using partial
     * returns will give better performance for situations where large
     * parts of the document are being scanned.  The following is an example
     * of using the partial return to access the entire document:
     * <p>
     * <pre>
     * &nbsp; int nleft = doc.getDocumentLength();
     * &nbsp; Segment text = new Segment();
     * &nbsp; int offs = 0;
     * &nbsp; text.setPartialReturn(true);
     * &nbsp; while (nleft > 0) {
     * &nbsp;     doc.getText(offs, nleft, text);
     * &nbsp;     // do something with text
     * &nbsp;     nleft -= text.count;
     * &nbsp;     offs += text.count;
     * &nbsp; }
     * </pre>
     *
     * @param offset the starting offset >= 0
     * @param length the number of characters to retrieve >= 0
     * @param txt the Segment object to retrieve the text into
     * @exception BadLocationException  the range given includes a position
     *   that is not a valid position within the document
     */
    public void getText(int offset, int length, Segment txt) throws BadLocationException {
        if (length < 0) {
            throw new BadLocationException("Length must be positive", length);
        }
        data.getChars(offset, length, txt);
    }

    /**
     * Returns a position that will track change as the document
     * is altered.
     * <p>
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * <A HREF="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
     * to Use Threads</A> for more information.
     *
     * @param offs the position in the model >= 0
     * @return the position
     * @exception BadLocationException  if the given position does not
     *   represent a valid location in the associated document
     * @see Document#createPosition
     */
    public synchronized Position createPosition(int offs) throws BadLocationException {
        return data.createPosition(offs);
    }

    /**
     * Returns a position that represents the start of the document.  The
     * position returned can be counted on to track change and stay
     * located at the beginning of the document.
     *
     * @return the position
     */
    public final Position getStartPosition() {
        Position p;
        try {
            p = createPosition(0);
        } catch (BadLocationException bl) {
            p = null;
        }
        return p;
    }

    /**
     * Returns a position that represents the end of the document.  The
     * position returned can be counted on to track change and stay
     * located at the end of the document.
     *
     * @return the position
     */
    public final Position getEndPosition() {
        Position p;
        try {
            p = createPosition(data.length());
        } catch (BadLocationException bl) {
            p = null;
        }
        return p;
    }

    /**
     * Gets all root elements defined.  Typically, there
     * will only be one so the default implementation
     * is to return the default root element.
     *
     * @return the root element
     */
    public Element[] getRootElements() {
        Element[] elems = new Element[2];
        elems[0] = getDefaultRootElement();
        elems[1] = getBidiRootElement();
        return elems;
    }

    /**
     * Returns the root element that views should be based upon
     * unless some other mechanism for assigning views to element
     * structures is provided.
     *
     * @return the root element
     * @see Document#getDefaultRootElement
     */
    public abstract Element getDefaultRootElement();

    // ---- local methods -----------------------------------------

    /**
     * Returns the <code>FilterBypass</code>. This will create one if one
     * does not yet exist.
     */
    private DocumentFilter.FilterBypass getFilterBypass() {
        if (filterBypass == null) {
            filterBypass = new DefaultFilterBypass();
        }
        return filterBypass;
    }

    /**
     * Returns the root element of the bidirectional structure for this
     * document.  Its children represent character runs with a given
     * Unicode bidi level.
     */
    public Element getBidiRootElement() {
        return bidiRoot;
    }

    /**
     * Returns true if the text in the range <code>p0</code> to
     * <code>p1</code> is left to right.
     */
    boolean isLeftToRight(int p0, int p1) {
        if(!getProperty(I18NProperty).equals(Boolean.TRUE)) {
            return true;
        }
        Element bidiRoot = getBidiRootElement();
        int index = bidiRoot.getElementIndex(p0);
        Element bidiElem = bidiRoot.getElement(index);
        if(bidiElem.getEndOffset() >= p1) {
            AttributeSet bidiAttrs = bidiElem.getAttributes();
            return ((StyleConstants.getBidiLevel(bidiAttrs) % 2) == 0);
        }
        return true;
    }

    /**
     * Get the paragraph element containing the given position.  Sub-classes
     * must define for themselves what exactly constitutes a paragraph.  They
     * should keep in mind however that a paragraph should at least be the
     * unit of text over which to run the Unicode bidirectional algorithm.
     *
     * @param pos the starting offset >= 0
     * @return the element */
    public abstract Element getParagraphElement(int pos);


    /**
     * Fetches the context for managing attributes.  This
     * method effectively establishes the strategy used
     * for compressing AttributeSet information.
     *
     * @return the context
     */
    protected final AttributeContext getAttributeContext() {
        return context;
    }

    /**
     * Updates document structure as a result of text insertion.  This
     * will happen within a write lock.  If a subclass of
     * this class reimplements this method, it should delegate to the
     * superclass as well.
     *
     * @param chng a description of the change
     * @param attr the attributes for the change
     */
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
        if( getProperty(I18NProperty).equals( Boolean.TRUE ) )
            updateBidi( chng );

        // Check if a multi byte is encountered in the inserted text.
        if (chng.type == DocumentEvent.EventType.INSERT &&
                        chng.getLength() > 0 &&
                        !Boolean.TRUE.equals(getProperty(MultiByteProperty))) {
            Segment segment = SegmentCache.getSharedSegment();
            try {
                getText(chng.getOffset(), chng.getLength(), segment);
                segment.first();
                do {
                    if ((int)segment.current() > 255) {
                        putProperty(MultiByteProperty, Boolean.TRUE);
                        break;
                    }
                } while (segment.next() != Segment.DONE);
            } catch (BadLocationException ble) {
                // Should never happen
            }
            SegmentCache.releaseSharedSegment(segment);
        }
    }

    /**
     * Updates any document structure as a result of text removal.  This
     * method is called before the text is actually removed from the Content.
     * This will happen within a write lock. If a subclass
     * of this class reimplements this method, it should delegate to the
     * superclass as well.
     *
     * @param chng a description of the change
     */
    protected void removeUpdate(DefaultDocumentEvent chng) {
    }

    /**
     * Updates any document structure as a result of text removal.  This
     * method is called after the text has been removed from the Content.
     * This will happen within a write lock. If a subclass
     * of this class reimplements this method, it should delegate to the
     * superclass as well.
     *
     * @param chng a description of the change
     */
    protected void postRemoveUpdate(DefaultDocumentEvent chng) {
        if( getProperty(I18NProperty).equals( Boolean.TRUE ) )
            updateBidi( chng );
    }


    /**
     * Update the bidi element structure as a result of the given change
     * to the document.  The given change will be updated to reflect the
     * changes made to the bidi structure.
     *
     * This method assumes that every offset in the model is contained in
     * exactly one paragraph.  This method also assumes that it is called
     * after the change is made to the default element structure.
     */
    void updateBidi( DefaultDocumentEvent chng ) {

        // Calculate the range of paragraphs affected by the change.
        int firstPStart;
        int lastPEnd;
        if( chng.type == DocumentEvent.EventType.INSERT
            || chng.type == DocumentEvent.EventType.CHANGE )
        {
            int chngStart = chng.getOffset();
            int chngEnd =  chngStart + chng.getLength();
            firstPStart = getParagraphElement(chngStart).getStartOffset();
            lastPEnd = getParagraphElement(chngEnd).getEndOffset();
        } else if( chng.type == DocumentEvent.EventType.REMOVE ) {
            Element paragraph = getParagraphElement( chng.getOffset() );
            firstPStart = paragraph.getStartOffset();
            lastPEnd = paragraph.getEndOffset();
        } else {
            throw new Error("Internal error: unknown event type.");
        }
        //System.out.println("updateBidi: firstPStart = " + firstPStart + " lastPEnd = " + lastPEnd );


        // Calculate the bidi levels for the affected range of paragraphs.  The
        // levels array will contain a bidi level for each character in the
        // affected text.
        byte levels[] = calculateBidiLevels( firstPStart, lastPEnd );


        Vector newElements = new Vector();

        // Calculate the first span of characters in the affected range with
        // the same bidi level.  If this level is the same as the level of the
        // previous bidi element (the existing bidi element containing
        // firstPStart-1), then merge in the previous element.  If not, but
        // the previous element overlaps the affected range, truncate the
        // previous element at firstPStart.
        int firstSpanStart = firstPStart;
        int removeFromIndex = 0;
        if( firstSpanStart > 0 ) {
            int prevElemIndex = bidiRoot.getElementIndex(firstPStart-1);
            removeFromIndex = prevElemIndex;
            Element prevElem = bidiRoot.getElement(prevElemIndex);
            int prevLevel=StyleConstants.getBidiLevel(prevElem.getAttributes());
            //System.out.println("createbidiElements: prevElem= " + prevElem  + " prevLevel= " + prevLevel + "level[0] = " + levels[0]);
            if( prevLevel==levels[0] ) {
                firstSpanStart = prevElem.getStartOffset();
            } else if( prevElem.getEndOffset() > firstPStart ) {
                newElements.addElement(new BidiElement(bidiRoot,
                                                       prevElem.getStartOffset(),
                                                       firstPStart, prevLevel));
            } else {
                removeFromIndex++;
            }
        }

        int firstSpanEnd = 0;
        while((firstSpanEnd<levels.length) && (levels[firstSpanEnd]==levels[0]))
            firstSpanEnd++;


        // Calculate the last span of characters in the affected range with
        // the same bidi level.  If this level is the same as the level of the
        // next bidi element (the existing bidi element containing lastPEnd),
        // then merge in the next element.  If not, but the next element
        // overlaps the affected range, adjust the next element to start at
        // lastPEnd.
        int lastSpanEnd = lastPEnd;
        Element newNextElem = null;
        int removeToIndex = bidiRoot.getElementCount() - 1;
        if( lastSpanEnd <= getLength() ) {
            int nextElemIndex = bidiRoot.getElementIndex( lastPEnd );
            removeToIndex = nextElemIndex;
            Element nextElem = bidiRoot.getElement( nextElemIndex );
            int nextLevel = StyleConstants.getBidiLevel(nextElem.getAttributes());
            if( nextLevel == levels[levels.length-1] ) {
                lastSpanEnd = nextElem.getEndOffset();
            } else if( nextElem.getStartOffset() < lastPEnd ) {
                newNextElem = new BidiElement(bidiRoot, lastPEnd,
                                              nextElem.getEndOffset(),
                                              nextLevel);
            } else {
                removeToIndex--;
            }
        }

        int lastSpanStart = levels.length;
        while( (lastSpanStart>firstSpanEnd)
               && (levels[lastSpanStart-1]==levels[levels.length-1]) )
            lastSpanStart--;


        // If the first and last spans are contiguous and have the same level,
        // merge them and create a single new element for the entire span.
        // Otherwise, create elements for the first and last spans as well as
        // any spans in between.
        if((firstSpanEnd==lastSpanStart)&&(levels[0]==levels[levels.length-1])){
            newElements.addElement(new BidiElement(bidiRoot, firstSpanStart,
                                                   lastSpanEnd, levels[0]));
        } else {
            // Create an element for the first span.
            newElements.addElement(new BidiElement(bidiRoot, firstSpanStart,
                                                   firstSpanEnd+firstPStart,
                                                   levels[0]));
            // Create elements for the spans in between the first and last
            for( int i=firstSpanEnd; i<lastSpanStart; ) {
                //System.out.println("executed line 872");
                int j;
                for( j=i;  (j<levels.length) && (levels[j] == levels[i]); j++ );
                newElements.addElement(new BidiElement(bidiRoot, firstPStart+i,
                                                       firstPStart+j,
                                                       (int)levels[i]));
                i=j;
            }
            // Create an element for the last span.
            newElements.addElement(new BidiElement(bidiRoot,
                                                   lastSpanStart+firstPStart,
                                                   lastSpanEnd,
                                                   levels[levels.length-1]));
        }

        if( newNextElem != null )
            newElements.addElement( newNextElem );


        // Calculate the set of existing bidi elements which must be
        // removed.
        int removedElemCount = 0;
        if( bidiRoot.getElementCount() > 0 ) {
            removedElemCount = removeToIndex - removeFromIndex + 1;
        }
        Element[] removedElems = new Element[removedElemCount];
        for( int i=0; i<removedElemCount; i++ ) {
            removedElems[i] = bidiRoot.getElement(removeFromIndex+i);
        }

        Element[] addedElems = new Element[ newElements.size() ];
        newElements.copyInto( addedElems );

        // Update the change record.
        ElementEdit ee = new ElementEdit( bidiRoot, removeFromIndex,
                                          removedElems, addedElems );
        chng.addEdit( ee );

        // Update the bidi element structure.
        bidiRoot.replace( removeFromIndex, removedElems.length, addedElems );
    }


    /**
     * Calculate the levels array for a range of paragraphs.
     */
    private byte[] calculateBidiLevels( int firstPStart, int lastPEnd ) {

        byte levels[] = new byte[ lastPEnd - firstPStart ];
        int  levelsEnd = 0;
        Boolean defaultDirection = null;
        Object d = getProperty(TextAttribute.RUN_DIRECTION);
        if (d instanceof Boolean) {
            defaultDirection = (Boolean) d;
        }

        // For each paragraph in the given range of paragraphs, get its
        // levels array and add it to the levels array for the entire span.
        for(int o=firstPStart; o<lastPEnd; ) {
            Element p = getParagraphElement( o );
            int pStart = p.getStartOffset();
            int pEnd = p.getEndOffset();

            // default run direction for the paragraph.  This will be
            // null if there is no direction override specified (i.e.
            // the direction will be determined from the content).
            Boolean direction = defaultDirection;
            d = p.getAttributes().getAttribute(TextAttribute.RUN_DIRECTION);
            if (d instanceof Boolean) {
                direction = (Boolean) d;
            }

            //System.out.println("updateBidi: paragraph start = " + pStart + " paragraph end = " + pEnd);

            // Create a Bidi over this paragraph then get the level
            // array.
            Segment seg = SegmentCache.getSharedSegment();
            try {
                getText(pStart, pEnd-pStart, seg);
            } catch (BadLocationException e ) {
                throw new Error("Internal error: " + e.toString());
            }
            // REMIND(bcb) we should really be using a Segment here.
            Bidi bidiAnalyzer;
            int bidiflag = Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT;
            if (direction != null) {
                if (TextAttribute.RUN_DIRECTION_LTR.equals(direction)) {
                    bidiflag = Bidi.DIRECTION_LEFT_TO_RIGHT;
                } else {
                    bidiflag = Bidi.DIRECTION_RIGHT_TO_LEFT;
                }
            }
            bidiAnalyzer = new Bidi(seg.array, seg.offset, null, 0, seg.count,
                    bidiflag);
            BidiUtils.getLevels(bidiAnalyzer, levels, levelsEnd);
            levelsEnd += bidiAnalyzer.getLength();

            o =  p.getEndOffset();
            SegmentCache.releaseSharedSegment(seg);
        }

        // REMIND(bcb) remove this code when debugging is done.
        if( levelsEnd != levels.length )
            throw new Error("levelsEnd assertion failed.");

        return levels;
    }

    /**
     * Gives a diagnostic dump.
     *
     * @param out the output stream
     */
    public void dump(PrintStream out) {
        Element root = getDefaultRootElement();
        if (root instanceof AbstractElement) {
            ((AbstractElement)root).dump(out, 0);
        }
        bidiRoot.dump(out,0);
    }

    /**
     * Gets the content for the document.
     *
     * @return the content
     */
    protected final Content getContent() {
        return data;
    }

    /**
     * Creates a document leaf element.
     * Hook through which elements are created to represent the
     * document structure.  Because this implementation keeps
     * structure and content separate, elements grow automatically
     * when content is extended so splits of existing elements
     * follow.  The document itself gets to decide how to generate
     * elements to give flexibility in the type of elements used.
     *
     * @param parent the parent element
     * @param a the attributes for the element
     * @param p0 the beginning of the range >= 0
     * @param p1 the end of the range >= p0
     * @return the new element
     */
    protected Element createLeafElement(Element parent, AttributeSet a, int p0, int p1) {
        return new LeafElement(parent, a, p0, p1);
    }

    /**
     * Creates a document branch element, that can contain other elements.
     *
     * @param parent the parent element
     * @param a the attributes
     * @return the element
     */
    protected Element createBranchElement(Element parent, AttributeSet a) {
        return new BranchElement(parent, a);
    }

    // --- Document locking ----------------------------------

    /**
     * Fetches the current writing thread if there is one.
     * This can be used to distinguish whether a method is
     * being called as part of an existing modification or
     * if a lock needs to be acquired and a new transaction
     * started.
     *
     * @return the thread actively modifying the document
     *  or <code>null</code> if there are no modifications in progress
     */
    protected synchronized final Thread getCurrentWriter() {
        return currWriter;
    }

    /**
     * Acquires a lock to begin mutating the document this lock
     * protects.  There can be no writing, notification of changes, or
     * reading going on in order to gain the lock.  Additionally a thread is
     * allowed to gain more than one <code>writeLock</code>,
     * as long as it doesn't attempt to gain additional <code>writeLock</code>s
     * from within document notification.  Attempting to gain a
     * <code>writeLock</code> from within a DocumentListener notification will
     * result in an <code>IllegalStateException</code>.  The ability
     * to obtain more than one <code>writeLock</code> per thread allows
     * subclasses to gain a writeLock, perform a number of operations, then
     * release the lock.
     * <p>
     * Calls to <code>writeLock</code>
     * must be balanced with calls to <code>writeUnlock</code>, else the
     * <code>Document</code> will be left in a locked state so that no
     * reading or writing can be done.
     *
     * @exception IllegalStateException thrown on illegal lock
     *  attempt.  If the document is implemented properly, this can
     *  only happen if a document listener attempts to mutate the
     *  document.  This situation violates the bean event model
     *  where order of delivery is not guaranteed and all listeners
     *  should be notified before further mutations are allowed.
     */
    protected synchronized final void writeLock() {
        try {
            while ((numReaders > 0) || (currWriter != null)) {
                if (Thread.currentThread() == currWriter) {
                    if (notifyingListeners) {
                        // Assuming one doesn't do something wrong in a
                        // subclass this should only happen if a
                        // DocumentListener tries to mutate the document.
                        throw new IllegalStateException(
                                      "Attempt to mutate in notification");
                    }
                    numWriters++;
                    return;
                }
                wait();
            }
            currWriter = Thread.currentThread();
            numWriters = 1;
        } catch (InterruptedException e) {
            throw new Error("Interrupted attempt to aquire write lock");
        }
    }

    /**
     * Releases a write lock previously obtained via <code>writeLock</code>.
     * After decrementing the lock count if there are no oustanding locks
     * this will allow a new writer, or readers.
     *
     * @see #writeLock
     */
    protected synchronized final void writeUnlock() {
        if (--numWriters <= 0) {
            numWriters = 0;
            currWriter = null;
            notifyAll();
        }
    }

    /**
     * Acquires a lock to begin reading some state from the
     * document.  There can be multiple readers at the same time.
     * Writing blocks the readers until notification of the change
     * to the listeners has been completed.  This method should
     * be used very carefully to avoid unintended compromise
     * of the document.  It should always be balanced with a
     * <code>readUnlock</code>.
     *
     * @see #readUnlock
     */
    public synchronized final void readLock() {
        try {
            while (currWriter != null) {
                if (currWriter == Thread.currentThread()) {
                    // writer has full read access.... may try to acquire
                    // lock in notification
                    return;
                }
                wait();
            }
            numReaders += 1;
        } catch (InterruptedException e) {
            throw new Error("Interrupted attempt to aquire read lock");
        }
    }

    /**
     * Does a read unlock.  This signals that one
     * of the readers is done.  If there are no more readers
     * then writing can begin again.  This should be balanced
     * with a readLock, and should occur in a finally statement
     * so that the balance is guaranteed.  The following is an
     * example.
     * <pre><code>
     * &nbsp;   readLock();
     * &nbsp;   try {
     * &nbsp;       // do something
     * &nbsp;   } finally {
     * &nbsp;       readUnlock();
     * &nbsp;   }
     * </code></pre>
     *
     * @see #readLock
     */
    public synchronized final void readUnlock() {
        if (currWriter == Thread.currentThread()) {
            // writer has full read access.... may try to acquire
            // lock in notification
            return;
        }
        if (numReaders <= 0) {
            throw new StateInvariantError(BAD_LOCK_STATE);
        }
        numReaders -= 1;
        notify();
    }

    // --- serialization ---------------------------------------------

    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException
    {
        s.defaultReadObject();
        listenerList = new EventListenerList();

        // Restore bidi structure
        //REMIND(bcb) This creates an initial bidi element to account for
        //the \n that exists by default in the content.
        bidiRoot = new BidiRootElement();
        try {
            writeLock();
            Element[] p = new Element[1];
            p[0] = new BidiElement( bidiRoot, 0, 1, 0 );
            bidiRoot.replace(0,0,p);
        } finally {
            writeUnlock();
        }
        // At this point bidi root is only partially correct. To fully
        // restore it we need access to getDefaultRootElement. But, this
        // is created by the subclass and at this point will be null. We
        // thus use registerValidation.
        s.registerValidation(new ObjectInputValidation() {
            public void validateObject() {
                try {
                    writeLock();
                    DefaultDocumentEvent e = new DefaultDocumentEvent
                                   (0, getLength(),
                                    DocumentEvent.EventType.INSERT);
                    updateBidi( e );
                }
                finally {
                    writeUnlock();
                }
            }
        }, 0);
    }

    // ----- member variables ------------------------------------------

    private transient int numReaders;
    private transient Thread currWriter;
    /**
     * The number of writers, all obtained from <code>currWriter</code>.
     */
    private transient int numWriters;
    /**
     * True will notifying listeners.
     */
    private transient boolean notifyingListeners;

    private static Boolean defaultI18NProperty;

    /**
     * Storage for document-wide properties.
     */
    private Dictionary<Object,Object> documentProperties = null;

    /**
     * The event listener list for the document.
     */
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Where the text is actually stored, and a set of marks
     * that track change as the document is edited are managed.
     */
    private Content data;

    /**
     * Factory for the attributes.  This is the strategy for
     * attribute compression and control of the lifetime of
     * a set of attributes as a collection.  This may be shared
     * with other documents.
     */
    private AttributeContext context;

    /**
     * The root of the bidirectional structure for this document.  Its children
     * represent character runs with the same Unicode bidi level.
     */
    private transient BranchElement bidiRoot;

    /**
     * Filter for inserting/removing of text.
     */
    private DocumentFilter documentFilter;

    /**
     * Used by DocumentFilter to do actual insert/remove.
     */
    private transient DocumentFilter.FilterBypass filterBypass;

    private static final String BAD_LOCK_STATE = "document lock failure";

    /**
     * Error message to indicate a bad location.
     */
    protected static final String BAD_LOCATION = "document location failure";

    /**
     * Name of elements used to represent paragraphs
     */
    public static final String ParagraphElementName = "paragraph";

    /**
     * Name of elements used to represent content
     */
    public static final String ContentElementName = "content";

    /**
     * Name of elements used to hold sections (lines/paragraphs).
     */
    public static final String SectionElementName = "section";

    /**
     * Name of elements used to hold a unidirectional run
     */
    public static final String BidiElementName = "bidi level";

    /**
     * Name of the attribute used to specify element
     * names.
     */
    public static final String ElementNameAttribute = "$ename";

    /**
     * Document property that indicates whether internationalization
     * functions such as text reordering or reshaping should be
     * performed. This property should not be publicly exposed,
     * since it is used for implementation convenience only.  As a
     * side effect, copies of this property may be in its subclasses
     * that live in different packages (e.g. HTMLDocument as of now),
     * so those copies should also be taken care of when this property
     * needs to be modified.
     */
    static final String I18NProperty = "i18n";

    /**
     * Document property that indicates if a character has been inserted
     * into the document that is more than one byte long.  GlyphView uses
     * this to determine if it should use BreakIterator.
     */
    static final Object MultiByteProperty = "multiByte";

    /**
     * Document property that indicates asynchronous loading is
     * desired, with the thread priority given as the value.
     */
    static final String AsyncLoadPriority = "load priority";

    /**
     * Interface to describe a sequence of character content that
     * can be edited.  Implementations may or may not support a
     * history mechanism which will be reflected by whether or not
     * mutations return an UndoableEdit implementation.
     * @see AbstractDocument
     */
    public interface Content {

        /**
         * Creates a position within the content that will
         * track change as the content is mutated.
         *
         * @param offset the offset in the content >= 0
         * @return a Position
         * @exception BadLocationException for an invalid offset
         */
        public Position createPosition(int offset) throws BadLocationException;

        /**
         * Current length of the sequence of character content.
         *
         * @return the length >= 0
         */
        public int length();

        /**
         * Inserts a string of characters into the sequence.
         *
         * @param where   offset into the sequence to make the insertion >= 0
         * @param str     string to insert
         * @return  if the implementation supports a history mechanism,
         *    a reference to an <code>Edit</code> implementation will be returned,
         *    otherwise returns <code>null</code>
         * @exception BadLocationException  thrown if the area covered by
         *   the arguments is not contained in the character sequence
         */
        public UndoableEdit insertString(int where, String str) throws BadLocationException;

        /**
         * Removes some portion of the sequence.
         *
         * @param where   The offset into the sequence to make the
         *   insertion >= 0.
         * @param nitems  The number of items in the sequence to remove >= 0.
         * @return  If the implementation supports a history mechansim,
         *    a reference to an Edit implementation will be returned,
         *    otherwise null.
         * @exception BadLocationException  Thrown if the area covered by
         *   the arguments is not contained in the character sequence.
         */
        public UndoableEdit remove(int where, int nitems) throws BadLocationException;

        /**
         * Fetches a string of characters contained in the sequence.
         *
         * @param where   Offset into the sequence to fetch >= 0.
         * @param len     number of characters to copy >= 0.
         * @return the string
         * @exception BadLocationException  Thrown if the area covered by
         *   the arguments is not contained in the character sequence.
         */
        public String getString(int where, int len) throws BadLocationException;

        /**
         * Gets a sequence of characters and copies them into a Segment.
         *
         * @param where the starting offset >= 0
         * @param len the number of characters >= 0
         * @param txt the target location to copy into
         * @exception BadLocationException  Thrown if the area covered by
         *   the arguments is not contained in the character sequence.
         */
        public void getChars(int where, int len, Segment txt) throws BadLocationException;
    }

    /**
     * An interface that can be used to allow MutableAttributeSet
     * implementations to use pluggable attribute compression
     * techniques.  Each mutation of the attribute set can be
     * used to exchange a previous AttributeSet instance with
     * another, preserving the possibility of the AttributeSet
     * remaining immutable.  An implementation is provided by
     * the StyleContext class.
     *
     * The Element implementations provided by this class use
     * this interface to provide their MutableAttributeSet
     * implementations, so that different AttributeSet compression
     * techniques can be employed.  The method
     * <code>getAttributeContext</code> should be implemented to
     * return the object responsible for implementing the desired
     * compression technique.
     *
     * @see StyleContext
     */
    public interface AttributeContext {

        /**
         * Adds an attribute to the given set, and returns
         * the new representative set.
         *
         * @param old the old attribute set
         * @param name the non-null attribute name
         * @param value the attribute value
         * @return the updated attribute set
         * @see MutableAttributeSet#addAttribute
         */
        public AttributeSet addAttribute(AttributeSet old, Object name, Object value);

        /**
         * Adds a set of attributes to the element.
         *
         * @param old the old attribute set
         * @param attr the attributes to add
         * @return the updated attribute set
         * @see MutableAttributeSet#addAttribute
         */
        public AttributeSet addAttributes(AttributeSet old, AttributeSet attr);

        /**
         * Removes an attribute from the set.
         *
         * @param old the old attribute set
         * @param name the non-null attribute name
         * @return the updated attribute set
         * @see MutableAttributeSet#removeAttribute
         */
        public AttributeSet removeAttribute(AttributeSet old, Object name);

        /**
         * Removes a set of attributes for the element.
         *
         * @param old the old attribute set
         * @param names the attribute names
         * @return the updated attribute set
         * @see MutableAttributeSet#removeAttributes
         */
        public AttributeSet removeAttributes(AttributeSet old, Enumeration<?> 
