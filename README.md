# 基于socket实现的局域网通信软件
客户端发送请求：

1.register:username:password  注册请求

2.login:username:password  登录请求

3.load:friends  加载好友列表

4.load:groups  加载群组列表

5.load:groupHistory:groupName 加载群组历史记录

6.load:friendHistory:friendName 加载好友历史记录

7.private:friendName:message

8.group:groupName:message

9.userSearch:username

服务器发送响应

1.注册/登录：success/fail

2.friends:json数据 好友列表

3.groups:json数据 群组列表

4.friendHistory:json数据

5.groupHistory:json数据

6.private:fromUser:message

7.group:groupId:fromUser:message

8.userSearch:username1,username2,...


