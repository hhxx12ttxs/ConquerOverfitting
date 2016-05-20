
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.plugin.DICOM_Sorter;
import ij.plugin.MontageMaker;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ColorModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Read_BI_database.java
 *
 * Created on Oct 19, 2009, 11:29:53 AM
 */

/**
 *
 * @author Ilan
 */
public class Read_BI_database extends javax.swing.JDialog implements MouseListener {
	static final int TblPatName = 0;
	static final int TblStudy = 1;
	static final int TblDate = 2;
	static final int TblSeries = 3;
	static final int TblPatID = 4;
	static final int TblSize = 5;

    /** Creates new form Read_BI_database */
    public Read_BI_database(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
		init();
    }

	class ColorDateRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, value, hasFocus, hasFocus, row, row);

			try {
				if( numDays <= 0) return this;
//				if( isSelected) return this;
				SimpleDateFormat df1 = new SimpleDateFormat("d MMM yyyy");
				Date dt0, dt1;
				dt0 = new Date();
				dt1 = df1.parse((String) value);
				long diff = (dt0.getTime() - dt1.getTime())/(1000l*60*60*24);
				if( diff > numDays) setBackground(Color.red);
				else setBackground(Color.green);
			} catch (Exception e) { e.printStackTrace();}
			return this;
		}
	}

	class SeriesRenderer extends DefaultTableCellRenderer {
		ImageIcon openIcon = null;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			try {
				if( openIcon == null) {
					ClassLoader cldr = getClass().getClassLoader();
					java.net.URL imageURL = cldr.getResource("resources/open.gif");
					openIcon = new ImageIcon(imageURL);
				}
				if( value == null) setIcon(openIcon);
				else setIcon(null);
			} catch (Exception e) { e.printStackTrace();}
			return this;
		}
	}

	void init() {
		int i, x, y;
		jPrefer = Preferences.userNodeForPackage(Read_BI_Studies.class);
		jPrefer = jPrefer.node("biplugins");
		jTable1.addMouseListener(this);
		jTable1.setAutoCreateRowSorter(true);
		jCheckExit.setSelected(jPrefer.getBoolean("exit after read", true));
		jComboBox1.setSelectedIndex(jPrefer.getInt("db combo", 0));
		jCheckSeries.setVisible(false);
		jTextPatName.setText(jPrefer.get("last patient", null));
		jTextPatName.addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent arg0) {
//				super.keyTyped(arg0);
				unselectAllTableEntries();
				int i = arg0.getKeyChar();
				if( i == KeyEvent.VK_ENTER) readButton();
			}
		});

		numDays =jPrefer.getInt("num of days", 30);
		x = jPrefer.getInt("read dialog x", 0);
		y = jPrefer.getInt("read dialog y", 0);
		if(x > 0 && y > 0) {
			TableColumn col1;
			setSize(x, y);
			for( i=0; i<5; i++) {
				x = jPrefer.getInt("read dialog col"+i, 0);
				if( x<=0) continue;
				col1 = jTable1.getColumnModel().getColumn(i);
				col1.setPreferredWidth(x);
			}
		}
