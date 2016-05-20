package org.mypomodoro.gui.create;

import org.mypomodoro.gui.create.list.TypeComboBox;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.JXDatePicker;
import org.mypomodoro.gui.ControlPanel;
import org.mypomodoro.gui.create.list.AuthorComboBox;
import org.mypomodoro.gui.create.list.PlaceComboBox;
import org.mypomodoro.model.Activity;
import org.mypomodoro.util.ColorUtil;
import org.mypomodoro.util.DateUtil;
import org.mypomodoro.util.Labels;

public class ActivityInputForm extends JPanel {

    private static final long serialVersionUID = 20110814L;
    protected static final Dimension PANEL_DIMENSION = new Dimension(400, 200);
    protected static final Dimension TEXT_AREA_DIMENSION = new Dimension(300, 50);
    protected static final Dimension TEXT_FIELD_DIMENSION = new Dimension(300, 25);
    protected static final Dimension COMBO_BOX_DIMENSION = new Dimension(300, 25);
    protected final GridBagConstraints c = new GridBagConstraints();
    protected final JTextField nameField = new JTextField();
    protected final JTextArea descriptionField = new JTextArea();
    protected JComboBox estimatedPomodoros = new JComboBox();
    protected JComboBox types = new JComboBox();
    protected JComboBox authors = new JComboBox();
    protected JComboBox places = new JComboBox();
    protected final JXDatePicker datePicker = new JXDatePicker(
            Labels.getLocale());
    protected int activityId = -1;

    public ActivityInputForm() {
        this(0);
    }

    public ActivityInputForm(int gridy) {
        setBorder(new TitledBorder(new EtchedBorder(), ""));
        setMinimumSize(PANEL_DIMENSION);
        setPreferredSize(PANEL_DIMENSION);
        setLayout(new GridBagLayout());

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;

        addForm(gridy);
    }

    protected void addForm(int gridy) {
        addDate(gridy);
        addName(++gridy);
        addEstimatedPoms(++gridy);
        addType(++gridy);
        addAuthor(++gridy);
        addPlace(++gridy);
        addDescription(++gridy);
    }

    protected void addDate(int gridy) {
        final FormLabel dateLabel = new FormLabel(
                Labels.getString("Common.Date") + "*: ");
        datePicker.setDate(new Date());
        c.gridx = 0;
        c.gridy = gridy;
        c.weighty = 0.5;
        add(dateLabel, c);
        c.gridx = 1;
        c.gridy = gridy;
        c.weighty = 0.5;
        add(datePicker, c);
    }

    protected void addName(int gridy) {
        // Name Label and Text Field
        ++gridy;
        c.gridx = 0;
        c.gridy = gridy;
        c.weighty = 0.5;
        add(new FormLabel(Labels.getString("Common.Title") + "*: "), c);
        c.gridx = 1;
        c.gridy = gridy;
        c.weighty = 0.5;
        addTextField(nameField);
    }

    protected void addEstimatedPoms(int gridy) {
        // init estimated Pomodoros combo box
        String items[] = new String[ControlPanel.preferences.getMaxNbPomPerActivity()];
        for (int i = 0; i < ControlPanel.preferences.getMaxNbPomPerActivity(); i++) {
            items[i] = (i + 1) + "";
        }
        estimatedPomodoros = new JComboBox(items);
        // Estimated Poms Description and TextField
        ++gridy;
        c.gridx = 0;
        c.gridy = gridy;
        c.weighty = 0.5;
        add(new FormLabel(Labels.getString("Common.Estimated pomodoros")
                + "*: "), c);
        c.gridx = 1;
        c.gridy = gridy;
        c.weighty = 0.5;
        estimatedPomodoros.setBackground(ColorUtil.WHITE);
        add(estimatedPomodoros, c);
    }

    protected void addType(int gridy) {
        types = new TypeComboBox();
        // Type Label and Combo box        
        ++gridy;
        c.gridx = 0;
        c.gridy = gridy;
        c.weighty = 0.5;
        add(new FormLabel(Labels.getString("Common.Type") + ": "), c);
        c.gridx = 1;
        c.gridy = gridy;
        c.weighty = 0.5;
        types.setBackground(ColorUtil.WHITE);
        types.setMinimumSize(COMBO_BOX_DIMENSION);
        types.setPreferredSize(COMBO_BOX_DIMENSION);
        types.setEditable(true);
        types.setFont(new Font(types.getFont().getName(), Font.PLAIN, types.getFont().getSize()));
        add(types, c);
    }

