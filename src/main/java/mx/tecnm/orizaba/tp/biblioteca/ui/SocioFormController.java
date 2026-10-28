package mx.tecnm.orizaba.tp.biblioteca.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Nivel;
import mx.tecnm.orizaba.tp.biblioteca.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Socio;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

public class SocioFormController {

    @FXML
    private TextField nombreField;

    @FXML
    private TextField correoField;

    @FXML
    private ComboBox<Nivel> nivelCombo;

    private Biblioteca biblioteca;

    private Stage stage;

    private Socio socioEditando;

    @FXML
    private void initialize() {

        nivelCombo.getItems().setAll(
                Nivel.values()
        );

        nivelCombo
                .getSelectionModel()
                .selectFirst();
    }

    public void setBiblioteca(
            Biblioteca biblioteca
    ) {
        this.biblioteca = biblioteca;
    }

    public void setStage(
            Stage stage
    ) {
        this.stage = stage;
    }

    public void setSocio(
            Socio socio
    ) {
        this.socioEditando = socio;

        nombreField.setText(
                socio.nombre()
        );

        correoField.setText(
                socio.correo()
        );

        nivelCombo.setValue(
                socio.nivel()
        );
    }

    @FXML
    private void guardar() {

        try {
            String nombre =
                    nombreField
                            .getText()
                            .trim();

            String correo =
                    correoField
                            .getText()
                            .trim();

            Nivel nivel =
                    nivelCombo.getValue();

            if (nivel == null) {
                throw new IllegalArgumentException(
                        "Selecciona un nivel."
                );
            }

            if (socioEditando == null) {

                biblioteca.registrarSocio(
                        nombre,
                        correo,
                        nivel
                );

            } else {

                biblioteca.actualizarSocio(
                        socioEditando.id(),
                        nombre,
                        correo,
                        nivel
                );
            }

            stage.close();

        } catch (ReglaNegocioException |
                 IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );
        }
    }

    @FXML
    private void cancelar() {
        stage.close();
    }

    private void mostrarError(
            String mensaje
    ) {
        Alert alerta =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alerta.setTitle(
                "Datos inválidos"
        );

        alerta.setHeaderText(
                "No se pudo guardar el socio"
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }
}