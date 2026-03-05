package com.waidp.repository;

import com.waidp.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门 Repository
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 查询所有部门
     */
    List<Department> findAllByOrderByCreateTimeDesc();

    /**
     * 根据名称查询部门
     */
    Department findByName(String name);
}
