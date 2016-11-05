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
package net.pms.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple blocking {@link Process} wrapper that runs the process in the same
 * thread, consumes all output and return the results in a
 * {@link SimpleProcessWrapperResult}.
 *
 * @author Nadahar
 */
public class SimpleProcessWrapper { //TODO: (Nad) Make sure this is safe

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleProcessWrapper.class);

	/**
	 * Not to be instantiated.
	 */
	private SimpleProcessWrapper() {
	}

	/**
	 * Runs a process with the given command array.
	 *
	 * @param command an array of {@link String} used to build the command line.
	 * @return The result from running the process.
	 * @throws IOException If a problem occurs during the operation.
	 * @throws InterruptedException If the operation is interrupted.
	 */
	public static SimpleProcessWrapperResult runProcess(String... command) throws IOException, InterruptedException {
		return runProcess(Arrays.asList(command));
	}

	/**
	 * Runs a process with the given command {@link List}.
	 *
	 * @param command an array of {@link String} used to build the command line.
	 * @return The result from running the process.
	 * @throws IOException If a problem occurs during the operation.
	 * @throws InterruptedException If the operation is interrupted.
	 */
	public static SimpleProcessWrapperResult runProcess(List<String> command) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
		List<String> output = new ArrayList<>();
		if (LOGGER.isTraceEnabled()) {
			//XXX: Replace with String.join() in Java 8
			LOGGER.trace("Executing \"{}\"", StringUtils.join(command, " "));
		}
		Process process = processBuilder.start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.add(line);
			}
		}
		int exitCode = process.waitFor();
		if (LOGGER.isTraceEnabled()) {
			for (String line : output) {
				LOGGER.trace("Process output: {}", line);
			}
		}
		return new SimpleProcessWrapperResult(output, exitCode);
	}

	/**
	 * A simple container for the process results.
	 */
	public static class SimpleProcessWrapperResult {
		/** The process output */
		protected final List<String> output;
		/** The exit code */
		protected final int exitCode;

		/**
		 * Creates a new {@link SimpleProcessWrapperResult} instance with the
		 * given data.
		 *
		 * @param output the process output.
		 * @param exitCode the process exit code.
		 */
		public SimpleProcessWrapperResult(List<String> output, int exitCode) {
			this.output = output;
			this.exitCode = exitCode;
		}

		/**
		 * @return The output as a {@link List} of {@link String}s.
		 */
		public List<String> getOutput() {
			return output;
		}

		/**
		 * @return the exit code.
		 */
		public int getExitCode() {
			return exitCode;
		}
	}
}
