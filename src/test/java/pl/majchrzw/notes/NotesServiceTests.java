package pl.majchrzw.notes;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pl.majchrzw.dto.NoteDTO;
import pl.majchrzw.note.Note;
import pl.majchrzw.note.NoteService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NotesServiceTests {
	
	@Autowired
	private NoteService noteService;
	
	@RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
			.withConfiguration(GreenMailConfiguration.aConfig().withUser("user", "admin"))
			.withPerMethodLifecycle(false);
	
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
	
	static {
		postgres.start();
	}
	
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}
	
	@AfterEach
	public void cleanDB(){
		noteService.deleteAll();
	}
	
	@Test
	public void shouldProperlyAddNotEncryptedNote() throws Exception {
		// given
		final String name = "notatka";
		final String text = "tekst";
		final String expectedText = "<p>tekst</p>\n";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(null)
				.build();
		// when
		Note note = noteService.saveNote(noteDTO);
		// then
		Assertions.assertEquals(expectedText, note.getText());
		Assertions.assertEquals(username, note.getUsername());
		Assertions.assertEquals(name, note.getName());
		Assertions.assertFalse(note.isPublic());
		Assertions.assertNull(note.getIv());
		Assertions.assertNull(note.getSalt());
	}
	
	@Test
	public void shouldAddEncryptedNoteAndDecryptIt() throws Exception {
		// given
		final String name = "notatka";
		final String text = "tekst";
		final String expectedText = "<p>tekst</p>\n";
		final String username = "username";
		final String password = "1234";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(password)
				.build();
		// when
		Note note = noteService.saveNote(noteDTO);
		Assertions.assertNotEquals(expectedText, note.getText());
		Note unencryptedNote = noteService.getNote(note.getId(), password);
		// then
		Assertions.assertEquals(expectedText, unencryptedNote.getText());
		Assertions.assertEquals(username, unencryptedNote.getUsername());
		Assertions.assertEquals(name, unencryptedNote.getName());
		Assertions.assertFalse(unencryptedNote.isPublic());
		Assertions.assertNotNull(unencryptedNote.getIv());
		Assertions.assertNotNull(unencryptedNote.getSalt());
	}
	
	@Test
	public void shouldThrowErrorWhenDecryptingNoteWithWrongPassword() throws Exception {
		// given
		final String name = "notatka";
		final String text = "tekst";
		final String expectedText = "<p>tekst</p>\n";
		final String username = "username";
		final String password = "1234";
		final String wrongPassword = "12345";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(password)
				.build();
		// when
		Note note = noteService.saveNote(noteDTO);
		Assertions.assertNotEquals(expectedText, note.getText());
		// then
		assertThrows(Exception.class , () -> noteService.getNote(note.getId(), wrongPassword));
	}
	
	@Test
	public void shouldThrowErrorWhenAddingEncryptedPublicNote() {
		// given
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		final String password = "1234";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(true)
				.text(text)
				.username(username)
				.password(password)
				.build();
		// when, then
		assertThrows(IllegalArgumentException.class , () -> noteService.saveNote(noteDTO));
	}
	@Test
	public void shouldAddPublicNote() throws Exception {
		// given
		final String name = "notatka";
		final String text = "tekst";
		final String expectedText = "<p>tekst</p>\n";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(true)
				.text(text)
				.username(username)
				.password(null)
				.build();
		// when
		Note note = noteService.saveNote(noteDTO);
		// then
		Assertions.assertEquals(expectedText, note.getText());
		Assertions.assertEquals(username, note.getUsername());
		Assertions.assertEquals(name, note.getName());
		Assertions.assertTrue(note.isPublic());
		Assertions.assertNull(note.getIv());
		Assertions.assertNull(note.getSalt());
	}
	
	@Test
	public void ShouldDeleteNoteAfterAdding() throws Exception {
		// given
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(null)
				.build();
		// when
		Note note = noteService.saveNote(noteDTO);
		noteService.deleteNoteById(note.getId());
		// then
		assertThrows( EntityNotFoundException.class, () -> noteService.getNote(note.getId()));
	}
	
	@Test
	public void shouldProperlyFormatNoteTextWithMarkdown() throws Exception {
		// given
		final String name = "notatka";
		final String text = """
				# Header
				### Smaller header
				Example of markdown
				**Bold text**
				*Italicized*
				
				- a list
				- of items
				
				1. ordered list
				2. of items
				
				![image](image.src)
				""";
		final String expectedText = """
				<h1>Header</h1>
				<h3>Smaller header</h3>
				<p>Example of markdown
				<strong>Bold text</strong>
				<em>Italicized</em></p>
				<ul>
				<li>a list</li>
				<li>of items</li>
				</ul>
				<ol>
				<li>ordered list</li>
				<li>of items</li>
				</ol>
				<p><img src="image.src" alt="image" /></p>
				""";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(null)
				.build();
		// when
		Note note = noteService.saveNote(noteDTO);
		// then
		Assertions.assertEquals(expectedText, note.getText());
	}
	
	@Test
	public void shouldEscapeHTMLInsideMarkdown() throws Exception {
		// given
		final String name = "notatka";
		final String text = """
				# Header
				<img src="image.src" alt="image" />
				<script>alert("test")</script>
				""";
		final String expectedText = """
				<h1>Header</h1>
				<p>&lt;img src=&quot;image.src&quot; alt=&quot;image&quot; /&gt;
				&lt;script&gt;alert(&quot;test&quot;)&lt;/script&gt;</p>
				""";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(null)
				.build();
		// when
		Note note = noteService.saveNote(noteDTO);
		// then
		Assertions.assertEquals(expectedText, note.getText());
	}
	
	@Test
	public void shouldGetNotesByUsername() throws Exception {
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(null)
				.build();
		Note note = noteService.saveNote(noteDTO);
		NoteDTO noteDTO2 = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password("1234")
				.build();
		Note note2 = noteService.saveNote(noteDTO2);
		
		List<Note> notes = noteService.getNotesByUsername(username);
		
		Assertions.assertEquals(2, notes.size());
	}
	
	@Test
	public void getNoteById() throws Exception {
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(null)
				.build();
		Note note = noteService.saveNote(noteDTO);
		
		
		Note noteFromDb = noteService.getNote(note.getId());
		
		Assertions.assertEquals(noteFromDb, note);
	}
	
	@Test
	public void getSecuredNoteById() throws Exception {
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		final String password = "1234";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(password)
				.build();
		Note note = noteService.saveNote(noteDTO);
		
		
		Note noteFromDb = noteService.getNote(note.getId(), password);
		
		Assertions.assertEquals(note.getId(), noteFromDb.getId());
	}
	
	@Test
	public void shouldThrowExceptionWhenNoteDoesNotExist() throws Exception {
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		final String password = "1234";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(password)
				.build();
		Note note = noteService.saveNote(noteDTO);
		
		assertThrows(EntityNotFoundException.class,() -> noteService.getNote(UUID.randomUUID(), password));
	}
	
	@Test
	public void shouldThrowExceptionWhenNoPasswordForEncryptedNoteIsProvided() throws Exception {
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		final String password = "1234";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(password)
				.build();
		Note note = noteService.saveNote(noteDTO);
		
		assertThrows(IllegalStateException.class,() -> noteService.getNote(note.getId(), null));
	}
	
	@Test
	public void shouldThrowExceptionWhenWrongPasswordForEncryptedNoteIsProvided() throws Exception {
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		final String password = "1234";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(password)
				.build();
		Note note = noteService.saveNote(noteDTO);
		
		assertThrows(Exception.class,() -> noteService.getNote(note.getId(), "12345"));
	}
	
	@Test
	public void shouldGetAllNotes() throws Exception {
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(null)
				.build();
		Note note = noteService.saveNote(noteDTO);
		NoteDTO noteDTO2 = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password("1234")
				.build();
		Note note2 = noteService.saveNote(noteDTO2);
		
		List<Note> notes = noteService.getNotes();
		
		Assertions.assertEquals(notes.size(), 2);
	}
	
	@Test
	public void shouldGetAllPublicNotes() throws Exception {
		final String name = "notatka";
		final String text = "tekst";
		final String username = "username";
		NoteDTO noteDTO = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password(null)
				.build();
		Note note = noteService.saveNote(noteDTO);
		NoteDTO noteDTO2 = NoteDTO.builder()
				.name(name)
				.isPublic(false)
				.text(text)
				.username(username)
				.password("1234")
				.build();
		Note note2 = noteService.saveNote(noteDTO2);
		NoteDTO noteDTO3 = NoteDTO.builder()
				.name(name)
				.isPublic(true)
				.text(text)
				.username(username)
				.password(null)
				.build();
		Note note3 = noteService.saveNote(noteDTO3);
		
		List<Note> notes = noteService.getPublicNotes();
		
		Assertions.assertEquals(1, notes.size());
	}
}
