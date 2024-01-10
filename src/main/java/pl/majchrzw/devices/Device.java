package pl.majchrzw.devices;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "devices_list")
public class Device {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String username;
	private String deviceDetails;
	
	private String ipAddress;
	private LocalDateTime lastLoggedIn;
}
