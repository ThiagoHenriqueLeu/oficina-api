package br.com.fatec.oficina.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity

@Table(name="veiculos")
public class Veiculo {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=7) private String placa;
 @Column(nullable=false,length=60) private String marca;
 @Column(nullable=false,length=100) private String modelo;
 @Column(nullable=false) private Integer ano;
 @ManyToOne(fetch=FetchType.LAZY,optional=false)
 @JoinColumn(name="cliente_id",nullable=false) private Cliente cliente;
}