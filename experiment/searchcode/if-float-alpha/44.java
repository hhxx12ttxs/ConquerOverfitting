package edu.cs424.traffic.central;

import java.util.HashSet;
import java.util.Set;

import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import edu.cs424.traffic.components.MainPanel.MouseMovements;

public abstract class Panel 
{
	float x0Zoom, y0Zoom, widthZoom, heightZoom, parentX0Zoom, parentY0Zoom;
	public float x0, y0, width, height, parentX0, parentY0;
	protected boolean needRedraw = true;

	Set<TouchEnabled> touchChildren = new HashSet<TouchEnabled>();

	public Panel(float x0, float y0, float width, float height, float parentX0, float parentY0) 
	{
		// with zoom applied
		this.x0Zoom = s(x0 + parentX0);
		this.y0Zoom = s(y0 + parentY0);
		this.parentX0Zoom = parentX0;
		this.parentY0Zoom = parentY0;
		this.widthZoom = s(width);
		this.heightZoom = s(height);

		// without zoom applied
		this.x0 = x0 + parentX0;
		this.y0 = y0 + parentY0;
		this.parentX0 = parentX0;
		this.parentY0 = parentY0;
		this.width = width;
		this.height = height;

		
	}
	
	public abstract void setup();

	public static float s(float x) {
		return x * SettingsLoader.getConfigValueAsInt(EnumConfig.SCALEFACTOR);
	}

	public static int s(int x) {
		return x * SettingsLoader.getConfigValueAsInt(EnumConfig.SCALEFACTOR);
	}

	public PVector createPvector(float x, float y)
	{
		return new PVector(s(x), s(y));
	}
	
	public void line(float x1, float y1, float x2, float y2) 
	{

		Main.main.line(x0Zoom + s(x1), y0Zoom + s(y1), x0Zoom + s(x2), y0Zoom + s(y2));

	}

	public void rect(float a, float b, float c, float d) {
		Main.main.rect(x0Zoom + s(a), y0Zoom + s(b), s(c), s(d));
	}

	public void rect(float a, float b, float c, float d, float r) {
		Main.main.rect(x0Zoom + s(a), y0Zoom + s(b), s(c), s(d), s(r));
	}

	public void rect(float a, float b, float c, float d, float tl, float tr, float br, float bl) {
		Main.main.rect(x0Zoom + s(a), y0Zoom + s(b), s(c), s(d), s(tl), s(tr), s(br), s(bl));
	}

	public void rectMode(int mode) {
		Main.main.rectMode(mode);
	}

	public void image(PImage img, float a, float b) {
		Main.main.image(img, x0Zoom + s(a), y0Zoom + s(b));
	}

	public void image(PImage img, float a, float b, float c, float d) {
		Main.main.image(img, x0Zoom + s(a), y0Zoom + s(b), s(c), s(d));
	}

	public void imageMode(int mode) {
		Main.main.imageMode(mode);
	}

	public void ellipse(float a, float b, float c, float d) {
		Main.main.ellipse(x0Zoom + s(a), y0Zoom + s(b), s(c), s(d));
	}

	public void ellipseMode(int mode) {
		Main.main.ellipseMode(mode);
	}

	public void text(String str, float x, float y) {
		Main.main.text(str, x0Zoom + s(x), y0Zoom + s(y));
	}
	
	public void text(String str, int start, int stop, float x, float y)
	{
		Main.main.text(str,x0Zoom+ s(start), y0Zoom+s(stop), s(x), s(y));		
	}
	
	public void text(String str, float x, float y, float a, float b)
	{
		Main.main.text(str,x0Zoom+ s(x),y0Zoom+ s(y), s(a), s(b));
	}

	public void textAlign(int alignX, int alignY) {
		Main.main.textAlign(alignX, alignY);
	}

	public void pushStyle() {
		Main.main.pushStyle();
	}

	public void popStyle() {
		Main.main.popStyle();
	}

	public void pushMatrix() {
		Main.main.pushMatrix();
	}

	public void popMatrix() {
		Main.main.popMatrix();
	}

	public void background(EnumColor color) {
		pushStyle();
		fill(color.getValue());
		Main.main.noStroke();
		Main.main.rect(x0Zoom, y0Zoom, widthZoom, heightZoom);
		popStyle();
	}
	
	public void backgroundBorder(EnumColor fillColor, EnumColor strokeColor,float strokeWeight)
	{
		pushStyle();
		fill(fillColor.getValue());
		stroke(strokeColor.getValue());
		strokeWeight(strokeWeight);
		Main.main.rect(x0Zoom, y0Zoom, widthZoom, heightZoom);
		popStyle();
	}
	
