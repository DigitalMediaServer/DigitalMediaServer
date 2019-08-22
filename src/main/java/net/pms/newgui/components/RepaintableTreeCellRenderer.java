package net.pms.newgui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.TreeCellRenderer;


/**
 * This is a copy of {@link javax.swing.tree.DefaultTreeCellRenderer} that does
 * not override {@link #invalidate()}, {@link #validate()},
 * {@link #revalidate()}, {@link #repaint()} and {@link #firePropertyChange}. It
 * also exposes {@link #tree} to subclasses.
 *
 * @see javax.swing.tree.DefaultTreeCellRenderer
 *
 * @author Nadahar
 */
public class RepaintableTreeCellRenderer extends JLabel implements TreeCellRenderer {

	private static final long serialVersionUID = 1L;

	/** Set to true after the constructor has run */
	private boolean inited;

	/**
	 * Creates a {@code RepaintableTreeCellRenderer}. Icons and text color are
	 * determined from the {@code UIManager}.
	 */
	public RepaintableTreeCellRenderer() {
		inited = true;
	}

	/** Last {@link JTree} the renderer was painted in */
	protected JTree tree;

	/** Is the value currently selected? */
	protected boolean selected;

	/** {@code true} if has focus */
	protected boolean hasFocus;

	/** {@code true} if draws focus border around icon as well */
	private boolean drawsFocusBorderAroundIcon;

	/** If {@code true}, a dashed line is drawn as the focus indicator */
	private boolean drawDashedFocusIndicator;

	// If drawDashedFocusIndicator is true, the following are used.
	/** The background {@link Color} of the tree. */
	private Color treeBGColor;

	/**
	 * The {@link Color} to draw the focus indicator in, determined from the
	 * background color
	 */
	private Color focusBGColor;

	// Icons
	/** The {@link Icon} used to show non-leaf nodes that aren't expanded */
	protected transient Icon closedIcon;

	/** The {@link Icon} used to show leaf nodes */
	protected transient Icon leafIcon;

	/** The {@link Icon} used to show non-leaf nodes that are expanded */
	protected transient Icon openIcon;

	// Colors
	/** The {@link Color} to use for the foreground for selected nodes */
	protected Color textSelectionColor;

	/** The {@link Color} to use for the foreground for non-selected nodes */
	protected Color textNonSelectionColor;

	/** The {@link Color} to use for the background when a node is selected. */
	protected Color backgroundSelectionColor;

	/**
	 * The {@link Color} to use for the background when the node isn't selected
	 */
	protected Color backgroundNonSelectionColor;

	/**
	 * The {@link Color} to use for the focus indicator when the node has focus
	 */
	protected Color borderSelectionColor;

	private boolean isDropCell;
	private boolean fillBackground = true;

	@Override
	public void updateUI() {
		super.updateUI();
		// To avoid invoking new methods from the constructor, the
		// inited field is first checked. If inited is false, the constructor
		// has not run and there is no point in checking the value. As
		// all look and feels have a non-null value for these properties,
		// a null value means the developer has specifically set it to
		// null. As such, if the value is null, this does not reset the
		// value.
		if (!inited || (getLeafIcon() instanceof UIResource)) {
			setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
		}
		if (!inited || (getClosedIcon() instanceof UIResource)) {
			setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
		}
		if (!inited || (getOpenIcon() instanceof UIManager)) {
			setOpenIcon(UIManager.getIcon("Tree.openIcon"));
		}
		if (!inited || (getTextSelectionColor() instanceof UIResource)) {
			setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
		}
		if (!inited || (getTextNonSelectionColor() instanceof UIResource)) {
			setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
		}
		if (!inited || (getBackgroundSelectionColor() instanceof UIResource)) {
			setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
		}
		if (!inited || (getBackgroundNonSelectionColor() instanceof UIResource)) {
			setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
		}
		if (!inited || (getBorderSelectionColor() instanceof UIResource)) {
			setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
		}
		drawsFocusBorderAroundIcon = UIManager.getBoolean("Tree.drawsFocusBorderAroundIcon");
		drawDashedFocusIndicator = UIManager.getBoolean("Tree.drawDashedFocusIndicator");
		Object value = UIManager.get("Tree.rendererFillBackground");
		fillBackground = (value instanceof Boolean) ? ((Boolean) value).booleanValue() : true;
		Insets margins = UIManager.getInsets("Tree.rendererMargins");
		if (margins != null) {
			setBorder(new EmptyBorder(margins.top, margins.left, margins.bottom, margins.right));
		}

		setName("Tree.cellRenderer");
	}

	/**
	 * @return The default {@link Icon}, for the current laf, that is used to
	 *         represent non-leaf nodes that are expanded.
	 */
	public Icon getDefaultOpenIcon() {
		return UIManager.getIcon("Tree.openIcon");
	}

