/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.etru.kurentino;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;

class SocketHandler extends WebSocketServer {
    private static final Gson gCreator = new GsonBuilder().create();
    private static final KMSManager kms = new KMSManager();
    private static final RoomManager manager = new RoomManager();
    private static final HashBiMap<WebSocket, User> registry = HashBiMap.create();
    
    public SocketHandler(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }
    
    public SocketHandler(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket ws, ClientHandshake ch) {
        System.out.println(ws.getRemoteSocketAddress().getAddress().getHostAddress() + " - вошел в комнату!");
    }

    @Override
    public void onClose(WebSocket ws, int i, String string, boolean bln) {
        try {
            User usr = registry.get(ws);
            if (usr != null) {
                Room rm = manager.getRoom(usr.getRoomName(), false);

                rm.remove(usr.getName());

                registry.remove(ws);

                if(rm.getUsersCount() == 0) {
                    manager.removeRoom(rm.getName());
                }

                System.err.println(ws + " вышел из команты!");
            }
        } catch (Exception ex) {
            Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onMessage(WebSocket ws, String msg) {
        System.out.println("Сообщение: " + msg);
        
        JsonObject json = gCreator.fromJson(msg, JsonObject.class);
        
        switch (json.get("id").getAsString()) {
            case "login": {
                try {
                    Room rm = manager.getRoom(json.get("room").getAsString(), true);
                    User usr = rm.getUser(json.get("name").getAsString(), true);
                    
                    usr.setWebSocket(ws);
                    registry.put(ws, usr);
                    
                    // Приглашение на вход
                    usr.canLogin();
                    
                    // Отправка приема потока для пользователей
                    rm.sendNewUserForAll(usr.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case "offerVideo": {
                    try {
                        User usr = registry.get(ws);
                        usr.processOffer(json.get("offer").getAsString());
                        
                        // Передача списка открытых пользователей
                        Room rm = manager.getRoom(usr.getRoomName(), false);
                        if(rm.getUsersCount() > 1) {
                            rm.sendExistsUsers(usr.getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "onIceCandidate": {
                JsonObject jsonCandidate = json.get("candidate").getAsJsonObject();
                String addedName = json.get("name").getAsString();
                
                User user = registry.get(ws);
                
                if (user != null) {
                    IceCandidate candidate = new IceCandidate(jsonCandidate.get("candidate").getAsString(),
                            jsonCandidate.get("sdpMid").getAsString(),
                                jsonCandidate.get("sdpMLineIndex").getAsInt());
                    user.addCandidate(candidate, addedName);
                }
                break;
            }
            case "sendVideoTo":  {
                    try {
                        // Извлечение пользователей
                        User usr = registry.get(ws);
                        Room rm = manager.getRoom(usr.getRoomName(), false);
                        String recvName = json.get("sender").getAsString();
                        User sender = rm.getUser(recvName, false);

                        // Соединение endpoint
                        usr.addRecvEndpoint(recvName);
                        WebRtcEndpoint point = usr.getRecvEndpoint(recvName);
                        WebRtcEndpoint sendPoint = sender.getWebRtcEndpoint();
                        sendPoint.connect(point);
//                        point.connect(sendPoint);
                        
                        // Обработка данных для приема
                        String answerSdp = point.processOffer(json.get("offer").getAsString());
                        JsonObject answer = new JsonObject();
                        answer.addProperty("id", "offerAnswer");
                        answer.addProperty("name", sender.getName());
                        answer.addProperty("answer", answerSdp);
                        
                        // Отправка
                        usr.sendMeassage(answer);
                        
                        // Прогон кандидатов
                        point.gatherCandidates();
                    } catch (Exception ex) {
                        Logger.getLogger(SocketHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            default:
                ws.send("Неверное сообщение " + json.get("id").getAsString());
                break;
          }
    }

    @Override
    public void onError(WebSocket ws, Exception excptn) {
        excptn.printStackTrace();
        if(ws != null) {
            // Вывод данных о текущем клиенте
        }
    }
}