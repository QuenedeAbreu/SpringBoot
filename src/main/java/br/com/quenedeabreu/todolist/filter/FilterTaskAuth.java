package br.com.quenedeabreu.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.quenedeabreu.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository IUserRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // Verificando a roda da requisição
    var servletPath = request.getServletPath();
    var servletMethod = request.getMethod();
    if (servletPath.equals("/users/") && servletMethod.equals("POST")) {
      filterChain.doFilter(request, response);
    } else {
      // Pegar a autenticação (usuario e senha)
      var authorization = request.getHeader("Authorization");
      // Remover a palavra Basic do header de autorização
      var authEncoder = authorization.substring("Basic".length()).trim();
      // Decodifica a autorização Base64
      byte[] authDecode = Base64.getDecoder().decode(authEncoder);
      // Transforma em string o resultado do base 64
      var authString = new String(authDecode);
      // Separa o usuário e senha que vem junto dividido por :
      String[] crendentials = authString.split(":");
      String username = crendentials[0];
      String password = crendentials[1];

      // Validar se existe o usuario no banco de dados;
      var userResponse = this.IUserRepository.findByUsername(username);
      if (userResponse == null) {
        response.sendError(401, "Usuário ou Senha invalido!");
      } else {
        // Validar senha
        var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), userResponse.getPassword());
        if (passwordVerify.verified) {
          request.setAttribute("idUser", userResponse.getIdUser());
          filterChain.doFilter(request, response);
        } else {
          response.sendError(401, "Usuário ou Senha invalido!");
        }

      }
    }
  }
}