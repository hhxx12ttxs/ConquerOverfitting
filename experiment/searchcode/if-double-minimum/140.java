package org.itx.jbalance.equeue.gwt.client;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that displays progress on an arbitrary scale.
 * 
 * <h3>CSS Style Rules</h3>
 * <ul class='css'>
 * <li>.gwt-ProgressBar-shell { primary style } </li>
 * <li>.gwt-ProgressBar-shell .gwt-ProgressBar-bar { the actual progress bar }
 * </li>
 * <li>.gwt-ProgressBar-shell .gwt-ProgressBar-text { text on the bar } </li>
 * <li>.gwt-ProgressBar-shell .gwt-ProgressBar-text-firstHalf { applied to text
 * when progress is less than 50 percent } </li>
 * <li>.gwt-ProgressBar-shell .gwt-ProgressBar-text-secondHalf { applied to
 * text when progress is greater than 50 percent } </li>
 * </ul>
 */
public class ProgressBar extends Widget implements ResizableWidget {

  private static final String DEFAULT_TEXT_CLASS_NAME =
      "gwt-ProgressBar-text";

  private String textClassName = DEFAULT_TEXT_CLASS_NAME;
  private String textFirstHalfClassName = DEFAULT_TEXT_CLASS_NAME + "-firstHalf";
  private String textSecondHalfClassName = DEFAULT_TEXT_CLASS_NAME + "-secondHalf";

  /**
   * A formatter used to format the text displayed in the progress bar widget.
   */
  public abstract static class TextFormatter {
    /**
     * Generate the text to display in the ProgressBar based on the current
     * value.
     * 
     * Override this method to change the text displayed within the ProgressBar.
     * 
     * @param bar the progress bar
     * @param curProgress the current progress
     * @return the text to display in the progress bar
     */
    protected abstract String getText(ProgressBar bar, double curProgress);
  }

  /**
   * The bar element that displays the progress.
   */
  private Element barElement;

  /**
   * The current progress.
   */
  private double curProgress;

  /**
   * The maximum progress.
   */
  private double maxProgress;

  /**
   * The minimum progress.
   */
  private double minProgress;

  /**
   * A boolean that determines if the text is visible.
   */
  private boolean textVisible = true;

  /**
   * The element that displays text on the page.
   */
  private Element textElement;

  /**
   * The current text formatter.
   */
  private TextFormatter textFormatter;

  /**
   * Create a progress bar with default range of 0 to 100.
   */
  public ProgressBar() {
    this(0.0, 100.0, 0.0);
  }

  /**
   * Create a progress bar with an initial progress and a default range of 0 to
   * 100.
   * 
   * @param curProgress the current progress
   */
  public ProgressBar(double curProgress) {
    this(0.0, 100.0, curProgress);
  }

  /**
   * Create a progress bar within the given range.
   * 
   * @param minProgress the minimum progress
   * @param maxProgress the maximum progress
   */
  public ProgressBar(double minProgress, double maxProgress) {
    this(minProgress, maxProgress, 0.0);
  }

  /**
   * Create a progress bar within the given range starting at the specified
   * progress amount.
   * 
   * @param minProgress the minimum progress
   * @param maxProgress the maximum progress
   * @param curProgress the current progress
   */
  public ProgressBar(double minProgress, double maxProgress, double curProgress) {
    this(minProgress, maxProgress, curProgress, null);
  }

  /**
   * Create a progress bar within the given range starting at the specified
   * progress amount.
   * 
   * @param minProgress the minimum progress
   * @param maxProgress the maximum progress
   * @param curProgress the current progress
   * @param textFormatter the text formatter
   */
  public ProgressBar(double minProgress, double maxProgress,
      double curProgress, TextFormatter textFormatter) {
    this.minProgress = minProgress;
    this.maxProgress = maxProgress;
    this.curProgress = curProgress;
    setTextFormatter(textFormatter);

    // Create the outer shell
    setElement(DOM.createDiv());
    DOM.setStyleAttribute(getElement(), "position", "relative");
    setStyleName("gwt-ProgressBar-shell");

    // Create the bar element
    barElement = DOM.createDiv();
    DOM.appendChild(getElement(), barElement);
    DOM.setStyleAttribute(barElement, "height", "100%");
    setBarStyleName("gwt-ProgressBar-bar");

    // Create the text element
    textElement = DOM.createDiv();
    DOM.appendChild(getElement(), textElement);
    DOM.setStyleAttribute(textElement, "position", "absolute");
    DOM.setStyleAttribute(textElement, "top", "0px");

    // Set the current progress
    setProgress(curProgress);
  }

