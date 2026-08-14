package org.dromara.demo.system.domain;

import java.util.List;

/**
 * 登录成功返回体（不含敏感信息）。
 *
 * @author demo
 * @since 2026-08-14
 */
public class LoginUser {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private List<String> roles;
    private List<String> permissions;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}
