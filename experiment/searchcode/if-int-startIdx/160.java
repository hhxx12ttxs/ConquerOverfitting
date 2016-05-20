/* TA-LIB Copyright (c) 1999-2007, Mario Fortier
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * - Neither name of author nor the names of its contributors
 *   may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/* List of contributors:
 *
 *  Initial  Name/description
 *  -------------------------------------------------------------------
 *  MF       Mario Fortier
 *  BT       Barry Tsung
 *
 * Change history:
 *
 *  MMDDYY BY     Description
 *  -------------------------------------------------------------------
 *  121005 MF     First Version
 *  022206 BT     1. initialization of candleSettings
 *                2. add SetCompatibility and GetCompatibility
 *                3. add SetUnstablePeriod, GetUnstablePeriod
 */

package com.tictactec.ta.lib;

public class Core {
   
   private int[] unstablePeriod;
   
   private CandleSetting[] candleSettings;
   
   private Compatibility compatibility;
   
   /** Creates a new instance of Core */
   public Core() {
      unstablePeriod = new int[com.tictactec.ta.lib.FuncUnstId.All
         .ordinal()];
      compatibility = Compatibility.Default;
      candleSettings = new CandleSetting[com.tictactec.ta.lib.CandleSettingType.AllCandleSettings
         .ordinal()];
      for(int i=0;i<candleSettings.length;i++){
         candleSettings[i] = new CandleSetting(TA_CandleDefaultSettings[i]);
      }
   }
   
   public RetCode SetCandleSettings(CandleSettingType settingType,
      RangeType rangeType, int avgPeriod, double factor) {
      if (settingType.ordinal() >= CandleSettingType.AllCandleSettings
         .ordinal())
         return RetCode.BadParam;
      candleSettings[settingType.ordinal()].settingType = settingType;
      candleSettings[settingType.ordinal()].rangeType = rangeType;
      candleSettings[settingType.ordinal()].avgPeriod = avgPeriod;
      candleSettings[settingType.ordinal()].factor = factor;
      return RetCode.Success;
   }
   
   final private CandleSetting TA_CandleDefaultSettings[] = {
      /*
      * real body is long when it's longer than the average of the 10
      * previous candles' real body
      */
      new CandleSetting(CandleSettingType.BodyLong,
         RangeType.RealBody, 10, 1.0),
      /*
      * real body is very long when it's longer than 3 times the average
      * of the 10 previous candles' real body
      */
      new CandleSetting(CandleSettingType.BodyVeryLong,
         RangeType.RealBody, 10, 3.0),
      /*
      * real body is short when it's shorter than the average of the 10
      * previous candles' real bodies
      */
      new CandleSetting(CandleSettingType.BodyShort,
         RangeType.RealBody, 10, 1.0),
      /*
      * real body is like doji's body when it's shorter than 10% the
      * average of the 10 previous candles' high-low range
      */
      new CandleSetting(CandleSettingType.BodyDoji,
         RangeType.HighLow, 10, 0.1),
      /* shadow is long when it's longer than the real body */
      new CandleSetting(CandleSettingType.ShadowLong,
         RangeType.RealBody, 0, 1.0),
      /* shadow is very long when it's longer than 2 times the real body */
      new CandleSetting(CandleSettingType.ShadowVeryLong,
         RangeType.RealBody, 0, 2.0),
      /*
      * shadow is short when it's shorter than half the average of the 10
      * previous candles' sum of shadows
      */
      new CandleSetting(CandleSettingType.ShadowShort,
         RangeType.Shadows, 10, 1.0),
      /*
      * shadow is very short when it's shorter than 10% the average of
      * the 10 previous candles' high-low range
      */
      new CandleSetting(CandleSettingType.ShadowVeryShort,
         RangeType.HighLow, 10, 0.1),
      /* when measuring distance between parts of candles or width of gaps */
      /*
      * "near" means "<= 20% of the average of the 5 previous candles'
      * high-low range"
      */
      new CandleSetting(CandleSettingType.Near,
         RangeType.HighLow, 5, 0.2),
      /* when measuring distance between parts of candles or width of gaps */
      /*
      * "far" means ">= 60% of the average of the 5 previous candles'
      * high-low range"
      */
      new CandleSetting(CandleSettingType.Far,
         RangeType.HighLow, 5, 0.6),
      /* when measuring distance between parts of candles or width of gaps */
      /*
      * "equal" means "<= 5% of the average of the 5 previous candles'
      * high-low range"
      */
      new CandleSetting(CandleSettingType.Equal,
         RangeType.HighLow, 5, 0.05) };
   
   public RetCode RestoreCandleDefaultSettings(
      CandleSettingType settingType) {
      int i;
      if (settingType.ordinal() > CandleSettingType.AllCandleSettings
         .ordinal())
         return RetCode.BadParam;
      if (settingType == CandleSettingType.AllCandleSettings) {
         for (i = 0; i < CandleSettingType.AllCandleSettings.ordinal(); ++i) {
            candleSettings[i].CopyFrom(TA_CandleDefaultSettings[i]);
         }
      } else {
         candleSettings[settingType.ordinal()]
            .CopyFrom(TA_CandleDefaultSettings[settingType.ordinal()]);
      }
      return RetCode.Success;
   }
   
   public RetCode SetUnstablePeriod(FuncUnstId id, int period)
   {
      if (id.ordinal() >= FuncUnstId.All
         .ordinal())
         return RetCode.BadParam;
      unstablePeriod[id.ordinal()] = period;
      return RetCode.Success;
   }
   
   public int GetUnstablePeriod(FuncUnstId id)
   {
      return unstablePeriod[id.ordinal()];
   }
   
   public void SetCompatibility(Compatibility compatibility)
   {
      this.compatibility = compatibility;
   }
   
   public Compatibility getCompatibility()
   {
      return compatibility;
   }
   
