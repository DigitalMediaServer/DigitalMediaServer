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
package net.pms.configuration;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;


/**
 * This class holds information about the different executable types stored for
 * a given external program. Callers can lock an instance when performing
 * multiple operations in an atomic manner using {@link #getLock()}.
 */
@ThreadSafe
public class ExternalProgramInfo {

	/** The lock protecting all mutable class fields */
	protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/** The default executable type for this external program. */
	@GuardedBy("lock")
	protected ProgramExecutableType defaultType;

	/**
	 * A {@link HashMap} containing the paths to the different
	 * {@link ProgramExecutableType}s for this executable.
	 */
	@GuardedBy("lock")
	protected final Map<ProgramExecutableType, String> executableMap;

	/** The human readable name of this external program. */
	protected final String programName;

	/**
	 * Creates a new instance with the given arguments.
	 *
	 * @param programName the human readable name for the program to which the
	 *            new {@link ExternalProgramInfo} applies, this is not the
	 *            filename of a particular executable, but the general name of
	 *            the program.
	 * @param defaultType the default {@link ProgramExecutableType} for this
	 *            external program.
	 */
	public ExternalProgramInfo(String programName, ProgramExecutableType defaultType) {
		this.programName = programName;
		this.defaultType = defaultType;
		this.executableMap = new HashMap<>();
	}

	/**
	 * Creates a new instance with the given arguments.
	 *
	 * @param programName the human readable name for the program to which the
	 *            new {@link ExternalProgramInfo} applies, this is not the
	 *            filename of a particular executable, but the general name of
	 *            the program.
	 * @param defaultType the default {@link ProgramExecutableType} for this
	 *            external program.
	 * @param executableMap a {@link Map} of {@link ProgramExecutableType}s with
	 *            their corresponding paths.
	 */
	public ExternalProgramInfo(
		String programName,
		ProgramExecutableType defaultType,
		Map<ProgramExecutableType, String> executableMap
	) {
		this.programName = programName;
		this.defaultType = defaultType;
		this.executableMap = new HashMap<>(executableMap);
	}

	/**
	 * @return The lock protecting all mutable class fields of this instance,
	 *         use it for locking before iteration or other situations where no
	 *         concurrent should be allowed between operations.
	 */
	public ReentrantReadWriteLock getLock() {
		return lock;
	}

	/**
	 * @return The human readable name of this external program.
	 */
	public String getName() {
		return programName;
	}

	/**
	 * @return The default {@link ProgramExecutableType} for this instance.
	 */
	public ProgramExecutableType getDefault() {
		lock.readLock().lock();
		try {
			return defaultType;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Sets the default {@link ProgramExecutableType} for this instance.
	 *
	 * @param defaultType The default value.
	 */
	public void setDefault(ProgramExecutableType defaultType) {
		lock.writeLock().lock();
		try {
			this.defaultType = defaultType;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * @return The path for the default {@link ProgramExecutableType} for this
	 *         instance.
	 */
	@Nullable
	public String getDefaultPath() {
		lock.readLock().lock();
		try {
			return executableMap.get(defaultType);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets the path for a given {@link ProgramExecutableType} for this instance.
	 *
	 * @param executableType the {@link ProgramExecutableType} to get.
	 * @return The executable path.
	 */
	@Nullable
	public String getPath(ProgramExecutableType executableType) {
		lock.readLock().lock();
		try {
			return executableMap.get(executableType);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Sets the path for a given {@link ProgramExecutableType} for this
	 * instance.
	 *
	 * @param executableType the {@link ProgramExecutableType} whose path to
	 *            set.
	 * @param path the executable path to set.
	 */
	public void putPath(ProgramExecutableType executableType, String path) {
		lock.writeLock().lock();
		try {
			executableMap.put(executableType, path);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes the given {@link ProgramExecutableType} executable path for this
	 * instance.
	 *
	 * @param executableType the {@link ProgramExecutableType} to remove.
	 */
	public void remove(ProgramExecutableType executableType) {
		lock.writeLock().lock();
		try {
			executableMap.remove(executableType);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * @return The number of executable paths registered for this instance.
	 */
	public int size() {
		lock.readLock().lock();
		try {
			return executableMap.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @return {@code true} if no executable paths are registered for this
	 *         instance, {@code false} otherwise.
	 */
	public boolean isEmpty() {
		lock.readLock().lock();
		try {
			return executableMap.isEmpty();
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks whether an executable path for the given
	 * {@link ProgramExecutableType} is registered for this instance.
	 *
	 * @param executableType the {@link ProgramExecutableType} to check.
	 * @return {@code true} if a path is registered for {@code executableType},
	 *         false otherwise.
	 */
	public boolean containsType(ProgramExecutableType executableType) {
		lock.readLock().lock();
		try {
			return executableMap.containsKey(executableType) && isNotBlank(executableMap.get(executableType));
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Checks whether the given path is registered for this instance.
	 *
	 * @param path the path to look for.
	 * @return {@code true} if {@code path} is registered, {@code false} otherwise.
	 */
	public boolean containsPath(String path) {
		lock.readLock().lock();
		try {
			return executableMap.containsValue(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Removes all registered executable paths for this instance.
	 */
	public void clear() {
		lock.writeLock().lock();
		try {
			executableMap.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * @return A {@link Set} of all registered {@link ProgramExecutableType}s
	 *         for this instance.
	 */
	public Set<ProgramExecutableType> executablesTypes() {
		lock.readLock().lock();
		try {
			return new HashSet<>(executableMap.keySet());
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * @return A {@link Collection} of all the registered paths for this
	 *         instance.
	 */
	public Collection<String> paths() {
		lock.readLock().lock();
		try {
			return new ArrayList<>(executableMap.values());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		lock.readLock().lock();
		try {
			sb.append(programName);
			boolean first = true;
			sb.append(": [");
			for (Entry<ProgramExecutableType, String> entry : executableMap.entrySet()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				if (entry.getKey() == defaultType) {
					sb.append("(*)");
				}
				sb.append(entry.getKey()).append(" = \"").append(entry.getValue()).append("\"");
			}
			sb.append("]");
		} finally {
			lock.readLock().unlock();
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		lock.readLock().lock();
		try {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((defaultType == null) ? 0 : defaultType.hashCode());
			result = prime * result + ((executableMap == null) ? 0 : executableMap.hashCode());
			result = prime * result + ((programName == null) ? 0 : programName.hashCode());
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ExternalProgramInfo)) {
			return false;
		}
		ExternalProgramInfo other = (ExternalProgramInfo) obj;
		ReentrantReadWriteLock otherLock = other.getLock();
		lock.readLock().lock();
		try {
			otherLock.readLock().lock();
			try {
				if (defaultType == null) {
					if (other.defaultType != null) {
						return false;
					}
				} else if (!defaultType.equals(other.defaultType)) {
					return false;
				}
				if (executableMap == null) {
					if (other.executableMap != null) {
						return false;
					}
				} else if (!executableMap.equals(other.executableMap)) {
					return false;
				}
				if (programName == null) {
					if (other.programName != null) {
						return false;
					}
				} else if (!programName.equals(other.programName)) {
					return false;
				}
				return true;
			} finally {
				otherLock.readLock().unlock();
			}
		} finally {
			lock.readLock().unlock();
		}
	}
}
