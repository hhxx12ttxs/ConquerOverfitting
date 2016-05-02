package net.spritegen.util;


import static org.lwjgl.opengl.GL11.*;

import org.newdawn.slick.opengl.Texture;

import com.porcupine.color.HSV;
import com.porcupine.color.RGB;
import com.porcupine.coord.Coord;
import com.porcupine.coord.Rect;


/**
 * Render utilities
 * 
 * @author Ondřej Hruška (MightyPork)
 */
public class RenderUtils {

	/**
	 * Render quad 2D
	 * 
	 * @param min min coord
	 * @param max max coord
	 */
	public static void quadCoord(Coord min, Coord max) {
		quadCoord(min.x, min.y, max.x, max.y);
	}

	/**
	 * Render quad with absolute coords (from screen bottom)
	 * 
	 * @param left left x
	 * @param bottom bottom y
	 * @param right right x
	 * @param top top y
	 */
	public static void quadCoord(double left, double bottom, double right, double top) {
		glBegin(GL_QUADS);
		glVertex2d(left, top);
		glVertex2d(right, top);
		glVertex2d(right, bottom);
		glVertex2d(left, bottom);
		glEnd();
	}

	/**
	 * Render quad with coloured border
	 * 
	 * @param min min
	 * @param max max
	 * @param border border width
	 * @param borderColor border color
	 * @param insideColor filling color
	 */
	public static void quadCoordBorder(Coord min, Coord max, double border, RGB borderColor, RGB insideColor) {
		quadCoordBorder(min.x, min.y, max.x, max.y, border, borderColor, insideColor);
	}

	/**
	 * Render quad with coloured border
	 * 
	 * @param minX min x
	 * @param minY min y
	 * @param maxX max x
	 * @param maxY max y
	 * @param border border width
	 * @param borderColor border color
	 * @param insideColor filling color
	 */
	public static void quadCoordBorder(double minX, double minY, double maxX, double maxY, double border, RGB borderColor, RGB insideColor) {
		double bdr = border;

		RenderUtils.setColor(borderColor);

		//@formatter:off
		RenderUtils.quadCoord(minX,			minY, 		minX + bdr,		maxY);
		RenderUtils.quadCoord(maxX - bdr, 	minY, 		maxX, 			maxY);
		RenderUtils.quadCoord(minX + bdr,	maxY - bdr,	maxX - bdr, 	maxY);
		RenderUtils.quadCoord(minX + bdr,	minY,		maxX - bdr, 	minY + bdr);
		//@formatter:on		

		if (insideColor != null) {
			RenderUtils.setColor(insideColor);
			RenderUtils.quadCoord(minX + bdr, minY + bdr, maxX - bdr, maxY - bdr);
		}
	}

	/**
	 * Render quad 2D with gradient horizontal
	 * 
	 * @param min min coord
	 * @param max max coord
	 * @param colorLeft color left
	 * @param colorRight color right
	 */
	public static void quadCoordGradH(Coord min, Coord max, RGB colorLeft, RGB colorRight) {
		quadCoordGradH(min.x, min.y, max.x, max.y, colorLeft, colorRight);
	}

	/**
	 * Render quad 2D with gradient horizontal
	 * 
	 * @param minX units from left
	 * @param minY units from bottom
	 * @param maxX quad width
	 * @param maxY quad height
	 * @param colorLeft color left
	 * @param colorRight color right
	 */
	public static void quadCoordGradH(double minX, double minY, double maxX, double maxY, RGB colorLeft, RGB colorRight) {
		glBegin(GL_QUADS);
		setColor(colorLeft);
		glVertex2d(minX, maxY);
		setColor(colorRight);
		glVertex2d(maxX, maxY);

		setColor(colorRight);
		glVertex2d(maxX, minY);
		setColor(colorLeft);
		glVertex2d(minX, minY);
		glEnd();
	}

	/**
	 * Render quad 2D with gradient horizontal
	 * 
	 * @param min min coord
	 * @param max max coord
	 * @param colorOuter color outer
	 * @param colorMiddle color inner
	 */
	public static void quadCoordGradHBilinear(Coord min, Coord max, RGB colorOuter, RGB colorMiddle) {
		quadCoordGradHBilinear(min.x, min.y, max.x, max.y, colorOuter, colorMiddle);
	}

	/**
	 * Render quad 2D with gradient horizontal
	 * 
	 * @param minX min x
	 * @param minY min y
	 * @param maxX max x
	 * @param maxY max y
	 * @param colorOuter color outer
	 * @param colorMiddle color inner
	 */
	public static void quadCoordGradHBilinear(double minX, double minY, double maxX, double maxY, RGB colorOuter, RGB colorMiddle) {
		glBegin(GL_QUADS);
		setColor(colorOuter);
		glVertex2d(minX, maxY);
		setColor(colorMiddle);
		glVertex2d((minX + maxX) / 2, maxY);

		setColor(colorMiddle);
		glVertex2d((minX + maxX) / 2, minY);
		setColor(colorOuter);
		glVertex2d(minX, minY);


		setColor(colorMiddle);
		glVertex2d((minX + maxX) / 2, maxY);
		setColor(colorOuter);
		glVertex2d(maxX, maxY);

		setColor(colorOuter);
		glVertex2d(maxX, minY);
		setColor(colorMiddle);
		glVertex2d((minX + maxX) / 2, minY);
		glEnd();
	}

