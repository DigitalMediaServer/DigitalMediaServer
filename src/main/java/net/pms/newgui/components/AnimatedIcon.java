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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Timer;
import net.pms.newgui.LooksFrame;

/**
 * An {@link AnimatedIcon} will display a series of {@link AnimatedIconFrame}s
 * that each contains the {@link Icon} itself and the time to display that
 * particular frame. The animation sequence will cycle until stopped.
 *
 * The size of the {@link Icon} is determined to be the largest width or height
 * of any of the {@link Icon}s. All other {@link Icon}s are then aligned within
 * the space available when they are painted.
 *
 * An {@link AnimatedIcon} instance cannot be shared by different components.
 * However, the {@link Icon}s added to an {@link AnimatedIcon} can be shared.
 *
 * This class is in part based on Rob Camick's AnimatedIcon
 * (https://tips4java.wordpress.com/2009/06/21/animated-icon/).
 *
 * @author Nadahar
 */
public class AnimatedIcon implements Icon, ActionListener {

	/** Top alignment */
	public static final float TOP = 0.0f;

	/** Left alignment */
	public static final float LEFT = 0.0f;

	/** Center alignment */
	public static final float CENTER = 0.5f;

	/** Bottom alignment */
	public static final float BOTTOM = 1.0f;

	/** Right alignment */
	public static final float RIGHT = 1.0f;

	private JComponent component;
	private final ArrayList<AnimatedIconFrame> frames = new ArrayList<>();

	/** The current {@link RunState} of this {@link AnimatedIcon} */
	private RunState runState = RunState.STOPPED;
	private boolean repeat = false;
	private AnimatedIconStage nextStage = null;
	private AnimatedIconStage permanentStage = null;

	/**
	 * The suspend reference counter.
	 * <p>
	 * Suspending an animation is only meant to be used when the animation is
	 * invisible because of events like selecting another tab or minimizing the
	 * application. The calls are thus expected to be event driven and matched
	 * by an equal number of {@link #unsuspend()} calls before the animation is
	 * visible again. Calls to {@link #suspend()} and {@link #unsuspend()} are
	 * therefore reference counted so that callers won't have to keep track of
	 * anything but canceling "their own" suspension.
	 */
	private int suspendLevel;

	private float alignmentX = CENTER;
	private float alignmentY = CENTER;

	//  Track the X, Y location of the Icon within its parent JComponent so we
	//  can request a repaint of only the Icon and not the entire JComponent

	private int iconX;
	private int iconY;

	//  Used for the implementation of Icon interface

	private int maxIconWidth;
	private int maxIconHeight;

	//  Use to control processing

	private int currentFrameIndex;
	private final Timer timer;
	private Random random;


