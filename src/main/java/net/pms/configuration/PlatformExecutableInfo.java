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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * This class holds information about the different types stored for a given
 * executable. It is thread-safe. Callers can lock it for performing multiple
 * operations in an atomic manner using {@link #getLock()}.
 */
public class PlatformExecutableInfo {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * The default executable type for this executable
	 */
	private ProgramExecutableType defaultType;

	/**
	 * The {@link HashMap} containing the paths to the different
	 * {@link ProgramExecutableType}s for this executable.
	 */
	private final Map<ProgramExecutableType, String> executableMap;

	public PlatformExecutableInfo(ProgramExecutableType defaultType) {
		this.defaultType = defaultType;
		this.executableMap = new HashMap<>();
	}

	public PlatformExecutableInfo(ProgramExecutableType defaultType, Map<ProgramExecutableType, String> executableMap) {
		this.defaultType = defaultType;
		this.executableMap = new HashMap<>(executableMap);
	}

	public ReentrantReadWriteLock getLock() {
		return lock;
	}

	public ProgramExecutableType getDefault() {
		lock.readLock().lock();
		try {
			return defaultType;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void setDefault(ProgramExecutableType defaultType) {
		lock.writeLock().lock();
		try {
			this.defaultType = defaultType;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String getDefaultPath() {
		lock.readLock().lock();
		try {
			return executableMap.get(defaultType);
		} finally {
			lock.readLock().unlock();
		}
	}

	public String getPath(ProgramExecutableType executableType) {
		lock.readLock().lock();
		try {
			return executableMap.get(executableType);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void putPath(ProgramExecutableType executableType, String path) {
		lock.writeLock().lock();
		try {
			executableMap.put(executableType, path);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void remove(ProgramExecutableType executableType) {
		lock.writeLock().lock();
		try {
			executableMap.remove(executableType);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public int size() {
		lock.readLock().lock();
		try {
			return executableMap.size();
		} finally {
			lock.readLock().unlock();
		}
	}

	public boolean isEmpty() {
		lock.readLock().lock();
		try {
			return executableMap.isEmpty();
		} finally {
			lock.readLock().unlock();
		}
	}

	public boolean containsType(ProgramExecutableType executableType) {
		lock.readLock().lock();
		try {
			return executableMap.containsKey(executableType);
		} finally {
			lock.readLock().unlock();
		}
	}

	public boolean containsPath(String path) {
		lock.readLock().lock();
		try {
			return executableMap.containsValue(path);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void clear() {
		lock.writeLock().lock();
		try {
			executableMap.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

	public Set<ProgramExecutableType> executablesTypes() {
		lock.readLock().lock();
		try {
			return new HashSet<>(executableMap.keySet());
		} finally {
			lock.readLock().unlock();
		}
	}

	public Collection<String> paths() {
		lock.readLock().lock();
		try {
			return new ArrayList<>(executableMap.values());
		} finally {
			lock.readLock().unlock();
		}
	}

	public int hashCode() {
		lock.readLock().lock();
		try {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((defaultType == null) ? 0 : defaultType.hashCode());
			result = prime * result + ((executableMap == null) ? 0 : executableMap.hashCode());
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
		if (!(obj instanceof PlatformExecutableInfo)) {
			return false;
		}
		PlatformExecutableInfo other = (PlatformExecutableInfo) obj;
		lock.readLock().lock();
		try {
			ReentrantReadWriteLock otherLock = other.getLock();
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
				return true;
			} finally {
				otherLock.readLock().unlock();
			}
		} finally {
			lock.readLock().unlock();
		}
	}
}
