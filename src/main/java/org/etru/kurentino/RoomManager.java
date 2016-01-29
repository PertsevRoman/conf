/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.etru.kurentino;

import com.google.gson.JsonArray;
import java.util.HashMap;

/**
 * Класс для менеджмента комнат
 * @author joker
 */
public class RoomManager {
    // Комнаты
    private final HashMap<String, Room> rooms = new HashMap<>();
    
    public RoomManager() {}
    
    /**
     * Создает новую комнату
     * @param name Имя комнаты
     * @return Экземпляр класса комнаты
     * @throws Exception Если комната существует
     */
    public Room createNewRoom(String name) throws Exception {
        if (rooms.containsKey(name)) {
            throw new Exception("Такая комната уже зарегистрироана");
        }
        
        Room state = new Room(name);
        
        rooms.put(name, state);
        
        return state;
    }
    /**
     * Удаление комнаты
     * @param roomName Имя удаляемой конаты
     */
    public void removeRoom(String roomName) {
        if(rooms.containsKey(roomName)) {
            System.out.println("Удаление комнаты: " + roomName);
            Room rm = rooms.remove(roomName);
            rm.release();
        }
    }
    
    /**
     * Возвращает список комнат в виде JSON массива
     * @return Список комнат
     */
    public JsonArray getRoomList() {
        JsonArray arr = new JsonArray();
        
        rooms.keySet().stream().forEach((rName) -> {
            arr.add(rName);
        });
        
        return arr;
    }
    
    /**
     * Возвращает команату по имени. Если комнаты нет и параметр create равен true, создает новую комнату
     * @param roomName Имя комнаты
     * @param create Создавать новую при отсутствии в систке
     * @return Экземпляр класса комнаты
     */
    public Room getRoom(String roomName, boolean create) throws Exception {
        if(rooms.containsKey(roomName)) {
            return rooms.get(roomName);
        } else if (create) {
            return this.createNewRoom(roomName);
        }
        
        return null;
    }
}
