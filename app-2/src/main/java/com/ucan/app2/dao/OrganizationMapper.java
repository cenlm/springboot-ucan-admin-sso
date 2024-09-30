package com.ucan.app2.dao;

import java.util.List;

import com.ucan.app2.base.BaseDao;
import com.ucan.app2.entity.Organization;

/**
 * @Description: 组织结构Dao
 * @author liming.cen
 * @date 2023年2月10日 下午4:13:22
 */
public interface OrganizationMapper extends BaseDao<Organization> {

    /**
     * 查询所有组织结构数据
     * 
     * @return
     */
    List<Organization> queryAllOrganizations();

    /**
     * 查找当前节点的直接子节点orgId
     * 
     * @param parentId
     * @return
     */
    List<Organization> queryOrgIdsByParentId(String parentId);

    /**
     * 通过orgId查询组织名称
     * 
     * @param orgId
     * @return
     */
    String getOrgNameById(String orgId);
}
