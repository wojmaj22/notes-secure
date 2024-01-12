package pl.majchrzw.security;

import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import pl.majchrzw.user.User;
import pl.majchrzw.user.UserRepository;

import java.util.Random;


public class CustomAuthenticationProvider extends DaoAuthenticationProvider {
	
	private final Random random = new Random();
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public Authentication authenticate(Authentication authentication) {
		String verificationCode = ((CustomWebAuthenticationDetails) authentication.getDetails()).getVerificationCode();
		try {
			Thread.sleep(random.nextInt(120, 200));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		User user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new BadCredentialsException("Invalid username, password or verification code."));
		if (user.isUsing2FA()) {
			Totp totp = new Totp(user.getSecret());
			if (!totp.verify(verificationCode) || !isCodeValid(verificationCode)) {
				throw new BadCredentialsException("Invalid username, password or verification code.");
			}
		}
		Authentication result = super.authenticate(authentication);
		return new UsernamePasswordAuthenticationToken(user, result.getCredentials(), result.getAuthorities());
	}
	
	private boolean isCodeValid(String code) {
		try {
			Long.parseLong(code);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
