package io.sol.loanmanagementsystemspringbootserver.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AutoupdateService {

    @Value("${spring.application.version}")
    private String currentVersion;

    public void checkForUpdatesAsync(){
        Thread updateThread = new Thread(()->{
            try{
                System.out.println("Current App version: "+currentVersion);
            }catch (Exception e){
                System.out.println("Silent Update check failed");
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }
}
