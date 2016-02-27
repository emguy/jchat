package userInfo;

import java.io.*;

public class UserInfo {

  String id;
  String pw;
  String email;

  public UserInfo(String id, String pw, String email) {
    this.id = id;
    this.pw = pw;
    this.email = email;
  }

  public static String findPasswordFromDB(String id, String fileName) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String s = null;
      String[] data = null;
      do {
        s = reader.readLine();
        data = s.split("\\s+");
      } while (!data[0].equals(id) && !s.equals(null));
      reader.close();
      return data[1];
    } catch (IOException ex) {
      return "";
    } catch (NullPointerException nu) {
      return "";
    }
  }

  public void addNewUserToDB(String fileName) {
    try {
      PrintWriter out = new PrintWriter(new FileWriter(fileName, true));
      out.println(this.toString());
      out.close();
    } catch (IOException ex) {
      System.out.println("<System> Error while reading from db.");
    }
  }

  public String toString() {
    return id + " " + pw + " " + email;
  }

  public String checkReg(String fileName) {
    String result = "valid";
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String s;
      String[] data;
      do {
        s = reader.readLine();
        data = s.split("\\s+");
        if (data[2].equals(this.email))
          result = "email";
        if (data[0].equals(this.id))
          result = "id";
      } while (!s.equals(null) || !result.equals("valid"));
      reader.close();
      return result;
    } catch (IOException ex) {
      return result;
    } catch (NullPointerException nu) {
      return result;
    }
  }

  public static String retrieveUID(String email, String fileName) {
    String uid = "";
    try {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String s;
      String[] data;
      do {
        s = reader.readLine();
        data = s.split("\\s+");
        if (data[2].equals(email))
          uid = data[0];
      } while (!s.equals(null) || !uid.equals(""));
      reader.close();
      return uid;
    } catch (IOException ex) {
      return uid;
    } catch (NullPointerException nu) {
      return uid;
    }
  }
}
