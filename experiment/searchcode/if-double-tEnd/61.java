package tools.SpliceSiteGui;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import tools.blast.LongBlastHit2blastM8alignmentGenerator;
import tools.blast.blastM8Alignment;
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
public class SpliceSiteGui2 extends javax.swing.JFrame implements ActionListener, CaretListener{
	private JTextArea Genome;
	private JLabel Start;
	private JLabel End;
	private JLabel PaddingLabel;
	private JTextField PaddingIn;
	private JLabel Position;
	private JScrollPane jScrollPane5;
	private JButton Initialize;
	private JScrollPane BlastPane;
	private JScrollPane jScrollPane4;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JScrollPane jScrollPane3;
	private JButton Restart;
	private JTextArea ProteinOut;
	private JTextArea BlastFileIn;
	private JButton Next;
	private JLabel OutputLabel;
	private JLabel EditedProtein;
	private JLabel BlastFileLabel;
	private JTextArea Output;
	private JTextArea GenomeIn;
	private JLabel GenomeLabel;
	private JLabel BlastFile;
	private JButton Previous;
	private JLabel Message;
	private JPanel BlastButtonsPanel;
	private HashMap<String, JRadioButton> BlastButtons;
//	private JRadioButton[] BlastButtons;
	private HashMap<String, blastM8Alignment> blastM8Alignments;
//	private blastM8Alignment[] blastM8Alignments;
	private HashMap<String, String> blastHits;
//	private String[] blastHits;
	private ArrayList<String> order;
	private ButtonGroup BlastGroup;
	
	private ArrayList<int[]> exons;
	
	
	private boolean reset,genome,blastfile,plusStrand,inIntron,first;
	private String genomeSeq, activeKey;
	private int padding=300;
	private int[] currentExon;

	/**
	* Auto-generated main method to display this JFrame
	*/
	public static void main(String[] args) {
		SpliceSiteGui2 inst = new SpliceSiteGui2();
		inst.setVisible(true);
	}
	
	public SpliceSiteGui2() {
		super();
		reset=true;
		genome=false;
		blastfile=false;
		inIntron=true;
		first=true;
		initGUI();
	}
	
