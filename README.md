# ucan-admin

### 项目简介

该项目是基于 Shiro + JWT令牌 实现的SSO系统。<br>
系统使用 Shiro进行用户认证与授权管理，JWT令牌作为用户登录凭证，使用Redis作为Shiro管理的session以及JWT 的RefreshTokenCookie的存储介质，<br>
实现了用户单点登录认证（主要功能）与授权的基本功能。
技术栈:<br>
SpringBoot + Shiro + JWT + Mysql + Redis + LayUi（及LayUi第三方插件） + FreeMarker<br>

**功能简介**<br>
1.用户登录认证、授权、Session管理、JWT凭证的生成、认证和刷新<br>
2.组织结构管理、职位管理、用户管理、组织分配。<br>
3.角色管理：<br>
	3.1 角色基本信息管理。<br>
	3.2 角色分层、角色互斥、角色权限继承、角色分配、权限分配、角色用户数限制（待办）等。<br>
4.权限管理。<br>

### 单点登录原理流程图

![Image text](https://gitee.com/mrcen/ucan-admin/raw/master/src/main/webapp/imgs/login-effect.png)<br>



### 数据库表关系图

**1.数据库表关系图：**<br>
![Image text](https://gitee.com/mrcen/ucan-admin/raw/master/src/main/webapp/imgs/db-erd.png)<br>

**注：**上图只展示了主要的关联字段，详细表字段，请查看src\main\resources\static\database目录的ucan_admin.sql文件<br>

**表名称说明：**<br>

users: 用户表<br>
organization: 组织表（有上下级关系）<br>
post: 职位表（有上下级关系）<br>
roles: 角色表（有上下级关系）<br>
permissions: 权限表（有上下级关系）<br>
user_organization: <用户-组织>关系表<br>
user_post: <用户-职位>关系表<br>
role_organization: <角色-组织>关系表<br>
role_post: <角色-职位>关系表<br>
role_permission: <角色-权限>关系表<br>
mutex_roles: 互斥角色表<br>

**注：**所有数据表仅在代码逻辑上做了外键约束，数据表结构未添加外键约束，可自行在数据表添加外键约束（如需要）。<br>


### 运行环境

1.  JDK 1.8+
2.  Tomcat-8.5.63
3.  SpringBoot2.7.9
4.  Mybatis-3.5.9
5.  shiro-core 1.10.1
6.  Redis
7.  Mysql 5.7.33
8.  Layui 2.7.6
9.  Maven
10. JWT

**项目安装、运行步骤**

1. 在本地准备好Java 8运行环境以及Tomcat。
2. 安装Mysql数据库，并新建ucan_admin数据库，运行项目中的ucan_admin.sql文件。
3. 将项目导入你自己的开发工具 eclipse中。
4. 分别修改app-1、app-2、sso-server 的 src/main/resources 目录下的application-dev.yml 文件中的用户名、密码、端口号为你所使用的数据库连接信息。
5. 在相应的主机上进行DNS域名解析配置，如：app-1、app-2、sso-server直接在当前windows系统上运行，那么就去修改 C:\Windows\System32\drivers\etc目录下的hosts文件：

127.0.0.1 localhost
127.0.0.1 ucan.com
127.0.0.1 www.ucan.com
127.0.0.1 login.ucan.com
127.0.0.1 umall.com
127.0.0.1 www.umall.com

然后 cmd 窗口执行：ipconfig /flushdns 

6. 安装 Redis。
7. 分别在app-1、app-2、sso-server 的 src/main/resources 目录下的application.yml 配置 Redis服务地址/域名、端口号，以及app-1、app-2的SSO系统的相关调用服务地址配置。
8. 启动 sso-server、app-1、app-2 项目，其端口号分别为：80/8080/8082

说明：如果sso-server、app-1、app-2 在同一台主机上运行，则可以跳过步骤7 ，否则请根据你的实际情况进行mysql、redis、DNS的配置。

用户名：admin  密码：123456<br>
用户名：小王  密码：123456<br>


### 功能描述

**组织架构、职位、用户、角色、权限管理功能概述**<br>

a. 组织节点之间有上下级关系，如总公司、分公司、总公司部门、分公司部门等（也可以是你认为合理的任何组织架构）；<br>
b. 你可以添加任何节点的同级节点与子节点（右击弹窗操作），其中“超级管理员节点”不可删除，主要为了维护超级管理员与组织结构之间的关系。<br>
c. 任何组织架构节点可直接新增职位，职位也可以有上下级关系。<br>
d. 新增用户时，必须先选择职位，否则不允许操作（后续如果新增了其他类型的分组再进行逻辑修改）。<br>
e. 角色、权限各自的CURD操作。<br>
f. <用户-组织>、<用户-职位>、<用户-角色>、<用户-权限>关系处理逻辑：<br>
![图片](https://gitee.com/mrcen/ucan-admin/raw/master/src/main/webapp/imgs/user-role-perm.png)<br>
g. 最后通过shiro标签或注解进行资源访问权限控制。<br>

**1. 登录模块**<br>

    注：假设存在 app1、app2、sso认证系统

1.1 用户在任何子系统进行登录/未认证被拦截时，都会跳转到SSO系统的登录页面；<br>
1.2 用户访问app1任何存在的url（除了/logout），如果app2已经成功登录，那么用户会直接跳转到app1的 index 页面；<br>
1.3 账号登录失败次数限制：<br>
    &nbsp;&nbsp;用户连续登录失败次数小于5期间，如果有一次登录成功，那么该用户登录失败次数、限制登录时长将清零；<br>
    &nbsp;&nbsp;用户连续5次登录失败时，提示"15分钟后再进行登录操作"，并开始记录限制登录时长15分钟；<br>
    &nbsp;&nbsp;用户连续10次登录失败时，提示"45分钟后再进行登录操作"，此时限制登录时长更新为45分钟；<br>
    &nbsp;&nbsp;用户连续15次登录失败时，提示"连续 15 次登录失败，再次尝试会被限制登录！"，限制登录时长为45分钟；<br>
     &nbsp;&nbsp;用户第16次登录失败时，提示"连续 16 次登录失败，该账号已被限制登录，请联系管理员！"，登录被拦截，后端对该用户的账号信息查询被终止；<br>
    &nbsp;&nbsp;在限制登录时段内，即使用户输入了正确的用户名、密码，依旧限制用户登录操作；过了限制登录时段，用户登录失败次数、限制登录时长自动清零，用户可以再次进行登录操作；<br>
    &nbsp;&nbsp; 用户登录失败次数小于15次时，如果用户有一次登录成功了，那么系统会清除该用户登录失败信息记录；<br>
    &nbsp;&nbsp; 管理员可以从后台页面手动解除用户的登录限制；<br>
    &nbsp;&nbsp;用户登录成功，系统自动完成用户授权，进行资源访问控制。

**2. 仪表盘**<br>

 &nbsp;&nbsp;board.ftl 加入echarts图表及layui表格的静态页面，未做更进一步的功能开发。<br>

**3. 用户管理**<br>

3.1 包含组织架构、职位、用户的CURD、分配组织、重置密码等功能，实现对相关信息有组织的管理。<br>
&nbsp;&nbsp;a.删除组织节点的时候，与其关联的节点的资源必须先得到释放。<br>
例如，公司节点有部门节点（关联着职位），则要先删除部门节点（删除职位），部门节点关联着职位，则必须先删除职位；如果职位还分配着用户，则要先删除用户。删除组织结构节点、职位节点时，相应的<组织-角色>、<职位-角色>、<角色-权限>的映射关系会自动解除。<br>
&nbsp;&nbsp;b. 用户信息管理：包含新增用户、查看及修改用户基本信息，为用户分配组织（主要是为了通过组织批量分配角色，达到批量分配权限的目的）。<br>
&nbsp;&nbsp;c. 新增组织、职位、用户的时候，系统自动为其分配基础角色，从而达到分配基础权限的目的。<br>
&nbsp;&nbsp;d. 用户状态为“禁用”时，用户不可登录系统。<br>
&nbsp;&nbsp;e. 个人基本信息设置与密码修改。<br>

**4. 角色管理**<br>
该模块包含角色基本信息的CURD、角色成员列表、角色权限分配、角色组织(职位)分配、角色互斥管理。<br>
&nbsp;&nbsp;a. 角色有上下级关系，上级角色继承其子孙角色的所有权限，子孙角色不可越权。例如，为会计助理分配权限的时候，只能选择会计已有的权限（通过后台数据及前端代码控制复选框是否可选）。<br>
&nbsp;&nbsp;b. 将角色分配给组织、职位，从而间接达到为用户分配权限的目的。<br>
&nbsp;&nbsp;c. 互斥角色管理：例如，会计和财务审核员不能同时分配给给某一个员工，这也意味着将角色分配给组织和职位的时候，系统会自动检查已勾选的组织、职位已分配的角色与待分配的角色之间是否存在互斥关系，如果存在，则不允许此次角色分配，需要按提示进行相应角色关系处理。<br>


**5. 权限管理**<br>
权限基本信息的CURD，删除权限时会自动解除<角色-权限>映射关系。<br>

---
**SSO单点登录逻辑参考：**<br>

[https://youngzhang08.github.io/2018/08/08/%E8%81%8A%E8%81%8A%E9%98%BF%E9%87%8C%E6%B7%98%E5%AE%9DSSO%E8%B7%A8%E5%9F%9F%E7%99%BB%E5%BD%95%E8%BF%87%E7%A8%8B/](https://youngzhang08.github.io/2018/08/08/%E8%81%8A%E8%81%8A%E9%98%BF%E9%87%8C%E6%B7%98%E5%AE%9DSSO%E8%B7%A8%E5%9F%9F%E7%99%BB%E5%BD%95%E8%BF%87%E7%A8%8B/)<br>


**RBAC业务逻辑参考：**<br>

[https://www.cnblogs.com/iceblow/p/11121362.html](https://www.cnblogs.com/iceblow/p/11121362.html)<br>
[https://juejin.cn/post/7121977695197970463](https://juejin.cn/post/7121977695197970463)<br>



RBAC权限管理系统的具体业务跟具体需求有关，欢迎大家的指正与交流。<br>

其他项目请移步：

[Gitee：springboot-ucan-admin](https://gitee.com/mrcen/springboot-ucan-admin)<br>
[Github：springboot-ucan-admin](https://github.com/cenlm/springboot-ucan-admin)


