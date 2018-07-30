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
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link TransformerFactory} implementation that creates {@link Transformer}
 * instances that is protected against <a
 * href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing"
 * >the XML External Entity (XXE) Processing vulnerability</a>.
 * <p>
 * The protection consists of disallowing the use of external {@code DTD}s and
 * external stylesheets.
 *
 * @author Nadahar
 */
public class SafeTransformerFactory extends TransformerFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(SafeTransformerFactory.class);

	@Nonnull
	private final TransformerFactory delegate;

	/**
	 * Creates a new instance.
	 */
	protected SafeTransformerFactory() {
		delegate = TransformerFactory.newInstance();
		secureDelegate();
	}

	/**
	 * Creates a new instance using the specified parameters.
	 *
	 * @param factoryClassName the fully qualified factory class name that
	 *            provides implementation of
	 *            {@code javax.xml.transform.TransformerFactory}.
	 * @param classLoader the {@link ClassLoader} to use when loading the
	 *            factory class. If {@code null}, the current {@link Thread}'s
	 *            context {@link ClassLoader} is used to load the factory class.
	 */
	protected SafeTransformerFactory(@Nonnull String factoryClassName, @Nullable ClassLoader classLoader) {
		delegate = TransformerFactory.newInstance(factoryClassName, classLoader);
		secureDelegate();
	}

	/**
	 * Applies the restrictions to the delegate {@link TransformerFactory} that
	 * protects against {@code XXE}.
	 */
	protected void secureDelegate() {
		ArrayList<String> attributes = new ArrayList<String>();

		attributes.add(SafeDocumentBuilderFactory.ACCESS_EXTERNAL_DTD);
		attributes.add(SafeDocumentBuilderFactory.ACCESS_EXTERNAL_STYLESHEET);

		for (String attribute : attributes) {
			try {
				delegate.setAttribute(attribute, "");
			} catch (Exception e) {
				LOGGER.warn(
					"Disabling \"{}\" isn't supported by transformer factory {}. XML transforms might be vulnerable for XXE exploits",
					attribute,
					delegate.getClass().getName()
				);
			}
		}
	}

	@Override
	public Transformer newTransformer(Source source) throws TransformerConfigurationException {
		return delegate.newTransformer(source);
	}

	@Override
	public Transformer newTransformer() throws TransformerConfigurationException {
		return delegate.newTransformer();
	}

	@Override
	public Templates newTemplates(Source source) throws TransformerConfigurationException {
		return delegate.newTemplates(source);
	}

	@Override
	public Source getAssociatedStylesheet(
		Source source,
		String media, String title,
		String charset
	) throws TransformerConfigurationException {
		return delegate.getAssociatedStylesheet(source, media, title, charset);
	}

	@Override
	public void setURIResolver(URIResolver resolver) {
		delegate.setURIResolver(resolver);
	}

	@Override
	public URIResolver getURIResolver() {
		return delegate.getURIResolver();
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
	public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
		delegate.setErrorListener(listener);
	}

	@Override
	public ErrorListener getErrorListener() {
		return delegate.getErrorListener();
	}

	@Override
	public void setFeature(@Nonnull String name, boolean value) throws TransformerConfigurationException {
		delegate.setFeature(name, value);

	}

	@Override
	public boolean getFeature(@Nonnull String name) {
		return delegate.getFeature(name);
	}

	/**
	 * Creates a new {@link SafeTransformerFactory} instance that doesn't allow
	 * external {@code DTD}s or external stylesheets.
	 *
	 * @return The new instance.
	 */
	public static SafeTransformerFactory newInstance() {
		return new SafeTransformerFactory();
	}

	/**
	 * Creates a new {@link SafeTransformerFactory} instance that doesn't allow
	 * {external {@code DTD}s or external stylesheets.
	 * {@link TransformerFactory#newInstance(String, ClassLoader)} is used
	 * internally.
	 *
	 * @param factoryClassName the fully qualified factory class name that
	 *            provides implementation of
	 *            {@code javax.xml.transform.TransformerFactory}.
	 * @param classLoader the {@link ClassLoader} to use when loading the
	 *            factory class. If {@code null}, the current {@link Thread}'s
	 *            context {@link ClassLoader} is used to load the factory class.
	 * @return The new instance.
	 *
	 * @see TransformerFactory#newInstance(String, ClassLoader)
	 */
	public static SafeTransformerFactory newInstance(@Nonnull String factoryClassName, @Nullable ClassLoader classLoader) {
		return new SafeTransformerFactory(factoryClassName, classLoader);
	}
}
