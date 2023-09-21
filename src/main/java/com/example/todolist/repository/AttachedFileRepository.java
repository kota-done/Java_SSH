package com.example.todolist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.todolist.entity.AttachedFile;

@Repository
public interface AttachedFileRepository extends JpaRepository<AttachedFile, Integer> {
	//todoIdをキーに設定して検索
	List<AttachedFile> findByTodoIdOrderById(Integer todoId);
}
