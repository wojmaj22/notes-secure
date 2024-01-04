package pl.majchrzw.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import pl.majchrzw.validators.PasswordMatches;
import pl.majchrzw.validators.UsernameTaken;
import pl.majchrzw.validators.ValidPassword;

@Data
@PasswordMatches
public class RegisterUserDTO {
	@NotNull
	@NotEmpty
	@Length(min = 6, max = 32)
	@UsernameTaken()
	private String username;
	@NotNull
	@NotEmpty
	@ValidPassword()
	private String password;
	@NotNull
	@NotEmpty
	private String matchingPassword;
	
	private Boolean isUsing2FA;
}
