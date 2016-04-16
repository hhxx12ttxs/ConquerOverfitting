package com.weanticipate.client.web_ui.gwt.components.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.weanticipate.client.web_ui.gwt.callbacks.Callback;
import com.weanticipate.client.web_ui.gwt.util.FormatUtil;
import com.weanticipate.client.web_ui.gwt.util.domevents.click.ClickEventManager;

public final class EventEmbedView extends BaseView implements EventEmbedDisplay {

	@UiTemplate("EventEmbedLargeView.ui.xml")
	interface LargeUIBinder extends UiBinder<Element, EventEmbedView> {
	}

	private static LargeUIBinder largeUIBinder = GWT.create(LargeUIBinder.class);

	@UiTemplate("EventEmbedMediumView.ui.xml")
	interface MediumUIBinder extends UiBinder<Element, EventEmbedView> {
	}

	private static MediumUIBinder mediumUIBinder = GWT.create(MediumUIBinder.class);

	@UiTemplate("EventEmbedSmallView.ui.xml")
	interface SmallBinder extends UiBinder<Element, EventEmbedView> {
	}

	private static SmallBinder smallUIBinder = GWT.create(SmallBinder.class);

	public static EventEmbedView createLargeView(String color) {
		return new EventEmbedView(largeUIBinder, color);
	}

	public static EventEmbedView createMediumView(String color) {
		return new EventEmbedView(mediumUIBinder, color);
	}

	public static EventEmbedView createSmallView(String color) {
		return new EventEmbedView(smallUIBinder, color);
	}

	@UiField
	Element eventTitleField1;
	@UiField
	Element eventTitleField2;
	@UiField
	Element pluralizeDaysField;
	@UiField
	Element pluralizeMinutesField;
	@UiField
	Element pluralizeHoursField;
	@UiField
	ImageElement profileImage;
	@UiField
	Element pastEventContainer;
	@UiField
	Element countdownContainer;
	@UiField
	Element recentEventContainer;
	@UiField
	Element tilLabel;
	@UiField
	Element daysRemainingField;
	@UiField
	Element hoursRemainingField;
	@UiField
	Element minutesRemainingField;
	@UiField
	Element secondsRemainingField;
	@UiField
	Element millisRemainingField;
	@UiField
	Element usernameField;
	@UiField
	Element followerCountField;
	@UiField
	Element followerCountPluralField;
	@UiField
	Element joinLink;

	private EventEmbedView(UiBinder<Element, EventEmbedView> uiBinder, String color) {
		Element self = uiBinder.createAndBindUi(this);
		setElement(self);

		if (color.equals("light")) {
			self.addClassName("black");
		}
	}

	@Override
	public void setFollowers(int followerCount) {
		followerCountField.setInnerText(FormatUtil.formatInteger(followerCount));
		FormatUtil.pluralize(followerCount, followerCountPluralField);
	}

	@Override
	public void setOwnerUsername(String username) {
		usernameField.setInnerText(username);
	}

	@Override
	public void setProfileImageUrl(String profileImageUrl) {
		profileImage.setSrc(profileImageUrl);
	}

	@Override
	public void setEventTitle(String title) {
		eventTitleField1.setInnerText(title);
		eventTitleField2.setInnerText(title);
	}

	@Override
	public void showPastEventLabel() {
		showElement(pastEventContainer);
		hideElement(countdownContainer);
		hideElement(recentEventContainer);
		hideElement(tilLabel);
	}

	@Override
	public void showRecentEventLabel() {
		hideElement(pastEventContainer);
		hideElement(countdownContainer);
		showElement(recentEventContainer);
		hideElement(tilLabel);
	}

	@Override
	public void updateRemainingTimeLabel(int days, int hours, int minutes, int seconds, int millis) {
		hideElement(pastEventContainer);
		showElement(countdownContainer);
		hideElement(recentEventContainer);
		showElement(tilLabel);

		daysRemainingField.setInnerText("" + days);
		hoursRemainingField.setInnerText("" + hours);
		minutesRemainingField.setInnerText("" + minutes);
		secondsRemainingField.setInnerText("" + seconds);
		millisRemainingField.setInnerText("" + millis);
		FormatUtil.pluralize(days, pluralizeDaysField);
		FormatUtil.pluralize(hours, pluralizeHoursField);
		FormatUtil.pluralize(minutes, pluralizeMinutesField);
	}

	@Override
	public void setTitleClickHandler(final Callback<Void> callback) {
		ClickHandler clickHandler = new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				callback.onDone(null);
			}
		};

		new ClickEventManager(eventTitleField1, clickHandler);
		new ClickEventManager(eventTitleField2, clickHandler);
	}

	@Override
	public void setJoinClickHandler(final Callback<Void> callback) {
		ClickHandler clickHandler = new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				callback.onDone(null);
			}
		};

		new ClickEventManager(joinLink, clickHandler);
	}

}

