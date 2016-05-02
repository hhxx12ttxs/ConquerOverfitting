package wa.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.*;
import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Caret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import wa.common.ConjugConfig;
import wa.common.Context;
import wa.common.DicWord;
import wa.common.OrthographyInfo;
import wa.common.RType;
import wa.common.RecomConfig;
import wa.common.RecomInfo;
import wa.common.SimilarityInfo;
import wa.common.WebDicWord;
import wa.control.WritingAssistantCtrl;
import wa.dic.DaumOpenAPIDic;
import wa.dic.DictionaryService;
import wa.util.ClipBoard;
import wa.util.FileAccess;

public class WritingAssistantUI extends javax.swing.JFrame implements WaUInterface {

    private ListViewer lv;
    private WritingAssistantCtrl control;
    // 조사추천,용언추천 리스트가 하나만 뜨게 함
    private ConfigViewer cv;
    private boolean show = false;
    private Map<String, List<SimilarityInfo>> orMap = new HashMap<String, List<SimilarityInfo>>();
    private final Logger logger = Logger.getLogger("wa");
    private SentenceRecommenderUI srUI;
    private DictionaryUI dicUI;

    /** Creates new form WritingAssistantUI */
    public WritingAssistantUI(WritingAssistantCtrl control) {
        this.control = control;
        lv = new ListViewer(control, this);
        cv = new ConfigViewer(control, this, true);
        cv.setConfiguration(control.getRConfig());
        initComponents();
        textArea.addMouseListener(new PopupListener(jPopupMenu1));
        textArea.setFocusable(true);

        tableAlignment();

        centerOnScreen(this);
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
        this.srUI = new SentenceRecommenderUI(this);
        this.dicUI = new DictionaryUI(this);
    }
    private void tableAlignment(){
        TableColumnModel tcm = jTable3.getColumnModel();
        //header

        /*
        DefaultTableCellHeaderRenderer hr = new DefaultTableCellHeaderRenderer();
        hr.setHorizontalAlignment(SwingConstants.CENTER);
        tcm.getColumn(0).setHeaderRenderer(hr);
        tcm.getColumn(1).setHeaderRenderer(hr);
        tcm.getColumn(2).setHeaderRenderer(hr);
        */
        //column
        

        DefaultTableCellRenderer crRight = new DefaultTableCellRenderer();
        crRight.setHorizontalAlignment(SwingConstants.RIGHT);
        tcm.getColumn(0).setCellRenderer(crRight);

        DefaultTableCellRenderer crCenter = new DefaultTableCellRenderer();
        crCenter.setHorizontalAlignment(SwingConstants.CENTER);
        tcm.getColumn(1).setCellRenderer(crCenter);

        DefaultTableCellRenderer crLeft = new DefaultTableCellRenderer();
        crLeft.setHorizontalAlignment(SwingConstants.LEFT);
        tcm.getColumn(2).setCellRenderer(crLeft);
    }
    public ConjugConfig getConjugationConfig(){
        int mood = 0;
        int tense = 0;

        if (radioPresentBtn.isSelected()) {
            tense = 0;
        } else if (radioPastBtn.isSelected()) {
            tense = 1;
        } else if (radioFutureBtn.isSelected()) {
            tense = 2;
        } else {
            tense = 3;
        }

        if (radioPyungseoBtn.isSelected()) {
            mood = 0;
        } else if (radioQuestionBtn.isSelected()) {
            mood = 1;
        } else {
            mood = 2;
        }
        ConjugConfig cc = new ConjugConfig();
        cc.setTense(tense);
        cc.setMood(mood);
        return cc;
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        newICON = new javax.swing.JButton();
        openICON = new javax.swing.JButton();
        saveICON = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        copyICON = new javax.swing.JButton();
        cutICON = new javax.swing.JButton();
        pasteICON = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        recomICON = new javax.swing.JButton();
        findICON1 = new javax.swing.JButton();
        findICON2 = new javax.swing.JButton();
        configICON = new javax.swing.JButton();
        findICON = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        ScrollPanel_textArea = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        rTypeLabel = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        stopBtn = new javax.swing.JButton();
        settingPanel = new javax.swing.JPanel();
        radioPresentBtn = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        radioPastBtn = new javax.swing.JRadioButton();
        radioFutureBtn = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        radioPyungseoBtn = new javax.swing.JRadioButton();
        radioQuestionBtn = new javax.swing.JRadioButton();
        webRadioBtn = new javax.swing.JRadioButton();
        corpusRadioBtn = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        radioImperativeBtn = new javax.swing.JRadioButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable(){
            public Component prepareRenderer(TableCellRenderer renderer, int index_row, int index_col){
                Component comp = super.prepareRenderer(renderer, index_row, index_col);
                //odd col index, selected or not selected
                if(index_col == 1){
                    comp.setBackground(Color.yellow);
                    comp.setForeground(getForeground());
                    //comp.setForeground(getSelectionForeground());
                }
                else{
                    comp.setBackground(getBackground());
                    comp.setForeground(getForeground());
                    //comp.setForeground(getSelectionForeground());
                }
                return comp;
            }

        }
        ;
        MenuBar5 = new javax.swing.JMenuBar();
        Menu_File5 = new javax.swing.JMenu();
        newDOC_MenuItem3 = new javax.swing.JMenuItem();
        loadDOC_MenuItem3 = new javax.swing.JMenuItem();
        SaveDoc_MenuItem3 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        Exit_MenuItem3 = new javax.swing.JMenuItem();
        Menu_Edit5 = new javax.swing.JMenu();
        Copy_MenuItem3 = new javax.swing.JMenuItem();
        Cut_MenuItem3 = new javax.swing.JMenuItem();
        Paste_MenuItem3 = new javax.swing.JMenuItem();
        Menu_Assistants5 = new javax.swing.JMenu();
        MenuItem_DWR5 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu24 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

        jMenuItem3.setText("조사&용언 추천");
        jMenuItem3.setActionCommand("어휘 추천");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem3);

