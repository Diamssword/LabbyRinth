package com.diamssword.labbyrinth;

import com.diamssword.labbyrinth.downloaders.Utils;
import com.diamssword.labbyrinth.logger.GameDownloadStream;
import com.diamssword.labbyrinth.view.WebGui;
import org.apache.commons.io.input.TeeInputStream;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class GameInstance {


    public static enum LoaderType{
        vanilla,
        fabric,
        forge,
        neoforge
    }
    private final LoaderType type;
    private final String version;
    private final String subversion;
    private final String packDirectory;
    public GameInstance(String pack,LoaderType type,String version)
    {
        this(pack,type,version,null);
    }
    public GameInstance(String pack, LoaderType type,String version,@Nullable String subversion )
    {
        this.type=type;
        this.version=version;
        this.subversion=subversion;
        this.packDirectory=pack;
    }
    private String getVersionCmd()
    {
        String s="";
        if(type!=LoaderType.vanilla)
        {
            s=type.toString()+":"+version;
        }
        else
            s=version;
        if(type==LoaderType.fabric)
        {
            if(subversion!=null)
                s=s+":"+subversion;
        }
        else if(type!=LoaderType.vanilla)
        {
            if(subversion!=null)
                s=s+"-"+subversion;
            else
                s=s+"-recommended";
        }

        return s;
    }

    public void start(String user, Consumer<Process> onExit)
    {
        try {
            List<String> cmd=new ArrayList<>();
            cmd.add(new File(LauncherVariables.gameDirectory,"portablemc.exe").getAbsolutePath());
            cmd.add("--main-dir");
            cmd.add(LauncherVariables.gameDirectory);
            cmd.add("--work-dir");
            cmd.add(new File(LauncherVariables.gameDirectory,"packs/"+this.packDirectory).getAbsolutePath());
            cmd.add("--output");
            cmd.add("human-color");
            var ram=4;
            JSONObject ob=Utils.readCommonCache();
            if(ob.has("ram"))
             ram=   Math.max(1,Utils.readCommonCache().getInt("ram"));
            cmd.add("start");
            String jv="-Xmx"+ram+"G";
            if(ob.has("javaArgs"))
                    jv=jv+" "+ob.getString("javaArgs");
            cmd.add("\"--jvm-args="+jv.trim()+"\"");
            cmd.add(getVersionCmd());
            cmd.add("-l");
            cmd.add("\""+user+"\"");
            Main.logger.info("Launching :" + Arrays.toString(cmd.toArray()));
            ProcessBuilder builder = new ProcessBuilder(cmd);//.redirectErrorStream(true);
         //   builder.inheritIO();
            Process process=builder.start();
            if(Utils.readCommonCache().optBoolean("hide",false))
                WebGui.show(false);
            forwardStream(process.getInputStream(),System.out);
            forwardStream(process.getErrorStream(),System.err);
            process.onExit().thenAccept((p)->{
                WebGui.show(true);

                onExit.accept(p);
            });
            process.waitFor();


        } catch (Exception  e) {
            throw new RuntimeException(e);
        }
    }
    public static void forwardStream(InputStream inputStream, PrintStream outputStream) throws IOException {
        var str=new GameDownloadStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            str.write(buffer,0,bytesRead);
        }
    }
}
