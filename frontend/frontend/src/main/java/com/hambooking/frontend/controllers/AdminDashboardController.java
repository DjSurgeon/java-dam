package com.hambooking.frontend.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del dashboard de administrador.
 *
 * fx:id declarados en admin-dashboard.fxml:
 *
 *   Sidebar:
 *     - sidebarUserName
 *   Cabecera:
 *     - pageTitle, pageBreadcrumb, searchField, btnNuevo
 *   KPIs:
 *     - kpiCortadores, kpiReservasHoy, kpiClientes, kpiPendientes
 *   TabPane:
 *     - mainTabPane
 *     - tabCortadores, tabUsuarios, tabReservas, tabNotificaciones, tabEstadisticas
 *   Tabla cortadores:
 *     - cortadoresTable, cColNombre, cColDni, cColEmail,
 *       cColEspecialidad, cColExperiencia, cColEstado, cColAcciones
 *   Tabla usuarios:
 *     - usuariosTable, uColNombre, uColDni, uColEmail,
 *       uColTelefono, uColEstado, uColAcciones
 *   Tabla reservas:
 *     - reservasTable, rColFecha, rColCliente, rColCortador,
 *       rColServicio, rColHora, rColEstado, rColAcciones
 *   Tabla notificaciones:
 *     - notificacionesTable, nColFecha, nColDestinatario, nColTipo, nColAsunto
 *
 * onAction declarados:
 *   - #showTabCortadores, #showTabUsuarios, #showTabReservas
 *   - #showTabNotificaciones, #showTabEstadisticas
 *   - #handleNuevo, #handleLogout
 */
public class AdminDashboardController implements Initializable {

    // ── Sidebar ──────────────────────────────────────────────────
    @FXML private Label sidebarUserName;

    // ── Cabecera ─────────────────────────────────────────────────
    @FXML private Label     pageTitle;
    @FXML private Label     pageBreadcrumb;
    @FXML private TextField searchField;

    // ── KPIs ─────────────────────────────────────────────────────
    @FXML private Label kpiCortadores;
    @FXML private Label kpiReservasHoy;
    @FXML private Label kpiClientes;
    @FXML private Label kpiPendientes;

    // ── TabPane ───────────────────────────────────────────────────
    @FXML private TabPane mainTabPane;
    @FXML private Tab     tabCortadores;
    @FXML private Tab     tabUsuarios;
    @FXML private Tab     tabReservas;
    @FXML private Tab     tabNotificaciones;
    @FXML private Tab     tabEstadisticas;

    // ── Tabla cortadores ─────────────────────────────────────────
    @FXML private TableView<String[]>           cortadoresTable;
    @FXML private TableColumn<String[], String> cColNombre;
    @FXML private TableColumn<String[], String> cColDni;
    @FXML private TableColumn<String[], String> cColEmail;
    @FXML private TableColumn<String[], String> cColEspecialidad;
    @FXML private TableColumn<String[], String> cColExperiencia;
    @FXML private TableColumn<String[], String> cColEstado;
    @FXML private TableColumn<String[], String> cColAcciones;

    // ── Tabla usuarios ───────────────────────────────────────────
    @FXML private TableView<String[]>           usuariosTable;
    @FXML private TableColumn<String[], String> uColNombre;
    @FXML private TableColumn<String[], String> uColDni;
    @FXML private TableColumn<String[], String> uColEmail;
    @FXML private TableColumn<String[], String> uColTelefono;
    @FXML private TableColumn<String[], String> uColEstado;
    @FXML private TableColumn<String[], String> uColAcciones;

    // ── Tabla reservas ───────────────────────────────────────────
    @FXML private TableView<String[]>           reservasTable;
    @FXML private TableColumn<String[], String> rColFecha;
    @FXML private TableColumn<String[], String> rColCliente;
    @FXML private TableColumn<String[], String> rColCortador;
    @FXML private TableColumn<String[], String> rColServicio;
    @FXML private TableColumn<String[], String> rColHora;
    @FXML private TableColumn<String[], String> rColEstado;
    @FXML private TableColumn<String[], String> rColAcciones;

