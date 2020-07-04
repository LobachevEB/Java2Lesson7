package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    Server server;
    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    private String nick;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split("\\s");
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                if(server.isLoggedIn(newNick)){
                                    sendMsg(String.format("Пользователь с ником %s уже подключен.",newNick));
                                    continue;
                                }
                                else {
                                    sendMsg("/authok " + newNick);
                                    nick = newNick;
                                    login = token[1];
                                    server.subscribe(this);
                                    System.out.printf("Клиент %s подключился \n", nick);
                                    break;
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                                continue;
                            }
                        }

                        server.broadcastMsg(str);
                    }
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }

                        if (str.startsWith("/w ")) { // Отправка конкретному юзеру
                            System.out.println(str);;
                            String[] userAndMsg = str.split("\\s",3);
                            if(userAndMsg.length > 2){ //0 - /w, 1 - юзер, 2 - сообщение
                                String user = userAndMsg[1];
                                String msg = userAndMsg[2];
                                server.sendMsgToUser(msg,user);
                            }
                        }
                        else if (str.startsWith("/g ")) { // Отправка группе юзеров (/g nick1,nick2,nick3 message)
                            String data = str.substring(2).trim();
                            String[] usersAndMsg = data.split("(?<!,)\\s",2);
                            if(usersAndMsg.length == 2){
                                String[] users = usersAndMsg[0].split(",\\s*");
                                if(users.length > 0){
                                    server.sendMsgToGroup(usersAndMsg[1],users);
                                }
                            }
                        }
                        else{
                            server.broadcastMsg(str);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Клиент отключился");
                    server.unsubscribe(this);
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void sendMsg(String str) {
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
