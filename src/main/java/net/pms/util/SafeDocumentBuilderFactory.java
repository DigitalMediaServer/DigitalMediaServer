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
package net.pms.util;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link DocumentBuilderFactory} implementation that creates
 * {@link DocumentBuilder} instances that is protected against <a
 * href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing"
 * >the XML External Entity (XXE) Processing vulnerability</a>.
 * <p>
 * {@code DTD} ({@code !DOCTYPE}) are disabled by default. It is possible to
 * create {@link DocumentBuilder} instances that allows {@code DTD} with this
 * implementation, but referral to external {@code DTD}s is prohibited. It this
 * functionality is needed, use another {@link DocumentBuilderFactory} and
 * implement some other form of protection against {@code XXE}.
 *
 * @author Nadahar
 */
public class SafeDocumentBuilderFactory extends DocumentBuilderFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(SafeDocumentBuilderFactory.class);

	/** The {@code DTD} "feature" */
	public static final String FEATURE_DTD = "http://apache.org/xml/features/disallow-doctype-decl";

	/** The external general entities "feature" */
	public static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

	/** The external parameter entities "feature" */
	public static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

	/** The external {@code DTD} "feature" */
	public static final String FEATURE_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	/** A copy of {@link javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING} because this constant isn't available in all JVMs*/
	public static final String FEATURE_SECURE_PROCESSING = "http://javax.xml.XMLConstants/feature/secure-processing";

	/** A copy of {@link javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD} because this constant isn't available in all JVMs*/
	public static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";

	/** A copy of {@link javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA} because this constant isn't available in all JVMs*/
	public static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";

	/** A copy of {@link javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET} because this constant isn't available in all JVMs*/
	public static final String ACCESS_EXTERNAL_STYLESHEET = "http://javax.xml.XMLConstants/property/accessExternalStylesheet";

	@Nonnull
	private final DocumentBuilderFactory delegate;

	/**
	 * Creates a new instance using the specified parameter.
	 *
	 * @param allowDTD {@code true} if {@link DocumentBuilder} instances created
	 *            by this {@link DocumentBuilderFactory} should allow
	 *            {@code DTD} processing.
	 */
	protected SafeDocumentBuilderFactory(boolean allowDTD) {
		delegate = DocumentBuilderFactory.newInstance();
		secureDelegate(allowDTD);
	}

	/**
	 * Creates a new instance using the specified parameters.
	 *
	 * @param factoryClassName the fully qualified factory class name that
	 *            provides implementation of
	 *            {@code javax.xml.parsers.DocumentBuilderFactory}.
	 * @param classLoader the {@link ClassLoader} to use when loading the
	 *            factory class. If {@code null}, the current {@link Thread}'s
	 *            context {@link ClassLoader} is used to load the factory class.
	 * @param allowDTD {@code true} if {@link DocumentBuilder} instances created
	 *            by this {@link DocumentBuilderFactory} should allow
	 *            {@code DTD} processing.
	 */
	protected SafeDocumentBuilderFactory(@Nonnull String factoryClassName, @Nullable ClassLoader classLoader, boolean allowDTD) {
		delegate = DocumentBuilderFactory.newInstance(factoryClassName, classLoader);
		secureDelegate(allowDTD);
	}

	/**
	 * Applies the restrictions to the delegate {@link DocumentBuilderFactory}
	 * that protects against {@code XXE}.
	 *
	 * @param allowDTD {@code true} if {@link DocumentBuilder} instances created
	 *            by this {@link DocumentBuilderFactory} should allow
	 *            {@code DTD} processing.
	 */
	protected void secureDelegate(boolean allowDTD) {
		ArrayList<String> features = new ArrayList<String>();

		// FEATURE_SECURE_PROCESSING only works in Java 8 and above, disables external DTD and Schema
		features.add(FEATURE_SECURE_PROCESSING);
		if (!allowDTD) {
			features.add(FEATURE_DTD);
		}
		features.add(FEATURE_EXTERNAL_DTD);
		features.add(FEATURE_EXTERNAL_GENERAL_ENTITIES);
		features.add(FEATURE_EXTERNAL_PARAMETER_ENTITIES);

		for (String feature : features) {
			try {
				delegate.setFeature(feature, false);
			} catch (Exception e) {
				LOGGER.warn(
					"Disabling \"{}\" isn't supported by document builder factory {}. XML parsing might be vulnerable for XXE exploits",
					feature,
					delegate.getClass().getName()
				);
			}
		}

		features.clear();
		ArrayList<String> attributes = features;

		attributes.add(ACCESS_EXTERNAL_DTD);
		attributes.add(ACCESS_EXTERNAL_SCHEMA);
		attributes.add(ACCESS_EXTERNAL_STYLESHEET);

		for (String attribute : attributes) {
			try {
				delegate.setAttribute(attribute, "");
			} catch (Exception e) {
				LOGGER.warn(
					"Disabling \"{}\" isn't supported by document builder factory {}. XML parsing might be vulnerable for XXE exploits",
					attribute,
					delegate.getClass().getName()
				);
			}
		}

		try {
			delegate.setXIncludeAware(false);
		} catch (UnsupportedOperationException e) {
			LOGGER.warn(
				"setXIncludeAware() isn't supported by document builder factory {}. XML parsing might be vulnerable for XXE exploits",
				delegate.getClass().getName()
			);
		}
		try {
			delegate.setExpandEntityReferences(false);
		} catch (UnsupportedOperationException e) {
			LOGGER.warn(
				"setExpandEntityReferences() isn't supported by document builder factory {}. " +
				"XML parsing might be vulnerable for XXE exploits",
				delegate.getClass().getName()
			);
		}
	}

	@Override
	public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return delegate.newDocumentBuilder();
	}

	@Override
	public void setAttribute(String name, Object value) throws IllegalArgumentException {
		delegate.setAttribute(name, value);
	}

	@Override
	public Object getAttribute(String name) throws IllegalArgumentException {
		return delegate.getAttribute(name);
	}

	@Override
	public void setFeature(String name, boolean value) throws ParserConfigurationException {
		delegate.setAttribute(name, value);
	}

	@Override
	public boolean getFeature(String name) throws ParserConfigurationException {
		return delegate.getFeature(name);
	}

	/**
	 * Creates a new {@link SafeDocumentBuilderFactory} instance that doesn't allow
	 * {@code DTD} processing at all.
	 *
	 * @return The new instance.
	 */
	public static SafeDocumentBuilderFactory newInstance() {
		return new SafeDocumentBuilderFactory(false);
	}

	/**
	 * Creates a new {@link SafeDocumentBuilderFactory} instance using the specified
	 * parameter.
	 *
	 * @param allowDTD {@code true} if {@link DocumentBuilder} instances created
	 *            by this {@link DocumentBuilderFactory} should allow
	 *            {@code DTD} processing.
	 * @return The new instance.
	 */
	public static SafeDocumentBuilderFactory newInstance(boolean allowDTD) {
		return new SafeDocumentBuilderFactory(allowDTD);
	}

	/**
	 * Creates a new {@link SafeDocumentBuilderFactory} instance that doesn't
	 * allow {@code DTD} processing at all.
	 * {@link DocumentBuilderFactory#newInstance(String, ClassLoader)} is used
	 * internally.
	 *
	 * @param factoryClassName the fully qualified factory class name that
	 *            provides implementation of
	 *            {@code javax.xml.parsers.DocumentBuilderFactory}.
	 * @param classLoader the {@link ClassLoader} to use when loading the
	 *            factory class. If {@code null}, the current {@link Thread}'s
	 *            context {@link ClassLoader} is used to load the factory class.
	 * @return The new instance.
	 *
	 * @see DocumentBuilderFactory#newInstance(String, ClassLoader)
	 */
	public static SafeDocumentBuilderFactory newInstance(@Nonnull String factoryClassName, @Nullable ClassLoader classLoader) {
		return new SafeDocumentBuilderFactory(factoryClassName, classLoader, false);
	}

	/**
	 * Creates a new {@link SafeDocumentBuilderFactory} instance using the
	 * specified parameters.
	 * {@link DocumentBuilderFactory#newInstance(String, ClassLoader)} is used
	 * internally.
	 *
	 * @param factoryClassName the fully qualified factory class name that
	 *            provides implementation of
	 *            {@code javax.xml.parsers.DocumentBuilderFactory}.
	 * @param classLoader the {@link ClassLoader} to use when loading the
	 *            factory class. If {@code null}, the current {@link Thread}'s
	 *            context {@link ClassLoader} is used to load the factory class.
	 * @param allowDTD {@code true} if {@link DocumentBuilder} instances created
	 *            by this {@link DocumentBuilderFactory} should allow
	 *            {@code DTD} processing.
	 * @return The new instance.
	 *
	 * @see DocumentBuilderFactory#newInstance(String, ClassLoader)
	 */
	public static SafeDocumentBuilderFactory newInstance(
		@Nonnull String factoryClassName,
		@Nullable ClassLoader classLoader,
		boolean allowDTD
	) {
		return new SafeDocumentBuilderFactory(factoryClassName, classLoader, allowDTD);
	}
}
