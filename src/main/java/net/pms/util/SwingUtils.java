/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package net.pms.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import javax.annotation.Nonnull;
import javax.swing.text.JTextComponent;

/**
 * This is a utility class for various reusable Swing methods.
 */
public class SwingUtils {

	// We don't want this to be instanced, only static methods
	private SwingUtils() {
	}

	/**
	 * Returns the {@link Dimension} for the given {@link JTextComponent}
	 * subclass that will show the whole word wrapped text in the given width.
	 * It won't work for styled text of varied size or style, it's assumed that
	 * the whole text is rendered with the {@link JTextComponent}s font.
	 *
	 * @param textComponent the {@link JTextComponent} to calculate the {@link Dimension} for
	 * @param width the width of the resulting {@link Dimension}
	 * @param text the {@link String} which should be word wrapped
	 * @return The calculated {@link Dimension}
	 */
	public static Dimension getWordWrappedTextDimension(JTextComponent textComponent, int width, String text) {
		if (textComponent == null) {
			throw new IllegalArgumentException("textComponent cannot be null");
		}
		if (width < 1) {
			throw new IllegalArgumentException("width must be 1 or greater");
		}
		if (text == null) {
			text = textComponent.getText();
		}
		if (text.isEmpty()) {
			return new Dimension(width, 0);
		}

		FontMetrics metrics = textComponent.getFontMetrics(textComponent.getFont());
		FontRenderContext rendererContext = metrics.getFontRenderContext();
		float formatWidth = width - textComponent.getInsets().left - textComponent.getInsets().right;

		int lines = 0;
		String[] paragraphs = text.split("\n");
		for (String paragraph : paragraphs) {
			if (paragraph.isEmpty()) {
				lines++;
			} else {
				AttributedString attributedText = new AttributedString(paragraph);
				attributedText.addAttribute(TextAttribute.FONT, textComponent.getFont());
				AttributedCharacterIterator charIterator = attributedText.getIterator();
				LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(charIterator, rendererContext);

				lineMeasurer.setPosition(charIterator.getBeginIndex());
				while (lineMeasurer.getPosition() < charIterator.getEndIndex()) {
					lineMeasurer.nextLayout(formatWidth);
					lines++;
				}
			}
		}

		return new Dimension(width, metrics.getHeight() * lines + textComponent.getInsets().top + textComponent.getInsets().bottom);
	}

	/**
	 * Returns the {@link Dimension} for the given {@link JTextComponent}
	 * subclass that will show the whole word wrapped text in the given width.
	 * The {@link String} from {@link JTextComponent#getText()} will be used
	 * as the word wrapped text. It won't work for styled text of varied size
	 * or style, it's assumed that the whole text is rendered with the
	 * {@link JTextComponent}s font.
	 *
	 * @param textComponent the {@link JTextComponent} to calculate the {@link Dimension} for
	 * @param width the width of the resulting {@link Dimension}
	 * @return The calculated {@link Dimension}
	 */
	public static Dimension getWordWrappedTextDimension(JTextComponent textComponent, int width) {
		return getWordWrappedTextDimension(textComponent, width, null);
	}

	/**
	 * Calculates the average character width for the given {@link Component}.
	 * This can be useful as a scaling factor when designing for font scaling.
	 *
	 * @param component the {@link Component} for which to calculate the
	 *        average character width.
	 * @return The average width in pixels
	 */
	public static double getComponentAverageCharacterWidth(@Nonnull Component component) {
		FontMetrics metrics = component.getFontMetrics(component.getFont());
		int i = 0;
		double avgWidth = 0;
		for (int width : metrics.getWidths()) {
			avgWidth += width;
			i++;
		}
		return avgWidth / i;
	}

	/**
	 * Creates {@link Insets} with predefined factors suitable for buttons which
	 * scales with the average character width.
	 *
	 * @param component the {@link Component} from which to measure the average
	 *            character width.
	 * @return The new {@link Insets}.
	 */
	public static Insets createButtonInsets(@Nonnull Component component) {
		return createScalableInsets(component, 4.0, 0.5);
	}

	/**
	 * Creates {@link Insets} which scales with the average character width.
	 *
	 * @param component the {@link Component} from which to measure the average
	 *            character width.
	 * @param xFactor the factor of the horizontal insets.
	 * @param yFactor the factor of the vertical insets.
	 * @return The new {@link Insets}.
	 */
	public static Insets createScalableInsets(@Nonnull Component component, double xFactor, double yFactor) {
		return createScalableInsets(SwingUtils.getComponentAverageCharacterWidth(component), xFactor, yFactor);
	}

	/**
	 * Creates {@link Insets} with predefined factors suitable for buttons which
	 * scales with the average character width.
	 *
	 * @param avgCharWidth the average character width (can be found with
	 *            {@link #getComponentAverageCharacterWidth(Component)}).
	 * @return The new {@link Insets}.
	 */
	public static Insets createButtonInsets(double avgCharWidth) {
		return createScalableInsets(avgCharWidth, 4.0, 0.5);
	}

	/**
	 * Creates {@link Insets} which scales with the average character width.
	 *
	 * @param avgCharWidth the average character width (can be found with
	 *            {@link #getComponentAverageCharacterWidth(Component)}).
	 * @param xFactor the factor of the horizontal insets.
	 * @param yFactor the factor of the vertical insets.
	 * @return The new {@link Insets}.
	 */
	public static Insets createScalableInsets(double avgCharWidth, double xFactor, double yFactor) {
		return new Insets(
			(int) Math.round(yFactor * avgCharWidth),
			(int) Math.round(xFactor * avgCharWidth),
			(int) Math.round(yFactor * avgCharWidth),
			(int) Math.round(xFactor * avgCharWidth));
	}

}
