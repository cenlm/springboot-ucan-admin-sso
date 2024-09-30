<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport"
	content="width=device-width, initial-scale=1, maximum-scale=1">
<title>处理中...</title>
</head>
<body>
	<#assign contextPath="${request.contextPath}" />

	

	<script src="js/jquery-3.6.3.min.js"></script>
	<script src="js/utils/sso_login.js"></script>

	<script type="text/javascript">
		
	    //获取当前页面url
                        const currentURL = window.location.href;
                        var protocol = getProtocol(currentURL);
                        var rootDomain = getRootDomain(currentURL);
                        var port = getPort(currentURL);
                        var url = '';
                        if (port == "") {//组装目标url
                            url = protocol + '://' + rootDomain;
                        } else {
                            url = protocol + '://' + rootDomain + ":" + port;

                        }
                        const urlParams = new URLSearchParams(window.location.search);
                        const fromLogout = urlParams.get('fromLogout');
                       
                        window.location.href = "http://login.ucan.com/jump?target="+encodeURIComponent(url)+"&fromLogout="+fromLogout;
	   /**
 * 获取协议
 */
function getProtocol(url) {
    var urlObj = new URL(url);
    var protocol = urlObj.protocol.slice(0, -1); // 去除最后的 ':'
    return protocol;

}
/**
 * 获取根域名
 */
function getRootDomain(url) {
    var urlObj = new URL(url);
    var host = urlObj.host;
    // 假设根域名不带 www 等前缀，找到第一个点之后的部分并截取其前面的内容作为根域名
    var firstDotIndex = host.indexOf('.');
    var secondDotIndex = host.indexOf('.', firstDotIndex + 1);
    var colonIndex = host.indexOf(':');
    var rootDomain = '';
    if(colonIndex==-1){
        rootDomain = secondDotIndex !== -1 ? host.substring(firstDotIndex+1) : host;
    }else{
        rootDomain = secondDotIndex !== -1 ? host.substring(firstDotIndex+1,colonIndex) : host.substring(0,colonIndex);
    }
    return rootDomain;
}
/**
 * 获取端口号
 */
function getPort(url) {
    var urlObj = new URL(url);
    var port = urlObj.port;
    return port;
}
	   
	</script>
</body>
</html>
