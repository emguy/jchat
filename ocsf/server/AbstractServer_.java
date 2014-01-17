package ocsf.server;

import java.net.*;
import java.util.*;
import java.io.*;

public abstract class AbstractServer implements Runnable{
  private ServerSocket serverSocket = null;
  private Thread connectionListener;
  private int port;
  private int timeout = 500;
  private int backlog = 10;
  private ThreadGroup clientThreadGroup;
  private boolean readyToStop = false;

  public AbstractServer(int port){
    this.port = port;
    this.clientThreadGroup =new ThreadGroup("ConnectionToClient threads"){
        public void uncaughtException(Thread thread, Throwable exception){
          clientException((ConnectionToClient)thread, exception);
        }
      };
  }

  final public void listen() throws IOException{
    if (!isListening()){
      if (serverSocket == null)
        serverSocket = new ServerSocket(getPort(), backlog);

      serverSocket.setSoTimeout(timeout);
      readyToStop = false;
      connectionListener = new Thread(this);
      connectionListener.start();
    }
  }

  final public void stopListening(){
    readyToStop = true;
  }

  final synchronized public void close() throws IOException{
    if (serverSocket == null)
      return;
      stopListening();
    try{
      serverSocket.close();
    }
    finally{
      // Close the client sockets of the already connected clients
      Thread[] clientThreadList = getClientConnections();
      for (int i=0; i<clientThreadList.length; i++){
         try{
           ((ConnectionToClient)clientThreadList[i]).close();
         }
         // Ignore all exceptions when closing clients.
         catch(Exception ex) {}
      }
      serverSocket = null;
      serverClosed();
    }
  }

  public void sendToAllClients(Object msg){
    Thread[] clientThreadList = getClientConnections();

    for (int i=0; i<clientThreadList.length; i++){
      try{
        ((ConnectionToClient)clientThreadList[i]).sendToClient(msg);
      }
      catch (Exception ex) {}
    }
  }


  final public boolean isListening(){
    return (connectionListener != null);
  }

  synchronized final public Thread[] getClientConnections(){
    Thread[] clientThreadList = new
      Thread[clientThreadGroup.activeCount()];

    clientThreadGroup.enumerate(clientThreadList);

    return clientThreadList;
  }

  final public int getNumberOfClients(){
    return clientThreadGroup.activeCount();
  }

  final public int getPort(){
    return port;
  }

  final public void setPort(int port){
    this.port = port;
  }

  final public void setTimeout(int timeout){
    this.timeout = timeout;
  }

  final public void setBacklog(int backlog){
    this.backlog = backlog;
  }
  final public void run(){
    // call the hook method to notify that the server is starting
    serverStarted();

    try{
      // Repeatedly waits for a new client connection, accepts it, and
      // starts a new thread to handle data exchange.
      while(!readyToStop)
      {
        try{
          // Wait here for new connection attempts, or a timeout
          Socket clientSocket = serverSocket.accept();

          // When a client is accepted, create a thread to handle
          // the data exchange, then add it to thread group

          synchronized(this){
            ConnectionToClient c = new ConnectionToClient(
              this.clientThreadGroup, clientSocket, this);
          }
        }
        catch (InterruptedIOException exception){
          // This will be thrown when a timeout occurs.
          // The server will continue to listen if not ready to stop.
        }
      }

      // call the hook method to notify that the server has stopped
      serverStopped();
    }
    catch (IOException exception){
      if (!readyToStop){
        // Closing the socket must have thrown a SocketException
        listeningException(exception);
      }
      else{
        serverStopped();
      }
    }
    finally{
      readyToStop = true;
      connectionListener = null;
    }
  }

  protected void clientConnected(ConnectionToClient client) {}
  synchronized protected void clientDisconnected(ConnectionToClient client) {}
  synchronized protected void clientException(ConnectionToClient client, Throwable exception) {}
  protected void listeningException(Throwable exception) {}
  protected void serverStarted() {}
  protected void serverStopped() {}
  protected void serverClosed() {}
  protected abstract void handleMessageFromClient(Object msg, ConnectionToClient client);

  final synchronized void receiveMessageFromClient(Object msg, ConnectionToClient client){
    this.handleMessageFromClient(msg, client);
  }
}
