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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.Properties;

public class Main {

    public static void main(String[] args){
        
        Person person = new Person();
        person.setXboxIdentifier("2533274792755153");
        person.setUserId(6666);
        person.setSteamIdentifier("76561198015600919");

        //XboxAPIRequest xboxAPIRequest = new XboxAPIRequest();

        //xboxAPIRequest.getGameLibrary(person);

//        SteamAPIRequest steamAPIRequest = new SteamAPIRequest();
//        steamAPIRequest.getGameLibrary(person);
//        steamAPIRequest.getFriendsStatus(person);


        Thread xboxThread = new Thread(new Runnable() {

            @Override
            public void run() {

                while(true) {

                    ServerUtil.runXboxServer();
                    ServerUtil.incrementXboxCount();

                }

            }
        });

        Thread steamThread = new Thread(new Runnable() {

            @Override
            public void run() {

                while(true) {

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
//        SteamAPIRequest steamAPIRequest = new SteamAPIRequest();


//        while(true)
//        {
//            steamAPIRequest.getGameLibrary(person);
//            steamAPIRequest.getFriendsStatus(person);
//        }

    }
}
