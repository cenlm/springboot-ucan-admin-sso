package com.ucan.app1.controller.system.index;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author liming.cen
 * @date 2022年12月28日 下午3:58:36
 */
@Controller
public class IndexController {
    /**
     * 进入到主界面
     * 
     * @return
     */
    @RequestMapping("/index")
    public String index(HttpServletRequest request) {
        return "home/index";
    }

    /**
     * 进入仪表盘
     * 
     * @return
     */
    @RequestMapping("/board")
    public String toBoard() {
        return "home/board";
    }
    
}
