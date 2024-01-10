package pl.majchrzw.passwordreset;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.majchrzw.user.User;
import pl.majchrzw.user.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
	
	private final PasswordResetTokenRepository tokenRepository;
	
	private final UserRepository userRepository;
	
	private final JavaMailSender mailSender;
	
	public void generateTokenAndSendEmail(String email){
		User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("No user with email:"+email+", has been found."));
		PasswordResetToken token = tokenRepository.save(new PasswordResetToken(user));
		// TODO - dodać tutaj jakieś lepsze body tej wiadomości
		String msgBody = "Kliknij w link aby przejść do odzyskiwania hasła: https://localhost:8443/password-reset-form?token=" + token.getToken() + "\nJeżeli link nie działa zmień 'localhost' na poprawny adres aplikacji";
		// TODO - może spróbować tutaj dać po prostu event - reset password i wysyłać tego mail asynchronicznie
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setSubject("Zresetuj swoje hasło");
		mailMessage.setText(msgBody);
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("wojjmaj22@gmail.com");
		
		mailSender.send(mailMessage);
	}
	
	public PasswordResetToken getResetToken(String token){
		return tokenRepository.findByToken(token).orElse(null);
	}
	
	public boolean isTokenValid(String token){
		Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
		
		return resetToken.map(PasswordResetToken::isNotExpired).orElse(false);
	}
	
	public void discardToken(PasswordResetToken token){
		tokenRepository.delete(token);
	}
}
