/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jfxnui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import javafx.animation.FadeTransition;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 *
 * @author 
 */
public class JFXNUIBeta extends Application implements EventHandler<WindowEvent> {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public boolean bstart = true;
    public Process child;
    public final TextArea text = new TextArea();
    public Thread console;
    public TextField TFNode = new TextField();
    public TextField TFScript = new TextField();
    public TextField TFHost = new TextField();
    public TextField TFLink = new TextField();
    public TextField TFConsole = new TextField();
    private Properties prop = new Properties();
    public Stage MainStage;
    public Stage SplashStage;
    public GridPane grid = new GridPane();
    public ArrayList<Stage> stages= new ArrayList<Stage>();
    
    private Pane splashLayout;
    private ProgressBar loadProgress;
    private Label progressText;
    
    private static final int SPLASH_WIDTH = 676;
    private static final int SPLASH_HEIGHT = 227;
    
    @Override  
    public void stop() throws Exception {  
        System.out.println("Stopping: stop()");
        if(child !=null){
            child.destroy();
            //System.out.println("clear");
        }else{
            //System.out.println("fail");
        }
    }  
   
    @Override  
    public void handle(WindowEvent event) {  
        System.out.println("Stopping: handle()");  
    }
    
    @Override public void init() {
        System.out.println("init...");
        ImageView splash = new ImageView(new Image(getClass().getResourceAsStream("splashscreen.png")));
        loadProgress = new ProgressBar();
        loadProgress.setPrefWidth(splash.getImage().getWidth());
        progressText = new Label("Loading . . .");
        splashLayout = new VBox();
        splashLayout.getChildren().addAll(splash, loadProgress, progressText);
        progressText.setAlignment(Pos.CENTER);
        splashLayout.setEffect(new DropShadow());
    }
    
    @Override
    public void start(Stage primaryStage) {
        //MainStage = primaryStage;
        //showSplash(primaryStage);
        showMain();
        MainStage.show();
        
        /*
        //FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
        FadeTransition fadeSplash = new FadeTransition(Duration.seconds(2), splashLayout);
          fadeSplash.setFromValue(1.0);
          fadeSplash.setToValue(0.0);
          fadeSplash.setOnFinished(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
              //System.out.println("init... trigger");
                SplashStage.hide();
                MainStage.show();
            }
          });
          fadeSplash.play();
        */
    }
    
    private void showMain() {
        MainStage = new Stage(StageStyle.DECORATED);
        System.out.println("...");
        System.out.println(MainStage);
        
        try {
               //load a properties file
    		prop.load(new FileInputStream("config.properties"));
 
               //get the property value and print it out
                System.out.println(prop.getProperty("nodepath"));
    		System.out.println(prop.getProperty("scriptpath"));
    		System.out.println(prop.getProperty("host"));
                
                TFNode.setText(prop.getProperty("nodepath"));
                TFScript.setText(prop.getProperty("scriptpath"));
                TFHost.setText(prop.getProperty("host"));
 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
        //System.out.println(com.sun.javafx.runtime.VersionInfo.getVersion());

        Label LNode = new Label("Nodejs: ");
        Label LScript = new Label("Script: ");
        text.setStyle("-fx-background-color: lightgray;");
        text.setEditable(false);
        BorderPane root = new BorderPane();
        ToolBar toolBar = new ToolBar();
        root.setTop(toolBar);
        root.setCenter(text);
        
        TFConsole.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override  public void handle(KeyEvent inputevent) {
              //System.out.println("typeing...!\n");
              //System.out.println(inputevent.getCharacter());
              /*
              if (!inputevent.getCharacter().matches("\\d")) {              
                           inputevent.consume();
              }
              */
              /*
              if (!inputevent.getCharacter().matches("\n")) {              
                           System.out.println("return key!");
              }
              */
            }
        });
        
        
        root.setBottom(TFConsole);
        
        Region spacer = new Region();
        spacer.getStyleClass().setAll("spacer");
        
        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().setAll("segmented-button-bar");

        
        final MenuButton MBServer = new MenuButton("Server");
        
        MBServer.setStyle("-fx-background-radius: 0, 0;");
        MenuItem MStart = new MenuItem("Start");
        