  /**
   * Get the maximum progress.
   *
   * @return the maximum progress
   */
  public double getMaxProgress() {
    return maxProgress;
  }

  /**
   * Get the minimum progress.
   *
   * @return the minimum progress
   */
  public double getMinProgress() {
    return minProgress;
  }

  /**
   * Get the current percent complete, relative to the minimum and maximum
   * values. The percent will always be between 0.0 - 1.0.
   *
   * @return the current percent complete
   */
  public double getPercent() {
    // If we have no range
    if (maxProgress <= minProgress) {
      return 0.0;
    }

    // Calculate the relative progress
    double percent = (curProgress - minProgress) / (maxProgress - minProgress);
    return Math.max(0.0, Math.min(1.0, percent));
  }

  /**
   * Get the current progress.
   *
   * @return the current progress
   */
  public double getProgress() {
    return curProgress;
  }

  /**
   * Get the text formatter.
   *
   * @return the text formatter
   */
  public TextFormatter getTextFormatter() {
    return textFormatter;
  }

  /**
   * Check whether the text is visible or not.
   *
   * @return true if the text is visible
   */
  public boolean isTextVisible() {
    return textVisible;
  }

  /**
   * This method is called when the dimensions of the parent element change.
   * Subclasses should override this method as needed.
   *
   * Move the text to the center of the progress bar.
   *
   * @param width the new client width of the element
   * @param height the new client height of the element
   */
  public void onResize(int width, int height) {
    if (textVisible) {
      int textWidth = DOM.getElementPropertyInt(textElement, "offsetWidth");
      int left = (width / 2) - (textWidth / 2);
      DOM.setStyleAttribute(textElement, "left", left + "px");
    }
  }

  /**
   * Redraw the progress bar when something changes the layout.
   */
  public void redraw() {
    if (isAttached()) {
      int width = DOM.getElementPropertyInt(getElement(), "clientWidth");
      int height = DOM.getElementPropertyInt(getElement(), "clientHeight");
      onResize(width, height);
    }
  }

  public void setBarStyleName(String barClassName) {
    DOM.setElementProperty(barElement, "className", barClassName);
  }

  /**
   * Set the maximum progress. If the minimum progress is more than the current
   * progress, the current progress is adjusted to be within the new range.
   *
   * @param maxProgress the maximum progress
   */
  public void setMaxProgress(double maxProgress) {
    this.maxProgress = maxProgress;
    curProgress = Math.min(curProgress, maxProgress);
    resetProgress();
  }

  /**
   * Set the minimum progress. If the minimum progress is more than the current
   * progress, the current progress is adjusted to be within the new range.
   *
   * @param minProgress the minimum progress
   */
  public void setMinProgress(double minProgress) {
    this.minProgress = minProgress;
    curProgress = Math.max(curProgress, minProgress);
    resetProgress();
  }

  /**
   * Set the current progress.
   *
   * @param curProgress the current progress
   */
  public void setProgress(double curProgress) {
    this.curProgress = Math.max(minProgress, Math.min(maxProgress, curProgress));

    // Calculate percent complete
    int percent = (int) (100 * getPercent());
    DOM.setStyleAttribute(barElement, "width", percent + "%");
    DOM.setElementProperty(textElement, "innerHTML", generateText(curProgress));
    updateTextStyle(percent);

    // Realign the text
    redraw();
  }

  public void setTextFirstHalfStyleName(String textFirstHalfClassName) {
    this.textFirstHalfClassName = textFirstHalfClassName;
    onTextStyleChange();
  }

