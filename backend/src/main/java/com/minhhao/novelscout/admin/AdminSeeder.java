package com.minhhao.novelscout.admin;

import com.minhhao.novelscout.user.Role;
import com.minhhao.novelscout.user.RoleName;
import com.minhhao.novelscout.user.RoleRepository;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByUsernameIgnoreCase("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));

            User admin = User.createAdmin(
                    "admin@novelscout.vn",
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "Quản Trị Viên",
                    adminRole
            );

            userRepository.save(admin);
            log.info("Đã khởi tạo tài khoản Admin mặc định (username: admin / pass: admin123).");
        }
    }
}
