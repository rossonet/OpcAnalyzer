package org.rossonet.debug.console.ua;

import java.util.logging.Logger;

import org.eclipse.milo.opcua.sdk.client.api.ServiceFaultListener;
import org.eclipse.milo.opcua.stack.core.types.structured.ServiceFault;

public class OpcUaListener implements ServiceFaultListener {

	private static final Logger logger = Logger.getLogger(OpcUaListener.class.getName());

	@Override
	public void onServiceFault(ServiceFault serviceFault) {
		logger.severe("onServiceFault -> " + serviceFault.toString());

	}

}
