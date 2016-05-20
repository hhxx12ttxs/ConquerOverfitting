/*
 * Copyright (c) 2005-2010 Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Substance Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.substance.internal.ui;

import java.awt.Adjustable;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;

import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.substance.api.ColorSchemeAssociationKind;
import org.pushingpixels.substance.api.ComponentState;
import org.pushingpixels.substance.api.SubstanceColorScheme;
import org.pushingpixels.substance.api.SubstanceConstants;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceConstants.ScrollPaneButtonPolicyKind;
import org.pushingpixels.substance.api.SubstanceConstants.Side;
import org.pushingpixels.substance.api.painter.border.SubstanceBorderPainter;
import org.pushingpixels.substance.api.painter.fill.SubstanceFillPainter;
import org.pushingpixels.substance.api.shaper.ClassicButtonShaper;
import org.pushingpixels.substance.api.shaper.SubstanceButtonShaper;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.animation.TransitionAwareUI;
import org.pushingpixels.substance.internal.painter.BackgroundPaintingUtils;
import org.pushingpixels.substance.internal.painter.SimplisticFillPainter;
import org.pushingpixels.substance.internal.painter.SimplisticSoftBorderPainter;
import org.pushingpixels.substance.internal.utils.HashMapKey;
import org.pushingpixels.substance.internal.utils.LazyResettableHashMap;
import org.pushingpixels.substance.internal.utils.RolloverControlListener;
import org.pushingpixels.substance.internal.utils.SubstanceColorSchemeUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceCoreUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceImageCreator;
import org.pushingpixels.substance.internal.utils.SubstanceOutlineUtilities;
import org.pushingpixels.substance.internal.utils.SubstanceSizeUtils;
import org.pushingpixels.substance.internal.utils.icon.ArrowButtonTransitionAwareIcon;
import org.pushingpixels.substance.internal.utils.scroll.SubstanceScrollButton;

/**
 * UI for scroll bars in <b>Substance </b> look and feel.
 * 
 * @author Kirill Grouchnikov
 */
