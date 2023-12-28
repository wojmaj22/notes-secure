package pl.majchrzw.user;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.majchrzw.dto.ChangePasswordDTO;
import pl.majchrzw.dto.RegisterUserDTO;
import pl.majchrzw.note.NoteRepository;
import pl.majchrzw.util.LoginAttemptService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	private final PasswordEncoder passwordEncoder;
	
	private final NoteRepository noteRepository;
	
	@Autowired
	private LoginAttemptService loginAttemptService;
	
	@Override
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		if( loginAttemptService.isBlocked()){
			throw new RuntimeException("This ip is blocked");
		}
		return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));
	}
	
	public User registerUser(RegisterUserDTO registerUserDTO){
		if( userRepository.existsByUsername(registerUserDTO.getUsername())){
			throw new RuntimeException("User already registered");
		}
		User user = User.builder()
				.enabled(true)
				.username(registerUserDTO.getUsername())
				.password(passwordEncoder.encode(registerUserDTO.getPassword()))
				.role(Role.USER)
				.build();
		if ( registerUserDTO.getIsUsing2FA()){
			user.setUsing2FA(true);
			user.setSecret(Base32.random());
		} else {
			user.setSecret("");
		}
		
		return userRepository.save(user);
	}
	
	public User changeUserPassword(String username, ChangePasswordDTO dto){
		User user = loadUserByUsername(username);
		user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
		return userRepository.save(user);
	}
	
	public void deleteUser(String username){
		userRepository.deleteByUsername(username);
		noteRepository.deleteAllByUsername(username);
	}
	
	public void enable2FA(User user){
		user.setUsing2FA(true);
		user.setSecret(Base32.random());
		userRepository.save(user);
	}
	
	public void disable2FA(User user){
		user.setUsing2FA(false);
		user.setSecret("");
		userRepository.save(user);
	}

	public String generateQRCode(User user) throws Exception{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(generateQRImage(user),"png", os);
		
		return Base64.getEncoder().encodeToString(os.toByteArray());
	}
	
	private BufferedImage generateQRImage(User user) throws Exception{
		String text = "otpauth://totp/notes:"+user.getUsername()+"?secret="+user.getSecret()+"&issuer=notes";
		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200);
		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}
}
