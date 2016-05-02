/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.stylebook.printers;

import org.apache.stylebook.*;
import javax.imageio.*;
import javax.imageio.plugins.jpeg.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * The ImagePrinter Printer writes images.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 313202 $ $Date: 1999-11-30 12:28:55 +0000 (Tue, 30 Nov 1999) $
 */
public class ImagePrinter extends AbstractComponent implements Printer {

    int R=0;
    int G=1;
    int B=2;
    int A=3;

    /**
     * Print a DOM Document.
     *
     * @param doc The Document to print.
     * @param out The OutputStream used for printing.
     * @param env The Environment of this printing request.
     * @exception IOException If an I/O error occourred accessing resources.
     * @exception CreationException If the Document cannot be printed.
     */
    public void print(Document doc, CreationContext ctx, OutputStream out)
    throws CreationException, IOException {
        // Check for proper root element
        Element element=doc.getDocumentElement();
        if (!element.getTagName().equals("image")) {
            throw new CreationException("Invalid document supplied",doc);
        }
        // Check wether we have a source image
        String source=element.getAttribute("source");
        BufferedImage img=null;
        Graphics2D gr=null;
        // If we have a source image, this is taken as our source.
        if (source.length()>0) {
            ImageIcon ico=null;
            if (source.indexOf(":/")>=0) {
                ico=new ImageIcon(new URL(source).openConnection().getURL());
            } else ico=new ImageIcon(source);

            if ((ico.getIconWidth()<1) | (ico.getIconHeight()<1)) {
                throw new CreationException("Cannot load image \""+source+"\"");
            }
            img=new BufferedImage(ico.getIconWidth(), ico.getIconHeight(),
                                  BufferedImage.TYPE_INT_RGB);
            gr=img.createGraphics();
            gr.drawImage(ico.getImage(),0,0,ico.getImageObserver());
        } else {
            // We don't have a source image. Build up a new getting height and
            // width from attributes
            int w=0;
            int h=0;
            try {
                String width=element.getAttribute("width");
                String height=element.getAttribute("height");
                w=Integer.parseInt(width);
                h=Integer.parseInt(height);
            } catch (NumberFormatException e) {
                throw new CreationException("Image witdth or height error");
            }
            if ((w<1) | (h<1)) {
                throw new CreationException("Image witdth or height unspecified");
            }
            img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
            gr=img.createGraphics();
            // Now process the bgcolor attribute for background colorization
            int bg=0;
            try {
                String bgcolor=element.getAttribute("bgcolor");
                bg=Integer.parseInt(bgcolor,16);
            } catch (NumberFormatException e) {
                throw new CreationException("Background color error");
            }
            Color c=new Color(bg);
            gr.setBackground(c);
            gr.clearRect(0,0,w,h);
            // Check if we have a background to tile
            String background=element.getAttribute("background");
            if (background.length()>0) {
                // We have a background image. Load it.
                ImageIcon ico=new ImageIcon(background);
                if ((ico.getIconWidth()<1) | (ico.getIconHeight()<1)) {
                    throw new CreationException("Cannot load background \""+source+"\"");
                }
                // Tile the image.
                int x=0;
                int y=0;
                while (true) {
                    gr.drawImage(ico.getImage(),x,y,ico.getImageObserver());
                    x+=ico.getIconWidth();
                    if (x>w) {
                        x=0;
                        y+=ico.getIconHeight();
                    }
                    if (y>h) break;
                }
            }
        }

        // Process child elements
        NodeList l=element.getChildNodes();
        for (int x=0;x<l.getLength();x++) {
            if (l.item(x).getNodeType()==Node.ELEMENT_NODE) {
                processElement((Element)l.item(x),img);
            }
        }

        ImageWriter encoder = (ImageWriter)ImageIO.getImageWritersByFormatName("JPEG").next();
        JPEGImageWriteParam param = new JPEGImageWriteParam(null);

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(1);

        encoder.setOutput(ImageIO.createImageOutputStream(out));
        encoder.write((IIOMetadata) null, new IIOImage(img,null,null), param);

        out.flush();
    }

