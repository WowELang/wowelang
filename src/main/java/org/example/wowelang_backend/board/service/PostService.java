package org.example.wowelang_backend.board.service;

import org.example.wowelang_backend.board.dto.FreeBoardPostListDTO;
import org.example.wowelang_backend.board.repository.PostRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private final PostRespository postRespository;

    public PostService(PostRespository postRespository) {
        this.postRespository = postRespository;
    }

//    public List<FreeBoardPostListDTO> getFreeBoardPostList() {
//
//        return postRespository.findAllByBoard().stream()
//                .map(FreeBoardPostListDTO::of).
//                toList();
//
//    }
}