package br.com.fatec.oficina.repository;
import br.com.fatec.oficina.model.Veiculo;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface VeiculoRepository extends JpaRepository<Veiculo,Long> {
 boolean existsByPlacaAndIdNot(String placa,Long id);
 boolean existsByClienteId(Long clienteId);
 List<Veiculo> findByClienteId(Long clienteId);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select v from Veiculo v where v.id = :id")
 Optional<Veiculo> buscarParaAtualizacao(Long id);
}