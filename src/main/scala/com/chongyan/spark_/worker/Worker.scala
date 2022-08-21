package com.chongyan.spark_.worker

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.chongyan.spark_.common.{HeartBeat, RegisterWorkerInfo, SendHeartBeat}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import java.util.UUID

/**
 * Created by chongyan 
 * at 2022/8/19 09:10
 * description: Worker
 */
class Worker(masterUrl: String) extends Actor {
  // masterProxy 是 Master 的代理/引用ref
  var masterProxy: ActorSelection = _
  val id: String = UUID.randomUUID().toString

  override def preStart(): Unit = {
    // 初始化 masterProxy
    masterProxy = context.actorSelection(masterUrl)
    println("masterProxy:" + masterProxy)
  }

  override def receive: Receive = {
    case "start" => {
      println("Worker 启动了...")
      // 发送一个注册消息
      val registerWorkerInfo: RegisterWorkerInfo = new RegisterWorkerInfo(id, 16, 16 * 1024)
      masterProxy ! registerWorkerInfo
      println(s"registerWorkerInfo:${registerWorkerInfo}")
    }
    case RegisterWorkerInfo => {
      println(s"worker:${id} 注册成功!")
      // 当触发成功后就定义一个定时器,每隔一段时间,发送 SendHeartBeat 消息给自己
      import context.dispatcher
      // 0 millis 表示不延时,立即执行触发
      // 3000 millis 表示每隔 3 秒执行一次
      // self 表示发送目标
      // SendHeartBeat 发送消息内容
      context.system.scheduler.schedule(0 millis, 3000 millis, self, SendHeartBeat)
    }
    case SendHeartBeat => {
      println(s"worker:${id} 发送心跳信息")
      masterProxy ! HeartBeat(id)
    }
  }
}

object Worker {
  def main(args: Array[String]): Unit = {

    val masterUrl: String = "akka.tcp://Master@127.0.0.1:10005/user/master"
    val workerName = "worker"
    val config = ConfigFactory.load("worker.conf")

    // 先创建 ActorSystem
    val workerActorSystem: ActorSystem = ActorSystem("Worker", config)

    // 创建 Worker 的引用/代理
    val workerActorRef: ActorRef = workerActorSystem.actorOf(Props(new Worker(masterUrl)), workerName)

    // 启动 actor
    workerActorRef ! "start"
  }
}