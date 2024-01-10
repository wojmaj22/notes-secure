package pl.majchrzw.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import pl.majchrzw.user.CustomUserDetailsService;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ComponentScan("pl.majchrzw.security")
public class WebSecurityConfiguration {
	
	private final DataSource dataSource;
	
	private final CustomWebAuthenticationDetailsSource customWebAuthenticationDetailsSource;
	
	private final CustomUserDetailsService userDetailsService;
	
	private final PasswordEncoder encoder;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.requiresChannel(channel -> channel
						.anyRequest().requiresSecure())
				.authorizeHttpRequests((authorize) -> authorize
						.requestMatchers("/styles.css").permitAll()
						.requestMatchers("/").permitAll()
						.requestMatchers("/teapot").permitAll()
						.requestMatchers("/password-reset").anonymous()
						.requestMatchers("/password-reset-form*").anonymous()
						.requestMatchers("/login").anonymous()
						.requestMatchers("/register").anonymous()
						.requestMatchers("/activate*").anonymous()
						.requestMatchers("/notes/details/*").permitAll()
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.permitAll()
						.loginProcessingUrl("/process-login")
						.permitAll()
						.defaultSuccessUrl("/notes")
						.authenticationDetailsSource(customWebAuthenticationDetailsSource))
				.exceptionHandling(exception -> exception
						.accessDeniedPage("/error"));
		return http.build();
	}
	
	@Bean
	public JdbcTokenRepositoryImpl tokenRepository() {
		JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
		tokenRepository.setDataSource(dataSource);
		return tokenRepository;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
		builder.authenticationProvider(authenticationProvider());
		
		return builder.build();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		final CustomAuthenticationProvider provider = new CustomAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(encoder);
		
		return provider;
	}
	
}
