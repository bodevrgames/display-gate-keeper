package com.brgplay.bluetoothmessenger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Set;

/**
 * BR Games LLC
 * Bluetooth Client Socket Plugin
 * Created by AugMac on 9/26/2017.
 * Updated -> 03/11/2017.
 */

public class Server{


    //Bluetooth Variables
    private String ClientID;
    private BluetoothDevice ClientDevice;
    private BluetoothAdapter BT_Adapter;
    private boolean AutoReconnect;

    // Bluetooth Server Sockets
    private ServerSocket SSocket;

    // Debug
    private final String dTAG = "<BTMsg> ";

    // Class Constructor
    public Server(String ClientDeviceID){

        // saving ClientDevice ID
        ClientID = ClientDeviceID;

        // Loading Bluetooth Adapter
        ClientDevice = null;
        BT_Adapter = BluetoothAdapter.getDefaultAdapter();

        // Turn on Bluetooth
        if (!BT_Adapter.isEnabled()) BT_Adapter.enable();
        if (BT_Adapter.isDiscovering()) BT_Adapter.cancelDiscovery();
        AutoReconnect = false;

        Log.d(dTAG,"[OK] Bluetooth Messenger Started -> SERVER !");
    }

    /**
     * STEP 1 - Verify if ClientDevice Device was Paired with the Server Device previously.
     * @return True - Has ClientDevice paired
     *         False - No ClientDevice Paired
     */
    public boolean hasClientDevice(){

        Set<BluetoothDevice> pairedDevices = BT_Adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if(device.getName().equals(ClientID)) {
                    Log.d(dTAG,"[OK] Client Device Paired = " + device.getName());
                    ClientDevice = device;
                    return true;
                }
            }
        }

        Log.d(dTAG,"[FAIL] Client Device not paired !!!");
        return false;
    }

    /**
     * STEP 2 - Start the Connection with the ClientDevice
     * @return True: Connection was established.
     *         False: Can't reach the ClientDevice
     */
    public void StartConnection(boolean AutoConnect){

        if ((BT_Adapter != null) && (ClientDevice != null)){
            AutoReconnect = AutoConnect;
            Connection C = new Connection();
            C.start();
        }
    }

    /**
     * STEP 3 - Test if the connection was done
     * @return True: Connection with ClientDevice OK.
     *         False: No connection with ClientDevice.
     */
    public boolean isConnected(){
        if(SSocket!=null)
           return SSocket.Connected;
        return false;
    }

    /**
     * Flag indicating if new message arrived.
     * @return  True: New Message available
     *          False: No Message available
     */
    public boolean hasMessage(){
        return SSocket.hasMessage();
    }

    /**
     * Get the bluetooth message received.
     * @return Byte Array with the message received.
     */
    public byte[] GetByteMessage(){
        return SSocket.GetByteMessage();
    }

    /**
     * Get the bluetooth message received.
     * @return String with the message received.
     */
    public String GetStringMessage(){
        return SSocket.GetStringMessage();
    }

    /**
     * Send Message to the ClientDevice
     * @param Message Byte array (up to 1024 bytes) with data to the Client Device.
     */
    public boolean SendMessage(byte[] Message){

        if((SSocket!=null)&&(Message!=null)){
            return SSocket.SendMessage(Message);
        }
        return false;
    }

    /**
     * Send Message to the ClientDevice
     * @param Message String (up to 1024 characters) with data to the Client Device.
     */
    public boolean SendMessage(String Message){

        if((SSocket!=null)&&(Message!=null)){
            return SSocket.SendMessage(Message);
        }
        return false;
    }

    /**
     * Close the Connection
     */
    public void Close(){
        if(SSocket!=null) {
            AutoReconnect = false;
            SSocket.CloseConnection();
        }
    }

    /**
     * Auto Reconnect Thread to keep the bluetooth link always alive.
     */
    private class Connection extends Thread {

        public void run() {

            do {
                // Start Connection
                SSocket = new ServerSocket(BT_Adapter, ClientDevice);
                SSocket.start();

                // Wait Until Conneted
                do {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (!SSocket.Connected);

                // Wait until Disconnect
                do {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (SSocket.Connected);
            } while (AutoReconnect);
        }
    }

}
