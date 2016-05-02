/*
 * Copyright 1998-2004 Sun Microsystems, Inc.  All Rights Reserved.
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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */

package com.sun.corba.se.impl.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.ObjectInputValidation;
import java.io.NotActiveException;
import java.io.InvalidObjectException;
import java.io.InvalidClassException;
import java.io.DataInputStream;
import java.io.OptionalDataException;
import java.io.WriteAbortedException;
import java.io.Externalizable;
import java.io.EOFException;
import java.lang.reflect.*;
import java.util.Vector;
import java.util.Stack;
import java.util.Hashtable;
import java.util.Enumeration;

import sun.corba.Bridge ;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import com.sun.corba.se.impl.io.ObjectStreamClass;
import com.sun.corba.se.impl.util.Utility;

import org.omg.CORBA.portable.ValueInputStream;

import org.omg.CORBA.ValueMember;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.ORB;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.portable.IndirectionException;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TypeCode;

import com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription;
import com.sun.org.omg.SendingContext.CodeBase;

import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.ValueHandler;

import java.security.*;
import java.util.*;

import com.sun.corba.se.impl.orbutil.ObjectUtility ;
import com.sun.corba.se.impl.logging.OMGSystemException ;
import com.sun.corba.se.impl.logging.UtilSystemException ;

import com.sun.corba.se.spi.logging.CORBALogDomains ;

/**
 * IIOPInputStream is used by the ValueHandlerImpl to handle Java serialization
 * input semantics.
 *
 * @author  Stephen Lewallen
 * @since   JDK1.1.6
 */

