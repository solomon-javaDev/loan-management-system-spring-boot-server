package io.sol.loanmanagementsystemspringbootserver;

import io.sol.loanmanagementsystemspringbootserver.utilities.DatabaseMigrationUtility;
import io.sol.loanmanagementsystemspringbootserver.utilities.GlobalExceptionHandler;
import io.sol.loanmanagementsystemspringbootserver.utilities.StageManager;
import io.sol.loanmanagementsystemspringbootserver.services.OfflineLicenseService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class FxMainApp extends Application {

    private ConfigurableApplicationContext applicationContext;
    private Timeline licenseWatch;

    @Override
    public void init() {
    DatabaseMigrationUtility.migrateDataDirectory();

    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
        GlobalExceptionHandler.handleException(throwable);
    });

    try {
        applicationContext = new SpringApplicationBuilder(LoanManagementSystemSpringBootServerApplication.class)
                .headless(false)
                .run(getParameters().getRaw().toArray(new String[0]));
    } catch (Throwable throwable) {
        throwable.printStackTrace();
        throw throwable;
    }
        //1. Catch exceptions thrown on any background or worker thread
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable)->{
            GlobalExceptionHandler.handleException(throwable);
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //2. Catch exceptions thrown on the main JavaFX UI
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable)->{
            GlobalExceptionHandler.handleException(throwable);
        });

        StageManager stageManager = applicationContext.getBean(StageManager.class);
        Parent root = stageManager.loadView("/ui/login/LoginView.fxml");

        Scene scene = new Scene(root);

        primaryStage.setTitle("Loan Management System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        OfflineLicenseService license = applicationContext.getBean(OfflineLicenseService.class);
        licenseWatch = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            if (!license.isAllowed() && primaryStage.getScene().getRoot() != root) {
                primaryStage.close();
                Platform.exit();
            }
        }));
        licenseWatch.setCycleCount(Timeline.INDEFINITE);
        licenseWatch.play();
    }

    @Override
    public void stop() {
        if (licenseWatch != null) licenseWatch.stop();
        if (applicationContext != null) {
            applicationContext.close();
        }
        Platform.exit();
    }
}
