layui.use(['form'], function() {
    var form = layui.form;
    form.render("checkbox");
    form.on('submit(userForm)', function(data) {
        var targetParam = url_parser.getUrlParam("target");
        var formData = form.val('userForm');
        if (targetParam != null) {//表单target数据
            formData.target = targetParam;
        }
        let requestUrl = contextPath + "/login";
        let loadIndex = layer.load(0);
        //连接socket服务端
        login_check(formData.username);
        $.ajax({
            type: "POST",
            url: requestUrl,
            data: formData,
            dataType: "json",
            success: function(result) {
                if (result.code == 0) {
                    layer.close(loadIndex);
                    layer.msg(result.msg, {
                        icon: 6,
                        shade: 0.3,
                        offset: [$(window).height() - 480, $(window).width() - 890]
                    });
                    setTimeout(function() {
                        //跳转到后端返回的目标地址的 /addToken ，用于目标根域名下的tokenCookie的设置
                        if (result.data != "") {
                            window.location.href = result.data;
                        }
                    }, "3000");
                } else {
                    layer.close(loadIndex);
                    layer.msg(result.msg, {
                        icon: 5,
                        shade: 0.3,
                        offset: [$(window).height() - 480, $(window).width() - 890]
                    });
                }
            },
            error: function(e) {
                layer.msg(e.responseText, {
                    icon: 2,
                    shade: 0.3,
                    offset: [$(window).height() - 480, $(window).width() - 890]
                });
            }
        });
        //防止表单重复提交
        return false;
    });

    function login_check(userName) {
        if ("WebSocket" in window) {
            console.log("您的浏览器支持 WebSocket!");

            // 创建一个 websocket
            var ws = new WebSocket("ws://localhost:9999");
            //消息窗口timeOut 秒后消失
            var timeOut = 5;
            ws.onopen = function() {
                // Web Socket 已连接上，使用 send() 方法发送数据
                //加 _login_js 后缀标识从哪个页面发送socket消息
                ws.send(userName + "_login");
                console.log("已连接到服务器");

            };

            ws.onmessage = function(evt) {
                var received_msg = evt.data;
                console.log("服务器返回数据：" + received_msg);
                if (received_msg != "") {
                    layer.msg(received_msg, {
                        icon: 5,
                        time: 6000
                    });
                }
            };

            ws.onclose = function() {
                // 关闭 websocket
                console.log("连接已关闭...");
            };
        }

        else {
            // 浏览器不支持 WebSocket
            alert("您的浏览器不支持 WebSocket!");
        }
    }

    /**
     * 获取当前页面url中的指定参数值，用于获取http://login.ucan.com/toLogin?target=XXX 中的target参数
     */
    function getUrlParam(name) {
        //构造一个含有目标参数的正则表达式对象
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        //匹配目标参数
        var r = window.location.search.substr(1).match(reg);
        //返回参数值，如果没有匹配到，则返回null
        if (r != null) return unescape(r[2]);
        return null;
    }
});