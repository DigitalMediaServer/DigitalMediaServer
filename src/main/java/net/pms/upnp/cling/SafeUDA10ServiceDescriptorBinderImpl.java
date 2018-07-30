package net.pms.upnp.cling;

import static org.fourthline.cling.binding.xml.Descriptor.Service.ATTRIBUTE;
import static org.fourthline.cling.binding.xml.Descriptor.Service.ELEMENT;
import static org.fourthline.cling.model.XMLUtil.appendNewElement;
import static org.fourthline.cling.model.XMLUtil.appendNewElementIfNotNull;
import java.io.StringReader;
import java.util.Locale;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import org.fourthline.cling.binding.xml.Descriptor;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderImpl;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.QueryStateVariableAction;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.types.CustomDatatype;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import net.pms.util.SafeDocumentBuilderFactory;

public class SafeUDA10ServiceDescriptorBinderImpl extends UDA10ServiceDescriptorBinderImpl {

	private static Logger log = Logger.getLogger(SafeUDA10ServiceDescriptorBinderImpl.class.getName());

	@SuppressWarnings("rawtypes")
	@Override
	public <S extends Service> S describe(S undescribedService, String descriptorXml) throws DescriptorBindingException,
		ValidationException {
		if (descriptorXml == null || descriptorXml.length() == 0) {
			throw new DescriptorBindingException("Null or empty descriptor");
		}

		try {
			log.fine("Populating service from XML descriptor: " + undescribedService);

			SafeDocumentBuilderFactory factory = SafeDocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			documentBuilder.setErrorHandler(this);

			Document d = documentBuilder.parse(new InputSource(
			// UPNP VIOLATION: Virgin Media Superhub sends trailing spaces/newlines after last XML element, need to trim()
				new StringReader(descriptorXml.trim())));

			return describe(undescribedService, d);

		} catch (ValidationException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new DescriptorBindingException("Could not parse service descriptor: " + ex.toString(), ex);
		}
	}

	@Override
	public Document buildDOM(Service service) throws DescriptorBindingException {

		try {
			log.fine("Generating XML descriptor from service model: " + service);

			SafeDocumentBuilderFactory factory = SafeDocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			Document d = factory.newDocumentBuilder().newDocument();
			generateScpd(service, d);

			return d;

		} catch (Exception ex) {
			throw new DescriptorBindingException("Could not generate service descriptor: " + ex.getMessage(), ex);
		}
	}

	private static void generateScpd(@SuppressWarnings("rawtypes") Service serviceModel, Document descriptor) {

		Element scpdElement = descriptor.createElementNS(Descriptor.Service.NAMESPACE_URI, ELEMENT.scpd.toString());
		descriptor.appendChild(scpdElement);

		generateSpecVersion(serviceModel, descriptor, scpdElement);
		if (serviceModel.hasActions()) {
			generateActionList(serviceModel, descriptor, scpdElement);
		}
		generateServiceStateTable(serviceModel, descriptor, scpdElement);
	}

	private static void generateSpecVersion(@SuppressWarnings("rawtypes") Service serviceModel, Document descriptor, Element rootElement) {
		Element specVersionElement = appendNewElement(descriptor, rootElement, ELEMENT.specVersion);
		appendNewElementIfNotNull(descriptor, specVersionElement, ELEMENT.major, serviceModel.getDevice().getVersion().getMajor());
		appendNewElementIfNotNull(descriptor, specVersionElement, ELEMENT.minor, serviceModel.getDevice().getVersion().getMinor());
	}

