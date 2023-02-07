package org.ar4k.qa.tests;

import java.net.UnknownHostException;

import org.jinterop.dcom.common.JIException;
import org.junit.jupiter.api.Test;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.common.NotConnectedException;
import org.openscada.opc.lib.da.DuplicateGroupException;
import org.rossonet.debug.console.classic.OpcDaShellInterface;

public class OpcClassicTests {

	@Test
	public void testDiscovery() {
		final OpcDaShellInterface opcDaShellInterface = new OpcDaShellInterface();
		final String result = opcDaShellInterface.discoverServiceClassicOpcDa("192.168.1.16", "WORKGROUP", "Rossonet",
				"!!PasSw0rd.");
		System.out.println(result);
	}

	@Test
	public void testDumpAddressSpaceGrayBox() throws IllegalArgumentException, UnknownHostException, JIException,
			AlreadyConnectedException, InterruptedException, NotConnectedException, DuplicateGroupException {
		final OpcDaShellInterface opcDaShellInterface = new OpcDaShellInterface();
		final String result = opcDaShellInterface.dumpTreeServiceClassicOpcDa("Graybox.Simulator.1",
				"2c2e36b7-fe45-4a29-bf89-9bfba6a40857", "192.168.1.16", "WORKGROUP", "Rossonet", "!!PasSw0rd.");
		System.out.println(result);
	}

	@Test
	public void testDumpAddressSpaceMatrikon() throws IllegalArgumentException, UnknownHostException, JIException,
			AlreadyConnectedException, InterruptedException, NotConnectedException, DuplicateGroupException {
		final OpcDaShellInterface opcDaShellInterface = new OpcDaShellInterface();
		final String result = opcDaShellInterface.dumpTreeServiceClassicOpcDa("Matrikon.OPC.Simulation.1",
				"f8582cf2-88fb-11d0-b850-00c0f0104305", "192.168.1.16", "WORKGROUP", "Rossonet", "!!PasSw0rd.");
		System.out.println(result);
	}

}
