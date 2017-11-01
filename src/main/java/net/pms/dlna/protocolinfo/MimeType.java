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
package net.pms.dlna.protocolinfo;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.pms.util.ParseException;


/**
 * This interface represents a MIME type. Implementations are required to
 * implement {@link #isCompatible(MimeType)}, {@link #equalValue(MimeType)} and
 * {@link #equalBaseValue(MimeType)} in such a way that all {@link MimeType}
 * instances from any implementation is compared by value.
 *
 * @author Nadahar
 */
public interface MimeType extends Serializable {

	/** The wildcard character */
	String ANY = "*";

	/** A MIME type that represent any type and any subtype */
	MimeTypeImpl ANYANY = new MimeTypeImpl();

	/**
	 * The static factory singleton instance used to create and retrieve cached
	 * {@link MimeType} instances.
	 */
	MimeTypeFactory FACTORY = new MimeTypeFactory();

	/**
	 * @return The {@code type} (first) part of this {@link MimeType}.
	 */
	public String getType();

	/**
	 * @return Whether the {@code type} (first) part of this {@link MimeType}
	 *         matches anything.
	 */
	public boolean isAnyType();

	/**
	 * @return The {@code subtype} (second) part of this {@link MimeType}.
	 */
	public String getSubtype();

	/**
	 * @return Whether the {@code subtype} (second) part of this
	 *         {@link MimeType} matches anything.
	 */
	public boolean isAnySubtype();

	/**
	 * @return A {@link Map} containing the {@code parameters} of this
	 *         {@link MimeType}. If no parameters exist, an empty {@link Map} is
	 *         returned.
	 */
	@Nonnull
	public Map<String, String> getParameters();

	/**
	 * Determines whether this and {@code other} is compatible. That means being
	 * either equal or having {@link #ANY} in {@code type} or {@code subtype} is
	 * such a way that they can describe the same content without taking
	 * parameters into account.
	 *
	 * @param other the {@link MimeType} to check compatibility against.
	 * @return {@code true} of this and {@code other} is compatible,
	 *         {@code false} otherwise.
	 */
	public boolean isCompatible(MimeType other);

	/**
	 * Creates a {@link org.seamless.util.MimeType} from this instance.
	 *
	 * @return The new {@link org.seamless.util.MimeType} instance.
	 */
	public org.seamless.util.MimeType toSeamlessMimeType();

	/**
	 * Determines if this {@link MimeType} is a Digital Rights Management
	 * MIME type.
	 *
	 * @return {@code true} if this {@link MimeType} represents a DRM MIME type,
	 *         {@code false} otherwise.
	 */
	public boolean isDRM();

	/**
	 * Determines if this {@link MimeType} is in the special DLNA-DRM format
	 * defined in the Digital Transmission Content Protection IP specification
	 * {@code DTCP-IP}.
	 *
	 * @return {@code true} if this {@link MimeType} represents a DTCP
	 *         MIME type, {@code false} otherwise.
	 */
	public boolean isDTCP();

	/**
	 * @return The properly formatted MIME type {@link String}.
	 */
	@Override
	@Nonnull
	public String toString();

	/**
	 * The same as {@link #toString()} but without any trailing parameters.
	 *
	 * @return The "basic MIME type" formatted {@link String}.
	 */
	@Nonnull
	public String toStringWithoutParameters();

	/**
	 * Compares the {@link MimeType} by value. This is needed instead of
	 * {@link #equals(Object)} to allow {@code enum}s to implement
	 * {@link MimeType} with consistent behavior.
	 *
	 * @param other the {@link MimeType} instance to compare with.
	 * @return {@code true} if the two are considered equal, {@code false}
	 *         otherwise.
	 */
	public boolean equalValue(@Nullable MimeType other);

	/**
	 * Compares the {@link MimeType} only by type and subtype. Parameters are
	 * ignored.
	 *
	 * @param other the {@link MimeType} instance to compare with.
	 * @return {@code true} if the two are considered equal, {@code false}
	 *         otherwise.
	 */
	public boolean equalBaseValue(@Nullable MimeType other);

