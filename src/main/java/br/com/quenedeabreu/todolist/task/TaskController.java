package br.com.quenedeabreu.todolist.task;

import org.springframework.web.bind.annotation.RestController;

import br.com.quenedeabreu.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/tasks")
public class TaskController {
  @Autowired
  private ITaskRepository taskRepository;

  @GetMapping("/")
  public List<TaskModel> listAll() {
    return this.taskRepository.findAll();
  }

  @GetMapping("/user/")
  public List<TaskModel> listForUser(HttpServletRequest request) {
    var user_id_request = request.getAttribute("idUser");
    System.out.println(request.getAttribute("idUser"));
    var tasks = this.taskRepository.findByIdUser((UUID) user_id_request);
    return tasks;

  }

  @PostMapping("/")
  public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var user_id_request = request.getAttribute("idUser");
    taskModel.setIdUser((UUID) user_id_request);

    var currenteDate = LocalDateTime.now();
    if (currenteDate.isAfter(taskModel.getStartAt()) || currenteDate.isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Data de inicio ou de termino deve ser maior que a data atual!");
    }
    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Data de inicio deve ser menor que a data de termino!");
    }
    var task = this.taskRepository.save(taskModel);
    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @PutMapping("/{idTask}/")
  public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request,
      @PathVariable UUID idTask) {
    var idUser = request.getAttribute("idUser");
    var task = this.taskRepository.findById(idTask).orElse(null);

    if (task == null) {
      ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("A task não existe!");
    } else if (!task.getIdUser().equals(idUser)) {
      ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Você não é dono desta Tesk!");
    }

    Utils.copyNonNullProperties(taskModel, task);

    // taskModel.setIdUser((UUID) idUser);
    // taskModel.setIdTask(idTask);
    var taskUpdate = this.taskRepository.save(task);

    return ResponseEntity.status(HttpStatus.OK).body(taskUpdate);
  }
}
