package com.ucan.app1.shiro;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

/**
 * @Description: 上下文属性设置器。例如设置SSO 主机地址和端口号到ServletContext，方便 /pass 获取并传递到前端页面
 * @author liming.cen
 * @date 2024-10-02 10:58:44
 * 
 */
@Component
public class ServletContextAttrSetter implements ServletContextAware {

    /**
     * sso系统主机
     */
    @Value("${ucan.sso.server.host}")
    private String ssoHost;
    /**
     * sso系统端口号
     */
    @Value("${ucan.sso.server.port}")
    private String ssoPort;

    @Override
    public void setServletContext(ServletContext servletContext) {
        servletContext.setAttribute("ssoServerUrl", ssoHost + ":" + ssoPort);
    }

}