	/**
	 * Render quad 2D with gradient vertical
	 * 
	 * @param min min coord
	 * @param max max coord
	 * @param colorTop top color
	 * @param colorBottom bottom color
	 */
	public static void quadCoordGradV(Coord min, Coord max, RGB colorTop, RGB colorBottom) {
		quadCoordGradV(min.x, min.y, max.x, max.y, colorTop, colorBottom);
	}

	/**
	 * Render quad 2D with gradient vertical
	 * 
	 * @param minX min X
	 * @param minY min Y
	 * @param maxX max X
	 * @param maxY max Y
	 * @param colorTop top color
	 * @param colorBottom bottom color
	 */
	public static void quadCoordGradV(double minX, double minY, double maxX, double maxY, RGB colorTop, RGB colorBottom) {
		glBegin(GL_QUADS);

		setColor(colorTop);
		glVertex2d(minX, maxY);
		glVertex2d(maxX, maxY);

		setColor(colorBottom);
		glVertex2d(maxX, minY);
		glVertex2d(minX, minY);
		glEnd();
	}

	/**
	 * Render quad 2D with gradient vertical
	 * 
	 * @param min min coord
	 * @param max max coord
	 * @param colorOuter outer color
	 * @param colorMiddle middle color
	 */
	public static void quadCoordGradVBilinear(Coord min, Coord max, RGB colorOuter, RGB colorMiddle) {
		quadCoordGradVBilinear(min.x, min.y, max.x, max.y, colorOuter, colorMiddle);
	}

	/**
	 * Render quad 2D with gradient vertical
	 * 
	 * @param minX min X
	 * @param minY min Y
	 * @param maxX max X
	 * @param maxY max Y
	 * @param colorOuter outer color
	 * @param colorMiddle middle color
	 */
	public static void quadCoordGradVBilinear(double minX, double minY, double maxX, double maxY, RGB colorOuter, RGB colorMiddle) {
		glBegin(GL_QUADS);

		setColor(colorOuter);
		glVertex2d(minX, maxY);
		glVertex2d(maxX, maxY);

		setColor(colorMiddle);
		glVertex2d(maxX, (maxY + minY) / 2);
		glVertex2d(minX, (maxY + minY) / 2);

		setColor(colorMiddle);
		glVertex2d(minX, (maxY + minY) / 2);
		glVertex2d(maxX, (maxY + minY) / 2);

		setColor(colorOuter);
		glVertex2d(maxX, minY);
		glVertex2d(minX, minY);

		glEnd();
	}

	/**
	 * Render quad with coloured border with outset effect
	 * 
	 * @param min min coord
	 * @param max max coord
	 * @param border border width
	 * @param fill fill color
	 * @param inset true for inset, false for outset
	 */
	public static void quadCoordOutset(Coord min, Coord max, double border, RGB fill, boolean inset) {
		quadCoordOutset(min.x, min.y, max.x, max.y, border, fill, inset);
	}

	/**
	 * Render quad with coloured border with outset effect
	 * 
	 * @param minX left X
	 * @param minY bottom Y
	 * @param maxX right X
	 * @param maxY top Y
	 * @param border border width
	 * @param fill fill color
	 * @param inset true for inset, false for outset
	 */
	public static void quadCoordOutset(double minX, double minY, double maxX, double maxY, double border, RGB fill, boolean inset) {
		HSV hsv = fill.toHSV();
		HSV h;

		h = hsv.copy();
		h.s *= 0.6;
		RGB a = h.toRGB();

		h = fill.toHSV();
		h.v *= 0.6;
		RGB b = h.toRGB();

		RGB leftTopC, rightBottomC;

		if (!inset) {
			leftTopC = a;
			rightBottomC = b;
		} else {
			leftTopC = b;
			rightBottomC = a;
		}

		double bdr = border;

		RenderUtils.setColor(leftTopC);

		// left + top
		glBegin(GL_QUADS);
		glVertex2d(minX, minY);
		glVertex2d(minX + bdr, minY + bdr);
		glVertex2d(minX + bdr, maxY - bdr);
		glVertex2d(minX, maxY);

		glVertex2d(minX, maxY);
		glVertex2d(minX + bdr, maxY - bdr);
		glVertex2d(maxX - bdr, maxY - bdr);
		glVertex2d(maxX, maxY);
		glEnd();


		RenderUtils.setColor(rightBottomC);

		// right + bottom
		glBegin(GL_QUADS);
		glVertex2d(maxX, minY);
		glVertex2d(maxX, maxY);
		glVertex2d(maxX - bdr, maxY - bdr);
		glVertex2d(maxX - bdr, minY + bdr);

		glVertex2d(minX, minY);
		glVertex2d(maxX, minY);
		glVertex2d(maxX - bdr, minY + bdr);
		glVertex2d(minX + bdr, minY + bdr);
		glEnd();

		RenderUtils.setColor(fill);
		RenderUtils.quadCoord(minX + bdr, minY + bdr, maxX - bdr, maxY - bdr);
	}

