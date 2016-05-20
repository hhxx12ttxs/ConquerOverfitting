package cc.clearcode.nudanachacie.facebook.parameters;

import android.os.Bundle;

public class PostDialogParameters {

	private Bundle bundle;
	private static final String fromKey = "from";
	private static final String toKey = "to";
	private static final String displayKey = "display";
	private static final String pictureKey = "picture";
	private static final String linkKey = "link";
	private static final String captionKey = "caption";
	private static final String descriptionKey = "description";
	private static final String sourceKey = "source";
	private static final String nameKey = "name";
	public static final String[] displayTypes = { "page", "popup", "touch" };

	public PostDialogParameters(String from, String to, String display,
			String picture, String link, String caption, String description,
			String source, String name) {
		super();
		this.bundle = new Bundle();
		if (from != null)
			bundle.putString(fromKey, from);
		if (to != null)
			bundle.putString(toKey, to);
		if (display != null)
			bundle.putString(displayKey, display);
		if (picture != null)
			bundle.putString(pictureKey, picture);
		if (link != null)
			bundle.putString(linkKey, link);
		if (caption != null)
			bundle.putString(captionKey, caption);
		if (description != null)
			bundle.putString(descriptionKey, description);
		if (source != null)
			bundle.putString(sourceKey, source);
		if (name != null)
			bundle.putString(nameKey, name);

	}

	public Bundle getBundle() {
		return bundle;
	}
}

