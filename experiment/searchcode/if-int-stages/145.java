/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package klabergame;

import java.util.Random;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import klabergame.misc.Stages;

/**
 *
 * @author KisCsaládom
 */
public class Animations {

    private static final String helpText[] = {"Olyan színre próbálj kötni, amiből nálad van a húszas (felső) és mellette még egy-két másik lap ebből a színből.",
                                              "Ha ász a legnagyobb valamelyik színből, akkor se azt akard tromfnak, mert az ütőlap lehet. Kivétel, ha legalább 4 olyan színed van.",
                                              "Arra próbálj kötni, amiből a legtöbb kártyád van.",
                                              "Ne csak a kártyák darabszámát, hanem értékét is vedd figyelembe kötéskor. Kevesebb, de erősebb tromffal jobban jársz."};
    public static boolean isMenuPaneHidden;
    public static boolean isSettingsPaneHidden = true;
    private static boolean isScorePaneHidden = true;
    private static final long slideAnimation = 500;
    public static boolean isFinished=true;

    public static void slideMenu(AnchorPane menuPane) {
        int direction;
        int opacity;
        if (isMenuPaneHidden) {
            direction = -1;
            opacity = 1;
         //   System.out.println("ddd");
        } else {
            direction = 1;
            opacity = 0;
        }
        final Timeline timeline = new Timeline();
        final KeyValue kv1 = new KeyValue(menuPane.translateXProperty(), menuPane.getTranslateX() + direction * (menuPane.getWidth()));
        final KeyValue kv2 = new KeyValue(menuPane.opacityProperty(), opacity);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(slideAnimation), kv1, kv2);

        timeline.getKeyFrames().add(kf1);

