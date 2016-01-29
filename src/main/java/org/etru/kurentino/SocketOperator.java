/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.etru.kurentino;

import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;

/**
 * Класс для операций с вебсокетами
 * @author joker
 */
public class SocketOperator {
    private WebSocket ws = null;

    /**
     * @return the ws
     */
    public WebSocket getWebSocket() {
        return ws;
    }
    
    /**
     * Отправка нового сообщения
     * @param message Сообщение
     */
    public void sendMessage(String message) {
        ws.send(message);
    }
    
    /**
     * Отправка сообщения в формате JSON
     * @param msg Сообщение
     */
    public void sendMeassage(JsonObject msg) {
        String strMsg = msg.toString();
        sendMessage(strMsg);
        
        System.out.println("На отправку: " + strMsg);
    }

    /**
     * @param socket the ws to set
     */
    public void setWebSocket(WebSocket socket) {
        this.ws = socket;
    }
}
