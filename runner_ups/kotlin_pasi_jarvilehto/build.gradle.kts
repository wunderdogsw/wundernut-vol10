import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    antlr
}

group = "lang.doggo"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    antlr("org.antlr:antlr4:4.7.2")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
}

tasks.getByName<AntlrTask>("generateGrammarSource") {
    arguments = arguments + listOf("-visitor", "-no-listener", "-long-messages")
    outputDirectory = file("src/main/java/lang/doggo/generated")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.dependsOn("generateGrammarSource")

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
