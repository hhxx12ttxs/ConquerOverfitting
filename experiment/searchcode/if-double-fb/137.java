package com.hf.soundSmartLink;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class SoundFileBuilder {
	// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
	private static int sampleRateInHz = 44100;
	// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
	// 缓冲区字节大小
	private int bufferSizeInBytes = 0;

	// AudioName裸音频数据文件
	private static final String AudioName = "/sdcard/demon.raw";
	// NewAudioName可播放的音频文件
	private static final String NewAudioName = "/sdcard/demon_new.wav";

	private static final String VoiceFileName = "/sdcard/voice.wav";
	private static byte[] voiceBytes = new byte[1024 * 1024 * 2];

	private String mSsidAndPswd;
	
	private String ssid;
	private String pswd;
	

	private final double PI = 3.1415926535897932384626433832795028841971;

	private final int FRAME_SAP = 44100;

	private boolean loop = false;
	
	private Context ctx;
	
	private MyAudioTrack adtc;
	private AudioTrack tigAduioTrack;
	private ArrayList<SoundBuilderSuccess> sbsls = new ArrayList<SoundFileBuilder.SoundBuilderSuccess>();

	public SoundFileBuilder(String ssid, String pswd) {
		// TODO Auto-generated constructor stub
		this.mSsidAndPswd = ssid + "\r" + pswd;
	}

	public SoundFileBuilder(Context ctx) {
		this.ctx = ctx;
	}

	public void setmSsidAndPswd(String ssid, String pswd) {
		this.mSsidAndPswd = ssid + "\r" + pswd;
		this.ssid = ssid;
		this.pswd = pswd;
	}

	public void start() {
		loop = true;
		new Thread(new AudioRecordThread()).start();
//		new Thread(new AudioRecordThreadtig()).start();
	}

	public void stop() {
		if (adtc != null) {
			adtc.release();
			loop = false;
		}
		if (tigAduioTrack != null) {
			tigAduioTrack.release();
			loop = false;
		}
	}

	class AudioRecordThread implements Runnable {

		@Override
		public void run() {
			// writeDateTOFile();// 往文件中写入裸数据
			// copyWaveFile(AudioName, NewAudioName);// 给裸数据加上头文件
			File voiceFile = new File(VoiceFileName);
			try {
				FileInputStream in = new FileInputStream(voiceFile);
				int num = in.read(voiceBytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			adtc = new MyAudioTrack(FRAME_SAP, AudioFormat.CHANNEL_OUT_STEREO,
					AudioFormat.ENCODING_PCM_16BIT);
			adtc.init();
			AudioEncoder fb = new AudioEncoder(ssid,pswd);
			double[] mm = fb.getDouble();
			ByteBuffer bf = ByteBuffer.allocate(mm.length * 4);
			File out = new File(NewAudioName);
			FileOutputStream ous;
			
			for (int i = 0, j = 0; i < mm.length; i++, j += 4) {
//				byte[] tmpb = new byte[2];
//				tmpb[0] = voiceBytes[44+j];
//				tmpb[1] = voiceBytes[45+j];
//				short tmpshort = byteToShort(tmpb);
				
				short tm = (short) (32767 * mm[i]);
				//tm += tmpshort/10;
				// System.out.printf(tm+",");
					bf.put(shortToByte(tm));
					bf.put(shortToByte(tm));
			}
			
			int i = 6;
			while (loop) {
				ByteBuffer threebf = ByteBuffer.allocate(bf.capacity()*4);
				threebf.put(bf.array());
				threebf.put(bf.array());
				threebf.put(bf.array());
				threebf.put(bf.array());
				adtc.playAudioTrack(threebf.array(), 0, threebf.array().length);
				
//				AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
//				
//				if(i%3==0){
//					int vol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//					am.setStreamVolume(AudioManager.STREAM_MUSIC, vol*i/30, -1);
//
//				}
//				i++;
//				if(i==24){
//					i=6;
//				}
				
			}
			/*
			 * writeDateToFile(bf.array()); try { if(out.exists()){
			 * out.delete(); } ous = new FileOutputStream(out);
			 * WriteWaveFileHeader(ous,
			 * bf.array().length,bf.array().length+36,sampleRateInHz, 2, 16 *
			 * sampleRateInHz * 2/8); ous.write(bf.array()); ous.close(); }
			 * catch (FileNotFoundException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); } catch (IOException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); } try{
			 * FileInputStream in = new FileInputStream(new File(NewAudioName));
			 * byte[] hh = new byte[1024*1024*2]; int num = in.read(hh);
			 * adtc.playAudioTrack(hh, 44, num); adtc.release();
			 * }catch(IOException e){
			 * 
			 * }
			 */
			notifyBuilderSuccess();
		}

		public byte[] shortToByte(short number) {
			
			int temp = number;
			byte[] b = new byte[2];
			for (int i = 0; i < b.length; i++) {
				b[i] = new Integer(temp & 0xff).byteValue();//
				// 将最低位保存在最低位
				temp = temp >> 8; // 向右移8位
			}
			return b;
		}
	}

	interface SoundBuilderSuccess {
		public void onSuccess(String fileName);
	}

	public void registSoundBuilderEvent(SoundBuilderSuccess sss) {
		sbsls.add(sss);
	}

	private void notifyBuilderSuccess() {
		ArrayList<SoundBuilderSuccess> tmp = sbsls;
		Iterator<SoundBuilderSuccess> it = tmp.iterator();
		while (it.hasNext()) {
			it.next().onSuccess(NewAudioName);
		}
	}

	/**
	 * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
	 * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
	 * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
	 */

	private void writeDateToFile(byte[] data) {
		FileOutputStream fos = null;
		try {
			File file = new File(AudioName);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);// 建立一个可存取字节的文件
			fos.write(data);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeDateTOFile() {
		// new一个byte数组用来存一些字节数据，大小为缓冲区大小
		byte[] audiodata = new byte[bufferSizeInBytes];
		FileOutputStream fos = null;
		int readsize = 0;
		try {
			File file = new File(AudioName);
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);// 建立一个可存取字节的文件
		} catch (Exception e) {
			e.printStackTrace();
		}
		AudioEncoder fb = new AudioEncoder("","");
		double[] mm = fb.getDouble();

		for (int i = 0; i < mm.length; i++) {
			try {
				WriteShort(fos, (short) (32767 * mm[i]));
				WriteShort(fos, (short) (32767 * mm[i]));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fos.close();// 关闭写入流
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 这里得到可播放的音频文件
	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = sampleRateInHz;
		int channels = 2;
		long byteRate = 16 * sampleRateInHz * channels / 8;
		byte[] data = new byte[bufferSizeInBytes];
		try {
			File file = new File(AudioName);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);

			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			while (in.read(data) != -1) {
				out.write(data);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double[] getShortDatas(byte[] data) {
		double[] signal_real = new double[data.length * 8 * 256];
		

		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < 8; j++) {
				for (int n = 0; n < 256; n++) {
					int m = (i * 8 + j) * 256 + n;
					signal_real[m] = 0.2 * Math.cos(2 * PI * 18604.7 * m
							/ FRAME_SAP);
					if (getBit(data[i], j)) {
						signal_real[m] += 0.35 * Math.cos(2 * PI * 18949.2 * m
								/ FRAME_SAP);
					} else {
						signal_real[m] += 0.35 * Math.cos(2 * PI * 19293.8 * m
								/ FRAME_SAP);
					}
				}
			}
		}
		return signal_real;
	}

	private boolean getBit(byte b, int i) {
		// 0101 0101
		// 7654 3210
		byte tmp = 1;
		return (b & (tmp << i)) != 0;
	}
	
	/**
	 * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
	 * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有 自己特有的头文件。
	 */
	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {
		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}

	private void WriteShort(FileOutputStream bos, Short s) throws IOException {
		byte[] mybyte = new byte[2];
		mybyte[1] = (byte) ((s << 16) >> 24);
		mybyte[0] = (byte) ((s << 24) >> 24);
		bos.write(mybyte);
	}
	
	public static short byteToShort(byte[] b) { 
        short s = 0; 
        short s0 = (short) (b[0] & 0xff);// 最低位 
        short s1 = (short) (b[1] & 0xff); 
        s1 <<= 8; 
        s = (short) (s0 | s1); 
        return s; 
    }
}

