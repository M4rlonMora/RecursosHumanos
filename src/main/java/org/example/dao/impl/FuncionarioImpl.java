package org.example.dao.impl;

import org.example.config.ConnectionConfig;
import org.example.dao.FuncionarioDAO;
import org.example.model.Funcionario;
import org.example.model.GrupoFamiliar;
import org.example.model.InformacionAcademica;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioImpl implements FuncionarioDAO {

    @Override
    public void guardar(Funcionario f) throws Exception {
        String sqlF= "INSERT INTO funcionarios(tipo_identificacion, numero_identificacion, nombres, apellidos, estado_civil, sexo, direccion, telefono, fecha_nacimiento) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlGF="INSERT INTO grupo_familiar(funcionario_id, nombre, fecha_nacimiento, parentesco, ocupacion, telefono) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlAC="NSERT INTO informacion_academica(funcionario_id, universidad, nivel_estudio, titulo, anio_graduacion) VALUES (?, ?, ?, ?, ?)";

        try(Connection conn= ConnectionConfig.getConnection()){
            conn.setAutoCommit(false);
            try(PreparedStatement ps= conn.prepareStatement(sqlF,Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, f.getTipoIdentificacion());
                ps.setString(2, f.getNumeroIdentificacion());
                ps.setString(3, f.getNombres());
                ps.setString(4, f.getApellidos());
                ps.setString(5, f.getEstadoCivil());
                ps.setString(6, f.getSexo());
                ps.setString(7, f.getDireccion());
                ps.setString(8, f.getTelefono());
                if (f.getFechaNacimiento() != null)
                    ps.setDate(9, Date.valueOf(f.getFechaNacimiento()));
                else
                    ps.setNull(9, Types.DATE);
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    f.setId(id);

                    // insertar grupo familiar si existe
                    if (f.getFamiliares() != null) {
                        try (PreparedStatement psGF = conn.prepareStatement(sqlGF)) {
                            for (GrupoFamiliar gf : f.getFamiliares()) {
                                psGF.setInt(1, id);
                                psGF.setString(2, gf.getNombre());
                                if (gf.getFechaNacimiento() != null) psGF.setDate(3, Date.valueOf(gf.getFechaNacimiento()));
                                else psGF.setNull(3, Types.DATE);
                                psGF.setString(4, gf.getParentesco());
                                psGF.setString(5, gf.getOcupacion());
                                psGF.setString(6, gf.getTelefono());
                                psGF.addBatch();
                            }
                            psGF.executeBatch();
                        }
                    }

                    // insertar informacion academica
                    if (f.getAcademica() != null) {
                        try (PreparedStatement psAC = conn.prepareStatement(sqlAC)) {
                            for (InformacionAcademica ac : f.getAcademica()) {
                                psAC.setInt(1, id);
                                psAC.setString(2, ac.getUniversidad());
                                psAC.setString(3, ac.getNivelEstudio());
                                psAC.setString(4, ac.getTitulo());
                                if (ac.getAnioGraduacion() != null) psAC.setInt(5, ac.getAnioGraduacion());
                                else psAC.setNull(5, Types.INTEGER);
                                psAC.addBatch();
                            }
                            psAC.executeBatch();
                        }
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw new Exception("Error guardando funcionario: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }



    @Override
    public Funcionario buscarPorId(int id) throws Exception {
        String sql = "SELECT * FROM funcionarios WHERE id = ?";
        Funcionario f = null;
        try (Connection conn = ConnectionConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                f = mapFuncionario(rs);
                // cargar familiares
                f.setFamiliares(loadFamiliares(conn, id));
                f.setAcademica(loadAcademica(conn, id));
            }
        } catch (SQLException ex) {
            throw new Exception("Error buscando funcionario: " + ex.getMessage(), ex);
        }
        return f;
    }

    @Override
    public List<Funcionario> listarTodos() throws Exception {
        String sql = "SELECT * FROM funcionarios ORDER BY apellidos, nombres";
        List<Funcionario> lista = new ArrayList<>();
        try (Connection conn = ConnectionConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Funcionario f = mapFuncionario(rs);
                lista.add(f);
            }
        } catch (SQLException ex) {
            throw new Exception("Error listando funcionarios: " + ex.getMessage(), ex);
        }
        return lista;
    }

    @Override
    public void actualizar(Funcionario f) throws Exception {
        String sqlF = "UPDATE funcionarios SET tipo_identificacion=?, numero_identificacion=?, nombres=?, apellidos=?, estado_civil=?, sexo=?, direccion=?, telefono=?, fecha_nacimiento=? WHERE id=?";
        // Estrategia simple: actualizar funcionario y eliminar/reinsertar dependientes
        String deleteGF = "DELETE FROM grupo_familiar WHERE funcionario_id=?";
        String deleteAC = "DELETE FROM informacion_academica WHERE funcionario_id=?";
        String insertGF = "INSERT INTO grupo_familiar(funcionario_id, nombre, fecha_nacimiento, parentesco, ocupacion, telefono) VALUES (?, ?, ?, ?, ?, ?)";
        String insertAC = "INSERT INTO informacion_academica(funcionario_id, universidad, nivel_estudio, titulo, anio_graduacion) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlF)) {
                ps.setString(1, f.getTipoIdentificacion());
                ps.setString(2, f.getNumeroIdentificacion());
                ps.setString(3, f.getNombres());
                ps.setString(4, f.getApellidos());
                ps.setString(5, f.getEstadoCivil());
                ps.setString(6, f.getSexo());
                ps.setString(7, f.getDireccion());
                ps.setString(8, f.getTelefono());
                if (f.getFechaNacimiento() != null) ps.setDate(9, Date.valueOf(f.getFechaNacimiento()));
                else ps.setNull(9, Types.DATE);
                ps.setInt(10, f.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement delGF = conn.prepareStatement(deleteGF);
                 PreparedStatement delAC = conn.prepareStatement(deleteAC)) {
                delGF.setInt(1, f.getId());
                delGF.executeUpdate();
                delAC.setInt(1, f.getId());
                delAC.executeUpdate();
            }

            if (f.getFamiliares() != null) {
                try (PreparedStatement psGF = conn.prepareStatement(insertGF)) {
                    for (GrupoFamiliar gf : f.getFamiliares()) {
                        psGF.setInt(1, f.getId());
                        psGF.setString(2, gf.getNombre());
                        if (gf.getFechaNacimiento() != null) psGF.setDate(3, Date.valueOf(gf.getFechaNacimiento()));
                        else psGF.setNull(3, Types.DATE);
                        psGF.setString(4, gf.getParentesco());
                        psGF.setString(5, gf.getOcupacion());
                        psGF.setString(6, gf.getTelefono());
                        psGF.addBatch();
                    }
                    psGF.executeBatch();
                }
            }

            if (f.getAcademica() != null) {
                try (PreparedStatement psAC = conn.prepareStatement(insertAC)) {
                    for (InformacionAcademica ac : f.getAcademica()) {
                        psAC.setInt(1, f.getId());
                        psAC.setString(2, ac.getUniversidad());
                        psAC.setString(3, ac.getNivelEstudio());
                        psAC.setString(4, ac.getTitulo());
                        if (ac.getAnioGraduacion() != null) psAC.setInt(5, ac.getAnioGraduacion());
                        else psAC.setNull(5, Types.INTEGER);
                        psAC.addBatch();
                    }
                    psAC.executeBatch();
                }
            }

            conn.commit();
        } catch (Exception ex) {
            throw new Exception("Error actualizando funcionario: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void eliminar(int id) throws Exception {
        String sql = "DELETE FROM funcionarios WHERE id=?";
        try (Connection conn = ConnectionConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new Exception("Error eliminando funcionario: " + ex.getMessage(), ex);
        }
    }

    // helpers
    private Funcionario mapFuncionario(ResultSet rs) throws SQLException {
        Funcionario f = new Funcionario();
        f.setId(rs.getInt("id"));
        f.setTipoIdentificacion(rs.getString("tipo_identificacion"));
        f.setNumeroIdentificacion(rs.getString("numero_identificacion"));
        f.setNombres(rs.getString("nombres"));
        f.setApellidos(rs.getString("apellidos"));
        f.setEstadoCivil(rs.getString("estado_civil"));
        f.setSexo(rs.getString("sexo"));
        f.setDireccion(rs.getString("direccion"));
        f.setTelefono(rs.getString("telefono"));
        Date d = rs.getDate("fecha_nacimiento");
        if (d != null) f.setFechaNacimiento(d.toLocalDate());
        return f;
    }

    private List<GrupoFamiliar> loadFamiliares(Connection conn, int funcionarioId) throws SQLException {
        String sql = "SELECT * FROM grupo_familiar WHERE funcionario_id=?";
        List<GrupoFamiliar> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, funcionarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GrupoFamiliar g = new GrupoFamiliar();
                    g.setId(rs.getInt("id"));
                    g.setFuncionarioId(rs.getInt("funcionario_id"));
                    g.setNombre(rs.getString("nombre"));
                    Date d = rs.getDate("fecha_nacimiento");
                    if (d != null) g.setFechaNacimiento(d.toLocalDate());
                    g.setParentesco(rs.getString("parentesco"));
                    g.setOcupacion(rs.getString("ocupacion"));
                    g.setTelefono(rs.getString("telefono"));
                    list.add(g);
                }
            }
        }
        return list;
    }

    private List<InformacionAcademica> loadAcademica(Connection conn, int funcionarioId) throws SQLException {
        String sql = "SELECT * FROM informacion_academica WHERE funcionario_id=?";
        List<InformacionAcademica> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, funcionarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InformacionAcademica a = new InformacionAcademica();
                    a.setId(rs.getInt("id"));
                    a.setFuncionarioId(rs.getInt("funcionario_id"));
                    a.setUniversidad(rs.getString("universidad"));
                    a.setNivelEstudio(rs.getString("nivel_estudio"));
                    a.setTitulo(rs.getString("titulo"));
                    int anio = rs.getInt("anio_graduacion");
                    if (!rs.wasNull()) a.setAnioGraduacion(anio);
                    list.add(a);
                }
            }
        }
        return list;
    }
}