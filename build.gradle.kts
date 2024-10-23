plugins {
    id("com.android.application") version ("8.6.1") apply (false)
    id("org.jetbrains.kotlin.android") version ("2.0.20") apply (false)
    id("com.google.devtools.ksp") version ("2.0.20-1.0.25") apply (false)
}

tasks.register("clean", Delete::class) {
    delete("$rootDir/build", "$rootDir/app/build")
}
