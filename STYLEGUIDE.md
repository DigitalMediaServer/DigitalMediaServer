# Style Guide

## Code

* Use tab for indentation.
* Keep each line of code to a readable length. Unless you have a reason to, keep lines to fewer than 120 characters.
* Never leave trailing whitespace.
* Use spaces after commas.
* No spaces after `(`, `[` or before `]`, `)`.
* No spaces after `!`.
* Use descriptive names and standard Java naming conventions.
* Write JavaDocs.
* Add code comments where the logic isn't obvious.
* Write tests if viable.

There are too many small details to list them all here, so to make it easier for everybody formatting templates for Eclipse and CheckStyle can be found in the repository root. Use of these is optional but can be helpful.

For small PRs the style guide isn't that important, we can modify the code before merging if necessary. For larger PRs that can be a lot of extra work and might slow down the merge process.

# Git 

Try to make commits that are logical. Don't split one change into several partial commits, the general idea is that the code should compile for every commit. At the same time, try to split different tasks into different commits. Interactive rebase of the PR is a good way to "clean up" commits by squashing or shuffling commits and code around to make it easily readable for those that will read your history in the future.