	/**
	 * A factory for creating, caching and retrieving {@link MimeType}
	 * instances.
	 */
	public static class MimeTypeFactory {

		/** The default audio MIME type when the real MIME type is unknown */
		public final MimeType audioDefault = KnownMimeTypes.MPA;

		/** The default image MIME type when the real MIME type is unknown */
		public final MimeType imageDefault = KnownMimeTypes.JPEG;

		/** The default video MIME type when the real MIME type is unknown */
		public final MimeType videoDefault = KnownMimeTypes.MPEG;

		/** The static logger */
		private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeFactory.class);

		/** The instance cache */
		protected final HashSet<MimeType> instanceCache = new HashSet<>();

		/**
		 * Searches through the predefined "known" instances, if any. Returns
		 * the instance if a match is found, otherwise {@code null}.
		 *
		 * @param mimeValue the {@link MimeType} to search for.
		 * @return The found {@link MimeType} or {@code null}.
		 */
		@Nullable
		protected MimeType searchKnownInstances(@Nullable MimeType mimeValue) {
			if (mimeValue != null) {
				for (KnownMimeTypes mimeType : KnownMimeTypes.values()) {
					if (mimeValue.equalValue(mimeType)) {
						return mimeType;
					}
				}
			}
			return null;
		}

		/**
		 * Creates a copy of the specified {@link MimeType} base type using the
		 * specified parameters.
		 *
		 * @param mimeType the {@link MimeType} to copy type and subtype from.
		 * @param parameters a {@link Map} of additional parameters for this
		 *            MIME type.
		 * @return The new {@link MimeType} instance.
		 * @throws IllegalArgumentException If {@code mimeType} is {@code null}.
		 */
		@Nonnull
		protected MimeType createNewInstance(
			@Nonnull MimeType mimeType,
			@Nullable Map<String, String> parameters
		) {
			if (mimeType == null) {
				throw new IllegalArgumentException("mimeType cannot be null");
			}
			return new MimeTypeImpl(mimeType.getType(), mimeType.getSubtype(), parameters);
		}

		/**
		 * Creates a new {@link MimeType} instance using the specified values.
		 *
		 * @param type the first part of the MIME type.
		 * @param subtype the second part of the MIME type.
		 * @param parameters a {@link Map} of additional parameters for this
		 *            MIME type.
		 * @return The new {@link MimeType} instance.
		 */
		@Nonnull
		protected MimeType createNewInstance(
			@Nullable String type,
			@Nullable String subtype,
			@Nullable Map<String, String> parameters
		) {
			return new MimeTypeImpl(type, subtype, parameters);
		}

		/**
		 * Creates a new {@link MimeType} instance by parsing the specified {@link String}.
		 *
		 * @param value the {@link String} value to parse.
		 * @return The new {@link MimeType} instance.
		 * @throws ParseException If {@code value} isn't a valid MIME type.
		 * @throws IllegalArgumentException If {@code value} is blank.
		 */
		@Nonnull
		protected MimeType createNewInstance(@Nonnull String value) throws ParseException {
			if (isBlank(value)) {
				throw new IllegalArgumentException("value cannot be blank");
			}
			return MimeTypeImpl.valueOf(value);
		}

		/**
		 * Retrieves a {@link MimeType} instance from {@link KnownMimeTypes}
		 * or the cache matching the specified values. If no such
		 * {@link MimeType} instance exists, {@code null} is returned.
		 *
		 * @param type the first part of the MIME type.
		 * @param subtype the second part of the MIME type.
		 * @return The found {@link MimeType} or {@code null}.
		 */
		@Nullable
		public MimeType getMimeType(@Nullable String type, @Nullable String subtype) {
			MimeType mimeValue = createNewInstance(type, subtype, null);
			return getMimeType(mimeValue);
		}

