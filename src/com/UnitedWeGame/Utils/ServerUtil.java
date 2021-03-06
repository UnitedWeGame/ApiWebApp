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

    public static List<String> currentLibraryThreads = new ArrayList<>();
    public static List<String> currentOnlineUserThreads = new ArrayList<>();

    // Xbox server thread code
    public static void runXboxServer() {
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
                            if (!currentLibraryThreads.contains(xboxUser.getUserId()+xboxUser.getXboxGamertag())) {
                                currentLibraryThreads.add(xboxUser.getUserId()+xboxUser.getXboxGamertag());
                                xboxInterface.getGameLibrary(xboxUser);
                                currentLibraryThreads.remove(xboxUser.getUserId()+xboxUser.getXboxGamertag());
                            }
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
                        // Old server code, keep in case of issues
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
                        if (!currentOnlineUserThreads.contains(xboxUser.getUserId()+xboxUser.getXboxGamertag())) {
                            currentOnlineUserThreads.add(xboxUser.getUserId()+xboxUser.getXboxGamertag());
                            xboxInterface.getFriendsStatus(xboxUser);
                            currentOnlineUserThreads.remove(xboxUser.getUserId()+xboxUser.getXboxGamertag());
                        }
                    }
                }
            });
            threads.add(thread);
            thread.start();
            thread.join();
        } catch (Exception ex) {
            System.err.println("Err: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Steam server code
    public static void runSteamServer() {
        try {
            APIInterface steamInterface = new SteamAPIRequest();
            List<Person> steamUsersToPoll = new DatabaseConnectionUtil().getSteamUsersToPoll();
            List<Thread> threads = new ArrayList<>();
            if (steamCount % 10000 == 0) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (Person steamUser : steamUsersToPoll) {
                            if (!currentLibraryThreads.contains(steamUser.getUserId()+steamUser.getSteamIdentifier())) {
                                currentLibraryThreads.add(steamUser.getUserId()+steamUser.getSteamIdentifier());
                                steamInterface.getGameLibrary(steamUser);
                                currentLibraryThreads.remove(steamUser.getUserId()+steamUser.getSteamIdentifier());
                            }
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
                        // Old server code, keep in case something goes wrong
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
                        if (!currentOnlineUserThreads.contains(steamUser.getUserId()+steamUser.getSteamIdentifier())) {
                            currentOnlineUserThreads.add(steamUser.getUserId()+steamUser.getSteamIdentifier());
                            steamInterface.getFriendsStatus(steamUser);
                            currentOnlineUserThreads.remove(steamUser.getUserId()+steamUser.getSteamIdentifier());
                        }
                    }
                }
            });
            threads.add(thread);
            thread.start();
            thread.join();
        } catch (Exception ex) {
            System.err.println("Err: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Code for NewUsers server
    public static void runNewUsersServer() {
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SteamAPIRequest steamInterface = new SteamAPIRequest();
                XboxAPIRequest xboxAPIRequest = new XboxAPIRequest();
                while (true) {
                    try {
                        Thread.sleep(2000);
                        List<Person> newUsers = new DatabaseConnectionUtil().returnNewUsers();
                        System.out.println("NewUsers: " + newUsers.size());
                        if (newUsers.size() != 0) {
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final List<Person> users = new ArrayList<>(newUsers);
                                    for (Person newUser : users) {
                                        if (!StringUtils.isEmpty(newUser.getXboxGamertag())) {
                                            newUser.setXboxIdentifier(xboxAPIRequest.getIdentifier(newUser));

                                            if (!currentLibraryThreads.contains(newUser.getUserId()+newUser.getXboxGamertag())) {
                                                currentLibraryThreads.add(newUser.getUserId()+newUser.getXboxGamertag());
                                                xboxAPIRequest.getGameLibrary(newUser);
                                                currentLibraryThreads.remove(newUser.getUserId()+newUser.getXboxGamertag());
                                            }
                                        }
                                    }
                                }
                            });
                            Thread steamThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final List<Person> users = new ArrayList<>(newUsers);

                                    for (Person newUser : users) {
                                        if (!StringUtils.isEmpty(newUser.getSteamIdentifier())) {
                                            newUser.setSteamIdentifier(newUser.getSteamIdentifier().trim());
                                            if (!currentLibraryThreads.contains(newUser.getUserId()+newUser.getSteamIdentifier())) {
                                                currentLibraryThreads.add(newUser.getUserId()+newUser.getSteamIdentifier());
                                                steamInterface.getGameLibrary(newUser);
                                                currentLibraryThreads.remove(newUser.getUserId()+newUser.getSteamIdentifier());
                                            }
                                        }
                                    }
                                }
                            });
                            Thread userThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final List<Person> users = new ArrayList<>(newUsers);

                                    for (Person newUser : users) {
                                        if (!StringUtils.isEmpty(newUser.getSteamIdentifier())) {
                                            newUser.setSteamIdentifier(newUser.getSteamIdentifier().trim());
                                            if (!currentOnlineUserThreads.contains(newUser.getUserId()+newUser.getSteamIdentifier())) {
                                                currentOnlineUserThreads.add(newUser.getUserId()+newUser.getSteamIdentifier());
                                                steamInterface.getFriendsStatus(newUser);
                                                currentOnlineUserThreads.remove(newUser.getUserId()+newUser.getSteamIdentifier());
                                            }
                                        }
                                    }
                                }
                            });
                            Thread xboxThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final List<Person> users = new ArrayList<>(newUsers);

                                    for (Person newUser : users) {
                                        if (!StringUtils.isEmpty(newUser.getXboxGamertag())) {
                                            newUser.setXboxIdentifier(xboxAPIRequest.getIdentifier(newUser));
                                            if (!currentOnlineUserThreads.contains(newUser.getUserId()+newUser.getXboxGamertag())) {
                                                currentOnlineUserThreads.add(newUser.getUserId()+newUser.getXboxGamertag());
                                                xboxAPIRequest.getFriendsStatus(newUser);
                                                currentOnlineUserThreads.remove(newUser.getUserId()+newUser.getXboxGamertag());
                                            }
                                        }
                                    }
                                }
                            });
                            for (Person newUser : newUsers)
                                new DatabaseConnectionUtil().deleteNewUser(newUser);

                            xboxThread.start();
                            userThread.start();
                            thread.start();
                            steamThread.start();
                        }
                        Thread.sleep(5000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        serverThread.start();
    }
}
