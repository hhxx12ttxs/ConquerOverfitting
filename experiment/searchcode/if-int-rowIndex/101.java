package pk.labs.Lab9.models;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import pk.labs.Lab9.beans.ConsultationList;
import javax.swing.table.AbstractTableModel;
import pk.labs.Lab9.beans.Consultation;

public class ConsultationListTableModel extends AbstractTableModel implements PropertyChangeListener {

    private ConsultationList consultations;
    private Map<Integer, JButton> buttons = new HashMap<Integer, JButton>();

    public ConsultationList getConsultations() {
        return consultations;
    }

    public void setConsultations(ConsultationList consultations) {
        if (this.consultations != null)
            this.consultations.removePropertyChangeListener(this);
        this.consultations = consultations;
        if (this.consultations != null)
            this.consultations.addPropertyChangeListener(this);
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Student";
            case 1:
                return "Początek";
            case 2:
                return "Koniec";
            case 3:
                return "Akcja";
        }
        return null;
    }
    
    @Override
    public int getRowCount() {
        if (consultations == null)
            return 0;
        return consultations.getSize();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (consultations == null)
            return null;
        Consultation consultation = consultations.getConsultation(rowIndex);
        switch (columnIndex) {
            case 0:
                return consultation.getStudent();
            case 1:
                return consultation.getBeginDate();
            case 2:
                return consultation.getEndDate();
            case 3:
                return getButton(rowIndex, consultation);
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 3;
    }

    private JButton getButton(int rowIndex, final Consultation consultation) {
        JButton button = buttons.get(rowIndex);
        if (button == null) {
            button = new JButton("Przedłuż");
            button.setName("action" + rowIndex);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String value = JOptionPane.showInputDialog("Wpisz o ile minut chcesz przedłużyć konsultacje", 15);
                    try {
                        int numberValue = Integer.parseInt(value);
                        consultation.prolong(numberValue);
                        if (numberValue > 0)
                            fireTableDataChanged();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Podałeś niepoprawną warość: '"+value+"'", "Błedny czas przedłużenia", JOptionPane.ERROR_MESSAGE);
                    } catch (PropertyVetoException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Błedny czas przedłużenia", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            buttons.put(rowIndex, button);
        }
        return button;
    }
    
    
}

