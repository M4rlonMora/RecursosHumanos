package org.example.ui;

import org.example.config.Validator;
import org.example.dao.FuncionarioDAO;
import org.example.dao.impl.FuncionarioImpl;
import org.example.model.Funcionario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FuncionarioForm extends JFrame {

    private FuncionarioDAO dao = new FuncionarioImpl();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtId;
    private JTextField txtTipo;
    private JTextField txtNumero;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JTextField txtTelefono;
    private JTextField txtFecha; // formato yyyy-MM-dd
    private JTextField txtDireccion;
    private JComboBox<String> txtSexo;
    private JComboBox<String> txtEstadoCivil; // nuevo

    public FuncionarioForm() {
        setTitle("Gestión de Funcionarios - RRHH");
        setSize(950, 540);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        loadData();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // columnas: añadimos Estado Civil
        tableModel = new DefaultTableModel(
                new Object[]{"ID","Tipo","Número","Nombres","Apellidos","Estado Civil","Teléfono","Dirección","Fecha Nac."}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);

        // formulario: ahora con 10 filas (ID, Tipo, Número, Nombres, Apellidos, Estado Civil, Teléfono, Dirección, Fecha, Sexo)
        JPanel form = new JPanel(new GridLayout(10,2,6,6));
        form.setBorder(BorderFactory.createTitledBorder("Datos del Funcionario"));
        form.setPreferredSize(new Dimension(340, 0));
        form.setBackground(Color.WHITE);
        form.setFont(new Font("SansSerif", Font.PLAIN, 12));
        form.setBorder(new EmptyBorder(8,8,8,8));

        txtId = new JTextField(); txtId.setEditable(false);
        txtTipo = new JTextField("Cédula");
        txtNumero = new JTextField();
        txtNombres = new JTextField();
        txtApellidos = new JTextField();
        txtEstadoCivil = new JComboBox<>(new String[]{"Soltero", "Casado", "Divorciado", "Viudo", "Otro"}); // nuevo
        txtTelefono = new JTextField();
        txtDireccion = new JTextField();
        txtFecha = new JTextField();
        txtSexo = new JComboBox<>(new String[]{"M","F","Otro"});

        form.add(new JLabel("ID:")); form.add(txtId);
        form.add(new JLabel("Tipo ID:")); form.add(txtTipo);
        form.add(new JLabel("Número ID:")); form.add(txtNumero);
        form.add(new JLabel("Nombres:")); form.add(txtNombres);
        form.add(new JLabel("Apellidos:")); form.add(txtApellidos);
        form.add(new JLabel("Estado Civil:")); form.add(txtEstadoCivil); // agregado
        form.add(new JLabel("Teléfono:")); form.add(txtTelefono);
        form.add(new JLabel("Dirección:")); form.add(txtDireccion);
        form.add(new JLabel("Fecha Nac (YYYY-MM-DD):")); form.add(txtFecha);
        form.add(new JLabel("Sexo:")); form.add(txtSexo);

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEditar = new JButton("Cargar para editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar lista");

        JPanel botones = new JPanel();
        botones.add(btnNuevo); botones.add(btnGuardar); botones.add(btnEditar); botones.add(btnEliminar); botones.add(btnRefrescar);

        getContentPane().setLayout(new BorderLayout(10,10));
        getContentPane().add(scroll, BorderLayout.CENTER);
        getContentPane().add(form, BorderLayout.EAST);
        getContentPane().add(botones, BorderLayout.SOUTH);

        // Estética: ajustar anchos de columnas
        table.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
        table.getColumnModel().getColumn(3).setPreferredWidth(160);// Nombres
        table.getColumnModel().getColumn(4).setPreferredWidth(140); // Apellidos
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Teléfono
        table.getColumnModel().getColumn(7).setPreferredWidth(180); // Dirección

        // Botón por defecto (Enter)
        getRootPane().setDefaultButton(btnGuardar);

        btnNuevo.addActionListener(e -> clearForm());

        btnGuardar.addActionListener(e -> {
            try {
                Funcionario f = new Funcionario();
                if (!txtId.getText().isEmpty()) f.setId(Integer.valueOf(txtId.getText()));
                f.setTipoIdentificacion(txtTipo.getText());
                f.setNumeroIdentificacion(txtNumero.getText());
                f.setNombres(txtNombres.getText());
                f.setApellidos(txtApellidos.getText());
                f.setEstadoCivil(txtEstadoCivil.getSelectedItem() != null ? txtEstadoCivil.getSelectedItem().toString() : null); // set estado civil
                f.setTelefono(txtTelefono.getText());
                f.setDireccion(txtDireccion.getText());
                f.setSexo(txtSexo.getSelectedItem() != null ? txtSexo.getSelectedItem().toString() : null);
                if (!txtFecha.getText().isEmpty()) {
                    try {
                        f.setFechaNacimiento(LocalDate.parse(txtFecha.getText()));
                    } catch (DateTimeParseException ex) {
                        JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD");
                        return;
                    }
                }

                // VALIDACIÓN
                List<String> errores = Validator.validarFuncionario(f);
                if (!errores.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Corrige los siguientes errores:\\n");
                    for (String err : errores) {
                        sb.append(" - ").append(err).append("\\n");
                    }
                    JOptionPane.showMessageDialog(this, sb.toString(), "Errores de validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Persistir
                if (f.getId() == null) {
                    dao.guardar(f);
                    JOptionPane.showMessageDialog(this, "Funcionario guardado.");
                } else {
                    dao.actualizar(f);
                    JOptionPane.showMessageDialog(this, "Funcionario actualizado.");
                }
                loadData();
                clearForm();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        btnEditar.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione un funcionario de la lista"); return; }
            Integer id = (Integer) tableModel.getValueAt(r, 0);
            try {
                Funcionario f = dao.buscarPorId(id);
                if (f != null) {
                    txtId.setText(String.valueOf(f.getId()));
                    txtTipo.setText(f.getTipoIdentificacion());
                    txtNumero.setText(f.getNumeroIdentificacion());
                    txtNombres.setText(f.getNombres());
                    txtApellidos.setText(f.getApellidos());
                    txtEstadoCivil.setSelectedItem(f.getEstadoCivil() != null ? f.getEstadoCivil() : "Soltero"); // cargar estado civil
                    txtTelefono.setText(f.getTelefono());
                    txtDireccion.setText(f.getDireccion() != null ? f.getDireccion() : "");
                    txtFecha.setText(f.getFechaNacimiento() != null ? f.getFechaNacimiento().toString() : "");
                    if (f.getSexo() != null) txtSexo.setSelectedItem(f.getSexo());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage());
            }
        });

        btnEliminar.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione un funcionario de la lista"); return; }
            int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar funcionario seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            Integer id = (Integer) tableModel.getValueAt(r, 0);
            try {
                dao.eliminar(id);
                loadData();
                JOptionPane.showMessageDialog(this, "Eliminado.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage());
            }
        });

        btnRefrescar.addActionListener(e -> loadData());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    btnEditar.doClick();
                }
            }
        });
    }

    private void loadData() {
        try {
            List<Funcionario> lista = dao.listarTodos();
            tableModel.setRowCount(0);
            for (Funcionario f : lista) {
                tableModel.addRow(new Object[]{
                        f.getId(),
                        f.getTipoIdentificacion(),
                        f.getNumeroIdentificacion(),
                        f.getNombres(),
                        f.getApellidos(),
                        f.getEstadoCivil() != null ? f.getEstadoCivil() : "", // mostrar estado civil
                        f.getTelefono(),
                        f.getDireccion() != null ? f.getDireccion() : "",
                        f.getFechaNacimiento() != null ? f.getFechaNacimiento().toString() : ""
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error cargando datos: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtTipo.setText("Cédula");
        txtNumero.setText("");
        txtNombres.setText("");
        txtApellidos.setText("");
        txtEstadoCivil.setSelectedIndex(0); // limpiar estado civil
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtFecha.setText("");
        txtSexo.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FuncionarioForm f = new FuncionarioForm();
            f.setVisible(true);
        });
    }
}
