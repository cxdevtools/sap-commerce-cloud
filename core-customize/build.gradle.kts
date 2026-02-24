import org.apache.tools.ant.taskdefs.condition.Os

import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.VerifyAction

import java.time.Instant
import java.util.Base64

plugins {
    id("sap.commerce.build") version("5.0.2")
    id("sap.commerce.build.ccv2") version("5.0.2")
    id("de.undercouch.download") version("5.6.0")
    `maven-publish`
}

val solrVersionMap = mapOf(
    /*
     * version 8 unfortunately has a different download URL and is currently not working. But it is the version that is
     * supplied with the standard zip, so we're fine unless the solr version is changed and then changed back in
     * manifest.json
     */
    "8.11" to "8.11.2",
    "9.2" to "9.2.1",
    "9.5" to "9.5.0"
)

val dependencyDir = "../dependencies"
val workingDir = project.projectDir
val binDir = "${workingDir}/hybris/bin"

repositories {
    flatDir { dirs(dependencyDir) }
    mavenCentral()
}

val solrVersion = solrVersionMap[CCV2.manifest.solrVersion] ?: "9.2.1"
val solrServer: Configuration by configurations.creating

dependencies {
    solrServer("org.apache.solr:solr:${solrVersion}")
}

hybris {
    // what files should be deleted when cleaning up the platform?
    // (cloudhofolders will be downloaded by custom configuration)
    cleanGlob.set("glob:**hybris/bin/{modules**,platform**,cloudhotfolders**}")

    // what should be unpacked from the platform zip files?
    bootstrapInclude.set(
        listOf(
            "hybris/**", //
            "azurecloudhotfolder/**", //
            "cloudcommons/**", //
            "cloudhotfolder/**" //
        )
    )

    // what should excluded when unpacking?
    // the default value is a npm package folder that includes UTF-8 filenames, which lead to problems on linux
    bootstrapExclude.set(
        listOf(
            "hybris/bin/ext-content/npmancillary/resources/npm/node_modules/http-server/node_modules/ecstatic/test/**"
        )
    )

    // Control the sparse platform bootstrap.
    // When enabled, the commerce extensions are extracted from the distribution zip on a as-needed basis.
    // Only extensions that are actually used in the project (either directly listed in the localextensions.xml or
    // required by other extensions) are extracted.
    // The platform itself is always extracted.
    // When this mode is enabled, the bootstrapInclude configuration property is ignored.
    sparseBootstrap {
        enabled = true
        alwaysIncluded = listOf<String>("solrserver")
    }
}

tasks.register<Download>("fetchSolr") {
    src(uri("https://archive.apache.org/dist/solr/solr/${solrVersion}/solr-${solrVersion}.tgz"))
    dest("${dependencyDir}/solr-${solrVersion}.tgz")
    overwrite(false)
}

val repackSolr = tasks.register<Zip>("repackSolr") {
    dependsOn("fetchSolr")
    from(tarTree("${dependencyDir}/solr-${solrVersion}.tgz"))
    archiveFileName = "solr-${solrVersion}.zip"
    destinationDirectory = file(dependencyDir)
}

publishing {
    publications {
        create<MavenPublication>("solr") {
            groupId = "org.apache.solr"
            artifactId = "solr"
            version = solrVersion

            artifact(repackSolr.get().archiveFile)
        }
    }
}

tasks.named("yinstallSolr") {
    // this task is available because of the solr-publication above.
    dependsOn("publishSolrPublicationToMavenLocal")
}

tasks.ybuild {
    dependsOn("publishSolrPublicationToMavenLocal")
    group = "build"
}

if (project.hasProperty("CXDEV_ARTEFACT_BASEURL") && project.hasProperty("CXDEV_ARTEFACT_USER") && project.hasProperty("CXDEV_ARTEFACT_PASSWORD")) {
    val BASEURL = project.property("CXDEV_ARTEFACT_BASEURL") as String
    val USER = project.property("CXDEV_ARTEFACT_USER") as String
    val PASSWORD = project.property("CXDEV_ARTEFACT_PASSWORD") as String
    val AUTHORIZATION = Base64.getEncoder().encodeToString((USER + ":" + PASSWORD).toByteArray())

    val COMMERCE_VERSION = CCV2.manifest.effectiveVersion
    tasks.register<Download>("downloadPlatform") {
        src(BASEURL + "/commerce/hybris-commerce-suite-${COMMERCE_VERSION}.zip")
        dest(file("${dependencyDir}/hybris-commerce-suite-${COMMERCE_VERSION}.zip"))
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
            dest(file("${dependencyDir}/hybris-commerce-integrations-${INTEXTPACK_VERSION}.zip"))
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

val symlinkConfigTask: TaskProvider<Task> = tasks.register("symlinkConfig")
val localConfig = file("hybris/config/local-config")
mapOf(
    "10-local.properties" to file("hybris/config/cloud/common.properties"),
    "20-local.properties" to file("hybris/config/cloud/persona/development.properties"),
    "50-local.properties" to file("hybris/config/cloud/local-dev.properties")
).forEach{
    val symlinkTask = tasks.register<Exec>("symlink${it.key}") {
        val path = it.value.relativeTo(localConfig)
        if (Os.isFamily(Os.FAMILY_UNIX)) {
            commandLine("sh", "-c", "ln -sfn $path ${it.key}")
        } else {
            // https://blogs.windows.com/windowsdeveloper/2016/12/02/symlinks-windows-10/
            val windowsPath = path.toString().replace("[/]".toRegex(), "\\")
            commandLine("cmd", "/c", """mklink "${it.key}" "$windowsPath" """)
        }
        workingDir(localConfig)
        dependsOn("generateLocalProperties")
    }
    symlinkConfigTask.configure {
        dependsOn(symlinkTask)
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

// https://help.sap.com/viewer/b2f400d4c0414461a4bb7e115dccd779/LATEST/en-US/784f9480cf064d3b81af9cad5739fecc.html
tasks.register<Copy>("enableModeltMock") {
    from("hybris/bin/custom/extras/modelt/extensioninfo.disabled")
    into("hybris/bin/custom/extras/modelt/")
    rename { "extensioninfo.xml" }
}

tasks.named("installManifestAddons") {
    mustRunAfter("generateLocalProperties")
}

tasks.register("setupLocalDevelopment") {
    group = "SAP Commerce"
    description = "Setup local development"
    dependsOn(
        "bootstrapPlatform",
        "yinstallSolr",
        "generateLocalDeveloperProperties",
        "installManifestAddons",
        "enableModeltMock"
    )
}