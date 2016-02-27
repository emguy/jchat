package ocsf.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionToClient extends Thread {
  private AbstractServer server;
  private Socket clientSocket;
  private ObjectInputStream input;
  private ObjectOutputStream output;
  private boolean readyToStop;
  private HashMap<String, Object> savedInfo = new HashMap<>(10);

  ConnectionToClient(ThreadGroup group, Socket clientSocket, AbstractServer server) throws IOException {
    super(group, (Runnable) null);
    this.clientSocket = clientSocket;
    this.server = server;
    clientSocket.setSoTimeout(0); // make sure timeout is infinite
    try {
      input = new ObjectInputStream(clientSocket.getInputStream());
      output = new ObjectOutputStream(clientSocket.getOutputStream());
    } catch (IOException ex) {
      try {
        closeAll();
      } catch (Exception exc) {
      }
      throw ex; // Rethrow the exception.
    }

    readyToStop = false;
    start(); // Start the thread waits for data from the socket
  }

  final public void sendToClient(Object msg) throws IOException {
    if (clientSocket == null || output == null)
      throw new SocketException("socket does not exist");

    output.writeObject(msg);
  }

  final public void close() throws IOException {
    readyToStop = true; // Set the flag that tells the thread to stop

    try {
      closeAll();
    } finally {
      server.clientDisconnected(this);
    }
  }

  final public InetAddress getInetAddress() {
    return clientSocket == null ? null : clientSocket.getInetAddress();
  }

  public String toString() {
    return clientSocket == null ? null
        : clientSocket.getInetAddress().getHostName() + " (" + clientSocket.getInetAddress().getHostAddress() + ")";
  }

  public void setInfo(String infoType, Object info) {
    savedInfo.put(infoType, info);
  }

  public Object getInfo(String infoType) {
    return savedInfo.get(infoType);
  }

  final public void run() {
    server.clientConnected(this);

    try {
      // The message from the client
      Object msg;

      while (!readyToStop) {
        // This block waits until it reads a message from the client
        // and then sends it for handling by the server
        msg = input.readObject();
        server.receiveMessageFromClient(msg, this);
      }
    } catch (Exception exception) {
      if (!readyToStop) {
        try {
          closeAll();
        } catch (Exception ex) {
        }

        server.clientException(this, exception);
      }
    }
  }

  private void closeAll() throws IOException {
    try {
      // Close the socket
      if (clientSocket != null)
        clientSocket.close();

      // Close the output stream
      if (output != null)
        output.close();

      // Close the input stream
      if (input != null)
        input.close();
    } finally {
      output = null;
      input = null;
      clientSocket = null;
    }
  }

  protected void finalize() {
    try {
      closeAll();
    } catch (IOException e) {
    }
  }
}
