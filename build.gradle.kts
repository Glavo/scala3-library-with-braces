// https://github.com/SimY4/xpath-to-xml/blob/java6/xpath-to-xml-scala/build-3.gradle

plugins {
    `java-library`
    `maven-publish`
    signing
    id("de.undercouch.download") version "4.1.1"
}

group = "org.glavo"
version = "3.0.0-RC3-3"

val scalaVersion = "3.0.0-RC3"
val useMirror = project.findProperty("useMirror") == "true"
val classesDir = file("$buildDir/classes/scala/main")
val sourceDir = file("$buildDir/download/dotty-$scalaVersion/library/src")
val downloadDir = file("$buildDir/download")

repositories {
    if (useMirror) {
        maven(url = "https://maven.aliyun.com/repository/central")
    } else {
        mavenCentral()
    }
}

val dotc by configurations.creating {}

dependencies {
    //dotc("org.scala-lang:scala3-library_$scalaVersion:$scalaVersion")
    dotc("org.scala-lang:scala3-compiler_$scalaVersion:$scalaVersion")
    //dotc("org.scala-lang:scala3-scaladoc_$scalaVersion:$scalaVersion")
}


val scalaCompilerOptions = listOf(
    "-rewrite", "-no-indent"
)

val downloadSource by tasks.registering(de.undercouch.gradle.tasks.download.Download::class) {
    doFirst {
        downloadDir.mkdirs()
    }

    src(
        (if (useMirror) "https://github.com.cnpmjs.org" else "https://github.com") +
                "/lampepfl/dotty/archive/refs/tags/$scalaVersion.zip"
    )
    dest(File(downloadDir, "dotty-$scalaVersion.zip"))
    overwrite(false)
}

val unzipSource by tasks.registering(Copy::class) {
    dependsOn(downloadSource)

    from(zipTree(downloadSource.get().dest).apply {
        include("dotty-$scalaVersion/library/src/**")
    })
    into(downloadDir)
}

val compileScala by tasks.registering(JavaExec::class) {
    group = "build"

    doFirst { classesDir.mkdirs() }
    classpath(dotc)
    main = "dotty.tools.dotc.Main"
    jvmArgs("-Dscala.usejavacp=true")

    args(
        scalaCompilerOptions +
                listOf("-d", classesDir) +
                fileTree(sourceDir) { include("**/*.scala") }.files
    )
}

tasks.jar {
    from(classesDir)

    manifest {

        attributes(
            "Implementation-Title" to "scala3-library-bootstrapped",
            "Implementation-Version" to scalaVersion,
            "Specification-Vendor" to "LAMP/EPFL",
            "Specification-Title" to "scala3-library-bootstrapped",
            "Implementation-Vendor-Id" to "org.scala-lang",
            "Specification-Version" to scalaVersion,
            "Implementation-URL" to "https://git.io/scala3-library-with-braces",
            "Implementation-Vendor" to "LAMP/EPFL"
        )
    }
}

tasks.compileJava {
    dependsOn(compileScala)
}

val sourcesJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("sources")
    from("$sourceDir")
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = project.name

            from(components["java"])
            artifact(sourcesJar)

            pom {
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("glavo")
                        name.set("Glavo")
                        email.set("zjx001202@gmail.com")
                    }
                }

            }
        }
    }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}