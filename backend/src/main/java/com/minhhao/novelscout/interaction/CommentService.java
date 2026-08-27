package com.minhhao.novelscout.interaction;

import com.minhhao.novelscout.catalog.Chapter;
import com.minhhao.novelscout.catalog.ChapterRepository;
import com.minhhao.novelscout.catalog.Novel;
import com.minhhao.novelscout.catalog.NovelRepository;
import com.minhhao.novelscout.common.api.ApiException;
import com.minhhao.novelscout.interaction.dto.CommentResponse;
import com.minhhao.novelscout.interaction.dto.CreateCommentRequest;
import com.minhhao.novelscout.user.RoleName;
import com.minhhao.novelscout.user.User;
import com.minhhao.novelscout.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CommentService {

    private final NovelCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final UserInteractionRepository userInteractionRepository;

    public CommentService(
            NovelCommentRepository commentRepository,
            UserRepository userRepository,
            NovelRepository novelRepository,
            ChapterRepository chapterRepository,
            UserInteractionRepository userInteractionRepository
    ) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.userInteractionRepository = userInteractionRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getNovelCommentsTree(Long novelId) {
        List<NovelComment> allComments = commentRepository.findByNovelIdOrderByCreatedAtAsc(novelId);
        return buildTree(allComments);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getChapterCommentsTree(Long chapterId) {
        List<NovelComment> chapterComments = commentRepository.findByChapterIdOrderByCreatedAtAsc(chapterId);
        return buildTree(chapterComments);
    }

    private List<CommentResponse> buildTree(List<NovelComment> comments) {
        Map<Long, CommentResponse> dtoMap = new HashMap<>();
        List<CommentResponse> rootComments = new ArrayList<>();

        for (NovelComment c : comments) {
            CommentResponse dto = CommentResponse.from(c);
            dtoMap.put(c.getId(), dto);
            if (c.getParent() == null) {
                rootComments.add(dto);
            }
        }

        for (NovelComment c : comments) {
            if (c.getParent() != null) {
                CommentResponse parentDto = dtoMap.get(c.getParent().getId());
                CommentResponse currentDto = dtoMap.get(c.getId());
                if (parentDto != null && currentDto != null) {
                    parentDto.replies().add(currentDto);
                }
            }
        }

        Collections.reverse(rootComments);
        return rootComments;
    }

    @Transactional
    public CommentResponse addComment(Long userId, Long novelId, CreateCommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Bạn cần đăng nhập để bình luận"));

        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOVEL_NOT_FOUND", "Không tìm thấy tác phẩm"));

        Chapter chapter = null;
        if (request.chapterId() != null) {
            chapter = chapterRepository.findById(request.chapterId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "CHAPTER_NOT_FOUND", "Không tìm thấy chương"));
            if (!chapter.getNovel().getId().equals(novelId)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CHAPTER", "Chương này không thuộc tác phẩm");
            }
        }

        NovelComment parentComment = null;
        if (request.parentId() != null) {
            parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "PARENT_NOT_FOUND", "Bình luận cha không tồn tại"));
            if (!parentComment.getNovel().getId().equals(novelId)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PARENT", "Bình luận cha không thuộc tác phẩm này");
            }
            // Nếu trả lời bình luận cha đã có chapter_id thì kế thừa chapter_id đó
            if (chapter == null && parentComment.getChapter() != null) {
                chapter = parentComment.getChapter();
            }
        }

        NovelComment comment = new NovelComment(novel, chapter, user, parentComment, request.content().trim());
        NovelComment saved = commentRepository.save(comment);

        // Ghi nhận tương tác làm giàu ma trận Gợi ý Lai (Hybrid Recommendation Engine)
        UserInteraction interaction = new UserInteraction(user, novel, "COMMENT", new BigDecimal("2.00"));
        userInteractionRepository.save(interaction);

        return CommentResponse.from(saved);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Bạn cần đăng nhập"));

        NovelComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", "Bình luận không tồn tại"));

        boolean isOwner = comment.getUser().getId().equals(userId);
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền xóa bình luận này");
        }

        commentRepository.delete(comment);
    }
}
