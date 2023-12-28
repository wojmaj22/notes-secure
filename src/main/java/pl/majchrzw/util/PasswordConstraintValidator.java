package pl.majchrzw.util;

import com.google.common.base.Joiner;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;

import java.util.Arrays;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {
	
	@Override
	public void initialize(ValidPassword arg0) {
	}
	
	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		PasswordValidator validator = new PasswordValidator(Arrays.asList(
				new LengthRule(8,32),
				new CharacterRule(EnglishCharacterData.UpperCase,1),
				new CharacterRule(EnglishCharacterData.LowerCase,1),
				new CharacterRule(EnglishCharacterData.Digit,1),
				new CharacterRule(EnglishCharacterData.Special,1),
				new WhitespaceRule()));
		
		RuleResult result = validator.validate(new PasswordData(password));
		if (result.isValid()) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(
						Joiner.on(",").join(validator.getMessages(result)))
				.addConstraintViolation();
		return false;
	}
}
