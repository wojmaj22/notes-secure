package pl.majchrzw.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.majchrzw.user.UserRepository;


public class UsernameTakenValidator implements ConstraintValidator<UsernameTaken, String> {
	
	@Autowired
	private UserRepository repository;
	
	@Override
	public void initialize(UsernameTaken constraintAnnotation) {
	}
	
	@Override
	public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
		return !repository.existsByEmail(email);
	}
}
