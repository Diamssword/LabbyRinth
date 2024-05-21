package com.diamssword.labbyrinth.logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class GameDownloadStream extends OutputStream {
    private StringBuilder line=new StringBuilder();
    public GameDownloadStream()
    {

    }
    public void onLine()
    {
        String l=line.toString();
        line=new StringBuilder();
        if(l.startsWith("[  ..  ] Download:"))
        {
            var trimed=l.replace("[  ..  ] Download:","").trim();
            var a=trimed.split(" ")[0];
            var a1=a.split("/");
            if(a1.length>1) {
                try {
                    Log.setProgress("Téléchargement de Minecraft", (int) (((float)Integer.parseInt(a1[0])/(float)Integer.parseInt(a1[1]))*100));
                } catch (NumberFormatException e) {}
            }
        }
    }
    @Override
    public void write(int b) throws IOException {
        if((char) b=='\n' || (char) b=='\r')
            onLine();
        else
            line.append((char)b);
    }
}
