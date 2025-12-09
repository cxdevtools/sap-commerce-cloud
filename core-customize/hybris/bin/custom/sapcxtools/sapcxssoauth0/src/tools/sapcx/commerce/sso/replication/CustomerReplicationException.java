package tools.sapcx.commerce.sso.replication;

public class CustomerReplicationException extends RuntimeException {
	public CustomerReplicationException() {
	}

	public CustomerReplicationException(String message) {
		super(message);
	}

	public CustomerReplicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CustomerReplicationException(Throwable cause) {
		super(cause);
	}

	public CustomerReplicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
