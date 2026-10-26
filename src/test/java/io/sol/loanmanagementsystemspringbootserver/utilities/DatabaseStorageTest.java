package io.sol.loanmanagementsystemspringbootserver.utilities;

import io.sol.loanmanagementsystemspringbootserver.services.DatabaseBackupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseStorageTest {

    @TempDir Path root;

    @Test
    void keepsExistingLegacyDatabaseAndOnlyImportsWhenAbsent() throws Exception {
        Path primary = root.resolve("AppData/LMS/data");
        Path previous = root.resolve("old/data");
        Files.createDirectories(previous);
        Files.writeString(previous.resolve("loan_db.mv.db"), "old");
        DatabaseMigrationUtility.prepareDatabase(primary, previous);
        assertEquals("old", Files.readString(primary.resolve("loan_db.mv.db")));
        Files.writeString(primary.resolve("loan_db.mv.db"), "current");
        DatabaseMigrationUtility.prepareDatabase(primary, previous);
        assertEquals("current", Files.readString(primary.resolve("loan_db.mv.db")));
        assertEquals(root.resolve("appdata/LMS/data"), DatabaseMigrationUtility.databaseDirectory(root.resolve("appdata").toString(), root.toString()));
    }

    @Test
    void backsUpLiveH2DatabaseIntoReadableArchive() throws Exception {
        Path db = root.resolve("primary/loan_db");
        Files.createDirectories(db.getParent());
        DriverManagerDataSource source = new DriverManagerDataSource("jdbc:h2:file:" + db.toString().replace('\\', '/'), "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE backup_check (id INT PRIMARY KEY)");
        jdbc.update("INSERT INTO backup_check VALUES (42)");
        Path backups = root.resolve("data");
        DatabaseBackupService service = new DatabaseBackupService(jdbc, backups);
        service.backup();
        try (ZipFile zip = new ZipFile(backups.resolve("loan_db-backup.zip").toFile())) {
            assertNotNull(zip.getEntry("loan_db.mv.db"));
        }
        jdbc.update("INSERT INTO backup_check VALUES (43)");
        service.backup();
        assertTrue(Files.size(backups.resolve("loan_db-backup.zip")) > 0);
    }
}