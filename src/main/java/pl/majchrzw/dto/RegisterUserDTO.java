package pl.majchrzw.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import pl.majchrzw.validators.*;

@Data
@PasswordMatches
public class RegisterUserDTO {
	@NotNull
	@NotEmpty
	@Length(min = 6, max = 32)
	@UsernameTaken
	private String username;
	@NotNull
	@NotEmpty
	@PasswordValid
	private String password;
	@NotNull
	@NotEmpty
	private String matchingPassword;
	
	@NotNull
	@NotEmpty
	@EmailValid
	@EmailTaken
	private String email;
	
	private Boolean isUsing2FA;
}
