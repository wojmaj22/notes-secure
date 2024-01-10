package pl.majchrzw.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pl.majchrzw.validators.PasswordMatches;
import pl.majchrzw.validators.PasswordValid;

@Data
@PasswordMatches
public class ResetPasswordFormDTO {
	
	@NotNull
	@NotEmpty
	@PasswordValid
	private String password;
	@NotNull
	@NotEmpty
	private String matchingPassword;
	
	private String token;
	
	public ResetPasswordFormDTO(String token){
		this.token = token;
	}
}
