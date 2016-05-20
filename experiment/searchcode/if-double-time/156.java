package client;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Observable;

import common.RtpPacketHeader;

/**
 * Represents a video player.
 */
public class VideoPlayer extends Observable implements Runnable
{
	private static final double VIDEO_FRAME_TIME = 0.04;
	private static final int RTP_PORT = 8003;
	private double currentPosition;
	private VideoPlayerState state;
	private int cseq;
	private String session;
	private URL url;
	private Image currentFrame;
	private DatagramSocket socket;
	private double length;
	
	public VideoPlayer() throws VideoPlayerException
	{
		try
		{
			socket = new DatagramSocket(RTP_PORT);
			
			Thread thread = new Thread(this);
			thread.start();
		}
		catch (SocketException ex)
		{
			throw new VideoPlayerException("Error setting up the video connection.");
		}
	}
	
	/**
	 * Plays the video with the specified URL.
	 * @param url The URL of the video to play.
	 * @throws VideoPlayerException Thrown if the operation failed.
	 */
	public void play(String url) throws VideoPlayerException
	{
		try
		{
			this.url = new URL(url);
			RtspClient client = new RtspClient(this.url, cseq++);
			HttpClientRequest request = client.createSetupRequest(RTP_PORT);
			HttpClientResponse response = client.getResponse(request);
			
			int status = response.getStatusCode();
			
			if (status == 404)
				throw new VideoPlayerException("Video not found: " + url);
			else if (status != 200)
				throw new VideoPlayerException(String.format("Error starting video (status %d): %s", status, url));
			
			session = response.getHeaders().get("Session");
			
			client = new RtspClient(this.url, cseq++, session);
			request = client.createDescribeRequest();
			response = client.getResponse(request);
			
			String line = response.getBody();
			
			if (!line.startsWith("length="))
				throw new VideoPlayerException("Bad response.");
			
			length = Double.parseDouble(line.substring(7));
			System.out.println("got the length");
			play();
		}
		catch (MalformedURLException ex)
		{
			throw new VideoPlayerException("The video URL was not in a valid format: " + url);
		}
		catch (IOException ex)
		{
			throw new VideoPlayerException("Error communicating with the server.");
		}
	}
	
	
	/**
	 * Plays the video from the specified time.
	 * @param time The time to play from.
	 * @throws VideoPlayerException Thrown if the operation failed.
	 */
	public void play(double time) throws VideoPlayerException
	{
		if (url == null)
			throw new IllegalStateException("No current video.");
		
		if (state == VideoPlayerState.Stopped)
		{
			updateState(VideoPlayerState.Playing);
			play(url.toString());
		}
		else
		{
			try
			{
				updateState(VideoPlayerState.Playing);
				
				RtspClient client = new RtspClient(url, cseq++, session);
				HttpClientRequest request = client.createPlayRequest(time);
				HttpClientResponse response = client.getResponse(request);
				
				if (response.getStatusCode() != 200)
					throw new VideoPlayerException("Error playing video.");
				
				updateCurrentPosition(time);
			}
			catch (IOException ex)
			{
				throw new VideoPlayerException("Error playing video.");
			}
		}
	}
	
	
	/**
	 * Plays the video from the current position or the beginning.
	 * @throws VideoPlayerException Thrown if the operation failed.
	 */
	public void play() throws VideoPlayerException
	{
		play(currentPosition);
	}
	
	
	/**
	 * Pauses the video.
	 * @throws VideoPlayerException Thrown if the operation failed.
	 */
	public void pause() throws VideoPlayerException
	{
		try
		{
			updateState(VideoPlayerState.Paused);
			
			RtspClient client = new RtspClient(url, cseq++, session);
			HttpClientRequest request = client.createPauseRequest();
			HttpClientResponse response = client.getResponse(request);
			
			if (response.getStatusCode() != 200)
				throw new VideoPlayerException("Error pausing video.");
		}
		catch (IOException ex)
		{
			throw new VideoPlayerException("Error pausing video.");
		}
	}
	
	
	/**
	 * Stops the video so that the next call to Play will start it from the beginning.
	 * @throws VideoPlayerException Thrown if the operation failed.
	 */
	public void stop() throws VideoPlayerException
	{
		try
		{
			updateState(VideoPlayerState.Stopped);
			
			RtspClient client = new RtspClient(url, cseq++, session);
			HttpClientRequest request = client.createTeardownRequest();
			HttpClientResponse response = client.getResponse(request);
			
			if (response.getStatusCode() != 200)
				throw new VideoPlayerException("Error stopping video.");
			
			updateCurrentPosition(0);
		}
		catch (IOException ex)
		{
			throw new VideoPlayerException("Error stopping video.");
		}
	}
	
	
	/**
	 * Seeks the video to the specified position.
	 * @param position The position in seconds to seek to.
	 * @throws VideoPlayerException Thrown if the operation failed.
	 */
	public void seek(double position) throws VideoPlayerException
	{
		if (state == VideoPlayerState.Playing)
			pause();
		
		play(position);
	}
	
	
	/**
	 * Gets the current position in seconds.
	 * @return
	 */
	public double getCurrentPosition()
	{
		return currentPosition;
	}
	
	
	/**
	 * Gets the total length of the video in seconds. 
	 * @return
	 */
	public double getVideoLength()
	{
		return length;
	}
	
	
	/**
	 * Gets the image representing the current frame. 
	 * @return
	 */
	public Image getCurrentFrame()
	{
		return currentFrame;
	}
	
	
	/**
	 * Gets the current state of the player.
	 * @return
	 */
	public VideoPlayerState getState()
	{
		return state;	
	}
	
	
	/**
	 * Gets the URL of the video currently being played.
	 * @return
	 */
	public String getCurrentUrl()
	{
		if (url == null)
			return null;
		else
			return url.toString();	
	}
	
	
	private synchronized void updateState(VideoPlayerState state)
	{
		this.state = state;
		this.setChanged();
		this.notifyObservers(UpdateReason.VideoPlayerStateChanged);
	}
	
	private synchronized void updateCurrentPosition(double position)
	{
		this.currentPosition = position;
		this.setChanged();
		this.notifyObservers(UpdateReason.VideoPositionChanged);
	}
	
	private synchronized void advanceCurrentPosition()
	{
		this.currentPosition += VIDEO_FRAME_TIME;
		this.setChanged();
		this.notifyObservers(UpdateReason.VideoPositionChanged);
	}

	@Override
	public void run()
	{
		byte[] buffer = new byte[20480];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		while (true)
		{
			try
			{
				socket.receive(packet);
				
				//check for an empty packet which signifies the end of stream
				if (packet.getLength() == RtpPacketHeader.HEADER_SIZE)
				{
					updateState(VideoPlayerState.Finished);
				}
				else
				{
					//read the image
					currentFrame = toolkit.createImage(buffer, RtpPacketHeader.HEADER_SIZE, packet.getLength() - RtpPacketHeader.HEADER_SIZE);
					
					//update the position only if the state is playing to prevent race condition
					if (state == VideoPlayerState.Playing)
						advanceCurrentPosition();
					else
						System.out.println(state);
					
					this.setChanged();
					this.notifyObservers(UpdateReason.VideoImageChanged);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}

