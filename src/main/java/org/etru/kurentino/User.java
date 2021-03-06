/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.etru.kurentino;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import org.kurento.client.Continuation;
import org.kurento.client.ElementConnectionData;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.MediaProfileSpecType;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;

/**
 * Класс для описания пользователя
 * @author joker
 */
public class User extends SocketOperator {
    private MediaPipeline pline = null;
    
    private WebRtcEndpoint endPoint = null;
    private final HashMap<String, WebRtcEndpoint> recvPoints = new HashMap<>();
    
    private String name = null;
    private String roomName = null;

    User(String userName, MediaPipeline line) {
        System.out.println("Пользователь создан: " + userName);
        setName(userName);
        
        this.pline = line;
        
        initialize();
    }
    
    /**
     * Метод инициализации
     */
    private void initialize() {
        endPoint = new WebRtcEndpoint.Builder(pline).build();
        
        endPoint.addOnIceCandidateListener((OnIceCandidateEvent t) -> {
            sendCandidate(name, t);
        });
    }
    
    /**
     * Возвращает передающую точку
     * @return 
     */
    public WebRtcEndpoint getWebRtcEndpoint() {
        return endPoint;
    }

    /**
     * Добавление ICE сервера
     * @param candidate Кандидат
     * @param userName Имя пользователя. Если совпадает с текущим, кандидат добавляется к передающей точке, иначе - к соответствующей принимающей.
     */
    public void addCandidate(IceCandidate candidate, String userName) {
        if(userName == null ? name == null : userName.equals(name)) {
            System.out.println(String.format("Кандидат текущему пользователю: %s", userName));
            endPoint.addIceCandidate(candidate);
        } else {
            System.out.println(String.format("Кандидат передающему: %s", userName));
            if(recvPoints.containsKey(userName)) {
                recvPoints.get(userName).addIceCandidate(candidate);
            }
        }
    }

    /**
     * Возвращает имя пользователя
     * @return the name
     */
    public String getName() {
        if(name == null) {
            throw new NullPointerException("Имя пользователя не задано");
        }
        
        return name;
    }

    /**
     * Установка имени пользователя
     * @param userName the name to set
     */
    public final void setName(String userName) {
        if(this.name == null ? userName != null : !this.name.equals(userName)) {
            this.name = userName;
        }
    }

    /**
     * Сообщение в систему о возможности входа
     */
    public void canLogin() {
        System.out.println("Можем заходить");
        
        JsonObject json = new JsonObject();
        json.addProperty("id", "comein");
        
        System.out.println("Сообщение: " + json.toString());
        
        sendMeassage(json);
    }

    /**
     * Обработка приглашения
     * @param offer Приглашение
     */
    public void processOffer(String offer) {
        String answer = endPoint.processOffer(offer);
        
        System.out.println("Ответ для приглашения: " + answer);
        
        JsonObject message = new JsonObject();
        message.addProperty("id", "offerAnswer");
        message.addProperty("name", name);
        message.addProperty("answer", answer);
        
        sendMeassage(message);
        
        endPoint.gatherCandidates();
        
        System.out.println("Приглашение обработано");
    }
    
    @Override
    public String toString() {
        return "Пользователь: " + name;
    }

    /**
     * Имя комнаты
     * @return the roomName
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Инициализация имени комнаты для пользавателя
     * @param roomName Имя пользователя
     */
    public void setRoomName(String roomName) {
        if(this.roomName == null ? roomName != null : !this.roomName.equals(roomName)) {
            this.roomName = roomName;
        }
    }

    /**
     *
     * @param userName
     */
    public void sendCandidate(String userName, OnIceCandidateEvent t) {
        JsonObject message = new JsonObject();
        message.addProperty("id", "iceCandidate");
        message.addProperty("name", userName);
        message.addProperty("candidate", JsonUtils.toJsonObject(t.getCandidate()).toString());

        sendMeassage(message);
    }

    /**
     * Добавляет точку для приема видеосигнала
     * @param userName Пользователь, для которого добавлена точка
     */
    public void addRecvEndpoint(String userName) {
        WebRtcEndpoint point = new WebRtcEndpoint.Builder(pline).build();

        point.addOnIceCandidateListener((OnIceCandidateEvent t) -> {
            sendCandidate(userName, t);
        });

        recvPoints.put(userName, point);
    }
    
    /**
     * Удаляет точку приема для пользователя
     * @param userName Имя пользователя
     */
    public void removeRecvEndpoint(String userName) {
        if(recvPoints.containsKey(userName)) {
            // Удаление точки
            WebRtcEndpoint endp = recvPoints.remove(userName);
            endp.release(new Continuation<Void>() {
                    @Override
                    public void onSuccess(Void result) throws Exception {
                        System.out.println("Приемник для пользователя " + userName + " освобожден");
                    }

                    @Override
                    public void onError(Throwable cause) throws Exception {
                        System.err.println("Приемник для пользователя " + userName + " не освобожден");
                    };
            });
            
            // Отправка сообщения
            JsonObject msg = new JsonObject();
            msg.addProperty("id", "removeUser");
            msg.addProperty("name", userName);
            sendMeassage(msg);
        }
    }
    
    /**
     * Извлекает точку приема по имени пользователя
     * @param userName Имя пользователя
     * @return Точка приема
     */
    public WebRtcEndpoint getRecvEndpoint(String userName) {
        return recvPoints.get(userName);
    }

    /**
     * Освобождение ресурсов
     */
    public void release() {
        endPoint.release(new Continuation<Void>() {
                    @Override
                    public void onSuccess(Void result) throws Exception {
                        System.out.println("Передатчик освобожден");
                    }

                    @Override
                    public void onError(Throwable cause) throws Exception {
                        System.err.println("Передатчик не освобожден");
                    };
            });
    }
}
