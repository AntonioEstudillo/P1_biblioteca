package mx.tecnm.orizaba.tp.biblioteca.ui;

import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ButtonType;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;
import mx.tecnm.orizaba.tp.biblioteca.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

public class CatalogoController implements Initializable{
    @FXML
    private TableView<Material> tablaMateriales;

    @FXML
    private TableColumn<Material, String> colClave;

    @FXML
    private TableColumn<Material, String> colTipo;

    @FXML
    private TableColumn<Material, String> colNombre;

    @FXML
    private TableColumn<Material, Number> colPrecio;

    @FXML
    private TableColumn<Material, String> colAtributo;

    private Biblioteca biblioteca;


    public void setBiblioteca(Biblioteca biblioteca){
        this.biblioteca = biblioteca;
        actualizarTabla();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        colClave.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().clave().toString()
                )
        );

        colTipo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().tipo()
                )
        );

        colNombre.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue().nombre()
                )
        );

        colPrecio.setCellValueFactory(datos ->
                new ReadOnlyDoubleWrapper(
                        datos.getValue().precioBase()
                )
        );

        colAtributo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        atributoDe(datos.getValue())
                )
        );
    }


    @FXML
    private void actualizarTabla() {
        if (biblioteca == null) {
            return;
        }

        var materiales =
                biblioteca.proyectar(material -> material);


        tablaMateriales.getItems().setAll(materiales);

    }

    private String atributoDe(Material material){
        return switch (material){
            case Material.Libro libro->
                libro.paginas() + " páginas ";
            case Material.Revista revista->
                revista.antiguedadAnios() + " años";
            case Material.Tesis tesis->
                tesis.grado();
        };
    }

    @FXML
    private void nuevoMaterial() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/mx/tecnm/orizaba/tp/biblioteca/ui/material-form.fxml"
                    )
            );


            Parent vista = loader.load();

            MaterialFormController controller =
                    loader.getController();

            Stage ventana = new Stage();

            controller.setBiblioteca(biblioteca);
            controller.setStage(ventana);

            ventana.initModality(
                    Modality.APPLICATION_MODAL
            );

            ventana.initOwner(
                    tablaMateriales
                            .getScene()
                            .getWindow()
            );

            ventana.setTitle("Nuevo material");

            ventana.setScene(
                    new Scene(vista)
            );

            ventana.setResizable(false);

            ventana.showAndWait();

            actualizarTabla();

        } catch (IOException e) {
            mostrarError(
                    "No se pudo abrir el formulario." + e.getMessage()
            );
        }
    }

    @FXML
    private void eliminarMaterial() {

        Material seleccionado = tablaMateriales.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAdvertencia("Selecciona primero un material de la tabla.");
            return;
        }

        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Eliminar material");
        confirmacion.setHeaderText(
                "¿Deseas eliminar este material?"
        );

        confirmacion.setContentText(
                seleccionado.clave()
                        + " - "
                        + seleccionado.nombre()
        );

        var respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {

            boolean eliminado = biblioteca.retirar(seleccionado.clave());

            if (eliminado) {
                actualizarTabla();
            }

        } catch (ReglaNegocioException e) {

            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void editarMaterial() {

        Material seleccionado =
                tablaMateriales
                        .getSelectionModel()
                        .getSelectedItem();

        if (seleccionado == null) {
            mostrarAdvertencia(
                    "Selecciona primero un material para editar."
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/mx/tecnm/orizaba/tp/biblioteca/ui/material-form.fxml"
                    )
            );

            Parent vista = loader.load();

            MaterialFormController controller =
                    loader.getController();

            Stage ventana = new Stage();

            controller.setBiblioteca(biblioteca);
            controller.setStage(ventana);

            // Esta línea activa el modo edición
            controller.setMaterial(seleccionado);

            ventana.initModality(
                    Modality.APPLICATION_MODAL
            );

            ventana.initOwner(
                    tablaMateriales
                            .getScene()
                            .getWindow()
            );

            ventana.setTitle("Editar material");
            ventana.setScene(new Scene(vista));
            ventana.setResizable(false);

            ventana.showAndWait();

            actualizarTabla();

        } catch (IOException e) {
            mostrarError(
                    "No se pudo abrir el formulario de edición."
            );
        }
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Error");
        alerta.setHeaderText("Ocurrió un problema");
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }

    private void mostrarAdvertencia(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.WARNING);

        alerta.setTitle("Biblioteca");
        alerta.setHeaderText("Selecciona un material");
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}
