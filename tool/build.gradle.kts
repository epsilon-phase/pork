plugins {
  application
  pork_module
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("org.graalvm.buildtools.native") version "0.9.25"
}

dependencies {
  api(project(":ast"))
  api(project(":parser"))
  api(project(":frontend"))
  api(project(":evaluator"))
  implementation(libs.clikt)
  implementation(project(":common"))
}

application {
  mainClass.set("gay.pizza.pork.tool.MainKt")
}

graalvmNative {
  binaries {
    named("main") {
      imageName.set("pork")
      mainClass.set("gay.pizza.pork.tool.MainKt")
      sharedLibrary.set(false)
    }
  }
}

tasks.run.get().outputs.upToDateWhen { false }