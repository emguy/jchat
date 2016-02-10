import java.io.*;
import common.*;
import server.*;

public class ServerConsole implements ChatIF {

  final public static int DEFAULT_PORT = 5555;
  ChatServer server;

  public ServerConsole(int port) {
    try {
      server = new ChatServer(port, this);
      server.listen();
    } catch (IOException exception) {
      this.display("ERROR - Could not listen for clients!");
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
        server.handleMessageFromServerUI(message);
      }
    } catch (Exception ex) {
      this.display("Unexpected error while reading from console!");
    }
  }

  public void display(String message) {
    System.out.println("> " + message);
    System.out.print("$ ");
  }

  public static void main(String[] args) {
    int port = 0;
    try {
      port = Integer.parseInt(args[0]);
    } catch (Throwable t) {
      port = DEFAULT_PORT;
    }
    ServerConsole sc = new ServerConsole(port);
    sc.accept();
  }
}
