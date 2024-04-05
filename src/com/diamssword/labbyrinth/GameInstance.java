package com.diamssword.labbyrinth;

import com.diamssword.labbyrinth.downloaders.Utils;
import com.diamssword.labbyrinth.view.ConsoleGui;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

    public void start(String user)
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
                Math.max(1,Utils.readCommonCache().getInt("ram"));
            cmd.add("start");
            cmd.add("--jvm-args=\"-Xmx"+ram+"G\"");
            cmd.add(getVersionCmd());
            cmd.add("-l");
            cmd.add("\""+user+"\"");

            System.out.println("Running :" + Arrays.toString(cmd.toArray()));
            ProcessBuilder builder = new ProcessBuilder(cmd);//.redirectErrorStream(true);
        //    builder.inheritIO();
            Process process=builder.start();
            ConsoleGui.pipeOutput(process.getInputStream(),process.getErrorStream());
            process.getOutputStream().close();
            process.waitFor();

        } catch (Exception  e) {
            throw new RuntimeException(e);
        }
    }
}
