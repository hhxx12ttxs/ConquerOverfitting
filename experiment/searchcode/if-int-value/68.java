/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.smpp.message.param;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.testng.annotations.Test;

import org.mobicents.protocols.smpp.message.param.IntegerParamDescriptor;
import org.mobicents.protocols.smpp.util.PacketDecoderImpl;
import org.mobicents.protocols.smpp.util.PacketEncoderImpl;
import org.mobicents.protocols.smpp.util.SMPPIO;

@Test
public class IntegerParamDescriptorTest {

    private IntegerParamDescriptor integer1 = new IntegerParamDescriptor(1);
    private IntegerParamDescriptor integer2 = new IntegerParamDescriptor(2);
    private IntegerParamDescriptor integer4 = new IntegerParamDescriptor(4);
    private IntegerParamDescriptor integer8 = new IntegerParamDescriptor(8);

    public void testSizeOf() throws Exception {
        doSizeOf(new IntegerParamDescriptor(1), 1);
        doSizeOf(new IntegerParamDescriptor(2), 2);
        doSizeOf(new IntegerParamDescriptor(4), 4);
        doSizeOf(new IntegerParamDescriptor(8), 8);
    }

    public void testWriteObject() throws Exception {
        doWriteObject(integer1, 0L);
        doWriteObject(integer1, 127L);
        doWriteObject(integer1, 255L);
        
        doWriteObject(integer2, 0L);
        doWriteObject(integer2, (long) Short.MAX_VALUE);
        doWriteObject(integer2, 65535L);
        
        doWriteObject(integer4, 0L);
        doWriteObject(integer4, (long) Integer.MAX_VALUE);
        doWriteObject(integer4, 4294967295L);
        
        doWriteObject(integer8, 0L);
        doWriteObject(integer8, Long.MAX_VALUE);
    }
    
    public void testReadObject() throws Exception {
        doReadObject(integer1, 0L);
        doReadObject(integer1, 127L);
        doReadObject(integer1, 255L);
        
        doReadObject(integer2, 0L);
        doReadObject(integer2, (long) Short.MAX_VALUE);
        doReadObject(integer2, 65535L);
        
        doReadObject(integer4, 0L);
        doReadObject(integer4, (long) Integer.MAX_VALUE);
        doReadObject(integer4, 4294967295L);
        
        doReadObject(integer8, 0L);
        doReadObject(integer8, Long.MAX_VALUE);
    }
    
    private void doSizeOf(IntegerParamDescriptor descriptor, int intSize) throws Exception {
        Integer value = new Integer(231);
        assertEquals(descriptor.sizeOf(value), intSize);
        assertEquals(descriptor.sizeOf(null), intSize);
    }
    
    private void doWriteObject(IntegerParamDescriptor descriptor, long value) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PacketEncoderImpl encoder = new PacketEncoderImpl(out);
        descriptor.writeObject(value, encoder);
        byte[] array = out.toByteArray();
        assertEquals(array.length, descriptor.sizeOf(value));
        switch (descriptor.sizeOf(value)) {
        case 8:
            assertEquals(SMPPIO.readInt8(array, 0), value);
            break;
        case 4:
            assertEquals(SMPPIO.readUInt4(array, 0), value);
            break;
        case 2:
            assertEquals(SMPPIO.readUInt2(array, 0), (int) value);
            break;
        default:
            assertEquals((int) array[0] & 0xff, (int) value);
            break;
        }
    }
    
    private void doReadObject(IntegerParamDescriptor descriptor, long value) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        switch (descriptor.sizeOf(value)) {
        case 8:
            SMPPIO.writeLong(value, out);
            break;
        case 4:
            SMPPIO.writeLongInt(value, out);
            break;
        case 2:
            SMPPIO.writeShort((int) value, out);
            break;
        default:
            SMPPIO.writeByte((int) value, out);
            break;
        }
        byte[] array = out.toByteArray();
        PacketDecoderImpl decoder = new PacketDecoderImpl(array);
        Number number = (Number) descriptor.readObject(decoder, -1);
        assertNotNull(number);
        assertEquals(number.longValue(), value);
        assertEquals(decoder.getParsePosition(), descriptor.sizeOf(value));
    }
}

