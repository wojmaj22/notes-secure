package pl.majchrzw.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Service
public class LoginAttemptService {
	
	private final static int MAX_ATTEMPTS = 10;
	private LoadingCache<String, Integer> attemptsCache;
	
	@Autowired
	private HttpServletRequest request;
	
	public LoginAttemptService() {
		attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofDays(1)).build(new CacheLoader<String, Integer>() {
			@Override
			public Integer load(String key) throws Exception {
				return 0;
			}
		});
	}
	
	public void loginFailed(String address) {
		int attempts;
		try {
			attempts = attemptsCache.get(address);
		} catch (ExecutionException e) {
			attempts = 0;
		}
		attempts++;
		attemptsCache.put(address, attempts);
	}
	
	public boolean isBlocked() {
		try {
			return attemptsCache.get(getClientIP()) >= MAX_ATTEMPTS;
		} catch (final ExecutionException e) {
			return false;
		}
	}
	
	private String getClientIP() {
		String xfHeader = request.getHeader("X-Forwarded-For");
		if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
			return request.getRemoteAddr();
		}
		return xfHeader.split(",")[0];
	}
}
