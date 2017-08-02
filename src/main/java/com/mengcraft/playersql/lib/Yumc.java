package com.mengcraft.playersql.lib;

/*
 * Copyright 2011-2015 喵♂呜. All rights reserved.
 */

import java.io.File;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Yum数据中心 数据统计类
 *
 * @since 2015年12月14日 下午1:36:42
 * @author 喵♂呜
 */
public class Yumc {
    /**
     * 统计系统版本
     */
    private final static int REVISION = 10;

    /**
     * 统计插件基础配置文件
     */
    private final static File configfile = new File("plugins" + File.separatorChar + "PluginHelper", "config.yml");

    /**
     * UTF-8编码
     */
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * getOnlinePlayers方法
     */
    private static Method getOnlinePlayers;

    /**
     * 插件实体
     */
    private Plugin plugin;

    static {
        try {
            getOnlinePlayers = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
            if (getOnlinePlayers.getReturnType() != Player[].class) {
                for (final Method method : Bukkit.class.getDeclaredMethods()) {
                    if (method.getReturnType() == Player[].class && method.getName().endsWith("getOnlinePlayers")) {
                        getOnlinePlayers = method;
                    }
                }
            }
        } catch (NoSuchMethodException | SecurityException  | IllegalArgumentException e) {
        }
    }

    /**
     * 统计插件基础配置文件
     */
    private YamlConfiguration config;

    /**
     * 调试模式
     */
    private final boolean debug;

    /**
     * 唯一服务器编码
     */
    private final String guid;

    /**
     * 线程任务
     */
    private volatile BukkitTask task = null;

    /**
     * 统计线程
     */
    private volatile StatisticsTimer timer = null;

    /**
     * 插件使用数据统计
     *
     * @throws IOException
     *             IO异常
     */
    public Yumc(Plugin plugin) {
        this.plugin=plugin;
        try {
            if (!configfile.exists()) {
                configfile.getParentFile().mkdirs();
                configfile.createNewFile();
            }
            config = YamlConfiguration.loadConfiguration(configfile);
            initFile(config);
        } catch (final IOException e) {
        }
        this.guid = config.getString("guid");
        this.debug = config.getBoolean("debug", false);
        start();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数
     * @return 所代表远程资源的响应结果
     */
    public static String postData(final String url, final String param) {
        PrintWriter out = null;
        String result = "";
        try {
            final URL realUrl = new URL(url);
            // 打开和URL之间的连接
            final URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
            // 设置超时时间 10秒
            conn.setReadTimeout(10000);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.write(param);
            // flush输出流的缓冲
            out.flush();
            String response = "";
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF_8));
            while ((response = reader.readLine()) != null) {
                result += response;
            }
            reader.close();
        } catch (final Exception e) {
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return result;
    }

    /**
     * 初始化配置文件
     *
     * @param config
     *            配置文件
     * @throws IOException
     */
    private static void initFile(final YamlConfiguration config) throws IOException {
        if (config.getString("guid") == null) {
            config.options().header("YUMC数据中心 http://www.yumc.pw 收集的数据仅用于统计插件使用情况").copyDefaults(true);
            config.set("guid", UUID.randomUUID().toString());
            config.set("debug", false);
            config.save(configfile);
        }
        if (!config.contains("YumAccount")) {
            config.set("YumAccount.username", "Username Not Set");
            config.set("YumAccount.password", "Password NotSet");
            config.save(configfile);
        }
        if (!config.contains("TellrawManualHandle")) {
            config.set("TellrawManualHandle", false);
            config.save(configfile);
        }
    }

    /**
     * 简化输出
     *
     * @param msg
     *            输出对象
     */
    public void print(final String msg) {
        if (debug) {
            System.out.println("[Yumc] " + msg);
        }
    }

    /**
     * 开启数据统计 这将会在异步执行
     *
     * @return 是否运行成功.
     */
    public boolean start() {
        if (task != null) {
            return true;
        }
        timer = new StatisticsTimer();
        // 开启TPS统计线程
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, timer, 0, 20);
        // 开启发送数据线程
        task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    postPlugin();
                } catch (final Throwable e) {
                    if (debug) {
                        e.printStackTrace();
                    }
                }
            }
        }, 50, 25 * 1200);
        return true;
    }

    /**
     * 获得在线玩家人数
     *
     * @return 在线玩家人数
     */
    private int getOnlinePlayerNumber() {
        try {
            return ((Player[]) getOnlinePlayers.invoke(Bukkit.getServer())).length;
        } catch (final Exception ex) {
            return Bukkit.getOnlinePlayers().size();
        }
    }

    /**
     * 发送服务器数据到统计网页
     */
    private void postPlugin() throws IOException {
        // 服务器数据获取
        final PluginDescriptionFile description = plugin.getDescription();
        final String pluginname = description.getName()+"WithThaumcraft";
        final String tmposarch = System.getProperty("os.arch");

        final Map<String, Object> data = new HashMap<>();
        data.put("guid", guid);
        data.put("server_version", Bukkit.getVersion());
        data.put("server_port", Bukkit.getServer().getPort());
        data.put("server_tps", timer.getAverageTPS());
        data.put("plugin_version", description.getVersion());
        data.put("players_online", getOnlinePlayerNumber());
        data.put("os_name", System.getProperty("os.name"));
        data.put("os_arch", tmposarch.equalsIgnoreCase("amd64") ? "x86_64" : tmposarch);
        data.put("os_version", System.getProperty("os.version"));
        data.put("os_usemem", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
        data.put("os_cores", Runtime.getRuntime().availableProcessors());
        data.put("auth_mode", Bukkit.getServer().getOnlineMode() ? 1 : 0);
        data.put("java_version", System.getProperty("java.version"));

        final String jsondata = "Info=" + JSONValue.toJSONString(data);

        final String url = String.format("http://api.yumc.pw/I/P/S/V/%s/P/%s", REVISION, URLEncoder.encode(pluginname, "UTF-8"));
        print("Plugin: " + pluginname + " Send Data To CityCraft Data Center");
        print("Address: " + url);
        print("Data: " + jsondata);
        // 发送数据
        final JSONObject result = (JSONObject) JSONValue.parse(postData(url, jsondata));
        print("Plugin: " + pluginname + " Recover Data From CityCraft Data Center: " + result.get("info"));
    }

    public class StatisticsTimer implements Runnable {
        private final LinkedList<Double> history = new LinkedList<Double>();
        private transient long lastPoll = System.nanoTime();

        public double getAverageTPS() {
            double avg = 0.0D;
            for (final Double f : history) {
                avg += f.doubleValue();
            }
            return avg / history.size();
        }

        @Override
        public void run() {
            final long startTime = System.nanoTime();
            final long timeSpent = (startTime - lastPoll) / 1000;
            if (history.size() > 10) {
                history.remove();
            }
            final double ttps = 2.0E7D / (timeSpent == 0 ? 1 : timeSpent);
            if (ttps <= 21.0D) {
                history.add(ttps);
            }
            lastPoll = startTime;
        }
    }
}