/*
 * Created on Sep 19, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package remote.motecontrol.client;
/**
 * @author zept
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MoteConsole
{

	protected int begin, length;
	protected byte buffer[] = new byte[30000];

	public MoteConsole()
	{
		begin = 0; length = 0;
	}

	public void write(byte[] input)
	{
		int i;
		int end = (begin+length)%buffer.length;
		if (length+input.length > buffer.length)
		{
			// begin always moves with end
			for (i=0; i<input.length; i++)
			{
				buffer[(end+i)%buffer.length] = input[i];
			}
			begin=(end+i)%buffer.length;
			length = buffer.length;
		}
		else
		{
			// begin will not change, do a normal copy
			for (i=0; i<input.length; i++)
			{
				buffer[end+i] = input[i];
			}
			length += input.length;
		}
	}
}