	/**
	 * Render quad 2D
	 * 
	 * @param rect rectangle
	 */
	public static void quadRect(Rect rect) {
		quadCoord(rect.getMin(), rect.getMax());
	}

	/**
	 * Render quad 2D
	 * 
	 * @param rect rectangle
	 * @param color draw color
	 */
	public static void quadRect(Rect rect, RGB color) {
		setColor(color);
		quadCoord(rect.getMin(), rect.getMax());
	}


	/**
	 * Render quad with coloured border
	 * 
	 * @param rect rect
	 * @param border border width
	 * @param borderColor border color
	 * @param insideColor filling color
	 */
	public static void quadBorder(Rect rect, double border, RGB borderColor, RGB insideColor) {
		quadCoordBorder(rect.getMin(), rect.getMax(), border, borderColor, insideColor);
	}

	/**
	 * Render quad 2D with gradient horizontal
	 * 
	 * @param rect rect
	 * @param colorLeft color left
	 * @param colorRight color right
	 */
	public static void quadGradH(Rect rect, RGB colorLeft, RGB colorRight) {
		quadCoordGradH(rect.getMin(), rect.getMax(), colorLeft, colorRight);
	}

	/**
	 * Render quad 2D with gradient horizontal
	 * 
	 * @param rect rect
	 * @param colorOuter color outer
	 * @param colorMiddle color inner
	 */
	public static void quadGradHBilinear(Rect rect, RGB colorOuter, RGB colorMiddle) {
		quadCoordGradHBilinear(rect.getMin(), rect.getMax(), colorOuter, colorMiddle);
	}



	/**
	 * Render quad 2D with gradient vertical
	 * 
	 * @param rect rect
	 * @param colorTop top color
	 * @param colorBottom bottom color
	 */
	public static void quadGradV(Rect rect, RGB colorTop, RGB colorBottom) {
		quadCoordGradV(rect.getMin(), rect.getMax(), colorTop, colorBottom);
	}

	/**
	 * Render quad 2D with gradient vertical
	 * 
	 * @param rect rect
	 * @param colorOuter outer color
	 * @param colorMiddle middle color
	 */
	public static void quadGradVBilinear(Rect rect, RGB colorOuter, RGB colorMiddle) {
		quadCoordGradVBilinear(rect.getMin(), rect.getMax(), colorOuter, colorMiddle);
	}

	/**
	 * Render quad with coloured border with outset effect
	 * 
	 * @param rect rectangle
	 * @param border border width
	 * @param fill fill color
	 * @param inset true for inset, false for outset
	 */
	public static void quadRectOutset(Rect rect, double border, RGB fill, boolean inset) {
		quadCoordOutset(rect.getMin(), rect.getMax(), border, fill, inset);
	}


	/**
	 * Render textured rect (texture must be binded already)
	 * 
	 * @param quad rectangle (px)
	 * @param textureCoords texture coords (0-1)
	 */
	public static void quadTexturedAbs(Rect quad, Rect textureCoords) {
		double left = quad.x1();
		double bottom = quad.y2();
		double right = quad.x2();
		double top = quad.y1();

		double tleft = textureCoords.x1();
		double tbottom = textureCoords.y1();
		double tright = textureCoords.x2();
		double ttop = textureCoords.y2();

		glBegin(GL_QUADS);
		glTexCoord2d(tleft, ttop);
		glVertex2d(left, top);
		glTexCoord2d(tright, ttop);
		glVertex2d(right, top);
		glTexCoord2d(tright, tbottom);
		glVertex2d(right, bottom);
		glTexCoord2d(tleft, tbottom);
		glVertex2d(left, bottom);
		glEnd();
	}

	/**
	 * Render quad 2D
	 * 
	 * @param left units from left
	 * @param bottom units from bottom
	 * @param width quad width
	 * @param height quad height
	 */
	public static void quadSize(double left, double bottom, double width, double height) {
		glBegin(GL_QUADS);
		glVertex2d(left, bottom + height);
		glVertex2d(left + width, bottom + height);
		glVertex2d(left + width, bottom);
		glVertex2d(left, bottom);
		glEnd();
	}


	/**
	 * Bing GL color
	 * 
	 * @param color RGB color
	 */
	public static void setColor(RGB color) {
		if (color != null) glColor4d(color.r, color.g, color.b, color.a);
	}

	/**
	 * Translate with coord
	 * 
	 * @param coord coord
	 */
	public static void translate(Coord coord) {
		glTranslated(coord.x, coord.y, coord.z);
	}
}

