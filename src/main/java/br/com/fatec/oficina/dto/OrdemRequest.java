package br.com.fatec.oficina.dto;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record OrdemRequest(@NotNull @Positive @Schema(example="1") Long veiculoId,
 @NotBlank @Size(max=2000) @Schema(example="Ruido nos freios") String descricaoProblema,
 @NotNull @Min(0) @Schema(example="45000") Integer quilometragem,
 @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) @Schema(example="150.00") java.math.BigDecimal valorPecas,
 @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) @Schema(example="200.00") java.math.BigDecimal valorMaoObra,
 @Size(max=2000) @Schema(example="Troca de pastilhas") String servicoRealizado) {}