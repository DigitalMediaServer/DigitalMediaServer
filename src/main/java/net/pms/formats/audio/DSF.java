	/*
	 * Digital Media Server, for streaming digital media to DLNA compatible devices
	 * based on www.ps3mediaserver.org and www.universalmediaserver.com.
	 * Copyright (C) 2016 Digital Media Server developers.
	 *
	 * This program is a free software; you can redistribute it and/or
	 * modify it under the terms of the GNU General Public License
	 * as published by the Free Software Foundation; version 2
	 * of the License only.
	 *
	 * This program is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	 * GNU General Public License for more details.
	 *
	 * You should have received a copy of the GNU General Public License
	 * along with this program; if not, write to the Free Software
	 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
	 */
	package net.pms.formats.audio;

	public class DSF extends AudioBase {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Identifier getIdentifier() {
			return Identifier.DSF;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String[] getSupportedExtensions() {
			return new String[] { "dsf" };
		}
	}
