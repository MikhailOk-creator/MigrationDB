package ru.rtu_mirea.migrationdb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import ru.rtu_mirea.migrationdb.entity.Role;
import ru.rtu_mirea.migrationdb.entity.User;
import ru.rtu_mirea.migrationdb.repository.UserRepository;

import java.util.UUID;

@Service
public class UserService {

    @Value("${admin.username}")
    private String username;
    @Value("${admin.password}")
    private String password;
    @Value("${admin.email}")
    private String email;

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addStartAdmin () {
        try {
            if (!username.isEmpty() || !password.isEmpty() || !email.isEmpty()) {
                userRepository.save(
                        new User(UUID.randomUUID(), username,
                                new BCryptPasswordEncoder().encode(password),
                                email, Role.ADMIN)
                );
                log.info("Admin added");
            } else {
                log.error("Please check Please check the correctness of the entered data");
            }
        } catch (Exception e) {
            log.error("Something wrong: {}", e);
        }
    }

}
