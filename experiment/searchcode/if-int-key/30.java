/*
	Sketch Elements: Chemistry molecular diagram drawing tool.
	
	(c) 2009 Dr. Alex M. Clark
	
	Released as GNUware, under the Gnu Public License (GPL)
	
	See www.gnu.org for details.
*/

package net.sf.sketchel.ds;

import net.sf.sketchel.*;

import java.io.*;
import java.awt.event.*;
import javax.swing.*;

/*
	An abstraction of work, which provides menu items and responses to them, in lieu of having the DataWindow do the work.
*/

public class DataManager implements ActionListener, StateListener
{
	private String filename=null,curDir=null;
	private SpreadSheet sheet;
	private StateListener slstn;
	private JFrame frameParent;

	private ConfigData cfg=null;

	private JMenu miRenderPolicy;

	private int rmbCol=-1,rmbRow=-1; // most recently right-clicked cell/col/row

	// main menubar items

	private final static boolean allKeys=true;
		
	private JMenuItem miFileNew=Util.menuItem(this,"New",KeyEvent.VK_N,null,key('N',InputEvent.CTRL_MASK));
	private JMenuItem miFileNewMol=Util.menuItem(this,"New Molecule",KeyEvent.VK_M);
	private JMenuItem miFileOpen=Util.menuItem(this,"Open",KeyEvent.VK_O,null,key('O',InputEvent.CTRL_MASK));
	private JMenuItem miFileSave=Util.menuItem(this,"Save",KeyEvent.VK_S,null,key('S',InputEvent.CTRL_MASK));
	private JMenuItem miFileSaveAs=Util.menuItem(this,"Save As",KeyEvent.VK_A);
	private JMenuItem miFileQuit=Util.menuItem(this,"Quit",KeyEvent.VK_Q,null,key('Q',InputEvent.CTRL_MASK));
	
	private JMenuItem miExportMDLSDF=Util.menuItem(this,"as MDL SDF",KeyEvent.VK_S,null,key('S',InputEvent.CTRL_MASK+InputEvent.SHIFT_MASK));
	private JMenuItem miExportODT=Util.menuItem(this,"as ODT",KeyEvent.VK_T,null,key('T',InputEvent.CTRL_MASK+InputEvent.SHIFT_MASK));
	private JMenuItem miExportODS=Util.menuItem(this,"as ODS",KeyEvent.VK_D,null,key('D',InputEvent.CTRL_MASK+InputEvent.SHIFT_MASK));
		
	private JMenuItem miEditUndo=Util.menuItem(this,"Undo",KeyEvent.VK_U,null,key('Z',InputEvent.CTRL_MASK));
	private JMenuItem miEditRedo=Util.menuItem(this,"Redo",KeyEvent.VK_R,null,key('Z',InputEvent.CTRL_MASK+InputEvent.SHIFT_MASK));
	private JMenuItem miEditAccept=Util.menuItem(this,"Accept",KeyEvent.VK_U,null,key(KeyEvent.VK_ENTER,0));
	private JMenuItem miEditReject=Util.menuItem(this,"Reject",KeyEvent.VK_J,null,key(KeyEvent.VK_ESCAPE,0));
	private JMenuItem miEditCell=Util.menuItem(this,"Edit Cell",KeyEvent.VK_E);
	private JMenuItem miEditMolNew=Util.menuItem(this,"Molecule New Window",KeyEvent.VK_W);
	private JMenuItem miEditAdd=Util.menuItem(this,"Add Row",KeyEvent.VK_A,null,key(KeyEvent.VK_INSERT,0));
	private JMenuItem miEditInsert=Util.menuItem(this,"Insert Row",KeyEvent.VK_I,null,key(KeyEvent.VK_INSERT,InputEvent.CTRL_MASK));
	private JMenuItem miEditDelete=Util.menuItem(this,"Delete Rows",KeyEvent.VK_D);
	private JMenuItem miEditMoveUp=Util.menuItem(this,"Move Rows Up",KeyEvent.VK_M,null,key(KeyEvent.VK_UP,InputEvent.ALT_MASK));
	private JMenuItem miEditMoveDown=Util.menuItem(this,"Move Rows Down",KeyEvent.VK_O,null,key(KeyEvent.VK_DOWN,InputEvent.ALT_MASK));
	private JMenuItem miEditCopy=Util.menuItem(this,"Copy",KeyEvent.VK_C,null,key('C',InputEvent.CTRL_MASK));
	private JMenuItem miEditCut=Util.menuItem(this,"Cut",KeyEvent.VK_T,null,key('X',InputEvent.CTRL_MASK));
	private JMenuItem miEditPaste=Util.menuItem(this,"Paste",KeyEvent.VK_P,null,key('V',InputEvent.CTRL_MASK));
	private JMenuItem miEditClear=Util.menuItem(this,"Clear",KeyEvent.VK_L,null,key(KeyEvent.VK_DELETE,InputEvent.CTRL_MASK));
	private JMenuItem miEditSummary=Util.menuItem(this,"Sheet Summary",KeyEvent.VK_S);
	private JMenuItem miEditCols=Util.menuItem(this,"Edit Columns",KeyEvent.VK_N);

