//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.lite.display;

import org.nrg.plexiViewer.lite.xml.Layout;
public class PlexiCoordinates {

	MontageDisplay mDisplay;
	int radiologic;
	String view;
	Point3d bBoxStart, bboxDim, imageDim ; //Bounding box and its dimensions
	Point3d origin;
	int scale;
	int voxelSize;
	int montageIncrement;
	int nMontageColumns;
	Point3d talPos;
	float montageScale;
	boolean nativeSpace;
	
	public PlexiCoordinates(int imgX, int imgY, int imgZ, MontageDisplay mDisplay, String view, boolean radiologic,  int nColumns) {
		bBoxStart = new Point3d();
		Layout l = mDisplay.getLayout(); 
		this.mDisplay = mDisplay;
		if (l.getName().equalsIgnoreCase("TAL_111")) {
			bboxDim = new Point3d(176,208,176);
		}else if(l.getName().equalsIgnoreCase("TAL_222")) {
			bboxDim = new Point3d(128,128,75);
		}else if (l.getName().equalsIgnoreCase("TAL_333")) {
			bboxDim = new Point3d(48,64,48);
		}else {
			bboxDim = new Point3d(imgX, imgY, imgZ);
		}
		imageDim = new Point3d(imgX, imgY, imgZ);
		origin = l.getOrigin();
		scale = 1;
		montageIncrement = mDisplay.getSliceSpacing();
		montageScale = mDisplay.getScale();
		voxelSize = l.getVoxelSize();
		this.radiologic = radiologic?-1:1;
		this.view = view;
		nMontageColumns = nColumns;
		talPos = new Point3d();
		if (l.getName().equalsIgnoreCase("native")) {
			nativeSpace=true;
		}else 
			nativeSpace = false;
	}  
	
	public  void setTalFromStack(int winX, int winY, int slice) {
		winX = (int)(winX / scale);
		winY = (int)(winY / scale);
		
		if (nativeSpace) {
			talPos.x = winX;
			talPos.y = winY;
			talPos.z = slice;
		}else {	    
		   //Note: the calculation for z in the coronal and sagittal views is a bit strange b/c
		   //the origin is specified from the _bottom_ of the box, while winY starts from the _top_ of the box
		   if (view.equalsIgnoreCase("TRANSVERSE")) { 
				 talPos.x =  ( radiologic * (winX - origin.x)  * voxelSize );
				 talPos.y =  ( (origin.y - winY)  * voxelSize );
				 talPos.z =  ( ( slice - origin.z) * voxelSize );
		   }else if (view.equalsIgnoreCase("CORONAL")) {
				talPos.x =  ( radiologic * (winX - origin.x)  * voxelSize );
				talPos.y =  ( ( (bboxDim.z - origin.z) - winY ) * voxelSize );
				talPos.z =  ( (origin.y - slice) * voxelSize );
		   }else if (view.equalsIgnoreCase("SAGITTAL")) {
				talPos.x =  ( (origin.y - winX ) * voxelSize );
				talPos.y =  ( ( (bboxDim.z - origin.z) - winY ) * voxelSize );
				talPos.z =  ( (slice - origin.x) * voxelSize );
		   }
		}  
	}
	
