/*
 * CVS identifier:
 *
 * $Id: FileBitstreamReaderAgent.java,v 1.68 2002/07/19 12:34:38 grosbois Exp $
 *
 * Class:                   FileBitstreamReaderAgent
 *
 * Description:             Retrieve code-blocks codewords in the bit stream
 *
 *
 *
 * COPYRIGHT:
 * 
 * This software module was originally developed by Raphaël Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askelöf (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, Félix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 * 
 * Copyright (c) 1999/2000 JJ2000 Partners.
 * */
package jj2000.j2k.codestream.reader;

import jj2000.j2k.quantization.dequantizer.*;
import jj2000.j2k.wavelet.synthesis.*;
import jj2000.j2k.entropy.decoder.*;
import jj2000.j2k.codestream.*;
import jj2000.j2k.decoder.*;
import jj2000.j2k.entropy.*;
import jj2000.j2k.image.*;
import jj2000.j2k.util.*;
import jj2000.j2k.io.*;
import jj2000.j2k.*;

import java.util.*;
import java.io.*;

/**
 * This class reads the bit stream (with the help of HeaderDecoder for tile
 * headers and PktDecoder for packets header and body) and retrives location
 * of all code-block's codewords.
 *
 * <p>Note: All tile-parts headers are read by the constructor whereas packets
 * are processed when decoding related tile (when setTile method is
 * called).</p>
 *
 * <p>In parsing mode, the reader simulates a virtual layer-resolution
 * progressive bit stream with the same truncation points in each code-block,
 * whereas in truncation mode, only the first bytes are taken into account (it
 * behaves like if it is a real truncated codestream).</p>
 *
 * @see HeaderDecoder
 * @see PktDecoder
 * */