	/**
	 * Creates a new instance.
	 *
	 * @param component the component the icon will be painted on.
	 * @param nextStage the {@link AnimatedIconStage} to call after this
	 *            animation has finished.
	 * @param repeat determines if the animation should loop once the end is
	 *            reached.
	 * @param frames the {@link AnimatedIconFrame}s to be painted as an
	 *            animation.
	 */
	private AnimatedIcon(
		@Nonnull JComponent component,
		@Nullable AnimatedIconStage nextStage,
		boolean repeat,
		@Nonnull List<AnimatedIconFrame> frames
	) {
		if (component == null) {
			throw new IllegalArgumentException("component cannot be null");
		}
		if (frames == null || frames.size() == 0) {
			throw new IllegalArgumentException("frames cannot be null or empty");
		}
		this.component = component;
		this.repeat = repeat;
		this.nextStage = nextStage;
		if (nextStage != null && nextStage.permanent) {
			this.permanentStage = nextStage;
		}

		timer = new Timer(frames.get(0).durationMS, this);
		timer.setRepeats(false);
		setFrames(frames);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param component the component the icon will be painted on.
	 * @param repeat determines if the animation should loop once the end is
	 *            reached.
	 * @param frames the {@link AnimatedIconFrame}s to be painted as an
	 *            animation.
	 */
	public AnimatedIcon(
		@Nonnull JComponent component,
		boolean repeat,
		@Nonnull List<AnimatedIconFrame> frames
	) {
		this(component, null, repeat, frames);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param component the component the icon will be painted on.
	 * @param nextStage the {@link AnimatedIconStage} to call after this
	 *            animation has finished.
	 * @param frames the {@link AnimatedIconFrame}s to be painted as an
	 *            animation.
	 */
	public AnimatedIcon(
		@Nonnull JComponent component,
		@Nullable AnimatedIconStage nextStage,
		@Nonnull List<AnimatedIconFrame> frames
	) {
		this(component, nextStage, false, frames);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param component the component the icon will be painted on.
	 * @param nextStage the {@link AnimatedIconStage} to call after this
	 *            animation has finished.
	 * @param repeat determines if the animation should loop once the end is
	 *            reached.
	 * @param frames the {@link AnimatedIconFrame}s to be painted as an
	 *            animation.
	 */
	private AnimatedIcon(
		@Nonnull JComponent component,
		@Nullable AnimatedIconStage nextStage,
		boolean repeat,
		@Nonnull AnimatedIconFrame... frames
	) {
		if (component == null) {
			throw new IllegalArgumentException("component cannot be null");
		}
		if (frames == null || frames.length == 0) {
			throw new IllegalArgumentException("frames cannot be null or empty");
		}
		this.component = component;
		this.repeat = repeat;
		this.nextStage = nextStage;
		if (nextStage != null && nextStage.permanent) {
			this.permanentStage = nextStage;
		}

		timer = new Timer(frames[0].durationMS, this);
		timer.setRepeats(false);
		setFrames(frames);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param component the component the icon will be painted on.
	 * @param repeat determines if the animation should loop once the end is
	 *            reached.
	 * @param frames the {@link AnimatedIconFrame}s to be painted as an
	 *            animation.
	 */
	public AnimatedIcon(
		@Nonnull JComponent component,
		boolean repeat,
		@Nonnull AnimatedIconFrame... frames
	) {
		this(component, null, repeat, frames);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param component the component the icon will be painted on.
	 * @param nextStage the {@link AnimatedIconStage} to call after this
	 *            animation has finished.
	 * @param frames the {@link AnimatedIconFrame}s to be painted as an
	 *            animation.
	 */
	public AnimatedIcon(
		@Nonnull JComponent component,
		@Nullable AnimatedIconStage nextStage,
		@Nonnull AnimatedIconFrame... frames
	) {
		this(component, nextStage, false, frames);
	}

	/**
	 * Creates a new instance with one static frame.
	 *
	 * @param component the component the icon will be painted on.
	 * @param icon the {@link Icon} to use for that one frame.
	 */
	public AnimatedIcon(
		@Nonnull JComponent component,
		@Nonnull Icon icon
	) {
		this(component, false, AnimatedIcon.buildAnimation(icon));
	}

	/**
	 * Creates a new instance with one static frame.
	 *
	 * @param component the component the icon will be painted on.
	 * @param resourceName the resource name of the image to use for that one
	 *            frame.
	 */
	public AnimatedIcon(
		@Nonnull JComponent component,
		@Nonnull String resourceName
	) {
		this(component, false, AnimatedIcon.buildAnimation(resourceName));
	}

	/**
	 * Sets the sequence of {@link AnimatedIconFrame}s to animate.
	 *
	 * @param frames a {@link List} of {@link AnimatedIconFrame}s.
	 */
	public void setFrames(@Nonnull List<AnimatedIconFrame> frames) {
		if (frames == null) {
			throw new IllegalArgumentException("frames cannot be null");
		}

		this.frames.clear();
		this.frames.ensureCapacity(frames.size());
		for (AnimatedIconFrame frame : frames) {
			if (frame == null) {
				throw new IllegalArgumentException("A frame cannot be null");
			}
			if (frame.icon == null) {
				throw new IllegalArgumentException("An icon cannot be null");
			}
			if (frame.durationMS < 0) {
				throw new IllegalArgumentException("Length can't be negative");
			}
			if (frame.minDurationMS < 0) {
				throw new IllegalArgumentException("Minimum length can't be negative");
			}

			this.frames.add(frame);
		}
		calculateIconDimensions();
		setCurrentFrameIndex(0, true);
	}

	/**
	 * Sets the sequence of {@link AnimatedIconFrame}s to animate.
	 *
	 * @param frames an array of {@link AnimatedIconFrame}s.
	 */
	public void setFrames(@Nonnull AnimatedIconFrame... frames) {
		if (frames == null) {
			throw new IllegalArgumentException("frames cannot be null");
		}

		this.frames.clear();
		this.frames.ensureCapacity(frames.length);
		for (AnimatedIconFrame frame : frames) {
			if (frame == null) {
				throw new IllegalArgumentException("A frame cannot be null");
			}
			if (frame.icon == null) {
				throw new IllegalArgumentException("An icon cannot be null");
			}
			if (frame.durationMS < 0) {
				throw new IllegalArgumentException("Length can't be negative");
			}
			if (frame.minDurationMS < 0) {
				throw new IllegalArgumentException("Minimum length can't be negative");
			}

			this.frames.add(frame);
		}
		calculateIconDimensions();
		setCurrentFrameIndex(0, true);
	}

	/**
	 * Sets the {@link AnimatedIconStage} to call after this animation has
	 * finished. Sets repeat to {@code false} and restarts the timer if it's not
	 * running.
	 *
	 * @param nextStage the {@link AnimatedIconStage} to set next.
	 */
	public void setNextStage(@Nullable AnimatedIconStage nextStage) {
		this.nextStage = nextStage;
		if (nextStage == null) {
			return;
		}
		if (nextStage.permanent) {
			this.permanentStage = nextStage;
		}
		if (!timer.isRunning() && suspendLevel == 0) {
			timer.restart();
		}
	}

	/**
	 * Calculates the width and height of the Icon based on the maximum width
	 * and height of any individual Icon.
	 */
	private void calculateIconDimensions() {
		maxIconWidth = 0;
		maxIconHeight = 0;

		for (AnimatedIconFrame frame : frames) {
			maxIconWidth = Math.max(maxIconWidth, frame.icon.getIconWidth());
			maxIconHeight = Math.max(maxIconHeight, frame.icon.getIconHeight());
		}
	}

	/**
	 * Gets the alignment of the icon on the x-axis.
	 *
	 * @return the alignment
	 */
	public float getAlignmentX() {
		return alignmentX;
	}

	/**
	 * Specifies the horizontal alignment of the icon.
	 *
	 * @param alignmentX common values are {@code LEFT}, {@code CENTER}
	 *            (default) or {@code RIGHT} although any value between
	 *            {@code 0.0} and {@code 1.0} can be used.
	 */
	public void setAlignmentX(float alignmentX) {
		this.alignmentX = alignmentX > 1.0f ? 1.0f : alignmentX < 0.0f ? 0.0f : alignmentX;
	}

	/**
	 * Gets the alignment of the icon on the y-axis.
	 *
	 * @return the alignment.
	 */
	public float getAlignmentY() {
		return alignmentY;
	}

	/**
	 * Specifies the vertical alignment of the icon.
	 *
	 * @param alignmentY common values {@code TOP}, {@code CENTER} (default) or
	 *            {@code BOTTOM} although any value between {@code 0.0} and
	 *            {@code 1.0} can be used.
	 */
	public void setAlignmentY(float alignmentY) {
		this.alignmentY = alignmentY > 1.0f ? 1.0f : alignmentY < 0.0f ? 0.0f : alignmentY;
	}

	/**
	 * Sets the index of the frame to be displayed and then repaint the
	 * {@link Icon}.
	 *
	 * @param index the index of the {@link AnimatedIconFrame} to be displayed
	 * @param paint determines if the new frame should be painted
	 */
	private void setCurrentFrameIndex(int index, boolean paint) {
		currentFrameIndex = index;
		final AnimatedIconFrame frame = frames.get(currentFrameIndex);
		if (frame.random) {
			if (random == null) {
				random = new Random();
			}
			timer.setInitialDelay(frame.minDurationMS + random.nextInt(frame.durationMS - frame.minDurationMS + 1));
		} else {
			timer.setInitialDelay(frame.durationMS);
		}
		if (runState == RunState.RUNNING && suspendLevel == 0) {
			timer.restart();
		}
		if (paint) {
			component.repaint(iconX, iconY, maxIconWidth, maxIconHeight);
			if (component instanceof AnimatedTreeCellRenderer) {
				// JTree needs some special treatment as nodes aren't automatically repainted
				((AnimatedTreeCellRenderer) component).repaintAffectedNodes(this);
			}
		}
	}

	/**
	 * Starts the animation from the beginning. Does not alter the
	 * {@link #runState}.
	 *
	 * @see #restartArm()
	 */
	public void restart() {
		setCurrentFrameIndex(0, true);
		startArm();
	}

	/**
	 * Sets the animation to continue from the beginning either by a call to
	 * {@link #resume()} or the next time {@link #paintIcon} is called.
	 *
	 * @see #restart()
	 */
	public void restartArm() {
		setCurrentFrameIndex(0, false);
		pause();
	}

	/**
	 * Starts the animation immediately without waiting for the
	 * {@link AnimatedIcon} to be painted.
	 *
	 * @see #startArm()
	 */
	public void start() {
		if (!timer.isRunning() && suspendLevel == 0) {
			timer.restart();
		}
		runState = RunState.RUNNING;
	}

	/**
	 * Arms the animation so that the timer is started either by a call to
	 * {@link #resume()} or the next time {@link #paintIcon} is called.
	 *
	 * @see #start()
	 */
	public void startArm() {
		runState = RunState.RUNNING;
	}

	/**
	 *  Stops the animation. The current frame will be displayed.
	 */
	public void stop() {
		if (timer.isRunning()) {
			timer.stop();
		}
		runState = RunState.STOPPED;
	}

	/**
	 * Pauses the animation at the current frame.
	 */
	public void pause() {
		if (timer.isRunning()) {
			timer.stop();
		}
		if (runState == RunState.RUNNING) {
			runState = RunState.PAUSED;
		}
	}

	/**
	 * Resumes the animation if it is paused/armed, automatically called by
	 * {@link #paintIcon}.
	 */
	public void resume() {
		if (runState != RunState.STOPPED) {
			if (!timer.isRunning() && suspendLevel == 0) {
				timer.restart();
			}
			runState = RunState.RUNNING;
		}
	}

	/**
	 * Stops and resets the animation. The first frame will be displayed.
	 */
	public void reset() {
		runState = RunState.STOPPED;
		if (timer.isRunning()) {
			timer.stop();
		}
		setCurrentFrameIndex(0, true);

	}

	/**
	 * @return {@code true} if repeat is enabled, {@code false} otherwise.
	 */
	public boolean isRepeat() {
		return repeat;
	}

	/**
	 * Sets whether the animation should continue with the first frame after the
	 * last frame has been shown.
	 *
	 * @param repeat {@code true} to repeat the animation frames, {@code false}
	 *            to stop after the last frame.
	 */
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}

	/**
	 * @return The current {@link RunState} of this {@link AnimatedIcon}.
	 */
	@Nonnull
	public RunState getRunState() {
		return runState;
	}

	/**
	 * Suspends the animation. For normal animation control, use
	 * {@link #pause()} instead. This will increase the suspend level by one and
	 * stop the timer that updates the animation if it's running. It will not
	 * change the {@link #runState} so that code controlling the animation run
	 * state can proceed as normal.
	 *
	 * @return The new suspend level.
	 *
	 * @see #suspendLevel
	 */
	public int suspend() {
		if (timer.isRunning()) {
			timer.stop();
		}
		return ++suspendLevel;
	}

	/**
	 * Unsuspends the animation. For normal animation control, use
	 * {@link #resume()} instead. This will decrease the suspend level by one
	 * restart the timer that updates the animation if the suspend level reached
	 * zero and {@link #runState} is {@link RunState#RUNNING}. It will not
	 * change the {@link #runState} so that code controlling the animation run
	 * state can proceed as normal.
	 *
	 * @return The new suspend level.
	 *
	 * @see #suspendLevel
	 */
	public int unsuspend() {
		if (suspendLevel > 0) {
			suspendLevel--;
		}
		if (suspendLevel == 0 && runState == RunState.RUNNING && !timer.isRunning()) {
			timer.restart();
		}
		return suspendLevel;
	}

	/**
	 * @return {@code true} if the suspend level is greater than zero,
	 *         {@code false} otherwise.
	 *
	 * @see #suspendLevel
	 */
	public boolean isSuspended() {
		return suspendLevel > 0;
	}

	/**
	 * @return the suspend level.
	 *
	 * @see #suspendLevel
	 */
	public int getSuspendLevel() {
		return suspendLevel;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Returns a {@link String} representation of this {@link AnimatedIcon}.
	 *
	 * @param debug if {@code true} all {@link AnimatedIconFrame}s will be
	 *            listed.
	 * @return The {@link String} representation.
	 */
	public String toString(boolean debug) {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append(" [").append("Component=").append(component.getClass().getSimpleName());
		if (component.getName() != null) {
			sb.append(" ").append(component.getName());
		}
		sb.append(", Run State=").append(runState)
			.append(", Suspend Level=").append(suspendLevel)
			.append(", Repeat=").append(repeat ? "True" : "False")
			.append(", Max Width=").append(maxIconWidth)
			.append(", Max Height=").append(maxIconHeight)
			.append(", Frames: ").append(frames.size());
		if (frames.size() > 1) {
			sb.append(", Current Frame: ").append(currentFrameIndex);
		}
		if (debug) {
			for (int i = 0; i < frames.size(); i++) {
				sb.append("\n").append(i).append(": ").append(frames.get(i));
			}
		} else if (frames.size() > 1) {
			AnimatedIconFrame firstFrame = frames.get(0);
			String description;
			if (firstFrame.getIcon() instanceof ImageIcon) {
				description = ((ImageIcon) firstFrame.getIcon()).getDescription();
				if (isNotBlank(description)) {
					int lastSeparator = description.lastIndexOf("/");
					if (lastSeparator > 0) {
						description = description.substring(lastSeparator + 1);
					}
				} else {
					description = firstFrame.getIcon().toString();
				}
			} else {
				description = firstFrame.getIcon() == null ? null : firstFrame.getIcon().toString();
			}
			sb.append(", First Frame=").append(description);
		}
		sb.append("]");
		return sb.toString();
	}

	// Implement the Icon Interface

	/**
	 *  Gets the width of this icon.
	 *
	 *  @return the width of the icon in pixels.
	 */
	@Override
	public int getIconWidth() {
		return maxIconWidth;
	}

	/**
	 *  Gets the height of this icon.
	 *
	 *  @return the height of the icon in pixels.
	 */
	@Override
	public int getIconHeight() {
		return maxIconHeight;
	}

	/**
	 * Paints the icons of this compound icon at the specified location.
	 *
	 * @param c The component on which the icon is painted.
	 * @param g the graphics context.
	 * @param x the {@code X} coordinate of the icon's top-left corner.
	 * @param y the {@code Y} coordinate of the icon's top-left corner.
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {

		// If the previous icon was an AnimatedIcon, stop the animation
		if (c instanceof AnimatedComponent) {
			AnimatedComponent animatedComponent = (AnimatedComponent) c;
			if (animatedComponent instanceof AnimatedTreeCellRenderer) {
				// AnimatedIconCallBack / Sequences of animated icons isn't supported for
				// AnimatedTreeCellRenderer because the renderer/component is shared between
				// all nodes.
				resume();
			} else if (animatedComponent.getCurrentIcon() != this) {
				if (animatedComponent.getCurrentIcon() != null) {
					animatedComponent.getCurrentIcon().pause();
				}

				animatedComponent.setCurrentIcon(this);
				resume();
			}
		}

		//  Saving the x, y coordinates allows us to only repaint the icon and
		//  not the entire component for each animation

		if (c == component) {
			iconX = x;
			iconY = y;
		}

		//  Determine the proper alignment of the Icon, then paint it

		Icon icon = frames.get(currentFrameIndex).icon;
		int width = getIconWidth();
		int height = getIconHeight();

		int offsetX = getOffset(width, icon.getIconWidth(), alignmentX);
		int offsetY = getOffset(height, icon.getIconHeight(), alignmentY);

		icon.paintIcon(c, g, x + offsetX, y + offsetY);
	}

	/*
	 *  When the icon value is smaller than the maximum value of all icons the
	 *  icon needs to be aligned appropriately. Calculate the offset to be used
	 *  when painting the icon to achieve the proper alignment.
	 */
	private static int getOffset(int maxValue, int iconValue, float alignment) {
		float offset = (maxValue - iconValue) * alignment;
		return Math.round(offset);
	}

	// Implement the ActionListener interface

	/**
	 *  Controls the animation of the {@link Icon}s when the {@link Timer} fires.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Take the appropriate action for the next step in the animation
		// This runs in Swing's event dispatcher thread, so no thread safety
		// is needed.

		int nextFrameIndex = (currentFrameIndex + 1) % frames.size();
		if (nextFrameIndex == 0) {
			// End of sequence
			if (nextStage != null) {
				if (component instanceof AnimatedIconCallback) {
					((AnimatedIconCallback) component).setNextIcon(nextStage);
				}
				nextStage = permanentStage;
				pause();
				return;
			} else if (!repeat) {
				pause();
				return;
			}
		}
		setCurrentFrameIndex(nextFrameIndex, true);
	}

	/**
	 * This will build and return an array of {@link AnimatedIconFrame}s based
	 * on a first and last index and a {@link Formatter} formatted resource name
	 * string.
	 * <p>
	 * <b>Note:</b> Leading zeroes can be specified by using the form
	 * {@code %0nd} where {@code n} is the total number of digits. To format the
	 * number {@code 4} as {@code 004} define it as {@code %03d} in the resource
	 * name pattern.
	 *
	 * @param resourceNamePattern the resource named written as a
	 *            {@link Formatter}.
	 * @param firstIdx the first index number to use with the pattern.
	 * @param lastIdx the last index number to use with the pattern.
	 * @param returnToFirst specifies whether the animation should reverse back
	 *            to the first frame after reaching the last frame.
	 * @param durationFirst the duration in milliseconds for the first frame.
	 * @param durationLast the duration in milliseconds for the last frame.
	 * @param duration the duration in milliseconds for all frames but the first
	 *            and the last.
	 * @return The built array of {@link AnimatedIconFrame}s.
	 */
	public static AnimatedIconFrame[] buildAnimation(
			String resourceNamePattern,
			int firstIdx,
			int lastIdx,
			boolean returnToFirst,
			int durationFirst,
			int durationLast,
			int duration
	) {
		return buildAnimation(
			resourceNamePattern,
			firstIdx,
			lastIdx,
			1,
			returnToFirst,
			durationFirst,
			durationLast,
			duration
		);
	}

	/**
	 * This will build and return an array of {@link AnimatedIconFrame}s based
	 * on a first and last index and a {@link Formatter} formatted resource name
	 * string.
	 * <p>
	 * <b>Note:</b> Leading zeroes can be specified by using the form
	 * {@code %0nd} where {@code n} is the total number of digits. To format the
	 * number {@code 4} as {@code 004} define it as {@code %03d} in the resource
	 * name pattern.
	 *
	 * @param resourceNamePattern the resource named written as a
	 *            {@link Formatter}.
	 * @param firstIdx the first index number to use with the pattern.
	 * @param lastIdx the last index number to use with the pattern.
	 * @param step the unsigned value to increase or decrease the index for each
	 *            frame.
	 * @param returnToFirst specifies whether the animation should reverse back
	 *            to the first frame after reaching the last frame.
	 * @param durationFirst the duration in milliseconds for the first frame.
	 * @param durationLast the duration in milliseconds for the last frame.
	 * @param duration the duration in milliseconds for all frames but the first
	 *            and the last.
	 * @return The built array of {@link AnimatedIconFrame}s.
	 */
	public static AnimatedIconFrame[] buildAnimation(
			String resourceNamePattern,
			int firstIdx,
			int lastIdx,
			int step,
			boolean returnToFirst,
			int durationFirst,
			int durationLast,
			int duration
	) {
		step = Math.abs(step);
		int count = Math.abs(lastIdx - firstIdx) / step;
		AnimatedIconFrame[] result = new AnimatedIconFrame[returnToFirst ? 2 * count : count + 1];

		boolean increasing = firstIdx <= lastIdx;
		int lastUsedIdx = increasing ? firstIdx + count * step : firstIdx - count * step;
		int idx = firstIdx;
		for (int i = 0; i <= count; i++) {
			Icon icon = LooksFrame.readImageIcon(String.format(resourceNamePattern, idx));
			if (icon == null) {
				throw new IllegalArgumentException(String.format(
					"Resource \"%s\" not found, please check your pattern (%s) and indices (%d-%d)!",
					String.format(resourceNamePattern, idx),
					resourceNamePattern,
					firstIdx,
					lastIdx
				));
			}
			if (
				increasing && idx > firstIdx && idx < lastUsedIdx ||
				!increasing && idx < firstIdx && idx > lastUsedIdx
			) {
				AnimatedIconFrame frame = new AnimatedIconFrame(icon, duration);
				result[i] = frame;
				if (returnToFirst) {
					result[2 * count - i] = frame;
				}
			} else if (idx == firstIdx) {
				result[i] = new AnimatedIconFrame(icon, durationFirst);
			} else {
				result[i] = new AnimatedIconFrame(icon, durationLast);
			}

			if (increasing) {
				idx += step;
			} else {
				idx -= step;
			}
		}
		return result;
	}

	/**
	 * This will build and return an array of one {@link AnimatedIconFrame}
	 * containing one icon. This is simply a convenience method for converting
	 * an {@link Icon} into an {@link AnimatedIconFrame} with no animation.
	 *
	 * @param icon the {@link Icon} to use for the single frame "animation".
	 * @return The built array of one {@link AnimatedIconFrame}.
	 */
	public static AnimatedIconFrame[] buildAnimation(Icon icon) {
		if (icon == null) {
			throw new IllegalArgumentException("icon cannot be null!");
		}
		return new AnimatedIconFrame[]{new AnimatedIconFrame(icon, 0)};
	}

	/**
	 * This will build and return an array of one {@link AnimatedIconFrame}
	 * containing one icon loaded from the given resource name.
	 *
	 * @param resourceName the name of the resource to use.
	 * @return The built array of one {@link AnimatedIconFrame}.
	 */
	public static AnimatedIconFrame[] buildAnimation(String resourceName) {
		Icon icon = LooksFrame.readImageIcon(resourceName);
		if (icon == null) {
			throw new IllegalArgumentException(String.format("Resource \"%s\" not found!", resourceName));
		}
		return buildAnimation(icon);
	}

	/**
	 * Defines one frame in an {@link AnimatedIcon} animation.
	 *
	 */
	public static class AnimatedIconFrame {

		/**
		 * The {@link Icon} to display for this frame
		 */
		protected final Icon icon;

		/**
		 * The number of milliseconds this frame will be displayed for constant
		 * duration frames or the maximum number of milliseconds this frame
		 * will be displayed for random duration frames.
		 */
		protected final int durationMS;

		/**
		 * The minimum number of milliseconds this frame will displayed when
		 * generating a random value.
		 */
		protected final int minDurationMS;

		/**
		 * Indicates if the frame is a random duration frame.
		 */
		protected final boolean random;

		/**
		 * Creates an {@link AnimatedIconFrame} frame with fixed duration.
		 *
		 * @param icon the {@link Icon} to display for this frame.
		 * @param lengthMS the duration of this frame in milliseconds.
		 */
		public AnimatedIconFrame(Icon icon, int lengthMS) {
			this.icon = icon;
			this.durationMS = lengthMS;
			this.random = false;
			this.minDurationMS = 0;
		}

		/**
		 * Creates an {@link AnimatedIconFrame} frame with random duration.
		 *
		 * @param icon the {@link Icon} to display for this frame
		 * @param minLengthMS the minimum duration of this frame in milliseconds
		 * @param maxLengthMS the maximum duration of this frame in milliseconds
		 */
		public AnimatedIconFrame(Icon icon, int minLengthMS, int maxLengthMS) {
			this.icon = icon;
			this.minDurationMS = minLengthMS;
			this.durationMS = maxLengthMS;
			this.random = true;
		}

		/**
		 * @return The {@link Icon} for this frame.
		 */
		public Icon getIcon() {
			return icon;
		}

		/**
		 * @return The duration of this frame in milliseconds.
		 */
		public int getLengthMS() {
			return durationMS;
		}

		/**
		 * @return The minimum duration of this frame in milliseconds. Only
		 *         relevant if this frame is a random length frame.
		 */
		public int getMinDurationMS() {
			return minDurationMS;
		}

		/**
		 * @return The maximum duration of this frame in milliseconds. Only
		 *         relevant if this frame is a random length frame.
		 */
		public int getMaxDurationMS() {
			return durationMS;
		}

		/**
		 * @return {@code true} if this is a random duration frame,
		 *         {@code false} otherwise.
		 */
		public boolean isRandom() {
			return random;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(getClass().getSimpleName());
			sb.append("[Type=");
			if (random) {
				sb.append("Random, Min Duration=").append(minDurationMS).append("ms");
				sb.append(", Max Duration=").append(durationMS).append("ms");
			} else {
				sb.append("Fixed, Duration=").append(durationMS).append("ms");
			}
			if (icon == null) {
				sb.append(", No Icon]");
			} else {
				sb.append(", Icon=\"").append(icon).append("\"]");
			}
			return sb.toString();
		}
	}

	/**
	 * Defines icon type used in callback
	 */
	public enum AnimatedIconType {

		/** The default icon */
		DEFAULTICON,

		/** The pressed icon */
		PRESSEDICON,

		/** The disabled icon */
		DISABLEDICON,

		/** The selected icon */
		SELECTEDICON,

		/** The disabled and selected icon */
		DISABLEDSELECTEDICON,

		/** The mouse-over icon */
		ROLLOVERICON,

		/** The mouse-over while selected icon */
		ROLLOVERSELECTEDICON
	}

	/**
	 * This class represents an {@link AnimatedIcon} stage used for callback.
	 *
	 * @author Nadahar
	 */
	public static class AnimatedIconStage {

		/** The icon type for this stage */
		public final AnimatedIconType iconType;

		/** The icon for this stage */
		public final AnimatedIcon icon;

		/** Whether this is a permanent stage or a one-time event */
		public final boolean permanent;

		/**
		 * Creates a new {@link AnimatedIcon} stage.
		 *
		 * @param iconType determines which {@link AnimatedIcon} this stage will
		 *            replace.
		 * @param icon the {@link AnimatedIcon} for this stage.
		 * @param permanent specifies whether this is a permanent stage or a
		 *            one-time event
		 */
		public AnimatedIconStage(AnimatedIconType iconType, AnimatedIcon icon, boolean permanent) {
			this.iconType = iconType;
			this.icon = icon;
			this.permanent = permanent;
		}

		@Override
		public String toString() {
			return
				getClass().getSimpleName() +
				" [iconType=" + iconType + ", permanent=" + permanent + ", icon=" + icon + "]";
		}
	}

	/**
	 * This is a marker interface for implementations that registers
	 * {@link AnimatedIcon}s with an {@link AnimatedIconListener}
	 * implementation.
	 *
	 * @author Nadahar
	 */
	public interface AnimatedIconListenerRegistrar {
	}

	/**
	 * Defines animation run states.
	 */
	public enum RunState {

		/** The animation is stopped */
		STOPPED,

		/** The animation is paused/armed */
		PAUSED,

		/** The animation is running */
		RUNNING;
	}
}
