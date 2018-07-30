package net.pms.upnp.cling;

import java.io.StringReader;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.UDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import net.pms.util.SafeDocumentBuilderFactory;

public class SafeUDA10DeviceDescriptorBinderImpl extends UDA10DeviceDescriptorBinderImpl {

	private static Logger log = Logger.getLogger(SafeUDA10DeviceDescriptorBinderImpl.class.getName());

	@SuppressWarnings("rawtypes")
	@Override
	public <D extends Device> D describe(D undescribedDevice, String descriptorXml) throws DescriptorBindingException, ValidationException {

		if (descriptorXml == null || descriptorXml.length() == 0) {
			throw new DescriptorBindingException("Null or empty descriptor");
		}

		try {
			log.fine("Populating device from XML descriptor: " + undescribedDevice);
			/*
			 * We can not validate the XML document. There is no possible XML
			 * schema (maybe RELAX NG) that would properly constrain the UDA 1.0
			 * device descriptor documents: Any unknown element or attribute
			 * must be ignored, order of elements is not guaranteed. Try to
			 * write a schema for that! No combination of <xsd:any
			 * namespace="##any"> and <xsd:choice> works with that... But hey,
			 * MSFT sure has great tech guys! So what we do here is just parsing
			 * out the known elements and ignoring the other shit. We'll also do
			 * some very very basic validation of required elements, but that's
			 * it.
			 *
			 * And by the way... try this with JAXB instead of manual DOM
			 * processing! And you thought it couldn't get worse....
			 */

			SafeDocumentBuilderFactory factory = SafeDocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			documentBuilder.setErrorHandler(this);

			Document d = documentBuilder.parse(new InputSource(
			// TODO: UPNP VIOLATION: Virgin Media Superhub sends trailing
			// spaces/newlines after last XML element, need to trim()
				new StringReader(descriptorXml.trim())));

			return describe(undescribedDevice, d);

		} catch (ValidationException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new DescriptorBindingException("Could not parse device descriptor: " + ex.toString(), ex);
		}
	}

	@Override
	public Document buildDOM(Device deviceModel, RemoteClientInfo info, Namespace namespace) throws DescriptorBindingException {

		try {
			log.fine("Generating DOM from device model: " + deviceModel);

			SafeDocumentBuilderFactory factory = SafeDocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			Document d = factory.newDocumentBuilder().newDocument();
			generateRoot(namespace, deviceModel, d, info);

			return d;

		} catch (Exception ex) {
			throw new DescriptorBindingException("Could not generate device descriptor: " + ex.getMessage(), ex);
		}
	}
}
