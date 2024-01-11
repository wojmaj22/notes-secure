package pl.majchrzw.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PasswordFormDTO {
	private String password;
	private UUID id;
	
	public PasswordFormDTO(UUID id) {
		this.id = id;
	}
	
	public PasswordFormDTO() {
	}
}
