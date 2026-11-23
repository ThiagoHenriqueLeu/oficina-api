package br.com.fatec.oficina.repository;
import br.com.fatec.oficina.model.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface OrdemRepository extends JpaRepository<OrdemServico,Long> {
 boolean existsByVeiculoId(Long veiculoId);
 boolean existsByVeiculoIdAndStatusIn(Long veiculoId,Collection<StatusOrdem> status);
 List<OrdemServico> findByVeiculoId(Long veiculoId);
 List<OrdemServico> findByStatus(StatusOrdem status);
 List<OrdemServico> findByVeiculoIdAndStatus(Long veiculoId,StatusOrdem status);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select o from OrdemServico o where o.id = :id")
 Optional<OrdemServico> buscarParaAtualizacao(Long id);
}