	public void setTalFromMontage(int winX, int winY) {
		int x = 0, y = 0, z = 0, sliceNum = 0;
		
		winX = (int)(winX / scale);
		winY = (int)(winY / scale);
		int montageStartSlice = mDisplay.getStartSlice();
		if (nativeSpace) {	
			int mSliceNum, row, col;
			float rowHeight, colWidth;
			rowHeight = (montageScale * bboxDim.y);
			colWidth = (montageScale * bboxDim.x); 
			row = (int)Math.floor(winY / rowHeight);
			col = (int)Math.ceil(winX / colWidth);
			mSliceNum =montageStartSlice + ( row * nMontageColumns + col -1) * montageIncrement;
			//System.out.println("WinX " + winX + "\t " + winY);
			//System.out.println("Slice Num " + SliceNum + "\t Row " + row + "\t Col " + col);
			//System.out.println("Row  Height " + rowHeight + "\t Col Width " + colWidth);
			talPos.z = mSliceNum;
			talPos.x = winX - (col-1)*colWidth;
			talPos.y = winY - row*rowHeight;
			talPos.x=talPos.x/montageScale;
			talPos.y=talPos.y/montageScale;
			//System.out.println("TalX " + talPos.x + "\t Y " + talPos.y + "\t Z " + talPos.z);
		}else {
				  //Note: the calculation for z in the coronal and sagittal views is a bit strange b/c the origin is specified from the
				  //_bottom_ of the box, while winY starts from the _top_ of the box
	        
			if (view.equalsIgnoreCase("TRANSVERSE")) {
					sliceNum = ( (int)Math.floor(winY /(montageScale * bboxDim.y)) * nMontageColumns) + ((int)Math.ceil(winX /(montageScale * bboxDim.x)));
					int bboxDimXY = (int)(bboxDim.x * montageScale);
					talPos.z = ( ( montageStartSlice + (montageIncrement * (sliceNum-1))  - origin.z)  * voxelSize );
					x =  (winX) % (int)bboxDimXY;                            //remove offset from previous slices in row
					talPos.x = ( radiologic * ( (int)(x/montageScale) - origin.x ) * voxelSize );
					bboxDimXY = (int)(bboxDim.y * montageScale);					
					y = (winY % (int)bboxDimXY);
					talPos.y = ( (origin.y -  (int)(y/montageScale)) * voxelSize );
		  } else if (view.equalsIgnoreCase("CORONAL")){
				sliceNum = ( (int)Math.floor(winY /(montageScale * bboxDim.z)) * nMontageColumns) + ((int)Math.ceil(winX / (montageScale*bboxDim.x)));
				talPos.z = ( ( origin.y - ( montageStartSlice + (montageIncrement * (sliceNum-1))) ) * voxelSize );
				int bboxDimXY = (int)(bboxDim.x * montageScale);
				x = winX % (int)bboxDimXY;
				talPos.x = ( radiologic * ((int) (x/montageScale) - origin.x ) * voxelSize );
				bboxDimXY = (int)(bboxDim.z * montageScale);
				z = winY %(int) bboxDimXY;
				talPos.y = ( -((int)(z/montageScale) + (origin.z - bboxDim.z) ) * voxelSize );
				
		  } else if (view.equalsIgnoreCase("SAGITTAL")){
				sliceNum = ( (int)Math.floor(winY / (montageScale*bboxDim.z)) * nMontageColumns) + ((int)Math.ceil(winX / (bboxDim.y*montageScale)));
				talPos.z = ( ( (montageStartSlice + (montageIncrement * (sliceNum-1))) - origin.x ) * voxelSize );
				int bboxDimXY = (int)(bboxDim.y * montageScale);
				y = winX % (int)bboxDimXY;
				talPos.x = ( (origin.y -  y/montageScale) * voxelSize );
				bboxDimXY = (int)(bboxDim.z * montageScale);
				z = winY % (int)bboxDimXY;
				talPos.y =  ((bboxDim.z - origin.z) - (int)(z/montageScale) ) * voxelSize ;
		  }
		}  
	  //System.out.println("WinX " + winX + " winY " + winY + "TAL " + talPos.toString());
	}
	
	public void setTalFromSlice(int slice) {
		//slice = slice - 1;
			if (nativeSpace) {
				talPos.z = slice;
			}
			else {
				if (view.equalsIgnoreCase("TRANSVERSE"))
					talPos.z = ( (slice - origin.z) * voxelSize );
				else if (view.equalsIgnoreCase("CORONAL"))
					talPos.z = ( (origin.y - slice ) * voxelSize );
				else if (view.equalsIgnoreCase("SAGITTAL"))
					talPos.z = ( (slice - origin.x ) * voxelSize );
			}		
	}

	
	
