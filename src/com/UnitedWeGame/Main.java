package com.UnitedWeGame;

import com.UnitedWeGame.APIServer.APIInterface;
import com.UnitedWeGame.APIServer.XboxAPIRequest;
import com.UnitedWeGame.InformationObjects.Game;
import com.UnitedWeGame.UserClasses.Friend;
import com.UnitedWeGame.UserClasses.Person;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        APIInterface xboxInterface = new XboxAPIRequest();

        Person person = new Person();
        person.setXboxIdentifier("2533274792755153");

        List<Game> games = xboxInterface.getGameLibrary(person);

        for(Game game : games)
        {
            System.out.println(game.getPlatform() + "\t" + game.getTitle());
        }


        List<Friend> friends =  xboxInterface.getFriendsStatus(person);

//        for(Friend friend : friends)
//        {
//            System.out.println(friend.getGamertag() + ":\t" + friend.isOnline());
//        }



    }
}
