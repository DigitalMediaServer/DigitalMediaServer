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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.apache.commons.configuration.ConfigurationException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class SpinnerIntModelTest {


	@Before
	public void setUp() throws ConfigurationException {
		// Silence all log messages from the DMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.OFF);
	}

	@Test
	public void testSpinnerIntModel() {
		SpinnerIntModel intModel = new SpinnerIntModel(65,50,250,100);

		assertEquals("InitValue",  65, intModel.getIntValue());
		assertEquals("LowerLimit", 50, intModel.getMinimum());
		assertEquals("UpperLimit", 250, intModel.getMaximum());
		assertEquals("StepSize", 100, intModel.getStepSize());
		assertEquals("NextValue", Integer.valueOf(165), intModel.getNextValue());
		assertEquals("PrevValue", Integer.valueOf(50), intModel.getPreviousValue());
		intModel.setIntValue(50);
		assertEquals("NextValue", Integer.valueOf(100), intModel.getNextValue());
		assertNull("PrevValue", intModel.getPreviousValue());
		assertEquals("CurrValue", Integer.valueOf(50), intModel.getValue());
		intModel.setValue(intModel.getNextValue());
		intModel.setValue(intModel.getNextValue());
		intModel.setValue(intModel.getNextValue());
		assertNull("NextValue", intModel.getNextValue());
		assertEquals("PrevValue", Integer.valueOf(200), intModel.getPreviousValue());
		assertEquals("CurrValue", Integer.valueOf(250), intModel.getValue());
	}
}
