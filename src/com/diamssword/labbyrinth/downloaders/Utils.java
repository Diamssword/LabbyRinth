package com.diamssword.labbyrinth.downloaders;

import com.diamssword.labbyrinth.LauncherVariables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utils {
    public static JSONObject getJson(URL url) throws IOException {
        String json = IOUtils.toString(url, Charset.forName("UTF-8"));

        return new JSONObject(json);
    }
    public static BufferedImage getImage(URL url) throws IOException {
        return ImageIO.read(url);
    }
    public static JSONObject jsonFromBase64(String encoded) throws IllegalArgumentException  {

        return new JSONObject(new String(Base64.getDecoder().decode(encoded)));
    }
    public static void setCommonCache(JSONObject common)
    {
        File f=new File(LauncherVariables.gameDirectory, "caches/commons.json");
        try {
            FileUtils.write(f, common.toString(), StandardCharsets.UTF_8);
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static JSONObject readCommonCache()
    {
      return readCache("commons");
    }
    public static JSONObject readCache(String name)
    {
        File f=new File(LauncherVariables.gameDirectory, "caches/"+name+".json");
        if(f.exists() && f.isFile())
        {
            try {
                return new JSONObject(FileUtils.readFileToString(f));
            }catch (IOException e)
            {
                return new JSONObject();
            }
        }
        return new JSONObject();
    }
}
