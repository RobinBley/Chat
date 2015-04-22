package de.bley.server;



import java.io.*;
import java.net.*;
import java.util.*;
import org.json.JSONObject;

public class Server {

    ArrayList<PrintWriter> clientAusgabeStroeme;
    HashMap<String, PrintWriter> users;

    public class ClientHandler implements Runnable {

        BufferedReader reader;

        Socket sock;

        public ClientHandler(Socket clientSocket) {
            users = new HashMap<String, PrintWriter>();
            try {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } // Konstruktor schlie�en

        public void run() {
            String nachricht;

            try {

                while ((nachricht = reader.readLine()) != null) {
                    JSONObject jMessage = new JSONObject(nachricht);
                    if (jMessage.get("type").equals("hello")) {
                        users.put(jMessage.getString("user"), clientAusgabeStroeme.get(0));
                        clientAusgabeStroeme.remove(0);
                        JSONObject message = new JSONObject();
                        message.put("type", "new connection");
                        message.put("user", jMessage.get("user"));
                        esAllenWeitersagen(message.toString());
                        System.out.println("Server: new user " + jMessage.getString("user"));
                    } else {
                        if (jMessage.get("type").equals("message")) {
                            if (jMessage.get("receiver").equals("")) {
                                esAllenWeitersagen(jMessage.toString());
                            }else{
                                unicast(jMessage.toString(), jMessage.get("receiver"));
                            }
                        }
                    }

                    System.out.println("Server: " + nachricht);

                } // Ende der while-Schleife
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } // run schlie�en
    } // innere Klasse schlie�en

    public static void main(String[] args) {
        new Server().los();
    }

    public void los() {

        try {
            ServerSocket serverSock = new ServerSocket(5000);
            JSONObject jmessage;

            while (true) {
                Socket clientSocket = serverSock.accept();
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientAusgabeStroeme.add(writer);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

            }
            // wenn wir hier angelangt sind, haben wir eine Verbindung

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void esAllenWeitersagen(String nachricht) {
        try {
            for (String key : users.keySet()) {
                ((PrintWriter) users.get(key)).println(nachricht);
                ((PrintWriter) users.get(key)).flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void unicast(String nachricht, String empfaenger) {
        try {
            for (String key : users.keySet()) {
                if (users.containsKey(empfaenger)) {
                    ((PrintWriter) users.get(empfaenger)).println(nachricht);
                    ((PrintWriter) users.get(empfaenger)).flush();

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}