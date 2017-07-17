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

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import net.pms.configuration.PmsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A modified JTextArea which only keeps a given number of lines and disposes
 * of the oldest first when the given number is exceeded.
 *
 * @author Nadahar
 */
@SuppressWarnings("serial")
public class TextAreaFIFO extends JTextArea implements DocumentListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(TextAreaFIFO.class);
	private int maxLines;

    public TextAreaFIFO(int lines) {
        maxLines = lines;
        getDocument().addDocumentListener(this);
    }

    @Override
	public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                removeLines();
            }
        });
    }

    @Override
	public void removeUpdate(DocumentEvent e) {
    }

    @Override
	public void changedUpdate(DocumentEvent e) {
    }

    public void removeLines() {
        Element root = getDocument().getDefaultRootElement();
        while (root.getElementCount() > maxLines) {
            Element firstLine = root.getElement(0);
            try {
                getDocument().remove(0, firstLine.getEndOffset());
            } catch (BadLocationException ble) {
            	LOGGER.warn("Can't remove excess lines: {}", ble);
            }
        }
    }

    /**
     * Get how many lines {@link TextAreaFIFO} keeps
     * @return the current number of kept lines
     */
    public int getMaxLines() {
    	return maxLines;
    }

    /**
     * Set how many lines {@link TextAreaFIFO} should keep
     * @param lines the new number of kept lines
     */
    public void setMaxLines(int lines) {
		lines = Math.min(Math.max(lines, PmsConfiguration.LOGGING_LOGS_TAB_LINEBUFFER_MIN),PmsConfiguration.LOGGING_LOGS_TAB_LINEBUFFER_MAX);
    	maxLines = lines;
    }
}