package com.example.todolist.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attached_file")
@Data
@NoArgsConstructor
public class AttachedFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "todo_id")
	private Integer todoId;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "create_time")
	private String createTime;

	@Column(name = "note")
	private String note;
}
