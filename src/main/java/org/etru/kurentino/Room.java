/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.etru.kurentino;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashMap;
import org.java_websocket.WebSocket;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;

/**
 * Класс команты
 * @author joker
 */
public class Room {
    private String name = null;
    private final HashMap<String, User> users = new HashMap<>();
    private final MediaPipeline pline = KMSManager.kmsClient().createMediaPipeline();
    
    public Room() {
    }

    Room(String roomName) {
        System.out.println("Комната " + roomName + " создана.");
        setName(roomName);
    }
    
    /**
     * Создает нового пользователя
     * @param userName Имя пользователя
     * @return Экземпляр класса пользователя
     * @throws Exception
     */
    public User createNewUser(String userName) throws Exception {
        if(users.containsKey(userName)) {
            throw new Exception("Пользователь с именем " + userName + " в комнате " + getName() + " уже зарегистрирован");
        }
        
        User newUser = new User(userName, pline);
        
        users.put(userName, newUser);
        
        return newUser;
    }
    
    /**
     * Количество пользователе в комнате
     * @return int
     */
    public int getUsersCount() {
        return users.size();
    }
    
    /**
     * Добавляет пользователя. Если параметр create равен true, создает нового
     * @param userName Имя пользователя
     * @param create Создавать нового пользователя
     * @return Экземпляр класса пользователя
     * @throws Exception 
     */
    public User getUser(String userName, boolean create) throws Exception {
        if(create) {
            if(!users.containsKey(userName)) {
                System.out.println("Создаем нового пользователя: " + userName);
                User usr = createNewUser(userName);
                usr.setRoomName(name);
                return usr;
            } else {
                System.out.println("Пользователь " + userName + " уже существует.");
                throw new Exception("Пользователь с таким именем уже существует");
            }
        } else {
            if(users.containsKey(userName)) {
                return users.get(userName);
            }
        }
        
        return null;
    }
    
    /**
     * Отправка нового пользователя для существующих
     * @param name  Имя пользователя
     */
    void sendNewUserForAll(String userName) {
        // Выборка пользователя
        User usr = users.get(userName);
        users.remove(userName);
        
        // Формирование данных
        JsonArray arr = new JsonArray();
        arr.add(userName);
        
        JsonObject data = new JsonObject();
        data.addProperty("id", "existsList");
        data.add("names", arr);
        
        // Отправка данных
        users.values().stream().forEach((user) -> {
            user.sendMeassage(data);
        });
        
        // Возвращение пользователя
        users.put(userName, usr);
    }
    
    /**
     * Отправка списка сущетвующих пользователей для некоторого
     * @param userName Имя пользователя
     */
    public void sendExistsUsers(String userName) {
        // Выборка пользователя
        User usr = users.get(userName);
        users.remove(userName);
        
        // Формирование данных
        JsonArray arr = new JsonArray();
        
        users.keySet().stream().forEach((uName) -> {
            arr.add(uName);
        });
        
        JsonObject res = new JsonObject();
        res.addProperty("id", "existsList");
        res.add("names", arr);
        
        // Отправка
        usr.sendMeassage(res);
        
        // Возвращение пользователя
        users.put(userName, usr);
    }

    /**
     * Возвращает имя комнаты
     * @return the name Имя комнтаы
     */
    public String getName() {
        if (name == null) {
            throw new NullPointerException("Имя комнаты не задано");
        }
        
        return name;
    }

    /**
     * Установка имени команты
     * @param roomName Имя комнаты
     */
    public final void setName(String roomName) {
        if(this.name == null ? roomName != null : !this.name.equals(roomName)) {
            this.name = roomName;
        }
    }

    public void getUser(String asString, WebSocket ws, boolean b) {
        
    }

    /**
     * Удаляет пользователя по имени
     * @param name Имя пользователя
     */
    public void remove(String name) {
        User usr = users.remove(name);
        
        // Разъединение пользователей
        users.values().stream().forEach((user) -> {
            user.removeRecvEndpoint(name);
        });
        
        usr.release();
    }

    /**
     * Освобождение ресурсов комнаты
     */
    public void release() {
        pline.release(new Continuation<Void>() {
                    @Override
                    public void onSuccess(Void result) throws Exception {
                        System.out.println("Контейнер медиаресурсов освобожден");
                    }

                    @Override
                    public void onError(Throwable cause) throws Exception {
                        System.err.println("Контейнер медиаресурсов не освобожден");
                    };
            });
    }
}
