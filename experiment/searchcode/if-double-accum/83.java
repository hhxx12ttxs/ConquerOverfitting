package com.shkoo.aprslib;

import java.nio.ByteBuffer;

public class AFSKDemod {
	static double multiplyAndAdd(short[] a1, int i1, double[] a2, int i2,
			int count) {
		double accum = 0;

		while (count-- > 0)
			accum += a1[i1++] * a2[i2++];
		return accum;
	}

	FrameReceiver target;

	int sampleFreq = 44100;
	int symbolFreq = 1200;
	int spaceFreq = 2200;

	int markFreq = 1200;

	int samplesPerSymbol;
	int subsampleIndex = 4;

	int receivePhaseSpeed = 0x10000 * symbolFreq * subsampleIndex / sampleFreq;

	double[] markTableReal, markTableImag, spaceTableReal, spaceTableImag;
	static final boolean debug = false;
	static final boolean debugCandidateFrames = true;

	static final boolean debugBits = false;

	ByteBuffer receiveFrame = ByteBuffer.allocate(1024);

	boolean potentialFrame = false;

	int rawReceivedBits;

	int unstuffedReceivedBits;

	boolean lastMarkReceived;

	boolean lastMarkish;

	int phaseSync = 0;
	int subsampleCount = 0;
	short[] leftoverSamples;

	public AFSKDemod() {
		initialize();
	}

	public AFSKDemod(int newSampleFreq) {
		sampleFreq = newSampleFreq;
		initialize();
	}

	void initialize() {
		samplesPerSymbol = sampleFreq / symbolFreq;

		markTableReal = new double[samplesPerSymbol];
		markTableImag = new double[samplesPerSymbol];
		spaceTableReal = new double[samplesPerSymbol];
		spaceTableImag = new double[samplesPerSymbol];

		double radians = 0;
		for (int i = 0; i < samplesPerSymbol; i++) {
			markTableReal[i] = Math.sin(radians);
			markTableImag[i] = Math.cos(radians);
			radians += 2 * Math.PI * markFreq / sampleFreq;
		}

		for (int i = 0; i < samplesPerSymbol; i++) {
			spaceTableReal[i] = Math.sin(radians);
			spaceTableImag[i] = Math.cos(radians);
			radians += 2 * Math.PI * spaceFreq / sampleFreq;
		}
		if (debug)
			System.err.println("Receive phase speed: " + receivePhaseSpeed
					+ " phase sync threshold: "
					+ (0x8000 - (receivePhaseSpeed / 2)));
	}

	void processCandidateFrame(ByteBuffer frameBuffer) {
		if (debugCandidateFrames)
			System.out.println("Processing candidate frame of length "
					+ frameBuffer.position());
		if (frameBuffer.position() < 18) {
			if (debug)
				System.err.println("Too short");
			return; // too short. two callsigns(7 each) + control(1) +
			// protocol(1) + checksum(2)
		}
		int framesize = frameBuffer.position() - 2;

		byte[] frame = new byte[framesize];
		frameBuffer.position(0);
		frameBuffer.get(frame, 0, framesize);

		if (debugCandidateFrames) {
			for (byte element : frame) {
				char c = (char) element;
				if (c >= 127 || c < 32)
					System.out.print('.');
				else
					System.out.print(c);
			}
			System.out.print('\n');
			for (byte element : frame) {
				char c = (char) ((element & 0xFF) >> 1);
				if (c > 127 || c < 32)
					System.out.print('.');
				else
					System.out.print(c);
			}
			System.out.print('\n');
		}

		byte[] crc = HDLC.calcCRC(frame, 0, frame.length);
		// check for crc failure
		byte c = frameBuffer.get();
		if (c != crc[0]) {
			if (debugCandidateFrames)
				System.err.println("First byte of crc failed: " + ((int) c)
						+ " != " + ((int) crc[0]));
			return;
		}
		c = frameBuffer.get();
		if (c != crc[1]) {
			if (debugCandidateFrames)
				System.err.println("Second byte of crc failed: " + ((int) c)
						+ " != " + ((int) crc[1]));
			return;
		}
		target.onFrameReceived(frame);
	}

	public void processSamples(short[] samples, int nsamples) {
		if (nsamples < samplesPerSymbol)
			throw new IllegalArgumentException("Most process at least "
					+ samplesPerSymbol + " samples at once");
		if (leftoverSamples == null)
			leftoverSamples = new short[samplesPerSymbol * 2];
		else {
			System.arraycopy(samples, 0, leftoverSamples, samplesPerSymbol,
					samplesPerSymbol);
			processSampleSection(leftoverSamples);
		}
		processSampleSection(samples);
		System.arraycopy(samples, nsamples - samplesPerSymbol, leftoverSamples,
				0, samplesPerSymbol);
	}

