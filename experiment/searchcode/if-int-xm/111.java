package net.schattenkind.androidLove.luan.module;

import java.io.IOException;

import net.schattenkind.androidLove.LoveVM;
import net.schattenkind.androidLove.luan.LuanBase;
import net.schattenkind.androidLove.luan.LuanObjBase;
import net.schattenkind.androidLove.utils.Vector3;

import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;

public class LuanAudio extends LuanBase {
	protected static final String TAG = "LoveAudio";
	
	public static final String SOURCE_TYPE_STATIC = "static";
	public static final String SOURCE_TYPE_STREAM = "stream";
	public SoundPool mSoundPool;

	private Vector3 orientationUp = new Vector3(0.0f, 0.0f, 1.0f);
	private Vector3 orientationForward = new Vector3(1.0f, 0.0f, 0.0f);
	private Vector3 position = new Vector3();
	private Vector3 velocity = new Vector3();

	static final String sMetaName_LuanSource = "__MetaLuanSource";
	static final String sMetaName_LuanDecoder = "__MetaLuanDecoder";
	static final String sMetaName_LuanSoundData = "__MetaLuanSoundData";
	
	public static final int kAudioChannels = 4; // max number of concurrent sounds playing at the same time, SoundPool constructor
	
	public void Log (String s) { LoveVM.LoveLog(TAG, s); }
	
	
	// 0.0f - 1.0f
	private float volume = 1.0f;

	public LuanAudio(LoveVM vm) {
		super(vm);
	}

	// call this if position, velocity, ... changed
	public void notifySpatialChange() {
		// TODO
	}

