package pl.majchrzw.exceptions;

public class TooMuchLoginAttemptsException extends RuntimeException {
	
	public TooMuchLoginAttemptsException(final String message) {
		super(message);
	}
	
}
