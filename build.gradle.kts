import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

// 17
plugins {
    kotlin("jvm") version "1.9.20"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

version = "1.3"

repositories {
    maven("https://maven.elmakers.com/repository")
    maven("https://nexus.cyanbukkit.cn/repository/maven-public")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:minecraft-server:1.8.8-SNAPSHOT")
    compileOnly("org.bukkit:craftbukkit:1.8.8-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
    // lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    // ProtocolLib
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    // MySQL HiKari 方案
    implementation("com.zaxxer:HikariCP:3.4.2")
    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.2")
    implementation(kotlin("reflect"))
}


bukkit {
    main = "cn.cyanbukkit.speed.SpeedBuildReloaded"
    name = "SpeedBuildReloaded"
    version = project.version.toString()
    description = ""
    authors = listOf("CyanBukkit","TheGoodBoys")
    website = "https://cyanbukkit.net"
    depend = listOf("ProtocolLib","PlaceholderAPI")
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

kotlin {
    jvmToolchain(8)
}


tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    shadowJar {
        archiveFileName.set("SpeedBuildReloaded-${project.version}.jar")
        // 编译后Copyjar 到Server plugins
        doLast {
            copy {
                from("$buildDir/libs/SpeedBuildReloaded-${project.version}.jar")
                into("E:\\Code\\Jatlin\\SpeedBuildReloaded\\Server\\plugins")
                println("Copy Jar Ok~")
            }
        }
    }

}
