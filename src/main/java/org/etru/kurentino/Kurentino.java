/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.etru.kurentino;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

public class Kurentino {
    static public void main(String[] args) throws InterruptedException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException{
        int port = 8025;
        
        SocketHandler s = new SocketHandler(port);
        
        
        String STORETYPE = "JKS";
        String KEYSTORE = "/home/joker/certs/keystore.jks";
        String STOREPASSWORD = "From1234qQ";
        String KEYPASSWORD = "From1234qQ";

        KeyStore ks = KeyStore.getInstance( STORETYPE );
        File kf = new File( KEYSTORE );
        ks.load( new FileInputStream( kf ), STOREPASSWORD.toCharArray() );

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init( ks, KEYPASSWORD.toCharArray() );
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init( ks );

        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance( "TLS" );
        sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
        
        s.setWebSocketFactory( new DefaultSSLWebSocketServerFactory( sslContext ) );
        
        s.start();
        
        System.err.println("Сервер стартовал на порту: " + s.getPort());
        
        BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
        
        while(true) {
            String in = sysin.readLine();
            if(in.equals("exit")) {
                s.stop();
                break;
            } else if(in.equals("restart" )) {
                s.stop();
                s.start();
                break;
            }
        }
    }
}
