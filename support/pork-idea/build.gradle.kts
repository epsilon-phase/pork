plugins {
  id("org.jetbrains.intellij") version "1.15.0"
  id("gay.pizza.pork.module")
}

dependencies {
  implementation(project(":parser"))
}

intellij {
  pluginName.set(properties["pluginName"].toString())
  version.set(properties["platformVersion"].toString())
  type.set(properties["platformType"].toString())
}

tasks {
  buildSearchableOptions {
    enabled = false
  }

  patchPluginXml {
    version.set(project.properties["pluginVersion"].toString())
    sinceBuild.set(project.properties["pluginSinceBuild"].toString())
    untilBuild.set(project.properties["pluginUntilBuild"].toString())
    pluginDescription.set("Pork Language support for IntelliJ IDEs")
  }
}

project.afterEvaluate {
  tasks.buildPlugin {
    exclude("**/lib/annotations*.jar")
    exclude("**/lib/kotlin*.jar")
  }
}
