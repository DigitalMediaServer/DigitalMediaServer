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
package net.pms.newgui;

import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import net.pms.Messages;
import net.pms.encoders.Player;
import net.pms.util.FormLayoutUtil;
import net.pms.util.Version;


public class EngineSelection extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String COL_SPEC = "left:pref, 3dlu, pref, 3dlu, pref, 3dlu, pref:grow";
	private static final String ROW_SPEC = "4*(pref, 3dlu), pref, 9dlu, pref, 9dlu:grow, pref";

	protected final Player player;
	protected ComponentOrientation orientation;

	public EngineSelection(@Nonnull Player player, @Nonnull ComponentOrientation orientation) {
		if (player == null) {
			throw new IllegalArgumentException("player cannot be null");
		}
		if (orientation == null) {
			throw new IllegalArgumentException("orientation cannot be null");
		}
		this.player = player;
		this.orientation = orientation;
		initialize();
	}

	protected void initialize() {
		String colSpec = FormLayoutUtil.getColSpec(COL_SPEC, orientation);
		FormLayout layout = new FormLayout(colSpec, ROW_SPEC);

		CellConstraints cc = new CellConstraints();

		setLayout(layout);
		setBorder(new CompoundBorder(
			new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), Messages.getString("Engine.Selection")),
			new EmptyBorder(15, 15, 15, 15) //TODO (Nad) DLU?
		));
		add(new JLabel("Effective executable:"), cc.xy(1, 1));
		add(new JLabel(player.getCurrentExecutableType().toString()), cc.xy(3, 1));
		Version version = player.getProgramInfo().getExecutableInfo(player.getCurrentExecutableType()).getVersion();
		if (version != null) {
			add(new JLabel("Version:"), cc.xy(5, 1));
			add(new JLabel(version.getVersionString()), cc.xy(7, 1));
		}
	}

	@Nonnull
	public Player getPlayer() {
		return player;
	}

}
