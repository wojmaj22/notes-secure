package pl.majchrzw.dto;

import lombok.Data;

@Data
public class PasswordFormDTO {
	private String password;
	private Integer id;
	
	public PasswordFormDTO(Integer id) {
		this.id = id;
	}
	
	public PasswordFormDTO() {
	}
	
	public PasswordFormDTO(Integer id, String password) {
		this.password = password;
		this.id = id;
	}
}
