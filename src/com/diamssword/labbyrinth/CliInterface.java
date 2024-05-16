package com.diamssword.labbyrinth;

import com.diamssword.labbyrinth.downloaders.Utils;
import com.diamssword.labbyrinth.downloaders.VersionChecker;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class CliInterface {
    public static final Logger logger = Logger.getLogger(CliInterface.class.getName());
    public static CompletableFuture<Boolean> verifyCLI() throws IOException
    {
        File f=new File(LauncherVariables.gameDirectory,"portablemc.exe");
        f.getParentFile().mkdirs();
        JSONObject cache=Utils.readCommonCache();
        if(cache.has("portablemc_version"))
        {
            return VersionChecker.getLatestVersion("portablemc").thenApply(v->{

               if(v!=null && (!f.exists() || !f.isFile()) || (v!=null &&  VersionChecker.shouldUpdate(v.getKey(),cache.getString("portablemc_version"))))
               {
                   try {
                       if(VersionChecker.downloadUpdate(v.getValue(),f))
                       {
                           Utils.setCommonCache(Utils.readCommonCache().put("portablemc_version",v.getKey()));
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
        else
            return updateCLI(f);
    }
    private static CompletableFuture<Boolean> updateCLI(File f) throws IOException
    {
        return VersionChecker.getLatestVersion("portablemc").thenApply(v->{
            if(v!=null)
            {
                try {
                    if(VersionChecker.downloadUpdate(v.getValue(),f))
                    {
                        Utils.setCommonCache(Utils.readCommonCache().put("portablemc_version",v.getKey()));
                        return true;
                    }
                    return false;
                } catch (IOException e) {
                    logger.warning(e.toString());
                    return false;
                }
            }
            return false;
        });
    }
    public static CompletableFuture<JSONArray> getProfiles() throws IOException, InterruptedException {

            return executeOpenMC("--output","machine","show","auth").thenApply(r->{
                JSONArray arr=new JSONArray();
                if(r.result !=null)
                {
                    for (String s : r.result.split("\n")) {
                        if(s.startsWith("row:") && !s.startsWith("row:Type"))
                        {
                            JSONObject ob= new JSONObject();
                            String[] parts=s.split(",");
                            if(parts.length>=4)
                            {
                                ob.put("type",parts[0].replace("row:","").strip());
                                ob.put("email",parts[1].strip());
                                ob.put("username",parts[2].strip());
                                ob.put("uuid",parts[3].strip());
                            }
                            arr.put(ob);
                        }
                    }
                }
                return arr;
            });

    }
    public static CompletableFuture<Boolean> login(String username,Consumer<Process> callback)
    {
        try {
            return executeOpenMC(callback,"--output","machine","login",username).thenApply(r->{

                if(r.error !=null)
                {
                    return false;
                }
                else {
                    String[] sp =r.result.split("\n");
                    for(String s : sp)
                    {
                        if(s.startsWith("task:FAILED"))
                            return false;
                        else if(s.startsWith("task:SUCCESS"))
                            return true;
                    }
                    return true;
                }
            });
        } catch (IOException | InterruptedException e) {
            logger.warning(e.toString());
        }
        return CompletableFuture.supplyAsync(()->false);
    }
    public static CompletableFuture<Boolean> logout(String username)
    {
        try {
            return executeOpenMC("--output","machine","logout",username).thenApply(r->{
                if(r.error !=null)
                {
                    return false;
                }
                else {
                    String[] sp =r.result.split("\n");
                    for(String s : sp)
                    {
                        if(s.startsWith("task:FAILED"))
                            return false;
                        else if(s.startsWith("task:OK"))
                            return true;
                    }
                    return true;
                }
            });
        } catch (IOException | InterruptedException e) {
            logger.warning(e.toString());
        }
        return CompletableFuture.supplyAsync(()->false);
    }
    public static CompletableFuture<ProcessResult> executeOpenMC(String... args) throws IOException, InterruptedException {
        return executeOpenMC(null,args);
    }
    public static CompletableFuture<ProcessResult> executeOpenMC(@Nullable Consumer<Process> callback,String... args) throws IOException, InterruptedException {
        List<String> ls=new ArrayList<>();
        ls.add(new File(LauncherVariables.gameDirectory,"portablemc.exe").getAbsolutePath());
        ls.add("--main-dir");
        ls.add(LauncherVariables.gameDirectory);
        ls.addAll(List.of(args));
        return executeCmd(ls,callback);
    }
    public static CompletableFuture<ProcessResult> executeCmd(List<String> args,@Nullable Consumer<Process> callback) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(args);

        Process process=builder.start();
        InputStream out=process.getInputStream();
        InputStream outerr=process.getErrorStream();
        if(callback!=null)
        {
            callback.accept(process);
        }
        Reader reader = new BufferedReader(new InputStreamReader(out, StandardCharsets.UTF_8));
        Reader reader1 = new BufferedReader(new InputStreamReader(outerr, StandardCharsets.UTF_8));
        return CompletableFuture.supplyAsync(()->{

            try {
                process.waitFor();

                StringBuilder textBuilder = new StringBuilder();
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
                StringBuilder textBuilder1 = new StringBuilder();
                int c1 = 0;
                while ((c1 = reader1.read()) != -1) {
                    textBuilder1.append((char) c1);
                }
                return new ProcessResult(textBuilder1.length()<2?null:textBuilder1.toString(),textBuilder.length()<2?null:textBuilder.toString(),process.exitValue());

            } catch (InterruptedException | IOException e) {
                logger.warning(e.toString());
            }
            return null;
        });


    }

    public record ProcessResult(String error, String result, int exitValue) {
    }
}
