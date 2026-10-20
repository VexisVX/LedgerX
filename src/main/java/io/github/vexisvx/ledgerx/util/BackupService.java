package io.github.vexisvx.ledgerx.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BackupService {
    public static void backup(File destination) throws Exception {
        Files.copy(new File("ledgerx.db").toPath(), destination.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }
    public static void restore(File source) throws Exception {
        DatabaseManager.close();
        Files.copy(source.toPath(), new File("ledgerx.db").toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }
}