        jMenuItem4.setText("문장 추천");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem4);

        jMenuItem5.setText("맞춤법 검사");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem5);

        jMenuItem6.setText("사전 검색");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem6);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Writing Assistant For Foreigners");
        setFocusable(false);
        setIconImage(new javax.swing.ImageIcon(getClass().getResource("/logo.png")).getImage());
        setResizable(false);

        jToolBar1.setBorder(jToolBar1.getBorder());
        jToolBar1.setFloatable(false);
        jToolBar1.setFocusable(false);

        newICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/new.PNG"))); // NOI18N
        newICON.setText("New");
        newICON.setToolTipText("New");
        newICON.setFocusable(false);
        newICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        newICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        newICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newICONActionPerformed(evt);
            }
        });
        jToolBar1.add(newICON);

        openICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/open.png"))); // NOI18N
        openICON.setText("Open");
        openICON.setToolTipText("Open");
        openICON.setFocusable(false);
        openICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        openICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        openICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openICONActionPerformed(evt);
            }
        });
        jToolBar1.add(openICON);

        saveICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/save.PNG"))); // NOI18N
        saveICON.setText("Save");
        saveICON.setToolTipText("Save");
        saveICON.setFocusable(false);
        saveICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveICONActionPerformed(evt);
            }
        });
        jToolBar1.add(saveICON);
        jToolBar1.add(jSeparator2);

        copyICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/copy.PNG"))); // NOI18N
        copyICON.setText("Copy");
        copyICON.setToolTipText("Copy");
        copyICON.setFocusable(false);
        copyICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        copyICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        copyICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyICONActionPerformed(evt);
            }
        });
        jToolBar1.add(copyICON);

        cutICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cut.png"))); // NOI18N
        cutICON.setText("Cut");
        cutICON.setToolTipText("Cut");
        cutICON.setFocusable(false);
        cutICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cutICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cutICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutICONActionPerformed(evt);
            }
        });
        jToolBar1.add(cutICON);

        pasteICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paste.PNG"))); // NOI18N
        pasteICON.setText("Paste");
        pasteICON.setToolTipText("Paste");
        pasteICON.setFocusable(false);
        pasteICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pasteICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pasteICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteICONActionPerformed(evt);
            }
        });
        jToolBar1.add(pasteICON);
        jToolBar1.add(jSeparator5);

        recomICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recom.PNG"))); // NOI18N
        recomICON.setText("어휘 추천");
        recomICON.setToolTipText("어휘 추천");
        recomICON.setFocusable(false);
        recomICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        recomICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        recomICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recomICONActionPerformed(evt);
            }
        });
        jToolBar1.add(recomICON);

        findICON1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wa/ui/article-32.png"))); // NOI18N
        findICON1.setText("문장 추천");
        findICON1.setToolTipText("문장 추천");
        findICON1.setFocusable(false);
        findICON1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        findICON1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        findICON1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findICON1ActionPerformed(evt);
            }
        });
        jToolBar1.add(findICON1);
        findICON1.getAccessibleContext().setAccessibleDescription("Sentence Recommend");

        findICON2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wa/ui/dictionary.png"))); // NOI18N
        findICON2.setText("사전 검색");
        findICON2.setToolTipText("사전 검색");
        findICON2.setFocusable(false);
        findICON2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        findICON2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        findICON2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findICON2ActionPerformed(evt);
            }
        });
        jToolBar1.add(findICON2);

        configICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/config.PNG"))); // NOI18N
        configICON.setText("환경설정");
        configICON.setToolTipText("환경설정");
        configICON.setFocusable(false);
        configICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        configICON.setPreferredSize(new java.awt.Dimension(29, 27));
        configICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        configICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configICONActionPerformed(evt);
            }
        });
        jToolBar1.add(configICON);

        findICON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/find.PNG"))); // NOI18N
        findICON.setText("로그");
        findICON.setToolTipText("로그");
        findICON.setFocusable(false);
        findICON.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        findICON.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        findICON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findICONActionPerformed(evt);
            }
        });
        jToolBar1.add(findICON);

        ScrollPanel_textArea.setFocusable(false);

        textArea.setColumns(20);
        textArea.setRows(5);
        textArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        textArea.setCaretColor(new java.awt.Color(51, 51, 51));
        textArea.setFocusCycleRoot(true);
        textArea.setSelectedTextColor(new java.awt.Color(204, 0, 0));
        textArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textAreaKeyPressed(evt);
            }
        });
        ScrollPanel_textArea.setViewportView(textArea);

        rTypeLabel.setText("  ");

        stopBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wa/ui/stop_on.png"))); // NOI18N
        stopBtn.setContentAreaFilled(false);
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(rTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addComponent(rTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE))))
                .addContainerGap())
        );

        buttonGroup1.add(radioPresentBtn);
        radioPresentBtn.setSelected(true);
        radioPresentBtn.setText("현재(Present)");
        radioPresentBtn.setFocusable(false);
        radioPresentBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPresentBtnActionPerformed(evt);
            }
        });

        jLabel1.setText("[검색 유형]");
        jLabel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel1.setFocusable(false);

        buttonGroup1.add(radioPastBtn);
        radioPastBtn.setText("과거(Past)");
        radioPastBtn.setFocusable(false);
        radioPastBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPastBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioFutureBtn);
        radioFutureBtn.setText("미래(Future)");
        radioFutureBtn.setFocusable(false);
        radioFutureBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioFutureBtnActionPerformed(evt);
            }
        });

        jLabel2.setText("[서법]");
        jLabel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel2.setFocusable(false);

        buttonGroup2.add(radioPyungseoBtn);
        radioPyungseoBtn.setSelected(true);
        radioPyungseoBtn.setText("평서형(Normal)");
        radioPyungseoBtn.setFocusable(false);

        buttonGroup2.add(radioQuestionBtn);
        radioQuestionBtn.setText("의문형(Question)");
        radioQuestionBtn.setFocusable(false);

        buttonGroup3.add(webRadioBtn);
        webRadioBtn.setText("웹 검색");
        webRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webRadioBtnActionPerformed(evt);
            }
        });

        buttonGroup3.add(corpusRadioBtn);
        corpusRadioBtn.setSelected(true);
        corpusRadioBtn.setText("말뭉치 검색");
        corpusRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                corpusRadioBtnActionPerformed(evt);
            }
        });

        jLabel7.setText("[시제]");
        jLabel7.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel7.setFocusable(false);

        buttonGroup2.add(radioImperativeBtn);
        radioImperativeBtn.setText("명령형(Imperative)");

        jCheckBox1.setText("Cache 사용(말뭉치 검색일 경우만 해당)");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel4.setForeground(new java.awt.Color(255, 51, 0));
        jLabel4.setText("- 사용시 문맥 정보를 표시하지 않습니다");

        jLabel8.setText("[맞춤법검사]");
        jLabel8.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel8.setFocusable(false);

        jButton1.setText("맞춤법 검사");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "틀린 단어"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setColumnSelectionAllowed(true);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.setShowVerticalLines(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTable1KeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.getColumnModel().getColumn(0).setResizable(false);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "추천하는 단어"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable2.getTableHeader().setReorderingAllowed(false);
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jTable2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable2KeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(jTable2);
        jTable2.getColumnModel().getColumn(0).setResizable(false);

        javax.swing.GroupLayout settingPanelLayout = new javax.swing.GroupLayout(settingPanel);
        settingPanel.setLayout(settingPanelLayout);
        settingPanelLayout.setHorizontalGroup(
            settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(corpusRadioBtn)
                .addGap(18, 18, 18)
                .addComponent(webRadioBtn))
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1))
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4))
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioPastBtn))
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioPresentBtn))
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioFutureBtn))
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioPyungseoBtn))
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioQuestionBtn))
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioImperativeBtn))
            .addComponent(jButton1)
            .addComponent(jScrollPane2, 0, 0, Short.MAX_VALUE)
            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
            .addComponent(jScrollPane3, 0, 0, Short.MAX_VALUE)
        );
        settingPanelLayout.setVerticalGroup(
            settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingPanelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(corpusRadioBtn)
                    .addComponent(webRadioBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioPastBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioPresentBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioFutureBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioPyungseoBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioQuestionBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioImperativeBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(settingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(settingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "이전 문맥", "중심어", "이후 문맥"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable3.setRowSelectionAllowed(false);
        jTable3.getTableHeader().setReorderingAllowed(false);
        jScrollPane4.setViewportView(jTable3);
        jTable3.getColumnModel().getColumn(1).setMinWidth(80);
        jTable3.getColumnModel().getColumn(1).setMaxWidth(150);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ScrollPanel_textArea)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(ScrollPanel_textArea, javax.swing.GroupLayout.PREFERRED_SIZE, 453, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, 0, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );

        MenuBar5.setFocusable(false);

        Menu_File5.setText("File");
        Menu_File5.setContentAreaFilled(false);
        Menu_File5.setFocusable(false);

        newDOC_MenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newDOC_MenuItem3.setFont(new java.awt.Font("굴림", 1, 12));
        newDOC_MenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/new.PNG"))); // NOI18N
        newDOC_MenuItem3.setText("New Document");
        newDOC_MenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newDOC_MenuItemActionPerformed(evt);
            }
        });
        Menu_File5.add(newDOC_MenuItem3);

        loadDOC_MenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        loadDOC_MenuItem3.setFont(new java.awt.Font("굴림", 1, 12));
        loadDOC_MenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/open.png"))); // NOI18N
        loadDOC_MenuItem3.setText("Load Document");
        loadDOC_MenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDOC_MenuItemActionPerformed(evt);
            }
        });
        Menu_File5.add(loadDOC_MenuItem3);

        SaveDoc_MenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        SaveDoc_MenuItem3.setFont(new java.awt.Font("굴림", 1, 12));
        SaveDoc_MenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/save.PNG"))); // NOI18N
        SaveDoc_MenuItem3.setText("Save Document");
        SaveDoc_MenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveDoc_MenuItemActionPerformed(evt);
            }
        });
        Menu_File5.add(SaveDoc_MenuItem3);
        Menu_File5.add(jSeparator6);

        Exit_MenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        Exit_MenuItem3.setFont(new java.awt.Font("굴림", 1, 12));
        Exit_MenuItem3.setText("Exit");
        Exit_MenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Exit_MenuItemActionPerformed(evt);
            }
        });
        Menu_File5.add(Exit_MenuItem3);

        MenuBar5.add(Menu_File5);

        Menu_Edit5.setText("Edit");
        Menu_Edit5.setFocusable(false);

        Copy_MenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        Copy_MenuItem3.setFont(new java.awt.Font("굴림", 1, 12));
        Copy_MenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/copy.PNG"))); // NOI18N
        Copy_MenuItem3.setText("Copy");
        Copy_MenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Copy_MenuItemActionPerformed(evt);
            }
        });
        Menu_Edit5.add(Copy_MenuItem3);

        Cut_MenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        Cut_MenuItem3.setFont(new java.awt.Font("굴림", 1, 12));
        Cut_MenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cut.png"))); // NOI18N
        Cut_MenuItem3.setText("Cut");
        Cut_MenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Cut_MenuItemActionPerformed(evt);
            }
        });
        Menu_Edit5.add(Cut_MenuItem3);

        Paste_MenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        Paste_MenuItem3.setFont(new java.awt.Font("굴림", 1, 12));
        Paste_MenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/paste.PNG"))); // NOI18N
        Paste_MenuItem3.setText("Paste");
        Paste_MenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Paste_MenuItemActionPerformed(evt);
            }
        });
        Menu_Edit5.add(Paste_MenuItem3);

        MenuBar5.add(Menu_Edit5);

        Menu_Assistants5.setText("Assistants");
        Menu_Assistants5.setFocusable(false);

        MenuItem_DWR5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        MenuItem_DWR5.setFont(new java.awt.Font("굴림", 0, 12));
        MenuItem_DWR5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recom.PNG"))); // NOI18N
        MenuItem_DWR5.setText("어휘 추천");
        MenuItem_DWR5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuItem_DWRActionPerformed(evt);
            }
        });
        Menu_Assistants5.add(MenuItem_DWR5);

        jMenuItem1.setFont(new java.awt.Font("굴림", 0, 12));
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/wa/ui/article-32.png"))); // NOI18N
        jMenuItem1.setText("문장 추천");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        Menu_Assistants5.add(jMenuItem1);

        MenuBar5.add(Menu_Assistants5);

        jMenu24.setText("Help");

        jMenuItem2.setText("about");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu24.add(jMenuItem2);

        MenuBar5.add(jMenu24);

        setJMenuBar(MenuBar5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 939, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void newDOC_MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newDOC_MenuItemActionPerformed
    textArea.setText("");
}//GEN-LAST:event_newDOC_MenuItemActionPerformed

