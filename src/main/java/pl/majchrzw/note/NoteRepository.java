package pl.majchrzw.note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepository extends JpaRepository<Note, UUID> {
	
	public List<Note> findAllByIsPublicIsTrue();
	
	public List<Note> findAllByUsername(String username);
	
	public void deleteAllByUsername(String username);
}
