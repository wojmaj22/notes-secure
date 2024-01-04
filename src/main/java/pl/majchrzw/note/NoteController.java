package pl.majchrzw.note;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pl.majchrzw.dto.NoteDTO;
import pl.majchrzw.dto.ProvidePasswordDTO;

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
	public String saveNote(@ModelAttribute NoteDTO noteDTO, Principal principal, Model model) throws Exception {
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
		Note note = noteService.getNote(id);
		if (!note.isPublic() && !note.getUsername().equals(principal.getName())) {
			model.addAttribute("error", "Nie możesz odczytać tej notatki ponieważ nie należy do ciebie i nie jest publiczna");
			return "error";
		}
		if (note.getIv() != null) {
			model.addAttribute("passwordDTO", new ProvidePasswordDTO(id));
			return "decrypt";
		}
		model.addAttribute("note", noteService.getNote(id, null));
		return "details";
	}
	
	@PostMapping("/notes/decrypt")
	public String decrypt(@ModelAttribute ProvidePasswordDTO passwordDTO, Model model, Principal principal) throws Exception {
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
			model.addAttribute("error", "Nie możesz usunąć notatki która nie należy do ciebie");
			return "error";
		}
		// TODO - może dodać że jeżeli notatka jest zahasłowana to należy podać hasło przed usunięciem?
		if (note.getIv() != null) {
			model.addAttribute("form", new ProvidePasswordDTO(id));
			return "delete";
		}
		noteService.deleteNoteById(id);
		return "redirect:/notes";
	}
	
	@PostMapping("/notes/delete/encrypted")
	public String deleteEncrypted(@ModelAttribute ProvidePasswordDTO passwordDTO, Model model, Principal principal) {
		Note note;
		try {
			note = noteService.getNote(passwordDTO.getId(), passwordDTO.getPassword());
			if (!note.getUsername().equals(principal.getName())) {
				model.addAttribute("error", "Błąd");
				return "error";
			}
		} catch (Exception e) {
			if (e.getClass() == BadPaddingException.class || e.getClass() == InvalidKeyException.class) {
				model.addAttribute("form", new ProvidePasswordDTO(passwordDTO.getId()));
				model.addAttribute("msg", "Niepoprawne hasło do notatki");
				return "delete";
			}
		}
		
		noteService.deleteNoteById(passwordDTO.getId());
		return "redirect:/";
	}
}
