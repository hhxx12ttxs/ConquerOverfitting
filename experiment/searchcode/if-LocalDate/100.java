/*
 * Copyright (c) 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ensemble.samples.controls.datepicker;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Locale;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * A sample that demonstrates the DatePicker. The sample uses a new Stage for
 * the DatePicker to allow changing of locales. 
 *
 * @sampleName DatePicker
 * @preview preview.png
 * @see javafx.scene.control.DateCell
 * @see javafx.scene.control.DatePicker
 */
public class DatePickerApp extends Application {

    private final static ObservableList<String> locales = FXCollections.observableArrayList();
    private DatePicker datePicker;
    private MenuBar datePickerMenuBar;
    private final LocalDate today = LocalDate.now();
    private final LocalDate tomorrow = today.plusDays(1);
    private Locale originalDefault;
    private Stage myStage;
    private Stage primStage;
    static {
        locales.addAll(new String[]{
            "en_US",
            "ar_SA",
            "en_GB",
            "cs_CZ",
            "el_GR",
            "he_IL",
            "hi_IN",
            "ja_JP",
            "ja_JP-u-ca-japanese",
            "ru_RU",
            "sv_SE",
            "th_TH",
            "th-TH-u-ca-buddhist",
            "th-TH-u-ca-buddhist-nu-thai",
            "zh_CN",
            "en-US-u-ca-islamic-umalqura",
            "ar-SA-u-ca-islamic-umalqura",
            "en-u-ca-japanese-nu-thai"
        });
    }

    public Parent createContent() {
        VBox buttonVbox = new VBox(8);
        buttonVbox.setAlignment(Pos.CENTER);               
        // Use a Button to create a new Stage that gives us an environment to switch Locales
        final ToggleButton button = new ToggleButton("Open a  new Stage for DatePicker");
        button.setPrefWidth(290);
        button.setMaxWidth(ToggleButton.USE_PREF_SIZE);
        button.setMinWidth(ToggleButton.USE_PREF_SIZE);
        final ToggleButton closeButton = new ToggleButton("Close DatePicker Stage");
        closeButton.setPrefWidth(290);
        closeButton.setMaxWidth(ToggleButton.USE_PREF_SIZE);
        closeButton.setMinWidth(ToggleButton.USE_PREF_SIZE);
        
        ToggleGroup group = new ToggleGroup();
        button.setToggleGroup(group);
        closeButton.setToggleGroup(group);

        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle selectedToggle) {
                if ((ToggleButton) selectedToggle != null) {
                    if (((ToggleButton) selectedToggle).getText().equals("Open a  new Stage for DatePicker")) {
                        if (myStage == null) {
                            myStage = new Stage();
                            Group rootGroup = new Group();
                            Scene scene = new Scene(rootGroup, 300, 200);
                            myStage.setScene(scene);
                            myStage.centerOnScreen();
                            myStage.initOwner(primStage);
                            myStage.show();
                            rootGroup.getChildren().add(createDatePickerSceneContent(myStage));                           
                        }
                    } else {
                        if (myStage != null) {
                            myStage.close();
                            myStage = null;
                            if (originalDefault != null) {
                                Locale.setDefault(originalDefault);
                            }
                            button.setDisable(false);
                        }
                    }
                }
            }
        });

        buttonVbox.getChildren().addAll(button, closeButton);
        return buttonVbox;
    }
    
    private Parent createDatePickerSceneContent(Stage inStage) {
        Text datePickerText = new Text("Date:");
        datePicker = new DatePicker();       
        // day cell factory
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(today)) {
                            setStyle("-fx-background-color: #8099ff;");
                        } else {
                            if (item.equals(tomorrow)) {
                                setTooltip(new Tooltip("Tomorrow is important"));
                            }
                        }
                    }
                };
            }
        };
        //Create the menubar to experiment with the DatePicker
        datePickerMenuBar = createMenuBar(inStage, dayCellFactory);
        // Listen for DatePicker actions
        datePicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                LocalDate isoDate = datePicker.getValue();
                if ((isoDate != null) && (!isoDate.equals(LocalDate.now()))) {
                    for (Menu menu : datePickerMenuBar.getMenus()) {
                        if (menu.getText().equals("Options for Locale")) {
                            for (MenuItem menuItem : menu.getItems()) {
                                if (menuItem.getText().equals("Set date to today")) {
                                    if ((menuItem instanceof CheckMenuItem) && ((CheckMenuItem) menuItem).isSelected()) {
                                        ((CheckMenuItem) menuItem).setSelected(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });


        
        HBox hbox = new HBox(18);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(datePickerText, datePicker);
        
        VBox vbox = new VBox(22);
        vbox.getChildren().addAll(datePickerMenuBar, hbox);
        vbox.setPrefSize(300, 200);
        vbox.setMinSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        return vbox;
    }

    private MenuBar createMenuBar(final Stage inStage, final Callback<DatePicker, DateCell> dayCellFac) {
        final MenuBar menuBar = new MenuBar();
        final ToggleGroup localeToggleGroup = new ToggleGroup();
        // Locales
        Menu localeMenu = new Menu("Locales");
        Iterator<String> localeIterator = locales.iterator();
        while (localeIterator.hasNext()) {
            RadioMenuItem localeMenuItem = new RadioMenuItem(localeIterator.next());
            localeMenuItem.setToggleGroup(localeToggleGroup);
            localeMenu.getItems().add(localeMenuItem);
        }

        Menu optionsMenu = new Menu("Options for Locale");
        //Style DatePicker with cell factory
        final CheckMenuItem cellFactoryMenuItem = new CheckMenuItem("Use cell factory to color past days and add tooltip to tomorrow");
        optionsMenu.getItems().add(cellFactoryMenuItem);
        cellFactoryMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (cellFactoryMenuItem.isSelected()) {
                    datePicker.setDayCellFactory(dayCellFac);
                } else {
                    datePicker.setDayCellFactory(null);
                }
            }
        });
                       
        //Set date to today
        final CheckMenuItem todayMenuItem = new CheckMenuItem("Set date to today");
        optionsMenu.getItems().add(todayMenuItem);
        todayMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                if (todayMenuItem.isSelected()) {
                    datePicker.setValue(today);
                }
            }
        });

        //Set date to today
        final CheckMenuItem showWeekNumMenuItem = new CheckMenuItem("Show week numbers");
        optionsMenu.getItems().add(showWeekNumMenuItem);
        showWeekNumMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                datePicker.setShowWeekNumbers(showWeekNumMenuItem.isSelected());
            }
        });        
        
        localeToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle) {
                if (localeToggleGroup.getSelectedToggle() != null) {
                    String selectedLocale = ((RadioMenuItem) localeToggleGroup.getSelectedToggle()).getText();
                    Locale locale = Locale.forLanguageTag(selectedLocale.replace('_', '-'));
                    if (originalDefault == null) { //save original default Locale for restoration later
                        originalDefault = Locale.getDefault();
                    }
                    Locale.setDefault(locale);
                   // sampleContent = createDatePickerSceneContent(inStage);
                    inStage.setScene(new Scene(createDatePickerSceneContent(inStage)));
                    inStage.show();
                }
            }
        });       

        menuBar.getMenus().addAll(localeMenu, optionsMenu);
        return menuBar;
    }    
 
    @Override
    public void stop() {
        if (originalDefault != null) {
            Locale.setDefault(originalDefault);
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception { 
        primStage = primaryStage;
        primaryStage.setScene(new Scene(createContent()));      
        primaryStage.show();
    }
    
    /**
     * Java main for when running without JavaFX launcher
     */
    public static void main(String[] args) {
        launch(args);
    }
}

