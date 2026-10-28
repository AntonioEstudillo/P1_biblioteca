package mx.tecnm.orizaba.tp.biblioteca.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Socio;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

import java.net.URL;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.ResourceBundle;

public class ReportesController implements Initializable {

    @FXML
    private ListView<String> ingresosList;

    @FXML
    private Label lblCantidad;

    @FXML
    private Label lblSuma;

    @FXML
    private Label lblPromedio;

    @FXML
    private Label lblMinimo;

    @FXML
    private Label lblMaximo;

    @FXML
    private Label lblMasDemandado;

    @FXML
    private Spinner<Integer> topSpinner;

    @FXML
    private ListView<String> mejoresList;

    @FXML
    private ListView<String> estadosList;

    @FXML
    private TextField umbralField;

    @FXML
    private Label lblMenoresTitulo;

    @FXML
    private Label lblMayoresTitulo;

    @FXML
    private ListView<String> menoresList;

    @FXML
    private ListView<String> mayoresList;

    private Biblioteca biblioteca;

    @Override
    public void initialize(
            URL location,
            ResourceBundle resources
    ) {

        topSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        10,
                        3
                )
        );
    }

    public void setBiblioteca(
            Biblioteca biblioteca
    ) {
        this.biblioteca = biblioteca;

        actualizarReportes();
    }

    @FXML
    private void actualizarReportes() {

        if (biblioteca == null) {
            return;
        }

        actualizarIngresos();
        actualizarEstadisticas();
        actualizarMasDemandandado();
        actualizarMejores();
        actualizarEstados();
        actualizarParticion();
    }

    private void actualizarIngresos() {

        ingresosList.getItems().clear();

        biblioteca.ingresosPorTipo()
                .forEach((tipo, ingreso) ->
                        ingresosList.getItems().add(
                                "%s: $%.2f".formatted(
                                        tipo,
                                        ingreso
                                )
                        )
                );

        if (ingresosList.getItems().isEmpty()) {
            ingresosList.getItems().add(
                    "No hay préstamos cerrados."
            );
        }
    }

    private void actualizarEstadisticas() {

        DoubleSummaryStatistics estadisticas =
                biblioteca.estadisticas();

        lblCantidad.setText(
                Long.toString(
                        estadisticas.getCount()
                )
        );

        lblSuma.setText(
                "$%.2f".formatted(
                        estadisticas.getSum()
                )
        );

        if (estadisticas.getCount() == 0) {

            lblPromedio.setText("-");
            lblMinimo.setText("-");
            lblMaximo.setText("-");

            return;
        }

        lblPromedio.setText(
                "$%.2f".formatted(
                        estadisticas.getAverage()
                )
        );

        lblMinimo.setText(
                "$%.2f".formatted(
                        estadisticas.getMin()
                )
        );

        lblMaximo.setText(
                "$%.2f".formatted(
                        estadisticas.getMax()
                )
        );
    }

    private void actualizarMasDemandandado() {

        String texto =
                biblioteca.masDemandado()
                        .map(material ->
                                material.clave()
                                        + " - "
                                        + material.nombre()
                        )
                        .orElse(
                                "No hay datos suficientes."
                        );

        lblMasDemandado.setText(texto);
    }

    @FXML
    private void actualizarMejores() {

        if (biblioteca == null) {
            return;
        }

        int cantidad = topSpinner.getValue();

        List<Socio> socios = biblioteca.mejores(cantidad);

        mejoresList.getItems().clear();

        for (int i = 0; i < socios.size(); i++) {

            Socio socio =
                    socios.get(i);

            mejoresList.getItems().add(
                    "%d. %s - %s".formatted(
                            i + 1,
                            socio.nombre(),
                            socio.nivel().etiqueta()
                    )
            );
        }

        if (socios.isEmpty()) {
            mejoresList.getItems().add(
                    "No hay datos suficientes."
            );
        }
    }

    private void actualizarEstados() {

        estadosList.getItems().clear();

        biblioteca.conteoPorEstado()
                .forEach((estado, cantidad) ->
                        estadosList.getItems().add(
                                "%s: %d".formatted(
                                        estado.etiqueta(),
                                        cantidad
                                )
                        )
                );

        if (estadosList.getItems().isEmpty()) {
            estadosList.getItems().add(
                    "No hay préstamos registrados."
            );
        }
    }

    @FXML
    private void actualizarParticion() {

        if (biblioteca == null) {
            return;
        }

        try {

            double umbral =
                    Double.parseDouble(
                            umbralField
                                    .getText()
                                    .trim()
                    );

            var particion =
                    biblioteca.particionarPorPrecio(
                            umbral
                    );

            List<Material> menores =
                    particion.getOrDefault(
                            false,
                            List.of()
                    );

            List<Material> mayores =
                    particion.getOrDefault(
                            true,
                            List.of()
                    );

            menoresList.getItems().setAll(
                    menores.stream()
                            .map(this::describirMaterial)
                            .toList()
            );

            mayoresList.getItems().setAll(
                    mayores.stream()
                            .map(this::describirMaterial)
                            .toList()
            );

            lblMenoresTitulo.setText(
                    "Menores a $%.2f (%d)"
                            .formatted(
                                    umbral,
                                    menores.size()
                            )
            );

            lblMayoresTitulo.setText(
                    "Iguales o mayores a $%.2f (%d)"
                            .formatted(
                                    umbral,
                                    mayores.size()
                            )
            );

        } catch (NumberFormatException e) {

            mostrarError("Introduce un umbral de precio válido.");
        }
    }

    private String describirMaterial(Material material) {

        return "%s - %s ($%.2f)"
                .formatted(
                        material.clave(),
                        material.nombre(),
                        material.precioBase()
                );
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Error");
        alerta.setHeaderText("No se pudo generar el reporte");

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}