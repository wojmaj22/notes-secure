package pl.majchrzw.notes;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import pl.majchrzw.dto.NoteDTO;
import pl.majchrzw.note.Note;
import pl.majchrzw.note.NoteController;
import pl.majchrzw.note.NoteService;

import javax.crypto.BadPaddingException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc
@MockBean(NoteService.class)
public class NotesControllerTests {
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	NoteService noteService;
	
	@Test
	public void shouldGetNotes() throws Exception {
		// given
		Note note = Note.builder()
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("username")
				.build();
		List<Note> notes = List.of(note);
		Mockito.when(noteService.getNotesByUsername("wojmaj22")).thenReturn(notes);
		// when
		ResultActions result = mockMvc.perform(get("/notes")
						.secure(true)
						.with(user("wojmaj22")
								.password("Qwertyu8!")
								.roles("USER")));
		// then
		result.andExpect(view().name("notes"));
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(model().attribute("notes", notes));
	}
	
	@Test
	public void shouldAddNote() throws Exception {
		Note note = Note.builder()
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.build();
		NoteDTO noteDTO = NoteDTO.builder()
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.password(null)
				.build();
		Mockito.when(noteService.saveNote(noteDTO)).thenReturn(note);
		
		ResultActions result = mockMvc.perform(post("/notes/add")
						.requestAttr("noteDTO", noteDTO)
						.param("name","notatka")
						.param("isPublic","false")
						.param("text","tekst")
						.param("password","")
						.secure(true)
						.with(csrf())
						.with(user("wojmaj22")
								.password("Qwertyu8!")
								.roles("USER")));
		
		result.andExpect(view().name("redirect:/notes"));
		result.andExpect(status().is3xxRedirection());
	}
	
	@Test
	public void shouldNotAddNoteWhenEncryptedAndPublic() throws Exception {
		NoteDTO noteDTO = NoteDTO.builder()
				.name("notatka")
				.isPublic(true)
				.text("tekst")
				.password("1234")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.saveNote(noteDTO)).thenThrow(new IllegalArgumentException(""));
		
		ResultActions result = mockMvc.perform(post("/notes/add")
						.param("name","notatka")
						.param("isPublic","true")
						.param("text","tekst")
						.param("password","1234")
						.secure(true)
						.with(csrf())
						.with(user("wojmaj22")
								.password("Qwertyu8!")
								.roles("USER")));
		