private void loadDOC_MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadDOC_MenuItemActionPerformed
    FileAccess fa = new FileAccess();
    FileDialog fileOpenDlg = new FileDialog(this, "파일열기", FileDialog.LOAD);
    
    fileOpenDlg.setVisible(true);

    String fileName = fileOpenDlg.getDirectory() + fileOpenDlg.getFile();
    //if(fileName == null) 
    // 선택된 파일의 내용을 TextArea에 보여준다. 
    if (fileOpenDlg.getFile() == null) {
        return;
    }
    String content = fa.fileOpen(fileName);

    logger.info("파일 열기: " + fileName);
    textArea.setText(content);
}//GEN-LAST:event_loadDOC_MenuItemActionPerformed

private void SaveDoc_MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveDoc_MenuItemActionPerformed
    FileAccess fa = new FileAccess();
    FileDialog fileSaveDlg = new FileDialog(this, "파일저장", FileDialog.SAVE);

    fileSaveDlg.setVisible(true);
    String fileName = fileSaveDlg.getDirectory() + fileSaveDlg.getFile();

    logger.info("파일 저장: " + fileName);
    fa.save(fileName, textArea.getText());
}//GEN-LAST:event_SaveDoc_MenuItemActionPerformed

private void Exit_MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Exit_MenuItemActionPerformed
    this.setVisible(false);
    this.dispose();
    System.exit(0);
}//GEN-LAST:event_Exit_MenuItemActionPerformed

