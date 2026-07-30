package dev.rafotijero.cms.api;

import dev.rafotijero.cms.api.dto.PostDetailDto;
import dev.rafotijero.cms.api.dto.PostSummaryDto;
import dev.rafotijero.cms.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Page<PostSummaryDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return postService.findPublished(page, size);
    }

    @GetMapping("/{slug}")
    public PostDetailDto detail(@PathVariable String slug) {
        return postService.findPublishedBySlug(slug);
    }
}
