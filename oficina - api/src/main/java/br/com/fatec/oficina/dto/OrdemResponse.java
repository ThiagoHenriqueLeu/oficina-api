package br.com.fatec.oficina.dto;
import br.com.fatec.oficina.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record OrdemResponse(Long id,Long veiculoId,String descricaoProblema,String servicoRealizado,
 Integer quilometragem,BigDecimal valorPecas,BigDecimal valorMaoObra,BigDecimal valorTotal,
 StatusOrdem status,LocalDateTime dataAbertura,LocalDateTime dataConclusao) {
 public static OrdemResponse from(OrdemServico o) {
  return new OrdemResponse(o.getId(),o.getVeiculo().getId(),o.getDescricaoProblema(),o.getServicoRealizado(),
   o.getQuilometragem(),o.getValorPecas(),o.getValorMaoObra(),o.getValorTotal(),o.getStatus(),o.getDataAbertura(),o.getDataConclusao());
 }
}