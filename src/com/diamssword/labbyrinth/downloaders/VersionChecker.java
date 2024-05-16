package com.diamssword.labbyrinth.downloaders;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.Main;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.utils.KeyPair;
import com.diamssword.labbyrinth.utils.VersionNumberComparator;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class VersionChecker {


    public static CompletableFuture<KeyPair> getLatestVersion(String pack) throws IOException
    {
        return loadVersionFile().thenApply((js)->{
            if(js!=null && js.has(pack))
            {
                JSONObject ob=js.getJSONObject(pack).getJSONObject("latest");
                return new KeyPair(ob.getString("version"),ob.getString("path"));
            }
            return null;
        });
    }
    public static boolean shouldUpdate(String latestVersion,String currentVersion)
    {
            return VersionNumberComparator.getInstance().compare(latestVersion,currentVersion)>0;
    }
    public static boolean downloadUpdate(String dlPath, File path) throws IOException {

        Log.setProgress("Téléchargement de "+dlPath);
       return FileDownloader.downloadFile(LauncherVariables.updateUrl +"/packs/"+dlPath,path,Log::setProgress);
    }
    public static CompletableFuture<JSONObject> loadVersionFile() throws IOException
    {
        return CompletableFuture.supplyAsync(()->{
            try {
                return Utils.getJson(new URL(LauncherVariables.updateUrl));
            } catch (IOException e) {
                Main.logger.warning(e.toString());
              return null;
            }
        });
    }
    private static File ogJar;
    private static File newJar;
   /* public static void updateCleanup()
    {
        if(ogJar !=null && newJar != null)
        {
            new Thread(()->{
            try {
                FileUtils.copyFile(newJar,ogJar);
                newJar.delete();
            } catch (IOException e) {
                Main.logger.warning(e.toString());
            }
            }).start();
        }
    }
    */
    public static void updateLauncher()
    {
        try {
            File f= new File(VersionChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File f1=new File(f.getParent(),"launcher_update.jar");
            if(f.isFile())
            {
                VersionChecker.getLatestVersion("launcher").thenApply(v->{

                    if(v!=null &&  VersionChecker.shouldUpdate(v.getKey(),LauncherVariables.version))
                    {
                        try {
                            if(VersionChecker.downloadUpdate(v.getValue(),f1))
                            {
                                ogJar=f;
                                newJar=f1;
                                Utils.setCommonCache(Utils.readCommonCache().put("need_refresh",true));
                                return true;
                            }
                            return false;
                        } catch (IOException e) {
                            Main.logger.warning(e.toString());
                            return false;
                        }
                    }
                    else return f.exists() && f.isFile();
                });
            }
        } catch (URISyntaxException | IOException e) {
            Main.logger.warning(e.toString());
        }
    }
}
