package pl.majchrzw.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.majchrzw.dto.RegisterUserDTO;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
	
	@Override
	public void initialize(PasswordMatches constraintAnnotation) {
	}
	
	@Override
	public boolean isValid(Object obj, ConstraintValidatorContext context) {
		RegisterUserDTO user = (RegisterUserDTO) obj;
		return user.getPassword().equals(user.getMatchingPassword());
	}
}
