package pl.majchrzw.dto;

import lombok.Data;
import pl.majchrzw.validators.EmailValid;

@Data
public class ProvideEmailDTO {
	
	@EmailValid
	private String email;
}
