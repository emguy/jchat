import java.io.*;
import client.*;
import common.*;

public class ClientConsole implements ChatIF {

  final public static int DEFAULT_PORT = 5555;
  ChatClient client;

  public ClientConsole(String host, int port) {
    try {
      client = new ChatClient(host, port, this);
    } catch (IOException exception) {
      this.display("Error: Can't setup connection!" + " Terminating client.");
      System.exit(1);
    }
  }

  public void accept() {
    try {
      BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
      String message;

      while (true) {
        message = fromConsole.readLine();
        while (message.equals(""))
          message = fromConsole.readLine();
        client.handleMessageFromClientUI(message);
      }
    } catch (Exception e) {
      this.display("Unexpected error while reading from console!");
    }
  }

  public void display(String message) {
    System.out.println(">" + message);
    System.out.print("$ ");
  }

  public static void main(String[] args) {
    String host = "";
    int port = 0;
    try {
      host = args[0];
      port = Integer.parseInt(args[1]);
    } catch (ArrayIndexOutOfBoundsException e) {
      host = "localhost";
      port = DEFAULT_PORT;
    }
    ClientConsole chat = new ClientConsole(host, port);
    chat.accept();
  }
}
