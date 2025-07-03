package com.example.rentingsystemdemo.config;

import com.example.rentingsystemdemo.model.*;
import com.example.rentingsystemdemo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // 创建权限
            Permission permRentItem = createPermission(permissionRepository, "item:rent");
            Permission permManageItem = createPermission(permissionRepository, "item:manage");
            Permission permViewAllOrders = createPermission(permissionRepository, "order:manage");
            Permission permManageUsers = createPermission(permissionRepository, "user:manage");


            // 创建角色
            Role adminRole = createRole(roleRepository, "ROLE_ADMIN",
                    Set.of(permRentItem, permManageItem,permViewAllOrders,permManageUsers));

            Role userRole = createRole(roleRepository, "ROLE_USER",
                    Set.of(permRentItem));

            // 创建用户
            User admin = createUser(userRepository, "admin", "admin123", adminRole, passwordEncoder);
            User user = createUser(userRepository, "user", "user123", userRole, passwordEncoder);

            // 创建租赁物品
            createItem(itemRepository, "专业相机", "佳能 EOS R5 专业级全画幅相机", new BigDecimal("150.00"), admin);
            createItem(itemRepository, "投影仪", "明基 4K 高清投影仪", new BigDecimal("80.00"), admin);
            createItem(itemRepository, "帐篷", "四人用专业户外帐篷", new BigDecimal("30.00"), user);
        };
    }

    private Permission createPermission(PermissionRepository repo, String name) {
        return repo.findByName(name)
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setName(name);
                    return repo.save(p);
                });
    }

    private Role createRole(RoleRepository repo, String name, Set<Permission> permissions) {
        return repo.findByName(name)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(name);
                    r.setPermissions(permissions);
                    return repo.save(r);
                });
    }

    private User createUser(UserRepository repo, String username, String password,
                            Role role, PasswordEncoder encoder) {
        if (repo.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(encoder.encode(password));
            user.setRoles(Set.of(role));
            return repo.save(user);
        }
        return null;
    }

    private void createItem(ItemRepository repo, String name, String description,
                            BigDecimal price, User owner) {
        if (repo.findByName(name).isEmpty()) {
            Item item = new Item();
            item.setName(name);
            item.setDescription(description);
            item.setDailyPrice(price);
            item.setAvailable(true);
            item.setOwner(owner);
            repo.save(item);
        }
    }
}