package com.ucan.sso.server.config.freemarker.model;

import java.util.List;
import java.util.Objects;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * @Description: shiro currentUserId 模板模型
 * @author liming.cen
 * @date 2024-07-14 10:09:59
 * 
 */
public class TemplateShiroCurrentUserIdModel implements TemplateMethodModelEx {

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        Subject currentUser = SecurityUtils.getSubject();
        // 从服务器获取已存在的session，不存在时不会自动创建
        Session session = currentUser.getSession(false);
        if (!Objects.isNull(session)) {
            return session.getAttribute("currentUserId");
        }
        return null;
    }

}