    void processElement(Element e, BufferedImage i)
    throws CreationException {
        if (e==null) return;
        int R=0; int G=1; int B=2; int A=3;
        // Create new alpha image
        int w=i.getWidth();
        int h=i.getHeight();
        BufferedImage n=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        // Clear new alpha image
        int np[]=new int[]{0,0,0,0};
        WritableRaster nr=n.getRaster();
        for (int x=0;x<nr.getWidth();x++) for (int y=10;y<nr.getHeight();y++) {
            nr.setPixel(x,y,np);
        }
        // Check wich element is being processed.
        boolean ret=false;
        if (e.getTagName().equals("text")) ret=placeText(e,n);
        if(!ret) return;
        // Apply new image over existing
        WritableRaster ir=i.getRaster();
        int ip[]=new int[]{0,0,0,0};
        double ia=0;
        double na=1;
        for (int x=0;x<ir.getWidth();x++) for (int y=0;y<ir.getHeight();y++) {
            ir.getPixel(x,y,ip);
            nr.getPixel(x,y,np);
            if (np[A]>0) {
                na=((double)np[A]/255);
                ia=(1-na);
                ip[R]=(int)(((double)ip[R]*ia) + ((double)np[R]*na));
                ip[G]=(int)(((double)ip[G]*ia) + ((double)np[G]*na));;
                ip[B]=(int)(((double)ip[B]*ia) + ((double)np[B]*na));;
            }
            ir.setPixel(x,y,ip);
        }
    }

    private boolean placeText(Element e, BufferedImage i)
    throws CreationException {
        String text=e.getAttribute("text");
        String font=e.getAttribute("font");
        String ssize=e.getAttribute("size");
        String sstyle=e.getAttribute("style");
        String scolor=e.getAttribute("color");
        String sx=e.getAttribute("x");
        String sy=e.getAttribute("y");
        String halign=e.getAttribute("halign");
        String valign=e.getAttribute("valign");
        // Check proper text
        if (text.length()<1) return(false);
        // Check size, x and y parameters
        int color,size,insx,insy;
        try {
            size=Integer.parseInt(ssize);
            insx=Integer.parseInt(sx);
            insy=Integer.parseInt(sy);
            color=Integer.parseInt(scolor,16);
        } catch (NumberFormatException ex) {
            throw new CreationException("Attribute size, x, y or color error");
        }
        // Get style
        int style=Font.PLAIN;
        if (sstyle.equals("bold")) style=Font.BOLD;
        else if (sstyle.equals("italic")) style=Font.ITALIC;
        else if (sstyle.equals("bolditalic")) style=Font.BOLD+Font.ITALIC;
        Font f=new Font(font,style,size);
        FontMetrics m=i.createGraphics().getFontMetrics(f);
        // Create temporary image
        int w=m.stringWidth(text)*2;
        int h=m.getHeight()*2;
        BufferedImage n=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        // Write out some text
        Graphics2D gr=n.createGraphics();
        gr.setColor(new Color(0x0ff000000,true));
        gr.setFont(f);
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        gr.drawString(text,5,h-(h/4));
        // Resize image
        int minx=w;
        int miny=h;
        int maxx=0;
        int maxy=0;
        WritableRaster nr=n.getRaster();
        int np[]=new int[]{0,0,0,0};
        for (int x=0;x<nr.getWidth();x++) for (int y=10;y<nr.getHeight();y++) {
            nr.getPixel(x,y,np);
            if (np[A]>0) {
                if(x<minx) minx=x;
                if(x>maxx) maxx=x;
                if(y<miny) miny=y;
                if(y>maxy) maxy=y;
            }
        }
        if (halign.equals("right")) insx=insx-((maxx-minx)+1);
        if (halign.equals("center")) insx=insx-(((maxx-minx)+1)/2);
        if (valign.equals("bottom")) insy=insy-((maxy-miny)+1);
        if (valign.equals("center")) insy=insy-(((maxy-miny)+1)/2);
        WritableRaster ir=i.getRaster();
        int newx,newy;
        newy=insy;
        for (int y=miny;y<=maxy;y++) {
            newx=insx;
            if(newy>=0) {
                for (int x=minx;x<=maxx;x++) {
                    nr.getPixel(x,y,np);
                    np[R]=(color >> 16) & 0x0ff;
                    np[G]=(color >> 8) & 0x0ff;
                    np[B]=(color & 0x0ff);
                    if(newx>=0) ir.setPixel(newx,newy,np);
                    newx++;
                    if(newx>=i.getWidth()) break;
                }
            }
            newy++;
            if(newy>=i.getHeight()) break;
        }
        return(true);
    }
}

