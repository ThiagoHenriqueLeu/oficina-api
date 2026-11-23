package br.com.fatec.oficina.service;
import br.com.fatec.oficina.model.*;
import br.com.fatec.oficina.dto.*;
import br.com.fatec.oficina.repository.*;
import br.com.fatec.oficina.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)

public class VeiculoService {
 private final VeiculoRepository repository;
 private final ClienteService clientes;
 private final OrdemRepository ordens;
 public List<VeiculoResponse> listar(Long clienteId) {
  if(clienteId!=null) clientes.buscar(clienteId);
  return (clienteId==null?repository.findAll():repository.findByClienteId(clienteId))
   .stream().map(VeiculoResponse::from).toList();
 }
 public Veiculo buscarEntidade(Long id) {
  return repository.findById(id).orElseThrow(()->new NaoEncontradoException("Veiculo nao encontrado"));
 }
 public VeiculoResponse buscar(Long id) { return VeiculoResponse.from(buscarEntidade(id)); }
 @Transactional public VeiculoResponse cadastrar(VeiculoRequest dados) { return salvar(new Veiculo(),dados); }
 @Transactional public VeiculoResponse atualizar(Long id,VeiculoRequest dados) {
  Veiculo v=repository.buscarParaAtualizacao(id).orElseThrow(()->new NaoEncontradoException("Veiculo nao encontrado"));
  if(!v.getCliente().getId().equals(dados.clienteId()) && ordens.existsByVeiculoId(id))
   throw new RegraNegocioException("Veiculo com historico de servicos nao pode trocar de cliente");
  return salvar(v,dados);
 }
 private VeiculoResponse salvar(Veiculo veiculo,VeiculoRequest dados) {
  String placa=dados.placa().toUpperCase(Locale.ROOT);
  if(repository.existsByPlacaAndIdNot(placa,veiculo.getId()==null?0L:veiculo.getId()))
   throw new RegraNegocioException("Placa ja cadastrada");
  veiculo.setPlaca(placa);
  veiculo.setMarca(dados.marca().strip());
  veiculo.setModelo(dados.modelo().strip());
  veiculo.setAno(dados.ano());
  veiculo.setCliente(clientes.buscar(dados.clienteId()));
  return VeiculoResponse.from(repository.save(veiculo));
 }
 @Transactional public void excluir(Long id) {
  Veiculo v=repository.buscarParaAtualizacao(id).orElseThrow(()->new NaoEncontradoException("Veiculo nao encontrado"));
  if(ordens.existsByVeiculoId(id)) throw new RegraNegocioException("Veiculo possui ordens de servico vinculadas");
  repository.delete(v);
 }
}