private void newICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newICONActionPerformed
    newDOC_MenuItemActionPerformed(evt);
}//GEN-LAST:event_newICONActionPerformed

private void openICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openICONActionPerformed
    loadDOC_MenuItemActionPerformed(evt);
}//GEN-LAST:event_openICONActionPerformed

private void saveICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveICONActionPerformed
    SaveDoc_MenuItemActionPerformed(evt);
}//GEN-LAST:event_saveICONActionPerformed

private void Cut_MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Cut_MenuItemActionPerformed
    ClipBoard cb = new ClipBoard();
    cb.setData(textArea.getSelectedText());
    textArea.replaceRange("", textArea.getSelectionStart(), textArea.getSelectionEnd());

}//GEN-LAST:event_Cut_MenuItemActionPerformed

private void cutICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutICONActionPerformed
    Cut_MenuItemActionPerformed(evt);
}//GEN-LAST:event_cutICONActionPerformed

private void pasteICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteICONActionPerformed
    Paste_MenuItemActionPerformed(evt);
}//GEN-LAST:event_pasteICONActionPerformed

private void copyICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyICONActionPerformed
    Copy_MenuItemActionPerformed(evt);
}//GEN-LAST:event_copyICONActionPerformed

private void Paste_MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Paste_MenuItemActionPerformed
    ClipBoard cb = new ClipBoard();
    String str = cb.getData();
    textArea.replaceRange(str, textArea.getSelectionStart(), textArea.getSelectionEnd());
}//GEN-LAST:event_Paste_MenuItemActionPerformed

