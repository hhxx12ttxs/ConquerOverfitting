package com.ja.raspicar.fx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Carousel {

	private static final Logger LOG = LoggerFactory.getLogger(Carousel.class);

	private HBox hbox;

	private List<ImageHolder> images = Collections
			.synchronizedList(new ArrayList<ImageHolder>());

	private int pos;

	private DoubleProperty imgWidth;
	
	private Rectangle clip;

	public Carousel(final HBox hbox, DoubleProperty imgWidth,
			final DoubleProperty clipWidth, final DoubleProperty clipHeight) {
		this.hbox = hbox;
		this.imgWidth = imgWidth;
		hbox.getChildren().add(createEmtpyImageView(imgWidth));
		hbox.getChildren().add(createEmtpyImageView(imgWidth));
		hbox.getChildren().add(createEmtpyImageView(imgWidth));
		clip = new Rectangle();
		clip.widthProperty().bind(clipWidth);
		clip.heightProperty().bind(clipHeight);
		hbox.widthProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number newValue) {
				int x = (int) (hbox.getWidth() / 2 - clipWidth.get() / 2);
				clip.translateXProperty().set(x);
			}
		});

		clip.setEffect(createBlur());
		hbox.setClip(clip);
	}

	public Rectangle getClip() {
		return clip;
	}
	
	protected Effect createBlur() {
		/* Currently not supported by JDK8 for ARM */
		BoxBlur boxBlur = new BoxBlur();
		boxBlur.setWidth(50);
		boxBlur.setHeight(10);
		boxBlur.setIterations(3);
		return boxBlur;

	}

	private ImageView createEmtpyImageView(DoubleProperty imgWidth) {
		ImageView imgView = new ImageView();
		imgView.setPreserveRatio(true);
		imgView.fitWidthProperty().bind(imgWidth);
		imgView.setCache(true);
		return imgView;
	}

	public void addImage(String name) {
		if (isEvenSize(images)) {
			hbox.getChildren().remove(hbox.getChildren().size() - 1);
		}
		Image img = new Image(getClass().getResourceAsStream(name));
		String key = name.substring(0, name.indexOf("."));
		images.add(new ImageHolder(key, img));

		ImageView imgView = (ImageView) hbox.getChildren().get(
				hbox.getChildren().size() - 1);
		imgView.setPreserveRatio(true);
		imgView.fitWidthProperty().bind(imgWidth);
		hbox.getChildren().add(createEmtpyImageView(imgWidth));
		if (isEvenSize(images)) {
			hbox.getChildren().add(createEmtpyImageView(imgWidth));
		}
		rearrangeImages();
	}

	private boolean isOddSize(List<ImageHolder> list) {
		return list.size() % 2 == 1;
	}

	private boolean isEvenSize(List<ImageHolder> list) {
		return !isOddSize(list);
	}

	private void rearrangeImages() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {

				LinkedList<Image> imgs = new LinkedList<>();
				for (ImageHolder ih : images) {
					imgs.add(ih.image);
				}
				int shift = imgs.size() / 2 - pos;

				for (int i = 0; i < shift; i++) {
					imgs.addFirst(imgs.removeLast());
				}

				for (int i = 0; i > shift; i--) {
					imgs.addLast(imgs.removeFirst());
				}
				for (int i = 0; i < images.size(); i++) {
					ImageView iv = (ImageView) hbox.getChildren().get(i + 1);
					iv.setImage(imgs.get(i));
				}
				((ImageView) hbox.getChildren().get(0)).setImage(imgs.get(imgs
						.size() - 1));

				if (isEvenSize(images)) {
					((ImageView) hbox.getChildren().get(
							hbox.getChildren().size() - 2)).setImage(imgs
							.get(0));
					((ImageView) hbox.getChildren().get(
							hbox.getChildren().size() - 1)).setImage(imgs
							.get(1));
				} else {
					((ImageView) hbox.getChildren().get(
							hbox.getChildren().size() - 1)).setImage(imgs
							.get(0));
				}

			}
		});

	}

	public void previous() {
		LOG.debug("previous");
		go(-1);
	}

	public void next() {
		LOG.debug("next");
		go(1);
	}

	private void go(int i) {
		int newPos = pos + i;
		if (newPos < 0) {
			newPos = images.size() - 1;
		} else if (newPos >= images.size()) {
			newPos = 0;
		}
		pos = newPos;

		ParallelTransition pt = new ParallelTransition();
		for (Node n : hbox.getChildren()) {
			ImageView iv = (ImageView) n;
			pt.getChildren().add(move(hbox, iv, i * imgWidth.get(), 250));
		}
		pt.onFinishedProperty().set(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						for (Node n : hbox.getChildren()) {
							ImageView iv = (ImageView) n;
							iv.translateXProperty().set(0);
						}
					}
				});
				rearrangeImages();
			}
		});
		pt.play();
		LOG.debug("Now selected: {}", getSelected());
	}

	private Animation move(final HBox node, ImageView imageView, double width,
			int duration) {
		TranslateTransition tt = new TranslateTransition(
				Duration.millis(duration), imageView);
		tt.setByX(-width);
		return tt;

	}

	static class ImageHolder {
		Image image;
		String name;

		public ImageHolder(String name, Image image) {
			this.image = image;
			this.name = name;
		}
	}

	public String getSelected() {
		return images.get(pos).name;
	}

}

