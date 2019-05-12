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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.swing.ActionMap;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import net.pms.PMS;


/**
 * This is a subclass of {@link JSpinner} that implements support for
 * {@link SpinnerIntModel}.
 * <p>
 * By using {@link SpinnerIntModel} model, mouse-wheel scrolling is enabled and
 * irregular values with auto-complete is supported.
 *
 * @author Nadahar
 */
public class CustomJSpinner extends JSpinner {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@link CustomJSpinner} with a default
	 * {@link SpinnerIntModel} instance (that is, a {@link SpinnerIntModel} with
	 * value {@code 0}, step size {@code 1}, and no upper or lower limit).
	 *
	 * @param enterMoveFocus if {@code true} the {@code Enter} key will move the
	 *            focus to the next component.
	 *
	 * @see SpinnerIntModel
	 */
	public CustomJSpinner(boolean enterMoveFocus) {
		this(new SpinnerIntModel(0), enterMoveFocus);
	}

	/**
	 * Creates a new {@link JSpinner} with the specified {@link SpinnerModel}.
	 *
	 * @param model the {@link SpinnerIntModel} to use.
	 * @param enterMoveFocus if {@code true} the {@code Enter} key will move the
	 *            focus to the next component (basically behave the same as the
	 *            {@code Tab} key).
	 * @throws NullPointerException if {@code model} is {@code null}.
	 */
	public CustomJSpinner(@Nonnull SpinnerModel model, boolean enterMoveFocus) {
		super(model);
		JComponent editor = getEditor();

		if (model instanceof SpinnerIntModel) {
			addMouseWheelListener(new MouseWheelRoll(this, (SpinnerIntModel) model));
			if (editor instanceof IntegerEditor) {
				updateIrregularEntries();
			}
		}

		if (enterMoveFocus) {
			if (editor instanceof DefaultEditor) {
				((DefaultEditor) editor).getTextField().addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							FocusManager.getCurrentManager().focusNextComponent();
						}
					}
				});
			}
		}
	}

	// Doc: Must be called after updating irregular entries on model
	/**
	 * Effectuates changes to the irregular entries. This must be called if
	 * changes to the irregular entries have been made to the model since this
	 * {@link CustomJSpinner} was created. This effectively means that you have
	 * to call this method after invoking
	 * {@link SpinnerIntModel#setIrregularEntries(Collection)} or
	 * {@link SpinnerIntModel#addIrregularEntries(Collection)}.
	 * <p>
	 * If the {@link SpinnerModel} isn't a {@link SpinnerIntModel}, this method
	 * is a no-op.
	 */
	public void updateIrregularEntries() {
		SpinnerModel model = getModel();
		if (model instanceof SpinnerIntModel) {
			JComponent editor = getEditor();
			if (editor instanceof IntegerEditor) {
				Set<IrregularEntry> irregularEntries = ((SpinnerIntModel) model).getIrregularEntries();
				if (!irregularEntries.isEmpty()) {
					((IntegerEditor) editor).setIrregularEntries(irregularEntries);
				}
			}
		}
	}

	@Override
	protected JComponent createEditor(SpinnerModel model) {
		if (model instanceof SpinnerIntModel) {
			return new IntegerEditor(this);
		}
		return super.createEditor(model);
	}

	/**
	 * This {@link MouseWheelListener} makes the spinner increment or decrement
	 * its value by rolling the mouse wheel.
	 *
	 * @author Nadahar
	 */
	protected static class MouseWheelRoll implements MouseWheelListener {

		private CustomJSpinner spinner;
		private SpinnerIntModel model;

		private MouseWheelRoll(CustomJSpinner spinner, SpinnerIntModel model) {
			this.spinner = spinner;
			this.model = model;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (!spinner.isEnabled()) {
				return;
			}
			if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				return;
			}

			Object newValue = e.getWheelRotation() < 0 ? model.getNextValue() : model.getPreviousValue();
			if (newValue != null) {
				model.setValue(newValue);
			}
		}
	}

	/**
	 * An editor for a {@link JSpinner} whose model is a
	 * {@link SpinnerIntModel}. The value of the editor is displayed with a
	 * {@link JFormattedTextField} whose format is defined by a
	 * {@link NumberFormatter} instance whose {@code minimum} and
	 * {@code maximum} properties are mapped to the {@link SpinnerIntModel}.
	 */
	public static class IntegerEditor extends DefaultEditor {

		private static final long serialVersionUID = 1L;
		private int regularPreferredWidth;
		private int irregularPreferredWidth;
		private FontMetrics fontMetrics;

		@Nonnull
		private final Set<IrregularEntry> irregularEntries = new HashSet<>();

		@Nonnull
		private IntegerEditorFormatter formatter;

		@Nullable
		private AutoCompleteListener autoCompleteListener;

		/**
		 * Creates a new {@link JSpinner} editor that supports displaying and
		 * editing the value of a {@link SpinnerIntModel} with a
		 * {@link JFormattedTextField}. The new {@link IntegerEditor} becomes
		 * both a {@link ChangeListener} on the specified {@link JSpinner} and a
		 * {@link PropertyChangeListener} on the new
		 * {@link JFormattedTextField}.
		 *
		 * @param spinner the {@link JSpinner} whose model this
		 *            {@link IntegerEditor} will monitor.
		 * @exception IllegalArgumentException if the {@link SpinnerModel} isn't
		 *                an {@link SpinnerIntModel}.
		 *
		 * @see #getModel
		 * @see #getFormat
		 * @see SpinnerIntModel
		 */
		public IntegerEditor(@Nonnull JSpinner spinner) {
			this(spinner, NumberFormat.getIntegerInstance(PMS.getLocale()));
		}

		/**
		 * Creates a new {@link JSpinner} editor that supports displaying and
		 * editing the value of a {@link SpinnerIntModel} with a
		 * {@link JFormattedTextField}. The new {@link IntegerEditor} becomes
		 * both a {@link ChangeListener} on the specified {@link JSpinner} and a
		 * {@link PropertyChangeListener} on the new
		 * {@link JFormattedTextField}.
		 *
		 * @param spinner the {@link JSpinner} whose model this
		 *            {@link IntegerEditor} will monitor.
		 * @param format the {@link NumberFormat} instance that's used to
		 *            display and parse the value of the text field.
		 * @exception IllegalArgumentException if the {@link SpinnerModel} isn't
		 *                an {@link SpinnerIntModel}.
		 *
		 * @see #getTextField
		 * @see SpinnerIntModel
		 * @see java.text.DecimalFormat
		 */
		public IntegerEditor(@Nonnull JSpinner spinner, @Nonnull NumberFormat format) {
			super(spinner);
			if (!(spinner.getModel() instanceof SpinnerIntModel)) {
				throw new IllegalArgumentException("model isn't a SpinnerIntModel");
			}

			// Hack the super constructor by removing the JFormattedTextField
			// it inserted and replacing it with our own.
			JFormattedTextField textField = new JFormattedTextField() {
				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getPreferredSize() {
					Dimension size = super.getPreferredSize();
					int width = Math.max(regularPreferredWidth, irregularPreferredWidth);
					if (width > 0) {
						if (fontMetrics == null) {
							setFontMetrics();
						}
						Insets insets = getInsets();
						size.width = width + insets.left + insets.right + fontMetrics.charWidth('e');
					}
					return size;
				}
			};
			textField.setName("Spinner.formattedTextField");
			textField.setValue(spinner.getValue());
			textField.addPropertyChangeListener(this);
			textField.setEditable(true);
			textField.setInheritsPopupMenu(true);

			String toolTipText = spinner.getToolTipText();
			if (toolTipText != null) {
				textField.setToolTipText(toolTipText);
			}

			JFormattedTextField oldTextField = getTextField();
			remove(oldTextField);
			add(textField);

			ActionMap oldTextFieldMap = oldTextField.getActionMap();
			ActionMap textFieldMap = textField.getActionMap();
			if (oldTextFieldMap != null && textFieldMap != null) {
				textFieldMap.put("increment", oldTextFieldMap.get("increment"));
				textFieldMap.put("decrement", oldTextFieldMap.get("decrement"));
			}

			oldTextField.removePropertyChangeListener(this);
			// Hack over

			format.setGroupingUsed(false);
			format.setMaximumFractionDigits(0);
			SpinnerIntModel model = (SpinnerIntModel) spinner.getModel();
			formatter = new IntegerEditorFormatter(model, format);
			DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
			textField.setFormatterFactory(factory);
			textField.setHorizontalAlignment(JTextField.TRAILING);

			calculateRegularPreferredWidth(model);
			irregularEntries.addAll(model.getIrregularEntries());
			if (!irregularEntries.isEmpty()) {
				calculateIrregularPreferredWidth();
				String[] entries = new String[irregularEntries.size()];
				int i = 0;
				for (IrregularEntry entry : irregularEntries) {
					entries[i] = entry.description;
					irregularPreferredWidth = Math.max(irregularPreferredWidth, fontMetrics.stringWidth(entry.description));
					i++;
				}
				autoCompleteListener = new AutoCompleteListener(textField, entries);
				textField.getDocument().addDocumentListener(autoCompleteListener);
				invalidate();
			} else if (regularPreferredWidth != 0) {
				invalidate();
			}
		}

		/**
		 * Sets the irregular entries for this {@link IntegerEditor}. This
		 * method will manage the {@link AutoCompleteListener} and update the
		 * {@link IntegerEditorFormatter} accordingly.
		 *
		 * @param irregularEntries the {@link Collection} of
		 *            {@link IrregularEntry} instances to set.
		 */
		public void setIrregularEntries(@Nullable Collection<IrregularEntry> irregularEntries) {
			JFormattedTextField textField = getTextField();
			if (autoCompleteListener != null) {
				textField.getDocument().removeDocumentListener(autoCompleteListener);
			}

			this.irregularEntries.clear();
			irregularPreferredWidth = 0;
			if (irregularEntries != null && !irregularEntries.isEmpty()) {
				this.irregularEntries.addAll(irregularEntries);
				if (fontMetrics == null) {
					setFontMetrics();
				}
				String[] entries = new String[irregularEntries.size()];
				int i = 0;
				for (IrregularEntry entry : irregularEntries) {
					entries[i] = entry.description;
					irregularPreferredWidth = Math.max(irregularPreferredWidth, fontMetrics.stringWidth(entry.description));
					i++;
				}
				autoCompleteListener = new AutoCompleteListener(textField, entries);
				textField.getDocument().addDocumentListener(autoCompleteListener);
			} else {
				autoCompleteListener = null;
			}
			formatter.setIrregularEntries(irregularEntries);
			invalidate();
		}

		/**
		 * Calculates the maximum width in pixels needed to display the
		 * "regular" range for the specified {@link SpinnerIntModel}.
		 *
		 * @param model the {@link SpinnerIntModel}.
		 */
		protected void calculateRegularPreferredWidth(@Nullable SpinnerIntModel model) {
			if (model == null) {
				return;
			}
			if (fontMetrics == null) {
				setFontMetrics();
			}

			try {
				regularPreferredWidth = Math.max(
					fontMetrics.stringWidth(formatter.valueToString(model.getMinimum())),
					fontMetrics.stringWidth(formatter.valueToString(model.getMaximum())));
			} catch (ParseException | NullPointerException e) {
				regularPreferredWidth = 0;
			}
		}

		/**
		 * Calculates the maximum width in pixels needed to display the
		 * currently defined irregular entries.
		 */
		protected void calculateIrregularPreferredWidth() {
			if (fontMetrics == null) {
				setFontMetrics();
			}

			irregularPreferredWidth = 0;
			if (irregularEntries != null) {
				for (IrregularEntry entry : irregularEntries) {
					irregularPreferredWidth = Math.max(irregularPreferredWidth, fontMetrics.stringWidth(entry.description));
				}
			}
		}

		/**
		 * Sets/updates {@code fontMetrics}.
		 */
		protected void setFontMetrics() {
			fontMetrics = getFontMetrics(getFont());
		}

		@Override
		public void setFont(Font font) {
			super.setFont(font);
			setFontMetrics();
			calculateRegularPreferredWidth(getModel());
			calculateIrregularPreferredWidth();
			invalidate();
		}

		/**
		 * @return The {@link SpinnerIntModel}.
		 */
		@Nullable
		public SpinnerIntModel getModel() {
			JSpinner spinner = getSpinner();
			return (SpinnerIntModel) (spinner == null ? null : getSpinner().getModel());
		}
	}

	/**
	 * This is a specialized {@link NumberFormatter} extension which respects
	 * the minimum and maximum values and handles conversion of the irregular
	 * entries from a given {@link SpinnerIntModel}. It also sets the value type
	 * to {@link Integer}.
	 *
	 * @author Nadahar
	 */
	protected static class IntegerEditorFormatter extends NumberFormatter {

		private static final long serialVersionUID = 1L;

		@Nonnull
		private final SpinnerIntModel model;

		@Nullable
		private final Map<Integer, String> irregularValueToString = new HashMap<>();

		@Nullable
		private final Map<String, Integer> irregularStringToValue = new HashMap<>();

		IntegerEditorFormatter(
			@Nonnull SpinnerIntModel model,
			@Nullable NumberFormat format
		) {
			super(format);
			if (model == null) {
				throw new IllegalArgumentException("model cannot be null");
			}
			this.model = model;
			setValueClass(model.getValue().getClass());
			Set<IrregularEntry> irregularEntries = model.getIrregularEntries();
			if (!irregularEntries.isEmpty()) {
				setIrregularEntries(irregularEntries);
			}
		}

		/**
		 * Sets the irregular entries for this {@link IntegerEditorFormatter}.
		 *
		 * @param irregularEntries the {@link Collection} of
		 *            {@link IrregularEntry} instances to set.
		 */
		public void setIrregularEntries(@Nullable Collection<IrregularEntry> irregularEntries) {
			irregularStringToValue.clear();
			irregularValueToString.clear();
			if (irregularEntries != null) {
				for (IrregularEntry entry : irregularEntries) {
					irregularStringToValue.put(entry.description, Integer.valueOf(entry.value));
					irregularValueToString.put(Integer.valueOf(entry.value), entry.description);
				}
			}
		}

		@Override
		public Comparable<Integer> getMinimum() {
			return model.getMinimum();
		}

		@Override
		public Comparable<Integer> getMaximum() {
			return model.getMaximum();
		}

		@Override
		public Object stringToValue(String text) throws ParseException {
			if (text != null) {
				Integer value = irregularStringToValue.get(text);
				if (value != null) {
					return value;
				}
			}

			return super.stringToValue(text);
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			if (value instanceof Integer) {
				String text = irregularValueToString.get(value);
				if (text != null) {
					return text;
				}
			}

			return super.valueToString(value);
		}
	}

	/**
	 * A simple container representing an irregular entry, that is a combination
	 * of an integer value and a description constituting an irregular entry.
	 *
	 * @author Nadahar
	 */
	@Immutable
	public static class IrregularEntry {

		private final int value;

		@Nonnull
		private final String description;

		/**
		 * Creates a new instance using the specified parameters.
		 *
		 * @param value the {@code int} value.
		 * @param description the descriptive text.
		 */
		public IrregularEntry(int value, @Nonnull String description) {
			if (description == null) {
				throw new IllegalArgumentException("description cannot be null");
			}
			this.value = value;
			this.description = description;
		}

		/**
		 * @return The {@code int} value.
		 */
		public int getValue() {
			return value;
		}

		/**
		 * @return The descriptive text.
		 */
		@Nonnull
		public String getDescription() {
			return description;
		}
	}

	/**
	 * This is a {@link DocumentListener} implementation that will monitor the
	 * {@link Document} of a given {@link JTextComponent} and auto-complete any
	 * of the given "completions" when the input matches the start of one of
	 * them.
	 * <p>
	 * The auto-completion isn't optional (the user isn't shown a suggestion
	 * which she can accept/reject), so this is probably most suitable for a
	 * relatively limited scope where the start of a given "completion" is very
	 * likely to have that word/phrase as the intended target.
	 *
	 * @author Nadahar
	 */
	public static class AutoCompleteListener implements DocumentListener {

		private final JTextComponent textComponent;
		private final Set<String> completions;

		/**
		 * Creates a new instance that will monitor the specified
		 * {@link JTextComponent} and auto-complete the specified "completions".
		 *
		 * @param textComponent the {@link JTextComponent} to monitor.
		 * @param completions one or more {@link String} to auto-complete.
		 *
		 * @throws IllegalArgumentException if {@code textComponent} is
		 *             {@code null} or if there isn't at least one "completion".
		 */
		public AutoCompleteListener(@Nonnull JTextComponent textComponent, @Nonnull String... completions) {
			if (textComponent == null) {
				throw new IllegalArgumentException("textComponent cannot be null");
			}
			if (completions == null || completions.length == 0) {
				throw new IllegalArgumentException("completions cannot be null or empty");
			}
			this.textComponent = textComponent;
			this.completions = new HashSet<>(Arrays.asList(completions));

		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (e.getLength() != 1) {
				return;
			}

			int pos = e.getOffset() + 1;
			String text;
			Locale locale = PMS.getLocale();
			try {
				text = e.getDocument().getText(0, pos).toLowerCase(locale);
			} catch (BadLocationException be) {
				return;
			}
			String lowerCompletion;
			for (String completion : completions) {
				if (text.length() < completion.length()) {
					lowerCompletion = completion.toLowerCase(locale);
					if (lowerCompletion.startsWith(text)) {
						SwingUtilities.invokeLater(new AutoCompleteTask(textComponent, completion, pos));
						return;
					}
				}
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		/**
		 * A {@link Runnable} implementation that performs an individual
		 * auto-complete task.
		 *
		 * @author Nadahar
		 */
		protected static class AutoCompleteTask implements Runnable {

			private final JTextComponent textComponent;
			private final String newText;
			private final int position;

			/**
			 * Creates a new instance that will perform auto-completion on the
			 * specified {@link JTextComponent} using the specified new text and
			 * position.
			 *
			 * @param textComponent the {@link JTextComponent} to act on.
			 * @param newText the new text to set.
			 * @param position the cursor position to maintain.
			 */
			public AutoCompleteTask(@Nonnull JTextComponent textComponent, @Nonnull String newText, int position) {
				this.textComponent = textComponent;
				this.newText = newText;
				this.position = position;
			}

			@Override
			public void run() {
				textComponent.setText(newText);
				textComponent.setCaretPosition(newText.length());
				textComponent.moveCaretPosition(position);
			}
		}
	}
}
