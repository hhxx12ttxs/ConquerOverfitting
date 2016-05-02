/****************************************************************************
*
* NAME: smbPitchShift.cpp
* VERSION: 1.2
* HOME URL: http://www.dspdimension.com
* KNOWN BUGS: none
*
* SYNOPSIS: Routine for doing pitch shifting while maintaining
* duration using the Short Time Fourier Transform.
*
* DESCRIPTION: The routine takes a pitchShift factor value which is
* between 0.5 (one octave down) and 2. (one octave up). A value of
* exactly 1 does not change the pitch. numSampsToProcess tells the
* routine how many samples in indata[0... numSampsToProcess-1] should
* be pitch shifted and moved to outdata[0 ... numSampsToProcess-1].
* The two buffers can be identical (ie. it can process the data
* in-place). fftFrameSize defines the FFT frame size used for the
* processing. Typical values are 1024, 2048 and 4096. It may be any
* value <= MAX_FRAME_LENGTH but it MUST be a power of 2. osamp is the
* STFT oversampling factor which also determines the overlap between
* adjacent STFT frames. It should at least be 4 for moderate scaling
* ratios. A value of 32 is recommended for best quality. sampleRate
* takes the sample rate for the signal in unit Hz, ie. 44100 for 44.1
* kHz audio. The data passed to the routine in indata[] should be in
* the range [-1.0, 1.0), which is also the output range for the data,
* make sure you scale the data accordingly (for 16bit signed integers
* you would have to divide (and multiply) by 32768).
*
* COPYRIGHT 1999-2006 Stephan M. Bernsee <smb [AT] dspdimension [DOT] com>
*
* 						The Wide Open License (WOL)
*
* Permission to use, copy, modify, distribute and sell this software and its
* documentation for any purpose is hereby granted without fee, provided that
* the above copyright notice and this license appear in all source copies. 
* THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY OF
* ANY KIND. See http://www.dspguru.com/wol.htm for more information.
*
*****************************************************************************/ 

import java.util.Arrays;

class Transform {

  final int fftFrameSize;
  final double gInFIFO[];
  final double gOutFIFO[];
  final double gFFTworkspR[];
  final double gFFTworkspI[];
  final double gLastPhase[];
  final double gSumPhase[];
  final double gOutputAccum[];
  final double gAnaFreq[];
  final double gAnaMagn[];
  final double gSynFreq[];
  final double gSynMagn[];
  int gRover = 0;

  Transform() { this(2048); }

  Transform(int fftFrameSize) {  
    // todo: throw err if not, 2^n e.g. 1024,2048,4096
    this.fftFrameSize = fftFrameSize;
    gInFIFO = new double[fftFrameSize];
    gOutFIFO = new double[fftFrameSize];
    gFFTworkspR = new double[fftFrameSize];
    gFFTworkspI = new double[fftFrameSize];
    gLastPhase = new double[fftFrameSize / 2 + 1];
    gSumPhase = new double[fftFrameSize / 2 + 1];
    gOutputAccum = new double[fftFrameSize * 2];
    gAnaFreq = new double[fftFrameSize];
    gAnaMagn = new double[fftFrameSize];
    gSynFreq = new double[fftFrameSize];
    gSynMagn = new double[fftFrameSize];
  }
  
