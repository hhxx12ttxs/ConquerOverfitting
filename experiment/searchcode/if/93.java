////////////////////////////////////////////////////////////////////////////////
//
//  This program is free software; you can redistribute it and/or modify 
//  it under the terms of the GNU General Public License as published by the 
//  Free Software Foundation; either version 3 of the License, or (at your 
//  option) any later version.
//
//  This program is distributed in the hope that it will be useful, but 
//  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
//  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
//  for more details.
//
//  You should have received a copy of the GNU General Public License along 
//  with this program; if not, see <http://www.gnu.org/licenses>.
//
////////////////////////////////////////////////////////////////////////////////

package merapi.io.amf;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Output;
import merapi.io.writer.IWriter;
import merapi.messages.IMessage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * The <code>AMF3Reader</code> class deserializes AMF 3 encoded binary data into an Object.
 * When a message has been received from the Flex bridge.
 *
 * @see merapi.io.reader.IReader;
 */
@Service
public class AMF3Writer implements IWriter {

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public AMF3Writer() {
        super();

        __amf3Output = new Amf3Output(new SerializationContext());
    }


    //--------------------------------------------------------------------------
    //
    //  Variables
    //
    //--------------------------------------------------------------------------

    /**
     * @private Used to serialize an object into AMF binary data.
     */
    Amf3Output __amf3Output = null;

    /**
     * @private Used to expose the binary data as a stream to <code>__amf3Output</code>
     */
    ByteArrayOutputStream __byteArrayOutputStream = new ByteArrayOutputStream();


    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    /**
     * Serializes <code>message</code> using <code>flex.messaging.io.amf.Amf3Output</code>.
     */
    public byte[] write(IMessage message) throws IOException {
        __byteArrayOutputStream.reset();

        __amf3Output.reset();
        __amf3Output.setOutputStream(__byteArrayOutputStream);
        __amf3Output.writeObject(message);

        return __byteArrayOutputStream.toByteArray();
	}

}

