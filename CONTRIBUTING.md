# Reporting an issue

In order to help diagnose and fix your issue we usually need your "debug information". To generate the "debug information" using the GUI, follow these steps:

* Start Digital Media Server (DMS).
* Go to the `Logs` tab.
* Press the `Create TRACE logs` button, which will restart DMS in the TRACE mode. If that doesn't work for some reason, either select `Trace` from the `Log Level` dropdown at the bottom of the screen or set `log_level = TRACE` in `DMS.conf` and restart DMS manually.
* Reproduce the bug.
* Click `Pack debug files` on the lower left.
* Click `Zip selected files`.
* Save the zip file to a location you will remember.
* Attach the zip file in the corresponding GitHub issue.

To generate the "debug information" for a headless installation, use the following steps:
* Start Digital Media Server (DMS) with ```trace``` added as a command line parameter, for example ```./DMS.sh trace```.
* Reproduce the bug.
* Stop DMS.
* Find the DMS log file and configuration file and put them in an archive format, for example ```zip``` or ```tar.gz```. The location of these files will vary depending your the platform, configuration and permissions, but their default names are ```debug.log``` and ```DMS.conf```. It might be easier to search for them, but the default locations on Linux are:
  * Configuration file: ```~/.config/DigitalMediaServer/DMS.conf```
  * Log file: ```/var/log/DMS/<username>/debug.log```. If this folder isn't available for writing, DMS will fall back to either the profile folder (where DMS.conf is found) or the system temp folder.
* Attach the zip file in the corresponding GitHub issue.

If it is a new bug that wasn't there in a previous version, we will often request debug information from the last version that did not have the bug too. If that is the case, please repeat the above steps for the last version without the bug as well.

Thanks for your efforts in helping us make Digital Media Server better!

# Creating a Pull Request

We welcome Pull Requests. Please be descriptive about the purpose of the code change, and please try to use the code conventions we use.

# <a name="Attachments"></a>Attachments

GitHub only allows a few attachment extensions: `.png`, `.gif`, `.jpg`, `.docx`, `.pptx`, `.xlsx`, `.txt`, `.pdf`, `.zip` and `.gz`. Other attachments can be zipped and attached here. Files can also simply have a `.txt` extension added to their file name.

GitHub has a size limit of 20 MB for attachments. If your attachment is too big, upload your attachment somewhere else (for example [wikisend.com](http://wikisend.com), [rapidshare.space](http://www.rapidshare.space/, [ge.tt](http://ge.tt) or [anonfile.com](https://anonfile.com)) and post the link in the GitHub issue. If you upload your attachment elsewhere, please avoid sites with restricted access or intrusive ads.
