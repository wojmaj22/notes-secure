package pl.majchrzw.passwordReset;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.majchrzw.dto.EmailFormDTO;
import pl.majchrzw.dto.ResetPasswordFormDTO;
import pl.majchrzw.user.CustomUserDetailsService;

import java.util.Random;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {
	
	private final PasswordResetService tokenService;
	
	private final CustomUserDetailsService userDetailsService;
	
	private Random random = new Random();
	
	@GetMapping("/password-reset")
	public String getResetPasswordForm(Model model){

		model.addAttribute("form", new EmailFormDTO());
		return "forgot-password/email-form";
		
	}
	
	@PostMapping("/password-reset")
	public String sendEmailForReset(@Valid @ModelAttribute("form") EmailFormDTO form, BindingResult result, Model model){
		// TODO - sprawdzić czy opóźnienie jest takie same
		if ( result.hasErrors()){
			return "forgot-password/email-form";
		}

		if (userDetailsService.existsByEmail((form.getEmail()))) {
			tokenService.generateTokenAndSendEmail(form.getEmail());
		} else {
			try {
				Thread.sleep(random.nextInt(1300, 1600));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		model.addAttribute("message", "Jeżeli email jest powiązany z kontem to została wysłana wiadomość z linkiem do zmiany hasła");
		return "message";
	}
	
	@GetMapping("/password-reset-form")
	public String getInputPasswordResetForm(@RequestParam String token, Model model){
		PasswordResetToken resetToken = tokenService.getResetToken(token);
		if ( resetToken != null){
			if ( resetToken.isNotExpired()){
				model.addAttribute("form", new ResetPasswordFormDTO(resetToken.getToken()));
				return "forgot-password/password-form";
			}
		}
		return "redirect:/login";
	}
	
	@PostMapping("password-reset-form")
	public String handlePasswordChangeForm(@Valid @ModelAttribute("form") ResetPasswordFormDTO form, BindingResult result, Model model){
		if ( !form.getPassword().equals(form.getMatchingPassword())){
			ObjectError error = new ObjectError("globalError", "Podane hasła są różne");
			result.addError(error);
		}
		if ( result.hasErrors()){
			return "forgot-password/password-form";
		}
		PasswordResetToken resetToken = tokenService.getResetToken(form.getToken());
		if ( resetToken != null){
			if ( resetToken.isNotExpired()){
				userDetailsService.changeUserPassword(resetToken.getUsername(), form.getPassword());
				tokenService.discardToken(resetToken);
			}
		}
		model.addAttribute("message", "Zaktualizowano hasło, zaloguj się na swoje konto");
		return "message";
	}
	
}