    // ── Tabla notificaciones ─────────────────────────────────────
    @FXML private TableView<String[]>           notificacionesTable;
    @FXML private TableColumn<String[], String> nColFecha;
    @FXML private TableColumn<String[], String> nColDestinatario;
    @FXML private TableColumn<String[], String> nColTipo;
    @FXML private TableColumn<String[], String> nColAsunto;

    // ── Inicializacion ───────────────────────────────────────────
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTablaCortadores();
        configurarTablaUsuarios();
        configurarTablaReservas();
        configurarTablaNotificaciones();
        cargarDatosEjemplo();
        actualizarKpis();
    }

    // ── Configuracion de columnas ────────────────────────────────

    private void configurarTablaCortadores() {
        cColNombre.setCellValueFactory(      d -> new SimpleStringProperty(d.getValue()[0]));
        cColDni.setCellValueFactory(         d -> new SimpleStringProperty(d.getValue()[1]));
        cColEmail.setCellValueFactory(       d -> new SimpleStringProperty(d.getValue()[2]));
        cColEspecialidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        cColExperiencia.setCellValueFactory( d -> new SimpleStringProperty(d.getValue()[4]));
        cColEstado.setCellValueFactory(      d -> new SimpleStringProperty(d.getValue()[5]));
        cColAcciones.setCellValueFactory(    d -> new SimpleStringProperty("Editar | Desactivar"));
    }

    private void configurarTablaUsuarios() {
        uColNombre.setCellValueFactory(  d -> new SimpleStringProperty(d.getValue()[0]));
        uColDni.setCellValueFactory(     d -> new SimpleStringProperty(d.getValue()[1]));
        uColEmail.setCellValueFactory(   d -> new SimpleStringProperty(d.getValue()[2]));
        uColTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        uColEstado.setCellValueFactory(  d -> new SimpleStringProperty(d.getValue()[4]));
        uColAcciones.setCellValueFactory(d -> new SimpleStringProperty("Activar | Desactivar"));
    }

    private void configurarTablaReservas() {
        rColFecha.setCellValueFactory(   d -> new SimpleStringProperty(d.getValue()[0]));
        rColCliente.setCellValueFactory( d -> new SimpleStringProperty(d.getValue()[1]));
        rColCortador.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        rColServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        rColHora.setCellValueFactory(    d -> new SimpleStringProperty(d.getValue()[4]));
        rColEstado.setCellValueFactory(  d -> new SimpleStringProperty(d.getValue()[5]));
        rColAcciones.setCellValueFactory(d -> new SimpleStringProperty("Ver | Cancelar"));
    }

    private void configurarTablaNotificaciones() {
        nColFecha.setCellValueFactory(        d -> new SimpleStringProperty(d.getValue()[0]));
        nColDestinatario.setCellValueFactory( d -> new SimpleStringProperty(d.getValue()[1]));
        nColTipo.setCellValueFactory(         d -> new SimpleStringProperty(d.getValue()[2]));
        nColAsunto.setCellValueFactory(       d -> new SimpleStringProperty(d.getValue()[3]));
    }

    // ── Datos de ejemplo ─────────────────────────────────────────
    // TODO Issue #36: sustituir por llamadas reales a la API

    private void cargarDatosEjemplo() {
        // Cortadores
        cortadoresTable.getItems().addAll(
                new String[]{"Carlos Martinez", "33333333P", "carlos@hambooking.com", "Jamon Iberico", "5 anos", "Activo"},
                new String[]{"Ana Lopez",       "55555555K", "ana@hambooking.com",    "Paleta",        "3 anos", "Activo"},
                new String[]{"Pedro Ruiz",      "66666666M", "pedro@hambooking.com",  "Todos",         "8 anos", "Activo"},
                new String[]{"Maria Sanchez",   "77777777T", "maria@hambooking.com",  "Embutidos",     "2 anos", "Inactivo"}
        );

        // Usuarios
        usuariosTable.getItems().addAll(
                new String[]{"Juan Garcia",  "12345678A", "juan@example.com",  "612345678", "Activo"},
                new String[]{"Laura Perez",  "87654321B", "laura@example.com", "698765432", "Activo"},
                new String[]{"Miguel Torres","11111111C", "miguel@example.com","611111111", "Activo"}
        );

        // Reservas
        reservasTable.getItems().addAll(
                new String[]{"22 Ene 2026", "Juan Garcia",  "Carlos Martinez", "Corte de Jamon",   "10:00-12:00", "Confirmada"},
                new String[]{"22 Ene 2026", "Laura Perez",  "Ana Lopez",       "Corte de Paleta",  "11:00-12:00", "Confirmada"},
                new String[]{"24 Ene 2026", "Juan Garcia",  "Ana Lopez",       "Corte de Paleta",  "14:30-15:30", "Pendiente"},
                new String[]{"10 Ene 2026", "Miguel Torres","Pedro Ruiz",      "Corte de Embutido","16:30-17:00", "Realizada"}
        );

        // Notificaciones
        notificacionesTable.getItems().addAll(
                new String[]{"22 Ene 2026 10:05", "juan@example.com",        "CREATED",   "Reserva Confirmada - Corte de Jamon"},
                new String[]{"22 Ene 2026 10:05", "carlos@hambooking.com",   "CREATED",   "Nueva reserva asignada el 22 Ene"},
                new String[]{"22 Ene 2026 10:05", "admin@hambooking.com",    "CREATED",   "Nueva reserva: Juan Garcia - 22 Ene"},
                new String[]{"10 Ene 2026 16:35", "miguel@example.com",      "CANCELLED", "Reserva cancelada - Corte de Embutido"}
        );
    }

    private void actualizarKpis() {
        kpiCortadores.setText("3");
        kpiReservasHoy.setText("2");
        kpiClientes.setText("3");
        kpiPendientes.setText("1");
    }

    // ── Navegacion entre tabs ────────────────────────────────────

    @FXML
    private void showTabCortadores() {
        mainTabPane.getSelectionModel().select(tabCortadores);
        pageTitle.setText("Gestion de Cortadores");
        pageBreadcrumb.setText("Inicio · Cortadores");
    }

    @FXML
    private void showTabUsuarios() {
        mainTabPane.getSelectionModel().select(tabUsuarios);
        pageTitle.setText("Gestion de Usuarios");
        pageBreadcrumb.setText("Inicio · Usuarios");
    }

    @FXML
    private void showTabReservas() {
        mainTabPane.getSelectionModel().select(tabReservas);
        pageTitle.setText("Todas las Reservas");
        pageBreadcrumb.setText("Inicio · Reservas");
    }

    @FXML
    private void showTabNotificaciones() {
        mainTabPane.getSelectionModel().select(tabNotificaciones);
        pageTitle.setText("Notificaciones");
        pageBreadcrumb.setText("Inicio · Notificaciones");
    }

    @FXML
    private void showTabEstadisticas() {
        mainTabPane.getSelectionModel().select(tabEstadisticas);
        pageTitle.setText("Estadisticas");
        pageBreadcrumb.setText("Inicio · Estadisticas");
    }

    // ── Acciones ─────────────────────────────────────────────────

    /**
     * Boton "+ Nuevo" de la cabecera.
     * Abre el formulario correspondiente al tab activo.
     * TODO Issue #36: abrir dialogo de creacion.
     */
    @FXML
    private void handleNuevo() {
        Tab tabActivo = mainTabPane.getSelectionModel().getSelectedItem();
        if (tabActivo == tabCortadores) {
            System.out.println("TODO: abrir dialogo nuevo cortador");
        } else if (tabActivo == tabUsuarios) {
            System.out.println("TODO: abrir dialogo nuevo usuario");
        }
    }

    @FXML
    private void handleLogout() {
        navigateTo("/com/hambooking/frontend/fxml/login.fxml", "HamBooking - Iniciar sesion");
    }

    // ── Utilidades ───────────────────────────────────────────────

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}