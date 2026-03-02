package com.waidp.controller;

import com.waidp.common.Result;
import com.waidp.entity.Department;
import com.waidp.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 */
@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取所有部门
     */
    @GetMapping("/list")
    public Result<List<Department>> list() {
        return Result.success(departmentService.getAllDepartments());
    }

    /**
     * 根据ID获取部门
     */
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        return Result.success(departmentService.getDepartmentById(id));
    }

    /**
     * 创建部门
     */
    @PostMapping("/create")
    public Result<Void> create(@RequestBody Department department) {
        departmentService.createDepartment(department);
        return Result.success("创建成功", null);
    }

    /**
     * 更新部门
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody Department department) {
        departmentService.updateDepartment(department);
        return Result.success("更新成功", null);
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success("删除成功", null);
    }
}
