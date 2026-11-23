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

public class ClienteService {
 private final ClienteRepository repository;
 private final VeiculoRepository veiculos;
 public List<Cliente> listar() { return repository.findAll(); }
 public Cliente buscar(Long id) {
  return repository.findById(id).orElseThrow(()->new NaoEncontradoException("Cliente nao encontrado"));
 }
 @Transactional public Cliente cadastrar(ClienteRequest dados) {
  return salvar(new Cliente(),dados);
 }
 @Transactional public Cliente atualizar(Long id,ClienteRequest dados) {
  return salvar(buscar(id),dados);
 }
 private Cliente salvar(Cliente cliente,ClienteRequest dados) {
  if(repository.existsByCpfAndIdNot(dados.cpf(),cliente.getId()==null?0L:cliente.getId()))
   throw new RegraNegocioException("CPF ja cadastrado");
  cliente.setCpf(dados.cpf());
  String email=dados.email().strip().toLowerCase(Locale.ROOT);
  if(repository.existsByEmailAndIdNot(email,cliente.getId()==null?0L:cliente.getId()))
   throw new RegraNegocioException("Email ja cadastrado");
  cliente.setNome(dados.nome().strip());
  cliente.setEmail(email);
  cliente.setTelefone(dados.telefone());
  return repository.save(cliente);
 }
 @Transactional public void excluir(Long id) {
  Cliente cliente=buscar(id);
  if(veiculos.existsByClienteId(id)) throw new RegraNegocioException("Cliente possui veiculos vinculados");
  repository.delete(cliente);
 }
}
