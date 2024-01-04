package pl.majchrzw.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import pl.majchrzw.devices.Device;
import pl.majchrzw.devices.DeviceService;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessListener {
	
	private final HttpServletRequest request;
	
	private final DeviceService deviceService;
	
	
	@EventListener
	public void onAuthenticationSuccess(AuthenticationSuccessEvent event){
		String ip = extractIp(request);
		String deviceDetails = extractDeviceDetails(request);
		
		Device device = new Device();
		device.setIpAddress(ip);
		device.setDeviceDetails(deviceDetails);
		device.setUsername(event.getAuthentication().getName());
		device.setLastLoggedIn(LocalDateTime.now());
		
		deviceService.saveDevice(device);
		
	}
	
	private String extractIp(HttpServletRequest request) {
		
		final String xFHeader = request.getHeader("X-Forwarded-For");
		if ( xFHeader == null || xFHeader.equals("") || !xFHeader.contains(request.getRemoteAddr())){
			return request.getRemoteAddr();
		} else {
			return xFHeader.split(",")[0];
		}
	}
	
	private String extractDeviceDetails(HttpServletRequest request){
		String deviceDetails = "";
		Parser parser = new Parser();
		Client client = parser.parse(request.getHeader("user-agent"));
		if (Objects.nonNull(client) && client.userAgent != null) {
			deviceDetails = client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor +
					" - " + client.os.family + " " + client.os.major;
		}
		return deviceDetails;
	}
}
