package com.diamssword.labbyrinth;

import com.diamssword.labbyrinth.downloaders.MrpackReader;
import com.diamssword.labbyrinth.downloaders.Utils;
import com.diamssword.labbyrinth.downloaders.VersionChecker;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.utils.KeyPair;
import com.diamssword.labbyrinth.utils.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class PacksManager {
    private static List<PackInstance> packs = new ArrayList<>();
    private static boolean isLocked=false;
    public static boolean isLocked()
    {
        return isLocked;
    }
    private static final List<Consumer<List<PackInstance>>> readyListeners=new ArrayList<>();
    private static Vector<KeyPair> packsDisplay=new Vector<>();
    private static final List<Consumer<Boolean>> updatedListeners=new ArrayList<>();
    public static void load() {
        JSONObject cache = Utils.readCommonCache();
        if (cache.has("packs")) {
            cache.getJSONArray("packs").forEach(p -> {
                PackInstance inst=loadPackInstance(p.toString());
                if(inst!=null)
                    packs.add(inst);
                if(getPreferedPack()==null)
                {
                    setPreferedPack(packs.get(0).name);
                }
                else
                    updatePreferredPack();
            });
        } else {
            PackInstance inst=loadPackInstance(LauncherVariables.default_pack);
            if(inst!=null) {
                packs.add(inst);
                setPreferedPack(inst.name);
                Utils.setCommonCache(cache.put("packs",new JSONArray().put(inst.name)));
            }

        }
        packsDisplay=new Vector<KeyPair>(packs.stream().map(v->new KeyPair(v.name, TextUtils.capitalizeWords(v.name().replaceAll("-"," ").replaceAll("_"," ")))).toList());
        readyListeners.forEach(c->{
            c.accept(packs);
        });
    }
    public static Vector<KeyPair> getDisplayList()
    {
        return packsDisplay;
    }
    public static KeyPair getSelectedDisplay()
    {
        return packsDisplay.stream().filter(v->v.getKey().equals(getPreferedPack())).findFirst().orElse(packsDisplay.get(0));
    }
    public static void addReadyListener(Consumer<List<PackInstance>> list)
    {
        readyListeners.add(list);
    }
    public static void addUpdatedListener(Consumer<Boolean> list)
    {
        updatedListeners.add(list);
    }
    public static void setPreferedPack(String packName)
    {
        Utils.setCommonCache(Utils.readCommonCache().put("defaultPack",packName));
        if(!isLocked())
            updatePreferredPack();


    }
    public static void launch()
    {
        JSONObject pack=Utils.readCache(getPreferedPack());
        if(pack.has("dependencies"))
        {
            JSONObject infos=pack.getJSONObject("dependencies");
            GameInstance.LoaderType type= GameInstance.LoaderType.vanilla;
            String subVersion=null;
            if(infos.has("fabric-loader")) {
                type = GameInstance.LoaderType.fabric;
                subVersion=infos.getString("fabric-loader");
            }
            GameInstance inst=new GameInstance(getPreferedPack(),type,infos.getString("minecraft"),subVersion);
            new Thread(()-> inst.start(Profiles.getSelectedProfile().get().email)).start();

        }

    }
    public static void updatePreferredPack()
    {
        isLocked=true;
        updatedListeners.forEach(c->c.accept(isLocked));
        getPack(getPreferedPack()).ifPresentOrElse(p->{
            if((p.version==null && p.latest!=null) ||(p.latest!=null && VersionChecker.shouldUpdate(p.latest.getKey(),p.version)))
            {
                new Thread(()->{
                try {
                    MrpackReader reader=prepareUpdate(p);
                    reader.updateOrInstall(p.name, p.latest.getKey());
                    packs.remove(p);
                    packs.add(new PackInstance(p.name,p.latest.getKey(),p.latest,false));

                    isLocked=false;
                    updatedListeners.forEach(c->c.accept(isLocked));
                    reader.file.delete();
                }catch (IOException e){
                    Main.logger.warning(e.toString());
                    isLocked=false;
                    updatedListeners.forEach(c->c.accept(isLocked));

                }
                }).start();
            }
            else {
                isLocked = false;
                updatedListeners.forEach(c->c.accept(isLocked));
            }
        },()->{isLocked=false; updatedListeners.forEach(c->c.accept(isLocked));});
    }
    public static Optional<PackInstance> getPack(String name)
    {
        return packs.stream().filter(v->v.name.equals(name)).findFirst();
    }
    public static String getPreferedPack()
    {
        if(Utils.readCommonCache().has("defaultPack"))
        {
            String pack=Utils.readCommonCache().getString("defaultPack");
            if(packs.stream().anyMatch(b->b.name.equals(pack)))
                return pack;
        }
        return null;
    }
    private static PackInstance loadPackInstance(String name) {
        JSONObject ob = Utils.readCache(name);

        try {
            KeyPair pair = VersionChecker.getLatestVersion(name).get();
            if (pair != null) {
                if (!ob.has("versionId")) {
                    return new PackInstance(name, null, pair, true);
                } else {
                    return new PackInstance(name, ob.getString("versionId"), pair, VersionChecker.shouldUpdate(pair.getKey(), ob.getString("versionId")));
                }


            }
            return new PackInstance(name, null, null, false);
        } catch (IOException | InterruptedException | ExecutionException e) {
            return null;
        }

    }

    private static MrpackReader prepareUpdate(PackInstance pack) throws IOException {
        File temp = File.createTempFile(pack.name, ".mrpack");
        VersionChecker.downloadUpdate(pack.latest.getValue(), temp);
        return new MrpackReader(temp);
    }

    public static record PackInstance(String name,String version,KeyPair latest,boolean shouldUpdate){};
}
