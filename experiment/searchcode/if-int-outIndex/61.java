package kinecttcpclient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** Currently undocumented experiment to align RGB and Depth images
 *
 * @author Rolf Lakaemper
 */
public class ImageCalibrator extends JPanel{
    JFrame f;
    JSlider shiftX, shiftY, zoom;
    BufferedImage image = null;
    double zoomValBest;
    int shiftXValBest, shiftYValBest, overlapBest;


    //-------------------------------------------------------------------------
    public ImageCalibrator (int[] im1, int[] im2){
        f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        setPreferredSize(new Dimension(640,480));
        image = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
        
        shiftX = new JSlider();
        shiftY = new JSlider(JSlider.VERTICAL);
        zoom = new JSlider(JSlider.VERTICAL);
        f.add(shiftX,"South");
        f.add(shiftY,"East");
        f.add(zoom,"West");

        f.getContentPane().add(this,"Center");
        f.pack();
        f.setVisible(true);

        calibrate(im1, im2);
    }

    //-------------------------------------------------------------------------
    public void paintComponent(Graphics g){
        if (image != null){
            g.drawImage(image,0,0, this);
        }
    }

    //-------------------------------------------------------------------------
    private void calibrate(int[] rgb, int[] depth){
        depth = flipImageLR(depth);
        int count = 0;
        int shiftXVal = -20;
        int shiftYVal = -20;
        double zoomVal = 1.1;

        while(true){
            // Transform
            shiftXVal = shiftXVal + 5;
            if (shiftXVal>20){
                shiftYVal = shiftYVal + 5;
                shiftXVal = -20;
            }
            if (shiftYVal>20){
                zoomVal = zoomVal-0.05;
                shiftYVal = -20;
            }
            if (zoomVal<0.7){
                break;
            }

            //zoomVal = Math.max(Math.min(zoomVal, 1.1),0.7);


            int []depthT = transformImage(depth,shiftXVal,shiftYVal,zoomVal);
            //overlay
            double [][] laplacian = new double[][]{{0.01,0.01,0.01},{0.01,-0.08,0.01},{0.01,0.01,0.01}};
            int[] edgeRGB = convolution(toGray(rgb),laplacian);
            int[] edgeDepth = convolution(toGray(depthT),laplacian);

            double [][] smooth = new double[][]{{0.1,0.1,0.1},{0.1,0.2,0.1},{0.1,0.1,0.1}};
            edgeRGB = convolution(edgeRGB,smooth);
            edgeDepth = convolution(edgeDepth,smooth);
            

            int overlap = imageIntersectionOverlap(edgeRGB, edgeDepth);
            if (overlap*zoomVal>overlapBest){
                System.out.println(overlapBest);
                overlapBest = (int)(overlap*zoomVal);
                shiftXValBest = shiftXVal;
                shiftYValBest = shiftYVal;
                zoomValBest = zoomVal;
            }
                
            int[] sum = addImages(edgeRGB, edgeDepth);
            depthT = transformImage(depth,shiftXValBest,shiftYValBest,zoomValBest);
            edgeDepth = convolution(toGray(depthT),laplacian);
            sum = addImages(sum, edgeDepth);
            image.setRGB(0,0,640,480,sum,0,640);
            repaint();
            f.repaint();

        }
        return;
    }

    //-------------------------------------------------------------------------
    public int[] addImages(int[] im1, int[] im2){
        if (im1.length!=im2.length){
            return(null);
        }
        int[]imOut = new int[im1.length];
        for (int i = 0; i<im1.length; i++){
            int r1,g1,b1,r2,g2,b2;
            r1 = (im1[i] & 0x00ff0000)>>16;
            g1 = (im1[i] & 0x0000ff00)>>8;
            b1 = (im1[i] & 0x000000ff);
            r2 = (im2[i] & 0x00ff0000)>>16;
            g2 = (im2[i] & 0x0000ff00)>>8;
            b2 = (im2[i] & 0x000000ff);
            r1 = (r1+r2)/2;
            g1 = (g1+g2)/2;
            b1 = (b1+b2)/2;

            int c = (0xff000000) + (r1<<16) + (g1<<8) + b1;
            imOut[i] = c;
        }
        return(imOut);
    }

