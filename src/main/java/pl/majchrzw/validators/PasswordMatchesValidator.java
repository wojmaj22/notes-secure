package pl.majchrzw.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.majchrzw.dto.RegisterFormDTO;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
	
	@Override
	public void initialize(PasswordMatches constraintAnnotation) {
	}
	
	@Override
	public boolean isValid(Object obj, ConstraintValidatorContext context) {
		RegisterFormDTO user = (RegisterFormDTO) obj;
		return user.getPassword().equals(user.getMatchingPassword());
	}
}
