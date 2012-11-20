#!/usr/bin/env groovy

/*
 * The code in this file is heavily based on the source obtained from 
 *     http://iterative.com/GroovyServer.tar.gz
 *
 * For more details, take a look at:
 *     http://groovy.codehaus.org/Embedding+a+Groovy+Console+in+a+Java+Server+Application
 */

package groovy.osgi.console

import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

import groovy.lang.Binding;
import org.codehaus.groovy.tools.shell.IO
import org.codehaus.groovy.tools.shell.Groovysh

class GroovyOSGiUtil {
    static def getSingletonService(context, name) {
        def references = context.getAllServiceReferences(name, null)
        return context.getService(references[0])
    }
}

abstract class GroovyService  {
    Map<String, Object> bindings;
    boolean launchAtStart = true;
    Thread serverThread;

    public GroovyService() {
        super();
    }
    
    public GroovyService(Map<String, Object> bindings) {
        this();
        this.bindings = bindings;
    }

    public void launchInBackground() {
        serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    launch();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        serverThread.setDaemon(true);
        serverThread.start();
    }

    public abstract void launch();

    protected Binding createBinding() {
        Binding binding = new Binding();

        if (bindings != null)  {
            for (Map.Entry<String, Object> nextBinding : bindings.entrySet()) {
                binding.setVariable(nextBinding.getKey(), nextBinding.getValue());
            }
        }

        return binding;
    }
    
    public void initialize() {
        if (launchAtStart) {
            launchInBackground();
        }
    }
    
    public void destroy() {
    }
}

class GroovyShellThread extends Thread {
    public static final String OUT_KEY = "out";

    Socket socket;
    Binding binding;

    public GroovyShellThread(Socket socket, Binding binding) {
        super();
        this.socket = socket;
        this.binding = binding;
    }

    @Override
    public void run() {
        try {
            final PrintStream out = new PrintStream(socket.getOutputStream())
            final InputStream inStream = socket.getInputStream();
            
            binding.setVariable(OUT_KEY, out);
            def groovyIO = new IO(inStream, out, out)
            final Groovysh groovy = new Groovysh(binding, groovyIO)

            try {
                groovy.run(null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            out.close();
            inStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class GroovyShellService extends GroovyService {
    public ServerSocket serverSocket;
    public int socket = 6789;
    public Thread serverThread;
    public List<GroovyShellThread>threads = new ArrayList<GroovyShellThread>();

    public GroovyShellService() {
        super();
    }

    public GroovyShellService(int socket) {
        super();
        this.socket = socket;
    }

    public GroovyShellService(Map bindings) {
        super(bindings);
    }
    
    public void launch() {
        println "GroovyShellService launch()";

        try {
            serverSocket = new ServerSocket(socket);
            println "GroovyShellService launch() serverSocket: " + serverSocket

            while (true) {
                Socket clientSocket = null;
                clientSocket = serverSocket.accept();
                println "GroovyShellService launch() clientSocket: " + clientSocket

                /*
                try {
                    clientSocket = serverSocket.accept();
                    println "GroovyShellService launch() clientSocket: " + clientSocket
                }
                catch (IOException e) {
                    e.printStackTrace()

                    // This particular "return" is causing groovyc to fail with an NPE.
                    return
                    }
                */

                GroovyShellThread clientThread = new GroovyShellThread(clientSocket, createBinding());
                threads.add(clientThread);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace()
            return;
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace()
                return;
            }

            println "GroovyShellService launch() closed connection"
        }
    }

    @Override
    public void destroy() {
        println "closing serverSocket: " + serverSocket;
        try {
            serverSocket.close();
            for (GroovyShellThread nextThread : threads)  {
                println "closing nextThread: " + nextThread;
                nextThread.getSocket().close();
            }
        } catch (IOException e) {
            e.printStackTrace()
        }
    }
}

class Server implements BundleActivator {
    GroovyShellService groovyShellService = null;

    void start(BundleContext context) {
        println "Groovy server started"
        groovyShellService = new GroovyShellService(["context": context, "util": GroovyOSGiUtil])
        groovyShellService.initialize()
    }

    void stop(BundleContext context) {
        println "Groovy server stopped"
        if (groovyShellService != null) {
            groovyShellService.destroy()
        }
    }
}
