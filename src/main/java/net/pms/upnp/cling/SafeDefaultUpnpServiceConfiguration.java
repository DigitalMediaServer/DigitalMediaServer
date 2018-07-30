package net.pms.upnp.cling;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;


public class SafeDefaultUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

	 /**
	 * Defaults to port '0', ephemeral
	 */
	public SafeDefaultUpnpServiceConfiguration() {
		super(false);
	}

	public SafeDefaultUpnpServiceConfiguration(int streamListenPort) {
		super(streamListenPort, false);
	}

	@Override
	protected GENAEventProcessor createGENAEventProcessor() {
		return new SafeGENAEventProcessorImpl();
	}

	@Override
	protected SOAPActionProcessor createSOAPActionProcessor() {
		return new SafeSOAPActionProcessorImpl();
	}

	@Override
	protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
		return new SafeUDA10DeviceDescriptorBinderImpl();
	}

	@Override
	protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
		return new SafeUDA10ServiceDescriptorBinderImpl();
	}
}