	/**
	 * @return The default {@link Icon}, for the current laf, that is used to
	 *         represent non-leaf nodes that are not expanded.
	 */
	public Icon getDefaultClosedIcon() {
		return UIManager.getIcon("Tree.closedIcon");
	}

	/**
	 * @return The default {@link Icon}, for the current laf, that is used to
	 *         represent leaf nodes.
	 */
	public Icon getDefaultLeafIcon() {
		return UIManager.getIcon("Tree.leafIcon");
	}

	/**
	 * Sets the {@link Icon} used to represent non-leaf nodes that are expanded.
	 *
	 * @param newIcon the new open {@link Icon}.
	 */
	public void setOpenIcon(Icon newIcon) {
		openIcon = newIcon;
	}

	/**
	 * @return The {@link Icon} used to represent non-leaf nodes that are
	 *         expanded.
	 */
	public Icon getOpenIcon() {
		return openIcon;
	}

	/**
	 * Sets the {@link Icon} used to represent non-leaf nodes that are not
	 * expanded.
	 *
	 * @param newIcon new closed {@link Icon}.
	 */
	public void setClosedIcon(Icon newIcon) {
		closedIcon = newIcon;
	}

	/**
	 * @return The {@link Icon} used to represent non-leaf nodes that are not
	 *         expanded.
	 */
	public Icon getClosedIcon() {
		return closedIcon;
	}

	/**
	 * Sets the {@link Icon} used to represent leaf nodes.
	 *
	 * @param newIcon the new leaf {@link Icon}.
	 */
	public void setLeafIcon(Icon newIcon) {
		leafIcon = newIcon;
	}

	/**
	 * @return The icon used to represent leaf nodes.
	 */
	public Icon getLeafIcon() {
		return leafIcon;
	}

	/**
	 * Sets the {@link Color} the text is drawn with when the node is selected.
	 *
	 * @param newColor the new text {@link Color}.
	 */
	public void setTextSelectionColor(Color newColor) {
		textSelectionColor = newColor;
	}

	/**
	 * @return The {@link Color} the text is drawn with when the node is
	 *         selected.
	 */
	public Color getTextSelectionColor() {
		return textSelectionColor;
	}

	/**
	 * Sets the {@link Color} the text is drawn with when the node isn't
	 * selected.
	 *
	 * @param newColor the new text {@link Color} for selected nodes.
	 */
	public void setTextNonSelectionColor(Color newColor) {
		textNonSelectionColor = newColor;
	}

	/**
	 * @return The {@link Color} the text is drawn with when the node isn't
	 *         selected.
	 */
	public Color getTextNonSelectionColor() {
		return textNonSelectionColor;
	}

	/**
	 * Sets the {@link Color} to use for the background if node is selected.
	 *
	 * @param newColor new background {@link Color} for selected nodes.
	 */
	public void setBackgroundSelectionColor(Color newColor) {
		backgroundSelectionColor = newColor;
	}

	/**
	 * @return The {@link Color} to use for the background if node is selected.
	 */
	public Color getBackgroundSelectionColor() {
		return backgroundSelectionColor;
	}

	/**
	 * Sets the background {@link Color} to be used for non selected nodes.
	 *
	 * @param newColor new background {@link Color}.
	 */
	public void setBackgroundNonSelectionColor(Color newColor) {
		backgroundNonSelectionColor = newColor;
	}

	/**
	 * @return The background {@link Color} to be used for non selected nodes.
	 */
	public Color getBackgroundNonSelectionColor() {
		return backgroundNonSelectionColor;
	}

	/**
	 * Sets the {@link Color} to use for the border.
	 *
	 * @param newColor the new border {@link Color}.
	 */
	public void setBorderSelectionColor(Color newColor) {
		borderSelectionColor = newColor;
	}

	/**
	 * @return The {@link Color} the border is drawn.
	 */
	public Color getBorderSelectionColor() {
		return borderSelectionColor;
	}

	/**
	 * Overridden to map {@link FontUIResource}s to {@code null}. If
	 * {@code font} is {@code null}, or a {@link FontUIResource}, this has the
	 * effect of letting the font of the {@link JTree} show through. On the
	 * other hand, if {@link Font} is non-{@code null}, and not a
	 * {@link FontUIResource}, the font becomes {@code font}.
	 */
	@Override
	public void setFont(Font font) {
		if (font instanceof FontUIResource) {
			font = null;
		}
		super.setFont(font);
	}

	/**
	 * Gets the {@link Font} of this component.
	 *
	 * @return This component's {@link Font}; if a {@link Font} has not been set
	 *         for this component, the {@link Font} of its parent is returned.
	 */
	@Override
	public Font getFont() {
		Font font = super.getFont();

		if (font == null && tree != null) {
			// Strive to return a non-null value, otherwise the html support
			// will typically pick up the wrong font in certain situations.
			font = tree.getFont();
		}
		return font;
	}

