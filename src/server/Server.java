package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();

        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                System.out.println("socket.getRemoteSocketAddress(): "+socket.getRemoteSocketAddress());
                System.out.println("socket.getLocalSocketAddress() "+socket.getLocalSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void broadcastMsg(String msg){
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }

    void sendMsgToUser(String msg,String user){
        for (ClientHandler client : clients) {
            if(client.getNick().equals(user)){
                client.sendMsg(msg);
            }
        }
    }

    void sendMsgToGroup(String msg, String... users){
        for (ClientHandler client : clients) {
            for (String user: users) {
                if(client.getNick().equals(user)){
                    client.sendMsg(msg);
                }
            }
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public Boolean isLoggedIn(String nickName){
        for (ClientHandler client: clients) {
            if(client.getNick().equals(nickName)){
                return true;
            }
        }
        return false;
    }

}
