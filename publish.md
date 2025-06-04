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

* Associate your sonartype account with permissions to publish to maven central.
* Use your personal gpg key to securely send you the casual software signing key and passphrase.
* Add your details to the `publish.gradle` `developer` list.

## Publishing 

### Setup

In order to be able to publish to maven central, you must configure secret details related to
the casual software signing key and your sonartype account.

If you have not already, Generate a User Token for your account, as detailed here:
https://central.sonatype.org/publish/generate-portal-token/

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

mavenCentralTokenUsername=<mavenCentralTokenUsername>
mavenCentralTokenPassword=<mavenCentralTokenPassword>
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
in the normal maven central, but can be access adding the following maven repository to your `build.gradle`.

```groovy
repositories {
    maven {
        name = 'Central Portal Snapshots'
        url = 'https://central.sonatype.com/repository/maven-snapshots/'

        // Only search this repository for the specific dependency group
        content {
            includeGroup("se.laz.casual")
        }
    }
  }
```

#### Local publish

If you want to test publishing locally you can update the `publish.gradle` lines with the following:

```groovy
            def releasesRepoUrl = layout.buildDirectory.dir('repo/releases')
            def snapshotsRepoUrl = layout.buildDirectory.dir('repo/snapshots')
```
This is not the same as command `publishToMavenLocal` as the above allows you to publish signed artifacts locally. 

### Manual Repository Upload

After the [sunsetting of OSSRH for maven central publishing](https://central.sonatype.org/news/20250326_ossrh_sunset/) 
and in the interim prior to a stable gradle plugin we have decided to utilise the existing OSSRH API that also
available on the new central portal for maven:

https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/

As we use the gradle `maven-publish` plugin for publishing, there is an additional manual step required to upload
the repository from the staging area after publishing, into the new Central Portal to make it available for releasing.

This requires an authenticated POST request to be sent to the following URL, with your token as authentication:
https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/{namespace}

One option is to perform the following:
Create a base64 encoded string of your : seperated token e.g. "<mavenCentralTokenUser>:<mavenCentralTokenPassword>"
```shell
echo user:password | base64 -
```
Save the value from that to use as the Authorization Header Bearer in the POST request:

```shell
curl -iv -X POST https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/se.laz.casual -H "Authorization: Bearer <base64 token>"
```

### Finalise Release

In order to finalise the release / publish:

Login to https://central.sonatype.com/publishing

Select deployments.

NB: If none are present check that you have performed the [manual repository upload](#manual-repository-upload) steps, as these are required to 
move the uploaded staged artifacts to central portal and make them available as a deployment to be published.

Check the details.

If you find a mistake you can `Drop` the staging repository, preventing the release.

**NB- you can never delete a published artifact, only publish again with a new version.**

Select `Publish`.

This will then show the deployment as "Publishing".

It should be available on maven central after around 30minutes - 8 hours.

## Secret Compromise

If for any reason you believe the secret details of the casual software signing key are compromised, i.e.
are no longer secret, have been shared accidentally etc.
You must inform the other maintainers and issue the casual software signing revocation key immediately
to ensure the integrity of casual signed artifacts.