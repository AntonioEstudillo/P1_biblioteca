package mx.tecnm.orizaba.tp.biblioteca.ui;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import mx.tecnm.orizaba.tp.biblioteca.modelo.Estado;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Material;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Prestamo;
import mx.tecnm.orizaba.tp.biblioteca.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.biblioteca.modelo.Socio;
import mx.tecnm.orizaba.tp.biblioteca.servicio.Biblioteca;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PrestamosController implements Initializable {

    // =========================================================
    // TABLA DE PRÉSTAMOS
    // =========================================================

    @FXML
    private TableView<Prestamo> tablaPrestamos;

    @FXML
    private TableColumn<Prestamo, Number> colId;

    @FXML
    private TableColumn<Prestamo, String> colSocio;

    @FXML
    private TableColumn<Prestamo, String> colFecha;

    @FXML
    private TableColumn<Prestamo, String> colEstado;

    @FXML
    private TableColumn<Prestamo, Number> colLineas;

    @FXML
    private TableColumn<Prestamo, String> colTotal;

    // =========================================================
    // TABLA DE LÍNEAS
    // =========================================================

    @FXML
    private TableView<Prestamo.Linea> tablaLineas;

    @FXML
    private TableColumn<Prestamo.Linea, String> colLineaClave;

    @FXML
    private TableColumn<Prestamo.Linea, String> colLineaMaterial;

    @FXML
    private TableColumn<Prestamo.Linea, Number> colLineaDias;

    @FXML
    private TableColumn<Prestamo.Linea, String> colLineaSubtotal;

    @FXML
    private TableColumn<Prestamo.Linea, String> colLineaImporte;

    // =========================================================
    // CONTROLES
    // =========================================================

    @FXML
    private ComboBox<Material> materialCombo;

    @FXML
    private Spinner<Integer> diasSpinner;

    @FXML
    private Label lblSocio;

    @FXML
    private Label lblEstado;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblTotal;

    @FXML
    private Button btnIniciar;

    @FXML
    private Button btnCerrar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnAgregarLinea;

    @FXML
    private Button btnQuitarLinea;

    private Biblioteca biblioteca;

    // =========================================================
    // INICIALIZACIÓN
    // =========================================================

    @Override
    public void initialize(
            URL location,
            ResourceBundle resources
    ) {

        configurarTablaPrestamos();
        configurarTablaLineas();
        configurarControles();

        tablaPrestamos
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, actual) ->
                                mostrarDetalle(actual)
                );

        tablaLineas
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, anterior, actual) ->
                                actualizarBotones()
                );

        actualizarBotones();
    }

    // =========================================================
    // TABLA PRINCIPAL
    // =========================================================

    private void configurarTablaPrestamos() {

        colId.setCellValueFactory(datos ->
                new ReadOnlyIntegerWrapper(
                        datos.getValue().id()
                )
        );

        colSocio.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue()
                                .socio()
                                .nombre()
                )
        );

        colFecha.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue()
                                .fecha()
                                .toString()
                )
        );

        colEstado.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue()
                                .estado()
                                .etiqueta()
                )
        );

        colLineas.setCellValueFactory(datos ->
                new ReadOnlyIntegerWrapper(
                        datos.getValue()
                                .lineas()
                                .size()
                )
        );

        colTotal.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        "$%.2f".formatted(
                                datos.getValue().total()
                        )
                )
        );
    }

    // =========================================================
    // TABLA DE LÍNEAS
    // =========================================================

    private void configurarTablaLineas() {

        colLineaClave.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue()
                                .elemento()
                                .clave()
                                .toString()
                )
        );

        colLineaMaterial.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        datos.getValue()
                                .elemento()
                                .nombre()
                )
        );

        colLineaDias.setCellValueFactory(datos ->
                new ReadOnlyIntegerWrapper(
                        datos.getValue()
                                .unidades()
                )
        );

        colLineaSubtotal.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        "$%.2f".formatted(
                                datos.getValue().subtotal()
                        )
                )
        );

        colLineaImporte.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(
                        "$%.2f".formatted(
                                datos.getValue().importe()
                        )
                )
        );
    }

    // =========================================================
    // OTROS CONTROLES
    // =========================================================

    private void configurarControles() {

        diasSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        1,
                        365,
                        1
                )
        );

        materialCombo.setConverter(
                new StringConverter<>() {

                    @Override
                    public String toString(
                            Material material
                    ) {
                        if (material == null) {
                            return "";
                        }

                        return material.clave()
                                + " - "
                                + material.nombre();
                    }

                    @Override
                    public Material fromString(String texto) {
                        return null;
                    }
                }
        );
    }

    // =========================================================
    // BIBLIOTECA
    // =========================================================

    public void setBiblioteca(Biblioteca biblioteca) {
        this.biblioteca = biblioteca;

        actualizarPrestamos();
    }

    // =========================================================
    // CARGAR DATOS
    // =========================================================

    @FXML
    private void actualizarPrestamos() {

        if (biblioteca == null) {
            return;
        }

        Prestamo seleccionado = tablaPrestamos.getSelectionModel().getSelectedItem();

        Integer idSeleccionado =
                seleccionado == null
                        ? null
                        : seleccionado.id();

        tablaPrestamos.getItems().setAll(biblioteca.registros());

        materialCombo.getItems().setAll(
                biblioteca.proyectar(material -> material));

        if (idSeleccionado != null) {
            seleccionarPrestamo(idSeleccionado);
        } else {
            mostrarDetalle(null);
        }
    }

    private void seleccionarPrestamo(int id) {

        tablaPrestamos
                .getItems()
                .stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .ifPresent(p -> tablaPrestamos.getSelectionModel().select(p));
    }

    // =========================================================
    // NUEVO PRÉSTAMO
    // =========================================================

    @FXML
    private void nuevoPrestamo() {

        List<Socio> socios = biblioteca.socios();

        if (socios.isEmpty()) {
            mostrarAdvertencia("Debes registrar al menos un socio antes de abrir un préstamo.");
            return;
        }

        ChoiceDialog<Socio> dialogo = new ChoiceDialog<>(socios.getFirst(), socios);

        dialogo.setTitle("Nuevo préstamo");

        dialogo.setHeaderText("Selecciona el socio");

        dialogo.setContentText("Socio:");

        dialogo.showAndWait().ifPresent(socio -> {

                    try {

                        Prestamo nuevo = biblioteca.abrir(socio.id());

                        actualizarPrestamos();

                        seleccionarPrestamo(nuevo.id());

                    } catch (ReglaNegocioException | IllegalArgumentException e) {

                        mostrarError(e.getMessage());
                    }
                });
    }

    // =========================================================
    // MOSTRAR DETALLE
    // =========================================================

    private void mostrarDetalle(
            Prestamo prestamo
    ) {

        if (prestamo == null) {

            tablaLineas
                    .getItems()
                    .clear();

            lblSocio.setText("-");
            lblEstado.setText("-");
            lblSubtotal.setText("$0.00");
            lblTotal.setText("$0.00");

            actualizarBotones();

            return;
        }

        tablaLineas.getItems().setAll(
                prestamo.lineas()
        );

        lblSocio.setText(
                prestamo.socio().nombre()
        );

        lblEstado.setText(
                prestamo.estado().etiqueta()
        );

        lblSubtotal.setText(
                "$%.2f".formatted(
                        prestamo.subtotal()
                )
        );

        lblTotal.setText(
                "$%.2f".formatted(
                        prestamo.total()
                )
        );

        actualizarBotones();
    }

    // =========================================================
    // AGREGAR LÍNEA
    // =========================================================

    @FXML
    private void agregarLinea() {

        Prestamo prestamo = prestamoSeleccionado();

        if (prestamo == null) {
            return;
        }

        Material material = materialCombo.getValue();

        if (material == null) {
            mostrarAdvertencia("Selecciona un material.");
            return;
        }

        int dias = diasSpinner.getValue();

        try {

            biblioteca.agregarLinea(
                    prestamo.id(),
                    material.clave().toString(),
                    dias
            );

            refrescarPrestamo(prestamo.id());

        } catch (ReglaNegocioException | IllegalArgumentException e) {

            mostrarError(e.getMessage());
        }
    }

    // =========================================================
    // QUITAR LÍNEA
    // =========================================================

    @FXML
    private void quitarLinea() {

        Prestamo prestamo = prestamoSeleccionado();

        Prestamo.Linea linea = tablaLineas.getSelectionModel().getSelectedItem();

        if (prestamo == null) {
            return;
        }

        if (linea == null) {
            mostrarAdvertencia("Selecciona una línea para eliminar.");
            return;
        }

        try {

            biblioteca.quitarLinea(prestamo.id(), linea.elemento().clave().toString());

            refrescarPrestamo(prestamo.id());

        } catch (ReglaNegocioException | IllegalArgumentException e) {

            mostrarError(e.getMessage());
        }
    }

    // =========================================================
    // ESTADOS
    // =========================================================

    @FXML
    private void iniciarPrestamo() {

        Prestamo prestamo = prestamoSeleccionado();

        if (prestamo == null) {
            return;
        }

        try {

            biblioteca.iniciar(prestamo.id());

            refrescarPrestamo(prestamo.id());

        } catch (ReglaNegocioException e) {

            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void cerrarPrestamo() {

        Prestamo prestamo = prestamoSeleccionado();

        if (prestamo == null) {
            return;
        }

        try {

            biblioteca.cerrar(prestamo.id());

            refrescarPrestamo(prestamo.id());

        } catch (ReglaNegocioException e) {

            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void cancelarPrestamo() {

        Prestamo prestamo = prestamoSeleccionado();

        if (prestamo == null) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);

        confirmacion.setTitle("Cancelar préstamo");

        confirmacion.setHeaderText("¿Deseas cancelar este préstamo?");

        confirmacion.setContentText(
                "Préstamo #" + prestamo.id());

        var respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {

            biblioteca.cancelar(prestamo.id());

            refrescarPrestamo(prestamo.id());

        } catch (ReglaNegocioException e) {

            mostrarError(e.getMessage());
        }
    }

    // =========================================================
    // ACTUALIZAR UN PRÉSTAMO
    // =========================================================

    private void refrescarPrestamo(int id) {

        actualizarPrestamos();

        seleccionarPrestamo(id);

        tablaPrestamos.refresh();

        Prestamo prestamo = biblioteca.registro(id);

        mostrarDetalle(prestamo);
    }

    // =========================================================
    // BOTONES SEGÚN ESTADO
    // =========================================================

    private void actualizarBotones() {

        Prestamo prestamo = tablaPrestamos.getSelectionModel().getSelectedItem();

        if (prestamo == null) {

            btnIniciar.setDisable(true);
            btnCerrar.setDisable(true);
            btnCancelar.setDisable(true);

            btnAgregarLinea.setDisable(true);
            btnQuitarLinea.setDisable(true);

            return;
        }

        Estado estado = prestamo.estado();

        boolean abierta = estado == Estado.ABIERTA;

        boolean enCurso = estado == Estado.EN_CURSO;

        btnIniciar.setDisable(!abierta);

        btnCerrar.setDisable(!enCurso);

        btnCancelar.setDisable(!(abierta || enCurso));

        btnAgregarLinea.setDisable(!abierta);

        btnQuitarLinea.setDisable(!abierta || tablaLineas.getSelectionModel().getSelectedItem() == null);
    }

    // =========================================================
    // AUXILIARES
    // =========================================================

    private Prestamo prestamoSeleccionado() {

        Prestamo prestamo = tablaPrestamos.getSelectionModel().getSelectedItem();

        if (prestamo == null) {

            mostrarAdvertencia("Selecciona primero un préstamo.");

            return null;
        }

        return prestamo;
    }

    private void mostrarAdvertencia(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.WARNING);

        alerta.setTitle("Biblioteca");

        alerta.setHeaderText("Atención");

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.ERROR);

        alerta.setTitle("Error");

        alerta.setHeaderText("No se pudo completar la operación");

        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}