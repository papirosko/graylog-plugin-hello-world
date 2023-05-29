package com.graylog.plugin.helloworld

import org.graylog2.plugin.{Plugin, PluginMetaData, PluginModule}

import java.util

class HelloWorldPlugin extends Plugin {
  override def metadata(): PluginMetaData = new HelloWorldMeta

  override def modules(): util.Collection[PluginModule] = util.Collections.singleton(new HelloWorldModule)
}
