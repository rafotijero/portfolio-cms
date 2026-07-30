package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.PostDetailDto;
import dev.rafotijero.cms.api.dto.PostSummaryDto;
import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Post;
import dev.rafotijero.cms.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<PostSummaryDto> findPublished(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return postRepository.findByStatus(ContentStatus.PUBLISHED, pageable)
                .map(PostSummaryDto::from);
    }

    public PostDetailDto findPublishedBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .filter(p -> p.getStatus() == ContentStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return PostDetailDto.from(post);
    }
}
