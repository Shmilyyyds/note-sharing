package com.project.login.controller;

import com.project.login.model.response.StandardResponse;
import com.project.login.model.vo.NoteSearchVO;
import com.project.login.model.vo.qa.QuestionVO;
import com.project.login.service.favorites.FavoritesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Favorites", description = "User favorites management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/favorites")
public class FavoritesController {

    private final FavoritesService favoritesService;

    @Operation(summary = "Get user's favorite notes")
    @GetMapping("/notes")
    public StandardResponse<List<NoteSearchVO>> getFavoriteNotes(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId) {
        List<NoteSearchVO> notes = favoritesService.getFavoriteNotes(userId);
        return StandardResponse.success(notes);
    }

    @Operation(summary = "Get user's favorite questions")
    @GetMapping("/questions")
    public StandardResponse<List<QuestionVO>> getFavoriteQuestions(
            @Parameter(description = "User ID", required = true)
            @RequestParam Long userId) {
        List<QuestionVO> questions = favoritesService.getFavoriteQuestions(userId);
        return StandardResponse.success(questions);
    }
}
