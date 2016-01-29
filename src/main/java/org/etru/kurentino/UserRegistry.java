/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.etru.kurentino;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.java_websocket.WebSocket;

/**
 * Реестр всех подключенных пользователей
 * @author joker
 */
public class UserRegistry {
    private final BiMap<WebSocket, User> users = HashBiMap.create();
    private final BiMap<String, User> names = HashBiMap.create();
    
    /**
     * Возвращает пользователя по сессии
     * @param session Сессия WebSocket
     * @return Пользователь
     */
    public User getUserBySession(WebSocket session) {
        return users.get(session);
    }
    
    /**
     * Возвращает сокет по пользователю
     * @param user Пользователь
     * @return Сокет
     */
    public WebSocket getSocketByUser(User user) {
        return users.inverse().get(user);
    }
    
    /**
     * Возвращает сокет по имени пользователя
     * @param userName Имя пользователя
     * @return Сокет
     */
    public WebSocket getSocketByUserName(String userName) {
        return users.inverse().get(names.get(userName));
    }
}
