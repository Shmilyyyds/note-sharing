package com.project.login.controller.admin;

import com.project.login.model.entity.CommentEntity;
import com.project.login.model.response.admin.CommentAdminVO;
import com.project.login.repository.CommentRepository;
import com.project.login.model.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Admin Comments", description = "Admin comment management")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/comments")
public class AdminCommentController {

    private final CommentRepository commentRepository;

    @Operation(summary = "Query comments by date and publisher")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)))
    })
    @GetMapping
    public ResponseEntity<StandardResponse<?>> list(@RequestParam(value = "date", required = false)
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam(value = "publisher", required = false) String publisher) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (date != null) {
            start = date.atStartOfDay();
            end = date.plusDays(1).atStartOfDay();
        }
        List<CommentEntity> list = commentRepository.search(publisher, start, end);
        List<CommentAdminVO> data = list.stream().map(CommentAdminVO::from).collect(Collectors.toList());
        return ResponseEntity.ok(StandardResponse.success(data));
    }

    @Operation(summary = "Delete a comment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<?>> delete(@PathVariable Long id) {
        commentRepository.deleteById(id);
        return ResponseEntity.ok(StandardResponse.success("删除成功", null));
    }
}