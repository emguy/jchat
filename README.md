# jchat
---------

A light-weight client-server console chatting program built on top of ocsf
framework. The bidirectional server-client communication is enabled through
Java sockets and Java threads.  This program also includes a user authentication
system, a command handler, and a command-line shell.

## OCSF: The Object Client/Server Framework 
OCSF is a Java framework that can be used to develop client-server systems.

This framework is called OCSF to reflect the fact that the client-server
systems built using this framework will exchange Java objects. It is therefore
strongly object-oriented as all the lower-level communication layers are
encapslated inside the framework.

This framework is described in the book [Object-Oriented Software Engineering:
Practical Software Development using UML and Java][2] By Timothy C.  Lethbridge
and Robert Laganière.

## Requirement
1. [Java SE JDK][1]

## Compilation and Usage

1. Change directory to the root of `jchat`.

2. Execute the following to compile the client program.
```
$ javac ClientConsole.java
```
3. Execute the following to compile the server program.
```
$ javac ServerConsole.java
```
4. To run the client program, execute following
```
$ java ClientConsole [HOST] [PORT]
```
Here, the default HOST is localhost, and the default PORT is 5555.

5. To run the server program, execute the following
```
$ java ServerConsole [PORT]
```
Here, the default PORT is 5555.

## Client-side Commands

All commands must begin with the charactor '#'. After you log in, all other
inputs will be treated as messages.

1. Enter `#login UID PASSWORD` to login. Here the argument `UID` is the
   username, and `PASSWORD` is the password.

2. Enter `#logoff` to logoff and exit this program.

3. To register an account, enter `#reg UID PASSWORD EMAIL`, where `UID` is your
   intended user id, `PASSWORD` is your password, and `EMAIL` is your email
address.

4. Enter `#reginfo UID` to display user information.

## Server-side Commands

All commands must begin with the character '#'. All other inputs are treated as
messages, which send to all connected clients.

1. Enter `#quit` to terminate the server program.

2. Enter `#stop` to stop listening the port.

3. Enter `#start` to resume listening the port.

4. Enter `#setport` to change the port number.

5. Enter `#getport` to display the current port.

[1]:http://www.oracle.com/technetwork/java/javase/downloads/index.html
[2]:http://www.amazon.com/Object-Oriented-Software-Engineering-Practical-Development/dp/0077109082/ref=cm_cr_pr_product_top?ie=UTF8