	public LuaTable InitLib() {
		mSoundPool = new SoundPool(kAudioChannels,AudioManager.STREAM_MUSIC,0);
		LuaTable t = LuaValue.tableOf();
		
		LuaValue _G = vm.get_G();
		
		_G.set(sMetaName_LuanSource,LuanObjSource.CreateMetaTable(this));
		_G.set(sMetaName_LuanDecoder,LuanObjDecoder.CreateMetaTable(this));
		_G.set(sMetaName_LuanSoundData,LuanObjSoundData.CreateMetaTable(this));
		

		// numSources = love.audio.getNumSources( )
		t.set("getNumSources", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				vm.NotImplemented("love.audio.getNumSources");
				return LuaValue.ZERO;
			}
		});

		// fx, fy, fz, ux, uy, uz = love.audio.getOrientation( )
		t.set("getOrientation", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				return varargsOf(new LuaValue[] {
						LuaNumber.valueOf(orientationForward.x),
						LuaNumber.valueOf(orientationForward.y),
						LuaNumber.valueOf(orientationForward.z),
						LuaNumber.valueOf(orientationUp.x),
						LuaNumber.valueOf(orientationUp.y),
						LuaNumber.valueOf(orientationUp.z) });
			}
		});

		// x, y, z = love.audio.getPosition( )
		t.set("getPosition", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				return varargsOf(new LuaValue[] {
						LuaNumber.valueOf(position.x),
						LuaNumber.valueOf(position.y),
						LuaNumber.valueOf(position.z) });
			}
		});

		// x, y, z = love.audio.getVelocity( )
		t.set("getVelocity", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				return varargsOf(new LuaValue[] {
						LuaNumber.valueOf(velocity.x),
						LuaNumber.valueOf(velocity.y),
						LuaNumber.valueOf(velocity.z) });
			}
		});

		// volume = love.audio.getVolume( )
		t.set("getVolume", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				return varargsOf(new LuaValue[] { LuaNumber.valueOf(volume) });
			}
		});

		
		// source = love.audio.newSource( file, type )
		t.set("newSource", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				Log("love.audio.newSource params:"+
					((args.narg() >= 1)?getLuaTypeName(args.type(1)):"notset")+","+
					((args.narg() >= 2)?getLuaTypeName(args.type(2)):"notset")+","+
					((args.narg() >= 3)?getLuaTypeName(args.type(3)):"notset")+"...");
				if (args.isstring(1)) {
					Log("love.audio.newSource(string,..)");
					String sFileName = args.checkjstring(1);
					String sType = IsArgSet(args,2) ? args.checkjstring(2) : "static";
					return LuaValue.userdataOf(new LuanObjSource(LuanAudio.this,sFileName,sType),vm.get_G().get(sMetaName_LuanSource));
				}
				if (IsArgSet(args,2) && args.isstring(2)) {
					Log("love.audio.newSource(???,string,..)");
					LuanObjDecoder decoder = (LuanObjDecoder)args.checkuserdata(1,LuanObjDecoder.class);
					String sType = args.checkjstring(2);
					return LuaValue.userdataOf(new LuanObjSource(LuanAudio.this,decoder,sType),vm.get_G().get(sMetaName_LuanSource));
				}
				LuanObjSoundData soundata = (LuanObjSoundData)args.checkuserdata(1,LuanObjSoundData.class);
				return LuaValue.userdataOf(new LuanObjSource(LuanAudio.this,soundata),vm.get_G().get(sMetaName_LuanSource));
			}
		});

		// love.audio.pause( )
		t.set("pause", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				// TODO
				vm.NotImplemented("love.audio.pause");
				return LuaValue.NONE;
			}
		});

		/// love.audio.play( source )
		///Plays the specified Source. 
		t.set("play", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				LuanObjSource src = (LuanObjSource)args.checkuserdata(1,LuanObjSource.class);
				src.play();
				return LuaValue.NONE;
			}
		});

		// love.audio.resume( )
		t.set("resume", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				// TODO
				vm.NotImplemented("love.audio.resume");
				return LuaValue.NONE;
			}
		});

		// love.audio.rewind( )
		t.set("rewind", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				// TODO
				vm.NotImplemented("love.audio.rewind");
				return LuaValue.NONE;
			}
		});

		// love.audio.setOrientation( fx, fy, fz, ux, uy, uz )
		t.set("setOrientation", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				orientationForward.x = args.arg(1).tofloat();
				orientationForward.y = args.arg(2).tofloat();
				orientationForward.z = args.arg(3).tofloat();

				orientationUp.x = args.arg(4).tofloat();
				orientationUp.y = args.arg(5).tofloat();
				orientationUp.z = args.arg(6).tofloat();

				notifySpatialChange();

				return LuaValue.NONE;
			}
		});

		// love.audio.setPosition( x, y, z )
		t.set("setPosition", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				position.x = args.arg(1).tofloat();
				position.y = args.arg(2).tofloat();
				position.z = args.arg(3).tofloat();

				notifySpatialChange();

				return LuaValue.NONE;
			}
		});

		// love.audio.setVelocity( x, y, z )
		t.set("setVelocity", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				position.x = args.arg(1).tofloat();
				position.y = args.arg(2).tofloat();
				position.z = args.arg(3).tofloat();

				notifySpatialChange();

				return LuaValue.NONE;
			}
		});

		// love.audio.setVolume( volume )
		t.set("setVolume", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				volume = args.arg(1).tofloat();

				// TODO
				vm.NotImplemented("love.audio.setVolume");

				return LuaValue.NONE;
			}
		});

		// love.audio.stop( )
		t.set("stop", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				// TODO
				vm.NotImplemented("love.audio.stop");
				return LuaValue.NONE;
			}
		});

		return t;
	}

	
	// ***** ***** ***** ***** *****  LuanSoundData
	
	public static class LuanObjSoundData extends LuanObjBase {
		public LuanObjSoundData(LoveVM vm) {
			super(vm);
		}

		public static LuaTable CreateMetaTable (final LuanAudio audio) {
			LuaTable mt = LuaValue.tableOf();
			LuaTable t = LuaValue.tableOf();
			mt.set("__index",t);
			return mt;
		}
	}
		
	// ***** ***** ***** ***** *****  LuanDecoder
		
	public static class LuanObjDecoder extends LuanObjBase {
		public LuanObjDecoder(LoveVM vm) {
			super(vm);
		}

		public static LuaTable CreateMetaTable (final LuanAudio audio) {
			LuaTable mt = LuaValue.tableOf();
			LuaTable t = LuaValue.tableOf();
			mt.set("__index",t);
			return mt;
		}
	}
		
	// ***** ***** ***** ***** *****  LuanSource
	
	public static class LuanObjSource extends LuanObjBase {
		protected static final String TAG = "LoveSource";
		
		private LuanAudio	audio;
		private String filename;
		public int miSoundID = 0;
		public boolean bMusic = false;
		public MediaPlayer mp;
		
		/// load from resource without sdcard access
		public LuanObjSource (LuanAudio audio,int iResID,String type) { 
			super(audio.vm);
			this.audio = audio;
			this.filename = "res:"+iResID; // debug output only?
			int iPriority = 0; // determines which sound gets halted if there's not enough channels
			if (type == "stream" || 
				filename.toLowerCase().endsWith("mp3") || 
				filename.toLowerCase().endsWith("ogg") || 
				filename.toLowerCase().endsWith("xm"))  // NOTE: clouds demo has xm(tracker music), but fails to load
				bMusic = true;
			
			LoveVM.LoveLog(TAG,"constructor filename="+filename+" type="+type+" bMusic="+bMusic);
			
			if (bMusic) {
				mp = MediaPlayer.create(audio.vm.getActivity(), iResID );
			} else {
				miSoundID = audio.mSoundPool.load(audio.vm.getActivity(),iResID,iPriority);
			}
		}
		
		/// load from file
		public LuanObjSource (LuanAudio audio,String filename,String type) { 
			super(audio.vm);
			this.audio = audio;
			this.filename = filename;
			int iPriority = 0; // determines which sound gets halted if there's not enough channels
			if (type == "stream" || 
				filename.toLowerCase().endsWith("mp3") ||
				filename.toLowerCase().endsWith("ogg") ||
				filename.toLowerCase().endsWith("xm"))  // NOTE: clouds demo has xm(tracker music), but fails to load
				bMusic = true;
			// TODO: only if stream ? might work for short sounds, didn't try, 
			// would need autodetect to decide if mSoundPool can work 
			// otherwise we'd loose a lot of compatibilty with existing love games not aware of android/soundbuf
			
			LoveVM.LoveLog(TAG,"constructor filename="+filename+" type="+type+" bMusic="+bMusic);
			
			try {
				if (bMusic) {
					// NOTE : 	MediaPlayer.create(Context context, int resid)
					// NOTE : 	MediaPlayer.create(Context context, Uri uri)
					// note : http://blog.endpoint.com/2011/03/api-gaps-android-mediaplayer-example.html
					// note : http://stackoverflow.com/questions/2458833/how-do-i-get-a-wav-sound-to-play-android
					// http://www.helloandroid.com/taxonomy/term/14
					// NOTE : tracker files like .xm in clouds demo : http://stackoverflow.com/questions/5597624/how-to-play-tracker-modules-on-android
					mp = MediaPlayer.create(audio.vm.getActivity(), Uri.fromFile(audio.vm.getStorage().forceGetFileFromLovePath(filename)) );
				} else {
					// TODO : load from zip ?  	load(AssetFileDescriptor afd, int priority)
					// http://developer.android.com/reference/android/content/res/AssetManager.html
					miSoundID = audio.mSoundPool.load(audio.vm.getStorage().forceGetFilePathFromLovePath(filename),iPriority);
				}
			} catch (IOException e) {
				LoveVM.LoveLogE(TAG,"constructor failed",e);
			}
		}
		
		/// load from LuanDecoder
		public LuanObjSource (LuanAudio audio,LuanObjDecoder decoder,String type) { super(audio.vm); this.audio = audio; audio.vm.NotImplemented("AudioSource: construct from Decoder"); } // TODO
		
		/// load from LuanSoundData
		public LuanObjSource (LuanAudio audio,LuanObjSoundData data) { super(audio.vm); this.audio = audio; audio.vm.NotImplemented("AudioSource: construct from SoundData"); } // TODO
			
	
		/// start
		public void play () {
			LoveVM.LoveLog(TAG,"play filename="+filename+" miSoundID="+miSoundID+" bMusic="+bMusic);
			if (bMusic) {
				if (mp != null) {
					mp.seekTo(0); // back to start
					mp.start();
				}
			} else if (miSoundID != 0) {
				float fLeftVol = 1f;
				float fRightVol = 1f;
				int iPrio = 0;
				int iLoopMode = 0;
				float fRate = 1f;
				audio.mSoundPool.play(miSoundID,fLeftVol,fRightVol,iPrio,iLoopMode,fRate);
			}
		}
		
		public static LuanObjSource self (Varargs args) { return (LuanObjSource)args.checkuserdata(1,LuanObjSource.class); }
		
		public static LuaTable CreateMetaTable (final LuanAudio audio) {
			LuaTable mt = LuaValue.tableOf();
			LuaTable t = LuaValue.tableOf();
			mt.set("__index",t);
			
			t.set("getDirection",	new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"getDirection"	);	return LuaValue.NONE; } });	
			t.set("getPitch",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"getPitch"		);	return LuaValue.NONE; } });	
			t.set("getPosition",	new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"getPosition"	);	return LuaValue.NONE; } });	
			t.set("getVelocity",	new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"getVelocity"	);	return LuaValue.NONE; } });	
			t.set("getVolume",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"getVolume"		);	return LuaValue.NONE; } });	
			t.set("isLooping",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"isLooping"		);	return LuaValue.NONE; } });	
			t.set("isPaused",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"isPaused"		);	return LuaValue.NONE; } });		
			t.set("isStatic",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"isStatic"		);	return LuaValue.NONE; } });		
			t.set("isStopped",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"isStopped"		);	return LuaValue.NONE; } });	
			t.set("pause",			new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"pause"			);	return LuaValue.NONE; } });	
			t.set("resume",			new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"resume"		);	return LuaValue.NONE; } });	
			t.set("rewind",			new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"rewind"		);	return LuaValue.NONE; } });	
			t.set("setDirection",	new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"setDirection"	);	return LuaValue.NONE; } });		
			t.set("setLooping",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"setLooping"	);	return LuaValue.NONE; } });	
			t.set("setPitch",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"setPitch"		);	return LuaValue.NONE; } });		
			t.set("setPosition",	new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"setPosition"	);	return LuaValue.NONE; } });		
			t.set("setVelocity",	new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"setVelocity"	);	return LuaValue.NONE; } });		
			t.set("setVolume",		new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"setVolume"		);	return LuaValue.NONE; } });	
			t.set("stop",			new VarArgFunction() { @Override public Varargs invoke(Varargs args) { audio.vm.NotImplemented("AudioSource:"+"stop"			);	return LuaValue.NONE; } });		
			
			/// Source:play()
			/// Starts playing the Source. 
			t.set("play",			new VarArgFunction() { @Override public Varargs invoke(Varargs args) { self(args).play(); return LuaValue.NONE; } });		
			
			
			/// type = Object:type()  , e.g. "Image" or audio:"Source"
			t.set("type", new VarArgFunction() { @Override public Varargs invoke(Varargs args) { return LuaValue.valueOf("Source"); } });
			
			/// b = Object:typeOf( name )
			t.set("typeOf", new VarArgFunction() { @Override public Varargs invoke(Varargs args) { 
				String s = args.checkjstring(2); 
				return LuaValue.valueOf(s.equals("Object") || s.equals("Source")); 
			} });
			
			
			return mt;
		}
	}
	
}