	private JMenuItem miViewSingle=Util.menuItem(this,"Single Line",KeyEvent.VK_L,null,key('1',InputEvent.CTRL_MASK));
	private JMenuItem miViewSmall=Util.menuItem(this,"Small",KeyEvent.VK_S,null,key('2',InputEvent.CTRL_MASK));
	private JMenuItem miViewMedium=Util.menuItem(this,"Medium",KeyEvent.VK_M,null,key('3',InputEvent.CTRL_MASK));
	private JMenuItem miViewLarge=Util.menuItem(this,"Large",KeyEvent.VK_L,null,key('4',InputEvent.CTRL_MASK));

	private JMenuItem miHelpAbout=Util.menuItem(this,"About",KeyEvent.VK_A);
	private JMenuItem miHelpConfig=Util.menuItem(this,"Config",KeyEvent.VK_C);

	// right mouse menu items

	private JMenuItem rmbEdit=Util.menuItem(this,"Edit",0);
	private JMenuItem rmbClear=Util.menuItem(this,"Clear",0);
	private JMenuItem rmbCopy=Util.menuItem(this,"Copy",0);
	private JMenuItem rmbCut=Util.menuItem(this,"Cut",0);
	private JMenuItem rmbPaste=Util.menuItem(this,"Paste",0);
	
	private JMenuItem rmbColMoveLeft=Util.menuItem(this,"Move Left",0);
	private JMenuItem rmbColMoveRight=Util.menuItem(this,"Move Right",0);
	private JMenuItem rmbColDelete=Util.menuItem(this,"Delete Column",0);
	
	private JMenuItem rmbRowInsAbove=Util.menuItem(this,"Insert Above",0);
	private JMenuItem rmbRowInsBelow=Util.menuItem(this,"Insert Below",0);
	private JMenuItem rmbRowMoveUp=Util.menuItem(this,"Move Up",0);
	private JMenuItem rmbRowMoveDown=Util.menuItem(this,"Move Down",0);
	private JMenuItem rmbRowDelete=Util.menuItem(this,"Delete Row",0);

	public DataManager(SpreadSheet sheet,StateListener slstn,JFrame frameParent)
	{
		this.sheet=sheet;
		this.slstn=slstn;
		this.frameParent=frameParent;
		
		sheet.addStateListener(this);
		
		curDir=System.getProperty("user.dir");
		
		cfg=new ConfigData(".sketchel");
		try {cfg.loadFile();}
		catch (IOException ex) {cfg.useDefaults();}
		
		if (cfg.numPolicies()>0) sheet.setRenderPolicy(cfg.getPolicy(0).clone());
	}

	// ------------------ utility functions --------------------

	public String getFilename() {return filename;}
	
	// replaces the current datasheet with a new one, without changing the filename
	public void setDataSheet(DataSheetHolder ds)
	{
		sheet.setDataSheet(ds,new DataSheetCache());
	}
	
