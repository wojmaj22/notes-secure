package pl.majchrzw.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.majchrzw.dto.ChangePasswordDTO;
import pl.majchrzw.dto.ChangeTotpDTO;
import pl.majchrzw.dto.RegisterUserDTO;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class UserController {
	
	private final CustomUserDetailsService userService;
	
	@GetMapping("/register")
	public String register(Model model){
		model.addAttribute("form", new RegisterUserDTO());
		return "register";
	}
	
	@PostMapping("/register")
	public String processRegister(@ModelAttribute @Valid RegisterUserDTO userDTO, Model model, BindingResult result) throws Exception{
		if( !userDTO.getPassword().equals(userDTO.getMatchingPassword()) || result.hasErrors()){
			model.addAttribute("error", "Password does not match!");
			return "redirect:/register";
			// TODO - tutaj chyba zmienić sposób w jaki jest pokazywane że hasła są różne
		}
		User user = userService.registerUser(userDTO);
		if ( userDTO.getIsUsing2FA()){
			model.addAttribute("qr", userService.generateQRCode(user));
			return "qrcode";
		} else {
			return "redirect:/login";
		}
	}
	
	@GetMapping("/manage")
	public String manage(Model model, Principal principal){
		model.addAttribute("passwordForm", new ChangePasswordDTO());
		ChangeTotpDTO totpDTO = new ChangeTotpDTO();
		totpDTO.setIsEnabledTotp(userService.loadUserByUsername(principal.getName()).isUsing2FA());
		model.addAttribute("totpForm", totpDTO);
		model.addAttribute("username", principal.getName());
		return "manage";
	}
	
	@GetMapping("/login")
	String login() {
		return "login";
	}
	
	
	@PostMapping("/change-password")
	public String changePassword(Principal principal, @ModelAttribute @Valid ChangePasswordDTO dto, BindingResult result){
		if(!dto.getNewPassword().equals(dto.getNewPasswordRepeat()) || result.hasErrors()){
			return "redirect:/manage";
		} else {
			userService.changeUserPassword(principal.getName(), dto);
			return "redirect:/notes";
		}
	}
	
	@PostMapping("/change-totp")
	public String changeTotp(Principal principal, @ModelAttribute ChangeTotpDTO dto, Model model) throws Exception {
		User user = userService.loadUserByUsername(principal.getName());
		if ( dto.getIsEnabledTotp() && !user.isUsing2FA()){
			// włączyć
			userService.enable2FA(user);
			model.addAttribute("qr", userService.generateQRCode(user));
			return "qrcode";
		} else if ( !dto.getIsEnabledTotp() && user.isUsing2FA()){
			// wyłączyć
			userService.disable2FA(user);
			return "redirect:/notes";
		}
		// inaczej nic nie robić
		return "redirect:/notes";
	}
	
	@GetMapping("/delete-account")
	public String deleteAccount(Principal principal, Authentication authentication){
		userService.deleteUser(principal.getName());
		authentication.setAuthenticated(false);
		return "redirect:/";
	}
	
	
}
