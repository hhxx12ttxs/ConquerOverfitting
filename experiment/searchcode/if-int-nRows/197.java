package com.atlassian.plugins.roadmap;

import com.atlassian.plugins.roadmap.beans.*;
import com.google.gson.Gson;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class RoadmapRenderer {

    private static final Font loadedFont = loadFont();

    private static final Font loadFont() {
        try
        {
            return Font.createFont(Font.TRUETYPE_FONT, RoadmapRenderer.class.getClassLoader().getResourceAsStream("arial.ttf"));
        }
        catch (Exception e)
        {
            System.err.print("could not load font");
            return new Font("Helvetica",Font.PLAIN,20);
        }
    }

    private static final Color COLOR_TEXT = new Color(0x707070);
    private static final Color COLOR_BORDER = new Color(0xD1D1D1);
    private static final Color COLOR_TEXT_TASK_LIGHT = new Color(0xF5F5F5);
    private static final Color COLOR_TEXT_TASK_DARK = new Color(0x333333);
    private static final Color COLOR_BACK_COLUMN_ODD  = new Color(0xF5F5F5);
    private static final Color COLOR_BACK_COLUMN_EVEN = new Color(0xE8E8E8);

    private static final Font FONT_TITLE = loadedFont.deriveFont(Font.PLAIN, 20);
    private static final Font FONT_COLUMNS = loadedFont.deriveFont(Font.PLAIN, 16);
    private static final Font FONT_THEMES = loadedFont.deriveFont(Font.BOLD, 12);
    private static final Font FONT_TASKS = loadedFont.deriveFont(Font.BOLD, 12);
    private static final Font FONT_MARKERS = loadedFont.deriveFont(Font.BOLD, 12);

    private static final Stroke STROKE_MARKER = new BasicStroke(2);
    private static final Stroke STROKE_COLUMN_LINE = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[]{1, 4}, 0);

    private static final int MARGIN_TITLE = 15;
    private static final int MARGIN_THEME = 5;
    private static final int MARGIN_TOP_COLUMNS = 5;
    private static final int MARGIN_TASK = 10;
    private static final int MARGIN_TASK_HORIZONTAL = 5;
    private static final int MARGIN_MARKER_LINE = 20;
    private static final int MARGIN_MARKER = 5;

    private static final int CORNER_SIZE_TITLE = 5;
    private static final int CORNER_SIZE_COLUMN = 5;
    private static final int CORNER_SIZE_THEME = 0;
    private static final int CORNER_SIZE_TASK = 10;

    private static final int SIZE_HEIGHT_COLUMN = 40;
    private static final int SIZE_HEIGHT_TASK = 40;


    public static BufferedImage drawImage(String roadmap) {
        Gson gson = new Gson();
        return drawImage(gson.fromJson(roadmap, Roadmap.class));
    }

    public static BufferedImage drawImage(Roadmap r) {
        // Incremental column widths, used for tasks & markers
        final Map<String, Integer> columnWidthsInc = new HashMap<String, Integer>();

        // FIXME: Check for boundaries, etc
        int prevWidth = 0;
        for (RoadmapColumn column : r.columns) {
            int newWidth = column.width + prevWidth;
            columnWidthsInc.put(column.id, newWidth);
            prevWidth = newWidth;
        }

        // FIXME: Do we really need a dummy image first?
        BufferedImage result = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = result.createGraphics();

        FontMetrics fmTitle = g2.getFontMetrics(FONT_TITLE);
        FontMetrics fmCols = g2.getFontMetrics(FONT_COLUMNS);
        FontMetrics fmTheme = g2.getFontMetrics(FONT_THEMES);
        FontMetrics fmTask = g2.getFontMetrics(FONT_TASKS);
        FontMetrics fmMarker = g2.getFontMetrics(FONT_MARKERS);

        Rectangle2D boundsTitle = fmTitle.getStringBounds(r.title, g2);

        int wTheme = fmTheme.getHeight() + MARGIN_THEME*2;
        int wTitle = (int)boundsTitle.getHeight() + MARGIN_TITLE * 2;
        int wTitleTheme = wTitle + wTheme;
        // Width of all the columns
        int wColumns = columnWidthsInc.get(r.columns.get(r.columns.size() - 1).id);

        // Process data to get maximums and minimums
        int totalRows = 0;
        for (RoadmapTheme theme : r.themes) {
            int maxRow = 0;
            for (RoadmapTask task : theme.tasks) {
                maxRow = Math.max(task.row, maxRow);
            }
            totalRows += (maxRow + 1);
        }

        int hRoadmap = totalRows * SIZE_HEIGHT_TASK + (totalRows + r.themes.size()) * MARGIN_TASK;
        int wRoadmap = wColumns + wTitleTheme;
        int rightMargin = 0;
        for (RoadmapMarker marker : r.markers) {
            int xPos = wTitleTheme + getXFromColPos(r, columnWidthsInc, marker.columnid, marker.columnpos);

            // Check if text is going to be bigger than image size
            Rectangle2D boundsMarker = fmMarker.getStringBounds(marker.title, g2);
            int halfWidth = (int)(boundsMarker.getWidth() / 2);
            if (xPos + halfWidth > wRoadmap) {
                rightMargin = Math.max(rightMargin, halfWidth);
            }
        }

        int realWidth = wRoadmap + rightMargin;
        int realHeight = hRoadmap + SIZE_HEIGHT_COLUMN + MARGIN_MARKER_LINE + MARGIN_MARKER * 2 + fmMarker.getHeight();

        g2.dispose();

        // Create the real image with the correct size
        result = new BufferedImage(realWidth + 1, realHeight + 1, BufferedImage.TYPE_INT_ARGB);
        g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Stroke origStroke = g2.getStroke();
        AffineTransform origTransform = g2.getTransform();

        // Title //

        // Draw Title box
//        g2.setColor(COLOR_BORDER);
//        g2.drawRoundRect(0, SIZE_HEIGHT_COLUMN, wTitle, hRoadmap, CORNER_SIZE_TITLE, CORNER_SIZE_TITLE);
        // Draw Title
        g2.setFont(FONT_TITLE);
        g2.setColor(COLOR_TEXT);
        g2.rotate(-Math.PI / 2);
        g2.drawString(r.title, (int)-(hRoadmap + boundsTitle.getWidth()) / 2 - SIZE_HEIGHT_COLUMN, (int)boundsTitle.getHeight() + MARGIN_TITLE - fmTitle.getDescent());
        g2.setTransform(origTransform);

        // Columns //
        g2.setFont(FONT_COLUMNS);
        int colXPos = wTitleTheme;
        // Draw top & bottom border
        g2.setColor(COLOR_BORDER);
        g2.drawLine(colXPos, SIZE_HEIGHT_COLUMN, colXPos + wColumns, SIZE_HEIGHT_COLUMN);
        g2.drawLine(colXPos, SIZE_HEIGHT_COLUMN + hRoadmap, colXPos + wColumns, SIZE_HEIGHT_COLUMN + hRoadmap);
        int j = 0;
        for (RoadmapColumn col : r.columns) {
            // Draw border
//            g2.setColor(COLOR_BORDER);
            // FIXME: Maybe we should use the max text size, instead of SIZE_HEIGHT_COLUMN
//            g2.drawRoundRect(colXPos, 0, col.width, SIZE_HEIGHT_COLUMN, CORNER_SIZE_COLUMN, CORNER_SIZE_COLUMN);

            // Fill background
            g2.setColor((j++ & 0x01) == 0 ? COLOR_BACK_COLUMN_ODD : COLOR_BACK_COLUMN_EVEN);
            g2.fillRect(colXPos, SIZE_HEIGHT_COLUMN + 1, col.width, hRoadmap - 1);

            // Draw vertical line
            g2.setStroke(STROKE_COLUMN_LINE);
            g2.drawLine(colXPos + col.width, SIZE_HEIGHT_COLUMN, colXPos + col.width, hRoadmap + SIZE_HEIGHT_COLUMN);
            g2.setStroke(origStroke);

            // Draw text
            g2.setColor(COLOR_TEXT);
            Rectangle2D boundsCol = fmCols.getStringBounds(col.title, g2);
            g2.drawString(col.title, colXPos + (int)(col.width - boundsCol.getWidth()) / 2, (int)boundsCol.getHeight() + MARGIN_TOP_COLUMNS);

            colXPos += col.width;
        }

        // Markers //
        g2.setFont(FONT_MARKERS);
        g2.setStroke(STROKE_MARKER);
        for (RoadmapMarker marker : r.markers) {
            int xPos = wTitleTheme + getXFromColPos(r, columnWidthsInc, marker.columnid, marker.columnpos);
            g2.setColor(decodeColor(marker.colour));

            // Draw line
            int hMarkerLine = hRoadmap + MARGIN_MARKER_LINE + SIZE_HEIGHT_COLUMN;
            g2.drawLine(xPos, SIZE_HEIGHT_COLUMN, xPos, hMarkerLine);

            // Draw text
            Rectangle2D boundsMarker = fmMarker.getStringBounds(marker.title, g2);
            int yPos = hMarkerLine + MARGIN_MARKER + (int)boundsMarker.getHeight();
            g2.drawString(marker.title, xPos - (int)(boundsMarker.getWidth() / 2), yPos);
        }
        g2.setStroke(origStroke);

        // Themes //
        int themeYPos = SIZE_HEIGHT_COLUMN;
        for (RoadmapTheme theme : r.themes) {
            Color colorTheme = decodeColor(theme.colour);
            // Draw tasks
            int nRows = 0;
            for (RoadmapTask task : theme.tasks) {
                int xPos = wTitleTheme + getXFromColPos(r, columnWidthsInc, task.startid, task.startpos);
                int wTask = wTitleTheme + getXFromColPos(r, columnWidthsInc, task.endid, task.endpos) - xPos;
                int yPos = themeYPos + task.row * SIZE_HEIGHT_TASK + (task.row + 1) * MARGIN_TASK;

                // Draw task background
                g2.setColor(colorTheme);
                g2.fillRoundRect(xPos + MARGIN_TASK_HORIZONTAL, yPos, wTask - MARGIN_TASK_HORIZONTAL * 2, SIZE_HEIGHT_TASK, CORNER_SIZE_TASK, CORNER_SIZE_TASK);

                // Draw task text
                g2.setColor(isContrasted(COLOR_TEXT_TASK_LIGHT, colorTheme) ? COLOR_TEXT_TASK_LIGHT : COLOR_TEXT_TASK_DARK);
                g2.setFont(FONT_TASKS);
                Rectangle2D boundsTask = fmTask.getStringBounds(task.title, g2);
                g2.drawString(task.title, xPos + (int)(wTask - boundsTask.getWidth()) / 2, (int)(yPos + (boundsTask.getHeight() + SIZE_HEIGHT_TASK) / 2 - fmTask.getDescent() + 1));

                nRows = Math.max(task.row, nRows);
            }
            int hTheme = (nRows + 1) * SIZE_HEIGHT_TASK + (nRows + 2) * MARGIN_TASK;

            // Draw theme box
            g2.setFont(FONT_THEMES);
            g2.setColor(colorTheme);
            g2.fillRoundRect(wTitle, themeYPos, wTheme, hTheme, CORNER_SIZE_THEME, CORNER_SIZE_THEME);
            g2.setColor(COLOR_BORDER);
            g2.drawRoundRect(wTitle, themeYPos, wTheme, hTheme, CORNER_SIZE_THEME, CORNER_SIZE_THEME);

            // Draw theme border
//            int borderXPos = wTitle + wTheme;
//            g2.drawLine(borderXPos, themeYPos + hTheme, wColumns + borderXPos, themeYPos + hTheme);

            // Draw theme title
            Rectangle2D boundsTheme = fmTheme.getStringBounds(theme.title, g2);
            g2.setFont(FONT_THEMES);
            g2.setColor(isContrasted(COLOR_TEXT_TASK_LIGHT, colorTheme) ? COLOR_TEXT_TASK_LIGHT : COLOR_TEXT_TASK_DARK);
            g2.rotate(-Math.PI / 2);
            int yPos = (int)-(hTheme + boundsTheme.getWidth()) / 2 - themeYPos;
            int xPos = wTitle + (int)boundsTheme.getHeight() + MARGIN_THEME - 1;
            g2.drawString(theme.title, yPos, xPos);
            g2.setTransform(origTransform);

            themeYPos += hTheme;
        }

        g2.dispose();
        return result;
    }

    private static Color decodeColor(String colour) {
        // Decode hex
        return new Color(Integer.decode("0x" + colour));
    }

    private static int getXFromColPos(Roadmap r, Map<String, Integer> columnWidthsInc, String columnid, double columnpos) {
        RoadmapColumn column = getColumn(r, columnid);
        return columnWidthsInc.get(columnid) + (int)(column.width * columnpos) - column.width;
    }

    private static RoadmapColumn getColumn(Roadmap r, String id) {
        for (RoadmapColumn column : r.columns) {
            if (column.id.equals(id)) {
                return column;
            }
        }
        return null;
    }

    private static boolean isContrasted(Color c1, Color c2) {
        double L1 = getLuminosity(c1);
        double L2 = getLuminosity(c2);

        double contrast = (L1 + 0.05) / (L2 + 0.05);

        return contrast > 0.5;
    }

    private static double getLuminosity(Color color) {
        return (0.2126 * getLinearisedColor(color.getRed()) +
                0.7152 * getLinearisedColor(color.getGreen()) +
                0.0722 * getLinearisedColor(color.getBlue()));
    }

    private static double getLinearisedColor(int component) {
        return Math.pow(component / 0xFF, 2.2);
    }
}

