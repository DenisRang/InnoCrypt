package com.sansara.develop.innocrypt.model;



public class Friend extends User{
    public String id;
    public String idRoom;
    public String key;

    @Override
    public String toString() {
        return "Friend{" +
                "id='" + id + '\'' +
                ", idRoom='" + idRoom + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
