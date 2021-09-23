package com.yusys.pipeline.utils

import groovy.json.JsonSlurperClassic
import groovy.json.JsonBuilder

class Handler implements Serializable {

  def steps
  Handler(steps) {this.steps = steps}

  def sendStartMsg() {
    def data = new StartData(steps.params.flowId,
                             steps.params.flowInstanceId,
                             steps.params.taskInstanceId,
                             steps.env.JOB_NAME,
                             steps.env.BUILD_ID,
                             steps.env.BUILD_URL)
    msgPublisher(data.toString(), Constants.RABBITMQ_EXCHANGE, Constants.RABBITMQ_START_ROUTING_KEY_DEV)
  }

  def sendEndMsg(extraData = []) {
    def data = new EndData(steps.params.flowId,
                           steps.params.flowInstanceId,
                           steps.params.taskInstanceId,
                           steps.env.JOB_NAME,
                           steps.env.BUILD_ID,
                           steps.currentBuild.currentResult)
    data.addExtraData(extraData)
    msgPublisher(data.toString(), Constants.RABBITMQ_EXCHANGE, Constants.RABBITMQ_END_ROUTING_KEY_DEV)

  }

  def msgPublisher(data, exchange, routingKey) {
    steps.rabbitMQPublisher conversion: false, data: data, exchange: exchange, rabbitName: 'rabbitmq', routingKey: routingKey
  }

  def dockerBuild(encryptedJsonString) {
    def dockerBuildDataList = new JsonSlurperClassic().parseText(new String(encryptedJsonString.decodeBase64()))
    for(data in dockerBuildDataList) {
      def tags = data.tags.join(" -t ")
      def script = """
          set +x
          echo ${data.password} | docker login -u ${data.username}  --password-stdin ${data.repository} >/dev/null 2>&1
          set -x
          docker build -f ${data.dockerfile} -t ${tags} ${data.content}
          """
      steps.sh script
      script = "docker push ${tags}"
      if (data.repository?.trim() && data.repository != "null") {
      steps.sh script
    }
  }
}