private void Copy_MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Copy_MenuItemActionPerformed
    ClipBoard cb = new ClipBoard();
    cb.setData(textArea.getSelectedText());
}//GEN-LAST:event_Copy_MenuItemActionPerformed

private void recomICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recomICONActionPerformed
    MenuItem_DWRActionPerformed(evt);
}//GEN-LAST:event_recomICONActionPerformed

private void configICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configICONActionPerformed
    cv.setVisible(true);
}//GEN-LAST:event_configICONActionPerformed

private void radioPresentBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPresentBtnActionPerformed
// TODO add your handling code here:
    if (radioPresentBtn.isSelected()) {
            radioPyungseoBtn.setEnabled(true);
            radioQuestionBtn.setEnabled(true);
            radioImperativeBtn.setEnabled(true);
            radioPyungseoBtn.setSelected(true);
    }
}//GEN-LAST:event_radioPresentBtnActionPerformed

private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
     showOrthoRecomWord();
}//GEN-LAST:event_jTable1MouseClicked

private void jTable2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable2KeyPressed
    if(evt.getKeyCode() == KeyEvent.VK_ENTER){
        replaceWrongWord();
    }
}//GEN-LAST:event_jTable2KeyPressed

private void jTable1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyReleased
    if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_DOWN) {
        showOrthoRecomWord();
    }
}//GEN-LAST:event_jTable1KeyReleased

