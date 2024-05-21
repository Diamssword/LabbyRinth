package com.diamssword.labbyrinth.downloaders;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.Main;
import com.diamssword.labbyrinth.PacksManager;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.utils.MD5Checker;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MrpackReader {
    public final File file;
    private JSONObject index;
    public MrpackReader(File file) throws IOException {
        this.file=file;
        this.index=readPack();

    }
    public void updateOrInstall(@Nullable String versionName,@Nullable String versionId)
    {
        File packFolder=LauncherVariables.getPackFolder(versionName==null?index.getString("name"):versionName);

        installPack(packFolder);
        try {
            Log.setProgress("Sauvegarde des caches",0);
            saveCache(packFolder,versionId);
            Log.setProgress(100);
            PacksManager.copyAuthFileToPacks();
        } catch (IOException e) {
            System.err.println("Failed to save cache");
        }
        Log.setProgress("Pack à jour!",100);
    }

    /**
     * Build an index of the files present on the system or clear them:
     * If a file has the right name and the right md5 as declared in the cache file, it is added to the map and marked for deletion
     * Else it is deleted from the system
     * @param packRoot the root file of the pack
     * @return a map of FileObject with the MD5 as key
     */
    private Map<String,FileObject> buildOldPackIndex(File packRoot)
    {
        Map<String,FileObject> res=new HashMap<>();
        try {
            String s = FileUtils.readFileToString(new File(LauncherVariables.gameDirectory, "caches/" + packRoot.getName() + ".json"));
            JSONObject ob = new JSONObject(s);
            JSONArray arr = ob.getJSONArray("files");
            for (int i = 0; i < arr.length(); i++) {
                try {
                    var ob1=arr.getJSONObject(i);
                    String f=findOldFile(packRoot,ob1);
                    if(f!=null) {
                        String md=ob1.getString("sha512");
                        res.put(md,new FileObject(f,md).markDelete(true));
                    }
                } catch (Exception e) {
                    System.err.println("Failed to index " + arr.getJSONObject(i).getString("path"));
                }
            }
        }catch (IOException ignored){}
        return res;
    }

    /**
     * check if a file as the right path and MD5 and return the path
     * Or delete it if something is wrong
     */
    private String findOldFile(File pack,JSONObject infos) throws Exception {
        File p=new File(pack,infos.getString("path"));
        if(p.exists() && p.isFile())
        {
            if(infos.has("sha512"))
            {
                if(MD5Checker.getMD5Checksum(p).equals(infos.getString("sha512")))
                    return infos.getString("path");
            }

        }
        deleteOldFile(pack,infos);
        return null;
    }

    /**
     * Delete a file by first trying to find it by the given path
     * If this fails, it scans the whole folder for a file with the right MD5
     * This prevents the mod folder from breaking if a file as been renamed
     */
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

    /**
     * Install a new version of the pack and delete the old version
     * First it iterate over all new files to download
     * For each file it check if the same file is already present on the system (from the old version)
     * If not it download it
     * Then it clear all the files marked as old version
     * Then it add the "overrides" folder (wich does not support the checking of old files ATM)
     */
    public void installPack(File packRoot)
    {
        Map<String, FileObject> oldIndex=buildOldPackIndex(packRoot);
        if( index !=null && index.has("files"))
        {
            JSONArray arr=index.getJSONArray("files");
            for(int i=0;i< arr.length();i++)
            {
                JSONObject file=arr.getJSONObject(i);
                System.out.print("Downloading "+file.getString("path") +"...");
                try {
                    downloadFile(packRoot,file,oldIndex);
                    System.out.println("Done!");
                }catch (IOException e)
                {
                    System.err.println("Download failed for file: "+file.getString("path"));
                    e.printStackTrace();
                }
            }
            oldIndex.values().forEach(v->{
                if(v.shouldDelete())
                {
                    Log.setProgress("Suppression de "+v.path,10);
                    Main.logger.info("Deleting "+v.path);
                    new File(packRoot,v.path).delete();
                }
            });
            Log.setProgress(100);
            addOverrides(packRoot);
        }
    }

    /**
     * Create the cache file for this version of the pack
     * Save all files added and their MD5
     * This allow the future version to compare and clear old pack more efficiently
     */
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

    /**
     * Unzip folder "overrides" from the new pack
     * This folder is used for assets wich don't have a cdn link on modrinth (ressource packs, logos, unreferenced mods...)
     * @param packRoot
     */
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

    /**
     * Check first if a file is already present on the system from an older pack version
     * If it is, it mark the file to be NOT deleted
     * If not it download the new file
     */
    private void downloadFile(File packRoot,JSONObject fileInfos,Map<String,FileObject> oldIndex) throws IOException {
        if(fileInfos.has("path") && fileInfos.has("downloads"))
        {
            JSONArray dls=fileInfos.getJSONArray("downloads");

            if(!dls.isEmpty()) {
                if(fileInfos.has("hashes"))
                {
                    var sha=fileInfos.getJSONObject("hashes").getString("sha512");
                    if(oldIndex.containsKey(sha))
                    {
                        Log.setProgress("Mod déja présent : "+fileInfos.getString("path"),100);
                        var f=oldIndex.get(sha);
                        if(f.path.equals(fileInfos.getString("path")))
                        {
                            f.markDelete(false);
                            return;
                        }
                        else {
                            var found=oldIndex.values().stream().filter(v->v.path.equals(fileInfos.getString("path"))).findFirst();
                            found.ifPresent(fileObject -> fileObject.markDelete(false));
                        }
                    }
                }
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

    public JSONObject readPack() throws IOException {
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
            throw e;
        }
        return null;
    }

    public static class FileObject
    {
        public final String path;
        public final String sha;
        private boolean shouldDelete=false;
        public FileObject(String path,String expectedSHA)
        {
            this.sha=expectedSHA;
            this.path=path;
        }
        public FileObject markDelete(boolean delete)
        {
            this.shouldDelete=delete;
            return this;
        }

        public boolean shouldDelete() {
            return shouldDelete;
        }
    }
}
