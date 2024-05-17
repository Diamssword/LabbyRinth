package com.diamssword.labbyrinth.view;

import com.diamssword.labbyrinth.Main;
import com.diamssword.labbyrinth.downloaders.Utils;
import com.diamssword.labbyrinth.downloaders.VersionChecker;
import com.diamssword.labbyrinth.logger.Log;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

public class WebGui extends Application {

    private static JavaBridge bridge;
    private static Stage view;
    private static boolean wasCloseBySettings=false;
    public static void show(boolean show)
    {

        if(view!=null) {

            Platform.runLater(() -> {
                if (show && !view.isShowing()) {
                    wasCloseBySettings=false;
                    view.show();
                    try {
                        Server.start(new String[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    wasCloseBySettings=true;
                    view.hide();
                    Server.stop();
                }
            });

        }
    }
    public static void start(String... args)
    {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        view=primaryStage;
        Platform.setImplicitExit(false);
        primaryStage.setTitle("LabbyRinth");
        WebView webView = new WebView();
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
                            bridge.sendReadyEvent();
                        }
                    });
           webView.getEngine().load("http://localhost:"+Server.getPort()+"/");
        VBox vBox = new VBox(webView);
        Scene scene = new Scene(vBox, 960, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        SplashGui.instance.close();
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(WebGui.class.getResourceAsStream("/images/logo.png"))));
        primaryStage.show();
        primaryStage.setOnHidden(event -> {
           Server.stop();
           if(!wasCloseBySettings)
               System.exit(1);
        });

    }

}