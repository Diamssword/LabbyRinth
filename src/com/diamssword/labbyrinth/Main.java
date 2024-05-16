package com.diamssword.labbyrinth;

import com.diamssword.labbyrinth.downloaders.VersionChecker;
import com.diamssword.labbyrinth.downloaders.ViewLoader;
import com.diamssword.labbyrinth.logger.Log;
import com.diamssword.labbyrinth.view.SplashGui;
import com.diamssword.labbyrinth.view.WebGui;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.application.Application.launch;


public class Main {
    public static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
                LauncherVariables.loadVars();
                System.setProperty("log4jLauncherRoot", LauncherVariables.gameDirectory+"/launcher_logs.txt");

                logger.info("Starting LabbyRinth");
                SplashGui.create();
                CompletableFuture<Boolean> f=CliInterface.verifyCLI();
                f.exceptionally(t -> {
                    logger.warning(t.toString());
                    return true;
                });
                if (!f.get()) {
                    Log.setProgress("Impossible de mettre à jour portablemc.");
                    logger.log(Level.SEVERE,"Can't find or update portablemc , game can't be launched");
                }
                else {

                    Log.setProgress("Lancement de LabbyRinth",100);

                    ViewLoader.load();
                    new Thread(PacksManager::load).start();
                    //new Thread(VersionChecker::updateLauncher).start();
                    WebGui.start(args);
//                    MainGui.create();
                }

       // new GameInstance("green_resurgence",GameInstance.LoaderType.fabric,"1.20.1").start("hdiamssword@gmail.com");

    }

}