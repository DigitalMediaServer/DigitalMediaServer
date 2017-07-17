@echo off
echo Digital Media Server
echo ---------------------
echo In case of troubles with DMS.exe, this shell will launch DMS in a more old fashioned way
echo You can try to reduce the Xmx parameter value if you keep getting "Cannot create Java virtual machine" errors...
echo Last word: You must have java installed ! http://www.java.com
echo ------------------------------------------------
pause
start javaw -Xmx768M -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -classpath update.jar;dms.jar net.pms.PMS