		/**
		 * Retrieves a {@link MimeType} instance from {@link KnownMimeTypes}
		 * or the cache matching a {@link MimeType} created by coping of the
		 * specified {@link MimeType} base type using the specified parameters.
		 * If no such {@link MimeType} instance exists, {@code null} is
		 * returned.
		 *
		 * @param mimeType the {@link MimeType} to copy type and subtype from.
		 * @param parameters a {@link Map} of additional parameters for this
		 *            MIME type.
		 * @return The found {@link MimeType} or {@code null}.
		 * @throws IllegalArgumentException If {@code mimeType} is {@code null}.
		 */
		@Nullable
		public MimeType getMimeType(
			@Nonnull MimeType mimeType,
			@Nullable Map<String, String> parameters
		) {
			MimeType mimeValue = createNewInstance(mimeType, parameters);
			return getMimeType(mimeValue);
		}

		/**
		 * Retrieves a {@link MimeType} instance from {@link KnownMimeTypes}
		 * or the cache matching the specified values. If no such
		 * {@link MimeType} instance exists, {@code null} is returned.
		 *
		 * @param type the first part of the MIME type.
		 * @param subtype the second part of the MIME type.
		 * @param parameters a {@link Map} of additional parameters for this
		 *            MIME type.
		 * @return The found {@link MimeType} or {@code null}.
		 */
		@Nullable
		public MimeType getMimeType(
			@Nullable String type,
			@Nullable String subtype,
			@Nullable Map<String, String> parameters
		) {
			MimeType mimeValue = createNewInstance(type, subtype, parameters);
			return getMimeType(mimeValue);
		}

		/**
		 * Retrieves a {@link MimeType} instance from {@link KnownMimeTypes}
		 * or the cache by parsing the specified {@link String} value. If no
		 * such {@link MimeType} instance exists, {@code null} is returned.
		 *
		 * @param value the {@link String} value to parse.
		 * @return The found {@link MimeType} or {@code null}.
		 * @throws ParseException If {@code value} isn't a valid MIME type.
		 */
		@Nullable
		public MimeType getMimeType(@Nullable String value) throws ParseException {
			if (isBlank(value)) {
				return null;
			}
			MimeType mimeValue = createNewInstance(value.trim().toLowerCase(Locale.ROOT));
			return getMimeType(mimeValue);
		}

		/**
		 * Retrieves a {@link MimeType} instance from {@link KnownMimeTypes}
		 * or the cache that has an equal value as the specified
		 * {@link MimeType}. If no such {@link MimeType} instance exists,
		 * {@code null} is returned.
		 *
		 * @param mimeValue the {@link MimeType} to look for.
		 * @return The found {@link MimeType} or {@code null}.
		 */
		@Nullable
		protected MimeType getMimeType(@Nullable MimeType mimeValue) {
			if (mimeValue == null) {
				return null;
			}

			// Check for known instances
			MimeType instance = searchKnownInstances(mimeValue);
			if (instance != null) {
				return instance;
			}

			// Check for cached instances
			synchronized (this) {
				for (MimeType cachedMimeType : instanceCache) {
					if (mimeValue.equalValue(cachedMimeType)) {
						return cachedMimeType;
					}
				}
			}
			return null;
		}

		/**
		 * Creates or retrieves a {@link MimeType} instance from
		 * {@link KnownMimeTypes} or the cache matching the specified values.
		 * All {@link KnownMimeTypes} and cached instances are first checked.
		 * If no such {@link MimeType} instance exists, a new is created, added
		 * to the cache and returned. Otherwise, the existing instance is
		 * returned.
		 *
		 * @param type the first part of the MIME type.
		 * @param subtype the second part of the MIME type.
		 * @return The already existing or newly created {@link MimeType}
		 *         instance.
		 */
		@Nonnull
		public MimeType createMimeType(@Nullable String type, @Nullable String subtype) {
			MimeType mimeValue = createNewInstance(type, subtype, null);
			return createMimeType(mimeValue);
		}

