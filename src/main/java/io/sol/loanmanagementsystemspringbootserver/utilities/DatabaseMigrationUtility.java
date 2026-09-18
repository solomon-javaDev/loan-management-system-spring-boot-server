package io.sol.loanmanagementsystemspringbootserver.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DatabaseMigrationUtility {

    public static void migrateDataDirectory() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null) {
            localAppData = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local";
        }

        File newDbDir = new File(localAppData, "LMS" + File.separator + "data");
        File oldDbDir = new File("data");

        if (oldDbDir.exists() && oldDbDir.isDirectory()) {
            if (!newDbDir.exists()) {
                newDbDir.mkdirs();
            }

            File[] files = oldDbDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        File targetFile = new File(newDbDir, file.getName());
                        if (!targetFile.exists()) {
                            try {
                                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                System.out.println("Migrated: " + file.getName() + " to " + targetFile.getAbsolutePath());
                            } catch (IOException e) {
                                System.err.println("Failed to migrate: " + file.getName() + " - " + e.getMessage());
                            }
                        }
                    }
                }
            }
            
            // Note: We don't delete the old directory yet just in case, 
            // but in a real release candidate we might want to archive it.
            System.out.println("Data migration check completed. Data is now primarily managed in: " + newDbDir.getAbsolutePath());
        }
    }
}
