/*
 * Copyright (C) 2011 Alex Kuiper
 * 
 * This file is part of PageTurner
 *
 * PageTurner is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PageTurner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PageTurner.  If not, see <http://www.gnu.org/licenses/>.*
 */

package net.nightwhistler.pageturner.view.bookview;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.graphics.*;
import com.google.inject.Inject;
import net.nightwhistler.htmlspanner.FontFamily;
import net.nightwhistler.htmlspanner.HtmlSpanner;
import net.nightwhistler.htmlspanner.TagNodeHandler;
import net.nightwhistler.htmlspanner.handlers.TableHandler;
import net.nightwhistler.htmlspanner.spans.CenterSpan;
import net.nightwhistler.pageturner.Configuration;
import net.nightwhistler.pageturner.R;
import net.nightwhistler.pageturner.epub.PageTurnerSpine;
import net.nightwhistler.pageturner.epub.ResourceLoader;
import net.nightwhistler.pageturner.epub.ResourceLoader.ResourceCallback;
import net.nightwhistler.pageturner.tasks.SearchTextTask;
import net.nightwhistler.pageturner.view.FastBitmapDrawable;
import nl.siegmann.epublib.Constants;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.util.StringUtil;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import roboguice.RoboGuice;

import static net.nightwhistler.pageturner.PlatformUtil.executeTask;

public class BookView extends ScrollView implements LinkTagHandler.LinkCallBack {

	private int storedIndex;
	private String storedAnchor;

	private InnerView childView;

	private Set<BookViewListener> listeners;

	private TableHandler tableHandler;

	private PageTurnerSpine spine;

	private String fileName;
	private Book book;


	private int prevIndex = -1;
	private int prevPos = -1;

	private PageChangeStrategy strategy;
	private ResourceLoader loader;

	private int horizontalMargin = 0;
	private int verticalMargin = 0;
	private int lineSpacing = 0;

	private Configuration configuration;
	
	private Handler scrollHandler;

	private static final Logger LOG = LoggerFactory.getLogger("BookView");

    @Inject
    private TextLoader textLoader;

	public BookView(Context context, AttributeSet attributes) {
		super(context, attributes);
		this.scrollHandler = new Handler();
        RoboGuice.injectMembers(context, this);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void init() {
		this.listeners = new HashSet<BookViewListener>();

		this.childView = (InnerView) this.findViewById(R.id.innerView);
		this.childView.setBookView(this);

		childView.setCursorVisible(false);
		childView.setLongClickable(true);
		this.setVerticalFadingEdgeEnabled(false);
		childView.setFocusable(true);
		childView.setLinksClickable(true);
/*
		FIXME: disabled text selection for now.
		if (Build.VERSION.SDK_INT >= 11) {
			childView.setTextIsSelectable(true);
		}  */
		
		this.setSmoothScrollingEnabled(false);
		this.tableHandler = new TableHandler();
        this.textLoader.registerTagNodeHandler("table", tableHandler);

        ImageTagHandler imgHandler = new ImageTagHandler(false);
        this.textLoader.registerTagNodeHandler("img", imgHandler);
        this.textLoader.registerTagNodeHandler("image", imgHandler);

        this.textLoader.setLinkCallBack(this);
	}

	private void onInnerViewResize() {
		restorePosition();

		if ( this.tableHandler != null ) {
			int tableWidth = (int) (childView.getWidth() * 0.9);
			tableHandler.setTableWidth(tableWidth);
		}
	}


	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

    @Override
    public void linkClicked(String href) {
        navigateTo(spine.resolveHref(href));
    }

    public PageChangeStrategy getStrategy() {
        return this.strategy;
    }

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		progressUpdate();
	}

	/**
	 * Returns if we're at the start of the book, i.e. displaying the title
	 * page.
	 * 
	 * @return
	 */
	public boolean isAtStart() {

		if (spine == null) {
			return true;
		}

		return spine.getPosition() == 0 && strategy.isAtStart();
	}

	public boolean isAtEnd() {
		if (spine == null) {
			return false;
		}

		return spine.getPosition() >= spine.size() - 1 && strategy.isAtEnd();
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		this.loader = new ResourceLoader(fileName);
	}

