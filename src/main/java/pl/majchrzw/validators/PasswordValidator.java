package pl.majchrzw.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<PasswordValid, String> {
	
	@Override
	public void initialize(PasswordValid arg0) {
	}
	
	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		
		String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!^&+=,.()*])(?=\\S+$).{8,64}$";
		
		Pattern p = Pattern.compile(regex);
		
		if (password == null) {
			return false;
		}
		
		Matcher m = p.matcher(password);
		
		if (m.matches()) {
			return true;
		}
		
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate("Hasło nie spełnia standardów bezpieczeństwa")
				.addConstraintViolation();
		return false;
	}
}
