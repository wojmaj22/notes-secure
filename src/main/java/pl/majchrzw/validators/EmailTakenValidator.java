package pl.majchrzw.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.majchrzw.user.UserRepository;

public class EmailTakenValidator  implements ConstraintValidator<EmailTaken, String> {
	
	@Autowired
	private UserRepository repository;
	
	@Override
	public void initialize(EmailTaken constraintAnnotation) {
	}
	
	@Override
	public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
		return !repository.existsByEmail(email);
	}
}
