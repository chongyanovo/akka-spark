package com.chongyan.spark_.master

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.chongyan.spark_.common.{HeartBeat, RegisterWorkerInfo, RemoveTimeOutWorker, StartTimeOutWorker, WorkerInfo}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

/**
 * Created by chongyan 
 * at 2022/8/19 09:10
 * description: Master 类
 */
class Master extends Actor {
  // 定义一个 HashMap
  val workers = mutable.Map[String, WorkerInfo]()

  override def receive: Receive = {
    case "start" => {
      println("Master 服务器启动...")
      self ! StartTimeOutWorker
    }
    case RegisterWorkerInfo(id, cpu, ram) => {
      // 接收到 Worker 的消息
      // 创建 RegisterWorkerInfo 对象
      val workerInfo: WorkerInfo = new WorkerInfo(id, cpu, ram)
      // 加入 workers
      workers += (id -> workerInfo)
      println(s"Master中注册的 workers:${workers}")
      // 回复消息,注册成功
      sender() ! RegisterWorkerInfo
    }
    case HeartBeat(id) => {
      // 更新对应的心跳时间
      // 从 Workers 中取出 workInfo
      val workerInfo: WorkerInfo = workers(id)
      workerInfo.lastHeartBeat = System.currentTimeMillis()
      println(s"master更新了${id}的心跳时间...")

    }
    case StartTimeOutWorker => {
      println("开始定时检测心跳任务...")
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 6000 millis, self, RemoveTimeOutWorker)
    }
    // 对 RemoveTimeOutWorker 消息处理
    // 需要检查哪些 Worker 心跳超时,并从 workers 中移除
    case RemoveTimeOutWorker => {
      // 取出 workers 中所有 workInfo
      val workerInfos: Iterable[WorkerInfo] = workers.values
      val nowTime: Long = System.currentTimeMillis()
      // 先把超时的所有 workerInfo 删除
      workerInfos.filter(workerInfo => (nowTime - workerInfo.lastHeartBeat > 6000))
        .foreach(workerInfo => workers.remove(workerInfo.id))
      println(s"当前有:${workers.size}个 worker 存活")
    }
  }
}

object Master {
  def main(args: Array[String]): Unit = {
    // 先创建 ActorSystem
    val config = ConfigFactory.load("master.conf")

    val masterActorSystem: ActorSystem = ActorSystem("Master", config)
    // 创建 Master -actor 引用
    val masterActorRef: ActorRef = masterActorSystem.actorOf(Props[Master], "master")
    // 启动 Master
    masterActorRef ! "start"
  }
}