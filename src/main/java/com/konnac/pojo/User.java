package com.konnac.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;// 用户id
    private String username;// 用户名
    private String password;// 密码
    private UserRole role; // ADMIN, PROJECT_MANAGER, EMPLOYEE
    private String realName;// 真实姓名
    private String email; //要做邮箱验证
    private LocalDateTime createTime; //创建时间
    private LocalDateTime updateTime; //修改时间

    @Getter
    public enum UserRole {
        ADMIN("管理员"),
        PROJECT_MANAGER("项目经理"),
        EMPLOYEE("普通员工");

        private final String description;

        UserRole(String description) {
            this.description = description;
        }
    }
}