        timeline.play();
        Sounds.playSound("src/sounds/newgame.mp3");
        isMenuPaneHidden = !isMenuPaneHidden;
    }

    public static void appearHelp(final AnchorPane helpPane, Label lblHelp, Stages stage, boolean helpOn) {
        
        FadeTransition fadeIn = FadeTransitionBuilder
                .create()
                .duration(new Duration(1000))
                .fromValue(0)
                .node(helpPane)
                .toValue(1)
                .cycleCount(1)
                .autoReverse(false)
                .build();

        FadeTransition fadeOut = FadeTransitionBuilder
                .create()
                .duration(new Duration(1000))
                .fromValue(1)
                .node(helpPane)
                .toValue(0)
                .cycleCount(1)
                .autoReverse(false)
                .onFinished(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent t) {
                        helpPane.setVisible(false);
                    }
                })
                .build();

        PauseTransition pause = new PauseTransition(Duration.millis(5000));
     //   System.out.println("help1");
        SequentialTransition seqT = new SequentialTransition(fadeIn, pause, fadeOut);
        Random rand = new Random();
        if (helpOn) {
            helpPane.setVisible(true);
            if (stage == Stages.START) {
                int x = rand.nextInt(4);
                lblHelp.setText(helpText[x]);
                seqT.play();
            } else if (stage == Stages.AFTER_TRUMP_SELECTION) {
                lblHelp.setText(helpText[0]);
                seqT.play();
            }
        } else {
            helpPane.setOpacity(0);
            helpPane.setVisible(false);
        }

    }

    public static void slideSettings(AnchorPane settingsPane) {
        int translateX;
        double newOpacity;
        double oldOpacity;
        if (isSettingsPaneHidden) {
            translateX = 5;
            newOpacity = 1.0;
            oldOpacity = 0.0;
        } else {
            translateX = -800;
            newOpacity = 0.0;
            oldOpacity = 1.0;
        }

        FadeTransition fade = FadeTransitionBuilder
                .create()
                .duration(new Duration(1000))
                .fromValue(oldOpacity)
                .node(settingsPane)
                .toValue(newOpacity)
                .cycleCount(1)
                .autoReverse(false)
                .build();
        TranslateTransition translate = TranslateTransitionBuilder
                .create()
                .duration(new Duration(1000))
                .node(settingsPane)
                .toX(translateX)
                .autoReverse(false)
                .cycleCount(1)
                .interpolator(Interpolator.EASE_IN)
                .build();

        ParallelTransition parallel = new ParallelTransition();
        parallel.getChildren().addAll(
                fade,
                translate
        );

        parallel.setCycleCount(1);
        parallel.play();
        
        if(FXMLKlaberController.isSoundOn) {
        Sounds.playSound("src/sounds/newgame.mp3");
        }
        isSettingsPaneHidden = !isSettingsPaneHidden;
    }
    
    public static void fadeOut(ImageView card) {
         FadeTransition fade = FadeTransitionBuilder
                .create()
                .duration(new Duration(1000))
                .fromValue(1)
                .node(card)
                 .delay(new Duration(4000))
                .toValue(0)
                 .onFinished(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent t) {
                       FXMLKlaberController.isDisabled = false;
                       
                    }
                })
                .autoReverse(false)
                .build();
         fade.play();
    }
    
     

   /* public void launchThis() {
        FXMLKlaberController fxml = new FXMLKlaberController();
        //slideScore(FXMLKlaberController.scorePane);
        fxml.resetGame(2);
    }*/
    public static void slideScore(AnchorPane scorePane) {
        int translateY;
        double newOpacity;
        double oldOpacity;
        if (isScorePaneHidden) {
            translateY = 0;
            newOpacity = 1.0;
            oldOpacity = 1.0;
        } else {
            translateY = -600;
            newOpacity = 1.0;
            oldOpacity = 1.0;
        }

        FadeTransition fade = FadeTransitionBuilder
                .create()
                .duration(new Duration(300))
                .fromValue(oldOpacity)
                .node(scorePane)
                .toValue(newOpacity)
                .cycleCount(1)
                .autoReverse(false)
                .build();
        TranslateTransition translate = TranslateTransitionBuilder
                .create()
                .duration(new Duration(1000))
                .node(scorePane)
                .toY(translateY)
                .autoReverse(false)
                .cycleCount(1)
                .interpolator(Interpolator.EASE_IN)
                .build();

        ParallelTransition parallel = new ParallelTransition();
        parallel.getChildren().addAll(
                fade,
                translate
        );

        parallel.setCycleCount(1);
        parallel.play();

        if(FXMLKlaberController.isSoundOn) {
        Sounds.playSound("src/sounds/newgame.mp3");
        }

        isScorePaneHidden = !isScorePaneHidden;
    }
    
    public static boolean getIsFinished(){
        return isFinished;
    }

    public static void slideCardIn(ImageView card) {
        TranslateTransition translate = TranslateTransitionBuilder
                .create()
                .duration(new Duration(1000))
                .node(card)
                .toY(-150)
                .toX(331 - card.getLayoutX() + 20)
                .autoReverse(false)
                .cycleCount(1)
                .interpolator(Interpolator.EASE_BOTH)
                .build();

        translate.play();
    }

    public static void slideCardIn2(ImageView card, int playerNumber) {
        
        int toX = 0, toY = 0, delay = 0;
        if (playerNumber == 2) {
            toX = -200;
            toY = 0;
            delay = 300;
        } else if (playerNumber == 3) {
            toX = 0;
            toY = 150;
            delay = 1000;
        } else if (playerNumber == 4) {
            toX = 200;
            toY = 0;
            delay = 1500;
        } else {
          //  System.out.println("Rossz playerNumber");
        }
        card.setOpacity(1);
        TranslateTransition translate = TranslateTransitionBuilder
                .create()
                .duration(new Duration(1000))
                .node(card)
                .toY(toY)
                .toX(toX)
                .delay(new Duration(delay))
                .autoReverse(false)
                .cycleCount(1)
                .interpolator(Interpolator.EASE_BOTH)
                .onFinished(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent t) {
                        isFinished = true;
                    }
                })
                .build();

        translate.play();
        card.setDisable(true);
    }

}