	private void initGUI() {
		try {
			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1};
			thisLayout.rowHeights = new int[] {42, 19, 29, 33, 243, 31, 149, 7};
			thisLayout.columnWeights = new double[] {0.0, 0.0, 2.0, 2.0};
			thisLayout.columnWidths = new int[] {149, 185, 132, 7};
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
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
				Previous = new JButton();
				getContentPane().add(Previous, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Previous.setText("Previous");
				Previous.addActionListener(this);
			}
			{
				Next = new JButton();
				getContentPane().add(Next, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Next.setText("Next");
				Next.addActionListener(this);
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
				BlastFileLabel.setText("Blast-file input:");
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
				jScrollPane2 = new JScrollPane();
				getContentPane().add(jScrollPane2, new GridBagConstraints(0, 6, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					BlastFileIn = new JTextArea();
					jScrollPane2.setViewportView(BlastFileIn);
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
					Genome = new JTextArea();
					jScrollPane5.setViewportView(Genome);
					Genome.setText("Genome");
					Genome.setFont(new java.awt.Font("Dialog", 0, 12));
					Genome.setPreferredSize(new java.awt.Dimension(5300, 18));
					Genome.setSize(785, 24);
					Genome.addCaretListener(this);
				}
			}
			{
				Position = new JLabel();
				getContentPane().add(Position, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				Position.setText("Position: 0");
			}
			{
				PaddingIn = new JTextField();
				getContentPane().add(PaddingIn, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				PaddingIn.setText("100");
			}
			{
				PaddingLabel = new JLabel();
				getContentPane().add(PaddingLabel, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				PaddingLabel.setText("Padding:");
			}
			pack();
			setSize(800, 600);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(Next)){
			//Next action
			next();
		}else if(e.getSource().equals(Previous)){
			//Previous action
			exons.remove(exons.size()-1);
			activeKey="";
			inIntron=true;
			updateOutput();
		}else if(e.getSource().equals(Restart)){
			GenomeIn.setText("");
			BlastFileIn.setText("");
			Genome.setText("");
			Output.setText("");
			ProteinOut.setText("");
			BlastFileIn.setText("");
			reset=true;
			genome=false;
			blastfile=false;
			inIntron=true;
			first=true;
		}else if(e.getSource().equals(Initialize)){
			//Initalize
			//Read genome
			System.out.println("initalize...");
			Message.setText("Initialize...");
			String[] tmp=GenomeIn.getText().split("\n");
			if(tmp.length>0){
				genomeSeq="";
				int i=tmp[0].charAt(0)=='>'?1:0;
				for(;i<tmp.length;i++){
					genomeSeq+=tmp[i];
				}
			}else{
				System.out.println("No genome");
			}
			//Initialize BlastList
			tmp=("adfadf\n"+BlastFileIn.getText()).split(" Score =");
			System.out.println(tmp.length);
			Message.setText(tmp.length+"");
			BlastButtons= new HashMap<String, JRadioButton>();
			blastM8Alignments= new HashMap<String, blastM8Alignment>();
			blastHits= new HashMap<String, String>();
			order= new ArrayList<String>();
			BlastGroup= new ButtonGroup();
			String key;
			JRadioButton value;
			for(int j=1;j<tmp.length;j++){
				System.out.println(j);
				key=j+"";
				blastHits.put(key, (" Score ="+tmp[j]).replaceAll("\n*$", "\n"));
				blastM8Alignments.put(key, LongBlastHit2blastM8alignmentGenerator.LongBlastHit2blastM8alignment(" Score ="+tmp[j]));
				value= new JRadioButton();
				value.setActionCommand(key);
				BlastGroup.add(value);
				BlastButtons.put(key, value);
				//sort hits... slow insert sort...
				int i;
				for(i=0;i<order.size();i++){
					if(blastM8Alignments.get(key).tstart<blastM8Alignments.get(order.get(i)).tstart){
						order.add(i, key);
						break;
					}
				}
				order.add(i, key);
			}
			//build layout
			BlastButtonsPanel=new JPanel();
			BlastButtonsPanel.setVisible(true);
			BlastButtonsPanel.setLayout(new BoxLayout(BlastButtonsPanel,BoxLayout.Y_AXIS));
			JPanel tmpPanel;
			for (String s : order) {
				tmpPanel= new JPanel();
				tmpPanel.setLayout(new BoxLayout(tmpPanel,BoxLayout.X_AXIS));
				tmpPanel.add(BlastButtons.get(s));
				tmpPanel.add(new JTextArea(blastHits.get(s)));
				BlastButtonsPanel.add(tmpPanel);
			}
			BlastButtons.get(order.get(0)).setSelected(true);
			activeKey="";
			BlastPane.setViewportView(BlastButtonsPanel);
		}
	}
	
	private void next(){
		//what do do when next is pushed
		if(inIntron){
			if(BlastButtons.containsKey(activeKey)&&BlastButtons.get(activeKey).isSelected()){
				Message.setText("First select next exon");
			}else{
				Message.setText("");
				if(!first){
					//retrieve ending of last exon
					currentExon[1]=blastM8Alignments.get(activeKey).tend-Integer.parseInt(PaddingIn.getText())+Genome.getCaretPosition();
					exons.add(currentExon);
				}
				for (JRadioButton jrb : BlastButtons.values()) {
					if(jrb.isSelected()){
						activeKey=jrb.getActionCommand();
						break;
					}
				}
				if(first){
					plusStrand=blastM8Alignments.get(activeKey).strand[0]=='+';
					exons= new ArrayList<int[]>();
					first=false;
				}
				//get Genome
				Start.setText("Start: "+(blastM8Alignments.get(activeKey).tstart-Integer.parseInt(PaddingIn.getText())));
				End.setText("End: "+(blastM8Alignments.get(activeKey).tstart+Integer.parseInt(PaddingIn.getText())));
				Genome.setText(getGenome(blastM8Alignments.get(activeKey).tstart-Integer.parseInt(PaddingIn.getText()), blastM8Alignments.get(activeKey).tstart+Integer.parseInt(PaddingIn.getText())));
				Genome.setCaretPosition(Integer.parseInt(PaddingIn.getText()));
				Genome.grabFocus();
				currentExon=new int[2];
				inIntron=false;
			}
		}else{
			currentExon[0]=blastM8Alignments.get(activeKey).tstart-Integer.parseInt(PaddingIn.getText())+Genome.getCaretPosition();
			Message.setText(Genome.getCaretPosition()+"");
			Start.setText("Start: "+(blastM8Alignments.get(activeKey).tend-Integer.parseInt(PaddingIn.getText())));
			End.setText("End: "+(blastM8Alignments.get(activeKey).tend+Integer.parseInt(PaddingIn.getText())));
			Genome.setText(getGenome(blastM8Alignments.get(activeKey).tend-Integer.parseInt(PaddingIn.getText()), blastM8Alignments.get(activeKey).tend+Integer.parseInt(PaddingIn.getText())));
			Genome.setCaretPosition(Integer.parseInt(PaddingIn.getText()));
			Genome.grabFocus();
			
			inIntron=true;
		}
		updateOutput();
	}
	
	private void updateOutput(){
		//TODO: print text to output
		String output=">exon1\n";
		String wholeRna="";
		String wholeProt="";
		if(exons.size()>0){
			String currentRna=getGenome(exons.get(0)[0], exons.get(0)[1]);
			wholeRna=currentRna;
			output+=currentRna+"\n\n>exon1_trans\n";
			wholeProt=fastaUtils.translateRNA(wholeRna);
			output+=wholeProt+"\n\n";
			int lastEnd=exons.get(0)[1];
			for(int i=1;i<exons.size();i++){
				output+=">intron"+i+"\n"+getGenome(lastEnd, exons.get(i)[0])+"\n\n>exon"+(i+1)+"\n";
				currentRna=getGenome(exons.get(i)[0], exons.get(i)[1]);
				wholeRna+=currentRna;
				output+=currentRna+"\n\n>exon"+(i+1)+"_trans\n"+fastaUtils.translateRNA(wholeRna).replaceAll(wholeProt, "")+"\n\n";
				wholeProt=fastaUtils.translateRNA(wholeRna);
			}
		}
		Output.setText(output);
		ProteinOut.setText(">Prot\n"+wholeProt+"\n");
	}
	

	
	private String getGenome(int p1,int p2){
		System.out.println("Start: "+p1+", End: "+p2);
		if(plusStrand){
			return genomeSeq.substring(p1, p2);
		}else{
//			TODO: what to return when on -strand
			return "";
		}
	}

	public void caretUpdate(CaretEvent e) {
		if(e.getSource().equals(Genome)){
			//get position
			try{
				int pos;
				if(inIntron){
					pos=blastM8Alignments.get(activeKey).tend-Integer.parseInt(PaddingIn.getText())+Genome.getCaretPosition();
				}else{
					pos=blastM8Alignments.get(activeKey).tstart-Integer.parseInt(PaddingIn.getText())+Genome.getCaretPosition();
				}
				Position.setText("Position: "+pos);
			}catch (Exception err) {
				err.printStackTrace();
			}
		}
	}

	public void caretPositionChanged(InputMethodEvent event) {
		//needed?
	}

//	public void inputMethodTextChanged(InputMethodEvent event) {
//		if(reset&&!blastfile&&event.getSource().equals(BlastFileIn)){
//			//Sort and clean the blast-input
//			String tmp=BlastFileIn.getText();
//			blastfile=true;
////			reset=!(genome&&blastfile);
//			if(blastfile&&genome){
//				Message.setText("Message: Mark first exon and press next");
//			}else{
//				Message.setText("Message: Paste genome");
//			}
//		}else if(reset&&!genome&&event.getSource().equals(GenomeIn)){
//			String[] tmp=GenomeIn.getText().split("\n");
//			genomeSeq="";
//			int i=tmp[0].charAt(0)=='>'?1:0;
//			for(;i<tmp.length;i++){
//				genomeSeq+=tmp[i];
//			}
//			genome=true;
////			reset=!(genome&&blastfile);
//			if(blastfile&&genome){
//				Message.setText("Message: Mark first exon and press next");
//			}else{
//				Message.setText("Message: Paste blastfile");
//			}
//		}
//	}

}

