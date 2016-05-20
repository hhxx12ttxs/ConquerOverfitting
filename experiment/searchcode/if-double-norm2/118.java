/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package linear_algebra_calculator;

import Jama.Matrix;
import Jama.QRDecomposition;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Ryan
 */
public class PolynomialRegression extends JPanel
{
    public PolynomialRegression()
    {
        m_Failure = new JLabel("The matrices are not compatible.");
        m_Failure.setVisible(false);
        
        add(Box.createVerticalGlue());
        add(m_Failure);
        add(Box.createVerticalGlue());
    }
    
    public void SetPoints(double [][] val, int degree)
    {
        int N;
        double [] x = new double[val.length];
        double [] y = new double[val.length];
        
        this.degree = degree;
        
        m_Points = val;
        
        for(int i = 0; i < x.length; i++)
            x[i] = val[i][0];
        
        for(int i = 0; i < y.length; i++)
            y[i] = val[i][1];
        
        N = x.length;

        // build Vandermonde matrix
        double[][] vandermonde = new double[N][degree+1];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j <= degree; j++) {
                vandermonde[i][j] = Math.pow(x[i], j);
            }
        }
        Matrix X = new Matrix(vandermonde);

        // create matrix from vector
        Matrix Y = new Matrix(y, N);

        // find least squares solution
        QRDecomposition qr = new QRDecomposition(X);
        beta = qr.solve(Y);


        // mean of y[] values
        double sum = 0.0;
        for (int i = 0; i < N; i++)
            sum += y[i];
        double mean = sum / N;

        // total variation to be accounted for
        for (int i = 0; i < N; i++) {
            double dev = y[i] - mean;
            SST += dev*dev;
        }

        // variation not accounted for
        Matrix residuals = X.times(beta).minus(Y);
        SSE = residuals.norm2() * residuals.norm2();
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        
        Font font = new Font("Sans Serif", Font.PLAIN, 18);
        FontMetrics metrics = g2d.getFontMetrics(font);
        g2d.setFont(font);
        
        if(!m_Failure.isVisible())
        {
            String equation;
            equation = "f(x) = ";
            
            int j = degree;
            
            while(Math.abs(beta.get(j, 0)) < 1E-5)
                j--;
            
            for(; j >= 0; j--)
            {
                if(j == 0)
                    equation += String.format("%.2f", beta.get(j, 0));
                else if (j == 1)
                    equation += String.format("%.2f N + ", beta.get(j, 0));
                else
                    equation += String.format("%.2f N^%d + ", beta.get(j, 0), j);
            }
            equation += "   (R^2 = " + String.format("%.3f", 1.0 - (SSE/SST))  +  ")";
            
            int x = getWidth() /2  - metrics.stringWidth(equation)/2;
            int y = getHeight() - 5;
            
            g2d.drawString(equation, x, y);
        
            DrawGraph(g2d);
        }
        
    }
    
    private void DrawGraph(Graphics2D g2d)
    {
        int left = 25;
        int right = getWidth() - 25;
        int top = 25;
        int bottom = getHeight() - 25;
        
        int height = bottom - top;
        int width = right - left;
        
        double lowX = m_Points[0][0];
        double highX = m_Points[0][0];
        double lowY = m_Points[0][1];
        double highY = m_Points[0][1];
        for(double[] point : m_Points)
        {
            if(point[0] < lowX)
                lowX = point[0];
            
            if(point[0] > highX)
                highX = point[0];
            
            if(point[1] < lowY)
                lowY = point[1];
            
            if(point[1] > highY)
                highY = point[1];
        }
        
        /*
        System.out.println("lowX = " + lowX + "   highX = " + highX);
        System.out.println("lowY = " + lowY + "   highY = " + highY);
        */
        
        double xRange = highX + Math.abs(lowX);
        double yRange = highY + Math.abs(lowY);
        
        System.out.println("xRange = " + xRange + "yRange = " + yRange);
        
        
        // draw grid lines
        // x axis
        if(lowY >= 0)
        {
            g2d.drawLine(left, bottom, right, bottom);
        }
        else if(highY <= 0)
        {
            g2d.drawLine(left, top, right, top);
        }
        else
        {
            System.out.println("Math.abs(lowY) / xRange" + ((double)Math.abs(lowY)/ xRange));
            double yAxisLoc = bottom - (((double)Math.abs(lowY)/ yRange) * height);
            
            g2d.drawLine(left, (int) yAxisLoc, right, (int) yAxisLoc);
        }
        

        // y axis
        if(lowX >= 0)
        {
            g2d.drawLine(left, bottom, left, top);
        }
        else if(highX <= 0)
        {
            g2d.drawLine(right, bottom, right, top);
        }
        else
        {
            double range = highX + Math.abs(lowX);
            double xAxisLoc = left + (((double)Math.abs(lowX)/xRange) * width);
            
            g2d.drawLine((int) xAxisLoc, bottom,(int) xAxisLoc, top);
        }
        
        // draw points
        for(double[] point : m_Points)
        {
            double x = left + (((point[0] - lowX)/xRange) * width);
            double y = bottom - (((point[1] - lowY)/yRange) * height);
            g2d.setStroke(new BasicStroke(5));
            //System.out.println("x = " + x + "   y = " + y);
            g2d.drawLine((int) x, (int) y, (int) x, (int) y);
        }
        
        // draw curve
        int resolution = 1000;
        if(m_Points.length <= 2)
            resolution = 1;
        
        
        double lastX = lowX;
        double delta = (highX - lowX)/ resolution;
        
        for(int i = 0; i < resolution; i++)
        {
            //System.out.println("drawing");
            
            double x1 = left + (((lastX - lowX) / xRange) * width);
            double x2 = left + ((((lastX + delta) - lowX)/ xRange) * width);
            double y1 = bottom - (((getValueAt(lastX) - lowY)/yRange) * height);
            double y2 = bottom - (((getValueAt(lastX + delta) - lowY)/yRange) * height);
        
            //System.out.println("f(lastX) = " + getValueAt(lastX) + " f(lastX + delta) = " + getValueAt(lastX + delta));
            
           // System.out.println("x1 = " + x1 + "x2 = " + x2);
            //System.out.println("y1 = " + y1 + "y2 = " + y2);
            
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
            
            lastX = lastX + delta;
        }
    }
    
    public double getValueAt(double x)
    {
        double y = 0;
        int j = degree;
            
        while(Math.abs(beta.get(j, 0)) < 1E-5)
            j--;
            
        for(; j >= 0; j--)
        {
            y += beta.get(j, 0) * Math.pow(x, j);
        }
        
        return y;
    }
    
    private final JLabel m_Failure;
    private double[][] m_Points;
    private Matrix beta;
    private int degree;
    private double SST;
    private double SSE;
}

