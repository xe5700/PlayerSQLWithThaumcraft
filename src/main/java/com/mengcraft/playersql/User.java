package com.mengcraft.playersql;

import com.avaje.ebean.annotation.UpdatedTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

import static com.mengcraft.playersql.User.TABLE_NAME;

/**
 * Created on 16-1-2.
 */
@Entity
@Table(name = TABLE_NAME)
public class User {

    public static final String TABLE_NAME = "PLAYERSQL";

    @Id
    private UUID uuid;

    private double health;

    private int food;

    private int hand;

    private int exp;

    @Column(columnDefinition = "TEXT")
    private String inventory;

    @Column(columnDefinition = "TEXT")
    private String armor;

    @Column(columnDefinition = "TEXT")
    private String chest;

    @Column(columnDefinition = "TEXT")
    private String effect;

    private boolean locked;

    @UpdatedTimestamp
    private Timestamp lastUpdate;

    public String getResearchCompleted() {
        return researchCompleted;
    }

    public void setResearchCompleted(String researchCompleted) {
        this.researchCompleted = researchCompleted;
    }

    public String getObjectsScanned() {
        return objectsScanned;
    }

    public void setObjectsScanned(String objectsScanned) {
        this.objectsScanned = objectsScanned;
    }

    public String getEntitiesScanned() {
        return entitiesScanned;
    }

    public void setEntitiesScanned(String entitiesScanned) {
        this.entitiesScanned = entitiesScanned;
    }

    public String getPhenomenaScanned() {
        return phenomenaScanned;
    }

    public void setPhenomenaScanned(String phenomenaScanned) {
        this.phenomenaScanned = phenomenaScanned;
    }

    @Column(columnDefinition = "TEXT")
    private String researchCompleted;

    public String getKnownAspects() {
        return knownAspects;
    }

    public void setKnownAspects(String knownAspects) {
        this.knownAspects = knownAspects;
    }

    @Column(columnDefinition = "TEXT")
    private String knownAspects;
    @Column(columnDefinition = "TEXT")
    private String objectsScanned;
    @Column(columnDefinition = "TEXT")
    private String entitiesScanned;
    @Column(columnDefinition = "TEXT")
    private String phenomenaScanned;

    public int getWarpCount() {
        return warpCount;
    }

    public void setWarpCount(int warpCount) {
        this.warpCount = warpCount;
    }

    public int getWarp() {
        return warp;
    }

    public void setWarp(int warp) {
        this.warp = warp;
    }

    public int getWarpSticky() {
        return warpSticky;
    }

    public void setWarpSticky(int warpSticky) {
        this.warpSticky = warpSticky;
    }

    public int getWarpTemp() {
        return warpTemp;
    }

    public void setWarpTemp(int warpTemp) {
        this.warpTemp = warpTemp;
    }

    @Column(columnDefinition = "TEXT")
    private int warpCount;
    @Column(columnDefinition = "TEXT")
    private int warp;
    @Column(columnDefinition = "TEXT")
    private int warpSticky;
    @Column(columnDefinition = "TEXT")
    private int warpTemp;

    public String getBaules() {
        return Baules;
    }

    public void setBaules(String baules) {
        Baules = baules;
    }

    @Column(columnDefinition = "TEXT")
    private String Baules;
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getHand() {
        return hand;
    }

    public void setHand(int hand) {
        this.hand = hand;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public String getArmor() {
        return armor;
    }

    public void setArmor(String armor) {
        this.armor = armor;
    }

    public String getChest() {
        return chest;
    }

    public void setChest(String chest) {
        this.chest = chest;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}
