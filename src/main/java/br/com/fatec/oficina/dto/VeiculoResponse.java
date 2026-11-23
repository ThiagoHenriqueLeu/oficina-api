package br.com.fatec.oficina.dto;
import br.com.fatec.oficina.model.Veiculo;
public record VeiculoResponse(Long id,String placa,String marca,String modelo,Integer ano,Long clienteId) {
 public static VeiculoResponse from(Veiculo v) {
  return new VeiculoResponse(v.getId(),v.getPlaca(),v.getMarca(),v.getModelo(),v.getAno(),v.getCliente().getId());
 }
}