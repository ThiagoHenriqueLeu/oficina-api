package br.com.fatec.oficina.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.examples.Example;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;
import java.util.*;
@Configuration
public class OpenApiConfig {
 @Bean public OpenAPI oficinaOpenApi() {
  return new OpenAPI().info(new Info().title("Oficina Mecanica - Manutencoes")
   .version("1.0.0").description("API REST para substituir anotacoes dispersas de clientes, veiculos e manutencoes. "
    +"Controla ordens de servico, custos e historico. Cliente possui varios veiculos; veiculo possui varias ordens. "
    +"Apenas uma ordem ABERTA ou EM_ANDAMENTO por veiculo. Total calculado: pecas + mao de obra. "
    +"Para concluir, iniciar o atendimento e informar o servico realizado. Valores em reais (BRL); datas locais do servidor. "
    +"Projeto academico local, sem autenticacao. Nao deve ser exposto publicamente sem controle de acesso."));
 }
 @Bean public OpenApiCustomizer exemplosERetornos() {
  return api -> api.getPaths().forEach((path,item)->item.readOperationsMap().forEach((metodo,op)->{
   boolean exclusao=metodo==PathItem.HttpMethod.DELETE;
   String codigo=exclusao?"204":metodo==PathItem.HttpMethod.POST?"201":"200";
   ApiResponse sucesso=op.getResponses().get(codigo);
   if(sucesso==null) sucesso=new ApiResponse();
   sucesso.setDescription(exclusao?"Excluido, sem corpo":codigo.equals("201")?"Criado; cabecalho Location indica o recurso":"Operacao realizada");
   if(!exclusao) {
    Map<String,Object> exemplo=new LinkedHashMap<>();
    exemplo.put("id",1);
    if(path.contains("clientes")) {
     exemplo.put("nome","Ana Souza"); exemplo.put("cpf","52998224725"); exemplo.put("email","ana@example.com"); exemplo.put("telefone","11987654321");
    } else if(path.contains("veiculos")) {
     exemplo.put("placa","ABC1D23"); exemplo.put("marca","Fiat"); exemplo.put("modelo","Uno");
     exemplo.put("ano",2018); exemplo.put("clienteId",1);
    } else {
     exemplo.put("veiculoId",1); exemplo.put("descricaoProblema","Ruido nos freios");
     exemplo.put("servicoRealizado","Troca de pastilhas"); exemplo.put("quilometragem",45000);
     exemplo.put("valorPecas",150.00); exemplo.put("valorMaoObra",200.00); exemplo.put("valorTotal",350.00);
     exemplo.put("status",path.endsWith("/status")?"EM_ANDAMENTO":"ABERTA");
     exemplo.put("dataAbertura","2026-11-22T10:00:00"); exemplo.put("dataConclusao",null);
    }
    if(sucesso.getContent()==null) sucesso.setContent(new Content());
    var media=sucesso.getContent().get("application/json");
    if(media==null) {media=new MediaType(); sucesso.getContent().addMediaType("application/json",media);}
    boolean lista=metodo==PathItem.HttpMethod.GET && !path.contains("{id}");
    media.addExamples("Sucesso",new Example().value(lista?List.of(exemplo):exemplo));
   }
   op.getResponses().addApiResponse(codigo,sucesso);
   for(var entry:Map.of("400","Dados invalidos ou JSON malformado","404","Recurso ou vinculo nao encontrado","409","Conflito: regra de negocio, duplicidade ou registro vinculado").entrySet()) {
    Map<String,Object> exemplo=Map.of("data","2026-11-22T10:00:00","status",Integer.parseInt(entry.getKey()),
     "mensagem",entry.getValue(),"caminho",path,"campos",Map.of());
    op.getResponses().addApiResponse(entry.getKey(),new ApiResponse().description(entry.getValue())
     .content(new Content().addMediaType("application/json",new MediaType().example(exemplo))));
   }
  }));
 }
}