	public void processSampleSection(short[] sampleSection) {
		for (int i = 0; i < sampleSection.length - samplesPerSymbol; i++) {
			subsampleCount++;
			if (subsampleCount == subsampleIndex)
				subsampleCount = 0;
			else
				continue;

			double markRealCorrelation = multiplyAndAdd(sampleSection, i,
					markTableReal, 0, samplesPerSymbol);
			double markImagCorrelation = multiplyAndAdd(sampleSection, i,
					markTableImag, 0, samplesPerSymbol);

			double spaceRealCorrelation = multiplyAndAdd(sampleSection, i,
					spaceTableReal, 0, samplesPerSymbol);
			double spaceImagCorrelation = multiplyAndAdd(sampleSection, i,
					spaceTableImag, 0, samplesPerSymbol);

			double markiness = 0 + markRealCorrelation * markRealCorrelation
					+ markImagCorrelation * markImagCorrelation
					- spaceRealCorrelation * spaceRealCorrelation
					- spaceImagCorrelation * spaceImagCorrelation;
			boolean currentlyMarkish = markiness > 0;
			if (debug)
				if (currentlyMarkish)
					System.err.print("1");
				else
					System.err.print("0");

			if (currentlyMarkish != lastMarkish) {
				// synchronize
				lastMarkish = currentlyMarkish;
				if ((phaseSync + (receivePhaseSpeed / 2)) < 0x8000) {
					if (debug)
						System.err.print("+");
					phaseSync += receivePhaseSpeed / 8;
				} else {
					if (debug)
						System.err.print("-");
					phaseSync -= receivePhaseSpeed / 8;
				}
			}
			phaseSync += receivePhaseSpeed;
			if (phaseSync >= 0x10000) {
				phaseSync &= 0x0ffff;
				if (lastMarkReceived == currentlyMarkish)
					// no transition -- receive 1 bit
					receiveBit(true);
				else {
					// transition -- receive 0 bit
					receiveBit(false);
					lastMarkReceived = currentlyMarkish;
				}
			}
		}
	}

	void receiveBit(boolean high) {
		if (debugBits)
			if (high)
				System.err.print(" 1 ");
			else
				System.err.print(" 0 ");

		rawReceivedBits <<= 1;
		if (high)
			rawReceivedBits |= 1;

		if ((rawReceivedBits & 0xFF) == 0x7E) {
			// flag pattern, before and after a frame
			if (potentialFrame && receiveFrame.position() >= 16)
				processCandidateFrame(receiveFrame);
			resetReceiveFrame();
			potentialFrame = true;
			return;
		}

		if (!potentialFrame)
			return;

		if ((rawReceivedBits & 0x7f) == 0x7f) {
			// 7 ones in a row; violation of bit stuffing rules
			resetReceiveFrame();
			return;
		}

		if ((rawReceivedBits & 0x3f) == 0x3e)
			// ignore stuffed bit
			return;

		if (high)
			unstuffedReceivedBits |= 0x100;

		if ((unstuffedReceivedBits & 1) != 0) {
			unstuffedReceivedBits >>= 1;
			if (debugBits) {
				char c = (char) unstuffedReceivedBits;
				if (!Character.isISOControl(c))
					System.out.print("'" + c + "'");
				final String hex = "0123456789ABCDEF";
				System.out.print("(" + hex.charAt((c >> 4) & 0xF)
						+ hex.charAt(c & 0xF) + ")");
			}
			receiveFrame.put((byte) unstuffedReceivedBits);
			if (receiveFrame.position() == receiveFrame.capacity())
				// packet too large
				resetReceiveFrame();
			unstuffedReceivedBits = 0x80;
		} else
			unstuffedReceivedBits >>= 1;
	}

	void resetReceiveFrame() {
		receiveFrame.position(0);
		potentialFrame = false;
		rawReceivedBits = 0;
		unstuffedReceivedBits = 0x80;
	}

	public void setFrameReceiver(FrameReceiver newTarget) {
		target = newTarget;
	}

	public void setPacketReceiver(final PacketReceiver newTarget) {
		target = new FrameReceiver() {
			public void onFrameReceived(byte[] frame) {
				AprsPacket packet = AprsPacket.decode(frame);
				if (packet != null)
					newTarget.onPacketReceived(packet);
			}
		};
	}
};
