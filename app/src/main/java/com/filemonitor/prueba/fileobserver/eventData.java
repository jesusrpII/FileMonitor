package com.filemonitor.prueba.fileobserver;

import java.io.Serializable;
import java.util.Date;

// Para enviar datos por broadcast deben extender la clase Serializable
public class eventData implements Serializable {
    public typeofevents event_type = typeofevents.UNKNOWN;
    public String event_path = "/path";
    public Date event_time;
    public int num = -1;

    public eventData(typeofevents type, String path, Date time){
        event_path = path;
        event_type = type;
        event_time = time;
    }

    public eventData(typeofevents type, String path, Date time, int n){
        event_path = path;
        event_type = type;
        event_time = time;
        num= n;
    }
}
