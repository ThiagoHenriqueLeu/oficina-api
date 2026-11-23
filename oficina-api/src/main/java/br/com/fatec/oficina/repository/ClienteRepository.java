package br.com.fatec.oficina.repository;
import br.com.fatec.oficina.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteRepository extends JpaRepository<Cliente,Long> {
 boolean existsByCpfAndIdNot(String cpf,Long id);
 boolean existsByEmailAndIdNot(String email,Long id);
}
