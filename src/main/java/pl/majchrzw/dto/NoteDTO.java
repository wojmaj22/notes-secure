package pl.majchrzw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class NoteDTO {
	
	@NotNull
	@NotEmpty
	private String text;
	
	@NotNull
	@NotBlank
	@Length( max = 255, message = "Długość nazwy notatki musi być mniejsza niż 255")
	private String name;
	
	private String username;
	
	private String password;
	
	private Boolean isPublic;
}
