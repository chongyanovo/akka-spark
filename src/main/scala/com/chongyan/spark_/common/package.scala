package com.chongyan.spark_

/**
 * Created by chongyan 
 * at 2022/8/19 09:44
 * description: MessageProtocol 消息协议
 */
package object common {
  // Worker 注册信息
  case class RegisterWorkerInfo(id: String, cpu: Int, ram: Int)

  // WorkerInfo,保存在 Master 中的 HashMap 中
  class WorkerInfo(val id: String, val cpu: Int, val ram: Int){
    var lastHeartBeat:Long = System.currentTimeMillis()
  }

  // 当 Worker 注册成功,服务器返回一个 RegisterWorkerInfo 信息
  case object RegisterWorkerInfo

  // Worker 每个一段时间由定时器发给自己的一个消息
  case object SendHeartBeat
  // Worker 每隔一段时间由定时器触发,而向 Master 发送的消息协议
  case class HeartBeat(id:String)

  // Master 给自己发送一个触发检查超时 Worker 的消息
  case object StartTimeOutWorker
  // Master 给自己发消息,检查 Worker,
  case object RemoveTimeOutWorker
}
