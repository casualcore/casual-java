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

## Export

secret binary
$ gpg --export-secret-key 330916DA5D304C2B84DD94CE15362C063E05B561 > secret.pgp
secret ascii
$ gpg -a --export-secret-key 330916DA5D304C2B84DD94CE15362C063E05B561 > secret.asc

public binary
$ gpg --export 330916DA5D304C2B84DD94CE15362C063E05B561 > public.pgp
public ascii
$ gpg -a --export 330916DA5D304C2B84DD94CE15362C063E05B561 > public.asc

## Publish
gpg --keyserver keyserver.ubuntu.com --send-keys 330916DA5D304C2B84DD94CE15362C063E05B561
gpg --keyserver hkps://keys.openpgp.org --send-keys 330916DA5D304C2B84DD94CE15362C063E05B561

Chris@aqua MINGW64 ~
$ gpg --keyserver keyserver.ubuntu.com --send-keys 330916DA5D304C2B84DD94CE15362C063E05B561
gpg: sending key 15362C063E05B561 to hkp://keyserver.ubuntu.com

Chris@aqua MINGW64 ~
$ gpg --keyserver hkps://keys.openpgp.org --send-keys 330916DA5D304C2B84DD94CE15362C063E05B561
gpg: sending key 15362C063E05B561 to hkps://keys.openpgp.org


## Retrieve key from key server
ck@node02:~/repos/casual-java$ gpg --keyserver hkp://keyserver.ubuntu.com --search-keys 330916DA5D304C2B84DD94CE15362C063E05B561
gpg: data source: http://162.213.33.8:11371
(1)     Casual Middleware (Software Signing Key) <casual@laz.se>
4096 bit RSA key 15362C063E05B561, created: 2023-01-17
Keys 1-1 of 1 for "330916DA5D304C2B84DD94CE15362C063E05B561".  Enter number(s), N)ext, or Q)uit > N
ck@node02:~/repos/casual-java$ gpg --keyserver hkp://keyserver.ubuntu.com --receive-keys 330916DA5D304C2B84DD94CE15362C063E05B561
gpg: key 15362C063E05B561: public key "Casual Middleware (Software Signing Key) <casual@laz.se>" imported
gpg: Total number processed: 1
gpg:               imported: 1


## Import key from file

gpg --import public.asc

## Trust key

gpg --edit-key 15362C063E05B561 trust
Select trust level e.g. 5 - Ultimate
y
quit

## Verify

gpg --verify <filename>

e.g.

gpg --verify casual/casual-api/build/repo/snapshots/se/laz/casual/casual-api/2.2.16-SNAPSHOT/casual-api-2.2.16-20230220.142326-1.jar.asc
gpg: assuming signed data in 'casual/casual-api/build/repo/snapshots/se/laz/casual/casual-api/2.2.16-SNAPSHOT/casual-api-2.2.16-20230220.142326-1.jar'
gpg: Signature made mån 20 feb 2023 15:24:39 CET
gpg:                using RSA key 15362C063E05B561
gpg: checking the trustdb
gpg: marginals needed: 3  completes needed: 1  trust model: pgp
gpg: depth: 0  valid:   2  signed:   0  trust: 0-, 0q, 0n, 0m, 0f, 2u
gpg: Good signature from "Casual Middleware (Software Signing Key) <casual@laz.se>" [ultimate]


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

Question what if a rando signs the public back up - is that desirable? unavoidable? necessary evil/....


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
