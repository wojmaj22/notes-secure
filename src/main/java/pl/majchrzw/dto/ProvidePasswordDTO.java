package pl.majchrzw.dto;

import lombok.Data;

@Data
public class ProvidePasswordDTO {
	private String password;
	private Integer id;
	
	public ProvidePasswordDTO(Integer id) {
		this.id = id;
	}
	
	public ProvidePasswordDTO(){}
	
	public ProvidePasswordDTO(Integer id, String password){
		this.password = password;
		this.id = id;
	}
}
