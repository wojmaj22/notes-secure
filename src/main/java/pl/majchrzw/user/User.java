package pl.majchrzw.user;

import jakarta.persistence.*;
import lombok.*;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.majchrzw.passwordreset.PasswordResetToken;

import java.util.Collection;

@Setter
@Getter
@Builder
@Table(name = "users")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(unique = true)
	private String username;
	
	@Column(unique = true)
	private String email;
	
	private String password;
	
	private boolean enabled;
	
	private boolean isUsing2FA;
	
	private String secret;
	
	private Role role;
	
	@OneToOne(mappedBy = "user")
	private PasswordResetToken passwordResetToken;
	
	@OneToOne(mappedBy = "user")
	private PasswordResetToken activateAccountToken;
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return role.getAuthorities();
	}
	
	@Override
	public String getPassword() {
		return password;
	}
	
	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
