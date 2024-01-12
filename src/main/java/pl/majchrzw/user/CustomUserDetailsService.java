package pl.majchrzw.user;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.majchrzw.accountActivation.RegistrationToken;
import pl.majchrzw.accountActivation.RegistrationTokenRepository;
import pl.majchrzw.dto.RegisterFormDTO;
import pl.majchrzw.exceptions.TooMuchLoginAttemptsException;
import pl.majchrzw.note.NoteRepository;
import pl.majchrzw.security.LoginAttemptService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	private final PasswordEncoder passwordEncoder;
	
	private final NoteRepository noteRepository;
	
	private final RegistrationTokenRepository tokenRepository;
	
	private final JavaMailSender mailSender;
	
	@Autowired
	private LoginAttemptService loginAttemptService;
	
	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		if (loginAttemptService.isBlocked()) {
			throw new TooMuchLoginAttemptsException("This ip is blocked");
		}
		return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));
	}
	
	public User registerUser(RegisterFormDTO registerFormDTO) {
		User user = User.builder()
				.enabled(false)
				.username(registerFormDTO.getUsername())
				.email(registerFormDTO.getEmail())
				.password(passwordEncoder.encode(registerFormDTO.getPassword()))
				.role(Role.USER)
				.build();
		if (registerFormDTO.getIsUsing2FA()) {
			user.setUsing2FA(true);
			user.setSecret(Base32.random());
		} else {
			user.setSecret("");
		}
		User registeredUser = userRepository.save(user);
		RegistrationToken token = tokenRepository.save(new RegistrationToken(registeredUser.getUsername()));
		sendActivationEmail(token, registeredUser);
		
		return registeredUser;
	}
	
	public User changeUserPassword(String username, String password) {
		User user = loadUserByUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		return userRepository.save(user);
	}
	
	public void deleteUser(String username) {
		userRepository.deleteByUsername(username);
		noteRepository.deleteAllByUsername(username);
	}
	
	public void enable2FA(User user) {
		user.setUsing2FA(true);
		user.setSecret(Base32.random());
		userRepository.save(user);
	}
	
	public void disable2FA(User user) {
		user.setUsing2FA(false);
		user.setSecret("");
		userRepository.save(user);
	}
	
	public String generateQRCode(User user) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(generateQRImage(user), "png", os);
		} catch (IOException e) {
			return null;
		}
		
		return Base64.getEncoder().encodeToString(os.toByteArray());
	}
	
	private BufferedImage generateQRImage(User user) {
		String text = "otpauth://totp/notes:" + user.getUsername() + "?secret=" + user.getSecret() + "&issuer=notes";
		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix bitMatrix = null;
		try {
			bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200);
		} catch (WriterException e) {
			return null;
		}
		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}
	
	public boolean existsByEmail(String email){
		return userRepository.existsByEmail(email);
	}
	
	
	public boolean activateAccount(String token, String username){
		RegistrationToken registrationToken = tokenRepository.findByToken(token).orElse(null);
		if( registrationToken != null){
			if ( registrationToken.isValid(username)){
				User user = loadUserByUsername(username);
				user.setEnabled(true);
				userRepository.save(user);
				return true;
			}
		}
		return false;
	}
	
	public void sendActivationEmail(RegistrationToken token, User user){
		String msgBody = "Kliknij w link aby aktywować konto - https://localhost:8443/activate?token=" + token.getToken() + "&user=" + user.getUsername() + "\n Jeżeli link nie działa zmień 'localhost' na poprawny adres aplikacji";

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setSubject("Aktywuj konto");
		mailMessage.setText(msgBody);
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("wojjmaj22@gmail.com");
		
		mailSender.send(mailMessage);
	}
}