   /**** START GENCODE SECTION 1 - DO NOT DELETE THIS LINE ****/
   public int acosLookback( )
   {
      return 0;
   }
   public RetCode acos( int startIdx,
      int endIdx,
      double inReal[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      for( i=startIdx, outIdx=0; i <= endIdx; i++, outIdx++ )
      {
         outReal[outIdx] = Math.acos (inReal[i]);
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   public RetCode acos( int startIdx,
      int endIdx,
      float inReal[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      for( i=startIdx, outIdx=0; i <= endIdx; i++, outIdx++ )
      {
         outReal[outIdx] = Math.acos (inReal[i]);
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int adLookback( )
   {
      return 0;
   }
   public RetCode ad( int startIdx,
      int endIdx,
      double inHigh[],
      double inLow[],
      double inClose[],
      double inVolume[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int nbBar, currentBar, outIdx;
      double high, low, close, tmp;
      double ad;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      nbBar = endIdx-startIdx+1;
      outNBElement.value = nbBar;
      outBegIdx.value = startIdx;
      currentBar = startIdx;
      outIdx = 0;
      ad = 0.0;
      while( nbBar != 0 )
      {
         high = inHigh[currentBar];
         low = inLow[currentBar];
         tmp = high-low;
         close = inClose[currentBar];
         if( tmp > 0.0 )
            ad += (((close-low)-(high-close))/tmp)*((double)inVolume[currentBar]);
         outReal[outIdx++] = ad;
         currentBar++;
         nbBar--;
      }
      return RetCode.Success ;
   }
   public RetCode ad( int startIdx,
      int endIdx,
      float inHigh[],
      float inLow[],
      float inClose[],
      float inVolume[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int nbBar, currentBar, outIdx;
      double high, low, close, tmp;
      double ad;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      nbBar = endIdx-startIdx+1;
      outNBElement.value = nbBar;
      outBegIdx.value = startIdx;
      currentBar = startIdx;
      outIdx = 0;
      ad = 0.0;
      while( nbBar != 0 )
      {
         high = inHigh[currentBar];
         low = inLow[currentBar];
         tmp = high-low;
         close = inClose[currentBar];
         if( tmp > 0.0 )
            ad += (((close-low)-(high-close))/tmp)*((double)inVolume[currentBar]);
         outReal[outIdx++] = ad;
         currentBar++;
         nbBar--;
      }
      return RetCode.Success ;
   }
   /* Generated */
   public int addLookback( )
   {
      return 0;
   }
   public RetCode add( int startIdx,
      int endIdx,
      double inReal0[],
      double inReal1[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      for( i=startIdx, outIdx=0; i <= endIdx; i++, outIdx++ )
      {
         outReal[outIdx] = inReal0[i]+inReal1[i];
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   public RetCode add( int startIdx,
      int endIdx,
      float inReal0[],
      float inReal1[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      for( i=startIdx, outIdx=0; i <= endIdx; i++, outIdx++ )
      {
         outReal[outIdx] = inReal0[i]+inReal1[i];
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int adOscLookback( int optInFastPeriod,
      int optInSlowPeriod )
   {
      int slowestPeriod;
      if( (int)optInFastPeriod == ( Integer.MIN_VALUE ) )
         optInFastPeriod = 3;
      else if( ((int)optInFastPeriod < 2) || ((int)optInFastPeriod > 100000) )
         return -1;
      if( (int)optInSlowPeriod == ( Integer.MIN_VALUE ) )
         optInSlowPeriod = 10;
      else if( ((int)optInSlowPeriod < 2) || ((int)optInSlowPeriod > 100000) )
         return -1;
      if( optInFastPeriod < optInSlowPeriod )
         slowestPeriod = optInSlowPeriod;
      else
         slowestPeriod = optInFastPeriod;
      return emaLookback ( slowestPeriod );
   }
   public RetCode adOsc( int startIdx,
      int endIdx,
      double inHigh[],
      double inLow[],
      double inClose[],
      double inVolume[],
      int optInFastPeriod,
      int optInSlowPeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int today, outIdx, lookbackTotal;
      int slowestPeriod;
      double high, low, close, tmp;
      double slowEMA, slowk, one_minus_slowk;
      double fastEMA, fastk, one_minus_fastk;
      double ad;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInFastPeriod == ( Integer.MIN_VALUE ) )
         optInFastPeriod = 3;
      else if( ((int)optInFastPeriod < 2) || ((int)optInFastPeriod > 100000) )
         return RetCode.BadParam ;
      if( (int)optInSlowPeriod == ( Integer.MIN_VALUE ) )
         optInSlowPeriod = 10;
      else if( ((int)optInSlowPeriod < 2) || ((int)optInSlowPeriod > 100000) )
         return RetCode.BadParam ;
      if( optInFastPeriod < optInSlowPeriod )
         slowestPeriod = optInSlowPeriod;
      else
         slowestPeriod = optInFastPeriod;
      lookbackTotal = emaLookback ( slowestPeriod );
      if( startIdx < lookbackTotal )
         startIdx = lookbackTotal;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      outBegIdx.value = startIdx;
      today = startIdx-lookbackTotal;
      ad = 0.0;
      fastk = ((double)2.0 / ((double)(optInFastPeriod + 1))) ;
      one_minus_fastk = 1.0 - fastk;
      slowk = ((double)2.0 / ((double)(optInSlowPeriod + 1))) ;
      one_minus_slowk = 1.0 - slowk;
      { high = inHigh[today]; low = inLow[today]; tmp = high-low; close = inClose[today]; if( tmp > 0.0 ) ad += (((close-low)-(high-close))/tmp)*((double)inVolume[today]); today++; } ;
      fastEMA = ad;
      slowEMA = ad;
      while( today < startIdx )
      {
         { high = inHigh[today]; low = inLow[today]; tmp = high-low; close = inClose[today]; if( tmp > 0.0 ) ad += (((close-low)-(high-close))/tmp)*((double)inVolume[today]); today++; } ;
         fastEMA = (fastk*ad)+(one_minus_fastk*fastEMA);
         slowEMA = (slowk*ad)+(one_minus_slowk*slowEMA);
      }
      outIdx = 0;
      while( today <= endIdx )
      {
         { high = inHigh[today]; low = inLow[today]; tmp = high-low; close = inClose[today]; if( tmp > 0.0 ) ad += (((close-low)-(high-close))/tmp)*((double)inVolume[today]); today++; } ;
         fastEMA = (fastk*ad)+(one_minus_fastk*fastEMA);
         slowEMA = (slowk*ad)+(one_minus_slowk*slowEMA);
         outReal[outIdx++] = fastEMA - slowEMA;
      }
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   public RetCode adOsc( int startIdx,
      int endIdx,
      float inHigh[],
      float inLow[],
      float inClose[],
      float inVolume[],
      int optInFastPeriod,
      int optInSlowPeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int today, outIdx, lookbackTotal;
      int slowestPeriod;
      double high, low, close, tmp;
      double slowEMA, slowk, one_minus_slowk;
      double fastEMA, fastk, one_minus_fastk;
      double ad;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInFastPeriod == ( Integer.MIN_VALUE ) )
         optInFastPeriod = 3;
      else if( ((int)optInFastPeriod < 2) || ((int)optInFastPeriod > 100000) )
         return RetCode.BadParam ;
      if( (int)optInSlowPeriod == ( Integer.MIN_VALUE ) )
         optInSlowPeriod = 10;
      else if( ((int)optInSlowPeriod < 2) || ((int)optInSlowPeriod > 100000) )
         return RetCode.BadParam ;
      if( optInFastPeriod < optInSlowPeriod )
         slowestPeriod = optInSlowPeriod;
      else
         slowestPeriod = optInFastPeriod;
      lookbackTotal = emaLookback ( slowestPeriod );
      if( startIdx < lookbackTotal )
         startIdx = lookbackTotal;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      outBegIdx.value = startIdx;
      today = startIdx-lookbackTotal;
      ad = 0.0;
      fastk = ((double)2.0 / ((double)(optInFastPeriod + 1))) ;
      one_minus_fastk = 1.0 - fastk;
      slowk = ((double)2.0 / ((double)(optInSlowPeriod + 1))) ;
      one_minus_slowk = 1.0 - slowk;
      { high = inHigh[today]; low = inLow[today]; tmp = high-low; close = inClose[today]; if( tmp > 0.0 ) ad += (((close-low)-(high-close))/tmp)*((double)inVolume[today]); today++; } ;
      fastEMA = ad;
      slowEMA = ad;
      while( today < startIdx )
      {
         { high = inHigh[today]; low = inLow[today]; tmp = high-low; close = inClose[today]; if( tmp > 0.0 ) ad += (((close-low)-(high-close))/tmp)*((double)inVolume[today]); today++; } ;
         fastEMA = (fastk*ad)+(one_minus_fastk*fastEMA);
         slowEMA = (slowk*ad)+(one_minus_slowk*slowEMA);
      }
      outIdx = 0;
      while( today <= endIdx )
      {
         { high = inHigh[today]; low = inLow[today]; tmp = high-low; close = inClose[today]; if( tmp > 0.0 ) ad += (((close-low)-(high-close))/tmp)*((double)inVolume[today]); today++; } ;
         fastEMA = (fastk*ad)+(one_minus_fastk*fastEMA);
         slowEMA = (slowk*ad)+(one_minus_slowk*slowEMA);
         outReal[outIdx++] = fastEMA - slowEMA;
      }
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int adxLookback( int optInTimePeriod )
   {
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return -1;
      return (2 * optInTimePeriod) + (this.unstablePeriod[FuncUnstId.Adx.ordinal()]) - 1;
   }
   public RetCode adx( int startIdx,
      int endIdx,
      double inHigh[],
      double inLow[],
      double inClose[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int today, lookbackTotal, outIdx;
      double prevHigh, prevLow, prevClose;
      double prevMinusDM, prevPlusDM, prevTR;
      double tempReal, tempReal2, diffP, diffM;
      double minusDI, plusDI, sumDX, prevADX;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      lookbackTotal = (2*optInTimePeriod) + (this.unstablePeriod[FuncUnstId.Adx.ordinal()]) - 1;
      if( startIdx < lookbackTotal )
         startIdx = lookbackTotal;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      outIdx = 0;
      outBegIdx.value = today = startIdx;
      prevMinusDM = 0.0;
      prevPlusDM = 0.0;
      prevTR = 0.0;
      today = startIdx - lookbackTotal;
      prevHigh = inHigh[today];
      prevLow = inLow[today];
      prevClose = inClose[today];
      i = optInTimePeriod-1;
      while( i-- > 0 )
      {
         today++;
         tempReal = inHigh[today];
         diffP = tempReal-prevHigh;
         prevHigh = tempReal;
         tempReal = inLow[today];
         diffM = prevLow-tempReal;
         prevLow = tempReal;
         if( (diffM > 0) && (diffP < diffM) )
         {
            prevMinusDM += diffM;
         }
         else if( (diffP > 0) && (diffP > diffM) )
         {
            prevPlusDM += diffP;
         }
         { tempReal = prevHigh-prevLow; tempReal2 = Math.abs (prevHigh-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; tempReal2 = Math.abs (prevLow-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; } ;
         prevTR += tempReal;
         prevClose = inClose[today];
      }
      sumDX = 0.0;
      i = optInTimePeriod;
      while( i-- > 0 )
      {
         today++;
         tempReal = inHigh[today];
         diffP = tempReal-prevHigh;
         prevHigh = tempReal;
         tempReal = inLow[today];
         diffM = prevLow-tempReal;
         prevLow = tempReal;
         prevMinusDM -= prevMinusDM/optInTimePeriod;
         prevPlusDM -= prevPlusDM/optInTimePeriod;
         if( (diffM > 0) && (diffP < diffM) )
         {
            prevMinusDM += diffM;
         }
         else if( (diffP > 0) && (diffP > diffM) )
         {
            prevPlusDM += diffP;
         }
         { tempReal = prevHigh-prevLow; tempReal2 = Math.abs (prevHigh-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; tempReal2 = Math.abs (prevLow-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; } ;
         prevTR = prevTR - (prevTR/optInTimePeriod) + tempReal;
         prevClose = inClose[today];
         if( ! (((-0.00000001)<prevTR)&&(prevTR<0.00000001)) )
         {
            minusDI = (100.0*(prevMinusDM/prevTR)) ;
            plusDI = (100.0*(prevPlusDM/prevTR)) ;
            tempReal = minusDI+plusDI;
            if( ! (((-0.00000001)<tempReal)&&(tempReal<0.00000001)) )
               sumDX += (100.0 * ( Math.abs (minusDI-plusDI)/tempReal)) ;
         }
      }
      prevADX = (sumDX / optInTimePeriod) ;
      i = (this.unstablePeriod[FuncUnstId.Adx.ordinal()]) ;
      while( i-- > 0 )
      {
         today++;
         tempReal = inHigh[today];
         diffP = tempReal-prevHigh;
         prevHigh = tempReal;
         tempReal = inLow[today];
         diffM = prevLow-tempReal;
         prevLow = tempReal;
         prevMinusDM -= prevMinusDM/optInTimePeriod;
         prevPlusDM -= prevPlusDM/optInTimePeriod;
         if( (diffM > 0) && (diffP < diffM) )
         {
            prevMinusDM += diffM;
         }
         else if( (diffP > 0) && (diffP > diffM) )
         {
            prevPlusDM += diffP;
         }
         { tempReal = prevHigh-prevLow; tempReal2 = Math.abs (prevHigh-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; tempReal2 = Math.abs (prevLow-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; } ;
         prevTR = prevTR - (prevTR/optInTimePeriod) + tempReal;
         prevClose = inClose[today];
         if( ! (((-0.00000001)<prevTR)&&(prevTR<0.00000001)) )
         {
            minusDI = (100.0*(prevMinusDM/prevTR)) ;
            plusDI = (100.0*(prevPlusDM/prevTR)) ;
            tempReal = minusDI+plusDI;
            if( ! (((-0.00000001)<tempReal)&&(tempReal<0.00000001)) )
            {
               tempReal = (100.0*( Math.abs (minusDI-plusDI)/tempReal)) ;
               prevADX = (((prevADX*(optInTimePeriod-1))+tempReal)/optInTimePeriod) ;
            }
         }
      }
      outReal[0] = prevADX;
      outIdx = 1;
      while( today < endIdx )
      {
         today++;
         tempReal = inHigh[today];
         diffP = tempReal-prevHigh;
         prevHigh = tempReal;
         tempReal = inLow[today];
         diffM = prevLow-tempReal;
         prevLow = tempReal;
         prevMinusDM -= prevMinusDM/optInTimePeriod;
         prevPlusDM -= prevPlusDM/optInTimePeriod;
         if( (diffM > 0) && (diffP < diffM) )
         {
            prevMinusDM += diffM;
         }
         else if( (diffP > 0) && (diffP > diffM) )
         {
            prevPlusDM += diffP;
         }
         { tempReal = prevHigh-prevLow; tempReal2 = Math.abs (prevHigh-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; tempReal2 = Math.abs (prevLow-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; } ;
         prevTR = prevTR - (prevTR/optInTimePeriod) + tempReal;
         prevClose = inClose[today];
         if( ! (((-0.00000001)<prevTR)&&(prevTR<0.00000001)) )
         {
            minusDI = (100.0*(prevMinusDM/prevTR)) ;
            plusDI = (100.0*(prevPlusDM/prevTR)) ;
            tempReal = minusDI+plusDI;
            if( ! (((-0.00000001)<tempReal)&&(tempReal<0.00000001)) )
            {
               tempReal = (100.0*( Math.abs (minusDI-plusDI)/tempReal)) ;
               prevADX = (((prevADX*(optInTimePeriod-1))+tempReal)/optInTimePeriod) ;
            }
         }
         outReal[outIdx++] = prevADX;
      }
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   public RetCode adx( int startIdx,
      int endIdx,
      float inHigh[],
      float inLow[],
      float inClose[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int today, lookbackTotal, outIdx;
      double prevHigh, prevLow, prevClose;
      double prevMinusDM, prevPlusDM, prevTR;
      double tempReal, tempReal2, diffP, diffM;
      double minusDI, plusDI, sumDX, prevADX;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      lookbackTotal = (2*optInTimePeriod) + (this.unstablePeriod[FuncUnstId.Adx.ordinal()]) - 1;
      if( startIdx < lookbackTotal )
         startIdx = lookbackTotal;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      outIdx = 0;
      outBegIdx.value = today = startIdx;
      prevMinusDM = 0.0;
      prevPlusDM = 0.0;
      prevTR = 0.0;
      today = startIdx - lookbackTotal;
      prevHigh = inHigh[today];
      prevLow = inLow[today];
      prevClose = inClose[today];
      i = optInTimePeriod-1;
      while( i-- > 0 )
      {
         today++;
         tempReal = inHigh[today];
         diffP = tempReal-prevHigh;
         prevHigh = tempReal;
         tempReal = inLow[today];
         diffM = prevLow-tempReal;
         prevLow = tempReal;
         if( (diffM > 0) && (diffP < diffM) )
         {
            prevMinusDM += diffM;
         }
         else if( (diffP > 0) && (diffP > diffM) )
         {
            prevPlusDM += diffP;
         }
         { tempReal = prevHigh-prevLow; tempReal2 = Math.abs (prevHigh-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; tempReal2 = Math.abs (prevLow-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; } ;
         prevTR += tempReal;
         prevClose = inClose[today];
      }
      sumDX = 0.0;
      i = optInTimePeriod;
      while( i-- > 0 )
      {
         today++;
         tempReal = inHigh[today];
         diffP = tempReal-prevHigh;
         prevHigh = tempReal;
         tempReal = inLow[today];
         diffM = prevLow-tempReal;
         prevLow = tempReal;
         prevMinusDM -= prevMinusDM/optInTimePeriod;
         prevPlusDM -= prevPlusDM/optInTimePeriod;
         if( (diffM > 0) && (diffP < diffM) )
         {
            prevMinusDM += diffM;
         }
         else if( (diffP > 0) && (diffP > diffM) )
         {
            prevPlusDM += diffP;
         }
         { tempReal = prevHigh-prevLow; tempReal2 = Math.abs (prevHigh-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; tempReal2 = Math.abs (prevLow-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; } ;
         prevTR = prevTR - (prevTR/optInTimePeriod) + tempReal;
         prevClose = inClose[today];
         if( ! (((-0.00000001)<prevTR)&&(prevTR<0.00000001)) )
         {
            minusDI = (100.0*(prevMinusDM/prevTR)) ;
            plusDI = (100.0*(prevPlusDM/prevTR)) ;
            tempReal = minusDI+plusDI;
            if( ! (((-0.00000001)<tempReal)&&(tempReal<0.00000001)) )
               sumDX += (100.0 * ( Math.abs (minusDI-plusDI)/tempReal)) ;
         }
      }
      prevADX = (sumDX / optInTimePeriod) ;
      i = (this.unstablePeriod[FuncUnstId.Adx.ordinal()]) ;
      while( i-- > 0 )
      {
         today++;
         tempReal = inHigh[today];
         diffP = tempReal-prevHigh;
         prevHigh = tempReal;
         tempReal = inLow[today];
         diffM = prevLow-tempReal;
         prevLow = tempReal;
         prevMinusDM -= prevMinusDM/optInTimePeriod;
         prevPlusDM -= prevPlusDM/optInTimePeriod;
         if( (diffM > 0) && (diffP < diffM) )
         {
            prevMinusDM += diffM;
         }
         else if( (diffP > 0) && (diffP > diffM) )
         {
            prevPlusDM += diffP;
         }
         { tempReal = prevHigh-prevLow; tempReal2 = Math.abs (prevHigh-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; tempReal2 = Math.abs (prevLow-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; } ;
         prevTR = prevTR - (prevTR/optInTimePeriod) + tempReal;
         prevClose = inClose[today];
         if( ! (((-0.00000001)<prevTR)&&(prevTR<0.00000001)) )
         {
            minusDI = (100.0*(prevMinusDM/prevTR)) ;
            plusDI = (100.0*(prevPlusDM/prevTR)) ;
            tempReal = minusDI+plusDI;
            if( ! (((-0.00000001)<tempReal)&&(tempReal<0.00000001)) )
            {
               tempReal = (100.0*( Math.abs (minusDI-plusDI)/tempReal)) ;
               prevADX = (((prevADX*(optInTimePeriod-1))+tempReal)/optInTimePeriod) ;
            }
         }
      }
      outReal[0] = prevADX;
      outIdx = 1;
      while( today < endIdx )
      {
         today++;
         tempReal = inHigh[today];
         diffP = tempReal-prevHigh;
         prevHigh = tempReal;
         tempReal = inLow[today];
         diffM = prevLow-tempReal;
         prevLow = tempReal;
         prevMinusDM -= prevMinusDM/optInTimePeriod;
         prevPlusDM -= prevPlusDM/optInTimePeriod;
         if( (diffM > 0) && (diffP < diffM) )
         {
            prevMinusDM += diffM;
         }
         else if( (diffP > 0) && (diffP > diffM) )
         {
            prevPlusDM += diffP;
         }
         { tempReal = prevHigh-prevLow; tempReal2 = Math.abs (prevHigh-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; tempReal2 = Math.abs (prevLow-prevClose); if( tempReal2 > tempReal ) tempReal = tempReal2; } ;
         prevTR = prevTR - (prevTR/optInTimePeriod) + tempReal;
         prevClose = inClose[today];
         if( ! (((-0.00000001)<prevTR)&&(prevTR<0.00000001)) )
         {
            minusDI = (100.0*(prevMinusDM/prevTR)) ;
            plusDI = (100.0*(prevPlusDM/prevTR)) ;
            tempReal = minusDI+plusDI;
            if( ! (((-0.00000001)<tempReal)&&(tempReal<0.00000001)) )
            {
               tempReal = (100.0*( Math.abs (minusDI-plusDI)/tempReal)) ;
               prevADX = (((prevADX*(optInTimePeriod-1))+tempReal)/optInTimePeriod) ;
            }
         }
         outReal[outIdx++] = prevADX;
      }
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int adxrLookback( int optInTimePeriod )
   {
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return -1;
      if( optInTimePeriod > 1 )
         return optInTimePeriod + adxLookback ( optInTimePeriod) - 1;
      else
         return 3;
   }
   public RetCode adxr( int startIdx,
      int endIdx,
      double inHigh[],
      double inLow[],
      double inClose[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      double []adx ;
      int adxrLookback, i, j, outIdx, nbElement;
      RetCode retCode;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      adxrLookback = adxrLookback ( optInTimePeriod );
      if( startIdx < adxrLookback )
         startIdx = adxrLookback;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      adx = new double[endIdx-startIdx+optInTimePeriod] ;
      retCode = adx ( startIdx-(optInTimePeriod-1), endIdx,
         inHigh, inLow, inClose,
         optInTimePeriod, outBegIdx, outNBElement, adx );
      if( retCode != RetCode.Success )
      {
         return retCode;
      }
      i = optInTimePeriod-1;
      j = 0;
      outIdx = 0;
      nbElement = endIdx-startIdx+2;
      while( --nbElement != 0 )
         outReal[outIdx++] = ((adx[i++]+adx[j++])/2.0) ;
      outBegIdx.value = startIdx;
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   public RetCode adxr( int startIdx,
      int endIdx,
      float inHigh[],
      float inLow[],
      float inClose[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      double []adx ;
      int adxrLookback, i, j, outIdx, nbElement;
      RetCode retCode;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      adxrLookback = adxrLookback ( optInTimePeriod );
      if( startIdx < adxrLookback )
         startIdx = adxrLookback;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      adx = new double[endIdx-startIdx+optInTimePeriod] ;
      retCode = adx ( startIdx-(optInTimePeriod-1), endIdx,
         inHigh, inLow, inClose,
         optInTimePeriod, outBegIdx, outNBElement, adx );
      if( retCode != RetCode.Success )
      {
         return retCode;
      }
      i = optInTimePeriod-1;
      j = 0;
      outIdx = 0;
      nbElement = endIdx-startIdx+2;
      while( --nbElement != 0 )
         outReal[outIdx++] = ((adx[i++]+adx[j++])/2.0) ;
      outBegIdx.value = startIdx;
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int apoLookback( int optInFastPeriod,
      int optInSlowPeriod,
      MAType optInMAType )
   {
      if( (int)optInFastPeriod == ( Integer.MIN_VALUE ) )
         optInFastPeriod = 12;
      else if( ((int)optInFastPeriod < 2) || ((int)optInFastPeriod > 100000) )
         return -1;
      if( (int)optInSlowPeriod == ( Integer.MIN_VALUE ) )
         optInSlowPeriod = 26;
      else if( ((int)optInSlowPeriod < 2) || ((int)optInSlowPeriod > 100000) )
         return -1;
      return movingAverageLookback ( (((optInSlowPeriod) > (optInFastPeriod)) ? (optInSlowPeriod) : (optInFastPeriod)) , optInMAType );
   }
   public RetCode apo( int startIdx,
      int endIdx,
      double inReal[],
      int optInFastPeriod,
      int optInSlowPeriod,
      MAType optInMAType,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      double []tempBuffer ;
      RetCode retCode;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInFastPeriod == ( Integer.MIN_VALUE ) )
         optInFastPeriod = 12;
      else if( ((int)optInFastPeriod < 2) || ((int)optInFastPeriod > 100000) )
         return RetCode.BadParam ;
      if( (int)optInSlowPeriod == ( Integer.MIN_VALUE ) )
         optInSlowPeriod = 26;
      else if( ((int)optInSlowPeriod < 2) || ((int)optInSlowPeriod > 100000) )
         return RetCode.BadParam ;
      tempBuffer = new double[(endIdx-startIdx+1)] ;
      retCode = TA_INT_PO ( startIdx, endIdx,
         inReal,
         optInFastPeriod,
         optInSlowPeriod,
         optInMAType,
         outBegIdx,
         outNBElement,
         outReal,
         tempBuffer,
         0 );
      return retCode;
   }
   RetCode TA_INT_PO( int startIdx,
      int endIdx,
      double inReal[],
      int optInFastPeriod,
      int optInSlowPeriod,
      MAType optInMethod_2,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[],
      double tempBuffer[],
      int doPercentageOutput )
   {
      RetCode retCode;
      double tempReal;
      int tempInteger;
      MInteger outBegIdx1 = new MInteger() ;
      MInteger outNbElement1 = new MInteger() ;
      MInteger outBegIdx2 = new MInteger() ;
      MInteger outNbElement2 = new MInteger() ;
      int i, j;
      if( optInSlowPeriod < optInFastPeriod )
      {
         tempInteger = optInSlowPeriod;
         optInSlowPeriod = optInFastPeriod;
         optInFastPeriod = tempInteger;
      }
      retCode = movingAverage ( startIdx, endIdx,
         inReal,
         optInFastPeriod,
         optInMethod_2,
         outBegIdx2 , outNbElement2 ,
         tempBuffer );
      if( retCode == RetCode.Success )
      {
         retCode = movingAverage ( startIdx, endIdx,
            inReal,
            optInSlowPeriod,
            optInMethod_2,
            outBegIdx1 , outNbElement1 ,
            outReal );
         if( retCode == RetCode.Success )
         {
            tempInteger = outBegIdx1.value - outBegIdx2.value ;
            if( doPercentageOutput != 0 )
            {
               for( i=0,j=tempInteger; i < outNbElement1.value ; i++, j++ )
               {
                  tempReal = outReal[i];
                  if( ! (((-0.00000001)<tempReal)&&(tempReal<0.00000001)) )
                     outReal[i] = ((tempBuffer[j]-tempReal)/tempReal)*100.0;
                  else
                     outReal[i] = 0.0;
               }
            }
            else
            {
               for( i=0,j=tempInteger; i < outNbElement1.value ; i++, j++ )
                  outReal[i] = tempBuffer[j]-outReal[i];
            }
            outBegIdx.value = outBegIdx1.value ;
            outNBElement.value = outNbElement1.value ;
         }
      }
      if( retCode != RetCode.Success )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
      }
      return retCode;
   }
   public RetCode apo( int startIdx,
      int endIdx,
      float inReal[],
      int optInFastPeriod,
      int optInSlowPeriod,
      MAType optInMAType,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      double []tempBuffer ;
      RetCode retCode;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInFastPeriod == ( Integer.MIN_VALUE ) )
         optInFastPeriod = 12;
      else if( ((int)optInFastPeriod < 2) || ((int)optInFastPeriod > 100000) )
         return RetCode.BadParam ;
      if( (int)optInSlowPeriod == ( Integer.MIN_VALUE ) )
         optInSlowPeriod = 26;
      else if( ((int)optInSlowPeriod < 2) || ((int)optInSlowPeriod > 100000) )
         return RetCode.BadParam ;
      tempBuffer = new double[(endIdx-startIdx+1)] ;
      retCode = TA_INT_PO ( startIdx, endIdx,
         inReal,
         optInFastPeriod,
         optInSlowPeriod,
         optInMAType,
         outBegIdx,
         outNBElement,
         outReal,
         tempBuffer,
         0 );
      return retCode;
   }
   RetCode TA_INT_PO( int startIdx,
      int endIdx,
      float inReal[],
      int optInFastPeriod,
      int optInSlowPeriod,
      MAType optInMethod_2,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[],
      double tempBuffer[],
      int doPercentageOutput )
   {
      RetCode retCode;
      double tempReal;
      int tempInteger;
      MInteger outBegIdx1 = new MInteger() ;
      MInteger outNbElement1 = new MInteger() ;
      MInteger outBegIdx2 = new MInteger() ;
      MInteger outNbElement2 = new MInteger() ;
      int i, j;
      if( optInSlowPeriod < optInFastPeriod )
      {
         tempInteger = optInSlowPeriod;
         optInSlowPeriod = optInFastPeriod;
         optInFastPeriod = tempInteger;
      }
      retCode = movingAverage ( startIdx, endIdx,
         inReal,
         optInFastPeriod,
         optInMethod_2,
         outBegIdx2 , outNbElement2 ,
         tempBuffer );
      if( retCode == RetCode.Success )
      {
         retCode = movingAverage ( startIdx, endIdx,
            inReal,
            optInSlowPeriod,
            optInMethod_2,
            outBegIdx1 , outNbElement1 ,
            outReal );
         if( retCode == RetCode.Success )
         {
            tempInteger = outBegIdx1.value - outBegIdx2.value ;
            if( doPercentageOutput != 0 )
            {
               for( i=0,j=tempInteger; i < outNbElement1.value ; i++, j++ )
               {
                  tempReal = outReal[i];
                  if( ! (((-0.00000001)<tempReal)&&(tempReal<0.00000001)) )
                     outReal[i] = ((tempBuffer[j]-tempReal)/tempReal)*100.0;
                  else
                     outReal[i] = 0.0;
               }
            }
            else
            {
               for( i=0,j=tempInteger; i < outNbElement1.value ; i++, j++ )
                  outReal[i] = tempBuffer[j]-outReal[i];
            }
            outBegIdx.value = outBegIdx1.value ;
            outNBElement.value = outNbElement1.value ;
         }
      }
      if( retCode != RetCode.Success )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
      }
      return retCode;
   }
   /* Generated */
   public int aroonLookback( int optInTimePeriod )
   {
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return -1;
      return optInTimePeriod;
   }
   public RetCode aroon( int startIdx,
      int endIdx,
      double inHigh[],
      double inLow[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outAroonDown[],
      double outAroonUp[] )
   {
      double lowest, highest, tmp, factor;
      int outIdx;
      int trailingIdx, lowestIdx, highestIdx, today, i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      if( startIdx < optInTimePeriod )
         startIdx = optInTimePeriod;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      outIdx = 0;
      today = startIdx;
      trailingIdx = startIdx-optInTimePeriod;
      lowestIdx = -1;
      highestIdx = -1;
      lowest = 0.0;
      highest = 0.0;
      factor = (double)100.0/(double)optInTimePeriod;
      while( today <= endIdx )
      {
         tmp = inLow[today];
         if( lowestIdx < trailingIdx )
         {
            lowestIdx = trailingIdx;
            lowest = inLow[lowestIdx];
            i = lowestIdx;
            while( ++i<=today )
            {
               tmp = inLow[i];
               if( tmp <= lowest )
               {
                  lowestIdx = i;
                  lowest = tmp;
               }
            }
         }
         else if( tmp <= lowest )
         {
            lowestIdx = today;
            lowest = tmp;
         }
         tmp = inHigh[today];
         if( highestIdx < trailingIdx )
         {
            highestIdx = trailingIdx;
            highest = inHigh[highestIdx];
            i = highestIdx;
            while( ++i<=today )
            {
               tmp = inHigh[i];
               if( tmp >= highest )
               {
                  highestIdx = i;
                  highest = tmp;
               }
            }
         }
         else if( tmp >= highest )
         {
            highestIdx = today;
            highest = tmp;
         }
         outAroonUp[outIdx] = factor*(optInTimePeriod-(today-highestIdx));
         outAroonDown[outIdx] = factor*(optInTimePeriod-(today-lowestIdx));
         outIdx++;
         trailingIdx++;
         today++;
      }
      outBegIdx.value = startIdx;
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   public RetCode aroon( int startIdx,
      int endIdx,
      float inHigh[],
      float inLow[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outAroonDown[],
      double outAroonUp[] )
   {
      double lowest, highest, tmp, factor;
      int outIdx;
      int trailingIdx, lowestIdx, highestIdx, today, i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      if( startIdx < optInTimePeriod )
         startIdx = optInTimePeriod;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      outIdx = 0;
      today = startIdx;
      trailingIdx = startIdx-optInTimePeriod;
      lowestIdx = -1;
      highestIdx = -1;
      lowest = 0.0;
      highest = 0.0;
      factor = (double)100.0/(double)optInTimePeriod;
      while( today <= endIdx )
      {
         tmp = inLow[today];
         if( lowestIdx < trailingIdx )
         {
            lowestIdx = trailingIdx;
            lowest = inLow[lowestIdx];
            i = lowestIdx;
            while( ++i<=today )
            {
               tmp = inLow[i];
               if( tmp <= lowest )
               {
                  lowestIdx = i;
                  lowest = tmp;
               }
            }
         }
         else if( tmp <= lowest )
         {
            lowestIdx = today;
            lowest = tmp;
         }
         tmp = inHigh[today];
         if( highestIdx < trailingIdx )
         {
            highestIdx = trailingIdx;
            highest = inHigh[highestIdx];
            i = highestIdx;
            while( ++i<=today )
            {
               tmp = inHigh[i];
               if( tmp >= highest )
               {
                  highestIdx = i;
                  highest = tmp;
               }
            }
         }
         else if( tmp >= highest )
         {
            highestIdx = today;
            highest = tmp;
         }
         outAroonUp[outIdx] = factor*(optInTimePeriod-(today-highestIdx));
         outAroonDown[outIdx] = factor*(optInTimePeriod-(today-lowestIdx));
         outIdx++;
         trailingIdx++;
         today++;
      }
      outBegIdx.value = startIdx;
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int aroonOscLookback( int optInTimePeriod )
   {
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return -1;
      return optInTimePeriod;
   }
   public RetCode aroonOsc( int startIdx,
      int endIdx,
      double inHigh[],
      double inLow[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      double lowest, highest, tmp, factor, aroon;
      int outIdx;
      int trailingIdx, lowestIdx, highestIdx, today, i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      if( startIdx < optInTimePeriod )
         startIdx = optInTimePeriod;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      outIdx = 0;
      today = startIdx;
      trailingIdx = startIdx-optInTimePeriod;
      lowestIdx = -1;
      highestIdx = -1;
      lowest = 0.0;
      highest = 0.0;
      factor = (double)100.0/(double)optInTimePeriod;
      while( today <= endIdx )
      {
         tmp = inLow[today];
         if( lowestIdx < trailingIdx )
         {
            lowestIdx = trailingIdx;
            lowest = inLow[lowestIdx];
            i = lowestIdx;
            while( ++i<=today )
            {
               tmp = inLow[i];
               if( tmp <= lowest )
               {
                  lowestIdx = i;
                  lowest = tmp;
               }
            }
         }
         else if( tmp <= lowest )
         {
            lowestIdx = today;
            lowest = tmp;
         }
         tmp = inHigh[today];
         if( highestIdx < trailingIdx )
         {
            highestIdx = trailingIdx;
            highest = inHigh[highestIdx];
            i = highestIdx;
            while( ++i<=today )
            {
               tmp = inHigh[i];
               if( tmp >= highest )
               {
                  highestIdx = i;
                  highest = tmp;
               }
            }
         }
         else if( tmp >= highest )
         {
            highestIdx = today;
            highest = tmp;
         }
         aroon = factor*(highestIdx-lowestIdx);
         outReal[outIdx] = aroon;
         outIdx++;
         trailingIdx++;
         today++;
      }
      outBegIdx.value = startIdx;
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   public RetCode aroonOsc( int startIdx,
      int endIdx,
      float inHigh[],
      float inLow[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      double lowest, highest, tmp, factor, aroon;
      int outIdx;
      int trailingIdx, lowestIdx, highestIdx, today, i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      if( startIdx < optInTimePeriod )
         startIdx = optInTimePeriod;
      if( startIdx > endIdx )
      {
         outBegIdx.value = 0 ;
         outNBElement.value = 0 ;
         return RetCode.Success ;
      }
      outIdx = 0;
      today = startIdx;
      trailingIdx = startIdx-optInTimePeriod;
      lowestIdx = -1;
      highestIdx = -1;
      lowest = 0.0;
      highest = 0.0;
      factor = (double)100.0/(double)optInTimePeriod;
      while( today <= endIdx )
      {
         tmp = inLow[today];
         if( lowestIdx < trailingIdx )
         {
            lowestIdx = trailingIdx;
            lowest = inLow[lowestIdx];
            i = lowestIdx;
            while( ++i<=today )
            {
               tmp = inLow[i];
               if( tmp <= lowest )
               {
                  lowestIdx = i;
                  lowest = tmp;
               }
            }
         }
         else if( tmp <= lowest )
         {
            lowestIdx = today;
            lowest = tmp;
         }
         tmp = inHigh[today];
         if( highestIdx < trailingIdx )
         {
            highestIdx = trailingIdx;
            highest = inHigh[highestIdx];
            i = highestIdx;
            while( ++i<=today )
            {
               tmp = inHigh[i];
               if( tmp >= highest )
               {
                  highestIdx = i;
                  highest = tmp;
               }
            }
         }
         else if( tmp >= highest )
         {
            highestIdx = today;
            highest = tmp;
         }
         aroon = factor*(highestIdx-lowestIdx);
         outReal[outIdx] = aroon;
         outIdx++;
         trailingIdx++;
         today++;
      }
      outBegIdx.value = startIdx;
      outNBElement.value = outIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int asinLookback( )
   {
      return 0;
   }
   public RetCode asin( int startIdx,
      int endIdx,
      double inReal[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      for( i=startIdx, outIdx=0; i <= endIdx; i++, outIdx++ )
      {
         outReal[outIdx] = Math.asin (inReal[i]);
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   public RetCode asin( int startIdx,
      int endIdx,
      float inReal[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      for( i=startIdx, outIdx=0; i <= endIdx; i++, outIdx++ )
      {
         outReal[outIdx] = Math.asin (inReal[i]);
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int atanLookback( )
   {
      return 0;
   }
   public RetCode atan( int startIdx,
      int endIdx,
      double inReal[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      for( i=startIdx, outIdx=0; i <= endIdx; i++, outIdx++ )
      {
         outReal[outIdx] = Math.atan (inReal[i]);
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   public RetCode atan( int startIdx,
      int endIdx,
      float inReal[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx;
      int i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      for( i=startIdx, outIdx=0; i <= endIdx; i++, outIdx++ )
      {
         outReal[outIdx] = Math.atan (inReal[i]);
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int atrLookback( int optInTimePeriod )
   {
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 1) || ((int)optInTimePeriod > 100000) )
         return -1;
      return optInTimePeriod + (this.unstablePeriod[FuncUnstId.Atr.ordinal()]) ;
   }
   public RetCode atr( int startIdx,
      int endIdx,
      double inHigh[],
      double inLow[],
      double inClose[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      RetCode retCode;
      int outIdx, today, lookbackTotal;
      int nbATR;
      MInteger outBegIdx1 = new MInteger() ;
      MInteger outNbElement1 = new MInteger() ;
      double prevATR;
      double []tempBuffer ;
      double []prevATRTemp = new double[1] ;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 1) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      outBegIdx.value = 0 ;
      outNBElement.value = 0 ;
      lookbackTotal = atrLookback ( optInTimePeriod );
      if( startIdx < lookbackTotal )
         startIdx = lookbackTotal;
      if( startIdx > endIdx )
         return RetCode.Success ;
      if( optInTimePeriod <= 1 )
      {
         return trueRange ( startIdx, endIdx,
            inHigh, inLow, inClose,
            outBegIdx, outNBElement, outReal );
      }
      tempBuffer = new double[lookbackTotal+(endIdx-startIdx)+1] ;
      retCode = trueRange ( (startIdx-lookbackTotal+1), endIdx,
         inHigh, inLow, inClose,
         outBegIdx1 , outNbElement1 ,
         tempBuffer );
      if( retCode != RetCode.Success )
      {
         return retCode;
      }
      retCode = TA_INT_SMA ( optInTimePeriod-1,
         optInTimePeriod-1,
         tempBuffer, optInTimePeriod,
         outBegIdx1 , outNbElement1 ,
         prevATRTemp );
      if( retCode != RetCode.Success )
      {
         return retCode;
      }
      prevATR = prevATRTemp[0];
      today = optInTimePeriod;
      outIdx = (this.unstablePeriod[FuncUnstId.Atr.ordinal()]) ;
      while( outIdx != 0 )
      {
         prevATR *= optInTimePeriod - 1;
         prevATR += tempBuffer[today++];
         prevATR /= optInTimePeriod;
         outIdx--;
      }
      outIdx = 1;
      outReal[0] = prevATR;
      nbATR = (endIdx - startIdx)+1;
      while( --nbATR != 0 )
      {
         prevATR *= optInTimePeriod - 1;
         prevATR += tempBuffer[today++];
         prevATR /= optInTimePeriod;
         outReal[outIdx++] = prevATR;
      }
      outBegIdx.value = startIdx;
      outNBElement.value = outIdx;
      return retCode;
   }
   public RetCode atr( int startIdx,
      int endIdx,
      float inHigh[],
      float inLow[],
      float inClose[],
      int optInTimePeriod,
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      RetCode retCode;
      int outIdx, today, lookbackTotal;
      int nbATR;
      MInteger outBegIdx1 = new MInteger() ;
      MInteger outNbElement1 = new MInteger() ;
      double prevATR;
      double []tempBuffer ;
      double []prevATRTemp = new double[1] ;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 14;
      else if( ((int)optInTimePeriod < 1) || ((int)optInTimePeriod > 100000) )
         return RetCode.BadParam ;
      outBegIdx.value = 0 ;
      outNBElement.value = 0 ;
      lookbackTotal = atrLookback ( optInTimePeriod );
      if( startIdx < lookbackTotal )
         startIdx = lookbackTotal;
      if( startIdx > endIdx )
         return RetCode.Success ;
      if( optInTimePeriod <= 1 )
      {
         return trueRange ( startIdx, endIdx,
            inHigh, inLow, inClose,
            outBegIdx, outNBElement, outReal );
      }
      tempBuffer = new double[lookbackTotal+(endIdx-startIdx)+1] ;
      retCode = trueRange ( (startIdx-lookbackTotal+1), endIdx,
         inHigh, inLow, inClose,
         outBegIdx1 , outNbElement1 ,
         tempBuffer );
      if( retCode != RetCode.Success )
      {
         return retCode;
      }
      retCode = TA_INT_SMA ( optInTimePeriod-1,
         optInTimePeriod-1,
         tempBuffer, optInTimePeriod,
         outBegIdx1 , outNbElement1 ,
         prevATRTemp );
      if( retCode != RetCode.Success )
      {
         return retCode;
      }
      prevATR = prevATRTemp[0];
      today = optInTimePeriod;
      outIdx = (this.unstablePeriod[FuncUnstId.Atr.ordinal()]) ;
      while( outIdx != 0 )
      {
         prevATR *= optInTimePeriod - 1;
         prevATR += tempBuffer[today++];
         prevATR /= optInTimePeriod;
         outIdx--;
      }
      outIdx = 1;
      outReal[0] = prevATR;
      nbATR = (endIdx - startIdx)+1;
      while( --nbATR != 0 )
      {
         prevATR *= optInTimePeriod - 1;
         prevATR += tempBuffer[today++];
         prevATR /= optInTimePeriod;
         outReal[outIdx++] = prevATR;
      }
      outBegIdx.value = startIdx;
      outNBElement.value = outIdx;
      return retCode;
   }
   /* Generated */
   public int avgPriceLookback( )
   {
      return 0;
   }
   public RetCode avgPrice( int startIdx,
      int endIdx,
      double inOpen[],
      double inHigh[],
      double inLow[],
      double inClose[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx, i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      outIdx = 0;
      for( i=startIdx; i <= endIdx; i++ )
      {
         outReal[outIdx++] = ( inHigh [i] +
            inLow [i] +
            inClose[i] +
            inOpen [i]) / 4;
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   public RetCode avgPrice( int startIdx,
      int endIdx,
      float inOpen[],
      float inHigh[],
      float inLow[],
      float inClose[],
      MInteger outBegIdx,
      MInteger outNBElement,
      double outReal[] )
   {
      int outIdx, i;
      if( startIdx < 0 )
         return RetCode.OutOfRangeStartIndex ;
      if( (endIdx < 0) || (endIdx < startIdx))
         return RetCode.OutOfRangeEndIndex ;
      outIdx = 0;
      for( i=startIdx; i <= endIdx; i++ )
      {
         outReal[outIdx++] = ( inHigh [i] +
            inLow [i] +
            inClose[i] +
            inOpen [i]) / 4;
      }
      outNBElement.value = outIdx;
      outBegIdx.value = startIdx;
      return RetCode.Success ;
   }
   /* Generated */
   public int bbandsLookback( int optInTimePeriod,
      double optInNbDevUp,
      double optInNbDevDn,
      MAType optInMAType )
   {
      if( (int)optInTimePeriod == ( Integer.MIN_VALUE ) )
         optInTimePeriod = 5;
      else if( ((int)optInTimePeriod < 2) || ((int)optInTimePeriod > 100000) )
         return -1;
      if( optInNbDevUp == (-4e+37) )
         optInNbDevUp = 2.000000e+0;
      else if( (optInNbDevUp < -3.000000e+37) || (optInNbDevUp > 3.000000e+37) )
         return -1;
      if( optInNbDevDn == (-4e+37) )
         optInNbDevDn = 2.000000e+0;
      else if( (optInNbDevDn < -3.000000e+37) || (optInNbDevDn > 3.000000e+37) )
         return -1;
      return movingAverageLookback ( optInTimePeriod, optInMATyp
