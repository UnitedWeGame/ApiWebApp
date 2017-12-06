package com.UnitedWeGame.Utils;

import com.UnitedWeGame.APIServer.IgdbAPIService;
import com.UnitedWeGame.UserClasses.Person;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 * Created by cweeter on 4/20/17.
 */
public class DatabaseConnectionUtil {
    public static int openConnections = 0;
    public static int closedConnections = 0;
    // 1 hour time limit currently //TODO change back to 20 minutes for actual demo
    public static final long TIME_LIMIT = 1200000;
    public static final long TIME_ZONE_DIFFERENCE = 14400000;

    public static void seedDatabase() {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", Property.USE_SSL);
            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            List<String> sql = Files.readAllLines(Paths.get("/Users/cweeter/git/ApiWebApp/src/com/UnitedWeGame/resources/output.sql"));
            for (String line : sql) {
                try {
                    PreparedStatement statement = c.prepareStatement(line);
                    statement.execute();
                } catch (Exception sqlstuff) {
                    sqlstuff.printStackTrace();
                    System.err.println("***Problem with line: " + line + "****");
                }
            }
        } catch (Exception ex) {
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
            props.setProperty("ssl", Property.USE_SSL);
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
            if (!rs.isBeforeFirst())
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

    public void insertGameIntoDb(String title, String platform_name, String image_url, Map<String, Object> gameData) {
        Connection c = null;

        if (gameData == null)
            gameData = new HashMap<>();

        if (gameData.get("first_release") == null)
            gameData.put("first_release", new java.sql.Date(System.currentTimeMillis()));

        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", Property.USE_SSL);
            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ResultSet rs = null;
            PreparedStatement insert = null;
            insert = c.prepareStatement("INSERT INTO game (image_url, title, platform_title, first_release_date, summary, total_rating, total_rating_count) VALUES(?,?,?,?,?,?,?)");
            insert.setString(1, image_url);
            insert.setString(2, title);
            insert.setString(3, platform_name);
            insert.setDate(4, (java.sql.Date) gameData.get("first_release"));
            insert.setString(5, StringUtils.defaultString(gameData.get("summary") == null ? "" : gameData.get("summary").toString()));
            insert.setDouble(6, gameData.get("total_rating") == null ? 0.0d : Double.parseDouble(gameData.get("total_rating").toString()));
            insert.setInt(7, gameData.get("total_rating_count") == null ? 0 : Integer.parseInt(gameData.get("total_rating_count").toString()));

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

    public void insertScreenshotsIntoDB(Long game_id, List<String> image_urls) {
        for (String image_url : image_urls) {
            Connection c = null;
            try {
                Properties props = new Properties();
                props.setProperty("user", Property.DATABASE_USER);
                props.setProperty("password", Property.DATABASE_PASSWORD);
                props.setProperty("ssl", Property.USE_SSL);
                c = DriverManager.getConnection(Property.DATABASE_URL, props);
                openConnections++;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ResultSet rs = null;
                PreparedStatement insert = null;
                insert = c.prepareStatement("INSERT INTO screenshot (url, game_id) VALUES(?,?)");
                image_url = image_url.replace("http:https:", "http:");
                insert.setString(1, image_url);
                insert.setLong(2, game_id);
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
    }

    public void addGameToUserLibrary(long userId, long gameId) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", Property.USE_SSL);
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
            props.setProperty("ssl", Property.USE_SSL);
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
            if (!rs.isBeforeFirst())
                return new ArrayList<>();
            while (rs.next()) {
                Timestamp lastActiveTimestamp = rs.getTimestamp("last_activity");
                if (lastActiveTimestamp == null || System.currentTimeMillis() + TIME_ZONE_DIFFERENCE < lastActiveTimestamp.getTime() + TIME_LIMIT) {
                    long userId = rs.getLong("id");
                    String gamerTag = getGamertag("Xbox Live", userId);
                    if (!gamerTag.isEmpty()) {
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
            props.setProperty("ssl", Property.USE_SSL);
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
            if (!rs.isBeforeFirst())
                return new ArrayList<>();
            while (rs.next()) {
                Timestamp lastActiveTimestamp = rs.getTimestamp("last_activity");
                if (lastActiveTimestamp == null || System.currentTimeMillis() + TIME_ZONE_DIFFERENCE < lastActiveTimestamp.getTime() + TIME_LIMIT) {
                    long userId = rs.getLong("id");
                    String gamerTag = getGamertag("Steam", userId);
                    if (!gamerTag.isEmpty()) {
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
            props.setProperty("ssl", Property.USE_SSL);
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
            if (!rs.isBeforeFirst())
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

    public static void getScreenshotsForExistingGames() {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", Property.USE_SSL);
            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ResultSet rs = null;
            PreparedStatement select = null;
            select = c.prepareStatement("select id, title from game");
            rs = select.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("id");
                String title = rs.getString("title");
                new DatabaseConnectionUtil().insertScreenshotsIntoDB(id, new IgdbAPIService().getScreenshots(title));
            }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateScreenshotURLs() {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", Property.USE_SSL);
            c = DriverManager.getConnection(Property.DATABASE_URL, props);
            openConnections++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ResultSet rs = null;
            PreparedStatement select = null;
            select = c.prepareStatement("select id, url from screenshot");
            rs = select.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("id");
                String url = rs.getString("url");
                if (url.contains("t_cover_big")) {
                    url = url.replace("t_cover_big", "t_screenshot_huge");
                    c.prepareStatement("update screenshot set url = '" + url + "' where id = " + id).executeUpdate();
                } else if (url.contains("t_screenshot_big")) {
                    url = url.replace("t_screenshot_big", "t_screenshot_huge");
                    c.prepareStatement("update screenshot set url = '" + url + "' where id = " + id).executeUpdate();
                }
            }
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

    public Connection deleteOldOnlineFeed(Person person, String platform) {
        Connection c = null;
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", Property.USE_SSL);
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
            } catch (Exception e) {
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
            props.setProperty("ssl", Property.USE_SSL);
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
            props.setProperty("ssl", Property.USE_SSL);
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
            if (!rs.isBeforeFirst())
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

    public List<Person> returnNewUsers() {
        Connection c = null;
        List<Person> gamertags = new ArrayList<>();
        try {
            Properties props = new Properties();
            props.setProperty("user", Property.DATABASE_USER);
            props.setProperty("password", Property.DATABASE_PASSWORD);
            props.setProperty("ssl", Property.USE_SSL);
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
            if (!rs.isBeforeFirst())
                return new ArrayList<>();
            while (rs.next()) {
                long userId = rs.getLong("user_id");
                String gamerTag = getGamertag("Xbox Live", userId);
                String steamId = getGamertag("Steam", userId);
                if (!gamerTag.isEmpty() || !steamId.isEmpty()) {
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
            props.setProperty("ssl", Property.USE_SSL);
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
        } finally {
            try {
                c.close();
                closedConnections++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addPSGamesToDB() {
        List<String> gameTitlesPS4 = new ArrayList<>();
        List<String> gameTitlesPS3 = new ArrayList<>();

        gameTitlesPS4.add("Grand Theft Auto V");
        gameTitlesPS4.add("Call of Duty: Advanced Warfare");
        gameTitlesPS4.add("Destiny");
        gameTitlesPS4.add("Watch Dogs");
        gameTitlesPS4.add("Assassin's Creed Unity");
        gameTitlesPS4.add("Far Cry 4");
        gameTitlesPS4.add("Middle-Earth: Shadow of Mordor");
        gameTitlesPS4.add("Middle-Earth: Shadow of War");
        gameTitlesPS4.add("The Last of Us Remastered");
        gameTitlesPS4.add("Sleeping Dogs Definitive Edition");
        gameTitlesPS4.add("Tomb Raider Definitive Edition");
        gameTitlesPS4.add("Metro: Redux");
        gameTitlesPS4.add("Call of Duty: Ghosts");
        gameTitlesPS4.add("Wolfenstein: The New Order");
        gameTitlesPS4.add("Metal Gear Solid V: Ground Zeroes");
        gameTitlesPS4.add("Assassin's Creed IV: Black Flag");
        gameTitlesPS4.add("Metal Gear Solid V: The Phantom Pain");
        gameTitlesPS4.add("Battlefield Hardline");
        gameTitlesPS4.add("LEGO Marvel Super Heroes");
        gameTitlesPS4.add("Little Big Planet 3");
        gameTitlesPS4.add("LEGO Batman 3: Beyond Gotham");
        gameTitlesPS4.add("Dragon Age: Inquisition");
        gameTitlesPS4.add("InFAMOUS: Second Son");
        gameTitlesPS4.add("InFAMOUS: First Light");
        gameTitlesPS4.add("Tom Clancy's The Division");
        gameTitlesPS4.add("Unchartered 4: A Thief's End");
        gameTitlesPS4.add("Rayman Legends");
        gameTitlesPS4.add("Lara Croft and the Temple of Osiris");
        gameTitlesPS4.add("The LEGO Movie: Videogame");
        gameTitlesPS4.add("FIFA 15");
        gameTitlesPS4.add("WWE 2k15");
        gameTitlesPS4.add("NBA 2k15");
        gameTitlesPS4.add("Madden NFL 15");
        gameTitlesPS4.add("EA Sports UFC");
        gameTitlesPS4.add("Pro Evolution Soccer 2015");
        gameTitlesPS4.add("The Crew");
        gameTitlesPS4.add("DriveClub");
        gameTitlesPS4.add("Need For Speed: Rivals");
        gameTitlesPS4.add("Project CARS");
        gameTitlesPS4.add("Lords of the Fallen");
        gameTitlesPS4.add("Final Fantasy Type-0");
        gameTitlesPS4.add("Diablo 3: Reaper of Souls - Ultimate Evil Edition");
        gameTitlesPS4.add("The Elder Scrolls Online");
        gameTitlesPS4.add("Minecraft");
        gameTitlesPS4.add("Singstar: Ultimate Party");
        gameTitlesPS4.add("Rocksmith 2014 Edition");
        gameTitlesPS4.add("Alien: Isolation");
        gameTitlesPS4.add("The Evil Within");
        gameTitlesPS4.add("The Witcher 3: Wild Hunt");
        gameTitlesPS4.add("The Walking Dead: Season 1");
        gameTitlesPS4.add("The Walking Dead: Season 2");
        gameTitlesPS4.add("Dying Light");
        gameTitlesPS4.add("Evolve");
        gameTitlesPS4.add("Horizon Zero Dawn");
        gameTitlesPS4.add("Destiny 2");
        gameTitlesPS4.add("Resident Evil 7: Biohazard");
        gameTitlesPS4.add("Nier: Automata");
        gameTitlesPS4.add("Star Wars Battlefront 2");
        gameTitlesPS4.add("Call of Duty: WW2");
        gameTitlesPS4.add("Nioh");
        gameTitlesPS4.add("FIFA 18");
        gameTitlesPS4.add("Injustice 2");
        gameTitlesPS4.add("Gran Turismo Sport");
        gameTitlesPS4.add("Prey");
        gameTitlesPS4.add("For Honor");
        gameTitlesPS4.add("Gravity Rush 2");
        gameTitlesPS4.add("South Park: The Fractured But Whole");
        gameTitlesPS4.add("Madden NFL 18");
        gameTitlesPS4.add("Everybody's Golf");
        gameTitlesPS4.add("Rocket League");
        gameTitlesPS4.add("Sonic Forces");
        gameTitlesPS4.add("Pyre");
        gameTitlesPS4.add("NBA 2K18");
        gameTitlesPS4.add("Dirt 4");
        gameTitlesPS4.add("WWE 2K18");
        gameTitlesPS4.add("F1 2017");
        gameTitlesPS4.add("Yooka-Laylee");
        gameTitlesPS4.add("Rime");


        gameTitlesPS3.add("Red Dead Redemption");
        gameTitlesPS3.add("The Last of Us");
        gameTitlesPS3.add("Grand Theft Auto V");
        gameTitlesPS3.add("Uncharted 2: Among Thieves");
        gameTitlesPS3.add("LittleBigPlanet");
        gameTitlesPS3.add("Batman: Arkham Asylum");
        gameTitlesPS3.add("Batman: Arkham City");
        gameTitlesPS3.add("God of War 3");
        gameTitlesPS3.add("The Elder Scrolls V: Skyrim");
        gameTitlesPS3.add("Heavy Rain");
        gameTitlesPS3.add("BioShock");
        gameTitlesPS3.add("Portal 2");
        gameTitlesPS3.add("Call of Duty: Modern Warfare 2");
        gameTitlesPS3.add("BioShock Infinite");
        gameTitlesPS3.add("Fallout 3");
        gameTitlesPS3.add("Dark Souls");
        gameTitlesPS3.add("Demon's Souls");
        gameTitlesPS3.add("Infamous 2");
        gameTitlesPS3.add("LittleBigPlanet 2");
        gameTitlesPS3.add("Deus Ex: Human Revolution");
        gameTitlesPS3.add("Far Cry 3");
        gameTitlesPS3.add("Mass Effect 2");
        gameTitlesPS3.add("Mass Effect 3");
        gameTitlesPS3.add("L.A. Noire");
        gameTitlesPS3.add("Fallout: New Vegas");
        gameTitlesPS3.add("Borderlands 2");
        gameTitlesPS3.add("Dragon Age: Origins");
        gameTitlesPS3.add("Bayonetta");
        gameTitlesPS3.add("Killzone 2");
        gameTitlesPS3.add("Super Street Fighter IV");
        gameTitlesPS3.add("Dead Space 2");
        gameTitlesPS3.add("Vanquish");
        gameTitlesPS3.add("Resident Evil 5");
        gameTitlesPS3.add("Dark Souls 2");
        gameTitlesPS3.add("Burnout Paradise");
        gameTitlesPS3.add("Resistance: Fall of Man");
        gameTitlesPS3.add("Rayman Legends");
        gameTitlesPS3.add("Tomb Raider");
        gameTitlesPS3.add("Minecraft");
        gameTitlesPS3.add("The Walking Dead");
        gameTitlesPS3.add("Killzone 3");
        gameTitlesPS3.add("Portal");

        IgdbAPIService igdb = new IgdbAPIService();

        for (String gameTitle : gameTitlesPS4) {

            if (this.getGameID(gameTitle, "PS4") == -1) {
                String imageUrl = igdb.getGameImage(gameTitle);
                List<String> screenshots = igdb.getScreenshots(gameTitle);
                this.insertGameIntoDb(gameTitle, "PS4", imageUrl, igdb.getGameData(gameTitle));
                long gameID = this.getGameID(gameTitle, "PS4");
                this.insertScreenshotsIntoDB(gameID, screenshots);
            }
        }

        for (String gameTitle : gameTitlesPS3) {

            if (this.getGameID(gameTitle, "PS3") == -1) {
                String imageUrl = igdb.getGameImage(gameTitle);
                List<String> screenshots = igdb.getScreenshots(gameTitle);
                this.insertGameIntoDb(gameTitle, "PS3", imageUrl, igdb.getGameData(gameTitle));
                long gameID = this.getGameID(gameTitle, "PS3");
                this.insertScreenshotsIntoDB(gameID, screenshots);
            }
        }

    }
}
