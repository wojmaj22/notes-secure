package pl.majchrzw.accountactivation;

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
@Table(name = "register_token")
@NoArgsConstructor
public class RegistrationToken {
	
	private static final int EXPIRATION = 24; // expiration in 60 minutes
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(unique = true)
	private String token;
	
	@OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
	@JoinColumn(nullable = false, name = "user_id")
	private User user;
	
	private LocalDateTime expiryDate;
	
	public RegistrationToken(User user){
		this.user = user;
		token = UUID.randomUUID().toString();
		LocalDateTime now = LocalDateTime.now();
		expiryDate = now.plusHours(EXPIRATION);
	}
	
	public boolean isValid(String username){
		return expiryDate.isAfter(LocalDateTime.now()) && username.equals(user.getUsername());
	}
}
