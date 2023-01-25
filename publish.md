# Maven Central Publish

The following is what needs to be done for publishing to maven central.

## Sonar Type registration

Need to register the project with sonartype in order to be able to publish to maven central.

https://central.sonatype.org/publish/publish-guide/#close-and-drop-or-release-your-staging-repository

Initially create a JIRA account.
Then create a new project ticket with the appropriate details.
Need to be able to verify the domain using DNS.

## Signing

In order to allow verification of the archives a gpg signature will be required.
The private key and passphrase must be kept local and secret.
The public key should be published to public key servers so others can verify the validity of the contents of the files.
SonarType will use this during their verification process too.

Question - using gradle.properties to maintain the keydata and having a local secret file is possible, but how do you distribute these?
There is a github action describing how to create a workflow for publishing -  does it show how to do this with the private key though?!

https://central.sonatype.org/publish/requirements/gpg/

* gpg - create a signature, maintain the private key locally.

gpg --gen-key

Casual Middleware
casual@laz.se
Enter passphrase

Export the secret as a keyring file:

Chris@aqua MINGW64 /d/Development/repos/casual-java-github (feature/maven-central-publishing#17)
$ gpg --keyring secring.pgp --export-secret-key > ./secring.pgp
gpg: keyblock resource '/c/Users/Chris/.gnupg/secring.pgp': No such file or directory


########################
gpg --full-generate-key
1
4096
0
y
Casual Middleware
casual@laz.se
Software Signing Key
o
Enter passphrase


## Revoke 
gpg --output casual-revoke.asc --gen-revoke 330916DA5D304C2B84DD94CE15362C063E05B561

sec  rsa4096/15362C063E05B561 2023-01-17 Casual Middleware (Software Signing Key) <casual@laz.se>

Create a revocation certificate for this key? (y/N) y
Please select the reason for the revocation:
0 = No reason specified
1 = Key has been compromised
2 = Key is superseded
3 = Key is no longer used
Q = Cancel
(Probably you want to select 1 here)
Your decision? 1
Enter an optional description; end it with an empty line:
>
Reason for revocation: Key has been compromised
(No description given)
Is this okay? (y/N) y
ASCII armored output forced.
Revocation certificate created.



TODO dont forget to create a revocation certificate.

## Export

public binary
public ascii
secret binary
secret ascii

## Publish
gpg --keyserver keyserver.ubuntu.com --send-keys
gpg --keyserver hkps://keys.openpgp.org --send-keys


### Signature / Secret Distribution

The following is the process for distributing the casual signature.

Create a gpg signature for your personal self.

Upload the public key to a key server.

Send the hash to a maintainer.

They will download your public key and import this into their key ring.

They will export the casual secret key as secring.pgp and a text file containing the secring password.

They will then sign and encrypt these along with the revocation certificate with your public key and send this to you via the email within your public the key.
Emphasis on sign and encrypt!!

Once you receive this you can verify and decrypt the files, import the casual cert into your gpg keyring using the provided password.

You can also change the trust for this signature to ultimate.

We should ensure that the casual signature is signed by all maintainers and uploaded again with these signature to ensure a chain of trust
which includes the casual maintainers.

The casual public signature will be signed by others first.

Question does the certificate validation change when you verify a file if you have signed with 1 chain of trust vs multiple chains of trust?


https://docs.gradle.org/current/userguide/signing_plugin.html


## Gradle publish setup

https://central.sonatype.org/publish/requirements/

The settings in sonartype here don't work:

https://central.sonatype.org/publish/publish-gradle/

Using this instead:

https://docs.gradle.org/current/userguide/publishing_maven.html

We need to provide all requirements that sonartype request.

JavaDoc and Sources
Checksums for all archives - minimum md5 or sha1
Signed Files with GPG/PGP
Correct coordinates
Project name and description and url
License Information
Developer information
SCM Information
