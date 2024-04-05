package com.diamssword.labbyrinth.downloaders;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.utils.MD5Checker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MrpackReader {
    public final File file;
    private JSONObject index;
    public MrpackReader(File file)
    {
        this.file=file;
        this.index=readPack();

    }
    public void updateOrInstall(@Nullable String versionName,@Nullable String versionId)
    {
        File packFolder=LauncherVariables.getPackFolder(versionName==null?index.getString("name"):versionName);
        try {
            clearOldPack(packFolder);
        }catch (IOException e)
        {
            System.err.println("Failed to clear old pack");
        }

        installPack(packFolder);
        try {
            Log.setProgress("Sauvegarde des caches",0);
            saveCache(packFolder,versionId);
            Log.setProgress(100);
        } catch (IOException e) {
            System.err.println("Failed to save cache");
        }
        Log.setProgress("Pack à jour!",100);
    }

    private void clearOldPack(File packRoot) throws IOException {
        String s=FileUtils.readFileToString(new File(LauncherVariables.gameDirectory,"caches/"+packRoot.getName()+".json"));
        JSONObject ob=new JSONObject(s);
        JSONArray arr=ob.getJSONArray("files");
        for(int i=0;i< arr.length();i++)
        {
            try {
                deleteOldFile(packRoot, arr.getJSONObject(i));
            }catch (Exception e)
            {
                System.err.println("Failed to delete "+arr.getJSONObject(i).getString("path"));
            }
        }

    }
    private void deleteOldFile(File pack,JSONObject infos) throws Exception {
        File p=new File(pack,infos.getString("path"));

        Log.setProgress("Suppression de "+infos.getString("path"),10);
        if(p.exists() && p.isFile())
        {
           p.delete();
        }
        else if(infos.has("sha512")){
            for (File f1 : p.getParentFile().listFiles()) {
                if(MD5Checker.getMD5Checksum(f1).equals(infos.getString("sha512")))
                {
                    f1.delete();
                    break;
                }
            }

        }
        Log.setProgress(100);
    }
    public void installPack(File packRoot)
    {
        if( index !=null && index.has("files"))
        {
            JSONArray arr=index.getJSONArray("files");
            for(int i=0;i< arr.length();i++)
            {
                JSONObject file=arr.getJSONObject(i);
                System.out.print("Downloading "+file.getString("path") +"...");
                try {
                    downloadFile(packRoot,file);
                    System.out.println("Done!");
                }catch (IOException e)
                {
                    System.err.println("Download failed for file: "+file.getString("path"));
                    e.printStackTrace();
                }
            }
            addOverrides(packRoot);
        }
    }
    private void saveCache(File pack,@Nullable String versionId) throws IOException {
        File save=new File(LauncherVariables.gameDirectory,"caches/"+pack.getName()+".json");
        save.getParentFile().mkdirs();
        JSONObject ob=new JSONObject();
        JSONArray dest=new JSONArray();
        JSONArray src=this.index.getJSONArray("files");
        for(int i=0;i<src.length();i++)
        {
            JSONObject r=new JSONObject();
            r.put("path",src.getJSONObject(i).getString("path"));
            r.put("sha512",src.getJSONObject(i).getJSONObject("hashes").getString("sha512"));
            dest.put(r);
        }
        addOverrideToCache(pack,dest);
        ob.put("files",dest);
        ob.put("name",index.getString("name"));
        ob.put("versionId",versionId !=null?versionId:index.getString("versionId"));
        ob.put("dependencies",index.getJSONObject("dependencies"));
        FileWriter w=new FileWriter(save);
        w.write(ob.toString());
        w.close();
    }

    private void addOverrideToCache(File packRoot,JSONArray array)
    {
        try {
            ZipFile zip = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    if (entry.getName().startsWith("overrides/")) {
                        JSONObject js=new JSONObject();
                        String name=entry.getName().replaceFirst("overrides/","");
                        js.put("path",name);
                        try {
                            js.put("sha512",MD5Checker.getMD5Checksum(new File(packRoot, name)));
                            array.put(js);
                        }catch (Exception ignored){
                            array.put(js);
                        }
                    }
                }
            }
        }catch (IOException e)
        {
            System.err.println("Le .mrpack est corrompu");
        }
    }
    private void addOverrides(File packRoot)
    {
        try {
            ZipFile zip = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    if (entry.getName().startsWith("overrides/")) {
                        String name=entry.getName().replaceFirst("overrides/","");
                        Log.setProgress(45);
                        Log.setProgress("Extraction de "+name);
                    try (InputStream inputStream = zip.getInputStream(entry)) {
                        File path=new File(packRoot,name);
                        path.getParentFile().mkdirs();
                        FileDownloader.streamToFile(inputStream,path,Log::setProgress);
                        //IOUtils.copy(inputStream,new FileOutputStream(path));
                        Log.setProgress(100);
                    }
                    catch (IOException e)
                    {
                        System.err.println("Failed to extract "+name);
                        e.printStackTrace();
                    }
                    }
                }
            }
        }catch (IOException e)
        {
            System.err.println("Le .mrpack est corrompu");
        }
    }
    private void downloadFile(File packRoot,JSONObject fileInfos) throws IOException {
        if(fileInfos.has("path") && fileInfos.has("downloads"))
        {
            JSONArray dls=fileInfos.getJSONArray("downloads");

            if(!dls.isEmpty()) {
                File path = new File(packRoot, fileInfos.getString("path"));
                path.getParentFile().mkdirs();
                Log.setProgress("Téléchargement de "+fileInfos.getString("path"));
                FileDownloader.downloadFile(dls.getString(0),path, Log::setProgress);
               // IOUtils.copy(new URL(dls.getString(0)),path);
                try {
                    if(fileInfos.has("hashes"))
                    {
                        if(!fileInfos.getJSONObject("hashes").getString("sha512").equals(MD5Checker.getMD5Checksum(path)))
                        {
                            System.err.println("BAD sha512 for file " + path);
                            path.delete();
                        }
                    }

                }catch (Exception e)
                {
                    System.err.println("BAD sha512 for file " + path);
                    path.delete();
                }

            }

        }
    }

    public JSONObject readPack() {
        try {
            ZipFile zip = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // Check if entry is a directory
                if (!entry.isDirectory()) {
                    if (entry.getName().equals("modrinth.index.json")) {
                    try (InputStream inputStream = zip.getInputStream(entry)) {
                            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                            String s = r.lines().collect(Collectors.joining("\n"));
                            try {
                                return new JSONObject(s);

                            } catch (JSONException e) {
                                return null;
                            }
                        }
                    }
                }
            }
        }catch (IOException e)
        {
            System.err.println("Le .mrpack est corrompu");
        }
        return null;
    }
}
