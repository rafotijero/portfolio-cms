package dev.rafotijero.cms.service;

import dev.rafotijero.cms.api.dto.PostDetailDto;
import dev.rafotijero.cms.api.dto.PostRequest;
import dev.rafotijero.cms.api.dto.PostSummaryDto;
import dev.rafotijero.cms.domain.ContentStatus;
import dev.rafotijero.cms.domain.Post;
import dev.rafotijero.cms.domain.Tag;
import dev.rafotijero.cms.repository.PostRepository;
import dev.rafotijero.cms.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository postRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
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

    public Page<PostSummaryDto> findAllAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable).map(PostSummaryDto::from);
    }

    public PostDetailDto findByIdAdmin(UUID id) {
        return PostDetailDto.from(getOrThrow(id));
    }

    @Transactional
    public PostDetailDto create(PostRequest request) {
        Post post = new Post();
        apply(post, request);
        return PostDetailDto.from(postRepository.save(post));
    }

    @Transactional
    public PostDetailDto update(UUID id, PostRequest request) {
        Post post = getOrThrow(id);
        apply(post, request);
        return PostDetailDto.from(postRepository.save(post));
    }

    @Transactional
    public void delete(UUID id) {
        Post post = getOrThrow(id);
        postRepository.delete(post);
    }

    private Post getOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void apply(Post post, PostRequest request) {
        post.setTitle(request.title());
        post.setSlug(request.slug());
        post.setStatus(request.status());
        post.setSummary(request.summary());
        post.setContent(request.content());
        post.setCoverImageUrl(request.coverImageUrl());
        post.setPublishedAt(request.publishedAt());
        post.setTags(resolveTags(request.tagIds()));
    }

    private Set<Tag> resolveTags(Set<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        if (tags.size() != tagIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o mas tagIds no existen");
        }

        return tags;
    }
}
