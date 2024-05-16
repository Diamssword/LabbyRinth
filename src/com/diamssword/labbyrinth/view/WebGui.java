package com.diamssword.labbyrinth.view;

import com.diamssword.labbyrinth.downloaders.VersionChecker;
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

import java.util.Objects;

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
           webView.getEngine().load("http://localhost:51973/");


        VBox vBox = new VBox(webView);
        Scene scene = new Scene(vBox, 960, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        SplashGui.instance.close();
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(WebGui.class.getResourceAsStream("/images/logo.png"))));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
           Server.stop();
        });

    }

}