	public void background(EnumColor color,EnumColor strokeColor,int round)
	{
		pushStyle();
		fill(color.getValue());
		Main.main.stroke(strokeColor.getValue());
		Main.main.rect(x0Zoom, y0Zoom, widthZoom, heightZoom,round,round,round,round);
		popStyle();
	}

	public void background(EnumColor color, float alpha) {
		pushStyle();
		fill(color.getValue(), alpha);
		Main.main.rect(x0Zoom, y0Zoom, widthZoom, heightZoom);
		popStyle();
	}

	public void background(EnumColor color, float alpha, float tl, float tr, float br, float bl) {
		pushStyle();
		fill(color.getValue(), alpha);
		Main.main.rect(x0Zoom, y0Zoom, widthZoom, heightZoom, s(tl), s(tr), s(br), s(bl));
		popStyle();
	}

	public void background(int rgb) {
		pushStyle();
		fill(rgb);

		Main.main.rect(x0Zoom, y0Zoom, widthZoom, heightZoom);
		popStyle();
	}

	public void background(int rgb, float alpha) {
		pushStyle();
		fill(rgb, alpha);
		Main.main.rect(x0Zoom, y0Zoom, widthZoom, heightZoom);
		popStyle();
	}

	public void textSize(float size) {
		Main.main.textSize(s(size));
	}

	public void textFont(PFont font) {
		Main.main.textFont(font);
	}

	public void fill(EnumColor color) {
		Main.main.fill(color.getValue());
	}

	public void fill(EnumColor color, float alpha) {
		Main.main.fill(color.getValue(), alpha);
	}

	public void fill(int rgb) {
		Main.main.fill(rgb);
	}

	public void fill(int rgb, float alpha) {
		Main.main.fill(rgb, alpha);
	}

	public void stroke(int rgb) {
		Main.main.stroke(rgb);
	}

	public void stroke(int rgb, float alpha) {
		Main.main.stroke(rgb, alpha);
	}

	public void stroke(EnumColor color) {
		Main.main.stroke(color.getValue());
	}

	public void stroke(EnumColor color, float alpha) {
		Main.main.stroke(color.getValue(), alpha);
	}

	public void strokeWeight(float weight) {
		Main.main.strokeWeight(s(weight));
	}

	public void noStroke() {
		Main.main.noStroke();
	}

	public void noFill() {
		Main.main.noFill();
	}

	public void beginShape() {
		Main.main.beginShape();
	}

	public void endShape(int mode) {
		Main.main.endShape(mode);
	}

	public void endShape() {
		Main.main.endShape();
	}

	public void shape(PShape shape, float x, float y) {
		Main.main.shape(shape, x0Zoom + s(x), y0Zoom + s(y));
	}

	public void shape(PShape shape, float x, float y, float width, float height) {
		Main.main.shape(shape, x0Zoom + s(x), y0Zoom + s(y), width, height);
	}

	public void vertex(float x, float y) {
		Main.main.vertex(s(x) + x0Zoom, s(y) + y0Zoom);
	}


	public float radians(float angle) {
		return Main.main.radians(angle);
	}

	public void arc(float x, float y, float startEdge, float stopEdge, float startAngle,
			float stopAngle) {
		Main.main.arc(s(x), s(y), s(startEdge), s(stopEdge), startAngle, stopAngle);
	}

	public PGraphics createGraphics(float a, float b) {
		return  Main.main.createGraphics((int) s(a), (int) s(b));
	}

	public void createGraphicsImage(PGraphics pg, float c, float d) {
		Main.main.image(pg, s(c), s(d));
	}


	public void draw() {
		Main.main.pushStyle();
		Main.main.fill(EnumColor.SOMERANDOM.getValue());
		Main.main.rect(x0Zoom, y0Zoom, widthZoom, heightZoom);
		Main.main.popStyle();
	}

	public boolean containsPoint(float x, float y) {
		return x > x0Zoom && x < x0Zoom + widthZoom && y > y0Zoom && y < y0Zoom + heightZoom;
	}

	public void addTouchSubscriber(TouchEnabled child) {
		touchChildren.add(child);
	}

	public boolean propagateTouch(float x, float y, MouseMovements event)
	{
		boolean consumed = false;

		for (TouchEnabled child : touchChildren) 
		{
			if( child.containsPoint(x, y) && child.touch(x, y, event) ) 
			{
				consumed = true;
				break;
			}
		}
		return consumed;
	}
	
	public void setReDraw()
	{
		needRedraw = true;
	}
	
	public void removeTouchSuscribedElement(TouchEnabled toReomve)
	{
		touchChildren.remove(toReomve);
	}
	
	public void removeAllTouchSuscribers()
	{
		touchChildren.clear();
	}
	
	


}

