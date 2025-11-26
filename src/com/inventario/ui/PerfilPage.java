package com.inventario.ui;

import com.inventario.config.AuthService;
import com.inventario.config.ConexionBD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PerfilPage extends BorderPane {

    private final String usuarioActual;
    private final String rolActual;
    private final String nombreActual;

    // Datos para la tabla de usuarios
    private TableView<UsuarioModel> tablaUsuarios;
    private ObservableList<UsuarioModel> listaUsuarios;
    private Integer idUsuarioEditando = null; // Para saber si editamos o creamos

    // Campos del formulario Usuario
    private TextField txtNombre, txtDNI, txtTel, txtUser, txtTurno, txtHorario;
    private PasswordField txtPass;
    private ComboBox<String> cmbRol;
    private Button btnGuardarUser, btnEliminarUser;

    // Estilos CSS
    private static final String CSS_STYLES = """
        .root { -fx-background-color: #FDF8F0; -fx-font-family: 'Segoe UI'; }
        .header-title { -fx-font-size: 2.2em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .tab-content-area { -fx-padding: 20 0 0 0; }
        .tab-pane .tab-header-area .tab-header-background { -fx-background-color: transparent; }
        .tab-pane .tab { -fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 8 15 8 15; -fx-font-size: 1.1em; }
        .tab-pane .tab:selected { -fx-background-color: transparent; -fx-border-color: #4A90E2; -fx-border-width: 0 0 3 0; -fx-text-fill: #4A90E2; -fx-font-weight: bold; }
        .card { -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 8; -fx-padding: 20px; }
        .card-title { -fx-font-size: 1.4em; -fx-font-weight: bold; -fx-text-fill: #333333; }
        .label { -fx-font-size: 1.05em; -fx-font-weight: 500; -fx-text-fill: #333333; }
        .combo-box, .text-field { -fx-font-size: 1.05em; -fx-pref-height: 38px; -fx-border-color: #CCCCCC; -fx-border-radius: 5; }
        .button-primary { -fx-background-color: #4A90E2; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 40px; -fx-background-radius: 5; -fx-cursor: hand; }
        .button-add { -fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 38px; -fx-cursor: hand; }
        .button-danger { -fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 1.4em; -fx-cursor: hand; }
        .button-delete-card { -fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 5 10; }
        .table-view .column-header { -fx-background-color: #F9FAFB; -fx-font-weight: bold; }
    """;

    public PerfilPage(String usuario) {
        this.usuarioActual = usuario;
        this.nombreActual = AuthService.obtenerNombre(usuario);
        this.rolActual = AuthService.obtenerTipoEmpleado(usuario);

        this.getStylesheets().add("data:text/css," + CSS_STYLES.replace("\n", ""));
        this.getStyleClass().add("root");
        setPadding(new Insets(20));

        // --- Encabezado con Tarjeta de Perfil ---
        VBox topContainer = new VBox(15);
        topContainer.getChildren().addAll(crearHeader(), crearTarjetaPerfil());
        setTop(topContainer);
        BorderPane.setMargin(topContainer, new Insets(0,0,15,0));

        // --- Pestañas ---
        TabPane tabs = new TabPane();

        // Pestaña 1: Gestión de Usuarios (Solo si es Admin)
        if (esAdministrador()) {
            Tab tabUsuarios = new Tab("👥 Gestión de Usuarios", crearPanelGestionUsuarios());
            tabUsuarios.setClosable(false);
            tabs.getTabs().add(tabUsuarios);
        } else {
            // Si no es admin, solo mensaje
            Tab tabInfo = new Tab("Información", new Label("  No tienes permisos para gestionar usuarios."));
            tabInfo.setClosable(false);
            tabs.getTabs().add(tabInfo);
        }

        // Pestaña 2: Gestión de Proveedores (Incrustamos la nueva clase)
        ProveedorPage paginaProveedores = new ProveedorPage();
        // Ajustamos padding para que encaje bien dentro del tab
        paginaProveedores.setPadding(new Insets(10));
        Tab tabProveedores = new Tab("🚚 Gestión de Proveedores", paginaProveedores);
        tabProveedores.setClosable(false);

        tabs.getTabs().add(tabProveedores);

        setCenter(tabs);
    }

    private boolean esAdministrador() {
        return "Administrador".equalsIgnoreCase(rolActual) || "admin".equals(usuarioActual);
    }

    private Node crearHeader() {
        Label l = new Label("Administración del Sistema");
        l.getStyleClass().add("header-title");
        return l;
    }

    private Node crearTarjetaPerfil() {
        HBox card = new HBox(15);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        StackPane avatar = new StackPane();
        Circle bg = new Circle(25, Color.web("#4A90E2"));
        String inicial = (nombreActual != null && !nombreActual.isEmpty()) ? nombreActual.substring(0, 1) : "?";
        Label lblInicial = new Label(inicial); lblInicial.setTextFill(Color.WHITE); lblInicial.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        avatar.getChildren().addAll(bg, lblInicial);

        VBox info = new VBox(2);
        Label lblNombre = new Label(nombreActual + " (" + rolActual + ")");
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 1.1em;");
        Label lblUser = new Label("Sesión activa: " + usuarioActual);
        lblUser.setStyle("-fx-text-fill: #777;");
        info.getChildren().addAll(lblNombre, lblUser);

        card.getChildren().addAll(avatar, info);
        return card;
    }

    // --- PANEL GESTIÓN USUARIOS (TABLA + FORMULARIO) ---
    private Node crearPanelGestionUsuarios() {
        HBox layout = new HBox(20);
        layout.setPadding(new Insets(15, 0, 0, 0));

        // 1. Tabla de Usuarios (Izquierda)
        tablaUsuarios = new TableView<>();
        listaUsuarios = FXCollections.observableArrayList();
        configurarTablaUsuarios();
        cargarUsuarios();

        tablaUsuarios.setPrefWidth(500);
        VBox.setVgrow(tablaUsuarios, Priority.ALWAYS); // Crece verticalmente

        // 2. Formulario (Derecha)
        VBox form = new VBox(10);
        form.getStyleClass().add("card");
        form.setPrefWidth(350);

        Label lblForm = new Label("Datos del Usuario");
        lblForm.setStyle("-fx-font-size: 1.2em; -fx-font-weight: bold;");

        txtNombre = new TextField(); txtNombre.setPromptText("Nombre Completo");
        txtDNI = new TextField(); txtDNI.setPromptText("DNI");
        txtTel = new TextField(); txtTel.setPromptText("Teléfono");
        cmbRol = new ComboBox<>();
        cmbRol.getItems().addAll("Administrador", "Cocinero", "Almacenero", "Vendedor");
        cmbRol.setPromptText("Rol"); cmbRol.setMaxWidth(Double.MAX_VALUE);

        txtTurno = new TextField(); txtTurno.setPromptText("Turno");
        txtHorario = new TextField(); txtHorario.setPromptText("Horario");

        txtUser = new TextField(); txtUser.setPromptText("Usuario Login");
        txtPass = new PasswordField(); txtPass.setPromptText("Contraseña");

        btnGuardarUser = new Button("Crear Usuario");
        btnGuardarUser.getStyleClass().add("button-primary");
        btnGuardarUser.setMaxWidth(Double.MAX_VALUE);
        btnGuardarUser.setOnAction(e -> guardarUsuario());

        Button btnLimpiar = new Button("Limpiar / Nuevo");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarFormularioUser());

        btnEliminarUser = new Button("Eliminar Seleccionado");
        btnEliminarUser.getStyleClass().add("button-danger");
        btnEliminarUser.setMaxWidth(Double.MAX_VALUE);
        btnEliminarUser.setDisable(true);
        btnEliminarUser.setOnAction(e -> eliminarUsuario());

        form.getChildren().addAll(lblForm, new Separator(),
                new Label("Personal:"), txtNombre, txtDNI, txtTel,
                new Label("Puesto:"), cmbRol, txtTurno, txtHorario,
                new Label("Acceso:"), txtUser, txtPass,
                new Separator(), btnGuardarUser, btnLimpiar, btnEliminarUser);

        // Listener Tabla
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) cargarUsuarioEnFormulario(val);
        });

        layout.getChildren().addAll(tablaUsuarios, form);
        HBox.setHgrow(tablaUsuarios, Priority.ALWAYS);

        return layout;
    }

    private void configurarTablaUsuarios() {
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<UsuarioModel, String> c1 = new TableColumn<>("Nombre"); c1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<UsuarioModel, String> c2 = new TableColumn<>("Rol"); c2.setCellValueFactory(new PropertyValueFactory<>("rol"));
        TableColumn<UsuarioModel, String> c3 = new TableColumn<>("Usuario"); c3.setCellValueFactory(new PropertyValueFactory<>("user"));
        tablaUsuarios.getColumns().addAll(c1, c2, c3);
    }

    private void cargarUsuarios() {
        listaUsuarios.clear();
        try (Connection c = ConexionBD.getConnection(); ResultSet rs = c.createStatement().executeQuery("SELECT * FROM empleado")) {
            while(rs.next()) {
                listaUsuarios.add(new UsuarioModel(
                        rs.getInt("IdEmpleado"), rs.getString("Nombre_y_Apellido"), rs.getString("Tipo_de_empleado"),
                        rs.getString("Telefono"), rs.getString("DNI"), rs.getString("Turnos"),
                        rs.getString("Horario"), rs.getString("user"), rs.getString("password")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        tablaUsuarios.setItems(listaUsuarios);
    }

    private void cargarUsuarioEnFormulario(UsuarioModel u) {
        idUsuarioEditando = u.getId();
        txtNombre.setText(u.getNombre()); txtDNI.setText(u.getDni()); txtTel.setText(u.getTelefono());
        cmbRol.setValue(u.getRol()); txtTurno.setText(u.getTurno()); txtHorario.setText(u.getHorario());
        txtUser.setText(u.getUser()); txtPass.setText(u.getPass());

        btnGuardarUser.setText("Actualizar Usuario");
        btnEliminarUser.setDisable(false);
    }

    private void limpiarFormularioUser() {
        idUsuarioEditando = null;
        txtNombre.clear(); txtDNI.clear(); txtTel.clear(); cmbRol.getSelectionModel().clearSelection();
        txtTurno.clear(); txtHorario.clear(); txtUser.clear(); txtPass.clear();

        btnGuardarUser.setText("Crear Usuario");
        btnEliminarUser.setDisable(true);
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    private void guardarUsuario() {
        if(txtNombre.getText().isEmpty() || txtUser.getText().isEmpty() || txtPass.getText().isEmpty() || cmbRol.getValue() == null) {
            mostrarAlerta("Error", "Faltan datos obligatorios."); return;
        }

        // REGLA: Solo 1 admin en BD
        if ("Administrador".equals(cmbRol.getValue())) {
            if (AuthService.existeAdministradorEnBD()) {
                // Si estamos editando AL MISMO admin, permitimos. Si es otro o nuevo, bloqueamos.
                // Buscamos si el admin existente es diferente al que editamos
                boolean esElMismo = false;
                if (idUsuarioEditando != null) {
                    // Chequeo simple: si ya hay admin, verificamos si yo soy ese admin
                    // (Para simplificar, asumimos que si editas y eres admin, está ok. Si creas nuevo, no).
                    // Pero la regla estricta era "Solo 1 admin".
                    // Si estoy creando uno nuevo (id == null), bloqueo.
                    if (idUsuarioEditando == null) {
                        mostrarAlerta("Bloqueado", "Ya existe un Administrador. No se puede crear otro.");
                        return;
                    }
                } else {
                    mostrarAlerta("Bloqueado", "Ya existe un Administrador.");
                    return;
                }
            }
        }

        try (Connection c = ConexionBD.getConnection()) {
            if (idUsuarioEditando == null) {
                // INSERT
                String sql = "INSERT INTO empleado (Tipo_de_empleado, Nombre_y_Apellido, Telefono, DNI, Turnos, Horario, user, password) VALUES (?,?,?,?,?,?,?,?)";
                PreparedStatement ps = c.prepareStatement(sql);
                ps.setString(1, cmbRol.getValue()); ps.setString(2, txtNombre.getText()); ps.setString(3, txtTel.getText());
                ps.setString(4, txtDNI.getText()); ps.setString(5, txtTurno.getText()); ps.setString(6, txtHorario.getText());
                ps.setString(7, txtUser.getText()); ps.setString(8, txtPass.getText());
                ps.executeUpdate();
                mostrarAlerta("Éxito", "Usuario creado.");
            } else {
                // UPDATE
                String sql = "UPDATE empleado SET Tipo_de_empleado=?, Nombre_y_Apellido=?, Telefono=?, DNI=?, Turnos=?, Horario=?, user=?, password=? WHERE IdEmpleado=?";
                PreparedStatement ps = c.prepareStatement(sql);
                ps.setString(1, cmbRol.getValue()); ps.setString(2, txtNombre.getText()); ps.setString(3, txtTel.getText());
                ps.setString(4, txtDNI.getText()); ps.setString(5, txtTurno.getText()); ps.setString(6, txtHorario.getText());
                ps.setString(7, txtUser.getText()); ps.setString(8, txtPass.getText()); ps.setInt(9, idUsuarioEditando);
                ps.executeUpdate();
                mostrarAlerta("Éxito", "Usuario actualizado.");
            }
            limpiarFormularioUser();
            cargarUsuarios();
        } catch (Exception e) { mostrarAlerta("Error", e.getMessage()); }
    }

    private void eliminarUsuario() {
        if(idUsuarioEditando == null) return;
        if("Administrador".equals(cmbRol.getValue())) {
            mostrarAlerta("Error", "No puedes eliminar al Administrador principal."); return;
        }

        try(Connection c = ConexionBD.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM empleado WHERE IdEmpleado=?")) {
            ps.setInt(1, idUsuarioEditando);
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Usuario eliminado.");
            limpiarFormularioUser();
            cargarUsuarios();
        } catch(Exception e) { mostrarAlerta("Error", e.getMessage()); }
    }

    private void mostrarAlerta(String t, String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }

    // Modelo Usuario para Tabla
    public static class UsuarioModel {
        int id; String nombre, rol, tel, dni, turno, horario, user, pass;
        public UsuarioModel(int id, String n, String r, String t, String d, String tu, String h, String u, String p) {
            this.id=id; nombre=n; rol=r; tel=t; dni=d; turno=tu; horario=h; user=u; pass=p;
        }
        public int getId() { return id; } public String getNombre() { return nombre; } public String getRol() { return rol; }
        public String getTelefono() { return tel; } public String getDni() { return dni; } public String getTurno() { return turno; }
        public String getHorario() { return horario; } public String getUser() { return user; } public String getPass() { return pass; }
    }
}