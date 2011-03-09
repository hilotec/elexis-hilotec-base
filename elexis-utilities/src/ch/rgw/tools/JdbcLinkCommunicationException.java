package ch.rgw.tools;

@SuppressWarnings("serial")
public class JdbcLinkCommunicationException extends JdbcLinkException {

	public JdbcLinkCommunicationException(Exception cause) {
		super(cause);
	}

	public JdbcLinkCommunicationException(String message, Exception cause) {
		super(message, cause);
	}

}
