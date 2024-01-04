package pl.majchrzw.note;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column
	private String name;
	
	@Column
	private String text;
	
	@Column(name = "iv")
	private byte[] iv;
	
	@Column(name = "is_public")
	private boolean isPublic;
	
	@Column
	private String username;
}
