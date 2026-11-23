package br.com.fatec.oficina.dto;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.br.CPF;

public record ClienteRequest(@NotBlank @Size(max=100) @Schema(example="Ana Souza") String nome,
 @NotBlank(message="CPF e obrigatorio") @Pattern(regexp="[0-9]{11}",message="Informe CPF com 11 digitos, sem pontuacao") @CPF(message="CPF invalido") @Schema(example="52998224725",description="CPF valido, unico, com 11 digitos sem pontuacao") String cpf,
 @NotBlank @Email @Size(max=150) @Schema(example="ana@example.com") String email,
 @NotBlank @Pattern(regexp="[0-9]{10,11}",message="Informe telefone com DDD, apenas 10 ou 11 digitos") @Schema(example="11987654321") String telefone) {}
