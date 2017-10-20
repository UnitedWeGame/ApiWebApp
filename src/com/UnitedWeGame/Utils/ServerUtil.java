package com.UnitedWeGame.Utils;

import com.UnitedWeGame.APIServer.APIInterface;
import com.UnitedWeGame.APIServer.SteamAPIRequest;
import com.UnitedWeGame.APIServer.XboxAPIRequest;
import com.UnitedWeGame.UserClasses.Person;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: cweeter
 * Date: 9/20/17
 */
public class ServerUtil {

    private static int xboxCount = 0;
    private static int steamCount = 0;


    public static void incrementXboxCount() {

        xboxCount++;
    }

    public static void incrementSteamCount() {

        steamCount++;
    }

        public static void runXboxServer()
    {
        try {

            APIInterface xboxInterface = new XboxAPIRequest();

            List<Person> xboxUsersToPoll = new DatabaseConnectionUtil().getXboxGamertagsToPoll();

            for (Person xboxUser : xboxUsersToPoll) {
                xboxUser.setXboxIdentifier(xboxInterface.getIdentifier(xboxUser));

            }


            List<Thread> threads = new ArrayList<>();

            if (xboxCount % 10000 == 0) {
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

//                        List<Person> newUsers = new DatabaseConnectionUtil().returnNewUsers();
//
//
//                        if(newUsers.size() != 0) {
//
//                            Thread thread = new Thread(new Runnable() {
//
//                                @Override
//                                public void run() {
//
//                                    for (Person newUser : newUsers) {
//
//                                        newUser.setXboxIdentifier(xboxInterface.getIdentifier(newUser));
//
//                                        xboxInterface.getGameLibrary(newUser);
//                                        new DatabaseConnectionUtil().deleteNewUser(newUser);
//                                    }
//
//                                }
//                            });
//
//                            thread.start();
//
//
//                            Thread userThread = new Thread(new Runnable() {
//
//                                @Override
//                                public void run() {
//
//                                    for (Person newXboxUser : newUsers) {
//
//
//                                        newXboxUser.setXboxIdentifier(xboxInterface.getIdentifier(newXboxUser));
//
//                                        xboxInterface.getFriendsStatus(newXboxUser);
//
//                                        new DatabaseConnectionUtil().deleteNewUser(newXboxUser);
//
//
//                                    }
//                                }
//                            });
//
//                            userThread.start();
//
//
//                        }
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
    }

    public static void runSteamServer()
    {
        try {

            APIInterface steamInterface = new SteamAPIRequest();

            List<Person> steamUsersToPoll = new DatabaseConnectionUtil().getSteamUsersToPoll();

            List<Thread> threads = new ArrayList<>();

            if (steamCount % 10000 == 0) {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        for (Person steamUser : steamUsersToPoll) {

                            steamInterface.getGameLibrary(steamUser);
                        }

                    }
                });
                threads.add(thread);
                thread.start();
            }


            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {

                    for (Person steamUser : steamUsersToPoll) {

//                        List<Person> newUsers = new DatabaseConnectionUtil().returnNewUsers();
//
//
//                        if(newUsers.size() != 0) {
//
//                            Thread thread = new Thread(new Runnable() {
//
//                                @Override
//                                public void run() {
//
//                                    for (Person newUser : newUsers) {
//
//                                        newUser.setSteamIdentifier(steamInterface.getIdentifier(newUser));
//
//                                        steamInterface.getGameLibrary(newUser);
//                                        new DatabaseConnectionUtil().deleteNewUser(newUser);
//                                    }
//
//                                }
//                            });
//
//                            thread.start();
//
//
//                            Thread userThread = new Thread(new Runnable() {
//
//                                @Override
//                                public void run() {
//
//                                    for (Person newSteamUser : newUsers) {
//
//
//                                        newSteamUser.setSteamIdentifier(steamInterface.getIdentifier(newSteamUser));
//
//                                        steamInterface.getFriendsStatus(newSteamUser);
//
//                                        new DatabaseConnectionUtil().deleteNewUser(newSteamUser);
//
//
//                                    }
//                                }
//                            });
//
//                            userThread.start();
//
//
//                        }
                        steamInterface.getFriendsStatus(steamUser);

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
    }

    public static void runNewUsersServer()
    {

        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {

                SteamAPIRequest steamInterface = new SteamAPIRequest();
                XboxAPIRequest xboxAPIRequest = new XboxAPIRequest();

                while(true)
                {
                    try {

                        List<Person> newUsers = new DatabaseConnectionUtil().returnNewUsers();

                        System.out.println("NewUsers: " + newUsers.size());

                        if(newUsers.size() != 0) {

                            Thread thread = new Thread(new Runnable() {

                                @Override
                                public void run() {

                                    for (Person newUser : newUsers) {

                                        if(!StringUtils.isEmpty(newUser.getXboxGamertag())) {
                                            newUser.setXboxIdentifier(xboxAPIRequest.getIdentifier(newUser));

                                            xboxAPIRequest.getGameLibrary(newUser);

                                            new DatabaseConnectionUtil().deleteNewUser(newUser);
                                        }
                                    }

                                }
                            });

                            Thread steamThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for (Person newUser : newUsers) {

                                        if(!StringUtils.isEmpty(newUser.getSteamIdentifier())) {
                                            steamInterface.getGameLibrary(newUser);

                                            new DatabaseConnectionUtil().deleteNewUser(newUser);
                                        }
                                    }
                                }
                            });

                            thread.start();
                            steamThread.start();

                            Thread userThread = new Thread(new Runnable() {

                                @Override
                                public void run() {

                                    for (Person newUser : newUsers) {

                                        if(!StringUtils.isEmpty(newUser.getSteamIdentifier())) {
                                            steamInterface.getFriendsStatus(newUser);

                                            new DatabaseConnectionUtil().deleteNewUser(newUser);
                                        }

                                    }
                                }
                            });

                            Thread xboxThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for (Person newUser : newUsers) {

                                        if (!StringUtils.isEmpty(newUser.getXboxGamertag())) {
                                            newUser.setXboxIdentifier(xboxAPIRequest.getIdentifier(newUser));

                                            xboxAPIRequest.getFriendsStatus(newUser);

                                            new DatabaseConnectionUtil().deleteNewUser(newUser);
                                        }

                                    }
                                }
                            });

                            xboxThread.start();

                            userThread.start();

                        }


                        Thread.sleep(5000);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }

            }
        });
        serverThread.start();


    }
}
