This file has notes for committers.

# Making a snapshot release

Note: depends on having access to the Sonatype repo described further below

    mvn deploy -Prelease

# Making a new release

First, understand that LocationTech projects undergo releases using an official process described here:
https://www.locationtech.org/documentation/handbook with complete and thorough details here:
https://wiki.eclipse.org/Development_Resources/HOWTO/Release_Reviews

Note:
 * See https://projects.eclipse.org/projects/locationtech.spatial4j and "Committer Tools" panel at right, including
  * "Create a new release"
  * "Generate IP Log"
 * References to the "PMC" (Project Management Committee) in Spatial4j's case is the
 <a href="https://locationtech.org/mailman/listinfo/technology-pmc">LocationTech Technology PMC</a>.  There aren't
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

 1. Optional: create a release branch if there will be release-specific changes.  Probably not.

 2. Use Maven's `release` plugin, but ONLY for the "prepare" step, *not* perform:
  
```
mvn release:prepare
```
This will create a tag in git named spatial4j-0.6 (or whatever the version is) with the pom updated. 
If something goes wrong, you'll have to do release:rollback and then possibly remove the tag (pushing to GitHub)
so that next time it can succeed.

Remove the "release.properties" file and "pom.xml.releaseBackup".  Since we won't do release:perform, we can remove
these artifacts now.

Note that org.locationtech.spatial4j.io.jackson.PackageVersion includes a hard-coded version.
It's overwritten in the build process.  You should manually update it to the next snapshot release and commit.
It's okay that the Maven release plugin, when setting the final version, did so only in the POM but not this
souce code file because the jars that get created include the modified file (both compiled and source).

    mvn release:clean

 3. Have LocationTech sign the artifacts (via Hudson)

Go to https://ci.locationtech.org/spatial4j/job/Spatial4j-Jarsign2/ and modify the configuration
to reference the release tag in the git configuration area.  Then execute a build; it should succeed.
Download a zip of the build artifacts (pom.xml plus jar files).  Do that by seeing the link
"Artifact(s) of the Last Successful Build", clicking it, then clicking the link "(all files in zip)".
Expand it somewhere.  Note that the jar files will contain META-INF/ECLIPSE_* entries with binary signing info. Also,
ensure these file names have the right version name and not a -SNAPSHOT.

 4. Use GPG to sign the artifacts then deploy to Sonatype 
   
You should open a command prompt to the expanded directory of downloaded artifacts from Hudson.  There will be a pom.xml
and some jars.

For reference on what we're about to do, see:
http://central.sonatype.org/pages/manual-staging-bundle-creation-and-deployment.html
This requires having an account on Sonatype for their repo, plus installing & configuring GPG.  I'll assume
those steps have been followed.  We're going to follow the "bundle creation" method as it's pretty easy and doesn't
require special SonaType plugins / configuration; likewise for GPG.


    #First, rename the pom.xml to include the artifact & version:
    mv pom.xml target/spatial4j-0.6.pom
    cd target/

    #Generate a signature for each file. gpg has no way to do this at once, so you'll enter your password each time.
    find . -type f -exec gpg --detach-sign --armor \{} \;
    
    #Create a bundle jar
    jar -cvf ../bundle.jar *
    
Now log into Sonatype and upload the bundle JAR according to the illustrated instructions at the link above. 
 
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

## Update download.locationtech.org and the CMS

Upload the download artifacts to:
sftp://download.locationtech.org:/home/httpd/downloads/spatial4j/

Look at the existing file structure and files to see what should be put where.

Then update the LocationTech CMS to have an updated download link.  Follow this link:
https://www.locationtech.org/projects/technology.spatial4j/edit
And edit the download URL to be like this (with an appropriate version)
http://download.locationtech.org/spatial4j/0_6/?d