		result.andExpect(view().name("error"));
		result.andExpect(status().is2xxSuccessful());
	}
	
	@Test
	public void shouldNotAddNoteWhenNoCsrfProvided() throws Exception {
		NoteDTO noteDTO = NoteDTO.builder()
				.name("notatka")
				.isPublic(true)
				.text("tekst")
				.password("1234")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.saveNote(noteDTO)).thenThrow(new IllegalArgumentException(""));
		
		ResultActions result = mockMvc.perform(post("/notes/add")
				.param("name","notatka")
				.param("isPublic","true")
				.param("text","tekst")
				.param("password","1234")
				.secure(true)
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		
		result.andExpect(status().isForbidden());
	}
	
	@Test
	public void shouldReadNote() throws Exception {
		UUID uuid = UUID.randomUUID();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid)).thenReturn(note);
		Mockito.when(noteService.getNote(uuid, null)).thenReturn(note);
		ResultActions result = mockMvc.perform(get("/notes/details/" + uuid)
				.secure(true)
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("details"));
		result.andExpect(model().attribute("note", note));
	}
	
	@Test
	public void shouldNotReadNoteWhenItDoesNotExist() throws Exception {
		UUID uuid = UUID.randomUUID();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid)).thenThrow(new EntityNotFoundException(""));
		ResultActions result = mockMvc.perform(get("/notes/details/" + uuid)
				.secure(true)
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("error"));
		result.andExpect(model().attribute("error", "Notatka nie istnieje, lub nie masz do niej dostępu"));
	}
	
	@Test
	public void shouldNotReadNoteWhenPrincipalIsWrong() throws Exception {
		UUID uuid = UUID.randomUUID();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid)).thenReturn(note);
		ResultActions result = mockMvc.perform(get("/notes/details/" + uuid)
				.secure(true)
				.with(user("wojmaj23")
						.password("Qwertyu9!")
						.roles("USER")));
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("error"));
		result.andExpect(model().attribute("error", "Notatka nie istnieje, lub nie masz do niej dostępu"));
	}
	
	@Test
	public void shouldRedirectWhenReadNoteIsEncrypted() throws Exception {
		UUID uuid = UUID.randomUUID();
		byte[] iv = "AAAA".getBytes();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.iv(iv)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid)).thenReturn(note);
		ResultActions result = mockMvc.perform(get("/notes/details/" + uuid)
				.secure(true)
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("decrypt"));
	}
	
	@Test
	public void shouldReadNoteAfterDecryption() throws Exception {
		UUID uuid = UUID.randomUUID();
		String password = "1234";
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid, password)).thenReturn(note);
		
		ResultActions result = mockMvc.perform(post("/notes/decrypt")
				.secure(true)
				.with(csrf())
						.param("password", password)
						.param("id", uuid.toString())
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("details"));
		result.andExpect(model().attribute("note", note));
	}
	
	@Test
	public void shouldNotReadNoteAfterDecryptionWithWrongPassword() throws Exception {
		UUID uuid = UUID.randomUUID();
		String password = "1234";
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid, password)).thenThrow(new BadPaddingException(""));
		
		ResultActions result = mockMvc.perform(post("/notes/decrypt")
				.secure(true)
				.with(csrf())
				.param("password", password)
				.param("id", uuid.toString())
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("error"));
		result.andExpect(model().attribute("error", "Podałeś złe hasło do notatki"));
	}
	
	@Test
	public void shouldReadNoteAfterDecryptionWhenUserIsDifferent() throws Exception {
		UUID uuid = UUID.randomUUID();
		String password = "1234";
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid, password)).thenReturn(note);
		
		ResultActions result = mockMvc.perform(post("/notes/decrypt")
				.secure(true)
				.with(csrf())
				.param("password", password)
				.param("id", uuid.toString())
				.with(user("wojmaj23")
						.password("Qwertyu9!")
						.roles("USER")));
		
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("error"));
		result.andExpect(model().attribute("error", "Nie możesz odczytać cudzej notatki"));
	}
	
	@Test
	public void shouldDeleteNote() throws Exception {
		UUID uuid = UUID.randomUUID();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid)).thenReturn(note);
		
		ResultActions result = mockMvc.perform(get("/notes/delete/"+uuid)
				.secure(true)
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		
		result.andExpect(status().is3xxRedirection());
		result.andExpect(view().name("redirect:/notes"));
	}
	
	@Test
	public void shouldNotDeleteNoteWhenPrincipalIsWrong() throws Exception {
		UUID uuid = UUID.randomUUID();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.build();
		Mockito.when(noteService.getNote(uuid)).thenReturn(note);
		
		ResultActions result = mockMvc.perform(get("/notes/delete/"+uuid)
				.secure(true)
				.with(user("wojmaj23")
						.password("Qwertyu9!")
						.roles("USER")));
		
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("error"));
		result.andExpect(model().attribute("error", "Notatka nie istnieje, lub nie masz do niej dostępu"));
	}
	
	@Test
	public void shouldAskForPasswordWhenDeletingEncryptedNote() throws Exception {
		UUID uuid = UUID.randomUUID();
		byte[] iv = "AAAA".getBytes();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.iv(iv)
				.build();
		Mockito.when(noteService.getNote(uuid)).thenReturn(note);
		
		ResultActions result = mockMvc.perform(get("/notes/delete/"+uuid)
				.secure(true)
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("delete"));
	}
	
	@Test
	public void shouldDeleteEncryptedNote() throws Exception {
		UUID uuid = UUID.randomUUID();
		String password = "1234";
		byte[] iv = "AAAA".getBytes();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.iv(iv)
				.build();
		Mockito.when(noteService.getNote(uuid, password)).thenReturn(note);
		
		ResultActions result = mockMvc.perform(post("/notes/delete/encrypted")
				.secure(true)
						.param("id", uuid.toString())
						.param("password", password)
						.with(csrf())
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		
		result.andExpect(status().is3xxRedirection());
		result.andExpect(view().name("redirect:/"));
	}
	
	@Test
	public void shouldNotDeleteEncryptedNoteWhenPasswordIsWrong() throws Exception {
		UUID uuid = UUID.randomUUID();
		String password = "1234";
		byte[] iv = "AAAA".getBytes();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.iv(iv)
				.build();
		Mockito.when(noteService.getNote(uuid, password)).thenThrow(new InvalidKeyException(""));
		
		ResultActions result = mockMvc.perform(post("/notes/delete/encrypted")
				.secure(true)
				.param("id", uuid.toString())
				.param("password", password)
				.with(csrf())
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("delete"));
		result.andExpect(model().attribute("msg", "Niepoprawne hasło do notatki"));
	}
	
	@Test
	public void shouldNotDeleteEncryptedNoteWhenWrongPrincipalIsProvided() throws Exception {
		UUID uuid = UUID.randomUUID();
		String password = "1234";
		byte[] iv = "AAAA".getBytes();
		Note note = Note.builder()
				.id(uuid)
				.name("notatka")
				.isPublic(false)
				.text("tekst")
				.username("wojmaj22")
				.iv(iv)
				.build();
		Mockito.when(noteService.getNote(uuid, password)).thenReturn(note);
		
		ResultActions result = mockMvc.perform(post("/notes/delete/encrypted")
				.secure(true)
				.param("id", uuid.toString())
				.param("password", password)
				.with(csrf())
				.with(user("wojmaj23")
						.password("Qwertyu9!")
						.roles("USER")));
		
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("error"));
		result.andExpect(model().attribute("error", "Błąd"));
		
	}
	
	@Test
	public void shouldReturnIndex() throws Exception {
		ResultActions result = mockMvc.perform(get("/")
				.secure(true)
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("index"));
		result.andExpect(model().hasNoErrors());
	}
	
	@Test
	public void shouldReturnAddNotePage() throws Exception {
		ResultActions result = mockMvc.perform(get("/notes/add")
				.secure(true)
				.with(user("wojmaj22")
						.password("Qwertyu8!")
						.roles("USER")));
		result.andExpect(status().is2xxSuccessful());
		result.andExpect(view().name("add"));
		result.andExpect(model().hasNoErrors());
	}
	
}

