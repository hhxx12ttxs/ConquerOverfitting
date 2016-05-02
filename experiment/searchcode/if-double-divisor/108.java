package ifs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.openal.AL;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;

import org.newdawn.slick.openal.OggData;
import org.newdawn.slick.openal.OggDecoder;

public class DJ implements Runnable{

	public Thread thisThread;
	OggDecoder od = new OggDecoder();
	
	public volatile boolean loaded = false;
	
	public List<String> playlist = new ArrayList<String>();
	int playlistPos = 0;
	
	int [] buffers = new int[3]; // three buffers for music, previous current and next track.
	int [] sources = new int[3];
	List<List<DJMusicPoint>> musicPoints = new ArrayList<List<DJMusicPoint>>();//java is shit and can't make arrays of lists so I have to make a list of lists
	DJFadeInfo[] fadeInfo = new DJFadeInfo[3];
	
	int currentSound = 0;
	
	boolean loadedNextTrack = false;
	
	boolean nextTrackPlaying = false;
	
	//boolean quickTransition = true;
	
	@Override
	public void run() 
	{//init
		try
		{
			buffers[0] = alGenBuffers();
			buffers[1] = alGenBuffers();
			buffers[2] = alGenBuffers();
			sources[0] = alGenSources();
			sources[1] = alGenSources();
			sources[2] = alGenSources();
			musicPoints.add(null);//just add three elements so you can go set(2, musicpointlist) later;
			musicPoints.add(null);
			musicPoints.add(null);
			fadeInfo[0] = new DJFadeInfo();//add three basic fade info elements
			fadeInfo[1] = new DJFadeInfo();
			fadeInfo[2] = new DJFadeInfo();
			
			//we can assume that the main thread put at least one track in the playlist
			loadTrack(currentSound,playlist.get(0));
	        alSourcei(sources[currentSound], AL_SAMPLE_OFFSET,749700);// This is 17 seconds for lost in dreams to play at the appropriate time on opening
	        
			//alSourcei(sources[currentSound], AL_SEC_OFFSET,60);//debug shit
			//alSourcei(sources[currentSound], AL_SEC_OFFSET,108);//debug shit
			
	        //set up a fade in at the start (17 seconds in) of the opening track
			fadeInfo[currentSound] = new DJFadeInfo(749700,44100*3,true,false); // 3 second fade;
			
	        loaded = true;
			while(true)// wait till main thread is loaded if its behind
			{
				if(IFS.loaded) break;// leave this loop
				Thread.sleep(1);
			}
			
			alSourcePlay(sources[currentSound]);// play music
			
			while(true)//DJ loop
			{
				if(ifs.IFS.closing) break;//stop thread if closing
				djLoop();
				Thread.sleep(1);//don't hog resources lel
			}
		}
		catch (InterruptedException e)
		{return;}
		catch (Exception e)//if theres any other sort of exception
		{
			e.printStackTrace();
			return;//give up and the app will probably crash anyway (unless its an interrupt exception)
		}
	}
	
	//loads a track into a "sound" 
	void loadTrack(int currentSound,String track) throws IOException
	{
		OggData data = od.getData(DJ.class.getResourceAsStream(track + ".ogg"));
		
		SoundEffectsProcessor sep = new SoundEffectsProcessor(data.data);
		//sep.applyReverb(0.125f,0.025f,0.5f);
		//sep.applyDistortion();
		//sep.applyReverb(0.080f,-0.030f,0.4f);
		sep.flush();
		
		//Effects code??
		/*
		ShortBuffer sb = data.data.asShortBuffer();
		
		for(int i = 0; i < sb.capacity(); i++)
		{
			short shrt = Short.reverseBytes(sb.get(i));
			double divisor = shrt < 0 ? 32768f : 32767f;
			double sample = shrt/divisor;
			
			boolean negative = sample < 0f;//remember if its negative
			
			//shitty reverb
			int baseDelay = 2*(44100/8); //reverb takes 1/4 seconds to bounce back
			float decay = 0.5f;      //and bounces back with 0.5 times the volume.
			int leftRightOffset = 2*(44100/(80/2));// left and right delays differ by this much.
			
			int delay = baseDelay + (i%2 == 0 ? leftRightOffset : -leftRightOffset);
			
			
			if(i < sb.capacity()-delay)
			{
				shrt = Short.reverseBytes(sb.get(i + delay));
				divisor = shrt < 0 ? 32768f : 32767f;
				double sample2 = shrt/divisor;
				
				sample2 += sample * decay;
				sample2 = Utility.clamp(sample2,-1d,1d);
				divisor = sample2 < 0f ? 32768f : 32767f;
				sb.put(i + delay, Short.reverseBytes((short) (sample2 * divisor)));
			}
			
			//*//*/shitty distortion
			sample = Math.abs(sample);
			sample = Math.pow(sample,negative ? 0.5f: 1.2f);
			//sample = Math.pow(sample,2f);
			if(negative)sample = -sample;
			//*//*
			//sample = clamp(sample,)
			divisor = sample < 0f ? 32768f : 32767f;
			sb.put(i, Short.reverseBytes((short) (sample * divisor)));
		}
		//*/
		
		alBufferData(buffers[currentSound], AL_FORMAT_STEREO16, data.data, 44100);
        alSourcei(sources[currentSound], AL_BUFFER, buffers[currentSound]);
        alSourcei(sources[currentSound], AL_LOOPING, AL_TRUE);// so in the worst case if the dj fucks up there won't be any pause in sound
        
        loadMusicPointData(currentSound, track);
	}
	void loadMusicPointData(int storeIndex, String track) throws IOException
	{
		List<DJMusicPoint> pointsList = new ArrayList<DJMusicPoint>();
        
		byte[] arr = new byte[4096];//should be more than big enough
        InputStream is = IFS.class.getResourceAsStream(track + ".dat");
        
        int offset = 0;
        int read = is.read(arr,0, 4096);
        
        while(read > 0)//this should read the whole file
        	read = is.read(arr,offset,4096-offset);

        ByteBuffer bb = ByteBuffer.wrap(arr);
        bb.order(ByteOrder.BIG_ENDIAN);
        int elems = bb.getInt();
        for(int i = 0; i < elems; i++)
        {
        	DJMusicPoint mp = new DJMusicPoint();
        	mp.sampleNo = bb.getInt();
        	mp.type = bb.get();
        	
        	if(mp.type == 1)
        	{//hakk
        		mp.bpm = bb.getFloat();
        	}
        	else if (mp.type == 2)
        	{//fade in
        		mp.bpm = bb.getFloat();
        		mp.fadeInfo = new DJFadeInfo(mp.sampleNo,bb.getInt(),false,false);
        	}
        	else if (mp.type == 3)
        	{//fade out
        		mp.bpm = bb.getFloat();
        		mp.fadeInfo = new DJFadeInfo(mp.sampleNo,bb.getInt(),false,true);
        	}
        	
        	pointsList.add(mp);
        }
        musicPoints.set(storeIndex, pointsList);
	}
	
