package com.waidp.service.impl;

import com.waidp.entity.Department;
import com.waidp.entity.User;
import com.waidp.repository.DepartmentRepository;
import com.waidp.repository.UserRepository;
import com.waidp.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门服务实现
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public List<Department> getAllDepartments() {
        List<Department> departments = departmentRepository.findAllByOrderByCreateTimeDesc();
        for (Department department : departments) {
            Long departmentId = department.getId();
            if (departmentId == null) {
                department.setEmployeeCount(0L);
                department.setEmployeeNames(new ArrayList<>());
                continue;
            }

            List<User> users = userRepository.findByDepartmentId(departmentId);
            List<String> names = new ArrayList<>();
            for (User user : users) {
                String name = user.getName();
                if (name == null || name.isBlank()) {
                    name = user.getUsername();
                }
                if (name != null && !name.isBlank()) {
                    names.add(name);
                }
            }

            department.setEmployeeNames(names);
            department.setEmployeeCount((long) users.size());
        }
        return departments;
    }

    @Override
    public Department getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id).orElse(null);
        if (department != null && department.getId() != null) {
            List<User> users = userRepository.findByDepartmentId(department.getId());
            List<String> names = new ArrayList<>();
            for (User user : users) {
                String name = user.getName();
                if (name == null || name.isBlank()) {
                    name = user.getUsername();
                }
                if (name != null && !name.isBlank()) {
                    names.add(name);
                }
            }
            department.setEmployeeNames(names);
            department.setEmployeeCount((long) users.size());
        }
        return department;
    }

    @Override
    @Transactional
    public void createDepartment(Department department) {
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());
        departmentRepository.save(department);
    }

    @Override
    @Transactional
    public void updateDepartment(Department department) {
        Department existing = departmentRepository.findById(department.getId())
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        existing.setName(department.getName());
        existing.setLeader(department.getLeader());
        existing.setLeaderPhone(department.getLeaderPhone());
        existing.setDescription(department.getDescription());
        existing.setUpdateTime(LocalDateTime.now());

        departmentRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        // 检查是否有用户关联
        if (department.getUsers() != null && !department.getUsers().isEmpty()) {
            throw new RuntimeException("该部门下还有用户，不能删除");
        }

        departmentRepository.deleteById(id);
    }
}
