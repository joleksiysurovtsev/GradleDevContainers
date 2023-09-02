import pl.allegro.tech.build.axion.release.domain.hooks.HookContext
import pl.allegro.tech.build.axion.release.domain.properties.TagProperties
import pl.allegro.tech.build.axion.release.domain.scm.ScmPosition

plugins {
    id("pl.allegro.tech.build.axion-release") version "1.15.4"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
}

gitProperties {
    failOnNoGitDirectory = false
    keys = mutableListOf(
        "git.commit.id",
        "git.commit.time",
        "git.branch",
        "git.build.version",
        "git.commit.message.full",
        "git.commit.user.name",
        "git.commit.id.abbrev",
    )
}

scmVersion {
    useHighestVersion.set(true)
    branchVersionIncrementer.set(
        mutableMapOf(
            "develop.*" to "incrementPatch",
        ),
    )
    branchVersionCreator.set(
        mutableMapOf(
            "master" to KotlinClosure2({ v: String, s: ScmPosition -> v }),
            ".*" to KotlinClosure2({ v: String, s: ScmPosition -> "$v-${s.branch}" }),
        ),
    )
    snapshotCreator.set { versionFromTag: String, scmPosition: ScmPosition -> "-${scmPosition.shortRevision}" }

    tag.initialVersion.set { tagProperties: TagProperties, _: ScmPosition -> "0.0.1" }
    tag.prefix.set("v")
    tag.versionSeparator.set("")

    hooks {
        post { hookContext: HookContext ->
            println("scmVersion previousVersion: ${hookContext.previousVersion}")
            println("scmVersion  releaseVersion: ${hookContext.releaseVersion}")
        }
    }
}

group = "o.sur"
version = 1
