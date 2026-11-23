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
@RequestMapping("/api/veiculos")
@Tag(name="Veiculos")
public class VeiculoController {
 private final VeiculoService service;
 @GetMapping @Operation(summary="Listar veiculos",description="Retorna uma lista JSON; sem resultados retorna lista vazia.")
 public List<VeiculoResponse> listar(@RequestParam(required=false) Long clienteId) { return service.listar(clienteId); }
 @GetMapping("/{id}") @Operation(summary="Consultar por ID")
 public VeiculoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
 @PostMapping @Operation(summary="Cadastrar veiculos")
 public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody VeiculoRequest dados) {
  VeiculoResponse salvo=service.cadastrar(dados);
  return ResponseEntity.created(URI.create("/api/veiculos/"+salvo.id())).body(salvo);
 }
 @PutMapping("/{id}") @Operation(summary="Atualizar veiculos",description="Envie todos os campos do cadastro. ID e campos calculados nao sao recebidos.")
 public VeiculoResponse atualizar(@PathVariable Long id,@Valid @RequestBody VeiculoRequest dados) { return service.atualizar(id,dados); }
 @DeleteMapping("/{id}") @Operation(summary="Excluir veiculos",description="Exclusao bloqueada se existirem registros vinculados.")
 public ResponseEntity<Void> excluir(@PathVariable Long id) { service.excluir(id); return ResponseEntity.noContent().build(); }
 
}