  /**
   * Set the text formatter.
   *
   * @param textFormatter the text formatter
   */
  public void setTextFormatter(TextFormatter textFormatter) {
    this.textFormatter = textFormatter;
  }

  public void setTextSecondHalfStyleName(String textSecondHalfClassName) {
    this.textSecondHalfClassName = textSecondHalfClassName;
    onTextStyleChange();
  }

  public void setTextStyleName(String textClassName) {
    this.textClassName = textClassName;
    onTextStyleChange();
  }

  /**
   * Sets whether the text is visible over the bar.
   *
   * @param textVisible True to show text, false to hide it
   */
  public void setTextVisible(boolean textVisible) {
    this.textVisible = textVisible;
    if (this.textVisible) {
      DOM.setStyleAttribute(textElement, "display", "");
      redraw();
    } else {
      DOM.setStyleAttribute(textElement, "display", "none");
    }
  }

  /**
   * Generate the text to display within the progress bar. Override this
   * function to change the default progress percent to a more informative
   * message, such as the number of kilobytes downloaded.
   *
   * @param curProgress the current progress
   * @return the text to display in the progress bar
   */
  protected String generateText(double curProgress) {
    if (textFormatter != null) {
      return textFormatter.getText(this, curProgress);
    } else {
      return (int) (100 * getPercent()) + "%";
    }
  }

  /**
   * Get the bar element.
   *
   * @return the bar element
   */
  protected Element getBarElement() {
    return barElement;
  }

  /**
   * Get the text element.
   *
   * @return the text element
   */
  protected Element getTextElement() {
    return textElement;
  }

  /**
   * This method is called immediately after a widget becomes attached to the
   * browser's document.
   */
  @Override
  protected void onLoad() {
    // Reset the position attribute of the parent element
    DOM.setStyleAttribute(getElement(), "position", "relative");
    ResizableWidgetCollection.get().add(this);
    redraw();
  }

  @Override
  protected void onUnload() {
    ResizableWidgetCollection.get().remove(this);
  }

  /**
   * Reset the progress text based on the current min and max progress range.
   */
  protected void resetProgress() {
    setProgress(getProgress());
  }

  private void onTextStyleChange() {
    int percent = (int) (100 * getPercent());
    updateTextStyle(percent);
  }

  private void updateTextStyle(int percent) {
    // Set the style depending on the size of the bar
    if (percent < 50) {
      DOM.setElementProperty(textElement, "className",
          textClassName + " " + textFirstHalfClassName);
    } else {
      DOM.setElementProperty(textElement, "className",
          textClassName + " " + textSecondHalfClassName);
    }
  }
}