  /*
    Routine smbPitchShift(). See top of file for explanation
    Purpose: doing pitch shifting while maintaining duration using the Short
    Time Fourier Transform.
    Author: (c)1999-2006 Stephan M. Bernsee <smb [AT] dspdimension [DOT] com>
  */
  double[] shiftPitch(double pitchShift, 
                      int osamp,   // 4 to 32 (best)
                      double sampleRate,  // Hz
                      double[] indata) {
    double[] outdata = new double[indata.length];

    // set up some handy variables
    final int fftFrameSize2 = fftFrameSize / 2;
    final int stepSize = fftFrameSize / osamp;
    final double freqPerBin = (double)sampleRate / fftFrameSize;
    final double expct = 2.0 * Math.PI * stepSize / fftFrameSize;
    final int inFifoLatency = fftFrameSize - stepSize;
    if (gRover == 0) gRover = inFifoLatency;
    
    // main processing loop
    for (int i = 0; i < indata.length; i++){

      // collect data
      gInFIFO[gRover] = indata[i];
      outdata[i] = gOutFIFO[gRover-inFifoLatency];
      ++gRover;
      
      // now we have enough data for processing
      if (gRover >= fftFrameSize) {
        gRover = inFifoLatency;
        
        // do windowing
        for (int k = 0; k < fftFrameSize; ++k) {
          final double window = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * 
                                                     k / fftFrameSize);
          gFFTworkspR[k] = gInFIFO[k] * window;
          gFFTworkspI[k] = 0.0;
        }
        
        
        // ANALYSIS

        smbFft(-1, fftFrameSize, gFFTworkspR, gFFTworkspI);
        
        // this is the analysis step
        for (int k = 0; k <= fftFrameSize2; k++) {
          
          // de-interlace FFT buffer
          final double real = gFFTworkspR[k];
          final double imag = gFFTworkspI[k];
          
          // compute magnitude and phase
          final double magn = 2.0 * Math.sqrt(real*real + imag*imag);
          final double phase = Math.atan2(imag, real);
          
          // compute phase difference 
          double tmp = phase - gLastPhase[k];
          gLastPhase[k] = phase;
          
          // subtract expected phase difference
          tmp -= k * expct;
          
          // map delta phase into +/- Pi interval
          int qpd = (int) (tmp / Math.PI);
          if (qpd >= 0) qpd += qpd & 1;
          else qpd -= qpd & 1;
          tmp -= Math.PI * qpd;
          
          // get deviation from bin frequency from the +/- Pi interval
          tmp = osamp * tmp / (2.*Math.PI);
          
          // compute the k-th partials' true frequency
          tmp = k * freqPerBin + tmp * freqPerBin;
          
          // store magnitude and true frequency in analysis arrays
          gAnaMagn[k] = magn;
          gAnaFreq[k] = tmp;
        }
        
        // PROCESSING
        // this does the actual pitch shifting
        Arrays.fill(gSynMagn, 0, fftFrameSize, 0);
        Arrays.fill(gSynFreq, 0, fftFrameSize, 0);
        for (int k = 0; k <= fftFrameSize2; k++) { 
          final int index = (int)(k * pitchShift);
          if (index <= fftFrameSize2) { 
            gSynMagn[index] += gAnaMagn[k]; 
            gSynFreq[index] = gAnaFreq[k] * pitchShift; 
          } 
        }
        
        // SYNTHESIS 
        // this is the synthesis step
        for (int k = 0; k <= fftFrameSize2; k++) {
          
          // get magnitude and true frequency from synthesis arrays
          final double magn = gSynMagn[k];
          double tmp = gSynFreq[k];
          
          // subtract bin mid frequency
          tmp -= k * freqPerBin;
          
          // get bin deviation from freq deviation 
          tmp /= freqPerBin;
          
          // take osamp into account 
          tmp = 2.0 * Math.PI * tmp / osamp;
          
          // add the overlap phase advance back in
          tmp += k * expct;
          
          // accumulate delta phase to get bin phase
          gSumPhase[k] += tmp;
          
          /* get real and imag part and re-interleave */
          gFFTworkspR[k] = magn * Math.cos(gSumPhase[k]);
          gFFTworkspI[k] = magn * Math.sin(gSumPhase[k]);
        } 
        
        // zero negative frequencies (?)
        Arrays.fill(gFFTworkspR, fftFrameSize2, fftFrameSize, 0);
        Arrays.fill(gFFTworkspI, fftFrameSize2, fftFrameSize, 0);
        
        // do inverse transform
        smbFft(1, fftFrameSize, gFFTworkspR, gFFTworkspI);
        
        // do windowing and add to output accumulator
        for(int k = 0; k < fftFrameSize; k++) {
          final double window = 
            0.5 - 0.5 * Math.cos(2.0 * Math.PI *  k / fftFrameSize);
          gOutputAccum[k] += 
            2.0 * window * gFFTworkspR[k] / (fftFrameSize2 * osamp);
        }
        System.arraycopy(gOutputAccum, 0, gOutFIFO, 0, stepSize);
        
        // shift accumulator
        System.arraycopy(gOutputAccum, stepSize, gOutputAccum, 0,fftFrameSize);
        
        // move input FIFO
        System.arraycopy(gInFIFO, 0, gInFIFO, stepSize, inFifoLatency);
      }
    }
    return outdata;
  }
  

  public static void fft(int sign, int n, double ar[], double ai[]) {
    double scale = Math.sqrt(1.0 / n);
    
    for (int i = 0, j = 0; i < n; ++i) {
      if (j >= i) {
        final double tempr = ar[j] * scale;
        final double tempi = ai[j] * scale;
        ar[j] = ar[i] * scale;
        ai[j] = ai[i] * scale;
        ar[i] = tempr;
        ai[i] = tempi;
      }
      int m = n/2;
      while (m >= 1 && j >= m) {
        j -= m;
        m /= 2;
      }
      j += m;
    }
    
    for (int mmax = 1, istep = 2 * mmax; 
         mmax < n; 
         mmax = istep, istep = 2 * mmax) {
      final double delta = sign * Math.PI / mmax;
      for (int m = 0; m < mmax; ++m) {
        final double w = m * delta;
        final double wr = Math.cos(w);
        final double wi = Math.sin(w);
        for (int i = m; i < n; i += istep) {
          final int j = i + mmax;
          final double tr = wr * ar[j] - wi * ai[j];
          final double ti = wr * ai[j] + wi * ar[j];
          ar[j] = ar[i] - tr;
          ai[j] = ai[i] - ti;
          ar[i] += tr;
          ai[i] += ti;
        }
      }
      mmax = istep;
    }
  }


  /* 
  FFT routine, (C)1996 S.M.Bernsee. Sign = -1 is FFT, 1 is iFFT (inverse)
  Fills fftBuffer[0...2*fftFrameSize-1] with the Fourier transform of the
  time domain data in fftBuffer[0...2*fftFrameSize-1]. The FFT array takes
  and returns the cosine and sine parts in an interleaved manner, ie.
  fftBuffer[0] = cosPart[0], fftBuffer[1] = sinPart[0], asf. fftFrameSize
  must be a power of 2. It expects a complex input signal (see footnote 2),
  ie. when working with 'common' audio signals our input signal has to be
  passed as {in[0],0.,in[1],0.,in[2],0.,...} asf. In that case, the transform
  of the frequencies of interest is in fftBuffer[0...fftFrameSize].
  */
  static void smbFft(int sign, int len, double real[], double imag[]) {
    // Apparently, swap ith value with jth, where j is i in bitwise
    // reverse order
    for (int i = 1; i < len - 1; ++i) {
      int j = 0;
      for (int bitmask = 1; bitmask < len/2; bitmask <<= 1) {
        if ((i & bitmask) != 0) ++j;
        j <<= 1;
      }
      if (i < j) {
        double temp = real[i]; 
        real[i] = real[j];
        real[j] = temp; 
        temp = imag[i];
        imag[i] = imag[j];
        imag[j] = temp;
      }
    }
    for (int k = 0, le = 1;
         k < (int)(Math.log(len)/Math.log(2.) + 0.5); 
         ++k) {
      final int le2 = le;
      le <<= 1;
      double ur = 1.0;
      double ui = 0.0;
      double arg = Math.PI / (le2 >> 1);
      double wr = Math.cos(arg);
      double wi = sign * Math.sin(arg);
      for (int j = 0; j < le2; ++j) {
        int p1 = j;
        int p2 = p1 + le2; 
        for (int i = j; i < len; i += le) {
          double tr = real[p2] * ur - imag[p2] * ui;
          double ti = real[p2] * ui + imag[p2] * ur;
          real[p2] = real[p1] - tr; 
          imag[p2] = imag[p1] - ti;
          real[p1] += tr; 
          imag[p1] += ti;
          p1 += le; 
          p2 += le; 
        }
        final double tmp = ur * wr - ui * wi;
        ui = ur * wi + ui * wr;
        ur = tmp;
      }
    }
  }

}

