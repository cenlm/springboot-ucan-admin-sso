package com.ucan.app1.dao;

import java.util.List;

import com.ucan.app1.base.BaseDao;
import com.ucan.app1.entity.Role;
import com.ucan.app1.entity.User;
import com.ucan.app1.entity.UserRole;

/**
 * @author liming.cen
 * @date 2022年12月24日 下午1:38:22
 */
public interface UserRoleMapper extends BaseDao<UserRole> {

    /**
     * 通过用户Id查询角色
     * 
     * @param userId
     * @return
     */
    List<UserRole> queryRolesByUserId(String userId);

    /**
     * 查询所有roleId，主要用于超级管理员授权
     * 
     * @return
     */
    List<UserRole> queryAllRoleIds();

    /**
     * 查询角色成员（多表联查）
     * 
     * @param roleId
     * @return
     */
    List<UserRole> queryRoleUsersByPage(UserRole userRole);
}
