package com.graylog.plugin.helloworld

class HelloWorldModule extends org.graylog2.plugin.PluginModule {

  override def configure(): Unit = {
    bind(classOf[HelloWorldService]).asEagerSingleton()
  }

}