	/**
	 * Overridden to map {@link ColorUIResource}s to {@code null}. If
	 * {@code color} is {@code null}, or a {@link ColorUIResource}, this has the
	 * effect of letting the background color of the {@link JTree} show through.
	 * On the other hand, if {@code color} is non-{@code null}, and not a
	 * {@link ColorUIResource}, the background becomes {@code color}.
	 */
	@Override
	public void setBackground(Color color) {
		if (color instanceof ColorUIResource) {
			color = null;
		}
		super.setBackground(color);
	}

	/**
	 * Configures the renderer based on the passed in components. The value is
	 * set from messaging the {@link JTree} with
	 * {@link JTree#convertValueToText}, which ultimately invokes
	 * {@link Object#toString()} on {@code value}. The foreground {@link Color}
	 * is set based on the selection and the {@link Icon} is set based on the
	 * {@code leaf} and {@code expanded} parameters.
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
		boolean hasFocus) {
		String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

		this.tree = tree;
		this.hasFocus = hasFocus;
		setText(stringValue);

		Color fg = null;
		isDropCell = false;

		JTree.DropLocation dropLocation = tree.getDropLocation();
		if (dropLocation != null && dropLocation.getChildIndex() == -1 && tree.getRowForPath(dropLocation.getPath()) == row) {

			Color col = UIManager.getColor("Tree.dropCellForeground");
			if (col != null) {
				fg = col;
			} else {
				fg = getTextSelectionColor();
			}

			isDropCell = true;
		} else if (sel) {
			fg = getTextSelectionColor();
		} else {
			fg = getTextNonSelectionColor();
		}

		setForeground(fg);

		Icon icon = null;
		if (leaf) {
			icon = getLeafIcon();
		} else if (expanded) {
			icon = getOpenIcon();
		} else {
			icon = getClosedIcon();
		}

		if (!tree.isEnabled()) {
			setEnabled(false);
			LookAndFeel laf = UIManager.getLookAndFeel();
			Icon disabledIcon = laf.getDisabledIcon(tree, icon);
			if (disabledIcon != null) {
				icon = disabledIcon;
			}
			setDisabledIcon(icon);
		} else {
			setEnabled(true);
			setIcon(icon);
		}
		setComponentOrientation(tree.getComponentOrientation());

		selected = sel;

		return this;
	}

	/**
	 * Paints the value. The background is filled based on selected.
	 */
	@Override
	public void paint(Graphics g) {
		Color bColor;

		if (isDropCell) {
			bColor = UIManager.getColor("Tree.dropCellBackground");
			if (bColor == null) {
				bColor = getBackgroundSelectionColor();
			}
		} else if (selected) {
			bColor = getBackgroundSelectionColor();
		} else {
			bColor = getBackgroundNonSelectionColor();
			if (bColor == null) {
				bColor = getBackground();
			}
		}

		int imageOffset = -1;
		if (bColor != null && fillBackground) {
			imageOffset = getLabelStart();
			g.setColor(bColor);
			if (getComponentOrientation().isLeftToRight()) {
				g.fillRect(imageOffset, 0, getWidth() - imageOffset, getHeight());
			} else {
				g.fillRect(0, 0, getWidth() - imageOffset, getHeight());
			}
		}

		if (hasFocus) {
			if (drawsFocusBorderAroundIcon) {
				imageOffset = 0;
			} else if (imageOffset == -1) {
				imageOffset = getLabelStart();
			}
			if (getComponentOrientation().isLeftToRight()) {
				paintFocus(g, imageOffset, 0, getWidth() - imageOffset, getHeight(), bColor);
			} else {
				paintFocus(g, 0, 0, getWidth() - imageOffset, getHeight(), bColor);
			}
		}
		super.paint(g);
	}

	private void paintFocus(Graphics g, int x, int y, int w, int h, Color notColor) {
		Color bsColor = getBorderSelectionColor();

		if (bsColor != null && (selected || !drawDashedFocusIndicator)) {
			g.setColor(bsColor);
			g.drawRect(x, y, w - 1, h - 1);
		}
		if (drawDashedFocusIndicator && notColor != null) {
			if (!treeBGColor.equals(notColor)) {
				treeBGColor = notColor;
				focusBGColor = new Color(~notColor.getRGB());
			}
			g.setColor(focusBGColor);
			BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
		}
	}

	private int getLabelStart() {
		Icon currentI = getIcon();
		if (currentI != null && getText() != null) {
			return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
		}
		return 0;
	}

	/**
	 * Overrides {@link JComponent#getPreferredSize()} to return a slightly
	 * wider preferred size value.
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension retDimension = super.getPreferredSize();

		if (retDimension != null) {
			retDimension = new Dimension(retDimension.width + 3, retDimension.height);
		}
		return retDimension;
	}

}
