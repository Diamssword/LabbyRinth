package com.diamssword.labbyrinth.downloaders;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class PlayerProfileDownloader {
    private String username;
    private String id;
    private String skinURL;
    private String capeURL;
    private BufferedImage skin;
    public PlayerProfileDownloader(String name,String uuid)
    {
        this.username=name;
        this.id=uuid;
    }
    private PlayerProfileDownloader syncload()
    {
        if(id==null)
            getUUID();
        if(skin==null)
            getProfile();
        return this;
    }
    public CompletableFuture<PlayerProfileDownloader> load()
    {
        return CompletableFuture.supplyAsync(this::syncload);
    }
    public CompletableFuture<BufferedImage> getHead(int size)
    {
        return CompletableFuture.supplyAsync(()->{
            try {
                return getHeadInternal(size);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static PlayerProfileDownloader fromName(String s)
    {
        return new PlayerProfileDownloader(s,null);
    }
    public static PlayerProfileDownloader fromID(String s)
    {
        return new PlayerProfileDownloader(null,s);
    }
    private void getUUID()
    {
        try {
            JSONObject js=Utils.getJson(new URL("https://api.mojang.com/users/profiles/minecraft/"+username));
            if(js.has("name") && js.has("id"))
            {
                this.username=js.getString("name");
                this.id=js.getString("id");
            }
        } catch (IOException e) {

        }
    }
    private void getProfile()
    {
        if(this.id!=null) {
            try {
                JSONObject js = Utils.getJson(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + this.id));
                if (js.has("name")) {
                    this.username = js.getString("name");
                }
                    JSONArray arr=js.getJSONArray("properties");
                String base64Texture=null;
                for(int i=0;i<arr.length();i++)
                {
                    if(arr.getJSONObject(i).has("name") &&arr.getJSONObject(i).getString("name").equals("textures"))
                    {
                        base64Texture=arr.getJSONObject((i)).getString("value");
                        break;
                    }
                }
                if(base64Texture!=null) {
                    JSONObject ob1=Utils.jsonFromBase64(base64Texture);
                    if(ob1.has("textures"))
                    {
                        this.skinURL=ob1.getJSONObject("textures").getJSONObject("SKIN").getString("url");
                        if(ob1.getJSONObject("textures").has("CAPE"))
                            this.capeURL=ob1.getJSONObject("textures").getJSONObject("CAPE").getString("url");
                        this.skin=getSkin();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsername() {
        return username;
    }

    private BufferedImage getSkin() throws IOException {
        if(skin!=null)
            return skin;
        return Utils.getImage(new URL(this.skinURL));
    }
    private BufferedImage getHeadInternal(int size) throws IOException {
        BufferedImage skin=getSkin();
        BufferedImage head=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D ctx=head.createGraphics();
        ctx.drawImage(skin,0,0,size,size,8,8,16,16,null);
        ctx.drawImage(skin, 0, 0, size, size, 40, 8, 48, 16, null);
        ctx.dispose();
        return head;
    }
    public String getId() {
        return id;
    }

    public String getSkinURL() {
        return skinURL;
    }

    public String getCapeURL() {
        return capeURL;
    }
}
