package br.com.fatec.oficina.service;
import java.time.LocalDateTime;
import java.math.RoundingMode;
import br.com.fatec.oficina.model.*;
import br.com.fatec.oficina.dto.*;
import br.com.fatec.oficina.repository.*;
import br.com.fatec.oficina.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)

public class OrdemService {
 private final OrdemRepository repository;
 private final VeiculoRepository veiculos;
 private static final List<StatusOrdem> ATIVAS=List.of(StatusOrdem.ABERTA,StatusOrdem.EM_ANDAMENTO);
 public List<OrdemResponse> listar(Long veiculoId,StatusOrdem status) {
  if(veiculoId!=null && !veiculos.existsById(veiculoId)) throw new NaoEncontradoException("Veiculo nao encontrado");
  List<OrdemServico> lista;
  if(veiculoId!=null && status!=null) lista=repository.findByVeiculoIdAndStatus(veiculoId,status);
  else if(veiculoId!=null) lista=repository.findByVeiculoId(veiculoId);
  else if(status!=null) lista=repository.findByStatus(status);
  else lista=repository.findAll();
  return lista.stream().map(OrdemResponse::from).toList();
 }
 public OrdemResponse buscar(Long id) {
  return OrdemResponse.from(repository.findById(id).orElseThrow(()->new NaoEncontradoException("Ordem nao encontrada")));
 }
 private OrdemServico bloquear(Long id) {
  return repository.buscarParaAtualizacao(id).orElseThrow(()->new NaoEncontradoException("Ordem nao encontrada"));
 }
 @Transactional public OrdemResponse cadastrar(OrdemRequest dados) {
  Veiculo veiculo=veiculos.buscarParaAtualizacao(dados.veiculoId())
   .orElseThrow(()->new NaoEncontradoException("Veiculo nao encontrado"));
  if(repository.existsByVeiculoIdAndStatusIn(veiculo.getId(),ATIVAS))
   throw new RegraNegocioException("Veiculo ja possui uma ordem aberta ou em andamento");
  OrdemServico ordem=new OrdemServico();
  ordem.setVeiculo(veiculo);
  ordem.setStatus(StatusOrdem.ABERTA);
  ordem.setDataAbertura(LocalDateTime.now());
  preencher(ordem,dados);
  return OrdemResponse.from(repository.save(ordem));
 }
 private void preencher(OrdemServico o,OrdemRequest d) {
  o.setDescricaoProblema(d.descricaoProblema().strip());
  o.setServicoRealizado(d.servicoRealizado()==null?null:d.servicoRealizado().strip());
  o.setQuilometragem(d.quilometragem());
  o.setValorPecas(d.valorPecas().setScale(2,RoundingMode.UNNECESSARY));
  o.setValorMaoObra(d.valorMaoObra().setScale(2,RoundingMode.UNNECESSARY));
  o.setValorTotal(o.getValorPecas().add(o.getValorMaoObra()));
 }
 private void exigirEditavel(OrdemServico ordem) {
  if(!ATIVAS.contains(ordem.getStatus())) throw new RegraNegocioException("Ordem encerrada nao pode ser alterada");
 }
 @Transactional public OrdemResponse atualizar(Long id,OrdemRequest dados) {
  OrdemServico ordem=bloquear(id);
  exigirEditavel(ordem);
  if(!ordem.getVeiculo().getId().equals(dados.veiculoId()))
   throw new RegraNegocioException("Nao e permitido trocar o veiculo da ordem");
  preencher(ordem,dados);
  return OrdemResponse.from(repository.save(ordem));
 }
 @Transactional public OrdemResponse alterarStatus(Long id,StatusRequest dados) {
  OrdemServico ordem=bloquear(id);
  exigirEditavel(ordem);
  StatusOrdem atual=ordem.getStatus(), novo=dados.status();
  boolean permitido=(atual==StatusOrdem.ABERTA && (novo==StatusOrdem.EM_ANDAMENTO || novo==StatusOrdem.CANCELADA))
   || (atual==StatusOrdem.EM_ANDAMENTO && (novo==StatusOrdem.CONCLUIDA || novo==StatusOrdem.CANCELADA));
  if(!permitido) throw new RegraNegocioException("Transicao de status nao permitida");
  if(dados.servicoRealizado()!=null) ordem.setServicoRealizado(dados.servicoRealizado().strip());
  if(novo==StatusOrdem.CONCLUIDA) {
   if(ordem.getServicoRealizado()==null || ordem.getServicoRealizado().isBlank())
    throw new RegraNegocioException("Informe o servico realizado antes de concluir");
   ordem.setDataConclusao(LocalDateTime.now());
  }
  ordem.setStatus(novo);
  return OrdemResponse.from(repository.save(ordem));
 }
 @Transactional public void excluir(Long id) {
  OrdemServico ordem=bloquear(id);
  if(ordem.getStatus()!=StatusOrdem.CANCELADA)
   throw new RegraNegocioException("Somente ordens canceladas podem ser excluidas; historico concluido e preservado");
  repository.delete(ordem);
 }
}