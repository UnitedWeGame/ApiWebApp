package com.UnitedWeGame;

import com.UnitedWeGame.APIServer.APIInterface;
import com.UnitedWeGame.APIServer.IgdbAPIService;
import com.UnitedWeGame.APIServer.XboxAPIRequest;
import com.UnitedWeGame.InformationObjects.Game;
import com.UnitedWeGame.UserClasses.Friend;
import com.UnitedWeGame.UserClasses.Person;

import java.sql.*;

import com.UnitedWeGame.Utils.DatabaseConnectionUtil;
import org.postgresql.Driver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.Properties;

public class Main {

    public static void main(String[] args){

        APIInterface xboxInterface = new XboxAPIRequest();

        Person person = new Person();
        person.setXboxIdentifier("2533274792755153");
        person.setUserId(6666);

        int count = 0;


        while(true) {

            try {

                List<Person> xboxUsersToPoll = new DatabaseConnectionUtil().getXboxGamertagsToPoll();

                for (Person xboxUser : xboxUsersToPoll) {
                    xboxUser.setXboxIdentifier(xboxInterface.getIdentifier(xboxUser));

                }


                List<Thread> threads = new ArrayList<>();

                if (count % 1000 == 0) {
                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {

                            for (Person xboxUser : xboxUsersToPoll) {

                                xboxInterface.getGameLibrary(xboxUser);
                            }

                        }
                    });
                    threads.add(thread);
                    thread.start();
                }


                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        for (Person xboxUser : xboxUsersToPoll) {

                            List<Person> newUsers = new DatabaseConnectionUtil().returnNewUsers();


                            if(newUsers.size() != 0) {

                                Thread thread = new Thread(new Runnable() {

                                    @Override
                                    public void run() {

                                        for (Person newUser : newUsers) {

                                            newUser.setXboxIdentifier(xboxInterface.getIdentifier(newUser));

                                            xboxInterface.getGameLibrary(newUser);
                                            new DatabaseConnectionUtil().deleteNewUser(newUser);
                                        }

                                    }
                                });

                                thread.start();


                                Thread userThread = new Thread(new Runnable() {

                                    @Override
                                    public void run() {

                                        for (Person newXboxUser : newUsers) {


                                            newXboxUser.setXboxIdentifier(xboxInterface.getIdentifier(newXboxUser));

                                            xboxInterface.getFriendsStatus(newXboxUser);

                                            new DatabaseConnectionUtil().deleteNewUser(newXboxUser);


                                        }
                                    }
                                });

                                userThread.start();


                            }
                                xboxInterface.getFriendsStatus(xboxUser);

                                }

                            }
                    });

                threads.add(thread);

                thread.start();

                thread.join();

            }
            catch (Exception ex)
            {
                System.err.println("Err: " + ex.getMessage());
                ex.printStackTrace();
            }

            count++;
        }




    }
}
