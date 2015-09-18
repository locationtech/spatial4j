This file has notes for committers.

# Making a snapshot release

Note: depends on having access to the sonatype repo described further below

    mvn -DperformRelease=true deploy

# Making a new release

## Review files...
 
 * Review CHANGES.md — up to date?

 * Review README.md — up to date?

 * Review pom — up to date?  Run display-plugin-updates & display-dependency-updates.  Do *not* remove the SNAPSHOT;
   that'll be handled later.


## Build, Tag, and Deploy to Sonatype

 1. Optional: create a release branch if there will be release-specific changes.  Probably not.

 2. Use maven release plugin: 
 
See http://central.sonatype.org/pages/ossrh-guide.html
And specifically follow the link to deploy using Maven:
https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
It includes having an account on Sonatype for their repo, plus installing & configuring GPG.  I'll assume
those steps have been followed.

TODO: move away from the oss-parent parent POM; Sonatype says it's deprecated & unmaintained.

Simple:
 
    mvn release:clean release:prepare
    mvn release:perform
    
That was easy!
 
 3. Now vote on the artifacts deployed to Sonatype's staging repo.  See next step for how to access them.  Ideally this
 step isn't a rubber stamp; try and use it.
 
## Release to Maven Central

http://central.sonatype.org/pages/releasing-the-deployment.html

## Publish Javadoc HTML

We publish the Javadocs on GitHub, and we link to them from the readme and others might too.

Instructions:
http://blog.progs.be/517/publishing-javadoc-to-github-using-maven

Test out the generated site before publishing it.

note: use Java 8 to get nicer javadocs