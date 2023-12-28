package pl.majchrzw.dto;

import lombok.Data;

@Data
public class NoteDTO {
	
	private String text;
	
	private String name;
	
	private String username;
	
	private String password;
	
	private Boolean isPublic;
}
