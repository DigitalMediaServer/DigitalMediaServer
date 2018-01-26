<!--
Add or remove elements as needed, not everything is relevant for every issue
"DMS" is short for Digital Media Server

In order to help diagnose and fix your issue we usually need your
"debug information". To generate the "debug information" using the GUI, follow
these steps:

* Start DMS.
* Go to the Logs tab.
* Press the Create TRACE logs button, which will restart DMS in the TRACE mode.
  If that doesn't work for some reason, either select Trace from the Log Level
  dropdown at the bottom of the screen or set "log_level = TRACE" in "DMS.conf"
  and restart DMS manually.
* Reproduce the bug.
* Click Pack debug files on the lower left.
* Click Zip selected files.
* Save the zip file to a location you will remember.
* Attach the zip file to this GitHub issue.

To generate the "debug information" for a headless installation, use the
following steps:

* Start DMS with "trace" added as a command line parameter, for example:
  "./DMS.sh trace"
* Reproduce the bug.
* Stop DMS.
* Find the DMS log file and configuration file and put them in an archive
  format, for example "zip" or "tar.gz". The location of these files will vary
  depending on your platform, configuration and permissions, but their default
  names are "debug.log" and "DMS.conf". It might be easier to search for them,
  but the default locations on Linux are:
  - Configuration file: "~/.config/DigitalMediaServer/DMS.conf"
  - Log file: "/var/log/DMS/<username>/debug.log". If this folder isn't
    available for writing, DMS will fall back to either the profile folder
	(where DMS.conf is found) or the system temp folder.
* Attach the zip file to this GitHub issue.

About GitHub attachments:

* GitHub only allows a few attachment extensions:
  `.png`, `.gif`, `.jpg`, `.docx`, `.pptx`, `.xlsx`, `.txt`, `.pdf`, `.zip` and `.gz`.
  Other attachments can be zipped and attached here. Files can also simply
  have a `.txt` extension added to their file name.

* GitHub has a size limit of 20 MB for attachments. If your attachment is too
  big, upload your attachment somewhere else (for example wikisend.com,
  send-anywhere.com, ge.tt or anonfile.com) and post the link in the GitHub
  issue. If you upload your attachment elsewhere, please avoid sites with
  restricted access or intrusive ads.
-->

### Describe your environment

* Type of computer running DMS:
* OS version of the computer running DMS:
* DMS version:
* UPnP AV or DLNA device(s) or software used:

### Issue Type
<!--
Put an X between the brackets for the one that applies
-->

* [ ] Bug report
* [ ] Feature request
* [ ] Other

### Description

[Describe the issue]

### Steps to reproduce

1. [First step]
2. [Second step]
3. [...]

### Expected behavior

[What you expect to happen]

### Actual behavior

[What actually happens]

### Reproducibility

[How often can the issue be reproduced?]

### Additional Information

[Any additional information, configuration or data that might be necessary to reproduce the issue]
