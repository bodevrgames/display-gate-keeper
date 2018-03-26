package com.brgplay.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.UUID;

/**
 * BR Games LLC
 * Bluetooth Server Plugin
 * Created by AugMac on 9/26/2017.
 */

class ServerSocket extends Thread {

    // SETUP
    private final int MessageBuffer = 2048; // Max Message Size

    // Bluetooth Variables
    private BluetoothDevice ClientDevice;
    private BluetoothAdapter BT_Adapter;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket MainSocket;
    public boolean Connected;

    // Connection with Client Device
    Transmission mTransmission;

    // UUID
    private final static UUID ClientUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Debug
    private final String dTAG = "<BTMsg> ";

    /**
     * CONSTRUCTOR
     * @param BTAdapter Bluetooth Adaptor Reference
     * @param ClientDevice    ClientDevice Device asking to connect
     */
    public ServerSocket(BluetoothAdapter BTAdapter, BluetoothDevice ClientDevice) {
        this.BT_Adapter = BTAdapter;
        this.ClientDevice = ClientDevice;
        Connected = false;
    }

    // Thread Start
    public void run() {


        // Creating Server to Client Socket
        try {
            // Creates a Server socket with the Client who has the respective UUID
            serverSocket = BT_Adapter.listenUsingInsecureRfcommWithServiceRecord(ClientDevice.getName(), ClientUUID);
            Log.d(dTAG, "[OK] Socket Created -> [" + ClientDevice.getName() + "] ");
        } catch (IOException e) {
            Log.d(dTAG, "[FAIL] Remote Device [" + ClientDevice.getName() + "]-> Socket ERROR");
            CloseConnection();
            return;
        }

        // Main Socket to Deal with Connection
        int maxConnectAttempts = 60; // 1 minute
        do {
            try {
                //Initiates gameSocket with severSocket first request accepted
                Log.d(dTAG, "[OK] Waiting Connection from [" + ClientDevice.getName() + "] ");
                MainSocket = serverSocket.accept();
                Log.e(dTAG, "[OK] Remote Device [" + ClientDevice.getName() + "] asking to connected...");
            } catch (IOException e) {
                Log.e(dTAG, "[FAIL] Remote Device [" + ClientDevice.getName() + "] Not Connected ! ");
            }

            maxConnectAttempts--;
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while((!MainSocket.isConnected())&&(maxConnectAttempts>0));

        if(maxConnectAttempts==0){
            CloseConnection();
            return;
        }

        // Start connection thread
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
        if((mTransmission!=null)&&(Connected)) {
            return mTransmission.SendMessage(data);
        }
        return false;
    }

    /**
     * Close Connection
     */
    public void CloseConnection(){

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Connected = false;
        }

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
     * Manages the Transmission between Server And Client Device
     */
    private class Transmission extends Thread {

        // Data Sender and Receiver
        private InputStream IS;
        private OutputStream OS;

        // Data
        int DataSize;
        byte[] Message;
        byte[] DataBuffer;
        boolean hasMessage = false;

        // Thread Start
        public void run() {


            // Loading InputStream
            try {
                IS = MainSocket.getInputStream();
                Log.d(dTAG, "[OK] [" + ClientDevice.getName() + "] Input Stream ready !");
            } catch (IOException e) {
                Log.e(dTAG, "[FAIL] [" + ClientDevice.getName() + "] Input Stream ERROR.", e);
                CloseConnection();
                return;
            }

            // Loading OutputStream
            try {
                OS = MainSocket.getOutputStream();
                Log.d(dTAG, "[OK] [" + ClientDevice.getName() + "] Output Stream ready !");
            } catch (IOException e) {
                Log.e(dTAG, "[FAIL] [" + ClientDevice.getName() + "] Output Stream ERROR.", e);
                CloseConnection();
                return;
            }

            // Ready for Connection
            Connected = true;
            Log.e(dTAG, "[OK] [" + ClientDevice.getName() + "] CONNECTED !");

            // Setting Data Buffer Size
            DataBuffer = new byte[MessageBuffer];

            // Keep Reading Messages
            while (true) {

                    try {
                        // Receives a message DataRead with bytes
                        Log.d(dTAG, "[Server] Waiting Message ... ");
                        DataSize = IS.read(DataBuffer);
                        Log.d(dTAG, "[Server] Message Received = " + DataBuffer);
                    } catch (IOException e) {
                        Log.d(dTAG, "[Server] [FAIL] Reading Message ERROR", e);
                        CloseConnection();
                        return;
                    }

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
                    Log.d(dTAG, "[OK] Message sent to [" + ClientDevice.getName() + "] -> " + Message.toString());
                } catch (IOException e) {
                    Log.d(dTAG, "[FAIL] Can't send message to [" + ClientDevice.getName() + "]", e);
                    return false;
                }

                return true;
        }

    }
}