	@Override
	public void setOnTouchListener(OnTouchListener l) {
		super.setOnTouchListener(l);
		this.childView.setOnTouchListener(l);
	}

	public ClickableSpan[] getLinkAt(float x, float y) {
		Integer offset = findOffsetForPosition(x, y);

		CharSequence text = childView.getText();
		
		if (offset == null || ! (text instanceof Spanned)) {
			return null;
		} 

		Spanned spannedText = (Spanned) text;
		ClickableSpan[] spans = spannedText.getSpans(offset, offset,
				ClickableSpan.class);

		return spans;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (strategy.isScrolling()) {
			return super.onTouchEvent(ev);
		} else {
			return childView.onTouchEvent(ev);
		}
	}

	public boolean hasPrevPosition() {
		return this.prevIndex != -1 && this.prevPos != -1;
	}

	public void setLineSpacing(int lineSpacing) {
		if (lineSpacing != this.lineSpacing) {
			this.lineSpacing = lineSpacing;
			this.childView.setLineSpacing(lineSpacing, 1);

			if (strategy != null) {
				strategy.updatePosition();
			}
		}
	}

	/*
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setTextSelectionCallback(TextSelectionCallback callback) {
		if (Build.VERSION.SDK_INT >= 11) {
			this.childView
					.setCustomSelectionActionModeCallback(new TextSelectionActions(
							callback, this));
		}
	}
	*/

	public int getLineSpacing() {
		return lineSpacing;
	}

	public void setHorizontalMargin(int horizontalMargin) {

		if (horizontalMargin != this.horizontalMargin) {
			this.horizontalMargin = horizontalMargin;
			setPadding(this.horizontalMargin, this.verticalMargin,
					this.horizontalMargin, this.verticalMargin);
			if (strategy != null) {
				strategy.updatePosition();
			}
		}
	}
	
	public CharSequence peekAhead() {
		return this.strategy.getNextPageText();
	}

	public void releaseResources() {
		this.strategy.clearText();
		this.textLoader.closeCurrentBook();
	}

	public void setLinkColor(int color) {
		this.childView.setLinkTextColor(color);
	}

	public void setVerticalMargin(int verticalMargin) {
		if (verticalMargin != this.verticalMargin) {
			this.verticalMargin = verticalMargin;
			setPadding(this.horizontalMargin, this.verticalMargin,
					this.horizontalMargin, this.verticalMargin);
			if (strategy != null) {
				strategy.updatePosition();
			}
		}
	}

	public int getHorizontalMargin() {
		return horizontalMargin;
	}

	public int getVerticalMargin() {
		return verticalMargin;
	}

	public int getSelectionStart() {
		return childView.getSelectionStart();
	}

	public int getSelectionEnd() {
		return childView.getSelectionEnd();
	}

	public String getSelectedText() {
		return childView.getText()
				.subSequence(getSelectionStart(), getSelectionEnd()).toString();
	}

	public void goBackInHistory() {

		if (this.prevIndex == this.getIndex()) {
			strategy.setPosition(prevPos);

			this.storedAnchor = null;
			this.prevIndex = -1;
			this.prevPos = -1;

			restorePosition();

		} else {
			this.strategy.clearText();
			this.spine.navigateByIndex(this.prevIndex);
			strategy.setPosition(this.prevPos);

			this.storedAnchor = null;
			this.prevIndex = -1;
			this.prevPos = -1;

			loadText();
		}
	}

	public void clear() {
		this.childView.setText("");
		this.storedAnchor = null;
		this.storedIndex = -1;
		this.book = null;
		this.fileName = null;

		this.strategy.reset();
	}

	/**
	 * Loads the text and saves the restored position.
	 */
	public void restore() {
		strategy.clearText();
		loadText();
	}

	public void setIndex(int index) {
		this.storedIndex = index;
	}

	void loadText() {
		executeTask(new LoadTextTask());
	}

	private void loadText(List<SearchTextTask.SearchResult> hightListResults) {
		executeTask(new LoadTextTask(hightListResults));
	}

    public void setFontFamily(FontFamily family) {
        this.childView.setTypeface(family.getDefaultTypeface());
        this.tableHandler.setTypeFace(family.getDefaultTypeface());
    }

