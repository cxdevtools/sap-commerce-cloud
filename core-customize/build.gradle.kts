import org.apache.tools.ant.taskdefs.condition.Os

import de.undercouch.gradle.tasks.download.Download

import java.time.Instant
import java.util.Base64

plugins {
    id("sap.commerce.build") version("4.0.0")
    id("sap.commerce.build.ccv2") version("4.0.0")
    id("de.undercouch.download") version("5.5.0")
}

val DEPENDENCY_FOLDER = "../dependencies"
repositories {
    flatDir { dirs(DEPENDENCY_FOLDER) }
    mavenCentral()
}

if (project.hasProperty("SAPCX_ARTEFACT_BASEURL") && project.hasProperty("SAPCX_ARTEFACT_USER") && project.hasProperty("SAPCX_ARTEFACT_PASSWORD")) {
    val BASEURL = project.property("SAPCX_ARTEFACT_BASEURL") as String
    val USER = project.property("SAPCX_ARTEFACT_USER") as String
    val PASSWORD = project.property("SAPCX_ARTEFACT_PASSWORD") as String
    val AUTHORIZATION = Base64.getEncoder().encodeToString((USER + ":" + PASSWORD).toByteArray())

    val COMMERCE_VERSION = CCV2.manifest.commerceSuiteVersion
    tasks.register<Download>("downloadPlatform") {
        src(BASEURL + "/commerce/hybris-commerce-suite-${COMMERCE_VERSION}.zip")
        dest(file("${DEPENDENCY_FOLDER}/hybris-commerce-suite-${COMMERCE_VERSION}.zip"))
        header("Authorization", "Basic ${AUTHORIZATION}")
        overwrite(false)
        tempAndMove(true)
        onlyIfModified(true)
        useETag(true)
    }

    tasks.named("bootstrapPlatform") {
        dependsOn("downloadPlatform")
    }

    //check if Integration Extension Pack is configured and download it too
    if (CCV2.manifest.extensionPacks.any{"hybris-commerce-integrations".equals(it.name)}) {
        val INTEXTPACK_VERSION = CCV2.manifest.extensionPacks.first{"hybris-commerce-integrations".equals(it.name)}.version        
        tasks.register<Download>("downloadIntExtPack") {
            src(BASEURL + "/integration/hybris-commerce-integrations-${INTEXTPACK_VERSION}.zip")
            dest(file("${DEPENDENCY_FOLDER}/hybris-commerce-integrations-${INTEXTPACK_VERSION}.zip"))
            header("Authorization", "Basic ${AUTHORIZATION}")
            overwrite(false)
            tempAndMove(true)
            onlyIfModified(true)
            useETag(true)
        }

        tasks.named("bootstrapPlatform") {
            dependsOn("downloadIntExtPack")
        }
    }
}

tasks.register<WriteProperties>("generateLocalProperties") {
    comment = "GENERATED AT " + Instant.now()
    destinationFile = project.file("hybris/config/local.properties")
    property("hybris.optional.config.dir", project.file("hybris/config/local-config").absolutePath)
    doLast {
        mkdir(project.file("hybris/config/local-config/"))
    }
}

val symlinkConfigTask = tasks.register("symlinkConfig")
val hybrisConfig = file("hybris/config")
val localConfig = file("hybris/config/local-config")
val homeDirectory = file(project.gradle.gradleUserHomeDir.parent)
mapOf(
    "10-local.properties" to "cloud/common.properties",
    "20-local.properties" to "cloud/persona/development.properties",
    "50-local.properties" to "cloud/local-dev.properties",
    "90-local.properties" to "local/90-local.properties",
    "91-local.properties" to "local/91-local.properties",
    "92-local.properties" to "local/92-local.properties",
    "93-local.properties" to "local/93-local.properties",
    "94-local.properties" to "local/94-local.properties",
    "95-local.properties" to "local/95-local.properties",
    "96-local.properties" to "local/96-local.properties",
    "97-local.properties" to "local/97-local.properties",
    "98-local.properties" to "local/98-local.properties"
).forEach{
    val link = it.key
    var path = file(hybrisConfig.absolutePath + "/" + it.value)
    if (!path.exists()) {
        path = file(homeDirectory.absolutePath + "/.sap-commerce/local-config/" + it.key)
    }
    
    if (path.exists()) {
        val symlinkTask = tasks.register<Exec>("symlink-${link}") {
            val relPath = path.relativeTo(localConfig)
            if (Os.isFamily(Os.FAMILY_UNIX)) {
                commandLine("sh", "-c", "ln -sfn ${relPath} ${link}")
            } else {
                // https://blogs.windows.com/windowsdeveloper/2016/12/02/symlinks-windows-10/
                val windowsPath = relPath.toString().replace("[/]".toRegex(), "\\")
                commandLine("cmd", "/c", """mklink "${link}" "${windowsPath}" """)
            }
            workingDir(localConfig)
            dependsOn("generateLocalProperties")
        }
        symlinkConfigTask.configure {
            dependsOn(symlinkTask)
        }
    } else {
        // Unlink if no longer existing
        val unlinkTask = tasks.register<Exec>("unlink-${link}") {
            if (Os.isFamily(Os.FAMILY_UNIX)) {
                commandLine("sh", "-c", "rm -f ${link}")
            } else {
                commandLine("cmd", "/c", "del /q ${link}")
            }
            workingDir(localConfig)
            dependsOn("generateLocalProperties")
        }
        symlinkConfigTask.configure {
            dependsOn(unlinkTask)
        }
    }
}

tasks.register<WriteProperties>("generateLocalDeveloperProperties") {
    dependsOn(symlinkConfigTask)
    comment = "my.properties - add your own local development configuration parameters here"
    destinationFile = project.file("hybris/config/local-config/99-local.properties")
    onlyIf {
        !project.file("hybris/config/local-config/99-local.properties").exists()
    }
}

tasks.named("installManifestAddons") {
    mustRunAfter("generateLocalProperties")
}

tasks.register("setupLocalDevelopment") {
    group = "SAP Commerce"
    description = "Setup local development"
    dependsOn("bootstrapPlatform", "generateLocalDeveloperProperties", "installManifestAddons")
}
