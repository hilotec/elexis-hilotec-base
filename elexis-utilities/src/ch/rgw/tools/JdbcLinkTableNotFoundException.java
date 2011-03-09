package ch.rgw.tools;

@SuppressWarnings("serial")
public class JdbcLinkTableNotFoundException extends JdbcLinkException {

	public JdbcLinkTableNotFoundException(Exception cause) {
		super(cause);
	}

	public JdbcLinkTableNotFoundException(String message, Exception cause) {
		super(message, cause);
	}
}
