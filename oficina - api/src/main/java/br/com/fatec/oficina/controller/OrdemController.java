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
@RequestMapping("/api/ordens-servico")
@Tag(name="Ordens de servico")
public class OrdemController {
 private final OrdemService service;
 @GetMapping @Operation(summary="Listar ordens de servico",description="Retorna uma lista JSON; sem resultados retorna lista vazia.")
 public List<OrdemResponse> listar(@RequestParam(required=false) Long veiculoId,@RequestParam(required=false) StatusOrdem status) { return service.listar(veiculoId,status); }
 @GetMapping("/{id}") @Operation(summary="Consultar por ID")
 public OrdemResponse buscar(@PathVariable Long id) { return service.buscar(id); }
 @PostMapping @Operation(summary="Cadastrar ordens de servico")
 public ResponseEntity<OrdemResponse> cadastrar(@Valid @RequestBody OrdemRequest dados) {
  OrdemResponse salvo=service.cadastrar(dados);
  return ResponseEntity.created(URI.create("/api/ordens-servico/"+salvo.id())).body(salvo);
 }
 @PutMapping("/{id}") @Operation(summary="Atualizar ordens de servico",description="Envie todos os campos do cadastro. ID e campos calculados nao sao recebidos.")
 public OrdemResponse atualizar(@PathVariable Long id,@Valid @RequestBody OrdemRequest dados) { return service.atualizar(id,dados); }
 @DeleteMapping("/{id}") @Operation(summary="Excluir ordens de servico",description="Somente ordens canceladas podem ser excluidas.")
 public ResponseEntity<Void> excluir(@PathVariable Long id) { service.excluir(id); return ResponseEntity.noContent().build(); }
 @PutMapping("/{id}/status") @Operation(summary="Alterar status da manutencao",description="ABERTA -> EM_ANDAMENTO ou CANCELADA; EM_ANDAMENTO -> CONCLUIDA ou CANCELADA. Conclusao exige servicoRealizado.")
 public OrdemResponse status(@PathVariable Long id,@Valid @RequestBody StatusRequest dados) { return service.alterarStatus(id,dados); }
}
