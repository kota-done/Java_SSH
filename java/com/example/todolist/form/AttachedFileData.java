package com.example.todolist.form;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttachedFileData {
	//id
	private Integer id;
	//ファイル名
	private String fileName;
	//メモ
	private String note;
	//新規タブで開くかどうか
	private boolean openInNewTab;
}
