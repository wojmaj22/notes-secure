package pl.majchrzw.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import pl.majchrzw.util.ValidPassword;

@Data
public class RegisterUserDTO {
	@NotNull
	@NotEmpty
	@Length(min = 6)
	private String username;
	@NotNull
	@NotEmpty
	@ValidPassword
	private String password;
	@NotNull
	@NotEmpty
	private String matchingPassword;
	
	private Boolean isUsing2FA;
}
