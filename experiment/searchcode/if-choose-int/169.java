/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GUI;

import Interfaces.I_MESI_Cache;
import Interfaces.I_MESI_Model;
import Interfaces.MESI_Operation_Descriptor;
import Logic.MESI_Model;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import static javax.swing.BorderFactory.createLineBorder;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Nadja
 */
public class MainFrame extends javax.swing.JFrame {

    private ArrayList<JTable> CacheTables;
    private ArrayList<JScrollPane> CacheScrollPanes;
    private int SelectedCacheNum;
    private int SelectedMemoryString;
    private int SelectedCacheString;
    private boolean ProgramSelection;
    private boolean SelectingCache;
    private boolean SelectingCacheString;
    private boolean SelectingMemoryString;
    private I_MESI_Model Model;
    
    private ArrayList<MESI_Operation_Descriptor> Operations;
    private int VisualisatorStepNum;
    
    enum Commands  {WRITE, READ, INVALIDATE};
    
    private Commands Command;
    
    Timer Timer;
    /**
     * Creates new form MainFrame
     */
    public MainFrame(I_MESI_Model M)
    {
        initComponents();
        adjustColumnSizes(MemoryTable, 0, 2);
        CacheTables = new ArrayList<>(6);
        CacheScrollPanes = new ArrayList<>(6);
        SelectedCacheNum = -1;
        CacheComboBox.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!ProgramSelection)
                    SelectCache(((JComboBox)e.getSource()).getSelectedIndex());
            }
        });
        StringComboBox.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!ProgramSelection)
                    SelectMemoryString(((JComboBox)e.getSource()).getSelectedIndex());
            }
        });
        CacheStringComboBox.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (!ProgramSelection)
                    SelectCacheString(SelectedCacheNum,((JComboBox)e.getSource()).getSelectedIndex());
            }
        });
        MemoryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener () {

            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!ProgramSelection)
                    SelectMemoryString(((ListSelectionModel)e.getSource()).getMaxSelectionIndex());
            }
          
        });
         Model = M;
         SelectingCacheString = false;
         SelectingMemoryString = false;
         
         Operations = new ArrayList<>(0);
         VisualisatorStepNum = 0;
         this.ReportArea.setEditable(false);
         this.ReportArea.setLineWrap(true);
         this.ReportArea.setWrapStyleWord(true);
         
         Timer = new Timer(1000, new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (VisualisatorStepNum < Operations.size()-1)
                    NextStep();
                else btPauseActionPerformed(null);
                repaint();
            }}
         );
    }
    
    

    private class CacheTableMouseListener extends MouseAdapter{

        @Override
        public void mouseClicked(MouseEvent e)
        {
            SelectCache(CacheTables.indexOf((JTable)e.getSource()));
        }      
        
    }
    
    private class CacheScrollPaneMouseListener extends MouseAdapter{

        @Override
        public void mouseClicked(MouseEvent e)
        {
            SelectCache(CacheScrollPanes.indexOf((JScrollPane)e.getSource()));
        }      
        
    }
    
    private class CacheTableSelectionListener implements ListSelectionListener{

        private int TableNum;
        public CacheTableSelectionListener(int TableNum) {
            this.TableNum = TableNum;
        }
        @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!ProgramSelection)                   
                    SelectCacheString(TableNum,((ListSelectionModel)e.getSource()).getMaxSelectionIndex());
            }  
        
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);
        formatter.setMaximum(Integer.MAX_VALUE);
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        InitButton = new javax.swing.JButton();
        Cache_Num_Field = new javax.swing.JFormattedTextField(formatter);
        Mem_Size_Field = new javax.swing.JFormattedTextField(formatter);
        Cache_Size_Field = new javax.swing.JFormattedTextField(formatter);
        String_Size_Field = new javax.swing.JFormattedTextField(formatter);
        jPanel4 = new javax.swing.JPanel();
        CacheComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        StringComboBox = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        ReadButton = new javax.swing.JButton();
        InvalidateButton = new javax.swing.JButton();
        NewStringField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        WriteButton = new javax.swing.JButton();
        CacheStringComboBox = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        ReportArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        MemoryTable = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btStart = new javax.swing.JButton();
        btPlay = new javax.swing.JButton();
        btPrev = new javax.swing.JButton();
        btPause = new javax.swing.JButton();
        btNext = new javax.swing.JButton();
        btEnd = new javax.swing.JButton();
        outputPanel1 = new GUI.OutputPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        CachePane = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(850, 600));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setPreferredSize(new java.awt.Dimension(217, 161));

        jLabel1.setText("Число кэшей");

        jLabel2.setText("<html>\nРазмер памяти\n<br> (в строках)\n</html>");

        jLabel3.setText("<html>\nРазмер кэша\n<br> (в строках)\n</html>");

        jLabel4.setText("<html>\nРазмер строки\n<br> (в символах)\n</html>");

        InitButton.setText("Иницализировать модель");
        InitButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                InitButtonActionPerformed(evt);
            }
        });

        Cache_Num_Field.setText("4");

        Mem_Size_Field.setText("12");

        Cache_Size_Field.setText("6");

        String_Size_Field.setText("64");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(InitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(Cache_Num_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(Mem_Size_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(Cache_Size_Field, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(String_Size_Field, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(Cache_Num_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Mem_Size_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Cache_Size_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(String_Size_Field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(InitButton)
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setEnabled(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(217, 178));

        CacheComboBox.setEnabled(false);

        jLabel5.setText("Выбор кэша");

        StringComboBox.setEnabled(false);

        jLabel6.setText("<html>\nВыбор строки в памяти\n</html>");

        ReadButton.setText("Считать");
        ReadButton.setEnabled(false);
        ReadButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                ReadButtonActionPerformed(evt);
            }
        });

        InvalidateButton.setText("Инвалидировать");
        InvalidateButton.setEnabled(false);
        InvalidateButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                InvalidateButtonActionPerformed(evt);
            }
        });

        NewStringField.setEnabled(false);

        jLabel7.setText("Новое значение строки:");

        WriteButton.setText("Записать");
        WriteButton.setEnabled(false);
        WriteButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                WriteButtonActionPerformed(evt);
            }
        });

        CacheStringComboBox.setEnabled(false);

        jLabel8.setText("<html> Выбор строки в кэше </html>");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ReadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(InvalidateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 81, Short.MAX_VALUE))
                    .addComponent(NewStringField)
                    .addComponent(WriteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CacheComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(StringComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(CacheStringComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CacheComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(StringComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CacheStringComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(ReadButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(InvalidateButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NewStringField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(WriteButton)
                .addGap(23, 23, 23))
        );

        jPanel7.setPreferredSize(new java.awt.Dimension(342, 238));

        ReportArea.setColumns(20);
        ReportArea.setRows(5);
        ReportArea.setWrapStyleWord(true);
        jScrollPane4.setViewportView(ReportArea);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setMinimumSize(new java.awt.Dimension(242, 214));
        jPanel2.setPreferredSize(new java.awt.Dimension(492, 214));

        MemoryTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {

            },
            new String []
            {
                "Номер", "Содержание"
            }
        )
        {
            Class[] types = new Class []
            {
                java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean []
            {
                false, false
            };

            public Class getColumnClass(int columnIndex)
            {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit [columnIndex];
            }
        });
        MemoryTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        MemoryTable.getTableHeader().setReorderingAllowed(false);
        MemoryTable.setShowVerticalLines(true);
        jScrollPane1.setViewportView(MemoryTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel6.setMinimumSize(new java.awt.Dimension(342, 251));
        jPanel6.setPreferredSize(new java.awt.Dimension(342, 251));

        btStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/control_start_blue.png"))); // NOI18N
        btStart.setDisabledIcon(null);
        btStart.setEnabled(false);
        btStart.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btStartActionPerformed(evt);
            }
        });

        btPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/control_play_blue.png"))); // NOI18N
        btPlay.setDisabledIcon(null);
        btPlay.setEnabled(false);
        btPlay.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btPlayActionPerformed(evt);
            }
        });

        btPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/control_rewind_blue.png"))); // NOI18N
        btPrev.setDisabledIcon(null);
        btPrev.setEnabled(false);
        btPrev.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btPrevActionPerformed(evt);
            }
        });

        btPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/control_pause_blue.png"))); // NOI18N
        btPause.setDisabledIcon(null);
        btPause.setEnabled(false);
        btPause.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btPauseActionPerformed(evt);
            }
        });

        btNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/control_fastforward_blue.png"))); // NOI18N
        btNext.setDisabledIcon(null);
        btNext.setEnabled(false);
        btNext.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btNextActionPerformed(evt);
            }
        });

        btEnd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/control_end_blue.png"))); // NOI18N
        btEnd.setDisabledIcon(null);
        btEnd.setEnabled(false);
        btEnd.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btEndActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout outputPanel1Layout = new javax.swing.GroupLayout(outputPanel1);
        outputPanel1.setLayout(outputPanel1Layout);
        outputPanel1Layout.setHorizontalGroup(
            outputPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        outputPanel1Layout.setVerticalGroup(
            outputPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(btStart)
                .addGap(6, 6, 6)
                .addComponent(btPrev)
                .addGap(6, 6, 6)
                .addComponent(btPlay)
                .addGap(6, 6, 6)
                .addComponent(btPause)
                .addGap(6, 6, 6)
                .addComponent(btNext)
                .addGap(6, 6, 6)
                .addComponent(btEnd)
                .addContainerGap(10, Short.MAX_VALUE))
            .addComponent(outputPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(outputPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btStart)
                    .addComponent(btPrev)
                    .addComponent(btPlay)
                    .addComponent(btPause)
                    .addComponent(btNext)
                    .addComponent(btEnd)))
        );

        java.awt.GridBagLayout CachePaneLayout = new java.awt.GridBagLayout();
        CachePaneLayout.columnWidths = new int[] {0};
        CachePaneLayout.rowHeights = new int[] {0};
        CachePane.setLayout(CachePaneLayout);
        jScrollPane2.setViewportView(CachePane);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void InitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_InitButtonActionPerformed
    {//GEN-HEADEREND:event_InitButtonActionPerformed
       
        this.CacheComboBox.setEnabled(true);
        this.StringComboBox.setEnabled(true);
        this.CacheStringComboBox.setEnabled(true);
        this.ReadButton.setEnabled(true);
        this.WriteButton.setEnabled(true);
        this.NewStringField.setEnabled(true);
        
        int Cache_Num = Integer.parseInt(this.Cache_Num_Field.getText());
        int Mem_Size = Integer.parseInt(this.Mem_Size_Field.getText());
        int Cache_Size = Integer.parseInt(this.Cache_Size_Field.getText());
        int String_Size = Integer.parseInt(this.String_Size_Field.getText());
        if (Cache_Size > Mem_Size)
        {
            JOptionPane.showMessageDialog(rootPane, "Размер кэша не может превышать размер памяти", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Model.Initialize(Cache_Num, Mem_Size, Cache_Size, String_Size);
        SelectedCacheNum = -1;
        SelectedMemoryString = -1;
        SelectedCacheString = -1;
        this.SetUpCaches();
        this.SetUpMemory();
        
        
        this.validate();
        this.repaint();
    }//GEN-LAST:event_InitButtonActionPerformed

    private void ReadButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ReadButtonActionPerformed
    {//GEN-HEADEREND:event_ReadButtonActionPerformed
        Model.ReadToCache(this.SelectedCacheNum, this.SelectedMemoryString);
        this.Command = Commands.READ;
        Update();
    }//GEN-LAST:event_ReadButtonActionPerformed

    private void InvalidateButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_InvalidateButtonActionPerformed
    {//GEN-HEADEREND:event_InvalidateButtonActionPerformed
        Model.DropFromCache(this.SelectedCacheNum, this.SelectedCacheString);
        this.Command = Commands.INVALIDATE;
        Update();
    }//GEN-LAST:event_InvalidateButtonActionPerformed

    private void WriteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_WriteButtonActionPerformed
    {//GEN-HEADEREND:event_WriteButtonActionPerformed
      Model.WriteToCache(SelectedCacheNum, SelectedMemoryString, this.NewStringField.getText());
      this.Command = Commands.WRITE;
      Update();
    }//GEN-LAST:event_WriteButtonActionPerformed

    private void btStartActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btStartActionPerformed
    {//GEN-HEADEREND:event_btStartActionPerformed
        InitReports();
        this.repaint();
    }//GEN-LAST:event_btStartActionPerformed

    private void btPrevActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btPrevActionPerformed
    {//GEN-HEADEREND:event_btPrevActionPerformed
       PrevStep();
       this.repaint();
    }//GEN-LAST:event_btPrevActionPerformed

    private void btPlayActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btPlayActionPerformed
    {//GEN-HEADEREND:event_btPlayActionPerformed
        if (this.VisualisatorStepNum < this.Operations.size() - 1)
        {
            this.Timer.start();
            this.btPlay.setEnabled(false);
            this.btPause.setEnabled(true);
        }
    }//GEN-LAST:event_btPlayActionPerformed

    private void btPauseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btPauseActionPerformed
    {//GEN-HEADEREND:event_btPauseActionPerformed
        this.Timer.stop();
        this.btPlay.setEnabled(true);
        this.btPause.setEnabled(false);
    }//GEN-LAST:event_btPauseActionPerformed

    private void btNextActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btNextActionPerformed
    {//GEN-HEADEREND:event_btNextActionPerformed
       NextStep();
       this.repaint();
    }//GEN-LAST:event_btNextActionPerformed

    private void btEndActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btEndActionPerformed
    {//GEN-HEADEREND:event_btEndActionPerformed
        while (this.VisualisatorStepNum < this.Operations.size()-1)
        NextStep();
        this.repaint();
    }//GEN-LAST:event_btEndActionPerformed

    private void Update() {
        
        InitReports();               
        
        ArrayList<I_MESI_Cache> Caches = Model.GetCaches();
        for (int i = 0; i < this.CacheTables.size(); i++)
        {
            ((CacheTableModel)CacheTables.get(i).getModel()).ExtractCacheData(Caches.get(i));
            adjustColumnSizes(CacheTables.get(i), 3, 2);
            adjustTableSize(CacheTables.get(i));
            
        }
        ArrayList<String> Memory = Model.GetMemory();
        for (int i = 0; i < this.MemoryTable.getRowCount(); i++)
        {
            this.MemoryTable.setValueAt(Memory.get(i), i, 1);
        }
        CheckInvalidateButton();
        this.validate();
        this.repaint();
    }
    
    private void InitReports() {
        Operations = Model.GetOperations();
        this.VisualisatorStepNum = -1;
        UpdateButtonsState();
        this.ReportArea.setText("");
        if (this.Operations.size() > 0)
            this.ReportArea.append(this.GetCommandStartDescription(this.Operations.get(0).Primary_Cache_Num,
                    this.Operations.get(0).Current_Memory_String_Num,this.Model.GetCahceSize()));
        this.btPlay.setEnabled(true);
        this.outputPanel1.Reset();

    }
    
    private void NextStep() {
        VisualisatorStepNum++;
        ReportArea.append("\n"+"\n"+GetOperationDescription(Operations.get(VisualisatorStepNum)));
        this.outputPanel1.SetState(Operations.get(VisualisatorStepNum));
        this.UpdateButtonsState();
        if (VisualisatorStepNum == this.Operations.size()-1)
            ReportArea.append("\n"+"\n"+this.GetCommandEndDescription(this.Operations.get(0).Primary_Cache_Num));
    }
    
    private void PrevStep() {
        
         VisualisatorStepNum--;
         if (VisualisatorStepNum <0)
         {
             this.InitReports();
             return;
         }
        String Temp = ReportArea.getText().substring(0,ReportArea.getText().lastIndexOf("\n"));
        Temp = Temp.substring(0, Temp.lastIndexOf("\n"));
        if (VisualisatorStepNum == this.Operations.size()-2)
        {
           Temp = Temp.substring(0, Temp.lastIndexOf("\n"));
           Temp = Temp.substring(0, Temp.lastIndexOf("\n"));
        }
        ReportArea.setText(Temp);
       
        this.outputPanel1.SetState(Operations.get(VisualisatorStepNum));
        this.UpdateButtonsState();
    }
    
    private String GetCommandStartDescription(int CacheNum, int MemStringNum, int CacheSize)
    {
        int CacheStringNum = MemStringNum % CacheSize;
        String Temp = "";
        switch (this.Command)
        {
            case READ: Temp = "В кэше НК выполняется операция чтения "
                    + "строки памяти НМ, соответствующей строке кэша НСК.";
                break;
            case WRITE: Temp = "В кэше НК выполняется операция записи "
                    + "строки памяти НМ, соответствующей строке кэша НСК.";
                break;
            case INVALIDATE: Temp = "В кэше НК выполняется операция инвалидации "
                    + "строки кэша НСК.";
                break;                
        }
        Temp = Temp.replaceAll("НК", "№"+String.valueOf(CacheNum+1));
        Temp = Temp.replaceAll("НСК", "№"+String.valueOf(CacheStringNum+1));
        Temp = Temp.replaceAll("НМ", "№"+String.valueOf(MemStringNum+1));
        return Temp;
    }
    
     private String GetCommandEndDescription(int CacheNum)
    {
        String Temp = "";
        switch (this.Command)
        {
            case READ: Temp = "Операция чтения в кэше НК успешно завершена. ";
                break;
            case WRITE: Temp = "Операция записи в кэше НК успешно завершена. ";
                break;
            case INVALIDATE: Temp = "Операция инвалидации в кэше НК успешно завершена. ";
                break;                
        }
        Temp = Temp.replaceAll("НК", "№"+String.valueOf(CacheNum+1));
        return Temp;
    }
    
    private String GetOperationDescription(MESI_Operation_Descriptor D)
    {
        String Temp = "";
        boolean User = D.Primary_Cache_Num == -1;
        switch (D.Operation)
        {
            case EXCLUSIVE_TO_EXCLUSIVE_READ: 
                Temp = "Попадание чтения в кэше НК. Кэш является единственным владельцем строки памяти НМ";
                break;
            case SHARED_TO_SHARED_READ: 
                Temp = "Попадание чтения в кэше НК. Кэш является одним из владельцев строки памяти НМ";
                break;
            case MODIFIED_TO_MODIFIED_READ: 
                Temp = "Попадание чтения в кэше НК. Кэш является единственным владельцем"
                        + " модифицированной строки памяти НМ";
                break;
            case READ_REQUEST: 
                Temp = "Кэш НК опрашивает другие кэши о наличии в них строки памяти НМ";
                break;
            case READING_FROM_MEMORY: 
                Temp = "Кэш НК читает из памяти строку НМ";
                break;
            case INVALID_TO_EXCLUSIVE: 
                Temp = "Кэш НК не получил от других кэшей ответов о владении строки НМ "
                        + "и становится ее единственным владельцем";
                break;
            case INVALID_TO_SHARED: 
                Temp = "Кэш НК становится одним из владельцев строки памяти НМ";
                break;
            case EXCLUSIVE_TO_SHARED: 
                Temp = "Кэш НК получает запрос от кэша НП о владении строкой НМ."
                        + " Он являлся единственным владельцем строки, но теперь будет разделять ее с кэщем НП";
                break;
            case SHARED_TO_SHARED: 
                Temp = "Кэш НК получает запрос от кэша НП о владении строкой НМ."
                        + " Он уже разделяет владение этой строкой, поэтому изменения состояния не происходит";
                break;
            case WRITE_TO_MEMORY: 
                Temp = "Кэш НК записывает сделанные модификации в память";
                break;
            case MODIFIED_TO_SHARED: 
                Temp = "Кэш НК получает запрос от кэша НП о владении строкой НМ."
                        + " Он модифицировал эту строку, и потому поместил внесенные изменения в память"
                        + " прежде, чем перевести ее в состояние разделенного владения";
                break;
            case MODIFIED_TO_MODIFIED_WRITE: 
                Temp = "Кэш НК осуществляет запись в уже ранее модифицированную им строку памяти НМ";
                break;
            case EXCLUSIVE_TO_MODIFIED: 
                Temp = "Кэш НК осуществляет запись в строку памяти НМ, которой владеет единолично";
                break;
            case INVALIDATE_REQUSET: 
                Temp = "Кэщ НК требует от других кэшей инвалидировать строку НМ, поскольку он собирается "
                        + "модифицировать ее";
                break;
            case INVALID_TO_MODIFIED: 
                Temp = "Кэш НК забирает владение строкой памяти НМ, послав требование инвалиации"
                        + " и записывает в себя новое значение. Чтения из памяти не происходит";
                break;
            case SHARED_TO_MODIFIED: 
                Temp = "Кэш НК заставляет кэши, разделявшие с ним владение инвалидировать свои копии строки НМ"
                        + " и записывает в себя новое значение этой строки";
                break;
            case EXCLUSIVE_TO_INVALID: 
                Temp = "Кэш НК получил запрос об инвалидации строки НМ от ";
                if (User)
                     Temp += "пользователя. ";
                else Temp += "кэша НП. ";
                Temp += "До инвалидации кэш был единственным владельцем строки.";
                break;
            case SHARED_TO_INVALID: 
                Temp = "Кэш НК получил запрос об инвалидации строки НМ от ";
                if (User)
                     Temp += "пользователя. ";
                else Temp += "кэша НП. ";
                Temp += "До инвалидации кэш был одним из владельцев строки.";
                break;
            case MODIFIED_TO_INVALID: 
                Temp = "Кэш НК получил запрос об инвалидации строки НМ от ";
                if (User)
                     Temp += "пользователя. ";
                else Temp += "кэша НП. ";
                Temp += "";
                break;
        }
        Temp = Temp.replaceAll("НК", "№"+String.valueOf(D.Current_Cache_Num+1));
        Temp = Temp.replaceAll("НП", "№"+String.valueOf(D.Primary_Cache_Num+1));
        Temp = Temp.replaceAll("НМ", "№"+String.valueOf(D.Current_Memory_String_Num+1));
        return Temp;
    }
    private void UpdateButtonsState()
    {
        this.btStart.setEnabled(this.VisualisatorStepNum >= 0);
        this.btPrev.setEnabled(this.VisualisatorStepNum >= 0);
        this.btEnd.setEnabled(this.VisualisatorStepNum < this.Operations.size() - 1);
        this.btNext.setEnabled(this.VisualisatorStepNum < this.Operations.size() - 1);

    }
    
    private void SetUpMemory()
    {
        ((DefaultTableModel)this.MemoryTable.getModel()).setRowCount(Model.GetMemSize());
        StringComboBox.removeAllItems();
        ArrayList<String> Memory = Model.GetMemory();
        for (int i=0;i<Model.GetMemSize();i++)
        {
            ((DefaultTableModel)this.MemoryTable.getModel()).setValueAt(i+1, i, 0);
            StringComboBox.addItem("Строка № " + String.valueOf(i+1)); 
            this.MemoryTable.setValueAt(Memory.get(i), i, 1);
        }
        adjustColumnSizes(MemoryTable, 0, 2);
        SelectMemoryString(0);
    }
    private void SetUpCaches()
    {
        ArrayList<I_MESI_Cache> Caches = Model.GetCaches();
        this.CachePane.removeAll();
        this.CacheTables.clear();
        this.CacheComboBox.removeAllItems();
        this.CacheScrollPanes.clear();
        ProgramSelection = true;
        for (int i=0; i<Model.GetCacheNum(); i++)
        {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = i%2;
            constraints.gridy = 2*(i/2);
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            JLabel Label = new JLabel("Кэш № " + String.valueOf(i+1));
            Label.setMaximumSize(Label.getPreferredSize());
            CachePane.add(Label,constraints);
            
          
            
            JTable CacheTable = new javax.swing.JTable();
            CacheTableModel TableModel = new CacheTableModel(0);
            TableModel.ExtractCacheData(Caches.get(0));
            CacheTable.setModel(TableModel);
            adjustColumnSizes(CacheTable, 0, 2);
            adjustColumnSizes(CacheTable, 1, 2);
            adjustColumnSizes(CacheTable, 2, 2);
            adjustColumnSizes(CacheTable, 3, 2);
            adjustTableSize(CacheTable);
            CacheTable.getTableHeader().setReorderingAllowed(false);
            CacheTable.setShowVerticalLines(true);
            CacheTable.addMouseListener(new CacheTableMouseListener());
            CacheTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            CacheTables.add(CacheTable);            
            CacheTable.getSelectionModel().addListSelectionListener(new CacheTableSelectionListener(CacheTables.size()-1));
            JScrollPane ScrollPane = new JScrollPane();
            //ScrollPane.setBorder(createLineBorder(Color.BLACK,1));
            ScrollPane.addMouseListener(new CacheScrollPaneMouseListener());
            CacheScrollPanes.add(ScrollPane);
            ScrollPane.add(CacheTable);
            ScrollPane.setViewportView(CacheTable);
            ScrollPane.setPreferredSize(new Dimension(CacheTable.getMaximumSize().width+20,
                    130));
            
            constraints.gridy = 2*(i/2)+1;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.fill = GridBagConstraints.BOTH;
            constraints.anchor = GridBagConstraints.NORTHWEST;
            
            CachePane.add(ScrollPane,constraints);
            
            CacheComboBox.addItem("Кэш № " + String.valueOf(i+1));
        }
        for (int i=0; i<Model.GetCahceSize();i++)
            this.CacheStringComboBox.addItem("Строка № "+ String.valueOf(i+1));
        ProgramSelection = false;
        this.SelectCache(0);           
    }
   private void adjustColumnSizes(JTable table, int column, int margin) {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        TableColumn col = colModel.getColumn(column);
        int width;

        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = table.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
        width = comp.getPreferredSize().width;

        for (int r = 0; r < table.getRowCount(); r++) {
            renderer = table.getCellRenderer(r, column);
            comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, column), false, false, r, column);
            int currentWidth = comp.getPreferredSize().width;
            width = Math.max(width, currentWidth);
        }

        width += 2 * margin;

        col.setPreferredWidth(width);
        col.setMaxWidth(width);
    }
   private void adjustTableSize(JTable table) {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        int MaxWidth  = 0;
        int PreferredWidth  = 0;
        for (int i=0;i<colModel.getColumnCount();i++)
        {
            MaxWidth +=colModel.getColumn(i).getMaxWidth();
            PreferredWidth +=colModel.getColumn(i).getPreferredWidth();
        }
        table.setPreferredSize(new Dimension(PreferredWidth,table.getPreferredSize().height));
        table.setMaximumSize(new Dimension(MaxWidth,table.getMaximumSize().height));
    }
   
   private void SelectCache(int Num)
   {
       boolean PrevPrgSel = ProgramSelection;
       ProgramSelection = true;
       SelectingCache = true;
       if ((SelectedCacheNum != Num))
       {
           if (SelectedCacheNum != -1)
           {
               CacheScrollPanes.get(SelectedCacheNum).setBorder(createLineBorder(Color.BLACK,1));
           }
           SelectedCacheNum = Num;
           CacheComboBox.setSelectedIndex(Num);
           if (Num != -1)
           {
               CacheScrollPanes.get(SelectedCacheNum).setBorder(createLineBorder(Color.BLACK,3));
               SelectMemoryString(SelectedMemoryString);
           }
       }
       SelectingCache = false;
       ProgramSelection = PrevPrgSel;
       
   }
   
    private void SelectMemoryString(int Num)
   {
       boolean PrevPrgSel = ProgramSelection;
       ProgramSelection = true;
       SelectingMemoryString = true;
       if (((SelectedMemoryString != Num) ||  SelectingCache) && (Num != -1))
       {
           SelectedMemoryString = Num;
           StringComboBox.setSelectedIndex(Num);
           MemoryTable.setRowSelectionInterval(Num, Num);
           if (!SelectingCacheString)
           {
               int CacheStringNum = Num % Model.GetCahceSize();
               SelectCacheString(SelectedCacheNum,CacheStringNum);
           }

       }
       SelectingMemoryString = false;
       ProgramSelection = PrevPrgSel;
       
   }
    
    private void SelectCacheString(int CacheNum, int Num)
    {
       boolean PrevPrgSel = ProgramSelection;
       ProgramSelection = true;
       SelectingCacheString = true;
       if ((SelectedCacheString != Num || SelectingCache) && (Num != -1) )
       {          
           if (CacheTables.get(CacheNum).getValueAt(Num, 2) != "" || SelectingMemoryString)
           {
               SelectedCacheString = Num;
               CacheTables.get(CacheNum).setRowSelectionInterval(Num, Num);
               CacheStringComboBox.setSelectedIndex(Num);
               if (!SelectingMemoryString)
               {
                   SelectMemoryString(Integer.parseInt(CacheTables.get(CacheNum).getValueAt(Num, 2).toString())-1);
               }
           } else
           {
               this.CacheStringComboBox.setSelectedIndex(SelectedCacheString);
               if (SelectedCacheString != -1)
               {
                   CacheTables.get(CacheNum).setRowSelectionInterval(SelectedCacheString, SelectedCacheString);
               } else
               {
                   CacheTables.get(CacheNum).clearSelection();
               }
           }          
       }
       CheckInvalidateButton();
       SelectingCacheString = false;
       ProgramSelection = PrevPrgSel;
    }
    private void CheckInvalidateButton()
    {
        if (SelectedCacheNum == -1 || SelectedCacheString == -1)
        {
           this.InvalidateButton.setEnabled(false);
           return;
        }
        if (CacheTables.get(SelectedCacheNum).getValueAt(SelectedCacheString, 2) != "")
               this.InvalidateButton.setEnabled(true);
           else this.InvalidateButton.setEnabled(false);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                MESI_Model M = new MESI_Model();
                new MainFrame(M).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox CacheComboBox;
    private javax.swing.JPanel CachePane;
    private javax.swing.JComboBox CacheStringComboBox;
    private javax.swing.JFormattedTextField Cache_Num_Field;
    private javax.swing.JFormattedTextField Cache_Size_Field;
    private javax.swing.JButton InitButton;
    private javax.swing.JButton InvalidateButton;
    private javax.swing.JFormattedTextField Mem_Size_Field;
    private javax.swing.JTable MemoryTable;
    private javax.swing.JTextField NewStringField;
    private javax.swing.JButton ReadButton;
    private javax.swing.JTextArea ReportArea;
    private javax.swing.JComboBox StringComboBox;
    private javax.swing.JFormattedTextField String_Size_Field;
    private javax.swing.JButton WriteButton;
    private javax.swing.JButton btEnd;
    private javax.swing.JButton btNext;
    private javax.swing.JButton btPause;
    private javax.swing.JButton btPlay;
    private javax.swing.JButton btPrev;
    private javax.swing.JButton btStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private GUI.OutputPanel outputPanel1;
    // End of variables declaration//GEN-END:variables
}

