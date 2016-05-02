
package tools.SpliceSiteGui;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import tools.fasta.fastaUtils;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class SpliceSiteGui extends javax.swing.JFrame implements ActionListener, CaretListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel Start;
	private JLabel End;
	private JTextField Manual;
	private JTextArea GotoHistory;
	private JLabel GotoHistoryLabel;
	private JTextField Genome;
	private JLabel Find;
	private JTextField Goto;
	private JLabel GotoLabel;
	private JLabel FrameLavel;
	private JRadioButton Framebutton2;
	private JRadioButton Framebutton1;
	private ButtonGroup Frame;
	private JPanel jPanel2;
	private JPanel jPanel1;
	private JLabel PaddingLabel;
	private JTextField PaddingIn;
	private JLabel Position;
	private JScrollPane jScrollPane5;
	private JButton Initialize;
	private JScrollPane BlastPane;
	private JScrollPane jScrollPane4;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane3;
	private JButton Restart;
	private JTextArea ProteinOut;
	private JButton Use;
	private JLabel OutputLabel;
	private JLabel EditedProtein;
	private JLabel BlastFileLabel;
	private JTextArea Output;
	private JTextArea GenomeIn;
	private JLabel GenomeLabel;
	private JLabel BlastFile;
	private JButton Previous;
	private JLabel Message;
	
	private ArrayList<Integer> exons;
	
	
	private boolean inIntron;
	private String genomeSeq;
	private int curPos,curPad;

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SpliceSiteGui inst = new SpliceSiteGui();
		inst.setVisible(true);
	}
	
	public SpliceSiteGui() {
		super();
		inIntron=true;
		initGUI();
	}
	
	private void initGUI() {
		try {
			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1};
			thisLayout.rowHeights = new int[] {42, 19, 29, 33, 243, 31, 149, 7};
			thisLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 2.0};
			thisLayout.columnWidths = new int[] {149, 425, 115, 7};
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				{
					Frame = new ButtonGroup();
				}
				Start = new JLabel();
				getContentPane().add(Start, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Start.setText("Start:");
			}
			{
				End = new JLabel();
				getContentPane().add(End, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				End.setText("End:");
			}
			{
				Message = new JLabel();
				getContentPane().add(Message, new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Message.setText("Message: Paste genome");
				Message.setPreferredSize(new java.awt.Dimension(529, 14));
			}
			{
				BlastFile = new JLabel();
				getContentPane().add(BlastFile, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				BlastFile.setText("BLAST-file:");
			}
			{
				GenomeLabel = new JLabel();
				getContentPane().add(GenomeLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				GenomeLabel.setText("Genome input:");
			}
			{
				OutputLabel = new JLabel();
				getContentPane().add(OutputLabel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				OutputLabel.setText("Output:");
			}
			{
				BlastFileLabel = new JLabel();
				getContentPane().add(BlastFileLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				BlastFileLabel.setText("Manual editing:");
			}
			{
				EditedProtein = new JLabel();
				getContentPane().add(EditedProtein, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				EditedProtein.setText("Edited protein:");
			}
			{
				Restart = new JButton();
				getContentPane().add(Restart, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Restart.setText("Restart");
				Restart.addActionListener(this);
			}
			{
				jScrollPane1 = new JScrollPane();
				getContentPane().add(jScrollPane1, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					GenomeIn = new JTextArea();
					jScrollPane1.setViewportView(GenomeIn);
				}
			}
			{
				jScrollPane3 = new JScrollPane();
				getContentPane().add(
					jScrollPane3,
					new GridBagConstraints(
						1,
						4,
						1,
						1,
						0.0,
						0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0),
						0,
						0));
				{
					Output = new JTextArea();
					jScrollPane3.setViewportView(Output);
					Output.setLineWrap(true);
				}
			}
			{
				jScrollPane4 = new JScrollPane();
				getContentPane().add(jScrollPane4, new GridBagConstraints(1, 6, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					ProteinOut = new JTextArea();
					jScrollPane4.setViewportView(ProteinOut);
					ProteinOut.setLineWrap(true);
				}
			}
			{
				BlastPane = new JScrollPane();
				getContentPane().add(BlastPane, new GridBagConstraints(2, 4, 2, 3, 2.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					jPanel1 = new JPanel();
					GridBagLayout jPanel1Layout = new GridBagLayout();
					jPanel1Layout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.1};
					jPanel1Layout.rowHeights = new int[] {34, 28, 26, 68, 26, 20};
					jPanel1Layout.columnWeights = new double[] {0.1, 0.1};
					jPanel1Layout.columnWidths = new int[] {7, 7};
					jPanel1.setLayout(jPanel1Layout);
					BlastPane.setViewportView(jPanel1);
					{
						PaddingIn = new JTextField();
						jPanel1.add(PaddingIn, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						PaddingIn.setText("45");
					}
					{
						PaddingLabel = new JLabel();
						jPanel1.add(PaddingLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						PaddingLabel.setText("Padding:");
					}
					{
						jPanel2 = new JPanel();
						BoxLayout jPanel2Layout = new BoxLayout(
							jPanel2,
							javax.swing.BoxLayout.Y_AXIS);
						jPanel1.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanel2.setLayout(jPanel2Layout);
						jPanel2.setPreferredSize(new java.awt.Dimension(45, 68));
						{
							FrameLavel = new JLabel();
							jPanel2.add(FrameLavel);
							FrameLavel.setText("Frame:");
						}
						{
							Framebutton1 = new JRadioButton();
							jPanel2.add(Framebutton1);
							Framebutton1.setText("+");
							Frame.add(Framebutton1);
							Framebutton1.setSelected(true);
							Framebutton1.setActionCommand("+");
						}
						{
							Framebutton2 = new JRadioButton();
							jPanel2.add(Framebutton2);
							Framebutton2.setText("-");
							Frame.add(Framebutton2);
							Framebutton2.setActionCommand("-");
						}
					}
					{
						GotoLabel = new JLabel();
						jPanel1.add(GotoLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						GotoLabel.setText("Goto position:");
					}
					{
						Goto = new JTextField();
						jPanel1.add(Goto, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						Goto.setText("0");
						Goto.addActionListener(this);
					}
					{
						Use = new JButton();
						jPanel1.add(Use, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						Use.setText("Use");
						Use.addActionListener(this);
					}
					{
						Previous = new JButton();
						jPanel1.add(Previous, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						Previous.setText("Previous");
						Previous.addActionListener(this);
					}
					{
						GotoHistoryLabel = new JLabel();
						jPanel1.add(GotoHistoryLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						GotoHistoryLabel.setText("Goto History:");
					}
					{
						GotoHistory = new JTextArea();
						jPanel1.add(GotoHistory, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					}
				}
			}
			{
				Initialize = new JButton();
				getContentPane().add(Initialize, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Initialize.setText("Initialize");
				Initialize.addActionListener(this);
			}
			{
				jScrollPane5 = new JScrollPane();
				jScrollPane5.getHorizontalScrollBar().setVisible(false);
				getContentPane().add(jScrollPane5, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jScrollPane5.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				{
					Genome = new JTextField();
					jScrollPane5.setViewportView(Genome);
					Genome.setText("Genome");
					Genome.addCaretListener(this);
					Genome.addActionListener(this);
				}
			}
			{
				Position = new JLabel();
				getContentPane().add(Position, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Position.setText("Position: 0");
			}
			{
				Find = new JLabel();
				getContentPane().add(Find, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Find.setText("Find start: ATG");
			}
			{
				Manual = new JTextField();
				getContentPane().add(Manual, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				Manual.addActionListener(this);
			}
			pack();
			this.setSize(800, 600);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(Previous)){
			//delete the last insertion
			inIntron=exons.size()%2==0;
			exons.remove(exons.size()-1);
			updateOutput();
		}else if(e.getSource().equals(Use)||e.getSource().equals(Genome)){
			//save the current position
			try{
				exons.add(getGenomePosition());
				inIntron=exons.size()%2==0;
			}catch (Exception err) {
				// TODO: handle exception
			}
			Goto.selectAll();
			Goto.grabFocus();
			updateOutput();
		}else if(e.getSource().equals(Restart)){
			GenomeIn.setText("");
			Genome.setText("");
			Output.setText("");
			ProteinOut.setText("");
			Manual.setText("");
			Position.setText("Position:");
			Find.setText("Find start: ATG");
			Start.setText("Start:");
			End.setText("End:");
			inIntron=true;
		}else if(e.getSource().equals(Initialize)){
			String[] tmp=GenomeIn.getText().split("\n");
			if(tmp.length>0){
				genomeSeq="";
				int i=tmp[0].charAt(0)=='>'?1:0;
				for(;i<tmp.length;i++){
					genomeSeq+=tmp[i];
				}
			}else{
				Message.setText("No genome");
			}
			exons=new ArrayList<Integer>();
		}else if(e.getSource().equals(Goto)){
			//Show new genome piece
			if(exons.size()>0){
				if(!inIntron){
					Find.setText("Find exon end: GT");
				}else{
					Find.setText("Find exon start: AG");
				}
			}
			String hist=Goto.getText();
			String[] oldHist=GotoHistory.getText().split("\n");
			for(int i=0;i<13&&i<oldHist.length;i++){
				hist+="\n"+oldHist[i];
			}
			GotoHistory.setText(hist);
			curPos=Integer.parseInt(Goto.getText());
			curPad=Integer.parseInt(PaddingIn.getText());
			Start.setText("Start: "+(curPos-curPad));
			End.setText("End: "+(curPos+curPad));
			Genome.setText(getGenome(curPos-curPad, curPos+curPad));
			Genome.setCaretPosition(curPad);
			Genome.grabFocus();
		}else if(e.getSource().equals(Manual)){
			exons=new ArrayList<Integer>();
			for (String s : Manual.getText().split("[E|I]")) {
				System.out.println(s);
				exons.add(new Integer(s.trim()));
			}
			updateOutput();
		}
		
		
		

	}
	
	public boolean plusStrand(){
		if(Framebutton1.isSelected()){
			return true;
		}else if(Framebutton2.isSelected()){
			return false;
		}else{
			Framebutton1.setSelected(true);
			return true;
		}
	}
	
	public int getGenomePosition(){
		if(plusStrand()){
			return curPos-curPad+Genome.getCaretPosition();
		}else{
			return curPos+curPad-Genome.getCaretPosition();
		}
	}

	
	private void updateOutput(){
		//print text to output
		String output="";
		String wholeRna="";
		String wholeProt="";
		String manual="";
		String currentRna;
		if(exons.size()>0){
			manual=exons.get(0).toString();
			for(int i=1;i<exons.size();i++){
				manual+=(i%2==1?" E ":" I ")+exons.get(i);
			}
			Manual.setText(manual);
		}
		if(exons.size()>1){
			int i;
			for(i=1;i<exons.size();i++){
				currentRna=getGenome(exons.get(i-1), exons.get(i));
				if(i%2==1){
					//print exon
					wholeRna+=currentRna;
					output+=">exon"+(i/2+1)+"\n"+currentRna+"\n\n";
					output+=">exon"+(i/2+1)+"_trans\n"+fastaUtils.translateRNA(wholeRna).replaceAll(wholeProt, "")+"\n\n";
					wholeProt=fastaUtils.translateRNA(wholeRna);
				}else{
					//print intron
					output+=">intron"+(i/2)+"\n"+currentRna+"\n\n";
				}
			}
		}

		Output.setText(output);
		ProteinOut.setText(">Prot\n"+wholeProt+"\n");
	}
	

	
	private String getGenome(int p1,int p2){
		System.out.println("Start: "+p1+", End: "+p2);
		if(plusStrand()){
			return genomeSeq.substring(p1, p2);
		}else{
//			returns the reverse complement
			String plus;
			if(p1<p2){
				plus=genomeSeq.substring(p1, p2);
			}else{
				plus=genomeSeq.substring(p2, p1);
			}
			String minus="";
			for (char c : plus.toUpperCase().toCharArray()) {
				switch (c) {
				case 'A':
					minus="T"+minus;
					break;
				case 'T':
					minus="A"+minus;
					break;
				case 'G':
					minus="C"+minus;
					break;
				case 'C':
					minus="G"+minus;
					break;
				default:
					System.out.println(""+c);
					minus="N"+minus;
					break;
				}
			}
			return minus;
		}
	}

	public void caretUpdate(CaretEvent e) {
		if(e.getSource().equals(Genome)){
			//get position
			try{
				Position.setText("Position: "+getGenomePosition());
			}catch (Exception err) {
				err.printStackTrace();
			}
		}
	}

	public void caretPositionChanged(InputMethodEvent event) {
		//needed?
	}



}

