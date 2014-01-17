package ocsf.client;
import java.io.*;
import java.net.*;
import java.util.*;
public abstract class AbstractClient implements Runnable{

  private Socket clientSocket;
  private ObjectOutputStream output;
  private ObjectInputStream input;
  private Thread clientReader;
  private boolean readyToStop= false;
  private String host;
  private int port;

  public AbstractClient(String host, int port){
    this.host = host;
    this.port = port;
  }

  final public void openConnection() throws IOException{
    if(isConnected())
      return;
    try{
      clientSocket= new Socket(host, port);
      output = new ObjectOutputStream(clientSocket.getOutputStream());
      input = new ObjectInputStream(clientSocket.getInputStream());
    }
    catch (IOException ex){
      try{
        closeAll();
      }
      catch (Exception exc) { }

      throw ex; // Rethrow the exception.
    }

    clientReader = new Thread(this);  //Create the data reader thread
    readyToStop = false;
    clientReader.start();  //Start the thread
  }

  final public void sendToServer(Object msg) throws IOException{
    if (clientSocket == null || output == null)
      throw new SocketException("socket does not exist");

    output.writeObject(msg);
  }

  final public void closeConnection() throws IOException{
    readyToStop= true;
    try{
      closeAll();
    }
    finally{
      connectionClosed();
    }
  }

  final public boolean isConnected(){
    return clientReader!=null && clientReader.isAlive();
  }

  final public int getPort(){
    return port;
  }

  final public void setPort(int port){
    this.port = port;
  }

  final public String getHost(){
    return host;
  }

  final public void setHost(String host){
    this.host = host;
  }

  final public InetAddress getInetAddress(){
    return clientSocket.getInetAddress();
  }

  final public void run(){
    connectionEstablished();
    Object msg;

    try{
      while(!readyToStop){
        msg = input.readObject();

        handleMessageFromServer(msg);
      }
    }
    catch (Exception exception){
      if(!readyToStop){
        try{
          closeAll();
        }
        catch (Exception ex) { }

        connectionException(exception);
      }
    }
    finally{
      clientReader = null;
    }
  }

  protected void connectionClosed() {}
  protected void connectionException(Exception exception) {}
  protected void connectionEstablished() {}
  protected abstract void handleMessageFromServer(Object msg);

  private void closeAll() throws IOException{
    try{
      if (clientSocket != null)
        clientSocket.close();
      if (output != null)
        output.close();
      if (input != null)
        input.close();
    }
    finally{
      output = null;
      input = null;
      clientSocket = null;
    }
  }
}
