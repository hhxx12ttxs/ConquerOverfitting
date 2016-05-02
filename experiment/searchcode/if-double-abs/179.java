package com.includio.interfaces.javafx.util;

import com.includio.domain.model.tech.Category;
import com.includio.domain.model.tech.Level;

public class TechLocator {

	private static final double RADIUS = 50d;
	private static final double RADIUS_5_PIXEL = 5d;
	private static final double CENTER_X = 250d;
	private static final double CENTER_Y = 250d;

	public static Category category(double xCoordinate, double yCoordinate) {
		double re = Math.pow(relatedX(xCoordinate), 2)
				+ Math.pow(relatedY(yCoordinate), 2);
		if (re > Math.pow(4 * RADIUS, 2))
			return null;
		if (xCoordinate > CENTER_X && yCoordinate > CENTER_Y)
			return Category.LANGUAGE;
		if (xCoordinate < CENTER_X && yCoordinate > CENTER_Y)
			return Category.PLATFORM;
		if (xCoordinate < CENTER_X && yCoordinate < CENTER_Y)
			return Category.TECHNOLOGY;
		if (xCoordinate > CENTER_X && yCoordinate < CENTER_Y)
			return Category.TOOL;
		return null;
	}

	public static Level level(double xCoordinate, double yCoordinate) {
		double re = Math.pow(relatedX(xCoordinate), 2)
				+ Math.pow(relatedY(yCoordinate), 2);
		if (re < Math.pow(4 * RADIUS, 2) && re >= Math.pow(3 * RADIUS, 2))
			return Level.HOLD;
		if (re < Math.pow(3 * RADIUS, 2) && re >= Math.pow(2 * RADIUS, 2))
			return Level.ASSESS;
		if (re < Math.pow(2 * RADIUS, 2) && re >= Math.pow(1 * RADIUS, 2))
			return Level.TRIAL;
		if (re < Math.pow(1 * RADIUS, 2))
			return Level.ADOPT;
		return null;
	}

	public static boolean isMoveValid(double oldXPosition, double oldYPosition,
			double newXPosition, double newYPosition) {
		if (!isValid(newXPosition, newYPosition))
			return false;
		double oldRelatedX = relatedX(oldXPosition);
		double oldRelatedY = relatedY(oldYPosition);
		double newRelatedX = relatedX(newXPosition);
		double newRelatedY = relatedY(newYPosition);

		return Math.abs(oldRelatedX + newRelatedX) == Math.abs(oldRelatedX)
				+ Math.abs(newRelatedX)
				&& Math.abs(oldRelatedY + newRelatedY) == Math.abs(oldRelatedY)
						+ Math.abs(newRelatedY);
	}

	public static boolean isValid(double xCoordinate, double yCoordinate) {
		double re = Math.pow(relatedX(xCoordinate), 2)
				+ Math.pow(relatedY(yCoordinate), 2);

		if (re > Math.pow((4 * RADIUS - RADIUS_5_PIXEL), 2))
			return false;

		if (Math.abs(relatedX(xCoordinate)) < RADIUS_5_PIXEL
				|| Math.abs(relatedY(yCoordinate)) < RADIUS_5_PIXEL)
			return false;
		return true;
	}

	private static double relatedX(double xCoordinate) {
		return xCoordinate - CENTER_X;
	}

	private static double relatedY(double yCoordinate) {
		return yCoordinate - CENTER_Y;
	}
}

