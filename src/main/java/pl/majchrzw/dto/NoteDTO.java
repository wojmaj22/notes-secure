package pl.majchrzw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
