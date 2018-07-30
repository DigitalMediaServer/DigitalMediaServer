package net.pms.upnp.cling;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.FactoryConfigurationError;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.message.UpnpMessage;
import org.fourthline.cling.model.message.gena.OutgoingEventRequestMessage;
import org.fourthline.cling.transport.impl.GENAEventProcessorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import net.pms.util.SafeDocumentBuilderFactory;

public class SafeGENAEventProcessorImpl extends GENAEventProcessorImpl {

	private static Logger log = Logger.getLogger(SafeGENAEventProcessorImpl.class.getName());

	@Override
	protected SafeDocumentBuilderFactory createDocumentBuilderFactory() throws FactoryConfigurationError {
		return SafeDocumentBuilderFactory.newInstance();
	}

	@Override
	public void writeBody(OutgoingEventRequestMessage requestMessage) throws UnsupportedDataException {
		log.fine("Writing body of: " + requestMessage);

		try {
			SafeDocumentBuilderFactory factory = SafeDocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document d = factory.newDocumentBuilder().newDocument();
			Element propertysetElement = writePropertysetElement(d);

			writeProperties(d, propertysetElement, requestMessage);

			requestMessage.setBody(UpnpMessage.BodyType.STRING, toString(d));

			if (log.isLoggable(Level.FINER)) {
				log.finer("===================================== GENA BODY BEGIN ============================================");
				log.finer(requestMessage.getBody().toString());
				log.finer("====================================== GENA BODY END =============================================");
			}
		} catch (Exception ex) {
			throw new UnsupportedDataException("Can't transform message payload: " + ex.getMessage(), ex);
		}
	}
}