	@SuppressWarnings("rawtypes")
	private static void generateActionList(Service serviceModel, Document descriptor, Element scpdElement) {

		Element actionListElement = appendNewElement(descriptor, scpdElement, ELEMENT.actionList);

		for (Action action : serviceModel.getActions()) {
			if (!action.getName().equals(QueryStateVariableAction.ACTION_NAME)) {
				generateAction(action, descriptor, actionListElement);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static void generateAction(Action action, Document descriptor, Element actionListElement) {

		Element actionElement = appendNewElement(descriptor, actionListElement, ELEMENT.action);

		appendNewElementIfNotNull(descriptor, actionElement, ELEMENT.name, action.getName());

		if (action.hasArguments()) {
			Element argumentListElement = appendNewElement(descriptor, actionElement, ELEMENT.argumentList);
			for (ActionArgument actionArgument : action.getArguments()) {
				generateActionArgument(actionArgument, descriptor, argumentListElement);
			}
		}
	}

	private static void generateActionArgument(
		@SuppressWarnings("rawtypes") ActionArgument actionArgument,
		Document descriptor,
		Element actionElement
	) {

		Element actionArgumentElement = appendNewElement(descriptor, actionElement, ELEMENT.argument);

		appendNewElementIfNotNull(descriptor, actionArgumentElement, ELEMENT.name, actionArgument.getName());
		appendNewElementIfNotNull(descriptor, actionArgumentElement, ELEMENT.direction, actionArgument.getDirection().toString()
			.toLowerCase(Locale.ROOT));
		if (actionArgument.isReturnValue()) {
			// UPNP VIOLATION: WMP12 will discard RenderingControl service if it contains <retval> tags
			log.warning("UPnP specification violation: Not producing <retval> element to be compatible with WMP12: " + actionArgument);
			// appendNewElement(descriptor, actionArgumentElement,
			// ELEMENT.retval);
		}
		appendNewElementIfNotNull(descriptor, actionArgumentElement, ELEMENT.relatedStateVariable,
			actionArgument.getRelatedStateVariableName());
	}

	@SuppressWarnings("rawtypes")
	private static void generateServiceStateTable(Service serviceModel, Document descriptor, Element scpdElement) {

		Element serviceStateTableElement = appendNewElement(descriptor, scpdElement, ELEMENT.serviceStateTable);

		for (StateVariable stateVariable : serviceModel.getStateVariables()) {
			generateStateVariable(stateVariable, descriptor, serviceStateTableElement);
		}
	}

	private static void generateStateVariable(
		@SuppressWarnings("rawtypes") StateVariable stateVariable,
		Document descriptor,
		Element serviveStateTableElement
	) {

		Element stateVariableElement = appendNewElement(descriptor, serviveStateTableElement, ELEMENT.stateVariable);

		appendNewElementIfNotNull(descriptor, stateVariableElement, ELEMENT.name, stateVariable.getName());

		if (stateVariable.getTypeDetails().getDatatype() instanceof CustomDatatype) {
			appendNewElementIfNotNull(descriptor, stateVariableElement, ELEMENT.dataType, ((CustomDatatype) stateVariable.getTypeDetails()
				.getDatatype()).getName());
		} else {
			appendNewElementIfNotNull(descriptor, stateVariableElement, ELEMENT.dataType, stateVariable.getTypeDetails().getDatatype()
				.getBuiltin().getDescriptorName());
		}

		appendNewElementIfNotNull(descriptor, stateVariableElement, ELEMENT.defaultValue, stateVariable.getTypeDetails().getDefaultValue());

		// The default is 'yes' but we generate it anyway just to be sure
		if (stateVariable.getEventDetails().isSendEvents()) {
			stateVariableElement.setAttribute(ATTRIBUTE.sendEvents.toString(), "yes");
		} else {
			stateVariableElement.setAttribute(ATTRIBUTE.sendEvents.toString(), "no");
		}

		if (stateVariable.getTypeDetails().getAllowedValues() != null) {
			Element allowedValueListElement = appendNewElement(descriptor, stateVariableElement, ELEMENT.allowedValueList);
			for (String allowedValue : stateVariable.getTypeDetails().getAllowedValues()) {
				appendNewElementIfNotNull(descriptor, allowedValueListElement, ELEMENT.allowedValue, allowedValue);
			}
		}

		if (stateVariable.getTypeDetails().getAllowedValueRange() != null) {
			Element allowedValueRangeElement = appendNewElement(descriptor, stateVariableElement, ELEMENT.allowedValueRange);
			appendNewElementIfNotNull(descriptor, allowedValueRangeElement, ELEMENT.minimum, stateVariable.getTypeDetails()
				.getAllowedValueRange().getMinimum());
			appendNewElementIfNotNull(descriptor, allowedValueRangeElement, ELEMENT.maximum, stateVariable.getTypeDetails()
				.getAllowedValueRange().getMaximum());
			if (stateVariable.getTypeDetails().getAllowedValueRange().getStep() >= 1L) {
				appendNewElementIfNotNull(descriptor, allowedValueRangeElement, ELEMENT.step, stateVariable.getTypeDetails()
					.getAllowedValueRange().getStep());
			}
		}

	}
}
