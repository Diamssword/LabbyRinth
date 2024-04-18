package com.diamssword.labbyrinth.view.browser;

import com.diamssword.labbyrinth.LauncherVariables;
import com.diamssword.labbyrinth.PacksManager;
import com.diamssword.labbyrinth.downloaders.FileDownloader;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.view.MainGui;
import com.diamssword.labbyrinth.view.SplashGui;
import com.diamssword.labbyrinth.view.components.PackPicker;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class WebGui extends Application {

    private static JavaBridge bridge;

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

            webView.getEngine().getLoadWorker().stateProperty().addListener(
                    (ObservableValue<? extends Worker.State> ov, Worker.State oldState,
                     Worker.State newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            JSObject win  = (JSObject) webView.getEngine().executeScript("window");
                            bridge= new JavaBridge(webView.getEngine());
                            win.setMember("bridge",bridge);

                        }
                    });

            // webView.getEngine().load("http://localhost:5173/");
            webView.getEngine().load("http://localhost:51973/");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        VBox vBox = new VBox(webView);
        Scene scene = new Scene(vBox, 960, 600);

        primaryStage.setScene(scene);
        SplashGui.instance.close();
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(WebGui.class.getResourceAsStream("/images/logo.png"))));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
           Server.stop();
        });

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