public class IIOPInputStream
    extends com.sun.corba.se.impl.io.InputStreamHook
{
    private static Bridge bridge =
        (Bridge)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return Bridge.get() ;
                }
            }
        ) ;

    private static OMGSystemException omgWrapper = OMGSystemException.get(
        CORBALogDomains.RPC_ENCODING ) ;
    private static UtilSystemException utilWrapper = UtilSystemException.get(
        CORBALogDomains.RPC_ENCODING ) ;

    // Necessary to pass the appropriate fields into the
    // defaultReadObjectDelegate method (which takes no
    // parameters since it's called from
    // java.io.ObjectInpuStream defaultReadObject()
    // which we can't change).
    //
    // This is only used in the case where the fields had
    // to be obtained remotely because of a serializable
    // version difference.  Set in inputObjectUsingFVD.
    // Part of serialization evolution fixes for Ladybird,
    // bug 4365188.
    private ValueMember defaultReadObjectFVDMembers[] = null;

    private org.omg.CORBA_2_3.portable.InputStream orbStream;

    private CodeBase cbSender;

    private ValueHandlerImpl vhandler;  //d4365188

    private Object currentObject = null;

    private ObjectStreamClass currentClassDesc = null;

    private Class currentClass = null;

    private int recursionDepth = 0;

    private int simpleReadDepth = 0;

    // The ActiveRecursionManager replaces the old RecursionManager which
    // used to record how many recursions were made, and resolve them after
    // an object was completely deserialized.
    //
    // That created problems (as in bug 4414154) because when custom
    // unmarshaling in readObject, there can be recursive references
    // to one of the objects currently being unmarshaled, and the
    // passive recursion system failed.
    ActiveRecursionManager activeRecursionMgr = new ActiveRecursionManager();

    private IOException abortIOException = null;

    /* Remember the first exception that stopped this stream. */
    private ClassNotFoundException abortClassNotFoundException = null;

    /* Vector of validation callback objects
     * The vector is created as needed. The vector is maintained in
     * order of highest (first) priority to lowest
     */
    private Vector callbacks;

    // Serialization machinery fields
    /* Arrays used to keep track of classes and ObjectStreamClasses
     * as they are being merged; used in inputObject.
     * spClass is the stack pointer for both.  */
    ObjectStreamClass[] classdesc;
    Class[] classes;
    int spClass;

    private static final String kEmptyStr = "";

    // TCKind TypeCodes used in FVD inputClassFields
    //public static final TypeCode kRemoteTypeCode = new TypeCodeImpl(TCKind._tk_objref);
    //public static final TypeCode kValueTypeCode =  new TypeCodeImpl(TCKind._tk_value);
    // removed TypeCodeImpl dependency
    public static final TypeCode kRemoteTypeCode = ORB.init().get_primitive_tc(TCKind.tk_objref);
    public static final TypeCode kValueTypeCode =  ORB.init().get_primitive_tc(TCKind.tk_value);

    // TESTING CODE - useFVDOnly should be made final before FCS in order to
    // optimize out the check.
    private static final boolean useFVDOnly = false;

    private byte streamFormatVersion;

    // Since java.io.OptionalDataException's constructors are
    // package private, but we need to throw it in some special
    // cases, we try to do it by reflection.
    private static final Constructor OPT_DATA_EXCEPTION_CTOR;

    private Object[] readObjectArgList = { this } ;

    static {
        OPT_DATA_EXCEPTION_CTOR = getOptDataExceptionCtor();
    }

    // Grab the OptionalDataException boolean ctor and make
    // it accessible.  Note that any exceptions
    // will be wrapped in ExceptionInInitializerErrors.
    private static Constructor getOptDataExceptionCtor() {

        try {

            Constructor result =

                (Constructor) AccessController.doPrivileged(
                                    new PrivilegedExceptionAction() {
                    public java.lang.Object run()
                        throws NoSuchMethodException,
                        SecurityException {

                        Constructor boolCtor
                            = OptionalDataException.class.getDeclaredConstructor(
                                                               new Class[] {
                                Boolean.TYPE });

                        boolCtor.setAccessible(true);

                        return boolCtor;
                    }});

            if (result == null)
                // XXX I18N, logging needed.
                throw new Error("Unable to find OptionalDataException constructor");

            return result;

        } catch (Exception ex) {
            // XXX I18N, logging needed.
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Create a new OptionalDataException with the EOF marker
    // set to true.  See handleOptionalDataMarshalException.
    private OptionalDataException createOptionalDataException() {
        try {
            OptionalDataException result
                = (OptionalDataException)
                   OPT_DATA_EXCEPTION_CTOR.newInstance(new Object[] {
                       Boolean.TRUE });

            if (result == null)
                // XXX I18N, logging needed.
                throw new Error("Created null OptionalDataException");

            return result;

        } catch (Exception ex) {
            // XXX I18N, logging needed.
            throw new Error("Couldn't create OptionalDataException", ex);
        }
    }

    // Return the stream format version currently being used
    // to deserialize an object
    protected byte getStreamFormatVersion() {
        return streamFormatVersion;
    }

    // At the beginning of data sent by a writeObject or
    // writeExternal method there is a byte telling the
    // reader the stream format version.
    private void readFormatVersion() throws IOException {

        streamFormatVersion = orbStream.read_octet();

        if (streamFormatVersion < 1 ||
            streamFormatVersion > vhandler.getMaximumStreamFormatVersion()) {
            SystemException sysex = omgWrapper.unsupportedFormatVersion(
                    CompletionStatus.COMPLETED_MAYBE);
            // XXX I18N?  Logging for IOException?
            IOException result = new IOException("Unsupported format version: "
                                                 + streamFormatVersion);
            result.initCause( sysex ) ;
            throw result ;
        }

        if (streamFormatVersion == 2) {
            if (!(orbStream instanceof ValueInputStream)) {
                SystemException sysex = omgWrapper.notAValueinputstream(
                    CompletionStatus.COMPLETED_MAYBE);
                // XXX I18N?  Logging for IOException?
                IOException result = new IOException("Not a ValueInputStream");
                result.initCause( sysex ) ;
                throw result;
            }
        }
    }

    public static void setTestFVDFlag(boolean val){
        //  useFVDOnly = val;
    }

    /**
     * Dummy constructor; passes upper stream a dummy stream;
     **/
    public IIOPInputStream()
        throws java.io.IOException {
        super();
        resetStream();
    }

    public final void setOrbStream(org.omg.CORBA_2_3.portable.InputStream os) {
        orbStream = os;
    }

    public final org.omg.CORBA_2_3.portable.InputStream getOrbStream() {
        return orbStream;
    }

    //added setSender and getSender
    public final void setSender(CodeBase cb) {
        cbSender = cb;
    }

    public final CodeBase getSender() {
        return cbSender;
    }

    // 4365188 this is added to enable backward compatability w/ wrong
    // rep-ids
    public final void setValueHandler(ValueHandler vh) {
        vhandler = (com.sun.corba.se.impl.io.ValueHandlerImpl) vh;
    }

    public final ValueHandler getValueHandler() {
        return (javax.rmi.CORBA.ValueHandler) vhandler;
    }

    public final void increaseRecursionDepth(){
        recursionDepth++;
    }

    public final int decreaseRecursionDepth(){
        return --recursionDepth;
    }

    /**
     * Override the actions of the final method "readObject()"
     * in ObjectInputStream.
     * @since     JDK1.1.6
     *
     * Read an object from the ObjectInputStream.
     * The class of the object, the signature of the class, and the values
     * of the non-transient and non-static fields of the class and all
     * of its supertypes are read.  Default deserializing for a class can be
     * overriden using the writeObject and readObject methods.
     * Objects referenced by this object are read transitively so
     * that a complete equivalent graph of objects is reconstructed by readObject. <p>
     *
     * The root object is completly restored when all of its fields
     * and the objects it references are completely restored.  At this
     * point the object validation callbacks are executed in order
     * based on their registered priorities. The callbacks are
     * registered by objects (in the readObject special methods)
     * as they are individually restored.
     *
     * Exceptions are thrown for problems with the InputStream and for classes
     * that should not be deserialized.  All exceptions are fatal to the
     * InputStream and leave it in an indeterminate state; it is up to the caller
     * to ignore or recover the stream state.
     * @exception java.lang.ClassNotFoundException Class of a serialized object
     *      cannot be found.
     * @exception InvalidClassException Something is wrong with a class used by
     *     serialization.
     * @exception StreamCorruptedException Control information in the
     *     stream is inconsistent.
     * @exception OptionalDataException Primitive data was found in the
     * stream instead of objects.
     * @exception IOException Any of the usual Input/Output related exceptions.
     * @since     JDK1.1
     */
    public final Object readObjectDelegate() throws IOException
    {
        try {

            readObjectState.readData(this);

            return orbStream.read_abstract_interface();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, true);
            throw marshalException;
        } catch(IndirectionException cdrie)
            {
                // The CDR stream had never seen the given offset before,
                // so check the recursion manager (it will throw an
                // IOException if it doesn't have a reference, either).
                return activeRecursionMgr.getObject(cdrie.offset);
            }
    }

    final Object simpleReadObject(Class clz,
                                  String repositoryID,
                                  com.sun.org.omg.SendingContext.CodeBase sender,
                                  int offset)
                                         /* throws OptionalDataException, ClassNotFoundException, IOException */
    {

        /* Save the current state and get ready to read an object. */
        Object prevObject = currentObject;
        ObjectStreamClass prevClassDesc = currentClassDesc;
        Class prevClass = currentClass;
        byte oldStreamFormatVersion = streamFormatVersion;

        simpleReadDepth++;      // Entering
        Object obj = null;

        /*
         * Check for reset, handle it before reading an object.
         */
        try {
            // d4365188: backward compatability
            if (vhandler.useFullValueDescription(clz, repositoryID)) {
                obj = inputObjectUsingFVD(clz, repositoryID, sender, offset);
            } else {
                obj = inputObject(clz, repositoryID, sender, offset);
            }

            obj = currentClassDesc.readResolve(obj);
        }
        catch(ClassNotFoundException cnfe)
            {
                bridge.throwException( cnfe ) ;
                return null;
            }
        catch(IOException ioe)
            {
                // System.out.println("CLZ = " + clz + "; " + ioe.toString());
                bridge.throwException(ioe) ;
                return null;
            }
        finally {
            simpleReadDepth --;
            currentObject = prevObject;
            currentClassDesc = prevClassDesc;
            currentClass = prevClass;
            streamFormatVersion = oldStreamFormatVersion;
        }


        /* Check for thrown exceptions and re-throw them, clearing them if
         * this is the last recursive call .
         */
        IOException exIOE = abortIOException;
        if (simpleReadDepth == 0)
            abortIOException = null;
        if (exIOE != null){
            bridge.throwException( exIOE ) ;
            return null;
        }


        ClassNotFoundException exCNF = abortClassNotFoundException;
        if (simpleReadDepth == 0)
            abortClassNotFoundException = null;
        if (exCNF != null) {
            bridge.throwException( exCNF ) ;
            return null;
        }

        return obj;
    }

    public final void simpleSkipObject(String repositoryID,
                                       com.sun.org.omg.SendingContext.CodeBase sender)
                                       /* throws OptionalDataException, ClassNotFoundException, IOException */
    {

        /* Save the current state and get ready to read an object. */
        Object prevObject = currentObject;
        ObjectStreamClass prevClassDesc = currentClassDesc;
        Class prevClass = currentClass;
        byte oldStreamFormatVersion = streamFormatVersion;

        simpleReadDepth++;      // Entering
        Object obj = null;

        /*
         * Check for reset, handle it before reading an object.
         */
        try {
            skipObjectUsingFVD(repositoryID, sender);
        }
        catch(ClassNotFoundException cnfe)
            {
                bridge.throwException( cnfe ) ;
                return;
            }
        catch(IOException ioe)
            {
                bridge.throwException( ioe ) ;
                return;
            }
        finally {
            simpleReadDepth --;
            streamFormatVersion = oldStreamFormatVersion;
            currentObject = prevObject;
            currentClassDesc = prevClassDesc;
            currentClass = prevClass;
        }


        /* Check for thrown exceptions and re-throw them, clearing them if
         * this is the last recursive call .
         */
        IOException exIOE = abortIOException;
        if (simpleReadDepth == 0)
            abortIOException = null;
        if (exIOE != null){
            bridge.throwException( exIOE ) ;
            return;
        }


        ClassNotFoundException exCNF = abortClassNotFoundException;
        if (simpleReadDepth == 0)
            abortClassNotFoundException = null;
        if (exCNF != null) {
            bridge.throwException( exCNF ) ;
            return;
        }

        return;
    }
    /////////////////

    /**
     * This method is called by trusted subclasses of ObjectOutputStream
     * that constructed ObjectOutputStream using the
     * protected no-arg constructor. The subclass is expected to provide
     * an override method with the modifier "final".
     *
     * @return the Object read from the stream.
     *
     * @see #ObjectInputStream()
     * @see #readObject
     * @since JDK 1.2
     */
    protected final Object readObjectOverride()
        throws OptionalDataException, ClassNotFoundException, IOException
    {
        return readObjectDelegate();
    }

    /**
     * Override the actions of the final method "defaultReadObject()"
     * in ObjectInputStream.
     * @since     JDK1.1.6
     *
     * Read the non-static and non-transient fields of the current class
     * from this stream.  This may only be called from the readObject method
     * of the class being deserialized. It will throw the NotActiveException
     * if it is called otherwise.
     *
     * @exception java.lang.ClassNotFoundException if the class of a serialized
     *              object could not be found.
     * @exception IOException        if an I/O error occurs.
     * @exception NotActiveException if the stream is not currently reading
     *              objects.
     * @since     JDK1.1
     */
    public final void defaultReadObjectDelegate()
    /* throws IOException, ClassNotFoundException, NotActiveException */
    {
        try {
            if (currentObject == null || currentClassDesc == null)
                // XXX I18N, logging needed.
                throw new NotActiveException("defaultReadObjectDelegate");

            // The array will be null unless fields were retrieved
            // remotely because of a serializable version difference.
            // Bug fix for 4365188.  See the definition of
            // defaultReadObjectFVDMembers for more information.
            if (defaultReadObjectFVDMembers != null &&
                defaultReadObjectFVDMembers.length > 0) {

                // WARNING:  Be very careful!  What if some of
                // these fields actually have to do this, too?
                // This works because the defaultReadObjectFVDMembers
                // reference is passed to inputClassFields, but
                // there is no guarantee that
                // defaultReadObjectFVDMembers will point to the
                // same array after calling inputClassFields.

                // Use the remote fields to unmarshal.
                inputClassFields(currentObject,
                                 currentClass,
                                 currentClassDesc,
                                 defaultReadObjectFVDMembers,
                                 cbSender);

            } else {

                // Use the local fields to unmarshal.
                ObjectStreamField[] fields =
                    currentClassDesc.getFieldsNoCopy();
                if (fields.length > 0) {
                    inputClassFields(currentObject, currentClass, fields, cbSender);
                }
            }
        }
        catch(NotActiveException nae)
            {
                bridge.throwException( nae ) ;
            }
        catch(IOException ioe)
            {
                bridge.throwException( ioe ) ;
            }
        catch(ClassNotFoundException cnfe)
            {
                bridge.throwException( cnfe ) ;
            }

    }

    /**
     * Override the actions of the final method "enableResolveObject()"
     * in ObjectInputStream.
     * @since     JDK1.1.6
     *
     * Enable the stream to allow objects read from the stream to be replaced.
     * If the stream is a trusted class it is allowed to enable replacment.
     * Trusted classes are those classes with a classLoader equals null. <p>
     *
     * When enabled the resolveObject method is called for every object
     * being deserialized.
     *
     * @exception SecurityException The classloader of this stream object is non-null.
     * @since     JDK1.1
     */
    public final boolean enableResolveObjectDelegate(boolean enable)
    /* throws SecurityException */
    {
        return false;
    }

    // The following three methods allow the implementing orbStream
    // to provide mark/reset behavior as defined in java.io.InputStream.

    public final void mark(int readAheadLimit) {
        orbStream.mark(readAheadLimit);
    }

    public final boolean markSupported() {
        return orbStream.markSupported();
    }

    public final void reset() throws IOException {
        try {
            orbStream.reset();
        } catch (Error e) {
            IOException err = new IOException(e.getMessage());
            err.initCause(e) ;
            throw err ;
        }
    }

    public final int available() throws IOException{
        return 0; // unreliable
    }

    public final void close() throws IOException{
        // no op
    }

    public final int read() throws IOException{
        try{
            readObjectState.readData(this);

            return (orbStream.read_octet() << 0) & 0x000000FF;
        } catch (MARSHAL marshalException) {
            if (marshalException.minor
                == OMGSystemException.RMIIIOP_OPTIONAL_DATA_INCOMPATIBLE1) {
                setState(IN_READ_OBJECT_NO_MORE_OPT_DATA);
                return -1;
            }

            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e) ;
            throw exc ;
        }
    }

    public final int read(byte data[], int offset, int length) throws IOException{
        try{
            readObjectState.readData(this);

            orbStream.read_octet_array(data, offset, length);
            return length;
        } catch (MARSHAL marshalException) {
            if (marshalException.minor
                == OMGSystemException.RMIIIOP_OPTIONAL_DATA_INCOMPATIBLE1) {
                setState(IN_READ_OBJECT_NO_MORE_OPT_DATA);
                return -1;
            }

            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e) ;
            throw exc ;
        }

    }

    public final boolean readBoolean() throws IOException{
        try{
            readObjectState.readData(this);

            return orbStream.read_boolean();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;

        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final byte readByte() throws IOException{
        try{
            readObjectState.readData(this);

            return orbStream.read_octet();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;

        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final char readChar() throws IOException{
        try{
            readObjectState.readData(this);

            return orbStream.read_wchar();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;

        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final double readDouble() throws IOException{
        try{
            readObjectState.readData(this);

            return orbStream.read_double();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final float readFloat() throws IOException{
        try{
            readObjectState.readData(this);

            return orbStream.read_float();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final void readFully(byte data[]) throws IOException{
// d11623 : implement readFully, required for serializing some core classes

        readFully(data, 0, data.length);
    }

    public final void readFully(byte data[],  int offset,  int size) throws IOException{
// d11623 : implement readFully, required for serializing some core classes
        try{
            readObjectState.readData(this);

            orbStream.read_octet_array(data, offset, size);
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);

            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final int readInt() throws IOException{
        try{
            readObjectState.readData(this);

            return orbStream.read_long();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final String readLine() throws IOException{
        // XXX I18N, logging needed.
        throw new IOException("Method readLine not supported");
    }

    public final long readLong() throws IOException{
        try{
            readObjectState.readData(this);

            return orbStream.read_longlong();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final short readShort() throws IOException{
        try{
            readObjectState.readData(this);

            return orbStream.read_short();
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    protected final void readStreamHeader() throws IOException, StreamCorruptedException{
        // no op
    }

    public final int readUnsignedByte() throws IOException{
        try{
            readObjectState.readData(this);

            return (orbStream.read_octet() << 0) & 0x000000FF;
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    public final int readUnsignedShort() throws IOException{
        try{
            readObjectState.readData(this);

            return (orbStream.read_ushort() << 0) & 0x0000FFFF;
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    /**
     * Helper method for correcting the Kestrel bug 4367783 (dealing
     * with larger than 8-bit chars).  The old behavior is preserved
     * in orbutil.IIOPInputStream_1_3 in order to interoperate with
     * our legacy ORBs.
     */
    protected String internalReadUTF(org.omg.CORBA.portable.InputStream stream)
    {
        return stream.read_wstring();
    }

    public final String readUTF() throws IOException{
        try{
            readObjectState.readData(this);

            return internalReadUTF(orbStream);
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);
            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e);
            throw exc ;
        }
    }

    // If the ORB stream detects an incompatibility between what's
    // on the wire and what our Serializable's readObject wants,
    // it throws a MARSHAL exception with a specific minor code.
    // This is rethrown to the readObject as an OptionalDataException.
    // So far in RMI-IIOP, this process isn't specific enough to
    // tell the readObject how much data is available, so we always
    // set the OptionalDataException's EOF marker to true.
    private void handleOptionalDataMarshalException(MARSHAL marshalException,
                                                    boolean objectRead)
        throws IOException {

        // Java Object Serialization spec 3.4: "If the readObject method
        // of the class attempts to read more data than is present in the
        // optional part of the stream for this class, the stream will
        // return -1 for bytewise reads, throw an EOFException for
        // primitive data reads, or throw an OptionalDataException
        // with the eof field set to true for object reads."
        if (marshalException.minor
            == OMGSystemException.RMIIIOP_OPTIONAL_DATA_INCOMPATIBLE1) {

            IOException result;

            if (!objectRead)
                result = new EOFException("No more optional data");
            else
                result = createOptionalDataException();

            result.initCause(marshalException);

            setState(IN_READ_OBJECT_NO_MORE_OPT_DATA);

            throw result;
        }
    }

    public final synchronized void registerValidation(ObjectInputValidation obj,
                                                      int prio)
        throws NotActiveException, InvalidObjectException{
        // XXX I18N, logging needed.
        throw new Error("Method registerValidation not supported");
    }

    protected final Class resolveClass(ObjectStreamClass v)
        throws IOException, ClassNotFoundException{
        // XXX I18N, logging needed.
        throw new IOException("Method resolveClass not supported");
    }

    protected final Object resolveObject(Object obj) throws IOException{
        // XXX I18N, logging needed.
        throw new IOException("Method resolveObject not supported");
    }

    public final int skipBytes(int len) throws IOException{
        try{
            readObjectState.readData(this);

            byte buf[] = new byte[len];
            orbStream.read_octet_array(buf, 0, len);
            return len;
        } catch (MARSHAL marshalException) {
            handleOptionalDataMarshalException(marshalException, false);

            throw marshalException;
        } catch(Error e) {
            IOException exc = new IOException(e.getMessage());
            exc.initCause(e) ;
            throw exc ;
        }
    }

    private Object inputObject(Class clz,
                               String repositoryID,
                               com.sun.org.omg.SendingContext.CodeBase sender,
                               int offset)
        throws IOException, ClassNotFoundException
    {

        /*
         * Get the descriptor and then class of the incoming object.
         */

        currentClassDesc = ObjectStreamClass.lookup(clz);
        currentClass = currentClassDesc.forClass();
        //currentClassDesc.setClass(currentClass);
        if (currentClass == null)
            // XXX I18N, logging needed.
            throw new ClassNotFoundException(currentClassDesc.getName());

        try {
            /* If Externalizable,
             *  Create an instance and tell it to read its data.
             * else,
             *  Handle it as a serializable class.
             */
            if (currentClassDesc.isExternalizable()) {
                try {
                    currentObject = (currentClass == null) ?
                        null : currentClassDesc.newInstance();
                    if (currentObject != null) {

                        // Store this object and its beginning position
                        // since there might be indirections to it while
                        // it's been unmarshalled.
                        activeRecursionMgr.addObject(offset, currentObject);

                        // Read format version
                        readFormatVersion();

                        Externalizable ext = (Externalizable)currentObject;
                        ext.readExternal(this);
                }
            } catch (InvocationTargetException e) {
                InvalidClassException exc = new InvalidClassException(
                    currentClass.getName(),
                    "InvocationTargetException accessing no-arg constructor");
                exc.initCause( e ) ;
                throw exc ;
            } catch (UnsupportedOperationException e) {
                InvalidClassException exc = new InvalidClassException(
                    currentClass.getName(),
                    "UnsupportedOperationException accessing no-arg constructor");
                exc.initCause( e ) ;
                throw exc ;
            } catch (InstantiationException e) {
                InvalidClassException exc = new InvalidClassException(
                    currentClass.getName(),
                    "InstantiationException accessing no-arg constructor");
                exc.initCause( e ) ;
                throw exc ;
            }
        } // end : if (currentClassDesc.isExternalizable())
        else {
            /* Count number of classes and descriptors we might have
             * to work on.
             */

            ObjectStreamClass currdesc = currentClassDesc;
            Class currclass = currentClass;

            int spBase = spClass;       // current top of stack

            /* The object's classes should be processed from supertype to subtype
             * Push all the clases of the current object onto a stack.
             * Note that only the serializable classes are represented
             * in the descriptor list.
             *
             * Handle versioning where one or more supertypes of
             * have been inserted or removed.  The stack will
             * contain pairs of descriptors and the corresponding
             * class.  If the object has a class that did not occur in
             * the original the descriptor will be null.  If the
             * original object had a descriptor for a class not
             * present in the local hierarchy of the object the class will be
             * null.
             *
             */

            /*
             * This is your basic diff pattern, made simpler
             * because reordering is not allowed.
             */
            // sun.4296963 ibm.11861
            // d11861 we should stop when we find the highest serializable class
            // We need this so that when we allocate the new object below, we
            // can call the constructor of the non-serializable superclass.
            // Note that in the JRMP variant of this code the
            // ObjectStreamClass.lookup() method handles this, but we've put
            // this fix here rather than change lookup because the new behaviour
            // is needed in other cases.

            for (currdesc = currentClassDesc, currclass = currentClass;
                 currdesc != null && currdesc.isSerializable();   /*sun.4296963 ibm.11861*/
                 currdesc = currdesc.getSuperclass()) {

                /*
                 * Search the classes to see if the class of this
                 * descriptor appears further up the hierarchy. Until
                 * it's found assume its an inserted class.  If it's
                 * not found, its the descriptor's class that has been
                 * removed.
                 */
                Class cc = currdesc.forClass();
                Class cl;
                for (cl = currclass; cl != null; cl = cl.getSuperclass()) {
                    if (cc == cl) {
                        // found a superclass that matches this descriptor
                        break;
                    } else {
                        /* Ignore a class that doesn't match.  No
                         * action is needed since it is already
                         * initialized.
                         */
                    }
                } // end : for (cl = currclass; cl != null; cl = cl.getSuperclass())
                /* Test if there is room for this new entry.
                 * If not, double the size of the arrays and copy the contents.
                 */
                spClass++;
                if (spClass >= classes.length) {
                    int newlen = classes.length * 2;
                    Class[] newclasses = new Class[newlen];
                    ObjectStreamClass[] newclassdesc = new ObjectStreamClass[newlen];

                    System.arraycopy(classes, 0,
                                     newclasses, 0,
                                     classes.length);
                    System.arraycopy(classdesc, 0,
                                     newclassdesc, 0,
                                     classes.length);

                    classes = newclasses;
                    classdesc = newclassdesc;
                }

                if (cl == null) {
                    /* Class not found corresponding to this descriptor.
                     * Pop off all the extra classes pushed.
                     * Push the descriptor and a null class.
                     */
                    classdesc[spClass] = currdesc;
                    classes[spClass] = null;
                } else {
                    /* Current class descriptor matches current class.
                     * Some classes may have been inserted.
                     * Record the match and advance the class, continue
                     * with the next descriptor.
                     */
                    classdesc[spClass] = currdesc;
                    classes[spClass] = cl;
                    currclass = cl.getSuperclass();
                }
            } // end : for (currdesc = currentClassDesc, currclass = currentClass;

            /* Allocate a new object.  The object is only constructed
             * above the highest serializable class and is set to
             * default values for all more specialized classes.
             */
            try {
                currentObject = (currentClass == null) ?
                    null : currentClassDesc.newInstance() ;

                // Store this object and its beginning position
                // since there might be indirections to it while
                // it's been unmarshalled.
                activeRecursionMgr.addObject(offset, currentObject);
            } catch (InvocationTargetException e) {
                InvalidClassException exc = new InvalidClassException(
                    currentClass.getName(),
                    "InvocationTargetException accessing no-arg constructor");
                exc.initCause( e ) ;
                throw exc ;
            } catch (UnsupportedOperationException e) {
                InvalidClassException exc = new InvalidClassException(
                    currentClass.getName(),
                    "UnsupportedOperationException accessing no-arg constructor");
                exc.initCause( e ) ;
                throw exc ;
            } catch (InstantiationException e) {
                InvalidClassException exc = new InvalidClassException(
                    currentClass.getName(),
                    "InstantiationException accessing no-arg constructor");
                exc.initCause( e ) ;
                throw exc ;
            }

            /*
             * For all the pushed descriptors and classes.
             *  if the class has its own writeObject and readObject methods
             *      call the readObject method
             *  else
             *      invoke the defaultReadObject method
             */
            try {
                for (spClass = spClass; spClass > spBase; spClass--) {
                    /*
                     * Set current descriptor and corresponding class
                     */
                    currentClassDesc = classdesc[spClass];
                    currentClass = classes[spClass];
                    if (classes[spClass] != null) {
                        /* Read the data from the stream described by the
                         * descriptor and store into the matching class.
                         */

                        ReadObjectState oldState = readObjectState;
                        setState(DEFAULT_STATE);

                        try {

                            // Changed since invokeObjectReader no longer does this.
                            if (currentClassDesc.hasWriteObject()) {

                                // Read format version
                                readFormatVersion();

                                // Read defaultWriteObject indicator
                                boolean calledDefaultWriteObject = readBoolean();

                                readObjectState.beginUnmarshalCustomValue(this,
                                                                          calledDefaultWriteObject,
                                                                          (currentClassDesc.readObjectMethod
                                                                           != null));
                            } else {
                                if (currentClassDesc.hasReadObject())
                                    setState(IN_READ_OBJECT_REMOTE_NOT_CUSTOM_MARSHALED);
                            }

                            if (!invokeObjectReader(currentClassDesc, currentObject, currentClass) ||
                                readObjectState == IN_READ_OBJECT_DEFAULTS_SENT) {

                                // Error case of no readObject and didn't call
                                // defaultWriteObject handled in default state

                                ObjectStreamField[] fields =
                                    currentClassDesc.getFieldsNoCopy();
                                if (fields.length > 0) {
                                    inputClassFields(currentObject, currentClass, fields, sender);
                                }
                            }

                            if (currentClassDesc.hasWriteObject())
                                readObjectState.endUnmarshalCustomValue(this);

                        } finally {
                            setState(oldState);
                        }

                    } else {

                        // _REVISIT_ : Can we ever get here?
                        /* No local class for this descriptor,
                         * Skip over the data for this class.
                         * like defaultReadObject with a null currentObject.
                         * The code will read the values but discard them.
                         */
                            ObjectStreamField[] fields =
                                currentClassDesc.getFieldsNoCopy();
                            if (fields.length > 0) {
                                inputClassFields(null, currentClass, fields, sender);
                            }

                        }

                }
            } finally {
                                // Make sure we exit at the same stack level as when we started.
                spClass = spBase;
            }
        }
        } finally {
            // We've completed deserializing this object.  Any
            // future indirections will be handled correctly at the
            // CDR level.  The ActiveRecursionManager only deals with
            // objects currently being deserialized.
            activeRecursionMgr.removeObject(offset);
        }

        return currentObject;
    }

    // This retrieves a vector of FVD's for the hierarchy of serializable classes stemming from
    // repositoryID.  It is assumed that the sender will not provide base_value id's for non-serializable
    // classes!
    private Vector getOrderedDescriptions(String repositoryID,
                                          com.sun.org.omg.SendingContext.CodeBase sender) {
        Vector descs = new Vector();

        if (sender == null) {
            return descs;
        }

        FullValueDescription aFVD = sender.meta(repositoryID);
        while (aFVD != null) {
            descs.insertElementAt(aFVD, 0);
            if ((aFVD.base_value != null) && !kEmptyStr.equals(aFVD.base_value)) {
                aFVD = sender.meta(aFVD.base_value);
            }
            else return descs;
        }

        return descs;
    }

    /**
     * This input method uses FullValueDescriptions retrieved from the sender's runtime to
     * read in the data.  This method is capable of throwing out data not applicable to client's fields.
     * This method handles instances where the reader has a class not sent by the sender, the sender sent
     * a class not present on the reader, and/or the reader's class does not match the sender's class.
     *
     * NOTE : If the local description indicates custom marshaling and the remote type's FVD also
     * indicates custom marsahling than the local type is used to read the data off the wire.  However,
     * if either says custom while the other does not, a MARSHAL error is thrown.  Externalizable is
     * a form of custom marshaling.
     *
     */
    private Object inputObjectUsingFVD(Class clz,
                                       String repositoryID,
                                       com.sun.org.omg.SendingContext.CodeBase sender,
                                       int offset)
        throws IOException, ClassNotFoundException
    {
        int spBase = spClass;   // current top of stack
        try{

            /*
             * Get the descriptor and then class of the incoming object.
             */

            ObjectStreamClass currdesc = currentClassDesc = ObjectStreamClass.lookup(clz);
            Class currclass = currentClass = clz;

            /* If Externalizable,
             *  Create an instance and tell it to read its data.
             * else,
             *  Handle it as a serializable class.
             */
            if (currentClassDesc.isExternalizable()) {
                try {
                    currentObject = (currentClass == null) ?
                        null : currentClassDesc.newInstance();
                    if (currentObject != null) {
                        // Store this object and its beginning position
                        // since there might be indirections to it while
                        // it's been unmarshalled.
                        activeRecursionMgr.addObject(offset, currentObject);

                        // Read format version
                        readFormatVersion();

                        Externalizable ext = (Externalizable)currentObject;
                        ext.readExternal(this);
                    }
                } catch (InvocationTargetException e) {
                    InvalidClassException exc = new InvalidClassException(
                        currentClass.getName(),
                        "InvocationTargetException accessing no-arg constructor");
                    exc.initCause( e ) ;
                    throw exc ;
                } catch (UnsupportedOperationException e) {
                    InvalidClassException exc = new InvalidClassException(
                        currentClass.getName(),
                        "UnsupportedOperationException accessing no-arg constructor");
                    exc.initCause( e ) ;
                    throw exc ;
                } catch (InstantiationException e) {
                    InvalidClassException exc = new InvalidClassException(
                        currentClass.getName(),
                        "InstantiationException accessing no-arg constructor");
                    exc.initCause( e ) ;
                    throw exc ;
                }
            } else {
                /*
                 * This is your basic diff pattern, made simpler
                 * because reordering is not allowed.
                 */
                for (currdesc = currentClassDesc, currclass = currentClass;
                     currdesc != null && currdesc.isSerializable();   /*sun.4296963 ibm.11861*/

                     currdesc = currdesc.getSuperclass()) {

                    /*
                     * Search the classes to see if the class of this
                     * descriptor appears further up the hierarchy. Until
                     * it's found assume its an inserted class.  If it's
                     * not found, its the descriptor's class that has been
                     * removed.
                     */
                    Class cc = currdesc.forClass();
                    Class cl;
                    for (cl = currclass; cl != null; cl = cl.getSuperclass()) {
                        if (cc == cl) {
                            // found a superclass that matches this descriptor
                            break;
                        } else {
                            /* Ignore a class that doesn't match.  No
                             * action is needed since it is already
                             * initialized.
                             */
                        }
                    } // end : for (cl = currclass; cl != null; cl = cl.getSuperclass())
                    /* Test if there is room for this new entry.
                     * If not, double the size of the arrays and copy the contents.
                     */
                    spClass++;
                    if (spClass >= classes.length) {
                        int newlen = classes.length * 2;
                        Class[] newclasses = new Class[newlen];
                        ObjectStreamClass[] newclassdesc = new ObjectStreamClass[newlen];

                        System.arraycopy(classes, 0,
                                         newclasses, 0,
                                         classes.length);
                        System.arraycopy(classdesc, 0,
                                         newclassdesc, 0,
                                         classes.length);

                        classes = newclasses;
                        classdesc = newclassdesc;
                    }

                    if (cl == null) {
                        /* Class not found corresponding to this descriptor.
                         * Pop off all the extra classes pushed.
                         * Push the descriptor and a null class.
                         */
                        classdesc[spClass] = currdesc;
                        classes[spClass] = null;
                    } else {
                        /* Current class descriptor matches current class.
                         * Some classes may have been inserted.
                         * Record the match and advance the class, continue
                         * with the next descriptor.
                         */
                        classdesc[spClass] = currdesc;
                        classes[spClass] = cl;
                        currclass = cl.getSuperclass();
                    }
                } // end : for (currdesc = currentClassDesc, currclass = currentClass;

                /* Allocate a new object.
                 */
                try {
                    currentObject = (currentClass == null) ?
                        null : currentClassDesc.newInstance();

                    // Store this object and its beginning position
                    // since there might be indirections to it while
                    // it's been unmarshalled.
                    activeRecursionMgr.addObject(offset, currentObject);
                } catch (InvocationTargetException e) {
                    InvalidClassException exc = new InvalidClassException(
                        currentClass.getName(),
                        "InvocationTargetException accessing no-arg constructor");
                    exc.initCause( e ) ;
                    throw exc ;
                } catch (UnsupportedOperationException e) {
                    InvalidClassException exc = new InvalidClassException(
                        currentClass.getName(),
                        "UnsupportedOperationException accessing no-arg constructor");
                    exc.initCause( e ) ;
                    throw exc ;
                } catch (InstantiationException e) {
                    InvalidClassException exc = new InvalidClassException(
                        currentClass.getName(),
                        "InstantiationException accessing no-arg constructor");
                    exc.initCause( e ) ;
                    throw exc ;
                }

                Enumeration fvdsList = getOrderedDescriptions(repositoryID, sender).elements();

                while((fvdsList.hasMoreElements()) && (spClass > spBase)) {
                    FullValueDescription fvd = (FullValueDescription)fvdsList.nextElement();
                    // d4365188: backward compatability
                    String repIDForFVD = vhandler.getClassName(fvd.id);
                    String repIDForClass = vhandler.getClassName(vhandler.getRMIRepositoryID(currentClass));

                    while ((spClass > spBase) &&
                           (!repIDForFVD.equals(repIDForClass))) {
                        int pos = findNextClass(repIDForFVD, classes, spClass, spBase);
                        if (pos != -1) {
                            spClass = pos;
                            currclass = currentClass = classes[spClass];
                            repIDForClass = vhandler.getClassName(vhandler.getRMIRepositoryID(currentClass));
                        }
                        else { // Read and throw away one level of the fvdslist

                            // This seems to mean that the sender had a superclass that
                            // we don't have

                            if (fvd.is_custom) {

                                readFormatVersion();
                                boolean calledDefaultWriteObject = readBoolean();

                                if (calledDefaultWriteObject)
                                    inputClassFields(null, null, null, fvd.members, sender);

                                if (getStreamFormatVersion() == 2) {

                                    ((ValueInputStream)getOrbStream()).start_value();
                                    ((ValueInputStream)getOrbStream()).end_value();
                                }

                                // WARNING: If stream format version is 1 and there's
                                // optional data, we'll get some form of exception down
                                // the line or data corruption.

                            } else {

                                inputClassFields(null, currentClass, null, fvd.members, sender);
                            }

                            if (fvdsList.hasMoreElements()){
                                fvd = (FullValueDescription)fvdsList.nextElement();
                                repIDForFVD = vhandler.getClassName(fvd.id);
                            }
                            else return currentObject;
                        }
                    }

                    currdesc = currentClassDesc = ObjectStreamClass.lookup(currentClass);

                    if (!repIDForClass.equals("java.lang.Object")) {

                        // If the sender used custom marshaling, then it should have put
                        // the two bytes on the wire indicating stream format version
                        // and whether or not the writeObject method called
                        // defaultWriteObject/writeFields.

                        ReadObjectState oldState = readObjectState;
                        setState(DEFAULT_STATE);

                        try {

                            if (fvd.is_custom) {

                                // Read format version
                                readFormatVersion();

                                // Read defaultWriteObject indicator
                                boolean calledDefaultWriteObject = readBoolean();

                                readObjectState.beginUnmarshalCustomValue(this,
                                                                          calledDefaultWriteObject,
                                                                          (currentClassDesc.readObjectMethod
                                                                           != null));
                            }

                            boolean usedReadObject = false;

                            // Always use readObject if it exists, and fall back to default
                            // unmarshaling if it doesn't.
                            try {

                                if (!fvd.is_custom && currentClassDesc.hasReadObject())
                                    setState(IN_READ_OBJECT_REMOTE_NOT_CUSTOM_MARSHALED);

                                // See the definition of defaultReadObjectFVDMembers
                                // for more information.  This concerns making sure
                                // we use the remote FVD's members in defaultReadObject.
                                defaultReadObjectFVDMembers = fvd.members;
                                usedReadObject = invokeObjectReader(currentClassDesc,
                                                                    currentObject,
                                                                    currentClass);

                            } finally {
                                defaultReadObjectFVDMembers = null;
                            }

                            // Note that the !usedReadObject !calledDefaultWriteObject
                            // case is handled by the beginUnmarshalCustomValue method
                            // of the default state
                            if (!usedReadObject || readObjectState == IN_READ_OBJECT_DEFAULTS_SENT)
                                inputClassFields(currentObject, currentClass, currdesc, fvd.members, sender);

                            if (fvd.is_custom)
                                readObjectState.endUnmarshalCustomValue(this);

                        } finally {
                            setState(oldState);
                        }

                        currclass = currentClass = classes[--spClass];

                    } else {

                        // The remaining hierarchy of the local class does not match the sender's FVD.
                        // So, use remaining FVDs to read data off wire.  If any remaining FVDs indicate
                        // custom marshaling, throw MARSHAL error.
                        inputClassFields(null, currentClass, null, fvd.members, sender);

                        while (fvdsList.hasMoreElements()){
                            fvd = (FullValueDescription)fvdsList.nextElement();

                            if (fvd.is_custom)
                                skipCustomUsingFVD(fvd.members, sender);
                            else
                                inputClassFields(null, currentClass, null, fvd.members, sender);
                        }

                    }

                } // end : while(fvdsList.hasMoreElements())
                while (fvdsList.hasMoreElements()){

                    FullValueDescription fvd = (FullValueDescription)fvdsList.nextElement();
                    if (fvd.is_custom)
                        skipCustomUsingFVD(fvd.members, sender);
                    else
                        throwAwayData(fvd.members, sender);
                }
            }

            return currentObject;
        }
        finally {
                // Make sure we exit at the same stack level as when we started.
                spClass = spBase;

                // We've completed deserializing this object.  Any
                // future indirections will be handled correctly at the
                // CDR level.  The ActiveRecursionManager only deals with
                // objects currently being deserialized.
                activeRecursionMgr.removeObject(offset);
            }

        }

    /**
     * This input method uses FullValueDescriptions retrieved from the sender's runtime to
     * read in the data.  This method is capable of throwing out data not applicable to client's fields.
     *
     * NOTE : If the local description indicates custom marshaling and the remote type's FVD also
     * indicates custom marsahling than the local type is used to read the data off the wire.  However,
     * if either says custom while the other does not, a MARSHAL error is thrown.  Externalizable is
     * a form of custom marshaling.
     *
     */
    private Object skipObjectUsingFVD(String repositoryID,
                                      com.sun.org.omg.SendingContext.CodeBase sender)
        throws IOException, ClassNotFoundException
    {

        Enumeration fvdsList = getOrderedDescriptions(repositoryID, sender).elements();

        while(fvdsList.hasMoreElements()) {
            FullValueDescription fvd = (FullValueDescription)fvdsList.nextElement();
            String repIDForFVD = vhandler.getClassName(fvd.id);

            if (!repIDForFVD.equals("java.lang.Object")) {
                if (fvd.is_custom) {

                    readFormatVersion();

                    boolean calledDefaultWriteObject = readBoolean();

                    if (calledDefaultWriteObject)
                        inputClassFields(null, null, null, fvd.members, sender);

                    if (getStreamFormatVersion() == 2) {

                        ((ValueInputStream)getOrbStream()).start_value();
                        
