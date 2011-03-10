package ch.rgw.tools;

import java.sql.SQLException;

public class JdbcLinkExceptionTranslation {

	public static JdbcLinkException translateException(Exception ex) {
		if(ex instanceof SQLException) {
			return translateSQLException(null, (SQLException)ex);
		}
		return new JdbcLinkException(null, ex); 
	}

	public static JdbcLinkException translateException(String message, Exception ex) {
		if(ex instanceof SQLException) {
			return translateSQLException(message, (SQLException)ex);
		}
		return new JdbcLinkException(message, ex); 
	}
	
	private static JdbcLinkException translateSQLException(String message, SQLException sql) {
		String state = sql.getSQLState();
		
		if("42S02".equalsIgnoreCase(state)) {
			return new JdbcLinkTableNotFoundException(message + " (SQLState: " + state + ")", sql);
		} else if("08S01".equalsIgnoreCase(state)) {
			return new JdbcLinkCommunicationException(message + " (SQLState: " + state + ")", sql);
		}
		
		return new JdbcLinkException(message + " (SQLState: " + state + ")", sql);
	}
}
