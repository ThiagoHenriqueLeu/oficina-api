package br.com.fatec.oficina.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity

@Table(name="clientes")
public class Cliente {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=100) private String nome;
 @Column(unique=true,length=11) private String cpf;
 @Column(nullable=false,unique=true,length=150) private String email;
 @Column(nullable=false,length=11) private String telefone;
}
