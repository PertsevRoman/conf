/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.etru.kurentino;

import org.kurento.client.KurentoClient;

/**
 * Класс для управления kms
 * @author joker
 */
public class KMSManager {
    private static final String DEF_KMS_URI = "ws://localhost:8888/kurento";
    
    private static KurentoClient kms = KurentoClient.create(System.getProperty("kms.ws.uri", DEF_KMS_URI));
    
    public KMSManager() {
    }
    
    public static KurentoClient kmsClient() {
        if(kms == null) {
            kms = KurentoClient.create(System.getProperty("kms.ws.uri", DEF_KMS_URI));
        }
        return kms;
    }
}