public class FileBitstreamReaderAgent extends BitstreamReaderAgent 
    implements Markers, ProgressionType, StdEntropyCoderOptions {

    /** Whether or not the last read Psot value was zero. Only the Psot in the
     * last tile-part in the codestream can have such a value. */
    private boolean isPsotEqualsZero = true;

    /** Reference to the PktDecoder instance */
    public PktDecoder pktDec;
    
    /** Reference to the ParameterList instance */
    private ParameterList pl;

    /** The RandomAccessIO where to get data from */
    private RandomAccessIO in;

    /** Offset of the first packet in each tile-part in each tile */
    private int[][] firstPackOff;

    /** 
     * Returns the number of tile-part found for a given tile
     * 
     * @param t Tile index
     *
     * */
    public int getNumTileParts(int t) {
        if(firstPackOff==null || firstPackOff[t]==null) {
            throw new Error("Tile "+t+" not found in input codestream.");
        }
        return firstPackOff[t].length;
    }

    /** 
     * Number of bytes allocated to each tile. In parsing mode, this number
     * is related to the tile length in the codestream whereas in truncation
     * mode all the rate is affected to the first tiles. */
    private int[] nBytes;

    /** Whether or not to print information found in codestream */
    private boolean printInfo = false;

    /** 
     * Backup of the number of bytes allocated to each tile. This array is
     * used to restore the number of bytes to read in each tile when the
     * codestream is read several times (for instance when decoding an R,G,B
     * image to three output files)
     * */
    private int[] baknBytes;

    /** Length of each tile-part (written in Psot) */
    private int[][] tilePartLen;

    /** Total length of each tile */
    private int[] totTileLen;

    /** Total length of tiles' header */
    private int[] totTileHeadLen;

    /** First tile part header length*/
    private int firstTilePartHeadLen;

    /** Total length of all tile parts in all tiles */
    private double totAllTileLen;

    /** Length of main header */
    private int mainHeadLen;

    /** Length of main and tile-parts headers */
    private int headLen = 0;

    /** Length of all tile-part headers */
    private int[][] tilePartHeadLen;

    /** Length of each packet head found in the tile */
    private Vector pktHL;

    /** True if truncation mode is used. False if parsing mode */
    private boolean isTruncMode;

    /** The number of tile-parts that remain to read */
    private int remainingTileParts;

    /** The number of tile-parts read so far for each tile */
    private int[] tilePartsRead;

    /** Thetotal  number of tile-parts read so far */
    private int totTilePartsRead=0;

    /** The number of found tile-parts in each tile. */
    private int[] tileParts;

    /** The current tile part being used */
    private int curTilePart;

    /** The number of the tile-parts found in the codestream after reading the
     * tp'th tile-part of tile t */
    private int[][] tilePartNum;

    /** Whether or not a EOC marker has been found instead of a SOT */
    private boolean isEOCFound = false;

    /** Reference to the HeaderInfo instance (used when reading SOT marker
     * segments) */
    private HeaderInfo hi;

    /** Array containing information for all the code-blocks:
     * 
     * <ul>
     * <li>1st dim: component index.</li>
     * <li>2nd dim: resolution level index.</li>
     * <li>3rd dim: subband index.</li>
     * <li>4th/5th dim: code-block index (vert. and horiz.).</li>
     * </ul>
     */
    private CBlkInfo[][][][][] cbI;


    /** Gets the reference to the CBlkInfo array */
    public CBlkInfo[][][][][] getCBlkInfo() {
        return cbI;
    }

    /** The maximum number of layers to decode for any code-block */
    private int lQuit;

    /** Whether or not to use only first progression order */
    private boolean usePOCQuit = false;

    /** 
     * Reads all tiles headers and keep offset of their first
     * packet. Finally it calls the rate allocation method.
     *
     * @param hd HeaderDecoder of the codestream.
     *
     * @param ehs The input stream where to read bit-stream.
     *
     * @param decSpec The decoder specifications 
     *
     * @param pl The ParameterList instance created from the
     * command-line arguments.
     *
     * @param cdstrInfo Whether or not to print information found in
     * codestream. 
     *
     * @see #allocateRate
     * */
    public FileBitstreamReaderAgent(HeaderDecoder hd,RandomAccessIO ehs,
                                    DecoderSpecs decSpec,ParameterList pl,
                                    boolean cdstrInfo,HeaderInfo hi)
	throws IOException {
        super(hd,decSpec);
        
        this.pl = pl;
        this.printInfo = cdstrInfo;
        this.hi = hi;
        String strInfo = "Codestream elements information in bytes "+
            "(offset, total length, header length):\n\n";
        
        // Check whether quit conditiosn used
        usePOCQuit = pl.getBooleanParameter("poc_quit");

        // Get decoding rate
        boolean rateInBytes;
        boolean parsing = pl.getBooleanParameter("parsing");
        try {
            trate = pl.getFloatParameter("rate");
            if(trate==-1) {
                trate = Float.MAX_VALUE;
            }
        } catch (NumberFormatException e) {
            throw new Error("Invalid value in 'rate' option: "+
			    pl.getParameter("rate"));
        } catch (IllegalArgumentException e) {
            throw new Error("'rate' option is missing");
        }
        
        try {
            tnbytes = pl.getIntParameter("nbytes");
        } catch (NumberFormatException e) {
            throw new Error("Invalid value in 'nbytes' option: "+
			    pl.getParameter("nbytes"));
        } catch (IllegalArgumentException e) {
            throw new Error("'nbytes' option is missing");
        }
        
	// Check that '-rate' and '-nbytes' are not used at the same time
        ParameterList defaults = pl.getDefaultParameterList();
        if(tnbytes!=defaults.getFloatParameter("nbytes")) {
            rateInBytes = true;
        } else {
            rateInBytes = false;
        }

        if(rateInBytes) {
            trate = tnbytes*8f/hd.getMaxCompImgWidth()/
                hd.getMaxCompImgHeight();
        } else {
            tnbytes = (int)(trate*hd.getMaxCompImgWidth()*
                            hd.getMaxCompImgHeight())/8;
        }
        isTruncMode = !pl.getBooleanParameter("parsing");

        // Check if quit conditions are being used
        int ncbQuit;
        try {
             ncbQuit = pl.getIntParameter("ncb_quit");
        } catch (NumberFormatException e) {
            throw new Error("Invalid value in 'ncb_quit' option: "+
                  pl.getParameter("ncb_quit"));
        } catch (IllegalArgumentException e) {
            throw new Error("'ncb_quit' option is missing");
        }
        if(ncbQuit != -1 && !isTruncMode){
            throw new Error("Cannot use -parsing and -ncb_quit condition at "+
                            "the same time.");
        }

        try {
             lQuit = pl.getIntParameter("l_quit");
        } catch (NumberFormatException e) {
            throw new Error("Invalid value in 'l_quit' option: "+
                  pl.getParameter("l_quit"));
        } catch (IllegalArgumentException e) {
            throw new Error("'l_quit' option is missing");
        }
       
        // initializations
        in = ehs;
        pktDec = new PktDecoder(decSpec,hd,ehs,this,isTruncMode,ncbQuit);

        tileParts = new int[nt];
        totTileLen = new int[nt];
	tilePartLen = new int[nt][];
        tilePartNum = new int[nt][];
        firstPackOff = new int[nt][];
        tilePartsRead = new int[nt];
        totTileHeadLen = new int[nt];
	tilePartHeadLen = new int[nt][];
	nBytes = new int[nt];
	baknBytes = new int[nt];
        hd.nTileParts = new int[nt];

	this.isTruncMode = isTruncMode;
        int t=0, pos, tp=0, tptot=0;

	// Keeps main header's length, takes file format overhead into account
        int cdstreamStart = hd.mainHeadOff; // Codestream offset in the file
        mainHeadLen = in.getPos() - cdstreamStart;
        headLen = mainHeadLen;

        // If ncb and lbody quit conditions are used, headers are not counted
        if(ncbQuit == -1) {
            anbytes = mainHeadLen;
	} else {
            anbytes = 0;
	}

        strInfo += "Main header length    : "+cdstreamStart+", "+mainHeadLen+
            ", "+mainHeadLen+"\n";

        // If cannot even read the first tile-part
        if(anbytes>tnbytes) {
            throw new Error("Requested bitrate is too small.");
        }

        // Read all tile-part headers from all tiles.
	int tilePartStart;
        boolean rateReached = false;
        int mdl;
        int numtp = 0;
        totAllTileLen = 0;
        remainingTileParts = nt; // at least as many tile-parts as tiles
        int maxTP = nt; // If maximum 1 tile part per tile specified

        try {
            while(remainingTileParts!=0) {
              
                tilePartStart = in.getPos();
                // Read tile-part header
		try {
                    t = readTilePartHeader();
                    if(isEOCFound) { // Some tiles are missing but the
                        // codestream is OK
                        break;
                    }
                    tp = tilePartsRead[t];
                    if(isPsotEqualsZero) { // Psot may equals zero for the
                        // last tile-part: it is assumed that this tile-part
                        // contain all data until EOC
                        tilePartLen[t][tp] = in.length()-2-tilePartStart;
                    }
		} catch(EOFException e) {
		    firstPackOff[t][tp] = in.length();
		    throw e;
		}

                pos = in.getPos();

                // In truncation mode, if target decoding rate is reached in
                // tile-part header, skips the tile-part and stop reading
                // unless the ncb and lbody quit condition is in use
                if(isTruncMode && ncbQuit == -1) {
                    if((pos-cdstreamStart)>tnbytes) {
                        firstPackOff[t][tp] = in.length();
                        rateReached = true;
                        break;
                    }
                }

                // Set tile part position and header length
                firstPackOff[t][tp] = pos;
                tilePartHeadLen[t][tp] = (pos-tilePartStart);

                strInfo += "Tile-part "+tp+" of tile "+t+" : "+tilePartStart
                    +", "+tilePartLen[t][tp]+", "+tilePartHeadLen[t][tp]+"\n";

                // Update length counters
                totTileLen[t] += tilePartLen[t][tp];
                totTileHeadLen[t] += tilePartHeadLen[t][tp];
                totAllTileLen += tilePartLen[t][tp];
                if(isTruncMode) {
                    if(anbytes+tilePartLen[t][tp]>tnbytes) {
                        anbytes += tilePartHeadLen[t][tp];
                        headLen += tilePartHeadLen[t][tp];
                        rateReached = true;
                        nBytes[t] += (tnbytes-anbytes);
                        break;
                    } else {
                        anbytes += tilePartHeadLen[t][tp];
                        headLen += tilePartHeadLen[t][tp];
                        nBytes[t] += (tilePartLen[t][tp]-
                                      tilePartHeadLen[t][tp]);
                    }
                } else {
                    if(anbytes+tilePartHeadLen[t][tp]>tnbytes) {
                        break;
                    } else {
                        anbytes += tilePartHeadLen[t][tp];
                        headLen += tilePartHeadLen[t][tp];
                    }
                }
                
                // If this is first tile-part, remember header length
                if(tptot==0)
                    firstTilePartHeadLen = tilePartHeadLen[t][tp];
                
                // Go to the beginning of next tile part
                tilePartsRead[t]++;
                in.seek(tilePartStart+tilePartLen[t][tp]);
                remainingTileParts--;
                maxTP--;
                tptot++;

                // If Psot of the current tile-part was equal to zero, it is
                // assumed that it contains all data until the EOC marker
                if(isPsotEqualsZero) {
                    if(remainingTileParts!=0) {
                        FacilityManager.getMsgLogger().printmsg
                            (MsgLogger.WARNING,"Some tile-parts have not "+
                             "been found. The codestream may be corrupted.");
                    } 
                    break;
                }
            }
        } catch(EOFException e) {
            if(printInfo) {
               FacilityManager.getMsgLogger().
                   printmsg(MsgLogger.INFO,strInfo); 
            }
	    FacilityManager.getMsgLogger().
		printmsg(MsgLogger.WARNING,"Codestream truncated in tile "+t);

	    // Set specified rate to end of file if valid
	    int fileLen = in.length();
	    if(fileLen<tnbytes) {
		tnbytes = fileLen;
		trate = tnbytes*8f/hd.getMaxCompImgWidth()/
		    hd.getMaxCompImgHeight();
	    }

            // Bit-rate allocation
            if(!isTruncMode) {
                allocateRate();
            }

            // Update 'res' value once all tile-part headers are read
            if(pl.getParameter("res")==null) {
                targetRes = decSpec.dls.getMin();
            } else {
                try {
                    targetRes = pl.getIntParameter("res");
                    if(targetRes<0) {
                        throw new
                            IllegalArgumentException("Specified negative "+
                                                     "resolution level "+
                                                     "index: "+targetRes);
                    }
                } catch(NumberFormatException f) {
                    throw new 
                        IllegalArgumentException("Invalid resolution level "+
                                                 "index ('-res' option) " +
                                                 pl.getParameter("res"));
                }
            }
            
            // Verify reduction in resolution level
            mdl = decSpec.dls.getMin();
            if(targetRes>mdl) {
                FacilityManager.getMsgLogger().
                    printmsg(MsgLogger.WARNING,
                             "Specified resolution level ("+targetRes+
                             ") is larger"+
                             " than the maximum value. Setting it to "+
                             mdl +" (maximum value)");
                targetRes = mdl;
            }
            
            // Backup nBytes
            for (int tIdx=0; tIdx<nt; tIdx++) {
                baknBytes[tIdx] = nBytes[tIdx];
            }

            return;
	}
        remainingTileParts = 0;
	
        // Update 'res' value once all tile-part headers are read
        if(pl.getParameter("res") == null) {
            targetRes = decSpec.dls.getMin();
        } else {
            try {
                targetRes = pl.getIntParameter("res");
                if(targetRes<0) {
                    throw new
                        IllegalArgumentException("Specified negative "+
                                                 "resolution level index: "+
                                                 targetRes);
                }
            } catch(NumberFormatException e) {
                throw new 
                    IllegalArgumentException("Invalid resolution level "+
                                             "index ('-res' option) " +
                                             pl.getParameter("res"));
            }
        }
        
        // Verify reduction in resolution level
        mdl = decSpec.dls.getMin();
        if(targetRes>mdl) {
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,
                         "Specified resolution level ("+targetRes+
                         ") is larger"+
                         " than the maximum possible. Setting it to "+
                         mdl +" (maximum possible)");
            targetRes = mdl;
        }

        if(printInfo) {
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
        }

	// Check presence of EOC marker is decoding rate not reached or if
        // this marker has not been found yet
        if(!isEOCFound && !isPsotEqualsZero) {
            try {
                if(!rateReached && !isPsotEqualsZero && in.readShort()!=EOC) {
                    FacilityManager.getMsgLogger().
                        printmsg(MsgLogger.WARNING,"EOC marker not found. "+
                                 "Codestream is corrupted.");
                }
            } catch(EOFException e) {
                FacilityManager.getMsgLogger().
                    printmsg(MsgLogger.WARNING,"EOC marker is missing");
            }
        }

	// Bit-rate allocation
        if(!isTruncMode) {
            allocateRate();
        } else {
            // Take EOC into account if rate is not reached
            if(in.getPos()>=tnbytes)
                anbytes += 2;
        }

        // Backup nBytes
        for (int tIdx=0; tIdx<nt; tIdx++) {
            baknBytes[tIdx] = nBytes[tIdx];
            if(printInfo) {
                FacilityManager.getMsgLogger().
                    println(""+hi.toStringTileHeader(tIdx,tilePartLen[tIdx].
                                                     length),2,2);
            }
        }
    }

    /** 
     * Allocates output bit-rate for each tile in parsing mode: The allocator
     * simulates the truncation of a virtual layer-resolution progressive
     * codestream.
     * */
    private void allocateRate() { 
	int stopOff = tnbytes;

	// In parsing mode, the bitrate is allocated related to each tile's
	// length in the bit stream

        // EOC marker's length 
        anbytes += 2;
            
        // If there are too few bytes to read the tile part headers throw an
        // error
        if(anbytes>stopOff) {
            throw new Error("Requested bitrate is too small for parsing");
        }
        
        // Calculate bitrate for each tile
        int rem = stopOff-anbytes;
        int totnByte = rem;
        for(int t=nt-1; t>0; t--) {
            rem -= nBytes[t]=(int)(totnByte*(totTileLen[t]/totAllTileLen));
        }
        nBytes[0] = rem;
    }

    /** 
     * Reads SOT marker segment of the tile-part header and calls related
     * methods of the HeaderDecoder to read other markers segments. The
     * tile-part header is entirely read when a SOD marker is encountered.
     *
     * @return The tile number of the tile part that was read
     * */
    private int readTilePartHeader() throws IOException {
        HeaderInfo.SOT ms = hi.getNewSOT();
        
        // SOT marker
        short marker = in.readShort();
        if(marker!=SOT) {
            if(marker==EOC) {
                isEOCFound = true;
                return -1;
            } else {
                throw new CorruptedCodestreamException("SOT tag not found "+
                                                       "in tile-part start");
            }
        }
        isEOCFound = false;

        // Lsot (shall equals 10)
        int lsot = in.readUnsignedShort();
        ms.lsot = lsot;
        if(lsot!=10)
            throw new CorruptedCodestreamException("Wrong length for "+
                                                   "SOT marker segment: "+
                                                   lsot); 

        // Isot
        int tile = in.readUnsignedShort();
        ms.isot = tile;
        if(tile>65534){
            throw new CorruptedCodestreamException("Tile index too high in "+
                                                   "tile-part.");
        }

        // Psot
        int psot = in.readInt();
        ms.psot = psot;
        isPsotEqualsZero = (psot!=0) ? false : true;
        if(psot<0) {
            throw new NotImplementedError("Tile length larger "+
					  "than maximum supported");
        }
        // TPsot
        int tilePart = in.read();
        ms.tpsot = tilePart;
        if( tilePart!=tilePartsRead[tile] || tilePart<0 || tilePart>254 ) {
            throw new CorruptedCodestreamException("Out of order tile-part");
        }
	// TNsot
	int nrOfTileParts = in.read();
        ms.tnsot = nrOfTileParts;
        hi.sot.put("t"+tile+"_tp"+tilePart,ms);
        if(nrOfTileParts==0) { // The number of tile-part is not specified in
            // this tile-part header.

            // Assumes that there will be another tile-part in the codestream
            // that will indicate the number of tile-parts for this tile)
            int nExtraTp;
            if(tileParts[tile]==0 || tileParts[tile]==tilePartLen.length ) {
                // Then there are two tile-parts (one is the current and the
                // other will indicate the number of tile-part for this tile)
                nExtraTp = 2;
                remainingTileParts += 1;
            } else {
                // There is already one scheduled extra tile-part. In this
                // case just add place for the current one
                nExtraTp = 1;
            }

            tileParts[tile] += nExtraTp;
            nrOfTileParts = tileParts[tile];
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.WARNING,"Header of tile-part "+tilePart+
                         " of tile "+tile+", does not indicate the total"+
                         " number of tile-parts. Assuming that there are "+
                         nrOfTileParts+" tile-parts for this tile.");

            // Increase and re-copy tilePartLen array
            int[] tmpA = tilePartLen[tile];
            tilePartLen[tile] = new int[nrOfTileParts];
            for(int i=0; i<nrOfTileParts-nExtraTp; i++) {
                tilePartLen[tile][i] = tmpA[i];
            }

            // Increase and re-copy tilePartNum array
            tmpA = tilePartNum[tile];
            tilePartNum[tile] = new int[nrOfTileParts];
            for(int i=0; i<nrOfTileParts-nExtraTp; i++) {
                tilePartNum[tile][i] = tmpA[i];
            }

            // Increase and re-copy firsPackOff array
            tmpA = firstPackOff[tile];
            firstPackOff[tile] = new int[nrOfTileParts];
            for(int i=0; i<nrOfTileParts-nExtraTp; i++) {
                firstPackOff[tile][i] = tmpA[i];
            }

            // Increase and re-copy tilePartHeadLen array
            tmpA = tilePartHeadLen[tile];
            tilePartHeadLen[tile] = new int[nrOfTileParts];
            for(int i=0; i<nrOfTileParts-nExtraTp; i++) {
                tilePartHeadLen[tile][i] = tmpA[i];
            }
        } else { // The number of tile-parts is specified in the tile-part
            // header

            // Check if it is consistant with what was found in previous
            // tile-part headers

            if(tileParts[tile]==0) { // First tile-part: OK
                remainingTileParts += nrOfTileParts- 1;
                tileParts[tile] = nrOfTileParts;
                tilePartLen[tile] = new int[nrOfTileParts];
                tilePartNum[tile] = new int[nrOfTileParts];
                firstPackOff[tile] = new int[nrOfTileParts];
                tilePartHeadLen[tile] = new int[nrOfTileParts];
            } else if(tileParts[tile] > nrOfTileParts ) {
                // Already found more tile-parts than signaled here
                throw new CorruptedCodestreamException("Invalid number "+
                                                       "of tile-parts in"+
                                                       " tile "+tile+": "+
                                                       nrOfTileParts);
            } else { // Signaled number of tile-part fits with number of
                // previously found tile-parts
                remainingTileParts += nrOfTileParts-tileParts[tile];

                if(tileParts[tile]!=nrOfTileParts) {

                    // Increase and re-copy tilePartLen array
                    int[] tmpA = tilePartLen[tile];
                    tilePartLen[tile] = new int[nrOfTileParts];
                    for(int i=0; i<tileParts[tile]-1; i++) {
                        tilePartLen[tile][i] = tmpA[i];
                    }

                    // Increase and re-copy tilePartNum array                
                    tmpA = tilePartNum[tile];
                    tilePartNum[tile] = new int[nrOfTileParts];
                    for(int i=0; i<tileParts[tile]-1; i++) {
                        tilePartNum[tile][i] = tmpA[i];
                    }
                
                    // Increase and re-copy firstPackOff array
                    tmpA = firstPackOff[tile];
                    firstPackOff[tile] = new int[nrOfTileParts];
                    for(int i=0; i<tileParts[tile]-1; i++) {
                        firstPackOff[tile][i] = tmpA[i];
                    }

                    // Increase and re-copy tilePartHeadLen array
                    tmpA = tilePartHeadLen[tile];
                    tilePartHeadLen[tile] = new int[nrOfTileParts];
                    for(int i=0; i<tileParts[tile]-1; i++) {
                        tilePartHeadLen[tile][i] = tmpA[i];
                    }
                }
            }
        }

        // Other markers
        hd.resetHeaderMarkers();
        hd.nTileParts[tile] = nrOfTileParts;
	// Decode and store the tile-part header (i.e. until a SOD marker is
	// found)
        do {
	    hd.extractTilePartMarkSeg(in.readShort(),in,tile,tilePart);
        } while ((hd.getNumFoundMarkSeg() & hd.SOD_FOUND)==0);

	// Read each marker segment previously found
	hd.readFoundTilePartMarkSeg(tile,tilePart);

        tilePartLen[tile][tilePart] = psot;

        tilePartNum[tile][tilePart] = totTilePartsRead;
        totTilePartsRead++;

        // Add to list of which tile each successive tile-part belongs.
        // This list is needed if there are PPM markers used
        hd.setTileOfTileParts(tile);
        
        return tile;
    }

    /** 
     * Reads packets of the current tile according to the
     * layer-resolution-component-position progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution.
     *
     * @param lye Index of the last layer.
     *
     * @param ress Index of the first resolution level.
     *
     * @param rese Index of the last resolution level.
     *
     * @param comps Index of the first component.
     *
     * @param compe Index of the last component.
     *
     * @return True if rate has been reached.
     * */
    private boolean readLyResCompPos(int[][] lys,int lye,int ress,int rese,
				     int comps,int compe) 
	throws IOException {

        int minlys = 10000;
	for(int c=comps; c<compe; c++) { //loop on components
	    // Check if this component exists
            if(c>=mdl.length) continue;

	    for(int r=ress; r<rese; r++) {//loop on resolution levels
		if(lys[c]!=null && r<lys[c].length && lys[c][r]<minlys) {
		    minlys = lys[c][r];
                }
	    }
	}

        int t = getTileIdx();
        int start;
        boolean status = false;
        int lastByte = firstPackOff[t][curTilePart]+
            tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];
        int numLayers = ((Integer)decSpec.nls.getTileDef(t)).intValue();
        int nPrec = 1;
        int hlen,plen;
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;
        boolean pph = false;
        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()) {
            pph = true;
        }
        for(int l=minlys; l<lye; l++) { // loop on layers
            for(int r=ress; r<rese; r++) { // loop on resolution levels
                for(int c=comps; c<compe; c++) { // loop on components
                    // Checks if component exists
                    if(c>=mdl.length) continue;
                    // Checks if resolution level exists
                    if(r>=lys[c].length) continue;
                    if(r>mdl[c]) continue;
                    // Checks if layer exists
                    if(l<lys[c][r] || l>=numLayers) continue;
                    
                    nPrec = pktDec.getNumPrecinct(c,r);
                    for(int p=0; p<nPrec; p++) { // loop on precincts
			start = in.getPos();

                        // If packed packet headers are used, there is no need
                        // to check that there are bytes enough to read header
                        if(pph) {
                            pktDec.readPktHead(l,r,c,p,cbI[c][r],nBytes);
                        }

                        // If we are about to read outside of tile-part,
                        // skip to next tile-part
                        if(start>lastByte && 
                           curTilePart<firstPackOff[t].length-1) {
                            curTilePart++;
                            in.seek(firstPackOff[t][curTilePart]);
                            lastByte = in.getPos()+
                                tilePartLen[t][curTilePart]-1-
                                tilePartHeadLen[t][curTilePart]; 
                        }

                        // Read SOP marker segment if necessary
                        status = pktDec.readSOPMarker(nBytes,p,c,r);

			if(status) {
                            if(printInfo) {
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
                            }
			    return true;
                        }

                        if(!pph) {
                            status = 
                                pktDec.readPktHead(l,r,c,p,cbI[c][r],nBytes);
                        }

			if(status) {
                            if(printInfo) {
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
                            }
			    return true;
                        }

			// Store packet's head length
                        hlen = in.getPos()-start;
			pktHL.addElement(new Integer(hlen));

			// Reads packet's body
			status = pktDec.readPktBody(l,r,c,p,cbI[c][r],nBytes);
                        plen = in.getPos()-start;
                        strInfo+= " Pkt l="+l+",r="+r+",c="+c+",p="+p+": "+
                            start+", "+plen+", "+hlen+"\n";

			if(status) {
                            if(printInfo) {
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
                            }
			    return true;
                        }
                        
                    } // end loop on precincts
                } // end loop on components
            } // end loop on resolution levels
        } // end loop on layers

        if(printInfo) {
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
        }
	return false; // Decoding rate was not reached
    }
    
    /** 
     * Reads packets of the current tile according to the
     * resolution-layer-component-position progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution.
     *
     * @param lye Index of the last layer.
     *
     * @param ress Index of the first resolution level.
     *
     * @param rese Index of the last resolution level.
     *
     * @param comps Index of the first component.
     *
     * @param compe Index of the last component.
     *
     * @return True if rate has been reached.
     * */
    private boolean readResLyCompPos(int lys[][],int lye,int ress,int rese,
				     int comps,int compe) 
        throws IOException {

	int t = getTileIdx(); // Current tile index
	boolean status=false; // True if decoding rate is reached when
        int lastByte = firstPackOff[t][curTilePart]+
            tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];
        int minlys = 10000;
	for(int c=comps; c<compe; c++) { //loop on components
	    // Check if this component exists
            if(c>=mdl.length) continue;

	    for(int r=ress; r<rese; r++) {//loop on resolution levels
                if(r>mdl[c]) continue;
		if(lys[c]!=null && r<lys[c].length && lys[c][r]<minlys) {
		    minlys = lys[c][r];
                }
	    }
	}

        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;
        int numLayers = ((Integer)decSpec.nls.getTileDef(t)).intValue();
        boolean pph = false;
        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()) {
            pph = true;
        }
        int nPrec = 1;
        int start;
        int hlen,plen;
        for(int r=ress; r<rese; r++) { // loop on resolution levels
            for(int l=minlys; l<lye; l++) { // loop on layers
                for(int c=comps; c<compe; c++) { // loop on components
                    // Checks if component exists
                    if(c>=mdl.length) continue;
                    // Checks if resolution level exists
                    if(r>mdl[c]) continue;
                    if(r>=lys[c].length) continue;
                    // Checks if layer exists
                    if(l<lys[c][r] || l>=numLayers) continue;

                    nPrec = pktDec.getNumPrecinct(c,r);

                    for(int p=0; p<nPrec; p++) { // loop on precincts
			start = in.getPos();

                        // If packed packet headers are used, there is no need
                        // to check that there are bytes enough to read header
                        if(pph) {
			    pktDec.readPktHead(l,r,c,p,cbI[c][r],nBytes);
                        }

                        // If we are about to read outside of tile-part,
                        // skip to next tile-part
                        if(start>lastByte && 
                           curTilePart<firstPackOff[t].length-1) {
                            curTilePart++;
                            in.seek(firstPackOff[t][curTilePart]);
                            lastByte = in.getPos()+
                                tilePartLen[t][curTilePart]-1-
                                tilePartHeadLen[t][curTilePart]; 
                        }

                        // Read SOP marker segment if necessary
                        status = pktDec.readSOPMarker(nBytes,p,c,r);

			if(status) {
                            if(printInfo) {
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
                            }
			    return true;
                        }

                        if(!pph) {
			    status = pktDec.
				readPktHead(l,r,c,p,cbI[c][r],nBytes);
                        }

			if(status) {
                            if(printInfo) {
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
                            }
			    // Output rate of EOF reached
			    return true;
                        }

			// Store packet's head length
                        hlen = in.getPos()-start;
			pktHL.addElement(new Integer(hlen));

			// Reads packet's body
			status = pktDec.readPktBody(l,r,c,p,cbI[c][r],nBytes);
                        plen = in.getPos()-start;
                        strInfo+= " Pkt l="+l+",r="+r+",c="+c+",p="+p+": "+
                            start+", "+plen+", "+hlen+"\n";

			if(status) {
                            if(printInfo) {
                                FacilityManager.getMsgLogger().
                                    printmsg(MsgLogger.INFO,strInfo);
                            }
			    // Output rate or EOF reached
			    return true;
                        }
                        
                    } // end loop on precincts
                } // end loop on components
            } // end loop on layers
        } // end loop on resolution levels

        if(printInfo) {
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
        }
	return false; // Decoding rate was not reached
   }
    
    /** 
     * Reads packets of the current tile according to the
     * resolution-position-component-layer progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution.
     *
     * @param lye Index of the last layer.
     *
     * @param ress Index of the first resolution level.
     *
     * @param rese Index of the last resolution level.
     *
     * @param comps Index of the first component.
     *
     * @param compe Index of the last component.
     *
     * @return True if rate has been reached.
     * */
    private boolean readResPosCompLy(int[][] lys,int lye,int ress,int rese,
				     int comps,int compe) 
        throws IOException {
        // Computes current tile offset in the reference grid

        Coord nTiles = getNumTiles(null);
        Coord tileI = getTile(null);
        int x0siz = hd.getImgULX();
        int y0siz = hd.getImgULY();
        int xsiz = x0siz + hd.getImgWidth();
        int ysiz = y0siz + hd.getImgHeight();
        int xt0siz = getTilePartULX();
        int yt0siz = getTilePartULY();
        int xtsiz = getNomTileWidth();
        int ytsiz = getNomTileHeight();
        int tx0 = (tileI.x==0) ? x0siz : xt0siz+tileI.x*xtsiz;
        int ty0 = (tileI.y==0) ? y0siz : yt0siz+tileI.y*ytsiz;
        int tx1 = (tileI.x!=nTiles.x-1) ? xt0siz+(tileI.x+1)*xtsiz : xsiz;
        int ty1 = (tileI.y!=nTiles.y-1) ? yt0siz+(tileI.y+1)*ytsiz : ysiz;
        
        // Get precinct information (number,distance between two consecutive
        // precincts in the reference grid) in each component and resolution
        // level
	int t = getTileIdx(); // Current tile index
        PrecInfo prec; // temporary variable
        int p; // Current precinct index
        int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
        int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
        int nPrec = 0; // Total number of found precincts
        int[][] nextPrec = new int [compe][]; // Next precinct index in each
        // component and resolution level
        int minlys = 100000; // minimum layer start index of each component
        int minx = tx1; // Horiz. offset of the second precinct in the
        // reference grid
        int miny = ty1; // Vert. offset of the second precinct in the
        // reference grid. 
        int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
        int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
        Coord numPrec;
        for(int c=comps; c<compe; c++) { // components
            for(int r=ress; r<rese; r++) { // resolution levels
                if(c>=mdl.length) continue;
                if(r>mdl[c]) continue;
                nextPrec[c] = new int[mdl[c]+1];
                if (lys[c]!=null && r<lys[c].length && lys[c][r]<minlys) {
		    minlys = lys[c][r];
                }
                p = pktDec.getNumPrecinct(c,r)-1;
                for(; p>=0; p--) {
                    prec = pktDec.getPrecInfo(c,r,p);
                    if(prec.rgulx!=tx0) {
                        if(prec.rgulx<minx) minx = prec.rgulx;
                        if(prec.rgulx>maxx) maxx = prec.rgulx;
                    }
                    if(prec.rguly!=ty0) {
                        if(prec.rguly<miny) miny = prec.rguly;
                        if(prec.rguly>maxy) maxy = prec.rguly;
                    }
                    
                    if(nPrec==0) {
                        gcd_x = prec.rgw;
                        gcd_y = prec.rgh;
                    } else {
                        gcd_x = MathUtil.gcd(gcd_x,prec.rgw);
                        gcd_y = MathUtil.gcd(gcd_y,prec.rgh);
                    }
                    nPrec++;
                } // precincts
            } // resolution levels
        } // components
        
        if(nPrec==0) {
            throw new Error("Image cannot have no precinct");
        }
        
        int pyend = (maxy-miny)/gcd_y+1;
        int pxend = (maxx-minx)/gcd_x+1;
        int x,y;
        int hlen,plen;
        int start;
        boolean status = false;
        int lastByte = firstPackOff[t][curTilePart]+
            tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];
        int numLayers = ((Integer)decSpec.nls.getTileDef(t)).intValue();
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;
        boolean pph = false;
        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()) {
            pph = true;
        }
        for(int r=ress; r<rese; r++) { // loop on resolution levels
            y = ty0;
            x = tx0;
            for(int py=0; py<=pyend; py++) { // Vertical precincts
                for(int px=0; px<=pxend; px++) { // Horiz. precincts
                    for(int c=comps; c<compe; c++) { // Components
                        if(c>=mdl.length) continue;
                        if(r>mdl[c]) continue;
                        if(nextPrec[c][r]>=pktDec.getNumPrecinct(c,r)) {
                            continue;
                        }
                        prec = pktDec.getPrecInfo(c,r,nextPrec[c][r]);
                        if((prec.rgulx!=x) || (prec.rguly!=y)) {
                            continue;
                        } 
                        for(int l=minlys; l<lye; l++) { // layers
                            if(r>=lys[c].length) continue;
                            if(l<lys[c][r] || l>=numLayers) continue;
                            
                            start = in.getPos();
                            
                            // If packed packet headers are used, there is no
                            // need to check that there are bytes enough to
                            // read header
                            if(pph) {
                                pktDec.readPktHead(l,r,c,nextPrec[c][r],
                                                   cbI[c][r],nBytes);
                            }

                            // If we are about to read outside of tile-part,
                            // skip to next tile-part
                            if(start>lastByte && 
                               curTilePart<firstPackOff[t].length-1) {
                                curTilePart++;
                                in.seek(firstPackOff[t][curTilePart]);
                                lastByte = in.getPos()+
                                    tilePartLen[t][curTilePart]-1-
                                    tilePartHeadLen[t][curTilePart]; 
                            }

                            // Read SOP marker segment if necessary
                            status = pktDec.readSOPMarker(nBytes,
                                                          nextPrec[c][r],c,r);

                            if(status) {
                                if(printInfo) {
                                    FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                }
                                return true;
                            }

                            if(!pph) {
                                status = 
                                    pktDec.readPktHead(l,r,c,
                                                       nextPrec[c][r],
                                                       cbI[c][r],nBytes);
                            }

                            if(status) {
                                if(printInfo) {
                                    FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                }
                                return true;
                            }

                            // Store packet's head length
                            hlen = in.getPos()-start;
                            pktHL.addElement(new Integer(hlen));
                            
                            // Reads packet's body
                            status = pktDec.readPktBody(l,r,c,nextPrec[c][r],
                                                        cbI[c][r],nBytes);
                            plen = in.getPos()-start;
                            strInfo+= " Pkt l="+l+",r="+r+",c="+c+",p="+
                                nextPrec[c][r]+": "+
                                start+", "+plen+", "+hlen+"\n";

                            if(status) {
                                if(printInfo) {
                                    FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                }
                                return true;
                            }
                        } // layers
                        nextPrec[c][r]++;
                    } // Components
                    if(px!=pxend) {
                        x = minx+px*gcd_x;
                    } else {
                        x = tx0;
                    }
                } // Horizontal precincts
                if(py!=pyend) {
                    y = miny+py*gcd_y;
                } else {
                    y = ty0;
                }
            } // Vertical precincts
        } // end loop on resolution levels

       if(printInfo) {
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
        }
	return false; // Decoding rate was not reached
    }

    /** 
     * Reads packets of the current tile according to the
     * position-component-resolution-layer progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution.
     *
     * @param lye Index of the last layer.
     *
     * @param ress Index of the first resolution level.
     *
     * @param rese Index of the last resolution level.
     *
     * @param comps Index of the first component.
     *
     * @param compe Index of the last component.
     *
     * @return True if rate has been reached.
     * */
    private boolean readPosCompResLy(int[][] lys,int lye,int ress,int rese,
				     int comps,int compe) 
        throws IOException {
        Coord nTiles = getNumTiles(null);
        Coord tileI = getTile(null);
        int x0siz = hd.getImgULX();
        int y0siz = hd.getImgULY();
        int xsiz = x0siz + hd.getImgWidth();
        int ysiz = y0siz + hd.getImgHeight();
        int xt0siz = getTilePartULX();
        int yt0siz = getTilePartULY();
        int xtsiz = getNomTileWidth();
        int ytsiz = getNomTileHeight();
        int tx0 = (tileI.x==0) ? x0siz : xt0siz+tileI.x*xtsiz;
        int ty0 = (tileI.y==0) ? y0siz : yt0siz+tileI.y*ytsiz;
        int tx1 = (tileI.x!=nTiles.x-1) ? xt0siz+(tileI.x+1)*xtsiz : xsiz;
        int ty1 = (tileI.y!=nTiles.y-1) ? yt0siz+(tileI.y+1)*ytsiz : ysiz;
        
        // Get precinct information (number,distance between two consecutive
        // precincts in the reference grid) in each component and resolution
        // level
	int t = getTileIdx(); // Current tile index
        PrecInfo prec; // temporary variable
        int p; // Current precinct index
        int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
        int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
        int nPrec = 0; // Total number of found precincts
        int[][] nextPrec = new int [compe][]; // Next precinct index in each
        // component and resolution level
        int minlys = 100000; // minimum layer start index of each component
        int minx = tx1; // Horiz. offset of the second precinct in the
        // reference grid
        int miny = ty1; // Vert. offset of the second precinct in the
        // reference grid. 
        int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
        int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
        Coord numPrec;
        for(int c=comps; c<compe; c++) { // components
            for(int r=ress; r<rese; r++) { // resolution levels
                if(c>=mdl.length) continue;
                if(r>mdl[c]) continue;
                nextPrec[c] = new int[mdl[c]+1];
                if (lys[c]!=null && r<lys[c].length && lys[c][r]<minlys) {
		    minlys = lys[c][r];
                }
                p = pktDec.getNumPrecinct(c,r)-1;
                for(; p>=0; p--) {
                    prec = pktDec.getPrecInfo(c,r,p);
                    if(prec.rgulx!=tx0) {
                        if(prec.rgulx<minx) minx = prec.rgulx;
                        if(prec.rgulx>maxx) maxx = prec.rgulx;
                    }
                    if(prec.rguly!=ty0) {
                        if(prec.rguly<miny) miny = prec.rguly;
                        if(prec.rguly>maxy) maxy = prec.rguly;
                    }
                    
                    if(nPrec==0) {
                        gcd_x = prec.rgw;
                        gcd_y = prec.rgh;
                    } else {
                        gcd_x = MathUtil.gcd(gcd_x,prec.rgw);
                        gcd_y = MathUtil.gcd(gcd_y,prec.rgh);
                    }
                    nPrec++;
                } // precincts
            } // resolution levels
        } // components
        
        if(nPrec==0) {
            throw new Error("Image cannot have no precinct");
        }
        
        int pyend = (maxy-miny)/gcd_y+1;
        int pxend = (maxx-minx)/gcd_x+1;
        int hlen,plen;
        int start;
        boolean status = false;
        int lastByte = firstPackOff[t][curTilePart]+
            tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];
        int numLayers = ((Integer)decSpec.nls.getTileDef(t)).intValue();
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;
        boolean pph = false;
        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()) {
            pph = true;
        }

        int y = ty0;
        int x = tx0;
        for(int py=0; py<=pyend; py++) { // Vertical precincts
            for(int px=0; px<=pxend; px++) { // Horiz. precincts
                for(int c=comps; c<compe; c++) { // Components
                    if(c>=mdl.length) continue;
                    for(int r=ress; r<rese; r++) { // Resolution levels
                        if(r>mdl[c]) continue;
                        if(nextPrec[c][r]>=pktDec.getNumPrecinct(c,r)) {
                            continue;
                        }
                        prec = pktDec.getPrecInfo(c,r,nextPrec[c][r]);
                        if((prec.rgulx!=x) || (prec.rguly!=y)) {
                            continue;
                        } 
                        for(int l=minlys; l<lye; l++) { // Layers
                            if(r>=lys[c].length) continue;
                            if(l<lys[c][r] || l>=numLayers) continue;

                            start = in.getPos();
                            
                            // If packed packet headers are used, there is no
                            // need to check that there are bytes enough to
                            // read header
                            if(pph) {
                                pktDec.readPktHead(l,r,c,nextPrec[c][r],
                                                   cbI[c][r],nBytes);
                            }

                            // If we are about to read outside of tile-part,
                            // skip to next tile-part
                            if(start>lastByte && 
                               curTilePart<firstPackOff[t].length-1) {
                                curTilePart++;
                                in.seek(firstPackOff[t][curTilePart]);
                                lastByte = in.getPos()+
                                    tilePartLen[t][curTilePart]-1-
                                    tilePartHeadLen[t][curTilePart]; 
                            }

                            // Read SOP marker segment if necessary
                            status = pktDec.readSOPMarker(nBytes,
                                                          nextPrec[c][r],c,r);

                            if(status) {
                                if(printInfo) {
                                    FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                }
                                return true;
                            }

                            if(!pph) {
                                status = 
                                    pktDec.readPktHead(l,r,c,
                                                       nextPrec[c][r],
                                                       cbI[c][r],nBytes);
                            }

                            if(status) {
                                if(printInfo) {
                                    FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                }
                                return true;
                            }

                            // Store packet's head length
                            hlen = in.getPos()-start;
                            pktHL.addElement(new Integer(hlen));
                            
                            // Reads packet's body
                            status = pktDec.readPktBody(l,r,c,nextPrec[c][r],
                                                        cbI[c][r],nBytes);
                            plen = in.getPos()-start;
                            strInfo+= " Pkt l="+l+",r="+r+",c="+c+",p="+
                                nextPrec[c][r]+": "+
                                start+", "+plen+", "+hlen+"\n";

                            if(status) {
                                if(printInfo) {
                                    FacilityManager.getMsgLogger().
                                        printmsg(MsgLogger.INFO,strInfo);
                                }
                                return true;
                            }

                        } // layers
                        nextPrec[c][r]++;
                    } // Resolution levels
                } // Components
                if(px!=pxend) {
                    x = minx+px*gcd_x;
                } else {
                    x = tx0;
                }
            } // Horizontal precincts
            if(py!=pyend) {
                y = miny+py*gcd_y;
            } else {
                y = ty0;
            }
        } // Vertical precincts        
        
        if(printInfo) {
            FacilityManager.getMsgLogger().printmsg(MsgLogger.INFO,strInfo);
        }
	return false; // Decoding rate was not reached
    }

    /** 
     * Reads packets of the current tile according to the
     * component-position-resolution-layer progressiveness.
     *
     * @param lys Index of the first layer for each component and resolution.
     *
     * @param lye Index of the last layer.
     *
     * @param ress Index of the first resolution level.
     *
     * @param rese Index of the last resolution level.
     *
     * @param comps Index of the first component.
     *
     * @param compe Index of the last component.
     *
     * @return True if rate has been reached.
     * */
    private boolean readCompPosResLy(int lys[][],int lye,int ress,int rese,
				     int comps,int compe) 
        throws IOException {
        Coord nTiles = getNumTiles(null);
        Coord tileI = getTile(null);
        int x0siz = hd.getImgULX();
        int y0siz = hd.getImgULY();
        int xsiz = x0siz + hd.getImgWidth();
        int ysiz = y0siz + hd.getImgHeight();
        int xt0siz = getTilePartULX();
        int yt0siz = getTilePartULY();
        int xtsiz = getNomTileWidth();
        int ytsiz = getNomTileHeight();
        int tx0 = (tileI.x==0) ? x0siz : xt0siz+tileI.x*xtsiz;
        int ty0 = (tileI.y==0) ? y0siz : yt0siz+tileI.y*ytsiz;
        int tx1 = (tileI.x!=nTiles.x-1) ? xt0siz+(tileI.x+1)*xtsiz : xsiz;
        int ty1 = (tileI.y!=nTiles.y-1) ? yt0siz+(tileI.y+1)*ytsiz : ysiz;
        
        // Get precinct information (number,distance between two consecutive
        // precincts in the reference grid) in each component and resolution
        // level
	int t = getTileIdx(); // Current tile index
        PrecInfo prec; // temporary variable
        int p; // Current precinct index
        int gcd_x = 0; // Horiz. distance between 2 precincts in the ref. grid
        int gcd_y = 0; // Vert. distance between 2 precincts in the ref. grid
        int nPrec = 0; // Total number of found precincts
        int[][] nextPrec = new int [compe][]; // Next precinct index in each
        // component and resolution level
        int minlys = 100000; // minimum layer start index of each component
        int minx = tx1; // Horiz. offset of the second precinct in the
        // reference grid
        int miny = ty1; // Vert. offset of the second precinct in the
        // reference grid. 
        int maxx = tx0; // Max. horiz. offset of precincts in the ref. grid
        int maxy = ty0; // Max. vert. offset of precincts in the ref. grid
        Coord numPrec;
        for(int c=comps; c<compe; c++) { // components
            for(int r=ress; r<rese; r++) { // resolution levels
                if(c>=mdl.length) continue;
                if(r>mdl[c]) continue;
                nextPrec[c] = new int[mdl[c]+1];
                if (lys[c]!=null && r<lys[c].length && lys[c][r]<minlys) {
		    minlys = lys[c][r];
                }
                p = pktDec.getNumPrecinct(c,r)-1;
                for(; p>=0; p--) {
                    prec = pktDec.getPrecInfo(c,r,p);
                    if(prec.rgulx!=tx0) {
                        if(prec.rgulx<minx) minx = prec.rgulx;
                        if(prec.rgulx>maxx) maxx = prec.rgulx;
                    }
                    if(prec.rguly!=ty0) {
                        if(prec.rguly<miny) miny = prec.rguly;
                        if(prec.rguly>maxy) maxy = prec.rguly;
                    }
                    
                    if(nPrec==0) {
                        gcd_x = prec.rgw;
                        gcd_y = prec.rgh;
                    } else {
                        gcd_x = MathUtil.gcd(gcd_x,prec.rgw);
                        gcd_y = MathUtil.gcd(gcd_y,prec.rgh);
                    }
                    nPrec++;
                } // precincts
            } // resolution levels
        } // components
        
        if(nPrec==0) {
            throw new Error("Image cannot have no precinct");
        }
        
        int pyend = (maxy-miny)/gcd_y+1;
        int pxend = (maxx-minx)/gcd_x+1;
        int hlen,plen;
        int start;
        boolean status = false;
        int lastByte = firstPackOff[t][curTilePart]+
            tilePartLen[t][curTilePart]-1-
            tilePartHeadLen[t][curTilePart];
        int numLayers = ((Integer)decSpec.nls.getTileDef(t)).intValue();
        String strInfo = "Tile "+getTileIdx()+" (tile-part:"+curTilePart+
            "): offset, length, header length\n";;
        boolean pph = false;
        if(((Boolean)decSpec.pphs.getTileDef(t)).booleanValue()) {
            pph = true;
        }

        int x,y;
        for(int c=comps; c<compe; c++) { // components
            if(c>=mdl.length) continue;
            y = ty0;
            x = tx0;
            for(int py=0; py<=pyend; py++) { // Vertical precincts
                for(int px=0; px<=pxend; px++) { // Horiz. precincts
                    for(int r=ress; r<rese; r++) { // Resolution levels
                        if(r>mdl[c]) continue;
                        if(nextPrec[c][r]>=pktDec.getNumPrecinct(c,r)) {
                            continue;
                        }
                        prec = pktDec.getPrecInfo(c,r,nextPrec[c][r]);
                        if((prec.rgulx!=x) || (prec.rguly!=y)) {
                            continue;
                        } 

                        for(int l=minlys; l<lye; l++) { // Layers
                            if(r>=lys[c].length) continue;
                            if(l<lys[c][r]) continue;

                            start = in.getPos();
                            
                            // If packed packet headers are used, there is no
                            // need to check that there are bytes enough to
                            // read header
                            if(pph) {
                                pktDec.readPktHead(l,r,c,nextPrec[c][r],
                                                   cbI[c][r],nBytes);
                            }

                            // If we are about to read outside of tile-part,
                            // skip to next tile-part
                            if(start>lastByte && 
                               curTilePart<firstPackOff[t].length-1) {
                                curTilePart++;
                                in.seek(firstPackOff[t][curTilePart]);
                                lastByte = in.getPos()+
                                    tilePartLen[t][curTilePart]-1-
                                    tilePartHeadLen[t][curTilePart]; 
           
