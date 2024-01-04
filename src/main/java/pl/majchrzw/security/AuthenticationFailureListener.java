package pl.majchrzw.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener {
	
	private final HttpServletRequest request;
	private final LoginAttemptService loginAttemptService;
	
	@EventListener
	public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
		final String xFHeader = request.getHeader("X-Forwarded-For");
		if (xFHeader == null || xFHeader.equals("") || !xFHeader.contains(request.getRemoteAddr())) {
			loginAttemptService.loginFailed(request.getRemoteAddr());
		} else {
			loginAttemptService.loginFailed(xFHeader.split(",")[0]);
		}
		
	}
}
