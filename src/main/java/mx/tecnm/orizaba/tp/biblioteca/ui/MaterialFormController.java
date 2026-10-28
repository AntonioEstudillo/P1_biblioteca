package mx.tecnm.orizaba.tp.biblioteca.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;
import mx.tecnm.orizaba.tp.biblioteca.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

public class MaterialFormController {
    @FXML
    private ComboBox<String> tipoCombo;

    @FXML
    private TextField nombreField;

    @FXML
    private TextField precioField;

    @FXML
    private Label atributoLabel;

    @FXML
    private TextField atributoField;

    private Biblioteca biblioteca;
    private Stage stage;

    private Material materialEditando;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void initialize(){
        tipoCombo.getItems().addAll("Libro","Revista", "Tesis");

        tipoCombo.getSelectionModel().selectFirst();

        tipoCombo.valueProperty().addListener(
                (observable, anterior, nuevo)-> actualizarAtributo(nuevo)
        );

        actualizarAtributo("Libro");

    }

    public void setBiblioteca(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;
    }

    public void actualizarAtributo(String tipo){
        switch (tipo){
            case "Libro" ->{
                atributoLabel.setText("Número de páginas");
                atributoField.setPromptText("Ej: 200");
            }

            case "Revista" ->{
                atributoLabel.setText("Antigüedad en años");
                atributoField.setPromptText("Ej: 2");
            }

            case "Tesis" ->{
                atributoLabel.setText("Grado");
                atributoField.setPromptText("Ej: Maestría");
            }

            default -> {
                atributoLabel.setText("Atributo");
                atributoField.setPromptText("");
            }
        }
    }

    @FXML
    private void guardar() {
        try {

            String nombre = nombreField.getText().trim();

            String textoPrecio = precioField.getText().trim();

            if (nombre.isBlank()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío.");
            }

            if (textoPrecio.isBlank()) {
                throw new IllegalArgumentException("Debes indicar el precio base.");
            }

            double precio = Double.parseDouble(textoPrecio);

            if (materialEditando != null) {

                biblioteca.actualizar(
                        materialEditando,
                        nombre,
                        precio
                );

                stage.close();
                return;
            }

            String tipo = tipoCombo.getValue();

            String atributo = atributoField.getText().trim();

            Material nuevo;

            switch (tipo) {
                case "Libro" ->
                        nuevo = biblioteca.registrar(
                                Material.Libro::new,
                                nombre,
                                precio,
                                Integer.parseInt(atributo)
                        );

                case "Revista" ->
                        nuevo = biblioteca.registrar(
                                Material.Revista::new,
                                nombre,
                                precio,
                                Integer.parseInt(atributo)
                        );

                case "Tesis" ->
                        nuevo = biblioteca.registrar(
                                Material.Tesis::new,
                                nombre,
                                precio,
                                atributo
                        );

                default ->
                        throw new IllegalArgumentException(
                                "Tipo no válido"
                        );
            }

            stage.close();

        } catch (NumberFormatException e) {
            mostrarError(
                    "Precio, páginas y antigüedad deben contener números válidos."
            );

        } catch (ReglaNegocioException | IllegalArgumentException e) {
            mostrarError(e.getMessage());
        }
    }
    @FXML
    private void cancelar(){
        stage.close();
    }

    public void setMaterial(Material material) {
        this.materialEditando = material;

        tipoCombo.setValue(material.tipo());
        tipoCombo.setDisable(true);

        nombreField.setText(material.nombre());
        precioField.setText(
                Double.toString(material.precioBase())
        );

        switch (material) {
            case Material.Libro libro -> {
                atributoLabel.setText("Número de páginas");
                atributoField.setText(Integer.toString(libro.paginas()));
            }

            case Material.Revista revista -> {
                atributoLabel.setText("Antigüedad en años");
                atributoField.setText(
                        Integer.toString(revista.antiguedadAnios()));
            }

            case Material.Tesis tesis -> {
                atributoLabel.setText("Grado");
                atributoField.setText(tesis.grado());
            }
        }

        atributoField.setDisable(true);
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Datos inválidos");
        alerta.setHeaderText("No se pudo registrar el material");
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}
