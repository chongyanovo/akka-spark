<a align="center" href="https://github.com/ChongYanOvO/akka-spark">
<img src="https://github-readme-streak-stats.herokuapp.com?user=ChongyanOvO&hide_border=true&locale=zh_Hans&date_format=M%20j%5B%2C%20Y%5D&mode=weekly](https://git.io/streak-stats)" />
</a>

# akka-spark
使用 akka 框架构建 Spark 通讯功能

## 整体架构介绍
- `common` 包: **MessageProtocol** 消息协议
- `master` 包: **Master.scala**文件
- `worker` 包: **Worker.scala**文件

## 通讯执行流程
1. Worker 注册到 Master 中,并回复 Worker 注册成功
2. Worker 定时发送心跳包,并在 Master 上接收心跳包
3. Master 接收心跳包确认 Worker 存活,并更新该 Worker 最后一次心跳时间
4. Master 启动定时任务,定期检查 Worker 最后一次心跳时间是否超时
5. 如果超时则将超时的 Worker 从 Master 中的注册中心移除
