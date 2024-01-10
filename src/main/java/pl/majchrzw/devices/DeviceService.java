package pl.majchrzw.devices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
	
	private final DeviceRepository deviceRepository;
	
	public Device saveDevice(Device device){
		return deviceRepository.save(device);
	}
	
	public List<Device> getDevicesByUsername(String username){
		return deviceRepository.findAllByUsername(username);
	}
}
