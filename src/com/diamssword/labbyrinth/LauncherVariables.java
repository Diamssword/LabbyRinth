package com.diamssword.labbyrinth;

import java.io.File;

public class LauncherVariables {
    public static String gameDirectory=System.getenv("appdata")+"\\.labbyrinth";
    public static String update_url="http://localhost:3000";
    public static String default_pack="green_resurgence";
    public static File getPackFolder(String packname)
    {
        return  new File(gameDirectory,"packs/"+packname.replaceAll("/","_").replaceAll("\\\\","_").replaceAll(" ","_"));
    }
}
