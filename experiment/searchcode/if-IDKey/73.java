package org.mypomodoro.gui;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.mypomodoro.gui.create.CreatePanel;

import org.mypomodoro.model.AbstractActivities;
import org.mypomodoro.model.Activity;

/**
 *
 * @author Phil Karoo
 */
public class ActivityEditTableListener implements ListSelectionListener {

    private final JTable table;
    private final CreatePanel panel;
    private final int idKey;
    private final AbstractActivities activities;

    public ActivityEditTableListener(AbstractActivities activities,
            JTable table, CreatePanel panel, int idKey) {
        this.activities = activities;
        this.table = table;
        this.panel = panel;
        this.idKey = idKey;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            Integer id = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), idKey);
            Activity activity = activities.getById(id);
            if (activity != null) {
                panel.fillOutInputForm(activity);
            }
        } else {
            panel.clearForm();
        }
    }
}

