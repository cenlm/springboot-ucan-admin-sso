package com.ucan.app1.config.freemarker.model;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.alibaba.fastjson2.JSONObject;
import com.ucan.app1.shiro.util.JwtBase64Util;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * @Description: shiro principal 模板模型，返回String类型
 * @author liming.cen
 * @date 2024-07-14 10:09:59
 * 
 */
public class TemplateShiroPrincipalModel implements TemplateMethodModelEx {

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        Subject currentUser = SecurityUtils.getSubject();
        JSONObject payload = JwtBase64Util.getPayload(String.valueOf(currentUser.getPrincipal()));
        return payload.getString("userName");
    }

}
