package pl.majchrzw.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.majchrzw.devices.DeviceService;
import pl.majchrzw.dto.ChangePasswordFormDTO;
import pl.majchrzw.dto.ChangeTotpFormDTO;
import pl.majchrzw.dto.RegisterFormDTO;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class UserController {
	
	private final CustomUserDetailsService userService;
	
	private final DeviceService deviceService;
	
	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("form", new RegisterFormDTO());
		return "register";
	}
	
	@PostMapping("/register")
	public String processRegister(@Valid @ModelAttribute("form") RegisterFormDTO form, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			return "register";
		}
		User user = userService.registerUser(form);
		
		if (form.getIsUsing2FA()) {
			model.addAttribute("qr", userService.generateQRCode(user));
			return "qrcode";
		} else {
			model.addAttribute("message", "Zostałeś poprawnie zarejestrowany, sprawdź maila aby aktywować konto");
			return "message";
		}
	}
	
	@GetMapping("/manage")
	public String manage(Model model, Principal principal) {
		model.addAttribute("passwordForm", new ChangePasswordFormDTO());
		ChangeTotpFormDTO totpDTO = new ChangeTotpFormDTO();
		totpDTO.setIsEnabledTotp(userService.loadUserByUsername(principal.getName()).isUsing2FA());
		model.addAttribute("totpForm", totpDTO);
		model.addAttribute("devices", deviceService.getDevicesByUsername(principal.getName()));
		model.addAttribute("username", principal.getName());
		return "manage";
	}
	
	@GetMapping("/login")
	String login() {
		return "login";
	}
	
	
	@PostMapping("/change-password")
	public String changePassword(Principal principal, @ModelAttribute @Valid ChangePasswordFormDTO dto, BindingResult result) {
		if (!dto.getNewPassword().equals(dto.getNewPasswordRepeat()) || result.hasErrors()) {
			return "redirect:/manage";
		} else {
			userService.changeUserPassword(principal.getName(), dto.getNewPassword());
			return "redirect:/notes";
		}
	}
	
	@PostMapping("/change-totp")
	public String changeTotp(Principal principal, @ModelAttribute ChangeTotpFormDTO dto, Model model) {
		User user = userService.loadUserByUsername(principal.getName());
		if (dto.getIsEnabledTotp() && !user.isUsing2FA()) {
			// włączyć
			userService.enable2FA(user);
			model.addAttribute("qr", userService.generateQRCode(user));
			return "qrcode";
		} else if (!dto.getIsEnabledTotp() && user.isUsing2FA()) {
			// wyłączyć
			userService.disable2FA(user);
			return "redirect:/notes";
		}
		// inaczej nic nie robić
		return "redirect:/notes";
	}
	
	@GetMapping("/delete-account")
	public String confirm() {
		return "confirm";
	}
	
	@GetMapping("/confirm-delete")
	public String deleteAccount(Principal principal, Authentication authentication, HttpServletRequest request) throws ServletException {
		userService.deleteUser(principal.getName());
		request.logout();
		authentication.setAuthenticated(false);
		return "redirect:/";
	}
	
	@GetMapping("/activate")
	public String activateAccount(@RequestParam("token") String token, Model model, @RequestParam("user") String username){
		if( userService.activateAccount(token, username)){
			model.addAttribute("message", "Konto zostało aktywowane możesz się zalogować");
			return "message";
		}
		return "redirect:/login";
	}
	
	@GetMapping("/teapot")
	@ResponseBody
	public ResponseEntity<String> teapot(){
		return new ResponseEntity<>("I’m a teapot.", HttpStatus.I_AM_A_TEAPOT);
	}

}