	public void pageDown() {
		strategy.pageDown();
		progressUpdate();
	}

	public void pageUp() {
		strategy.pageUp();
		progressUpdate();
	}

	TextView getInnerView() {
		return childView;
	}

	PageTurnerSpine getSpine() {
		return this.spine;
	}

	private Integer findOffsetForPosition(float x, float y) {

		if (childView == null || childView.getLayout() == null) {
			return null;
		}

		Layout layout = this.childView.getLayout();
		int line = layout.getLineForVertical((int) y);

		return layout.getOffsetForHorizontal(line, x);
	}

    private int[] findPositionForOffset(int offset) {
        Layout layout = this.childView.getLayout();
        int line = layout.getLineForOffset(offset);
        int y = layout.getLineBottom(line);

        int x = (int) layout.getPrimaryHorizontal(offset);

        LOG.debug("Coordinates for offset " + offset + " x:" + x + " y:" + y );

        int[] result = { x, y };

        return  result;
    }

	/**
	 * Returns the full word containing the character at the selected location.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public CharSequence getWordAt(float x, float y) {

		if (childView == null) {
			return null;
		}

		CharSequence text = this.childView.getText();

		if (text.length() == 0) {
			return null;
		}

		Integer offset = findOffsetForPosition(x, y);

		if (offset == null) {
			return null;
		}

		if (offset < 0 || offset > text.length() - 1) {
			return null;
		}

		if (isBoundaryCharacter(text.charAt(offset))) {
			return null;
		}

		int left = Math.max(0, offset - 1);
		int right = Math.min(text.length(), offset);

		CharSequence word = text.subSequence(left, right);
		while (left > 0 && !isBoundaryCharacter(word.charAt(0))) {
			left--;
			word = text.subSequence(left, right);
		}

		if (word.length() == 0) {
			return null;
		}

		while (right < text.length()
				&& !isBoundaryCharacter(word.charAt(word.length() - 1))) {
			right++;
			word = text.subSequence(left, right);
		}

		int start = 0;
		int end = word.length();

		if (isBoundaryCharacter(word.charAt(0))) {
			start = 1;
		}

		if (isBoundaryCharacter(word.charAt(word.length() - 1))) {
			end = word.length() - 1;
		}

		if (start > 0 && start < word.length() && end < word.length()) {
			return word.subSequence(start, end);
		}

		return null;
	}

	private static boolean isBoundaryCharacter(char c) {
		char[] boundaryChars = { ' ', '.', ',', '\"', '\'', '\n', '\t', ':',
				'!', '\'' };

		for (int i = 0; i < boundaryChars.length; i++) {
			if (boundaryChars[i] == c) {
				return true;
			}
		}

		return false;
	}

	public void navigateTo(String rawHref) {

		this.prevIndex = this.getIndex();
		this.prevPos = this.getProgressPosition();

		// URLDecode the href, so it does not contain %20 etc.
		String href = URLDecoder.decode(StringUtil.substringBefore(rawHref,
                Constants.FRAGMENT_SEPARATOR_CHAR));

		// Don't decode the anchor.
		String anchor = StringUtil.substringAfterLast(rawHref,
				Constants.FRAGMENT_SEPARATOR_CHAR);

		if (!"".equals(anchor)) {
			this.storedAnchor = anchor;
		}

		// Just an anchor and no href; resolve it on this page
		if (href.length() == 0) {
			restorePosition();
		} else {

			this.strategy.clearText();
			this.strategy.setPosition(0);

			if (this.spine.navigateByHref(href)) {
				loadText();
			} else {				
				executeTask(new LoadTextTask(), href);					
			}
		}
	}

	public void navigateToPercentage(int percentage) {

		if (spine == null) {
			return;
		}
		
		int index = 0;
		
		if ( percentage > 0 ) {

			double targetPoint = (double) percentage / 100d;
			List<Double> percentages = this.spine.getRelativeSizes();

			if (percentages == null || percentages.isEmpty()) {
				return;
			}
			
			double total = 0;

			for (; total < targetPoint && index < percentages.size(); index++) {
				total = total + percentages.get(index);
			}

			index--;

			// Work-around for when we get multiple events.
			if (index < 0 || index >= percentages.size()) {
				return;
			}

			double partBefore = total - percentages.get(index);
			double progressInPart = (targetPoint - partBefore)
					/ percentages.get(index);
			
			this.strategy.setRelativePosition(progressInPart);
		} else {
			
			//Simply jump to titlepage			
			this.strategy.setPosition(0);
		}

		this.prevPos = this.getProgressPosition();
		doNavigation(index);
	}

	public void navigateBySearchResult(
			List<SearchTextTask.SearchResult> result, int selectedResultIndex) {
		SearchTextTask.SearchResult searchResult = result
				.get(selectedResultIndex);
		
		this.prevPos = this.getProgressPosition();
		this.strategy.setPosition(searchResult.getStart());

		this.prevIndex = this.getIndex();

		this.storedIndex = searchResult.getIndex();
		this.strategy.clearText();
		this.spine.navigateByIndex(searchResult.getIndex());

		loadText(result);
	}

	private void doNavigation(int index) {

		// Check if we're already in the right part of the book
		if (index == this.getIndex()) {
			restorePosition();
			progressUpdate();
			return;
		}

		this.prevIndex = this.getIndex();

		this.storedIndex = index;
		this.strategy.clearText();
		this.spine.navigateByIndex(index);

		loadText();
	}

	public void navigateTo(int index, int position) {

		this.prevPos = this.getProgressPosition();
		this.strategy.setPosition(position);

		doNavigation(index);
	}

	public List<TocEntry> getTableOfContents() {
		if (this.book == null) {
			return null;
		}

		List<TocEntry> result = new ArrayList<BookView.TocEntry>();

		flatten(book.getTableOfContents().getTocReferences(), result, 0);

		return result;
	}

	private void flatten(List<TOCReference> refs, List<TocEntry> entries,
			int level) {

		if (spine == null || refs == null || refs.isEmpty()) {
			return;
		}

		for (TOCReference ref : refs) {

			String title = "";

			for (int i = 0; i < level; i++) {
				title += "-";
			}

			title += ref.getTitle();

			if (ref.getResource() != null) {
				entries.add(new TocEntry(title, spine.resolveTocHref(ref
						.getCompleteHref())));
			}

			flatten(ref.getChildren(), entries, level + 1);
		}
	}

	@Override
	public void fling(int velocityY) {
		strategy.clearStoredPosition();
		super.fling(velocityY);
	}

	public int getIndex() {
		if (this.spine == null) {
			return storedIndex;
		}

		return this.spine.getPosition();
	}

    public int getStartOfCurrentPage() {
        return strategy.getTopLeftPosition();
    }

	public int getProgressPosition() {
		return strategy.getProgressPosition();
	}

	public void setPosition(int pos) {
		this.strategy.setPosition(pos);
	}

	/**
	 * Scrolls to a previously stored point.
	 * 
	 * Call this after setPosition() to actually go there.
	 */
	private void restorePosition() {

        if (this.storedAnchor != null  ) {

            Integer anchorValue = this.textLoader.getAnchor(
                    spine.getCurrentHref(), storedAnchor );

            if ( anchorValue != null ) {
                strategy.setPosition(anchorValue);
			    this.storedAnchor = null;
            }
		}

		this.strategy.updatePosition();
	}

