package com.example.demo.api;

import com.example.demo.model.Note;
import com.example.demo.repo.NoteRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NotesController {

  private final NoteRepository repo;

  public NotesController(NoteRepository repo) {
    this.repo = repo;
  }

  @PostMapping
  public NoteResponse create(@RequestBody @Valid CreateNoteRequest req) {
    Note created = repo.create(req.body());
    return new NoteResponse(created.id(), created.body());
  }

  @GetMapping
  public List<NoteResponse> list() {
    return repo.findAll().stream().map(n -> new NoteResponse(n.id(), n.body())).toList();
  }
}
