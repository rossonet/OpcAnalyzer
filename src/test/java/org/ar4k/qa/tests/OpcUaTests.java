package org.ar4k.qa.tests;

import org.junit.jupiter.api.Test;
import org.rossonet.debug.console.ua.OpcUaShellInterface;

public class OpcUaTests {

	@Test
	public void testDiscovery() {
		final OpcUaShellInterface opcUaShellInterface = new OpcUaShellInterface();
		System.out.print(opcUaShellInterface.discover("opc.tcp://192.168.1.53:49320/"));
	}

	@Test
	public void testDumpTree() throws Exception {
		final OpcUaShellInterface opcUaShellInterface = new OpcUaShellInterface();
		System.out.print(opcUaShellInterface.dumpTree("opc.tcp://192.168.1.53:49320/"));
	}

}
