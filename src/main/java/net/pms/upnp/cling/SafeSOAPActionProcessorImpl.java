package net.pms.upnp.cling;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.FactoryConfigurationError;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.control.ActionRequestMessage;
import org.fourthline.cling.model.message.control.ActionResponseMessage;
import org.fourthline.cling.transport.impl.SOAPActionProcessorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import net.pms.util.SafeDocumentBuilderFactory;

/**
 * Default implementation based on the <em>W3C DOM</em> XML processing API.
 *
 * @author Christian Bauer
 */
public class SafeSOAPActionProcessorImpl extends SOAPActionProcessorImpl {

	private static Logger log = Logger.getLogger(SafeSOAPActionProcessorImpl.class.getName());

	@Override
	protected SafeDocumentBuilderFactory createDocumentBuilderFactory() throws FactoryConfigurationError {
		return SafeDocumentBuilderFactory.newInstance();
	}

	@Override
	public void writeBody(ActionRequestMessage requestMessage, ActionInvocation actionInvocation) throws UnsupportedDataException {

		log.fine("Writing body of " + requestMessage + " for: " + actionInvocation);

		try {

			SafeDocumentBuilderFactory factory = SafeDocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document d = factory.newDocumentBuilder().newDocument();
			Element body = writeBodyElement(d);

			writeBodyRequest(d, body, requestMessage, actionInvocation);

			if (log.isLoggable(Level.FINER)) {
				log.finer("===================================== SOAP BODY BEGIN ============================================");
				log.finer(requestMessage.getBodyString());
				log.finer("-===================================== SOAP BODY END ============================================");
			}

		} catch (Exception ex) {
			throw new UnsupportedDataException("Can't transform message payload: " + ex, ex);
		}
	}

	@Override
	public void writeBody(ActionResponseMessage responseMessage, ActionInvocation actionInvocation) throws UnsupportedDataException {

		log.fine("Writing body of " + responseMessage + " for: " + actionInvocation);

		try {

			SafeDocumentBuilderFactory factory = SafeDocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document d = factory.newDocumentBuilder().newDocument();
			Element body = writeBodyElement(d);

			if (actionInvocation.getFailure() != null) {
				writeBodyFailure(d, body, responseMessage, actionInvocation);
			} else {
				writeBodyResponse(d, body, responseMessage, actionInvocation);
			}

			if (log.isLoggable(Level.FINER)) {
				log.finer("===================================== SOAP BODY BEGIN ============================================");
				log.finer(responseMessage.getBodyString());
				log.finer("-===================================== SOAP BODY END ============================================");
			}

		} catch (Exception ex) {
			throw new UnsupportedDataException("Can't transform message payload: " + ex, ex);
		}
	}
}
