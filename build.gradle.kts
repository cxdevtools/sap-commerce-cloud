plugins {
    id("com.diffplug.spotless") version("8.1.0")
}

repositories {
    mavenCentral()
}

spotless {
    val importOrderConfigFile = project.file("core-customize/conventions/eclipse.importorder")
    val javaFormatterConfigFile = project.file("core-customize/conventions/eclipse-formatter-settings.xml")
    java {
        target("core-customize/hybris/bin/custom/sapcxtools/**/*.java")
        targetExclude("core-customize/hybris/bin/custom/sapcxtools/**/gensrc/**")
        importOrderFile(importOrderConfigFile)
        eclipse().configFile(javaFormatterConfigFile)
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }

    //val frontendFormatterConfigFile = project.file("js-storefront/*/.prettierrc")
    //format("frontend") {
    //    target(
    //        "js-storefront/*/src/**/*.scss",
    //        "js-storefront/*/src/**/*.ts",
    //        "js-storefront/*/src/**/*.html"
    //    )
    //    prettier("3.5.3").configFile(frontendFormatterConfigFile)
    //}
}