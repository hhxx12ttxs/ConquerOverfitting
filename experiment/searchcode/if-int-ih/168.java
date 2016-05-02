package mindnotes.client.ui.embedded.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHyperlink;
import com.google.gwt.user.client.ui.Widget;

public class ImageContainer extends Composite implements EmbeddedObjectWidget {

	private static final int SMALL_SIZE_PX = 72;

	private static ImageContainerUiBinder uiBinder = GWT
			.create(ImageContainerUiBinder.class);

	interface ImageContainerUiBinder extends UiBinder<Widget, ImageContainer> {
	}

	private enum Size {
		SMALL, LARGE, ORIGINAL
	};

	private Size _size = Size.SMALL;

	@UiField(provided = true)
	FlowPanel container;
	@UiField
	Image imageWidget;
	@UiField
	FlowPanel options;
	@UiField
	InlineHyperlink smallButton;
	@UiField
	InlineHyperlink largeButton;
	@UiField
	InlineHyperlink originalButton;

	private Listener _listener;

	private int _imageWidth;

	private int _imageHeight;

	public ImageContainer() {

		container = new FlowPanel() {
			{
				addDomHandler(new MouseOverHandler() {

					@Override
					public void onMouseOver(MouseOverEvent event) {
						options.setVisible(true);
					}
				}, MouseOverEvent.getType());
				addDomHandler(new MouseOutHandler() {

					@Override
					public void onMouseOut(MouseOutEvent event) {
						options.setVisible(false);
					}
				}, MouseOutEvent.getType());
			}
		};
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("imageWidget")
	public void onImageLoad(LoadEvent e) {
		_imageWidth = imageWidget.getWidth();
		_imageHeight = imageWidget.getHeight();
		updateSize();
	}

	@UiHandler("smallButton")
	public void onSmallButtonClicked(ClickEvent e) {
		_size = Size.SMALL;
		updateSize();
	}

	@UiHandler("largeButton")
	public void onLargeButtonClicked(ClickEvent e) {
		_size = Size.LARGE;
		updateSize();
	}

	@UiHandler("originalButton")
	public void onOriginalButtonClicked(ClickEvent e) {
		_size = Size.ORIGINAL;
		updateSize();
	}

	private void updateSize() {
		int iw = _imageWidth;
		int ih = _imageHeight;
		int w = 0, h = 0;
		double ar = ih == 0 ? 0 : (double) iw / ih;
		switch (_size) {
		case SMALL:

			if (ar > 1) {
				w = SMALL_SIZE_PX;
				h = (int) ((ar == 0) ? 0 : (w / ar));
			} else {
				h = SMALL_SIZE_PX;
				w = (int) (h * ar);
			}

			break;
		case LARGE:
			if (ar > 1) {
				w = 500;
				h = (int) ((ar == 0) ? 0 : (w / ar));
			} else {
				h = 500;
				w = (int) (h * ar);
			}
			break;
		case ORIGINAL:
			w = iw;
			h = ih;
			break;
		}
		imageWidget.setPixelSize(w, h);
		if (_listener != null) {
			_listener.layoutChanged();
		}
	}

	public void setListener(Listener listener) {
		_listener = listener;
	}

	@Override
	public void setData(String data) {
		imageWidget.setUrl(data);
	}

	@Override
	public Widget getObjectWidget() {
		return this;
	}

	@Override
	public String getObjectTitle() {
		return "Image";
	}
}

