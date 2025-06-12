package org.example.wowelang_backend.board.service;


import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.board.domain.Board;
import org.example.wowelang_backend.board.domain.Post;
import org.example.wowelang_backend.board.dto.*;
import org.example.wowelang_backend.board.repository.BoardRepository;
import org.example.wowelang_backend.board.repository.PostRepository;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private final ImageService imageService;
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public PageResponseDTO<PostResponseDTO> getPostList(Long boardId, Pageable pageable) throws Exception {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException(BOARD_NOT_FOUND.getMessage()));

        Slice<Post> slice = postRepository.findAllByBoardAndIsDeleteFalse(board, pageable);

        // TODO: 커스텀 예외처리 필요합니다.
        if(slice.getContent().isEmpty() && pageable.getPageNumber()==0) {
            throw new Exception(POST_NO_CONTENT.getMessage());
        } else if (slice.getContent().isEmpty() && pageable.getPageNumber()>0) {
            throw new Exception(POST_NO_MORE.getMessage());
        }

        return PageResponseDTO.from(slice.map(PostResponseDTO::from));
    }

    @Transactional
    public Long createPost(Long boardId, PostCreateDTO postCreateDto, User user) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException(BOARD_NOT_FOUND.getMessage()));

        Post post = Post.createPost(postCreateDto, user, board);

        postRepository.save(post);

        // 이미지 확정 업로드 처리
        if(postCreateDto.getImageKeyList() != null && !postCreateDto.getImageKeyList().isEmpty()) {
            imageService.markImageAsPosted(postCreateDto.getImageKeyList());
        }

        return post.getId();
    }

    @Transactional
    public PostResponseDTO.PostDetailDTO getPost(Long postId) {

        postRepository.upPostViews(postId);

        Post afterUpdatePost = postRepository.findByIdAndIsDeleteFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException(POST_NOT_FOUND.getMessage()));

        return PostResponseDTO.PostDetailDTO.from(afterUpdatePost);
    }

    @Transactional
    public PostUpdateResponseDTO updatePost(Long postId, PostUpdateDTO postUpdateDto) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(POST_NOT_FOUND.getMessage()));

        post.updatePost(postUpdateDto.getTitle(), postUpdateDto.getContent());

        return PostUpdateResponseDTO.from(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findByIdAndIsDeleteFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException(POST_NOT_FOUND.getMessage()));

        post.softDeletePost();
    }
}
