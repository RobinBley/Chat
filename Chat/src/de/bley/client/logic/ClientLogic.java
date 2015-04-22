/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bley.client.logic;

import de.bley.client.gui.Gui;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author Robin
 */
public class ClientLogic {

    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    String username;
    ArrayList<String> users;

    public ClientLogic(String ip, int port, String username) {
        users = new ArrayList<String>();
        try {
            socket = new Socket(ip, port);
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            reader = new BufferedReader(streamReader);
            writer = new PrintWriter(socket.getOutputStream());
            this.username = username;
            JSONObject helloMessage = new JSONObject();
            helloMessage.put("type", "hello");
            helloMessage.put("user", username);
            writer.println(helloMessage.toString());
            writer.flush();

            //
            //SERVER NUTZERDATEN SAGEN!!!!!!
            //
            System.out.println("Netzwerkverbindung steht");
        } catch (IOException ex) {
            Logger.getLogger(ClientLogic.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

    }

    public void sendMessage(String message, String receiver) {
        JSONObject jdata = new JSONObject();
        jdata.put("type", "message");
        jdata.put("message", message);
        jdata.put("receiver", receiver);
        jdata.put("sender", username);
        try {
            writer.println(jdata.toString());
            writer.flush();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void receiveMessage(String message) {
        JSONObject jdata = new JSONObject(message);

        if(jdata.get("type").equals("message")){
            Gui.getInstance().addMessage(jdata.getString("sender"), jdata.getString("message"));
        }else{
            if(jdata.get("type").equals("new connection")){
                users.add(jdata.getString("user"));
            }
        }
    }

    public class MessageListener implements Runnable {

        public void run() {
            String nachricht;
            try {

                while ((nachricht = reader.readLine()) != null) {
                    receiveMessage(nachricht);

                } // Ende der while-Schleife
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } // run schlie�en
    }

}
