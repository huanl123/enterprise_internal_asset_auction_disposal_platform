package com.waidp.service;

import com.waidp.entity.Department;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService {

    /**
     * 查询所有部门
     */
    List<Department> getAllDepartments();

    /**
     * 根据ID获取部门
     */
    Department getDepartmentById(Long id);

    /**
     * 创建部门
     */
    void createDepartment(Department department);

    /**
     * 更新部门
     */
    void updateDepartment(Department department);

    /**
     * 删除部门
     */
    void deleteDepartment(Long id);
}
