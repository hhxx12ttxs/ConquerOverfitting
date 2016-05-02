package ptilemaputility;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import org.gicentre.utils.spatial.*;    // For map projections.

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;



public class PTileMapUtility extends PApplet {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	PImage img;
	tileType t;
	String cornerTile;
	String rowCornerTile;
	
	
	int tileCollectionSize = 8; // Recommended size 2, 4, 8
	
	int c = 2;
	int superCount = 0;
	int superEnd = 40;
	int superRow = 8;
	
	String prefix;
	int prefixAdd = 99; // change
	
	
	public void setup() {
		
		// suvaline muudatus
		// http://smalltowngeeks.net/2011/02/13/subversion-set-up-configuring-bit-bucket-with-eclipse/
		// noh veel midagi?
		
		size(200, 200);
		
		
		 frameRate(10);
		 
		 geoConverting();
		 //
		 
		
		 
		 
		 
		/**
		combineTiles(3,2,2, 
				
				"D:\\9083.data\\Satellite_Image\\Level_7\\",
				"jpg", 
				"0311000",
				"D:\\9083.data\\Satellite_Image\\EU_out1.jpg"  
				
				);  // EU
		
		
		 exit();
		 **/
		 
		 
		size(256, 256);
		DownloadSetup(); // download tiles 
		//size(2048 ,2048); // works with tileCollectionSize = 8
		 
		  
		
		
	}


	
	public void draw() {
		
		//DownloadImage();
		DownloadOneTile();
		
		
	}
	
	
	public void geoConverting(){
		
		
		// random comment
		
		WebMercator proj = new WebMercator();
		PVector projCoord;
		PVector pMercator;
		PVector pGeo;
		
		// convert Geo Coordinates to Mercator
		
		 pGeo  = new PVector(24.80071f, 59.4164f); // where -20 is longitude and 60 is latitude
		 println("WCS (long, lat) = " + pGeo.x + " " + pGeo.y);
		 pMercator = proj.transformCoords(pGeo);
		 println("Mercator (X, Y): = " + pMercator.x+","+pMercator.y);  
		 
		 
		 // convert Mercator to Geo
		 
		pMercator = new PVector(-2348145.5f, -1878516.4f); // where X is left to right and Y is up down
		println("Mercator (X, Y): = " + pMercator.x+","+pMercator.y);  
		pGeo =  proj.invTransformCoords(pMercator);
		println("WCS (long, lat) =" + pGeo.x+","+pGeo.y);  
		
		
		
		// get Mercator from tile name
		
		//1234567
		//0310000
		
		tileType t = new tileType("3133111");
		t.moveDown(8);
		
		pMercator = t.getTileUpLeftCoordinate();
		println("Mercator (X, Y): = " + pMercator.x+","+pMercator.y);  
		pGeo =  proj.invTransformCoords(pMercator);
		println("WCS (long, lat) =" + pGeo.x+","+pGeo.y);  
		
		
		
		
	}

