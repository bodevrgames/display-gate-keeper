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

public class Client {

    //Bluetooth Variables
    private String ServerID;
    private BluetoothDevice ServerDevice;
    private BluetoothAdapter BT_Adapter;
    private boolean AutoReconnect;

    // Bluetooth Server Sockets
    private ClientSocket CSocket;

    // Debug
    private final String dTAG = "<BTMsg> ";

    // Class Constructor
    public Client(String ServerDeviceID){

        // saving ClientDevice ID
        ServerID = ServerDeviceID;

        // Loading Bluetooth Adapter
        ServerDevice = null;
        BT_Adapter = BluetoothAdapter.getDefaultAdapter();

        // Turn on Bluetooth
        if (!BT_Adapter.isEnabled()) BT_Adapter.enable();
        if (BT_Adapter.isDiscovering()) BT_Adapter.cancelDiscovery();
        AutoReconnect = false;

        Log.d(dTAG,"[OK] Bluetooth Messenger Started -> CLIENT !");

    }

    /**
     * STEP 1 - Verify if Server Device was Paired with the Client Device previously.
     * @return True - Has Server Device paired
     *         False - No Server Device Paired
     */
    public boolean hasServerDevice(){

        Set<BluetoothDevice> pairedDevices = BT_Adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if(device.getName().equals(ServerID)) {
                    Log.d(dTAG,"[OK] Server Device Paired = " + device.getName());
                    ServerDevice = device;
                    return true;
                }
            }
        }

        Log.d(dTAG,"[FAIL] Server Device not paired !!!");
        return false;
    }

    /**
     * STEP 2 - Start the Connection with the ClientDevice
     * @AutoConnec True: Reconnect if Connection was lost.
     *
     * @return True: Connection was established.
     *         False: Can't reach the ClientDevice
     */
    public void StartConnection(boolean AutoConnect){

        if( ServerDevice!=null) {
            AutoReconnect = AutoConnect;
            Connection C = new Connection();
            C.start();
        }
    }

    /**
     * STEP 3 - Test if the connection was done
     * @return True: Connection with Server Device  OK.
     *         False: No connection with Server Device.
     */
    public boolean isConnected(){
        if(CSocket!=null)
          return CSocket.Connected;
        return false;
    }

    /**
     * Flag indicating if new message arrived.
     * @return  True: New Message available
     *          False: No Message available
     */
    public boolean hasMessage(){
        if(CSocket!=null)
           return CSocket.hasMessage();
        return false;
    }

    /**
     * Get the bluetooth message received.
     * @return Byte Array with the message received.
     */
    public byte[] GetByteMessage(){
      return CSocket.GetByteMessage();
    }

    /**
     * Get the bluetooth message received.
     * @return String with the message received.
     */
    public String GetStringMessage(){
        return CSocket.GetStringMessage();

    }

    /**
     * Send Message to the ClientDevice
     * @param Message Byte array (up to 1024 bytes) with data to the Server Device.
     */
    public boolean SendMessage(byte[] Message){
        if((CSocket!=null)&&(Message!=null)) {
            return CSocket.SendMessage(Message);
        }
        return false;
    }

    /**
     * Send Message to the ClientDevice
     * @param Message String (up to 1024 characters) with data to the Server Device.
     */
    public boolean SendMessage(String Message){
        if((CSocket!=null)&&(Message!=null)) {
            return CSocket.SendMessage(Message);
        }
        return false;
    }

    /**
     * Close the Connection
     */
    public void Close(){

        if(CSocket!=null) {
            AutoReconnect = false;
            CSocket.CloseConnection();
        }
    }

    /**
     * Auto Reconnect Thread to keep the bluetooth link always alive.
     */
    private class Connection extends Thread {

        public void run() {

            while (AutoReconnect) {

                do {
                    // Start Connection
                    CSocket = new ClientSocket(ServerDevice);
                    CSocket.start();

                    // Wait Until Conneted
                    do {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (!CSocket.Connected);

                    // Wait until Disconnect
                    do {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (CSocket.Connected);
                } while (AutoReconnect);
            }

        }
    }

}
