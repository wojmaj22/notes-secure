package pl.majchrzw.passwordreset;


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
@Table(name = "reset_token")
public class PasswordResetToken {
	
	private static final int EXPIRATION = 60; // expiration in 60 minutes
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(unique = true)
	private String token;
	

	private String username;
	
	private LocalDateTime expiryDate;
	
	public PasswordResetToken(String user){
		this.username = user;
		token = UUID.randomUUID().toString();
		LocalDateTime now = LocalDateTime.now();
		expiryDate = now.plusMinutes(EXPIRATION);
	}
	
	public boolean isNotExpired(){
		return expiryDate.isAfter(LocalDateTime.now());
	}
}