    protected void addAuthor(int gridy) {
        authors = new AuthorComboBox();
        // Author Label and Combo box
        ++gridy;
        c.gridx = 0;
        c.gridy = gridy;
        c.weighty = 0.5;
        add(new FormLabel(Labels.getString("Common.Author") + ": "), c);
        c.gridx = 1;
        c.gridy = gridy;
        c.weighty = 0.5;
        authors.setBackground(ColorUtil.WHITE);
        authors.setMinimumSize(COMBO_BOX_DIMENSION);
        authors.setPreferredSize(COMBO_BOX_DIMENSION);
        authors.setEditable(true);
        authors.setFont(new Font(authors.getFont().getName(), Font.PLAIN, authors.getFont().getSize()));
        add(authors, c);
    }

    protected void addPlace(int gridy) {
        places = new PlaceComboBox();
        // Place label and Combo box
        ++gridy;
        c.gridx = 0;
        c.gridy = gridy;
        c.weighty = 0.5;
        c.weightx = 0.0;
        add(new FormLabel(Labels.getString("Common.Place") + ": "), c);
        c.gridx = 1;
        c.gridy = gridy;
        c.weighty = 0.5;
        places.setBackground(ColorUtil.WHITE);
        places.setMinimumSize(COMBO_BOX_DIMENSION);
        places.setPreferredSize(COMBO_BOX_DIMENSION);
        places.setEditable(true);
        places.setFont(new Font(places.getFont().getName(), Font.PLAIN, places.getFont().getSize()));
        add(places, c);
    }

    protected void addDescription(int gridy) {
        // Description Label and TextArea
        ++gridy;
        c.gridx = 0;
        c.gridy = gridy;
        c.weighty = 0.5;
        add(new FormLabel(Labels.getString("Common.Description") + ": "), c);
        c.gridx = 1;
        c.gridy = gridy;
        c.weighty = 0.5;
        descriptionField.setFont(this.getFont());
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        JScrollPane description = new JScrollPane(descriptionField);
        description.setMinimumSize(TEXT_AREA_DIMENSION);
        description.setPreferredSize(TEXT_AREA_DIMENSION);
        description.setFont(getFont());
        add(description, c);
    }

    protected void addTextField(JTextField field) {
        field.setMinimumSize(TEXT_FIELD_DIMENSION);
        field.setPreferredSize(TEXT_FIELD_DIMENSION);
        add(field, c);
    }

    /**
     * Returns a new activity from the class fields and null if there was an
     * error while parsing the fields
     *
     * @return activity
     */
    public Activity getActivityFromFields() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String type = (String) types.getSelectedItem();
        type = type != null ? type.trim() : "";
        String author = (String) authors.getSelectedItem();
        author = author != null ? author.trim() : "";
        String place = (String) places.getSelectedItem();
        place = place != null ? place.trim() : "";
        int estimatedPoms = estimatedPomodoros.getSelectedIndex() + 1;
        Date dateActivity = datePicker.getDate();

        return new Activity(place, author, name, description, type,
                estimatedPoms, dateActivity, activityId);
    }

    /*
     * Setters
     */
    public void setNameField(String value) {
        nameField.setText(value);
    }

    public void setDescriptionField(String value) {
        descriptionField.setText(value);
    }

    public void setTypeField(String type) {
        types.setSelectedItem(type);
    }

    public void setType(int index) {
        types.setSelectedIndex(index);
    }

    public void setAuthorField(String author) {
        authors.setSelectedItem(author);
    }

    public void setAuthor(int index) {
        authors.setSelectedIndex(index);
    }

    public void setPlaceField(String place) {
        places.setSelectedItem(place);
    }

    public void setPlace(int index) {
        places.setSelectedIndex(index);
    }

    public void setEstimatedPomodoro(int value) {
        value = value > ControlPanel.preferences.getMaxNbPomPerActivity() ? ControlPanel.preferences.getMaxNbPomPerActivity() : value;
        estimatedPomodoros.setSelectedIndex(value - 1);
    }

    public void setDate(Date value) {
        datePicker.setDate(value);
    }

    public void setActivityId(int value) {
        activityId = value;
    }

    public boolean isDateToday() {
        String datePickerFormat = DateUtil.getFormatedDate(datePicker.getDate());
        String todayFormat = DateUtil.getFormatedDate(new Date());
        return datePickerFormat.equalsIgnoreCase(todayFormat);
    }
}

