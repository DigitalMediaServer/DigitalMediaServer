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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class SearchableMutableTreeNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = 8466944555448955118L;

	public SearchableMutableTreeNode(String nodeName) {
		super(nodeName);
	}

	public SearchableMutableTreeNode(String nodeName, boolean allowsChildren) {
		super(nodeName, allowsChildren);
	}

	protected SearchableMutableTreeNode findChild(String searchName, boolean recursive, boolean specialGroupRules) throws IllegalChildException {
		if (getChildCount() > 0) {
			for (int i = 0; i < getChildCount(); i++) {
				TreeNode currentTNChild = getChildAt(i);
				if (currentTNChild instanceof SearchableMutableTreeNode) {
					SearchableMutableTreeNode currentChild = (SearchableMutableTreeNode) currentTNChild;
					if (currentChild.getNodeName().equalsIgnoreCase(searchName)) {
						return currentChild;
					} else if (specialGroupRules && searchName.equalsIgnoreCase(currentChild.getParent().getNodeName() + " " + currentChild.getNodeName())) {
						// Search for the special group rule where grouping is done on the first word (so that the parent has the first word in the name)
						return currentChild;
					}
				} else {
					throw new IllegalChildException("All children must be SearchMutableTreeNode or subclasses thereof");
				}
			}
			// Do recursive search in separate loop to avoid finding a matching subnode before a potential match at the current level
			if (recursive) {
				SearchableMutableTreeNode result = null;
				for (int i = 0; i < getChildCount(); i++) {
					SearchableMutableTreeNode currentChild = (SearchableMutableTreeNode) getChildAt(i);
					if (!currentChild.isLeaf()) {
						result = currentChild.findChild(searchName, true, specialGroupRules);
						if (result != null) {
							return result;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Search the node's immediate children.
	 *
	 * @param searchName the object to search for.
	 * @return the found node or {@code null}.
	 * @throws IllegalChildException If a child that doesn't implement
	 *             {@link SearchableMutableTreeNode} is encountered.
	 */
	public SearchableMutableTreeNode findChild(String searchName) throws IllegalChildException {

		return findChild(searchName, false, false);
	}

	/**
	 * Search the node's children recursively.
	 *
	 * @param searchName the object to search for.
	 * @return The found node or {@code null}.
	 * @throws IllegalChildException If a child that doesn't implement
	 *             {@link SearchableMutableTreeNode} is encountered.
	 */
	public SearchableMutableTreeNode findInBranch(String searchName, boolean specialGroupRules) throws IllegalChildException {
		return findChild(searchName, true, specialGroupRules);
	}

	public String getNodeName() {
		return (String) super.getUserObject();
	}

	@Override
	public SearchableMutableTreeNode getParent() {
		return (SearchableMutableTreeNode) parent;
	}

	/**
	 * A checked {@link Exception} that indicates that a child that doesn't
	 * fulfill the requirements has been encountered.
	 */
	public static class IllegalChildException extends Exception {

		private static final long serialVersionUID = 1152260088011461750L;

		/**
		 * Creates a new exception with {@code null} as its detail message.
		 */
		public IllegalChildException() {
		}

		/**
		 * Creates a new exception with the specified detail message.
		 *
		 * @param message the detail message.
		 */
		public IllegalChildException(String message) {
			super(message);
		}

		/**
		 * Creates a new exception with the specified code cause and a detail
		 * message of {@code (cause==null ? null : cause.toString())}.
		 *
		 * @param cause the {@link Throwable} which caused this
		 *            {@link IllegalChildException}.
		 */
		public IllegalChildException(Throwable cause) {
			super(cause);
		}

		/**
		 * Creates a new exception with the specified detail message and cause.
		 * <p>
		 * <b>Note</b>: The detail message associated with {@code cause} is
		 * <b><i>not</i></b> automatically incorporated in this exception's detail
		 * message.
		 *
		 * @param message the detail message.
		 * @param cause the {@link Throwable} which caused this
		 *            {@link IllegalChildException}.
		 */
		public IllegalChildException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
