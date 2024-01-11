package pl.majchrzw.accountActivation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class RegistrationToken {
	
	private static final int EXPIRATION = 24; // expiration in 60 minutes
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(unique = true)
	private String token;
	
	private String username;
	
	private LocalDateTime expiryDate;
	
	public RegistrationToken(String username){
		this.username = username;
		token = UUID.randomUUID().toString();
		LocalDateTime now = LocalDateTime.now();
		expiryDate = now.plusHours(EXPIRATION);
	}
	
	public boolean isValid(String username){
		return expiryDate.isAfter(LocalDateTime.now()) && username.equals(this.username);
	}
}
