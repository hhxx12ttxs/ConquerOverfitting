/*
 * Java Payloads.
 * 
 * Copyright (c) 2012 Michael 'mihi' Schierl
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *   
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *   
 * - Neither name of the copyright holders nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *   
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND THE CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR THE CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package javapayload.stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MultiStageMux implements Stage {

	public void start(DataInputStream in, OutputStream rawOut, String[] parameters) throws Exception {
		DataOutputStream out = new DataOutputStream(rawOut);
		try {
			List/* <MultiStageClassLoader> */stages = new ArrayList();
			while (true) {
				int index = in.readInt();
				if (index == -1)
					break;
				if (index < 0 || index > stages.size()) {
					throw new RuntimeException("Invalid stage index: " + index + " (stages size = " + stages.size() + ")");
				}
				if (index == stages.size()) {
					stages.add(new MultiStageClassLoader(in, new MultiStageMuxOutputStream(index, out)));
				} else {
					OutputStream outBuf = ((MultiStageClassLoader) stages.get(index)).getBuffer();
					byte[] buf = new byte[in.readInt()];
					if (buf.length == 0) {
						outBuf.close();
					} else {
						in.readFully(buf);
						outBuf.write(buf);
						outBuf.flush();
					}
				}
			}
			while (in.read() != -1)
				;
			out.writeInt(-1);
			out.close();
			out = null;
		} finally {
			if (out != null)
				out.writeInt(-2);
		}
	}
}