//
//import com.google.gwt.user.client.ui.FlexTable;
//import com.google.gwt.user.client.ui.Grid;
//import com.google.gwt.user.client.ui.Label;
//import com.google.gwt.user.client.ui.VerticalPanel;
//
//public class ProgressBar extends VerticalPanel {
//
//	  /**
//	   * Option to show text label above progress bar
//	   */
//	  public static final int SHOW_TEXT = 2;
//	  
//	  /**
//	   * Option to show time remaining
//	   */
//	  public static final int SHOW_TIME_REMAINING = 1;
//
//	  /**
//	   * The time the progress bar was started
//	   */
//	  private long startTime = System.currentTimeMillis();
//	  
//	  /**
//	   * The number of bar elements to show
//	   */
//	  private int elements = 20;
//	  
//	  /**
//	   * Time element text
//	   */
//	  private String secondsMessage = "????? ????????: {0} ??????";
//	  private String minutesMessage = "????? ????????: {0} ?????";
//	  private String hoursMessage = "????? ????????: {0} ?????";
//	  
//	  /**
//	   * Current progress (as a percentage)
//	   */
//	  private int progress = 0;
//	  
//	  /**
//	   * This is the frame around the progress bar
//	   */
//	  private FlexTable barFrame = new FlexTable();
//	  
//	  /**
//	   * This is the grid used to show the elements
//	   */
//	  private Grid elementGrid;
//	  
//	  /**
//	   * This is the current text label below the progress bar
//	   */
//	  private Label remainLabel = new Label();
//
//	  /**
//	   * This is the current text label above the progress bar
//	   */
//	  private Label textLabel = new Label();
//	  
//	  /** 
//	   * internal flags for options
//	   */
//	  private boolean showRemaining = false;
//	  private boolean showText = false;
//	  
//	  /**
//	   * Base constructor for this widget
//	   * 
//	   * @param elements The number of elements (bars) to show on the progress bar
//	   * @param options The display options for the progress bar
//	   */
//	  public ProgressBar (int elements, int options)
//	    {
//	        // Read the options and set convenience variables
//	        if ((options & SHOW_TIME_REMAINING) == SHOW_TIME_REMAINING) showRemaining = true;
//	        if ((options & SHOW_TEXT) == SHOW_TEXT) showText = true;
//	    
//	    // Set element count
//	    this.elements = elements;
//	    				
//	    // Styling
//	    remainLabel.setStyleName("progressbar-remaining");
//	    textLabel.setStyleName("progressbar-text");
//	    
//	    // Initialize the progress elements
//	    elementGrid = new Grid(1, elements);
//	        elementGrid.setStyleName("progressbar-inner");
//	        elementGrid.setCellPadding(0);
//	        elementGrid.setCellSpacing(0);
//	        
//	        for (int loop = 0; loop < elements; loop++) {
//	            Grid elm = new Grid(1, 1);
//	            //elm.setHTML(0, 0, "&nbsp;");
//	            elm.setHTML(0, 0, "");
//	            elm.setStyleName("progressbar-blankbar");
//	            elm.addStyleName("progressbar-bar");
//	            elementGrid.setWidget(0, loop, elm);
//	    }
//	    
//	    // Create the container around the elements
//	    Grid containerGrid = new Grid(1,1);
//	        containerGrid.setCellPadding(0);
//	        containerGrid.setCellSpacing(0);
//	    containerGrid.setWidget(0, 0, elementGrid);
//	    containerGrid.setStyleName("progressbar-outer");
//	      //containerGrid.setBorderWidth(1);
//	      
//	      // Set up the surrounding flex table based on the options
//	      int row = 0;
//	        if (showText) barFrame.setWidget(row++, 0, textLabel);
//	        barFrame.setWidget(row++, 0, containerGrid);
//	        if (showRemaining) barFrame.setWidget(row++, 0, remainLabel);
//
//	    barFrame.setWidth("100%");
//	    
//	    // Add the frame to the panel
//	    this.add(barFrame);
//	    
//	    // Initialize progress bar
//	    setProgress(0);
//	  }
//
//	  /**
//	   * Constructor without options
//	   * 
//	   * @param elements The number of elements (bars) to show on the progress bar
//	   */
//	  public ProgressBar (int elements)
//	    {
//	        this(elements, 0);
//	    }
//	  
//	  /**
//	   * Set the current progress as a percentage
//	   * 
//	   * @param percentage Set current percentage for the progress bar
//	   */
//	  public void setProgress (int percentage)
//	    {
//	    // Make sure we are error-tolerant
//	    if (percentage > 100) percentage = 100;
//	        if (percentage < 0) percentage = 0;
//	    
//	    // Set the internal variable
//	    progress = percentage;
//	    
//	    // Update the elements in the progress grid to
//	        // reflect the status
//	        int completed = elements * percentage / 100;
//	        for (int loop = 0; loop < elements; loop++) {
//	            Grid elm = (Grid) elementGrid.getWidget(0, loop);
//	            if (loop < completed) {
//	                elm.setStyleName("progressbar-fullbar");
//	                elm.addStyleName("progressbar-bar");
//	            }
//	            else {
//	                elm.setStyleName("progressbar-blankbar");
//	                elm.addStyleName("progressbar-bar");
//	            }
//	        }
//	    
//	    if (percentage > 0) {
//	            // Calculate the new time remaining
//	            long soFar = (System.currentTimeMillis() - startTime) / 1000;
//	            long remaining = soFar * (100 - percentage) / percentage;
//	            // Select the best UOM
//	            String remainText = secondsMessage;
//	            if (remaining > 120) {
//	                remaining = remaining / 60;
//	                remainText = minutesMessage;
//	                if (remaining > 120) {
//	                    remaining = remaining / 60;
//	                    remainText = hoursMessage;
//	                }
//	            }
//	            // Locate the position to insert out time remaining
//	            int pos = remainText.indexOf("{0}");
//	            if (pos >= 0) {
//	                String trail = "";
//	                if (pos + 3 < remainText.length()) trail = remainText.substring(pos + 3);
//	                remainText = remainText.substring(0, pos) + remaining + trail;
//	            }
//	            // Set the label
//	            remainLabel.setText(remainText);
//	        }
//	        else {
//	            // If progress is 0, reset the start time
//	            startTime = System.currentTimeMillis();
//	        }
//	  }
//
//	  /**
//	   * Get the current progress as a percentage
//	   * 
//	   * @return Current percentage for the progress bar
//	   */
//	  public int getProgress ()
//	    {
//	        return (progress);
//	    }
//
//	  /**
//	   * Get the text displayed above the progress bar
//	   * 
//	   * @return the text
//	   */
//	  public String getText ()
//	    {
//	        return this.textLabel.getText();
//	    }
//
//	  /**
//	   * Set the text displayed above the progress bar
//	   * 
//	   * @param text the text to set
//	   */
//	  public void setText (String text)
//	    {
//	        this.textLabel.setText(text);
//	    }
//
//	  /**
//	   * Get the message used to format the time remaining text
//	   * for hours
//	   * 
//	   * @return the hours message
//	   */
//	  public String getHoursMessage ()
//	    {
//	        return hoursMessage;
//	    }
//
//	  /**
//	   * Set the message used to format the time remaining text
//	   * below the progress bar. There are 3 messages used for
//	   * hours, minutes and seconds respectively.
//	   * 
//	   * The message must contain a placeholder for the value. The
//	   * placeholder must be {0}. For example, the following is
//	   * a valid message:
//	   * 
//	   *     "Hours remaining: {0}"
//	   * 
//	   * @param hoursMessage the hours message to set
//	   */
//	  public void setHoursMessage (String hoursMessage)
//	    {
//	        this.hoursMessage = hoursMessage;
//	    }
//
//	  /**
//	   * Get the message used to format the time remaining text
//	   * for minutes
//	   * 
//	   * @return the minutesMessage
//	   */
//	  public String getMinutesMessage ()
//	    {
//	        return minutesMessage;
//	    }
//
//	  /**
//	   * Set the message used to format the time remaining text
//	   * below the progress bar. There are 3 messages used for
//	   * hours, minutes and seconds respectively.
//	   * 
//	   * The message must contain a placeholder for the value. The
//	   * placeholder must be {0}. For example, the following is
//	   * a valid message:
//	   * 
//	   *     "Minutes remaining: {0}"
//	   * 
//	   * @param minutesMessage the minutes message to set
//	   */
//	  public void setMinutesMessage (String minutesMessage)
//	    {
//	        this.minutesMessage = minutesMessage;
//	    }
//
//	  /**
//	   * Get the message used to format the time remaining text
//	   * for seconds
//	   * 
//	   * @return the secondsMessage
//	   */
//	  public String getSecondsMessage ()
//	    {
//	        return secondsMessage;
//	    }
//
//	  /**
//	   * Set the message used to format the time remaining text
//	   * below the progress bar. There are 3 messages used for
//	   * hours, minutes and seconds respectively.
//	   * 
//	   * The message must contain a placeholder for the value. The
//	   * placeholder must be {0}. For example, the following is
//	   * a valid message:
//	   * 
//	   *     "Seconds remaining: {0}"
//	   * 
//	   * @param secondsMessage the secondsMessage to set
//	   */
//	  public void setSecondsMessage (String secondsMessage)
//	    {
//	        this.secondsMessage = secondsMessage;
//	    }
//
//	  
//	}
