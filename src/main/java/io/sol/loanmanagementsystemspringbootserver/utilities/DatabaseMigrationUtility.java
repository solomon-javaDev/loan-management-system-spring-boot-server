package io.sol.loanmanagementsystemspringbootserver.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.DosFileAttributeView;

public class DatabaseMigrationUtility {

    public static void migrateDataDirectory() {
        try {
            Path directory = databaseDirectory(System.getenv("LOCALAPPDATA"), System.getProperty("user.home"));
            prepareDatabase(directory, Path.of(System.getProperty("user.dir"), "data"));
            System.setProperty("lms.db.path", directory.resolve("loan_db").toAbsolutePath().toString().replace('\\', '/'));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare the loan database directory", e);
        }
    }

    public static Path databaseDirectory(String localAppData, String userHome) {
        Path local = localAppData == null || localAppData.isBlank()
                ? Path.of(userHome, "AppData", "Local") : Path.of(localAppData);
        return local.resolve("LMS").resolve("data");
    }

    public static void prepareDatabase(Path directory, Path oldDirectory) throws IOException {
        Files.createDirectories(directory);
        Path database = directory.resolve("loan_db.mv.db");
        Path oldDatabase = oldDirectory.resolve("loan_db.mv.db");
        if (!Files.exists(database) && Files.isRegularFile(oldDatabase)
                && !Files.isSameFile(directory, oldDirectory)) {
            Files.copy(oldDatabase, database);
        }
        DosFileAttributeView attributes = Files.getFileAttributeView(directory, DosFileAttributeView.class);
        if (attributes != null) {
            attributes.setHidden(true);
        }
    }
}
