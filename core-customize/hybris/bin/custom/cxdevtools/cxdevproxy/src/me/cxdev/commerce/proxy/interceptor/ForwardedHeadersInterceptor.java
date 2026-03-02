package me.cxdev.commerce.proxy.interceptor;

import java.net.InetSocketAddress;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor that ensures the {@code X-Forwarded-*} headers are correctly populated
 * on the incoming HTTP exchange before it is routed to the target system.
 * <p>
 * If the headers (Proto, Host, Port) are already provided by an upstream proxy or load balancer,
 * they are preserved. Additionally, the original client's IP address is appended to the
 * {@code X-Forwarded-For} header. This is a crucial requirement for SAP Commerce (Tomcat)
 * to correctly resolve absolute URLs, avoid redirect loops, and determine the security context.
 * </p>
 */
public class ForwardedHeadersInterceptor implements ProxyExchangeInterceptor {
	private static final Logger LOG = LoggerFactory.getLogger(ForwardedHeadersInterceptor.class);

	private String serverProtocol;
	private String serverHostname;
	private int serverPort;

	/**
	 * Injects the required forwarded headers into the current HTTP exchange.
	 * Evaluates existing headers and falls back to server configuration or
	 * the incoming {@code Host} header if necessary.
	 *
	 * @param exchange The current HTTP server exchange being processed.
	 */
	@Override
	public void apply(HttpServerExchange exchange) {
		// 1. Resolve and set X-Forwarded-Proto
		String existingProto = exchange.getRequestHeaders().getFirst(Headers.X_FORWARDED_PROTO);
		if (StringUtils.isBlank(existingProto)) {
			exchange.getRequestHeaders().put(Headers.X_FORWARDED_PROTO, this.serverProtocol);
		}

		// 2. Resolve and set X-Forwarded-Host & X-Forwarded-Port
		String existingHost = exchange.getRequestHeaders().getFirst(Headers.X_FORWARDED_HOST);
		String existingPort = exchange.getRequestHeaders().getFirst(Headers.X_FORWARDED_PORT);

		if (StringUtils.isBlank(existingHost) || StringUtils.isBlank(existingPort)) {
			String hostHeader = exchange.getRequestHeaders().getFirst(Headers.HOST);
			String forwardedHost = this.serverHostname;
			int forwardedPort = this.serverPort;

			if (StringUtils.isNotBlank(hostHeader)) {
				if (hostHeader.contains(":")) {
					String[] parts = hostHeader.split(":");
					forwardedHost = parts[0];
					try {
						forwardedPort = Integer.parseInt(parts[1]);
					} catch (NumberFormatException e) {
						LOG.warn("Could not parse port from Host header: '{}'. Falling back to protocol default.", hostHeader);
						forwardedPort = "https".equalsIgnoreCase(this.serverProtocol) ? 443 : 80;
					}
				} else {
					forwardedHost = hostHeader;
					forwardedPort = "https".equalsIgnoreCase(this.serverProtocol) ? 443 : 80;
				}
			}

			if (StringUtils.isBlank(existingHost)) {
				exchange.getRequestHeaders().put(Headers.X_FORWARDED_HOST, forwardedHost);
			}
			if (StringUtils.isBlank(existingPort)) {
				exchange.getRequestHeaders().put(Headers.X_FORWARDED_PORT, String.valueOf(forwardedPort));
			}
		}

		// 3. Append client IP to X-Forwarded-For
		InetSocketAddress sourceAddress = exchange.getSourceAddress();
		if (sourceAddress != null && sourceAddress.getAddress() != null) {
			String clientIp = sourceAddress.getAddress().getHostAddress();
			String existingFor = exchange.getRequestHeaders().getFirst(Headers.X_FORWARDED_FOR);

			if (StringUtils.isNotBlank(existingFor)) {
				// Append to the standard HTTP comma-separated list if multiple proxies are involved
				exchange.getRequestHeaders().put(Headers.X_FORWARDED_FOR, existingFor + ", " + clientIp);
			} else {
				exchange.getRequestHeaders().put(Headers.X_FORWARDED_FOR, clientIp);
			}
		}
	}

	public void setServerProtocol(String serverProtocol) {
		this.serverProtocol = serverProtocol;
	}

	public void setServerHostname(String serverHostname) {
		this.serverHostname = serverHostname;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
}
