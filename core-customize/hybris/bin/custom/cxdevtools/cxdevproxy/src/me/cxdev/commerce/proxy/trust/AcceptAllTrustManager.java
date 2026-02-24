package me.cxdev.commerce.proxy.trust;

import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

public class AcceptAllTrustManager extends X509ExtendedTrustManager {
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}

	@Override
	public void checkClientTrusted(X509Certificate[] certs, String authType) {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] certs, String authType) {
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
	}
}
