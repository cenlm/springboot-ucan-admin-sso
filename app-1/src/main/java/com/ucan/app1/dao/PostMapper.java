package com.ucan.app1.dao;

import java.util.List;

import com.ucan.app1.base.BaseDao;
import com.ucan.app1.entity.Post;
import com.ucan.app1.entity.UserPost;

/**
 * @Description: 职位Dao
 * @author liming.cen
 * @date 2023年2月10日 下午4:13:22
 */
public interface PostMapper extends BaseDao<Post> {
    /**
     * 通过组织ID查询职位信息
     * 
     * @param orgId
     * @return
     */
    List<Post> queryPostsByOrgId(String orgId);

    /**
     * 获取职位名称
     * 
     * @param postId
     * @return
     */
    String getPostNameById(String postId);

    /**
     * 统计职位节点子节点个数
     * 
     * @return
     */
    int queryPostCountByParentId(String parentId);

    /**
     * 通过职位id统计<职位-用户>记录数
     * 
     * @param postId
     * @return
     */
    int queryUserPostCountByPostId(String postId);

    /**
     * 通过userId获取<用户-职位>映射记录
     * 
     * @param userId
     * @return
     */
    List<UserPost> queryUserPostByUserId(String userId);
}