public class SubstanceScrollBarUI extends BasicScrollBarUI implements
		TransitionAwareUI {
	/**
	 * The second decrease button. Is shown under
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#ADJACENT},
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#MULTIPLE} and
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#MULTIPLE_BOTH}
	 * modes.
	 * 
	 * @since version 3.1
	 */
	protected JButton mySecondDecreaseButton;

	/**
	 * The second increase button. Is shown only under
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#MULTIPLE_BOTH} mode.
	 * 
	 * @since version 3.1
	 */
	protected JButton mySecondIncreaseButton;

	/**
	 * Surrogate button model for tracking the thumb transitions.
	 */
	private ButtonModel thumbModel;

	/**
	 * Stores computed images for vertical thumbs.
	 */
	private static LazyResettableHashMap<BufferedImage> thumbVerticalMap = new LazyResettableHashMap<BufferedImage>(
			"SubstanceScrollBarUI.thumbVertical");

	/**
	 * Stores computed images for horizontal thumbs.
	 */
	private static LazyResettableHashMap<BufferedImage> thumbHorizontalMap = new LazyResettableHashMap<BufferedImage>(
			"SubstanceScrollBarUI.thumbHorizontal");

	/**
	 * Stores computed images for full vertical tracks under
	 * {@link DefaultControlBackgroundComposite}.
	 */
	private static LazyResettableHashMap<BufferedImage> trackFullVerticalMap = new LazyResettableHashMap<BufferedImage>(
			"SubstanceScrollBarUI.trackFullVertical");

	/**
	 * Stores computed images for full horizontal tracks under
	 * {@link DefaultControlBackgroundComposite}.
	 */
	private static LazyResettableHashMap<BufferedImage> trackFullHorizontalMap = new LazyResettableHashMap<BufferedImage>(
			"SubstanceScrollBarUI.trackFullHorizontal");

	/**
	 * Mouse listener on the associated scroll bar.
	 */
	private MouseListener substanceMouseListener;

	/**
	 * Listener for thumb transition animations.
	 */
	private RolloverControlListener substanceThumbRolloverListener;

	protected StateTransitionTracker compositeStateTransitionTracker;

	/**
	 * Property change listener.
	 * 
	 */
	private PropertyChangeListener substancePropertyListener;

	/**
	 * Scroll bar width.
	 */
	protected int scrollBarWidth;

	/**
	 * Cache of images for horizontal tracks.
	 */
	private static LazyResettableHashMap<BufferedImage> trackHorizontalMap = new LazyResettableHashMap<BufferedImage>(
			"SubstanceScrollBarUI.trackHorizontal");

	/**
	 * Cache of images for vertical tracks.
	 */
	private static LazyResettableHashMap<BufferedImage> trackVerticalMap = new LazyResettableHashMap<BufferedImage>(
			"SubstanceScrollBarUI.trackVertical");

	/**
	 * Listener on adjustments made to the scrollbar model - this is for the
	 * overlay mode (see {@link SubstanceLookAndFeel#OVERLAY_PROPERTY} and
	 * repaiting both scrollbars with the viewport.
	 * 
	 * @since version 3.2
	 */
	protected AdjustmentListener substanceAdjustmentListener;

	/**
	 * Surrogate model to sync between rollover effects of scroll buttons and
	 * scroll track / scroll thumb.
	 * 
	 * @since version 3.2
	 */
	protected CompositeButtonModel compositeScrollTrackModel;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent comp) {
		SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
		return new SubstanceScrollBarUI(comp);
	}

	/**
	 * Simple constructor.
	 * 
	 * @param b
	 *            Associated component.
	 */
	protected SubstanceScrollBarUI(JComponent b) {
		super();
		this.thumbModel = new DefaultButtonModel();
		this.thumbModel.setArmed(false);
		this.thumbModel.setSelected(false);
		this.thumbModel.setPressed(false);
		this.thumbModel.setRollover(false);

		b.setOpaque(false);
	}

	/**
	 * Creates a decrease button.
	 * 
	 * @param orientation
	 *            Button orientation.
	 * @param isRegular
	 *            if <code>true</code>, the regular (upper / left) decrease
	 *            button is created, if <code>false</code>, the additional
	 *            (lower / right) decrease button is created for
	 *            {@link SubstanceConstants.ScrollPaneButtonPolicyKind#ADJACENT}
	 *            ,
	 *            {@link SubstanceConstants.ScrollPaneButtonPolicyKind#MULTIPLE}
	 *            and
	 *            {@link SubstanceConstants.ScrollPaneButtonPolicyKind#MULTIPLE_BOTH}
	 *            kinds.
	 * @return Decrease button.
	 */
	protected JButton createGeneralDecreaseButton(final int orientation,
			boolean isRegular) {
		JButton result = new SubstanceScrollButton(orientation);
		result.setName("Decrease " + (isRegular ? "regular" : "additional"));
		result.setFont(this.scrollbar.getFont());
		Icon icon = new ArrowButtonTransitionAwareIcon(result, orientation);
		result.setIcon(icon);
		result.setFont(scrollbar.getFont());

		result.setPreferredSize(new Dimension(this.scrollBarWidth,
				this.scrollBarWidth));

		Set<Side> openSides = EnumSet.noneOf(Side.class);
		Set<Side> straightSides = EnumSet.noneOf(Side.class);
		switch (orientation) {
		case NORTH:
			openSides.add(Side.BOTTOM);
			if (!isRegular)
				openSides.add(Side.TOP);
			if (isRegular)
				straightSides.add(Side.TOP);
			break;
		case EAST:
			openSides.add(Side.LEFT);
			if (!isRegular)
				openSides.add(Side.RIGHT);
			if (isRegular)
				straightSides.add(Side.RIGHT);
			break;
		case WEST:
			openSides.add(Side.RIGHT);
			if (!isRegular)
				openSides.add(Side.LEFT);
			if (isRegular)
				straightSides.add(Side.LEFT);
			break;
		}
		result.putClientProperty(
				SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY, openSides);
		result.putClientProperty(SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
				straightSides);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#createDecreaseButton(int)
	 */
	@Override
	protected JButton createDecreaseButton(int orientation) {
		return this.createGeneralDecreaseButton(orientation, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#createIncreaseButton(int)
	 */
	@Override
	protected JButton createIncreaseButton(int orientation) {
		return this.createGeneralIncreaseButton(orientation, true);
	}

	/**
	 * Creates a increase button.
	 * 
	 * @param orientation
	 *            Button orientation.
	 * @param isRegular
	 *            if <code>true</code>, the regular (lower / right) increase
	 *            button is created, if <code>false</code>, the additional
	 *            (upper / left) increase button is created for
	 *            {@link SubstanceConstants.ScrollPaneButtonPolicyKind#MULTIPLE_BOTH}
	 *            kind.
	 * @return Increase button.
	 */
	protected JButton createGeneralIncreaseButton(final int orientation,
			boolean isRegular) {
		JButton result = new SubstanceScrollButton(orientation);
		result.setName("Increase " + (isRegular ? "regular" : "additional"));
		result.setFont(this.scrollbar.getFont());
		Icon icon = new ArrowButtonTransitionAwareIcon(result, orientation);
		result.setIcon(icon);
		result.setFont(scrollbar.getFont());
		// JButton result = new SubstanceScrollBarButton(icon, orientation);
		result.setPreferredSize(new Dimension(this.scrollBarWidth,
				this.scrollBarWidth));

		Set<Side> openSides = EnumSet.noneOf(Side.class);
		Set<Side> straightSides = EnumSet.noneOf(Side.class);
		switch (orientation) {
		case SOUTH:
			openSides.add(Side.TOP);
			if (!isRegular)
				openSides.add(Side.BOTTOM);
			if (isRegular)
				straightSides.add(Side.BOTTOM);
			break;
		case EAST:
			openSides.add(Side.LEFT);
			if (!isRegular)
				openSides.add(Side.RIGHT);
			if (isRegular)
				straightSides.add(Side.RIGHT);
			break;
		case WEST:
			openSides.add(Side.RIGHT);
			if (!isRegular)
				openSides.add(Side.LEFT);
			if (isRegular)
				straightSides.add(Side.LEFT);
			break;
		}
		result.putClientProperty(
				SubstanceLookAndFeel.BUTTON_OPEN_SIDE_PROPERTY, openSides);
		result.putClientProperty(SubstanceLookAndFeel.BUTTON_SIDE_PROPERTY,
				straightSides);
		return result;
	}

	/**
	 * Returns the image for a horizontal track.
	 * 
	 * @param trackBounds
	 *            Track bounds.
	 * @param leftActiveButton
	 *            The closest left button in the scroll bar. May be
	 *            <code>null</code>.
	 * @param rightActiveButton
	 *            The closest right button in the scroll bar. May be
	 *            <code>null</code> .
	 * @return Horizontal track image.
	 */
	private void paintTrackHorizontal(Graphics g, Rectangle trackBounds,
			SubstanceScrollButton leftActiveButton,
			SubstanceScrollButton rightActiveButton) {
		int width = Math.max(1, trackBounds.width);
		int height = Math.max(1, trackBounds.height);

		paintTrackBackHorizontal(g, this.scrollbar, leftActiveButton,
				rightActiveButton, width, height);
		BufferedImage horizontalTrack = getTrackHorizontal(this.scrollbar,
				width, height);
		g.drawImage(horizontalTrack, 0, 0, null);
	}

	/**
	 * Returns the image for a horizontal track.
	 * 
	 * @param scrollBar
	 *            Scroll bar.
	 * @param trackBounds
	 *            Track bounds.
	 * @param compLeftState
	 *            The state of the left button in the scroll bar.
	 * @param compRightState
	 *            The state of the closest right button in the scroll bar.
	 * @param width
	 *            Scroll track width.
	 * @param height
	 *            Scroll track height.
	 * @param graphicsComposite
	 *            Composite to apply before painting the track.
	 * @return Horizontal track image.
	 */
	private static BufferedImage getTrackHorizontal(JScrollBar scrollBar,
			int width, int height) {
		SubstanceButtonShaper shaper = SubstanceCoreUtilities
				.getButtonShaper(scrollBar);
		SubstanceColorScheme mainScheme = SubstanceColorSchemeUtilities
				.getColorScheme(scrollBar,
						scrollBar.isEnabled() ? ComponentState.ENABLED
								: ComponentState.DISABLED_UNSELECTED);
		SubstanceColorScheme mainBorderScheme = SubstanceColorSchemeUtilities
				.getColorScheme(scrollBar, ColorSchemeAssociationKind.BORDER,
						scrollBar.isEnabled() ? ComponentState.ENABLED
								: ComponentState.DISABLED_UNSELECTED);
		HashMapKey key = SubstanceCoreUtilities.getHashKey(mainScheme
				.getDisplayName(), mainBorderScheme.getDisplayName(), width,
				height, shaper.getDisplayName());
		float radius = height / 2;
		if (shaper instanceof ClassicButtonShaper)
			radius = SubstanceSizeUtils
					.getClassicButtonCornerRadius(SubstanceSizeUtils
							.getComponentFontSize(scrollBar));

		int borderDelta = (int) Math.floor(SubstanceSizeUtils
				.getBorderStrokeWidth(SubstanceSizeUtils
						.getComponentFontSize(scrollBar)) / 2.0);
		Shape contour = SubstanceOutlineUtilities.getBaseOutline(width, height,
				radius, null, borderDelta);
		BufferedImage result = SubstanceScrollBarUI.trackHorizontalMap.get(key);
		if (result == null) {
			result = SubstanceCoreUtilities.getBlankImage(width, height);
			SimplisticFillPainter.INSTANCE.paintContourBackground(result
					.createGraphics(), scrollBar, width, height, contour,
					false, mainScheme, true);

			SubstanceBorderPainter borderPainter = new SimplisticSoftBorderPainter();
			borderPainter.paintBorder(result.getGraphics(), scrollBar, width,
					height, contour, null, mainBorderScheme);

			SubstanceScrollBarUI.trackHorizontalMap.put(key, result);
		}
		return result;
	}

	/**
	 * Returns the image for a horizontal track.
	 * 
	 * @param scrollBar
	 *            Scroll bar.
	 * @param trackBounds
	 *            Track bounds.
	 * @param leftActiveButton
	 *            The closest left button in the scroll bar. May be
	 *            <code>null</code>.
	 * @param rightActiveButton
	 *            The closest right button in the scroll bar. May be
	 *            <code>null</code> .
	 * @param width
	 *            Scroll track width.
	 * @param height
	 *            Scroll track height.
	 * @param graphicsComposite
	 *            Composite to apply before painting the track.
	 * @return Horizontal track image.
	 */
	private static void paintTrackBackHorizontal(Graphics g,
			JScrollBar scrollBar, AbstractButton leftActiveButton,
			AbstractButton rightActiveButton, int width, int height) {
		SubstanceButtonShaper shaper = SubstanceCoreUtilities
				.getButtonShaper(scrollBar);
		int radius = height / 2;
		if (shaper instanceof ClassicButtonShaper)
			radius = 2;
		SubstanceImageCreator.paintCompositeRoundedBackground(scrollBar, g,
				width, height, radius, leftActiveButton, rightActiveButton,
				false);
	}

	/**
	 * Returns the image for a vertical track.
	 * 
	 * @param trackBounds
	 *            Track bounds.
	 * @param scrollBar
	 *            Scroll bar.
	 * @param topActiveButton
	 *            The closest top button in the scroll bar. May be
	 *            <code>null</code>.
	 * @param bottomActiveButton
	 *            The closest bottom button in the scroll bar. May be
	 *            <code>null</code>.
	 * @return Vertical track image.
	 */
	private void paintTrackVertical(Graphics g, Rectangle trackBounds,
			SubstanceScrollButton topActiveButton,
			SubstanceScrollButton bottomActiveButton) {

		int width = Math.max(1, trackBounds.width);
		int height = Math.max(1, trackBounds.height);

		paintTrackBackVertical(g, this.scrollbar, topActiveButton,
				bottomActiveButton, width, height);
		BufferedImage horizontalTrack = getTrackVertical(this.scrollbar, width,
				height);
		g.drawImage(horizontalTrack, 0, 0, null);
	}

	/**
	 * Returns the image for a vertical track.
	 * 
	 * @param trackBounds
	 *            Track bounds.
	 * @param scrollBar
	 *            Scroll bar.
	 * @param compTopState
	 *            The state of the top button in the scroll bar.
	 * @param compBottomState
	 *            The state of the closest bottom button in the scroll bar.
	 * @param width
	 *            Scroll track width.
	 * @param height
	 *            Scroll track height.
	 * @param graphicsComposite
	 *            Composite to apply before painting the track.
	 * @return Vertical track image.
	 */
	private static BufferedImage getTrackVertical(JScrollBar scrollBar,
			int width, int height) {
		SubstanceButtonShaper shaper = SubstanceCoreUtilities
				.getButtonShaper(scrollBar);
		SubstanceColorScheme mainScheme = SubstanceColorSchemeUtilities
				.getColorScheme(scrollBar,
						scrollBar.isEnabled() ? ComponentState.ENABLED
								: ComponentState.DISABLED_UNSELECTED);
		SubstanceColorScheme mainBorderScheme = SubstanceColorSchemeUtilities
				.getColorScheme(scrollBar, ColorSchemeAssociationKind.BORDER,
						scrollBar.isEnabled() ? ComponentState.ENABLED
								: ComponentState.DISABLED_UNSELECTED);
		HashMapKey key = SubstanceCoreUtilities.getHashKey(mainScheme
				.getDisplayName(), mainBorderScheme.getDisplayName(), width,
				height, shaper.getDisplayName());
		BufferedImage result = SubstanceScrollBarUI.trackVerticalMap.get(key);
		if (result == null) {
			float radius = width / 2;
			if (shaper instanceof ClassicButtonShaper)
				radius = SubstanceSizeUtils
						.getClassicButtonCornerRadius(SubstanceSizeUtils
								.getComponentFontSize(scrollBar));

			int borderDelta = (int) Math.floor(SubstanceSizeUtils
					.getBorderStrokeWidth(SubstanceSizeUtils
							.getComponentFontSize(scrollBar)) / 2.0);
			Shape contour = SubstanceOutlineUtilities.getBaseOutline(height,
					width, radius, null, borderDelta);

			result = SubstanceCoreUtilities.getBlankImage(height, width);
			SimplisticFillPainter.INSTANCE.paintContourBackground(result
					.createGraphics(), scrollBar, height, width, contour,
					false, mainScheme, true);

			SubstanceBorderPainter borderPainter = new SimplisticSoftBorderPainter();
			borderPainter.paintBorder(result.getGraphics(), scrollBar, height,
					width, contour, null, mainBorderScheme);
			result = SubstanceImageCreator.getRotated(result, 3);

			SubstanceScrollBarUI.trackVerticalMap.put(key, result);
		}
		return result;
	}

	/**
	 * Returns the image for a vertical track.
	 * 
	 * @param trackBounds
	 *            Track bounds.
	 * @param scrollBar
	 *            Scroll bar.
	 * @param topActiveButton
	 *            The closest top button in the scroll bar. May be
	 *            <code>null</code>.
	 * @param bottomActiveButton
	 *            The closest bottom button in the scroll bar. May be
	 *            <code>null</code>.
	 * @param width
	 *            Scroll track width.
	 * @param height
	 *            Scroll track height.
	 * @param graphicsComposite
	 *            Composite to apply before painting the track.
	 * @return Vertical track image.
	 */
	private static void paintTrackBackVertical(Graphics g,
			JScrollBar scrollBar, AbstractButton topActiveButton,
			AbstractButton bottomActiveButton, int width, int height) {
		SubstanceButtonShaper shaper = SubstanceCoreUtilities
				.getButtonShaper(scrollBar);
		int radius = width / 2;
		if (shaper instanceof ClassicButtonShaper)
			radius = 2;

		Graphics2D g2d = (Graphics2D) g.create();
		AffineTransform at = AffineTransform.getTranslateInstance(0, height);
		at.rotate(-Math.PI / 2);
		g2d.transform(at);
		SubstanceImageCreator.paintCompositeRoundedBackground(scrollBar, g2d,
				height, width, radius, topActiveButton, bottomActiveButton,
				true);
		g2d.dispose();
	}

	/**
	 * Retrieves image for vertical thumb.
	 * 
	 * @param thumbBounds
	 *            Thumb bounding rectangle.
	 * @return Image for vertical thumb.
	 */
	private BufferedImage getThumbVertical(Rectangle thumbBounds) {
		int width = Math.max(1, thumbBounds.width);
		int height = Math.max(1, thumbBounds.height);

		StateTransitionTracker.ModelStateInfo modelStateInfo = this.compositeStateTransitionTracker
				.getModelStateInfo();
		ComponentState currState = modelStateInfo.getCurrModelState();

		// enabled scroll bar is always painted as active
		SubstanceColorScheme baseFillScheme = (currState != ComponentState.ENABLED) ? SubstanceColorSchemeUtilities
				.getColorScheme(this.scrollbar, currState)
				: SubstanceColorSchemeUtilities.getActiveColorScheme(
						this.scrollbar, currState);
		SubstanceColorScheme baseBorderScheme = SubstanceColorSchemeUtilities
				.getColorScheme(this.scrollbar,
						ColorSchemeAssociationKind.BORDER, currState);
		BufferedImage baseLayer = getThumbVertical(this.scrollbar, width,
				height, baseFillScheme, baseBorderScheme);

		Map<ComponentState, StateTransitionTracker.StateContributionInfo> activeStates = modelStateInfo
				.getStateContributionMap();
		if (currState.isDisabled() || (activeStates.size() == 1)) {
			return baseLayer;
		}

		BufferedImage result = SubstanceCoreUtilities.getBlankImage(baseLayer
				.getWidth(), baseLayer.getHeight());
		Graphics2D g2d = result.createGraphics();
		g2d.drawImage(baseLayer, 0, 0, null);

		for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : activeStates
				.entrySet()) {
			ComponentState activeState = activeEntry.getKey();
			if (activeState == modelStateInfo.getCurrModelState())
				continue;

			float contribution = activeEntry.getValue().getContribution();
			if (contribution == 0.0f)
				continue;

			g2d.setComposite(AlphaComposite.SrcOver.derive(contribution));

			SubstanceColorScheme fillScheme = (activeState != ComponentState.ENABLED) ? SubstanceColorSchemeUtilities
					.getColorScheme(this.scrollbar, activeState)
					: SubstanceColorSchemeUtilities.getActiveColorScheme(
							this.scrollbar, activeState);
			SubstanceColorScheme borderScheme = SubstanceColorSchemeUtilities
					.getColorScheme(this.scrollbar,
							ColorSchemeAssociationKind.BORDER, activeState);
			BufferedImage layer = getThumbVertical(this.scrollbar, width,
					height, fillScheme, borderScheme);
			g2d.drawImage(layer, 0, 0, null);
		}

		g2d.dispose();
		return result;
	}

	/**
	 * Retrieves image for vertical thumb.
	 * 
	 * @param scrollBar
	 *            Scroll bar.
	 * @param width
	 *            Thumb width.
	 * @param height
	 *            Thumb height.
	 * @param kind
	 *            Color scheme kind.
	 * @param cyclePos
	 *            Cycle position.
	 * @param scheme
	 *            The first color scheme.
	 * @param scheme2
	 *            The second color scheme.
	 * @param borderScheme
	 *            The first border color scheme.
	 * @param borderScheme2
	 *            The second border color scheme.
	 * @return Image for vertical thumb.
	 */
	private static BufferedImage getThumbVertical(JScrollBar scrollBar,
			int width, int height, SubstanceColorScheme scheme,
			SubstanceColorScheme borderScheme) {
		SubstanceFillPainter painter = SubstanceCoreUtilities
				.getFillPainter(scrollBar);
		SubstanceButtonShaper shaper = SubstanceCoreUtilities
				.getButtonShaper(scrollBar);
		SubstanceBorderPainter borderPainter = SubstanceCoreUtilities
				.getBorderPainter(scrollBar);
		HashMapKey key = SubstanceCoreUtilities.getHashKey(width, height,
				scheme.getDisplayName(), borderScheme.getDisplayName(), painter
						.getDisplayName(), shaper.getDisplayName(),
				borderPainter.getDisplayName());
		BufferedImage result = SubstanceScrollBarUI.thumbVerticalMap.get(key);
		if (result == null) {
			// System.out.println("Cache miss - computing");
			// System.out.println("New image for vertical thumb");
			float radius = width / 2;
			if (shaper instanceof ClassicButtonShaper)
				radius = SubstanceSizeUtils
						.getClassicButtonCornerRadius(SubstanceSizeUtils
								.getComponentFontSize(scrollBar));

			int borderDelta = (int) Math.floor(SubstanceSizeUtils
					.getBorderStrokeWidth(SubstanceSizeUtils
							.getComponentFontSize(scrollBar)) / 2.0);
			GeneralPath contour = SubstanceOutlineUtilities.getBaseOutline(
					height, width, radius, null, borderDelta);

			result = SubstanceCoreUtilities.getBlankImage(height, width);
			painter.paintContourBackground(result.createGraphics(), scrollBar,
					height, width, contour, false, scheme, true);

			// int borderThickness = (int) SubstanceSizeUtils
			// .getBorderStrokeWidth(SubstanceSizeUtils
			// .getComponentFontSize(scrollBar));
			// GeneralPath contourInner = SubstanceOutlineUtilities
			// .getBaseOutline(height, width, radius, null,
			// borderThickness + borderDelta);
			borderPainter.paintBorder(result.getGraphics(), scrollBar, height,
					width, contour, null, borderScheme);
			result = SubstanceImageCreator.getRotated(result, 3);
			// System.out.println(key);
			SubstanceScrollBarUI.thumbVerticalMap.put(key, result);
		}

		return result;
	}

	/**
	 * Retrieves image for horizontal thumb.
	 * 
	 * @param thumbBounds
	 *            Thumb bounding rectangle.
	 * @return Image for horizontal thumb.
	 */
	private BufferedImage getThumbHorizontal(Rectangle thumbBounds) {
		int width = Math.max(1, thumbBounds.width);
		int height = Math.max(1, thumbBounds.height);

		StateTransitionTracker.ModelStateInfo modelStateInfo = this.compositeStateTransitionTracker
				.getModelStateInfo();
		ComponentState currState = modelStateInfo.getCurrModelState();
		// enabled scroll bar is always painted as active
		// if (currState == ComponentState.ENABLED)
		// currState = ComponentState.SELECTED;

		SubstanceColorScheme baseFillScheme = (currState != ComponentState.ENABLED) ? SubstanceColorSchemeUtilities
				.getColorScheme(this.scrollbar, currState)
				: SubstanceColorSchemeUtilities.getActiveColorScheme(
						this.scrollbar, currState);
		SubstanceColorScheme baseBorderScheme = SubstanceColorSchemeUtilities
				.getColorScheme(this.scrollbar,
						ColorSchemeAssociationKind.BORDER, currState);
		BufferedImage baseLayer = getThumbHorizontal(this.scrollbar, width,
				height, baseFillScheme, baseBorderScheme);

		Map<ComponentState, StateTransitionTracker.StateContributionInfo> activeStates = modelStateInfo
				.getStateContributionMap();
		if (currState.isDisabled() || (activeStates.size() == 1)) {
			return baseLayer;
		}

		BufferedImage result = SubstanceCoreUtilities.getBlankImage(baseLayer
				.getWidth(), baseLayer.getHeight());
		Graphics2D g2d = result.createGraphics();
		g2d.drawImage(baseLayer, 0, 0, null);

		for (Map.Entry<ComponentState, StateTransitionTracker.StateContributionInfo> activeEntry : activeStates
				.entrySet()) {
			ComponentState activeState = activeEntry.getKey();
			if (activeState == modelStateInfo.getCurrModelState())
				continue;
			// if (activeState == ComponentState.ENABLED)
			// activeState = ComponentState.SELECTED;

			float contribution = activeEntry.getValue().getContribution();
			if (contribution == 0.0f)
				continue;

			g2d.setComposite(AlphaComposite.SrcOver.derive(contribution));

			SubstanceColorScheme fillScheme = (activeState != ComponentState.ENABLED) ? SubstanceColorSchemeUtilities
					.getColorScheme(this.scrollbar, activeState)
					: SubstanceColorSchemeUtilities.getActiveColorScheme(
							this.scrollbar, activeState);
			SubstanceColorScheme borderScheme = SubstanceColorSchemeUtilities
					.getColorScheme(this.scrollbar,
							ColorSchemeAssociationKind.BORDER, activeState);
			BufferedImage layer = getThumbHorizontal(this.scrollbar, width,
					height, fillScheme, borderScheme);
			g2d.drawImage(layer, 0, 0, null);
		}

		g2d.dispose();
		return result;
	}

	/**
	 * Retrieves image for horizontal thumb.
	 * 
	 * @param scrollBar
	 *            Scroll bar.
	 * @param width
	 *            Thumb width.
	 * @param height
	 *            Thumb height.
	 * @param kind
	 *            Color scheme kind.
	 * @param cyclePos
	 *            Cycle position.
	 * @param scheme
	 *            The first color scheme.
	 * @param scheme2
	 *            The second color scheme.
	 * @param borderScheme
	 *            The first border color scheme.
	 * @param borderScheme2
	 *            The second border color scheme.
	 * @return Image for horizontal thumb.
	 */
	private static BufferedImage getThumbHorizontal(JScrollBar scrollBar,
			int width, int height, SubstanceColorScheme scheme,
			SubstanceColorScheme borderScheme) {
		SubstanceFillPainter painter = SubstanceCoreUtilities
				.getFillPainter(scrollBar);
		SubstanceButtonShaper shaper = SubstanceCoreUtilities
				.getButtonShaper(scrollBar);
		SubstanceBorderPainter borderPainter = SubstanceCoreUtilities
				.getBorderPainter(scrollBar);
		HashMapKey key = SubstanceCoreUtilities.getHashKey(width, height,
				scheme.getDisplayName(), borderScheme.getDisplayName(), painter
						.getDisplayName(), shaper.getDisplayName(),
				borderPainter.getDisplayName());

		float radius = height / 2;
		if (shaper instanceof ClassicButtonShaper)
			radius = SubstanceSizeUtils
					.getClassicButtonCornerRadius(SubstanceSizeUtils
							.getComponentFontSize(scrollBar));
		int borderDelta = (int) Math.floor(SubstanceSizeUtils
				.getBorderStrokeWidth(SubstanceSizeUtils
						.getComponentFontSize(scrollBar)) / 2.0);
		GeneralPath contour = SubstanceOutlineUtilities.getBaseOutline(width,
				height, radius, null, borderDelta);
		BufferedImage opaque = SubstanceScrollBarUI.thumbHorizontalMap.get(key);
		if (opaque == null) {
			// System.out.println("New image for horizontal thumb");

			opaque = SubstanceCoreUtilities.getBlankImage(width, height);
			painter.paintContourBackground(opaque.createGraphics(), scrollBar,
					width, height, contour, false, scheme, true);

			borderPainter.paintBorder(opaque.getGraphics(), scrollBar, width,
					height, contour, null, borderScheme);
			SubstanceScrollBarUI.thumbHorizontalMap.put(key, opaque);
		}

		return opaque;
	}

	/**
	 * Returns the scroll button state.
	 * 
	 * @param scrollButton
	 *            Scroll button.
	 * @return Scroll button state.
	 */
	protected ComponentState getState(JButton scrollButton) {
		if (scrollButton == null)
			return null;

		ComponentState result = ((TransitionAwareUI) scrollButton.getUI())
				.getTransitionTracker().getModelStateInfo().getCurrModelState();
		if ((result == ComponentState.ENABLED)
				&& SubstanceCoreUtilities.hasFlatAppearance(this.scrollbar,
						false)) {
			result = null;
		}
		if (SubstanceCoreUtilities.isButtonNeverPainted(scrollButton)) {
			result = null;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicScrollBarUI#paintTrack(java.awt.Graphics,
	 * javax.swing.JComponent, java.awt.Rectangle)
	 */
	@Override
	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
		Graphics2D graphics = (Graphics2D) g.create();

		// System.out.println("Track");
		ScrollPaneButtonPolicyKind buttonPolicy = SubstanceCoreUtilities
				.getScrollPaneButtonsPolicyKind(this.scrollbar);
		SubstanceScrollButton compTopState = null;
		SubstanceScrollButton compBottomState = null;
		if (this.decrButton.isShowing() && this.incrButton.isShowing()
				&& this.mySecondDecreaseButton.isShowing()
				&& this.mySecondIncreaseButton.isShowing()) {
			switch (buttonPolicy) {
			case OPPOSITE:
				compTopState = (SubstanceScrollButton) this.decrButton;
				compBottomState = (SubstanceScrollButton) this.incrButton;
				break;
			case ADJACENT:
				compBottomState = (SubstanceScrollButton) this.mySecondDecreaseButton;
				break;
			case MULTIPLE:
				compTopState = (SubstanceScrollButton) this.decrButton;
				compBottomState = (SubstanceScrollButton) this.mySecondDecreaseButton;
				break;
			case MULTIPLE_BOTH:
				compTopState = (SubstanceScrollButton) this.mySecondIncreaseButton;
				compBottomState = (SubstanceScrollButton) this.mySecondDecreaseButton;
				break;
			}
		}

		graphics.translate(trackBounds.x, trackBounds.y);
		if (this.scrollbar.getOrientation() == Adjustable.VERTICAL) {
			paintTrackVertical(graphics, trackBounds, compTopState,
					compBottomState);
		} else {
			if (this.scrollbar.getComponentOrientation().isLeftToRight()) {
				paintTrackHorizontal(graphics, trackBounds, compTopState,
						compBottomState);
			} else {
				paintTrackHorizontal(graphics, trackBounds, compBottomState,
						compTopState);
			}
			// BufferedImage bi = this.scrollbar.getComponentOrientation()
			// .isLeftToRight() ? this.getTrackHorizontal(trackBounds,
			// compTopState, compBottomState) : this.getTrackHorizontal(
			// trackBounds, compBottomState, compTopState);
			// graphics.drawImage(bi, trackBounds.x, trackBounds.y, null);
		}

		graphics.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicScrollBarUI#paintThumb(java.awt.Graphics,
	 * javax.swing.JComponent, java.awt.Rectangle)
	 */
	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
		// System.out.println("Thumb");
		Graphics2D graphics = (Graphics2D) g.create();
		// ControlBackgroundComposite composite = SubstanceCoreUtilities
		// .getControlBackgroundComposite(this.scrollbar);

		// JScrollBar scrollBar = (JScrollBar) c;
		this.thumbModel.setSelected(this.thumbModel.isSelected()
				|| this.isDragging);
		this.thumbModel.setEnabled(c.isEnabled());
		boolean isVertical = (this.scrollbar.getOrientation() == Adjustable.VERTICAL);
		if (isVertical) {
			Rectangle adjustedBounds = new Rectangle(thumbBounds.x,
					thumbBounds.y, thumbBounds.width, thumbBounds.height);
			BufferedImage thumbImage = this.getThumbVertical(adjustedBounds);
			graphics.drawImage(thumbImage, adjustedBounds.x, adjustedBounds.y,
					null);
		} else {
			Rectangle adjustedBounds = new Rectangle(thumbBounds.x,
					thumbBounds.y, thumbBounds.width, thumbBounds.height);
			BufferedImage thumbImage = this.getThumbHorizontal(adjustedBounds);
			graphics.drawImage(thumbImage, adjustedBounds.x, adjustedBounds.y,
					null);
		}
		graphics.dispose();
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		Graphics2D graphics = (Graphics2D) g.create();
		BackgroundPaintingUtils.update(graphics, c, false);
		float alpha = SubstanceColorSchemeUtilities.getAlpha(this.scrollbar,
				ComponentState.getState(this.thumbModel, this.scrollbar));
		graphics
				.setComposite(LafWidgetUtilities.getAlphaComposite(c, alpha, g));
		super.paint(graphics, c);
		graphics.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#installDefaults()
	 */
	@Override
	protected void installDefaults() {
		super.installDefaults();
		this.scrollBarWidth = SubstanceSizeUtils
				.getScrollBarWidth(SubstanceSizeUtils
						.getComponentFontSize(this.scrollbar));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#installComponents()
	 */
	@Override
	protected void installComponents() {
		super.installComponents();
		switch (this.scrollbar.getOrientation()) {
		case JScrollBar.VERTICAL:
			this.mySecondDecreaseButton = this.createGeneralDecreaseButton(
					NORTH, false);
			this.mySecondIncreaseButton = this.createGeneralIncreaseButton(
					SOUTH, false);
			break;

		case JScrollBar.HORIZONTAL:
			if (this.scrollbar.getComponentOrientation().isLeftToRight()) {
				this.mySecondDecreaseButton = this.createGeneralDecreaseButton(
						WEST, false);
				this.mySecondIncreaseButton = this.createGeneralIncreaseButton(
						EAST, false);
			} else {
				this.mySecondDecreaseButton = this.createGeneralDecreaseButton(
						EAST, false);
				this.mySecondIncreaseButton = this.createGeneralIncreaseButton(
						WEST, false);
			}
			break;
		}
		this.scrollbar.add(this.mySecondDecreaseButton);
		this.scrollbar.add(this.mySecondIncreaseButton);

		this.compositeScrollTrackModel = new CompositeButtonModel(
				this.thumbModel, this.incrButton, this.decrButton,
				this.mySecondDecreaseButton, this.mySecondIncreaseButton);
		this.compositeScrollTrackModel.registerListeners();

		this.compositeStateTransitionTracker = new StateTransitionTracker(
				this.scrollbar, this.compositeScrollTrackModel);
		this.compositeStateTransitionTracker.registerModelListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#uninstallComponents()
	 */
	@Override
	protected void uninstallComponents() {
		this.compositeScrollTrackModel.unregisterListeners();
		this.compositeStateTransitionTracker.unregisterModelListeners();

		this.scrollbar.remove(this.mySecondDecreaseButton);
		this.scrollbar.remove(this.mySecondIncreaseButton);
		super.uninstallComponents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#installListeners()
	 */
	@Override
	protected void installListeners() {
		super.installListeners();
		this.substanceMouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				SubstanceScrollBarUI.this.scrollbar.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				SubstanceScrollBarUI.this.scrollbar.repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				SubstanceScrollBarUI.this.scrollbar.repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				SubstanceScrollBarUI.this.scrollbar.repaint();
			}
		};

		this.incrButton.addMouseListener(this.substanceMouseListener);
		this.decrButton.addMouseListener(this.substanceMouseListener);
		this.mySecondDecreaseButton
				.addMouseListener(this.substanceMouseListener);
		this.mySecondIncreaseButton
				.addMouseListener(this.substanceMouseListener);

		this.substanceThumbRolloverListener = new RolloverControlListener(this,
				this.thumbModel);
		this.scrollbar.addMouseListener(this.substanceThumbRolloverListener);
		this.scrollbar
				.addMouseMotionListener(this.substanceThumbRolloverListener);

		// this.thumbStateTransitionTracker.registerModelListeners();

		this.substancePropertyListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("font".equals(evt.getPropertyName())) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							scrollbar.updateUI();
						}
					});
				}
				if ("background".equals(evt.getPropertyName())) {
					// propagate application-specific background color to the
					// scroll buttons.
					Color newBackgr = (Color) evt.getNewValue();
					if (!(newBackgr instanceof UIResource)) {
						if (mySecondDecreaseButton != null) {
							if (mySecondDecreaseButton.getBackground() instanceof UIResource) {
								mySecondDecreaseButton.setBackground(newBackgr);
							}
						}
						if (mySecondIncreaseButton != null) {
							if (mySecondIncreaseButton.getBackground() instanceof UIResource) {
								mySecondIncreaseButton.setBackground(newBackgr);
							}
						}
						if (incrButton != null) {
							if (incrButton.getBackground() instanceof UIResource) {
								incrButton.setBackground(newBackgr);
							}
						}
						if (decrButton != null) {
							if (decrButton.getBackground() instanceof UIResource) {
								decrButton.setBackground(newBackgr);
							}
						}
					}
				}
			}
		};
		this.scrollbar
				.addPropertyChangeListener(this.substancePropertyListener);

		this.mySecondDecreaseButton.addMouseListener(this.buttonListener);
		this.mySecondIncreaseButton.addMouseListener(this.buttonListener);

		this.substanceAdjustmentListener = new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				SubstanceCoreUtilities
						.testComponentStateChangeThreadingViolation(scrollbar);
				Component parent = SubstanceScrollBarUI.this.scrollbar
						.getParent();
				if (parent instanceof JScrollPane) {
					JScrollPane jsp = (JScrollPane) parent;
					JScrollBar hor = jsp.getHorizontalScrollBar();
					JScrollBar ver = jsp.getVerticalScrollBar();

					JScrollBar other = null;
					if (SubstanceScrollBarUI.this.scrollbar == hor) {
						other = ver;
					}
					if (SubstanceScrollBarUI.this.scrollbar == ver) {
						other = hor;
					}

					if ((other != null) && other.isVisible())
						other.repaint();
					SubstanceScrollBarUI.this.scrollbar.repaint();
				}
			}
		};
		this.scrollbar.addAdjustmentListener(this.substanceAdjustmentListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#uninstallListeners()
	 */
	@Override
	protected void uninstallListeners() {
		// fix for defect 109 - memory leak on changing skin
		this.incrButton.removeMouseListener(this.substanceMouseListener);
		this.decrButton.removeMouseListener(this.substanceMouseListener);
		this.mySecondDecreaseButton
				.removeMouseListener(this.substanceMouseListener);
		this.mySecondIncreaseButton
				.removeMouseListener(this.substanceMouseListener);
		this.substanceMouseListener = null;

		this.scrollbar.removeMouseListener(this.substanceThumbRolloverListener);
		this.scrollbar
				.removeMouseMotionListener(this.substanceThumbRolloverListener);
		this.substanceThumbRolloverListener = null;

		this.scrollbar
				.removePropertyChangeListener(this.substancePropertyListener);
		this.substancePropertyListener = null;

		this.mySecondDecreaseButton.removeMouseListener(this.buttonListener);
		this.mySecondIncreaseButton.removeMouseListener(this.buttonListener);

		this.scrollbar
				.removeAdjustmentListener(this.substanceAdjustmentListener);
		this.substanceAdjustmentListener = null;

		super.uninstallListeners();
	}

	public boolean isInside(MouseEvent me) {
		Rectangle trackB = this.getTrackBounds();
		if (trackB == null)
			return false;
		return trackB.contains(me.getX(), me.getY());
	}

	@Override
	public StateTransitionTracker getTransitionTracker() {
		return this.compositeStateTransitionTracker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.basic.BasicScrollBarUI#scrollByBlock(int)
	 */
	@Override
	public void scrollByBlock(int direction) {
		// This method is called from SubstanceScrollPaneUI to implement wheel
		// scrolling.
		int oldValue = this.scrollbar.getValue();
		int blockIncrement = this.scrollbar.getBlockIncrement(direction);
		int delta = blockIncrement * ((direction > 0) ? +1 : -1);
		int newValue = oldValue + delta;

		// Check for overflow.
		if ((delta > 0) && (newValue < oldValue)) {
			newValue = this.scrollbar.getMaximum();
		} else if ((delta < 0) && (newValue > oldValue)) {
			newValue = this.scrollbar.getMinimum();
		}

		this.scrollbar.setValue(newValue);
	}

	/**
	 * Scrolls the associated scroll bar.
	 * 
	 * @param direction
	 *            Direction.
	 * @param units
	 *            Scroll units.
	 */
	public void scrollByUnits(int direction, int units) {
		// This method is called from SubstanceScrollPaneUI to implement wheel
		// scrolling.
		int delta;

		for (int i = 0; i < units; i++) {
			if (direction > 0) {
				delta = this.scrollbar.getUnitIncrement(direction);
			} else {
				delta = -this.scrollbar.getUnitIncrement(direction);
			}

			int oldValue = this.scrollbar.getValue();
			int newValue = oldValue + delta;

			// Check for overflow.
			if ((delta > 0) && (newValue < oldValue)) {
				newValue = this.scrollbar.getMaximum();
			} else if ((delta < 0) && (newValue > oldValue)) {
				newValue = this.scrollbar.getMinimum();
			}
			if (oldValue == newValue) {
				break;
			}
			this.scrollbar.setValue(newValue);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicScrollBarUI#layoutVScrollbar(javax.swing.
	 * JScrollBar)
	 */
	@Override
	protected void layoutVScrollbar(JScrollBar sb) {
		ScrollPaneButtonPolicyKind buttonPolicy = SubstanceCoreUtilities
				.getScrollPaneButtonsPolicyKind(this.scrollbar);
		this.mySecondDecreaseButton.setBounds(0, 0, 0, 0);
		this.mySecondIncreaseButton.setBounds(0, 0, 0, 0);
		switch (buttonPolicy) {
		case OPPOSITE:
			super.layoutVScrollbar(sb);
			break;
		case NONE:
			this.layoutVScrollbarNone(sb);
			break;
		case ADJACENT:
			this.layoutVScrollbarAdjacent(sb);
			break;
		case MULTIPLE:
			this.layoutVScrollbarMultiple(sb);
			break;
		case MULTIPLE_BOTH:
			this.layoutVScrollbarMultipleBoth(sb);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.plaf.basic.BasicScrollBarUI#layoutHScrollbar(javax.swing.
	 * JScrollBar)
	 */
	@Override
	protected void layoutHScrollbar(JScrollBar sb) {
		this.mySecondDecreaseButton.setBounds(0, 0, 0, 0);
		this.mySecondIncreaseButton.setBounds(0, 0, 0, 0);
		ScrollPaneButtonPolicyKind buttonPolicy = SubstanceCoreUtilities
				.getScrollPaneButtonsPolicyKind(this.scrollbar);
		switch (buttonPolicy) {
		case OPPOSITE:
			super.layoutHScrollbar(sb);
			break;
		case NONE:
			this.layoutHScrollbarNone(sb);
			break;
		case ADJACENT:
			this.layoutHScrollbarAdjacent(sb);
			break;
		case MULTIPLE:
			this.layoutHScrollbarMultiple(sb);
			break;
		case MULTIPLE_BOTH:
			this.layoutHScrollbarMultipleBoth(sb);
			break;
		}
	}

	/**
	 * Lays out the vertical scroll bar when the button policy is
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#ADJACENT}.
	 * 
	 * @param sb
	 *            Scroll bar.
	 */
	protected void layoutVScrollbarAdjacent(JScrollBar sb) {
		Dimension sbSize = sb.getSize();
		Insets sbInsets = sb.getInsets();

		/*
		 * Width and left edge of the buttons and thumb.
		 */
		int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
		int itemX = sbInsets.left;

		/*
		 * Nominal locations of the buttons, assuming their preferred size will
		 * fit.
		 */
		int incrButtonH = itemW;
		int incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);

		int decrButton2H = itemW;
		int decrButton2Y = incrButtonY - decrButton2H;

		/*
		 * The thumb must fit within the height left over after we subtract the
		 * preferredSize of the buttons and the insets.
		 */
		int sbInsetsH = sbInsets.top + sbInsets.bottom;
		int sbButtonsH = decrButton2H + incrButtonH;
		float trackH = sbSize.height - (sbInsetsH + sbButtonsH);

		/*
		 * Compute the height and origin of the thumb. The case where the thumb
		 * is at the bottom edge is handled specially to avoid numerical
		 * problems in computing thumbY. Enforce the thumbs min/max dimensions.
		 * If the thumb doesn't fit in the track (trackH) we'll hide it later.
		 */
		float min = sb.getMinimum();
		float extent = sb.getVisibleAmount();
		float range = sb.getMaximum() - min;
		float value = sb.getValue();

		int thumbH = (range <= 0) ? this.getMaximumThumbSize().height
				: (int) (trackH * (extent / range));
		thumbH = Math.max(thumbH, this.getMinimumThumbSize().height);
		thumbH = Math.min(thumbH, this.getMaximumThumbSize().height);

		int thumbY = decrButton2Y - thumbH;
		if (value < (sb.getMaximum() - sb.getVisibleAmount())) {
			float thumbRange = trackH - thumbH;
			thumbY = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
		}

		/*
		 * If the buttons don't fit, allocate half of the available space to
		 * each and move the lower one (incrButton) down.
		 */
		int sbAvailButtonH = (sbSize.height - sbInsetsH);
		if (sbAvailButtonH < sbButtonsH) {
			incrButtonH = decrButton2H = sbAvailButtonH / 2;
			incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
		}
		this.mySecondIncreaseButton.setBounds(0, 0, 0, 0);
		this.decrButton.setBounds(0, 0, 0, 0);
		this.mySecondDecreaseButton.setBounds(itemX,
				incrButtonY - decrButton2H, itemW, decrButton2H);
		this.incrButton.setBounds(itemX, incrButtonY - 1, itemW,
				incrButtonH + 1);

		/*
		 * Update the trackRect field.
		 */
		int itrackY = 0;
		int itrackH = decrButton2Y - itrackY;
		this.trackRect.setBounds(itemX, itrackY, itemW, itrackH);

		/*
		 * If the thumb isn't going to fit, zero it's bounds. Otherwise make
		 * sure it fits between the buttons. Note that setting the thumbs bounds
		 * will cause a repaint.
		 */
		if (thumbH >= (int) trackH) {
			this.setThumbBounds(0, 0, 0, 0);
		} else {
			if ((thumbY + thumbH) > decrButton2Y) {
				thumbY = decrButton2Y - thumbH;
			}
			if (thumbY < 0) {
				thumbY = 0;
			}
			this.setThumbBounds(itemX, thumbY, itemW, thumbH);
		}
	}

	/**
	 * Lays out the vertical scroll bar when the button policy is
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#ADJACENT}.
	 * 
	 * @param sb
	 *            Scroll bar.
	 */
	protected void layoutVScrollbarNone(JScrollBar sb) {
		Dimension sbSize = sb.getSize();
		Insets sbInsets = sb.getInsets();

		/*
		 * Width and left edge of the buttons and thumb.
		 */
		int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
		int itemX = sbInsets.left;

		/*
		 * Nominal locations of the buttons, assuming their preferred size will
		 * fit.
		 */
		int incrButtonH = 0;
		int incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);

		int decrButton2H = 0;
		int decrButton2Y = incrButtonY - decrButton2H;

		/*
		 * The thumb must fit within the height left over after we subtract the
		 * preferredSize of the buttons and the insets.
		 */
		int sbInsetsH = sbInsets.top + sbInsets.bottom;
		int sbButtonsH = decrButton2H + incrButtonH;
		float trackH = sbSize.height - (sbInsetsH + sbButtonsH);

		/*
		 * Compute the height and origin of the thumb. The case where the thumb
		 * is at the bottom edge is handled specially to avoid numerical
		 * problems in computing thumbY. Enforce the thumbs min/max dimensions.
		 * If the thumb doesn't fit in the track (trackH) we'll hide it later.
		 */
		float min = sb.getMinimum();
		float extent = sb.getVisibleAmount();
		float range = sb.getMaximum() - min;
		float value = sb.getValue();

		int thumbH = (range <= 0) ? this.getMaximumThumbSize().height
				: (int) (trackH * (extent / range));
		thumbH = Math.max(thumbH, this.getMinimumThumbSize().height);
		thumbH = Math.min(thumbH, this.getMaximumThumbSize().height);

		int thumbY = decrButton2Y - thumbH;
		if (value < (sb.getMaximum() - sb.getVisibleAmount())) {
			float thumbRange = trackH - thumbH;
			thumbY = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
		}

		/*
		 * If the buttons don't fit, allocate half of the available space to
		 * each and move the lower one (incrButton) down.
		 */
		int sbAvailButtonH = (sbSize.height - sbInsetsH);
		if (sbAvailButtonH < sbButtonsH) {
			incrButtonH = 0;// decrButton2H = 0;
			// incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
		}
		this.decrButton.setBounds(0, 0, 0, 0);
		this.mySecondDecreaseButton.setBounds(0, 0, 0, 0);
		this.incrButton.setBounds(0, 0, 0, 0);
		this.mySecondIncreaseButton.setBounds(0, 0, 0, 0);

		/*
		 * Update the trackRect field.
		 */
		int itrackY = 0;
		int itrackH = decrButton2Y - itrackY;
		this.trackRect.setBounds(itemX, itrackY, itemW, itrackH);

		/*
		 * If the thumb isn't going to fit, zero it's bounds. Otherwise make
		 * sure it fits between the buttons. Note that setting the thumbs bounds
		 * will cause a repaint.
		 */
		if (thumbH >= (int) trackH) {
			this.setThumbBounds(0, 0, 0, 0);
		} else {
			if ((thumbY + thumbH) > decrButton2Y) {
				thumbY = decrButton2Y - thumbH;
			}
			if (thumbY < 0) {
				thumbY = 0;
			}
			this.setThumbBounds(itemX, thumbY, itemW, thumbH);
		}
	}

	/**
	 * Lays out the vertical scroll bar when the button policy is
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#MULTIPLE}.
	 * 
	 * @param sb
	 *            Scroll bar.
	 */
	protected void layoutVScrollbarMultiple(JScrollBar sb) {
		Dimension sbSize = sb.getSize();
		Insets sbInsets = sb.getInsets();

		/*
		 * Width and left edge of the buttons and thumb.
		 */
		int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
		int itemX = sbInsets.left;

		/*
		 * Nominal locations of the buttons, assuming their preferred size will
		 * fit.
		 */
		int incrButtonH = itemW;
		int incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);

		int decrButton2H = itemW;
		int decrButton2Y = incrButtonY - decrButton2H;

		int decrButtonH = itemW;
		int decrButtonY = sbInsets.top;

		/*
		 * The thumb must fit within the height left over after we subtract the
		 * preferredSize of the buttons and the insets.
		 */
		int sbInsetsH = sbInsets.top + sbInsets.bottom;
		int sbButtonsH = decrButton2H + incrButtonH + decrButtonH;
		float trackH = sbSize.height - (sbInsetsH + sbButtonsH);

		/*
		 * Compute the height and origin of the thumb. The case where the thumb
		 * is at the bottom edge is handled specially to avoid numerical
		 * problems in computing thumbY. Enforce the thumbs min/max dimensions.
		 * If the thumb doesn't fit in the track (trackH) we'll hide it later.
		 */
		float min = sb.getMinimum();
		float extent = sb.getVisibleAmount();
		float range = sb.getMaximum() - min;
		float value = sb.getValue();

		int thumbH = (range <= 0) ? this.getMaximumThumbSize().height
				: (int) (trackH * (extent / range));
		thumbH = Math.max(thumbH, this.getMinimumThumbSize().height);
		thumbH = Math.min(thumbH, this.getMaximumThumbSize().height);

		int thumbY = decrButton2Y - thumbH;
		if (value < (sb.getMaximum() - sb.getVisibleAmount())) {
			float thumbRange = trackH - thumbH;
			thumbY = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
			thumbY += decrButtonY + decrButtonH;
		}

		/*
		 * If the buttons don't fit, allocate half of the available space to
		 * each and move the lower one (incrButton) down.
		 */
		int sbAvailButtonH = (sbSize.height - sbInsetsH);
		if (sbAvailButtonH < sbButtonsH) {
			incrButtonH = decrButton2H = decrButtonH = sbAvailButtonH / 2;
			incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
		}
		this.decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
		this.mySecondDecreaseButton.setBounds(itemX,
				incrButtonY - decrButton2H, itemW, decrButton2H);
		this.incrButton.setBounds(itemX, incrButtonY - 1, itemW,
				incrButtonH + 1);
		this.mySecondIncreaseButton.setBounds(0, 0, 0, 0);

		/*
		 * Update the trackRect field.
		 */
		int itrackY = decrButtonY + decrButtonH;
		int itrackH = decrButton2Y - itrackY;
		this.trackRect.setBounds(itemX, itrackY, itemW, itrackH);

		/*
		 * If the thumb isn't going to fit, zero it's bounds. Otherwise make
		 * sure it fits between the buttons. Note that setting the thumbs bounds
		 * will cause a repaint.
		 */
		if (thumbH >= (int) trackH) {
			this.setThumbBounds(0, 0, 0, 0);
		} else {
			if ((thumbY + thumbH) > decrButton2Y) {
				thumbY = decrButton2Y - thumbH;
			}
			if (thumbY < (decrButtonY + decrButtonH)) {
				thumbY = decrButtonY + decrButtonH + 1;
			}
			this.setThumbBounds(itemX, thumbY, itemW, thumbH);
		}
	}

	/**
	 * Lays out the vertical scroll bar when the button policy is
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#MULTIPLE_BOTH}.
	 * 
	 * @param sb
	 *            Scroll bar.
	 */
	protected void layoutVScrollbarMultipleBoth(JScrollBar sb) {
		Dimension sbSize = sb.getSize();
		Insets sbInsets = sb.getInsets();

		/*
		 * Width and left edge of the buttons and thumb.
		 */
		int itemW = sbSize.width - (sbInsets.left + sbInsets.right);
		int itemX = sbInsets.left;

		/*
		 * Nominal locations of the buttons, assuming their preferred size will
		 * fit.
		 */
		int incrButtonH = itemW;
		int incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);

		int decrButton2H = itemW;
		int decrButton2Y = incrButtonY - decrButton2H;

		int decrButtonH = itemW;
		int decrButtonY = sbInsets.top;

		int incrButton2H = itemW;
		int incrButton2Y = decrButtonY + decrButtonH;

		/*
		 * The thumb must fit within the height left over after we subtract the
		 * preferredSize of the buttons and the insets.
		 */
		int sbInsetsH = sbInsets.top + sbInsets.bottom;
		int sbButtonsH = decrButton2H + incrButtonH + decrButtonH
				+ incrButton2H;
		float trackH = sbSize.height - (sbInsetsH + sbButtonsH);

		/*
		 * Compute the height and origin of the thumb. The case where the thumb
		 * is at the bottom edge is handled specially to avoid numerical
		 * problems in computing thumbY. Enforce the thumbs min/max dimensions.
		 * If the thumb doesn't fit in the track (trackH) we'll hide it later.
		 */
		float min = sb.getMinimum();
		float extent = sb.getVisibleAmount();
		float range = sb.getMaximum() - min;
		float value = sb.getValue();

		int thumbH = (range <= 0) ? this.getMaximumThumbSize().height
				: (int) (trackH * (extent / range));
		thumbH = Math.max(thumbH, this.getMinimumThumbSize().height);
		thumbH = Math.min(thumbH, this.getMaximumThumbSize().height);

		int thumbY = decrButton2Y - thumbH;
		if (value < (sb.getMaximum() - sb.getVisibleAmount())) {
			float thumbRange = trackH - thumbH;
			thumbY = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
			thumbY += incrButton2Y + incrButton2H;
		}

		/*
		 * If the buttons don't fit, allocate half of the available space to
		 * each and move the lower one (incrButton) down.
		 */
		int sbAvailButtonH = (sbSize.height - sbInsetsH);
		if (sbAvailButtonH < sbButtonsH) {
			incrButtonH = decrButton2H = decrButtonH = incrButton2H = sbAvailButtonH / 4;
			incrButtonY = sbSize.height - (sbInsets.bottom + incrButtonH);
		}
		this.decrButton.setBounds(itemX, decrButtonY, itemW, decrButtonH);
		this.mySecondDecreaseButton.setBounds(itemX,
				incrButtonY - decrButton2H, itemW, decrButton2H);
		this.incrButton.setBounds(itemX, incrButtonY - 1, itemW,
				incrButtonH + 1);
		this.mySecondIncreaseButton.setBounds(itemX, decrButtonY + decrButtonH
				- 1, itemW, incrButton2H + 1);

		/*
		 * Update the trackRect field.
		 */
		int itrackY = incrButton2Y + incrButton2H;
		int itrackH = decrButton2Y - itrackY;
		this.trackRect.setBounds(itemX, itrackY, itemW, itrackH);

		/*
		 * If the thumb isn't going to fit, zero it's bounds. Otherwise make
		 * sure it fits between the buttons. Note that setting the thumbs bounds
		 * will cause a repaint.
		 */
		if (thumbH >= (int) trackH) {
			this.setThumbBounds(0, 0, 0, 0);
		} else {
			if ((thumbY + thumbH) > decrButton2Y) {
				thumbY = decrButton2Y - thumbH;
			}
			if (thumbY < (incrButton2Y + incrButton2H)) {
				thumbY = incrButton2Y + incrButton2H + 1;
			}
			this.setThumbBounds(itemX, thumbY, itemW, thumbH);
		}
	}

	/**
	 * Lays out the horizontal scroll bar when the button policy is
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#ADJACENT}.
	 * 
	 * @param sb
	 *            Scroll bar.
	 */
	protected void layoutHScrollbarAdjacent(JScrollBar sb) {
		Dimension sbSize = sb.getSize();
		Insets sbInsets = sb.getInsets();

		/*
		 * Height and top edge of the buttons and thumb.
		 */
		int itemH = sbSize.height - (sbInsets.top + sbInsets.bottom);
		int itemY = sbInsets.top;

		boolean ltr = sb.getComponentOrientation().isLeftToRight();

		/*
		 * Nominal locations of the buttons, assuming their preferred size will
		 * fit.
		 */
		int decrButton2W = itemH;
		int incrButtonW = itemH;
		int incrButtonX = ltr ? sbSize.width - (sbInsets.right + incrButtonW)
				: sbInsets.left;
		int decrButton2X = ltr ? incrButtonX - decrButton2W : incrButtonX
				+ decrButton2W;

		/*
		 * The thumb must fit within the width left over after we subtract the
		 * preferredSize of the buttons and the insets.
		 */
		int sbInsetsW = sbInsets.left + sbInsets.right;
		int sbButtonsW = decrButton2W + incrButtonW;
		float trackW = sbSize.width - (sbInsetsW + sbButtonsW);

		/*
		 * Compute the width and origin of the thumb. Enforce the thumbs min/max
		 * dimensions. The case where the thumb is at the right edge is handled
		 * specially to avoid numerical problems in computing thumbX. If the
		 * thumb doesn't fit in the track (trackH) we'll hide it later.
		 */
		float min = sb.getMinimum();
		float max = sb.getMaximum();
		float extent = sb.getVisibleAmount();
		float range = max - min;
		float value = sb.getValue();

		int thumbW = (range <= 0) ? this.getMaximumThumbSize().width
				: (int) (trackW * (extent / range));
		thumbW = Math.max(thumbW, this.getMinimumThumbSize().width);
		thumbW = Math.min(thumbW, this.getMaximumThumbSize().width);

		int thumbX = ltr ? decrButton2X - thumbW : sbInsets.left;
		if (value < (max - sb.getVisibleAmount())) {
			float thumbRange = trackW - thumbW;
			if (ltr) {
				thumbX = (int) (0.5f + (thumbRange * ((value - min) / (range - extent))));
			} else {
				thumbX = (int) (0.5f + (thumbRange * ((max - extent - value) / (range - extent))));
				thumbX += decrButton2X + decrButton2W;
			}
		}

		/*
		 * If the buttons don't fit, allocate half of the available space to
		 * each and move the right one over.
		 */
		int sbAvailButtonW = (sbSize.width - sbInsetsW);
		if (sbAvailButtonW < sbButtonsW) {
			incrButtonW = decrButton2W = sbAvailButtonW / 2;
			incrButtonX = ltr ? sbSize.width - (sbInsets.right + incrButtonW)
					: sbInsets.left;
		}

		this.mySecondDecreaseButton.setBounds(decrButton2X + (ltr ? 0 : -1),
				itemY, decrButton2W + 1, itemH);
		this.incrButton.setBounds(incrButtonX, itemY, incrButtonW, itemH);
		this.decrButton.setBounds(0, 0, 0, 0);
		this.mySecondIncreaseButton.setBounds(0, 0, 0, 0);

		/*
		 * Update the trackRect field.
		 */
		if (ltr) {
			int itrackX = sbInsets.left;
			int itrackW = decrButton2X - itrackX;
			this.trackRect.setBounds(itrackX, itemY, itrackW, itemH);
		} else {
			int itrackX = decrButton2X + decrButton2W;
			int itrackW = sbSize.width - itrackX;
			this.trackRect.setBounds(itrackX, itemY, itrackW, itemH);
		}

		/*
		 * Make sure the thumb fits between the buttons. Note that setting the
		 * thumbs bounds causes a repaint.
		 */
		if (thumbW >= (int) trackW) {
			this.setThumbBounds(0, 0, 0, 0);
		} else {
			if (ltr) {
				if (thumbX + thumbW > decrButton2X) {
					thumbX = decrButton2X - thumbW;
				}
				if (thumbX < 0) {
					thumbX = 1;
				}
			} else {
				if (thumbX + thumbW > (sbSize.width - sbInsets.left)) {
					thumbX = sbSize.width - sbInsets.left - thumbW;
				}
				if (thumbX < (decrButton2X + decrButton2W)) {
					thumbX = decrButton2X + decrButton2W + 1;
				}
			}
			this.setThumbBounds(thumbX, itemY, thumbW, itemH);
		}
	}

	/**
	 * Lays out the horizontal scroll bar when the button policy is
	 * {@link SubstanceConstants.ScrollPaneButtonPolicyKind#NONE}.
	 * 
	 * @param sb
	 *            Scroll bar.
	 */
	protected void layoutHScrollbarNone(JScrollBar sb) {
		Dimension sbSize = sb.getSize();
		Insets sbInsets = sb.getInsets();

		/*
		 * Height and top edge of the buttons and thumb.
		 */
		int itemH = sbSize.height - (sbInsets.top + sbInsets.bottom);
		int itemY = sbInsets.top;

		boolean ltr = sb.getComponentOrientation().isLeftToRight();

		/*
		 * Nominal locations of the buttons, assuming their preferred size will
		 * fit.
		 */
		int decrButton2W = 0;
		int incrButtonW = 0;
		int incrButtonX = ltr ? sbSize.width - (sbInsets.right + incrButtonW)
				: sbInsets.left;
		int decrButton2X = ltr ? incrButtonX - decrButton2W : incrButtonX
				+ decrButton2W;

		/*
		 * The thumb must fit within the width left over after we subtract the
		 * preferredSize of the butt
