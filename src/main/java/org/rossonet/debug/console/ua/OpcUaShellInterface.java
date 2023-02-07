package org.rossonet.debug.console.ua;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.security.ClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.rossonet.agent.OpcKeyUtils;
import org.rossonet.utils.LogHelper;

public class OpcUaShellInterface {

	private static final Logger logger = Logger.getLogger(OpcUaShellInterface.class.getName());

	private void browseNode(StringBuilder output, String indent, OpcUaClient client, NodeId browseRoot) {
		final BrowseDescription browse = new BrowseDescription(browseRoot, BrowseDirection.Forward,
				Identifiers.References, true, uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue()),
				uint(BrowseResultMask.All.getValue()));

		try {
			final BrowseResult browseResult = client.browse(browse).get();

			final List<ReferenceDescription> references = toList(browseResult.getReferences());

			for (final ReferenceDescription rd : references) {
				output.append(indent + " Node=" + rd.getBrowseName().getName() + "\n");
				// recursively browse to children
				rd.getNodeId().toNodeId(client.getNamespaceTable())
						.ifPresent(nodeId -> browseNode(output, indent + "  ", client, nodeId));
			}
		} catch (InterruptedException | ExecutionException e) {
			logger.severe("Browsing nodeId=" + browseRoot + " failed: " + e.getMessage() + "\n"
					+ LogHelper.stackTraceToString(e));
		}
	}

	private synchronized OpcUaClient createConnection(OpcUaConfiguration configuration, OpcUaListener opcUaHandler)
			throws Exception {
		try {
			final EndpointDescription endpoint = getEndpoint(configuration);
			logger.info("OPCUA connection to " + endpoint);
			if (!endpoint.getServerCertificate().isNullOrEmpty()) {
				logger.info("--- server certificate ---\n" + CertificateFactory.getInstance("X.509")
						.generateCertificate(new ByteArrayInputStream(endpoint.getServerCertificate().bytes())));
			}
			final OpcUaClientConfigBuilder config = OpcUaClientConfig.builder().setEndpoint(endpoint)
					.setApplicationUri(OpcKeyUtils.APPLICATION_URI)
					.setApplicationName(LocalizedText.english("opcua_client_" + UUID.randomUUID().toString()));
			if (configuration.getSessionTimeout() != null) {
				config.setSessionTimeout(configuration.getSessionTimeout());
			}
			if (configuration.getKeyPair() != null) {
				config.setKeyPair(configuration.getKeyPair());
			}
			if (configuration.getConnectTimeout() != null) {
				config.setConnectTimeout(configuration.getConnectTimeout());
			}
			if (configuration.getKeepAliveTimeout() != null) {
				config.setKeepAliveTimeout(configuration.getKeepAliveTimeout());
			}
			if (configuration.getRequestTimeout() != null) {
				config.setRequestTimeout(configuration.getRequestTimeout());
			}
			if (configuration.getChannelLifetime() != null) {
				config.setChannelLifetime(configuration.getChannelLifetime());
			}
			if (configuration.getIdentityProvider() != null) {
				config.setIdentityProvider(configuration.getIdentityProvider());
			}
			if (configuration.getAcknowledgeTimeout() != null) {
				config.setAcknowledgeTimeout(configuration.getAcknowledgeTimeout());
			}
			if (configuration.getCertificateChain() != null) {
				config.setCertificateChain(configuration.getCertificateChain());
			}
			if (configuration.getCertificate() != null) {
				config.setCertificate(configuration.getCertificate());
			}
			if (configuration.getForceCertificateValidator()) {
				final ClientCertificateValidator alwaysValidCert = new ClientCertificateValidator() {

					@Override
					public void validateCertificateChain(List<X509Certificate> certificateChain) throws UaException {
						final StringBuilder sb = new StringBuilder();
						for (final X509Certificate c : certificateChain) {
							sb.append("\n" + c + "\n");
						}
						logger.warning(
								"beacause forceCertificateValidator is true, the function validateCertificateChain authorizes the follow certificates"
										+ sb.toString());
					}

					@Override
					public void validateCertificateChain(List<X509Certificate> certificateChain, String applicationUri,
							String... validHostNames) throws UaException {
						final StringBuilder sb = new StringBuilder();
						final StringBuilder hosts = new StringBuilder();
						for (final X509Certificate c : certificateChain) {
							sb.append("\n" + c + "\n");
						}
						for (final String host : validHostNames) {
							sb.append(host + ", ");
						}
						logger.warning(
								"beacause forceCertificateValidator is true, the function validateCertificateChain authorizes the follow certificates"
										+ sb.toString() + "\nApplicationUri is " + applicationUri + " and hosts are "
										+ hosts.toString());

					}

				};
				config.setCertificateValidator(alwaysValidCert);
			}
			final OpcUaClient client = OpcUaClient.create(config.build());
			client.addFaultListener(opcUaHandler);
			return (OpcUaClient) client.connect().get();
		} catch (final Exception a) {
			logger.severe(LogHelper.stackTraceToString(a));
			throw a;
		}
	}

	public String discover(String endpoint) {
		final StringBuilder sb = new StringBuilder();
		final OpcUaConfiguration configuration = new OpcUaConfiguration();
		configuration.setDiscoveryEndpoint(endpoint);
		final List<EndpointDescription> endpoints = discoveryConnection(configuration);
		for (final EndpointDescription e : endpoints) {
			sb.append(e.toString() + "\n");
		}
		return sb.toString();
	}

	private List<EndpointDescription> discoveryConnection(OpcUaConfiguration configuration) {
		List<EndpointDescription> endpoints = null;
		try {
			final String discoveryEndpoint = configuration.getDiscoveryEndpoint();
			logger.info("DISCOVERY ENDPOINT " + discoveryEndpoint);
			endpoints = DiscoveryClient.getEndpoints(discoveryEndpoint).get();
		} catch (InterruptedException | ExecutionException exception) {
			logger.severe("error looking for endpoint " + LogHelper.stackTraceToString(exception));
		}
		return endpoints;
	}

	public String dumpTree(String endpoint) throws Exception {
		final StringBuilder sb = new StringBuilder();
		final OpcUaConfiguration configuration = new OpcUaConfiguration();
		configuration.setDiscoveryEndpoint(endpoint);
		final OpcUaListener listener = new OpcUaListener();
		final OpcUaClient client = createConnection(configuration, listener);
		browseNode(sb, "", client, Identifiers.RootFolder);
		return sb.toString();
	}

	private EndpointDescription getEndpoint(OpcUaConfiguration configuration) {
		EndpointDescription endpointDescriptionFound = null;
		final List<EndpointDescription> endpoints = discoveryConnection(configuration);
		final MessageSecurityMode searchSecurityMode = configuration.getSecurityMode();
		final SecurityPolicy searchSecurityPolicyTarget = configuration.getSecurityPolicyUri();
		if (endpoints != null) {
			for (final EndpointDescription e : endpoints) {
				if (e.getSecurityPolicyUri().equals(searchSecurityPolicyTarget.getUri())
						&& e.getSecurityMode().equals(searchSecurityMode)) {
					logger.fine("found OPCUA endpoint with the selected functions -> " + e);
					endpointDescriptionFound = e;
					break;
				}
			}
		}
		if (endpointDescriptionFound == null) {
			logger.severe("NO ENDPOINT OPCUA FOUND IN " + endpoints);
		} else if (configuration.getForceEndpointUrl() != null && !configuration.getForceEndpointUrl().isEmpty()) {
			logger.warning("FORCE ENDPOINT TO: " + configuration.getForceEndpointUrl());
			endpointDescriptionFound = EndpointUtil.updateUrl(endpointDescriptionFound,
					configuration.getForceEndpointUrl());
		}
		return endpointDescriptionFound;
	}

}
