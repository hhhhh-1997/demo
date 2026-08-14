package org.dromara.demo.system.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 用户新增/修改请求体。
 *
 * @author demo
 * @since 2026-08-14
 */
public class SysUserDTO {

    /** 新增校验分组。 */
    public interface Create {
    }

    /** 修改校验分组。 */
    public interface Update {
    }

    @NotBlank(message = "用户名不能为空", groups = {Create.class, Update.class})
    private String username;

    @NotBlank(message = "密码不能为空", groups = Create.class)
    private String password;

    private String nickname;
    private Long deptId;
    private Integer status;
    private List<Long> roleIds;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
