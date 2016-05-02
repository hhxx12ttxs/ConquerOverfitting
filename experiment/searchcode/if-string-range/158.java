package server;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.SocketException;

/**
 * Supports requests to change the video such as pause and play, tells the RTP
 * session to pause or play based on the user requests.
 * 
 * @author qrb08163
 * 
 */
public class RtspHandler implements IHttpHandler
{
	private RtpServer rtpServer;
	
	public RtspHandler(RtpServer rtpServer)
	{
		this.rtpServer = rtpServer;
	}
	
	
	@Override
	public boolean ProcessRequest(HttpContext context)
	{
		HttpRequest request = context.getRequest();
		HttpResponse response = context.getResponse();
		
		String sessionName = request.getHeaders().get("Session");
		String method = request.getMethod();
		
		if (method.equals("SETUP"))
		{
			String transport = request.getHeaders().get("Transport");
			String port[] = transport.split("=");
			String url = request.getUrl();
			String path = "." + url.substring(url.lastIndexOf("/"));
			
			try
			{
				sessionName = rtpServer.setup(path, request.getClientAddress(), Integer.parseInt(port[1]));
				response.setHeader("Session", sessionName);
				response.setStatus(200, "OK");
			}
			catch (NumberFormatException e)
			{
				response.setStatus(400, "Bad Request");
			}
			catch (SocketException e)
			{
				response.setStatus(500, "Internal Server Error");
				e.printStackTrace();
			}
			catch (FileNotFoundException e)
			{
				response.setStatus(404, "Not Found");
			}
			return true;
		}
		else
		{
			RtpServerSession session = rtpServer.getSession(sessionName);
			
			if (session == null)
				throw new IllegalStateException("Could not find session: " + sessionName);
			
			if (method.equals("PLAY"))
			{
				if (request.getHeaders().containsKey("Range"))
				{
					
					String range = request.getHeaders().get("Range");
					range = range.substring(4, range.length() - 1);
					double time = Double.parseDouble(range);
					
					try
					{
						session.play(time);
						response.setStatus(200, "OK");
					}
					catch (IOException e)
					{
						response.setStatus(500, "Internal Server Error");
						e.printStackTrace();
					}
					
					return true;
				}
				else
				{
					session.play();
					response.setStatus(200, "OK");
					return true;
				}
			}
			else if (method.equals("PAUSE"))
			{
				session.pause();
				response.setStatus(200, "OK");
				return true;
			}
			else if (method.equals("TEARDOWN"))
			{
				try
				{
					rtpServer.teardown(sessionName);
					response.setStatus(200, "OK");
				}
				catch (IOException ex)
				{
					response.setStatus(500, "Internal Server Error");
					ex.printStackTrace();
				}
				
				return true;
			}
			else if(method.equals("DESCRIBE")){
				
				try
				{
					double length = session.getLength();
					response.setStatus(200, "OK");
					OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());
					writer.write(String.format("length=%.2f\r\n", length));
					writer.flush();
					writer.close();
				}
				catch(IOException ex){
					response.setStatus(500, "Internal Server Error");
					ex.printStackTrace();	
				}
				return true;
			}
			else return false;
		}
	}
	
	
}

