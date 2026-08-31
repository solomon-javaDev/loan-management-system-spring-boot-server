package io.sol.loanmanagementsystemspringbootserver;

import io.sol.loanmanagementsystemspringbootserver.utilities.GlobalExceptionHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

public class FxMainApp extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
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

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login/LoginView.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setTitle("Loan Management System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
        Platform.exit();
    }
}