    //-------------------------------------------------------------------------
    public int[] transformImage(int[]inIm, int sx, int sy, double zoom){
        int [] outIm = new int[inIm.length];
        int base = (int) (Math.sqrt(inIm.length/12));
        int resX = base*4;
        int resY = base*3;
        int [][] image = new int[resX][resY];
        int count = 0;

        // convert to 2D array
        for (int y = 0; y<resY; y++){
            for (int x=0; x<resX; x++){
                image[x][y]=inIm[count++];
            }
        }

        // Transformation
        // Zoom
        int[][]outImage = new int[resX][resY];
        if (zoom > 1.0){
            int newSizeX = (int)((double)resX/zoom);
            int newSizeY = (int)((double)resY/zoom);

            int ssX = (resX-newSizeX)/2;    // source start X
            int ssY = (resY-newSizeY)/2;    // source start Y

            for (int x = 0; x<resX; x++){
                for (int y=0; y<resY; y++){
                    int indX = (int)(Math.round(ssX + (double)x/resX*newSizeX));
                    int indY = (int)(Math.round(ssY + (double)y/resY*newSizeY));

                    outImage[indX][indY]=image[x][y];
                }
            }
        }
        if (zoom <= 1.0){
            int newSizeX = (int)((double)resX*zoom);
            int newSizeY = (int)((double)resY*zoom);

            int ssX = (resX-newSizeX)/2;    // source start X
            int ssY = (resY-newSizeY)/2;    // source start Y

            for (int x = 0; x<resX; x++){
                for (int y=0; y<resY; y++){
                    int indX = (int)(Math.round(ssX + (double)x/resX*newSizeX));
                    int indY = (int)(Math.round(ssY + (double)y/resY*newSizeY));

                    outImage[x][y]=image[indX][indY];
                }
            }
        }

        // Shift
        int[][]outImage2 = new int[resX][resY];
        for (int x = 0; x<resX; x++){
            for (int y=0; y<resY; y++){
                int indX = Math.min(Math.max(x+sx,0), resX-1);
                int indY = Math.min(Math.max(y+sy,0), resY -1);
                outImage2[indX][indY]=outImage[x][y];
            }
        }

        // convert back to 1D
        count = 0;
        for (int y = 0; y<resY; y++){
            for (int x=0; x<resX; x++){
                outIm[count++]=outImage2[x][y];
            }
        }
        return(outIm);
    }


    //-------------------------------------------------------------------------
    public int[]flipImageLR(int[] inIm){
        int[] outIm = new int[inIm.length];
        int base = (int) (Math.sqrt(inIm.length/12));
        int resX = base*4;
        int resY = base*3;
        int count = 0;
        for (int y = 0; y<resY; y++){
            for (int x=0; x<resX; x++){
                int index = y*resX + resX - x -1;
                outIm[index]  = inIm[count++];
            }
        }
        return(outIm);
    }

    //-------------------------------------------------------------------------
    public int[]toGray(int[] inIm){
        int[] out = new int[inIm.length];
        for (int i=0; i<out.length; i++){
            double red = (inIm[i]>>16)&0xff;
            double green = (inIm[i]>>8)&0xff;
            double blue = (inIm[i])&0xff;
            int outs=(int)(Math.round(0.2989 * red + 0.5870 * green + 0.1140 * blue));
            out[i]= 0xff000000 | (outs<<16) | (outs<<8) | (outs);
        }
        return(out);
    }

    //-------------------------------------------------------------------------
    // convolution of image with kernel.
    // assumes gray-value image (use 'toGray' first)
    // kernel does not have to be square.
    // inImage must have format 4:3
    // output: gray-value image.
    public int[] convolution(int[]inIm, double[][]kernel){
        // padding: embed image into larger 2D array with 0-margins
        int rows = (int)(Math.sqrt(inIm.length/12)*3);
        int cols = rows * 4 / 3;
        int kernelRows = kernel.length;
        int kernelCols = kernel[0].length;
        int kernelRadiusR = kernelRows/2;
        int kernelRadiusC = kernelCols/2;

        int[][] padded = new int[rows+kernelRows][cols+kernelCols];
        int [] outIm = new int[inIm.length];
        for (int r = 0; r<rows; r++){
            System.arraycopy(inIm,r*cols,padded[r+kernelRadiusC],kernelRadiusC,cols);
        }

        // convolution (assumes gray image. only the lowest byte of inIm is used!)
        int outIndex = 0;
        for (int r = kernelRadiusR; r < kernelRadiusR + rows; r++){
            for (int c = kernelRadiusC; c<kernelRadiusC+cols; c++){
                double sum = 0.0;
                for (int kr  = -kernelRadiusR; kr<=kernelRadiusR; kr++){
                    for (int kc = -kernelRadiusC; kc<=kernelRadiusC; kc++ ){
                        sum = sum + (double)(padded[r+kr][c+kc] & 0xff)* kernel[kr+kernelRadiusR][kc+kernelRadiusC];
                    }
                }
                int iSum = (int)(Math.min(Math.round(sum),255.0));
                outIm[outIndex] = 0xff000000 | (iSum<<16) | (iSum<<8) | (iSum);
                //outIm[outIndex] = padded[r][c];
                outIndex++;
            }
        }
        return(outIm);
    }

    //-------------------------------------------------------------------------
    // imageDifference
    // grayimages a,b: sum(|a-b|)
    public long imageDistance(int[] im1, int[] im2){
        long imDist=0;
        if (im1.length != im2.length){
            return (-1);
        }

        //
        for (int i = 0; i<im1.length; i++){
          imDist += Math.abs((im1[i]&0xff) - (im2[i]&0xff));
        }

        return(imDist);
    }

    //-------------------------------------------------------------------------
    // grayimages a,b: sum(a and b)
    public int imageIntersectionOverlap(int[] im1, int[] im2){
        int imDist=0;
        if (im1.length != im2.length){
            return (-1);
        }

        //
        for (int i = 0; i<im1.length; i++){
          if ((im1[i]&0xff)>0x40 && (im2[i]&0xff)>0x40){
              imDist++;
            }
        }
        return(imDist);
    }
}

