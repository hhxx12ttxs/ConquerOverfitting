/*
	Sketch Elements: Chemistry molecular diagram drawing tool.
	
	(c) 2005 Dr. Alex M. Clark
	
	Released as GNUware, under the Gnu Public License (GPL)
	
	See www.gnu.org for details.
*/

package net.sf.sketchel;

import net.sf.sketchel.ds.*;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import java.beans.*;

// Previewing molecule-type files within the file choose mechanism.

public class FileMolPreview extends JLineup implements PropertyChangeListener
{
	private final int WIDTH=200,HEIGHT=200;
	private JLabel picture;
	private JComboBox typelist=null;

	private File file=null;

	public FileMolPreview(JFileChooser fc,boolean withType)
	{
		super(VERTICAL,1);
		fc.addPropertyChangeListener(this);
		
		picture=new JLabel();
		picture.setOpaque(true);
		picture.setBackground(Color.WHITE);
		picture.setBorder(new LineBorder(Color.BLACK,1));
		add(picture,null,1,1,NOINDENT);
		
		if (withType)
		{
			typelist=new JComboBox(FileTypeGuess.NAME_TYPES);
			add(typelist,"Type:",1,0);
		}
		
		setBlank();
	}
	
	public int getFormatType() {return typelist==null ? FileTypeGuess.TYPE_UNKNOWN : typelist.getSelectedIndex();}

	public void propertyChange(PropertyChangeEvent ev) 
	{
		boolean update=false;
		String prop=ev.getPropertyName();

		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) // changed directory, do nothing much
		{
			file=null;
			update=true;
		}
		else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) // file just got selected
		{
			file=(File)ev.getNewValue();
			update=true;
		}
		if (!update) return;

		if (typelist!=null) typelist.setSelectedIndex(0);

		if (file!=null && file.isFile() && file.exists() && file.canRead()) 
		{
			try 
			{
				FileTypeGuess ft=new FileTypeGuess(file);
				ft.guess();
				
				if (typelist!=null) typelist.setSelectedIndex(ft.getType());
				
				InputStream istr=null;
				if (ft.getType()==FileTypeGuess.TYPE_SKETCHEL) setMolecule(MoleculeReader.readNative(file));
				else if (ft.getType()==FileTypeGuess.TYPE_DATASHEET) 
				{
					DataSheetLoader ldr=new DataSheetLoader(new BufferedReader(new FileReader(file)));
					ldr.readUntil(Math.min(ldr.numRows(),9));
					ldr.close();
					setDataSheet(ldr);
				}
				else if (ft.getType()==FileTypeGuess.TYPE_MDLMOL) setMolecule(MoleculeReader.readUnknown(file));
				else if (ft.getType()==FileTypeGuess.TYPE_MDLSDF) {} // !! do something interesting
				else if (ft.getType()==FileTypeGuess.TYPE_CML) setMolecule(MoleculeReader.readCML(file));
				else if (ft.getType()==FileTypeGuess.TYPE_SVGMOL) setMolecule(MoleculeReader.readSVG(file));
				else if (ft.getType()==FileTypeGuess.TYPE_ODGMOL) setMolecule(MoleculeReader.readODG(file));
				else if (ft.getType()==FileTypeGuess.TYPE_ODFDS) {} // !! do something interesting
				else setBlank();
				
				if (istr!=null) istr.close();
			}
			catch (IOException ex)
			{
				setBlank();
				ex.printStackTrace();
			}
		}
		else setBlank();
	}
	
	private void setBlank()
	{
		BufferedImage img=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
		picture.setIcon(new ImageIcon(img));
	}
	
	private void setMolecule(Molecule mol)
	{
		BufferedImage img=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=(Graphics2D)img.getGraphics(); 
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		drawMolInBox(g,mol,new Rectangle(0,0,WIDTH,HEIGHT));
		picture.setIcon(new ImageIcon(img));
	}
	
	private void setDataSheet(DataSheetHolder ds)
	{
		BufferedImage img=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=(Graphics2D)img.getGraphics(); 
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		// draw in the title, if applicable
		int insetY=0;
		g.setColor(Color.BLACK);
		String str=ds.getTitle();
		if (str.length()>0)
		{
			g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
			FontMetrics fm=g.getFontMetrics();
			g.drawString(str,1,fm.getAscent()+1);
			insetY+=(fm.getAscent()*3)/2;
		}
		
		// display # columns and rows
		str=ds.numCols()+" column"+(ds.numCols()==1 ? "" : "s")+", "+ds.numRows()+" row"+(ds.numRows()==1 ? "" : "s");
		int fsz=12;
		while (fsz>6)
		{
			g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,fsz));
			if (g.getFontMetrics().stringWidth(str)<=WIDTH) break;
			fsz--;
		}
		g.drawString(str,1,insetY+fsz+1);
		insetY+=(fsz*3)/2;
		
		final int MAX_MOLS=9;
		Molecule[] mols=new Molecule[MAX_MOLS];
		int nmols=0;
		for (int i=0;i<ds.watermark() && i<MAX_MOLS && nmols<MAX_MOLS;i++) 
			for (int j=0;j<ds.numCols() && nmols<MAX_MOLS;j++) 
				if (ds.colType(j)==DataSheet.COLTYPE_MOLECULE && !ds.isNull(i,j))
					mols[nmols++]=ds.getMolecule(i,j);
		
		if (nmols>0)
		{
			int csz=Util.iceil(Math.sqrt(nmols)),rsz=Util.iceil((double)nmols/csz);
			int molW=WIDTH/csz,molH=(HEIGHT-insetY)/rsz;
			
			for (int n=0;n<nmols;n++)
			{
				int x=(n%csz)*molW,y=(n/csz)*molH;
				drawMolInBox(g,mols[n],new Rectangle(x,y+insetY,molW,molH));
			}
		}
		
		picture.setIcon(new ImageIcon(img));
	}
	
	private void drawMolInBox(Graphics2D g,Molecule mol,Rectangle r)
	{
		RenderPolicy pol=new RenderPolicy();
		double[] box=DrawMolecule.measureLimits(mol,pol,null);
		box[0]-=0.2; box[1]-=0.2; box[2]+=0.2; box[3]+=0.2;
		double aw=box[2]-box[0],ah=box[3]-box[1];
		double sw=r.width/aw,sh=r.width/ah,scale=Math.min(Math.min(sw,sh),30);
		
		int offsetX=(int)(0.5*r.width-scale*0.5*(box[0]+box[2]));
		int offsetY=(int)(0.5*r.height+scale*0.5*(box[1]+box[3]));

		DrawMolecule draw=new DrawMolecule(mol,g,scale);
		draw.setOffset(offsetX+r.x,offsetY+r.y);
		draw.setRenderPolicy(pol);
		draw.draw();
	}
}

