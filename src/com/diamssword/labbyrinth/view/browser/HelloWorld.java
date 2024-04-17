package com.diamssword.labbyrinth.view.browser;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.downloaders.FileDownloader;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.view.components.PackPicker;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HelloWorld extends Application {

    public static void start(String... args)
    {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LabbyRinth");

        WebView webView = new WebView();

        try {
            File f=new File(LauncherVariables.gameDirectory,"view");
            f.delete();
            File f1=new File(f,"temp.zip");
            FileUtils.copyInputStreamToFile(PackPicker.class.getResource("/view/dist.zip").openStream(),f1);
            unzip(f1,f);
            f1.delete();
            Server.start(new String[0]);

            webView.getEngine().setJavaScriptEnabled(true);
            WebConsoleListener.setDefaultListener((webView1, message, lineNumber, sourceId) -> {
                System.out.println(message + "[at " + lineNumber + "]");
            });
            webView.getEngine().load("http://localhost:51973/");

            //webView.getEngine().load(new File(f,"index.html").toURI().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VBox vBox = new VBox(webView);
        Scene scene = new Scene(vBox, 960, 600);

        primaryStage.setScene(scene);
        primaryStage.show();

    }
    private void unzip(File zipFile, File ouptut)
    {
        try {
            ZipFile zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                        String name=entry.getName();
                        try (InputStream inputStream = zip.getInputStream(entry)) {
                            File path=new File(ouptut,name);
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
        }catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Le .mrpack est corrompu");
        }
    }
}