	public void combineTiles(int _leftToRight, int _uptodown, int _outputScale, String _tileSrc,String _tileFormat, String tileIndex, String outputPath){
		
		
		// kui on soov kokku panna tilidest maailm
		
		
		int startRow = 0;
		int endHeight = height;
		int endWidht = width;
		
		
		// -------------------------------------------------------------------------
		
		
		int scale = _outputScale;  // 1 = max size and all larger numbers are makeing it smaller
		int tilesFromLeftToRight = _leftToRight;
		int tilesFromUptoDown = _uptodown;
		
		// size of the image 
		// image.width = 2048 / scale * tilesFromLeftToRight
		// image.height = 2048 / scale * tilesFromUptoDown
		
		
		//String upleftCornerTile = "120120230111320";
		//String tileAddress = "D:\\9083.data\\Satellite_Image\\Level_15\\";
		//String outputImage = "D:\\9083.data\\Satellite_Image\\vabalinn_f2.jpg";
		
		//String upleftCornerTile = "1200310200";
		//String tileAddress = "D:\\9083.data\\Satellite_Image\\Level_10\\";
		//String outputImage = "D:\\9083.data\\Satellite_Image\\overEstonia_6by4.jpg";
		
		String upleftCornerTile = tileIndex;
		String tileAddress = _tileSrc;
		String outputImage = outputPath;
		//
		
		// -------------------------------------------------------------------------
		
		
		
		size(2048/scale * tilesFromLeftToRight,2048/scale *tilesFromUptoDown);
		
		float tileSize = width / tilesFromLeftToRight; 
		
		String mostRightBottom;
		
		println("tileSize = " + tileSize);
		println("size  = (" + width + ", " + height + ")");
		println();

		PImage p;
		
		tileType t = new tileType(upleftCornerTile); // Ülemine vasak

		String tSave = t.tileName;
	
		println("Upper Left Corner tile = " + t.tileName);
			
		PVector pt = t.getTileUpLeftCoordinate();
		println("long_start = " + pt.x);
		println("lat_start =  " + pt.y);
		
		
		println();
		
		
		mostRightBottom  =t.tileName;
		
		for ( int i = 0; i < tilesFromUptoDown; i++) {
			
			// rows
			
			tSave = t.tileName;
			
			for (int j = 0; j < tilesFromLeftToRight; j++) {
				
				p = loadImage(tileAddress + t.tileName + "." + _tileFormat);
				
				image(p, j*tileSize, i*tileSize, tileSize, tileSize);
				t.moveRight(8);
				
				//println("( " + nf(i,3) + " , " + nf(j,3) + " ) = " + t.tileName);

				// cols
				
				
			}
			
			mostRightBottom = t.tileName;
						
			t.setByName(tSave);
			t.moveDown(8);
			
			
			
		}
		
		
		t.setByName(mostRightBottom);
		
		println("Bottom Right Corner tile (inside)  = " + mostRightBottom);
		
		t.moveRight(8);
		t.moveDown(8);
		
		println("Bottom Right Corner tile (outside) = " + t.tileName);
		pt = t.getTileUpLeftCoordinate();
		println("long_end = " + pt.x);
		println("lat_end =  " + pt.y);
		
		saveFrame(outputImage);
		

		
		
		
	}
	
