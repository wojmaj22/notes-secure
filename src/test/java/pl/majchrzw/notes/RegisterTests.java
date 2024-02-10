package pl.majchrzw.notes;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RegisterTests {
	
	@Autowired
	private MockMvc mockMvc;
	
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
	
	@Test
	public void getRegister() throws Exception {
		mockMvc.perform(get("/register").secure(true)).andExpect(status().isOk()).andExpect(view().name("register"));
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
	public void shouldRegisterUser() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj22@gmail.com");
		registerFormDTO.setUsername("wojmaj22");
		registerFormDTO.setPassword("Qwertyu8!");
		registerFormDTO.setMatchingPassword("Qwertyu8!");
		registerFormDTO.setIsUsing2FA(false);
		//when
		ResultActions resultActions = performPostToRegisterForm(registerFormDTO);
		//then
		resultActions.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("message"))
				.andExpect(model().hasNoErrors());
	}
	
	@Test
	public void shouldRegisterUserWith2FA() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj21@gmail.com");
		registerFormDTO.setUsername("wojmaj21");
		registerFormDTO.setPassword("Qwertyu8!");
		registerFormDTO.setMatchingPassword("Qwertyu8!");
		registerFormDTO.setIsUsing2FA(true);
		//when
		ResultActions resultActions = performPostToRegisterForm(registerFormDTO);
		//then
		resultActions.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("qrcode"))
				.andExpect(model().hasNoErrors());
	}
	
	@Test
	public void shouldThrowErrorWhenUsernameAndEmailAreTaken() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj22@gmail.com");
		registerFormDTO.setUsername("wojmaj22");
		registerFormDTO.setPassword("Qwertyu8!");
		registerFormDTO.setMatchingPassword("Qwertyu8!");
		registerFormDTO.setIsUsing2FA(true);
		//when
		ResultActions resultActions = performPostToRegisterForm(registerFormDTO);
		//then
		resultActions.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("register"))
				.andExpect(model().attributeHasFieldErrors("form","username"))
				.andExpect(model().attributeHasFieldErrors("form","email"));
	}
	
	@Test
	public void shouldThrowExceptionWhenPasswordDontMatch() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj22@gmail.com");
		registerFormDTO.setUsername("wojmaj22");
		registerFormDTO.setPassword("Qwertyu8!");
		registerFormDTO.setMatchingPassword("Qwertyu9!");
		registerFormDTO.setIsUsing2FA(false);
		//when
		ResultActions resultActions = performPostToRegisterForm(registerFormDTO);
		//then
		resultActions.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("register"))
				.andExpect(model().errorCount(1));
	}
	
	@Test
	public void shouldThrowExceptionWhenEmailIsWrong() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj22");
		registerFormDTO.setUsername("wojmaj23");
		registerFormDTO.setPassword("Qwertyu8!");
		registerFormDTO.setMatchingPassword("Qwertyu8!");
		registerFormDTO.setIsUsing2FA(false);
		//when
		ResultActions resultActions = performPostToRegisterForm(registerFormDTO);
		//then
		resultActions.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("register"))
				.andExpect(model().attributeHasFieldErrors("form","email"));
	}
	
	@Test
	public void shouldThrowExceptionWhenUsernameIsTooShort() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj24@gmail.com");
		registerFormDTO.setUsername("12");
		registerFormDTO.setPassword("Qwertyu8!");
		registerFormDTO.setMatchingPassword("Qwertyu8!");
		registerFormDTO.setIsUsing2FA(false);
		//when
		ResultActions resultActions = performPostToRegisterForm(registerFormDTO);
		//then
		resultActions.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("register"))
				.andExpect(model().attributeHasFieldErrors("form","username"));
	}
	
	@Test
	public void shouldThrowExceptionWhenPasswordIsWrong() throws Exception {
		//given
		RegisterFormDTO registerFormDTO = new RegisterFormDTO();
		registerFormDTO.setEmail("wojjmaj25@gmail.com");
		registerFormDTO.setUsername("wojmaj25");
		registerFormDTO.setPassword("1234");
		registerFormDTO.setMatchingPassword("1234");
		registerFormDTO.setIsUsing2FA(false);
		//when
		ResultActions resultActions = performPostToRegisterForm(registerFormDTO);
		//then
		resultActions.andExpect(status().is2xxSuccessful())
				.andExpect(view().name("register"))
				.andExpect(model().attributeHasFieldErrors("form","password"));
	}
}
