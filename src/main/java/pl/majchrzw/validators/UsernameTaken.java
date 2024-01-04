package pl.majchrzw.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = UsernameTakenValidator.class)
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface UsernameTaken {
	
	String message() default "Nazwa jest już zajęta";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
