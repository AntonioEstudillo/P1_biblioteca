package mx.tecnm.orizaba.tp.biblioteca.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import mx.tecnm.orizaba.tp.biblioteca.eventos.Evento;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class EventosController
        implements Initializable {

    @FXML
    private TableView<Evento> tablaEventos;

    @FXML
    private TableColumn<Evento, String> colFecha;

    @FXML
    private TableColumn<Evento, String> colTipo;

    @FXML
    private TableColumn<Evento, String> colMensaje;

    @FXML
    private Label lblMonitor;

    private Biblioteca biblioteca;

    private ObservableList<Evento> eventos;

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        colFecha.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue()
                                .instante()
                                .format(formatoFecha)
                )
        );

        colTipo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().tipo().name())
        );

        colMensaje.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().mensaje())
        );
    }

    public void setDatos(
            Biblioteca biblioteca,
            ObservableList<Evento> eventos
    ) {

        this.biblioteca = biblioteca;
        this.eventos = eventos;

        tablaEventos.setItems(eventos);

        actualizarEstadoMonitor();
    }

    private void actualizarEstadoMonitor() {

        if (biblioteca == null) {
            lblMonitor.setText("-");
            return;
        }

        lblMonitor.setText(
                biblioteca.monitorActivo() ? "Activo" : "Detenido"
        );
    }

    @FXML
    private void revisarAhora() {

        if (biblioteca == null) {
            return;
        }

        int acciones =
                biblioteca.revisar();

        actualizarEstadoMonitor();

        if (acciones == 0) {

            mostrarInformacion("La revisión terminó sin acciones pendientes.");

        } else {

            mostrarInformacion("La revisión realizó " + acciones + " acción(es).");
        }
    }

    @FXML
    private void limpiar() {

        if (eventos != null) {
            eventos.clear();
        }
    }

    private void mostrarInformacion(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);

        alerta.setTitle("Monitor");

        alerta.setHeaderText("Revisión completada");

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}