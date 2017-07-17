#!/bin/sh

CMD=`readlink -f $0`
DIRNAME=`dirname "$CMD"`

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$DMS_HOME" ] &&
		DMS_HOME=`cygpath --unix "$DMS_HOME"`
    [ -n "$JAVA_HOME" ] &&
		JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Setup DMS_HOME
if [ "x$DMS_HOME" = "x" ]; then
    DMS_HOME="$DIRNAME"
fi

export DMS_HOME
# XXX: always cd to the working dir: https://code.google.com/p/ps3mediaserver/issues/detail?id=730
cd "$DMS_HOME"

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
		JAVA="$JAVA_HOME/bin/java"
    else
		JAVA="java"
    fi
fi

# Setup the classpath
# since we always cd to the working dir, these a) can be unqualified and b) *must*
# be unqualified: https://code.google.com/p/ps3mediaserver/issues/detail?id=1122
DMS_JARS="update.jar:dms.jar"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    DMS_HOME=`cygpath --path --windows "$DMS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi
 
# Configure fontconfig (used by our build of FFmpeg)
if [ "x$FONTCONFIG_PATH" = "x" ]; then
    FONTCONFIG_PATH=/etc/fonts
    export FONTCONFIG_PATH
fi
if [ "x$FONTCONFIG_FILE" = "x" ]; then
    FONTCONFIG_FILE=/etc/fonts/fonts.conf
    export FONTCONFIG_FILE
fi

# Provide a means of setting max memory using an environment variable
if [ "x$DMS_MAX_MEMORY" = "x" ]; then
    DMS_MAX_MEMORY=768M
fi

# Execute the JVM
exec "$JAVA" $JAVA_OPTS -Xmx$DMS_MAX_MEMORY -Xss2048k -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Djna.nosys=true -classpath "$DMS_JARS" net.pms.PMS "$@"
