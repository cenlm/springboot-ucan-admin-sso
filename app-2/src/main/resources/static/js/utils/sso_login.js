var jwt_sso = {
    /**
     * sso单点登录尝试：携带token发起登录校验请求，如果校验成功了，则会直接跳转到目标页面<br>
     * 如果校验失败，则会被过滤器重定向到登录页面<br>
     * @param requestUrl 目标地址
     */
    sso_attempt: function(requestUrl) {
        //contextPath + "/index"
        // 设置请求头 Authorization
        var token = localStorage.getItem('token');
        if (token == null || token == undefined) {//跳转到登录页面进行登录认证
            layer.msg("请您先进行登录认证~", {
                icon: 5,
                offset: [$(window).height() - 480, $(window).width() - 890]
            });
            return;
        }
        var headers = {
            'Authorization': token
        };

        // 携带请求头，发送 GET 请求到目标地址
        window.fetch(requestUrl, { headers })
            .then(response => {
                if (response.ok) {
                    // 认证成功，跳转到目标页面
                    window.location.href = requestUrl;
                } else {
                    // 请求失败
                    console.error("The header failed to send, and the SSO login authentication was terminated.");
                }
            }).catch(error => {
                console.error("Error:", error);
            });
    }
}