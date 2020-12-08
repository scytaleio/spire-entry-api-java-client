package io.spiffe.entryapi;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

@Command(subcommands = {CreateEntry.class}, mixinStandardHelpOptions = true)
public class EntryAdmin implements Runnable {


    public static void main(String[] args) {
        configureLogging();

        int exitCode =
                new CommandLine(new EntryAdmin())
                        .execute(args);

        System.exit(exitCode);
    }

    @Override
    public void run() {
        // parent command, no logic needed
    }

    private static void configureLogging() {
        final InputStream inputStream = EntryAdmin.class.getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
