package com.example.agent.repository;

import com.example.agent.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByStatus(Todo.Status status);

    List<Todo> findByPriority(Todo.Priority priority);

    @Query("SELECT t FROM Todo t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Todo> searchByKeyword(@Param("keyword") String keyword);

    List<Todo> findByOrderByPriorityDescCreatedAtAsc();
}
