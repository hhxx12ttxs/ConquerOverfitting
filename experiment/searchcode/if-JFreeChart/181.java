package com.luver;

import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Author: Vladislav Lubenskiy, vlad.lubenskiy@gmail.com
 */
public class Report {
    private double result;
    private double left;
    private double right;
    private double error;
    private JFreeChart chart;
    private ArrayList<double[]> table;
    private ArrayList<double[]> polynom;

    public JFreeChart getChart() {
        return chart;
    }

    public void setConstraints(double left, double right, double error) {
        this.left = left;
        this.right = right;
        this.error = error;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public void setChart(JFreeChart chart) {
        this.chart = chart;
    }

    public void setTable(ArrayList<double[]> table) {
        this.table = table;
    }

    public void setPolynom(ArrayList<double[]> polynom) {
        this.polynom = polynom;
    }

    public void saveToFile(String location) throws IOException {
        File directory = new File(location);
        directory.mkdir();

        if (!directory.canWrite()) {
            throw new RuntimeException("I can't write to file");
        }
        File chart = new File(location, "chart.png");
        chart.createNewFile();
        ImageIO.write(this.chart.createBufferedImage(500, 500), "png", chart);

        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(location + "/report.html"), Charset.defaultCharset()));
        writer.write("<html><head><title>??i?</title><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"></head><body>");
        writer.write("<p>???????: </p><table border=1><tr><td>x</td><td>y</td></tr>");

        for (double[] point : table) {
            writer.write("<tr><td>" + point[0] + "</td><td>" + point[1] + "</td></tr>");
        }
        writer.write("</table>");

        writer.write("<p>???i???: </p><p>");
        for (double[] point : polynom) {
            writer.write(point[1] + "x<sup>" + ((int) point[0]) + "</sup> + ");
        }
        writer.write("</p>");
        writer.write("<p>x?[" + left + ";" + right + "]</p>");
        writer.write("<p>? = " + error + "</p>");
        writer.write("<p>Root: " + result + "</p>");
        writer.write("<p><img src=\"chart.png\"/></p>");

        writer.write("</body></html>");
        writer.close();
    }
}

