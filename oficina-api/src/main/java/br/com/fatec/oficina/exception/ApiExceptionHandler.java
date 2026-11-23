package br.com.fatec.oficina.exception;
import java.time.LocalDateTime;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
@RestControllerAdvice
public class ApiExceptionHandler {
 public record ErroApi(LocalDateTime data,int status,String mensagem,String caminho,Map<String,String> campos) {}
 private ResponseEntity<ErroApi> erro(int status,String mensagem,HttpServletRequest req,Map<String,String> campos) {
  return ResponseEntity.status(status).body(new ErroApi(LocalDateTime.now(),status,mensagem,req.getRequestURI(),campos));
 }
 @ExceptionHandler(NaoEncontradoException.class)
 public ResponseEntity<ErroApi> naoEncontrado(NaoEncontradoException ex,HttpServletRequest req) {
  return erro(404,ex.getMessage(),req,Map.of());
 }
 @ExceptionHandler(RegraNegocioException.class)
 public ResponseEntity<ErroApi> regra(RegraNegocioException ex,HttpServletRequest req) {
  return erro(409,ex.getMessage(),req,Map.of());
 }
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<ErroApi> validacao(MethodArgumentNotValidException ex,HttpServletRequest req) {
  Map<String,String> campos=new LinkedHashMap<>();
  ex.getBindingResult().getFieldErrors().forEach(e->campos.put(e.getField(),e.getDefaultMessage()));
  return erro(400,"Dados invalidos",req,campos);
 }
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
 public ResponseEntity<ErroApi> formato(Exception ex,HttpServletRequest req) {
  return erro(400,"JSON, tipo de parametro ou valor de status invalido",req,Map.of());
 }
 @ExceptionHandler(DataIntegrityViolationException.class)
 public ResponseEntity<ErroApi> integridade(DataIntegrityViolationException ex,HttpServletRequest req) {
  return erro(409,"Operacao viola unicidade ou relacionamento entre registros",req,Map.of());
 }
}