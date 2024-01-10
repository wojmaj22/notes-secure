package pl.majchrzw.note;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pl.majchrzw.dto.NoteDTO;
import pl.majchrzw.dto.PasswordFormDTO;

import javax.crypto.BadPaddingException;
import java.security.InvalidKeyException;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class NoteController {
	
	private final NoteService noteService;
	
	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("notes", noteService.getPublicNotes());
		return "index";
	}
	
	
	@GetMapping("/notes")
	public String notes(Model model, Principal principal) {
		model.addAttribute("notes", noteService.getNotesByUsername(principal.getName()));
		model.addAttribute("username", principal.getName());
		return "notes";
	}
	
	@GetMapping("/notes/add")
	public String addNote(Model model) {
		model.addAttribute("note", new NoteDTO());
		return "add";
	}
	
	@PostMapping("/notes/add")
	public String saveNote(@Valid @ModelAttribute NoteDTO noteDTO, BindingResult bindingResult, Principal principal, Model model) throws Exception {
		if ( bindingResult.hasErrors()){
			return "add";
		}
		noteDTO.setUsername(principal.getName());
		try {
			noteService.saveNote(noteDTO);
		} catch (IllegalArgumentException exception) {
			model.addAttribute("error", "Nie możesz ustawić szyfrowanej notatki jako publicznej");
			return "error";
		}
		return "redirect:/notes";
	}
	
	@GetMapping("/notes/details/{id}")
	public String noteDetails(@PathVariable Integer id, Model model, Principal principal) throws Exception {
		Note note;
		try{
			note = noteService.getNote(id);
		} catch ( EntityNotFoundException e) {
			model.addAttribute("error", "Notatka nie istnieje, lub nie masz do niej dostępu");
			return "error";
		}
		if (!note.isPublic() && !note.getUsername().equals(principal.getName())) {
			model.addAttribute("error", "Notatka nie istnieje, lub nie masz do niej dostępu");
			return "error";
		}

		if (note.getIv() != null) {
			model.addAttribute("passwordDTO", new PasswordFormDTO(id));
			return "decrypt";
		}
		model.addAttribute("note", noteService.getNote(id, null));
		return "details";
	}
	
	@PostMapping("/notes/decrypt")
	public String decrypt(@ModelAttribute PasswordFormDTO passwordDTO, Model model, Principal principal) throws Exception {
		Note note;
		try {
			note = noteService.getNote(passwordDTO.getId(), passwordDTO.getPassword());
		} catch (BadPaddingException e) {
			model.addAttribute("error", "Podałeś złe hasło do notatki");
			return "error";
		}
		if (!note.getUsername().equals(principal.getName())) {
			model.addAttribute("error", "Nie możesz odczytać cudzej notatki");
			return "error";
		}
		model.addAttribute("note", note);
		return "details";
	}
	
	@GetMapping("/notes/delete/{id}")
	public String delete(@PathVariable Integer id, Principal principal, Model model) {
		Note note = noteService.getNote(id);
		if (!note.getUsername().equals(principal.getName())) {

			model.addAttribute("error", "Notatka nie istnieje, lub nie masz do niej dostępu");
			return "error";
		}

		if (note.getIv() != null) {
			model.addAttribute("form", new PasswordFormDTO(id));
			return "delete";
		}
		noteService.deleteNoteById(id);
		return "redirect:/notes";
	}
	
	@PostMapping("/notes/delete/encrypted")
	public String deleteEncrypted(@ModelAttribute PasswordFormDTO passwordDTO, Model model, Principal principal) {
		Note note;
		try {
			note = noteService.getNote(passwordDTO.getId(), passwordDTO.getPassword());
			if (!note.getUsername().equals(principal.getName())) {
				model.addAttribute("error", "Błąd");
				return "error";
			}
		} catch (Exception e) {
			if (e.getClass() == BadPaddingException.class || e.getClass() == InvalidKeyException.class) {
				model.addAttribute("form", new PasswordFormDTO(passwordDTO.getId()));
				model.addAttribute("msg", "Niepoprawne hasło do notatki");
				return "delete";
			}
		}
		
		noteService.deleteNoteById(passwordDTO.getId());
		return "redirect:/";
	}
}