private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
    if (evt.getClickCount() == 2) {

        DefaultTableModel t1Model = (DefaultTableModel) jTable1.getModel();
        DefaultTableModel t2Model = (DefaultTableModel) jTable2.getModel();

        int selected1 = jTable1.getSelectedRow();
        int selected2 = jTable2.getSelectedRow();


        OrthographyInfo orInfo = (OrthographyInfo)t1Model.getValueAt(selected1, 0);
        String wrongWord = orInfo.getWrongword();
        String selectedWord = (String) t2Model.getValueAt(selected2, 0);
        
        String msg = "\"" + wrongWord + "\"을(를)  " +"\"" + selectedWord + "\"로 바꾸시겠습니까? ";
        int change = JOptionPane.showConfirmDialog(this, msg, "맞춤법 검사 적용", JOptionPane.OK_CANCEL_OPTION);

        if(change == 0) {
            logger.info("맞춤법 검사 적용 \"" + wrongWord + "\"을(를)  " +"\"" + selectedWord + "\"로 바꿈");
            replaceWrongWord();
        }
    }
}//GEN-LAST:event_jTable2MouseClicked
private void tableClear(){
    DefaultTableModel model1 = (DefaultTableModel) jTable1.getModel();
    DefaultTableModel model2 = (DefaultTableModel) jTable2.getModel();
    while (true) {
        int rowCount = model1.getRowCount();
        if (rowCount == 0) {
            break;
        }
        model1.removeRow(0);
    }
    while (true) {
        int rowCount = model2.getRowCount();
        if (rowCount == 0) {
            break;
        }
        model2.removeRow(0);
    }
}
public JProgressBar getProgressBar(){
    return jProgressBar1;
}
private void findICONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findICONActionPerformed
    // TODO add your handling code here:
    control.showLogView(true);
}//GEN-LAST:event_findICONActionPerformed

private void findICON1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findICON1ActionPerformed
    // TODO add your handling code here:
    srUI.setVisible(true);
}//GEN-LAST:event_findICON1ActionPerformed

private void radioPastBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPastBtnActionPerformed
    // TODO add your handling code here:
    if (radioPastBtn.isSelected()) {
            radioPyungseoBtn.setEnabled(true);
            radioQuestionBtn.setEnabled(true);
            radioImperativeBtn.setEnabled(false);
            radioPyungseoBtn.setSelected(true);
    }
}//GEN-LAST:event_radioPastBtnActionPerformed

private void radioFutureBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioFutureBtnActionPerformed
    // TODO add your handling code here:
    if (radioFutureBtn.isSelected()) {
            radioPyungseoBtn.setEnabled(true);
            radioQuestionBtn.setEnabled(true);
            radioImperativeBtn.setEnabled(false);
            radioPyungseoBtn.setSelected(true);
        }
}//GEN-LAST:event_radioFutureBtnActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    // TODO add your handling code here:
    tableClear();
    control.spellingCheck(textArea.getText());
}//GEN-LAST:event_jButton1ActionPerformed

private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
    // TODO add your handling code here:
    AboutUI about = new AboutUI();
    about.setVisible(true);
}//GEN-LAST:event_jMenuItem2ActionPerformed

private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
    // TODO add your handling code here:
    MenuItem_DWRActionPerformed(evt);
}//GEN-LAST:event_jMenuItem3ActionPerformed

private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
    // TODO add your handling code here:
    srUI.setVisible(true);
}//GEN-LAST:event_jMenuItem4ActionPerformed

private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
    // TODO add your handling code here:
    tableClear();
    control.spellingCheck(textArea.getText());
}//GEN-LAST:event_jMenuItem5ActionPerformed

private void textAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textAreaKeyPressed

}//GEN-LAST:event_textAreaKeyPressed

private void findICON2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findICON2ActionPerformed
    // TODO add your handling code here:
    dicUI.setVisible(true);
}//GEN-LAST:event_findICON2ActionPerformed

private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_jCheckBox1ActionPerformed

