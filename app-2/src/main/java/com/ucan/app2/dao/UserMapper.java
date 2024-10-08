package com.ucan.app2.dao;

import java.util.List;
import java.util.Map;

import com.ucan.app2.base.BaseDao;
import com.ucan.app2.entity.User;

/**
 * 用户数据持久层
 * 
 * @author liming.cen
 * @date 2022年12月23日 下午8:36:11
 */

public interface UserMapper extends BaseDao<User> {

    /**
     * 添加<用户-职位>关系映射
     * 
     * @param map
     * @return
     */
    int addUserPostMapping(Map<String, String> map);

    List<User> queryAll();

    /**
     * 分页查询用户
     * 
     * @param user
     * @return
     */
    List<User> queryUserByPage(User user);

    /**
     * 通过组织Id查询用户总数
     * 
     * @param orgIds
     * @return
     */
    int queryUsersCountsByOrgIds(Map<String, Object> map);

    /**
     * 通过组织Id查询用户
     * 
     * @param map
     * @return
     */
    List<User> queryUsersByOrgIdsPageWithMap(Map<String, Object> map);

    /**
     * 通过职位Id查询用户总数
     * 
     * @param orgIds
     * @return
     */
    int queryCountByPostId(Map<String, Object> map);

    /**
     * 通过职位Id查询用户（分页查询）
     * 
     * @param map
     * @return
     */
    List<User> queryUsersByPostId(Map<String, Object> map);

    /**
     * 通过用户id删除<用户-职位>记录
     * 
     * @param userId
     * @return
     */
    int deleteUserPostByUserId(String userId);

    /**
     * 通过批量userId删除<用户-职位>记录
     * 
     * @param userIds
     * @return
     */
    int deleteUserPostByUserIds(List<String> userIds);

    /**
     * 通过userId统计<用户-职位>记录数
     * 
     * @param userId
     * @return
     */
    int queryUserPostCountByUserId(String userId);

    /**
     * 通过批量userId统计<用户-职位>记录数
     * 
     * @param userIds
     * @return
     */
    int queryUserPostCountByUserIds(List<String> userIds);

    /**
     * 通过用户Id和密码查询用户（修改密码时，用于校验）
     * 
     * @param user
     * @return
     */
    int queryByPasswordAndUserId(User user);

    /**
     * 修改密码
     * 
     * @param paramMap
     * @return
     */
    int updatePassword(Map<String, String> paramMap);

    /**
     * 重置密码
     * 
     * @param userId
     * @param password
     * @return
     */
    int updatePasswordReset(User user);

    /**
     * 个人设置页详情内容
     * 
     * @param map
     * @return
     */
    List<User> queryUserDetail(Map<String, String> map);
    
    /**
     * 通过用户名查询用户数（用于新增、修改用户信息时的检测）
     * 
     * @param user
     * @return 用户数统计
     */
    int queryUsersCountByName(String userName);

}
