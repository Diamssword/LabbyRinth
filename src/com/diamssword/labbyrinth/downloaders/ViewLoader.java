package com.diamssword.labbyrinth.downloaders;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.utils.KeyPair;
import com.diamssword.labbyrinth.view.Server;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ViewLoader {

static File path=new File(LauncherVariables.gameDirectory,"view");
    public static void load() throws IOException {
        path=new File(LauncherVariables.gameDirectory,"view");
        var cache=Utils.readCommonCache();
        if(LauncherVariables.devMode )  {
            Utils.setCommonCache(cache);
            FileUtils.deleteDirectory(path);
           Log.setProgress("dev mode: unziping view from local...",0);
           unzipLocal();
        }
        else if(!new File(path,"index.html").exists())
        {
            Log.setProgress("No view found, downloading...",0);
            onlineLoad(true);
        }
        else
        {
            onlineLoad(false);
        }
        Server.start(new String[0]);
    }
    public static boolean onlineLoad(boolean force)
    {
        try {
           KeyPair v= VersionChecker.getLatestVersion(LauncherVariables.ui_name).get();
           if(v !=null)
           {
               if(force || VersionChecker.shouldUpdate(v.getKey(),Utils.getUIVersion()))
               {
                   Log.setProgress("Téléchargement de l'UI en cours...",0);
                   File f1 = new File(path.getParent(), "temp.zip");
                   VersionChecker.downloadUpdate(v.getValue(),f1);
                   FileUtils.deleteDirectory(path);
                   unzip(f1);
                   f1.delete();
                   var cache=Utils.readCommonCache();
                   cache.put("ui_version",v.getKey());
                   Utils.setCommonCache(cache);
                   return true;
               }
           }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }
    private static void unzipLocal() throws IOException {
        unzip(new File("dist.zip"));
       // unzip(ViewLoader.class.getResource("/view/dist.zip").openStream());
    }
    private static void unzip(File f) throws IOException {
        Log.setProgress("Unziping view...");
        try {
            ZipFile zip = new ZipFile(f);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    try (InputStream inputStream = zip.getInputStream(entry)) {
                        File path1 = new File(path, name);
                        path1.getParentFile().mkdirs();
                        FileDownloader.streamToFile(inputStream, path1, Log::setProgress);
                        Log.setProgress(100);
                    } catch (IOException e) {
                        System.err.println("Failed to extract " + name);
                        e.printStackTrace();
                    }

                }
            }
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Le .zip est corrompu");
        }
    }
}
