# Maven Central Publish

The following is what needs to be done for publishing to maven central.

## Who can publish?

Currently the following maintainers of casual java have permission to publish to maven central:

* Mathias Creutz
* Chris Kelly
* Tobias Leo

### New publishers

To become a publisher, you must first be a maintainer and approved for publishing by the casual open source community.

You then need to create:
* a sonartype account and 
* a personal gpg key.

Once you have done this, provide these details to an existing publisher, who will then:

Associate your sonartype account with permissions to publish casual java to maven central.
Use your personal gpg key to securely send you the casual software signing key and passphrase.
Add your details to the `publish.gradle` `developer` list.

## Publishing 

### Setup

In order to be able to publish to maven central, you must configure secret details related to
the casual software signing key and your sonartype account.

**NB - these details must never be commited back to the repository!**

Create a binary export of the casual software signing secret. (ascii armour does not work for some reason.)

If you have not already done so, import the provided casual software signing secret to your gpg keyring.

Generated the binary secret file as follows:
```shell
gpg --export-secret-key 330916DA5D304C2B84DD94CE15362C063E05B561 > secret.gpg
```
This secret gpg file can be located anywhere you wish using an absolute path to the file.

The simplest way to set this up is to create a `gradle.properties` file in the root folder:

```properties
signing.keyId=3E05B561
signing.password=<casualsoftwarepassphrase>
signing.secretKeyRingFile=<absolute path to binary secret>

ossrhUsername=<sonartypeuser>
ossrhPassword=<sonartypepass>
```

**NB - `gradle.properties` and `secret.gpg` on the root folder have been added to `.gitignore`, but still be careful 
to never publish these files!**

### Publish

Normally new artifact versions will be published after the PR is approved and merged to the `dev` branch and tagged.

It is also possible to publish to the sonartype snapshot repository, just ensure the version is set to end with the
value `SNAPSHOT`.

In order to publish, just issue the following command:
```shell
./gradlew clean build publish
```

A non `SNAPSHOT` version, when published, is placed into a staging repository and needs to be released in order to
make it available in maven central. 

If you find a mistake you can drop the staging repository, preventing the release.

**NB- you can never delete a published artifact, only publish again with a new version.**

#### SNAPSHOT publish

A `SNAPSHOT` version, when published, is placed into the maven central snapshot repository, it is not available
in the normal maven central, but can be access adding the following maven repository to your build.gradle.

```groovy
repositories {
    maven {url 'https://s01.oss.sonatype.org/content/repositories/snapshots' }
  }
```

#### Local publish

If you want to test publishing locally you can update the publish.gradle lines 67-72 with the following:

```groovy
            def releasesRepoUrl = layout.buildDirectory.dir('repo/releases')
            def snapshotsRepoUrl = layout.buildDirectory.dir('repo/snapshots')
```
This is not the same as command `publishToMavenLocal` as the above allows you to publish signed artifacts locally. 

### Release Staging

In order to release from the staging repository:

Login to the sonartype ui: [https://s01.oss.sonatype.org](https://s01.oss.sonatype.org)

Find the staging repository.

Check the details.

If you find a mistake you can `drop` the staging repository, preventing the release.

**NB- you can never delete a published artifact, only publish again with a new version.**

Select `Close`.

This will run validation checks, which once passed will allow a release.

Select `Release`.

It should be available on maven central after around 30minutes - 8 hours.

## Secret Compromise

If for any reason you believe the secret details of the casual software signing key are compromised, i.e.
are no longer secret, have been shared accidentally etc.
You must inform the other maintainers and issue the casual software signing revocation key immediately
to ensure the integrity of casual signed artifacts.