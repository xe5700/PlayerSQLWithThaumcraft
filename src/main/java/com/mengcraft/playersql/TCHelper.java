package com.mengcraft.playersql;

import baubles.api.BaublesApi;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.mengcraft.playersql.lib.JSONUtil;
import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import sun.nio.cs.US_ASCII;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.CommonProxy;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.research.PlayerKnowledge;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xjboss on 2017/4/30.
 */
public class TCHelper {
    public static CommonProxy proxy;
    public static PlayerKnowledge know;
    public static PluginMain pm;
    public static HashMap<String, InventoryBaubles> pBaules;
    static {
            proxy=Thaumcraft.proxy;
            know=proxy.getPlayerKnowledge();
            try {
                Field f=PlayerHandler.class.getDeclaredField("playerBaubles");
                f.setAccessible(true);
                pBaules = (HashMap<String, InventoryBaubles>)f.get(PlayerHandler.class);
            }catch (Exception e){
                if(Config.DEBUG) {
                    e.printStackTrace();
                }
            }
    }
    public static String getCompletedResearch(Player p){
        try {
            final StringBuilder SB = new StringBuilder();
            proxy.getCompletedResearch().get(p.getName()).forEach((o) -> {
                SB.append(o);
                SB.append(",");
            });
            SB.deleteCharAt(SB.length() - 1);

            if (Config.DEBUG) {
                Bukkit.getLogger().info("[PlayerSQL] getCompletedResearch successful");
            }
            return SB.toString();
        }catch (Exception e){
            return "";
        }
    }
    public static String getKnownAspects(Player p){
        try {
            final StringBuilder SB = new StringBuilder();
            AspectList aspectslist = proxy.getPlayerKnowledge().getAspectsDiscovered(p.getName());
            aspectslist.aspects.entrySet().forEach((e) -> {
                SB.append(e.getKey().getTag());
                SB.append(":");
                SB.append(e.getValue());
                SB.append(",");
            });
            SB.deleteCharAt(SB.length() - 1);
            if (Config.DEBUG) {
                Bukkit.getLogger().info("[PlayerSQL] getKnownAspects successful");
            }
            return SB.toString();
        }catch (Exception e){return "";}
    }
    public static String getObjectsScanned(Player p){
        try {
        final StringBuilder SB=new StringBuilder();
        proxy.getScannedObjects().get(p.getName()).forEach((o)->{
            SB.append(o);
            SB.append(",");
        });
        SB.deleteCharAt(SB.length()-1);
            if (Config.DEBUG) {
                Bukkit.getLogger().info("[PlayerSQL] getObjectsScanned successful");
            }
        return SB.toString();
    }catch (Exception e){return "";}
    }
    public static String getEntitiesScanned(Player p){
        try {
        final StringBuilder SB=new StringBuilder();
        proxy.getScannedEntities().get(p.getName()).forEach((o)->{
            SB.append(o);
            SB.append(",");
        });
        SB.deleteCharAt(SB.length()-1);
            if (Config.DEBUG) {
                Bukkit.getLogger().info("[PlayerSQL] getEntitiesScanned successful");
            }
        return SB.toString();
    }catch (Exception e){return "";}
    }
    public static String getScannedPhenomena(Player p){
        try {
        final StringBuilder SB=new StringBuilder();
        proxy.getScannedPhenomena().get(p.getName()).forEach((o)->{
            SB.append(o);
            SB.append(",");
        });
        SB.deleteCharAt(SB.length()-1);
            if (Config.DEBUG) {
                Bukkit.getLogger().info("[PlayerSQL] getScannedPhenomena successful");
            }
        return SB.toString();
        }catch (Exception e){return "";}
    }
    public static String getBaules(Player p){
        try {
            String pn=p.getName();
             InventoryBaubles inv = pBaules.get(pn);
            NBTTagCompound tag=new NBTTagCompound();
            inv.saveNBT(tag);
            if(Config.DEBUG){
                Bukkit.getLogger().info("[PlayerSQL] get baubles successful");
            }
            return tag.toString();
        }catch (Exception e){}
        return "";
    }
    public static void getTC(Player p,User user){
        final Object o=new Object();
        Runnable r=()-> {
            synchronized (o) {
                user.setResearchCompleted(TCHelper.getCompletedResearch(p));
                user.setKnownAspects(TCHelper.getKnownAspects(p));
                user.setObjectsScanned(TCHelper.getObjectsScanned(p));
                user.setEntitiesScanned(TCHelper.getEntitiesScanned(p));
                user.setPhenomenaScanned(TCHelper.getScannedPhenomena(p));
                String pn = p.getName();
                Integer i;
                user.setWarp((i = TCHelper.proxy.getPlayerKnowledge().warp.get(pn)) != null ? i : 0);
                user.setWarpCount((i = TCHelper.proxy.getPlayerKnowledge().warpCount.get(pn)) != null ? i : 0);
                user.setWarpSticky((i = TCHelper.proxy.getPlayerKnowledge().warpSticky.get(pn)) != null ? i : 0);
                user.setWarpTemp((i = TCHelper.proxy.getPlayerKnowledge().warpTemp.get(pn)) != null ? i : 0);
                user.setBaules(TCHelper.getBaules(p));
                o.notifyAll();
            }
        };
        if(pm.shutdown){
            r.run();
        }else {
            pm.run(r);
            synchronized (o){
                try {
                    o.wait();
                }catch (Exception e) {
                    return;
                }
            }
        }
    }
    public static void syncTC(Player p, User u){
        final Object o=new Object();

        Runnable r=()-> {
            synchronized (o) {
                String pn = p.getName();
                if (!u.getResearchCompleted().equals("")) {
                    String[] inf = u.getResearchCompleted().split(",");
                    ArrayList<String> research = new ArrayList<>();
                    for (String i : inf) {
                        research.add(i);
                    }
                    if (Config.DEBUG) {
                        Bukkit.getLogger().info("[PlayerSQL] set ScannedPhenomena successful");
                    }
                    know.researchCompleted.put(pn, research);
                }
                if (!u.getKnownAspects().equals("")) {
                    AspectList aspects = new AspectList();
                    String[] inf = u.getKnownAspects().split(",");
                    for (String i : inf) {
                        String[] aspectinf = i.split(":");
                        aspects.aspects.put(Aspect.getAspect(aspectinf[0]), Integer.valueOf(aspectinf[1]));
                    }
                    if (Config.DEBUG) {
                        Bukkit.getLogger().info("[PlayerSQL] set KnownAspects successful");
                    }
                    proxy.getKnownAspects().put(pn, aspects);
                }
                if (!u.getObjectsScanned().equals("")) {
                    ArrayList<String> scan = new ArrayList<>();
                    for (String i : u.getObjectsScanned().split(",")) {
                        scan.add(i);
                    }
                    if (Config.DEBUG) {
                        Bukkit.getLogger().info("[PlayerSQL] set ScannedObjects successful");
                    }
                    proxy.getScannedObjects().put(pn, scan);
                }

                if (!u.getEntitiesScanned().equals("")) {
                    ArrayList<String> escan = new ArrayList<>();
                    escan.clear();
                    for (String i : u.getEntitiesScanned().split(",")) {
                        escan.add(i);
                    }
                    if (Config.DEBUG) {
                        Bukkit.getLogger().info("[PlayerSQL] set EntitiesScanned successful");
                    }
                    proxy.getScannedEntities().put(pn, escan);
                }

                if (!u.getPhenomenaScanned().equals("")) {
                    ArrayList<String> pscan = new ArrayList<>();
                    pscan.clear();
                    for (String i : u.getPhenomenaScanned().split(",")) {
                        pscan.add(i);
                    }
                    if (Config.DEBUG) {
                        Bukkit.getLogger().info("[PlayerSQL] set PhenomenaScanned successful");
                    }
                    proxy.getScannedPhenomena().put(pn, pscan);
                }
                know.warp.put(pn, u.getWarp());
                if (Config.DEBUG) {
                    Bukkit.getLogger().info("[PlayerSQL] set Warp successful");
                }
                know.warpTemp.put(pn, u.getWarpTemp());
                if (Config.DEBUG) {
                    Bukkit.getLogger().info("[PlayerSQL] set WarpTemp successful");
                }
                know.warpSticky.put(pn, u.getWarpSticky());
                if (Config.DEBUG) {
                    Bukkit.getLogger().info("[PlayerSQL] set WarpSticky successful");
                }
                know.warpCount.put(pn, u.getWarpCount());
                if (Config.DEBUG) {
                    Bukkit.getLogger().info("[PlayerSQL] set WarpCount successful");
                }
                if (!u.getBaules().isEmpty()) {
                    InventoryBaubles inv=pBaules.get(pn);
                    for(int i=0;i<inv.stackList.length;i++){
                        inv.stackList[i]=null;
                    }
                    try {
                        inv.readNBT((NBTTagCompound) JsonToNBT.func_150315_a(u.getBaules()));
                        if(Config.DEBUG){
                            Bukkit.getLogger().info("[PlayerSQL] set baubles successful");
                        }
                    }catch (Exception e){
                        if (Config.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
                o.notifyAll();
            }
        };
        if(pm.shutdown){
            r.run();
        }else {
            pm.run(r);
            synchronized (o){
                try {
                    o.wait();
                }catch (Exception e) {
                    return;
                }
            }
        }
    }
}