		/**
		 * Creates or retrieves a {@link MimeType} instance from
		 * {@link KnownMimeTypes} or the cache matching a {@link MimeType}
		 * created by coping of the specified {@link MimeType} base type using
		 * the specified parameters. All {@link KnownMimeTypes} and cached
		 * instances are first checked. If no such {@link MimeType} instance
		 * exists, a new is created, added to the cache and returned. Otherwise,
		 * the existing instance is returned.
		 *
		 * @param mimeType the {@link MimeType} to copy type and subtype from.
		 * @param parameters a {@link Map} of additional parameters for this
		 *            MIME type.
		 * @return The already existing or newly created {@link MimeType}
		 *         instance.
		 * @throws IllegalArgumentException If {@code mimeType} is {@code null}.
		 */
		@Nonnull
		public MimeType createMimeType(
			@Nonnull MimeType mimeType,
			@Nullable Map<String, String> parameters
		) {
			MimeType mimeValue = createNewInstance(mimeType, parameters);
			return createMimeType(mimeValue);
		}

		/**
		 * Creates or retrieves a {@link MimeType} instance from
		 * {@link KnownMimeTypes} or the cache matching the specified values.
		 * All {@link KnownMimeTypes} and cached instances are first checked.
		 * If no such {@link MimeType} instance exists, a new is created, added
		 * to the cache and returned. Otherwise, the existing instance is
		 * returned.
		 *
		 * @param type the first part of the MIME type.
		 * @param subtype the second part of the MIME type.
		 * @param parameters a {@link Map} of additional parameters for this
		 *            MIME type.
		 * @return The already existing or newly created {@link MimeType}
		 *         instance.
		 */
		@Nonnull
		public MimeType createMimeType(
			@Nullable String type,
			@Nullable String subtype,
			@Nullable Map<String, String> parameters
		) {
			MimeType mimeValue = createNewInstance(type, subtype, parameters);
			return createMimeType(mimeValue);
		}

		/**
		 * Creates or retrieves a {@link MimeType} instance from
		 * {@link KnownMimeTypes} or the cache by parsing the specified
		 * {@link String} value. All {@link KnownMimeTypes} and cached
		 * instances are first checked. If no such {@link MimeType} instance
		 * exists, a new is created, added to the cache and returned. Otherwise,
		 * the existing instance is returned.
		 *
		 * @param value the {@link String} value to parse.
		 * @return The already existing or newly created {@link MimeType}
		 *         instance.
		 * @throws ParseException If {@code value} isn't a valid MIME type.
		 * @throws IllegalArgumentException If {@code value} is blank.
		 */
		@Nonnull
		public MimeType createMimeType(@Nonnull String value) throws ParseException {
			if (isBlank(value)) {
				throw new IllegalArgumentException("value cannot be blank");
			}
			MimeType mimeValue = createNewInstance(value.trim().toLowerCase(Locale.ROOT));
			return createMimeType(mimeValue);
		}

