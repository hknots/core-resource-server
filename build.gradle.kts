import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	`java-library`
	`maven-publish`
}

group = "no.fintlabs"
version = project.findProperty("version") as String? ?: "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testImplementation("io.mockk:mockk:1.14.3")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") }
}

tasks.withType<Test> { useJUnitPlatform() }
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> { enabled = false }

val sourcesJar by tasks.registering(Jar::class) {
	archiveClassifier.set("sources")
	from(sourceSets["main"].allSource)
}

apply(from = "https://raw.githubusercontent.com/FINTLabs/fint-buildscripts/master/reposilite.ga.gradle")

publishing {
	publications.named<MavenPublication>("maven") {
		artifact(sourcesJar.get())
	}
}

fun Project.configureTestSets() = sourceSets {
	val unit by creating {
		kotlin.srcDir("src/test/unit/kotlin")
		resources.srcDir("src/test/unit/resources")
		compileClasspath += main.get().output + configurations["testRuntimeClasspath"]
		runtimeClasspath += output + compileClasspath
	}
	val integration by creating {
		kotlin.srcDir("src/test/integration/kotlin")
		resources.srcDir("src/test/integration/resources")
		compileClasspath += main.get().output + configurations["testRuntimeClasspath"]
		runtimeClasspath += output + compileClasspath
	}
}

fun Project.configureTestConfigs() = listOf("unit", "integration")
	.forEach {
		configurations["${it}Implementation"].extendsFrom(configurations["testImplementation"])
		configurations["${it}RuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])
	}

fun Project.configureTestTasks() {
	tasks.register<Test>("unitTest") {
		testClassesDirs = sourceSets["unit"].output.classesDirs
		classpath = sourceSets["unit"].runtimeClasspath
	}
	tasks.register<Test>("integrationTest") {
		shouldRunAfter("unitTest")
		testClassesDirs = sourceSets["integration"].output.classesDirs
		classpath = sourceSets["integration"].runtimeClasspath
	}
	tasks.named("check") { dependsOn("unitTest", "integrationTest") }
}

configureTestSets()
configureTestConfigs()
configureTestTasks()
