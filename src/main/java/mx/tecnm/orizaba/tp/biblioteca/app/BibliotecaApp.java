package mx.tecnm.orizaba.tp.biblioteca.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import mx.tecnm.orizaba.tp.biblioteca.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;
import mx.tecnm.orizaba.tp.biblioteca.ui.PrincipalController;

import java.io.IOException;

public final class BibliotecaApp extends Application {

    private ConexionSQLite bd;
    private Biblioteca biblioteca;

    @Override
    public void start(Stage stage) throws IOException {

        bd = new ConexionSQLite("datos.db");

        biblioteca = new Biblioteca(bd);

        FXMLLoader loader = new FXMLLoader(
                BibliotecaApp.class.getResource(
                        "/mx/tecnm/orizaba/tp/biblioteca/ui/principal.fxml"));

        Scene escena = new Scene(loader.load(), 1100, 700);

        escena.getStylesheets().add(
                BibliotecaApp.class.getResource(
                        "/mx/tecnm/orizaba/tp/biblioteca/ui/biblioteca.css").toExternalForm()
        );

        PrincipalController controller = loader.getController();

        controller.setBiblioteca(biblioteca);

        stage.setTitle("Biblioteca");

        stage.setScene(escena);

        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.show();
    }

    @Override
    public void stop() {

        if (biblioteca != null) {biblioteca.close();}

        if (bd != null) {bd.close();}
    }

    public static void main(String[] args) {launch(args);
    }
}