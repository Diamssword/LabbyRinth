package com.diamssword.labbyrinth.view.browser;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.PacksManager;
import com.diamssword.labbyrinth.Profiles;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JavaBridge {
    public static JavaBridge instance;
    private final WebEngine engine;
    public JavaBridge(WebEngine engine)
    {
        this.engine = engine;
        instance=this;
        PacksManager.addUpdatedListener(lo->{
            System.out.println("ready"+lo);
            sendEvent("packsReady",lo);
        });
        PacksManager.addReadyListener(r->{
            JSONObject ob=new JSONObject();
            JSONArray arr=new JSONArray();
            PacksManager.getDisplayList().forEach(v->{
                JSONObject ob1=new JSONObject();
                ob1.put("value",v.getKey());
                ob1.put("name",v.getValue());
                arr.put(ob1);
            });
            String s=PacksManager.getPreferedPack();
            if(s!=null)
                ob.put("selected",s);
            ob.put("list",arr);

            sendEvent("packsList",ob);
        });
    }
    public void startGame()
    {
        if(Profiles.getSelectedProfile().isPresent())
            PacksManager.launch();
    }
    public void isPackLocked(int callback)
    {
        sendValue(callback,PacksManager.isLocked());

    }
    public void getPacks(int callback)
    {
        JSONObject ob=new JSONObject();
        JSONArray arr=new JSONArray();
        PacksManager.getDisplayList().forEach(v->{
            JSONObject ob1=new JSONObject();
            ob1.put("value",v.getKey());
            ob1.put("name",v.getValue());
            arr.put(ob1);
        });
        String s=PacksManager.getPreferedPack();
        if(s!=null)
            ob.put("selected",s);
        ob.put("list",arr);

        sendValue(callback,ob);
    }
    public void selectPack(String name)
    {
        PacksManager.setPreferedPack(name);
    }
    public void selectProfile(String uuid)
    {
        Profiles.getUser(uuid).thenAccept(v->{
            v.ifPresent(Profiles::setSelectedProfile);
        });
    }
    public void getProfiles(int callback)
    {
        Profiles.loadProfiles().thenAccept((v)->{
            JSONObject ob=new JSONObject();
            JSONArray arr=new JSONArray();
            v.forEach(v1->{
                JSONObject ob1=new JSONObject();
                ob1.put("uuid",v1.uuid);
                ob1.put("name",v1.username);
                arr.put(ob1);
            });
            ob.put("list",arr);
            Profiles.getSelectedProfile().ifPresentOrElse((p)->{
                ob.put("selected",p.uuid);
                sendValue(callback,ob);
            },()->{
                sendValue(callback,ob);
            });
        });
    }
    public void getSkin(String uuid,int callback)
    {
        Profiles.getUser(uuid).thenAccept((p)->{
            p.ifPresentOrElse(p1->{
                p1.skin.load().thenAccept(sk->sk.getHead(64).thenAccept(i->{
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    ImageIO.write(i, "png", os);
                    sendValue(callback, Base64.getEncoder().encodeToString(os.toByteArray()));
                }catch (Exception e)
                {
                    sendValue(callback,"none");
                }
            }));
            },()->{
                sendValue(callback,"none");
            });
        });
    }
    public void getPackLogo(String name,int callback)
    {
        File f=new File(LauncherVariables.getPackFolder(name),"logo.png");
        if(f.exists() && f.isFile()) {
            try {
                sendValue(callback, Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(f)));
            }catch (Exception e)
            {
                sendValue(callback,"none");
            }
        }
    }
    public void sendValue(int callBack,Object value)
    {
        Platform.runLater(() -> {
            String url= URLEncoder.encode(value.toString().replaceAll("\\+","&plus;"), StandardCharsets.UTF_8);
            engine.executeScript("JavaAsyncResult('"+callBack+"','"+ url+"')");
        });

    }
    public void sendEvent(String event,Object value)
    {
        Platform.runLater(() -> {
            String url= URLEncoder.encode(value.toString().replaceAll("\\+","&plus;"), StandardCharsets.UTF_8);
            engine.executeScript("if(window.JavaEvent)JavaEvent('"+event+"','"+ url+"');");
        });

    }
}
