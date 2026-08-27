package com.minhhao.novelscout.interaction;

import com.minhhao.novelscout.auth.CustomUserPrincipal;
import com.minhhao.novelscout.interaction.dto.CommentResponse;
import com.minhhao.novelscout.interaction.dto.CreateCommentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/public/novels/{novelId}/comments")
    public ResponseEntity<List<CommentResponse>> getNovelComments(@PathVariable Long novelId) {
        return ResponseEntity.ok(commentService.getNovelCommentsTree(novelId));
    }

    @GetMapping("/api/public/chapters/{chapterId}/comments")
    public ResponseEntity<List<CommentResponse>> getChapterComments(@PathVariable Long chapterId) {
        return ResponseEntity.ok(commentService.getChapterCommentsTree(chapterId));
    }

    @PostMapping("/api/novels/{novelId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            Authentication authentication,
            @PathVariable Long novelId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        CommentResponse response = commentService.addComment(principal.id(), novelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            Authentication authentication,
            @PathVariable Long commentId
    ) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        commentService.deleteComment(principal.id(), commentId);
        return ResponseEntity.noContent().build();
    }
}
