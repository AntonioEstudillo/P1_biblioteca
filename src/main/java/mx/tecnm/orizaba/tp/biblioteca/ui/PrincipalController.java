package mx.tecnm.orizaba.tp.biblioteca.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;
import java.io.IOException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import mx.tecnm.orizaba.tp.biblioteca.eventos.Evento;
import mx.tecnm.orizaba.tp.biblioteca.eventos.OyenteEvento;

public class PrincipalController {

    @FXML
    private StackPane  contenido;

    private Biblioteca biblioteca;

    public void setBiblioteca(Biblioteca biblioteca){
        this.biblioteca = biblioteca;

        if (!biblioteca.monitorActivo()) {
            biblioteca.iniciarMonitor(1000);
        }
    }

    private final ObservableList<Evento> eventosUI = FXCollections.observableArrayList();

    private final OyenteEvento oyenteUI = evento -> Platform.runLater(() -> eventosUI.add(evento));

    @FXML
    private Label contenidoTitulo;

    @FXML
    private void mostrarSocios() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/mx/tecnm/orizaba/tp/biblioteca/ui/Socios.fxml")
                    );

            Parent vista = loader.load();

            SociosController controller = loader.getController();

            controller.setBiblioteca(biblioteca);

            contenido.getChildren().setAll(vista);

        } catch (IOException e) {

            mostrarError(
                    "No se pudo abrir la vista de socios."
            );
        }
    }

    @FXML
    private void mostrarCatalogo(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mx/tecnm/orizaba/tp/biblioteca/ui/catalogo.fxml")
            );

            Parent vista = loader.load();

            CatalogoController controller = loader.getController();

            controller.setBiblioteca(biblioteca);

            contenido.getChildren().setAll(vista);


        } catch (IOException e) {
            mostrarError("No se pudo abrir la vista de catálogo.");
        }


    }

    @FXML
    private void mostrarPrestamos() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/mx/tecnm/orizaba/tp/biblioteca/ui/prestamos.fxml"
                            ));

            Parent vista = loader.load();

            PrestamosController controller = loader.getController();

            controller.setBiblioteca(biblioteca);

            contenido.getChildren().setAll(vista);

        } catch (IOException e) {

            System.err.println(
                    "ERROR AL CARGAR PRESTAMOS.FXML:"
            );

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir la vista de préstamos.\n"
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void mostrarReportes() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/mx/tecnm/orizaba/tp/biblioteca/ui/reportes.fxml")
                    );

            Parent vista = loader.load();

            ReportesController controller = loader.getController();

            controller.setBiblioteca(biblioteca);

            contenido.getChildren().setAll(vista);

        } catch (IOException e) {

            mostrarError("No se pudo abrir la vista de reportes.");
        }
    }

    @FXML
    private void mostrarEventos() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource("/mx/tecnm/orizaba/tp/biblioteca/ui/eventos.fxml")
                    );

            Parent vista = loader.load();

            EventosController controller = loader.getController();

            controller.setDatos(biblioteca, eventosUI);

            contenido.getChildren().setAll(vista);

        } catch (IOException e) {
            mostrarError("No se pudo abrir la bitácora de eventos.");
        }
    }

    private void mostrarPendiente(String nombre) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Biblioteca");
        alerta.setHeaderText(nombre);
        alerta.setContentText(
                "Esta pantalla se implementará posteriormente."
        );
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText("No se pudo cargar la vista");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
