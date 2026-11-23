package br.com.fatec.oficina.model;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity

@Table(name="ordens_servico")
public class OrdemServico {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)
 @JoinColumn(name="veiculo_id",nullable=false) private Veiculo veiculo;
 @Column(nullable=false,length=2000) private String descricaoProblema;
 @Column(length=2000) private String servicoRealizado;
 @Column(nullable=false) private Integer quilometragem;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal valorPecas;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal valorMaoObra;
 @Column(nullable=false,precision=13,scale=2) private BigDecimal valorTotal;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatusOrdem status;
 @Column(nullable=false) private LocalDateTime dataAbertura;
 private LocalDateTime dataConclusao;
}