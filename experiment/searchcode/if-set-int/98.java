/*  
 * Copyright (c) 2009  Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package cc.creativecomputing.cv;

import java.util.Arrays;

import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.texture.CCTextureData;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCVector2i;

/**
 * @author christianriekoff
 *
 */
public class CCPixelRaster {
   
	private int _myWidth;
	private int _myHeight;
	
	private String _myName;
	
	private float[] _myData;
	
	public CCPixelRaster(final String theName,final int theWidth, final int theHeight) {
		_myWidth = theWidth;
		_myHeight = theHeight;
		
		
		_myData = new float[_myWidth * _myHeight];
		
		_myName = theName;
	}
	
	public CCPixelRaster(final CCTextureData theData) {
		this("",theData.width(), theData.height());

		System.out.println(_myWidth +":" + _myHeight);
		for(int x = 0; x < _myWidth;x++) {
			for(int y = 0; y < _myHeight;y++) {
				int value = (int)(theData.getPixel(x, y).brightness() * 255);
				_myData[y * _myWidth + x] = value;
			}
		}
	}
	
	public int width() {
		return _myWidth;
	}
	
	public int height() {
		return _myHeight;
	}
	
	public float get(int theX, int theY) {
		theX = CCMath.constrain(theX, 0, _myWidth - 1);
		theY = CCMath.constrain(theY, 0, _myHeight - 1);
		return _myData[_myWidth * theY + theX];
	}
	
	public void set(int theX, int theY, float theValue) {
		theX = CCMath.constrain(theX, 0, _myWidth - 1);
		theY = CCMath.constrain(theY, 0, _myHeight - 1);
		_myData[_myWidth * theY + theX] = theValue;
	}
	
	public float[] data() {
		return _myData;
	}
	
	public void threshold (float theThreshold){
		for(int i = 0; i < _myData.length;i++) {
			_myData[i] = _myData[i] < theThreshold ? 0 : (_myData[i] - theThreshold) * (1 + theThreshold / _myData[i]);
		}
    }
	
	public void power(float thePower) {
		for(int i = 0; i < _myData.length;i++) {
			_myData[i] = CCMath.pow(_myData[i] / 255f, thePower) * 255f;
		}
	}
	
	public CCPixelRaster clone() {
		CCPixelRaster myResult = new CCPixelRaster(_myName, _myWidth, _myHeight);
		myResult._myData = Arrays.copyOf(_myData, _myData.length);
		return myResult;
	}
	
	public float maximum(final CCBox2i theBox, final CCVector2i thePosition) {
		float max = -1;
		for (int y = theBox.minY(); y < theBox.maxY(); y++) {
            for (int x = theBox.minX(); x < theBox.maxX(); x++) {
            	float value = get(x,y);
                if(value > max) {
                    max = value;
                    if(thePosition!=null)thePosition.set(x, y);
                }
            }
        }
		return max;
	}
	
	public int average() {
		float r = 0;
		
		for (int x = 0; x < _myWidth; x++) {
			for (int y = 0; y < _myHeight; y++) {
				r += get(x, y);
			}
		}
		return (int) r / (_myWidth * _myHeight);
	}
	
	public CCPixelRaster add(CCPixelRaster theRaster) {
		for (int x = 0; x < _myWidth; x++) {
			for (int y = 0; y < _myHeight; y++) {
				set(x,y, get(x,y) + theRaster.get(x, y));
			}
		}
		return this;
	}
	
	public CCPixelRaster subtract(CCPixelRaster theRaster) {
		for (int x = 0; x < _myWidth; x++) {
			for (int y = 0; y < _myHeight; y++) {
				set(x,y, get(x,y) - theRaster.get(x, y));
			}
		}
		return this;
	}
	
	public CCPixelRaster scale(final float theScale) {
		for (int x = 0; x < _myWidth; x++) {
			for (int y = 0; y < _myHeight; y++) {
				_myData[_myWidth * y + x] *= theScale;
				_myData[_myWidth * y + x] = CCMath.min(_myData[_myWidth * y + x],255);
			}
		}
		return this;
	}
	
	public void draw(CCGraphics g) {
		for(int x = 0; x < _myWidth; x++) {
			for(int y = 0; y < _myHeight; y++) {
				g.color((int)get(x,y));
				g.point(x,y);
			}
		}
	}
}