	public void DownloadSetup(){
		
		
		// USER
		
		
		 //rowCornerTile = "12012"; 
		 rowCornerTile = "033302010";
		 rowCornerTile = "210131323";
		 rowCornerTile = "311023131";
		 //31102313120
		 //rowCornerTile = "0000000";
		 //               123456789012345
		 rowCornerTile = "120120230111320"; //level 15
		 
		 //1234567890 Level_10 around estonia
		 //1200312132
		 //1200310200
		 rowCornerTile = "1200310200"; 
		 
		 
		 
		 // over estonia Level_11
		 rowCornerTile = "12003110231"; 
		 
		 
		// over world
		rowCornerTile = "2020000"; 
		superCount = 0;
		superEnd = 16384;
		superRow = 128; 
		tileCollectionSize = 1;
		 
		 // SYSTEM
		 
		 t = new tileType(rowCornerTile);


		 
		
	}
	
	
	public void DownloadOneTile(){
		
		if (superCount == superEnd ) {
			
			exit();
			
		}
		
		c--;
		
		if (c < 0) {
			c= 3 + floor(random(5)); // delay in sec's
			

			println("element : " + nf(floor(superCount / superRow)+prefixAdd, 2));
			

			// move from one row to an other one
			if (superCount % superRow == 0) {
				
				println( "superCount : " + superCount);
				println( "row : " + superCount % superRow);
				
				if (superCount > 0) {
					
					
					println("move row down");
					println( "rowCornerTile : " + rowCornerTile);
					
					t.setByName(rowCornerTile);
					t.moveDown(tileCollectionSize);					
					rowCornerTile = t.tileName;
					
					
				}
				
				
				
			}
			
			
			
			
			
			// download tiles
			
			cornerTile = t.tileName; //save corner
			//println( "cornerTile : " + cornerTile);
			
			for (int j = 0; j < tileCollectionSize; j++){

				for (int i = 0; i < tileCollectionSize; i++){
					
					//println("j = " + j + ", i = " + i + " : "+ t.tileName + " = "+ t.getBingAddress());
					
					
					println( "Long: " + t.tileLong + " Lat: " + t.tileLat );
					println( t.tileName + " \t "+ t.getBingAddress());
	
					
					img = loadImage(t.getBingAddress());
					image(img, 256*i, 256*j);
					t.moveRight(1);
					
				}
			
				t.moveLeft(tileCollectionSize);
				t.moveDown(1);
			
			}
			
			prefix = "R" +  nf(floor(superCount / superRow)+prefixAdd,2) + "-"; 
			
			// save image
			//save("D:\\9083.data\\bingmap\\dump\\" + superCount + "-" + cornerTile.substring(0, cornerTile.length()-2) + ".jpg");
			saveFrame("D:\\9083.data\\Satellite_Image\\Level_7\\" + cornerTile.substring(0, cornerTile.length()-0) + ".png");
			
			// go back to original corner
			
			t.setByName(cornerTile);
			t.moveRight(tileCollectionSize);
			
			superCount++;
			


			
		}
		
		
		
		
		
	}
	
	
	public void DownloadImage(){
		

		println("delay = " + c);
		
		
		if (superCount == superEnd ) {
			
			exit();
			
		}
		
		
		c--;
		
		if (c < 0) {
			c= 3 + floor(random(5)); // delay in sec's
			

			println("element : " + nf(floor(superCount / superRow)+prefixAdd, 2));
			

			// move from one row to an other one
			if (superCount % superRow == 0) {
				
				println( "superCount : " + superCount);
				println( "row : " + superCount % superRow);
				
				if (superCount > 0) {
					
					
					println("move row down");
					println( "rowCornerTile : " + rowCornerTile);
					
					t.setByName(rowCornerTile);
					t.moveDown(tileCollectionSize);					
					rowCornerTile = t.tileName;
					
					
				}
				
				
				
			}
			
			
			
			
			
			// download tiles
			
			cornerTile = t.tileName; //save corner
			println( "cornerTile : " + cornerTile);
			
			for (int j = 0; j < tileCollectionSize; j++){

				for (int i = 0; i < tileCollectionSize; i++){
					
					println("j = " + j + ", i = " + i + " : "+ t.tileName);
							
					
					img = loadImage(t.getBingAddress());
					image(img, 256*i, 256*j);
					t.moveRight(1);
					
				}
			
				t.moveLeft(tileCollectionSize);
				t.moveDown(1);
			
			}
			
			prefix = "R" +  nf(floor(superCount / superRow)+prefixAdd,2) + "-"; 
			
			// save image
			//save("D:\\9083.data\\bingmap\\dump\\" + superCount + "-" + cornerTile.substring(0, cornerTile.length()-2) + ".jpg");
			saveFrame("D:\\9083.data\\Satellite_Image\\Level_11\\" + cornerTile.substring(0, cornerTile.length()-0) + ".png");
			
			// go back to original corner
			
			t.setByName(cornerTile);
			t.moveRight(tileCollectionSize);
			
			superCount++;
			


			
		}
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	public int[] binarySubtruction(int[] _array){
		
		// input : array of binary number
		
		// function substrucst one number from array. function do not go negative.
		
		// output: array of subtructed or smallest array. 
		
		
		if (_array[_array.length-1] == 1) {
			
			_array[_array.length-1] = 0;
			
			return _array;
						
		} else {
			
						
			for (int i = _array.length-1; i >= 0; i--){
				
				if (_array[i] == 1) {
					

					_array[i] = 0;
					
					for (int j = i+1; j <_array.length; j++){
					
						_array[j] = 1;
					
					}
										
					return _array;
					
				}
				
			}
			

		}
		
		
		return _array;
	}
	
	public int[] binaryAddition(int[] _array){
		
			
			for (int i = _array.length-1; i >= 0; i--){
				
				if (_array[i] == 0) {
					
					_array[i] = 1;
					
					
					for (int j = i+1; j < _array.length; j++) {
						
						_array[j] = 0;
						
					}
					
					return _array;
					
				}
				
			}
			

		
		return _array;
	}
	

	public int[] binaryReverse(int[] _array){
		
			int[] reverseArray = new int[_array.length];

			for (int i = _array.length-1; i >= 0; i--){
								
				reverseArray[_array.length-1 -i] = _array[i];
			}
			
		return reverseArray;
	}
	
	public int[] binaryInvert(int[] _array){
		
		for (int i = _array.length-1; i >= 0; i--){
							
			_array[i] = (_array[i] + 1) % 2;
		}
		
	return _array;
}
	
	public int[] binaryTrim(int[] _array, int _start, int _stop){
		
		
		int[] _arrayTrimed = new int[_stop - _start];
		int c=0;
		for (int i = _start; i < _stop; i++){
			
			_arrayTrimed[c] = _array[i];
			c++;
			
			
			
		}
		
		return _arrayTrimed;
		
		
		
	}
	
	public int[] binarySize(int[] _array, int _size){
		
		int[] _returnArray =  new int[_size];
		int _c = 0;
		
		for (int i = _size-1; i >=0; i--){
			
			if (_c <  _array.length) {
				
				_returnArray[i] = _array[_array.length - _c -1];
				
			} else {
				
				
				_returnArray[i] = 0;
			}
			
			
			_c++;
			
		}
		

		return _returnArray;
		
	}
	
	public int binarytoInt(int[] _array){
		
		int val =0;
		
		//println(_array);
		
		for (int i = 0; i < _array.length; i++){
			
			//println("val = " + val);
			
			val  += pow(2,i) * _array[_array.length -1 - i];

			
		}
		
		
		return val;
		
		
	}
	
	public int[] binaryFromInt(int _int){
		
		
		int[] _array = new int[0];
		
		int i = _int;
		int iLeftover;
		int iCurrent;
		String iCollection = "";
		
		println(" _int = " + _int);
		
		if (i == 0 ) {
			
			_array = new int[1];
			_array[0] = 0;
			
			
		} else {
		
		
			while (i > 0){
				
				iCurrent = i;
				i = floor(i/2);
				iLeftover = iCurrent % 2;
								
				_array = expand(_array,  _array.length+1);
				_array[_array.length-1] = iLeftover;
				
			}
			
		}
		
		_array = binaryReverse(_array);
		
		println(_array);
		
		return _array;
				
		
	}
	
	
	
	
	
	
	
	
	public class tileMapSystem{
		
		// Caching
		// Calculating
		// Drawing
		
		tileMapSystem(){
			
			
			
			
			
		}
		
		
		
		
		
	}
		
	public class tileType{
		
		// pre req:
		
		// binarySubtruction
		// binaryAddition
		// binaryReverse
		// binaryInvert
		// binaryTrim
		// binarySize
		// binarytoInt
		// binaryFromInt
		
		
		String tileName;
		int[] tileLong; // left to right
		int[] tileLat; // top to down

		double worldSize = 40075016.704;
	
		
		int tileSizeInPixels; // 256, 512, 1024, 2048
		
		
		tileType(String _tileName){
			
			
			setByName(_tileName);
			
			
		}
		
		
		// setters
		
		void setByName(String _tileName) {
			
			tileName = _tileName;
			
			tileLong = new int[tileName.length()];
			tileLat = new int[tileName.length()];
			
			String _tileSub;
			
			
			for (int i = 0; i < tileName.length(); i++){
				
				_tileSub = tileName.substring(i, i+1);
				
				if (_tileSub.equals("0")) {
					
					tileLong[i] = 0;
					tileLat[i] = 0;
										
				}
				
				if (_tileSub.equals("1")) {
					
					tileLong[i] = 1;
					tileLat[i] = 0;
										
				}
				
				if (_tileSub.equals("2")) {
					
					tileLong[i] = 0;
					tileLat[i] = 1;
										
				}
				
				if (_tileSub.equals("3")) {
					
					tileLong[i] = 1;
					tileLat[i] = 1;
										
				}
				
			}
			
		}
		
		String setByWorldCoordinates(double _XMercInMeters, double _YMercInMeters, int _level){
			
			// sets tile what contains this following coordinate 
			

			
			int[] tileXAddress = new int[0];
			int[] tileYAddress = new int[0];
			
			
			int tilesFromCenter = -1;
			
			int[] tileAddressInvert = new int[0];
			int[] tileAddressInvertLong = new int[0];
			int[] tileAddressRight = new int[0];
			int[] tileAddressRightLong = new int[0];
		
			
			tilesFromCenter = ceil( (float)  (_XMercInMeters * pow(2, _level-1) / (worldSize/2))); // potential error place!
			
			println("tilesFromCenter = " +tilesFromCenter);
			
			if ( tilesFromCenter < 0 ) {
				
				tilesFromCenter*= -1;
				tilesFromCenter--;
				tileAddressInvert = binaryFromInt(tilesFromCenter);
				
				println("tileAddressInvert");
				println(tileAddressInvert);
				println("");
				
				tileAddressInvertLong = binarySize(tileAddressInvert, _level-1);
				println("tileAddressInvertLong");
				println(tileAddressInvertLong);
				println("");

				
				tileAddressRight = binaryInvert(tileAddressInvertLong);
				
				println("tileAddressRight");
				println(tileAddressRight);
				println("");
				

				tileAddressRightLong = binarySize(tileAddressRight, _level);
				println("tileAddressRightLong");
				println(tileAddressRightLong);
				println("");
				
				
			
			} else {
				
				
				tileAddressRight = binaryFromInt(tilesFromCenter);
				println("tileAddressRight");
				println(tileAddressRight);
				println();
				
				tileAddressRightLong = binarySize(tileAddressRight, _level);
				println("tileAddressRightLong");
				println(tileAddressRightLong);
				println();
				
				tileAddressRightLong[0] = 1;
				println("tileAddressRightLong_new");
				println(tileAddressRightLong);
				println();
				
				
			}
			
			tileLong = tileAddressRightLong;
			
			
			
			// Latitude
			
			// TODO: i think here is some problem with - sign
			
			
			tilesFromCenter = ceil( (float)  (_YMercInMeters * pow(2, _level-1) / (worldSize/2)));
			
			if ( tilesFromCenter < 0 ) {
				
				tilesFromCenter*= -1;
				tilesFromCenter--;
				tileAddressInvert = binaryFromInt(tilesFromCenter);
				tileAddressInvertLong = binarySize(tileAddressInvert, _level-1);
				tileAddressRight = binaryInvert(tileAddressInvertLong);
				tileAddressRightLong = binarySize(tileAddressRight, _level);
				
			
			} else {
				
				
				tileAddressRight = binaryFromInt(tilesFromCenter);
				tileAddressRightLong = binarySize(tileAddressRight, _level);
				tileAddressRightLong[0] = 1;
				
			}
			

			tileLat = tileAddressRightLong;
			
			updateName();
			
			return tileName;			
			
		}
		
		
		// getters
		
		
		PVector getTileUpLeftCoordinate(){
			
			// Returns Upper Left Corner coordinate in Mercator Projection [m]
			// where X is longitude
			// where Y is latitude
			
			
			PVector _return;
			
			int level = tileLong.length;
					
			int[] tileAddressTrim;
			int[] tileAddressInverse;
			int tileAddressPos = 0;
			
			
			// Longitude
			
			if (tileLong[0] == 0) {
				

				tileAddressTrim = binaryTrim(tileLong, 1, tileLong.length); // we skip direction part. 
				
				tileAddressInverse = binaryInvert(tileAddressTrim); // we want to start counting tiles from center not from left edge
				
				tileAddressPos = binarytoInt(tileAddressInverse); // we want to know how many tiles there are from center.
				
				tileAddressPos++; // without that we will get right side. now it is left side
				tileAddressPos *= -1; // since it is from right to left we need to give that negative direction
				
			} else {
				
				tileAddressTrim = binaryTrim(tileLong, 1, tileLong.length);
				tileAddressPos = binarytoInt(tileAddressTrim);
								
			}
			
			
			// worldSize/2  		is quarter of the world and maximum size for longitude and latitude
			// pow(2, (level-1))	amount of tiles in this zoom level
			// tileAddressPos		is left/top side
			
			double X = tileAddressPos * (worldSize/2) / pow(2,(level-1) ); // [m] in Mercator.
			

			// latitude
			

			if (tileLat[0] == 0) {
				

				tileAddressTrim = binaryTrim(tileLat, 1, tileLat.length); // we skip direction part. 
				tileAddressInverse = binaryInvert(tileAddressTrim); // we want to start counting tiles from center not from left edge
				tileAddressPos = binarytoInt(tileAddressInverse); // we want to know how many tiles there are from center.
				tileAddressPos++; // without that we will get right side. now it is left side
				
			} else {

				tileAddressTrim = binaryTrim(tileLat, 1, tileLat.length);
				tileAddressPos = binarytoInt(tileAddressTrim);
				//TODO: make sure that is right
				tileAddressPos *= -1; // since it is from right to left we need to give that negative direction
				
			}
			
			
			
			// worldSize/2  		is quarter of the world and maximum size for longitude and latitude
			// pow(2, (level-1))	amount of tiles in this zoom level
			// tileAddressPos		is left/top side
			
			double Y = tileAddressPos * (worldSize/2) / pow(2,(level-1) ); // [m] in Mercator.
			
			_return = new PVector((float)  X, (float)  Y);
			
			return _return;
			
		}

		String getBingAddress(){
			
			//http://ecn.t0.tiles.virtualearth.net/tiles/h120122010220.jpeg?g=774&mkt=en-us&n=z
			
			/**
			
			
			http://ecn.t0.tiles.virtualearth.net/tiles/h120122010220.jpeg?g=774&mkt=en-us&n=z

			t0 where 0 is correlating with h120122010220

			h12012201022
			a12012201022

			h for hybrid and a for area

			g=774, bit random


			 */
			
			String _url;
			
			_url = "http://ecn.t" + tileName.substring(tileName.length()-1) + ".tiles.virtualearth.net/tiles/a" + tileName + ".jpeg?g=77" + floor(random(10)) + "&mkt=en-us&n=z";
			
			return _url;
			
			
			
			
		}

		
		// functions
		
		void moveLeft(int _i){
			
			for (int i = 0; i < _i; i++){
				
				tileLong =  binarySubtruction(tileLong);	
				
			}
					
			
			updateName();
			
		}

		void moveRight(int _i){
			
			for (int i = 0; i < _i; i++){
			tileLong =  binaryAddition(tileLong);
			}
			updateName();
			
		}
		
		void moveUp(int _i){
			
			for (int i = 0; i < _i; i++){
				tileLat =  binarySubtruction(tileLat);
			}
			updateName();
			
		}
		
		void moveDown(int _i){
			
			for (int i = 0; i < _i; i++){
				tileLat =  binaryAddition(tileLat);
			}
			updateName();
			
		}
		
		void updateName(){
			
			
			tileName ="";
			
			for (int i = 0; i< tileLong.length; i++){
				
				
				if (tileLat[i]==0 && tileLong[i] ==0 ){
					
					tileName += "0";
					
				}
				
				if (tileLat[i]==0 && tileLong[i] ==1 ){
					
					tileName += "1";
					
				}
				if (tileLat[i]==1 && tileLong[i] ==0 ){
					
					tileName += "2";
					
				}
				if (tileLat[i]==1 && tileLong[i] ==1 ){
					
					tileName += "3";
					
				}

			}
			
			
		}
		
		
		
		
		
	}
	

	

public void saveFrame(String filename) {
 BufferedImage img=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 loadPixels();
 img.setRGB(0, 0, width, height, g.pixels, 0, width);

 String extn=filename.substring(filename.lastIndexOf('.')+1).toLowerCase();
 if (extn.equals("jpg")) {

   try{
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
     JPEGEncodeParam p = encoder.getDefaultJPEGEncodeParam(img);
     // set JPEG quality to 50% with baseline optimization
     p.setQuality((float) 0.9,true);
     encoder.setJPEGEncodeParam(p);
     encoder.encode(img);

     File file = new File(savePath(filename));
     FileOutputStream fo = new FileOutputStream(file);
     out.writeTo(fo);
     println("s: "+filename);
   }
   catch(FileNotFoundException e){
     System.out.println(e);
   }
   catch(IOException ioe){
     System.out.println(ioe);
   }

 }
 else if (extn.equals("png")) { // add here as needed

   try {
     javax.imageio.ImageIO.write(img, extn, new File(savePath(filename)));
     println("s: "+filename);
   }
   catch(Exception e) {
     System.err.println("error while saving as "+extn);
     e.printStackTrace();
   }

 }
 else {
   super.saveFrame(filename);
 }
} 
	
	
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { ptilemaputility.PTileMapUtility.class.getName() });
	}
}

