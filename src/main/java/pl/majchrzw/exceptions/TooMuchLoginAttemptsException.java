package pl.majchrzw.exceptions;

public class TooMuchLoginAttemptsException extends RuntimeException {

	public TooMuchLoginAttemptsException() {
		super();
	}
	
	public TooMuchLoginAttemptsException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public TooMuchLoginAttemptsException(final String message) {
		super(message);
	}
	

	public TooMuchLoginAttemptsException(final Throwable cause) {
		super(cause);
	}
}
