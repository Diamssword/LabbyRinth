package com.diamssword.labbyrinth.downloaders;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.view.Server;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ViewLoader {

static File path=new File(LauncherVariables.gameDirectory,"view");
    public static void load() throws IOException {
        path=new File(LauncherVariables.gameDirectory,"view");
        if(LauncherVariables.devMode)
            FileUtils.deleteDirectory(path);
        if(!new File(path,"index.html").exists())
        {
            System.out.println("no view found, unziping from jar...");
            unzipLocal();
        }
        Server.start(new String[0]);
    }
    private static void unzipLocal() throws IOException {
        unzip(ViewLoader.class.getResource("/view/dist.zip").openStream());
    }
    private static void unzip(InputStream in) throws IOException {
        Log.setProgress("Unziping view...");
        File f1 = new File(path, "temp.zip");
        FileUtils.copyInputStreamToFile(in, f1);
        try {
            ZipFile zip = new ZipFile(f1);
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
            f1.delete();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Le .zip est corrompu");
        }
    }
}
