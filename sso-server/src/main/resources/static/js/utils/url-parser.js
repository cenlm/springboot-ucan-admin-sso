/**
 * url解析工具
 */
var url_parser = {
    /**
     * 获取当前页面url中的指定参数值
     */
    getUrlParam: function(name) {
        //构造一个含有目标参数的正则表达式对象
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        //匹配目标参数
        var r = window.location.search.substr(1).match(reg);
        //返回参数值，如果没有匹配到，则返回null
        if (r != null) return unescape(r[2]);
        return null;
    },
    /**
        * 获取请求协议
        */
    getProtocol: function(url) {
        var urlObj = new URL(url);
        var protocol = urlObj.protocol.slice(0, -1); // 去除最后的 ':'
        return protocol;

    },
    /**
    * 获取url根域名
    */
    getRootDomain: function(url) {
        var urlObj = new URL(url);
        var host = urlObj.host;
        // 假设根域名不带 www 等前缀，找到第一个点之后的部分并截取其前面的内容作为根域名
        var firstDotIndex = host.indexOf('.');
        var secondDotIndex = host.indexOf('.', firstDotIndex + 1);
        var rootDomain = secondDotIndex !== -1 ? host.substring(firstDotIndex + 1) : host;
        return rootDomain;
    },
    /**
   * 获取url端口号
   */
    getPort: function(url) {
        var urlObj = new URL(url);
        var port = urlObj.port;
        return port;
    }
}