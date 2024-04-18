package com.diamssword.labbyrinth.view.browser;

import com.diamssword.labbyrinth.Profiles;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JavaBridge {
    private final WebEngine engine;
    public JavaBridge(WebEngine engine)
    {
        this.engine = engine;
    }
    public void startGame()
    {

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
    public void sendValue(int callBack,Object value)
    {
        Platform.runLater(() -> {
            String url= URLEncoder.encode(value.toString().replaceAll("\\+","&plus;"), StandardCharsets.UTF_8);
            engine.executeScript("AndroidAsyncResult('"+callBack+"','"+ url+"')");
        });

    }
}
