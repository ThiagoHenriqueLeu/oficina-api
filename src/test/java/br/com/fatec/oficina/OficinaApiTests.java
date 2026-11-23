package br.com.fatec.oficina;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.net.*;
import java.net.http.*;
import java.util.*;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OficinaApiTests {
 @LocalServerPort int porta;
 HttpClient http=HttpClient.newHttpClient();
 ObjectMapper json=new ObjectMapper();
 record Resultado(int status,Map<String,Object> body,String raw) {}
 @SuppressWarnings("unchecked")
 Resultado enviar(String metodo,String rota,Object body) throws Exception {
  var builder=HttpRequest.newBuilder(URI.create("http://localhost:"+porta+rota))
   .header("Content-Type","application/json");
  builder.method(metodo,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
  var r=http.send(builder.build(),HttpResponse.BodyHandlers.ofString());
  Map<String,Object> map=r.body().startsWith("{")?json.readValue(r.body(),Map.class):Map.of();
  return new Resultado(r.statusCode(),map,r.body());
 }
 long id(Resultado r) {return ((Number)r.body.get("id")).longValue();}
 String email() {return UUID.randomUUID()+"@example.com";}
 String cpf() {
  String valor=String.format("%09d",new Random().nextInt(900000000)+100000000);
  for(int tamanho=9;tamanho<11;tamanho++) {
   int soma=0;
   for(int i=0;i<tamanho;i++) soma+=(valor.charAt(i)-'0')*(tamanho+1-i);
   int digito=11-soma%11;valor+=digito>=10?0:digito;
  }
  return valor;
 }
 Map<String,Object> cliente(String email) {return Map.of("nome","Ana","cpf",cpf(),"email",email,"telefone","11987654321");}
 @Test void cpfObrigatorioValidoEUnico() throws Exception {
  var dados=new HashMap<>(cliente(email()));
  dados.remove("cpf");assertEquals(400,enviar("POST","/api/clientes",dados).status);
  for(String invalido:List.of("","123","11111111111","52998224724","529.982.247-25")) {
   dados.put("cpf",invalido);assertEquals(400,enviar("POST","/api/clientes",dados).status);
  }
  dados.put("cpf",cpf());var primeiro=enviar("POST","/api/clientes",dados);
  assertEquals(201,primeiro.status);assertEquals(dados.get("cpf"),primeiro.body.get("cpf"));
  String rota="/api/clientes/"+id(primeiro);
  assertEquals(200,enviar("PUT",rota,dados).status);
  dados.put("email",email());assertEquals(409,enviar("POST","/api/clientes",dados).status);
  var segundo=enviar("POST","/api/clientes",cliente(email()));
  assertEquals(409,enviar("PUT","/api/clientes/"+id(segundo),dados).status);
  dados.remove("cpf");assertEquals(400,enviar("PUT",rota,dados).status);
  assertEquals(primeiro.body.get("cpf"),enviar("GET",rota,null).body.get("cpf"));
 }
 long novoVeiculo() throws Exception {
  var c=enviar("POST","/api/clientes",cliente(email())); assertEquals(201,c.status);
  long n=id(c);
  String placa=String.format("T%02d",n%100).replace('0','A');
  // Tres letras e quatro digitos permitem placas diferentes em cada teste.
  placa="T"+(char)('A'+(n/26)%26)+(char)('A'+n%26)+"1234";
  var v=enviar("POST","/api/veiculos",Map.of("placa",placa,"marca","Fiat","modelo","Uno","ano",2018,"clienteId",n));
  assertEquals(201,v.status,v.raw); return id(v);
 }
 Map<String,Object> ordem(long v) {
  return Map.of("veiculoId",v,"descricaoProblema","Freios","quilometragem",45000,"valorPecas",150.25,"valorMaoObra",200);
 }
 @Test void fluxoCompletoEHistorico() throws Exception {
  long v=novoVeiculo();
  var o=enviar("POST","/api/ordens-servico",ordem(v));
  assertEquals(201,o.status,o.raw);
  assertEquals(350.25,((Number)o.body.get("valorTotal")).doubleValue());
  long id=id(o); String rota="/api/ordens-servico/"+id;
  assertEquals(409,enviar("POST","/api/ordens-servico",ordem(v)).status);
  assertEquals(409,enviar("PUT",rota+"/status",Map.of("status","CONCLUIDA")).status);
  assertEquals(200,enviar("PUT",rota+"/status",Map.of("status","EM_ANDAMENTO")).status);
  assertEquals(409,enviar("PUT",rota+"/status",Map.of("status","CONCLUIDA")).status);
  var dados=new HashMap<>(ordem(v)); dados.put("valorPecas",180);dados.put("servicoRealizado","Troca de pastilhas");
  assertEquals(200,enviar("PUT",rota,dados).status);
  var atualizado=enviar("GET",rota,null);
  assertEquals(380.0,((Number)atualizado.body.get("valorTotal")).doubleValue());
  var concluida=enviar("PUT",rota+"/status",Map.of("status","CONCLUIDA"));
  assertEquals(200,concluida.status);assertNotNull(concluida.body.get("dataConclusao"));
  assertEquals(409,enviar("PUT",rota,dados).status);
  assertEquals(409,enviar("DELETE",rota,null).status);
  assertEquals(201,enviar("POST","/api/ordens-servico",ordem(v)).status);
 }
 @Test void crudRelacionamentosEExclusao() throws Exception {
  var c=enviar("POST","/api/clientes",cliente(email()));long cid=id(c);
  var dc=new HashMap<>(cliente(email()));dc.put("nome","Ana atualizada");
  assertEquals(200,enviar("PUT","/api/clientes/"+cid,dc).status);
  assertEquals("Ana atualizada",enviar("GET","/api/clientes/"+cid,null).body.get("nome"));
  var dv=new HashMap<String,Object>(Map.of("placa","ZZZ9Z99","marca","Fiat","modelo","Uno","ano",2018,"clienteId",cid));
  var v=enviar("POST","/api/veiculos",dv);long vid=id(v);
  dv.put("modelo","Argo");assertEquals(200,enviar("PUT","/api/veiculos/"+vid,dv).status);
  assertEquals("Argo",enviar("GET","/api/veiculos/"+vid,null).body.get("modelo"));
  assertEquals(409,enviar("DELETE","/api/clientes/"+cid,null).status);
  var o=enviar("POST","/api/ordens-servico",ordem(vid));String rota="/api/ordens-servico/"+id(o);
  assertEquals(409,enviar("DELETE","/api/veiculos/"+vid,null).status);
  assertEquals(409,enviar("DELETE",rota,null).status);
  assertEquals(200,enviar("PUT",rota+"/status",Map.of("status","CANCELADA")).status);
  assertEquals(204,enviar("DELETE",rota,null).status);
  assertEquals(404,enviar("GET",rota,null).status);
  assertEquals(204,enviar("DELETE","/api/veiculos/"+vid,null).status);
  assertEquals(204,enviar("DELETE","/api/clientes/"+cid,null).status);
 }
 @Test void validacaoUnicidadeEReferencias() throws Exception {
  assertEquals(400,enviar("POST","/api/clientes",Map.of("nome","","email","invalido","telefone","1")).status);
  String e=email();
  assertEquals(201,enviar("POST","/api/clientes",cliente(e)).status);
  assertEquals(409,enviar("POST","/api/clientes",cliente(e.toUpperCase())).status);
  long v=novoVeiculo(); var dados=new HashMap<>(ordem(v));
  dados.put("valorPecas",-1);assertEquals(400,enviar("POST","/api/ordens-servico",dados).status);
  dados.put("valorPecas",0.123);assertEquals(400,enviar("POST","/api/ordens-servico",dados).status);
  assertEquals(404,enviar("POST","/api/ordens-servico",ordem(999999)).status);
  assertEquals(404,enviar("GET","/api/clientes/999999",null).status);
  assertEquals(400,enviar("GET","/api/ordens-servico?status=INVALIDO",null).status);
  assertEquals(400,enviar("GET","/api/clientes/abc",null).status);
 }
 @Test void listagensFiltrosEDocumentacao() throws Exception {
  for(String rota:List.of("/api/clientes","/api/veiculos","/api/ordens-servico","/api/ordens-servico?status=ABERTA")) {
   var r=enviar("GET",rota,null);assertEquals(200,r.status);assertTrue(r.raw.startsWith("["));
  }
  var doc=enviar("GET","/v3/api-docs",null);
  assertEquals(200,doc.status,doc.raw);
  assertTrue(doc.raw.contains("/api/ordens-servico/{id}/status"));
  assertTrue(doc.raw.contains("valorTotal"));
 }
}
