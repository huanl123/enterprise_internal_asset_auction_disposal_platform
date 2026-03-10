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

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepreciationRuleRepository depreciationRuleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("开始初始化基础数据");
        initSystemAdmin();
        initDepartments();
        initDepreciationRules();
        log.info("基础数据初始化完成");
    }

    private void initSystemAdmin() {
        User admin = userRepository.findByUsername("admin").orElse(null);
        LocalDateTime now = LocalDateTime.now();
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("系统管理员");
            admin.setRole("SYSTEM_ADMIN");
            admin.setStatus(true);
            admin.setCreateTime(now);
            admin.setUpdateTime(now);
            userRepository.save(admin);
            log.info("系统管理员账号已创建：admin/admin123");
            return;
        }
        if (!passwordEncoder.matches("admin123", admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setUpdateTime(now);
            userRepository.save(admin);
            log.info("系统管理员密码已更新：admin/admin123");
        }
    }

    private void initDepartments() {
        if (departmentRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String[] deptNames = {"技术部", "财务部", "人力资源部", "市场部", "行政部"};
        for (String name : deptNames) {
            Department dept = new Department();
            dept.setName(name);
            dept.setLeader("部门经理");
            dept.setCreateTime(now);
            dept.setUpdateTime(now);
            departmentRepository.save(dept);
        }
        log.info("示例部门已创建");
    }

    private void initDepreciationRules() {
        if (depreciationRuleRepository.count() > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        DepreciationRule rule1 = new DepreciationRule();
        rule1.setName("电子设备折旧规则");
        rule1.setUsefulLife(5);
        rule1.setSalvageRate(5.0);
        rule1.setDescription("适用于电脑、打印机等电子设备");
        rule1.setStatus(true);
        rule1.setCreateTime(now);
        rule1.setUpdateTime(now);
        depreciationRuleRepository.save(rule1);

        DepreciationRule rule2 = new DepreciationRule();
        rule2.setName("办公设备折旧规则");
        rule2.setUsefulLife(8);
        rule2.setSalvageRate(5.0);
        rule2.setDescription("适用于桌椅、柜子等办公设备");
        rule2.setStatus(true);
        rule2.setCreateTime(now);
        rule2.setUpdateTime(now);
        depreciationRuleRepository.save(rule2);

        DepreciationRule rule3 = new DepreciationRule();
        rule3.setName("车辆折旧规则");
        rule3.setUsefulLife(10);
        rule3.setSalvageRate(5.0);
        rule3.setDescription("适用于公司车辆");
        rule3.setStatus(true);
        rule3.setCreateTime(now);
        rule3.setUpdateTime(now);
        depreciationRuleRepository.save(rule3);

        log.info("示例折旧规则已创建");
    }
}

