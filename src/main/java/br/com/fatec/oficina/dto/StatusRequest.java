package br.com.fatec.oficina.dto;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record StatusRequest(@NotNull @Schema(example="EM_ANDAMENTO") br.com.fatec.oficina.model.StatusOrdem status,
 @Size(max=2000) @Schema(example="Pastilhas substituidas e freios testados") String servicoRealizado) {}