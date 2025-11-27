package com.project.login.controller.admin;

import com.project.login.convert.SearchConvert;
import com.project.login.model.dto.search.NoteSearchDTO;
import com.project.login.model.request.search.NoteSearchRequest;
import com.project.login.model.response.StandardResponse;
import com.project.login.model.vo.NoteSearchVO;
import com.project.login.service.search.AdminSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin Search", description = "Admin search notes using Elasticsearch")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/search")
public class AdminSearchController {

    private final AdminSearchService adminSearchService;
    @Qualifier("searchConvert")
    private final SearchConvert convert;

    @Operation(summary = "Search notes by keyword for admin")
    @PostMapping("/notes")
    public ResponseEntity<StandardResponse<List<NoteSearchVO>>> searchNotes(
            @Valid @RequestBody NoteSearchRequest request
    ) {
        NoteSearchDTO dto = convert.toSearchDTO(request);
        List<NoteSearchVO> results = adminSearchService.searchNotes(dto);
        return ResponseEntity.ok(StandardResponse.success(results));
    }
}
