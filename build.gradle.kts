// https://github.com/SimY4/xpath-to-xml/blob/java6/xpath-to-xml-scala/build-3.gradle

plugins {
    `java-library`
    `maven-publish`
    signing
    id("de.undercouch.download") version "4.1.1"
}

group = "org.glavo"
version = "3.0.0-RC3-4"

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

    api("org.scala-lang:scala-library:2.13.5")
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
                        id.set("odersky")
                        name.set("Martin Odersky")
                        url.set("https://github.com/odersky")
                        email.set("martin.odersky@epfl.ch")
                    }

                    developer {
                        id.set("DarkDimius")
                        name.set("Dmitry Petrashko")
                        url.set("https://d-d.me")
                        email.set("me@d-d.me")
                    }

                    developer {
                        id.set("smarter")
                        name.set("Guillaume Martres")
                        url.set("https://guillaume.martres.me")
                        email.set("smarter@ubuntu.com")
                    }

                    developer {
                        id.set("felixmulder")
                        name.set("Felix Mulder")
                        url.set("https://felixmulder.com")
                        email.set("felix.mulder@gmail.com")
                    }

                    developer {
                        id.set("liufengyun")
                        name.set("Liu Fengyun")
                        url.set("https://fengy.me")
                        email.set("liu@fengy.me")
                    }

                    developer {
                        id.set("nicolasstucki")
                        name.set("Nicolas Stucki")
                        url.set("https://github.com/nicolasstucki")
                        email.set("nicolas.stucki@gmail.com")
                    }

                    developer {
                        id.set("OlivierBlanvillain")
                        name.set("Olivier Blanvillain")
                        url.set("https://github.com/OlivierBlanvillain")
                        email.set("olivier.blanvillain@gmail.com")
                    }

                    developer {
                        id.set("biboudis")
                        name.set("Aggelos Biboudis")
                        url.set("https://biboudis.github.io")
                        email.set("aggelos.biboudis@epfl.ch")
                    }

                    developer {
                        id.set("allanrenucci")
                        name.set("Allan Renucci")
                        url.set("https://github.com/allanrenucci")
                        email.set("allan.renucci@gmail.com")
                    }

                    developer {
                        id.set("Duhemm")
                        name.set("Martin Duhem")
                        url.set("https://github.com/Duhemm")
                        email.set("martin.duhem@gmail.com")
                    }
                }

            }
        }
    }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    enabled = false
}