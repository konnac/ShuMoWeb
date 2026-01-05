package com.konnac.mapper;

import com.konnac.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UsersMapper {

    /**
     *  添加员工
     */
    void addUser(User user);

    /**
     *  删除员工(软删除)
     */
    void deleteUser(Integer[] ids);

    /**
     *  根据id查询
     */
    @Select("select * from users where id = #{id}")
    User getUserById(Integer id);

    /**
     * 修改员工(普通员工)
     */
    void updateUser(User user);

    /**
     *  修改员工(管理员)
     */
    void updateUserAdmin(User user);

    /**
     *  分页条件查询
     */
    List<User> list(Integer id, String username, String realName, User.UserRole role, LocalDate begin, LocalDate end);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(1) FROM users WHERE username = #{username} AND id != #{excludeId}")
    boolean existsByUsername(@Param("username") String username, @Param("excludeId") Integer excludeId);

    /**
     * 统计员工总数
     */
    @Select("SELECT COUNT(1) FROM users")
    long countUsers();
}
