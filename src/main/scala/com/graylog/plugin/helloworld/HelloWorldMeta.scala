package com.graylog.plugin.helloworld

import org.graylog2.plugin.{PluginMetaData, ServerStatus, Version}

import java.net.URI
import java.util
import java.util.Collections

class HelloWorldMeta extends PluginMetaData {

  // we will set the plugin ID to the meta classname
  override def getUniqueId: String = "com.graylog.plugin.helloworld.HelloWorldMeta"

  override def getName: String = "Hello World"

  override def getAuthor: String = "Vladimir Penkov"

  override def getURL: URI = URI.create("https://google.com");

  override def getVersion: Version = Version.from(1, 0, 0, "unknown")

  override def getDescription: String = "Plugin example"

  override def getRequiredVersion: Version = Version.from(5, 0, 0, "unknown")

  override def getRequiredCapabilities: util.Set[ServerStatus.Capability] = Collections.emptySet()
}
