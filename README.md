# MusicManager
  Music Manager it is an application written in JAVA. The purpose of the application is to implement a music manager for clients and also give him the facility to choose the music to be played to a Radio Server, where more clients can choose what music to be played on the Radio.
The technologies used are: JAVA, SWING(for GUI), multi-threading, networking(sockets), Oracle Database, ORM(Eclipse-Link-JPA2.0).
It has 2 projects: -The server side and The Client part.
  The server side is the RadioServer which operates all the requests from the clients and play music according to clients requests. It has a database which keeps the requested songs to be "played". The server communicates with clients through sockets and it handles every client request on a new thread.
  The client side has Graphical User Interface where the client can add his songs, albums and artists and visualize them(songs, albums and artists are stored in the database), and, also, he can send what song he wants to be played on the radio server and see what song is playing now.
