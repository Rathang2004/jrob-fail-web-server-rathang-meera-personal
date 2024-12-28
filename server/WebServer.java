package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class WebServer extends Thread implements AutoCloseable
{

    private ServerSocket serverSocket;
    int port;
    String documentRoot;

    public static void main(String[] args) throws NumberFormatException, Exception {
        if (args.length != 2) {
            System.err.println("usage: java WebServer <port number> <document root>");
            System.exit(1);
        }

        try (WebServer server = new WebServer(
                Integer.parseInt(args[0]),
                args[1])) {
            server.listen();
        }
    }
    //ObjectMapper mapper = new ObjectMapper();
    
    public WebServer(int port, String documentRoot) throws IOException 
    {
        this.port = port;
        this.documentRoot = documentRoot;
        System.out.println("Port NUMBER: " + port + " WebRoot:!!!! " + documentRoot);
        serverSocket = new ServerSocket(port);
    }
    public WebServer(int port, String documentRoot, String mimeTypes) 
    {

    }
    public String getDocumentRoot()
    {
        return documentRoot;
    }

    /**
     * After the webserver instance is constructed, this method will be
     * called to begin listening for requestd
     */
    public void listen() 
    {
        // Feel free to change this logic
        while (true) 
        {
            try 
            {
                Socket socket = serverSocket.accept();
                MultiThreading threading = new MultiThreading(socket,this);
                Thread threads = new Thread((Runnable) threading);
                threads.start();
            } 
            catch (IOException e) 
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
                

            
        }
    }

    @Override
    public void close() throws Exception 
    {
        this.serverSocket.close();
    }
}