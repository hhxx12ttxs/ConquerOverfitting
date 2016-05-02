/*
 * Copyright (c) 2014 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;


/**
 * User: hansolo
 * Date: 28.03.14
 * Time: 10:24
 */
public class DemoPaperFold extends Application {
    private int             duration;
    private PixelReader     pixelReader;
    private WritableImage   image;
    private int             noOfTiles;
    private double          stepSize;
    private List<ImageView> imageViews;
    private InnerShadow     shadowLeft;
    private InnerShadow     shadowRight;
    private DoubleProperty  inset;
    private double          insetValue;
    private Timeline        timelineHide;
    private Timeline        timelineShow;
    private Pane            imagePane;

    private AnchorPane pane;

    @Override public void init() {
        duration    = 500;        
        noOfTiles   = 5;  
        insetValue  = 10;
        imageViews  = new ArrayList<>(noOfTiles);
        inset       = new SimpleDoubleProperty(this, "inset", 0);
        shadowLeft  = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.1), 0, 0, 0, 0);
        shadowRight = new InnerShadow(BlurType.TWO_PASS_BOX, Color.rgb(0, 0, 0, 0.1), 0, 0, 0, 0);
        
        shadowLeft.radiusProperty().bind(inset.multiply(5));
        shadowLeft.offsetXProperty().bind(inset.multiply(1));

        shadowRight.radiusProperty().bind(inset.multiply(5));
        shadowRight.offsetXProperty().bind(inset.multiply(-1));

        timelineHide = new Timeline();
        timelineShow = new Timeline();

        for (int x = 0; x < noOfTiles; x++) {
            imageViews.add(new ImageView());
        }
        imagePane = new Pane();
        imagePane.getChildren().setAll(imageViews);
        imagePane.setVisible(false);
        
        initInterface();
    }

    @Override public void start(Stage stage) {
        Button showButton = new Button("show");
        showButton.setOnAction(event1 -> {
            timelineShow.setOnFinished(event2 -> {
                pane.setVisible(true);
                imagePane.setVisible(false);
            });
            timelineShow.play();    
        });
        showButton.toBack();
        
        StackPane root = new StackPane();
        root.getChildren().addAll(showButton, pane, imagePane);

        Scene scene = new Scene(root);
        scene.setCamera(new PerspectiveCamera());

        stage.setScene(scene);
        stage.show();        
    }

    private void preparePaperfold(final Node NODE) {
        SnapshotParameters param = new SnapshotParameters();
        param.setDepthBuffer(true);
        param.setViewport(new Rectangle2D(NODE.getBoundsInParent().getMinX(), NODE.getBoundsInParent().getMinY(), NODE.getBoundsInParent().getWidth(), NODE.getBoundsInParent().getHeight()));
        image = NODE.snapshot(param, null);
        stepSize = image.getWidth() / noOfTiles;
        splitImage(image);
        initTimelineHide();
        initTimelineShow();
    }

    private void splitImage(final Image IMAGE) {                        
        pixelReader = IMAGE.getPixelReader();
        for (int i = 0; i < noOfTiles; i++) {            
            Image tile = new WritableImage(pixelReader, (int) (i * stepSize), 0, (int) stepSize, (int) IMAGE.getHeight());
            
            // Update the imageviews
            imageViews.get(i).getTransforms().clear();            
            imageViews.get(i).setImage(tile);

            // Position image views
            imageViews.get(i).setTranslateX(i * stepSize);
            imageViews.get(i).setCache(true);
            imageViews.get(i).setCacheHint(CacheHint.SPEED);
            
            // Add perspective transforms
            PerspectiveTransform transform = new PerspectiveTransform();
            if (i % 2 == 0) {
                // UpperLeft
                transform.setUlx(imageViews.get(i).getLayoutX());
                transform.setUly(0);

                // LowerLeft
                transform.setLlx(imageViews.get(i).getLayoutX());
                transform.setLly(image.getHeight());

                // UpperRight
                transform.setUrx(imageViews.get(i).getLayoutX() + stepSize);
                transform.uryProperty().bind(inset);

                // LowerRight
                transform.setLrx(imageViews.get(i).getLayoutX() + stepSize);
                transform.lryProperty().bind(inset.negate().add(image.getHeight()));

                shadowRight.setInput(transform);
                imageViews.get(i).setEffect(shadowRight);
            } else {
                // UpperLeft
                transform.setUlx(imageViews.get(i).getLayoutX());
                transform.ulyProperty().bind(inset);

                // LowerLeft
                transform.setLlx(imageViews.get(i).getLayoutX());
                transform.llyProperty().bind(inset.negate().add(image.getHeight()));

                // UpperRight
                transform.setUrx(imageViews.get(i).getLayoutX() + stepSize);
                transform.setUry(0);

                // LowerRight
                transform.setLrx(imageViews.get(i).getLayoutX() + stepSize);
                transform.setLry(image.getHeight());

                shadowLeft.setInput(transform);
                imageViews.get(i).setEffect(shadowLeft);
            }
        }
    }
                
    private void initTimelineHide() {
        KeyValue kv0 = new KeyValue(inset, 0);
        KeyValue kv1 = new KeyValue(inset, insetValue);
        KeyValue kv2 = new KeyValue(imagePane.scaleXProperty(), 1);
        KeyValue kv3 = new KeyValue(imagePane.scaleXProperty(), 0);
        KeyValue kv4 = new KeyValue(imagePane.translateXProperty(), -pane.getWidth() * 0.5);
        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0, kv2);
        KeyFrame kf1 = new KeyFrame(Duration.millis(duration), kv1, kv3, kv4);
        timelineHide.getKeyFrames().setAll(kf0, kf1);
    }

    private void initTimelineShow() {
        KeyValue kv0 = new KeyValue(inset, insetValue);
        KeyValue kv1 = new KeyValue(inset, 0);
        KeyValue kv2 = new KeyValue(imagePane.scaleXProperty(), 0);
        KeyValue kv3 = new KeyValue(imagePane.scaleXProperty(), 1);
        KeyValue kv4 = new KeyValue(imagePane.translateXProperty(), 0);
        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0, kv2);
        KeyFrame kf1 = new KeyFrame(Duration.millis(duration), kv1, kv3, kv4);
        timelineShow.getKeyFrames().setAll(kf0, kf1);
    }
    
    private void initInterface() {
        GridPane grid = new GridPane();
        grid.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(20);
        grid.add(new Label("Paperfolding ist fun"), 0, 0);
        grid.add(new TextField(), 0, 1);
        grid.add(new Label("Doesn't look that bad... :)"), 0, 2);
        Button hideButton = new Button("Hide");
        hideButton.setOnAction(event -> {
            preparePaperfold(pane);
            imagePane.setVisible(true);
            pane.setVisible(false);            
            timelineHide.play();
        });
        grid.add(hideButton, 0, 3);        
        
        pane = new AnchorPane();        
        pane.getChildren().add(grid);
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
}

