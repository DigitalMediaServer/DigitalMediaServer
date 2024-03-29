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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.Iterator;
import java.util.LinkedList;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special LogBack cacher that administers {@link CacheAppender}.
 *
 * @author Nadahar
 */
public class CacheLogger {

	private static Logger LOGGER = LoggerFactory.getLogger(CacheLogger.class);
	private static LinkedList<Appender<ILoggingEvent>> appenderList = new LinkedList<>();
	private static volatile CacheAppender<ILoggingEvent> cacheAppender = null;
	private static LoggerContext loggerContext = null;
	private static ch.qos.logback.classic.Logger rootLogger;

	private static void detachRootAppenders() {
		Iterator<Appender<ILoggingEvent>> it = rootLogger.iteratorForAppenders();
		while (it.hasNext()) {
			Appender<ILoggingEvent> appender = it.next();
			if (appender != cacheAppender) {
				appenderList.add(appender);
				rootLogger.detachAppender(appender);
			}
		}
	}

	private static void attachRootAppenders() {
		while (!appenderList.isEmpty()) {
			Appender<ILoggingEvent> appender = appenderList.poll();
			rootLogger.addAppender(appender);
		}
	}

	private static void disposeOfAppenders() {
		appenderList.clear();
	}

	/**
	 * @return whether or not CacheLogger is currently running
	 */
	public static boolean isActive() {
		return cacheAppender != null;
	}

	/**
	 * Sets references to the LoggerContext. Must be called whenever logging
	 * configuration changes between {@link #startCaching()} and {@link #stopAndFlush()}
	 */
	public static synchronized void initContext() {
		ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
		if (!(iLoggerFactory instanceof LoggerContext)) {
			// Not using LogBack, CacheAppender not applicable
			LOGGER.debug("Not using LogBack, aborting CacheLogger");
			loggerContext = null;
			return;
		} else if (!isActive()) {
			LOGGER.error("initContext() cannot be called while isActive() is false");
			return;
		}

		loggerContext = (LoggerContext) iLoggerFactory;
		rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		disposeOfAppenders();
		detachRootAppenders();
		cacheAppender.setContext(loggerContext);
		cacheAppender.setName("CacheAppender");
		attachCacheAppender();
	}

	/**
	 * Attaches the {@link CacheAppender} to the current root {@link Logger} if
	 * the {@link CacheAppender} has been created, the root {@link Logger} has
	 * been identified and the {@link CacheAppender} isn't already attached.
	 */
	public static synchronized void attachCacheAppender() {
		if (rootLogger != null && cacheAppender != null && !rootLogger.isAttached(cacheAppender)) {
			rootLogger.addAppender(cacheAppender);
			cacheAppender.start();
		}
	}

	public static synchronized void startCaching() {
		if (isActive()) {
			LOGGER.debug("StartCaching() failed: Caching already started");
		} else {
			cacheAppender = new CacheAppender<>();
			initContext();
		}
	}

	public static synchronized void stopAndFlush() {
		if (loggerContext == null) {
			LOGGER.debug("Not using LogBack, aborting CacheLogger.stopAndFlush()");
			return;
		} else if (!isActive()) {
			LOGGER.error("stopAndFlush() cannot be called while isActive() is false");
			return;
		}

		cacheAppender.stop();
		rootLogger.detachAppender(cacheAppender);
		attachRootAppenders();
		cacheAppender.flush(rootLogger);
		cacheAppender = null;
	}

	public static synchronized Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
		return appenderList.iterator();
	}

	public static synchronized void addAppender(Appender<ILoggingEvent> newAppender) {
		appenderList.add(newAppender);
	}

	public static synchronized boolean removeAppender(Appender<ILoggingEvent> appender) {
		return appenderList.remove(appender);
	}
}
