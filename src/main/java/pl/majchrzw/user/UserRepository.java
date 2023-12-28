package pl.majchrzw.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
	
	public Optional<User> findByUsername(String username);
	
	public boolean existsByUsername(String username);
	
	public void deleteByUsername(String username);

}