	private void setImageSpan(SpannableStringBuilder builder,
			Drawable drawable, int start, int end) {
		builder.setSpan(new ImageSpan(drawable), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		if (spine.isCover()) {
			builder.setSpan(new CenterSpan(), start, end,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}

	private class ImageCallback implements ResourceCallback {

		private SpannableStringBuilder builder;
		private int start;
		private int end;

		private String storedHref;
		
		private boolean fakeImages;

		public ImageCallback(String href, SpannableStringBuilder builder,
				int start, int end, boolean fakeImages) {
			this.builder = builder;
			this.start = start;
			this.end = end;
			this.storedHref = href;
			this.fakeImages = fakeImages;
		}

		@Override
		public void onLoadResource(String href, InputStream input) {

			if ( fakeImages ) {
				LOG.debug("Faking image for href: " + href);
				setFakeImage(input);
			} else {
				LOG.debug("Loading real image for href: " + href);
				setBitmapDrawable(input);
			}

		}
		
		private void setFakeImage(InputStream input) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(input, null, options);
			
			int[] sizes = calculateSize(options.outWidth, options.outHeight );
			
			ShapeDrawable draw = new ShapeDrawable(new RectShape());
			draw.setBounds(0, 0, sizes[0], sizes[1]);
			
			setImageSpan(builder, draw, start, end);
		}
		
		private void setBitmapDrawable(InputStream input) {
			Bitmap bitmap = null;
			try {
				bitmap = getBitmap(input);

				if (bitmap == null || bitmap.getHeight() < 1
						|| bitmap.getWidth() < 1) {
					return;
				}

			} catch (OutOfMemoryError outofmem) {
				LOG.error("Could not load image", outofmem);
			}

			if (bitmap != null) {
				
				FastBitmapDrawable drawable = new FastBitmapDrawable(bitmap);
				
				drawable.setBounds(0, 0, bitmap.getWidth() - 1,
						bitmap.getHeight() - 1);
				setImageSpan(builder, drawable, start, end);

				LOG.debug("Storing image in cache: " + storedHref);

                textLoader.storeImageInChache(storedHref, drawable);
			}
		}		
		

		private Bitmap getBitmap(InputStream input) {
			if(Configuration.IS_NOOK_TOUCH) {
				// Workaround for skia failing to load larger (>8k?) JPEGs on Nook Touch and maybe other older Eclair devices (unknown!)
				// seems the underlying problem is the ZipInputStream returning data in chunks,
				// may be as per http://code.google.com/p/android/issues/detail?id=6066
				// workaround is to stream the whole image out of the Zip to a ByteArray, then pass that on to the bitmap decoder
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while(true) {
						byte[] buffer  = new byte[4096];
						int read = input.read(buffer);
						if(read <= 0)
							break;
						baos.write(buffer, 0, read);
					}
					input = new ByteArrayInputStream(baos.toByteArray());
				} catch(IOException ex) {
					LOG.error("Failed to extract full image from epub stream: " + ex.toString());
				}
			}

			Bitmap originalBitmap = BitmapFactory.decodeStream(input);

			if (originalBitmap != null) {
				int originalWidth = originalBitmap.getWidth();
				int originalHeight = originalBitmap.getHeight();

				int[] targetSizes = calculateSize(originalWidth, originalHeight);
				int targetWidth = targetSizes[0];
				int targetHeight = targetSizes[1];
				
				if ( targetHeight != originalHeight || targetWidth != originalWidth ) {					
					return Bitmap.createScaledBitmap(originalBitmap,
							targetWidth, targetHeight, true);
				}
			}

			return originalBitmap;
		}
	}
	
	private int[] calculateSize(int originalWidth, int originalHeight ) {
		int[] result = new int[] { originalWidth, originalHeight };		
		
		int screenHeight = getHeight() - (verticalMargin * 2);
		int screenWidth = getWidth() - (horizontalMargin * 2);
		
		// We scale to screen width for the cover or if the image is too
		// wide.
		if (originalWidth > screenWidth
				|| originalHeight > screenHeight || spine.isCover()) {

			float ratio = (float) originalWidth
					/ (float) originalHeight;

			int targetHeight = screenHeight - 1;
			int targetWidth = (int) (targetHeight * ratio);

			if (targetWidth > screenWidth - 1) {
				targetWidth = screenWidth - 1;
				targetHeight = (int) (targetWidth * (1 / ratio));
			}

			LOG.debug("Rescaling from " + originalWidth + "x"
					+ originalHeight + " to " + targetWidth + "x"
					+ targetHeight);

			if (targetWidth > 0 || targetHeight > 0) {
				result[0] = targetWidth;
				result[1] = targetHeight;
			}
		}
		
		return result;		
	}

	private class ImageTagHandler extends TagNodeHandler {

		private boolean fakeImages;
		
		public ImageTagHandler(boolean fakeImages) {
			this.fakeImages = fakeImages;			
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		public void handleTagNode(TagNode node, SpannableStringBuilder builder,
				int start, int end) {
			String src = node.getAttributeByName("src");

			if (src == null) {
				src = node.getAttributeByName("href");
			}

			if (src == null) {
				src = node.getAttributeByName("xlink:href");
			}
			builder.append("\uFFFC");
			
			if (src.startsWith("data:image/png;base64")) {
				String dataString = src.substring(src
						.indexOf(',') + 1);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
					byte[] binData = Base64.decode(dataString,
							Base64.DEFAULT);
					
					setImageSpan(builder, new BitmapDrawable(
							getContext().getResources(),
							BitmapFactory.decodeByteArray(binData, 0, binData.length )),
							start, builder.length());
					
				} else {
					// fallback for Android 2.1					
				}

			} else {

				String resolvedHref = spine.resolveHref(src);

                if ( textLoader.hasCachedImage(resolvedHref) && ! fakeImages ) {
                    Drawable drawable = textLoader.getCachedImage(resolvedHref);
					setImageSpan(builder, drawable, start, builder.length());
					LOG.debug("Got cached href: " + resolvedHref);
				} else {
					LOG.debug("Loading href: " + resolvedHref);
					this.registerCallback(resolvedHref, new ImageCallback(
							resolvedHref, builder, start, builder.length(), fakeImages));
				}
			}
		}
		
		protected void registerCallback(String resolvedHref, ImageCallback callback ) {
			BookView.this.loader.registerCallback(resolvedHref, callback);
		}

	}

	@Override
	public void setBackgroundColor(int color) {
		super.setBackgroundColor(color);

		if (this.childView != null) {
			this.childView.setBackgroundColor(color);
		}
	}

	public void setTextColor(int color) {
		if (this.childView != null) {
			this.childView.setTextColor(color);
		}

		this.tableHandler.setTextColor(color);
	}

	public static class TocEntry {
		private String title;
		private String href;

		public TocEntry(String title, String href) {
			this.title = title;
			this.href = href;
		}

		public String getHref() {
			return href;
		}

		public String getTitle() {
			return title;
		}
	}
	
	public Book getBook() {
		return book;
	}

	public float getTextSize() {
		return childView.getTextSize();
	}
	
	public CharSequence getDisplayedText() {
		return this.childView.getText();
	}

	public void setTextSize(float textSize) {
		this.childView.setTextSize(textSize);
		this.tableHandler.setTextSize(textSize);
	}

	public void addListener(BookViewListener listener) {
		this.listeners.add(listener);
	}

	private void bookOpened(Book book) {
		for (BookViewListener listener : this.listeners) {
			listener.bookOpened(book);
		}
	}

	private void errorOnBookOpening(String errorMessage) {
		for (BookViewListener listener : this.listeners) {
			listener.errorOnBookOpening(errorMessage);
		}
	}

	private void parseEntryStart(int entry) {
		for (BookViewListener listener : this.listeners) {
			listener.parseEntryStart(entry);
		}
	}

	private void parseEntryComplete(int entry, String name) {
		for (BookViewListener listener : this.listeners) {
			listener.parseEntryComplete(entry, name);
		}
	}

	private void fireOpenFile() {
		for (BookViewListener listener : this.listeners) {
			listener.readingFile();
		}
	}

	private void fireRenderingText() {
		for (BookViewListener listener : this.listeners) {
			listener.renderingText();
		}
	}



	private void progressUpdate() {

		if (this.spine != null && this.strategy.getText() != null
				&& this.strategy.getText().length() > 0) {

			double progressInPart = (double) this.getProgressPosition()
					/ (double) this.strategy.getText().length();

			if (strategy.getText().length() > 0 && strategy.isAtEnd()) {
				progressInPart = 1d;
			}

			int progress = spine.getProgressPercentage(progressInPart);

			if (progress != -1) {

				int pageNumber = getPageNumberFor(getIndex(),
						getProgressPosition());

				for (BookViewListener listener : this.listeners) {
					listener.progressUpdate(progress, pageNumber,
							spine.getTotalNumberOfPages());
				}
			}
		}
	}
	
	public int getPageNumberFor( int index, int position ) {
		
		int pageNum = 0;
		
		List<List<Integer>> pageOffsets = spine.getPageOffsets();
		
		if ( pageOffsets == null || index >= pageOffsets.size() ) {
			return -1;
		}
		
		for ( int i=0; i < index; i++ ) {
			pageNum += pageOffsets.get(i).size();			
		}
		
		List<Integer> offsets;
		
		if ( this.strategy.isScrolling() ) {		
			offsets = pageOffsets.get(index);
		
			for ( int i=0; i < offsets.size() && offsets.get(i) < position; i++ ) {
				pageNum++;
			}
		
		} else {
			pageNum+= ((FixedPagesStrategy) strategy).getCurrentPage();
		}		
		
		return pageNum;
	}

	public void setEnableScrolling(boolean enableScrolling) {

		if (this.strategy == null
				|| this.strategy.isScrolling() != enableScrolling) {

			int pos = -1;
			boolean wasNull = true;

			Spanned text = null;

			if (this.strategy != null) {
				pos = this.strategy.getTopLeftPosition();
				text = this.strategy.getText();
				this.strategy.clearText();
				wasNull = false;
			}

			if (enableScrolling) {
				this.strategy = new ScrollingStrategy(this, this.getContext());
			} else {
				this.strategy = new FixedPagesStrategy(this, configuration, new StaticLayoutFactory());
			}

			if (!wasNull) {
				this.strategy.setPosition(pos);
			}

			if (text != null && text.length() > 0) {
				this.strategy.loadText(text);
			}
		}
	}

    private List<Integer> getOffsetsForResource(int spineIndex )
            throws IOException {

        HtmlSpanner mySpanner = new HtmlSpanner();
        final ResourceLoader privateLoader = new ResourceLoader(fileName);

        ImageTagHandler tagHandler = new ImageTagHandler(true) {
            protected void registerCallback(String resolvedHref, ImageCallback callback) {
                privateLoader.registerCallback(resolvedHref, callback);
            }
        };

        mySpanner.registerHandler("table", tableHandler );
        mySpanner.registerHandler("img", tagHandler);
        mySpanner.registerHandler("image", tagHandler);

        Resource res = spine.getResourceForIndex(spineIndex);
        CharSequence text = textLoader.getText(res,  false);

        privateLoader.load();

        return new FixedPagesStrategy(this, configuration, new StaticLayoutFactory()).getPageOffsets(text, true);
    }
	
	

	public static class InnerView extends TextView {

		private BookView bookView;

        private Paint paint = new Paint();

		public InnerView(Context context, AttributeSet attributes) {
			super(context, attributes);
		}

        public void setTextColor(int color) {
            super.setTextColor(color);
            this.paint.setColor(color);
        }

		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			bookView.onInnerViewResize();
		}

		public boolean dispatchKeyEvent(KeyEvent event) {
			return bookView.dispatchKeyEvent(event);
		}

		public void setBookView(BookView bookView) {
			this.bookView = bookView;
		}

    }

