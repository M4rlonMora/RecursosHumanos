package org.example.dao;
import org.example.model.Funcionario;
import java.util.List;


public interface FuncionarioDAO {
    void guardar(Funcionario f) throws Exception;
    Funcionario buscarPorId(int id) throws Exception;
    List<Funcionario> listarTodos() throws Exception;
    void actualizar(Funcionario f) throws Exception;
    void eliminar(int id) throws Exception;
}
