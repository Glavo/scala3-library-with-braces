# Scala 3 with braces

[![](https://jitpack.io/v/org.glavo/scala3-library-with-braces.svg)](https://jitpack.io/#org.glavo/scala3-library-with-braces)

Scala adds a terrible new feature, [optional braces](https://dotty.epfl.ch/docs/reference/other-new-features/indentation.html),
which allow use indentation instead of braces. The new syntax is widely used in the standard library.

In order to exclude this function, this project provides a Scala 3 standard library built with the old version syntax,
and provides the corresponding sources jar. We suggest using this project instead of relying on official builds.

(Note: this project is temporarily published on jitpack and will be transferred to Maven central in the future.)

To use this project in SBT, first remove the dependency on the official build:

```sbt
autoScalaLibrary := false
```

Secondly, add the resolver for jitpack at the end of resolvers:
(TODO: After the project publish to Maven central, this resolver will no longer be needed)

```sbt
   resolvers += "jitpack" at "https://jitpack.io"
```

Then add the dependency:

```sbt
libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-library" % "2.13.5",
    "org.glavo" % "scala3-library-with-braces" % "3.0.0-RC3-1"
)
```

(Optional) Finally, disable indentation syntax in your project:
```sbt
scalacOptions += "-no-indent"
```
