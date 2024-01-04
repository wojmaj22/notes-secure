package pl.majchrzw.passwordreset;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import pl.majchrzw.user.User;


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
	
	@OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private User user;
	
	private LocalDateTime expiryDate;
	
	public PasswordResetToken(User user){
		this.user = user;
		token = UUID.randomUUID().toString();
		LocalDateTime now = LocalDateTime.now();
		expiryDate = now.minusMinutes(EXPIRATION);
	}
	
	public boolean isNotExpired(){
		return expiryDate.isBefore(LocalDateTime.now());
	}
}
