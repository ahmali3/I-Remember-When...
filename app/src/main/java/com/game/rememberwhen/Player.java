/* POJO / Model Class for Player Entity */
package com.game.rememberwhen;

import java.io.Serializable;

public class Player implements Serializable, Comparable<Player> {
    public String token;
    private String name;
    private int index;
    private int roomId;
    private int score;
    private int scoreDif;
    private String status;
    private String response;

    // Default Constructor
    public Player() {
    }

    // Overridden constructor
    public Player(String name, int score, int scoreDif, String status, String deviceToken) {
        this.name = name;
        this.score = score;
        this.scoreDif = scoreDif;
        this.status = status;
        this.token = deviceToken;
    }

    public Player(String name, int index, int score, int scoreDif, String status, String deviceToken) {
        this.name = name;
        this.index = index;
        this.score = score;
        this.scoreDif = scoreDif;
        this.status = status;
        this.token = deviceToken;
    }

    public boolean equals(Player p1) {
        return (name.equals(p1.getName())) && (roomId == p1.getRoomId()) && (score == p1.getScore());
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScoreDif() {
        return scoreDif;
    }

    public void setScoreDif(int scoreDif) {
        this.scoreDif = scoreDif;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getResponse() {
        return this.response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public String toString() {
        if (scoreDif > 0)
            return "Name: " + name + " \tScore: " + score + " \tGained: " + scoreDif;
        else
            return "Name: " + name + " \tScore: " + score + " \tLost: " + -1 * scoreDif;

    }

    @Override
    public int compareTo(Player o) {
        return (this.getScore() < o.getScore() ? -1 :
                (this.getScore() == o.getScore() ? 0 : 1));
    }
}

