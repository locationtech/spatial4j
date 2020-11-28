This file has notes for committers.

# Making a snapshot release

Note: depends on having access to the Sonatype repo described further below

    mvn deploy -Prelease

# Making a new release

First, understand that LocationTech projects undergo releases using an official process described here:
https://www.eclipse.org/projects/handbook/#release with complete and thorough details here:
https://wiki.eclipse.org/Development_Resources/HOWTO/Release_Reviews

Note:
 * See https://projects.eclipse.org/projects/locationtech.spatial4j and "Committer Tools" panel at right, including
  * "Create a new release"
  * "Generate IP Log"
 * References to the "PMC" (Project Management Committee) in Spatial4j's case is the
 <a href="https://accounts.eclipse.org/mailing-list/technology-pmc">LocationTech Technology PMC</a>.  There aren't
 project-specific PMCs.
 
*TODO distill the process here.*

Those steps can be concurrent with following some of the earlier technical steps below.  Deploying/releasing any
jars must wait until the release date assuming the release review is successful.

## Review files...
 
 * Review CHANGES.md — up to date?

 * Review README.md — up to date?

 * Review pom — up to date?  Run display-plugin-updates & display-dependency-updates.  Do *not* remove the SNAPSHOT;
   that'll be handled later.


## Build, Tag, and Deploy to Sonatype

Optional: create a release branch if there will be release-specific changes.  Probably not.

**Prepare** the release: https://maven.apache.org/maven-release/maven-release-plugin/examples/prepare-release.html
  
```
mvn release:prepare
```

This will create a tag in git named spatial4j-0.6 (or whatever the version is) with the pom updated. 
If something goes wrong, you'll have to do `release:rollback` and then possibly remove the tag (pushing to GitHub)
so that next time it can succeed.

Some "release.properties" file and "pom.xml.releaseBackup" files will be produced.  They will be removed later at the `clean` phase

Note that org.locationtech.spatial4j.io.jackson.PackageVersion includes a hard-coded version.
It's overwritten in the build process.  You should manually update it to the next snapshot release and commit.
It's okay that the Maven release plugin, when setting the final version, did so only in the POM but not this
souce code file because the jars that get created include the modified file (both compiled and source).

**Perform** the release: https://maven.apache.org/maven-release/maven-release-plugin/examples/perform-release.html

    mvn release:perform
    
This should build, GPG sign, and deploy artifacts to Sonatype.
When I last edited these instructions, there was previously a different process.  So... something will probably go wrong.
It is intentional that there remains a manual step further below at Sonatype to send the binaries to Maven Central.
 Further info: https://central.sonatype.org/pages/apache-maven.html#performing-a-release-deployment-with-the-maven-release-plugin

**Clean**

    mvn release:clean

 
## Release deployed artifacts to Maven Central

http://central.sonatype.org/pages/releasing-the-deployment.html

## Publish the Maven site (includes Javadoc)

We publish the Maven "site" HTML on GitHub, and we link to it from the readme and others might too.  The site
includes the javadoc API.

Instructions:
http://blog.progs.be/517/publishing-javadoc-to-github-using-maven

Summary:

First checkout the release tag (e.g. spatial4j-0.5) or modify pom.xml temporarily to have this version.  The site
reports reference the version, so this is why.

    mvn clean site
    mvn scm-publish:publish-scm

When site completes, open the target/site/index.html to view it to see if it's reasonable.  Then continue to the publish
step.  The publish step will require your username & password for GitHub.  Observe the final published content online:

https://locationtech.github.io/spatial4j/

## GitHub Release

On the project's Git based homepage, navigate to the "Tags": https://github.com/locationtech/spatial4j/tags 

Find the tag for 0.8 (or whatever the version is), click the "..." menu next to it, and choose "Create a Release".

Put a brief characterization of the release at the top, and then paste in the content from CHANGES.md.  Add the release binaries as well (all JAR files).

## Update the Eclipse CMS

At the Eclipse project site for Spatial4j, hit the Edit button:
https://projects.eclipse.org/projects/locationtech.spatial4j
Then on the "Download" section, update the version.

## Send announcement
