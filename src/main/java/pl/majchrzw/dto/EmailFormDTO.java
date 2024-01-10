package pl.majchrzw.dto;

import lombok.Data;
import pl.majchrzw.validators.EmailValid;

@Data
public class EmailFormDTO {
	
	@EmailValid
	private String email;
}
