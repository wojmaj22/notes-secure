package pl.majchrzw.note;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column
	private String name;
	
	@Column(columnDefinition = "TEXT")
	private String text;
	
	@Column(name = "iv")
	private byte[] iv;
	
	private byte[] salt;
	
	@Column(name = "is_public")
	private boolean isPublic;
	
	@Column
	private String username;
}
