package pl.majchrzw.security;

import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.majchrzw.user.CustomUserDetailsService;
import pl.majchrzw.user.User;
import pl.majchrzw.user.UserRepository;


//@Component
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public Authentication authenticate(Authentication authentication){
		String verificationCode = ((CustomWebAuthenticationDetails) authentication.getDetails()).getVerificationCode();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		User user = userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new RuntimeException(""));
		if ( user.isUsing2FA()){
			Totp totp = new Totp(user.getSecret());
			if ( !totp.verify(verificationCode) || !isCodeValid(verificationCode)){
				throw new BadCredentialsException("Invalid username, password or verification code.");
			}
		}
		Authentication result = super.authenticate(authentication);
		return new UsernamePasswordAuthenticationToken(user, result.getCredentials(), result.getAuthorities());
	}
	
	private boolean isCodeValid(String code){
		try{
			Long.parseLong(code);
		} catch (NumberFormatException e){
			return false;
		}
		return true;
	}
	
	@Override
	public boolean supports(Class<?> authentication){
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}
}
