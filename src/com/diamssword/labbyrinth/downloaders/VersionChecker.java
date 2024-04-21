package com.diamssword.labbyrinth.downloaders;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.Main;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.utils.KeyPair;
import com.diamssword.labbyrinth.utils.VersionNumberComparator;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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

}