	public Point3d getPosWindow(String display, String fromView, boolean fromRadiologic ){
		int x = 0, y = 0, z=0;
		int montageStartSlice = mDisplay.getStartSlice();
		
		if (display.equalsIgnoreCase("STACK")) {
			if (nativeSpace) {
				Point3d rtn = PlexiNativeCoordinates.getNativeCoordinates(talPos, fromView, view, (radiologic==-1), imageDim, fromRadiologic, this.scale);
				//System.out.println(radiologic + " NATIVE STACK " + rtn.toString());
				return rtn;	
			}else {
				Point3d rtn = PlexiNativeCoordinates.getTalCoordinates(talPos, fromView, view, radiologic,bboxDim, imageDim, origin, voxelSize, this.scale, fromRadiologic);
				return rtn;
			}						
		}else if (display.equalsIgnoreCase("MONTAGE")) {
		   int nBox=0, nRow, nColumn;
			if (nativeSpace) {
				nBox =  (int) ((montageStartSlice - talPos.z) / Math.abs(montageIncrement) );
				nRow = nBox / nMontageColumns;
				nColumn = nBox - (nRow * nMontageColumns) - 1;
				System.out.println("Reverse Comp BOX: " + nBox + "\t Row " + nRow + "\t Col " + nColumn );
				x = (int)(nColumn * bboxDim.x) + (int)(talPos.x / voxelSize);
				y = (int)(nRow * bboxDim.y) + (int)(talPos.y  / voxelSize);
				z = (int)talPos.z;
				return PlexiNativeCoordinates.getNativeCoordinates(x,y,z, fromView, view, (radiologic==-1), imageDim, fromRadiologic, this.scale);
				
			}else {
			   if (view.equalsIgnoreCase("TRANSVERSE")){
				   nBox =  (int) ((montageStartSlice - talPos.z/voxelSize - origin.z) / Math.abs(montageIncrement) );
				   nRow = nBox / nMontageColumns;
				   nColumn = nBox - (nRow * nMontageColumns);
				   x = (int)((nColumn * bboxDim.x) + (int)(origin.x + (talPos.x / voxelSize)));
				   y = (int)((nRow * bboxDim.y) + (int)(origin.y - (talPos.y  / voxelSize)));
			   }else if (view.equalsIgnoreCase("CORONAL")) {
					nBox =  (int)(-(montageStartSlice + talPos.z/voxelSize - origin.y) / Math.abs(montageIncrement) );
					nRow = nBox / nMontageColumns;
					nColumn = nBox - (nRow * nMontageColumns);
					x = (int)((nColumn * bboxDim.x) + (int)(origin.x + (talPos.x / voxelSize)));
					y = (int)((nRow * bboxDim.z) - (int)((talPos.y  / voxelSize) + origin.z  - bboxDim.z));
			   }else if (view.equalsIgnoreCase("SAGITTAL")) {
					nBox =  (int)((talPos.z/voxelSize - montageStartSlice + origin.x) / Math.abs(montageIncrement) );
					nRow = nBox / nMontageColumns;
					nColumn = nBox - (nRow * nMontageColumns);
					x = (int)((nColumn * bboxDim.y) + (int)(origin.y - (talPos.x / voxelSize)));
					y = (int)((nRow * bboxDim.z) - (int)((talPos.y  / voxelSize) + origin.z  - bboxDim.z));
			   }
			}  
		   z = nBox;
		}
	   //System.out.println("Display is "+ display + " View is " + view + "WinX " + x + " winY " + y + "TAL " + talPos.toString()); 
	   Point3d p = new Point3d(x*scale,y*scale,z);
	   return p;
	}
	
	
	
	//	return current tailarach coords
	  public Point3d getPosTal() { return talPos; }
	  
	public void setTalFromTal(Point3d p){
			talPos.set(p);
	}
    
	public void setTalFromTal(int x, int y, int z){
		   talPos.set(x,y,z);
   }
	

	/**
	 * @param point3d
	 */
	public void setBboxDim(Point3d point3d) {
		bboxDim = point3d;
	}

	
	/**
	 * @return
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * @param i
	 */
	public void setScale(int i) {
		scale = i;
	}

}



