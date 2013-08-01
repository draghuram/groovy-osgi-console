
===================
groovy-osgi-console
===================

This project aims to provide command line access to OSGi run time
environment so that exploring services becomes easier. 

Introduction
============

Any one who has worked with a OSGi container (such as
http://www.eclipse.org/virgo) would attest to the fact that exploring
services and their APIs is a non-trivial task. You might end up
writing a test bundle that does nothing but call the APIs you want to
play with. This is very tedious as any small change requires full
compilation and deployment in the container. Alternatives exist but
none of them are much better.

This becomes apparent to any one who is accustomed to firing up a
shell such as Jython or Groovy, start instantiating classes and
calling Java APIs from the command prompt.  It is the quickest way of
playing around with Java APIs that obviates the need to write classes
and compile them. How ever, this approach doesn't work with a OSGi 
contaner such as Virgo because all the java services are only
available to the code running in the container. So I started looking
for ways of running a server in the container that provides network
connection to an outside shell such as Jython. With the power of
Google, I soon found out the following excellent article:

http://groovy.codehaus.org/Embedding+a+Groovy+Console+in+a+Java+Server+Application

which in turn led to:

http://69.89.31.118/~iterativ/wordpress/2007/05/14/embedding-a-groovy-console-in-a-java-server-application

The above project aims to do something very similar but in a Spring
MVC world. I took the code and modified it in order to get it working
in the brave and shiny world of OSGi. The code is tested with Virgo.

Building
========

You can simply start using the server found in the "dist" folder of
the repository. How ever, if you need to make some changes in the
server or perhaps want a different version of Groovy (the distributed
server is built with Groovy 2.0.5), you do need to build it
locally. The build requires "groovyc" to be available. ::

 $ cd groovy-osgi-console 
 $ build.sh

This builds a bundle called "groovy-osgi-console-server.jar".

Deployment
==========

Install "groovy-osgi-console-server.jar" in the OSGi container and
start it. The bundle has no external dependencies (all its
dependencies are packaged with in the jar file).

In case of Virgo, just copy it to the $VIRGODIR/pickup and Virgo would
automatically load it.  

Usage 
=====

let us assume that we have a OSGi service available that implements
the following interface: ::

    public interface Service {
        public String echo(String message);
    }

Here is one possible way of obtaining the service and calling its
method. ::

    $ telnet localhost 6789 (or)
    $ nc localhost 6789
    
    groovy:000> refs = context.getAllServiceReferences("com.example.Service", null)
    
    groovy:000> service = context.getService(refs[0]) 
    
    groovy:000> service.echo("Hello, World!\n")
    Hello, World!
    
Links 
=====

- http://www.eclipse.org/virgo/

- http://groovy.codehaus.org/Embedding+a+Groovy+Console+in+a+Java+Server+Application

- http://69.89.31.118/~iterativ/wordpress/2007/05/14/embedding-a-groovy-console-in-a-java-server-application

TODO 
====

This project should be considered an extremely early version in the
true tradition of "release early and release often". Several
enhancements can be done to make it easier to work with (though, it
works perfectly good enough for me in its present incarnation). Some
of the tasks include:

- Have a Makefile instead of shell script

- Currently, all the dependencies are checked in to the repo. instead,
  one should be able to build the project with latest versions of
  those dependencies instead of being forced to use the checked in
  versions.

Contact 
=======

Please send email to draghuram at gamil dot com if you have any
feedback and/or questions.

Acknowledgements 
================

Huge thanks go to Bruce Fancher whose idea and code are shamelessly
stolen. Similarly, Groovy folks deserve appreciation for a language
whose excellent integration with Java made this project possible.

