package pl.majchrzw.notes;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.linkedin.urls.Url;
import com.linkedin.urls.detection.UrlDetector;
import com.linkedin.urls.detection.UrlDetectorOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pl.majchrzw.dto.RegisterFormDTO;
import pl.majchrzw.user.CustomUserDetailsService;

import javax.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LoginTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private CustomUserDetailsService userDetailsService;
	
	@RegisterExtension
	static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
			.withConfiguration(GreenMailConfiguration.aConfig().withUser("user", "admin"))
			.withPerMethodLifecycle(false);
	
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));
	
	static {
		postgres.start();
	}
	
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}
	
	public ResultActions performPostToRegisterForm(RegisterFormDTO registerFormDTO) throws Exception {
		return mockMvc.perform(post("/register").secure(true)
				.param("email", registerFormDTO.getEmail())
				.param("username", registerFormDTO.getUsername())
				.param("password", registerFormDTO.getPassword())
				.param("matchingPassword", registerFormDTO.getMatchingPassword())
				.param("isUsing2FA", registerFormDTO.getIsUsing2FA().toString())
				.with(csrf()));
	}
	
	@Test
	public void shouldNotLoginAfterRegisterWithoutActivation() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj22@gmail.com");
		registerFormDTO.setUsername("wojmaj22");
		registerFormDTO.setPassword("Qwertyu8!");
		registerFormDTO.setMatchingPassword("Qwertyu8!");
		registerFormDTO.setIsUsing2FA(false);
		//when
		performPostToRegisterForm(registerFormDTO);
		ResultActions resultActions = mockMvc.perform(post("/process-login")
				.param("username","wojmaj22")
				.param("password","Qwertyu8!")
				.param("code", "")
				.secure(true)
				.with(csrf()));
		//then
		resultActions.andExpect(unauthenticated());
		
		//clean
		userDetailsService.deleteUser("wojmaj22");
	}
	
	@Test
	public void shouldLoginAfterRegisterWithActivation() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj22@gmail.com");
		registerFormDTO.setUsername("wojmaj22");
		registerFormDTO.setPassword("Qwertyu8!");
		registerFormDTO.setMatchingPassword("Qwertyu8!");
		registerFormDTO.setIsUsing2FA(false);
		//when
		performPostToRegisterForm(registerFormDTO);

		MimeMessage message = greenMail.getReceivedMessages()[0];
		String msgContent = GreenMailUtil.getBody(message);
		Map<String, String> params = extractTokenAndUsernameFromMessage(msgContent);
		
		ResultActions activateAction = mockMvc.perform(get("/activate?token=" + params.get("token") + "&user=" + params.get("user"))
				.secure(true));
		
		ResultActions loginAction = mockMvc.perform(post("/process-login")
						.param("username","wojmaj22")
						.param("password","Qwertyu8!")
						.param("code", "")
						.secure(true)
						.with(csrf()));
		//then
		activateAction.andExpect(view().name("message"));
		loginAction.andExpect(authenticated().withRoles("USER"));
		
		//clean
		userDetailsService.deleteUser("wojmaj22");
	}
	
	private Map<String, String> extractTokenAndUsernameFromMessage(String message){
		UrlDetector parser = new UrlDetector(message, UrlDetectorOptions.ALLOW_SINGLE_LEVEL_DOMAIN);
		List<Url> found = parser.detect();
		
		String query = found.get(0).getQuery().substring(1);
		Map<String, String> params = new HashMap<>();
		for (String param : query.split("&")) {
			String[] pair = param.split("=");
			String key = pair[0];
			String value = pair.length > 1 ? pair[1] : "";
			params.put(key, value);
		}
		
		return params;
	}
}
