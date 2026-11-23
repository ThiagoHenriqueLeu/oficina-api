package br.com.fatec.oficina.controller;
import br.com.fatec.oficina.model.*;
import br.com.fatec.oficina.dto.*;
import br.com.fatec.oficina.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/clientes")
@Tag(name="Clientes")
public class ClienteController {
 private final ClienteService service;
 @GetMapping @Operation(summary="Listar clientes",description="Retorna uma lista JSON; sem resultados retorna lista vazia.")
 public List<Cliente> listar() { return service.listar(); }
 @GetMapping("/{id}") @Operation(summary="Consultar por ID")
 public Cliente buscar(@PathVariable Long id) { return service.buscar(id); }
 @PostMapping @Operation(summary="Cadastrar clientes")
 public ResponseEntity<Cliente> cadastrar(@Valid @RequestBody ClienteRequest dados) {
  Cliente salvo=service.cadastrar(dados);
  return ResponseEntity.created(URI.create("/api/clientes/"+salvo.getId())).body(salvo);
 }
 @PutMapping("/{id}") @Operation(summary="Atualizar clientes",description="Envie todos os campos do cadastro. ID e campos calculados nao sao recebidos.")
 public Cliente atualizar(@PathVariable Long id,@Valid @RequestBody ClienteRequest dados) { return service.atualizar(id,dados); }
 @DeleteMapping("/{id}") @Operation(summary="Excluir clientes",description="Exclusao bloqueada se existirem registros vinculados.")
 public ResponseEntity<Void> excluir(@PathVariable Long id) { service.excluir(id); return ResponseEntity.noContent().build(); }
 
}
