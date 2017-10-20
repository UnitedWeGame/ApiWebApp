package com.UnitedWeGame.Utils;

import com.UnitedWeGame.UserClasses.Person;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by cweeter on 4/20/17.
 */
public class DatabaseConnectionUtil {

    public static int openConnections = 0;
    public static int closedConnections = 0;

    // 1 hour time limit currently //TODO change back to 20 minutes for actual demo
    public static final long TIME_LIMIT = 1200000;
    public static final long TIME_ZONE_DIFFERENCE = 14400000;

    public static void seedDatabase()
    {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            List<String> sql = Files.readAllLines(Paths.get("/Users/cweeter/git/ApiWebApp/src/com/UnitedWeGame/resources/output.sql"));

            for(String line : sql)
            {
                try{
                    PreparedStatement statement = c.prepareStatement(line);
                    statement.execute();
                }
                catch (Exception sqlstuff)
                {
                    sqlstuff.printStackTrace();
                    System.err.println("***Problem with line: " + line + "****");

                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        } finally {
        try {
            c.close();
            closedConnections++;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    }

    public long getGameID(String name, String platform) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        try {
            ResultSet rs = null;
            PreparedStatement select = null;

            select = c.prepareStatement("select * from game where title = ? and platform_title = ?");
            select.setString(1, name);
            select.setString(2, platform);

            rs = select.executeQuery();

            if(!rs.isBeforeFirst())
                return -1;


            rs.next();

            int id = rs.getInt("id");
            return id;

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }

        return -1;
    }



    public void insertGameIntoDb(String title, String platform_name, String image_url) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ResultSet rs = null;
            PreparedStatement insert = null;

            insert = c.prepareStatement("INSERT INTO game (image_url, title, platform_title) VALUES(?,?,?)");
            insert.setString(1, image_url);
            insert.setString(2, title);
            insert.setString(3, platform_name);

            insert.executeUpdate();

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();


            }
        }
    }


    public void addGameToUserLibrary(long userId, long gameId) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ResultSet rs = null;
            PreparedStatement insert = null;

            insert = c.prepareStatement("INSERT INTO user_library (user_id, game_id) VALUES(?,?)");
            insert.setLong(1, userId);
            insert.setLong(2, gameId);

            insert.executeUpdate();

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();


            }
        }
    }

    public List<Person> getXboxGamertagsToPoll() {
        Connection c = null;
        List<Person> gamertags = new ArrayList<>();
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        try {

            ResultSet rs = null;
            PreparedStatement select = null;

            select = c.prepareStatement("select * from users");

            rs = select.executeQuery();

            if(!rs.isBeforeFirst())
                return new ArrayList<>();


            while(rs.next())
            {
                Timestamp lastActiveTimestamp = rs.getTimestamp("last_activity");
                if(System.currentTimeMillis() + TIME_ZONE_DIFFERENCE < lastActiveTimestamp.getTime() + TIME_LIMIT)
                {
                    long userId = rs.getLong("id");

                    String gamerTag = getGamertag("Xbox Live", userId);

                    if(!gamerTag.isEmpty())
                    {


                        gamertags.add(new Person(userId, gamerTag, ""));
                    }
                }
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            return gamertags;
        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();


            }
        }

        return gamertags;
    }

    public List<Person> getSteamUsersToPoll() {
        Connection c = null;
        List<Person> gamertags = new ArrayList<>();
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        try {

            ResultSet rs = null;
            PreparedStatement select = null;

            select = c.prepareStatement("select * from users");

            rs = select.executeQuery();

            if(!rs.isBeforeFirst())
                return new ArrayList<>();


            while(rs.next())
            {
                Timestamp lastActiveTimestamp = rs.getTimestamp("last_activity");
                if(System.currentTimeMillis() + TIME_ZONE_DIFFERENCE < lastActiveTimestamp.getTime() + TIME_LIMIT)
                {
                    long userId = rs.getLong("id");

                    String gamerTag = getGamertag("Steam", userId);

                    if(!gamerTag.isEmpty())
                    {
                        gamertags.add(new Person(userId, "", gamerTag));
                    }
                }
            }

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            return gamertags;
        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return gamertags;
    }

    public String getGamertag(String platform, long userId) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        try {
            ResultSet rs = null;
            PreparedStatement select = null;

            select = c.prepareStatement("select * from gamer_identifier where platform = ? and user_id = ?");
            select.setString(1, platform);
            select.setLong(2, userId);

            rs = select.executeQuery();

            if(!rs.isBeforeFirst())
                return "";


            rs.next();

            String id = rs.getString("identifier");
            return id;

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();


            }
        }

        return "";
    }


    public void insertIntoOnlineFeed(long userId, String gamerTag, long gameId, String platform, Connection c) {

        try {
            ResultSet rs = null;
            PreparedStatement insert = null;

            insert = c.prepareStatement("INSERT INTO online_feed(gamer_tag, last_activity, game_id, user_id, platform) VALUES(?, NOW(),?,?,?)");
            insert.setString(1, gamerTag);
            insert.setLong(2, gameId);
            insert.setLong(3, userId);
            insert.setString(4, platform);

            insert.executeUpdate();

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            try {
                c.rollback();
                c.setAutoCommit(true);
                c.close();
                closedConnections++;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    public Connection deleteOldOnlineFeed(Person person, String platform) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;

            c.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement delete = null;

            delete = c.prepareStatement("DELETE FROM online_feed where user_id = ? AND platform = ?");

            delete.setLong(1, person.getUserId());
            delete.setString(2, platform);

            delete.executeUpdate();


        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            try {
                c.rollback();
                c.setAutoCommit(true);
                c.close();
                closedConnections++;
            }
            catch(Exception e)
            {
                ex.printStackTrace();


            }
        }

        return c;
    }


    public void deleteGamesFromDb() {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ResultSet rs = null;
            PreparedStatement delete = null;

            delete = c.prepareStatement("DELETE FROM game where id < 100");


            delete.executeUpdate();


        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();


            }
        }
    }

    public boolean userOwnsGame(long userId, long gameId) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            ResultSet rs = null;
            PreparedStatement select = null;

            select = c.prepareStatement("select * from user_library where user_id = ? and game_id = ?");
            select.setLong(1, userId);
            select.setLong(2, gameId);

            rs = select.executeQuery();

            if(!rs.isBeforeFirst())
                return false;
            else
                return true;

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();


            }
        }

        return false;
    }

    public List<Person> returnNewUsers()
    {
        Connection c = null;
        List<Person> gamertags = new ArrayList<>();
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        try {

            ResultSet rs = null;
            PreparedStatement select = null;

            select = c.prepareStatement("select * from new_users");

            rs = select.executeQuery();

            if(!rs.isBeforeFirst())
                return new ArrayList<>();


            while(rs.next())
            {
                    long userId = rs.getLong("user_id");

                    String gamerTag = getGamertag("Xbox Live", userId);

                    String steamId = getGamertag("Steam", userId);

                    if(!gamerTag.isEmpty() || !steamId.isEmpty())
                    {
                        gamertags.add(new Person(userId, gamerTag, steamId));
                    }
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            System.err.println(ex.getMessage());
            return gamertags;
        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception ex) {
                ex.printStackTrace();


            }
        }

        return gamertags;

    }

    public void deleteNewUser(Person person) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", "true");

            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement delete = null;

            delete = c.prepareStatement("DELETE FROM new_users where user_id = ?");

            delete.setLong(1, person.getUserId());

            delete.executeUpdate();


        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();

            try {
                c.close();
                closedConnections++;
            }
            catch(Exception e)
            {
                ex.printStackTrace();

            }
        }
    }




}
