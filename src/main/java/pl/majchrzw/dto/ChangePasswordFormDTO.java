package pl.majchrzw.dto;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.majchrzw.validators.PasswordValid;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordFormDTO {
	@Nonnull
	@NotEmpty
	private String oldPassword;
	
	@Nonnull
	@NotEmpty
	@PasswordValid
	private String newPassword;
	
	@Nonnull
	@NotEmpty
	private String newPasswordRepeat;
}
