package com.waidp.repository;

import com.waidp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据部门ID查询用户
     */
    List<User> findByDepartmentId(Long departmentId);

    /**
     * 统计部门员工数
     */
    long countByDepartmentId(Long departmentId);

    /**
     * 根据角色查询用户
     */
    List<User> findByRole(String role);

    /**
     * 根据部门ID和状态查询用户
     */
    List<User> findByDepartmentIdAndStatus(Long departmentId, Boolean status);

    /**
     * 多条件搜索用户
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:name IS NULL OR u.realName LIKE %:name%) AND " +
           "(:role IS NULL OR LOWER(REPLACE(u.role, 'ROLE_', '')) = LOWER(REPLACE(:role, 'ROLE_', ''))) AND " +
           "(:departmentId IS NULL OR u.departmentId = :departmentId) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(LOWER(REPLACE(u.role, 'ROLE_', '')) NOT IN ('admin', 'system_admin'))")
    Page<User> searchUsers(@Param("username") String username,
                           @Param("name") String name,
                           @Param("role") String role,
                           @Param("departmentId") Long departmentId,
                           @Param("status") Boolean status,
                           Pageable pageable);
}