	void djLoop()
	{
		/* shouldn't need this code because since the dj loads the sounds it should know when to reset the music points and shit
		int newSamplePos = alGetSourcei(sources[currentSound], AL_SAMPLE_OFFSET);
		if(newSamplePos < samplePos)//if we went back in time then reset triggers because the sound looped
		{
			for(int i = 0; i < musicPoints.get(currentSound).size(); i++)
				musicPoints.get(currentSound).get(i).triggered = false;
		}
		samplePos = newSamplePos;
		*/
		
		if(!loadedNextTrack && playlistPos < playlist.size()-1)//if we haven't loaded the next track, and there is a next one in the playlist
		{//then get the dj loaded to load it
			DJTrackLoader trackLoader = new DJTrackLoader();
			trackLoader.djClass = this;
			trackLoader.sountToLoad = (currentSound + 1)%3;
			trackLoader.track = playlist.get(playlistPos+1);
			
			Thread t = new Thread(trackLoader);
			t.setDaemon(true);
			t.start();
			
			loadedNextTrack = true; //assume it completes
		}
			

		//dispatch triggers to the main class
		handleMusicPoints(currentSound);
		if(nextTrackPlaying)handleMusicPoints((currentSound + 1) % 3);//handle points for the next track as well

		
		//handle fading
		//handleFade((currentSound+2)%3);//previous track;
		handleFade(currentSound);//current track
		if(nextTrackPlaying)handleFade((currentSound+1)%3);//next track;
	}
	
	void handleMusicPoints(int sound)
	{
		for(int i = 0; i < musicPoints.get(sound).size(); i++)//now run triggers
		{
			int samplePos = alGetSourcei(sources[sound], AL_SAMPLE_OFFSET);
			
			if(musicPoints.get(sound).get(i).triggered == false && samplePos > musicPoints.get(sound).get(i).sampleNo)
			{
				musicPoints.get(sound).get(i).triggered = true;
				switch (musicPoints.get(sound).get(i).type)
				{
				case 3://fade out this track
					fadeInfo[sound] = musicPoints.get(sound).get(i).fadeInfo;
					fadeInfo[sound].active = true;
					break;
				case 4://fade in other track
					int nextSound = (sound + 1) %3;
					//search for it's fade in point (type = 2) and set shit up
					for (int i2 = 0; i2 < musicPoints.get(nextSound).size(); i2++)
					{
						if(musicPoints.get(nextSound).get(i2).type == 2)
						{//load its fade info in
							fadeInfo[nextSound] = musicPoints.get(nextSound).get(i2).fadeInfo;
							fadeInfo[nextSound].active = true;
							//play the track
							alSourcei(sources[nextSound], AL_SAMPLE_OFFSET,musicPoints.get(nextSound).get(i2).sampleNo);
							alSourcef(sources[nextSound], AL_GAIN, 0f);//assume the fade starts at 0;
							alSourcePlay(sources[nextSound]);// play it
							
							nextTrackPlaying = true;
							break;
						}
					}
					
					break;
				default://send to main class to handle it
					IFS.triggeredMusicPoints.add(musicPoints.get(sound).get(i));
					break;
				}
			}
		}
	}
	
	void handleFade(int sound)
	{
		int samplePos = alGetSourcei(sources[sound], AL_SAMPLE_OFFSET);
		
		if(fadeInfo[sound].active)//if theres a current fade in this track
		{//calculate fade value
			float fadeValue;
			if(fadeInfo[sound].length > 0)//if the length is valid then set up a fade
				fadeValue = (float)(samplePos - fadeInfo[sound].startSample)/(float)fadeInfo[sound].length;
			else//otherwise assume its done
				 fadeValue = 1.0f;
			
			if(fadeValue < 0) fadeValue = 0;
			else if(fadeValue >= 1 )
			{//should be finished fading if its > 1
					fadeInfo[sound].active = false;
					fadeValue = 1;
			}//at this point, fadevalue should be clamped from 0 to 1
			if(fadeInfo[sound].out)
			{
				fadeValue = 1 - fadeValue;
				
				if(fadeValue == 0)//if we just faded a track out then that means its time to switch tracks
				{
					alSourceStop(sources[sound]);
					currentSound = (currentSound+1) %3;
					
					loadedNextTrack = false;
					nextTrackPlaying = false;
				}
			}
			
			alSourcef(sources[sound], AL_GAIN, fadeValue);
		}
	}
}

