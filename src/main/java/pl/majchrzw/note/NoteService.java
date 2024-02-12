package pl.majchrzw.note;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import pl.majchrzw.dto.NoteDTO;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NoteService {
	
	private final NoteRepository noteRepository;
	
	private final Parser parser = Parser.builder().build();
	private final HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
	
	private final String algorithm = "AES/CBC/PKCS5Padding";
	
	private static IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}
	
	public Note saveNote(NoteDTO noteDTO) throws Exception {
		Node document = parser.parse(noteDTO.getText());
		String parsedText = renderer.render(document);
		if (noteDTO.getPassword() == null || noteDTO.getPassword().equals("")) {
			Note note = Note.builder()
					.name(noteDTO.getName())
					.text(parsedText)
					.username(noteDTO.getUsername())
					.iv(null)
					.isPublic(noteDTO.getIsPublic())
					.build();
			return noteRepository.save(note);
		} else {
			if (noteDTO.getIsPublic()) {
				throw new IllegalArgumentException("Szyfrowana notatka nie może być publiczna.");
			}
			byte[] salt = new byte[128];
			new SecureRandom().nextBytes(salt);
			SecretKey key = getKey(noteDTO.getPassword(), salt);
			Cipher cipher = Cipher.getInstance(algorithm);
			IvParameterSpec iv = generateIv();
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			byte[] cipherText = cipher.doFinal(parsedText.getBytes());
			
			
			Note note = Note.builder()
					.name(noteDTO.getName())
					.text(Base64.getEncoder().encodeToString(cipherText))
					.username(noteDTO.getUsername())
					.iv(iv.getIV())
					.salt(salt)
					.isPublic(noteDTO.getIsPublic())
					.build();
			return noteRepository.save(note);
		}
	}
	
	public Note getNote(UUID id, String password) throws Exception {
		Optional<Note> noteOptional = noteRepository.findById(id);
		if (noteOptional.isEmpty()) {
			throw new EntityNotFoundException("No note with id: " + id + ", has been found.");
		}
		Note note = noteOptional.get();
		if (note.getIv() != null) {
			if (password == null) {
				throw new IllegalStateException("No password provided to decrypt an encrypted note");
			}
			String cipherText = noteOptional.get().getText();
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, getKey(password, note.getSalt()), new IvParameterSpec(note.getIv()));
			byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
			note.setText(new String(plainText));
		}
		note.setIv("".getBytes());
		return note;
	}
	
	public Note getNote(UUID id) {
		return noteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No note with id: " + id + ", has been found"));
	}
	
	public List<Note> getNotes() {
		List<Note> notes = noteRepository.findAll();
		for (Note note : notes) {
			note.setIv("".getBytes());
		}
		return notes;
	}
	
	public List<Note> getPublicNotes() {
		List<Note> notes = noteRepository.findAllByIsPublicIsTrue();
		for (Note note : notes) {
			note.setIv("".getBytes());
		}
		return notes;
	}
	
	public List<Note> getNotesByUsername(String username) {
		List<Note> notes = noteRepository.findAllByUsername(username);
		for (Note note : notes) {
			note.setIv("".getBytes());
		}
		return notes;
	}
	
	public void deleteNoteById(UUID id) {
		noteRepository.deleteById(id);
	}
	
	public boolean isNoteEncrypted(UUID id) {
		Note note = noteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("No note with id: " + id + ", has been found"));
		return note.getIv() != null;
	}
	
	private SecretKey getKey(String password, byte[] salt) throws Exception {
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 262144, 256);
		return new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");
	}
	
	public void deleteAll(){
		noteRepository.deleteAll();
	}
}
