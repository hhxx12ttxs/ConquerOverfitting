/**
 * A library to interact with Virtual Worlds such as OpenSim
 * Copyright (C) 2012  Jitendra Chauhan, Email: jitendra.chauhan@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package jj2000.j2k.roi.encoder;

import jj2000.j2k.image.*;
import jj2000.j2k.quantization.*;
import jj2000.j2k.wavelet.*;
import jj2000.j2k.wavelet.analysis.*;
import jj2000.j2k.codestream.writer.*;
import jj2000.j2k.util.*;
import jj2000.j2k.roi.*;

/** 
 * This class generates the ROI masks for the ROIScaler.It gives the scaler
 * the ROI mask for the current code-block.
 *
 * <P>The values are calculated from the scaling factors of the ROIs. The
 * values with which to scale are equal to u-umin where umin is the lowest
 * scaling factor within the block. The umin value is sent to the entropy
 * coder to be used for scaling the distortion values.
 *
 * @see RectROIMaskGenerator
 *
 * @see ArbROIMaskGenerator
 *  */
public abstract class ROIMaskGenerator{

    /** Array containing the ROIs */
    protected ROI[] rois;

    /** Number of components */
    protected int nrc;

    /** Flag indicating whether a mask has been made for the current tile */
    protected boolean tileMaskMade[];

    /* Flag indicating whether there are any ROIs in this tile */
    protected boolean roiInTile;

    /** 
     * The constructor of the mask generator
     *
     * @param rois The ROIs in the image
     *
     * @param nrc The number of components
     */
    public ROIMaskGenerator(ROI[] rois, int nrc){
        this.rois=rois;
        this.nrc=nrc;
        tileMaskMade=new boolean[nrc];
    }

    /** 
     * This function returns the ROIs in the image
     *
     * @return The ROIs in the image
     */
    public ROI[] getROIs(){
        return rois;
    }

    /** 
     * This functions gets a DataBlk with the size of the current code-block
     * and fills it with the ROI mask. The lowest scaling value in the mask
     * for this code-block is returned by the function to be used for
     * modifying the rate distortion estimations.
     *
     * @param db The data block that is to be filled with the mask
     *
     * @param sb The root of the current subband tree
     *
     * @param magbits The number of magnitude bits in this code-block
     *
     * @param c Component number
     *
     * @return Whether or not a mask was needed for this tile */
    public abstract boolean getROIMask(DataBlkInt db, Subband sb, 
                                       int magbits, int c); 

    /**
     * This function generates the ROI mask for the entire tile. The mask is
     * generated for one component. This method is called once for each tile
     * and component.
     *
     * @param sb The root of the subband tree used in the decomposition
     *
     * @param magbits The max number of magnitude bits in any code-block
     *
     * @param n component number
     */
    public abstract void makeMask(Subband sb, int magbits, int n);

    /**
     * This function is called every time the tile is changed to indicate
     * that there is need to make a new mask
     */
    public void tileChanged(){
        for(int i=0;i<nrc;i++)
            tileMaskMade[i]=false;
    }

}

