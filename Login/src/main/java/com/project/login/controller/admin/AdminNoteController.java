package com.project.login.controller.admin;

import com.project.login.model.request.note.NoteDeleteRequest;
import com.project.login.model.entity.NoteEntity;
import com.project.login.model.response.admin.NoteAdminSummary;
import com.project.login.repository.NoteRepository;
import com.project.login.service.noting.NoteService;
import com.project.login.model.response.StandardResponse;
import com.project.login.convert.NoteConvert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Admin Notes", description = "Admin note listing")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/notes")
public class AdminNoteController {

    private final NoteRepository noteRepository;
    private final NoteService noteService;
    @Qualifier("noteConvert")
    private final NoteConvert convert;

    @Operation(summary = "Query notes by author and tag")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)))
    })
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<StandardResponse<?>> list(@RequestParam(value = "author", required = false) String author,
                                  @RequestParam(value = "tag", required = false) String tag) {
        List<NoteEntity> notes = noteRepository.searchForAdmin(author, tag);
        List<NoteAdminSummary> data = notes.stream().map(NoteAdminSummary::from).collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(data));
    }

    @Operation(summary = "Delete note")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<?>> delete(@PathVariable("id") Long id) {
        NoteDeleteRequest req = new NoteDeleteRequest();
        req.setNoteId(id);
        noteService.deleteNote(convert.toDeleteDTO(req));
        return ResponseEntity.ok(StandardResponse.success("删除成功", null));
    }
}
