package io.github.vexisvx.ledgerx.util;

import javafx.application.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RestartService {

    public static void restart() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");
            String projectDir = System.getProperty("user.dir");

            // Detect how we're running
            String classpath = System.getProperty("java.class.path", "");
            boolean isMavenRun = classpath.contains(".m2") || classpath.contains("maven");

            List<String> cmd = new ArrayList<>();

            if (isWindows) {
                // Windows: use cmd /c start for detached
                if (isMavenRun) {
                    cmd.add("cmd.exe");
                    cmd.add("/c");
                    cmd.add("start");
                    cmd.add("\"LedgerX\"");
                    cmd.add("cmd.exe");
                    cmd.add("/c");
                    cmd.add("cd /d \"" + projectDir + "\" && mvn javafx:run");
                } else {
                    String javaBin = System.getProperty("java.home") + "\\bin\\java.exe";
                    String jar = findRunningJar();
                    if (jar != null) {
                        cmd.add("cmd.exe");
                        cmd.add("/c");
                        cmd.add("start");
                        cmd.add("\"LedgerX\"");
                        cmd.add(javaBin);
                        cmd.add("-jar");
                        cmd.add(jar);
                    }
                }
            } else {
                // Linux/Mac: use setsid + nohup for true detach
                String innerScript;
                if (isMavenRun) {
                    innerScript = "cd \"" + projectDir + "\" && mvn javafx:run";
                } else {
                    String jar = findRunningJar();
                    if (jar != null) {
                        String javaBin = System.getProperty("java.home") + "/bin/java";
                        innerScript = "\"" + javaBin + "\" -jar \"" + jar + "\"";
                    } else {
                        String javaBin = System.getProperty("java.home") + "/bin/java";
                        innerScript = "\"" + javaBin + "\" -cp \"" + classpath + "\" io.github.vexisvx.ledgerx.Launcher";
                    }
                }

                // setsid detaches from terminal session; nohup ignores SIGHUP
                cmd.add("setsid");
                cmd.add("sh");
                cmd.add("-c");
                cmd.add("nohup " + innerScript + " > /tmp/ledgerx-restart.log 2>&1 &");
            }

            AppLogger.info("Restarting: " + String.join(" ", cmd));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(new File(projectDir));
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            pb.start();

            Thread.sleep(800);

        } catch (Exception e) {
            AppLogger.error("Restart failed", e);
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    private static String findRunningJar() {
        try {
            String path = RestartService.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
            if (path.endsWith(".jar")) return path;

            File target = new File("target");
            if (target.isDirectory()) {
                File[] jars = target.listFiles((dir, name) ->
                    name.startsWith("LedgerX") && name.endsWith(".jar"));
                if (jars != null && jars.length > 0) {
                    return jars[0].getAbsolutePath();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
