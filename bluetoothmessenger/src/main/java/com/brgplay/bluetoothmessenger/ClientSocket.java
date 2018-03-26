package com.brgplay.bluetoothmessenger;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * BR Games LLC
 * Bluetooth Client Socket Plugin
 * Created by AugMac on 9/26/2017.
 * Updated -> 03/11/2017.
 */


class ClientSocket extends Thread{

    // SETUP
    private final int MessageBuffer = 1024; // Max Message Size

    // Bluetooth Variables
    private BluetoothSocket MainSocket;
    private BluetoothDevice ServerDevice;
    public Boolean Connected;
    private int maxConnectAttempts = 60; // 1 minute


    // Connection with Server Device
    Transmission mTransmission;

    // UUID
    private final static UUID ClientUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Debug
    private final String dTAG = "<BTMsg> ";

    /**
     * CONSTRUCTOR
     * @param ServerDevice Server Device to connect
     */
    public ClientSocket(BluetoothDevice ServerDevice) {
        this.ServerDevice = ServerDevice;
        Connected = false;
    }

    // Thread Start
    public void run() {

        // Creating Client to Server Socket
        try {
            // Creates a Client Socket to connect with the Server
            MainSocket = ServerDevice.createRfcommSocketToServiceRecord(ClientUUID);
            Log.d(dTAG, "[OK] Socket Created !");
        } catch (IOException e) {
            Log.d(dTAG, "[ERROR] [" + ServerDevice.getName() + "] Not Found !!");
            CloseConnection();
            return;
        }

        // Starting the Connection
        maxConnectAttempts = 60; // 1 min
        do {
            try {
                Log.d(dTAG, "[OK] Searching for Remote Device [" + ServerDevice.getName() + "]");
                MainSocket.connect();
                Log.d(dTAG, "[OK] [" + ServerDevice.getName() + "] Found ! Starting Connection...");
            } catch (IOException e) {
                Log.e(dTAG, "[FAIL] [" + ServerDevice.getName() + "] -> Not Found !");
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            maxConnectAttempts--;
        }while(!MainSocket.isConnected()&&(maxConnectAttempts>0));

        if(maxConnectAttempts==0){
            CloseConnection();
            return;
        }

        // Starting Transmission
        mTransmission = new Transmission();
        mTransmission.start();

    }

    /**
     * Flag indicating if new message arrived.
     * @return  True: New Message available
     *          False: No Message available
     */
    public boolean hasMessage(){

        if((mTransmission!=null)&&(Connected))
            return mTransmission.hasMessage();
        return false;
    }

    /**
     * Get the bluetooth message received.
     * @return Byte Array with the message received.
     */
    public byte[] GetByteMessage(){
           return mTransmission.GetMessage();
    }

    /**
     * Get the bluetooth message received.
     * @return String with the message received.
     */
    public String GetStringMessage(){
        return new String(mTransmission.GetMessage());
    }

    /**
     * Send Message to the Server Device
     * @param Message Byte array (up to 1024 bytes) with data to the client.
     */
    public boolean SendMessage(byte[] Message){

        if((mTransmission!=null)&&(Connected)) {
            return mTransmission.SendMessage(Message);
        }
        return false;
    }

    /**
     * Send Message to the Server Device
     * @param Message String that will be sent to the Server (up to 1024 characters)
     */
    public boolean SendMessage(String Message){

        byte[] data = Message.getBytes();

        if((mTransmission!=null)&&(Connected)){
            return mTransmission.SendMessage(data);
        }
        return false;
    }

    /**
     * Close Socket
     */
    public void CloseConnection(){

        if(MainSocket!=null) {
            try {
                MainSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Connected = false;
        }
    }

    /** PRIVATE CLASS
     * Manages the Transmission between Client and Server Device
     */
    private class Transmission extends Thread {

        // Data Sender and Receiver
        private InputStream IS;
        private OutputStream OS;

        // Data
        int DataSize;
        byte[] Message;
        byte[] DataBuffer;
        boolean hasMessage;

        // Thread Start
        public void run() {

            // Loading InputStream
            try {
                IS = MainSocket.getInputStream();
                Log.d(dTAG, "[OK] [" + ServerDevice.getName() + "] Input Stream ready !");
            } catch (IOException e) {
                Log.e(dTAG, "[FAIL] ["+ServerDevice.getName() +"] Lost Connection...");
                CloseConnection();
                return;
            }

            // Loading OutputStream
            try {
                OS = MainSocket.getOutputStream();
                Log.d(dTAG, "[OK] ["+ServerDevice.getName() +"] Output Stream ready !");
            } catch (IOException e) {
                Log.e(dTAG, "[FAIL] ["+ServerDevice.getName() +"] Lost Connection...");
                CloseConnection();
                return;
            }

            // Ready for StarTransmission
            hasMessage = false;
            Connected = true;
            Log.d(dTAG, "[OK] [" + ServerDevice.getName() + "] CONNECTED !");

            // Setting Data Buffer Size
            DataBuffer = new byte[MessageBuffer];

            // Keep Reading Messages
            while (true) {

                try {
                    // Receives a message DataRead with bytes
                    Log.d(dTAG, "[Client] Waiting Message...");
                    DataSize = IS.read(DataBuffer);
                    Log.d(dTAG, "[Client] Message Received = " + DataBuffer);
                } catch (IOException e) {
                    Log.d(dTAG, "[Client] [FAIL] Reading Message ERROR", e);
                    CloseConnection();
                    return;
                }

                // Saving Message Read
                Message = new byte[DataSize];
                System.arraycopy(DataBuffer, 0, Message, 0, DataSize);
                hasMessage = true;
            }
        }

        /**
         * FLAG indicating if a new message has arrived.
         * @return  True: new message received
         *          False: no new message.
         */
        public boolean hasMessage(){
            return hasMessage;
        }

        /**
         * Get the Message received
         * @return Byte array with the message received.
         */
        public byte[] GetMessage() {
                hasMessage = false;
                return Message;
        }

        /**
         * Send Message
         * @param Message Byte Array with the message (up to 1024 bytes) to be send to the client.
         */
        public boolean SendMessage(byte[] Message) {

            try {
                OS.write(Message);
                Log.d(dTAG, "[OK] Message sent to [" + ServerDevice.getName() + "] -> " + Message.toString());
            } catch (IOException e) {
                Log.d(dTAG, "[FAIL] Can't send message to [" + ServerDevice.getName() + "]", e);
                return false;
            }

            return true;
        }

    }

}
