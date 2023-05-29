package com.graylog.plugin.helloworld

import org.slf4j.{Logger, LoggerFactory}

class HelloWorldService {

  private val log: Logger = LoggerFactory.getLogger(getClass)

  log.info("Hello World!")

}