	// loads a new datasheet and replaces the old one, without question
	public void loadDataSheet(String FN)
	{
		DataSheetHolder newDS=null;
		FileInputStream istr=null;
		
		try
		{
			FileTypeGuess ft=new FileTypeGuess(new File(FN));
			ft.guess();
			if (ft.getType()==FileTypeGuess.TYPE_DATASHEET) newDS=DataSheetStream.readXML(istr=new FileInputStream(FN));
			else if (ft.getType()==FileTypeGuess.TYPE_MDLSDF) newDS=DataSheetStream.readSDF(istr=new FileInputStream(FN));
			else if (ft.getType()==FileTypeGuess.TYPE_ODFDS) newDS=DataSheetStream.readODF(istr=new FileInputStream(FN));
			else
			{
				String msg="["+FN+"]\n"+
						   "The file does not appear to be of the XML\n"+
						   "SketchEl DataSheet format, or an MDL SD file.";
				JOptionPane.showMessageDialog(null,msg,"Open Failed",JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,e.toString(),"Open Failed",JOptionPane.ERROR_MESSAGE);
		}
		finally 
		{
			try {if (istr!=null) istr.close();} catch (IOException e) {}
		}

		if (newDS==null) 
		{
			sheet.setDataSheet(new DataSheetHolder(),new DataSheetCache());
			if (slstn!=null) slstn.replaceTitle();
			return;
		}

		sheet.setDataSheet(newDS,new DataSheetCache());
		setFilename(FN);
		File parent=new File(FN).getAbsoluteFile().getParentFile();
		if (parent!=null) curDir=parent.getAbsolutePath();
	}
	
	// assembles the menu items, with appropriate context
	public void createMenuBar(JMenuBar menubar)
	{
		JMenu menufile=new JMenu("File");
		menufile.setMnemonic(KeyEvent.VK_F);
		menufile.add(miFileNew);
		menufile.add(miFileNewMol);
		menufile.add(miFileOpen);
		menufile.add(miFileSave);
		menufile.add(miFileSaveAs);
		JMenu menuexport=new JMenu("Export");
		menuexport.setMnemonic(KeyEvent.VK_X);
		menuexport.add(miExportMDLSDF);
		menuexport.add(miExportODT);
		menuexport.add(miExportODS);
		menufile.add(menuexport);
		if (frameParent!=null)
		{
			menufile.addSeparator();
			menufile.add(miFileQuit);
		}

		JMenu menuedit=new JMenu("Edit");
		menuedit.setMnemonic(KeyEvent.VK_E);
		menuedit.add(miEditUndo);
		menuedit.add(miEditRedo);
		menuedit.addSeparator();
		menuedit.add(miEditAccept);
		menuedit.add(miEditReject);
		menuedit.addSeparator();
		menuedit.add(miEditCell);
		menuedit.add(miEditMolNew);
		menuedit.addSeparator();
		menuedit.add(miEditAdd);
		menuedit.add(miEditInsert);
		menuedit.add(miEditDelete);
		menuedit.add(miEditMoveUp);
		menuedit.add(miEditMoveDown);
		menuedit.addSeparator();
		menuedit.add(miEditCopy);
		menuedit.add(miEditCut);
		menuedit.add(miEditPaste);
		menuedit.add(miEditClear);
		menuedit.addSeparator();
		menuedit.add(miEditSummary);
		menuedit.add(miEditCols);

		JMenu menuview=new JMenu("View");
		menuview.setMnemonic(KeyEvent.VK_V);
		menuview.add(miViewSingle);
		menuview.add(miViewSmall);
		menuview.add(miViewMedium);
		menuview.add(miViewLarge);
		menuview.addSeparator();
		miRenderPolicy=new JMenu("Render Policy");
		miRenderPolicy.setMnemonic(KeyEvent.VK_R);
		menuview.add(miRenderPolicy);

		JMenu menuhelp=new JMenu("Help");
		menuhelp.setMnemonic(KeyEvent.VK_H);
		menuhelp.add(miHelpAbout);
		menuhelp.add(miHelpConfig);

		menubar.removeAll();
		menubar.add(menufile);
		menubar.add(menuedit);
		menubar.add(menuview);
		menubar.add(Box.createHorizontalGlue());
		menubar.add(menuhelp);
		
		reviewMenuState();
	}

	private KeyStroke key(char key,int mods) {return KeyStroke.getKeyStroke(key,mods);}
	private KeyStroke key(int key,int mods) {return KeyStroke.getKeyStroke(key,mods);}

	private void setFilename(String FN)
	{
		filename=FN;
		if (slstn!=null) slstn.replaceTitle();
	}
	
	public void saveCurrent()
	{
		if (filename==null) return;
		try
		{
			int fmt=FileTypeGuess.TYPE_DATASHEET,doctype=0;
			
			if (filename.toLowerCase().endsWith(".sdf")) 
			{
				String msg="The filename to save ends with '.sdf', which is the\n"+
						   "conventional suffix for MDL SD-files. Exporting to\n"+
						   "this format will cause some information loss. Do you wish\n"+
						   "to save in MDL SD-file format?";
				if (JOptionPane.showConfirmDialog(null,msg,"Format",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
					fmt=FileTypeGuess.TYPE_MDLSDF;
			}
			else if (filename.toLowerCase().endsWith(".odt"))
			{
				String msg="The filename to save ends with '.odt', which is the\n"+
						   "conventional suffix for the OpenDocument Text\n"+
						   "format. Do you wish to create a document, with\n"+
						   "embedded source data?";
				if (JOptionPane.showConfirmDialog(null,msg,"Format",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
					{fmt=FileTypeGuess.TYPE_ODFDS; doctype=ODFComposer.DOCTYPE_ODT;}
			}
			else if (filename.toLowerCase().endsWith(".ods"))
			{
				String msg="The filename to save ends with '.ods', which is the\n"+
						   "conventional suffix for the OpenDocument Spreadsheet\n"+
						   "format. Do you wish to create a document, with\n"+
						   "embedded source data?";
				if (JOptionPane.showConfirmDialog(null,msg,"Format",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
					{fmt=FileTypeGuess.TYPE_ODFDS; doctype=ODFComposer.DOCTYPE_ODS;}
			}
			
			DataSheetHolder ds=sheet.getDataSheet();
			FileOutputStream ostr=new FileOutputStream(filename);
			if (fmt==FileTypeGuess.TYPE_DATASHEET) DataSheetStream.writeXML(ostr,ds);
			else if (fmt==FileTypeGuess.TYPE_MDLSDF) DataSheetStream.writeSDF(ostr,ds);
			else if (fmt==FileTypeGuess.TYPE_ODFDS)
			{
				ODFComposer odf=new ODFComposer(sheet.getDataSheet(),doctype);
				odf.setRenderPolicy(sheet.getRenderPolicy());
				odf.build(ostr);
			}
			ostr.close();
			ds.clearDirty();
			sheet.getCache().notifySave();
			if (slstn!=null) slstn.replaceTitle();
		}
		catch (IOException e)
		{
			Util.errmsg("Save Failed",e.toString());
		}
	}

	// ------------------ user responses --------------------

	public void fileQuit()
	{
		if (sheet.getDataSheet().isDirty())
		{
			if (JOptionPane.showConfirmDialog(null,
				"Current datasheet has been modified. Exit without saving?","Quit",
				JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
		}
		frameParent.dispose();
	}
	
	public void fileNew()
	{
		new DataWindow(null).setVisible(true);
	}
	
	public void fileNewMolecule()
	{
		new MainWindow(null,false).setVisible(true);
	}
	
	public void fileOpen()
	{
		JFileChooser chooser=new JFileChooser(curDir);
		chooser.setDragEnabled(false);
		/*if (filename!=null)
		{
			File parent=new File(filename).getAbsoluteFile().getParentFile();
			if (parent!=null) chooser.setCurrentDirectory(parent);
		}*/
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileExtFilter("DataSheet Formats",".ds;.xml;.sdf"));
		FileMolPreview prev=new FileMolPreview(chooser,true);
		chooser.setAccessory(prev);
		if (chooser.showOpenDialog(frameParent)!=JFileChooser.APPROVE_OPTION) return;
		String newfn=chooser.getSelectedFile().getPath();
		
		if (!new File(newfn).exists())
		{
			JOptionPane.showMessageDialog(null,
				new File(newfn).getAbsolutePath(),
				"File Not Found",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		int formatType=prev.getFormatType();
		if (formatType==FileTypeGuess.TYPE_UNKNOWN || formatType==FileTypeGuess.TYPE_DATASHEET || 
			formatType==FileTypeGuess.TYPE_MDLSDF || formatType==FileTypeGuess.TYPE_ODFDS)
		{
			DataSheetHolder ds=sheet.getDataSheet();
			if (ds.numRows()==0 && !ds.isDirty())
				loadDataSheet(newfn); // replace blank with new thing
			else 
				new DataWindow(newfn).setVisible(true); // pop up a new window
			return;
		}
		else
		{
			new MainWindow(newfn,false).setVisible(true);
		}
	}

	public void fileSave()
	{
		if (filename==null) {fileSaveAs(); return;}
		saveCurrent();
	}
	
	public void fileSaveAs()
	{
		JFileChooser chooser=new JFileChooser(curDir);
		chooser.setDragEnabled(false);
		/*if (filename!=null)
		{
			File parent=new File(filename).getAbsoluteFile().getParentFile();
			if (parent!=null) chooser.setCurrentDirectory(parent);
		}*/
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileExtFilter("Molecular DataSheets",".ds"));
		chooser.setAccessory(new FileMolPreview(chooser,false));
		if (chooser.showSaveDialog(frameParent)!=JFileChooser.APPROVE_OPTION) return;
	
		String fn=chooser.getSelectedFile().getPath();
		if (chooser.getSelectedFile().getName().indexOf('.')<0) fn=fn+".ds";
	
		File newf=new File(fn);
		if (newf.exists())
		{
			if (JOptionPane.showConfirmDialog(null,
				"Overwrite existing file ["+newf.getName()+"]?","Save As",
				JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
		}		
	
		setFilename(fn);
		saveCurrent();
		File parent=new File(fn).getAbsoluteFile().getParentFile();
		if (parent!=null) curDir=parent.getAbsolutePath();
	}
	
	public void fileExportSDF()
	{
		JFileChooser chooser=new JFileChooser(curDir);
		chooser.setDialogTitle("Export as MDL SDF");
		chooser.setDragEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new FileExtFilter("MDL SD files",".sdf"));
		chooser.setAccessory(new FileMolPreview(chooser,false));
		if (chooser.showSaveDialog(frameParent)!=JFileChooser.APPROVE_OPTION) return;
	
		String fn=chooser.getSelectedFile().getPath();
		if (chooser.getSelectedFile().getName().indexOf('.')<0) fn=fn+".sdf";
	
		File newf=new File(fn);
		if (newf.exists())
		{
			if (JOptionPane.showConfirmDialog(null,
				"Overwrite existing file ["+newf.getName()+"]?","Save As",
				JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
		}
		
		// !! perhaps a warning if there are multiple molecule fields?
		
		try
		{
			DataSheetHolder ds=sheet.getDataSheet();
			FileOutputStream ostr=new FileOutputStream(fn);
			DataSheetStream.writeSDF(ostr,ds);
			ostr.close();
		}
		catch (IOException e)
		{
			Util.errmsg("Export Failed",e.toString());
		}
	}
	
	public void fileExportODF(int doctype)
	{
		JFileChooser chooser=new JFileChooser(curDir);
		chooser.setDragEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (doctype==ODFComposer.DOCTYPE_ODT)
		{
			chooser.setDialogTitle("Export as OpenDocument Text");
			chooser.setFileFilter(new FileExtFilter("OpenDocument Text",".odt"));
		}
		else
		{
			chooser.setDialogTitle("Export as OpenDocument Spreadsheet");
			chooser.setFileFilter(new FileExtFilter("OpenDocument Spreadsheet",".ods"));
		}
		chooser.setAccessory(new FileMolPreview(chooser,false));
		if (chooser.showSaveDialog(frameParent)!=JFileChooser.APPROVE_OPTION) return;
	
		String fn=chooser.getSelectedFile().getPath();
		if (chooser.getSelectedFile().getName().indexOf('.')<0) fn=fn+(doctype==ODFComposer.DOCTYPE_ODT ? ".odt" : ".ods");
	
		File newf=new File(fn);
		if (newf.exists())
		{
			if (JOptionPane.showConfirmDialog(null,
				"Overwrite existing file ["+newf.getName()+"]?","Save As",
				JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
		}
		
		try
		{
			FileOutputStream ostr=new FileOutputStream(fn);
			ODFComposer odf=new ODFComposer(sheet.getDataSheet(),doctype);
			odf.setRenderPolicy(sheet.getRenderPolicy());
			odf.build(ostr);
			ostr.close();
		}
		catch (IOException e)
		{
			Util.errmsg("Export Failed",e.toString());
		}
	}

	public void editSummary()
	{
		DataSheetHolder ds=sheet.getDataSheet();
		DialogEditSummary edsumm=new DialogEditSummary(frameParent,ds);
		if (!edsumm.execute()) return;

		if (edsumm.resultTitle().equals(ds.getTitle()) && edsumm.resultDescr().equals(ds.getDescription())) return;

		sheet.cacheUndo();
		ds.setTitle(edsumm.resultTitle());
		ds.setDescription(edsumm.resultDescr());
		ds.setDirty();
		if (slstn!=null) slstn.replaceTitle();
	}

	public void editColumns()
	{
		DataSheetHolder ds=sheet.getDataSheet();
		DialogEditColumns edcols=new DialogEditColumns(frameParent,ds);
		if (!edcols.execute()) return;
		
		sheet.modifyColumns(edcols.resultOldPos(),edcols.resultNewPos(),
							edcols.resultName(),edcols.resultType(),edcols.resultDescr());
	}
	
	public void helpAbout()
	{
		MainPanel.helpAbout();
	}
	
	private void helpConfig()
	{
		cfg.refresh();
		ConfigData newCfg=new ConfigData(cfg);
		if (!new DialogConfig(frameParent,newCfg).exec()) return;
		cfg=newCfg;
		try 
		{
			cfg.saveFile();
			reviewMenuState();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,
				"Unable to save configuration file:\n  "+cfg.fullFN(),
				"Config Unwritable",JOptionPane.ERROR_MESSAGE);
		}
	}
	

	public void reviewMenuState()
	{
		// update the list of render policies
		miRenderPolicy.removeAll();
		for (int n=0;n<cfg.numPolicies();n++)
		{
			miRenderPolicy.add(Util.menuItem(this,cfg.getPolicy(n).name,0));
		}
	}

	// combines all molecules within the selection area, and opens them in a new molecule editing window
	public void openMolecules()
	{
		Molecule mol=new Molecule();
	
		DataSheetHolder ds=sheet.getDataSheet();
		int[] cols=sheet.getSelectedCols(),rows=sheet.getSelectedRows();
		for (int i=0;i<cols.length && mol.numAtoms()<1000;i++) 
			if (ds.colType(cols[i])==DataSheet.COLTYPE_MOLECULE)
				for (int j=0;j<rows.length && mol.numAtoms()<1000;j++)
		{
			ToolChest.addArbitraryFragment(mol,ds.getMolecule(rows[j],cols[i]));
		}
		
		if (mol.numAtoms()==0) 
		{
			Util.errmsg("No Data","The selected cells contain no molecule data.");
			return;
		}
		
		MainWindow mw=new MainWindow(null,false);
		mw.mainPanel().setMolecule(mol);
		mw.setVisible(true);
	}

	public void populateRightMouseCell(JPopupMenu menu,int col,int row)
	{
		rmbCol=col;
		rmbRow=row;
	
		boolean single=sheet.getLowerSelectedCol()==sheet.getUpperSelectedCol() && 
					   sheet.getLowerSelectedRow()==sheet.getUpperSelectedRow();
		
		if (single) menu.add(rmbEdit);
		menu.add(rmbClear);
		menu.add(rmbCopy);
		menu.add(rmbCut);
		menu.add(rmbPaste);
	}
	
	public void populateRightMouseColumn(JPopupMenu menu,int col)
	{
		rmbCol=col;
		rmbRow=-1;
		
		DataSheetHolder ds=sheet.getDataSheet();
		
		if (col>0) menu.add(rmbColMoveLeft);
		if (col<ds.numCols()-1) menu.add(rmbColMoveRight);
		if (ds.numCols()>1) menu.add(rmbColDelete); // disallow deleting of the last column
		// !! add an 'Edit' option, which opens up the editor panel, with the given row selected
	}
	
	public void populateRightMouseRow(JPopupMenu menu,int row)
	{
		rmbCol=-1;
		rmbRow=row;
		
		DataSheetHolder ds=sheet.getDataSheet();
		
		menu.add(rmbRowInsAbove);
		menu.add(rmbRowInsBelow);
		if (row>0) menu.add(rmbRowMoveUp);
		if (row<ds.numRows()-1) menu.add(rmbRowMoveDown);
		menu.add(rmbRowDelete);
	}


	// ------------------ event functions --------------------

	public void actionPerformed(ActionEvent e)
	{
		Object src=e.getSource();
		String cmd=e.getActionCommand();

		//System.out.println("CMD:["+cmd+"]");

		for (int n=0;n<miRenderPolicy.getItemCount();n++) if (miRenderPolicy.getItem(n)==src)
		{
			sheet.setRenderPolicy(cfg.getPolicy(n).clone());
			return;
		}

		if (src==miFileQuit) fileQuit();
		else if (src==miFileNew) fileNew();
		else if (src==miFileNewMol) fileNewMolecule();
		else if (src==miFileOpen) fileOpen();
		else if (src==miFileSave) fileSave();
		else if (src==miFileSaveAs) fileSaveAs();
		else if (src==miExportMDLSDF) fileExportSDF();
		else if (src==miExportODT) fileExportODF(ODFComposer.DOCTYPE_ODT);
		else if (src==miExportODS) fileExportODF(ODFComposer.DOCTYPE_ODS);
		else if (src==miEditUndo) sheet.actionUndo();
		else if (src==miEditRedo) sheet.actionRedo();
		else if (src==miEditAccept) sheet.acceptEdit();
		else if (src==miEditReject) sheet.rejectEdit();
		else if (src==miEditCell || src==rmbEdit) sheet.startEdit();
		else if (src==miEditMolNew) openMolecules();
		else if (src==miEditAdd) sheet.actionAddRow();
		else if (src==miEditInsert) sheet.actionInsertRow();
		else if (src==miEditDelete) sheet.actionDeleteRows();
		else if (src==miEditMoveUp) sheet.actionMoveRowsUp();
		else if (src==miEditMoveDown) sheet.actionMoveRowsDown();
		else if (src==miEditCopy || src==rmbCopy) sheet.copySelection();
		else if (src==miEditCut || src==rmbCut) sheet.cutSelection();
		else if (src==miEditPaste || src==rmbPaste) sheet.pasteSelection();
		else if (src==miEditClear || src==rmbClear) sheet.clearSelection();
		else if (src==miEditSummary) editSummary();
		else if (src==miEditCols) editColumns();
		else if (src==miViewSingle) sheet.sizeByMolecules(0);
		else if (src==miViewSmall) sheet.sizeByMolecules(1);
		else if (src==miViewMedium) sheet.sizeByMolecules(2);
		else if (src==miViewLarge) sheet.sizeByMolecules(3);
		else if (src==miHelpAbout) helpAbout();
		else if (src==miHelpConfig) helpConfig();
		else if (src==rmbColMoveLeft) sheet.actionMoveColumn(rmbCol,-1);
		else if (src==rmbColMoveRight) sheet.actionMoveColumn(rmbCol,1);
		else if (src==rmbColDelete) sheet.actionDeleteColumn(rmbCol);
		else if (src==rmbRowInsAbove) sheet.actionInsertRow(rmbRow,-1);
		else if (src==rmbRowInsBelow) sheet.actionInsertRow(rmbRow,1);
		else if (src==rmbRowMoveUp) sheet.actionMoveRow(rmbRow,-1);
		else if (src==rmbRowMoveDown) sheet.actionMoveRow(rmbRow,1);
		else if (src==rmbRowDelete) sheet.actionDeleteRow(rmbRow);
	}
	
	public void replaceTitle() {}
	public void dataModified() {}
	public void notifyEditStart(StateListener.EditInfo info)
	{
		// (maybe limit menu choices...)
	}
	public void notifyEditStop(StateListener.EditInfo info)
	{
		// (maybe restore menu choices...)
	}
	
}

