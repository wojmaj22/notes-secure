package pl.majchrzw.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EmailValidator implements ConstraintValidator<EmailValid, String> {
	@Override
	public void initialize(EmailValid constraintAnnotation) {
	}
	
	@Override
	public boolean isValid(String email, ConstraintValidatorContext context) {
		
		String regex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
		
		Pattern p = Pattern.compile(regex);
		
		if (email == null) {
			return false;
		}
		
		Matcher m = p.matcher(email);
		
		if (m.matches()) {
			return true;
		}
		
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate("Wpisz poprawny email")
				.addConstraintViolation();
		return false;
	}
	
}
