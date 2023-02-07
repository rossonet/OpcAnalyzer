package org.rossonet.debug.console.ua;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.X509IdentityProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;

public class OpcUaConfiguration {

	private boolean forceCertificateValidator = false;
	private String endpoint = null;
	private UInteger sessionTimeout = UInteger.valueOf(60000);
	private UInteger connectTimeout = UInteger.valueOf(10000);
	private KeyPair keyPair = null;
	private UInteger keepAliveTimeout = UInteger.valueOf(5000);
	private UInteger requestTimeout = UInteger.valueOf(60000);
	private UInteger channelLifetime = UInteger.valueOf(60000);
	private X509Certificate[] certificateChain = null;
	private UInteger acknowledgeTimeout = UInteger.valueOf(5000);
	private X509Certificate certificate = null;
	private MessageSecurityMode securityMode = MessageSecurityMode.None;
	private SecurityPolicy securityPolicyUri = SecurityPolicy.None;
	private UInteger maxChunkCount = UInteger.valueOf(0);
	private UInteger maxChunkSize = UInteger.valueOf(8196);
	private UInteger maxMessageSize = UInteger.valueOf(0);
	private final String authMode = "none";
	private final PrivateKey clientAuthPrivateKey = null;
	private final X509Certificate clientAuthCertificate = null;
	private final String password = null;
	private final String userName = null;
	private final String forceEndpointUrl = null;

	public UInteger getAcknowledgeTimeout() {
		return acknowledgeTimeout;
	}

	private String getAuthMode() {
		return authMode;
	}

	public X509Certificate getCertificate() {
		return certificate;
	}

	public X509Certificate[] getCertificateChain() {
		return certificateChain;
	}

	public UInteger getChannelLifetime() {
		return channelLifetime;
	}

	private X509Certificate getClientAuthCertificate() {
		return clientAuthCertificate;
	}

	private PrivateKey getClientAuthPrivateKey() {
		return clientAuthPrivateKey;
	}

	public UInteger getConnectTimeout() {
		return connectTimeout;
	}

	public String getDiscoveryEndpoint() {
		return endpoint;
	}

	public boolean getForceCertificateValidator() {
		return forceCertificateValidator;
	}

	public String getForceEndpointUrl() {
		return forceEndpointUrl;
	}

	public IdentityProvider getIdentityProvider() {
		IdentityProvider idp = null;
		switch (getAuthMode()) {
		case "password":
			if (getUserName() != null && !getUserName().isEmpty()) {
				idp = new UsernameProvider(getUserName(), getPassword());
			}
			break;
		case "none":
			idp = new AnonymousProvider();
			break;
		case "certificate":
			idp = new X509IdentityProvider(getClientAuthCertificate(), getClientAuthPrivateKey());
			break;
		default:
			idp = new AnonymousProvider();
			break;
		}
		return idp;
	}

	public UInteger getKeepAliveTimeout() {
		return keepAliveTimeout;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public UInteger getMaxChunkCount() {
		return maxChunkCount;
	}

	public UInteger getMaxChunkSize() {
		return maxChunkSize;
	}

	public UInteger getMaxMessageSize() {
		return maxMessageSize;
	}

	private String getPassword() {
		return password;
	}

	public UInteger getRequestTimeout() {
		return requestTimeout;
	}

	public MessageSecurityMode getSecurityMode() {
		return securityMode;
	}

	public SecurityPolicy getSecurityPolicyUri() {
		return securityPolicyUri;
	}

	public UInteger getSessionTimeout() {
		return sessionTimeout;
	}

	private String getUserName() {
		return userName;
	}

	public void setAcknowledgeTimeout(int acknowledgeTimeout) {
		this.acknowledgeTimeout = UInteger.valueOf(acknowledgeTimeout);
	}

	public void setCertificate(X509Certificate certificate) {
		this.certificate = certificate;
	}

	public void setCertificateChain(X509Certificate[] certificateChain) {
		this.certificateChain = certificateChain;
	}

	public void setChannelLifetime(int channelLifetime) {
		this.channelLifetime = UInteger.valueOf(channelLifetime);
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = UInteger.valueOf(connectTimeout);
	}

	public void setDiscoveryEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public void setForceCertificateValidator(boolean forceCertificateValidator) {
		this.forceCertificateValidator = forceCertificateValidator;
	}

	public void setKeepAliveTimeout(int keepAliveTimeout) {
		this.keepAliveTimeout = UInteger.valueOf(keepAliveTimeout);
	}

	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public void setMaxChunkCount(int maxChunkCount) {
		this.maxChunkCount = UInteger.valueOf(maxChunkCount);
	}

	public void setMaxChunkSize(int maxChunkSize) {
		this.maxChunkSize = UInteger.valueOf(maxChunkSize);
	}

	public void setMaxMessageSize(int maxMessageSize) {
		this.maxMessageSize = UInteger.valueOf(maxMessageSize);
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = UInteger.valueOf(requestTimeout);
	}

	public void setSecurityMode(String securityMode) {
		this.securityMode = MessageSecurityMode.valueOf(securityMode);
	}

	public void setSecurityPolicyUri(String securityPolicyUri) {
		this.securityPolicyUri = SecurityPolicy.valueOf(securityPolicyUri);
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = UInteger.valueOf(sessionTimeout);
	}

}
