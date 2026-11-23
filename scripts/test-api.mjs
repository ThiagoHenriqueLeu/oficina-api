import assert from 'node:assert/strict';
const base=process.env.API_URL || 'http://localhost:8081';
let checks=0;
async function call(method,path,body,status) {
 const r=await fetch(base+path,{method,headers:{'Content-Type':'application/json'},body:body?JSON.stringify(body):undefined});
 const text=await r.text(); assert.equal(r.status,status,method+' '+path+': '+text);
 checks++; console.log(method+' '+path+' -> '+r.status);
 return text?JSON.parse(text):null;
}
const marker='teste-'+Date.now();
let cpf=String(Math.floor(Math.random()*900000000)+100000000);
for(let n=9;n<11;n++){let soma=0;for(let i=0;i<n;i++)soma+=Number(cpf[i])*(n+1-i);let d=11-soma%11;cpf+=d>=10?0:d;}
let cliente,veiculo,ordem;
try {
 cliente=await call('POST','/api/clientes',{nome:'Cliente teste',email:marker+'@example.com',cpf,telefone:'11987654321'},201);
 await call('GET','/api/clientes',null,200);
 await call('GET','/api/clientes/'+cliente.id,null,200);
 await call('PUT','/api/clientes/'+cliente.id,{nome:'Cliente atualizado',email:marker+'@example.com',cpf,telefone:'11912345678'},200);
 await call('POST','/api/clientes',{nome:'Duplicado',email:marker.toUpperCase()+'@EXAMPLE.COM',cpf,telefone:'11912345678'},409);
 await call('POST','/api/clientes',{nome:'',email:'invalido',telefone:'1'},400);
 const placa='T'+String.fromCharCode(65+Math.floor(cliente.id/26)%26)+String.fromCharCode(65+cliente.id%26)+'9876';
 const vd={placa,marca:'Fiat',modelo:'Uno',ano:2018,clienteId:cliente.id};
 veiculo=await call('POST','/api/veiculos',vd,201);
 await call('GET','/api/veiculos',null,200);
 assert.equal((await call('GET','/api/veiculos?clienteId='+cliente.id,null,200))[0].id,veiculo.id);
 await call('GET','/api/veiculos/'+veiculo.id,null,200);
 await call('PUT','/api/veiculos/'+veiculo.id,{...vd,modelo:'Argo'},200);
 await call('POST','/api/veiculos',vd,409);
 await call('DELETE','/api/clientes/'+cliente.id,null,409);
 const od={veiculoId:veiculo.id,descricaoProblema:'Freios',quilometragem:10000,valorPecas:150.25,valorMaoObra:200};
 // Duas chamadas simultaneas: uma cria, a outra deve receber conflito.
 const respostas=await Promise.all([1,2].map(()=>fetch(base+'/api/ordens-servico',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(od)})));
 assert.deepEqual(respostas.map(r=>r.status).sort(),[201,409]);checks++;
 for(const r of respostas) {const data=await r.json(); if(r.status===201) ordem=data;}
 console.log('POST simultaneo: 201 + 409');
 assert.equal(ordem.valorTotal,350.25);assert.equal(ordem.status,'ABERTA');
 await call('GET','/api/ordens-servico',null,200);
 await call('GET','/api/ordens-servico/'+ordem.id,null,200);
 const filtro=await call('GET','/api/ordens-servico?veiculoId='+veiculo.id+'&status=ABERTA',null,200);
 assert.equal(filtro[0].id,ordem.id);
 await call('DELETE','/api/veiculos/'+veiculo.id,null,409);
 await call('DELETE','/api/ordens-servico/'+ordem.id,null,409);
 const atualizado=await call('PUT','/api/ordens-servico/'+ordem.id,{...od,valorPecas:180.50},200);
 assert.equal(atualizado.valorTotal,380.50);
 assert.equal((await call('GET','/api/ordens-servico/'+ordem.id,null,200)).valorTotal,380.50);
 await call('POST','/api/ordens-servico',{...od,valorPecas:-1},400);
 await call('POST','/api/ordens-servico',{...od,valorTotal:1},400);
 await call('PUT','/api/ordens-servico/'+ordem.id+'/status',{status:'CONCLUIDA'},409);
 await call('PUT','/api/ordens-servico/'+ordem.id+'/status',{status:'EM_ANDAMENTO'},200);
 await call('PUT','/api/ordens-servico/'+ordem.id+'/status',{status:'CONCLUIDA'},409);
 await call('PUT','/api/ordens-servico/'+ordem.id+'/status',{status:'CANCELADA'},200);
 await call('PUT','/api/ordens-servico/'+ordem.id,od,409);
 await call('DELETE','/api/ordens-servico/'+ordem.id,null,204);
 await call('GET','/api/ordens-servico/'+ordem.id,null,404);ordem=null;
 await call('DELETE','/api/veiculos/'+veiculo.id,null,204);veiculo=null;
 await call('DELETE','/api/clientes/'+cliente.id,null,204);cliente=null;
 const doc=await call('GET','/v3/api-docs',null,200);
 const operations=Object.values(doc.paths).flatMap(p=>Object.keys(p).filter(m=>['get','post','put','delete'].includes(m)));
 assert.equal(operations.length,16);
 const ui=await fetch(base+'/swagger-ui.html'); assert.equal(ui.status,200);assert.match(await ui.text(),/swagger/i);checks++;
 console.log('SUCESSO: '+checks+' verificacoes HTTP, CRUD e regras; dados temporarios removidos.');
} finally {
 if(ordem) {await fetch(base+'/api/ordens-servico/'+ordem.id+'/status',{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({status:'CANCELADA'})});await fetch(base+'/api/ordens-servico/'+ordem.id,{method:'DELETE'});}
 if(veiculo) await fetch(base+'/api/veiculos/'+veiculo.id,{method:'DELETE'});
 if(cliente) await fetch(base+'/api/clientes/'+cliente.id,{method:'DELETE'});
}
