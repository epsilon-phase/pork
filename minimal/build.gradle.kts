plugins {
  application
  id("gay.pizza.pork.module")
  id("com.github.johnrengelman.shadow") version "8.1.1"
  id("org.graalvm.buildtools.native") version "0.9.25"
}

dependencies {
  api(project(":ast"))
  api(project(":parser"))
  api(project(":frontend"))
  api(project(":evaluator"))
  api(project(":stdlib"))
  api(project(":ffi"))
  implementation(project(":common"))
}

application {
  applicationName = "pork-rt"
  mainClass.set("gay.pizza.pork.minimal.MainKt")
}

for (task in arrayOf(tasks.shadowDistTar, tasks.shadowDistZip, tasks.shadowJar)) {
  val suffix = when {
    task == tasks.shadowJar -> ""
    task.name.startsWith("shadow") -> "-shadow"
    else -> ""
  }
  task.get().archiveBaseName.set("pork-rt${suffix}")
}

graalvmNative {
  binaries {
    named("main") {
      imageName.set("pork-rt")
      mainClass.set("gay.pizza.pork.minimal.MainKt")
      sharedLibrary.set(false)
      buildArgs("-march=compatibility")
      resources {
        includedPatterns.addAll(listOf(
          ".*/*.pork$",
          ".*/*.manifest$"
        ))
      }
    }
  }
}

tasks.run.get().outputs.upToDateWhen { false }
