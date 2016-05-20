package se.kth.csc.sima;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class SimaGUICourseDialog extends JPanel {
    private static final long serialVersionUID = 3356190276831049476L;

    private SimaManager manager;

    CourseModel queueListModel;

    private JPanel bottomPanel;
    private JPanel queueDialog;

    private JTextField nameField;
    private JRadioButton helpButton;
    private JRadioButton examButton;
    private JRadioButton defaultHostButton;
    private JRadioButton customHostButton;
    private JTextField customHostField;
    private JTextField messageField;

    private JButton queueButton;
    private ActionListener addToQueue;
    private ActionListener removeFromQueue;

    public SimaGUICourseDialog(SimaManager manager) {
        this.manager = manager;

        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setLayout(new BorderLayout(5, 5));

        JLabel label = new JLabel(manager.getJoinedCourseName());
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setFont(new Font("sans-serif", Font.BOLD, 20));
        this.add(label, BorderLayout.NORTH);

        this.queueListModel = new CourseModel();
        JTable courseList = new JTable(this.queueListModel);
        { // set up table/list for displaying queue
            courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            courseList.setColumnSelectionAllowed(false);
            for (int i = 0; i < 5; i++) {
                courseList.getColumnModel().getColumn(i).setPreferredWidth((new int[] {10, 100, 20, 70, 10})[i]);
            }
            courseList.setRowSorter(null);
        }
        this.add(new JScrollPane(courseList), BorderLayout.CENTER);

        this.bottomPanel = new JPanel();
        this.bottomPanel.setLayout(new BorderLayout());

        this.queueDialog = new JPanel();
        { // create panel/dialog for inputting information for queueing
            GridBagLayout gridLayout = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            this.queueDialog.setLayout(gridLayout);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.insets = new Insets(5, 5, 5, 5);
            gridLayout.setConstraints(this.queueDialog.add(new JLabel("Namn")), c);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridLayout.setConstraints(this.queueDialog.add(this.nameField = new JTextField(Utils.getFullName())), c);

            c.gridwidth = 1;
            c.gridheight = 2;
            c.weighty = 1.0;
            gridLayout.setConstraints(this.queueDialog.add(new JLabel("Typ")), c);
            ButtonGroup type = new ButtonGroup();
            this.helpButton = new JRadioButton("Hjälp", true);
            this.examButton = new JRadioButton("Redovisning");
            type.add(this.helpButton);
            type.add(this.examButton);
            c.weighty = 0.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.insets = new Insets(0, 5, 0, 5);
            gridLayout.setConstraints(this.queueDialog.add(this.helpButton), c);
            c.insets = new Insets(0, 5, 5, 5);
            gridLayout.setConstraints(this.queueDialog.add(this.examButton), c);

            c.gridwidth = 1;
            c.gridheight = 2;
            c.insets = new Insets(5, 5, 5, 5);
            gridLayout.setConstraints(this.queueDialog.add(new JLabel("Plats")), c);
            ButtonGroup place = new ButtonGroup();
            this.defaultHostButton = new JRadioButton("Använd datornamnet (" + Utils.getHost() + ")", true);
            this.customHostButton = new JRadioButton();
            place.add(this.defaultHostButton);
            place.add(this.customHostButton);
            c.weighty = 0.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.gridheight = 1;
            c.insets = new Insets(0, 5, 0, 5);
            gridLayout.setConstraints(this.queueDialog.add(this.defaultHostButton), c);
            c.gridwidth = 1;
            c.weightx = 0.0;
            c.insets = new Insets(0, 5, 5, 5);
            gridLayout.setConstraints(this.queueDialog.add(this.customHostButton), c);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridLayout.setConstraints(this.queueDialog.add(this.customHostField = new JTextField()), c);
            
            c.gridwidth = 1;
            c.insets = new Insets(5, 5, 5, 5);
            gridLayout.setConstraints(this.queueDialog.add(new JLabel("Meddelande")), c);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridLayout.setConstraints(this.queueDialog.add(this.messageField = new JTextField()), c);
            
            defaultHostButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    customHostField.setEnabled(false);
                }
            });
            customHostField.setEnabled(false);
            customHostButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    customHostField.setEnabled(true);
                }
            });
        }
        this.bottomPanel.add(this.queueDialog, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        { // create panel for queueing, logging out, removing from queue
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
            this.queueButton = new JButton("Ställ dig i kö");

            this.addToQueue = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = SimaGUICourseDialog.this.nameField.getText();
                    int type = SimaGUICourseDialog.this.helpButton.isSelected() ? Utils.TYPE_HELP : Utils.TYPE_EXAM;
                    String host = SimaGUICourseDialog.this.defaultHostButton.isSelected() ? Utils.getHost()
                            : SimaGUICourseDialog.this.customHostField.getText();
                    String message = SimaGUICourseDialog.this.messageField.getText();
                    SimaGUICourseDialog.this.manager.queue(name, type, host, message);
                }
            };
            this.removeFromQueue = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SimaGUICourseDialog.this.manager.exitQueue();
                }
            };

            this.queueButton.addActionListener(this.addToQueue);
            buttonPanel.add(this.queueButton);
            buttonPanel.add(Box.createHorizontalGlue());

            JButton logout = new JButton("Logga ut");
            logout.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SimaGUICourseDialog.this.manager.exitCourse();
                }
            });

            buttonPanel.add(logout);
        }
        this.bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        this.add(this.bottomPanel, BorderLayout.SOUTH);
    }

    public void showQueueDialog(boolean show) {
        if (show) {
            this.bottomPanel.add(this.queueDialog);
            this.queueButton.setText("Ställ dig i kö");
            this.queueButton.removeActionListener(this.removeFromQueue);
            this.queueButton.addActionListener(this.addToQueue);
        } else {
            this.bottomPanel.remove(this.queueDialog);
            this.queueButton.setText("Gå ur kön");
            this.queueButton.removeActionListener(this.addToQueue);
            this.queueButton.addActionListener(this.removeFromQueue);
        }
        this.validate();
    }

    class CourseModel implements TableModel {
        private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();

        @Override
        public int getRowCount() {
            return SimaGUICourseDialog.this.manager.getCourseQueue().size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return (new String[] {"", "Namn", "Plats", "Meddelande", "Typ"})[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SimaQueueObject sqq = SimaGUICourseDialog.this.manager.getCourseQueue().get(rowIndex);
            switch (columnIndex) {
            case 0:
                return rowIndex + 1 + ".";
            case 1:
                return sqq.getName();
            case 2:
                return sqq.getHost();
            case 3:
                return sqq.getMessage();
            case 4:
                return sqq.getType() == Utils.TYPE_HELP ? "Hjälp" : "Redovisning";
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            return;
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            this.listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            this.listeners.remove(l);
        }

        public void updateListeners() {
            for (TableModelListener l : this.listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
        }
    }

}

