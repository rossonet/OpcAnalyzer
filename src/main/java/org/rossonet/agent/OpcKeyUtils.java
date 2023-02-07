package org.rossonet.agent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrea Ambrosini
 *
 */
public class OpcKeyUtils {

	public static final String APPLICATION_URI = "urn:rossonet:bridge:opc-ua:client";

	private static final String CLIENT_ALIAS = "Rossonet connector";

	public static final String COMMON_NAME = "Rossonet OPC-UA " + UUID.randomUUID().toString();

	public static final String COUNTRY = "IT";

	public static final String DNS = "localhost";

	public static final String IP = "127.0.0.1";

	private static final Pattern IP_ADDR_PATTERN = Pattern
			.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

	private static final String KEYSTORE_PASSWORD = "password123.";

	private static final String keyStorePassword = KEYSTORE_PASSWORD;

	public static final String LOCALITY = "Imola";

	private final static Logger logger = LoggerFactory.getLogger(OpcKeyUtils.class);

	public static final String ORGANIZATION = "Rossonet s.c.a r.l.";

	private static final char[] PASSWORD = keyStorePassword.toCharArray();

	public static final String STATE = "Emilia Romagna";

	public static final String UNIT = "Java dev";

	private static String getDefaultCharSet() {
		final OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
		final String enc = writer.getEncoding();
		return enc;
	}

	private X509Certificate clientCertificate = null;
	private X509Certificate[] clientCertificateChain = new X509Certificate[0];

	private KeyPair clientKeyPair = null;

	private PrivateKey privateKey = null;

	public OpcKeyUtils create() {
		return create(COMMON_NAME, ORGANIZATION, UNIT, LOCALITY, STATE, COUNTRY, APPLICATION_URI, DNS, IP);
	}

	public OpcKeyUtils create(final String commonName, final String organization, final String unit,
			final String locality, final String state, final String country, final String uri, final String dns,
			final String ip) {
		try {
			final KeyStore keyStore = KeyStore.getInstance("PKCS12");
			final File serverKeyStore = new File("keystore.pfx");
			if (!serverKeyStore.exists()) {
				logger.debug("CERTIFICATE GENERATION");
				keyStore.load(null, PASSWORD);

				final KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);

				final SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
						.setCommonName(commonName).setOrganization(organization).setOrganizationalUnit(unit)
						.setLocalityName(locality).setStateName(state).setCountryCode(country).setApplicationUri(uri)
						.addDnsName(dns).addIpAddress(ip);

				for (final String hostname : HostnameUtil.getHostnames("0.0.0.0")) {
					if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
						builder.addIpAddress(hostname);
					} else {
						builder.addDnsName(hostname);
					}
				}

				final X509Certificate certificate = builder.build();

				keyStore.setKeyEntry(CLIENT_ALIAS, keyPair.getPrivate(), PASSWORD,
						new X509Certificate[] { certificate });
				keyStore.store(new FileOutputStream(serverKeyStore), PASSWORD);
			} else {
				logger.debug("LOAD CERTIFICATE");
				keyStore.load(new FileInputStream(serverKeyStore), PASSWORD);
			}

			final Key serverPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD);

			if (serverPrivateKey instanceof PrivateKey) {
				clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);
				final PublicKey serverPublicKey = clientCertificate.getPublicKey();

				clientCertificateChain = new X509Certificate[] { clientCertificate };

				logger.debug("\n\n-----BEGIN CERTIFICATE-----\n"
						+ Base64.getEncoder().encodeToString(clientCertificate.getEncoded())
						+ "\n-----END CERTIFICATE-----\n\n");
				logger.debug("\n\n-----BEGIN RSA PRIVATE KEY-----\n"
						+ Base64.getEncoder().encodeToString(serverPrivateKey.getEncoded())
						+ "\n-----END RSA PRIVATE KEY-----\n\n");

				clientKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
				privateKey = (PrivateKey) serverPrivateKey;
			}
		} catch (final Exception df) {
			logger.error("error in key generation", df);
		}
		return this;
	}

	public X509Certificate getClientCertificate() {
		return clientCertificate;
	}

	public String getClientCertificateBase64() throws CertificateEncodingException {
		return Base64.getEncoder().encodeToString(clientCertificate.getEncoded());
	}

	public X509Certificate[] getClientCertificateChain() {
		return clientCertificateChain;
	}

	public KeyPair getClientKeyPair() {
		return clientKeyPair;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public String getPrivateKeyBase64() {
		return Base64.getEncoder().encodeToString(privateKey.getEncoded());
	}

	public boolean setClientKeyPair(final String key, final String crt) {
		logger.info("Default Charset=" + Charset.defaultCharset());
		logger.info("Default Charset in Use=" + getDefaultCharSet());
		logger.trace("checking cert/key\nkey:" + key + "\ncrt:" + crt);

		boolean result = false;
		try {
			final KeyStore keyStore = KeyStore.getInstance("PKCS12");
			final File keystoreFile = new File("keystore.pfx");
			keystoreFile.deleteOnExit();
			keyStore.load(null, PASSWORD);

			logger.debug("CERTIFICATE FROM CONFIGURATION");

			final String privateKeyContent = key;
			final String crtContent = crt;

			final KeyFactory kf = KeyFactory.getInstance("RSA");

			final byte[] decodedCrt = Base64.getDecoder().decode(crtContent);
			clientCertificate = (X509Certificate) CertificateFactory.getInstance("X.509")
					.generateCertificate(new ByteArrayInputStream(decodedCrt));
			final PublicKey pubKey = clientCertificate.getPublicKey();

			final byte[] decodedKey = Base64.getDecoder().decode(privateKeyContent);
			final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
			final PrivateKey privKey = kf.generatePrivate(keySpec);

			clientKeyPair = new KeyPair(pubKey, privKey);
			privateKey = privKey;
			clientCertificateChain = new X509Certificate[] { clientCertificate };
			keyStore.setKeyEntry(CLIENT_ALIAS, clientKeyPair.getPrivate(), PASSWORD, clientCertificateChain);

			keyStore.store(new FileOutputStream(keystoreFile), PASSWORD);

			result = true;
		} catch (final Exception df) {
			result = false;
			logger.error("error in select keypair", df);
			logger.error("checking cert/key\nkey:xxxx" + "\ncrt:" + crt);
		}
		return result;
	}

}
