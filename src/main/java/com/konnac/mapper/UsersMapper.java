package com.konnac.mapper;

import com.konnac.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UsersMapper {

    //添加员工
    void addUser(User user);

    //删除员工
    void deleteUser(Integer[] ids);

    //根据id查询员工
    @Select("select * from users where id = #{id}")
    User getUserById(Integer id);

    //修改员工
    void updateUser(User user);

    //分页查询
    List<User> list(Integer id, String username, String realName, User.UserRole role, LocalDate begin, LocalDate end);
}
