package server;

import java.io.*;
import ocsf.server.*;
import common.*;
import userInfo.*;

public class ChatServer extends AbstractServer {

  final public static int DEFAULT_PORT = 5555;
  final static String DB_PATH = "userInfo/db";
  ChatIF serverUI;
  boolean serverOn;

  public ChatServer(int port, ChatIF serverUI) {
    super(port);
    this.serverUI = serverUI;
    serverOn = true;
  }

  public void handleMessageFromClient(Object msg, ConnectionToClient client) {
    if (client.getInfo("ID") != null || msg.toString().substring(0, 7).equals("#login ")
        || msg.toString().substring(0, 5).equals("#reg ") || msg.toString().substring(0, 9).equals("#regInfo ")) {
      if (msg.toString().charAt(0) == '#') {
        String[] command;
        command = msg.toString().split("\\s+");
        this.handleClientInteractions(command, client);
      } else {
        serverUI.display(client.getInfo("ID") + " said: " + msg);
        this.sendToAllClients(client.getInfo("ID") + " said: " + msg);
      }
    } else {
      try {
        client.sendToClient("<SERVER MSG> Login ID required.");
      } catch (IOException stc) {
        serverUI.display("~~~~~");
      }
      this.clientDisconnected(client);
    }
  }

  public void handleMessageFromServerUI(String message) {
    if (message.charAt(0) == '#') {
      String[] command;
      command = message.split("\\s+");
      this.handleCommandFromServerUI(command);
    } else {
      if (serverOn)
        this.sendToAllClients("SERVER MSG:" + message);
      else
        serverUI.display("<System> Server is down. Message cannot be sent.");
    }
  }

  public void handleCommandFromServerUI(String[] command) {
    try {
      if (command[0].equals("#quit")) {
        if (serverOn)
          this.handleMessageFromServerUI("Server shut down.");
        close();
        serverUI.display("<System> Server shut down.");
        System.exit(1);
      } else if (command[0].equals("#stop")) {
        if (serverOn) {
          if (this.isListening()) {
            this.stopListening();
            serverUI.display("<Sytem> Server has stopped accepting new clients.");
          } else
            serverUI.display("<System> Listening has already stopped.");
        } else
          serverUI.display("<System> Server is down now.");
      } else if (command[0].equals("#close")) {
        if (serverOn) {
          this.close();
          this.handleMessageFromServerUI("Server is down now.");
          serverUI.display("<System> Server is closed.");
          serverOn = false;
        } else
          serverUI.display("<System> Server has already been closed.");
      } else if (command[0].equals("#setport")) {
        if (serverOn)
          serverUI.display("<System> Run #close first before setting the port.");
        else {
          try {
            setPort(Integer.parseInt(command[1]));
            serverUI.display("<System> port is set to " + command[1] + ".");
          } catch (NumberFormatException e) {
            serverUI.display("<System> Invalid port.");
          }
        }
      } else if (command[0].equals("#start")) {
        if (serverOn) {
          if (this.isListening())
            serverUI.display("<System> Server already started.");
          else {
            listen();
            System.out.println("<System> Server starts.");
          }
        } else {
          if (!isListening()) {
            this.listen();
            serverOn = true;
            serverUI.display("<System> Server restarts.");
          }
        }
      } else if (command[0].equals("#getport")) {
        serverUI.display("<System> Current port number is " + this.getPort());
      } else
        serverUI.display("Command not found");
    } catch (Exception e) {
      serverUI.display("Error occurs while excuting commands");
    }
  }

  public void handleClientInteractions(String[] command, ConnectionToClient client) {
    try {
      if (command[0].equals("#login")) {
        if (client.getInfo("ID") == null) {
          if (UserInfo.findPasswordFromDB(command[1], DB_PATH).equals(""))
            client.sendToClient("<SERVER MSG> Incorrect User ID.");
          else if (!UserInfo.findPasswordFromDB(command[1], DB_PATH).equals(command[2]))
            client.sendToClient("<SERVER MSG> Incorrect password.");
          else {
            serverUI.display(command[1] + " has connected.");
            this.sendToAllClients(command[1] + " has logged on the server.");
            client.sendToClient("Welcome to SimpleChat version 0.1");
            client.setInfo("ID", command[1]);
          }
        } else
          client.sendToClient("<SERVER MSG> You have already logged in.");
      } else if (command[0].equals("#logoff")) {
        clientDisconnected(client);
        serverUI.display(client.getInfo("ID") + " has disconnected.");
        this.sendToAllClients(client.getInfo("ID") + " has disconnected.");
      } else if (command[0].equals("#reg")) {
        UserInfo info = new UserInfo(command[1], command[2], command[3]);
        if (info.checkReg(DB_PATH).equals("valid")) {
          info.addNewUserToDB(DB_PATH);
          serverUI.display("<System> New user " + command[1] + " has registered.");
          client.sendToClient("Registration Accepted.");
        } else if (info.checkReg(DB_PATH).equals("id"))
          client.sendToClient("<SERVER MSG> Uid has been used.");
        else if (info.checkReg(DB_PATH).equals("email"))
          client.sendToClient("<SERVER MSG> Email has been used.");
      } else if (command[0].equals("#regInfo")) {
        String uid = UserInfo.retrieveUID(command[1], DB_PATH);
        if (!uid.equals(""))
          client.sendToClient("<SERVER MSG> You uid is " + uid + ".");
        else
          client.sendToClient("<SERVER MSG> Email address not found.");
      }
    } catch (IOException stc) {
      serverUI.display("~~~~~");
    }
  }

  protected void serverStarted() {
    serverUI.display("Server listening for connections on port " + getPort());
  }

  protected void serverStopped() {
    serverUI.display("Server has stopped listening for connections.");
  }
}