	private static enum BookReadPhase {
		START, OPEN_FILE, PARSE_TEXT, DONE
	};

	private class LoadTextTask extends
			AsyncTask<String, BookReadPhase, Spanned> {

		private String name;

		private boolean wasBookLoaded;

		private String error;
		private boolean needToCalcPageNumbers = false;

		private List<SearchTextTask.SearchResult> searchResults = new ArrayList<SearchTextTask.SearchResult>();

		public LoadTextTask() {

		}

		LoadTextTask(List<SearchTextTask.SearchResult> searchResults) {
			this.searchResults = searchResults;
		}

		@Override
		protected void onPreExecute() {
			this.wasBookLoaded = book != null;
		}

		private void setBook(Book book) {

			BookView.this.book = book;
			BookView.this.spine = new PageTurnerSpine(book);

			String file = StringUtil.substringAfterLast(fileName, '/');

			BookView.this.spine.navigateByIndex(BookView.this.storedIndex);

			if (configuration.isShowPageNumbers()) {

				List<List<Integer>> offsets = configuration
						.getPageOffsets(file);

				if (offsets != null && offsets.size() > 0) {
					spine.setPageOffsets(offsets);
					needToCalcPageNumbers = false;
				} else {
					needToCalcPageNumbers = true;
				}

			}

		}

		protected Spanned doInBackground(String... hrefs) {

			publishProgress(BookReadPhase.START);

			if (loader != null) {
				loader.clear();
			}

			if (BookView.this.book == null) {
				try {
                    publishProgress(BookReadPhase.OPEN_FILE);
					setBook(textLoader.initBook(fileName));
				} catch (IOException io) {
					this.error = io.getMessage();
					return null;
				}
			}

			this.name = spine.getCurrentTitle();

			Resource resource;

			if (hrefs.length == 0) {
				resource = spine.getCurrentResource();
			} else {
				resource = book.getResources().getByHref(hrefs[0]);
			}

			if (resource == null) {
				return new SpannedString(
						"Sorry, it looks like you clicked a dead link.\nEven books have 404s these days.");
			}

			publishProgress(BookReadPhase.PARSE_TEXT);

			try {
				Spannable result = textLoader.getText(resource, true);
				loader.load(); // Load all image resources.

                //Clear any old highlighting spans
                BackgroundColorSpan[] spans = result.getSpans(0, result.length(), BackgroundColorSpan.class);
                for ( BackgroundColorSpan span: spans ) {
                    result.removeSpan(span);
                }

				// Highlight search results (if any)
				for (SearchTextTask.SearchResult searchResult : this.searchResults) {
					if (searchResult.getIndex() == spine.getPosition()) {
						result.setSpan(new BackgroundColorSpan(Color.YELLOW),
								searchResult.getStart(), searchResult.getEnd(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}

                //If the view isn't ready yet, wait a bit.
                while ( getInnerView().getWidth() == 0 ) {
                    Thread.sleep(100);
                }

                strategy.loadText(result);

				return result;
			} catch (Exception io) {
				return new SpannableString("Could not load text: "
						+ io.getMessage());
			}

		}

		@Override
		protected void onProgressUpdate(BookReadPhase... values) {

			BookReadPhase phase = values[0];

			switch (phase) {
			case START:
				parseEntryStart(getIndex());
				break;
			case OPEN_FILE:
				fireOpenFile();
				break;
			case PARSE_TEXT:
				fireRenderingText();
				break;
			case DONE:
				parseEntryComplete(spine.getPosition(), this.name);
				break;
			}
		}

		@Override
		protected void onPostExecute(final Spanned result) {

			if (!wasBookLoaded) {
				if (book != null) {
					bookOpened(book);
				} else {
					errorOnBookOpening(this.error);
					return;
				}
			}

			restorePosition();
			strategy.updateGUI();
			progressUpdate();
			
			onProgressUpdate(BookReadPhase.DONE);

			if (needToCalcPageNumbers) {
				executeTask( new CalculatePageNumbersTask() );
			}
			
			/**
			 * This is a hack for scrolling not updating to the right position 
			 * on Android 4+
			 */
			if ( strategy.isScrolling() ) {
				scrollHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						restorePosition();						
					}
				}, 100);
			}
		}
	}

	private class CalculatePageNumbersTask extends
			AsyncTask<Object, Void, List<List<Integer>>> {

        @Override
        protected void onPreExecute() {
            for ( BookViewListener listener: listeners ) {
                listener.onStartCalculatePageNumbers();
            }
        }

        @Override
		protected List<List<Integer>> doInBackground(Object... params) {

			try {
				List<List<Integer>> offsets = new ArrayList<List<Integer>>();

				for (int i = 0; i < spine.size(); i++) {
					offsets.add(getOffsetsForResource(i));
				}

				String file = StringUtil.substringAfterLast(fileName, '/');
				configuration.setPageOffsets(file, offsets);

				return offsets;

			} catch (IOException io) {
				LOG.error("Could not read pagenumers", io);
			}

			return null;
		}

		@Override
		protected void onPostExecute(List<List<Integer>> result) {

            LOG.debug("Pagenumber calculation completed.");

            for ( BookViewListener listener: listeners ) {
                listener.onCalculatePageNumbersComplete();
            }

			spine.setPageOffsets(result);
			progressUpdate();
		}
	}

}

