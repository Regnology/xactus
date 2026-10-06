# About

Xactus is [FINGO's](https://github.com/fingo) open-source XPath 2.0 processing library based on the [Eclipse's XPath 2.0 implementation](https://github.com/eclipse/webtools.sourceediting/tree/master/xpath/bundles/org.eclipse.wst.xml.xpath2) from the [Eclipse Web Tools Platform Project's](https://www.eclipse.org/webtools/).

# Contributing

Pull requests are welcome. We will respond as much as possible. For major changes please open an issue first to discuss what you would like to do.

To protect the rights of others, we use the [Developer Certificate of Origin](https://developercertificate.org/). If you want to develop the project with us, please confirm authorship by [signing-off your commits](https://git-scm.com/docs/git-commit#Documentation/git-commit.txt---signoff). The procedure is verified using [Probot: DCO](https://probot.github.io/apps/dco/).

# License

The library is published under the Eclipse Public License v2.0 license, a copy you will find in the [license file](/LICENSE). If you received this library from another party, conditions other than those stated here might apply. Please check it with your Redistributor.

# Development

## Building

* `./gradlew jar` - builds the library.
* `./gradlew sourcesJar` - builds sources JAR.
* `./gradlew javadoc` - builds Javadocs.
* `./gradlew javadocJar` - builds Javadoc JAR.

## Running tests

* `./gradlew test` - run all tests excluding JUnit 3 test suites.
* `./gradlew testSuite` - runs JUnit 3 test suites only.

# Publishing

## Version management

Xactus is using [Axion Release Plugin](https://github.com/allegro/axion-release-plugin) for version management. Please refer to [Axion's documentation](https://axion-release-plugin.readthedocs.io/en/latest/) for usage tips.

## Automatic publications - GitHub Actions

Xactus has an automated release process created with [GitHub Actions](https://github.com/features/actions). [Validate and publish workflow](https://github.com/Regnology/xactus/actions/workflows/ci.yml) is responsible for validating the build and releasing artifacts via [Sonatype Central Portal](https://central.sonatype.com/). Snapshot versions are published to the [Maven Central snapshots repository](https://central.sonatype.com/repository/maven-snapshots/info/fingo/xactus/xactus/) and release versions go to [Maven Central repository](https://repo.maven.apache.org/maven2/info/fingo/xactus/xactus/).  

For every commit pushed to master a new snapshot will be published. The version for the snapshot is determined using Axion's `currentVersion` Gradle task:

    ./gradlew cV

Release versions are automatically published whenever a release tag shows up in the repo (e.g. `v1.2.3`). The release tag can be created either manually or using Axions's `./gradlew release` task:

    ./gradlew release

**Successful test run is a prerequisite to all publications. Also be advised that both snapshot and release publications might become available with a delay (from a few minutes to a few hours after publication).** 

## Manual publication

There are a few publishing targets:
* local folder ('build/repos/releases' and 'build/repos/snapshots'):

      ./gradlew -Pversion=<publish version> publishToLocalRepository

* M2 local cache:

      ./gradlew -Pversion=<publish version> publishToMavenLocal
  
* Maven Central (requires [Central Portal](https://central.sonatype.com/) user token - [see below](#manual-publication---publishing-to-maven-central)):

      ./gradlew -Pversion=<publish version> publishToSonatype

## Manual publication - PGP configuration

Publishing tasks require some [project properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties) to be set:
* `pgpSecretKey` - ascii-armored PGP secret key to sign the artifact.
* `pgpSecretKeyPassword` - the password for the PGP secret key.
* `pgpKeyId` (optional) - the id of the OpenPGP sub-key.

Specifying `pgpSecretKey` and `pgpSecretKeyPassword` causes the signing mechanism to use [in-memory ascii armored keys](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys) or [in-memory ascii-armored OpenPGP subkeys](https://docs.gradle.org/current/userguide/signing_plugin.html#using_in_memory_ascii_armored_openpgp_subkeys) (when `signing.pgp.keyId` is set as well).

When neither of the `pgpSecretKey` and `pgpSecretKeyPassword` parameters are supplied then [the default set of settings](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials) is taken into account.

## Manual publication - Publishing to Maven Central

Command to publish Xactus to Maven Central:

      ./gradlew -Pversion=<publish version> publishToSonatype

You can also automatically close and release the staging repository after the release (thanks to [Gradle Nexus Publish Plugin](https://github.com/gradle-nexus/publish-plugin)):

      ./gradlew -Pversion=<publish version> publishToSonatype closeAndReleaseSonatypeStagingRepository

This will publish Xactus to Maven Central automatically, without the need to use Sonatype Nexus UI.

To publish an artifact to Maven Central, generate a [user token](https://central.sonatype.org/publish/generate-portal-token/) at [central.sonatype.com](https://central.sonatype.com/) (not the retired OSSRH login). Supply it in addition to the [PGP configuration](#manual-publication---pgp-configuration):
* `ossrhUserName` - Central Portal token username (`ORG_GRADLE_PROJECT_ossrhUserName` in CI, secret `OSSRH_USERNAME`).
* `ossrhPassword` - Central Portal token password (`ORG_GRADLE_PROJECT_ossrhPassword` in CI, secret `OSSRH_TOKEN`).

# Links

* [Xactus releases in Maven Central Repository](https://repo.maven.apache.org/maven2/info/fingo/xactus/xactus/)
* [Xactus snapshots](https://central.sonatype.com/repository/maven-snapshots/info/fingo/xactus/xactus/)
* [Xactus in Maven Central Repository Search](https://search.maven.org/artifact/info.fingo.xactus/xactus)
* [Xactus on MVNRepository](https://mvnrepository.com/artifact/info.fingo.xactus/xactus)
