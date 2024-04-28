package ru.rtu_mirea.migrationdb.component;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.rtu_mirea.migrationdb.entity.Role;
import ru.rtu_mirea.migrationdb.repository.UserRepository;
import ru.rtu_mirea.migrationdb.service.UserService;

@Component
public class AppStartupRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    public AppStartupRunner(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(userRepository.findByRole(Role.ADMIN).isEmpty()) {
            userService.addStartAdmin();
        }
    }

}
