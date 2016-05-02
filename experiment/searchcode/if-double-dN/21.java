package neuralnetbot;

import java.awt.Color;

import processing.core.PApplet;

public class Plant extends WorldObject {
	Double health, water, nutrients, waterThreshHold, nutrientThreshHold, dhWater, dhNutrient;
	Color color;
	PApplet parent;
	
	
	Plant(PApplet p, int x, int y){
		super(p, x, y);
		health = 100.0;
		water = 100.0;
		nutrients = 100.0;
		color = Color.GREEN;
		rad = 7;
		parent = p;
		
	}

	
	/**
	 * Adds water of amount dw to plant
	 * @param dw
	 * @return Water percentage of plant
	 */
	public Double water(Double dw){
		water += dw;
		return water;
	}
	
	/**
	 * Adds nutrients of amount dn percent to plant 
	 * @param dn
	 * @return
	 */
	public Double fertilize(Double dn){
		nutrients += dn;
		return nutrients;
	}
	
	public void stepTime(){
		if (water < waterThreshHold){
			health -= dhWater;
		}
		if (nutrients < nutrientThreshHold){
			health -= dhNutrient;
		}
	}
	
	public void draw(){
		parent.fill(color.getRed(), color.getGreen(), color.getBlue());
		Color tempColor = parent.getBackground();
		parent.stroke(tempColor.getRed(), tempColor.getGreen(),  tempColor.getBlue());
		parent.ellipse(X, Y, rad, rad);
	}
	
	public boolean isSelected(){
		if((parent.mouseX <= X+(rad) && parent.mouseX >= X-(rad)) && (parent.mouseY <= Y+(rad) && parent.mouseY >= Y-(rad))){
			return true;
		}else{
			return false;
		}
	}
	
	
	
	
	
		
}

