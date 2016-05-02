/*
 *
 *  *  PCHPrintSOE - Advanced printing SOE for ArcGIS Server
 *  *  Copyright (C) 2010-2012 Tom Schuller
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU Lesser General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU Lesser General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU Lesser General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package lu.etat.pch.gis.utils;

import com.esri.arcgis.carto.*;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.display.ITextSymbol;
import com.esri.arcgis.display.esriTextHorizontalAlignment;
import com.esri.arcgis.display.esriTextVerticalAlignment;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;

import java.io.IOException;

public class LayoutUtils {
    private static final String TAG = "LayoutUtils";

    public static <T extends ITextElement> void positionTextElement(T frame, IGeometry point, Integer anchor) throws IOException {
        if (anchor == null) return;
        ITextSymbol textSymbol = frame.getSymbol();
        switch (anchor) {
            default:
            case esriAnchorPointEnum.esriBottomLeftCorner:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHALeft);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVABottom);
                break;
            case esriAnchorPointEnum.esriBottomMidPoint:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHACenter);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVABottom);
                break;
            case esriAnchorPointEnum.esriBottomRightCorner:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHARight);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVABottom);
                break;
            case esriAnchorPointEnum.esriCenterPoint:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHACenter);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVACenter);
                break;
            case esriAnchorPointEnum.esriLeftMidPoint:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHALeft);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVACenter);
                break;
            case esriAnchorPointEnum.esriRightMidPoint:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHARight);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVACenter);
                break;
            case esriAnchorPointEnum.esriTopLeftCorner:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHALeft);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVATop);
                break;
            case esriAnchorPointEnum.esriTopMidPoint:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHACenter);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVATop);
                break;
            case esriAnchorPointEnum.esriTopRightCorner:
                textSymbol.setHorizontalAlignment(esriTextHorizontalAlignment.esriTHARight);
                textSymbol.setVerticalAlignment(esriTextVerticalAlignment.esriTVATop);
                break;
        }
        frame.setSymbol(textSymbol);
        ((IElement) frame).setGeometry(point);
    }

    //--------------------------------------------------------------------------
    //
    //  FORMAT BOUNDING BOX
    //
    //--------------------------------------------------------------------------
    public static <T extends IMapSurroundFrame & IElement> void positionElement(SOELogger logger, T element, IPoint point, IScreenDisplay screenDisplay) throws IOException {
        positionElement(logger, element, point, esriAnchorPointEnum.esriBottomLeftCorner, screenDisplay);
    }

    public static <T extends IMapSurroundFrame & IElement> void positionElement(SOELogger logger, T element, IPoint point, Integer anchor, IScreenDisplay screenDisplay) throws IOException {
        if (anchor == null) return;
        double x = point.getX();
        double y = point.getY();

        IMapSurroundFrame mapSurroundFrame = (IMapSurroundFrame) element;
        IMapSurround mapSurround = mapSurroundFrame.getMapSurround();


        IEnvelope newEnv = new Envelope();
        mapSurround.queryBounds(screenDisplay, null, newEnv);

        double height = newEnv.getHeight();
        double width = newEnv.getWidth();

        try {
            Envelope envelope = new Envelope();
            switch (anchor) {
                case esriAnchorPointEnum.esriBottomLeftCorner:
                    // default one
                    envelope.putCoords(
                            x,
                            y,
                            x + width,
                            y + height
                    );
                    break;
                case esriAnchorPointEnum.esriBottomMidPoint:
                    envelope.putCoords(
                            x - (width / 2),
                            y,
                            x + (width / 2),
                            y + height
                    );
                    break;
                case esriAnchorPointEnum.esriBottomRightCorner:
                    envelope.putCoords(
                            x - width,
                            y,
                            x,
                            y + height
                    );
                    break;
                case esriAnchorPointEnum.esriCenterPoint:
                    envelope.putCoords(
                            x - (width / 2),
                            y - (height / 2),
                            x + (width / 2),
                            y + (height / 2)
                    );
                    break;
                case esriAnchorPointEnum.esriLeftMidPoint:
                    envelope.putCoords(
                            x,
                            y - (height / 2),
                            x + width,
                            y + (height / 2)
                    );
                    break;
                case esriAnchorPointEnum.esriRightMidPoint:
                    envelope.putCoords(
                            x - width,
                            y - (height / 2),
                            x,
                            y + (height / 2)
                    );
                    break;
                case esriAnchorPointEnum.esriTopLeftCorner:
                    envelope.putCoords(
                            x,
                            y - height,
                            x + width,
                            y
                    );
                    break;
                case esriAnchorPointEnum.esriTopMidPoint:
                    envelope.putCoords(
                            x - (width / 2),
                            y - height,
                            x + (width / 2),
                            y
                    );
                    break;
                case esriAnchorPointEnum.esriTopRightCorner:
                    envelope.putCoords(
                            x - width,
                            y - height,
                            x,
                            y
                    );
                    break;
                default:
            }

            element.setGeometry(envelope);
        } catch (Exception e) {
            logger.error(TAG, "positionElement(IPoint)", e);
        }
    }

    public static void positionElementX(SOELogger logger, IElement element, IEnvelope oldEnv, Integer anchor) {
        if (anchor == null) return;
        try {
            double xMin = oldEnv.getXMin();
            double yMin = oldEnv.getYMin();
            double xMax = oldEnv.getXMax();
            double yMax = oldEnv.getYMax();
            double width = oldEnv.getWidth();
            double height = oldEnv.getHeight();
            Envelope envelope = new Envelope();
            switch (anchor) {
                case esriAnchorPointEnum.esriTopLeftCorner: //0
                    envelope.putCoords(
                            xMin,
                            yMin - height,
                            xMax,
                            yMax - height
                    );
                    break;
                case esriAnchorPointEnum.esriTopMidPoint: //1
                    envelope.putCoords(
                            xMin - (width / 2),
                            yMin - height,
                            xMax - (width / 2),
                            yMax - height
                    );
                    break;
                case esriAnchorPointEnum.esriTopRightCorner: //2
                    envelope.putCoords(
                            xMin - width,
                            yMin - height,
                            xMax - width,
                            yMax - height
                    );
                    break;
                case esriAnchorPointEnum.esriLeftMidPoint: //3
                    envelope.putCoords(
                            xMin,
                            yMin - (height / 2),
                            xMax,
                            yMax - (height / 2)
                    );
                    break;
                case esriAnchorPointEnum.esriCenterPoint: //4
                    envelope.putCoords(
                            xMin - (width / 2),
                            yMin - (height / 2),
                            xMax - (width / 2),
                            yMax - (height / 2)
                    );
                    break;
                case esriAnchorPointEnum.esriRightMidPoint: //5
                    envelope.putCoords(
                            xMin - width,
                            yMin - (height / 2),
                            xMax - width,
                            yMax - (height / 2)
                    );
                    break;
                case esriAnchorPointEnum.esriBottomLeftCorner: //6
                    //default case
                    envelope.putCoords(
                            xMin,
                            yMin,
                            xMax,
                            yMax
                    );
                    break;
                case esriAnchorPointEnum.esriBottomMidPoint: //7
                    envelope.putCoords(
                            xMin - (width / 2),
                            yMin,
                            xMax - (width / 2),
                            yMax
                    );
                    break;
                case esriAnchorPointEnum.esriBottomRightCorner: //8
                    envelope.putCoords(
                            xMin - width,
                            yMin,
                            xMax - width,
                            yMax
                    );
                    break;
                default:
                    logger.error(TAG, "positionElement(layoutElem,envelope,anchorPoint).UNKNOWN anchor", anchor);
            }
            element.setGeometry(envelope);
        } catch (Exception e) {
            logger.error(TAG, "positionElement(layoutElem,envelope,anchorPoint)", e);
        }
    }

    public static void positionElementX(SOELogger logger, IElement layoutElem, Integer anchorPoint) {
        try {
            IEnvelope env = layoutElem.getGeometry().getEnvelope();
            positionElementX(logger, layoutElem, env, anchorPoint);
        } catch (IOException e) {
            logger.error(TAG, "positionElement(layoutElem,anchorPoint)", e);
        }
    }
}
