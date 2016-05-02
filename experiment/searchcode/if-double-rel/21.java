/*
 * This file is part of LightServer.
 * 
 * Copyright 2012 Michael St&#x161;ckle (BaderSt&#x161;ckle Media-Agentur GbR)
 * 
 * LightServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LightServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with LightServer. If not, see <http://www.gnu.org/licenses/>.
 */

package struktur;

import gui.User;

import java.io.Serializable;
import java.util.ArrayList;

import org.simpleframework.xml.*;

/* ---------------------------------------------------------------------------- */
/*  Channel 																	*/	
/* ---------------------------------------------------------------------------- */
/*	Kanal eines Ger&#x160;ts	(24 bit)												*/
/* ---------------------------------------------------------------------------- */

@Root
public class Channel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2474897191619243024L;

	public static final int MAX = 16777215;
	
	@Attribute
	String label;
	
	@Attribute(required=false)
	private boolean invert = false;			// invert channel
	@Attribute(required=false)
	private boolean dimmable = false;		// dimmable channel (submaster / master)
	@Attribute(required=false)
	private boolean master = false;			// dimmed by master
	
	@Attribute(required=false)
	private int defaultValue = 0;			// 24bit default value
	
	@Attribute(required=false)
	private int highlight_off = -1;			// HighlightMode OFF-Value (-1 if no HighlightMode)
	@Attribute(required=false)
	private int highlight_on = -1;			// HighlightMode ON-Value
	
	@Attribute(required=false)
	private int minOUT = 0;					// min and maximum 24bit output value
	@Attribute(required=false)
	private int maxOUT = MAX;
	
	@Attribute(required=false)
	private float minIN = 0;				// min and maximum percentage input values
	@Attribute(required=false)
	private float maxIN = 100;
	
	@Attribute(required=false)
	private float minINp = 0;				// min and maximum physical input values
	@Attribute(required=false)
	private float maxINp = 100;
	
	@ElementList
	ArrayList<Slot> slots = new ArrayList<Slot>();
	
	// dmx adressen
	@Attribute
	private int adressCoarse;
	@Attribute(required=false)
	private int adressFine = -1;
	@Attribute(required=false)
	private int adressUltra = -1;
	
	// values
	private double presetValue = Double.NaN;
	private double programmerValue = Double.NaN;

	private ArrayList<Value> valueRel0 = new ArrayList<Value>();
	private ArrayList<Value> valueRel1 = new ArrayList<Value>(); 
	private ArrayList<Value> valueRel2 = new ArrayList<Value>();	
	
	private ArrayList<Value> valueAbs0 = new ArrayList<Value>();
	private ArrayList<Value> valueAbs1 = new ArrayList<Value>(); 
	private ArrayList<Value> valueAbs2 = new ArrayList<Value>();
	
	/* ------------------------ */
	/*	Konstruktoren			*/
	/* ------------------------ */
	Channel() {
	}
	
	Channel(String label, int defaultValue, int highlight_off, int highlight_on, boolean invert, boolean dimmable, boolean master) {
		this.label = label;
		this.defaultValue = defaultValue;
		this.highlight_off = highlight_off;
		this.highlight_on = highlight_on;
		this.presetValue = defaultValue;
		this.invert = invert;
		this.dimmable = dimmable;
		this.master = master;
	}
	
	public void addValue(Value value, int level) {
		switch(level) {
			case 0:
				if(value.getAbsolut()) {
					valueAbs0.remove(value);
					valueAbs0.add(value);
				} else {
					valueRel0.remove(value);
					valueRel0.add(value);
				}
				break;
			
			case 1:
				if(value.getAbsolut()) {
					valueAbs1.remove(value);
					valueAbs1.add(value);
				} else {
					valueRel1.remove(value);
					valueRel1.add(value);
				}
				break;
				
			case 2:
				if(value.getAbsolut()) {
					valueAbs2.remove(value);
					valueAbs2.add(value);
				} else {
					valueRel2.remove(value);
					valueRel2.add(value);
				}
				break;
		}
	}
	
	public void removeValue(Value value, int level) {
		switch(level) {
			case 0:
				if(value.getAbsolut()) {
					while(valueAbs0.contains(value))
						valueAbs0.remove(value);
				} else {
					while(valueRel0.contains(value))
						valueRel0.remove(value);
				}
				break;
			
			case 1:
				if(value.getAbsolut()) {
					while(valueAbs1.contains(value))
						valueAbs1.remove(value);
				} else {
					while(valueRel1.contains(value))
						valueRel1.remove(value);
				}
				break;
				
			case 2:
				if(value.getAbsolut()) {
					while(valueAbs2.contains(value))
						valueAbs2.remove(value);
				} else {
					while(valueRel2.contains(value))
						valueRel2.remove(value);
				}
				break;
		}
	}
	
	public void removeValue(Value value) {
		while(valueAbs0.contains(value))
			valueAbs0.remove(value);
		while(valueRel0.contains(value))
			valueRel0.remove(value);
		
		while(valueAbs1.contains(value))
			valueAbs1.remove(value);
		while(valueRel1.contains(value))
			valueRel1.remove(value);
		
		while(valueAbs2.contains(value))
			valueAbs2.remove(value);
		while(valueRel2.contains(value))
			valueRel2.remove(value);
	}
	
	public void clearValue(boolean programming) {
		if(programming)
			programmerValue = Double.NaN;
		else
			presetValue = defaultValue;
	}
	
	/* ------------------------ */
	/*	calculate value			*/
	/* ------------------------ */
	public double valueFromPercent(double value, boolean rangecheck) {
		if(!rangecheck || (value >= minIN && value <= maxIN)) {
			double rangeIN = maxIN - minIN;
			double rangeOUT = maxOUT - minOUT;
			double retvalue = (value - minIN) / rangeIN;
			return  (retvalue * rangeOUT) + minOUT;
		}
		
		if(value < minIN)
			return minOUT;
		
		return maxOUT;
	}
	
	public double valueToPercent(double value) {
		double rangeIN = maxIN - minIN;
		double rangeOUT = maxOUT - minOUT;
		double retvalue = (value - minOUT) / rangeOUT;
		return  ((retvalue * rangeIN) + minIN);
	}
	
	public double valueFromPhys(double value, boolean rangecheck) {
		if(!rangecheck || (value >= minINp && value <= maxINp)) {
			double rangeIN = maxINp - minINp;
			double rangeOUT = maxOUT - minOUT;
			double retvalue = (value - minINp) / rangeIN;
			return  (retvalue * rangeOUT) + minOUT;
		}
		
		if(value < minINp)
			return minOUT;
		
		return maxOUT;
	}
	
	public double valueToPhys(double value) {
		double rangeINp = maxINp - minINp;
		double rangeOUTp = maxOUT - minOUT;
		double retvalue = (value - minOUT) / rangeOUTp;
		return  ((retvalue * rangeINp) + minINp);
	}
	
	/* ------------------------ */
	/*	set values				*/
	/* ------------------------ */
	public void setValue(int value) {
		if(User.programming())
			programmerValue = value;
		else
			presetValue = value;
	}
	
	public void setValuePercent(double value, boolean absolut) {
		if(value != Float.NaN) {
			if(absolut) {
				if(User.programming())
					programmerValue = valueFromPercent(value, true);
				else
					presetValue = valueFromPercent(value, true);
			} else {
				if(User.programming())
					programmerValue = valueFromPercent(valueToPercent(programmerValue) + value, true);
				else
					presetValue = valueFromPercent(valueToPercent(presetValue) + value, true);
			}
		}
	}
	
	public void setValuePhysical(double value, boolean absolut) {
		if(!Double.isNaN(value)) {
			if(absolut) {
				if(User.programming())
					programmerValue = valueFromPhys(value, true);
				else
					presetValue = valueFromPhys(value, true);
			} else {
				if(User.programming())
					programmerValue = valueFromPhys(valueToPhys(programmerValue) + value, true);
				else
					presetValue = valueFromPhys(valueToPhys(presetValue) + value, true);
			}
		}
	}
	
	public void setValueSlot(String value) {
		System.out.println("DEBUG Channel: set slot " + value + " --> " + slots.size());
		for(int i = 0 ; i < slots.size() ; i++) {
			int id = -1;
			try {
				id = Integer.parseInt(value.substring(1));
				if(slots.get(i).slotID == id) 
					if(User.programming())
						programmerValue = slots.get(i).value;
					else
						presetValue = slots.get(i).value;
			} catch(Exception e) {
				if(slots.get(i).label.equalsIgnoreCase(value)) 
					if(User.programming())
						programmerValue = slots.get(i).value;
					else
						presetValue = slots.get(i).value;
			}
		}
	}
	// get values
	public double getValuePercent() {
		if(User.programming())
			return valueToPercent(programmerValue);
		
		if(presetValue < 0 || Double.isNaN(presetValue))
			presetValue = defaultValue;
		return valueToPercent(presetValue);
	}
	
	public double getValuePhysical() {
		if(User.programming())
			return valueToPhys(programmerValue);
		
		if(presetValue < 0 || Double.isNaN(presetValue))
			presetValue = defaultValue;
		return valueToPhys(presetValue);
	}
	
	public double getPresetValue() {
		return programmerValue;
	}
	
	public double getProgrammerValue() {
		return programmerValue;
	}

	private double calculateValue(double presetValue) {
		return calculateValueAbs(presetValue);
	}
	
	// relativen wert aus einzelwerten berechnen
	private double[] calculateValueRel(int level) {
		// wegen nebenl&#x160;ufigkeit muss es ne tempor&#x160;re kopie der values geben (z.b. wegen zerst&#x161;rung w&#x160;rend verarbeitung)
		ArrayList<Value> vRel0 = (ArrayList<Value>) valueRel0.clone();
		ArrayList<Value> vRel1 = (ArrayList<Value>) valueRel1.clone();
		ArrayList<Value> vRel2 = (ArrayList<Value>) valueRel2.clone();
		
		double[] ret = new double[2];
		ret[0] = 0;	// value
		ret[1] = 0; // dimmer
		
		ArrayList<Value> valueRel = vRel0;
		if(level == 1)
			valueRel = vRel1;
		if(level == 2)
			valueRel = vRel2;
		
		
		for(int i = valueRel.size() - 1 ; i >= 0 ; i--) {
			Value v = valueRel.get(i);
			
			if(!Double.isNaN(v.getValue())) {
				double perc = v.getDimmer();
				if(v.getPhysical())	// bei physicalvalues ist der nullpunkt eventuell verschoben, daher korrektur
					ret[0] = ret[0] + ((v.getValueDMX(false) - this.valueFromPhys(0,false)) * perc / 100);
				else
					ret[0] = ret[0] + (v.getValueDMX(false) * perc / 100);
				ret[1] = ret[1] + perc;
				if(ret[1] >= 100)
					return ret;
			}
		}
		
		return ret;
	}
	
	// absolute werte berechnen, prozentuale anteile relativer werte mit einbinden
	// ausabe: array: [value]
	private double calculateValueAbs(double preset) {
		// wegen nebenl&#x160;ufigkeit muss es ne tempor&#x160;re kopie der values geben (z.b. wegen zerst&#x161;rung w&#x160;rend verarbeitung)
		ArrayList<Value> vAbs0 = (ArrayList<Value>) valueAbs0.clone();
		ArrayList<Value> vAbs1 = (ArrayList<Value>) valueAbs1.clone();
		ArrayList<Value> vAbs2 = (ArrayList<Value>) valueAbs2.clone();
		
		double[] ret = new double[2];
			 	 ret[0] = 0;			// value
				 ret[1] = 0; 			// dimmer
				 
		double reldim0 = 100;			// dimmer-restwert relativvalues...
		double reldim1 = 100;
		double reldim2 = 100;
		
		double[] rel0 = calculateValueRel(0);
		
		for(int i = vAbs0.size() - 1 ; i >= 0 ; i--) {
			 Value v = vAbs0.get(i);
			if(!Double.isNaN(v.getValue())) {
				double perc = ((100 - ret[1]) * v.getDimmer() / 100);
				ret[0] = ret[0] + (v.getValueDMX(true) * perc / 100);
				ret[1] = ret[1] + perc;
				reldim0 = reldim0 - perc;
				if(ret[1] >= 100) {
					return ret[0] + (rel0[0] * rel0[1] / 100);	// oberstes absolutes layer hat 100%
				}
			}
		}
		
		double[] rel1 = calculateValueRel(1);
		
		for(int i = vAbs1.size() - 1 ; i >= 0 ; i--) {
			Value v = vAbs1.get(i);
			if(!Double.isNaN(v.getValue())) {
				double perc = ((100 - ret[1]) * v.getDimmer() / 100);
				ret[0] = ret[0] + (v.getValueDMX(true) * perc / 100);
				ret[1] = ret[1] + perc;
				reldim1 = (reldim1 - perc) * (100 - reldim0) / 100;
				if(ret[1] >= 100)
					return ret[0] + (rel0[0] * rel0[1] * reldim0 / 10000) + (rel1[0] * rel1[1] * reldim1 / 10000);
			}
		}

		double[] rel2 = calculateValueRel(2);
		
		for(int i = vAbs2.size() - 1 ; i >= 0 ; i--) {
			 Value v = vAbs2.get(i);
			if(!Double.isNaN(v.getValue())) {
				double perc = ((100 - ret[1]) * v.getDimmer() / 100);
				ret[0] = ret[0] + (v.getValueDMX(true) * perc / 100);
				ret[1] = ret[1] + perc;
				reldim1 = (reldim1 - perc) * (100 - reldim1) / 10000 * (100 - reldim0) / 100;
				if(ret[1] >= 100)
					return ret[0] + (rel0[0] * rel0[1] * reldim0 / 10000) + (rel1[0] * rel1[1] * reldim1 / 10000) + (rel2[0] * rel2[1] * reldim2 / 10000);
			}
		}
		
		//System.out.println("0 rel[0] " + rel0[0] + " rel[1] " + rel0[1] + " - dimmer: " + reldim0);
		//System.out.println("1 rel[0] " + rel1[0] + " rel[1] " + rel1[1] + " - dimmer: " + reldim1);
		//System.out.println("2 rel[0] " + rel2[0] + " rel[1] " + rel2[1] + " - dimmer: " + reldim2);
		ret[0] = ret[0] + (preset * (100 - ret[1]) / 100)  + (rel0[0] * rel0[1] * reldim0 / 10000) + (rel1[0] * rel1[1] * reldim1 / 10000) + (rel2[0] * rel2[1] * reldim2 / 10000);

		return ret[0];
	}
	
	// berechnet den ausgabe-dmx-wert
	public double getOutValue() {
		if(presetValue < 0 || Double.isNaN(presetValue))
			presetValue = defaultValue;
		
		double outValue = presetValue;
		
		// wenn programming, programmervalue ausgeben, sonst wert aus chasern und szenen kalkulieren
		if(!User.blind && User.programming())
			outValue = programmerValue;
		else
			outValue = calculateValue(outValue);
		
		//TODO: hier muss dringend noch die submaster und master calc rein
		if(dimmable && master)
			outValue = outValue * Master.getValueDMX();
		
		return outValue;
	}
	
	// ausgaberoutine, schreibt value in das entsprechende universum
	public void update(Universe universe, int startAdress) {
		byte[] outBytes = intToByteArray(this, getOutValue());
		
		if(adressCoarse > 0)
			universe.getUniverseDataNoUpdate()[startAdress + adressCoarse - 2] = outBytes[0];
		if(adressFine > 0)
			universe.getUniverseDataNoUpdate()[startAdress + adressFine - 2] = outBytes[1];
		if(adressUltra > 0)
			universe.getUniverseDataNoUpdate()[startAdress + adressUltra - 2] = outBytes[2];
	}
	
	// DMX byte array f&#x;r ausgabetabelle
	public static final byte[] intToByteArray(Channel c, double dvalue) {
		int value = (int) dvalue;
		
		// letzter rangecheck
		if(dvalue > c.maxOUT)
			value = c.maxOUT;
		if(dvalue < c.minOUT)
			value = c.minOUT;
		
	    return new byte[] {
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value
	    };
	}
}

