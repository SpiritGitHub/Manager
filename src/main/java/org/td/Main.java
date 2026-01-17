package org.td;

import javafx.application.Application;
import org.td.view.MainView;

/**
 * Classe principale pour lancer l'application
 * Point d'entrée du jeu ÉnergiVille
 */
public class Main {

    /**
     * Version de l'application
     */
    public static final String VERSION = "1.0.0";

    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("   ÉnergiVille - Tycoon Énergétique   ");
        System.out.println("            Version " + VERSION + "            ");
        System.out.println("═══════════════════════════════════════");
        System.out.println();
        System.out.println("Démarrage de l'application...");

        // Vérifier la version Java
        checkJavaVersion();

        // Créer le dossier de sauvegarde s'il n'existe pas
        createSaveDirectory();

        // Lancer l'application JavaFX
        try {
            Application.launch(AppLauncher.class, args);
        } catch (Exception e) {
            System.err.println("❌ Erreur fatale au démarrage:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Classe interne pour lancer l'application JavaFX via le WelcomeView
     */
    public static class AppLauncher extends Application {
        @Override
        public void start(javafx.stage.Stage primaryStage) {
            org.td.view.WelcomeView welcomeView = new org.td.view.WelcomeView(primaryStage);
            welcomeView.show();
        }
    }

    /**
     * Vérifie que la version Java est compatible
     */
    private static void checkJavaVersion() {
        String version = System.getProperty("java.version");
        System.out.println("✓ Java version: " + version);

        // Extraire le numéro de version majeur
        int majorVersion = getMajorVersion(version);

        if (majorVersion < 11) {
            System.err.println("⚠️ ATTENTION: Java 11 ou supérieur recommandé");
            System.err.println("   Votre version: " + version);
        }
    }

    /**
     * Extrait le numéro de version majeur
     */
    private static int getMajorVersion(String version) {
        try {
            String[] parts = version.split("\\.");
            if (parts[0].equals("1")) {
                // Format ancien (1.8.x)
                return Integer.parseInt(parts[1]);
            } else {
                // Format nouveau (11.x, 17.x, etc.)
                return Integer.parseInt(parts[0]);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Crée le dossier de sauvegarde
     */
    private static void createSaveDirectory() {
        java.io.File saveDir = new java.io.File("saves");
        if (!saveDir.exists()) {
            if (saveDir.mkdirs()) {
                System.out.println("✓ Dossier de sauvegarde créé: saves/");
            } else {
                System.err.println("⚠️ Impossible de créer le dossier saves/");
            }
        } else {
            System.out.println("✓ Dossier de sauvegarde: saves/");
        }
    }
}