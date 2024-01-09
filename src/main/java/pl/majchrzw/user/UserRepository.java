package pl.majchrzw.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
	
	Optional<User> findByUsername(String username);
	
	boolean existsByUsername(String username);
	
	void deleteByUsername(String username);
	
	boolean existsByEmail(String email);
	
	Optional<User> findByEmail(String email);
}
