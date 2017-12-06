package com.UnitedWeGame;

import com.UnitedWeGame.APIServer.APIInterface;
import com.UnitedWeGame.APIServer.IgdbAPIService;
import com.UnitedWeGame.APIServer.SteamAPIRequest;
import com.UnitedWeGame.APIServer.XboxAPIRequest;
import com.UnitedWeGame.InformationObjects.Game;
import com.UnitedWeGame.UserClasses.Friend;
import com.UnitedWeGame.UserClasses.Person;

import java.sql.*;

import com.UnitedWeGame.Utils.DatabaseConnectionUtil;
import com.UnitedWeGame.Utils.ServerUtil;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {

        Thread xboxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ServerUtil.runXboxServer();
                    ServerUtil.incrementXboxCount();
                }
            }
        });
        Thread steamThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ServerUtil.runSteamServer();
                    ServerUtil.incrementSteamCount();
                }
            }
        });
        Thread newUsers = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerUtil.runNewUsersServer();
            }
        });
        newUsers.start();
        xboxThread.start();
        steamThread.start();
    }
}
