package coloryr.allmusic.hud;

import coloryr.allmusic.AllMusic;
import coloryr.allmusic.hud.obj.HudPos;
import coloryr.allmusic.hud.obj.PosOBJ;
import coloryr.allmusic.hud.obj.SaveOBJ;
import coloryr.allmusic.music.api.SongInfo;
import coloryr.allmusic.music.lyric.LyricItem;
import coloryr.allmusic.music.play.PlayMusic;
import coloryr.allmusic.utils.Function;
import com.google.gson.Gson;

public class HudUtils {
    public static PosOBJ setHudPos(String player, String pos, String x, String y) {
        SaveOBJ obj = HudSave.get(player);
        if (obj == null)
            obj = AllMusic.getConfig().getDefaultHud().copy();
        HudPos pos1 = HudPos.valueOf(pos);
        PosOBJ posOBJ = new PosOBJ(0, 0);
        if (!Function.isInteger(x) && !Function.isInteger(y))
            return null;
        int x1 = Integer.parseInt(x);
        int y1 = Integer.parseInt(y);

        switch (pos1) {
            case lyric:
                posOBJ = obj.getLyric();
                break;
            case list:
                posOBJ = obj.getList();
                break;
            case info:
                posOBJ = obj.getInfo();
                break;
            case pic:
                posOBJ = obj.getPic();
        }
        posOBJ.setX(x1);
        posOBJ.setY(y1);
        switch (pos1) {
            case lyric:
                obj.setLyric(posOBJ);
                break;
            case list:
                obj.setList(posOBJ);
                break;
            case info:
                obj.setInfo(posOBJ);
                break;
            case pic:
                obj.setPic(posOBJ);
                break;
        }

        HudSave.add(player, obj);
        AllMusic.save();
        HudUtils.sendHudSave(player);
        return posOBJ;
    }

    public static void sendHudListData() {
        String info;
        if (PlayMusic.getSize() == 0) {
            info = AllMusic.getMessage().getHud().getNoList();
        } else {
            String now;
            StringBuilder list = new StringBuilder();
            for (SongInfo info1 : PlayMusic.getList()) {
                if (info1 == null)
                    continue;
                now = info1.getInfo();
                if (now.length() > AllMusic.getConfig().getMessageLimitSize())
                    now = now.substring(0, AllMusic.getConfig().getMessageLimitSize() - 1) + "...";
                list.append(now).append("\n");
            }
            info = AllMusic.getMessage().getHud().getList()
                    .replace("%Size%", String.valueOf(PlayMusic.getList().size()))
                    .replace("%List%", list.toString());
        }

        AllMusic.side.sendHudList(info);
    }

    private static String tranTime(int time) {
        int m = time / 60;
        int s = time - m * 60;
        return (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }

    public static void sendHudNowData() {
        String info;
        if (PlayMusic.nowPlayMusic == null) {
            info = AllMusic.getMessage().getHud().getNoMusic();
        } else {
            info = AllMusic.getMessage().getHud().getMusic()
                    .replace("%Name%", PlayMusic.nowPlayMusic.getName())
                    .replace("%AllTime%", tranTime(PlayMusic.musicAllTime))
                    .replace("%NowTime%", tranTime(PlayMusic.musicNowTime / 1000))
                    .replace("%Author%", PlayMusic.nowPlayMusic.getAuthor())
                    .replace("%Alia%", PlayMusic.nowPlayMusic.getAlia())
                    .replace("%Al%", PlayMusic.nowPlayMusic.getAl())
                    .replace("%Player%", PlayMusic.nowPlayMusic.getCall());
        }

        AllMusic.side.sendHudInfo(info);
    }

    public static void sendHudLyricData(LyricItem showobj) {
        String info;
        if (showobj == null) {
            info = AllMusic.getMessage().getHud().getNoLyric();
        } else {
            info = AllMusic.getMessage().getHud().getLyric()
                    .replace("%Lyric%", showobj.getLyric() == null ? "" : showobj.getLyric())
                    .replace("%Tlyric%", (showobj.isHaveT() && showobj.getTlyric() != null) ?
                            showobj.getTlyric() : "");
        }

        AllMusic.side.sendHudLyric(info);
    }

    public static boolean setHudEnable(String player, String pos) {
        SaveOBJ obj = HudSave.get(player);
        boolean a = false;
        if (obj == null) {
            obj = AllMusic.getConfig().getDefaultHud().copy();
            a = obj.isEnableInfo() && obj.isEnableList() && obj.isEnableLyric();
        } else {
            if (pos == null) {
                if (obj.isEnableInfo() && obj.isEnableList() && obj.isEnableLyric()) {
                    obj.setEnableInfo(false);
                    obj.setEnableList(false);
                    obj.setEnableLyric(false);
                    obj.setEnablePic(false);
                    a = false;
                } else {
                    obj.setEnableInfo(true);
                    obj.setEnableList(true);
                    obj.setEnableLyric(true);
                    obj.setEnablePic(true);
                    a = true;
                }
            } else {
                HudPos pos1 = HudPos.valueOf(pos);
                switch (pos1) {
                    case info:
                        obj.setEnableInfo(!obj.isEnableInfo());
                        break;
                    case list:
                        obj.setEnableList(!obj.isEnableList());
                        break;
                    case lyric:
                        obj.setEnableLyric(!obj.isEnableLyric());
                        break;
                    case pic:
                        obj.setEnablePic(!obj.isEnablePic());
                        break;
                }
            }
        }
        clearHud(player);
        HudSave.add(player, obj);
        AllMusic.save();
        HudUtils.sendHudSave(player);
        if (pos == null) {
            return a;
        } else {
            HudPos pos1 = HudPos.valueOf(pos);
            switch (pos1) {
                case info:
                    return obj.isEnableInfo();
                case list:
                    return obj.isEnableList();
                case lyric:
                    return obj.isEnableLyric();
                case pic:
                    return obj.isEnablePic();
            }
        }
        return false;
    }

    public static void clearHud() {
        AllMusic.side.clearHud();
    }

    public static void clearHud(String Name) {
        AllMusic.side.clearHud(Name);
    }

    public static void sendHudSave(String Name) {
        AllMusic.side.runTask(() -> {
            try {
                SaveOBJ obj = HudSave.get(Name);
                if (obj == null) {
                    obj = AllMusic.getConfig().getDefaultHud().copy();
                    HudSave.add(Name, obj);
                    AllMusic.save();
                }
                String data = new Gson().toJson(obj);
                AllMusic.side.send(data, Name, null);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        });
    }

    public static void reset(String name) {
        SaveOBJ obj = AllMusic.getConfig().getDefaultHud().copy();
        clearHud(name);
        HudSave.add(name, obj);
        AllMusic.save();
        HudUtils.sendHudSave(name);
    }

    public static boolean setPicSize(String name, String size) {
        SaveOBJ obj = HudSave.get(name);
        if (obj == null)
            obj = AllMusic.getConfig().getDefaultHud().copy();
        if (!Function.isInteger(size))
            return false;
        int size1 = Integer.parseInt(size);

        obj.setPicSize(size1);

        HudSave.add(name, obj);
        AllMusic.save();
        HudUtils.sendHudSave(name);
        return true;
    }
}
