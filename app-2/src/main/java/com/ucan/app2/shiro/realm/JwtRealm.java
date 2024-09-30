package com.ucan.app2.shiro.realm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson2.JSONObject;
import com.ucan.app2.entity.Permission;
import com.ucan.app2.entity.Role;
import com.ucan.app2.entity.RolePermission;
import com.ucan.app2.entity.User;
import com.ucan.app2.entity.UserRole;
import com.ucan.app2.service.IPermissionService;
import com.ucan.app2.service.IRolePermissionService;
import com.ucan.app2.service.IUserRoleService;
import com.ucan.app2.service.IUserService;
import com.ucan.app2.shiro.token.JwtToken;
import com.ucan.app2.shiro.util.JwtBase64Util;
import com.ucan.app2.shiro.util.JwtTokenUtil;

/**
 * @author liming.cen
 * @date 2022年12月25日 上午10:36:08
 */
public class JwtRealm extends AuthorizingRealm {
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserRoleService userRoleService;
    @Autowired
    private IRolePermissionService rolePermissionService;
    @Autowired
    private IPermissionService permissionService;
    @Autowired
    private JwtTokenUtil tokenUtil;
    /**
     * 远程SSO token校验 接口地址
     */
    @Value("${ucan.sso.server.verify}")
    private String ssoTokenVerifyUrl;

    /**
     * token 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken jwtToken) throws AuthenticationException {
        String token = (String) jwtToken.getPrincipal();
        /**
         * 验证客户端access token，如果验证失败，则去验证该用户的refresh token refresh token
         * 验证成功后会尝试返回新access token
         */
        String result = tokenUtil.verifyAccessToken(token);
        JSONObject jsonObject = JSONObject.parseObject(result);
        Integer code = jsonObject.getInteger("code");
        if (code == 0) {// token验证成功（包括通过refresh token协助的认证）
            String newAccessToken = jsonObject.getString("data");
            if (!Objects.isNull(newAccessToken) && !newAccessToken.equals("")) {// 说明旧token验证失败，有新token生成
                JwtAuthenticationInfo jwtAuthenticationInfo = new JwtAuthenticationInfo(newAccessToken, newAccessToken,
                        getName());
                jwtAuthenticationInfo.setNewAccessToken(newAccessToken);
                return jwtAuthenticationInfo;
            } else {// 旧token依然有效
                return new JwtAuthenticationInfo(token, token, getName());
            }
        } else {
            throw new AuthenticationException(jsonObject.getString("msg"));
        }
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        String token = (String) principals.getPrimaryPrincipal();
        JSONObject payload = JwtBase64Util.getPayload(token);
        String userName = payload.getString("userName");
        Map<String, User> map = new HashMap<>();
        Set<String> roleIds = new HashSet<>();
        Set<String> roleCodes = new HashSet<>();
        // 通过用户名获取角色，再从角色获取权限
        List<UserRole> userRoles = getRoles(userName, map);
        if (userRoles.size() > 0) {
            userRoles.forEach(item -> {
                roleIds.add(item.getRoleId());
                Role role = item.getRole();
                if (null != role) {
                    roleCodes.add(role.getRoleCode());
                }
            });

        }
        Set<String> permissionCodes = getPermissions(roleIds, map);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(roleCodes);
        info.setStringPermissions(permissionCodes);
        return info;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 获取某用户的所有角色ID和角色编码
     * 
     * @param userName
     * @return
     */
    private List<UserRole> getRoles(String userName, Map<String, User> map) {
        User user = userService.queryByName(userName);
        List<UserRole> userRoles = new ArrayList<>();
        try {
            if (null == user) {
                map.put("user", user);
                throw new UnknownAccountException("抱歉，没有查询到该用户！");
            } else {
                map.put("user", user);
                if (user.getIsSuper() == 1) {// 如果是超级管理员，则直接获取所有roleId
                    userRoles.addAll(userRoleService.queryAllRoleIds());
                } else {
                    userRoles.addAll(userRoleService.queryRolesByUserId(user.getUserId()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userRoles;
    }

    /**
     * 获取某用户的所有权限ID和权限编码
     * 
     * @param roleIds
     * @return
     */
    private Set<String> getPermissions(Set<String> roleIds, Map<String, User> map) {
        List<RolePermission> rolePermissions = null;
        Set<String> permissionSet = new HashSet<String>();
        User user = map.get("user");
        if (null != user) {
            if (user.getIsSuper() == 1) {// 如果是超级管理员，则直接获取所有权限id
                List<Permission> allPermissions = permissionService.queryAllPermissions();
                if (allPermissions.size() > 0) {
                    allPermissions.forEach(item -> {
                        permissionSet.add(item.getPermissionCode());
                    });
                }
            } else {
                for (String roleId : roleIds) {
                    rolePermissions = rolePermissionService.queryPermissionsByRoleId(roleId);
                    if (rolePermissions.size() > 0) {
                        for (RolePermission rolePermission : rolePermissions) {
                            // 以权限ID作为权限标识
                            // permissionSet.add(rolePermission.getPermissionId());
                            // 以权限编码作为权限标识
                            permissionSet.add(rolePermission.getPermission().getPermissionCode());
                        }
                    }
                }
            }
        } else {
            throw new UnknownAccountException("抱歉，没有查询到该用户！");
        }

        return permissionSet;
    }

}