/*		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) { e.printStackTrace(); }*/
		jCurrDB = jPrefer.getInt("current database", 0);
		linuxURLdrive = -1;
		isInitialized = true;
		killRead = false;
		updateDbList();
		fillReadTable(-1);
	}

	void savePrefs() {
//		jPrefer.putBoolean("db read petct", jCheckPetCt.isSelected());
		jPrefer.putBoolean("exit after read", jCheckExit.isSelected());
		jPrefer.putInt("db combo", jComboBox1.getSelectedIndex());
		jPrefer.put("last patient", jTextPatName.getText());
		jPrefer.putInt("current database", jCurrDB);
		Dimension sz1 = getSize();
		jPrefer.putInt("read dialog x",sz1.width);
		jPrefer.putInt("read dialog y",sz1.height);
		boolean serFlg = jCheckSeries.isSelected();
		jPrefer.putBoolean("db series flag", serFlg);
		setOrSaveColumnWidths(0, true);
	}

	void updateDbList() {
		int i, last1;
		if( !isInitialized) return;
		boolean failed = false;
		String tmp1;
		JRadioButton curBut;
		buttonGroup1 = new javax.swing.ButtonGroup();
		last1 = -1;
		for( i=0; i<10; i++) {
			curBut = getDBbutton(i+1);
			curBut.setVisible(false);
			buttonGroup1.add(curBut);
			if( !failed) {
				tmp1 = jPrefer.get("ODBC" + i, null);
				if( tmp1 != null && !tmp1.isEmpty()) last1 = i;
				else failed = true;
			}
		}
		jLabelDbName.setVisible(last1 > 0);
		jButRead.setEnabled(last1 >= 0);
		if( last1 > 0) {
			if( jCurrDB > last1) jCurrDB = last1;
			for( i=0; i<=last1; i++) {
				curBut = getDBbutton(i+1);
				curBut.setVisible(true);
			}
			curBut = getDBbutton( jCurrDB+1);
			curBut.setSelected(true);
		} else {
			jCurrDB = 0;
		}
		changeDBSelected(jCurrDB + 1);
	}

	/** Used to read study when user double clicks on table. */
	@Override
	public void mouseClicked(MouseEvent e) {
		int i = e.getClickCount();
		if( i == 1) seriesMouseClick(e);
		if( i == 2) readButton();
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}

	Connection openDBConnection() {
		Connection conn1 = null;
		String ODBCName= null, ODBCPassword=null, tmp0, tmp1=null;
		try {
			tmp0 = "sun.jdbc.odbc.JdbcOdbcDriver";
			ODBCName =jPrefer.get("ODBC" + jCurrDB, null);
			ODBCUser = jPrefer.get("db user" + jCurrDB, null);
			ODBCPassword = jPrefer.get("db pass" + jCurrDB, null);
			m_dataPath = jPrefer.get("db path" + jCurrDB, null);
			if( ODBCName == null || ODBCName.isEmpty()) return null;
			int i = jPrefer.getInt("db type" + jCurrDB, 0);
			switch(i) {
				case 0:
				case 1:
					tmp1 = "jdbc:odbc:" + ODBCName;
					break;

				case 2:
					tmp0 = "net.sourceforge.jtds.jdbc.Driver";
					tmp1 = "jdbc:jtds:sqlserver:" + ODBCName;
					break;
			}
			Class.forName(tmp0);
			if( ODBCUser.isEmpty() || ODBCPassword.isEmpty())
				conn1 = DriverManager.getConnection(tmp1);
			else conn1 = DriverManager.getConnection(tmp1, ODBCUser, ODBCPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn1;
	}

	void readButton() {
		int n = jTable1.getSelectedRowCount();
		Container c = getContentPane();
		c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if( n <= 0) {
			fillReadTable( jComboBox1.getSelectedIndex());
			c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		}
		loadData();
	}

	void doRead() {
		int n = jTable1.getSelectedRowCount();
		int cols = 5;
//		if( jCheckSeries.isSelected()) cols = 5;
		String [] row1 = new String[cols];
		String path1;
		int [] selected = jTable1.getSelectedRows();
		int i, j, k;
		File[] files1;
		for( i=0; i<n; i++) {
			selected[i] = jTable1.convertRowIndexToModel(selected[i]);
		}
		out1: for( j=0; j<n; j++) {
			k = selected[j];
			for( i=0; i<cols; i++) {
				row1[i] = (String) jTable1.getModel().getValueAt(k, i);
			}
			files1 = queryRow4NM( row1, cols);
			if( files1 == null) {
				IJ.log("query failed for " + row1[TblPatName] +
						"  " + row1[TblDate] + "  " + row1[TblStudy]);
				continue;
			}

			for(k=0; k<files1.length; k++) {
				path1 = files1[k].getPath();
				readFiles(path1);
				if( killRead) break out1;
			}
		}
		unselectAllTableEntries();
		Container c = getContentPane();
		c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		if( jCheckExit.isSelected()) dispose();
	}

	void loadData() {
		work2 = new bkgdLoadData();
		work2.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String propertyName = evt.getPropertyName();
				if( propertyName.equals("state")) {
					SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
					if( state == SwingWorker.StateValue.DONE) {
						work2 = null;
					}
				}
			}
		});
		work2.execute();
	}

	boolean readFiles(String path1) {
		int i, n, width = 0, height=0, depth=0, count = 0;
		Calibration cal = null;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		double progVal;
		String info, imgTitle = null, label1= null;
		File[] list1 = null;
		String[] frameText = null;
		File dir1 = new File(path1);
		try {
			if( !dir1.isDirectory()) return false;
			list1 = dir1.listFiles();
			ImagePlus imp = null;
			ImageStack stack = null;
			n = list1.length;
			info = null;
			for( i=0; i<n; i++) {
				count++;
				progVal = ((double) count)/n;
				IJ.showStatus(count+"/"+n);
				IJ.showProgress(progVal);
//				IJ.showProgress(count,n);
				Opener opener = new Opener();
				opener.setSilentMode(false);
				imp = opener.openImage(list1[i].getPath());
				if (killRead) {
					stack = null;
					imp = null;
					IJ.showProgress(1.0);
					return false;
				}
				if( imp == null) {
					imgTitle = list1[i].getName();
					if( imgTitle.startsWith("graphic") && imgTitle.endsWith("gr1"))
						frameText = getFrameText(list1[i].getPath());
					continue;
				}
				if( stack==null) {
					width = imp.getWidth();
					height = imp.getHeight();
					depth = imp.getStackSize();
//					bitDepth = imp.getBitDepth();
					imgTitle = imp.getTitle();
					cal = imp.getCalibration();
					fi = imp.getOriginalFileInfo();
					ColorModel cm = imp.getProcessor().getColorModel();
					stack = new ImageStack(width, height, cm);
				}
				if( width != imp.getWidth() || height != imp.getHeight()) continue;
				info = (String)imp.getProperty("Info");
				label1 = null;
				if (depth==1) {
					label1 = imp.getTitle();
					if (info!=null)
						label1 += "\n" + info;
				}
				ImageStack inputStack = imp.getStack();
				for (int slice=1; slice<=inputStack.getSize(); slice++) {
					ImageProcessor ip = inputStack.getProcessor(slice);
					if (ip.getMin()<min) min = ip.getMin();
					if (ip.getMax()>max) max = ip.getMax();
					stack.addSlice(label1, ip);
				}
			}
			if( stack != null && stack.getSize() > 0) {
				stack = (new DICOM_Sorter()).sort(stack);
				fi.fileFormat = FileInfo.UNKNOWN;
				fi.fileName = "";
				fi.directory = path1;
				ImagePlus imp2 = new ImagePlus(getTitleInfo(info), stack);
				imp2.getProcessor().setMinAndMax(min, max);
				if( list1.length == 1) {
					imp2.setProperty("Info", info);
//					fi.description = "spect";
				}
				imp2.setFileInfo(fi);
				imp2.setCalibration(cal);
				if( frameText != null) for( i=0; i < frameText.length; i++) {
					label1 = frameText[i];
					if( label1 != null) stack.setSliceLabel(label1, i+1);
				}
				myMakeMontage(imp2, info, frameText != null);
			}
			IJ.showProgress(1.0);
		} catch (Exception e)  { e.printStackTrace(); }
		return true;
	}

	/**
	 * Read the graphic file and search it for frame text.
	 * It will parse this file and return set of strings, one string for each frame.
	 * The whole object may be null, or certain frames may be null.
	 * @param path - location of the  file
	 * @return string for each frame with its text.
	 */
	String[] getFrameText(String path) {
		String[] retVal = null;
		try {
			boolean line1 = true;
			String tmp, tmpStr;
			int i1, y1, frm1, maxFrm = -1;
			char c1;
			Scanner sc;
			Vector<Integer> pos, frmNm, yPos;
			Vector<String> strVals = new Vector<String>();
			FileReader fl1 = new FileReader(path);
			BufferedReader br1 = new BufferedReader(fl1);
			frmNm = new Vector<Integer>();
			yPos = new Vector<Integer>();
			while( (tmp = br1.readLine()) != null) {
				if( line1) {
					line1 = false;
					if( !tmp.startsWith("ver 1.4")) return null;
					i1 = tmp.indexOf(",");
					if( i1 <= 0) return null;
					while( --i1 > 0) {
						c1 = tmp.charAt(i1);
						if( c1 < '0' || c1 >'9') break;
					}
					tmp = tmp.substring(i1+1);
				}
				if( tmp.isEmpty()) continue;
				i1 = tmp.indexOf('\t');
				if( i1 <= 0) continue;
				tmpStr = tmp.substring(i1+1);
				tmp = tmp.substring(0, i1);
				sc = new Scanner(tmp).useDelimiter(",");
				pos = new Vector<Integer>();
				while( sc.hasNextInt()) {
					i1 = sc.nextInt();
					pos.add(i1);
				}
				if( pos.size() != 4 || pos.elementAt(3) != 1) continue;
				frm1 = pos.elementAt(2);
				if( frm1 > maxFrm) maxFrm = frm1;
				frmNm.add(frm1);
				yPos.add(pos.elementAt(1));
				strVals.add(tmpStr);
			}

			if( frmNm.size() > 0 && maxFrm >= 0) {
				retVal = new String[maxFrm+1];
				Integer[] maxY = new Integer[maxFrm+1];
				for( i1=0; i1<frmNm.size(); i1++) {
					frm1 = frmNm.elementAt(i1);
					y1 = yPos.elementAt(i1);
					if(maxY[frm1] != null  && y1 < maxY[frm1]) continue;
					maxY[frm1] = y1;
					retVal[frm1] = strVals.elementAt(i1);
				}
			}
		} catch (Exception e) { e.printStackTrace();}
		return retVal;
	}

	void myMakeMontage(ImagePlus imp, String info, boolean label) {
		int nSlices = imp.getStackSize();
		ImagePlus impMon = null;
		int columns, rows, first, last, inc, borderWidth;
		double scale;
		int maxSlices = jPrefer.getInt("montage slices", 20);
		if( nSlices < 2 || nSlices > maxSlices) {
			imp.show();	// show a normal stack
			return;
		}
		columns = (int) Math.sqrt(nSlices);
		rows = columns;
		int n = nSlices - columns*rows;
		if (n>0) columns += (int)Math.ceil((double)n/rows);
		first = 1;
		last = nSlices;
		inc = 1;
		borderWidth = 0;
		scale = 1.0;
		if (imp.getWidth()*columns>800) scale = 0.5;
		MontageMaker mm = new MontageMaker();
		impMon = mm.makeMontage2( imp, columns, rows, scale, first, last, inc, borderWidth, label);
		String title = imp.getTitle();
		if( impMon != null) {
			impMon.setTitle(title);
			impMon.setProperty("Info", info);
			impMon.show();
		}
	}

	File [] queryRow4NM( String [] row1, int cols) {
		File[] ret1 = null;
		int i, n;
		Vector<File> flVect = new Vector<File>();
		File currFile = null;
		String sql, flName, series, mrn1;
		mrn1 = compressID( row1[TblPatID]);
		series = cleanIt(row1[TblSeries]);
		sql = "select filename from studies where pat_id = '" + mrn1;
		sql += "' and label1 = '" +  cleanIt(row1[TblStudy]) + "'";
		if( cols > 4 && series != null) {
			if( !series.isEmpty()) sql +=  " and label2 = '" + series + "'";
			else sql += " and (label2 is null or label2 ='') ";
		}
		sql += " and sty_date = ";
		if( isAccessDb) sql += "#" + row1[TblDate] + "#";
		else sql += "'" + row1[TblDate] + "'";

		try {
			Connection conn1 = openDBConnection();
			Statement stm = conn1.createStatement();
			ResultSet rSet = stm.executeQuery(sql);
			while( rSet.next()) {
				flName = rSet.getString(1);
				currFile = getDicomFromArchive(flName);
				if( currFile == null) continue;
				flVect.add(currFile);
			}
			n = flVect.size();
			if( n>0) {
				ret1 = new File[n];
				for(i=0; i<n; i++) {
					ret1[i] = flVect.elementAt(i);
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		return ret1;
	}

	String getTitleInfo( String meta) {
		String ret1 = null;
		Date date1;
		String patName, patID, studyName, series;
		patName = getDicomValue( meta, "0010,0010");
		if( patName == null) return null;
		patName = compressDicomName(patName);
		patID = getDicomValue( meta, "0010,0020");
		date1 = getDateTime(getDicomValue( meta, "0008,0020"), null);
		studyName = getDicomValue( meta, "0008,1030");
		series = getDicomValue( meta, "0008,103E");
		if( series == null || series.isEmpty()) series = getDicomValue( meta, "0054,0400");
		ret1 = patName + "   " + patID + "   ";
		ret1 += DateFormat.getDateInstance(DateFormat.MEDIUM).format(date1);
		ret1 += "   " + studyName + "   " + series;
		return ret1;
	}

	static String getMeta(int slice, ImagePlus img1) {
		String meta = img1.getStack().getSliceLabel(slice);
		// meta will be null for SPECT studies
		if (meta == null || meta.indexOf("0010,0010") < 0) meta = (String) img1.getProperty("Info");
		return meta;
	}

	static String getDicomValue( String meta, String key1) {
		String tmp1, ret1 = null;
		int k0 = meta.indexOf(key1);
		if( k0 > 0) {
			int k1 = meta.indexOf("\n", k0);
			if( k1 < 0) return null;
			tmp1 = meta.substring(k0, k1);
			k1 = tmp1.indexOf(": ");
			if( k1 > 0) ret1 = tmp1.substring(k1+2);
			else ret1 = tmp1;
			ret1 = ret1.trim();
		}
		return ret1;
	}

	static int parseInt( String tmp1) {
		int ret1 = 0;
		if( tmp1 != null) ret1 = Integer.parseInt(tmp1);
		return ret1;
	}

	static String compressID( String in1) {
		String ret1, ret0;
		if( in1 == null || in1.isEmpty()) return "0";
		ret0 = in1.toLowerCase();
		int i, i1, n = ret0.length();
		char a1;
		ret1 = "";
		for( i = i1 = 0; i < n; i++) {
			a1 = ret0.charAt(i);
			if( (a1 >= '0' && a1 <= '9') || a1 >= 'a' && a1 <= 'z') {
				if( i1 == 0 && a1 == '0') continue;
				ret1 = ret1 + a1;
				i1++;
			}
		}
		if( i1 == 0) return "0";
		return ret1;
	}

	static String compressDicomName(String inName) {
		String retVal = inName.trim();
		int i = retVal.indexOf('^');
		if( i < 0) return retVal;
		retVal = retVal.substring(0, i) + "," + retVal.substring(i+1);
		retVal = retVal.replace('^', ' ');
		return retVal;
	}

	/**
	 * Helper routine to convert from Dicom style date-time to Java date-time.
	 * @param inDate Dicom date format
	 * @param inTime Dicom time format
	 * @return Java Date object
	 */
	public static Date getDateTime(String inDate, String inTime) {
		Date retDate;
		GregorianCalendar dat1 = new GregorianCalendar();
		int year = 0, month = 0, day = 0, hour = 0, min1 = 0, sec = 0;
		if( inDate != null && inDate.length() >= 8) {
			year = Integer.valueOf(inDate.substring(0, 4));
			month = Integer.valueOf(inDate.substring(4, 6)) - 1;	// month 0 based
			day = Integer.valueOf(inDate.substring(6, 8));
		}
		if( inTime != null && inTime.length() >= 6) {
			hour = Integer.valueOf(inTime.substring(0, 2));
			min1 = Integer.valueOf(inTime.substring(2, 4));
			sec = Integer.valueOf(inTime.substring(4, 6));
		}
		dat1.set(year, month, day, hour, min1, sec);
		retDate = dat1.getTime();
		return retDate;
	}

	void seriesMouseClick(MouseEvent e) {
		if( jTable1.columnAtPoint(e.getPoint()) != TblSeries) return;
		int j, i = jTable1.getSelectedRow();
		i = jTable1.convertRowIndexToModel(i);
//		Vector<Integer> remove1;
		Vector<String> serVect;
		DefaultTableModel mod1;
		mod1 = (DefaultTableModel) jTable1.getModel();
		boolean petCt = false;
		String tmp1;
		String patName = (String) mod1.getValueAt(i, TblPatName);
		String study = (String) mod1.getValueAt(i, TblStudy);
		String date1 = (String) mod1.getValueAt(i, TblDate);
		String mrn1 = (String) mod1.getValueAt(i, TblPatID);
		mrn1 = compressID(mrn1);
		String series = (String) mod1.getValueAt(i, TblSeries);
		if( series == null) {
			unselectAllTableEntries();
			String[] row1 = new String[TblPatID+1];
			row1[TblPatName] = patName;
			row1[TblStudy] = study;
			row1[TblDate] = date1;
			row1[TblPatID] = mrn1;
			String sql = "select distinct label2 from studies where pat_id = '" + mrn1;
			sql += "' and label1 = '" + cleanIt(study) + "' and sty_date = ";
			if( isAccessDb) sql += "#" +date1 + "#";
			else sql += "'" + date1 + "'";
			if( petCt) sql += " and (sty_type='CT' or sty_type='MR')";

			try {
				Connection conn1 = openDBConnection();
				Statement stm = conn1.createStatement();
				ResultSet rSet = stm.executeQuery(sql);
				serVect = new Vector<String>();
				while( rSet.next()) {
					series = rSet.getString(1);
					if( series == null) series = "";	// empty string
					if( petCt) {
						tmp1 =series.toLowerCase();
						if( tmp1.contains("scout")) continue;
					}
					serVect.add(series);
				}
				if( serVect.size() > 0) {
					mod1.setValueAt(serVect.elementAt(0), i, TblSeries);
				}
				for( j=1; j<serVect.size(); j++) {
					row1[TblSeries] = serVect.elementAt(j);
					mod1.insertRow(j+i, row1);
				}
			} catch (Exception e1) { e1.printStackTrace(); }

		} /* else {
			int k, n;
			n = mod1.getRowCount();
			unselectAllTableEntries();
			remove1 = new Vector<Integer>();
			for( j=0; j<n; j++) {
				if( i == j || !patName.equals((String) mod1.getValueAt(j, TblPatName))) continue;
				if( !study.equals((String) mod1.getValueAt(j, TblStudy))) continue;
				if( !date1.equals((String) mod1.getValueAt(j, TblDate))) continue;
				if( !mrn1.equals((String) mod1.getValueAt(j, TblPatID))) continue;
				remove1.add(j);
			}
			mod1.setValueAt(null, i, TblSeries);
			for( j=0; j<remove1.size(); j++) {
				k = remove1.elementAt(j) - j;
				mod1.removeRow(k);
			}
		}*/
	}

	String cleanIt(String in1) {
		if( in1 == null) return null;
		String ret1 = in1;
		int i = ret1.indexOf("'");
		if( i > 0) {
			ret1 = in1.substring(0, i+1) + in1.substring(i);
		}
		return ret1;
	}

	File getDicomFromArchive( String flName) {
		int i, j, year, month, day;
		String tmp1, tmp2, paths, path1;
		final char dirSepChar = File.separatorChar;
		try {
			i = convertLetter(flName.charAt(0));
			j = convertLetter(flName.charAt(1));
			year = (i << 2) + (j >> 3) + 1980;
			i = convertLetter( flName.charAt(2));
			month = ((j & 7) << 1) + (i >> 4);
			j =convertLetter(flName.charAt(3));
			day = ((i & 15) << 1) + (j >> 4);
			tmp1 = "ic" + year + dirSepChar + String.format("ic%02da%02d", month, day/7);
			tmp1 += dirSepChar + flName + "_dcm";

			paths = new String(m_dataPath);
			Character ch1 = new Character(dirSepChar);
			while( paths != null) {
				i = paths.indexOf("|");
				if( i>0) {
					path1 = paths.substring(0, i);
					paths = paths.substring(i+1);
				} else {
					path1 = paths;
					paths = null;
				}
				if( !path1.endsWith(ch1.toString())) path1 += dirSepChar;
				tmp2 = path1 + tmp1;
				File fl1 = new File(tmp2);
				if( fl1.exists()) {
					return fl1;
				}
				reportFailure2Log(tmp2);
			}
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	int convertLetter( char in) {
		if( in >= 'a' && in <= 'z') return in - 'a';
		if( in >= 'A' && in <= 'Z') return in - 'A';
		if( in >= '0' && in <= '9') return in - '0' + 26;
		return -1;
	}

	void reportFailure2Log(String path) {
		String path1 = path;
		File fl1;
		final char dirSepChar = File.separatorChar;
		int i;
		i = path1.lastIndexOf(dirSepChar);
		while( i>5) {
			path1 = path1.substring(0, i);
			fl1 = new File(path1);
			if( fl1.exists()) {
				IJ.log(path + "  doesn't exist, but this does: " + path1);
				return;
			}
			i = path1.lastIndexOf(dirSepChar);
		}
		IJ.log("total path failure at: " + path);
	}

	void maybeRereadTable() {
		int indx = -1;
		int n = jTable1.getRowCount();
		if( n > 0) indx =  jComboBox1.getSelectedIndex();
		fillReadTable(indx);
	}

	void fillReadTable( int indx) {
		boolean serFlg = jCheckSeries.isSelected();
//		int i = 4;
//		if( serFlg) i =5;
		DefaultTableModel mod1;
		mod1 = (DefaultTableModel) jTable1.getModel();
		mod1.setNumRows(0);
		setOrSaveColumnWidths(0, false);
		if( indx < 0) return;
		String name1 = jTextPatName.getText().trim();
		String sql = "select distinct name, s.pat_id, sty_date, label1";
		if( serFlg) sql += ", label2";
		sql += " from patients p, studies s where p.pat_id=s.pat_id and ";
		switch(indx) {
			case 0:
				if( isID(name1)) {
					name1 = compressID(name1);
					sql += "s.pat_id = '" + name1 + "'";
				}
				else sql += "name like '" + compressName(name1) + "%'";
				break;

			case 1:
				sql = sql.substring(0, sql.length()-4);	// remove last "and "
				break;

			case 2:
				sql += "label1 like '" + name1 + "%'";
				break;

			case 3:
				sql += "label2 like '" + name1 + "%'";
				break;

			case 4:
				sql += "label3 like '" + name1 + "%'";
				break;
		}
		if( isAccessDb) {
			sql += " and sty_date between #" + getDate(0) + "# and #" + getDate(1) + "#";
		} else {
			sql += " and sty_date between '" + getDate(0) + "' and '" + getDate(1) + "'";
		}
		try {
			// PatID is the last column
			String[] row1 = new String[TblPatID+1];
			Connection conn1 = openDBConnection();
			Statement stm = conn1.createStatement();
			ResultSet rSet = stm.executeQuery(sql);
			while( rSet.next()) {
				row1[TblPatName] = rSet.getString(1);
				row1[TblPatID] = rSet.getString(2);
				row1[TblDate] = date2String(rSet.getDate(3));
				row1[TblStudy] = rSet.getString(4);
				if( serFlg) {
					row1[TblSeries] = rSet.getString(5);
				}
				mod1.addRow(row1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void fillWriteTable() {
		DefaultTableModel mod1;
		mod1 = (DefaultTableModel) jTable2.getModel();
		mod1.setNumRows(0);
		setOrSaveColumnWidths(1, false);
		imgList = new Vector<ImagePlus>();
		ImagePlus img1;
		String meta, patName, patID, study, series, tmp1;
		Date date1;
		int i, j, row0, col0;
		int [] fullList = WindowManager.getIDList();
		if( fullList == null) return;
		for( i=0; i<fullList.length; i++) {
			img1 = WindowManager.getImage(fullList[i]);
			j = img1.getStackSize();
			if( j <= 0) continue;
			meta = getMeta(1, img1);
			if( meta == null) continue;	// no information, skip it
			Object[] row1 = new Object[TblSize+1];
			patName = getDicomValue( meta, "0010,0010");
			if( patName == null) continue;
			row1[TblPatName] = compressDicomName(patName);
			patID = getDicomValue( meta, "0010,0020");
			row1[TblPatID] = patID;
			date1 = getDateTime(getDicomValue( meta, "0008,0020"), null);
			row1[TblDate] = date2String(date1);
			study = getDicomValue( meta, "0008,1030");
			row1[TblStudy] = study;
			series = getDicomValue( meta, "0008,103E");
			if( series == null || series.isEmpty()) series = getDicomValue( meta, "0054,0400");
			row1[TblSeries] = series;
			col0 = parseInt(getDicomValue(meta, "0028,0011"));
			row0 = parseInt(getDicomValue(meta, "0028,0010"));
			tmp1 = col0 + "*" + row0 + "*" + j;
			row1[TblSize] = tmp1;
			mod1.addRow(row1);
			imgList.add(img1);
		}
	}

	void setDates() {
		unselectAllTableEntries();
		Date dt0 = new Date(), dt1 = new Date();
		boolean showPatName = true;
		SimpleDateFormat df1 = new SimpleDateFormat("d MMM yyyy");
		try {
			dateTo.setDate(dt1);
			switch(jComboBox1.getSelectedIndex()) {
				case 0:
					dt0 =df1.parse("1 Jan 1980");
					break;

				case 1:
					showPatName = false;
					break;

				case 2:
				case 3:
					dt0.setTime(dt1.getTime()-1000l*60*60*24*30);
					break;
			}
			dateFrom.setDate(dt0);
			jTextPatName.setVisible(showPatName);
//			jTextPatName.setEnabled(showPatName);
			jTextPatName.setText("");
			validate();
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void unselectAllTableEntries() {
		jTable1.clearSelection();
	}

	void setOrSaveColumnWidths(int type, boolean saveFlg) {
		DefaultTableModel mod1;
		String colStr = "readDb series col";
		int i, x, n = 5;
		String [] colNames;
		colNames = new String[]{"Name", "Study", "Date", "Series", "ID"};
		JTable jTab = jTable1;
		if( type > 0) {
			jTab = jTable2;
			colStr = "writeDb series col";
			colNames = new String[]{"Name", "Study", "Date", "Series", "ID", "Size"};
			n = 6;
		}

		mod1 = (DefaultTableModel) jTab.getModel();
		// if saving the data, there is no reason to change the column names
		if( !saveFlg) {
			if( type == 0 && !tab1Dirty) return;	// if not dirty, go home
			if( type > 0 && !tab2Dirty) return;	// if not dirty, go home
			mod1.setColumnIdentifiers(colNames);
			TableColumn col0;
			col0 = jTab.getColumnModel().getColumn(TblDate);
			col0.setCellRenderer(new ColorDateRenderer());
			if( type == 0) {
				col0 = jTab.getColumnModel().getColumn(TblSeries);
				col0.setCellRenderer(new SeriesRenderer());
				tab1Dirty = false;
			}
			else tab2Dirty = false;
		}

		TableColumn col1;
		for( i=0; i<n; i++) {
			col1 = jTab.getColumnModel().getColumn(i);
			if( saveFlg) {
				x = col1.getPreferredWidth();
				jPrefer.putInt(colStr+i, x);
			} else {
				x = jPrefer.getInt(colStr+i, 0);
				if( x <= 0) continue;
				col1.setPreferredWidth(x);
			}
		}
	}

	String getDate(int indx) {
		Date dt1;
		if( indx == 0) dt1= dateFrom.getDate();
		else dt1 = dateTo.getDate();
		return date2String(dt1);
	}

	String date2String( Date dt1) {
		SimpleDateFormat df1 = new SimpleDateFormat("d MMM yyyy");
		return df1.format(dt1);
	}

	boolean isID( String name1) {
		if( name1 == null || name1.isEmpty()) return false;
		char a1 = name1.charAt(0);
		if( a1 >= '0' && a1 <= '9') return true;
		int i, j, n = name1.length();
		for( i=j=0; i<n; i++) {
			a1 = name1.charAt(i);
			if( a1 >=  '0' && a1 <= '9') j++;
		}
		if( 2*j > n) return true;	// majority of chars are digits
		return false;
	}

	String compressName( String in1) {
		int j;
		String ret1 = in1;
		String ret2 = ret1.replaceAll(", ", ",");
		while( !ret1.equals(ret2)) {
			ret1 = ret2;
			ret2 = ret1.replaceAll(", ", ",");
		}
		return ret1;
	}

	JRadioButton getDBbutton( int indx) {
		JRadioButton ret1 = null;
		switch( indx) {
			case 1:
				ret1 = jDB1;
				break;

			case 2:
				ret1 = jDB2;
				break;

			case 3:
				ret1 = jDB3;
				break;

			case 4:
				ret1 = jDB4;
				break;

			case 5:
				ret1 = jDB5;
				break;

			case 6:
				ret1 = jDB6;
				break;

			case 7:
				ret1 = jDB7;
				break;

			case 8:
				ret1 = jDB8;
				break;

			case 9:
				ret1 = jDB9;
				break;

			case 10:
				ret1 = jDB10;
				break;
		}
		return ret1;
	}

	void changeDBSelected(int indx1) {
		jCurrDB = indx1 - 1;
		String tmp1;
		tmp1 = jPrefer.get("db display name" + jCurrDB, null);
		if (tmp1 == null || tmp1.isEmpty()) tmp1 = jPrefer.get("ODBC" + jCurrDB, null);
		jLabelDbName.setText(tmp1);
		int i = jPrefer.getInt("db type" + jCurrDB, 0);
		isAccessDb = true;
		if( i > 0) isAccessDb = false;
		unMountMount(jCurrDB);
	}

	// Options tab
	void fillBoxes() {
		String mntCmd;
		jTextODBC.setText(jPrefer.get("ODBC" + jOptionDB, null));
		jTextUser.setText(jPrefer.get("db user" + jOptionDB, null));
		jPasswordField1.setText(jPrefer.get("db pass" + jOptionDB, null));
		jTextPath.setText(jPrefer.get("db path" + jOptionDB, null));
		Integer days = jPrefer.getInt("num of days", 30);
		jTextNumDays.setText(days.toString());
		int i = jPrefer.getInt("db type" + jOptionDB, 0);
		jComboDbType.setSelectedIndex(i);
		jTextDisplayName.setText(jPrefer.get("db display name" + jOptionDB, null));
		mntCmd = jPrefer.get("linux mount cmd" + jOptionDB, null);
		if( mntCmd != null && !mntCmd.isEmpty()) mntCmd = "(already entered - do not change)";
		jTextMount.setText(mntCmd);
		jTextUmount.setText(jPrefer.get("linux unmount cmd" + jOptionDB, null));

	}

	void saveBoxes() {
		boolean write1 = true;
		if( jTextODBC.getText().isEmpty() || !isInitialized) return;
		jPrefer.put("ODBC" + jOptionDB, jTextODBC.getText());
		jPrefer.put("db user" + jOptionDB, jTextUser.getText());
		String pass1 = new String( jPasswordField1.getPassword());
		jPrefer.put("db pass" + jOptionDB, pass1);
		jPrefer.put("db path" + jOptionDB, jTextPath.getText());
		Integer days = Integer.parseInt(jTextNumDays.getText());
		jPrefer.putInt("num of days", days);
		int i = jComboDbType.getSelectedIndex();
		jPrefer.putInt("db type" + jOptionDB, i);
		jPrefer.put("db display name" + jOptionDB, jTextDisplayName.getText());
		String mntCmd =  jTextMount.getText();
		if( mntCmd != null &&  mntCmd.startsWith("(already")) write1 = false;
		if( write1) jPrefer.put("linux mount cmd" + jOptionDB,mntCmd);
		jPrefer.put("linux unmount cmd" + jOptionDB, jTextUmount.getText());
	}

	void fillOptions() {
		Integer num = jPrefer.getInt("montage slices", 20);
		jTextN.setText(num.toString());
	}

	void saveOptions() {
		if( jTextN.getText().isEmpty() || !isInitialized) return;
		int i = Integer.parseInt(jTextN.getText());
		jPrefer.putInt("montage slices", i);
	}

	@Override
	public void dispose() {
		killRead = true;
		savePrefs();
		if( writeWasShown) setOrSaveColumnWidths(1, true);
		super.dispose();
		unMountMount(-1);
	}

	void unMountMount(int newDrive) {
		String tmp1;
		Process p;
		if( newDrive == linuxURLdrive) return;	// nothing to do
		if( linuxURLdrive >= 0) {
			try {
				tmp1 = jPrefer.get("linux unmount cmd"+linuxURLdrive, null);
				p = Runtime.getRuntime().exec(tmp1);
				p.waitFor();
				linuxURLdrive = -1;
			} catch (Exception e) {e.printStackTrace(); }
		}
		if( newDrive >= 0) {
			try {
				tmp1 = jPrefer.get("linux mount cmd"+newDrive, null);
				if( tmp1 != null && !tmp1.isEmpty()) {
					p = Runtime.getRuntime().exec(tmp1);
					linuxURLdrive = newDrive;
				}
			} catch (Exception e) {e.printStackTrace(); }
		}
	}

	/**
	 * To have showProgress work, the data loading is done in the background.
	 */
	protected class bkgdLoadData extends SwingWorker {
		@Override
		protected Void doInBackground() {
			doRead();
			return null;
		}
	}


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelRead = new javax.swing.JPanel();
        jCheckSeries = new javax.swing.JCheckBox();
        jComboBox1 = new javax.swing.JComboBox();
        jTextPatName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        dateFrom = new com.michaelbaranov.microba.calendar.DatePicker();
        jLabel2 = new javax.swing.JLabel();
        dateTo = new com.michaelbaranov.microba.calendar.DatePicker();
        jButRead = new javax.swing.JButton();
        jCheckExit = new javax.swing.JCheckBox();
        jButExit = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jDB1 = new javax.swing.JRadioButton();
        jDB2 = new javax.swing.JRadioButton();
        jDB3 = new javax.swing.JRadioButton();
        jDB4 = new javax.swing.JRadioButton();
        jDB5 = new javax.swing.JRadioButton();
        jDB6 = new javax.swing.JRadioButton();
        jDB7 = new javax.swing.JRadioButton();
        jDB8 = new javax.swing.JRadioButton();
        jDB9 = new javax.swing.JRadioButton();
        jDB10 = new javax.swing.JRadioButton();
        jLabelDbName = new javax.swing.JLabel();
        jPanelWrite = new javax.swing.JPanel();
        jButWrite = new javax.swing.JButton();
        jButExit1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanelOptions = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jTextN = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanelSetup = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabelODBCName = new javax.swing.JLabel();
        jTextODBC = new javax.swing.JTextField();
        jLabelWhichDB = new javax.swing.JLabel();
        jComboDB = new javax.swing.JComboBox();
        jLabelUser = new javax.swing.JLabel();
        jTextUser = new javax.swing.JTextField();
        jLabelPW = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabelPath = new javax.swing.JLabel();
        jTextPath = new javax.swing.JTextField();
        jComboDbType = new javax.swing.JComboBox();
        jLabelDays = new javax.swing.JLabel();
        jTextNumDays = new javax.swing.JTextField();
        jLabelDisp = new javax.swing.JLabel();
        jTextDisplayName = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextMount = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextUmount = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jCheckSeries.setText("Series");
        jCheckSeries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckSeriesActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Patient Name/ID", "Date", "Study", "Series", "Teaching" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel1.setText("from");

        jLabel2.setText("to");

        jButRead.setText("Read");
        jButRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButReadActionPerformed(evt);
            }
        });

        jCheckExit.setText("Exit after read");

        jButExit.setText("Exit");
        jButExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButExitActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "Study", "Date", "Series", "ID"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jDB1.setText("1");
        jDB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB1ActionPerformed(evt);
            }
        });

        jDB2.setText("2");
        jDB2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB2ActionPerformed(evt);
            }
        });

        jDB3.setText("3");
        jDB3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB3ActionPerformed(evt);
            }
        });

        jDB4.setText("4");
        jDB4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB4ActionPerformed(evt);
            }
        });

        jDB5.setText("5");
        jDB5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB5ActionPerformed(evt);
            }
        });

        jDB6.setText("6");
        jDB6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB6ActionPerformed(evt);
            }
        });

        jDB7.setText("7");
        jDB7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB7ActionPerformed(evt);
            }
        });

        jDB8.setText("8");
        jDB8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB8ActionPerformed(evt);
            }
        });

        jDB9.setText("9");
        jDB9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB9ActionPerformed(evt);
            }
        });

        jDB10.setText("10");
        jDB10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDB10ActionPerformed(evt);
            }
        });

        jLabelDbName.setText("1");
        jLabelDbName.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        javax.swing.GroupLayout jPanelReadLayout = new javax.swing.GroupLayout(jPanelRead);
        jPanelRead.setLayout(jPanelReadLayout);
        jPanelReadLayout.setHorizontalGroup(
            jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReadLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
                    .addGroup(jPanelReadLayout.createSequentialGroup()
                        .addComponent(jDB1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDB2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDB3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDB4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDB5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDB6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDB7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDB8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jDB9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDB10)
                        .addGap(18, 18, 18)
                        .addComponent(jLabelDbName)
                        .addGap(235, 235, 235))
                    .addGroup(jPanelReadLayout.createSequentialGroup()
                        .addGroup(jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckSeries)
                            .addGroup(jPanelReadLayout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextPatName, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addGroup(jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckExit)
                            .addGroup(jPanelReadLayout.createSequentialGroup()
                                .addComponent(jButRead)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButExit)))
                        .addContainerGap())))
        );
        jPanelReadLayout.setVerticalGroup(
            jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelReadLayout.createSequentialGroup()
                .addGroup(jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelReadLayout.createSequentialGroup()
                        .addGroup(jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckSeries)
                            .addComponent(jButExit)
                            .addComponent(jButRead))
                        .addGap(5, 5, 5)
                        .addGroup(jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextPatName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel1))
                            .addComponent(jCheckExit)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelReadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jDB1)
                    .addComponent(jDB2)
                    .addComponent(jDB3)
                    .addComponent(jDB4)
                    .addComponent(jDB5)
                    .addComponent(jDB6)
                    .addComponent(jDB7)
                    .addComponent(jDB8)
                    .addComponent(jDB9)
                    .addComponent(jDB10)
                    .addComponent(jLabelDbName)))
        );

        jTabbedPane1.addTab("Read Database", jPanelRead);

        jPanelWrite.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanelWriteComponentShown(evt);
            }
        });

        jButWrite.setText("Write");
        jButWrite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButWriteActionPerformed(evt);
            }
        });

        jButExit1.setText("Exit");
        jButExit1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButExit1ActionPerformed(evt);
            }
        });

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Name", "Study", "Date", "Series", "ID", "Size"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, true, false, true, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout jPanelWriteLayout = new javax.swing.GroupLayout(jPanelWrite);
        jPanelWrite.setLayout(jPanelWriteLayout);
        jPanelWriteLayout.setHorizontalGroup(
            jPanelWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelWriteLayout.createSequentialGroup()
                .addContainerGap(487, Short.MAX_VALUE)
                .addComponent(jButWrite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButExit1)
                .addContainerGap())
            .addGroup(jPanelWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelWriteLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)))
        );
        jPanelWriteLayout.setVerticalGroup(
            jPanelWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelWriteLayout.createSequentialGroup()
                .addGroup(jPanelWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButExit1)
                    .addComponent(jButWrite))
                .addContainerGap(230, Short.MAX_VALUE))
            .addGroup(jPanelWriteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanelWriteLayout.createSequentialGroup()
                    .addGap(28, 28, 28)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jTabbedPane1.addTab("Write Database", jPanelWrite);

        jPanelOptions.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jPanelOptionsComponentHidden(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanelOptionsComponentShown(evt);
            }
        });

        jLabel5.setText("Make montage for series up to");

        jLabel6.setText("sllices.");

        javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
        jPanelOptions.setLayout(jPanelOptionsLayout);
        jPanelOptionsLayout.setHorizontalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextN, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addContainerGap(396, Short.MAX_VALUE))
        );
        jPanelOptionsLayout.setVerticalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap(222, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Options", jPanelOptions);

        jPanelSetup.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                jPanelSetupComponentHidden(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanelSetupComponentShown(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Database"));

        jLabelODBCName.setText("ODBC Name");

        jLabelWhichDB.setText("Which db");

        jComboDB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        jComboDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboDBActionPerformed(evt);
            }
        });

        jLabelUser.setText("User");

        jLabelPW.setText("Password");

        jLabelPath.setText("Data path");

        jComboDbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ODBC Access", "ODBC SQL Server", "Java SQL Server", "Java MySQL" }));
        jComboDbType.setMinimumSize(new java.awt.Dimension(110, 20));

        jLabelDays.setText("Num of days");

        jLabelDisp.setText("Display");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelODBCName)
                    .addComponent(jLabelUser)
                    .addComponent(jLabelPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextPath, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextODBC, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabelWhichDB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextUser, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabelPW)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPasswordField1, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jComboDbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelDays)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextNumDays, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelDisp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextDisplayName, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelODBCName)
                    .addComponent(jTextODBC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelWhichDB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelUser)
                    .addComponent(jTextUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPW))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelPath)
                    .addComponent(jTextPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboDbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelDays)
                    .addComponent(jTextNumDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelDisp)
                    .addComponent(jTextDisplayName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Macintosh and Linux users"));

        jLabel3.setText("Mount command");

        jLabel4.setText("Unmount");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextUmount, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE))
                    .addComponent(jTextMount, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextMount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextUmount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanelSetupLayout = new javax.swing.GroupLayout(jPanelSetup);
        jPanelSetup.setLayout(jPanelSetupLayout);
        jPanelSetupLayout.setHorizontalGroup(
            jPanelSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSetupLayout.createSequentialGroup()
                .addGroup(jPanelSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelSetupLayout.setVerticalGroup(
            jPanelSetupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSetupLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Setup", jPanelSetup);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void jButExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButExitActionPerformed
		dispose();
	}//GEN-LAST:event_jButExitActionPerformed

	private void jButReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButReadActionPerformed
		if( work2 != null) return;
		readButton();
	}//GEN-LAST:event_jButReadActionPerformed

	private void jCheckSeriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckSeriesActionPerformed
//		int i = 5;
//		if( jCheckSeries.isSelected()) i = 4;
//		setOrSaveColumnWidths(i, true);
		tab1Dirty = true;
		maybeRereadTable();
	}//GEN-LAST:event_jCheckSeriesActionPerformed

	private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
		setDates();
	}//GEN-LAST:event_jComboBox1ActionPerformed

	private void jDB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB1ActionPerformed
		changeDBSelected(1);
	}//GEN-LAST:event_jDB1ActionPerformed

	private void jDB2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB2ActionPerformed
		changeDBSelected(2);
	}//GEN-LAST:event_jDB2ActionPerformed

	private void jDB3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB3ActionPerformed
		changeDBSelected(3);
	}//GEN-LAST:event_jDB3ActionPerformed

	private void jDB4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB4ActionPerformed
		changeDBSelected(4);
	}//GEN-LAST:event_jDB4ActionPerformed

	private void jDB5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB5ActionPerformed
		changeDBSelected(5);
	}//GEN-LAST:event_jDB5ActionPerformed

	private void jDB6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB6ActionPerformed
		changeDBSelected(6);
	}//GEN-LAST:event_jDB6ActionPerformed

	private void jDB7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB7ActionPerformed
		changeDBSelected(7);
	}//GEN-LAST:event_jDB7ActionPerformed

	private void jDB8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB8ActionPerformed
		changeDBSelected(8);
	}//GEN-LAST:event_jDB8ActionPerformed

	private void jDB9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB9ActionPerformed
		changeDBSelected(9);
	}//GEN-LAST:event_jDB9ActionPerformed

	private void jDB10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDB10ActionPerformed
		changeDBSelected(10);
	}//GEN-LAST:event_jDB10ActionPerformed

	private void jPanelSetupComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelSetupComponentShown
		fillBoxes();
	}//GEN-LAST:event_jPanelSetupComponentShown

	private void jComboDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboDBActionPerformed
		int i = jComboDB.getSelectedIndex();
		if( i == jOptionDB || i < 0 || i > 9) return;
		saveBoxes();
		jOptionDB = i;
		fillBoxes();
	}//GEN-LAST:event_jComboDBActionPerformed

	private void jPanelSetupComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelSetupComponentHidden
		saveBoxes();
		updateDbList();
	}//GEN-LAST:event_jPanelSetupComponentHidden

	private void jPanelOptionsComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelOptionsComponentHidden
		saveOptions();
	}//GEN-LAST:event_jPanelOptionsComponentHidden

	private void jPanelOptionsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelOptionsComponentShown
		fillOptions();
	}//GEN-LAST:event_jPanelOptionsComponentShown

	private void jButWriteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButWriteActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_jButWriteActionPerformed

	private void jButExit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButExit1ActionPerformed
		dispose();
	}//GEN-LAST:event_jButExit1ActionPerformed

	private void jPanelWriteComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanelWriteComponentShown
		writeWasShown = true;
		fillWriteTable();
	}//GEN-LAST:event_jPanelWriteComponentShown


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private com.michaelbaranov.microba.calendar.DatePicker dateFrom;
    private com.michaelbaranov.microba.calendar.DatePicker dateTo;
    private javax.swing.JButton jButExit;
    private javax.swing.JButton jButExit1;
    private javax.swing.JButton jButRead;
    private javax.swing.JButton jButWrite;
    private javax.swing.JCheckBox jCheckExit;
    private javax.swing.JCheckBox jCheckSeries;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboDB;
    private javax.swing.JComboBox jComboDbType;
    private javax.swing.JRadioButton jDB1;
    private javax.swing.JRadioButton jDB10;
    private javax.swing.JRadioButton jDB2;
    private javax.swing.JRadioButton jDB3;
    private javax.swing.JRadioButton jDB4;
    private javax.swing.JRadioButton jDB5;
    private javax.swing.JRadioButton jDB6;
    private javax.swing.JRadioButton jDB7;
    private javax.swing.JRadioButton jDB8;
    private javax.swing.JRadioButton jDB9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelDays;
    private javax.swing.JLabel jLabelDbName;
    private javax.swing.JLabel jLabelDisp;
    private javax.swing.JLabel jLabelODBCName;
    private javax.swing.JLabel jLabelPW;
    private javax.swing.JLabel jLabelPath;
    private javax.swing.JLabel jLabelUser;
    private javax.swing.JLabel jLabelWhichDB;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JPanel jPanelRead;
    private javax.swing.JPanel jPanelSetup;
    private javax.swing.JPanel jPanelWrite;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextDisplayName;
    private javax.swing.JTextField jTextMount;
    private javax.swing.JTextField jTextN;
    private javax.swing.JTextField jTextNumDays;
    private javax.swing.JTextField jTextODBC;
    private javax.swing.JTextField jTextPatName;
    private javax.swing.JTextField jTextPath;
    private javax.swing.JTextField jTextUmount;
    private javax.swing.JTextField jTextUser;
    // End of variables declaration//GEN-END:variables
	String m_dataPath = null;
	Preferences jPrefer = null;
	private boolean tab1Dirty = true, tab2Dirty = true;
	boolean isAccessDb = false, isInitialized = false, killRead = false, writeWasShown = false;
	int linuxURLdrive = -1;	// undefined
	private int numDays = 0, jCurrDB = 0, jOptionDB = 0;
	Vector<ImagePlus> imgList = null;
	FileInfo fi;
	String ODBCUser = null;
	bkgdLoadData work2 = null;
}

