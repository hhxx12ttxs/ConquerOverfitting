package remote.gui.util;

import java.io.IOException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class InputLogStream extends FilterInputStream {

	OutputStream logStream;
	InputStream sourceStream;

	protected InputLogStream(OutputStream logStream, InputStream sourceStream) throws Exception
	{
		super(sourceStream);
		this.logStream = logStream;
		this.sourceStream = sourceStream;
	}

	public int read() throws IOException {
		int b = sourceStream.read();
		if (b>=0)
		{
			this.logStream.write(b);
			this.logStream.flush();
		}
		return b;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int r = sourceStream.read(b,off,len);
		this.logStream.write(b,off,len);
		this.logStream.flush();
		return r;
	}

	protected void finalize() throws Throwable {
		System.out.println("Finalizing InputLogStream");

		super.finalize();
	}


}

