package com.cos.blogapp.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cos.blogapp.domain.board.Board;
import com.cos.blogapp.domain.board.BoardRepository;
import com.cos.blogapp.domain.user.User;
import com.cos.blogapp.handler.exception.MyAsyncNotFoundException;
import com.cos.blogapp.handler.exception.MyNotFoundException;
import com.cos.blogapp.web.dto.BoardSaveReqDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoardService {
	
	
	// 생성자 주입 (DI)
	private final BoardRepository boardRepository;
	
	@Transactional(rollbackFor = MyAsyncNotFoundException.class)
	public void 게시글수정(int id, User principal, BoardSaveReqDto dto) {
		Board boardEntity = boardRepository.findById(id)
				.orElseThrow(() -> new MyAsyncNotFoundException("해당 글을 찾을 수가 없습니다."));
		
		if (principal.getId() != boardEntity.getUser().getId()) {
			throw new MyAsyncNotFoundException("해당 게시글의 주인이 아닙니다.");
		}
		
		// 영속화된 데이터를 변경하면 !!						
		boardEntity.setTitle(dto.getTitle());
		boardEntity.setContent(dto.getContent());
	} // 트랜잭션 종료 (더티체킹)
	
	public Board 게시글수정페이지이동(int id) {
		// 게시글 정보를 가지고 가야함.
		Board boardEntity = boardRepository.findById(id)
				.orElseThrow(()-> new MyNotFoundException(id + "번호의 게시글을 찾을 수 없습니다."));
		return boardEntity;
	}
	
	// 트랜잭션 어노테이션 (트랜잭션을 시작하는 것)
	// rollbackFor (함수내부에 하나의 write라도 실패하면 전체를 rollback 하는 것)
	// 주의 : RuntimeException을 던져야 동작한다.
	@Transactional(rollbackFor = MyAsyncNotFoundException.class)
	public void 게시글삭제(int id, User principal) {
		// 권한이 있는 사람만 함수 접근 가능(principal.id == {id})
		Board boardEntity = boardRepository.findById(id)
				.orElseThrow(() -> new MyAsyncNotFoundException("해당 글을 찾을 수가 없습니다."));
		if (principal.getId() != boardEntity.getUser().getId()) {
			throw new MyAsyncNotFoundException("해당글을 삭제할 권한이 없습니다.");
		}
		try {
			boardRepository.deleteById(id);
		} catch (Exception e) {
			throw new MyAsyncNotFoundException(id + "를 찾을 수 없어서 삭제할 수 없어요.");
		}
	} // 트랜잭션 종료
	
	public Board 게시글상세보기(int id) {
		Board boardEntity = boardRepository.findById(id).orElseThrow(() -> new MyNotFoundException(id + " 못찾았어요"));
		return boardEntity;
	}
	
	@Transactional(rollbackFor = MyNotFoundException.class)
	public void 게시글등록(BoardSaveReqDto dto, User principal) {
		boardRepository.save(dto.toEntity(principal));
	} // 트랜잭션 종료
	
	public Page<Board> 게시글목록보기(Integer page) {
		PageRequest pageRequest = PageRequest.of(page, 3, Sort.by(Sort.Direction.DESC, "id"));
		Page<Board> boardsEntity = boardRepository.findAll(pageRequest);
		return boardsEntity;
	}
}
