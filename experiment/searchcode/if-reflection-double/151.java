package com.includio.interfaces.javafx;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.Reflection;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import com.includio.domain.model.tech.Category;
import com.includio.domain.model.tech.Level;
import com.includio.domain.model.tech.Tech;
import com.includio.interfaces.javafx.util.ServiceLocator;
import com.includio.interfaces.javafx.util.TechLocator;

public class MainPane extends Pane {

	private static final double CENTER_X = 250d;
	private static final double CENTER_Y = 250d;
	private static final double RADIUS = 50d;

	private NewTechPane newTechPane;
	private TechDetailPane techDetailPane;
	private CommentPane commentPane;
	private List<TechCircle> circles;
	private Group group;

	public MainPane() {
		init();
	}

	private void init() {

		List<Circle> levels = new ArrayList<Circle>();
		for (int i = 1; i < 5; i++) {
			Circle circle = new Circle(CENTER_X, CENTER_Y, i * RADIUS);
			circle.setSmooth(true);
			circle.setStroke(Color.GREEN);
			circle.setStrokeWidth(1);
			circle.setFill(null);
			levels.add(circle);
		}
		this.getChildren().addAll(levels);

		Line xAxis = new Line(25, 250, 475, 250);
		xAxis.setStroke(Color.WHITE);
		xAxis.setStrokeWidth(1);
		Line yAxis = new Line(250, 25, 250, 475);
		yAxis.setStroke(Color.WHITE);
		yAxis.setStrokeWidth(1);
		this.getChildren().addAll(xAxis, yAxis);

		Label technologyText = new Label(Category.TECHNOLOGY.toString());
		technologyText.setLayoutX(50);
		technologyText.setLayoutY(50);
		technologyText.setFont(Font.font("serif", FontWeight.BOLD, FontPosture.REGULAR, 12));
		final Reflection reflection = new Reflection();
		technologyText.setEffect(reflection);

		Label platformText = new Label(Category.PLATFORM.toString());
		platformText.setLayoutX(50);
		platformText.setLayoutY(400);
		platformText.setFont(Font.font("serif", FontWeight.BOLD, FontPosture.REGULAR, 12));
		platformText.setEffect(reflection);

		Label toolText = new Label(Category.TOOL.toString());
		toolText.setLayoutX(400);
		toolText.setLayoutY(50);
		toolText.setFont(Font.font("serif", FontWeight.BOLD, FontPosture.REGULAR, 12));
		toolText.setEffect(reflection);

		Label languageText = new Label(Category.LANGUAGE.toString());
		languageText.setLayoutX(400);
		languageText.setLayoutY(400);
		languageText.setFont(Font.font("serif", FontWeight.BOLD, FontPosture.REGULAR, 12));
		languageText.setEffect(reflection);

		this.getChildren().addAll(technologyText, platformText, toolText, languageText);

		Label holdLabel = new Label(Level.HOLD.toString());
		holdLabel.setLayoutX(252);
		holdLabel.setLayoutY(77);
		holdLabel.setFont(Font.font("serif", FontWeight.BOLD, FontPosture.REGULAR, 10));

		Label assessLabel = new Label(Level.ASSESS.toString());
		assessLabel.setLayoutX(252);
		assessLabel.setLayoutY(127);
		assessLabel.setFont(Font.font("serif", FontWeight.BOLD, FontPosture.REGULAR, 10));

		Label trialLabel = new Label(Level.TRIAL.toString());
		trialLabel.setLayoutX(252);
		trialLabel.setLayoutY(177);
		trialLabel.setFont(Font.font("serif", FontWeight.BOLD, FontPosture.REGULAR, 10));

		Label adoptLabel = new Label(Level.ADOPT.toString());
		adoptLabel.setLayoutX(252);
		adoptLabel.setLayoutY(227);
		adoptLabel.setFont(Font.font("serif", FontWeight.BOLD, FontPosture.REGULAR, 10));

		this.getChildren().addAll(holdLabel, assessLabel, trialLabel, adoptLabel);
		group = new Group();
		this.loadTechs();
		this.getChildren().add(group);

		this.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (event.getClickCount() == 2)
					if (TechLocator.isValid(event.getSceneX(), event.getSceneY()))
						showNewTechPane(event.getSceneX(), event.getSceneY());
			}
		});
	}

	private void showNewTechPane(double sceneX, double sceneY) {
		if (null != newTechPane)
			removeNewTechPane(newTechPane);
		newTechPane = new NewTechPane(sceneX, sceneY, this);
		this.getChildren().add(newTechPane);
	}

	public void removeNewTechPane(NewTechPane newTechPane) {
		this.getChildren().remove(newTechPane);
	}

	public void showTechDetailPane(Tech tech) {
		techDetailPane = new TechDetailPane(tech);
		this.getChildren().add(techDetailPane);
	}

	public void hideTechDetailPane() {
		techDetailPane.setVisible(false);
	}

	public void removeTechDetailPane() {
		this.getChildren().remove(techDetailPane);
	}

	public void showCommentPane(double sceneX, double sceneY, Tech tech) {
		commentPane = new CommentPane(sceneX, sceneY, tech, this);
		this.getChildren().add(commentPane);
	}

	public void removeCommentPane() {
		this.getChildren().remove(commentPane);
	}

	public void loadTechs() {
		group.getChildren().clear();
		List<Tech> teches = ServiceLocator.techRepository().findAll();
		circles = new ArrayList<TechCircle>();
		for (Tech each : teches)
			circles.add(new TechCircle(each, this));
		group.getChildren().addAll(circles);
	}

	public TechDetailPane techDetailPane() {
		return this.techDetailPane;
	}
}