        MStart.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                runserver();
                System.out.println("start server");
            }
        });  
        
        MenuItem MIStop = new MenuItem("Stop");
        MIStop.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                stopserver();
                System.out.println("stop server");      
            }
        });  
        
        MenuItem MIClearlog = new MenuItem("Clear Log");
        
        MIClearlog.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                    System.out.println("clear log");  
                    text.setText("");
            }
        }); 
        
        MBServer.getItems().add(MStart);
        MBServer.getItems().add(MIStop);
        MBServer.getItems().add(MIClearlog);
        
        buttonBar.getChildren().add(MBServer);
        buttonBar.getChildren().add(LNode);
        buttonBar.getChildren().add(TFNode);
        buttonBar.getChildren().add(LScript);
        buttonBar.getChildren().add(TFScript);
        
        final MenuButton MBBrowser = new MenuButton("Browser");
        
        MenuItem MICNewBrowser = new MenuItem("New Browser");
        
        MICNewBrowser.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                    System.out.println("clear log");  
                    text.appendText("\nNew browser");
                    newbrowser();
            }
        });
        
        MenuItem MICloseAllBrowsers = new MenuItem("Close all");
        
        MICloseAllBrowsers.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                    System.out.println("clear log");
                    text.appendText("\nAll web browsers closed.");
                    clearallwebs();
            }
        }); 
        MBBrowser.getItems().add(MICNewBrowser);
        MBBrowser.getItems().add(MICloseAllBrowsers);        
        buttonBar.getChildren().add(MBBrowser);
        
        toolBar.getItems().addAll(spacer, buttonBar);
        
        //Scene scene = new Scene(root, 640, 280);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("segmented.css").toExternalForm());
        MainStage.setTitle("JFXNUI Beta");
        MainStage.setScene(scene);
    }
    
    private void newbrowser(){
        final Stage webStage = new Stage(StageStyle.UTILITY);
        webStage.setTitle("Web View");
        Scene scene = new Scene(new Browser(),750,500, Color.web("#666970"));
        webStage.setScene(scene);
        webStage.show();
        stages.add(webStage);
    }
    
    
    private void clearallwebs(){
        
        while(stages.size() > 0){
        
            for (int i = 0;i < stages.size();i++){
                //stages.get(i).hide();
                stages.get(i).close();
                stages.remove(i);
                break;
            }
        }
    }
    
    
    private void runserver() {
        //System.out.println("run!");
                if(child == null ){
                    text.appendText("\n"+"init server...");
                    console = new Thread(new Runnable() {
                        @Override public void run() {
                            
                            try {
                            String line;
                            String[] commands ={prop.getProperty("nodepath"),prop.getProperty("scriptpath")};
                            
                            System.out.println(TFScript.getText());
                            child = Runtime.getRuntime().exec(commands);

                            BufferedReader bri = new BufferedReader
                                (new InputStreamReader(child.getInputStream()));
                            BufferedReader bre = new BufferedReader
                                (new InputStreamReader(child.getErrorStream()));

                            while ((line = bri.readLine()) != null) {
                                System.out.println(line);
                                String str = line.toString();
                                String[] lines = str.split("\n");
                                for(int i = 0;i < lines.length;i++){
                                    text.appendText("\n"+lines[i]);
                                }
                            }

                            bri.close();
                            
                            while ((line = bre.readLine()) != null) {
                                System.out.println(line);
                                text.appendText("\n"+line);
                            }
                            bre.close();
                            
                            child.waitFor();
                            System.out.println("Done.");

                        }
                        catch (Exception err) {
                            err.printStackTrace();
                        }
                    }
                    });
                    
                    Runtime.getRuntime().addShutdownHook(console); 
                    console.start();
                    }else{
                    text.appendText("\n"+"server running...");
                    }
            
    }
    
    private void stopserver(){
        if(child !=null){
                    child.destroy();
                    child = null;
                    //System.out.println("clear");
                    text.appendText("\n"+"server stop!");
                }else{
                    //System.out.println("fail");
                    text.appendText("\n"+"fail to stop server!");
                }
        
    }
    
    private void showSplash(Stage initStage) {
        Scene splashScene = new Scene(splashLayout);
        initStage.initStyle(StageStyle.UNDECORATED);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        //initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        //initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.show();
        SplashStage = initStage;
    }
}

class Browser extends Region {
   private HBox toolBar;
   private Properties _prop = new Properties();

   private static String[] imageFiles = new String[]{
        "product.png",
        "product.png",
        "product.png",
        "product.png"
    };
    private static String[] captions = new String[]{
        "Home",
        "Blogs",
        "Forums",
        "Partners"
    };
 
    private static String[] urls;
 
    final ImageView selectedImage = new ImageView();
    final Hyperlink[] hpls = new Hyperlink[captions.length];
    final Image[] images = new Image[imageFiles.length];
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    private TextField TFNodeUrl = new TextField();
 
    public Browser() {
        
        //load a properties file
        try{
            _prop.load(new FileInputStream("config.properties"));
            System.out.println(_prop.getProperty("host"));
            
            urls = new String[]{
            _prop.getProperty("host"),
            _prop.getProperty("host"),
            _prop.getProperty("host"),
            _prop.getProperty("host")
        };
            
            
        } catch (IOException ex) {
    		ex.printStackTrace();
        }

        //apply the styles
        getStyleClass().add("browser");
 
        // load the home page        
        webEngine.load(_prop.getProperty("host"));
        
        for (int i = 0; i < captions.length; i++) {
            final Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
            Image image = images[i] =
                new Image(getClass().getResourceAsStream(imageFiles[i]));
            hpl.setGraphic(new ImageView (image));
            final String url = urls[i];
 
            hpl.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    webEngine.load(url);                    
                }
            });
        }        
        
        Button btnSetUrl = new Button("Url");
        btnSetUrl.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //System.out.println("Hello World!");
                webEngine.load(TFNodeUrl.getText());
            }
        });
        
        // create the toolbar
        toolBar = new HBox();
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().addAll(hpls);
        toolBar.getChildren().add(TFNodeUrl);
        toolBar.getChildren().add(btnSetUrl);
        
    
        //add components
        getChildren().add(toolBar);
        getChildren().add(browser); 
    }
 
    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }
 
    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser,0,0,w,h-tbHeight,0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar,0,h-tbHeight,w,tbHeight,0,HPos.CENTER,VPos.CENTER);
    }
 
    @Override protected double computePrefWidth(double height) {
        return 750;
    }
 
    @Override protected double computePrefHeight(double width) {
        return 500;
    }
}

