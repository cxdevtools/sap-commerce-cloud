package me.cxdev.cpi.generic.outbound;

import me.cxdev.cpi.generic.remoteclient.ScpiRemoteClientFactory;

public abstract class AbstractScpiOutboundStrategy {

	private ScpiRemoteClientFactory remoteClientFactory;
	private String destinationId;

	public void setRemoteClientFactory(ScpiRemoteClientFactory remoteClientFactory) {
		this.remoteClientFactory = remoteClientFactory;
	}

	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}

	public ScpiRemoteClientFactory getRemoteClientFactory() {
		return remoteClientFactory;
	}

	public String getDestinationId() {
		return destinationId;
	}
}
