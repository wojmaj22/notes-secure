package pl.majchrzw.util;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.majchrzw.exceptions.TooMuchLoginAttemptsException;

@ControllerAdvice
public class ExceptionResolver {
	
	@ExceptionHandler(TooMuchLoginAttemptsException.class)
	public String handleLoginAttemptsException(TooMuchLoginAttemptsException exception, Model model) {
		model.addAttribute("error", exception.getMessage());
		return "error";
	}
	
}
