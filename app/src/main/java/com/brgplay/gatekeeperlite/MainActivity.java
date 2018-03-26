package com.brgplay.gatekeeperlite;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.os.Environment;
import android.provider.SyncStateContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.brgplay.bluetoothmessenger.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Objects BTE
    private Button    btStartBTE;
    private TextView  TvPlayedGamesBTE;
    private TextView  TvPlayedGamesTitleBTE;
    private ImageView LogoBTE;

    // UI Objects 3042
    private Button    btStart3042;
    private TextView  TvPlayedGames3042;
    private TextView  TvPlayedGamesTitle3042;
    private ImageView Logo3042;

    //UI Objects Connections
    private TextView  ConnectionStatus;
    private TextView  PlayingStatus;
    private EditText  inputUsername;
   

    //FILE Objects
    private String          FILERANK  = "rank.txt";
    private String          FILEGAMEPLAYED = "GamePlayed.txt";
    private File            rank;
    private File            gamePlayed;


    // Game Info
    private int                 PlayedGamesBTE = 0;
    private int                 PlayedGames3042 = 0;
    private boolean             isPlayingBTE;
    private boolean             isPlaying3042;
    private String[]            GameList;
    private String[]            Ranking;
    private String              DeviceIMEI;

    // Messages Handle
    MessageExchange             messageExchange;

    // Games Constn
    private final String StartGame      = "STG";
    private final String EndGame        = "ENG";
    private final String SendGameList   = "SGL";
    private final String SendIMEI       = "SIM";
    private final String UpdateRank     = "RAN";


    // Bluetooth Objects
    private Server BTServer;

    //Username
    private String username;

    // Debug
    private final String TAG = "<GATE KEEPER> ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting Up UI BTE
        btStartBTE = (Button) findViewById(R.id.btStartBTE);
        TvPlayedGamesBTE = (TextView) findViewById(R.id.PlayedGamesBTE);
        TvPlayedGamesTitleBTE = (TextView) findViewById(R.id.GamesPlayedTitleBTE);
        LogoBTE = (ImageView) findViewById(R.id.LogoBTE);

        // Setting Up UI 3042
        btStart3042 = (Button) findViewById(R.id.btStart3042);
        TvPlayedGames3042 = (TextView) findViewById(R.id.PlayedGames3042);
        TvPlayedGamesTitle3042 = (TextView) findViewById(R.id.GamesPlayedTitle3042);
        Logo3042 = (ImageView) findViewById(R.id.Logo3042);

        // Setting Up Connection UI
        ConnectionStatus = (TextView) findViewById(R.id.ConectionStatus);
        PlayingStatus = (TextView) findViewById(R.id.PlayingStatus);
        inputUsername = (EditText) findViewById(R.id.username);

        //Configuring UIs
        ConnectionStatus.setVisibility(View.VISIBLE);

        LogoBTE.setVisibility(View.INVISIBLE);
        TvPlayedGamesBTE.setVisibility(View.INVISIBLE);
        TvPlayedGamesTitleBTE.setVisibility(View.INVISIBLE);
        btStartBTE.setVisibility(View.INVISIBLE);

        Logo3042.setVisibility(View.INVISIBLE);
        TvPlayedGames3042.setVisibility(View.INVISIBLE);
        TvPlayedGamesTitle3042.setVisibility(View.INVISIBLE);
        btStart3042.setVisibility(View.INVISIBLE);

        PlayingStatus.setVisibility(View.INVISIBLE);
        inputUsername.setVisibility(View.INVISIBLE);
        username = "";

        //FILES
        createFiles();

        Log.d(TAG,"[OK] App Started !");

        // Updating UI
        TvPlayedGamesBTE.setText(String.valueOf(PlayedGamesBTE));
        TvPlayedGames3042.setText(String.valueOf(PlayedGames3042));

        // Starting Bluetooth Server
        BTServer = new Server("GEAR VR");
        if(BTServer.hasClientDevice()){
            BTServer.StartConnection(true);
            messageExchange  = new MessageExchange();
            messageExchange.start();
            Log.d(TAG,"[OK] Bluetooth Server Started !");
        }else{
            Log.d(TAG,"[ERROR] No Remote Device Paired !!");
        }

    }


    protected void createFiles() {
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                String storagePath = Environment.getExternalStorageDirectory().toString();
                File root = new File(storagePath + "/BRGPLAY"); //Creating the directory
                if (!root.exists()) {
                    Log.i("Test", "This path is doesn't exist: " + root.getAbsolutePath());
                    if (root.mkdirs()) {
                        Log.i("Test", "The path was created: " + root.getAbsolutePath());
                    } else {
                        Log.i("Test", "The path wasn't created: " + root.getAbsolutePath());
                    }

                } else {
                    Log.i("Test", "This path is already exist: " + root.getAbsolutePath());
                }
                rank = new File(root, FILERANK);
                gamePlayed = new File(root, FILEGAMEPLAYED);
                if (!rank.createNewFile()) {
                    Log.i("Test", "This file is already exist: " + rank.getAbsolutePath());
                }else{
                    FileWriter writer = new FileWriter(rank);
                    writer.append("Enzo/500");
                    writer.append("\n");
                    writer.append("João/200");
                    writer.append("\n");
                    writer.append("Raquel/125");
                    writer.append("\n");
                    writer.append("Pedro/100");
                    writer.append("\n");
                    writer.append("Mariana/80");
                    writer.append("\n");
                    writer.append("Harry/50");
                    writer.append("\n");
                    writer.append("Jota/20");
                    writer.append("\n");
                    writer.append("Paula/10");
                    writer.append("\n");
                    writer.flush();
                    writer.close();
                }
                // If the file isn't new, read the file
                if (!gamePlayed.createNewFile()) {
                    Log.i("Test", "This file is already exist: " + gamePlayed.getAbsolutePath());
                    BufferedReader br = new BufferedReader(new FileReader(gamePlayed));

                    try {
                        String line;
                        int lineCount = 0;
                        while ((line = br.readLine()) != null) {
                            // First line contains 3042 info
                            if(lineCount == 0)
                                PlayedGames3042 = Integer.parseInt(line);
                            //Second line contains BTE info
                            if(lineCount == 1)
                                PlayedGamesBTE = Integer.parseInt(line);
                            lineCount ++;
                        }
                    } catch (IOException ioEx) {
                        ioEx.printStackTrace();
                    } finally {
                        br.close();
                    }
                }else{
                    FileWriter writer = new FileWriter(gamePlayed);
                    writer.write("0\n0");
                    writer.flush();
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        updateServerStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BTServer.Close();
    }

    public void updateServerStatus(){

        if(BTServer.isConnected()){
            ConnectionStatus.setVisibility(View.INVISIBLE);

            LogoBTE.setVisibility(View.VISIBLE);
            TvPlayedGamesBTE.setVisibility(View.VISIBLE);
            TvPlayedGamesTitleBTE.setVisibility(View.VISIBLE);
            btStartBTE.setVisibility(View.VISIBLE);

            Logo3042.setVisibility(View.VISIBLE);
            TvPlayedGames3042.setVisibility(View.VISIBLE);
            TvPlayedGamesTitle3042.setVisibility(View.VISIBLE);
            btStart3042.setVisibility(View.VISIBLE);

            inputUsername.setVisibility(View.VISIBLE);
        }else{
            ConnectionStatus.setVisibility(View.VISIBLE);

            LogoBTE.setVisibility(View.INVISIBLE);
            TvPlayedGamesBTE.setVisibility(View.INVISIBLE);
            TvPlayedGamesTitleBTE.setVisibility(View.INVISIBLE);
            btStartBTE.setVisibility(View.INVISIBLE);

            Logo3042.setVisibility(View.INVISIBLE);
            TvPlayedGames3042.setVisibility(View.INVISIBLE);
            TvPlayedGamesTitle3042.setVisibility(View.INVISIBLE);
            btStart3042.setVisibility(View.INVISIBLE);

            inputUsername.setVisibility(View.INVISIBLE);
        }
    }

    public void setUsername(View view){
        username = inputUsername.getText().toString();
        if(username.isEmpty()){
            return;
        }
        btStartBTE.setEnabled(true);
        btStart3042.setEnabled(true);
    }


    /**
     * Button Pressed to BTE Start Game */
    public void startGameBTE(View view){
        username = inputUsername.getText().toString();
        if(username.isEmpty()){
            btStartBTE.setEnabled(false);
            return;
        }

        if(!BTServer.isConnected()) return;
        String[] GameInfo = {"com.brgplay.game.backtoearth",username};
        messageExchange.sendMessage(StartGame,GameInfo);
        isPlayingBTE = true;
        isPlaying3042 = false;
    }

    /**
     * Button Pressed to BTE Start Game */
    public void startGame3042(View view){
        username = inputUsername.getText().toString();
        if(username.isEmpty()){
            btStartBTE.setEnabled(false);
            return;
        }

        if(!BTServer.isConnected()) return;
        String[] GameInfo = {"com.brgplay.game.earthinvasion",username};
        messageExchange.sendMessage(StartGame,GameInfo);
        isPlayingBTE = false;
        isPlaying3042 = true;
    }

    private void sendRanking(){
        if(!BTServer.isConnected()) return;
        if(isPlayingBTE) return;
        List<String> Rank = readRankingFile();
        if(Rank == null){
            return;
        }
        String[] ranking = new String[Rank.size()];
        for(int i = 0; i < Rank.size(); i++){
            ranking[i] = Rank.get(i);
            Log.d("RANK",i + " " + ranking[i]);
        }
        messageExchange.sendMessage(UpdateRank, ranking);

    }

    private void writeRankingFile(String[] newRanking){
        try {
            FileWriter writer = new FileWriter(rank);
            for(int i =0; i < newRanking.length; i++) {
                writer.append(newRanking[i]);
                writer.append("\n");
                writer.flush();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> readRankingFile(){
        if(rank == null){
            return null;
        }
        if(!rank.exists()){
            try {
                rank.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        List<String> rankList = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(rank));
            String line;
            while ((line = br.readLine()) != null) {
                rankList.add(line);
                Log.d("[RANK]","Line:" + line);
            }
            br.close();
        }
        catch (IOException e) {
            Log.d(TAG,"[ERROR] While reading Ranking File");
            e.printStackTrace();
        }
        return rankList;
    }

    /**
     * UI Update -> Avoid Game to be started when a instance is already playing.
     */
    public void blockStartGame(){
        btStartBTE.setEnabled(false);
        btStart3042.setEnabled(false);
        inputUsername.setEnabled(false);
        if(BTServer.isConnected()) {
            PlayingStatus.setVisibility(View.VISIBLE);
        }else{
            PlayingStatus.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * Enable a Game start when the las instance was finished.
     */
    public void enableStartGame(){
        inputUsername.setEnabled(true);
        PlayingStatus.setVisibility(View.INVISIBLE);
    }

    /** Increment Games Played at UI */
    public void updateGamesPlayed() {
        try {
            if (isPlaying3042)
                PlayedGames3042++;
            if(isPlayingBTE)
                PlayedGamesBTE++;
            FileWriter writer = new FileWriter(gamePlayed);
            writer.write(Integer.toString(PlayedGames3042)+"\n"+ Integer.toString(PlayedGamesBTE));
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Updating UI
        TvPlayedGames3042.setText(String.valueOf(PlayedGames3042));
        TvPlayedGamesBTE.setText(String.valueOf(PlayedGamesBTE));
    }

    /** Private Thread Class to Receive Message  */
    class MessageExchange extends Thread {


        // Data Variables
        JSONObject JSonObject;
        String     JSonType;
        JSONArray  JsonData;

        public void run() {

            // Connection LOOP
            do {

                if(!BTServer.isConnected()){
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateServerStatus();
                                blockStartGame();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    do{}while(!BTServer.isConnected());
                }
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateServerStatus();
                                enableStartGame();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                while(BTServer.isConnected()) {

                    // Waiting JSON with Game List
                    if (BTServer.hasMessage()) {

                        // Reading JSON
                        try {
                            JSonObject = new JSONObject(BTServer.GetStringMessage());
                            JSonType = JSonObject.getString("Type");
                            JsonData = JSonObject.getJSONArray("Data");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }

                        // Message Type Received
                        Log.d(TAG, "[JSON Received] Type = " + JSonType);


                        // Device IMEI
                        if (JSonType.equals(SendIMEI)) {
                            try {
                                DeviceIMEI = JsonData.getString(0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "[IMEI] = " + DeviceIMEI);
                        }

                        // Game List
                        if (JSonType.equals(SendGameList)) {
                            GameList = new String[JsonData.length()];
                            for (int i = 0; i < GameList.length; i++) {
                                try {
                                    GameList[i] = JsonData.getString(i);
                                    Log.d(TAG, "[Game " + (i + 1) + "]  = " + GameList[i]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }

                            /** Sending Ranking*/
                            sendRanking();
                        }

                        // Game Started
                        if (JSonType.equals(StartGame)) {
                            // Updating UI
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        blockStartGame();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Game End
                        if (JSonType.equals(EndGame)) {
                            // Updating UI
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        enableStartGame();
                                        updateGamesPlayed();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Ranking
                        if (JSonType.equals(UpdateRank)) {
                            Ranking = new String[JsonData.length()];
                            for (int i = 0; i < Ranking.length; i++) {
                                try {
                                    Ranking[i] = JsonData.getString(i);
                                    Log.d(TAG, "[POS " + (i + 1) + "]  = " + Ranking[i]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }
                            if(Ranking.length > 0){
                                writeRankingFile(Ranking);
                            }
                        }
                    }
                }

            } while (true);

        }

        /******* MESSAGE ROUTINES **********/
        public boolean sendMessage(String Type, String[] Data){

            // JSON Objects
            JSONObject Package = new JSONObject();

            Log.d(TAG, "[Message Sent =  " + Data[0]);

            try {
                JSONArray  jData = new JSONArray(Data);
                Package.put("Type",Type);        // Message Type
                Package.put("Data",jData);       // Message Data
                BTServer.SendMessage(Package.toString());
                Log.d(TAG, "[Package Sent =  " + Package.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

}
