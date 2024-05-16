package com.diamssword.labbyrinth;

import java.io.File;
import java.util.Scanner;

public class LauncherVariables {
    public static String gameDirectory=System.getenv("appdata")+"\\.labbyrinth";
    public static String version="0.0.1";
    public static boolean devMode=true;
    public static String updateUrl ="http://localhost:3000";
    public static String defaultPack ="green_resurgence";
    public static File getPackFolder(String packname)
    {
        return  new File(gameDirectory,"packs/"+packname.replaceAll("/","_").replaceAll("\\\\","_").replaceAll(" ","_"));
    }
    public static void loadVars()
    {
        try {
            Scanner s = new Scanner(LauncherVariables.class.getResourceAsStream("/settings.txt"));
            while (s.hasNextLine()) {
                String[] var = s.nextLine().trim().split("=");
                if (var.length == 2) {
                    if (var[0].equals("gameDirectory"))
                        gameDirectory = System.getenv("appdata") + "\\" + var[1];
                    if (var[0].equals("devMode"))
                        devMode = var[1].equals("true");
                    if (var[0].equals("updateUrl"))
                        updateUrl = var[1];
                    if (var[0].equals("defaultPack"))
                        defaultPack = var[1];
                    if (var[0].equals("version"))
                        version=var[1];
                }
            }
        }catch (NullPointerException e)
        {
            System.err.println("Can't load variable file");
            throw  new RuntimeException(e);
        }
    }
}