private void corpusRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_corpusRadioBtnActionPerformed
    // TODO add your handling code here:
    jCheckBox1.setEnabled(true);
}//GEN-LAST:event_corpusRadioBtnActionPerformed

private void webRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webRadioBtnActionPerformed
    // TODO add your handling code here:
    jCheckBox1.setEnabled(false);
}//GEN-LAST:event_webRadioBtnActionPerformed

private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
    // TODO add your handling code here:
    findICON2ActionPerformed(evt);
}//GEN-LAST:event_jMenuItem6ActionPerformed

private void stopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBtnActionPerformed
    // TODO add your handling code here:
    control.cancel();
}//GEN-LAST:event_stopBtnActionPerformed

private void MenuItem_DWRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuItem_DWRActionPerformed
    control.recommend(textArea.getText());
}//GEN-LAST:event_MenuItem_DWRActionPerformed

private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
    // TODO add your handling code here:
    srUI.setVisible(true);
}//GEN-LAST:event_jMenuItem1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Copy_MenuItem3;
    private javax.swing.JMenuItem Cut_MenuItem3;
    private javax.swing.JMenuItem Exit_MenuItem3;
    private javax.swing.JMenuBar MenuBar5;
    private javax.swing.JMenuItem MenuItem_DWR5;
    private javax.swing.JMenu Menu_Assistants5;
    private javax.swing.JMenu Menu_Edit5;
    private javax.swing.JMenu Menu_File5;
    private javax.swing.JMenuItem Paste_MenuItem3;
    private javax.swing.JMenuItem SaveDoc_MenuItem3;
    private javax.swing.JScrollPane ScrollPanel_textArea;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton configICON;
    private javax.swing.JButton copyICON;
    private javax.swing.JRadioButton corpusRadioBtn;
    private javax.swing.JButton cutICON;
    private javax.swing.JButton findICON;
    private javax.swing.JButton findICON1;
    private javax.swing.JButton findICON2;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu24;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuItem loadDOC_MenuItem3;
    private javax.swing.JMenuItem newDOC_MenuItem3;
    private javax.swing.JButton newICON;
    private javax.swing.JButton openICON;
    private javax.swing.JButton pasteICON;
    private javax.swing.JLabel rTypeLabel;
    private javax.swing.JRadioButton radioFutureBtn;
    private javax.swing.JRadioButton radioImperativeBtn;
    private javax.swing.JRadioButton radioPastBtn;
    private javax.swing.JRadioButton radioPresentBtn;
    private javax.swing.JRadioButton radioPyungseoBtn;
    private javax.swing.JRadioButton radioQuestionBtn;
    private javax.swing.JButton recomICON;
    private javax.swing.JButton saveICON;
    private javax.swing.JPanel settingPanel;
    private javax.swing.JButton stopBtn;
    private javax.swing.JTextArea textArea;
    private javax.swing.JRadioButton webRadioBtn;
    // End of variables declaration//GEN-END:variables

     public void addRecomItem(RecomInfo recomInfo) {
        lv.addListItem(recomInfo);
        lv.getJList().requestFocus(true);
       // lv.getJList().setSelectedIndex(0);
    }

    public void showListViewer(RType rtype) {
        Point p;

        if (show == true) {
            return;
        }
        setShow(true);
        JPanel listPanel = lv.getListPanel();
        p = getCurrentTextAreaPoint();

        Caret caret = textArea.getCaret();

        // 한글 에러문제 처리
        if (p == null) {
            setShow(false);
            return;
        }
        // assistant 좌표 처리
        if ((p.x + 350) > textArea.getSize().width) {
            p.x = textArea.getSize().width - 335;
            p.y = p.y + 20;
            listPanel.setLocation(p.x, p.y);
            if ((p.y + 220) > textArea.getSize().height) {
                p.y = p.y - 230;
                listPanel.setLocation(p.x, p.y);
            }
        } else if ((p.y + 220) > textArea.getSize().height) {
            p.y = p.y - 215;
            listPanel.setLocation(p.x, p.y);
            if ((p.x + 350) > textArea.getSize().width) {
                p.x = textArea.getSize().width - 335;
                //p.y = p.y + 20;
                listPanel.setLocation(p.x, p.y);
            }
        } else {
            listPanel.setLocation((p.x) + 2, (p.y) + 1);
        }

        lv.getListViewerStatic().setText(rtype.toString());
        lv.setCurrentRType(rtype);
        
        textArea.add(listPanel);
        listPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
        listPanel.setVisible(true);
        textArea.setFocusable(false);
        lv.getJList().requestFocus(true);
    }

    public void showCorrectWord(List<String> cList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void markWrongWord(List<String> wList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public void command() {
        this.setVisible(true);
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public WritingAssistantCtrl getControl() {
        return control;
    }

    public void showRecomList(List<String> rList, RType rtype) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void stopBtnEnable(boolean b){
        stopBtn.setEnabled(b);
    }
    public Point getCurrentTextAreaPoint() {

        Point point = textArea.getCaret().getMagicCaretPosition();
        return point;
    }

    public void addCandidateItem(OrthographyInfo orInfo) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        OrthographyInfo[] columData = {orInfo};
        model.addRow(columData);
        jTable1.scrollRectToVisible(jTable1.getCellRect(jTable1.getRowCount() - 1, jTable1.getColumnCount(), true));
    }
    public void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public JRadioButton getCurrentBTN() {
        return radioPresentBtn;
    }

    public JRadioButton getFutureBTN() {
        return radioFutureBtn;
    }

    public JRadioButton getPastBTN() {
        return radioPastBtn;
    }

    public JRadioButton getSangdeNopimBTN() {
        return radioQuestionBtn;
    }

    public JRadioButton getSangdePyungseoBTN() {
        return radioPyungseoBtn;
    }

    public ButtonGroup getButtonGroup1() {
        return buttonGroup1;
    }

    public ButtonGroup getButtonGroup2() {
        return buttonGroup2;
    }


    public void showContextTable(List<Context> list) {
        DefaultTableModel dt = (DefaultTableModel) jTable3.getModel();
        while (dt.getRowCount() > 0) {
            dt.removeRow(0);
        }

        for (Context s : list) {
            Vector v = new Vector();
            v.add(s.getLeftContext());
            v.add(s.getCenterContext());
            v.add(s.getRightContext());
            dt.addRow(v);
        }
    }
    public void showOrthoRecomWord() {
        DefaultTableModel t1Model = (DefaultTableModel) jTable1.getModel();
        //추천 단어 table clear
        DefaultTableModel t2Model = (DefaultTableModel) jTable2.getModel();
        while (t2Model.getRowCount() > 0) {
            t2Model.removeRow(0);
        }

        int selectedRow = jTable1.getSelectedRow();
        OrthographyInfo orInfo = (OrthographyInfo) t1Model.getValueAt(selectedRow, 0);
        List<SimilarityInfo> sList = orInfo.getCandidates();
        for (SimilarityInfo s : sList) {
            Vector v = new Vector();
            v.add(s.getStr());
            t2Model.addRow(v);
        }
    }

    /**
     *
     * @return 1: web  2: corpus
     */
    public int getSelectedSearchType(){
        if(webRadioBtn.isSelected()) return 1;
        else return 0;
    }
    public void replaceWrongWord() {
        DefaultTableModel t1Model = (DefaultTableModel) jTable1.getModel();
        DefaultTableModel t2Model = (DefaultTableModel) jTable2.getModel();

        int selected1 = jTable1.getSelectedRow();
        int selected2 = jTable2.getSelectedRow();


        OrthographyInfo orInfo = (OrthographyInfo) t1Model.getValueAt(selected1, 0);
        String wrongWord = orInfo.getWrongword();
        String selectedWord = (String) t2Model.getValueAt(selected2, 0);
       
        String all = textArea.getText();
        all = all.replaceAll(wrongWord, selectedWord);
        textArea.setText(all);

        //table에서 지움
        while(t2Model.getRowCount() > 0){
            t2Model.removeRow(0);
        }
        t1Model.removeRow(selected1);
    }
    
    public void clearAll(){
    	orMap.clear();
    	DefaultTableModel t1Model = (DefaultTableModel) jTable1.getModel();
    	while(t1Model.getRowCount() > 0){
    		t1Model.removeRow(0);
    	}
    	DefaultTableModel t2Model = (DefaultTableModel) jTable2.getModel();
    	while(t2Model.getRowCount() > 0){
    		t2Model.removeRow(0);
    	}
    }
    public static void centerOnScreen(Window window) {
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Rectangle max = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int x = Math.max(center.x - Math.round(window.getWidth()/2f), max.x);
        int y = Math.max(center.y - Math.round(window.getHeight()/2f), max.y);
        window.setLocation(new Point(x, y));
    }
    public void setRTypeLableText(String str){
        rTypeLabel.setText(str);
    }
    public RecomConfig getRecomConfig(){
        return cv.getRc();
    }
    class PopupListener extends MouseAdapter {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }
    public boolean checkCache(){
        if(jCheckBox1.isSelected() == true){
            return true;
        }
        return false;
    }
}



