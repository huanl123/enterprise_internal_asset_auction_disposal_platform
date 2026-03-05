package com.waidp.config;

import com.waidp.entity.Department;
import com.waidp.entity.DepreciationRule;
import com.waidp.entity.User;
import com.waidp.repository.DepartmentRepository;
import com.waidp.repository.DepreciationRuleRepository;
import com.waidp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 用于在应用启动时初始化基础数据
 */
@Component  // 恢复启用自动初始化
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepreciationRuleRepository depreciationRuleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("开始初始化数据...");

        // 初始化系统管理员
        initSystemAdmin();

        // 初始化示例部门
        initDepartments();

        // 初始化示例折旧规则
        initDepreciationRules();

        log.info("数据初始化完成");
    }

    /**
     * 初始化系统管理员账号
     */
    private void initSystemAdmin() {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("系统管理员");
            admin.setRole("系统管理员");
            admin.setStatus(true);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());
            userRepository.save(admin);
            log.info("系统管理员账号已创建：admin/admin123");
        } else {
            // 如果管理员账户已存在，但密码不是admin123，则更新密码
            if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setUpdateTime(LocalDateTime.now());
                userRepository.save(admin);
                log.info("系统管理员密码已更新：admin/admin123");
            }
        }
    }

    /**
     * 初始化示例部门
     */
    private void initDepartments() {
        if (departmentRepository.count() == 0) {
            String[] deptNames = {"技术部", "财务部", "人力资源部", "市场部", "行政部"};

            for (String name : deptNames) {
                Department dept = new Department();
                dept.setName(name);
                dept.setLeader("部门经理");
                dept.setCreateTime(LocalDateTime.now());
                dept.setUpdateTime(LocalDateTime.now());
                departmentRepository.save(dept);
            }

            log.info("示例部门已创建");
        }
    }

    /**
     * 初始化示例折旧规则
     */
    private void initDepreciationRules() {
        if (depreciationRuleRepository.count() == 0) {
            DepreciationRule rule1 = new DepreciationRule();
            rule1.setName("电子设备折旧规则");
            rule1.setUsefulLife(5);
            rule1.setSalvageRate(5.0);
            rule1.setDescription("适用于电脑、打印机等电子设备");
            rule1.setStatus(true);
            rule1.setCreateTime(LocalDateTime.now());
            rule1.setUpdateTime(LocalDateTime.now());
            depreciationRuleRepository.save(rule1);

            DepreciationRule rule2 = new DepreciationRule();
            rule2.setName("办公设备折旧规则");
            rule2.setUsefulLife(8);
            rule2.setSalvageRate(5.0);
            rule2.setDescription("适用于桌椅、柜子等办公设备");
            rule2.setStatus(true);
            rule2.setCreateTime(LocalDateTime.now());
            rule2.setUpdateTime(LocalDateTime.now());
            depreciationRuleRepository.save(rule2);

            DepreciationRule rule3 = new DepreciationRule();
            rule3.setName("车辆折旧规则");
            rule3.setUsefulLife(10);
            rule3.setSalvageRate(5.0);
            rule3.setDescription("适用于公司车辆");
            rule3.setStatus(true);
            rule3.setCreateTime(LocalDateTime.now());
            rule3.setUpdateTime(LocalDateTime.now());
            depreciationRuleRepository.save(rule3);

            log.info("示例折旧规则已创建");
        }
    }
}