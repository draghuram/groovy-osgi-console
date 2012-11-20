#!/bin/bash

set -e

LIBDIR=`pwd`/lib
SRCDIR=`pwd`/osgi_console_server

mkdir -p classes

groovyc -d classes -cp $LIBDIR/org.osgi.impl.framework.eclipse.jar $SRCDIR/osgi_console_server.groovy 

cd classes

for f in groovy-all-2.0.5.jar jansi-1.9.jar jline-1.0.jar
do
    cp $LIBDIR/$f .
done

jar cvfm ../groovy-osgi-console-server.jar $SRCDIR/MANIFEST.MF *

cd ..