		/**
		 * Adds the specified {@link MimeType} instance to the cache or
		 * retrieves an instance with an equal value. All
		 * {@link KnownMimeTypes} and cached instances are first checked. If
		 * a match is found, the pre-existing instance is returned. Otherwise
		 * the specified instance is added to the cache and returned.
		 *
		 * @param mimeValue the {@link MimeType} to retrieve or add.
		 * @return The {@link MimeType} instance.
		 */
		@Nullable
		protected MimeType createMimeType(@Nullable MimeType mimeValue) {
			if (mimeValue == null) {
				return null;
			}

			// Check if an instance for this value already exists
			MimeType instance = getMimeType(mimeValue);
			if (instance != null) {
				return instance;
			}

			// Prepare to create a new instance
			synchronized (this) {
				// Check cache again as it could have been added while the
				// lock was released
				for (MimeType cachedMimeType : instanceCache) {
					if (mimeValue.equalValue(cachedMimeType)) {
						return cachedMimeType;
					}
				}

				// None was found, use the newly created instance
				instanceCache.add(mimeValue);
				LOGGER.trace("{} added MIME type \"{}\" to cache", getClass().getSimpleName(), mimeValue);
				return mimeValue;
			}
		}
	}

	/**
	 * This immutable class is the standard {@link MimeType} implementation.
	 *
	 * @author Nadahar
	 */
	@Immutable
	public class MimeTypeImpl implements MimeType {

		private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypeImpl.class);
		private static final long serialVersionUID = 1L;

		/** The type. */
		protected final String type;

		/** The subtype. */
		protected final String subtype;

		/** The {@link Map} of parameters. */
		protected final Map<String, String> parameters;

		/** The cached {@link #toString()} value */
		protected final String stringValue;

		/**
		 * Creates a new {@link MimeTypeImpl} instance with both {@code type} and
		 * {@code subtype} set to {@link MimeTypeImpl#ANY}.
		 */
		protected MimeTypeImpl() {
			this(ANY, ANY);
		}

		/**
		 * Creates a new {@link MimeTypeImpl} instance using the given values.
		 *
		 * @param type the first part of the MIME type.
		 * @param subtype the second part of the MIME type.
		 */
		protected MimeTypeImpl(@Nullable String type, @Nullable String subtype) {
			this(type, subtype, null);
		}

		/**
		 * Creates a copy of the specified {@link MimeType} base type using the
		 * specified parameters.
		 *
		 * @param mimeType the {@link MimeType} to copy type and subtype from.
		 * @param parameters a {@link Map} of additional parameters for this
		 *            MIME type.
		 */
		protected MimeTypeImpl(@Nonnull MimeType mimeType, @Nullable Map<String, String> parameters) {
			this(mimeType.getType(), mimeType.getSubtype(), parameters);
		}

		/**
		 * Creates a new {@link MimeTypeImpl} instance using the given values.
		 *
		 * @param type the first part of the MIME type.
		 * @param subtype the second part of the MIME type.
		 * @param parameters a {@link Map} of additional parameters for this
		 *            MIME type.
		 */
		protected MimeTypeImpl(@Nullable String type, @Nullable String subtype, @Nullable Map<String, String> parameters) {
			this.type = type == null ? ANY : type;
			this.subtype = subtype == null ? ANY : subtype;
			if (parameters == null) {
				this.parameters = Collections.EMPTY_MAP;
			} else {
				TreeMap<String, String> map = new TreeMap<String, String>(new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						return o1.compareToIgnoreCase(o2);
					}

				});
				for (Entry<String, String> entry : parameters.entrySet()) {
					map.put(entry.getKey(), entry.getValue());
				}
				this.parameters = Collections.unmodifiableSortedMap(map);
			}
			this.stringValue = generateStringValue();
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public boolean isAnyType() {
			return isBlank(type) || ANY.equals(type);
		}

		@Override
		public String getSubtype() {
			return subtype;
		}

		@Override
		public boolean isAnySubtype() {
			return ANY.equals(subtype);
		}

		@Override
		@Nonnull
		public Map<String, String> getParameters() {
			return parameters;
		}

		@Override
		public boolean isCompatible(MimeType other) {
			if (other == null) {
				return false;
			}
			if (
				(isBlank(type) || ANY.equals(type)) &&
				isBlank(subtype) ||
				(isBlank(other.getType()) || ANY.equals(other.getType())) &&
				isBlank(other.getSubtype())
			) {
				return true;
			} else if (isBlank(type) || (isBlank(other.getType()))) {
				return
					isBlank(subtype) ||
					isBlank(other.getSubtype()) ||
					ANY.equals(subtype) ||
					ANY.equals(other.getSubtype()) ||
					subtype.toLowerCase(Locale.ROOT).equals(other.getSubtype().toLowerCase(Locale.ROOT));
			} else if (
				type.toLowerCase(Locale.ROOT).equals(other.getType().toLowerCase(Locale.ROOT)) &&
				(
					isBlank(subtype) ||
					ANY.equals(subtype) ||
					isBlank(other.getSubtype()) ||
					ANY.equals(other.getSubtype())
				)
			) {
				return true;
			} else if (isBlank(subtype) || isBlank(other.getSubtype())) {
				return false;
			} else {
				return
					type.toLowerCase(Locale.ROOT).equals(other.getType().toLowerCase(Locale.ROOT)) &&
					subtype.toLowerCase(Locale.ROOT).equals(other.getSubtype().toLowerCase(Locale.ROOT));
			}
		}

		@Override
		public boolean isDRM() {
			return isDTCP();
		}

		@Override
		public boolean isDTCP() {
			return "application".equalsIgnoreCase(type) && "x-dtcp1".equalsIgnoreCase(subtype);
		}

		@Override
		public String toString() {
			return stringValue;
		}

		@Override
		public String toStringWithoutParameters() {
			return type + "/" + subtype;
		}

		@Override
		public org.seamless.util.MimeType toSeamlessMimeType() {
			return new org.seamless.util.MimeType(type, subtype, parameters);
		}

		/**
		 * Generates the {@link String} value for caching of the {@link #toString()}
		 * value.
		 *
		 * @return The generated {@link String} value.
		 */
		@Nonnull
		protected String generateStringValue() {
			StringBuilder sb = new StringBuilder(toStringWithoutParameters());
			if (parameters != null && parameters.size() > 0) {
				for (Entry<String, String> parameter : parameters.entrySet()) {
					sb.append(";").append(parameter.getKey()).append("=").append(parameter.getValue());
				}
			}
			return sb.toString();
		}

		/**
		 * Creates a new {@link MimeTypeImpl} by attempting to parse
		 * {@code stringValue}.
		 *
		 * @param stringValue the {@link String} to parse.
		 * @return The new {@link MimeTypeImpl}.
		 * @throws ParseException If {@code stringValue} isn't a valid MIME type.
		 */
		@Nonnull
		protected static MimeTypeImpl valueOf(String stringValue) throws ParseException {
			if (isBlank(stringValue)) {
				return ANYANY;
			}

			String[] parts = stringValue.trim().split("\\s*;\\s*");
			String[] elements = parts[0].split("\\s*/\\s*");

			String type = null;
			String subtype = null;

			if (elements.length < 2) {
				if (parts[0].equals(ANY) || isBlank(parts[0])) {
					type = ANY;
					subtype = ANY;
				} else {
					type = elements[0];
					subtype = ANY;
				}
			} else if (elements.length == 2) {
				type = elements[0];
				subtype = elements[1];
			} else if (elements.length > 2) {
				throw new ParseException("Error parsing MimeTypeImpl \"" + parts[0] + "\" from \"" + stringValue + "\"");
			}

			if (parts.length > 1) {
				HashMap<String, String> parameterMap = new HashMap<>();
				for (int i = 1; i < parts.length; i++) {
					if (isBlank(parts[i])) {
						continue;
					}
					String[] parameter = parts[i].trim().split("\\s*=\\s*");
					if (parameter.length == 2 && isNotBlank(parameter[0])) {
						parameterMap.put(parameter[0], parameter[1]);
					} else if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("MimeTypeImpl: Unable to parse parameter \"{}\" - it will be ignored", parts[i]);
					}
				}
				return new MimeTypeImpl(type, subtype, parameterMap);
			}
			return new MimeTypeImpl(type, subtype);
		}

		@Override
		public boolean equalValue(MimeType other) {
			return evaluateEquals(other, true);
		}

		@Override
		public boolean equalBaseValue(MimeType other) {
			return evaluateEquals(other, false);
		}

		/**
		 * Evaluates the {@link MimeType} specific "equals" implementations.
		 *
		 * @param other the {@link MimeType} to compare with.
		 * @param equalParameters whether the parameters should be compared.
		 * @return {@code true} if the two are considered equal, {@code false}
		 *         otherwise.
		 */
		protected boolean evaluateEquals(MimeType other, boolean equalParameters) {
			if (this == other) {
				return true;
			}
			if (other == null) {
				return false;
			}
			if (equalParameters && !parameters.equals(other.getParameters())) {
				return false;
			}
			if (subtype == null) {
				if (other.getSubtype() != null) {
					return false;
				}
			} else if (other.getSubtype() == null) {
				return false;
			} else if (!subtype.toLowerCase(Locale.ROOT).equals(other.getSubtype().toLowerCase(Locale.ROOT))) {
				return false;
			}
			if (type == null) {
				if (other.getType() != null) {
					return false;
				}
			} else if (other.getType() == null) {
				return false;
			} else if (!type.toLowerCase(Locale.ROOT).equals(other.getType().toLowerCase(Locale.ROOT))) {
				return false;
			}
			return true;
		}
	}
}
