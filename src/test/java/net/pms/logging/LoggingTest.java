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
package net.pms.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.filter.Filter;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.util.FileUtil;
import org.apache.commons.configuration.ConfigurationException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * Test net.pms.logging package
 */
public class LoggingTest {

	private static class TestAppender<E> extends AppenderBase<E> {

		private final Object lastEventLock = new Object();
		private E lastEvent = null;

		public E getLastEvent() {
			synchronized (lastEventLock) {
				return lastEvent;
			}
		}
		@Override
		protected void append(E eventObject) {
			synchronized (lastEventLock) {
				lastEvent = eventObject;
			}
		}
	}

	private static class TestFileAppender<E> extends FileAppender<E> {

		@Override
		protected void append(E eventObject) {
		}
	}

	@Before
	public void setUp() {
		// Silence all log messages from the DMS code that is being tested
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.OFF);
	}

	private static boolean findAppender(Iterator<Appender<ILoggingEvent>> iterator, Appender<ILoggingEvent> appender) {
		boolean found = false;
		while (iterator.hasNext()) {
			Appender<ILoggingEvent> a = iterator.next();
			if (a == appender) {
				found = true;
			}
		}
		return found;
	}

	private static boolean syslogAppenderFound(Iterator<Appender<ILoggingEvent>> iterator) {
		while (iterator.hasNext()) {
			Appender<ILoggingEvent> appender = iterator.next();
			if (appender instanceof SyslogAppender) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Test CacheAppender and it's utility class CacheLogger
	 */
	@Test
	public void testCacheLogger() {
		final String testMessage = "Test logging event";

		// Set up logging framework for testing
		assertTrue("LogBack", LoggerFactory.getILoggerFactory() instanceof LoggerContext);
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();
		Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
		TestAppender<ILoggingEvent> testAppender = new TestAppender<>();
		rootLogger.addAppender(testAppender);
		testAppender.setContext(context);
		testAppender.start();
		rootLogger.setLevel(Level.ERROR);

		// Test basic functionality
		assertFalse("CacheLoggerInactive", CacheLogger.isActive());
		CacheLogger.startCaching();
		rootLogger.error(testMessage);
		assertTrue("CacheLoggerActive", CacheLogger.isActive());
		CacheLogger.stopAndFlush();
		assertEquals("LoggedMessage", testAppender.getLastEvent().getMessage(), testMessage);
		assertFalse("CacheLoggerInactive", CacheLogger.isActive());
		rootLogger.setLevel(Level.OFF);

		// Test other CacheLogger functions
		CacheLogger.startCaching();
		assertTrue("AppenderIterator", findAppender(CacheLogger.iteratorForAppenders(), testAppender));
		CacheLogger.removeAppender(testAppender);
		assertFalse("AppenderRemoval", findAppender(CacheLogger.iteratorForAppenders(), testAppender));
		TestAppender<ILoggingEvent> testAppender2 = new TestAppender<>();
		CacheLogger.addAppender(testAppender2);
		assertTrue("AppenderAdding", findAppender(CacheLogger.iteratorForAppenders(), testAppender2));
		CacheLogger.stopAndFlush();
		assertTrue("AppenderTransferred", findAppender(rootLogger.iteratorForAppenders(), testAppender2));
		assertFalse("RemovedAppenderNotTransferred", findAppender(rootLogger.iteratorForAppenders(), testAppender));

		// Cleanup
		rootLogger.detachAppender(testAppender2);
	}

	/**
	 * Test
	 * @throws InterruptedException
	 *
	 */
	@Test
	public void testDebugLogPropertyDefiner() throws ConfigurationException, InterruptedException {

		// Set up DMS configuration
		PMS.setTestConfiguration();
		PMS.get();
		DebugLogPropertyDefiner propertyDefiner = new DebugLogPropertyDefiner();

		// Test logFilePath
		propertyDefiner.setKey("logFilePath");
		File file = new File(propertyDefiner.getPropertyValue());
		assertTrue("logFilePathIsDirectory", file.isDirectory());
		assertFalse("logFilePathIsNotFile", file.isFile());

		// Test rootLevel
		propertyDefiner.setKey("rootLevel");
		assertNotNull("ValidLevel", Level.toLevel(propertyDefiner.getPropertyValue(), null));

		// Test logFileName
		propertyDefiner.setKey("logFileName");
		assertTrue("ValidLogFileName", FileUtil.isValidFileName(propertyDefiner.getPropertyValue()));
	}

	@Test
	public void testLoggingConfig() throws ConfigurationException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InterruptedException {

		// Set up a test (default) configuration
		PMS.setTestConfiguration();
		PmsConfiguration configuration = PMS.getConfiguration();
		PMS.get();

		// Load logback configuration
		LoggingConfig.loadFile();
		// Silence logger
		LoggingConfig.setRootLevel(LogLevel.OFF);

		// Get access to logger
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);

		/*
		 * During DMS build a valid configuration should be accessible at least under "external resources"
		 * and thus testing for a valid configuration is considered OK to be able to do the other tests.
		 * Null is returned if a valid configuration can't be found.
		 */

		// Test for a valid configuration
		File file = LoggingConfig.getConfigFilePath().toFile();
		assertTrue("LoggingConfigIsFile", file.isFile());
		assertFalse("LoggingConfigIsFile", file.isDirectory());

		// Test getLogFilePaths() and LoggingConfigFileLoader.getLogFilePaths()
		Map<String, String> logFilePaths = LoggingConfig.getLogFilePaths();
		Map<String, String> compLogFilePaths = LoggingConfig.getLogFilePaths();
		Iterator<Appender<ILoggingEvent>> iterator = rootLogger.iteratorForAppenders();
		while (iterator.hasNext()) {
			Appender<ILoggingEvent> appender = iterator.next();
			if (appender instanceof FileAppender) {
				FileAppender<ILoggingEvent> fa = (FileAppender<ILoggingEvent>) appender;
				assertTrue("LogFilePathsContainsKey", logFilePaths.containsKey(fa.getName()));
				assertEquals("LogFilePathsHasPath", logFilePaths.get(fa.getName()), fa.getFile());
				assertTrue("CompatibleLogFilePathsContainsKey", compLogFilePaths.containsKey(fa.getName()));
				assertEquals("CompatibleLogFilePathsHasPath", compLogFilePaths.get(fa.getName()), fa.getFile());
			}
		}

		// Reset LogBack configuration and create a fake one to not rely on the existing configuration file
		context.reset();

		TestFileAppender<ILoggingEvent> testDefaultAppender = new TestFileAppender<>();
		testDefaultAppender.setName("default.log");
		testDefaultAppender.setContext(context);
		PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
		layoutEncoder.setPattern("%-5level %d{HH:mm:ss.SSS} [%thread] %msg%n");
		layoutEncoder.setContext(context);
		testDefaultAppender.setEncoder(layoutEncoder);
		rootLogger.addAppender(testDefaultAppender);

		TestFileAppender<ILoggingEvent> testGenericAppender = new TestFileAppender<>();
		testGenericAppender.setName("SomeOtherFileAppender");
		testGenericAppender.setContext(context);
		layoutEncoder = new PatternLayoutEncoder();
		layoutEncoder.setPattern("%-5level %d %msg%n");
		layoutEncoder.setContext(context);
		testGenericAppender.setEncoder(layoutEncoder);
		rootLogger.addAppender(testGenericAppender);

		TestAppender<ILoggingEvent> testNonFileAppender = new TestAppender<>();
		testNonFileAppender.setName("SomeNonFileAppender");
		testNonFileAppender.setContext(context);
		rootLogger.addAppender(testNonFileAppender);

		// Test setBuffered()
		LoggingConfig.setBuffered(true);
		iterator = rootLogger.iteratorForAppenders();
		while (iterator.hasNext()) {
			Appender<ILoggingEvent> appender = iterator.next();
			if (appender instanceof OutputStreamAppender && !(appender instanceof ConsoleAppender<?>)) {
				// Appender has ImmediateFlush property
				assertFalse("LogFileIsBuffered", ((OutputStreamAppender<ILoggingEvent>) appender).isImmediateFlush());
			}
		}
		LoggingConfig.setBuffered(false);
		iterator = rootLogger.iteratorForAppenders();
		while (iterator.hasNext()) {
			Appender<ILoggingEvent> appender = iterator.next();
			if (appender instanceof OutputStreamAppender && !(appender instanceof ConsoleAppender<?>)) {
				assertTrue("LogFileIsNotBuffered", ((OutputStreamAppender<ILoggingEvent>) appender).isImmediateFlush());
				// Appender has ImmediateFlush property
			}
		}

		// Test getRootLevel()
		assertEquals("GetRootLevel", LoggingConfig.getRootLevel(), rootLogger.getLevel());

		// Test setRootLevel()
		LoggingConfig.setRootLevel(LogLevel.ALL);
		assertEquals("SetRootLevel", LoggingConfig.getRootLevel(), Level.ALL);
		LoggingConfig.setRootLevel(LogLevel.INFO);
		assertEquals("SetRootLevel", LoggingConfig.getRootLevel(), Level.INFO);
		LoggingConfig.setRootLevel(LogLevel.OFF);

		// Test setConsoleFilter()
		configuration.setLoggingFilterConsole(LogLevel.WARN);
		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setContext(context);
		PatternLayoutEncoder patternEncoder = new PatternLayoutEncoder();
		patternEncoder.setPattern("%msg%n");
		patternEncoder.setContext(context);
		patternEncoder.start();
		consoleAppender.setEncoder(patternEncoder);
		consoleAppender.start();
		rootLogger.addAppender(consoleAppender);
		LoggingConfig.setConsoleFilter();
		List<Filter<ILoggingEvent>> filterList = consoleAppender.getCopyOfAttachedFiltersList();
		assertEquals("NumberOfConsoleFilters", filterList.size(), 1);
		assertTrue("ConsoleFilterIsThresholdFilter", filterList.get(0) instanceof ThresholdFilter);
		ThresholdFilter thresholdFilter = (ThresholdFilter) filterList.get(0);
		Field field = thresholdFilter.getClass().getDeclaredField("level");
		field.setAccessible(true);
		assertEquals("ConsoleFilterLevel", field.get(thresholdFilter), Level.WARN);
		configuration.setLoggingFilterConsole(LogLevel.TRACE);
		LoggingConfig.setConsoleFilter();
		filterList = consoleAppender.getCopyOfAttachedFiltersList();
		assertEquals("NumberOfConsoleFilters", filterList.size(), 1);
		assertTrue("ConsoleFilterIsThresholdFilter", filterList.get(0) instanceof ThresholdFilter);
		thresholdFilter = (ThresholdFilter) filterList.get(0);
		field = thresholdFilter.getClass().getDeclaredField("level");
		field.setAccessible(true);
		assertEquals("ConsoleFilterLevel", field.get(thresholdFilter), Level.TRACE);
		rootLogger.detachAppender(consoleAppender);

		// Test setTracesFilter()
		configuration.setLoggingFilterLogsTab(LogLevel.WARN);
		FrameAppender<ILoggingEvent> frameAppender = new FrameAppender<>();
		frameAppender.setContext(context);
		patternEncoder = new PatternLayoutEncoder();
		patternEncoder.setPattern("%msg%n");
		patternEncoder.setContext(context);
		patternEncoder.start();
		frameAppender.setEncoder(patternEncoder);
		frameAppender.start();
		rootLogger.addAppender(frameAppender);
		LoggingConfig.setTracesFilter();
		filterList = frameAppender.getCopyOfAttachedFiltersList();
		assertEquals("NumberOfTracesFilters", filterList.size(), 1);
		assertTrue("TracesFilterIsThresholdFilter", filterList.get(0) instanceof ThresholdFilter);
		thresholdFilter = (ThresholdFilter) filterList.get(0);
		field = thresholdFilter.getClass().getDeclaredField("level");
		field.setAccessible(true);
		assertEquals("TracesFilterLevel", field.get(thresholdFilter), Level.WARN);
		configuration.setLoggingFilterLogsTab(LogLevel.TRACE);
		LoggingConfig.setTracesFilter();
		filterList = frameAppender.getCopyOfAttachedFiltersList();
		assertEquals("NumberOfTracesFilters", filterList.size(), 1);
		assertTrue("TracesFilterIsThresholdFilter", filterList.get(0) instanceof ThresholdFilter);
		thresholdFilter = (ThresholdFilter) filterList.get(0);
		field = thresholdFilter.getClass().getDeclaredField("level");
		field.setAccessible(true);
		assertEquals("TracesFilterLevel", field.get(thresholdFilter), Level.TRACE);
		rootLogger.detachAppender(frameAppender);

		// Test isSyslogDisabled()
		if (syslogAppenderFound(rootLogger.iteratorForAppenders())) {
			assertTrue("SyslogDisabledByConfiguration", LoggingConfig.isSyslogDisabled());
		} else {
			assertFalse("SyslogNotDisabledByConfiguration", LoggingConfig.isSyslogDisabled());
		}

		// Test setSyslog() if possible
		if (!syslogAppenderFound(rootLogger.iteratorForAppenders())) {
			configuration.setLoggingSyslogHost("localhost");
			configuration.setLoggingUseSyslog(true);
			LoggingConfig.setSyslog();
			assertTrue("SyslogEnabled", syslogAppenderFound(rootLogger.iteratorForAppenders()));
			configuration.setLoggingUseSyslog(false);
			LoggingConfig.setSyslog();
			assertFalse("SyslogDisabled", syslogAppenderFound(rootLogger.iteratorForAppenders()));
		}

		// Test forceVerboseFileEncoder() given that LogBack configuration
		// contains at least one file appender with PatternLayoutEncoder
		LoggingConfig.forceVerboseFileEncoder();
		iterator = rootLogger.iteratorForAppenders();
		while (iterator.hasNext()) {
			Appender<ILoggingEvent> appender = iterator.next();
			if (appender instanceof OutputStreamAppender && !(appender instanceof ConsoleAppender<?>)) {
				// Appender has Encoder property
				Encoder<ILoggingEvent> encoder = ((OutputStreamAppender<ILoggingEvent>) appender).getEncoder();
				if (encoder instanceof PatternLayoutEncoder) {
					// Encoder has pattern
					patternEncoder = (PatternLayoutEncoder) encoder;
					assertTrue("AppenderPatternHasCorrectTimestamp", patternEncoder.getPattern().matches(".*%(d|date)\\{yyyy-MM-dd HH:mm:ss.SSS\\}.*"));
					assertTrue("AppenderPatternHasLogger", patternEncoder.getPattern().matches(".*%logger.*"));
				}
			}
		}

		context.reset();
	}
}
