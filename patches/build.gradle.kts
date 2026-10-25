group = "app.morphe"

patches {
    about {
        // A fork's own identity: Morphe Manager treats bundles with the same name/source as one and keeps the
        // highest version, so with ajstrick81's name this fork was hidden behind the original's 1.42.x.
        name = "AllGray Patches"
        description = "Pluto TV ad patches (5.66 and 17), from ajstrick81's Android TV patches"
        source = "https://github.com/AllGray/morphe-androidtv-patches"
        author = "AllGray (fork of ajstrick81)"
        contact = "https://github.com/AllGray"
        website = "https://github.com/AllGray/morphe-androidtv-patches"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    implementation(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"
        dependsOn(build)
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("ajstrick81.morphe.util.PatchListGeneratorKt")
    }
    publish {
        dependsOn("generatePatchesList")
    }
}
