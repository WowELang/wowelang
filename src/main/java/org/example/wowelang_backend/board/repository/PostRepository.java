package org.example.wowelang_backend.board.repository;

import org.example.wowelang_backend.board.domain.Board;
import org.example.wowelang_backend.board.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findAllByBoard(Board board);
}
