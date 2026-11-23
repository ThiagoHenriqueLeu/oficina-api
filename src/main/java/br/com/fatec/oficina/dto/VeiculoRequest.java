package br.com.fatec.oficina.dto;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record VeiculoRequest(@NotBlank @Pattern(regexp="[A-Za-z]{3}[0-9][A-Za-z0-9][0-9]{2}",message="Placa deve ter 7 caracteres, sem hifen") @Schema(example="ABC1D23") String placa,
 @NotBlank @Size(max=60) @Schema(example="Fiat") String marca,
 @NotBlank @Size(max=100) @Schema(example="Uno") String modelo,
 @NotNull @Min(1886) @Max(2100) @Schema(example="2018") Integer ano,
 @NotNull @Positive @Schema(example="1") Long clienteId) {}