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
package net.pms.newgui.components;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * This class extends {@link JTextField}. In addition to the standard behavior,
 * this implements a {@link ChangeListener} that will only fire when the text
 * value is actually changed. A known limitation is that if
 * {@link System#exit(int)} is called, the application might quit before the
 * {@link ChangeListener} can fire and the change might not register.
 *
 * This also implements a default text that can be displayed when the actual
 * text is blank. The default text will disappear as soon as something is
 * entered, and will reappear if the text is deleted.
 *
 * @author Nadahar
 */
public class DefaultTextField extends JTextField implements FocusListener, ActionListener, KeyListener {

	private static final long serialVersionUID = 1L;

	/** The {@link UIManager}'s inactive text color */
	protected static final Color STANDARD_DEFAULT_FONT_COLOR = UIManager.getColor("textInactiveText");

	/** The default text displayed when the field is blank */
	protected String defaultText;

	/** The default text {@link Font} style ({@link Font} constant) */
	protected int defaultFontStyle = -1;

	/** The default text {@link Font} {@link Color} */
	protected Color defaultFontColor = STANDARD_DEFAULT_FONT_COLOR;

	/** The cached text used for undo ({@code Esc}) */
	protected String undoText = null;

	/** The change listeners for the {@link DefaultTextField} */
	protected EventListenerList changeListenerList = new EventListenerList();

	/**
	 * Creates a new instance. A default model is created, the initial string
	 * and the default text is {@code null}, and the number of columns is set to
	 * 0. {@code Enter} has standard behavior.
	 */
	public DefaultTextField() {
		this(null, 0, null, false);
	}

	/**
	 * Creates a new instance with the specified default text. A default model
	 * is created, the initial string is {@code null}, and the number of columns
	 * is set to 0. {@code Enter} has standard behavior.
	 *
	 * @param defaultText The {@link String} that is to be displayed when the
	 *            field is blank.
	 */
	public DefaultTextField(String defaultText) {
		this(null, 0, defaultText, false);
	}

	/**
	 * Creates a new instance with the specified default text. A default model
	 * is created, the initial string is {@code null}, and the number of columns
	 * is set to 0.
	 *
	 * @param defaultText The {@link String} that is to be displayed when the
	 *            field is blank.
	 * @param enterCommits whether or not the {@code Enter} key should
	 *            commit/save the current text.
	 */
	public DefaultTextField(String defaultText, boolean enterCommits) {
		this(null, 0, defaultText, enterCommits);
	}

	/**
	 * Constructs a new {@link DefaultTextField} initialized with the specified
	 * text and default text. A default model is created and the number of
	 * columns is 0. {@code Enter} has standard behavior.
	 *
	 * @param text the text to be displayed, or {@code null}.
	 * @param defaultText The {@link String} that is to be displayed when the
	 *            text is blank.
	 */
	public DefaultTextField(String text, String defaultText) {
		this(text, 0, defaultText, false);
	}

	/**
	 * Creates a new instance with the specified text and default text. A
	 * default model is created and the number of columns is 0.
	 *
	 * @param text the text to be displayed, or {@code null}.
	 * @param defaultText The {@link String} that is to be displayed when the
	 *            field is blank.
	 * @param enterCommits whether or not the {@code Enter} key should
	 *            commit/save the current text.
	 */
	public DefaultTextField(String text, String defaultText, boolean enterCommits) {
		this(text, 0, defaultText, enterCommits);
	}

	/**
	 * Creates a new instance with the specified default text and number of
	 * columns. A default model is created and the initial string is set to
	 * {@code null}. {@code Enter} has standard behavior.
	 *
	 * @param columns the number of columns to use to calculate the preferred
	 *            width; if columns is set to zero, the preferred width will be
	 *            whatever naturally results from the component implementation.
	 * @param defaultText The {@link String} that is to be displayed when the
	 *            field is blank.
	 */
	public DefaultTextField(int columns, String defaultText) {
		this(null, columns, defaultText, false);
	}

	/**
	 * Creates a new instance with the specified default text and number of
	 * columns. A default model is created and the initial string is set to
	 * {@code null}.
	 *
	 * @param columns the number of columns to use to calculate the preferred
	 *            width; if columns is set to zero, the preferred width will be
	 *            whatever naturally results from the component implementation.
	 * @param defaultText The {@link String} that is to be displayed when the
	 *            field is blank.
	 * @param enterCommits whether or not the {@code Enter} key should
	 *            commit/save the current text.
	 */
	public DefaultTextField(int columns, String defaultText, boolean enterCommits) {
		this(null, columns, defaultText, enterCommits);
	}

	/**
	 * Creates a new instance with the specified text, columns and default text.
	 * A default model is created.
	 *
	 * @param text the text to be displayed, or {@code null}
	 * @param columns the number of columns to use to calculate the preferred
	 *            width; if columns is set to zero, the preferred width will be
	 *            whatever naturally results from the component implementation.
	 * @param defaultText The {@link String} that is to be displayed when the
	 *            field is blank.
	 * @param enterCommits whether or not the {@code Enter} key should
	 *            commit/save the current text.
	 */
	public DefaultTextField(String text, int columns, String defaultText, boolean enterCommits) {
		super(null, text, columns);
		this.defaultText = defaultText;
		this.addFocusListener(this);
		if (enterCommits) {
			this.addActionListener(this);
		}
		this.addKeyListener(this);
	}

	/**
	 * Sets the default text to display when the field is blank.
	 *
	 * @param defaultText the {@link String} to set.
	 */
	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;
		repaint();
	}

	/**
	 * @return The {@link String} displayed when the field is blank.
	 */
	public String getDefaultText() {
		return defaultText;
	}

	/**
	 * Sets the font style to apply to the default text. Use the constants from
	 * the {@link Font} class like {@link Font#BOLD}, {@link Font#ITALIC} or
	 * {@link Font#MONOSPACED}. Set to a negative value to disable style
	 * override.
	 *
	 * @param defaultFontStyle any valid constant from {@link Font}.
	 */
	public void setDefaultFontStyle(int defaultFontStyle) {
		this.defaultFontStyle = defaultFontStyle;
		repaint();
	}

	/**
	 * Gets the font style integer applied to the default text. The integer
	 * value effect can be found from the {@link Font} constants. A negative
	 * value means that style override is disabled.
	 *
	 * @return The default text {@link Font} style integer value.
	 */
	public int getDefaultFontStyle() {
		return defaultFontStyle;
	}

	/**
	 * Sets the default text font color to whatever color {@link UIManager}
	 * returns for the specified {@link String}. If {@code uiManagerColorName}
	 * is {@code null} or invalid, the default font color is set to
	 * {@link #STANDARD_DEFAULT_FONT_COLOR}.
	 *
	 * @param uiManagerColorName the {@link String} consisting of a valid
	 *            {@link UIManager} font name.
	 */
	public void setDefaultFontColor(String uiManagerColorName) {
		Color newFontColor = STANDARD_DEFAULT_FONT_COLOR;
		if (isNotBlank(uiManagerColorName)) {
			newFontColor = UIManager.getColor(uiManagerColorName);
			if (newFontColor == null) {
				newFontColor = STANDARD_DEFAULT_FONT_COLOR;
			}
		}
		if (newFontColor != defaultFontColor) {
			defaultFontColor = newFontColor;
			repaint();
		}
	}

	/**
	 * Sets the default text font color. Set to {@code null} to disable default
	 * text color override.
	 *
	 * @param color the {@link Color} to set for the default text.
	 */
	public void setDefaultFontColor(Color color) {
		defaultFontColor = color;
		repaint();
	}

	/**
	 * @return The default text font color or {@code null} if color override is
	 *         disabled.
	 */
	public Color getDefaultFontColor() {
		return defaultFontColor;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);


		if (defaultText != null && this.getText().equals("")) {
			int height = this.getHeight();
			Font prevFont = g.getFont();
			Font defaultFont;
			if (defaultFontStyle >= 0) {
				defaultFont = prevFont.deriveFont(defaultFontStyle);
			} else {
				defaultFont = prevFont;
			}
			Color prevColor = g.getColor();
			Color defaultColor;
			if (defaultFontColor != null) {
				defaultColor = defaultFontColor;
			} else {
				defaultColor = prevColor;
			}
			g.setFont(defaultFont);
			g.setColor(defaultColor);
			int h = g.getFontMetrics().getHeight();
			int textBottom = (height - h) / 2 + h - 4;
			int x = this.getInsets().left;
			Graphics2D g2d = (Graphics2D) g;
			RenderingHints hints = g2d.getRenderingHints();
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.drawString(defaultText, x, textBottom);
			g2d.setRenderingHints(hints);
			g.setFont(prevFont);
			g.setColor(prevColor);
		}
	}

	/**
	 * Adds a {@link ChangeListener}.
	 *
	 * @param listener the {@link ChangeListener} to add.
	 */
	public void addChangeListener(ChangeListener listener) {
		changeListenerList.add(ChangeListener.class, listener);
	}

	/**
	 * Removes a {@link ChangeListener}.
	 *
	 * @param listener the {@link ChangeListener} to remove.
	 */
	public void removeChangeListener(ChangeListener listener) {
		changeListenerList.remove(ChangeListener.class, listener);
	}

	/**
	 * Returns an array of all registered {@link ChangeListener}s.
	 *
	 * @return The registered {@link ChangeListener}s.
	 */
	public ChangeListener[] getChangeListeners() {
		return changeListenerList.getListeners(ChangeListener.class);
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 *
	 * @param changeEvent the {@link ChangeEvent} to send to the registered
	 *            {@link ChangeListener}s.
	 * @see EventListenerList
	 */
	protected void fireStateChanged(ChangeEvent changeEvent) {
		// Guaranteed to return a non-null array
		ChangeListener[] listeners = changeListenerList.getListeners(ChangeListener.class);
		// Process the listeners last to first in case of removal.
		for (int i = listeners.length - 1; i >= 0; i--) {
			listeners[i].stateChanged(changeEvent);
		}
	}

	/**
	 * Commits a change, that is fire a {@link ChangeEvent} if the text has been
	 * changed.
	 */
	protected void commit() {
		if (undoText == null || !undoText.equals(getText())) {
			fireStateChanged(new ChangeEvent(this));
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		undoText = getText();
	}

	@Override
	public void focusLost(FocusEvent e) {
		commit();
		undoText = null;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_ESCAPE && undoText != null) {
			setText(undoText);
			e.consume();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	/**
	 * Due to the Swing authors' unique approach to OO (using private and packet
	 * private by default) this method is needed to make the "Enter" key
	 * register as a focus lost event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		commit();
		undoText = getText();
	}
}
