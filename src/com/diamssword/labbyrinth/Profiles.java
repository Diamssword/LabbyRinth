package com.diamssword.labbyrinth;

import com.diamssword.labbyrinth.downloaders.PlayerProfileDownloader;
import com.diamssword.labbyrinth.downloaders.Utils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Profiles {

    private static boolean needRefresh=true;
    private static List<PlayerProfile> profiles= new ArrayList<>();
    private static List<Consumer<List<PlayerProfile>>> listeners=new ArrayList<>();
    private static PlayerProfile selectedProfile;

    public static Optional<PlayerProfile> getSelectedProfile() {
        if(selectedProfile ==null)
        {
            JSONObject ob=Utils.readCommonCache();
            if(ob.has("defaultProfile"))
            {
                String s= ob.getString("defaultProfile");
                Optional<PlayerProfile> op= profiles.stream().filter(v->v.uuid.equals(s)).findFirst();
                op.ifPresent(playerProfile -> selectedProfile = playerProfile);
                return op;
            }
        }
        return Optional.ofNullable(selectedProfile);
    }
    public static void setSelectedProfile(PlayerProfile profile) {

        selectedProfile=profile;
        Utils.setCommonCache(Utils.readCommonCache().put("defaultProfile",selectedProfile.uuid));
    }
    public static void registerUpdateListener(Consumer<List<PlayerProfile>> listener)
    {

        System.out.println("addingList");
        listeners.add(listener);
    }

    public static CompletableFuture<Optional<PlayerProfile>> getUser(String uuid)
    {
        return loadProfiles().thenApply(c->c.stream().filter(p->p.uuid.equals(uuid)).findFirst());
    }
    public static CompletableFuture<Optional<PlayerProfile>> getUserByEmail(String email)
    {
        return loadProfiles().thenApply(c->c.stream().filter(p->p.email.equals(email)).findFirst());
    }
    private static Process lastLoginProcess;
    public static CompletableFuture<Boolean> login(String email)
    {
        if(lastLoginProcess!=null)
            lastLoginProcess.destroy();
        Consumer<Process> call=p->{
            lastLoginProcess=p;
        };
        return CliInterface.login(email,call);

    }
    public static void setReloadNeeded()
    {
        needRefresh=true;

    }
    public static CompletableFuture<List<PlayerProfile>> loadProfiles()
    {
        if(!needRefresh)
            return CompletableFuture.supplyAsync(()->new ArrayList<>(profiles));
        else
        {
            needRefresh=false;
            try {
                profiles.clear();
                return CliInterface.getProfiles().thenApply(c->{
                    for(int i=0;i<c.length();i++)
                    {
                        JSONObject ob=c.getJSONObject(i);
                        profiles.add(new PlayerProfile(ob.getString("username"),ob.getString("uuid"),ob.getString("email")));
                    }
                    listeners.forEach(l->l.accept(new ArrayList<>(profiles)));
                    return new ArrayList<>(profiles);
                });
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class PlayerProfile{
        public final String username;
        public final String uuid;
        public final String email;
        public final PlayerProfileDownloader skin;
        PlayerProfile(String username, String uuid, String email)
        {
            this.username = username;
            this.uuid = uuid;
            this.email = email;
            this.skin=new PlayerProfileDownloader(username,uuid);
        }
    }

}
