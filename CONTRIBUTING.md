# Contributing to Spatial4j

The Spatial4j project is always excited to accept contributions from the community. This document
contains some guidelines to help users and developers contribute to the project.

 - [Code Style](#code)
 - [Issues and Bugs](#bugs)
 - [Discussion Forum](#discuss)
 - [Submitting Patches](#patches)

## <a name="code">Code Style</a>

Spatial4j adheres to (as much as possible) the 
[Google Java Style](https://google.github.io/styleguide/javaguide.html) conventions. If a patch 
or commit deviates from these guidelines a reviewer will likely ask for it to be reformatted.

## <a name="bugs">Issues, Bugs, and Feature Requests</a>

Spatial4j utilizes Github for issue tracking. Bugs, issues, and feature requests should be 
filed [here](https://github.com/locationtech/spatial4j/issues).

## <a name="discuss">Discussion Forum</a>

Often communication can be carried out through comments on an issue or pull request directly but 
for larger discussions that are more general in nature it is recommended that the project 
[mailing list](https://locationtech.org/mailman/listinfo/spatial4j-dev) be used. 

## <a name="patches">Submitting Patches</a>

The best way to submit a patch or add a new feature to the code is to submit a [
pull request](https://help.github.com/articles/using-pull-requests/). Below are some guidelines to 
follow when developing code intended to be submitted via pull request.

This [guide](http://people.redhat.com/rjones/how-to-supply-code-to-open-source-projects/) contains 
some useful guidelines for contributing to open source projects in general. Below are some additionally 
stressed points.

### Send email first

It is never a bad idea to email the mailing list with thoughts about a change you intend to make
before you make it. This allows the committers to weigh in with thoughts and suggestions that will 
help you make the change and ultimately ensure your successful contribution to the project. 

### Filing an ECA

Contributors must electronically sign the Eclipse Contributor Agreement (ECA).
This is a one-time event, and is quick & easy.

* http://www.eclipse.org/legal/ECA.php

For more information, please see the Eclipse Committer Handbook:
https://www.eclipse.org/projects/handbook/#resources-commit

### One patch per one bug/feature

Avoid submitting patches that mix together multiple features and/or bug fixes into a single changeset. 
It is much easier to review and understand a patch that is dedicated to a single purpose. 

### No cruft

While working on a patch often developers can't resist the urge to reformat code that is unrelated
to the patch. This adds unnecessary "noise" that makes the job of the reviewer more difficult. It
also makes the history of a change harder to analyze after the fact. If a patch contains unnecessary
whitespace or other formatting changes a reviewer will ask for them to be removed.

### Viewing the entire project history

(Optional) We recommended that developers do this step to be able to view the 
pre-LocationTech history of the project. This can be achieved with the following
command after the repository has been cloned:

    git fetch origin refs/replace/*:refs/replace/*

