package com.coloryr.allmusic.server.side.forge;

import com.coloryr.allmusic.server.AllMusicForge;
import com.coloryr.allmusic.server.TaskItem;
import com.coloryr.allmusic.server.Tasks;
import com.coloryr.allmusic.server.core.AllMusic;
import com.coloryr.allmusic.server.core.objs.config.SaveObj;
import com.coloryr.allmusic.server.core.objs.enums.ComType;
import com.coloryr.allmusic.server.core.objs.enums.HudType;
import com.coloryr.allmusic.server.core.objs.music.MusicObj;
import com.coloryr.allmusic.server.core.objs.music.SongInfoObj;
import com.coloryr.allmusic.server.core.side.ISide;
import com.coloryr.allmusic.server.core.utils.HudUtils;
import com.coloryr.allmusic.server.side.forge.event.MusicAddEvent;
import com.coloryr.allmusic.server.side.forge.event.MusicPlayEvent;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SideForge extends ISide {

    @Override
    public void reload() {
        String path = String.format(Locale.ROOT, "config/%s/", "AllMusic");
        new AllMusic().init(new File(path));
    }

    @Override
    public int getAllPlayer() {
        return AllMusicForge.server.getCurrentPlayerCount();
    }

    @Override
    public void runTask(Runnable run) {
        AllMusicForge.server.addScheduledTask(run);
    }

    @Override
    public void runTask(Runnable run1, int delay) {
        Tasks.add(new TaskItem() {{
            tick = delay;
            run = run1;
        }});
    }

    @Override
    public boolean checkPermission(String player, String permission) {
        EntityPlayerMP player1 = AllMusicForge.server.getPlayerList().getPlayerByUsername(player);
        if (player1 == null)
            return false;

        return player1.canUseCommand(2, "music");
    }

    @Override
    public boolean needPlay() {
        int online = getAllPlayer();
        for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
            if (AllMusic.getConfig().mutePlayer.contains(player.getName())) {
                online--;
            }
        }
        return online > 0;
    }

    @Override
    protected void topSendStop() {
        try {
            for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.STOP.ordinal());
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void topSendStop(String name) {
        try {
            EntityPlayerMP player = AllMusicForge.server.getPlayerList().getPlayerByUsername(name);
            if (player == null)
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.STOP.ordinal());
            send(player, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendMusic(String url) {
        try {
            for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
                if (AllMusic.isOK(player.getName(), null, false))
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.PLAY.ordinal());
                writeString(buf, url);
                send(player, buf);
                AllMusic.addNowPlayPlayer(player.getName());
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void topSendMusic(String player, String url) {
        try {
            EntityPlayerMP player1 = AllMusicForge.server.getPlayerList().getPlayerByUsername(player);
            if (player1 == null)
                return;
            if (AllMusic.isOK(player, null, false))
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.PLAY.ordinal());
            writeString(buf, url);
            send(player1, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPic(String url) {
        try {
            for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
                if (AllMusic.isOK(player.getName(), null, true))
                    continue;
                String name = player.getName();
                SaveObj obj = HudUtils.get(name);
                if (!obj.pic.enable)
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.IMG.ordinal());
                writeString(buf, url);
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c图片指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPic(String player, String url) {
        try {
            EntityPlayerMP player1 = AllMusicForge.server.getPlayerList().getPlayerByUsername(player);
            if (player1 == null)
                return;
            if (AllMusic.isOK(player1.getName(), null, true))
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.IMG.ordinal());
            writeString(buf, url);
            send(player1, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c图片指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPos(String player, int pos) {
        try {
            EntityPlayerMP player1 = AllMusicForge.server.getPlayerList().getPlayerByUsername(player);
            if (player1 == null)
                return;
            if (AllMusic.isOK(player1.getName(), null, true))
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.POS.ordinal());
            buf.writeInt(pos);
            send(player1, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudLyric(String data) {
        try {
            for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
                if (AllMusic.isOK(player.getName(), null, true))
                    continue;
                String name = player.getName();
                SaveObj obj = HudUtils.get(name);
                if (!obj.lyric.enable)
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.LYRIC.ordinal());
                writeString(buf, data);
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudInfo(String data) {
        try {
            for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
                if (AllMusic.isOK(player.getName(), null, true))
                    continue;
                String name = player.getName();
                SaveObj obj = HudUtils.get(name);
                if (!obj.info.enable)
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.INFO.ordinal());
                writeString(buf, data);
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词信息发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudPos(String name) {
        try {
            EntityPlayerMP player = AllMusicForge.server.getPlayerList().getPlayerByUsername(name);
            if (player == null)
                return;
            SaveObj obj = HudUtils.get(name);
            String data = AllMusic.gson.toJson(obj);
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.HUD.ordinal());
            writeString(buf, data);
            send(player, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c界面位置发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHud(String name, HudType pos, String data) {
        try {
            if (pos == HudType.PIC) {
                return;
            }
            EntityPlayerMP player = AllMusicForge.server.getPlayerList().getPlayerByUsername(name);
            if (player == null)
                return;
            if (AllMusic.isOK(name, null, true))
                return;
            ByteBuf buf = Unpooled.buffer();
            switch (pos) {
                case INFO:
                    buf.writeByte(ComType.INFO.ordinal());
                    break;
                case LIST:
                    buf.writeByte(ComType.LIST.ordinal());
                    break;
                case LYRIC:
                    buf.writeByte(ComType.LYRIC.ordinal());
                    break;
            }
            writeString(buf, data);
            send(player, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudList(String data) {
        try {
            for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
                if (AllMusic.isOK(player.getName(), null, true))
                    continue;
                String name = player.getName();
                SaveObj obj = HudUtils.get(name);
                if (!obj.list.enable)
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.LIST.ordinal());
                writeString(buf, data);
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲列表发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudUtilsAll() {
        for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
            String Name = player.getName();
            try {
                SaveObj obj = HudUtils.get(Name);
                String data = new Gson().toJson(obj);
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.HUD.ordinal());
                writeString(buf, data);
                send(player, buf);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void sendBar(String data) {
        for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
            try {
                if (AllMusic.isOK(player.getName(), null, true))
                    continue;
                ForgeApi.sendBar(player, data);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void clearHud(String name) {
        try {
            EntityPlayerMP player = AllMusicForge.server.getPlayerList().getPlayerByUsername(name);
            if (player == null)
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.CLEAR.ordinal());
            send(player, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void clearHud() {
        try {
            for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.CLEAR.ordinal());
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void bq(String data) {
        if (AllMusic.getConfig().messageLimit
                && data.length() > AllMusic.getConfig().messageLimitSize) {
            data = data.substring(0, AllMusic.getConfig().messageLimitSize - 1) + "...";
        }
        for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
            if (!AllMusic.getConfig().mutePlayer.contains(player.getName())) {
                player.sendMessage(new TextComponentString(data));
            }
        }
    }

    @Override
    public void bqRun(String message, String end, String command) {
        ForgeApi.sendMessageBqRun(message, end, command);
    }

    @Override
    public void bqt(String data) {
        if (AllMusic.getConfig().messageLimit
                && data.length() > AllMusic.getConfig().messageLimitSize) {
            data = data.substring(0, AllMusic.getConfig().messageLimitSize - 1) + "...";
        }
        TextComponentString finalData = new TextComponentString(data);
        runTask(() -> {
            for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
                if (!AllMusic.getConfig().mutePlayer.contains(player.getName())) {
                    player.sendMessage(finalData);
                }
            }
        });
    }

    @Override
    public void sendMessaget(Object obj, String message) {
        runTask(() -> ((ICommandSender) obj).sendMessage(new TextComponentString(message)));
    }

    @Override
    public void sendMessage(Object obj, String message) {
        ICommandSender sender = (ICommandSender) obj;
        sender.sendMessage(new TextComponentString(message));
    }

    @Override
    public void sendMessageRun(Object obj, String message, String end, String command) {
        ForgeApi.sendMessageRun(obj, message, end, command);
    }

    @Override
    public void sendMessageSuggest(Object obj, String message, String end, String command) {
        ForgeApi.sendMessageSuggest(obj, message, end, command);
    }

    @Override
    public boolean onMusicPlay(SongInfoObj obj) {
        MusicPlayEvent event = new MusicPlayEvent(obj);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Override
    public boolean onMusicAdd(Object obj, MusicObj music) {
        MusicAddEvent event = new MusicAddEvent(music, (ICommandSender) obj);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Override
    public void updateInfo() {

    }

    @Override
    public void updateLyric() {

    }

    @Override
    public void ping() {

    }

    @Override
    public List<String> getPlayerList() {
        List<String> list = new ArrayList<>();
        for (EntityPlayerMP player : AllMusicForge.server.getPlayerList().getPlayers()) {
            list.add(player.getName());
        }
        return list;
    }

    private void send(EntityPlayerMP players, ByteBuf data) {
        if (players == null)
            return;
        try {
            runTask(() -> AllMusicForge.channel.sendTo(new PacketMessage(data), players));
        } catch (Exception e) {
            AllMusic.log.warning("§c数据发送发生错误");
            e.printStackTrace();
        }
    }

    private void writeString(ByteBuf buf, String data) {
        byte[] temp = data.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(temp.length)
                .writeBytes(temp);
    }
}
