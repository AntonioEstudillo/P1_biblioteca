package mx.tecnm.orizaba.tp.biblioteca.ui;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import mx.tecnm.orizaba.tp.biblioteca.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Socio;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SociosController implements Initializable {

    @FXML
    private TableView<Socio> tablaSocios;

    @FXML
    private TableColumn<Socio, Number> colId;

    @FXML
    private TableColumn<Socio, String> colNombre;

    @FXML
    private TableColumn<Socio, String> colCorreo;

    @FXML
    private TableColumn<Socio, String> colNivel;

    @FXML
    private TableColumn<Socio, String> colDescuento;

    private Biblioteca biblioteca;

    @Override
    public void initialize(
            URL location,
            ResourceBundle resources
    ) {

        colId.setCellValueFactory(datos ->
                new ReadOnlyIntegerWrapper(
                        datos.getValue().id()
                )
        );

        colNombre.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().nombre()
                )
        );

        colCorreo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().correo()
                )
        );

        colNivel.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue()
                                .nivel()
                                .etiqueta()
                )
        );

        colDescuento.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        "%.0f %%".formatted(
                                datos.getValue()
                                        .nivel()
                                        .descuento()
                                        * 100
                        )
                )
        );
    }

    public void setBiblioteca(
            Biblioteca biblioteca
    ) {
        this.biblioteca = biblioteca;
        actualizarTabla();
    }

    @FXML
    private void actualizarTabla() {
        if (biblioteca == null) {
            return;
        }

        tablaSocios.getItems().setAll(
                biblioteca.socios()
        );
    }

    @FXML
    private void nuevoSocio() {
        abrirFormulario(null);
    }

    @FXML
    private void editarSocio() {

        Socio seleccionado =
                tablaSocios
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {
            mostrarAdvertencia(
                    "Selecciona primero un socio."
            );
            return;
        }

        abrirFormulario(seleccionado);
    }

    private void abrirFormulario(
            Socio socio
    ) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/mx/tecnm/orizaba/tp/biblioteca/ui/SocioForm.fxml"
                            )
                    );

            Parent vista = loader.load();

            SocioFormController controller =
                    loader.getController();

            Stage ventana = new Stage();

            controller.setBiblioteca(
                    biblioteca
            );

            controller.setStage(
                    ventana
            );

            if (socio != null) {
                controller.setSocio(socio);
            }

            ventana.initModality(
                    Modality.APPLICATION_MODAL
            );

            ventana.initOwner(
                    tablaSocios
                            .getScene()
                            .getWindow()
            );

            ventana.setTitle(
                    socio == null
                            ? "Nuevo socio"
                            : "Editar socio"
            );

            ventana.setScene(
                    new Scene(vista)
            );

            ventana.setResizable(false);

            ventana.showAndWait();

            actualizarTabla();

        } catch (IOException e) {
            mostrarError(
                    "No se pudo abrir el formulario."
            );
        }
    }

    @FXML
    private void eliminarSocio() {

        Socio seleccionado =
                tablaSocios
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {
            mostrarAdvertencia(
                    "Selecciona primero un socio."
            );
            return;
        }

        Alert confirmacion =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmacion.setTitle(
                "Eliminar socio"
        );

        confirmacion.setHeaderText(
                "¿Deseas eliminar este socio?"
        );

        confirmacion.setContentText(
                seleccionado.id()
                        + " - "
                        + seleccionado.nombre()
        );

        var respuesta =
                confirmacion.showAndWait();

        if (respuesta.isEmpty()
                || respuesta.get()
                != ButtonType.OK) {
            return;
        }

        try {
            if (biblioteca.retirarSocio(
                    seleccionado.id()
            )) {
                actualizarTabla();
            }

        } catch (ReglaNegocioException e) {
            mostrarError(
                    e.getMessage()
            );
        }
    }

    private void mostrarAdvertencia(
            String mensaje
    ) {
        Alert alerta =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alerta.setTitle("Biblioteca");
        alerta.setHeaderText(
                "Selección requerida"
        );
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }

    private void mostrarError(
            String mensaje
    ) {
        Alert alerta =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alerta.setTitle("Error");
        alerta.setHeaderText(
                "No se pudo completar la operación"
        );
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}